package rooms;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class SetupClass extends StateBasedGame{

	public static int screenHeight = 800;//was 600
	public static int screenWidth = 1200;//was 1500


	public SetupClass(String name) {
		super(name);
	}
	
	public static void main(String[] args) throws SlickException{
		AppGameContainer app = new AppGameContainer(new SetupClass("Battlecode 2014 Map Editor- by Max Mann"));
		app.setDisplayMode(screenWidth, screenHeight, false);
		app.setShowFPS(false);
		app.setTargetFrameRate(60);
		app.start();
	}
	
	@Override
	public void initStatesList(GameContainer arg0) throws SlickException {
		addState(new Room1());
		addState(new Room2());
	}
	
}
