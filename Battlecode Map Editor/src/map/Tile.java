package map;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import utility.Vector;

public class Tile {

	public Vector pos;
	public int component;
	
	public Tile(int x, int y, int c){
		pos = new Vector(x,y);
		component = c;
	}
	
	public void draw(Graphics g,int size,Vector offset){
		if(component== 0){
			g.setColor(new Color(0,0,0));
		}else if(component==1){
			g.setColor(new Color(200,200,200));
		}else if(component==2){
			g.setColor(new Color(100,100,100));
		}else if(component==3){
			g.setColor(new Color(255,0,0));
		}else if(component==4){
			g.setColor(new Color(0,0,255));
		}else if(component<0){
			g.setColor(new Color(0,(int)(-(double)component/9.0*255),0,150));
		}
		g.fillRect(pos.x*size+offset.x, pos.y*size+offset.y, size, size);
	}
}
