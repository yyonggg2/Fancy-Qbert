import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;

public class CanvasPanel extends JPanel {
    private static final int TILE_SIZE = 20;
    private static final int GRID_SIZE = 16;

    @Override
    protected void paintComponent(Graphics g) {
       super.paintComponent(g);
    

    /*Draw the grid */
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                g.setColor(Color.WHITE);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
    }

}
