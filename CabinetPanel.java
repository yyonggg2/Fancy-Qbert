import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;


public class CabinetPanel extends JPanel {
    private ArrayList<PixelCharacter> cabinet;
    private int selectedIndex = -1;

    // receive the canbinet list
    public CabinetPanel(ArrayList<PixelCharacter> cabinet) {
        this.cabinet = cabinet;
        setPreferredSize(new Dimension(500, 135));
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
            Color[][] pixels = cabinet.get(i).getImage();
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
            g.drawRect(x - 1, 9, 122, 122); // 20 pixels * 6 size + 2 for border
            g.setColor(Color.WHITE);
            g.drawString(cabinet.get(i).getName(), x, 150);
            setPreferredSize(new Dimension(500, 140));
            x += 130; // 20 pixels * 6 size + 10 padding
        }
    }

    public Color[][] getSelected(){
        if (selectedIndex == -1 || cabinet.isEmpty()){
            return null;
        }
        return cabinet.get(selectedIndex).getImage();
    }

    public void setList(ArrayList<PixelCharacter> list) {
        this.cabinet = list;
        repaint();
    }



}
