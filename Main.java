import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Qbert Pixel Canvas");
        frame.setMinimumSize(new Dimension(800, 700));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        CanvasPanel canvas = new CanvasPanel();
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 0, 8, 0);
        JLabel canvasLabel = new JLabel("Create your own character overhere!");
        canvasLabel.setForeground(Color.WHITE);
        canvasLabel.setFont(new Font("Papyrus", Font.BOLD, 24));
        centerWrapper.add(canvasLabel, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 0, 0);
        centerWrapper.add(canvas, gbc);

        frame.add(centerWrapper, BorderLayout.CENTER);

        // Creation of cabinet and cabinetpanel
        ArrayList<PixelCharacter> localCabinet = CabinetStorage.loadLocal();
            if (localCabinet.stream().noneMatch(c -> c.name.equals("Q*bert"))) {
                localCabinet.add(0, new PixelCharacter("Q*bert", createQbertPixels()));
                CabinetStorage.save(localCabinet);
            }
            if (localCabinet.stream().noneMatch(c -> c.name.equals("Q*bert 2"))) {
                localCabinet.add(1, new PixelCharacter("Q*bert 2", loadSpritePixels("/Users/student/Desktop/FANCY Qbert/sprites/Player/qbert2_sprite.png")));
                CabinetStorage.save(localCabinet);
            }

        ArrayList<PixelCharacter> globalCabinet = new ArrayList<>();

        CabinetPanel cabinetPanel = new CabinetPanel(localCabinet);
        
        /*Color Palette */
        JPanel palette = new JPanel();
        frame.add(palette, BorderLayout.SOUTH);
        Color[] recentColors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.WHITE};
        JButton[] colorButtons = new JButton[8];
        for (int i = 0; i < recentColors.length; i++) {
            JButton colorButton = new JButton();
            colorButton.setBackground(recentColors[i]);
            colorButton.setPreferredSize(new Dimension(50, 50));
            colorButton.setOpaque(true);
            colorButton.setBorderPainted(false);
            final int idx = i;
            colorButton.addActionListener(e -> canvas.setColor(recentColors[idx]));
            palette.add(colorButton);
            colorButtons[i] = colorButton;
        }


        //Erase Button
        JButton eraseBtn = new JButton("Eraser");
        eraseBtn.setFont(new Font("Arial", Font.BOLD, 16));
        eraseBtn.addActionListener(e -> canvas.setColor(null));
        palette.add(eraseBtn);

        //Color Picker Button
        JButton colorPickerBtn = new JButton("Custom Color");
        colorPickerBtn.setFont(new Font("Arial", Font.BOLD, 16));
        colorPickerBtn.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(frame, "Choose Color", Color.BLACK);
            if (selectedColor != null) {
                canvas.setColor(selectedColor);
                // making sure the recent colors list is updated when you pick a custom color, and limit the size to 8
                for (int j = recentColors.length - 1; j > 0; j--) {
                    recentColors[j] = recentColors[j-1];
                    }
                    recentColors[0] = selectedColor;
                    for (int j = 0; j < colorButtons.length; j++) {
                        colorButtons[j].setBackground(recentColors[j]);
                    }

                }
        });
        palette.add(colorPickerBtn);

        /*Preview Panel */
        PreviewPanel preview = new PreviewPanel(canvas);
        frame.add(preview, BorderLayout.EAST);
        canvas.setPreview(preview);

        // Bubble text field — player types what their character says when hit (15 char max)
        JTextField bubbleField = new JTextField("@#$%!", 10);
        ((AbstractDocument) bubbleField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (fb.getDocument().getLength() + string.length() <= 40)
                    super.insertString(fb, offset, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                int newLen = fb.getDocument().getLength() - length + text.length();
                if (newLen <= 40)
                    super.replace(fb, offset, length, text, attrs);
            }
        });
        bubbleField.setFont(new Font("Arial", Font.PLAIN, 14));

        // Play button — opens the game window with your drawn character as the sprite
        JButton playBtn = new JButton("▶ Play Game");
        playBtn.setFont(new Font("Arial", Font.BOLD, 16));
        playBtn.addActionListener(e -> {
            JFrame gameFrame = new JFrame("Fancy Qbert");
            gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            GamePanel game = new GamePanel(cabinetPanel.getSelected(), bubbleField.getText());
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
            boolean isEmpty = true;
            Color[][] original = canvas.getPixels();
            for (Color[] row : original) {
                for (Color cell : row) {
                    if (cell != null) { isEmpty = false; break; }
                }
            }
            if (isEmpty) {
                JOptionPane.showMessageDialog(frame, "Cannot store an empty character.");
                return;
            }
            Color[][] copy = new Color[original.length][original[0].length];
            for (int r = 0; r < original.length; r++) {
                for (int c = 0; c < original[r].length; c++) {
                    copy[r][c] = original[r][c];
                }
            }
            String name = JOptionPane.showInputDialog(frame, "Enter a name for your character:");
            if (name == null || name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a name.");
                return;
            }
            String[] options = {"Private", "Public"};
            int choice = JOptionPane.showOptionDialog(frame, "How do you want to store this character?", "Store",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            localCabinet.add(new PixelCharacter(name, copy));
            CabinetStorage.save(localCabinet);
            if (choice == 1) {
                boolean success = SupabaseClient.upload(name, copy);
                System.out.println("Upload success: " + success);
            }
            cabinetPanel.repaint();
            JOptionPane.showMessageDialog(frame, "Your character has been stored!");
        });
    


        //Cabinet button (open the cabinet)
        JButton cabinetBtn = new JButton("Cabinet");
        cabinetBtn.setFont(new Font("Arial", Font.BOLD, 16));
        cabinetBtn.addActionListener(e -> {
            JFrame cabinetFrame = new JFrame("Cabinet");
            cabinetFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            cabinetFrame.add(cabinetPanel);

            // Back Button
            JButton backBtn = new JButton("Back");
            backBtn.addActionListener(back -> cabinetFrame.dispose());
            cabinetFrame.add(backBtn, BorderLayout.SOUTH);

            // Load Global Button
            JButton loadCommunityBtn = new JButton("Global");
            loadCommunityBtn.addActionListener(s -> {
                List<PixelCharacter> globalCharacters = SupabaseClient.fetchGlobal();
                globalCabinet.clear();
                globalCabinet.addAll(globalCharacters);
                cabinetPanel.setList(globalCabinet);
            });

            // Load Local Button
            JButton loadPrivateBtn = new JButton("Private");
            loadPrivateBtn.addActionListener(i -> {
                cabinetPanel.setList(localCabinet);
            });

            JPanel cabinetBar = new JPanel();
            cabinetBar.add(loadPrivateBtn);
            cabinetBar.add(loadCommunityBtn);
            cabinetFrame.add(cabinetBar, BorderLayout.NORTH);

            cabinetFrame.pack();
            cabinetFrame.setVisible(true);

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
        buttonBar.add(new JLabel("Death quote:"));
        buttonBar.add(bubbleField);
        frame.add(buttonBar, BorderLayout.NORTH);

        frame.setVisible(true);
    }

    private static Color[][] loadSpritePixels(String path) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(path));
            int h = img.getHeight(), w = img.getWidth();
            Color[][] pixels = new Color[h][w];
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    int argb = img.getRGB(c, r);
                    int alpha = (argb >> 24) & 0xff;
                    if (alpha > 10) pixels[r][c] = new Color(argb, true);
                }
            }
            return pixels;
        } catch (Exception e) {
            System.out.println("Could not load sprite: " + path + ": " + e.getMessage());
            return new Color[20][20];
        }
    }

    // Loads the Q*bert sprite from qbert_sprite.png — transparent pixels become null
    private static Color[][] createQbertPixels() {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(
                new java.io.File("/Users/student/Desktop/FANCY Qbert/sprites/Player/qbert_sprite.png"));
            int h = img.getHeight(), w = img.getWidth();
            Color[][] pixels = new Color[h][w];
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    int argb = img.getRGB(c, r);
                    int alpha = (argb >> 24) & 0xff;
                    if (alpha > 10) pixels[r][c] = new Color(argb, true);
                }
            }
            return pixels;
        } catch (Exception e) {
            System.out.println("Could not load qbert_sprite.png: " + e.getMessage());
            return new Color[20][20];
        }
    }
}