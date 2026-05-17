import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class GamePanel extends JPanel implements KeyListener {

    // Board constants
    private static final int ROWS = 7;
    private static final int W = 40;
    private static final int H = 20;
    private static final int D = 25;
    private static final int CENTER_X = 400;
    private static final int TOP_Y = 60;
    private static final int TOTAL_TILES = 28;
    private static final String SOUND_PATH = "/Users/student/Desktop/FANCY Qbert/";

    // Game state
    private boolean[][] visited = new boolean[ROWS][];
    private int playerRow = 0, playerCol = 0;
    private int lives = 3;
    private int score = 0;
    private int visitedCount = 0;
    private boolean gameOver = false;
    private boolean won = false;
    private int level = 1;

    // Pixel art character (student's custom feature)
    private Color[][] character;

    // Player jump animation
    private boolean isJumping = false;
    private float jumpT = 0f;
    private int startX, startY, endX, endY;
    private int pendingRow, pendingCol;
    private javax.swing.Timer jumpTimer;

    // Player fall animation
    private boolean isFalling = false;
    private boolean isPlayerFall = false;
    private int fallX, fallY;
    private javax.swing.Timer fallTimer;

    // Coily — starts as egg, hatches into snake that chases player
    private int coilyRow = 0, coilyCol = 0;
    private int coilyState = 0; // 0 = egg, 1 = snake
    private int coilyBounces = 0;
    private boolean coilyJumping = false;
    private float coilyJumpT = 0f;
    private int coilyStartX, coilyStartY, coilyEndX, coilyEndY;
    private int coilyPendingRow, coilyPendingCol;
    private javax.swing.Timer coilyMoveTimer;
    private javax.swing.Timer coilyJumpTimer;

    // Slick — moves randomly downward, reverts visited tiles back to unvisited
    private int slickRow = -1, slickCol = -1;
    private boolean slickActive = false;
    private boolean slickJumping = false;
    private float slickJumpT = 0f;
    private int slickStartX, slickStartY, slickEndX, slickEndY;
    private int slickPendingRow, slickPendingCol;
    private javax.swing.Timer slickMoveTimer;
    private javax.swing.Timer slickJumpTimer;
    private javax.swing.Timer slickSpawnTimer;

    // Flying discs — two platforms beside pyramid, teleport player to top
    private boolean disc1Active = true;
    private boolean disc2Active = true;

    // Coily delayed spawn — false until 3 seconds after game/level start
    private boolean coilyStarted = false;

    // Speech bubble shown when a monster hits the player
    private boolean showBubble = false;
    private String bubbleText = "@#$%!";
    private int bubbleX, bubbleY;
    private javax.swing.Timer bubbleTimer;

    // Color change in stages for each level
    private static final Color[] VISITED_COLORS = {
        new Color(255, 200, 0),   // level 1 — yellow
        new Color(220, 60,  60),  // level 2 — red
        new Color(60,  120, 220), // level 3 — blue
        new Color(180, 60,  220), // level 4 — purple
        new Color(255, 140, 0)    // level 5 — orange
    };
    private static final Color[] UNVISITED_COLORS = {
        new Color(0,   140, 130), // level 1 — teal
        new Color(30,  30,  80),  // level 2 — dark navy
        new Color(0,   100, 60),  // level 3 — dark green
        new Color(80,  20,  80),  // level 4 — dark purple
        new Color(60,  30,  0)    // level 5 — dark brown
    };


    public GamePanel(Color[][] character, String customBubbleText) {
        this.character = character;
        if (customBubbleText != null && !customBubbleText.isEmpty()) {
            bubbleText = customBubbleText.substring(0, Math.min(40, customBubbleText.length()));
        }
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 580));
        setFocusable(true);
        addKeyListener(this);
        initBoard();
        initTimers();
    }

    // Sets up the pyramid tile grid and marks the starting tile as visited
    private void initBoard() {
        for (int r = 0; r < ROWS; r++) {
            visited[r] = new boolean[r + 1];
        }
        visited[0][0] = true;
        visitedCount = 1;
    }

    // Creates all game timers (player jump, fall, enemies)
    private void initTimers() {

        // Advances player jump animation each frame
        jumpTimer = new javax.swing.Timer(16, e -> {
            jumpT += 0.1f;
            if (jumpT >= 1f) {
                jumpT = 1f;
                jumpTimer.stop();
                isJumping = false;
                playerRow = pendingRow;
                playerCol = pendingCol;
                landOnTile();
            }
            repaint();
        });

        // Moves player downward during fall, then triggers loseLife or enemy reset
        fallTimer = new javax.swing.Timer(16, e -> {
            fallY += 8;
            if (fallY > getHeight()) {
                fallTimer.stop();
                isFalling = false;
                if (isPlayerFall) loseLife();
                else resetCoily();
            }
            repaint();
        });

        // Coily decides its next move — random when egg, chasing when snake
        int coilySpeed = Math.max(200, 600 - (level - 1) * 80);
        coilyMoveTimer = new javax.swing.Timer(coilySpeed, e -> {
            if (gameOver || won || coilyJumping) return;
            int newR = coilyRow + 1;
            int newC;
            if (coilyState == 0) {
                // Egg: bounce randomly
                newC = coilyCol + (int)(Math.random() * 2);
                coilyBounces++;
                if (coilyBounces >= 5) coilyState = 1; // hatch into snake
            } else {
                // Snake: move toward player column
                if (playerCol < coilyCol) newC = coilyCol - 1;
                else if (playerCol > coilyCol) newC = coilyCol + 1;
                else newC = coilyCol;
            }
            if (newR >= ROWS || newC < 0 || newC > newR) {
                resetCoily();
                return;
            }
            coilyStartX = tileX(coilyRow, coilyCol);
            coilyStartY = tileY(coilyRow, coilyCol);
            coilyEndX   = tileX(newR, newC);
            coilyEndY   = tileY(newR, newC);
            coilyPendingRow = newR;
            coilyPendingCol = newC;
            coilyJumping = true;
            coilyJumpT = 0f;
            coilyJumpTimer.start();
        });

        // Advances Coily's jump animation; checks player contact on landing
        coilyJumpTimer = new javax.swing.Timer(16, e -> {
            coilyJumpT += 0.1f;
            if (coilyJumpT >= 1f) {
                coilyRow = coilyPendingRow;
                coilyCol = coilyPendingCol;
                coilyJumpT = 1f;
                coilyJumping = false;
                coilyJumpTimer.stop();
                if (coilyRow == playerRow && coilyCol == playerCol && !isFalling) {
                    playerHit();
                }
            }
            repaint();
        });

        // Slick decides its next move — always random downward
        slickMoveTimer = new javax.swing.Timer(800, e -> {
            if (!slickActive || gameOver || won || slickJumping) return;
            int newR = slickRow + 1;
            int newC = slickCol + (int)(Math.random() * 2);
            if (newR >= ROWS || newC < 0 || newC > newR) {
                slickActive = false;
                return;
            }
            slickStartX = tileX(slickRow, slickCol);
            slickStartY = tileY(slickRow, slickCol);
            slickEndX   = tileX(newR, newC);
            slickEndY   = tileY(newR, newC);
            slickPendingRow = newR;
            slickPendingCol = newC;
            slickJumping = true;
            slickJumpT = 0f;
            slickJumpTimer.start();
        });

        // Advances Slick's jump animation; reverts tile on landing
        slickJumpTimer = new javax.swing.Timer(16, e -> {
            slickJumpT += 0.1f;
            if (slickJumpT >= 1f) {
                slickRow = slickPendingRow;
                slickCol = slickPendingCol;
                slickJumpT = 1f;
                slickJumping = false;
                slickJumpTimer.stop();
                if (visited[slickRow][slickCol]) {
                    visited[slickRow][slickCol] = false;
                    visitedCount--;
                }
                if (slickRow == playerRow && slickCol == playerCol && !isFalling) {
                    playerHit();
                }
            }
            repaint();
        });

        // Spawns Slick every 8 seconds if not already on board
        slickSpawnTimer = new javax.swing.Timer(5000, e -> {
            if (!slickActive && !gameOver && !won) {
                slickRow = 0;
                slickCol = 0;
                slickActive = true;
                slickMoveTimer.start();
            }
        });

        // Shows bubble for 3 seconds then deducts a life
        bubbleTimer = new javax.swing.Timer(3000, e -> {
            bubbleTimer.stop();
            showBubble = false;
            loseLife();
        });
        bubbleTimer.setRepeats(false);

        startCoilyDelayed();
        slickSpawnTimer.start();
    }

    // Waits 3 seconds then spawns Coily — called on game start, restart, and next level
    private void startCoilyDelayed() {
        coilyStarted = false;
        coilyMoveTimer.stop();
        javax.swing.Timer delay = new javax.swing.Timer(3000, e -> {
            resetCoily();
            coilyStarted = true;
            coilyMoveTimer.start();
        });
        delay.setRepeats(false);
        delay.start();
    }

    // Resets Coily back to the top as a new egg
    private void resetCoily() {
        coilyRow = 0;
        coilyCol = 0;
        coilyState = 0;
        coilyBounces = 0;
        coilyJumping = false;
    }

    // Converts grid row/col to screen pixel X (isometric formula)
    private int tileX(int row, int col) {
        return CENTER_X + (2 * col - row) * W;
    }

    // Converts grid row/col to screen pixel Y
    private int tileY(int row, int col) {
        return TOP_Y + H + row * (H + D);
    }

    private Polygon topFace(int cx, int cy) {
        return new Polygon(
            new int[]{cx,     cx + W, cx,     cx - W},
            new int[]{cy - H, cy,     cy + H, cy    },
            4
        );
    }

    private Polygon leftFace(int cx, int cy) {
        return new Polygon(
            new int[]{cx - W, cx,         cx,         cx - W    },
            new int[]{cy,     cy + H,     cy + H + D, cy + D    },
            4
        );
    }

    private Polygon rightFace(int cx, int cy) {
        return new Polygon(
            new int[]{cx,     cx + W,  cx + W,  cx        },
            new int[]{cy + H, cy,      cy + D,  cy + H + D},
            4
        );
    }

    // Draws one isometric cube with top, left, and right faces
    private void drawTile(Graphics g, int row, int col) {
        int cx = tileX(row, col);
        int cy = tileY(row, col);
        boolean v = visited[row][col];
        // Color loop
        g.setColor(v ? VISITED_COLORS[(level - 1) % VISITED_COLORS.length] : UNVISITED_COLORS[(level - 1) % UNVISITED_COLORS.length]);
        g.fillPolygon(topFace(cx, cy));
        g.setColor(Color.BLACK);
        g.drawPolygon(topFace(cx, cy));
        g.setColor(new Color(30, 30, 80));
        g.fillPolygon(leftFace(cx, cy));
        g.setColor(Color.BLACK);
        g.drawPolygon(leftFace(cx, cy));
        g.setColor(new Color(20, 20, 55));
        g.fillPolygon(rightFace(cx, cy));
        g.setColor(Color.BLACK);
        g.drawPolygon(rightFace(cx, cy));
    }

    // Draws player's pixel art sprite centered at (cx, cy) — student's custom feature
    private void drawCharacter(Graphics g, int cx, int cy) {
        if (character == null) {
            g.setColor(new Color(225, 140, 0));
            g.fillOval(cx - 15, cy - 15, 30, 30);
            return;
        }
        for (int r = 0; r < character.length; r++) {
            for (int c = 0; c < character[r].length; c++) {
                if (character[r][c] != null) {
                    g.setColor(character[r][c]);
                    g.fillRect(cx - 20 + c * 2, cy - 20 + r * 2, 2, 2);
                }
            }
        }
    }

    // Draws the two flying discs beside the pyramid
    private void drawDiscs(Graphics g) {
        // Row 3 is the 4th row — discs sit just off the left and right edges of that row
        if (disc1Active) {
            int dx = tileX(3, 0) - W - 50;
            int dy = tileY(3, 0);
            drawSingleDisc(g, dx, dy);
        }
        if (disc2Active) {
            int dx = tileX(3, 3) + W + 50;
            int dy = tileY(3, 3);
            drawSingleDisc(g, dx, dy);
        }
    }

    private void drawSingleDisc(Graphics g, int dx, int dy) {
        int thick = 7;               // how many pixels of edge are visible
        int ex = dx - 22, ey = dy - 16; // top-left corner of the top ellipse

        // Bottom ellipse — same shape as top but shifted down by thickness.
        // The top face will cover the overlapping upper portion, leaving only
        // the bottom rim visible, which is what gives the 3-D depth.
        g.setColor(new Color(0, 140, 130));
        g.fillOval(ex, ey + thick, 44, 18);

        // Top face — four coloured quadrants
        g.setColor(Color.YELLOW);
        g.fillArc(ex, ey, 44, 18, 0, 90);
        g.setColor(new Color(0, 190, 80));
        g.fillArc(ex, ey, 44, 18, 90, 90);
        g.setColor(Color.RED);
        g.fillArc(ex, ey, 44, 18, 180, 90);
        g.setColor(new Color(80, 120, 255));
        g.fillArc(ex, ey, 44, 18, 270, 90);

        // Top face outline
        g.setColor(Color.BLACK);
        g.drawOval(ex, ey, 44, 18);

        // Side edge lines — connect the leftmost/rightmost points of the two ellipses
        g.setColor(Color.BLACK);
        g.drawLine(ex,      ey + 9, ex,      ey + 9 + thick); // left edge
        g.drawLine(ex + 44, ey + 9, ex + 44, ey + 9 + thick); // right edge
        // Only draw the bottom half of the lower ellipse (top half is hidden)
        g.drawArc(ex, ey + thick, 44, 18, 180, 180);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw pyramid
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c <= r; c++) {
                drawTile(g, r, c);
            }
        }

        // Draw flying discs
        drawDiscs(g);

        // Draw Slick (green circle)
        if (slickActive) {
            int sx, sy;
            if (slickJumping) {
                sx = (int)(slickStartX + (slickEndX - slickStartX) * slickJumpT);
                sy = (int)(slickStartY + (slickEndY - slickStartY) * slickJumpT - 25 * 4 * slickJumpT * (1 - slickJumpT));
            } else {
                sx = tileX(slickRow, slickCol);
                sy = tileY(slickRow, slickCol);
            }
            g.setColor(new Color(0, 200, 80));
            g.fillOval(sx - 12, sy - 12, 24, 24);
            g.setColor(Color.BLACK);
            g.drawOval(sx - 12, sy - 12, 24, 24);
        }

        // Draw Coily — purple circle (only after 3-second delay)
        if (coilyStarted) {
            int coilyCx, coilyCy;
            if (coilyJumping) {
                coilyCx = (int)(coilyStartX + (coilyEndX - coilyStartX) * coilyJumpT);
                coilyCy = (int)(coilyStartY + (coilyEndY - coilyStartY) * coilyJumpT - 30 * 4 * coilyJumpT * (1 - coilyJumpT));
            } else {
                coilyCx = tileX(coilyRow, coilyCol);
                coilyCy = tileY(coilyRow, coilyCol);
            }
            g.setColor(new Color(170, 0, 200));
            g.fillOval(coilyCx - 12, coilyCy - 12, 24, 24);
            g.setColor(Color.BLACK);
            g.drawOval(coilyCx - 12, coilyCy - 12, 24, 24);
        }

        // Draw player (pixel art or fallback orange circle)
        int px = tileX(playerRow, playerCol);
        int py = tileY(playerRow, playerCol);
        if (isFalling) {
            drawCharacter(g, fallX, fallY);
        } else if (isJumping) {
            int cx = (int)(startX + (endX - startX) * jumpT);
            int cy = (int)(startY + (endY - startY) * jumpT - 30 * 4 * jumpT * (1 - jumpT));
            drawCharacter(g, cx, cy);
        } else {
            drawCharacter(g, px, py);
        }

        // Draw speech bubble on top of the character when hit by a monster
        if (showBubble) {
            drawBubble(g, bubbleX, bubbleY);
        }

        // HUD
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, 10, 25);
        g.drawString("Lives: " + lives, 10, 50);
        g.drawString("Level: " + level, 10, 75);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Q/← up-left   W/↑ up-right   A/↓ down-left   S/→ down-right   R restart", 10, getHeight() - 8);

        if (won) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("LEVEL " + level + " CLEAR!", 190, getHeight() / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press ENTER or R for next level", 240, getHeight() / 2 + 40);
        }
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", 230, getHeight() / 2);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to restart", 300, getHeight() / 2 + 40);
        }
    }

    // Handles all player movement including disc detection and boundary fall
    private void movePlayer(int newRow, int newCol) {
        if (gameOver || won || isJumping || isFalling || showBubble) return;

        // Left disc: player tries col = -1 (steps off left edge)
        if (newCol == -1 && newRow >= 0 && newRow < ROWS && disc1Active) {
            disc1Active = false;
            score += 50;
            playerRow = 0;
            playerCol = 0;
            repaint();
            return;
        }
        // Right disc: player tries col = row+1 (steps off right edge)
        if (newRow >= 0 && newRow < ROWS && newCol == newRow + 1 && disc2Active) {
            disc2Active = false;
            score += 50;
            playerRow = 0;
            playerCol = 0;
            repaint();
            return;
        }

        // Off the board — start fall
        if (newRow < 0 || newRow >= ROWS || newCol < 0 || newCol > newRow) {
            startFall(playerRow, playerCol, true);
            return;
        }

        startX = tileX(playerRow, playerCol);
        startY = tileY(playerRow, playerCol);
        endX   = tileX(newRow, newCol);
        endY   = tileY(newRow, newCol);
        pendingRow = newRow;
        pendingCol = newCol;
        isJumping = true;
        jumpT = 0f;
        jumpTimer.start();
        playSound(SOUND_PATH + "Jumping.wav");
    }

    // Shows the speech bubble when a monster hits the player
    private void playerHit() {
        if (showBubble || isFalling) return;
        showBubble = true;
        bubbleX = tileX(playerRow, playerCol);
        bubbleY = tileY(playerRow, playerCol);
        playSound(SOUND_PATH + "speech.wav");
        bubbleTimer.start();
    }

    // Draws the speech bubble above the player, oval body with a pointed tail at bottom-left.
    // Text wraps across multiple lines to fit the fixed bubble width.
    private void drawBubble(Graphics g, int cx, int cy) {
        int bw = 140;
        int padH = 16; // horizontal padding inside oval
        int padV = 10; // vertical padding inside oval
        int maxLineWidth = bw - padH * 2;

        g.setFont(new Font("Georgia", Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();

        // Word-wrap bubbleText into lines that fit within maxLineWidth.
        // If a single word is wider than the bubble, break it by character.
        ArrayList<String> lines = new ArrayList<>();
        String[] words = bubbleText.split(" ", -1);
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (fm.stringWidth(word) > maxLineWidth) {
                // Word itself is too wide — flush current line, then break by char
                if (current.length() > 0) { lines.add(current.toString()); current.setLength(0); }
                StringBuilder chars = new StringBuilder();
                for (char ch : word.toCharArray()) {
                    chars.append(ch);
                    if (fm.stringWidth(chars.toString()) > maxLineWidth) {
                        chars.deleteCharAt(chars.length() - 1);
                        lines.add(chars.toString());
                        chars.setLength(0);
                        chars.append(ch);
                    }
                }
                if (chars.length() > 0) current.append(chars);
            } else {
                String test = current.length() == 0 ? word : current + " " + word;
                if (fm.stringWidth(test) <= maxLineWidth) {
                    current = new StringBuilder(test);
                } else {
                    if (current.length() > 0) lines.add(current.toString());
                    current = new StringBuilder(word);
                }
            }
        }
        if (current.length() > 0) lines.add(current.toString());

        // Bubble height grows to fit the number of lines
        int lineHeight = fm.getHeight();
        int bh = Math.max(50, lines.size() * lineHeight + padV * 2);
        int bx = cx - bw / 2;
        int by = cy - 28 - bh; // bottom of oval sits 28px above player center

        // Tail triangle — drawn first so the oval paints over the top of it cleanly
        int tailLx = cx - 28, tailRx = cx - 10;
        int tailBaseY = by + bh - 8;
        int tailTipX = cx - 30, tailTipY = cy - 8;
        g.setColor(Color.WHITE);
        g.fillPolygon(new int[]{tailLx, tailRx, tailTipX},
                      new int[]{tailBaseY, tailBaseY, tailTipY}, 3);

        // Oval body drawn on top — covers the upper portion of the tail
        g.fillOval(bx, by, bw, bh);
        g.setColor(Color.BLACK);
        g.drawOval(bx, by, bw, bh);

        // Only draw the two outer edges of the tail (the part outside the oval)
        g.drawLine(tailLx, tailBaseY, tailTipX, tailTipY);
        g.drawLine(tailRx, tailBaseY, tailTipX, tailTipY);

        // Draw each wrapped line centered horizontally and vertically in the oval
        g.setColor(Color.BLACK);
        int totalTextHeight = lines.size() * lineHeight;
        int textY = by + bh / 2 - totalTextHeight / 2 + fm.getAscent();
        for (String line : lines) {
            g.drawString(line, cx - fm.stringWidth(line) / 2, textY);
            textY += lineHeight;
        }
    }

    // Starts a fall animation from the given tile position
    private void startFall(int row, int col, boolean playerFall) {
        if (isFalling) return;
        isPlayerFall = playerFall;
        isFalling = true;
        fallX = tileX(row, col);
        fallY = tileY(row, col);
        playSound(SOUND_PATH + "Falling.wav");
        fallTimer.start();
    }

    // Called when player lands on a tile: updates score, checks enemy contact, win condition
    private void landOnTile() {
        if (!visited[playerRow][playerCol]) {
            visited[playerRow][playerCol] = true;
            visitedCount++;
            score += 25;
            if (score % 8000 == 0 && score > 0) lives++;
        }
        // Kill player if they land on Coily or Slick
        if (coilyStarted && coilyRow == playerRow && coilyCol == playerCol) {
            playerHit();
            return;
        }
        if (slickActive && slickRow == playerRow && slickCol == playerCol) {
            playerHit();
            return;
        }
        if (visitedCount == TOTAL_TILES) {
            won = true;
            repaint();
        }
    }

    // Deducts a life, resets player to top; ends game if no lives left
    private void loseLife() {
        lives--;
        playerRow = 0;
        playerCol = 0;
        if (lives <= 0) {
            gameOver = true;
            coilyMoveTimer.stop();
            slickMoveTimer.stop();
            slickSpawnTimer.stop();
        }
        repaint();
    }

    // Advances to the next level: resets board, restores discs, speeds up enemies
    private void nextLevel() {
        score += 500 * level; // bonus for completing the level
        level++;
        won = false;
        disc1Active = true;
        disc2Active = true;
        for (int r = 0; r < ROWS; r++) visited[r] = new boolean[r + 1];
        playerRow = 0;
        playerCol = 0;
        visited[0][0] = true;
        visitedCount = 1;
        slickActive = false;
        showBubble = false;
        bubbleTimer.stop();
        slickMoveTimer.setDelay(Math.max(300, 800 - (level - 1) * 80));
        coilyMoveTimer.setDelay(Math.max(200, 600 - (level - 1) * 80));
        startCoilyDelayed();
        repaint();
    }

    // Full game restart — resets everything to level 1
    private void restart() {
        level = 1;
        lives = 3;
        score = 0;
        won = false;
        gameOver = false;
        disc1Active = true;
        disc2Active = true;
        for (int r = 0; r < ROWS; r++) visited[r] = new boolean[r + 1];
        playerRow = 0;
        playerCol = 0;
        visited[0][0] = true;
        visitedCount = 1;
        slickActive = false;
        showBubble = false;
        bubbleTimer.stop();
        coilyMoveTimer.setDelay(600);
        startCoilyDelayed();
        if (!slickSpawnTimer.isRunning()) slickSpawnTimer.start();
        repaint();
    }

    // Plays a WAV file on a background thread using SourceDataLine (macOS compatible)
    private void playSound(String filename) {
        new Thread(() -> {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));
                AudioFormat format = ais.getFormat();
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                byte[] buf = new byte[4096];
                int bytesRead;
                while ((bytesRead = ais.read(buf)) != -1) {
                    line.write(buf, 0, bytesRead);
                }
                line.drain();
                line.close();
                ais.close();
            } catch (Exception e) {
                System.out.println("Sound error: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_R) restart();
            return;
        }
        if (won) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_R) nextLevel();
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:  case KeyEvent.VK_Q: movePlayer(playerRow - 1, playerCol - 1); break;
            case KeyEvent.VK_UP:    case KeyEvent.VK_W: movePlayer(playerRow - 1, playerCol);     break;
            case KeyEvent.VK_DOWN:  case KeyEvent.VK_A: movePlayer(playerRow + 1, playerCol);     break;
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_S: movePlayer(playerRow + 1, playerCol + 1); break;
            case KeyEvent.VK_R: restart(); break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
