package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Player extends Entity
{
	private static final float RUN_SPEED = 20;// units per second
	private static final float TURN_SPEED = 160;// degrees 
	private static final float GRAVITY = -50;// used to calculate jump falling speed
	private static final float JUMP_POWER = 30;// used to calculate jump power speed
	
	//private static final float TERRAIN_HEIGHT = 0;// used for protecting player from fall threw the textures
	
	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private float upwardsSpeed = 0;// how much "y" units player overcomes per second 
	
	private boolean isInAir = false;// used for blocking player "jump" method if we already in the air
	
	public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) 
	{
		super(model, position, rotX, rotY, rotZ, scale);
	}
	
	public void move(Terrain terrain)
	{
		checkInputs();// check state of player control keys
		super.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);// rotation speed measures at "units per seconds", not at "units per frame" so we multiply rotation speed on time which was spent on previous frame
		
		float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));// calculates x-axis movement by multiplying "distance" on sin(angle of current view point). "super.getRotY()" - used as direction pointer(which size of angle on which player model was turned). For explanation "explanations\add#1.png".
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));// calculates y-axis movement by multiplying "distance" on cos(angle of current view point). "super.getRotY()" - used as direction pointer(which size of angle on which player model was turned).For explanation "explanations\add#1.png".	
		super.increasePosition(dx, 0, dz);
		
		upwardsSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();// "GRAVITY" - distance which player overcomes by one second. "upwardsSpeed" - speed which object already have
		
		/*if(super.getPosition().y + upwardsSpeed * DisplayManager.getFrameTimeSeconds() < TERRAIN_HEIGHT)
		{
			upwardsSpeed = TERRAIN_HEIGHT;
			isInAir = false;
		}*/
		
		super.increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);// set final "y" position for rendering
		
		float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);// "terrainHeight" - calculates terrain height at current position
		
		if(super.getPosition().y < terrainHeight)
		{
			upwardsSpeed = terrainHeight;
			isInAir = false;
			super.getPosition().y = terrainHeight;
		}
	}
	
	private void jump()
	{
		if(!isInAir)
		{
			this.upwardsSpeed = JUMP_POWER;
			isInAir = true;
		}
	}
	
	private void checkInputs()
	{
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			this.currentSpeed = RUN_SPEED;
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			this.currentSpeed = -RUN_SPEED;
		}
		else
		{
			this.currentSpeed = 0;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			this.currentTurnSpeed = TURN_SPEED;
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			this.currentTurnSpeed = -TURN_SPEED;
		}
		else
		{
			this.currentTurnSpeed = 0;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
		{
			jump();
		}
	}	
}
