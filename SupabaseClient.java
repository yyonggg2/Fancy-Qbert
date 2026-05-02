import java.util.*;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.awt.Color;

public class SupabaseClient {
    private static String URL;
    private static String KEY;

    static {
        try {
            Scanner sc = new Scanner(new File("supabase.config"));
            for (int i = 0; i < 2; i++) {
                if (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (line.startsWith("URL=")) {
                        URL = line.substring(4);
                    } else if (line.startsWith("KEY=")) {
                        KEY = line.substring(4);
                    }
                }
            } 
            } catch (FileNotFoundException e) {
            System.err.println("Supabase config file not found!");
            e.printStackTrace();
            }
        }

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

    public static boolean upload(String name, Color[][] pixels){
        try{
            String json = "{\"name\":\"" + name + "\",\"pixels\":\"" + pixelsToString(pixels) + "\"}";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/rest/v1/characters"))
                .header("apikey", KEY)
                .header("Authorization", "Bearer " + KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201;

        }catch(Exception e){
           System.out.println("Upload failed: " + e.getMessage());
           return false;
        }

    }

    public static List<PixelCharacter> fetchGlobal(){
        ArrayList<PixelCharacter> resultGlobal = new ArrayList<>();
        try{
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/rest/v1/characters?select=name,pixels"))
                .header("apikey", KEY)
                .header("Authorization", "Bearer " + KEY)
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            String[] entries = body.split("\"pixels\":\"");
            String[] names = body.split("\"name\":\"");
            for (int i = 1; i < entries.length; i++) {
                String pixelStr = entries[i].split("\"")[0];
                String name = names[i].split("\"")[0];
                resultGlobal.add(new PixelCharacter(name, stringToPixels(pixelStr)));
            }
        }catch(Exception e){
           System.out.println("Fetch failed: " + e.getMessage());
        }
        return resultGlobal;
    }


}