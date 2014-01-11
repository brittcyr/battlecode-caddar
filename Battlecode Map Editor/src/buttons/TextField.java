package buttons;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.UnicodeFont;

import rooms.Room1;

import utility.Vector;

public class TextField {
	public String text;
	public Vector position;
	public Vector size;
	public UnicodeFont boxFont;
	public boolean selected;
	public String label;
	public UnicodeFont labelFont;
	public int callbackNumber;
	
	
	public TextField(String t, Vector p, Vector s, UnicodeFont bf, String l, UnicodeFont lf, int cn){
		text =t;
		position=p;
		size=s;
		boxFont = bf;
		label = l;
		labelFont = lf;
		callbackNumber = cn;
	}
	
	public boolean clicked(int x, int y){
		boolean clicked = (x>position.x&&x<position.x+size.x&&y>position.y&&y<position.y+size.y);
		if(label!=null){//when a text box is clicked, delete the text. Also it's drawn differently.
			if(clicked){
				text = "";
				selected = true;
			}else{
				selected = false;
			}
			return clicked;
		}
		if(clicked)
			Room1.internalButtonClick(callbackNumber);
		return false;//internal buttons always pretend they are not clicked so that they don't get edited.
	}
	
	public void render(Graphics g){
		if(label!=null){
			labelFont.drawString(position.x+size.x+10,position.y-4,label);
			g.setColor(new Color(255,255,255));
		}else{
			g.setColor(new Color(220,220,255));
		}
		g.fillRect(position.x, position.y, size.x, size.y);
		if(selected){
			g.setColor(new Color(255,255,255));
		}
		g.setLineWidth(2);
		g.drawRect(position.x, position.y,size.x,size.y);
		boxFont.drawString(position.x+4, position.y-4, text);
	}
	
}