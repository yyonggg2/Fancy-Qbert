import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GamePanel extends JPanel implements KeyListener {

    private static final int ROWS = 7;
    private static final int W = 40;
    private static final int H = 20;
    private static final int D = 25;
    private static final int CENTER_X = 400;
    private static final int TOP_Y = 60;

    private boolean[][] visited = new boolean[ROWS][];
    private int playerRow = 0, playerCol = 0;
    private int lives = 3;
    private int score = 0;
    private int visitedCount = 0;
    private boolean gameOver = false;
    private boolean won = false;
    private Color[][] character;

    // fields for jumping
    private boolean isJumping = false;
    private float jumpT = 0f;
    private int startX, startY, endX, endY;
    private int pendingRow, pendingCol;
    private javax.swing.Timer jumpTimer;


    public GamePanel(Color[][] character) {
        this.character = character;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 520));
        setFocusable(true);
        addKeyListener(this);

        for (int r = 0; r < ROWS; r++) {
            visited[r] = new boolean[r + 1];
        }
        visited[0][0] = true;
        visitedCount = 1;

        jumpTimer = new javax.swing.Timer(16, e -> {
            if (isJumping) {
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
            }
        });
    }




    private int tileX(int row, int col) {
        return CENTER_X + (2 * col - row) * W;
    }

    private int tileY(int row, int col) {
        return TOP_Y + H + row * (H + D);
    }

    private Polygon topFace(int cx, int cy) {
        return new Polygon(
            new int[]{cx,     cx + W, cx,     cx - W},
            new int[]{cy - H, cy,     cy + H, cy},
            4
        );
    }

    private Polygon leftFace(int cx, int cy) {
        return new Polygon(
            new int[]{cx - W, cx,         cx,         cx - W},
            new int[]{cy,     cy + H,     cy + H + D, cy + D},
            4
        );
    }

    private Polygon rightFace(int cx, int cy) {
        return new Polygon(
            new int[]{cx,     cx + W, cx + W,  cx},
            new int[]{cy + H, cy,     cy + D,  cy + H + D},
            4
        );
    }

    private void drawTile(Graphics g, int row, int col) {
        int cx = tileX(row, col);
        int cy = tileY(row, col);
        boolean v = visited[row][col];

        g.setColor(v ? new Color(255, 200, 0) : new Color(0, 140, 130));
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

    private void drawCharacter(Graphics g, int cx, int cy) {
        if (character == null) return;
        for (int r = 0; r < character.length; r++) {
            for (int c = 0; c < character[r].length; c++) {
                if (character[r][c] != null) {
                    g.setColor(character[r][c]);
                    g.fillRect(cx - 20 + c * 2, cy - 20 + r * 2, 2, 2);
                }
            }
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c <= r; c++) {
                drawTile(g, r, c);
            }
        }

        // Placeholder orange circle for the player — we'll replace this together
        int px = tileX(playerRow, playerCol);
        int py = tileY(playerRow, playerCol);
        if (isJumping) {
            int cx = (int)(startX + (endX - startX) * jumpT);
            int cy = (int)(startY + (endY - startY) * jumpT - 30 * 4 * jumpT * (1 - jumpT));
            drawCharacter(g, cx, cy);
        } else {
            drawCharacter(g, px, py);
        }


        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, 10, 25);
        g.drawString("Lives: " + lives, 10, 50);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.drawString("← ↑ ↓ → to move   |   R to restart", 10, 510);

        if (won) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("YOU WIN!", 270, 470);
        }
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", 230, 470);
        }
    }

    private void movePlayer(int newRow, int newCol) {
        if (gameOver || won || isJumping) return;
        if (newRow < 0 || newRow >= ROWS || newCol < 0 || newCol > newRow) {
            loseLife();
            return;
        }
        startX = tileX(playerRow, playerCol);
        startY = tileY(playerRow, playerCol);
        endX = tileX(newRow, newCol);
        endY = tileY(newRow, newCol);
        pendingRow = newRow;
        pendingCol = newCol;
        isJumping = true;
        jumpT = 0f;
        jumpTimer.start();
    }

    private void landOnTile() {
        if (!visited[playerRow][playerCol]) {
            visited[playerRow][playerCol] = true;
            visitedCount++;
            score += 25;
        }if (visitedCount == 28) {
            won = true;
            repaint();
        }
    }

    private void loseLife() {
        lives--;
        playerRow = 0;
        playerCol = 0;
        if (lives <= 0) gameOver = true;
        repaint();
    }

    private void restart() {
        for (int r = 0; r < ROWS; r++) visited[r] = new boolean[r + 1];
        playerRow = 0;
        playerCol = 0;
        visited[0][0] = true;
        visitedCount = 1;
        lives = 3;
        score = 0;
        gameOver = false;
        won = false;
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:  movePlayer(playerRow - 1, playerCol - 1); break;
            case KeyEvent.VK_UP:    movePlayer(playerRow - 1, playerCol);     break;
            case KeyEvent.VK_DOWN:  movePlayer(playerRow + 1, playerCol);     break;
            case KeyEvent.VK_RIGHT: movePlayer(playerRow + 1, playerCol + 1); break;
            case KeyEvent.VK_R:     restart(); break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
