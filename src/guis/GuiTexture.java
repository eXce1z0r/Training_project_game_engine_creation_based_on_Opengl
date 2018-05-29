package guis;

import org.lwjgl.util.vector.Vector2f;

public class GuiTexture 
{
	private int texture;
	private Vector2f position;// store center vertex coordinates of our gui at the game display window 
	private Vector2f scale;// store dimension size of our gui at relation to game display window
	
	public GuiTexture(int texture, Vector2f position, Vector2f scale) 
	{
		this.texture = texture;
		this.position = position;
		this.scale = scale;
	}

	public int getTexture() 
	{
		return texture;
	}

	public Vector2f getPosition() 
	{
		return position;
	}

	public Vector2f getScale() 
	{
		return scale;
	}
	
		
}
