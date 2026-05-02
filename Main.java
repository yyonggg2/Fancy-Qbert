import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Qbert Pixel Canvas");
        frame.setSize(600, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        CanvasPanel canvas = new CanvasPanel();
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(Color.BLACK);
        centerWrapper.add(canvas);
        frame.add(centerWrapper, BorderLayout.CENTER);

        // Creation of cabinet and cabinetpanel
        ArrayList<PixelCharacter> localCabinet = CabinetStorage.loadLocal();
        ArrayList<PixelCharacter> globalCabinet = new ArrayList<>();

        CabinetPanel cabinetPanel = new CabinetPanel(localCabinet);
        
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
            }
        });
        palette.add(colorPickerBtn);

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
        frame.add(buttonBar, BorderLayout.NORTH);

        frame.setVisible(true);
    }
}