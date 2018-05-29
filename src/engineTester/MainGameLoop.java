package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.Model;
import models.TexturedModel;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import renderEngine.DisplayManager;
import renderEngine.MasterRenderer;
import renderEngine.MemoryModelLoader;
import shaders.StaticShader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.Maths;
import toolbox.MousePicker;

public class MainGameLoop 
{

	public static void main(String[] args) 
	{
		DisplayManager.createDisplay();
		
		MemoryModelLoader loader = new MemoryModelLoader();
		/*StaticShader shader = new StaticShader();
		Renderer renderer= new Renderer(shader);*/
		
		// OpenGL expects vertices to be defined counter clockwise by default
		/*float[] vertices = {
				//Left bottom triangle
				-0.5f, 0.5f, 0f,
				-0.5f, -0.5f, 0f,
				0.5f, -0.5f, 0f,
				//Right top triangle
				0.5f, -0.5f, 0f,
				0.5f, 0.5f, 0f,
				-0.5f, 0.5f, 0f
		};*/
		
		/*float[] vertices = {
				-0.5f, 0.5f, 0f,	//V0
				-0.5f, -0.5f, 0f,	//V1
				0.5f, -0.5f, 0f,	//V2
				0.5f, 0.5f, 0f		//V3
		};
		
		int[] indices = 
			{
					0, 1, 3,	//Top left triangle (V0, V1, V3)
					3, 1, 2		//Bottom right triangle (V3, V1, V2)
			};
		
		float[] textureCoords = 
			{
					0,0,	//V0
					0,1,	//V1
					1,1,	//V2
					1,0		//V3
			};*/
		
		 
		 /* //TEST
		  float[] vertices = {            
	                -0.5f,0.5f,0,   
	                -0.5f,-0.5f,0,  
	                0.5f,-0.5f,0,   
	                0.5f,0.5f,0,        
	                 
	                -0.5f,0.5f,1,   
	                -0.5f,-0.5f,1,  
	                0.5f,-0.5f,1,   
	                0.5f,0.5f,1,
	                 
	                0.5f,0.5f,0,    
	                0.5f,-0.5f,0,   
	                0.5f,-0.5f,1,   
	                0.5f,0.5f,1,
	                 
	                -0.5f,0.5f,0,   
	                -0.5f,-0.5f,0,  
	                -0.5f,-0.5f,1,  
	                -0.5f,0.5f,1,
	                 
	                -0.5f,0.5f,1,
	                -0.5f,0.5f,0,
	                0.5f,0.5f,0,
	                0.5f,0.5f,1,
	                 
	                -0.5f,-0.5f,1,
	                -0.5f,-0.5f,0,
	                0.5f,-0.5f,0,
	                0.5f,-0.5f,1	                 
	        };
	         
	        float[] textureCoords = {
	                 
	                0,0,
	                0,1,
	                1,1,
	                1,0,            
	                0,0,
	                0,1,
	                1,1,
	                1,0,            
	                0,0,
	                0,1,
	                1,1,
	                1,0,
	                0,0,
	                0,1,
	                1,1,
	                1,0,
	                0,0,
	                0,1,
	                1,1,
	                1,0,
	                0,0,
	                0,1,
	                1,1,
	                1,0	 	                 
	        };
	         
	        int[] indices = {
	                0,1,3,  
	                3,1,2,  
	                4,5,7,
	                7,5,6,
	                8,9,11,
	                11,9,10,
	                12,13,15,
	                15,13,14,   
	                16,17,19,
	                19,17,18,
	                20,21,23,
	                23,21,22	 
	        };
		
		Model model = loader.loadToVAO(vertices, textureCoords, indices);*/
		
		/*ModelTexture texture = new ModelTexture(loader.loadTexture("texture_not_found"));
		//TexturedModel texturedModel = new TexturedModel(model, texture);		
		TexturedModel staticModel = new TexturedModel(model, texture);*/
		//TexturedModel staticModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("test/texture_not_found")));// the same like code above
		
		//Model model = OBJLoader.loadObjModel("crawler/crawler", loader);	
		
		ModelData data = OBJFileLoader.loadOBJ("testPlayer/testPlayer");
		Model playerModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices(), data.getTopModelVertex());
		TexturedModel texPlayerModel = new TexturedModel(playerModel, new ModelTexture(loader.loadTexture("testPlayer/testPlayerTexture")));
		ModelTexture texture = texPlayerModel.getTexture();
		texture.setShineDamper(10);
		texture.setReflectivity(0.4f);
		data.clear();
		
