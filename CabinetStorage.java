import java.awt.*;
import java.util.*;
import java.io.*;

public class CabinetStorage {
    private static final String FILE = "cabinet.dat";

    //pixelsToString again
    private static String pixelsToString(Color[][] pixels) {
            StringBuilder sb = new StringBuilder();
            for (int r = 0; r < pixels.length; r++) {
                for (int c = 0; c < pixels[r].length; c++) {
                    Color color = pixels[r][c];
                    if (r > 0 || c > 0) {
                        sb.append(",");
                    }   
                    if (color == null) {
                        sb.append("null");
                    } else {
                        sb.append(String.format("%06X", pixels[r][c].getRGB() & 0xFFFFFF));
                    }
                }
            }        
            return sb.toString();
        }
    
    //stringToPixels again
    private static Color[][] stringToPixels(String s){
            String[] parts = s.split(",");
            int size = (int)Math.sqrt(parts.length);
            Color[][] pixels = new Color[size][size];
            for (int i = 0; i < parts.length; i++){
                String part = parts[i];
                int r = i / size;
                int c = i % size;
                if (part.equals("null")){
                    pixels[r][c] = null;
                }else{
                    pixels[r][c] = new Color(Integer.parseInt(part, 16));
                }
            }
            return pixels;
        }

    public static void save(ArrayList<PixelCharacter> cabinet) {
        try{
            PrintWriter pw = new PrintWriter(new FileWriter(FILE));
            for (PixelCharacter character : cabinet){
                pw.println(character.getName() + ":" + pixelsToString(character.getImage()));
            }
            pw.close();
        } catch (IOException e){
            System.err.println("Save failed " + e.getMessage());
        }
       
    }

    public static ArrayList<PixelCharacter> loadLocal() {
        ArrayList<PixelCharacter> cabinetLocal = new ArrayList<>();
        try{
            Scanner sc = new Scanner(new File(FILE));
            while (sc.hasNextLine()){
                String line = sc.nextLine();
                    String[] parts = line.split(":", 2);
                    cabinetLocal.add(new PixelCharacter(parts[0], stringToPixels(parts[1])));
            }sc.close();
            } catch (IOException e){
            System.err.println("Load failed ");
        }
        return cabinetLocal;
    }
}

    
