package toolbox;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;

public class Maths 
{
	public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale)// set "transformationMatrix" which used for setting object position at the world.
	{
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
		return matrix;
	}
	
	public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos)// calculate the height of player position vertex inside the triangle. "pos" - is the object position. "p1", "p2", "p3" - is three vertex of triangle which located on this "pos"(object position) 
	{
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;// calculate how close vertex of player position located to corner of triangle "p1" by using "barycentric coordinates on triangles"(wiki)
		float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;// calculate how close vertex of player position located to corner of triangle "p2" by using "barycentric coordinates on triangles"(wiki)
		float l3 = 1.0f - l1 - l2;// calculate how close vertex of player position located to corner of triangle "p3" by using "barycentric coordinates on triangles"(wiki)
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;// calculate final height of vertex where player located at the triangle. By using percentage of distances from player to each of corner of this triangle and height of each corner  
	}
	
	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale)// set "transformationMatrix" which used for setting object position and rotation at the world.
	{
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		
		Matrix4f.rotate((float) Math.toRadians(rx)/*choose the measuring of rotation and it's directions*/, new Vector3f(1,0,0)/*choose one of 3 axis rx,ry,rz*/, matrix/*object for rotation*/, matrix/*object for saving changes*/);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0,1,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0,0,1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale, scale, scale), matrix, matrix);
		return matrix;
	}
	
	public static Matrix4f createViewMatrix(Camera camera)//used for creating effect of camera movement by moving all objects and terrain at the opposite side to the camera movement. Because at OpenGL Display coordinates always the same(-1<x<1;-1<y<1). So it updates every frame 
	{
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.setIdentity();
		Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getRoll()), new Vector3f(0, 0, 1), viewMatrix, viewMatrix);
		Vector3f cameraPos = camera.getPosition();
		Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
		return viewMatrix;
	}
}
