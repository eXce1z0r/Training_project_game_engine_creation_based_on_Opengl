package skyBox;

import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import renderEngine.DisplayManager;
import shaders.ShaderProgram;
import toolbox.Maths;

public class SkyBoxShader extends ShaderProgram// missed explanations about some method and classes here already exists at shaders/StaticShader and shaders/TerrainShader 
{
	private static final String VERTEX_FILE = "src/skyBox/skyboxVertexShader.txt";
	private static final String FRAGMENT_FILE = "src/skyBox/skyboxFragmentShader.txt";
	
	private static final float ROTATE_SPEED = 1f;// speed of sky rotation. Value at degrees per secound.
	
	private float rotation = 0;// current rotation speed of skybox
	
	private int location_projectionMatrix;// used to store int position of "projectionMatrix" from "skyboxVertexShader.txt"
	private int location_viewMatrix;// used to store int position of "viewMatrix" from "skyboxVertexShader.txt"
	
	private int location_fogColour;// used to store int position of "fogColour" from "skyboxFragmentShader.txt"
	
	private int location_dayCubeMap;// used to store int position of "cubeMap" from "skyboxFragmentShader.txt"
	private int location_nightCubeMap;// used to store int position of "cubeMap2" from "skyboxFragmentShader.txt"
	private int location_blendFactor;// used to store int position of "blendFactor" from "skyboxFragmentShader.txt"
	
	public SkyBoxShader() 
	{
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	public void loadProjectionMatrix(Matrix4f matrix)
	{
		super.loadMatrix(location_projectionMatrix, matrix);
	}

	public void loadViewMatrix(Camera camera)
	{
		Matrix4f matrix = Maths.createViewMatrix(camera);
		/*    viewMatrix
		 * 1 0 0 translation_x
		 * 0 1 0 translation_y
		 * 0 0 1 translation_z 
		 * 0 0 0 1
		 * 
		 * so if we set the translation to translation_x = 0
		 * 									translation_y = 0
		 * 									translation_z = 0
		 * then the viewMatrix won't cause the skyBox to move in relation to the camera
		 * */
		matrix.m30 = 0;// translation_x
		matrix.m31 = 0;// translation_y
		matrix.m32 = 0;// translation_z 
		
		rotation += ROTATE_SPEED * DisplayManager.getFrameTimeSeconds();// calculate rotation angle of skybox for a current frame
		Matrix4f.rotate((float)Math.toRadians(rotation), new Vector3f(0,1,0), matrix, matrix);// used for determination at "viewMatrix"(matrix) how much "(float)Math.toRadians(rotation)" and by which axis "new Vector3f(0,1,0)" skybox must rotate
		
		super.loadMatrix(location_viewMatrix, matrix);
	}
	
	public void loadFogColour(float r, float g, float b)// method which allows us load up color value to uniform variable "fogColour" from "skyboxFragmentShader.txt"
	{
		super.loadVector(location_fogColour, new Vector3f(r,g,b));
	}
	
	public void connectTextureUnits()// method used for binding samplerCube uniforms from "skyboxFragmentShader.txt" to GL13.GL_TEXTURE'X' variables
	{
		super.loadInt(location_dayCubeMap, 0);// used for binding "uniform samplerCube cubeMap;" from "skyboxFragmentShader.txt" to GL13.GL_TEXTURE0 variable(setting at SkyBoxRenderer.bindTextures())
		super.loadInt(location_nightCubeMap, 1);// used for binding "uniform samplerCube cubeMap2;" from "skyboxFragmentShader.txt" to GL13.GL_TEXTURE1 variable(setting at SkyBoxRenderer.bindTextures())
	}
	
	public void loadBlendFactor(float blend)// method which allows us load up color value to uniform variable "blendFactor" from "skyboxFragmentShader.txt"
	{
		super.loadFloat(location_blendFactor, blend);
	}
	
	@Override
	protected void getAllUniformLocations() 
	{
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_fogColour = super.getUniformLocation("fogColour");
		
		location_dayCubeMap = super.getUniformLocation("cubeMap");
		location_nightCubeMap = super.getUniformLocation("cubeMap2");
		location_blendFactor = super.getUniformLocation("blendFactor");
	}

	@Override
	protected void bindAttributes() 
	{
		super.bindAttribute(0, "position");
	}
}