		/*Used very interesting method to "setShineDamper" and "setReflectivity" by creating link of object file*/
		data = OBJFileLoader.loadOBJ("cube/cube");
		Model cubeModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices(), data.getTopModelVertex());	
		TexturedModel staticModel = new TexturedModel(cubeModel, new ModelTexture(loader.loadTexture("cube/cubeTexture")));
		texture = staticModel.getTexture();//getting texture of object to set their reflectivity and damping
		texture.setShineDamper(10);//setting model texture shineDamper(shine reflecting from object)
		texture.setReflectivity(1);//setting model texture reflectivity stronger or lower(if reflectivity=0 - object have no reflection on the object and have no light reflection from the object at all)
		data.clear();
		
		data = OBJFileLoader.loadOBJ("fern/fern");
		Model fernModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices(), data.getTopModelVertex());
		ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern/fernTextureAtlase"));
		fernTextureAtlas.setNumberOfRows(2);// set amount of object textures at one loaded texture atlas
		TexturedModel texFernModel = new TexturedModel(fernModel, fernTextureAtlas);
		texFernModel.getTexture().setHasTransparency(true);//object rendering from both sides(from inside and outside)
		data.clear();
		
		data = OBJFileLoader.loadOBJ("grass/grass");
		TexturedModel texGrassModel = new TexturedModel(loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices(), data.getTopModelVertex()), new ModelTexture(loader.loadTexture("grass/grassTexture")));
		texGrassModel.getTexture().setHasTransparency(true);//object rendering from both sides(from inside and outside)
		texGrassModel.getTexture().setUseFakeLighting(true);//set direction of object normals to the top
		data.clear();
		
		data = OBJFileLoader.loadOBJ("grass/grass");
		TexturedModel texflowerModel = new TexturedModel(loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices(), data.getTopModelVertex()), new ModelTexture(loader.loadTexture("flower/flowerTexture")));
		texflowerModel.getTexture().setHasTransparency(true);//object rendering from both sides(from inside and outside)
		texflowerModel.getTexture().setUseFakeLighting(true);//set direction of object normals to the top
		data.clear();
		
		data = OBJFileLoader.loadOBJ("tree/tree");
		TexturedModel texTreeModel = new TexturedModel(loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices(), data.getTopModelVertex()), new ModelTexture(loader.loadTexture("tree/treeTexture")));
		data.clear();
		
		data = OBJFileLoader.loadOBJ("lamp/lamp");
		Model lampModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices(), data.getTopModelVertex());
		ModelTexture textureForLampModel = new ModelTexture(loader.loadTexture("lamp/lampTexture"));
		TexturedModel texLampModel = new TexturedModel(lampModel, textureForLampModel);
		texLampModel.getTexture().setUseFakeLighting(true);
		data.clear();
		
		/*data = OBJFileLoader.loadOBJ("lowPolyTree/lowPolyTree");
		Model lowPolyTreeModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());
		TexturedModel texLowPolyTreeModel = new TexturedModel(lowPolyTreeModel, new ModelTexture(loader.loadTexture("lowPolyTree/lowPolyTreeTexture")));
		data.clear();*/
		
		//Entity entity = new Entity(staticModel, new Vector3f(0,-2,-25),0,0,0,1);//check+
		
		//Light light = new Light(new Vector3f(0,10000,-7000), new Vector3f(1,1,1));
		List<Light> lights = new ArrayList<Light>();
		//lights.add(light);
		lights.add(new Light(new Vector3f(0, 1000, -7000), new Vector3f(0.4f, 0.4f, 0.4f), true));
		
		/*lights.add(new Light(new Vector3f(185, 10, -293), new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
		lights.add(new Light(new Vector3f(370, 17, -300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
		lights.add(new Light(new Vector3f(293, 7, -305), new Vector3f(2, 2, 0), new Vector3f(1, 0.01f, 0.002f)));*/
		
		//Creating terrain texture pack BEGIN
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("testMapTextures/grassTexture"));// texture which will main texture and will be rendered instead amount of black color on a blendMap Texture  
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("testMapTextures/mudTexture"));// texture which will be rendered instead amount of red color on a blendMap Texture  
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("testMapTextures/flowersTexture"));// texture which will be rendered instead amount of green color on a blendMap Texture  
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("testMapTextures/pathTexture"));// texture which will be rendered instead amount of blue color on a blendMap Texture  
		
		TerrainTexturePack terrainTexturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);	
		
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("testMapTextures/testMap"));// represent a texture map of one part of terrain, where one color corresponds to one texture, and final texture calculates by mix of amount of each texture at color
		
		//Creating terrain texture pack END
		
		Terrain terrain = new Terrain(0, 0, loader, terrainTexturePack, blendMap, "testMapTextures/heightMap");
		/*Terrain terrain2 = new Terrain(1, 0, loader, terrainTexturePack, blendMap, "testMapTextures/heightMap");
		Terrain terrain3 = new Terrain(0, 1, loader, terrainTexturePack, blendMap, "testMapTextures/heightMap");
		Terrain terrain4 = new Terrain(1, 1, loader, terrainTexturePack, blendMap, "testMapTextures/heightMap");*/
		
		/*Terrain terrain = new Terrain(0, 0, loader, new ModelTexture(loader.loadTexture("testMapTex/grassTexture")));
		Terrain terrain2 = new Terrain(1, 0, loader, new ModelTexture(loader.loadTexture("testMapTex/flowersTexture")));
		Terrain terrain3 = new Terrain(0, 1, loader, new ModelTexture(loader.loadTexture("testMapTex/mudTexture")));
		Terrain terrain4 = new Terrain(1, 1, loader, new ModelTexture(loader.loadTexture("testMapTex/pathTexture")));*/
		
		Player player = new Player(texPlayerModel, new Vector3f(50, 0, 50), 0, 0, 0, 1);
		Camera camera = new Camera(player);
		
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiTexture gui = new GuiTexture(loader.loadTexture("GUI/health"), new Vector2f(-0.7f, 0.9f), new Vector2f(0.25f, 0.25f));
		guis.add(gui);
		
		MasterRenderer renderer = new MasterRenderer(loader);
		
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);// using to trace mouse pointer ray at 3d world 
		Entity pointEntityBox = new Entity(staticModel, new Vector3f(-10f, -10f, -10f), 0f, 0f, 0f, 1f);// using at visualizing collision mouse pointer ray and terrain at 3d world 
		
		List<Entity> allEntities = new ArrayList<Entity>();
		Random random = new Random();
		
		allEntities.add(pointEntityBox);// using at visualizing collision mouse pointer ray and terrain at 3d world 
		
		/*allEntities.add(new Entity(texLampModel, new Vector3f(185, -4.7f, 293), 0, 0, 0, 1));
		allEntities.add(new Entity(staticModel, new Vector3f(185, 10, 293), 0, 0, 0, 1));
		lights.add(new Light(new Vector3f(185, 10, 293), new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
		
		allEntities.add(new Entity(texLampModel, new Vector3f(370, 4.2f, 300), 0, 0, 0, 1));
		lights.add(new Light(new Vector3f(370, 17, 300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
		
		allEntities.add(new Entity(texLampModel, new Vector3f(293, -6.8f, 305), 0, 0, 0, 1));
		lights.add(new Light(new Vector3f(293, 7, 305), new Vector3f(2, 2, 0), new Vector3f(1, 0.01f, 0.002f)));*/
		
		int step = 100;
		for(int i = step; i< step*6; i+=step)
		{
			float testLightX = i;
			float testLightZ = i;
			float testLightY = terrain.getHeightOfTerrain(testLightX, testLightZ);
			Vector3f color = null;
			if(i == step)
			{
				color = new Vector3f(0, 0, 2);
			}
			else if(i == 2*step)
			{
				color = new Vector3f(0, 2, 0);
			}
			else if(i == 3*step)
			{
				color = new Vector3f(2, 0, 0);
			}
			else if(i == 4*step)
			{
				color = new Vector3f(0, 2, 2);
			}
			else
			{
				color = new Vector3f(2, 2, 0);
			}
			
			allEntities.add(new Entity(texLampModel, new Vector3f(testLightX, testLightY, testLightZ), 0, 0, 0, 1));
			lights.add(new Light(new Vector3f(testLightX, testLightY+13, testLightZ), color, new Vector3f(1, 0.01f, 0.002f), false));
		}
		
		/*for(int i = 0; i< 50; i++)
		{
			float x = random.nextFloat() * 800 + 5;
			float z = random.nextFloat() * 800 + 5;
			float y = terrain.getHeightOfTerrain(x, z);
			allEntities.add(new Entity(texLampModel, new Vector3f(x, y, z), 0, 0, 0, 1));
			lights.add(new Light(new Vector3f(x, y+1, z), new Vector3f(random.nextFloat() * 10, random.nextFloat() * 10, random.nextFloat() * 10), new Vector3f(1, 0.01f, 0.002f)));
		}*/
		
		for(int i = 0; i< 200; i++)
		{
			float x = random.nextFloat() * 100 - 50;
			float y = random.nextFloat() * 100 - 50;
			float z = random.nextFloat() * 300;
			allEntities.add(new Entity(staticModel, new Vector3f(x, y, z), random.nextFloat() * 180f, random.nextFloat() * 180f, 0f, 1f));
		}
		
		for(int i = 0; i< 150; i++)
		{
			float x = random.nextFloat() * 800 + 5;
			float z = random.nextFloat() * 800 + 5;
			float y = terrain.getHeightOfTerrain(x, z);
			allEntities.add(new Entity(texFernModel, random.nextInt(4), new Vector3f(x, y, z), 0f, random.nextFloat() * 180f, 0f, 1f));
		}
		
		for(int i = 0; i< 500; i++)
		{
			float x = random.nextFloat() * 800 + 5;
			float z = random.nextFloat() * 800 + 5;
			float y = terrain.getHeightOfTerrain(x, z);
			allEntities.add(new Entity(texGrassModel, new Vector3f(x, y, z), 0f, random.nextFloat() * 180f, 0f, 1f));
		}
		
		for(int i = 0; i< 500; i++)
		{
			float x = random.nextFloat() * 800 + 5;
			float z = random.nextFloat() * 800 + 5;
			float y = terrain.getHeightOfTerrain(x, z);
			allEntities.add(new Entity(texflowerModel, new Vector3f(x, y, z), 0f, random.nextFloat() * 180f, 0f, 1f));
		}
		
		for(int i = 0; i< 75; i++)
		{
			float x = random.nextFloat() * 800 + 5;
			float z = random.nextFloat() * 800 + 5;
			float y = terrain.getHeightOfTerrain(x, z);
			allEntities.add(new Entity(texTreeModel, new Vector3f(x, y, z), 0f, random.nextFloat() * 180f, 0f, random.nextFloat() + 0.9f));		
		}
		
		/*for(int i = 0; i< 50; i++)
		{
			float x = random.nextFloat() * 800 + 5;
			float y = 0;
			float z = random.nextFloat() * 800 + 5;
			allEntities.add(new Entity(texLowPolyTreeModel, new Vector3f(x, y, z), 0f, random.nextFloat() * 180f, 0f, 1f));
		}*/
		
		
		while(!Display.isCloseRequested())
		{
			//game logic BEGIN
			/*entity.increasePosition(0.002f, 0, 0);
			entity.increaseRotation(0, 1, 0);*/
			//entity.increasePosition(0, 0, -0.1f);//move object entity
			//entity.increaseRotation(0, 0.5f, 0);//rotate object entity
			player.move(terrain);
			
			camera.move();
			
			picker.update();
			Vector3f terrainPoint = picker.getCurrentTerrainPoint();// using for getting current mouse pointer ray and terrain collision point
			if(terrainPoint!=null)
			{
				pointEntityBox.setPosition(terrainPoint);
			}
			
			
			//System.out.println(picker.getCurrentRay());
			
			//game logic END
			
			/*renderer.prepare();
			shader.start();
			shader.loadLight(light);
			shader.loadViewMatrix(camera);*/
			//renderer.render(model);//render
			//renderer.render(texturedModel);//render
			//renderer.render(entity, shader);//render entity with shaders
			
			renderer.processEntity(player);
			
			renderer.processTerrain(terrain);
			/*renderer.processTerrain(terrain2);
			renderer.processTerrain(terrain3);
			renderer.processTerrain(terrain4);*/
			
			for(Entity object : allEntities)
			{
				//renderer.render(object, shader);
				renderer.processEntity(object);//load "object" to HashMap which will be rendered
			}
			/*shader.stop();*/
			renderer.render(lights, camera);
			guiRenderer.render(guis);
			DisplayManager.updateDisplay();
		}
		
		guiRenderer.cleanUp();
		
		renderer.cleanUp();
		
		/*shader.cleanUp();*/
		
		loader.cleanUP();
		
		DisplayManager.closeDisplay();

	}

}
