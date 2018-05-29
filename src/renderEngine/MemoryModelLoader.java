package renderEngine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Position;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import models.Model;
import textures.TextureData;

public class MemoryModelLoader 
{
	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();
	
	public Model loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices, float topModelVertex)// used to load all game objects but GUI
	{
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);//1-st line in VAO is positions VBO
		storeDataInAttributeList(1, 2, textureCoords);//2-nd line in VAO is textures VBO
		storeDataInAttributeList(2, 3, normals);//3-nd line in VAO is normals(using for lightning effect) VBO
		unbindVAO();
		return new Model(vaoID, indices.length, topModelVertex);
	}
	
	public Model loadToVAO(float[] positions, int dimensions)// used to load GUI objects or SkyBox
	{
		int vaoID = createVAO();
		storeDataInAttributeList(0, dimensions, positions);// 1-st line in VAO is positions VBO for GUI object
		unbindVAO();
		return new Model(vaoID, positions.length / dimensions, 0);
	}
	
	public int loadTexture(String fileName)
	{
		Texture texture = null;
		try 
		{
			texture = TextureLoader.getTexture("PNG", new FileInputStream("res/textures/"+fileName+".png"));
			/*used for optimize code by decreasing texture resolution of objects which located far from the camera(then higher than biggest decreasing) BEGIN*/
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);// Generate all low resolution versions of the textures
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);// used to tell OpenGL use low resolution textures. "GL11.GL_TEXTURE_MIN_FILTER" - used to tell OpenGL use low resolution models on visible object parts which have less dimension size that texture by which they covered. "GL11.GL_LINEAR_MIPMAP_LINEAR" - tell to use generated above mipmap texture and make texture more smoother than "GL11.GL_TEXTURE_MIN_NEAREST".  
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.7f);// "GL14.GL_TEXTURE_LOD_BIAS"- level of detail bias. Used to decrease(negative values) or increase(positive values) effect from using low resolution textures
			/*used for optimize code by decreasing texture resolution of objects which located far from the camera(then higher than biggest decreasing) END*/
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		int textureID = texture.getTextureID();
		textures.add(textureID);
		return textureID;
	}
	
	public int loadCubeMap(String[] textureFiles)//method which can load a Cube Map to OpenGL. textureFiles - stores 6 texture file names 1 texture for one edge of cube
	{
		int texID = GL11.glGenTextures();// generate a completely empty texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);// activate texture unit 0 to bind texture to it
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID);// bind new empty texture "texID" to currently active texture unit "GL13.GL_TEXTURE0" as "GL13.GL_TEXTURE_CUBE_MAP"
		
		for(int i=0; i<textureFiles.length; i++)// loop threw all 6 textures from "textureFiles" and load up each one of it to cube map
		{
			TextureData data = decodeTextureFile("res/textures/SkyBoxes/"+textureFiles[i]+".png");// decode .png files into TextureData
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i/*target*/, 0/*level*/, GL11.GL_RGBA/*format at which we want to store the data*/, data.getWidth(), data.getHeight(), 0/*border*/, GL11.GL_RGBA/*format at which we want to store the data*/, GL11.GL_UNSIGNED_BYTE/*type of the data is bytes*/, data.getBuffer()/*actual texture data*/);// load up the TextureData into a cube map which we bind above
			/*
			 * "target" shows which face of the cube map we want to load the texture data to. Calculated by increasing int numbers of constants by 1 that changes face of cube map texture
			 	At "textureFiles" array textures must be stored at same order like constants below
			 	GL_TEXTURE_CUBE_MAP_POSITIVE_X = 34069 = Right Face
				GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 34070 = Left Face
				GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 34071 = Top Face
				GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 34072 = Bottom Face
				GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 34073 = Back Face
				GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 34074 = Front Face
			*/
		}
		
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);// if original image is SMALLER than the rectangle it was rasterized on when scaling the image up "GL11.GL_TEXTURE_MAG_FILTER". Makes cube map texture pics smooth. GL11.GL_LINEAR- Returns the weighted average of the 4 pixels surrounding the given coordinates.
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);// used if original image is BIGGER than the rectangle it was rasterized on when scaling the image down "GL11.GL_TEXTURE_MIN_FILTER". Makes cube map texture pics smooth. GL11.GL_LINEAR- Returns the weighted average of the 4 pixels surrounding the given coordinates.
		
		textures.add(texID);// used for storing texture id for moment when game will be closed and all textures must be cleaning up
		
		return texID;
	}
	
	private TextureData decodeTextureFile(String fileName) // used PNGDecoder to load image as ByteBuffer and after return it in TextureData object
	{
		int width = 0;
		int height = 0;
		ByteBuffer buffer = null;
		try 
		{
			FileInputStream in = new FileInputStream(fileName);
			PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			buffer = ByteBuffer.allocateDirect(4 * width * height);//created a direct buffer(direct buffer), which created by calling method (allocateDirect). This buffer allows to take data outside the heap in the Java language.
			decoder.decode(buffer, width * 4, Format.RGBA);// put decoded data into buffer
			buffer.flip();// tell that buffer is ready for reading
			in.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.err.println("Tried to load texture " + fileName + ", didn't work");
			System.exit(-1);
		}
		return new TextureData(buffer, width, height);
	}
	
	public void cleanUP()
	{
		for(int vao:vaos)
		{
			GL30.glDeleteVertexArrays(vao);
		}
		
		for(int vbo:vbos)
		{
			GL15.glDeleteBuffers(vbo);
		}
		for(int texture:textures)
		{
			GL11.glDeleteTextures(texture);
		}
	}
	
	private int createVAO()
	{
		int vaoID= GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}
	
	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data)
	{
		int vboID = GL15.glGenBuffers(); 
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);//offset
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	private void unbindVAO()
	{
		GL30.glBindVertexArray(0);
	}
	
	private void bindIndicesBuffer(int[] indices)
	{
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}
	
	private IntBuffer storeDataInIntBuffer(int[] data)
	{
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	private FloatBuffer storeDataInFloatBuffer(float[] data)
	{
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;		
	}
}
