import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CanvasPanel extends JPanel {
    private static final int TILE_SIZE = 20;
    private static final int GRID_SIZE = 16;
    private Color[][] painted = new Color[GRID_SIZE][GRID_SIZE];
    private Color selectedColor = Color.BLACK;
    private PreviewPanel preview;

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
       Graphics2D g2 = (Graphics2D) g;
       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);


    /*Draw the grid */
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (painted[row][col] != null) {
                    g.setColor(painted[row][col]);
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
            painted[row][col] = selectedColor;
            repaint();
            if (preview != null) {
                preview.repaint();
            }
        }
    
    }

    public void setColor(Color c){
        selectedColor = c;
    }

    public Color[][] getPixels(){
        return painted;
    }

    public int getGridSize(){
        return GRID_SIZE;
    }

    public void setPreview(PreviewPanel preview){
        this.preview = preview;
    }

    public void clean(){
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                painted[row][col] = null;
            }
        repaint();
        if (preview != null) preview.repaint();
    }

}
}
