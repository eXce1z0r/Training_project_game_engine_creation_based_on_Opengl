package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import shaders.StaticShader;
import shaders.TerrainShader;
import skyBox.SkyBoxRenderer;
import terrains.Terrain;

public class MasterRenderer 
{
	private static final float FOV = 70;// Field/angle of view
	private static final float NEAR_PLANE = 0.1f;// Closest distance at which you can see
	private static final float FAR_PLANE = 1000;// Farthest distance at which you can see
	
	private static final float RED = 0.5444f/*0.4f*/;// used to stored amount of RED color at fog
	private static final float GREEN = 0.62f/*0.5f*/;// used to stored amount of GREEN color at fog
	private static final float BLUE = 0.69f/*0.9f*/;// used to stored amount of BLUE color at fog
	
	private Matrix4f projectionMatrix;// used for create wider view at the distance so this create effect that objects with distance became smaller
	
	private StaticShader shader = new StaticShader();// calculation shader for objects, used for drawing every pixel
	private EntityRenderer renderer;
	
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader = new TerrainShader();// calculation shader for terrain, used for drawing every pixel
	
	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
	private List<Terrain> terrains = new ArrayList<Terrain>();
	
	private SkyBoxRenderer skyBoxRenderer;// used to render skyBox
	
	public MasterRenderer(MemoryModelLoader loader/*because skyBoxRenderer need "MemoryModelLoader" at constructor*/)
	{
		enableCulling();
		createProjectionMatrix();
		renderer = new EntityRenderer(shader, projectionMatrix);
		
		/*//some my decisions BEGIN
		renderer.setRenderDistance(FAR_PLANE);
		//some my decisions ENDS*/
		
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
	
		skyBoxRenderer = new SkyBoxRenderer(loader, projectionMatrix);
		//skyBoxRenderer.setSkyBoxSize(FAR_PLANE/2);
	}
	
	public Matrix4f getProjectionMatrix()
	{
		return projectionMatrix;
	}
	
	public static void enableCulling()//method for enabling backface culling. Using for correct visualization of non-transparency models
	{
		GL11.glEnable(GL11.GL_CULL_FACE);//enable the switch off rendering triangles which faces inside or outside of the object
		GL11.glCullFace(GL11.GL_BACK);//we choose do not render triangles which pointing inside the object(which could be seen from inside)
	}
	
	public static void disableCulling()//method for disabling backface culling. Using for correct visualization of transparency models
	{
		GL11.glDisable(GL11.GL_CULL_FACE);//switch on rendering triangles which faces inside or outside of the object
	}
	
	public void render(List<Light> lights, Camera camera)//render all object which we see on the screen
	{
		prepare();
		
		//My smart light rendering array generation BEGIN
		ArrayList<Light> lightsForRender = new ArrayList<Light>();
		
		for(int i=0;i<lights.size();i++)
		{
			Light light = lights.get(i);
			
			double distanceFromLightToCam = Math.sqrt(Math.pow((camera.getPosition().x - light.getPosition().x), 2) + Math.pow((camera.getPosition().y - light.getPosition().y), 2) + Math.pow(camera.getPosition().z - light.getPosition().z, 2));// counting distance between camera and light source
					
			if(distanceFromLightToCam <= FAR_PLANE || light.isGlobalLighting())
			{
				lightsForRender.add(light);
			}
		}
		//My smart light rendering array generation END
		
		shader.start();
		shader.loadSkyColour(this.RED, this.GREEN, this.BLUE);
		shader.loadLights(lightsForRender);//we add lights effect to the shader when we rendering image
		shader.loadViewMatrix(camera);//simulate camera view
		
		/*//some my decisions BEGIN
		renderer.setCameraPosition(camera.getPosition());
		//some my decisions ENDS*/
		
		renderer.render(entities);
		shader.stop();
		
		terrainShader.start();
		terrainShader.loadLights(lightsForRender);
		terrainShader.loadViewMatrix(camera);
		terrainShader.loadSkyColour(this.RED, this.GREEN, this.BLUE);// loading up "r","g","b" colors of fog every frame
		terrainRenderer.render(terrains);
		terrainShader.stop();
		
		skyBoxRenderer.render(camera, this.RED, this.GREEN, this.BLUE);// render skyBox every frame by using "r","g","b" colors of fog 
		
		terrains.clear();//to free space because if we didn't do this current terrain details will be rendered with details which we render in the future. And this will drop framerates
		entities.clear();//to free space because if we didn't do this current entities will be rendered with entities which we render in the future. And this will drop framerates
	}
	
	public void processTerrain(Terrain terrain)//add new "terrain" to rendering list
	{
		terrains.add(terrain);
	}
	
	public void processEntity(Entity entity)//method which put entities to the entities hashmap
	{
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		if(batch!=null)
		{
			batch.add(entity);
		}
		else
		{
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}
	
	public void cleanUp()//for cleaning up all shaders when we close the game
	{
		shader.cleanUp();//clean up the "shader" when the game will close
		
		terrainShader.cleanUp();//clean up the "terrainShader" when the game will close
	}
	
	public void prepare()
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST);//check that triangles which closer to us be on the upper layer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(this.RED, this.GREEN, this.BLUE, 1);
		//GL11.glClearColor(0.3f, 0, 0, 1);
	}
	
	private void createProjectionMatrix()//check+*
	{
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
		float x_scale = (float) y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;
		
		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
	}
}
