package toolbox;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import terrains.Terrain;

public class MousePicker 
{	
	private static final int RECURSION_COUNT = 200;// accuracy at calculation collision ray of mouse point and terrain. Used to find intersection point of mouse pointer ray and the terrain
	private static final float RAY_RANGE = 600;// max range of mouse pointer 3d ray. Used to find intersection point of mouse pointer ray and the terrain
	
	private Vector3f currentRay;// stores ray which pointed by mouse cursor threw the map
	
	private Matrix4f projectionMatrix;// used for create wider view at the distance so this create effect that objects with distance became smaller
	private Matrix4f viewMatrix;//used for creating effect of camera movement by moving all objects and terrain at the opposite side to the camera movement. Because at OpenGL Display coordinates always the same(-1<x<1;-1<y<1). So it updates every frame 
	private Camera camera;
	
	private Terrain terrain;// used to find intersection point of mouse pointer ray and the terrain 
	private Vector3f currentTerrainPoint;// used to find intersection point of mouse pointer ray and the terrain
	
	public MousePicker(Camera camera, Matrix4f projectionMatrix)
	{
		this.camera = camera;
		this.projectionMatrix = projectionMatrix;
		this.viewMatrix = Maths.createViewMatrix(this.camera);
	}
	
	public MousePicker(Camera camera, Matrix4f projectionMatrix, Terrain terrain)// used to find intersection point of mouse pointer ray and the terrain
	{
		this.camera = camera;
		this.projectionMatrix = projectionMatrix;
		this.viewMatrix = Maths.createViewMatrix(this.camera);
		this.terrain = terrain;
	}
	
	public Vector3f getCurrentTerrainPoint()// used to find intersection point of mouse pointer ray and the terrain
	{
		return currentTerrainPoint;
	}
	
	public Vector3f getCurrentRay()
	{
		return currentRay;
	}
	
	/*public void update()
	{
		viewMatrix = Maths.createViewMatrix(camera);// creating new viewMatrix by using a camera
		currentRay = calculateMouseRay();// used for update current mouse pointer ray
	}*/
	
	public void update()// used to find intersection point of mouse pointer ray and the terrain. Called every frame
	{
		viewMatrix = Maths.createViewMatrix(camera);// creating new viewMatrix by using a camera
		currentRay = calculateMouseRay();// used for update current mouse pointer ray
		
		if (intersectionInRange(0, RAY_RANGE, currentRay)) 
		{
			currentTerrainPoint = binarySearch(0, 0, RAY_RANGE, currentRay);
		} 
		else 
		{
			currentTerrainPoint = null;
		}
	}
	
	private Vector3f calculateMouseRay()//used for converting 2d mouse position on the screen to 3d ray
	{
		float mouseX = Mouse.getX();// storing 2d position of mouse by x-axis
		float mouseY = Mouse.getY();// storing 2d position of mouse by y-axis
		
		Vector2f normalizedCoords = getNormalizedDeviceCoords(mouseX, mouseY);
		
		Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1f/*because we want that ray pointing into the screen*/, 1f);// to use 3d directional ray instead 3d point, we just set "z" coordinate equal to -1f for pointing directional ray from mouse pointer into the 3d world in front of the player
		
		Vector4f eyeCoords = toEyeCoords(clipCoords);
		
		Vector3f worldRay = toWorldCoords(eyeCoords);
		
		return worldRay;
	}
	
	private Vector3f toWorldCoords(Vector4f eyeCoords)//convert vector by using inverse view matrix
	{
		Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);// invert "viewMatrix" and put result to "invertedView"
		Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);// used to transform "eyeCoords" coordinates into world coordinates
		Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);// just converting into 3d vector array
		mouseRay.normalise();// set length of vector to 1 because it is just direction
		return mouseRay;
	}
	
	private Vector4f toEyeCoords(Vector4f clipCoords)//convert vector by using inverse projection matrix
	{
		Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);// invert "projectionMatrix" and put result to "invertedProjection"
		Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);// save at "eyeCoords" new 4d transformed matrix which was formed from two matrices "invertedProjection" and "clipCoords"
		
		return new Vector4f(eyeCoords.x, eyeCoords.y, -1f/*because we want that ray pointing into the screen*/, 0f);
	}
	
	private Vector2f getNormalizedDeviceCoords(float mouseX, float mouseY)//convert mouse 2d screen coordinates int openGL 2d coordinates (-1<x<1;-1<y<1)
	{
		float x = (2f*mouseX) / Display.getWidth() - 1;//convert "x" screen 2d coordinates into OpenGL 2d coordinates
		float y = (2f*mouseY) / Display.getHeight() - 1;//convert "y" screen 2d coordinates into OpenGL 2d coordinates
		return new Vector2f(x, y);// if use some other not LightWeightJavaGameLibrary then "y" must be "-y" 
	}
	
//BEGIN methods which used to find intersection point of mouse pointer ray and the terrain BEGIN
	
		private Vector3f getPointOnRay(Vector3f ray, float distance) 
		{
			Vector3f camPos = camera.getPosition();
			Vector3f start = new Vector3f(camPos.x, camPos.y, camPos.z);
			Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);
			return Vector3f.add(start, scaledRay, null);
		}
		
		private Vector3f binarySearch(int count, float start, float finish, Vector3f ray) // checking by "y"-axis if center point of current vector is above or under terrain z-coordinates. If current center point above the terrain z-coordinates than it take those part of vector which under of current point and repeat this action. Else if current center point under the terrain z-coordinates than it take those part of vector which above of current point and repeat this action.   
		{
			float half = start + ((finish - start) / 2f);
			if (count >= RECURSION_COUNT) 
			{
				Vector3f endPoint = getPointOnRay(ray, half);
				Terrain terrain = getTerrain(endPoint.getX(), endPoint.getZ());
				if (terrain != null) 
				{
					return endPoint;
				} 
				else 
				{
					return null;
				}
			}
			if (intersectionInRange(start, half, ray))// if current center point under the terrain z-coordinates than it take those part of vector which above of current point and repeat this action.
			{
				return binarySearch(count + 1, start, half, ray);
			} 
			else // if current center point above the terrain z-coordinates than it take those part of vector which under of current point and repeat this action.
			{
				return binarySearch(count + 1, half, finish, ray);
			}
		}

		private boolean intersectionInRange(float start, float finish, Vector3f ray) 
		{
			Vector3f startPoint = getPointOnRay(ray, start);
			Vector3f endPoint = getPointOnRay(ray, finish);
			if (!isUnderGround(startPoint) && isUnderGround(endPoint)) 
			{
				return true;
			} 
			else 
			{
				return false;
			}
		}

		private boolean isUnderGround(Vector3f testPoint) 
		{
			Terrain terrain = getTerrain(testPoint.getX(), testPoint.getZ());
			float height = 0;
			if (terrain != null) 
			{
				height = terrain.getHeightOfTerrain(testPoint.getX(), testPoint.getZ());
			}
			if (testPoint.y < height) 
			{
				return true;
			} 
			else 
			{
				return false;
			}
		}

		private Terrain getTerrain(float worldX, float worldZ) 
		{
			return terrain;
		}
//END methods which used to find intersection point of mouse pointer ray and the terrain END
}
