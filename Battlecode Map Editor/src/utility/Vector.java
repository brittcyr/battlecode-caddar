package utility;

import map.TileSet;

import java.util.ArrayList;

import map.TileSet;

public class Vector {
	public int x,y;
	public Vector(int xi, int yi){
		x=xi;
		y=yi;
	}
	public Vector rotateRight(){
		return new Vector(y,-x);
	}
	public Vector rotateLeft(){
		return new Vector(-y,x);
	}
	public Vector turnAround(){
		return new Vector(-x,-y);
	}
	public Vector add(Vector v){
		return new Vector(x+v.x,y+v.y);
	}
	public Vector subtract(Vector v){
		return this.add(v.times(-1.0));
	}
	public Vector times(Double m){
		return new Vector((int) (x*m),(int) (y*m));
	}
	public Boolean sameas(Vector v){
		return x==v.x&&y==v.y;
	}
	public int magnitude(){
		return Math.abs(x+y);//this computation is okay because it's always orthogonal motion
	}
	public Vector normal(){
		return this.times(1.0/(double)this.magnitude());
	}
	public int lengthSquared(){//slightly more expensive computation
		return (int)(Math.pow(x,2)+Math.pow(y, 2));
	}
	public double cross(Vector v) {
		return x*v.y-y*v.x;
	}
	public int dot(Vector v){
		return x*v.x+y*v.y;
	}
	public DVector toDVector(){
		return new DVector((double)x,(double)y);
	}
	
	public static ArrayList<Vector> getLine(Vector start, Vector end, TileSet ts){
		//computationally cheap method for finding a line
		ArrayList<Vector> outputLine = new ArrayList<Vector>();
		outputLine.add(start);
		Vector toward = end.subtract(start);
		int overTiles = Math.abs(toward.x);
		int upTiles = Math.abs(toward.y);
		Vector horizontal = toward.x>0?new Vector(1,0):new Vector(-1,0);
		Vector vertical = toward.y>0?new Vector(0,1):new Vector(0,-1);
		Vector workingTile = start;
		//find the majority direction
		Vector majorityDir;
		Vector minorityDir;
		double majorityNumber;
		if(overTiles>upTiles){
			majorityDir = horizontal;
			minorityDir = vertical;
			majorityNumber = (double)overTiles/(double)upTiles;
		}else{
			majorityDir = vertical;
			minorityDir = horizontal;
			majorityNumber = (double)upTiles/(double)overTiles;
		}
		double counter = 0;
		for(int i=0;i<(overTiles+upTiles);i++){
			if(counter<majorityNumber){
				workingTile = workingTile.add(majorityDir);
				counter+=1;
			}else{
				counter = counter - majorityNumber;
				workingTile = workingTile.add(minorityDir);
			}
			outputLine.add(workingTile);
		}
		return outputLine;
	}
}
