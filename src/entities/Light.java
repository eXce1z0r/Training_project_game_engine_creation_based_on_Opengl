package entities;

import org.lwjgl.util.vector.Vector3f;

public class Light//class which represent lightning
{
	private Vector3f position;
	private Vector3f colour;
	private Vector3f attenuation = new Vector3f(1, 0, 0);// value which responsible for decreasing intensity of the light by the distance(attenuation). new Vector3f(1, 0, 0) - because we do not want to divide by zero at "fragmentShader" counting 
	private boolean globalLight = false;
	
	public Light(Vector3f position, Vector3f colour, boolean globalLight) 
	{
		this.position = position;
		this.colour = colour;
		this.globalLight = globalLight;
	}
	
	public Light(Vector3f position, Vector3f colour, Vector3f attenuation, boolean globalLight)
	{
		this.position = position;
		this.colour = colour;
		this.attenuation = attenuation;
		this.globalLight = globalLight;
	}
	
	public boolean isGlobalLighting()
	{
		return globalLight;
	}
	
	public Vector3f getAttenuation()
	{
		return attenuation;
	}

	public Vector3f getPosition() 
	{
		return position;
	}

	public void setPosition(Vector3f position) 
	{
		this.position = position;
	}

	public Vector3f getColour() 
	{
		return colour;
	}

	public void setColour(Vector3f colour) 
	{
		this.colour = colour;
	}	
}
