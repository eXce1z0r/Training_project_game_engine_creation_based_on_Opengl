package shaders;

import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Light;
import toolbox.Maths;

public class StaticShader extends ShaderProgram 
{
	private static final int MAX_LIGHTS = 25;// amount of light by which one vertex of object can be illuminated
	
	private static final String VERTEX_FILE = "src/shaders/vertexShader.txt";
	private static final String FRAGMENT_FILE = "src/shaders/fragmentShader.txt";

	private int location_transformationMatrix;//hold location matrix which store entity transformation in different position
	private int location_projectionMatrix;//hold location matrix which simulate more realistic object depth/z projection
	private int location_viewMatrix;//hold location matrix which simulate camera moving
	
	private int location_lightPosition[];//hold location of the "vec3 lightPosition[amountOfLightSourcesForOneVertex]" uniform variable from vertexShader
	private int location_lightColour[];//hold location of the "vec3 lightColour[amountOfLightSourcesForOneVertex]" uniform variable from fragmentShader
	private int location_lightAttenuation[];//hold location of the "vec3 lightAttenuation[amountOfLightSourcesForOneVertex]" uniform variable from fragmentShader
	
	private int location_shineDamper;//hold location of the "float shineDamper" uniform variable from fragmentShader
	private int location_reflectivity;//hold location of the "float reflectivity" uniform variable from fragmentShader
	private int location_useFakeLighting;//hold\store location of the "float useFakeLighting" uniform variable from fragmentShader
	private int location_skyColour;//hold\store location of the "float skyColour" uniform variable from fragmentShader
	private int location_numberOfRows;//hold\store location of the "float numberOfRows" uniform variable from vertexShader
	private int location_offset;//hold\store location of the "vec2 offset" uniform variable from vertexShader
	
	public StaticShader() 
	{
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes()//method using to give ability to the vertexShader get info from variables
	{
		super.bindAttribute(0, "position");//binds VBO(attribute from VAO) number 0 to this variable "position"
		super.bindAttribute(1, "textureCoords");//binds VBO(attribute from VAO) number 1 to this variable "textureCoords"
		super.bindAttribute(2, "normal");//binds VBO(attribute from VAO) number 2 to this variable "normal"
	}

	@Override
	protected void getAllUniformLocations() 
	{
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		//location_lightPosition = super.getUniformLocation("lightPosition");
		//location_lightColour = super.getUniformLocation("lightColour");
		location_shineDamper = super.getUniformLocation("shineDamper");
		location_reflectivity = super.getUniformLocation("reflectivity");
		location_useFakeLighting = super.getUniformLocation("useFakeLighting");
		location_skyColour = super.getUniformLocation("skyColour");
		location_numberOfRows = super.getUniformLocation("numberOfRows");
		location_offset = super.getUniformLocation("offset");
	
		location_lightPosition = new int[MAX_LIGHTS];
		location_lightColour = new int[MAX_LIGHTS];
		location_lightAttenuation = new int[MAX_LIGHTS];
		for(int i=0;i<MAX_LIGHTS;i++)
		{
			location_lightPosition[i] = super.getUniformLocation("lightPosition["+ i +"]");
			location_lightColour[i] = super.getUniformLocation("lightColour["+ i +"]");
			location_lightAttenuation[i] = super.getUniformLocation("lightAttenuation["+ i +"]");
		}
	}
	
	public void loadNumberOfRows(int numberOfRows)
	{
		super.loadFloat(location_numberOfRows, numberOfRows);
	}
	
	public void loadOffset(float x, float y)
	{
		super.load2DVector(location_offset, new Vector2f(x, y));
	}
	
	public void loadSkyColour(float r, float g, float b)
	{
		super.loadVector(location_skyColour, new Vector3f(r, g, b));
	}
	
	public void loadFakeLightingVariable(boolean useFake)//method which allows us load up values to uniform variable "useFakeLighting" from vertexShader. "useFake" - indicates should we use fake lighting or not
	{
		super.loadBoolean(location_useFakeLighting, useFake);
	}
	
	public void loadTransformationMatrix(Matrix4f matrix)
	{
		super.loadMatrix(location_transformationMatrix, matrix);
	}
	
	public void loadShineVariables(float damper, float reflectivity)//method using for loading variables "damper" and "reflectivity" into fragment shader 
	{
		super.loadFloat(location_shineDamper, damper);
		super.loadFloat(location_reflectivity, reflectivity);
	}
	
	public void loadLights(List<Light> lights)//method which load up values from light variables to the shaders
	{
		//super.loadVector(location_lightPosition, light.getPosition());//load light.getPosition() to the location_lightPosition
		//super.loadVector(location_lightColour, light.getColour());// load light.getColour() to the location_lightColour
		for(int i=0;i<MAX_LIGHTS;i++)
		{
			if(i<lights.size())//check if "MAX_LIGHTS"(max amount of light sources which can illuminate one vertex) less then amount of light sources which we want to illuminate one vertex of object
			{
				super.loadVector(location_lightPosition[i], lights.get(i).getPosition());//load light.getPosition() to the location_lightPosition
				super.loadVector(location_lightColour[i], lights.get(i).getColour());// load light.getColour() to the location_lightColour
				super.loadVector(location_lightAttenuation[i], lights.get(i).getAttenuation());// load light.getAttenuation() to the location_lightAttenuation
			}
			else//if amount of lights which illuminate one vertex on object less than "MAX_LIGHTS" than remaining lights values sets on 0
			{
				super.loadVector(location_lightPosition[i], new Vector3f(0, 0, 0));//load empty position to the location_lightPosition
				super.loadVector(location_lightColour[i], new Vector3f(0, 0, 0));// load empty light color to the location_lightColour
				super.loadVector(location_lightAttenuation[i], new Vector3f(1, 0, 0));// load empty light attenuation to the location_lightAttenuation. new Vector3f(1, 0, 0) - because we do not want to divide by zero at "fragmentShader" counting 
			}
		}
	}
	
	public void loadViewMatrix(Camera camera)
	{
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}
	
	public void loadProjectionMatrix(Matrix4f projection)
	{
		super.loadMatrix(location_projectionMatrix, projection);
	}

}
