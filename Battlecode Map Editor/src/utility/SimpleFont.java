package utility;

import java.awt.Color;
import java.awt.Font;
import java.util.Dictionary;
import java.util.Hashtable;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

public class SimpleFont {

	private UnicodeFont font;
	//Store fonts so that they are not reloaded, since that's slow and barfs to console
	//Use a dictionary that goes from name+style+size+color to the font
	private static Dictionary<String,UnicodeFont> loadedFonts = new Hashtable<String,UnicodeFont>();
	
    public SimpleFont(String fontName, int style, int size, Color color) throws SlickException {
    	String key = fontName+style+size+color.getRed()+color.getGreen()+color.getBlue();
    	if(loadedFonts.get(key)==null){
    		this.font = new UnicodeFont(new Font(fontName, style, size));
    		ColorEffect colorEffect = new ColorEffect(color);
    		this.font.getEffects().add(colorEffect);
    		this.font.addNeheGlyphs();
    		this.font.loadGlyphs();
    		loadedFonts.put(key, font);
    	}else{
    		this.font = loadedFonts.get(key);
    	}
    }
    public UnicodeFont get(){
    	return font;
    }
    /* List of safe fonts I consider decent
     * Arial
     * Courier New
     * Tahoma
     * Times New Roman
     * Trebuchet MS
     * Verdana
     */
}
