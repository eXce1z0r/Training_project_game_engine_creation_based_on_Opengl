package objConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class OBJFileLoader {
	
	private static final String RES_LOC = "res/models/";
	
	// method used to load data from .obj file to 4 arrays: "vertices", "textures", "normals", "indices" and return ModelData format which used to load this data to VAO
	public static ModelData loadOBJ(String objFileName) 
	{
		FileReader isr = null;
		File objFile = new File(RES_LOC + objFileName + ".obj");
		try {
			isr = new FileReader(objFile);
		} catch (FileNotFoundException e) {
			System.err.println("File not found in res; don't use any extention");
		}
		BufferedReader reader = new BufferedReader(isr);
		String line;
		List<Vertex> vertices = new ArrayList<Vertex>();
		List<Vector2f> textures = new ArrayList<Vector2f>();
		List<Vector3f> normals = new ArrayList<Vector3f>();
		List<Integer> indices = new ArrayList<Integer>();
		
		float topModelVertex = 0;
		
		try {
			while (true) {
				line = reader.readLine();
				/*	
				 * //for Blender objects
				 * 
				 * if (line.startsWith("v ")) {
					String[] currentLine = line.split(" ");
					Vector3f vertex = new Vector3f((float) Float.valueOf(currentLine[1]),
							(float) Float.valueOf(currentLine[2]),
							(float) Float.valueOf(currentLine[3]));
					Vertex newVertex = new Vertex(vertices.size(), vertex);
					vertices.add(newVertex);

				}*/
				
				//for 3d max objects
				if (line.startsWith("v ")) {
					String[] currentLine = line.split(" ");
					Vector3f vertex = new Vector3f((float) Float.valueOf(currentLine[2]),
							(float) Float.valueOf(currentLine[3]),
							(float) Float.valueOf(currentLine[4]));
					Vertex newVertex = new Vertex(vertices.size(), vertex);
					vertices.add(newVertex);
					
					if((vertex.y > topModelVertex) || (topModelVertex == 0))// used to find top model vertex
					{
						topModelVertex = vertex.y;
					}

				} else if (line.startsWith("vt ")) {
					String[] currentLine = line.split(" ");
					Vector2f texture = new Vector2f((float) Float.valueOf(currentLine[1]),
							(float) Float.valueOf(currentLine[2]));
					textures.add(texture);
				} else if (line.startsWith("vn ")) {
					String[] currentLine = line.split(" ");
					Vector3f normal = new Vector3f((float) Float.valueOf(currentLine[1]),
							(float) Float.valueOf(currentLine[2]),
							(float) Float.valueOf(currentLine[3]));
					normals.add(normal);
				} else if (line.startsWith("f ")) {
					break;
				}
			}
			while (line != null && line.startsWith("f ")) {
				String[] currentLine = line.split(" ");
				String[] vertex1 = currentLine[1].split("/");
				String[] vertex2 = currentLine[2].split("/");
				String[] vertex3 = currentLine[3].split("/");
				processVertex(vertex1, vertices, indices);
				processVertex(vertex2, vertices, indices);
				processVertex(vertex3, vertices, indices);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Error reading the file");
		}
		removeUnusedVertices(vertices);
		float[] verticesArray = new float[vertices.size() * 3];
		float[] texturesArray = new float[vertices.size() * 2];
		float[] normalsArray = new float[vertices.size() * 3];
		float furthest = convertDataToArrays(vertices, textures, normals, verticesArray,
				texturesArray, normalsArray);
		int[] indicesArray = convertIndicesListToArray(indices);
		
		ModelData data = new ModelData(verticesArray, texturesArray, normalsArray, indicesArray,
				furthest, topModelVertex);
		
		return data;
	}

	// method used to arrange data from .obj file at correct sequence with help lines begins with letter "f " and separate by "/"
	// 1/2/3 
	// where:
	// 1 - vertex id
	// 2 - texture id
	// 3 - normal id
	private static void processVertex(String[] vertex, List<Vertex> vertices, List<Integer> indices)
	{
		int index = Integer.parseInt(vertex[0]) - 1;
		int textureIndex = Integer.parseInt(vertex[1]) - 1;
		int normalIndex = Integer.parseInt(vertex[2]) - 1;
		Vertex currentVertex = vertices.get(index); // getting all info about new vertex which we try to add in vertex array which will be drawn
		
		if (!currentVertex.isSet()) // vertex is just created and have no texture and normal, so we can set them 
		{
			currentVertex.setTextureIndex(textureIndex);
			currentVertex.setNormalIndex(normalIndex);
			indices.add(index);
		} 
		else // for this vertex already been added textures and normals 
		{
			dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices,
					vertices);
		}
	}

	private static int[] convertIndicesListToArray(List<Integer> indices) {
		int[] indicesArray = new int[indices.size()];
		for (int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		return indicesArray;
	}

	private static float convertDataToArrays(List<Vertex> vertices, List<Vector2f> textures,
			List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
			float[] normalsArray) {
		float furthestPoint = 0;
		for (int i = 0; i < vertices.size(); i++) {
			Vertex currentVertex = vertices.get(i);
			if (currentVertex.getLength() > furthestPoint) {
				furthestPoint = currentVertex.getLength();
			}
			Vector3f position = currentVertex.getPosition();
			Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
			Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
			verticesArray[i * 3] = position.x;
			verticesArray[i * 3 + 1] = position.y;
			verticesArray[i * 3 + 2] = position.z;
			texturesArray[i * 2] = textureCoord.x;
			texturesArray[i * 2 + 1] = 1 - textureCoord.y;
			normalsArray[i * 3] = normalVector.x;
			normalsArray[i * 3 + 1] = normalVector.y;
			normalsArray[i * 3 + 2] = normalVector.z;
		}
		return furthestPoint;
	}

	//method used for setting texture and normal for already existed vertex 
	private static void dealWithAlreadyProcessedVertex(Vertex previousVertex, int newTextureIndex,
			int newNormalIndex, List<Integer> indices, List<Vertex> vertices) 
	{
		if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) // one vertex used by few triangles because position coordinates, texture and normal parameters the same
		{
			indices.add(previousVertex.getIndex());
		} 
		else // new vertex with same position coordinates, but different texture and normal parameters with already added vertex. But in our game engine this is impossible due some OpenGL features
		{
			Vertex anotherVertex = previousVertex.getDuplicateVertex();
			if (anotherVertex != null) // if we find more than two vertex with same position coordinates, but different texture and normal. We moved by this recursion to find vertex with empty "duplicateVertex" and added them to it by the help else of this if case
			{
				dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex,
						indices, vertices);
			} 
			else // create duplicate of current vertex in already existed vertex(in array which will be converted to VAO array) with this position coordinates, but set them new texture and normal coordinates. By this way we can have vertices with same coordinates but two or more different textures and normals, so By this way we solved OpenGL problem 
			{
				Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition());
				duplicateVertex.setTextureIndex(newTextureIndex);
				duplicateVertex.setNormalIndex(newNormalIndex);
				previousVertex.setDuplicateVertex(duplicateVertex);
				vertices.add(duplicateVertex);
				indices.add(duplicateVertex.getIndex());
			}

		}
	}
	
	private static void removeUnusedVertices(List<Vertex> vertices){
		for(Vertex vertex:vertices){
			if(!vertex.isSet()){
				vertex.setTextureIndex(0);
				vertex.setNormalIndex(0);
			}
		}
	}

}