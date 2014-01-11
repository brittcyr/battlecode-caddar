package rooms;

import java.awt.Color;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import utility.SimpleFont;

public class Room2 extends BasicGameState {
	private StateBasedGame game;
	public GameContainer gameContainer;
	public static UnicodeFont f;
	
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		this.game=sbg;
		this.gameContainer=gc;
		f = (new SimpleFont("Courier",0, 20,new Color(255,255,255))).get();
	}
	public void update(GameContainer gc, StateBasedGame sbg, int delta){
	}
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		f.drawString(10, 50, "Battlecode Map Editor");
	}
	public void mousePressed(int button, int x, int y){
		
	}
	public void keyPressed(int key, char c){
	}
	public int getID() {
		return 1;
	}

}
