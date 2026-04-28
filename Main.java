import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Qbert Pixel Canvas");
        frame.setSize(600, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        CanvasPanel canvas = new CanvasPanel();
        frame.add(canvas, BorderLayout.CENTER);

        /*Color Palette */
        JPanel palette = new JPanel();
        frame.add(palette, BorderLayout.SOUTH);
        Color[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.MAGENTA, Color.CYAN};
        for (Color c : colors) {
            JButton colorButton = new JButton();
            colorButton.setBackground(c);
            colorButton.setPreferredSize(new Dimension(50, 50));
            colorButton.setOpaque(true);
            colorButton.setBorderPainted(false);
            colorButton.addActionListener(e -> canvas.setColor(c));
            palette.add(colorButton);
        }

        /*Preview Panel */
        PreviewPanel preview = new PreviewPanel(canvas);
        frame.add(preview, BorderLayout.EAST);
        canvas.setPreview(preview);

        frame.setVisible(true);

    }
}