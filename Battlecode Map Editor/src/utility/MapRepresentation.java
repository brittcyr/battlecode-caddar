package utility;

import java.util.ArrayList;

import map.TileSet;

public class MapRepresentation {
	public static void save(TileSet terrain, TileSet cows, int seed, int rounds, String fname){
		ArrayList<String> text = mapsToText(terrain,cows,seed,rounds);
		FileIO.exportLines(text, fname);
	}
	
	public static ArrayList<String> mapsToText(TileSet terrain, TileSet cows, int seed, int rounds){
		ArrayList<String> text = new ArrayList<String>();
		text.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.add("<map height=\""+terrain.height+"\" width=\""+terrain.width+"\">");
		text.add("    <game seed=\""+seed+"\" rounds=\""+rounds+"\"/>");
		text.add("    <symbols>");
		text.add("        <symbol terrain=\"NORMAL\" type=\"TERRAIN\" character=\"n\"/>");
		text.add("        <symbol terrain=\"VOID\" type=\"TERRAIN\" character=\"v\"/>");
		text.add("        <symbol terrain=\"ROAD\" type=\"TERRAIN\" character=\"r\"/>");
		text.add("        <symbol team=\"A\" type=\"HQ\" character=\"a\"/>");
		text.add("        <symbol team=\"B\" type=\"HQ\" character=\"b\"/>");
		text.add("    </symbols>");
		text.add("    <data>");
		text.add("<![CDATA[");
		for(int y=0;y<terrain.height;y++){
			String aline = "";
			for(int x=0;x<terrain.width;x++){
				aline = aline+getCode(terrain,cows,x,y)+" ";
			}
			text.add(aline.trim());
		}
		text.add("]]>");
		text.add("    </data>");
		text.add("</map>");
		return text;
	}
	public static String getCode(TileSet terrain, TileSet cows, int x,int y){
		int tc = terrain.getTile(new Vector(x,y)).component;
		String ts="x";
		switch(tc){
		case 0:
			ts="r";
			break;
		case 1:
			ts="n";
			break;
		case 2:
			ts="v";
			break;
		case 3:
			ts="a";
			break;
		case 4:
			ts="b";
			break;
		}
		int cc = -cows.getTile(new Vector(x,y)).component;
		String cs = ""+cc;
		return ts+cs;
	}
}