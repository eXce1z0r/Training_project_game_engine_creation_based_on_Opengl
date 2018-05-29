package models;

import org.lwjgl.util.vector.Vector3f;

public class Model 
{
	private int vaoID;
	private int vertexCount;
	
	private float topModelVertex;
	
	public Model(int vaoID, int vertexCount, float topModelVertex)
	{
		this.vaoID= vaoID;
		this.vertexCount= vertexCount;
		this.topModelVertex = topModelVertex;
	}

	public int getVaoID() 
	{
		return vaoID;
	}

	public int getVertexCount() 
	{
		return vertexCount;
	}

	public float getTopModelVertex() 
	{
		return topModelVertex;
	}		
}
