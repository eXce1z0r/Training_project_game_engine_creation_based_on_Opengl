package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

public class Camera 
{
	private Vector3f position = new Vector3f(50,10,0);
	private float pitch = 15;//vertical look position
	private float yaw= -180;//horizontal look position
	private float roll;//how much camera tilted or rotate around itself. Barrel roll movement
	private float speed = 0.5f;
	
	private Player player;
	
	private float distanceFromPlayer = 35;// distance from player to camera
	private float angleAroundPlayer = 0;// rotation camera by z-axis relatively from player
	
	private float maxZoom = 75;
	private float minZoom = 10;
	private float maxPitch = 90;
	private float minPitch = -10;
	
	private float modelTopValue = 0;

	public Camera(Player player)
	{
		this.player = player;
		
		/*calculate camera aiming BEGIN*/
		float modelTopValueTemp = player.getModel().getRawModel().getTopModelVertex();
		modelTopValue = modelTopValueTemp - (float) (modelTopValueTemp / 9);
		/*calculate camera aiming END*/
	}
	
	public void move()
	{		
		calculateZoom();
		calculateAngleAroundPlayer();
		calculatePitch();
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		this.yaw = 180- (player.getRotY() + angleAroundPlayer);// calculate camera rotation in a such way that camera always focuses on the player 
		/*yaw =  - (Display.getWidth() - Mouse.getX() / 2);
		pitch =  (Display.getHeight() / 2) - Mouse.getY();
		
		if (pitch >= 90)
		{			
			pitch = 90;			
		}
		else if (pitch <= -90)
		{
			pitch = -90;			
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
		{
			position.y+=0.2f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			position.y-=0.2f;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			position.z-=speed;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			position.z+=speed;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			position.x+=speed;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			position.x-=speed;
		}*/
	}

	public Vector3f getPosition() 
	{
		return position;
	}

	public float getPitch() 
	{
		return pitch;
	}

	public float getYaw() 
	{
		return yaw;
	}

	public float getRoll() 
	{
		return roll;
	}
	
	private void calculateCameraPosition(float horizDistanceFromPlayer, float verticDistanceFromPlayer)// calculates camera position relative to player
	{
		float finalCameraViewAngle = player.getRotY() + angleAroundPlayer;// "finalCameraViewAngle" - calculates by summary of angle on which player model view vector was turned + mouse rotation
		float offsetX = (float) (horizDistanceFromPlayer * Math.sin(Math.toRadians(finalCameraViewAngle)));// "offsetX" - calculates distance between camera and player at horizontal dimension
		float offsetZ = (float) (horizDistanceFromPlayer * Math.cos(Math.toRadians(finalCameraViewAngle)));// "offsetY" - calculates distance between camera and player at vertical dimension
		position.x = player.getPosition().x - offsetX;// We subtract this two values, because camera locates behind the player. Calculates final distance between camera and player at X dimension
		position.y = player.getPosition().y + verticDistanceFromPlayer;// We add this two values, because camera locates higher than player. Calculates final distance between camera and player at Y dimension
		position.z = player.getPosition().z - offsetZ;// We subtract this two values, because camera locates behind the player. Calculates final distance between camera and player at Z dimension
	}
	
	private float calculateHorizontalDistance()// horizontal distance from the player to the camera
	{
		return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
	}
	
	private float calculateVerticalDistance()// vertical distance from the player to the camera
	{
		return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch))) + modelTopValue;
	}
	
	private void calculateZoom()// calculates distance between player and camera
	{
		float zoomLevel = Mouse.getDWheel() * 0.02f;// "Mouse.getDWheel()"- returns value of mouse down rotation. Multiply by "0.02f", because "Mouse.getDWheel()" returns quite large value.
		distanceFromPlayer -=zoomLevel;// decreasing distance between player and camera on mouse down rotation value
		if(distanceFromPlayer < minZoom)
		{
			distanceFromPlayer = minZoom;
		}
		else if(distanceFromPlayer > maxZoom)
		{
			distanceFromPlayer = maxZoom;
		}
	}
	
	private void calculatePitch()// calculates horizontal angle between player and camera
	{
		if(Mouse.isButtonDown(1))// "0" - left mouse button; "1" - right mouse button
		{
			float pitchChange = Mouse.getDY() * 0.1f;// "Mouse.getDY()" - get value on which mouse moved up or down. Multiply by "0.1f", because "Mouse.getDY()" returns quite large value.
			pitch -= pitchChange;// decreasing horizontal angle between player and camera on mouse down rotation value 
			
			if(pitch < minPitch)
			{
				pitch = minPitch;
			}
			else if(pitch > maxPitch)
			{
				pitch = maxPitch;
			}
		}
		/*else if((Keyboard.isKeyDown(Keyboard.KEY_W)) || 
				(Keyboard.isKeyDown(Keyboard.KEY_A)) || 
				(Keyboard.isKeyDown(Keyboard.KEY_D)) || 
				(Keyboard.isKeyDown(Keyboard.KEY_S)) || 
				(Keyboard.isKeyDown(Keyboard.KEY_SPACE)))
		{
			pitch = 20;
		}*/
	}
	
	private void calculateAngleAroundPlayer()// calculates angle between player model view vector and camera view vector
	{
		if(Mouse.isButtonDown(1))// "0" - left mouse button; "1" - right mouse button
		{
			float angleChange = Mouse.getDX() * 0.3f;// "Mouse.getDX()"- get value on which mouse moved left or right. Multiply by "0.3f", because "Mouse.getDX()" returns also quite large value, but it's normal when horizontal rotation move little faster.
			angleAroundPlayer -= angleChange;// increasing angle between player model view vector and camera view vector
		}
		else if((Keyboard.isKeyDown(Keyboard.KEY_W)) || 
				(Keyboard.isKeyDown(Keyboard.KEY_A)) || 
				(Keyboard.isKeyDown(Keyboard.KEY_D)) || 
				(Keyboard.isKeyDown(Keyboard.KEY_S)) || 
				(Keyboard.isKeyDown(Keyboard.KEY_SPACE)))
		{
			angleAroundPlayer = 0;
		}
	}
	
	
}
