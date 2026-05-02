import java.awt.*;
import java.util.*;

public class PixelCharacter {
    public String name;
    public Color[][] pixels;

    public PixelCharacter(String name, Color[][] pixels){
        this.name = name;
        this.pixels = pixels;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setPixels(Color[][] pixels){
        this.pixels = pixels;
    }

    public String getName(){
        return name;
    }

    public Color[][] getImage(){
        return pixels;
    }
}
