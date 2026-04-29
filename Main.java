import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Qbert Pixel Canvas");
        frame.setSize(600, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        CanvasPanel canvas = new CanvasPanel();
        frame.add(canvas, BorderLayout.CENTER);

        // Creation of cabinet and cabinetpanel
        ArrayList<Color[][]> cabinet = new ArrayList<>();
        CabinetPanel cabinetPanel = new CabinetPanel(cabinet);

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

        // Play button — opens the game window with your drawn character as the sprite
        JButton playBtn = new JButton("▶ Play Game");
        playBtn.setFont(new Font("Arial", Font.BOLD, 16));
        playBtn.addActionListener(e -> {
            JFrame gameFrame = new JFrame("Fancy Qbert");
            gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // *** THIS LINE passes your pixel drawing into the game as the character ***
            GamePanel game = new GamePanel(cabinetPanel.getSelected());
            gameFrame.add(game);
            gameFrame.pack();
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setVisible(true);
            game.requestFocusInWindow();
        });

        // Store button — saves your pixel drawing to a the cabinet for later retrieval
        JButton storeBtn = new JButton("Store");
        storeBtn.setFont(new Font("Arial", Font.BOLD, 16));
        storeBtn.addActionListener(e -> {
            // *** THIS LINE saves your pixel drawing to the cabinet for later retrieval ***
            Color[][] original = canvas.getPixels();
            Color[][] copy = new Color[original.length][original[0].length];
            for (int r = 0; r < original.length; r++) {
                for (int c = 0; c < original[r].length; c++) {
                    copy[r][c] = original[r][c];
                }
            }
            cabinet.add(copy);
            cabinetPanel.repaint();
            JOptionPane.showMessageDialog(frame, "Your character has been stored in the cabinet!");
        });

        //Cabinet button (open the cabinet)
        JButton cabinetBtn = new JButton("Cabinet");
        cabinetBtn.setFont(new Font("Arial", Font.BOLD, 16));
        cabinetBtn.addActionListener(e -> {
            JFrame cabinetFrame = new JFrame("Cabinet");
            cabinetFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            cabinetFrame.add(cabinetPanel);
            cabinetFrame.pack();
            cabinetFrame.setVisible(true);

            // Back Button
            JButton backBtn = new JButton("Back");
            backBtn.addActionListener(back -> cabinetFrame.dispose());
            cabinetFrame.add(backBtn, BorderLayout.SOUTH);
        });

        //Clean Button - clean the canvas
        JButton cleanBtn = new JButton("Clean");
        cleanBtn.setFont(new Font("Arial", Font.BOLD, 16));
        cleanBtn.addActionListener(e -> {
            canvas.clean();
        });
    



        // button bar
        JPanel buttonBar = new JPanel();
        buttonBar.add(playBtn);
        buttonBar.add(storeBtn);
        buttonBar.add(cabinetBtn);
        buttonBar.add(cleanBtn);
        frame.add(buttonBar, BorderLayout.NORTH);

        frame.setVisible(true);
        }
    }
