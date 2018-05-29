package objConverter;

//class ModelData used to store all needed for load to VAO arrays
public class ModelData {

	private float[] vertices;
	private float[] textureCoords;
	private float[] normals;
	private int[] indices;
	private float furthestPoint;
	
	private float topModelVertex;

	public ModelData(float[] vertices, float[] textureCoords, float[] normals, int[] indices,
			float furthestPoint, float topModelVertex) {
		this.vertices = vertices;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.indices = indices;
		this.furthestPoint = furthestPoint;
		this.topModelVertex = topModelVertex;
	}

	public float[] getVertices() {
		return vertices;
	}

	public float[] getTextureCoords() {
		return textureCoords;
	}

	public float[] getNormals() {
		return normals;
	}

	public int[] getIndices() {
		return indices;
	}

	public float getFurthestPoint() {
		return furthestPoint;
	}
	
	public float getTopModelVertex() {
		return topModelVertex;
	}

	public void clear()
	{
		vertices = null;
		textureCoords = null;
		normals = null;
		indices = null;
		furthestPoint = 0;
		topModelVertex = 0;
	}

}
