import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

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

    public GamePanel(Color[][] character) {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 520));
        setFocusable(true);
        addKeyListener(this);

        for (int r = 0; r < ROWS; r++) {
            visited[r] = new boolean[r + 1];
        }
        visited[0][0] = true;
        visitedCount = 1;
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

    private void drawQbert(Graphics g, int cx, int cy) {
        //Body
        g.setColor(new Color(225,140,0));
        g.fillOval(cx - 18, cy - 18, 36, 36);

        //Eyes
        g.setColor(Color.WHITE);
        g.fillOval(cx - 10, cy - 10, 8, 8);
        g.fillOval(cx + 2, cy - 10, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(cx - 8, cy - 8, 4, 4);
        g.fillOval(cx + 4, cy - 8, 4, 4);   

        //Nose
        g.setColor(new Color(200,100,0));
        g.fillOval(cx - 4, cy - 2, 8, 8);
        
        //Legs
        g.setColor(new Color(200, 100, 0));
        g.drawLine(cx - 12, cy + 18, cx - 18, cy + 30);
        g.drawLine(cx - 4,  cy + 18, cx - 8,  cy + 30);
        g.drawLine(cx + 4,  cy + 18, cx + 8,  cy + 30);
        g.drawLine(cx + 12, cy + 18, cx + 18, cy + 30);

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
        drawQbert(g, px, py);

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
        if (gameOver || won) return;
        if (newRow < 0 || newRow >= ROWS || newCol < 0 || newCol > newRow) {
            loseLife();
            return;
        }
        playerRow = newRow;
        playerCol = newCol;
        if (!visited[playerRow][playerCol]) {
            visited[playerRow][playerCol] = true;
            visitedCount++;
            score += 25;
        }
        if (visitedCount == 28) won = true;
        repaint();
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
