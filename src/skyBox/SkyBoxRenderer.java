package skyBox;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import entities.Camera;
import models.Model;
import renderEngine.DisplayManager;
import renderEngine.MemoryModelLoader;

public class SkyBoxRenderer 
{
	private static float SIZE = 500f;//determine size of each edge at skyBox cube
	
	private static final float[] VERTICES = {        
	    -SIZE,  SIZE, -SIZE,
	    -SIZE, -SIZE, -SIZE,
	    SIZE, -SIZE, -SIZE,
	     SIZE, -SIZE, -SIZE,
	     SIZE,  SIZE, -SIZE,
	    -SIZE,  SIZE, -SIZE,

	    -SIZE, -SIZE,  SIZE,
	    -SIZE, -SIZE, -SIZE,
	    -SIZE,  SIZE, -SIZE,
	    -SIZE,  SIZE, -SIZE,
	    -SIZE,  SIZE,  SIZE,
	    -SIZE, -SIZE,  SIZE,

	     SIZE, -SIZE, -SIZE,
	     SIZE, -SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE, -SIZE,
	     SIZE, -SIZE, -SIZE,

	    -SIZE, -SIZE,  SIZE,
	    -SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE, -SIZE,  SIZE,
	    -SIZE, -SIZE,  SIZE,

	    -SIZE,  SIZE, -SIZE,
	     SIZE,  SIZE, -SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	    -SIZE,  SIZE,  SIZE,
	    -SIZE,  SIZE, -SIZE,

	    -SIZE, -SIZE, -SIZE,
	    -SIZE, -SIZE,  SIZE,
	     SIZE, -SIZE, -SIZE,
	     SIZE, -SIZE, -SIZE,
	    -SIZE, -SIZE,  SIZE,
	     SIZE, -SIZE,  SIZE
	};//determine all vertex positions of skyBox cube
	
	private static String[] DAY_TEXTURE_FILES = {"1/day/right", "1/day/left", "1/day/top", "1/day/bottom", "1/day/back", "1/day/front"};// used to store names of 6 textures which used at creation of skyBox
	private static String[] NIGHT_TEXTURE_FILES = {"1/night/right", "1/night/left", "1/night/top", "1/night/bottom", "1/night/back", "1/night/front"};// used to store names of 6 textures which used at creation of skyBox

	private Model cube;// SkyBox model
	private int texture;// texture id for a cube map
	private int night_texture;// texture id for a night cube map
	private SkyBoxShader shader;// calculation shader for skybox, used for drawing every pixel
	
	private float time = 0;// used for setting day/night cycle
	
	public SkyBoxRenderer(MemoryModelLoader loader/*load the texture and VAO*/, Matrix4f projectionMatrix/*to load up it into the shader*/)
	{
		cube = loader.loadToVAO(VERTICES, 3);// creating and loading into VAO skyBox cube model
		
		texture = loader.loadCubeMap(DAY_TEXTURE_FILES);// create cubeMap texture from 6 different textures and load it to VAO
		night_texture = loader.loadCubeMap(NIGHT_TEXTURE_FILES);// create cubeMap texture from 6 different textures and load it to VAO
		
		shader = new SkyBoxShader();
		shader.start();
		
		shader.connectTextureUnits();
		
		shader.loadProjectionMatrix(projectionMatrix);// load "projectionMatrix" to the shader
		shader.stop();
	}
	
	public void setSkyBoxSize(float Size)
	{
		SIZE = Size;
	}
	
	public void render(Camera camera/*need camera to get "viewMatrix"*/, float r/*amount of red color at the fog*/, float g/*amount of green color at the fog*/, float b/*amount of blue color at the fog*/)// SkyBox render method
	{
		shader.start();
		shader.loadViewMatrix(camera);// loading "viewMatrix"
		
		shader.loadFogColour(r, g, b);// loading up "r", "g", "b" values to fogColour every frame
		
		GL30.glBindVertexArray(cube.getVaoID());//bind VAO id of the cube
		GL20.glEnableVertexAttribArray(0);// enable attribute 0 which responds for skyBox "position" 
		
		/*GL13.glActiveTexture(GL13.GL_TEXTURE0);// activate "GL13.GL_TEXTURE0" 
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);// bind cube map texture "texture" to current active "GL13.GL_TEXTURE0" which stored as "GL13.GL_TEXTURE_CUBE_MAP"*/
		bindTextures();
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cube.getVertexCount());// used to draw cube at "GL11.GL_TRIANGLES" mode
		GL20.glDisableVertexAttribArray(0);// unbinds attribute 0 which responds for skyBox "position" 
		GL30.glBindVertexArray(0);// unbind current cube VAO id
		shader.stop();
	}
	
	/*private void bindTextures()
	{
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, night_texture);
	
		shader.loadBlendFactor(0.5f);
	}*/
	
	private void bindTextures()
	{
		time += DisplayManager.getFrameTimeSeconds() * 1000;// increasing time each frame
		time %= 24000;// if time > 24000 it becames remainder of the division. For example if time = 24041, after time %= 24000; it became 41 
		int texture1;
		int texture2;
		float blendFactor;
		
		/*BEGIN calculation amount of each texture(texture1, texture2) depending from current time variable value BEGIN*/
		if(time >= 0 && time < 5000)
		{
			texture1 = night_texture;
			texture2 = night_texture;
			blendFactor = (time - 0)/(5000 - 0);
		}
		else if(time >= 5000 && time < 8000)
		{
			texture1 = night_texture;
			texture2 = texture;
			blendFactor = (time - 5000)/(8000 - 5000);
		}
		else if(time >= 8000 && time < 21000)
		{
			texture1 = texture;
			texture2 = texture;
			blendFactor = (time - 8000)/(21000 - 8000);
		}
		else
		{
			texture1 = texture;
			texture2 = night_texture;
			blendFactor = (time - 21000)/(24000 - 21000);
		}
		
		/*END calculation amount of each texture(texture1, texture2) depending from current time variable value END*/

		GL13.glActiveTexture(GL13.GL_TEXTURE0);// activate texture unit 0(GL13.GL_TEXTURE0)
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture1);// load "texture1"(day skybox textures) to GL13.GL_TEXTURE0
		GL13.glActiveTexture(GL13.GL_TEXTURE1);// activate texture unit 1(GL13.GL_TEXTURE1)
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture2);// load "texture2"(night skybox textures) to GL13.GL_TEXTURE1
		shader.loadBlendFactor(blendFactor);// load blend factor which determines how much each of this textures would be used at final skybox texture
	}
}
