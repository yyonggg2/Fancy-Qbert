import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.*;

public class CanvasPanel extends JPanel {
    private static final int TILE_SIZE = 20;
    private static final int GRID_SIZE = 16;
    private boolean[][] painted = new boolean[GRID_SIZE][GRID_SIZE];

    /* Constructor */
    public CanvasPanel(){
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                paintCell(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                paintCell(e.getX(), e.getY());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
       super.paintComponent(g);

    /*Draw the grid */
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (painted[row][col]){
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
    }

    private void paintCell(int x, int y){
        int col = x / TILE_SIZE;
        int row = y / TILE_SIZE;
        if ( row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE){
            painted[row][col] = true;
            repaint();
        }
    }

}
