package terrains;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import models.Model;
import renderEngine.MemoryModelLoader;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.Maths;

public class Terrain 
{
	private static final float SIZE = 800;
	//private static final int VERTEX_COUNT = 128;
	private static final float MAX_HEIGHT = 40;// the highest point of the map will be "40" and the lowest point of the map was "-40"
	private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;// maximum color value which pixel on a "heightMap" can have. Because each color from RGB have it's own channel and so we have 3 channels with range 0-255  
	
	private float x;
	private float z;
	private Model model;
	private TerrainTexturePack texturePack;
	private TerrainTexture blendMap;
	
	private float [] [] heights; // here storing heights of each vertex on the terrain
	
	public Terrain(int gridX, int gridZ, MemoryModelLoader loader, TerrainTexturePack texturePack, TerrainTexture blendMap, String heightMap)
	{
		this.texturePack = texturePack;
		this.blendMap = blendMap;
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		this.model = generateTerrain(loader, heightMap);	
	}
	
	public float getX() 
	{
		return x;
	}

	public float getZ() 
	{
		return z;
	}

	public Model getModel() 
	{
		return model;
	}

	
	
	public TerrainTexturePack getTexturePack() 
	{
		return texturePack;
	}

	public TerrainTexture getBlendMap() 
	{
		return blendMap;
	}
	
