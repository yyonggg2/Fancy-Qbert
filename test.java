import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class test {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("qbert.png"));
        BufferedImage scaled = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(img.getScaledInstance(20, 20, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 20; r++) {
            for (int c = 0; c < 20; c++) {
                if (r > 0 || c > 0) sb.append(",");
                int rgb = scaled.getRGB(c, r);
                int alpha = (rgb >> 24) & 0xFF;
                if (alpha == 0) sb.append("null");
                else sb.append(String.format("%06X", rgb & 0xFFFFFF));
            }
        }
        System.out.println("qbert:" + sb.toString());
    }
}
