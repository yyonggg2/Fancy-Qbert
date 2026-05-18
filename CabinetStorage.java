import java.awt.*;
import java.util.*;
import java.io.*;

public class CabinetStorage {
    private static final String FILE = "cabinet.dat";

    // Format: comma-separated hex color values (null for transparent)
    private static String pixelsToString(Color[][] pixels) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[r].length; c++) {
                if (r > 0 || c > 0) sb.append(",");
                Color color = pixels[r][c];
                sb.append(color == null ? "null" : String.format("%06X", color.getRGB() & 0xFFFFFF));
            }
        }
        return sb.toString();
    }

    // Parses pixel data given explicit row and column counts — handles non-square grids
    private static Color[][] stringToPixels(String s, int rows, int cols) {
        String[] parts = s.split(",");
        Color[][] pixels = new Color[rows][cols];
        for (int i = 0; i < parts.length && i < rows * cols; i++) {
            int r = i / cols;
            int c = i % cols;
            pixels[r][c] = parts[i].equals("null") ? null : new Color(Integer.parseInt(parts[i], 16));
        }
        return pixels;
    }

    // Save format: name:ROWS:COLS:pixeldata
    public static void save(ArrayList<PixelCharacter> cabinet) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE));
            for (PixelCharacter ch : cabinet) {
                Color[][] img = ch.getImage();
                int rows = img.length, cols = img[0].length;
                pw.println(ch.getName() + ":" + rows + ":" + cols + ":" + pixelsToString(img));
            }
            pw.close();
        } catch (IOException e) {
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    public static ArrayList<PixelCharacter> loadLocal() {
        ArrayList<PixelCharacter> cabinetLocal = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File(FILE));
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(":", 4);
                if (parts.length < 4) continue;
                int rows = Integer.parseInt(parts[1]);
                int cols = Integer.parseInt(parts[2]);
                cabinetLocal.add(new PixelCharacter(parts[0], stringToPixels(parts[3], rows, cols)));
            }
            sc.close();
        } catch (IOException e) {
            System.err.println("Load failed");
        }
        return cabinetLocal;
    }
}

    
