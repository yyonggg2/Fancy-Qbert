import javax.swing.*;
import java.awt.*; 

public class PreviewPanel extends JPanel{
    private static final int PIXEL_SIZE = 6;
    private CanvasPanel canvas;

    public PreviewPanel(CanvasPanel canvas) {
        this.canvas = canvas;
    }

    @Override
    public Dimension getPreferredSize() {
            return new Dimension(canvas.getGridSize() * PIXEL_SIZE, canvas.getGridSize() * PIXEL_SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < canvas.getGridSize(); row++) {
            for (int col = 0; col < canvas.getGridSize(); col++) {
                Color c = canvas.getPixels()[row][col];
                if (c != null) {
                    g.setColor(c);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(col * PIXEL_SIZE, row * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);

            }
        }

}
}
