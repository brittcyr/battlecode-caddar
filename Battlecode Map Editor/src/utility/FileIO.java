package utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class FileIO {
	public static ArrayList<String> importLines(String filename){
		ArrayList<String> lines = new ArrayList<String>();
		try{
		//load image filenames from a resource text file
		BufferedReader br = null;
		try { br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"US-ASCII"));
		} catch (UnsupportedEncodingException e) {e.printStackTrace();
		} catch (FileNotFoundException e) {System.out.println("File not found. Returning empty lines.");return lines;}

		String str = "start";
		while(str!= null){
			try {str = br.readLine();
			} catch (IOException e) {e.printStackTrace();}
			if(str!=null){lines.add(str);}
		}
		br.close();
		}catch (Exception e){
			e.printStackTrace();
			return new ArrayList<String>();
		}
		return lines;
	}
	public static int exportLines(ArrayList<String> text, String fname){
		try{
		PrintWriter out = new PrintWriter(new FileWriter(fname)); 
		for(String t:text){
			out.println(t);
		}
		out.close();
		}catch (Exception e){
			e.printStackTrace();
			return -1;
		}
		return 1;
	}
	public static Double[] importOneColumnData(String filename){
		ArrayList<String> lines = importLines(filename);
		ArrayList<Double> doubles = new ArrayList<Double>();
		for(String s:lines){
			doubles.add(Double.parseDouble(s));
		}
		return doubles.toArray(new Double[doubles.size()]);
	}
	public static double interpolatePoint(Double[] values, double tstep, double t){
		int len = values.length;
		int leftSide = ((int) Math.floor(t/tstep))%len;//wrapped interpolation - edge cases disabled below
		int rightSide = (leftSide + 1)%len;
		double maxT = tstep*len;
//		if(rightSide>values.length)//query outside range -> return far
//			return values[values.length];
//		if(leftSide<0)//query outside range -> return near
//			return values[0];
		double frac = ((t%maxT)/tstep-leftSide);
		return values[leftSide]+(values[rightSide]-values[leftSide])*frac;
	}
	public static Double[] interpolate(Double[] values, double tstep, Double[] times){
		Double[] results = new Double[times.length];
		for(int i=0;i<times.length;i++){
			results[i] = interpolatePoint(values,tstep,times[i]);
		}
		return results;
	}
}
