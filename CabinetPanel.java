import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;


public class CabinetPanel extends JPanel {
    private ArrayList<Color[][]> cabinet;
    private int selectedIndex = -1;

    // receive the canbinet list
    public CabinetPanel(ArrayList<Color[][]> cabinet) {
        this.cabinet = cabinet;
        setPreferredSize(new Dimension(500, 120));
        setBackground(Color.DARK_GRAY);

        // Mouse listener for selection
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int clickedIndex = e.getX() / 106;
                if (clickedIndex < cabinet.size()){
                    selectedIndex = clickedIndex;
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = 10;
        for (int i = 0; i < cabinet.size(); i++){
            Color[][] pixels = cabinet.get(i);
            for (int r = 0; r < pixels.length; r++){
                for (int c = 0; c < pixels[r].length; c++){
                    Color color = pixels[r][c];
                    if (color != null){
                        g.setColor(color);
                        g.fillRect(x + c * 6, 10 + r * 6, 6, 6);
                    }else{
                        g.setColor(Color.WHITE);
                        g.fillRect(x + c * 6, 10 + r * 6, 6, 6);
                    }
                }
            }
            // The light border for the thumbnails
            g.setColor(selectedIndex == i ? Color.YELLOW : Color.WHITE);
            g.drawRect(x - 1, 9, 98, 98);
            x += 106; // 16 pixels * 6 size + 10 padding
        }
    }

    public Color[][] getSelected(){
        if (selectedIndex == -1 || cabinet.isEmpty()){
            return null;
        }
        return cabinet.get(selectedIndex);
    }


}
