package rooms;

import java.util.ArrayList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.Color;

import buttons.TextField;

import utility.DVector;
import utility.FileIO;
import utility.MapRepresentation;
import utility.SimpleFont;
import utility.Vector;
import map.TileSet;

public class Room1 extends BasicGameState {
	private StateBasedGame game;
	public GameContainer gameContainer;
	public static UnicodeFont f;
	public static TileSet map;
	public static TileSet cowMap;
	public static boolean editCows = false;
	static Vector mapOffset;
	public static int tileSize=16;
	static Input input;
	static Vector start,end;
	//clickable interface
	static ArrayList<TextField> fields = new ArrayList<TextField>();
	static TextField selectedField;
	static int selected = 0;
	
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		this.game=sbg;
		this.gameContainer=gc;
		f = (new SimpleFont("Courier",0, 20,new java.awt.Color(255,255,255))).get();
		UnicodeFont blackFont = (new SimpleFont("Courier",0, 20,new java.awt.Color(0,0,0))).get();
		input = new Input(SetupClass.screenHeight);
		mapOffset = new Vector(200,80);
		fields.add(new TextField("30", new Vector (30,50),new Vector (50,20), blackFont,"Width",f,0));
		fields.add(new TextField("30", new Vector (30,80),new Vector (50,20), blackFont,"Height",f,0));
		fields.add(new TextField("16", new Vector (30,110),new Vector (50,20), blackFont,"Pixels",f,0));
		fields.add(new TextField("New map", new Vector (30,140),new Vector (120,30), blackFont,null,null,3));
		fields.add(new TextField("Land", new Vector (30,200),new Vector (100,30), blackFont,null,null,4));
		fields.add(new TextField("Road", new Vector (30,240),new Vector (100,30), blackFont,null,null,5));
		fields.add(new TextField("Wall", new Vector (30,280),new Vector (100,30), blackFont,null,null,6));
		fields.add(new TextField("HQ", new Vector (30,320),new Vector (100,30), blackFont,null,null,7));
		fields.add(new TextField("Cow Field", new Vector (30,400),new Vector (120,30), blackFont,null,null,8));
		fields.add(new TextField("1", new Vector (30,440),new Vector (50,20), blackFont,"Cow Gen",f,0));
		fields.add(new TextField("1337", new Vector (30,500),new Vector (70,20), blackFont,"Seed",f,0));
		fields.add(new TextField("2000", new Vector (30,540),new Vector (70,20), blackFont,"Rounds",f,0));
		fields.add(new TextField("map1", new Vector (200,50),new Vector (300,20), blackFont,"Filename",f,0));
		fields.add(new TextField("Save", new Vector (650,50),new Vector (80,20), blackFont,null,null,1));
		fields.add(new TextField("Load", new Vector (750,50),new Vector (80,20), blackFont,null,null,2));
		makeNewMap();
	}
	private static void makeNewMap() {
		int width = Integer.parseInt(fields.get(0).text);
		int height = Integer.parseInt(fields.get(1).text);
		int newTileSize = Integer.parseInt(fields.get(2).text);
		if(map!=null&&
				(width==map.width&&height==map.height&&newTileSize!=tileSize)
				){
			tileSize=newTileSize;//can reset tile size without redoing map
		}else{
			map = new TileSet(width,height,1);
			cowMap = new TileSet(width,height,-1);
		}
	}
	public static void internalButtonClick(int code){//intercepts button presses
		switch(code){
		case 1://save
			int seed = Integer.parseInt(fields.get(10).text);
			int rounds = Integer.parseInt(fields.get(11).text);
			MapRepresentation.save(map,cowMap,seed,rounds,fields.get(12).text);
			break;
		case 2://load
			load(fields.get(12).text);
			break;
		case 3://new map
			makeNewMap();
			break;
		case 4://land
			selected = 1;
			break;
		case 5://road
			selected = 0;
			break;
		case 6://wall
			selected = 2;
			break;
		case 7://hq
			selected = 3;
			break;
		case 8://show cow field
			editCows=!editCows;
			break;
		}
	}

	public void update(GameContainer gc, StateBasedGame sbg, int delta){
	}
	public Vector getClick(){
		//compute the clicked tile
		Vector clicked = (new Vector(input.getMouseX(),input.getMouseY())).add(mapOffset.times(-1.0)).times(1.0/tileSize);
		//check if it's on the map
		if(clicked.x>=0&&clicked.x<map.width
				&&clicked.y>=0&&clicked.y<map.height
				){
			return clicked;
		}else{
			return null;
		}
	}
	
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		f.drawString(10, 5, "Battlecode Map Editor");
		//draw map
		map.render(g, tileSize, mapOffset, true);
		if(editCows)
			cowMap.render(g,tileSize,mapOffset,false);
		//predraw line
		g.setColor(new Color(255,200,0));
		g.setLineWidth(3);
		if(input.isMouseButtonDown(0)){
			Vector hover = getClick();
			if(hover!=null&&start!=null)
				g.drawLine(start.x*tileSize+mapOffset.x+tileSize/2, 
						start.y*tileSize+mapOffset.y+tileSize/2, 
						hover.x*tileSize+mapOffset.x+tileSize/2, 
						hover.y*tileSize+mapOffset.y+tileSize/2);
		}else if(input.isMouseButtonDown(1)){//predraw rectangle
			Vector hover = getClick();
			if(hover!=null&&start!=null)
				g.drawRect(start.x*tileSize+mapOffset.x+tileSize/2, 
						start.y*tileSize+mapOffset.y+tileSize/2, 
						(hover.x-start.x)*tileSize, 
						(hover.y-start.y)*tileSize);
		}
		//draw text fields
		for(TextField tf:fields){
			tf.render(g);
		}
	}
	public void mousePressed(int button, int x, int y){
		//check click on tile field
		start =getClick();
		// Check text fields.
		for(TextField tf:fields){
			if(tf.clicked(x, y))
				selectedField=tf;
		}
	}
	public void mouseReleased(int button, int x, int y){
		//drawing functions
		ArrayList<Vector> toChange;
		if(start!=null){
			end=getClick();
			if(end!=null){
				if(button==0){//release left click --> draw line
					toChange = Vector.getLine(start,end,map);
					for(Vector v:toChange){
						symmetricalChange(v,selected);
						//map.getTile(v).component = selected;
					}
				}else if(button==1){//release right click --> draw filled box
					int startx = Math.min(start.x, end.x);
					int endx = Math.max(start.x,end.x);
					int starty = Math.min(start.y,end.y);
					int endy = Math.max(start.y, end.y);
					for(int i=startx;i<=endx;i++){
						for(int j=starty;j<=endy;j++){
							symmetricalChange(new Vector(i,j),selected);
							//map.getTile(new Vector(i,j)).component = selected;
						}
					}
				}
			}
		}
	}
	
	public void symmetricalChange(Vector position, int val){
		DVector mapCenter = new DVector(map.width,map.height).times(0.5);
		DVector relativePosition = position.toDVector().subtract(mapCenter);
		Vector position2 = mapCenter.add(relativePosition.rotate(Math.PI)).toVector().subtract(new Vector(1,1));
		if(!editCows){
			map.getTile(position).component = val;
			map.getTile(position2).component = val==3?4:val;//different HQs
		}else{
			val = -Integer.parseInt(fields.get(9).text);
			cowMap.getTile(position).component = val;
			cowMap.getTile(position2).component = val;//different HQs
		}
	}
	
	public void keyPressed(int key, char c){
		if(selectedField!=null)
			selectedField.text = selectedField.text+c;
	}
	
	public static void load(String fname){
		ArrayList<String> text = FileIO.importLines(fname);
		if(text.size()!=0){
			//get height and width, seed and rounds
			fields.get(0).text = getXMLParam(text.get(1),"height");
			fields.get(1).text = getXMLParam(text.get(1),"width");
			fields.get(10).text = getXMLParam(text.get(2),"seed");
			fields.get(11).text = getXMLParam(text.get(2),"rounds");
			makeNewMap();
			for(int lineNum = 0;lineNum<map.height;lineNum++){
				String[] parts = text.get(lineNum+12).split(" ");
				for(int pNum = 0;pNum<parts.length;pNum++){
					String info = parts[pNum];
					int component = 1;
					switch(info.substring(0,1)){
					case "r":
						component = 0;
						break;
					case "n":
						component = 1;
						break;
					case "v":
						component = 2;
						break;
					case "a":
						component = 3;
						break;
					case "b":
						component = 4;
						break;
					}
					Vector pos = new Vector(pNum,lineNum);
					map.getTile(pos).component=component;
					cowMap.getTile(pos).component=-Integer.parseInt(info.substring(1,2));
				}
			}
		}
	}

	public static String getXMLParam(String line, String name){
		String actualName = name+"=\"";
		int nameWidth = actualName.length();
		int start = line.indexOf(actualName)+nameWidth;
		int end = line.indexOf("\"",start);
		return line.substring(start,end);
	}
	
	public int getID() {
		return 0;
	}

}