	public float getHeightOfTerrain(float worldX, float worldZ)
	{
		float terrainX = worldX - this.x;// "terrainX" - used to get info is object located at this terrain by x-axis or not, where (0,0) is beginning of current terrain and ("SIZE", "SIZE") is end of the current terrain, if value more or less than this coordinates that means that player are not located at current terrain. "worldX" - is x-axis position of our 3d-object at the world. "this.x" - is position("this.x" * "SIZE") of terrain by x-axis at grid map of all terrains
		float terrainZ = worldZ - this.z;// "terrainZ" - used to get info is object located at this terrain by z-axis or not, where (0,0) is beginning of current terrain and ("SIZE", "SIZE") is end of the current terrain, if value more or less than this coordinates that means that player are not located at current terrain. "worldZ" - is z-axis position of our 3d-object at the world. "this.y" - is position("this.z" * "SIZE") of terrain by z-axis at grid map of all terrains
		
		float gridSquareSize = SIZE / (float) (heights.length - 1);// used to calculate amount of squares at current terrain by using constant "SIZE" of the terrain and amount of vertices at line on it. "(heights.length - 1)" - this part used because in a grid 4 vertices in the row from one side and 4 vertices in a row at another side can create just 3 grid squares.
		
		int gridX = (int) Math.floor(terrainX / gridSquareSize);// used to find out at which grid square of this terrain this "X" coordinate(player "X" coordinate) is in. "terrainX / gridSquareSize" - used to calculate at which square of this terrain object located, where "terrainX" - is size by x-axis this terrain and "gridSquareSize" - is amount of squares at one line of this terrain. "Math.floor()" - get just integer part of number
		int gridZ = (int) Math.floor(terrainZ / gridSquareSize);// used to find out at which grid square of this terrain this "Z" coordinate(player "Z" coordinate) is in. "terrainZ / gridSquareSize" - used to calculate at which square of this terrain object located, where "terrainZ" - is size by z-axis this terrain and "gridSquareSize" - is amount of squares at one line of this terrain. "Math.floor()" - get just integer part of number
		
		if(gridX >= heights.length - 1 ||
		   gridX < 0 ||
		   gridZ >= heights.length -1 ||
		   gridZ < 0)// test if this "X", "Z" position is on the terrain
		{
			return 0;
		}
		float xCoord = (terrainX % gridSquareSize) / gridSquareSize;// looking for where at grid square with number "gridX" by x-axis player located relatively from top left corner by "X"-axis. We divide(/) on "gridSquareSize" again to get values from (0, 0) to (1, 1) 
		float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;// looking for where at grid square with number "gridY" by y-axis player located relatively from top left corner by "Z"-axis. We divide(/) on "gridSquareSize" again to get values from (0, 0) to (1, 1)
		
		float answer;
		
		if(xCoord <= (1 - zCoord))// our square consist of two triangles, so this check used to decide at which triangle player located. All vertices at line between this two triangles we can count by formula (x+z=1) or (x=1-z). In other case all vertices at top triangle could be counted by this formula (x<1-z) and at bottom triangle all vertices would be counted like (x>1-z)   
		{
			//top left triangle consist of (top left), (top right) and (bottom left) vertices
			answer = Maths.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0)/*(top left)*/, 
										new Vector3f(1,	heights[gridX + 1][gridZ], 0)/*(top right)*/,
										new Vector3f(0,	heights[gridX][gridZ + 1], 1)/*(bottom left)*/,
										new Vector2f(xCoord, zCoord)/*(current player position at triangle which created when we connect 3 vertex above)*/);
		}
		else
		{
			//bottom right triangle consist of (top right), (bottom right) and (bottom left) vertices
			answer = Maths.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0)/*(top right)*/,
										new Vector3f(1,	heights[gridX + 1][gridZ + 1], 1)/*(bottom right)*/,
										new Vector3f(0,	heights[gridX][gridZ + 1], 1)/*(bottom left)*/,
										new Vector2f(xCoord, zCoord)/*(current player position at triangle which created when we connect 3 vertex above)*/);
		}
		return answer;
	}

	private Model generateTerrain(MemoryModelLoader loader, String heightMap)//check
	{
		BufferedImage image = null;
		try 
		{
			image = ImageIO.read(new File("res/textures/"+heightMap+".png"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		int VERTEX_COUNT = image.getHeight();// one pixel on the heightMap is one vertex. heightMap should be square(have same height and width). 
		
		heights = new float [VERTEX_COUNT][VERTEX_COUNT];
		
		int count = VERTEX_COUNT * VERTEX_COUNT;// amount of points from which map created
		
		float[] vertices = new float[count * 3];//current vertex targeting (3D coordinates)
		float[] normals = new float[count * 3];//current normal targeting (3D coordinates)
		float[] textureCoords = new float[count*2];//current texture position (2D coordinates)
		
		int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];//indices for vertex from which triangles created one polygon have 2 triangles and each triangle have by 3 vertexes 2 of them same but we take 3 vertex from one triangle + 3 from another = 6 because 1 vertex could be in a three triangles
		
		int vertexPointer = 0;
		for(int i=0;i<VERTEX_COUNT;i++)
		{
			for(int j=0;j<VERTEX_COUNT;j++)
			{
				vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * SIZE;//calculating vertex "x" position coordinates
				float height = getHeight(j, i, image);//calculating vertex "y" position coordinates
				heights[j][i] = height;
				vertices[vertexPointer*3+1] = height;
				vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * SIZE;//calculating vertex "z" position coordinates
				
				Vector3f normal = calculateNormal(j, i, image);// calculate normal pointing
				normals[vertexPointer*3] = normal.x;//calculating vertex normal aiming by axis "x"
				normals[vertexPointer*3+1] = normal.y;//calculating vertex normal aiming by axis "y"
				normals[vertexPointer*3+2] = normal.z;//calculating vertex normal aiming by axis "z"
				
				textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);//calculating vertex texture position be axis "x"
				textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);//calculating vertex texture position be axis "y"
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz=0;gz<VERTEX_COUNT-1;gz++)
		{
			for(int gx=0;gx<VERTEX_COUNT-1;gx++)
			{
				/*Calculation of map rendering by line*/
				int topLeft = (gz*VERTEX_COUNT)+gx;//calculating top left coordinates of line. By "x" and "z" which was equal First value was: 0;
				int topRight = topLeft + 1;//calculating top right coordinates of line. By "x" and "z" which was equal First value was: 1;
				int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;//calculating bottom left coordinates of line. By "x" and "z" which was equal First value was: 128;
				int bottomRight = bottomLeft + 1;//calculating bottom right coordinates of line. By "x" and "z" which was equal First value was: 129;
				
				/*save one line (polygon) like 2 triangles*/
				indices[pointer++] = topLeft;//1
				indices[pointer++] = bottomLeft;//2
				indices[pointer++] = topRight;//3
				indices[pointer++] = topRight;//3
				indices[pointer++] = bottomLeft;//2
				indices[pointer++] = bottomRight;//1
			}
		}
		return loader.loadToVAO(vertices, textureCoords, normals, indices, 0);
	}
	
	//check
	private Vector3f calculateNormal(int x, int z, BufferedImage image)// used for calculate and return normal(Vector3f) for vertex with coordinates "x" and "z" on the map. 
	{
		/*For calculate normal of current vertex we need calculate height of all four neighbors of this vertex
		 * BEGIN*/
		float heightL = getHeight(x - 1, z, image);// calculate left neighbor height of current vertex
		float heightR = getHeight(x + 1, z, image);// calculate right neighbor height of current vertex
		float heightU = getHeight(x, z + 1, image);// calculate up neighbor height of current vertex
		float heightD = getHeight(x, z - 1, image);// calculate down neighbor height of current vertex
		/*END*/
		Vector3f normal = new Vector3f(heightL - heightR,
										2f,
										heightD - heightU);// calculation of normal vector aiming. We set Vector3f("normal") "y" value to "2f" - to set length of vector to "2f". And "x" and "z" values we calculates by principle than more difference between height of to opposite hills then more normal points by side of smaller hill 
		normal.normalise();// used for check and if needed made length of normal equal to one
		return normal;
	}
	
	private float getHeight(int x, int z, BufferedImage image)// used to get vertex on the map with coordinates "x" and "z", height which corresponds to color of point which located by "x" and "z" coordinates at the heightMap("image")
	{
		if(x < 0 || x >= image.getWidth() ||
		   z < 0 || z >= image.getHeight())
		{
			return 0;
		}
		float height = image.getRGB(x, z);// returns float value(from -"MAX_PIXEL_COLOUR" to 0) which corresponds to color of pixel with coordinates "x" and "z" on heightMap("image")
		height += MAX_PIXEL_COLOUR / 2f;// convert "height" to new range (from -("MAX_PIXEL_COLOUR"/2) to ("MAX_PIXEL_COLOUR"/2))
		height /= MAX_PIXEL_COLOUR / 2f;// convert "height" to new range (from -1 to 1)
		height *= MAX_HEIGHT;// final height variable range is from -MAX_HEIGHT to MAX_HEIGHT. But "MAX_HEIGHT"(40) less than "MAX_PIXEL_COLOUR"(256*256*256)
		return height;
	}
}
