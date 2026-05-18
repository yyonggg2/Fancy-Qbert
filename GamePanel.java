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
    private static final String BASE       = "";
    private static final String SOUND_PATH = BASE + "sounds/";
    private static final String P_PATH     = BASE + "sprites/Player/";
    private static final String C_PATH     = BASE + "sprites/Coily/";
    private static final String S_PATH     = BASE + "sprites/Slick/";
    private static final String RB_PATH    = BASE + "sprites/RedBall/";
    private static final String GB_PATH    = BASE + "sprites/GreenBall/";
    private static final String UFO_PATH   = BASE + "sprites/UFO2/";

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

    // Flying discs — two platforms beside pyramid, carry player to top
    private boolean disc1Active = true;
    private boolean disc2Active = true;
    private Color[][][] discFrames = new Color[4][][];
    private boolean isOnDisc = false;
    private int discRideStartX, discRideStartY;
    private float discRideT = 0f;
    private javax.swing.Timer discRideTimer;

    // Coily delayed spawn — false until 3 seconds after game/level start
    private boolean coilyStarted = false;
    // Coily hatching — vibrates briefly before switching to snake form
    private boolean coilyHatching = false;

    // Slick transformation — starts as ball, vibrates, then becomes creature
    private boolean slickTransformed = false;
    private boolean slickVibrating = false;
    private boolean slickFalling = false;
    private int slickFallX, slickFallY;
    private javax.swing.Timer slickFallTimer;
    private Color[][] slickFallSprite;

    // Player directional sprites
    private Color[][] playerMoveLeft;
    private Color[][] playerIdleRight;
    private Color[][] playerMoveRight;
    private boolean playerFacingRight = false;
    private boolean playerMovingDown  = false;

    // Coily directional sprites
    private Color[][] coilyBallSprite;
    private Color[][] coilyBallMovingSprite;
    private Color[][] coilyFallSprite;
    private Color[][] coilySnakeLeft;
    private Color[][] coilySnakeMoveLeft;
    private Color[][] coilySnakeRight;
    private Color[][] coilySnakeMoveRight;
    private boolean coilyFacingRight = false;

    // Slick directional sprites
    private Color[][] greenballSprite;
    private Color[][] slickIdleLeft;
    private Color[][] slickMoveLeft;
    private Color[][] slickIdleRight;
    private Color[][] slickMoveRight;
    private boolean slickFacingRight = false;

    // Red Ball — falls randomly downward, kills player on contact, does not chase or revert tiles
    private int redBallRow = -1, redBallCol = -1;
    private boolean redBallActive = false;
    private boolean redBallJumping = false;
    private float redBallJumpT = 0f;
    private int redBallStartX, redBallStartY, redBallEndX, redBallEndY;
    private int redBallPendingRow, redBallPendingCol;
    private javax.swing.Timer redBallMoveTimer;
    private javax.swing.Timer redBallJumpTimer;
    private javax.swing.Timer redBallSpawnTimer;
    private Color[][] redBallIdleSprite;
    private Color[][] redBallMoveSprite;
    private Color[][] redBallFallSprite;
    private boolean redBallFalling = false;
    private int redBallFallX, redBallFallY;
    private javax.swing.Timer redBallFallTimer;

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
        playerMoveLeft       = loadSprite(P_PATH + "Player_1_Up_Down_Moving.png");
        playerIdleRight      = loadSprite(P_PATH + "Player_2_Right_Left.png");
        playerMoveRight      = loadSprite(P_PATH + "Player_4_Up_Down_Moving_UFO.png");

        coilyBallSprite      = loadSprite(C_PATH + "Coily_0_qbert_snakeball.png");
        coilyBallMovingSprite= loadSprite(C_PATH + "Coily_1_qbert_snakeball_moving.png");
        coilyFallSprite      = loadSprite(C_PATH + "Coily_5_Right_Left_Moving.png");
        coilySnakeLeft       = loadSprite(C_PATH + "Coily_2_Up_Down.png");
        coilySnakeMoveLeft   = loadSprite(C_PATH + "Coily_3_Up_Down_Moving.png");
        coilySnakeRight      = loadSprite(C_PATH + "Coily_4_Right_Left.png");
        coilySnakeMoveRight  = loadSprite(C_PATH + "Coily_5_Right_Left_Moving.png");

        greenballSprite      = loadSprite(GB_PATH + "greenball_sprite.png");
        discFrames[0] = loadSprite(UFO_PATH + "UFO2_4_qbert_disc_lvl1_r1.png");
        discFrames[1] = loadSprite(UFO_PATH + "UFO2_5_qbert_disc_lvl1_2_r1.png");
        discFrames[2] = loadSprite(UFO_PATH + "UFO2_6_qbert_disc_lvl1_3_r1.png");
        discFrames[3] = loadSprite(UFO_PATH + "UFO2_7_qbert_disc_lvl1_4_r1.png");
        slickIdleLeft        = loadSprite(S_PATH + "Slick_0_QBert_Green_Annoying_Guy3.png");
        slickMoveLeft        = loadSprite(S_PATH + "Slick_1_QBert_Green_Annoying_Guy4.png");
        slickIdleRight       = loadSprite(S_PATH + "Slick_4_QBert_Green_Annoying_Guy7.png");
        slickMoveRight       = loadSprite(S_PATH + "Slick_5_QBert_Green_Annoying_Guy8.png");
        slickFallSprite      = loadSprite(S_PATH + "Slick_2_QBert_Green_Annoying_Guy5.png");
        redBallIdleSprite    = loadSprite(RB_PATH + "Red_Ball_0_qbert_enemyballs2.png");
        redBallMoveSprite    = loadSprite(RB_PATH + "Red_Ball_1_qbert_enemyball_moving.png");
        redBallFallSprite    = loadSprite(RB_PATH + "Red_Ball_2_qbert_enemyballs3.png");
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 580));
        setFocusable(true);
        addKeyListener(this);
        initBoard();
        initTimers();
        playSound(SOUND_PATH + "level-start.wav");
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
                else { resetCoily(); startCoilyDelayed(); }
            }
            repaint();
        });

        // Coily decides its next move — random when egg, chasing when snake
        int coilySpeed = Math.max(400, 1000 - (level - 1) * 80);
        coilyMoveTimer = new javax.swing.Timer(coilySpeed, e -> {
            if (gameOver || won || coilyJumping || isFalling) return;
            int newR, newC;
            if (coilyState == 0) {
                // Ball: bounce randomly downward, then vibrate and hatch into snake
                newR = coilyRow + 1;
                newC = coilyCol + (int)(Math.random() * 2);
                coilyBounces++;
                if (coilyBounces >= 5 && !coilyHatching) {
                    coilyHatching = true;
                    javax.swing.Timer hatch = new javax.swing.Timer(500, ev -> {
                        coilyState = 1;
                        coilyHatching = false;
                    });
                    hatch.setRepeats(false);
                    hatch.start();
                }
            } else {
                // Snake: chase player in all directions
                if (playerRow < coilyRow) {
                    // Player is above — move up
                    newR = coilyRow - 1;
                    newC = (playerCol < coilyCol) ? coilyCol - 1 : coilyCol;
                } else {
                    // Player is below or same row — move down
                    newR = coilyRow + 1;
                    newC = (playerCol > coilyCol) ? coilyCol + 1 : coilyCol;
                }
            }
            if (newR < 0 || newR >= ROWS || newC < 0 || newC > newR) {
                coilyMoveTimer.stop();
                startFall(coilyRow, coilyCol, false);
                return;
            }
            coilyFacingRight = (newC > coilyCol);
            coilyStartX = tileX(coilyRow, coilyCol);
            coilyStartY = tileY(coilyRow, coilyCol);
            coilyEndX   = tileX(newR, newC);
            coilyEndY   = tileY(newR, newC);
            coilyPendingRow = newR;
            coilyPendingCol = newC;
            coilyJumping = true;
            coilyJumpT = 0f;
            coilyJumpTimer.start();
            playSound(SOUND_PATH + "jump-4.wav");
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

        // Slick hunts the nearest visited tile — moves up or down to reach it
        slickMoveTimer = new javax.swing.Timer(800, e -> {
            if (!slickActive || gameOver || won || slickJumping) return;

            // Find the nearest visited tile (ignoring current tile)
            int targetRow = -1, targetCol = -1, minDist = Integer.MAX_VALUE;
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c <= r; c++) {
                    if (visited[r][c]) {
                        int dist = Math.abs(r - slickRow) + Math.abs(c - slickCol);
                        if (dist > 0 && dist < minDist) { minDist = dist; targetRow = r; targetCol = c; }
                    }
                }
            }

            int newR, newC;
            if (targetRow == -1) {
                // No visited tiles left — drift randomly downward
                newR = slickRow + 1;
                newC = slickCol + (int)(Math.random() * 2);
            } else {
                // Pick whichever of the 4 moves minimises distance to target
                int[][] moves = {
                    {slickRow-1, slickCol-1}, {slickRow-1, slickCol},
                    {slickRow+1, slickCol},   {slickRow+1, slickCol+1}
                };
                int bestDist = Integer.MAX_VALUE, bestR = -1, bestC = -1;
                for (int[] m : moves) {
                    int mr = m[0], mc = m[1];
                    if (mr >= 0 && mr < ROWS && mc >= 0 && mc <= mr) {
                        int d = Math.abs(mr - targetRow) + Math.abs(mc - targetCol);
                        if (d < bestDist) { bestDist = d; bestR = mr; bestC = mc; }
                    }
                }
                if (bestR == -1) {
                    slickMoveTimer.stop();
                    slickActive = false;
                    startSlickFall(slickRow, slickCol);
                    return;
                }
                newR = bestR; newC = bestC;
            }

            if (newR < 0 || newR >= ROWS || newC < 0 || newC > newR) {
                slickMoveTimer.stop();
                slickActive = false;
                startSlickFall(slickRow, slickCol);
                return;
            }

            slickFacingRight = (newC > slickCol);
            slickStartX = tileX(slickRow, slickCol);
            slickStartY = tileY(slickRow, slickCol);
            slickEndX   = tileX(newR, newC);
            slickEndY   = tileY(newR, newC);
            slickPendingRow = newR;
            slickPendingCol = newC;
            slickJumping = true;
            slickJumpT = 0f;
            slickJumpTimer.start();
            playSound(SOUND_PATH + "jump-3.wav");
        });

        // Drops Slick off screen after it goes out of bounds or is scared off by disc
        slickFallTimer = new javax.swing.Timer(16, e -> {
            slickFallY += 8;
            if (slickFallY > getHeight()) {
                slickFallTimer.stop();
                slickFalling = false;
            }
            repaint();
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

        // Spawns Slick every 5 seconds; starts as ball, transforms into creature after 3s
        slickSpawnTimer = new javax.swing.Timer(5000, e -> {
            if (!slickActive && !gameOver && !won) {
                slickRow = 0;
                slickCol = 0;
                slickActive = true;
                slickTransformed = false;
                slickVibrating = false;
                slickMoveTimer.start();
                javax.swing.Timer t1 = new javax.swing.Timer(3000, ev -> {
                    slickVibrating = true;
                    javax.swing.Timer t2 = new javax.swing.Timer(500, ev2 -> {
                        slickVibrating = false;
                        slickTransformed = true;
                    });
                    t2.setRepeats(false);
                    t2.start();
                });
                t1.setRepeats(false);
                t1.start();
            }
        });

        // Shows bubble for 3 seconds then deducts a life
        bubbleTimer = new javax.swing.Timer(3000, e -> {
            bubbleTimer.stop();
            showBubble = false;
            loseLife();
        });
        bubbleTimer.setRepeats(false);

        // Carries player from disc position to top of pyramid over 1.5 seconds
        discRideTimer = new javax.swing.Timer(16, e -> {
            discRideT += 16f / 1500f;
            if (discRideT >= 1f) {
                discRideT = 1f;
                discRideTimer.stop();
                isOnDisc = false;
                playerRow = 0;
                playerCol = 0;
                landOnTile();
            }
            repaint();
        });

        // Red Ball decides its next move — always falls randomly downward
        redBallMoveTimer = new javax.swing.Timer(600, e -> {
            if (!redBallActive || gameOver || won || redBallJumping) return;
            int newR = redBallRow + 1;
            int newC = redBallCol + (int)(Math.random() * 2);
            if (newR >= ROWS || newC < 0 || newC > newR) {
                redBallMoveTimer.stop();
                redBallActive = false;
                redBallFalling = true;
                redBallFallX = tileX(redBallRow, redBallCol);
                redBallFallY = tileY(redBallRow, redBallCol);
                playSound(SOUND_PATH + "Falling.wav");
                redBallFallTimer.start();
                return;
            }
            redBallStartX = tileX(redBallRow, redBallCol);
            redBallStartY = tileY(redBallRow, redBallCol);
            redBallEndX   = tileX(newR, newC);
            redBallEndY   = tileY(newR, newC);
            redBallPendingRow = newR;
            redBallPendingCol = newC;
            redBallJumping = true;
            redBallJumpT = 0f;
            redBallJumpTimer.start();
            playSound(SOUND_PATH + "jump-3.wav");
        });

        // Advances Red Ball's jump animation; checks player contact on landing
        redBallJumpTimer = new javax.swing.Timer(16, e -> {
            redBallJumpT += 0.1f;
            if (redBallJumpT >= 1f) {
                redBallRow = redBallPendingRow;
                redBallCol = redBallPendingCol;
                redBallJumpT = 1f;
                redBallJumping = false;
                redBallJumpTimer.stop();
                if (redBallRow == playerRow && redBallCol == playerCol && !isFalling) {
                    playerHit();
                }
            }
            repaint();
        });

        // Drops the Red Ball off the screen after it goes out of bounds
        redBallFallTimer = new javax.swing.Timer(16, e -> {
            redBallFallY += 8;
            if (redBallFallY > getHeight()) {
                redBallFallTimer.stop();
                redBallFalling = false;
            }
            repaint();
        });

        // Spawns Red Ball every 8 seconds
        redBallSpawnTimer = new javax.swing.Timer(8000, e -> {
            if (!redBallActive && !gameOver && !won) {
                redBallRow = 0;
                redBallCol = 0;
                redBallActive = true;
                redBallMoveTimer.start();
            }
        });

        startCoilyDelayed();
        // Slick only appears from level 3 onward
        if (level >= 3) slickSpawnTimer.start();
        redBallSpawnTimer.start();
    }

    // Waits 5 seconds then spawns Coily — gives start sound time to finish
    private void startCoilyDelayed() {
        coilyStarted = false;
        coilyMoveTimer.stop();
        javax.swing.Timer delay = new javax.swing.Timer(5000, e -> {
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

    private void startSlickFall(int row, int col) {
        slickFalling = true;
        slickFallX = tileX(row, col);
        slickFallY = tileY(row, col);
        playSound(SOUND_PATH + "Falling.wav");
        slickFallTimer.start();
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

    // Reads a PNG file into a Color[][] — transparent pixels become null
    private Color[][] loadSprite(String path) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(path));
            int h = img.getHeight(), w = img.getWidth();
            Color[][] pixels = new Color[h][w];
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    int argb = img.getRGB(c, r);
                    if (((argb >> 24) & 0xff) > 10) pixels[r][c] = new Color(argb, true);
                }
            }
            return pixels;
        } catch (Exception e) {
            System.out.println("Could not load sprite: " + path);
            return null;
        }
    }

    // Draws a Color[][] sprite centered at (cx, cy), 1px per cell
    private void drawSprite(Graphics g, Color[][] sprite, int cx, int cy) {
        if (sprite == null) return;
        int h = sprite.length, w = sprite[0].length;
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (sprite[r][c] != null) {
                    g.setColor(sprite[r][c]);
                    g.fillRect(cx - w / 2 + c, cy - h / 2 + r, 1, 1);
                }
            }
        }
    }

    // Draws player sprite — custom cabinet character takes priority; Q*bert sprites used as default
    private void drawCharacter(Graphics g, int cx, int cy) {
        if (character != null) {
            for (int r = 0; r < character.length; r++) {
                for (int c = 0; c < character[r].length; c++) {
                    if (character[r][c] != null) {
                        g.setColor(character[r][c]);
                        g.fillRect(cx - 20 + c * 2, cy - 20 + r * 2, 2, 2);
                    }
                }
            }
            return;
        }
        // No cabinet character selected — use Q*bert directional sprites
        if (playerIdleRight != null) {
            Color[][] sprite;
            if (!isJumping || playerMovingDown) {
                sprite = playerIdleRight;
            } else if (playerFacingRight) {
                sprite = playerMoveRight;
            } else {
                sprite = playerMoveLeft;
            }
            drawSprite(g, sprite, cx, cy);
            return;
        }
        g.setColor(new Color(225, 140, 0));
        g.fillOval(cx - 15, cy - 15, 30, 30);
    }

    // Draws the two flying discs using animated UFO sprite frames
    private void drawDiscs(Graphics g) {
        int frame = (int)((System.currentTimeMillis() / 150) % 4);
        Color[][] sprite = discFrames[frame];
        if (disc1Active) {
            int dx = tileX(3, 0) - W - 50;
            int dy = tileY(3, 0);
            drawSprite(g, sprite, dx, dy);
        }
        if (disc2Active) {
            int dx = tileX(3, 3) + W + 50;
            int dy = tileY(3, 3);
            drawSprite(g, sprite, dx, dy);
        }
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

        // Draw Slick — green ball for first 3s, vibrates, then becomes creature sprite
        if (slickActive) {
            int sx, sy;
            if (slickJumping) {
                sx = (int)(slickStartX + (slickEndX - slickStartX) * slickJumpT);
                sy = (int)(slickStartY + (slickEndY - slickStartY) * slickJumpT - 25 * 4 * slickJumpT * (1 - slickJumpT));
            } else {
                sx = tileX(slickRow, slickCol);
                sy = tileY(slickRow, slickCol);
            }
            int svibX = slickVibrating ? (int)(3 * Math.sin(System.currentTimeMillis() / 40.0)) : 0;
            if (slickTransformed) {
                Color[][] slickSprite = slickFacingRight
                    ? (slickJumping ? slickMoveRight : slickIdleRight)
                    : (slickJumping ? slickMoveLeft  : slickIdleLeft);
                drawSprite(g, slickSprite, sx + svibX, sy);
            } else {
                drawSprite(g, greenballSprite, sx + svibX, sy);
            }
        }

        // Draw Slick fall animation
        if (slickFalling) {
            drawSprite(g, slickFallSprite, slickFallX, slickFallY);
        }

        // Draw Red Ball — bounces randomly downward, kills player on contact
        if (redBallActive) {
            int rx, ry;
            if (redBallJumping) {
                rx = (int)(redBallStartX + (redBallEndX - redBallStartX) * redBallJumpT);
                ry = (int)(redBallStartY + (redBallEndY - redBallStartY) * redBallJumpT - 25 * 4 * redBallJumpT * (1 - redBallJumpT));
            } else {
                rx = tileX(redBallRow, redBallCol);
                ry = tileY(redBallRow, redBallCol);
            }
            Color[][] rbSprite = redBallJumping ? redBallMoveSprite : redBallIdleSprite;
            drawSprite(g, rbSprite, rx, ry);
        }
        // Draw Red Ball fall animation
        if (redBallFalling) {
            drawSprite(g, redBallFallSprite, redBallFallX, redBallFallY);
        }

        // Draw Coily — hidden while falling, ball sprite until hatched, then snake sprite
        if (coilyStarted && (!isFalling || isPlayerFall)) {
            int coilyCx, coilyCy;
            if (coilyJumping) {
                coilyCx = (int)(coilyStartX + (coilyEndX - coilyStartX) * coilyJumpT);
                coilyCy = (int)(coilyStartY + (coilyEndY - coilyStartY) * coilyJumpT - 30 * 4 * coilyJumpT * (1 - coilyJumpT));
            } else {
                coilyCx = tileX(coilyRow, coilyCol);
                coilyCy = tileY(coilyRow, coilyCol);
            }
            int cvibX = coilyHatching ? (int)(3 * Math.sin(System.currentTimeMillis() / 40.0)) : 0;
            if (coilyState == 0) {
                Color[][] ballSprite = coilyJumping ? coilyBallMovingSprite : coilyBallSprite;
                drawSprite(g, ballSprite, coilyCx + cvibX, coilyCy);
            } else {
                Color[][] snakeSprite = coilyFacingRight
                    ? (coilyJumping ? coilySnakeMoveRight : coilySnakeRight)
                    : (coilyJumping ? coilySnakeMoveLeft  : coilySnakeLeft);
                drawSprite(g, snakeSprite, coilyCx, coilyCy);
            }
        }

        // Draw Coily fall animation
        if (isFalling && !isPlayerFall) {
            drawSprite(g, coilyFallSprite, fallX, fallY);
        }

        // Draw player (pixel art or fallback orange circle)
        int px = tileX(playerRow, playerCol);
        int py = tileY(playerRow, playerCol);
        if (isFalling && isPlayerFall) {
            drawCharacter(g, fallX, fallY);
        } else if (isOnDisc) {
            int tx = tileX(0, 0), ty = tileY(0, 0);
            int cx = (int)(discRideStartX + (tx - discRideStartX) * discRideT);
            int cy = (int)(discRideStartY + (ty - discRideStartY) * discRideT);
            int rideFrame = (int)((System.currentTimeMillis() / 150) % 4);
            drawSprite(g, discFrames[rideFrame], cx, cy + 20);
            drawCharacter(g, cx, cy);
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
        if (gameOver || won || isJumping || isFalling || showBubble || isOnDisc) return;

        // Left disc: only catchable from row 3 (the row the disc sits beside)
        if (newCol == -1 && playerRow == 3 && disc1Active) {
            disc1Active = false;
            score += 50;
            isOnDisc = true;
            discRideStartX = tileX(playerRow, playerCol);
            discRideStartY = tileY(playerRow, playerCol);
            discRideT = 0f;
            discRideTimer.start();
            if (coilyStarted) { coilyMoveTimer.stop(); startFall(coilyRow, coilyCol, false); }
            if (slickActive) { slickMoveTimer.stop(); slickActive = false; startSlickFall(slickRow, slickCol); }
            return;
        }
        // Right disc: only catchable from row 3 (the row the disc sits beside)
        if (newCol == newRow + 1 && playerRow == 3 && disc2Active) {
            disc2Active = false;
            score += 50;
            isOnDisc = true;
            discRideStartX = tileX(playerRow, playerCol);
            discRideStartY = tileY(playerRow, playerCol);
            discRideT = 0f;
            discRideTimer.start();
            if (coilyStarted) { coilyMoveTimer.stop(); startFall(coilyRow, coilyCol, false); }
            if (slickActive) { slickMoveTimer.stop(); slickActive = false; startSlickFall(slickRow, slickCol); }
            return;
        }

        // Off the board — start fall
        if (newRow < 0 || newRow >= ROWS || newCol < 0 || newCol > newRow) {
            startFall(playerRow, playerCol, true);
            return;
        }

        playerFacingRight = newCol > playerCol || (newCol == playerCol && newRow < playerRow);
        playerMovingDown  = newRow > playerRow;
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
        playSound(SOUND_PATH + (playerFall ? "Falling.wav" : "snake-fall.wav"));
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
        if (redBallActive && redBallRow == playerRow && redBallCol == playerCol) {
            playerHit();
            return;
        }
        if (visitedCount == TOTAL_TILES) {
            won = true;
            playSound(SOUND_PATH + "victory.wav");
            repaint();
        }
    }

    // Deducts a life, resets player to top; ends game if no lives left
    private void loseLife() {
        lives--;
        // Only reset position when player fell off the board; keep position on enemy hit
        if (isPlayerFall) { playerRow = 0; playerCol = 0; }
        isFalling = false;
        fallTimer.stop();
        if (lives <= 0) {
            gameOver = true;
            coilyMoveTimer.stop();
            slickMoveTimer.stop();
            slickFallTimer.stop();
            slickFalling = false;
            slickSpawnTimer.stop();
            redBallMoveTimer.stop();
            redBallFallTimer.stop();
            redBallSpawnTimer.stop();
            redBallActive = false;
            redBallFalling = false;
        } else {
            // All enemies vanish immediately and restart from the top with delay
            coilyMoveTimer.stop();
            coilyJumpTimer.stop();
            coilyJumping = false;
            coilyHatching = false;
            resetCoily();
            startCoilyDelayed();
            slickMoveTimer.stop();
            slickJumpTimer.stop();
            slickFallTimer.stop();
            slickJumping = false;
            slickActive = false;
            slickFalling = false;
            slickTransformed = false;
            slickVibrating = false;
            redBallMoveTimer.stop();
            redBallFallTimer.stop();
            redBallJumpTimer.stop();
            redBallJumping = false;
            redBallActive = false;
            redBallFalling = false;
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
        slickFallTimer.stop();
        slickFalling = false;
        slickTransformed = false;
        slickVibrating = false;
        coilyHatching = false;
        isOnDisc = false;
        discRideTimer.stop();
        showBubble = false;
        bubbleTimer.stop();
        redBallMoveTimer.stop();
        redBallFallTimer.stop();
        redBallJumpTimer.stop();
        redBallJumping = false;
        redBallActive = false;
        redBallFalling = false;
        slickMoveTimer.setDelay(Math.max(300, 800 - (level - 1) * 80));
        coilyMoveTimer.setDelay(Math.max(400, 1000 - (level - 1) * 80));
        startCoilyDelayed();
        slickSpawnTimer.stop();
        if (level >= 3 && !slickSpawnTimer.isRunning()) slickSpawnTimer.start();
        playSound(SOUND_PATH + "level-start.wav");
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
        slickFallTimer.stop();
        slickFalling = false;
        slickTransformed = false;
        slickVibrating = false;
        coilyHatching = false;
        isOnDisc = false;
        discRideTimer.stop();
        showBubble = false;
        bubbleTimer.stop();
        coilyMoveTimer.setDelay(1000);
        startCoilyDelayed();
        slickSpawnTimer.stop(); // restart at level 1, Slick not active yet
        redBallMoveTimer.stop();
        redBallFallTimer.stop();
        redBallJumpTimer.stop();
        redBallJumping = false;
        redBallActive = false;
        redBallFalling = false;
        if (!redBallSpawnTimer.isRunning()) redBallSpawnTimer.start();
        playSound(SOUND_PATH + "level-start.wav");
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
