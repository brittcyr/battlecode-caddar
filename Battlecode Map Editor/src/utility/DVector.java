package utility;

public class DVector{

	public double x,y;
	
	public DVector(double xi, double yi) {
		x=xi;
		y=yi;
	}

	public DVector rotateRight(){
		return new DVector(y,-x);
	}
	public DVector rotateLeft(){
		return new DVector(-y,x);
	}
	public DVector turnAround(){
		return new DVector(-x,-y);
	}
	public DVector add(DVector v){
		return new DVector(x+v.x,y+v.y);
	}
	public DVector times(Double m){
		return new DVector((x*m), (y*m));
	}
	public Boolean sameas(DVector v){
		return x==v.x&&y==v.y;
	}
	public DVector normal(){
		double length = length();
		if(length>0){
			return this.times(1.0/length);
		}else{
			return new DVector(0.0,0.0);
		}
	}
	public double lengthSquared(){//slightly more expensive computation
		return (Math.pow(x,2)+Math.pow(y, 2));
	}
	public double length(){
		return Math.pow(this.lengthSquared(),0.5);
	}
	public double cross(DVector v) {
		return x*v.y-y*v.x;
	}
	public DVector rotate(double theta){
		return new DVector(x*Math.cos(theta)- y*Math.sin(theta),y*Math.cos(theta)+x*Math.sin(theta));
	}
	public double dot(DVector v){
		return x*v.x+y*v.y;
	}
	public DVector subtract(DVector v){
		return new DVector(x-v.x,y-v.y);
	}
	public double angle(){
		
		double half = Math.atan(y/x);
		if (x==0){
			return (y<0?0:Math.PI);
		}else if(x<0){
			return half+1.5*Math.PI;
		}else{
			return half+0.5*Math.PI;
		}
	}
	public static DVector random(double magnitude){
		return (new DVector(Math.random()-.5,Math.random()-.5)).times(magnitude);
	}
	public Vector toVector(){
		return new Vector((int)Math.round(x),(int)Math.round(y));
	}
}
