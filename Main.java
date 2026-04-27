import javax.swing.JFrame;
public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Qbert Pixel Canvas");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        CanvasPanel canvas = new CanvasPanel();
        frame.add(canvas);

    }
}