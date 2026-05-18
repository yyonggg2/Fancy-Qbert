import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

public class CabinetPanel extends JPanel {
    private ArrayList<PixelCharacter> cabinet;
    private int selectedIndex = -1;

    private static final int COLS     = 4;   // characters per row
    private static final int SLOT_W   = 130; // px wide per slot
    private static final int SLOT_H   = 155; // px tall per slot (thumbnail + name)
    private static final int THUMB    = 120; // thumbnail drawing area
    private static final int MARGIN_X = 10;
    private static final int MARGIN_Y = 10;

    public CabinetPanel(ArrayList<PixelCharacter> cabinet) {
        this.cabinet = cabinet;
        setBackground(Color.DARK_GRAY);
        updatePreferredSize();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int col = (e.getX() - MARGIN_X) / SLOT_W;
                int row = (e.getY() - MARGIN_Y) / SLOT_H;
                if (col < 0 || col >= COLS) return;
                int clicked = row * COLS + col;
                if (clicked >= 0 && clicked < cabinet.size()) {
                    selectedIndex = clicked;
                    repaint();
                }
            }
        });
    }

    private void updatePreferredSize() {
        int rows = Math.max(1, (int) Math.ceil((double) cabinet.size() / COLS));
        setPreferredSize(new Dimension(MARGIN_X + COLS * SLOT_W, MARGIN_Y + rows * SLOT_H));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < cabinet.size(); i++) {
            int col  = i % COLS;
            int row  = i / COLS;
            int x    = MARGIN_X + col * SLOT_W;
            int y    = MARGIN_Y + row * SLOT_H;

            Color[][] pixels = cabinet.get(i).getImage();
            // Scale to fit within THUMB x THUMB
            int imgH = pixels.length;
            int imgW = imgH > 0 ? pixels[0].length : 1;
            int scale = Math.max(1, Math.min(THUMB / imgW, THUMB / imgH));

            for (int r = 0; r < imgH; r++) {
                for (int c = 0; c < imgW; c++) {
                    Color color = pixels[r][c];
                    g.setColor(color != null ? color : Color.WHITE);
                    g.fillRect(x + c * scale, y + r * scale, scale, scale);
                }
            }

            // Border — yellow when selected, white otherwise
            g.setColor(selectedIndex == i ? Color.YELLOW : Color.WHITE);
            g.drawRect(x - 1, y - 1, THUMB + 1, THUMB + 1);

            // Name label below thumbnail
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            String name = cabinet.get(i).getName();
            FontMetrics fm = g.getFontMetrics();
            int nameX = x + (THUMB - fm.stringWidth(name)) / 2;
            g.drawString(name, Math.max(x, nameX), y + THUMB + 14);
        }
    }

    public Color[][] getSelected() {
        if (selectedIndex == -1 || cabinet.isEmpty()) return null;
        return cabinet.get(selectedIndex).getImage();
    }

    public void setList(ArrayList<PixelCharacter> list) {
        this.cabinet = list;
        selectedIndex = -1;
        updatePreferredSize();
        revalidate();
        repaint();
    }
}
