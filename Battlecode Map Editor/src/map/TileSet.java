package map;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import utility.Vector;

public class TileSet {
	//contains all the tiles
	//methods for finding mass and other global parameters
	//methods for getting individual tiles according to their coordinates
	public ArrayList<Tile> tiles;
	public int width,height;
	
	public TileSet(int w, int h,int type){
		width=w;
		height=h;
		tiles = new ArrayList<Tile>();
		for(int x=0;x<w;x++){
			for(int y=0;y<h;y++){
				Tile t = new Tile(x,y,type);
				tiles.add(t);
			}
		}
	}
	
	public Tile getTile(Vector pos){
		int loc = pos.y+height*pos.x;
		return tiles.get(loc);
	}
	
	public void render(Graphics g,int size,Vector offset,boolean inSpaceDock){
		for(Tile t:tiles){
			t.draw(g,size,offset);
		}
		if(inSpaceDock){//draw a border around the ship
			g.setColor(new Color(255,200,50,155));
			g.setLineWidth(5);
			g.drawRect(offset.x-1, offset.y-1, size*width+1, size*height+1);
			g.setLineWidth(1);
		}
	}
	
	public boolean noShipPresent(){
		for(Tile t:tiles){
			if(t.component!=0){
				return false;
			}
		}
		return true;
	}
	
}
