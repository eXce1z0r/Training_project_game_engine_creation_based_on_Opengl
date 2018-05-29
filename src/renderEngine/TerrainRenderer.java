package renderEngine;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import models.Model;
import models.TexturedModel;
import shaders.TerrainShader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexturePack;
import toolbox.Maths;

public class TerrainRenderer 
{
	private TerrainShader shader;// calculation shader for terrain, used for drawing every pixel
	
	public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix)
	{
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.connectTextureUnits();
		shader.stop();
	}
	
	public void render(List<Terrain> terrains)
	{
		for(Terrain terrain:terrains)
		{
			prepareTerrainModel(terrain);//binds the model of terrain, binds the texture
			loadModelMatrix(terrain);//load model matrix for the terrain
			GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.getModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);//final render method
			unbindTexturedModel();
		}
	}
	
	private void prepareTerrainModel(Terrain terrain)//this part needs to be done just one time for each group of TexturedModel
	{
		Model rawModel = terrain.getModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		
		GL20.glEnableVertexAttribArray(0);//enables VBO(attribute from VAO) number 0
		GL20.glEnableVertexAttribArray(1);//enables VBO(attribute from VAO) number 1
		GL20.glEnableVertexAttribArray(2);//enables VBO(attribute from VAO) number 2
	
		
		bindTextures(terrain);//getting the textures
		
		shader.loadShineVariables(1, 0);//using for loading shine variables: 1) reflectivity and 2) shineDump(spread range of reflective light) of the texture
		
		/*ModelTexture texture = terrain.getTexture();//getting the texture
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());//using for loading shine variables: 1) reflectivity and 2) shineDump(spread range of reflective light) of the texture
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getID());//binding the texture*/
	}
	
	private void bindTextures(Terrain terrain)
	{
		TerrainTexturePack texturePack = terrain.getTexturePack();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getBackgroundTexture().getTextureID());//binding the background texture to GL13.GL_TEXTURE0 which will be connected with sampler2D variables in future
		
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getrTexture().getTextureID());//binding secound texture to GL13.GL_TEXTURE1 which will be connected with sampler2D variables in future
		
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getgTexture().getTextureID());//binding third texture to GL13.GL_TEXTURE2 which will be connected with sampler2D variables in future
		
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getbTexture().getTextureID());//binding fourth texture to GL13.GL_TEXTURE3 which will be connected with sampler2D variables in future
		
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getBlendMap().getTextureID());//binding fifth texture(blend map) to GL13.GL_TEXTURE4 which will be connected with sampler2D variables in future	
	}
	
	private void unbindTexturedModel()//when we finished rendering all the entities we unbind them
	{
		GL20.glDisableVertexAttribArray(0);//disables VBO(attribute from VAO) number 0
		GL20.glDisableVertexAttribArray(1);//disables VBO(attribute from VAO) number 1
		GL20.glDisableVertexAttribArray(2);//disables VBO(attribute from VAO) number 2
		
		GL30.glBindVertexArray(0);
	}
	
	private void loadModelMatrix(Terrain terrain)// set "transformationMatrix" which used for setting object position at the world. Load model matrix. Prepare all instances of the entities of all binded textured models 
	{
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(new Vector3f(terrain.getX(), 0, terrain.getZ()), 0, 0, 0, 1);//creation of transformationMatrix
		shader.loadTransformationMatrix(transformationMatrix);//load it up to the shader
	}
	
}
