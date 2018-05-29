package guis;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import models.Model;
import renderEngine.MemoryModelLoader;
import toolbox.Maths;

public class GuiRenderer 
{
	private final Model quad;// OpenGL game display have coordinates (-1,-1);(1,-1);(1,1);(-1,1);  
	private GuiShader shader;
	
	public GuiRenderer(MemoryModelLoader loader)
	{
		float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};// OpenGL game display have coordinates (-1,1); (-1,-1); (1,1); (1,-1); Last point connected by method of "Triangle Strips"
		quad = loader.loadToVAO(positions, 2);// load to "quad" model with 2 dimensions(x, y)
		shader = new GuiShader();
	}
	
	public void render(List<GuiTexture> guis)
	{
		shader.start();
		GL30.glBindVertexArray(quad.getVaoID());// used bind VAO of our "quad" model, because at future all GUI will be situated on it
		GL20.glEnableVertexAttribArray(0);// activate VBO number 0 from already binded VAO above. This VBO stored position of "quad" model 
		
		GL11.glEnable(GL11.GL_BLEND);// ? used to enable transparency at GUIs
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);// used to set kind of transparency
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);//disable depth test(tell OpenGL render GUI textures which was located under another GUI texture) 
		
		//rendering BEGIN
		for(GuiTexture gui: guis)
		{
			GL13.glActiveTexture(GL13.GL_TEXTURE0);// used for binding texture storage to our "quad"
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, gui.getTexture());// used for binding GUI texture to our texture storage which we already binded to "quad"
			
			Matrix4f matrix = Maths.createTransformationMatrix(gui.getPosition(), gui.getScale());// creation transformation matrix for each GUI used for moving, scrolling or rotating out GUI 
			shader.loadTransformation(matrix);// used to load transformation matrix for each GUI to shader
			
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0/*first index of vertex*/, quad.getVertexCount()/*last index of vertex*/);// GL_TRIANGLE_STRIP(we should set first 3 vertexes which created a triangle, but after that all new vertexes which will be created would automaticaly connected to 2 closest alreary existed vertexes and created new triangle)
		}
		//rendering END
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);// enable depth test(tell OpenGL do not render GUI textures which was located under another GUI texture) 
		
		GL11.glDisable(GL11.GL_BLEND);// ? used to disable transparency at GUIs
		
		GL20.glDisableVertexAttribArray(0);// disable VBO number 0 from already binded VAO above. This VBO stored position of "quad" model 
		GL30.glBindVertexArray(0);// unbind "quad"-s VAO
		shader.stop();
	}
	
	public void cleanUp()
	{
		shader.cleanUp();
	}
}
