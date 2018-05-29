package textures;

public class ModelTexture 
{
	private int textureID;
	
	private float shineDamper = 1;//using to store how close camera needs to be from reflective light to see any changes on the surface of the object
	private float reflectivity = 0;//using for measuring thickness of reflection
	
	private boolean hasTransparency = false;//determines is this object has transparency or not
	
	private boolean useFakeLighting = false;//for making more realistic lightning of transparency object. Where the normals of this objects separated from lightning normals by transparent wall. SOLVING THIS PROBLEM BY DIRECTING NORMALS TO THE TOP
	
	private int numberOfRows = 1;//shows amount of model textures at this texture atlas image just at rows, so right amount of model textures would be (numberOfRows*numberOfRows)
	
	public ModelTexture(int id)
	{
		this.textureID = id;
	}
		
	public int getNumberOfRows() 
	{
		return numberOfRows;
	}

	public void setNumberOfRows(int numberOfRows) 
	{
		this.numberOfRows = numberOfRows;
	}

	public boolean isUseFakeLighting() 
	{
		return useFakeLighting;
	}

	public void setUseFakeLighting(boolean useFakeLighting) 
	{
		this.useFakeLighting = useFakeLighting;
	}

	public boolean isHasTransparency() 
	{
		return hasTransparency;
	}

	public void setHasTransparency(boolean hasTransparency) 
	{
		this.hasTransparency = hasTransparency;
	}

	public int getID()
	{
		return this.textureID;
	}

	public float getShineDamper() 
	{
		return shineDamper;
	}

	public void setShineDamper(float shineDamper) 
	{
		this.shineDamper = shineDamper;
	}

	public float getReflectivity() 
	{
		return reflectivity;
	}

	public void setReflectivity(float reflectivity) 
	{
		this.reflectivity = reflectivity;
	}
	
	

}
