package renderEngine;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import entities.Entity;
import models.Model;
import models.TexturedModel;
import shaders.StaticShader;
import textures.ModelTexture;
import toolbox.Maths;

public class EntityRenderer 
{	
	/*private static final float FOV = 70;// Field/angle of view
	private static final float NEAR_PLANE = 0.1f;// Closest distance which you can see
	private static final float FAR_PLANE = 1000;// Farrest distance which you can see*/
	
	//private Matrix4f projectionMatrix;
	
	/*//My variables BEGIN
	//private float closestDistance = 0;
	private float farestDistance = 0;
	
	private Vector3f cameraPos = null;
	//My variables ENDS*/
	
	private StaticShader shader;// calculation shader for objects, used for drawing every pixel
	
	public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix)// check projectionMatrix
	{
		this.shader = shader;
		
		//createProjectionMatrix();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);//matrix which add "z" axis. So it makes view angles more realistic
		shader.stop();
	}
	
	
	/*//My Methods BEGIN
	public void setRenderDistance(float farestDistance)
	{
		this.farestDistance = farestDistance;
	}
	
	public void setCameraPosition(Vector3f cameraPos)
	{
		this.cameraPos = cameraPos;
	}
	//My Methods ENDS*/

	/*public void render(Model model)// render just models without textures
	{
		GL30.glBindVertexArray(model.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		//GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getVertexCount());
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}*/
	/*public void render(TexturedModel texturedModel)// render models with textures
	{
		Model model = texturedModel.getRawModel();
		GL30.glBindVertexArray(model.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturedModel.getTexture().getID());
		//GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getVertexCount());
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}*/
	
	public void render(Map<TexturedModel, List<Entity>> entities)
	{
		for(TexturedModel model:entities.keySet())//entities.keySet() means that "model" gets one next entity key which have type "TexturedModel" on each iteration
		{
			prepareTexturedModel(model);
			List<Entity> batch = entities.get(model);
			for(Entity entity:batch)
			{
				/*//My idea BEGIN
				 	if((entity.getPosition().getX() > cameraPos.getX() - farestDistance)&&
				   (entity.getPosition().getX() < cameraPos.getX() + farestDistance)&&
				   (entity.getPosition().getY() > cameraPos.getY() - farestDistance)&&
				   (entity.getPosition().getY() < cameraPos.getY() + farestDistance)&&
				   (entity.getPosition().getZ() > cameraPos.getZ() - farestDistance)&&
				   (entity.getPosition().getZ() < cameraPos.getZ() + farestDistance))
				{
				//my idea ENDS*/
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);//final render method
				//}
			}
			unbindTexturedModel();
		}
	}
	
	private void prepareTexturedModel(TexturedModel model)//this part needs to be done just one time for each group of TexturedModel
	{
		Model rawModel = model.getRawModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		
		GL20.glEnableVertexAttribArray(0);//enables VBO(attribute from VAO) number 0
		GL20.glEnableVertexAttribArray(1);//enables VBO(attribute from VAO) number 1
		GL20.glEnableVertexAttribArray(2);//enables VBO(attribute from VAO) number 2
	
		ModelTexture texture = model.getTexture();//getting the texture
		
		shader.loadNumberOfRows(texture.getNumberOfRows());
		
		if(texture.isHasTransparency())//check if current "texture" have a transparency or not
		{
			MasterRenderer.disableCulling();//switch on rendering of object from inside in OpenGL options
		}
		/*else
		{
			MasterRenderer.enableCulling();//switch off rendering of object from inside in OpenGL options
		}*/
		
		shader.loadFakeLightingVariable(texture.isUseFakeLighting());//used to load up boolean value from "texture" to solve use fake lighting or not 
		
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());//using for loading shine variables: 1) reflectivity and 2) shineDump(spread range of reflective light) of the texture
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());//binding the texture
	}
	
	private void unbindTexturedModel()//when we finished rendering all the entities we unbind them
	{
		MasterRenderer.enableCulling();////switch off rendering of object from inside in OpenGL options
		
		GL20.glDisableVertexAttribArray(0);//disables VBO(attribute from VAO) number 0
		GL20.glDisableVertexAttribArray(1);//disables VBO(attribute from VAO) number 1
		GL20.glDisableVertexAttribArray(2);//disables VBO(attribute from VAO) number 2
		
		GL30.glBindVertexArray(0);
	}
	
	private void prepareInstance(Entity entity)// set "transformationMatrix" which used for setting object position at the world. Load model matrix. Prepare all instances of the entities of all binded textured models 
	{
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());//creation of transformationMatrix
		shader.loadTransformationMatrix(transformationMatrix);//load it up to the shader
		shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
	}
	
	/*public void render(Entity entity, StaticShader shader)
	{
		TexturedModel model = entity.getModel();
		Model rawModel = model.getRawModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		
		GL20.glEnableVertexAttribArray(0);//enables VBO(attribute from VAO) number 0
		GL20.glEnableVertexAttribArray(1);//enables VBO(attribute from VAO) number 1
		GL20.glEnableVertexAttribArray(2);//enables VBO(attribute from VAO) number 2
		
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());//creation of transformationMatrix
		shader.loadTransformationMatrix(transformationMatrix);//load it up to the shader
		
		ModelTexture texture = model.getTexture();//getting the texture
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());//using for loading shine variables: 1) reflectivity and 2) shineDump(spread range of reflective light) of the texture
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());//binding the texture
		//GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getVertexCount());
		GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		
		GL20.glDisableVertexAttribArray(0);//disables VBO(attribute from VAO) number 0
		GL20.glDisableVertexAttribArray(1);//disables VBO(attribute from VAO) number 1
		GL20.glDisableVertexAttribArray(2);//disables VBO(attribute from VAO) number 2
		
		GL30.glBindVertexArray(0);
	}*/
	
	/*private void createProjectionMatrix()//check+*
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
	}*/
}
