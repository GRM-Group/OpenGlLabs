package eu.grmdev.senryaku.core.loaders.obj;

import static org.lwjgl.assimp.Assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import eu.grmdev.senryaku.core.misc.Utils;
import eu.grmdev.senryaku.graphic.mesh.*;

public class StaticMeshesLoader {
	public static final int DEF_FLAGS = aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_FixInfacingNormals;
	
	public static Mesh[] load(String resourcePath, String texturesDir) throws Exception {
		return load(resourcePath, texturesDir, DEF_FLAGS);
	}
	
	public static Mesh[] load(String resourcePath, String texturesDir, int flags) throws Exception {
		AIScene aiScene = Utils.loadAssimpObject(resourcePath, flags);
		return load(texturesDir, aiScene);
	}
	
	public static Mesh[] load(String texturesDir, AIScene aiScene) throws Exception {
		int numMaterials = aiScene.mNumMaterials();
		PointerBuffer aiMaterials = aiScene.mMaterials();
		List<Material> materials = new ArrayList<>();
		for (int i = 0; i < numMaterials; i++) {
			AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
			processMaterial(aiMaterial, materials, texturesDir);
		}
		
		int numMeshes = aiScene.mNumMeshes();
		PointerBuffer aiMeshes = aiScene.mMeshes();
		Mesh[] meshes = new Mesh[numMeshes];
		for (int i = 0; i < numMeshes; i++) {
			AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			Mesh mesh = processMesh(aiMesh, materials);
			meshes[i] = mesh;
		}
		
		return meshes;
	}
	
	private static void processMaterial(AIMaterial aiMaterial, List<Material> materials, String texturesDir) throws Exception {
		AIColor4D colour = AIColor4D.create();
		AIString path = AIString.calloc();
		Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
		String textPath = path.dataString();
		Texture texture = null;
		if (textPath != null && textPath.length() > 0) {
			TextureCache textCache = TextureCache.getInstance();
			texture = textCache.getTexture(texturesDir + "/" + textPath);
		}
		
		Vector4f ambient = Material.DEFAULT_COLOR;
		int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, colour);
		if (result == 0) {
			ambient = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
		}
		
		Vector4f diffuse = Material.DEFAULT_COLOR;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, colour);
		if (result == 0) {
			diffuse = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
		}
		
		Vector4f specular = Material.DEFAULT_COLOR;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, colour);
		if (result == 0) {
			specular = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
		}
		
		Material material = new Material(ambient, diffuse, specular, 1.0f);
		material.setTexture(texture);
		materials.add(material);
	}
	
	private static Mesh processMesh(AIMesh aiMesh, List<Material> materials) {
		List<Float> vertices = new ArrayList<>();
		List<Float> textures = new ArrayList<>();
		List<Float> normals = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		
		processVertices(aiMesh, vertices);
		processNormals(aiMesh, normals);
		processTextCoords(aiMesh, textures);
		processIndices(aiMesh, indices);
		
		Mesh mesh = new Mesh(Utils.listToArray(vertices), Utils.listToArray(textures), Utils.listToArray(normals), Utils.listIntToArray(indices));
		Material material;
		int materialIdx = aiMesh.mMaterialIndex();
		if (materialIdx >= 0 && materialIdx < materials.size()) {
			material = materials.get(materialIdx);
		} else {
			material = new Material();
		}
		mesh.setMaterial(material);
		return mesh;
	}
	
	private static void processVertices(AIMesh aiMesh, List<Float> vertices) {
		AIVector3D.Buffer aiVertices = aiMesh.mVertices();
		while (aiVertices.remaining() > 0) {
			AIVector3D aiVertex = aiVertices.get();
			vertices.add(aiVertex.x());
			vertices.add(aiVertex.y());
			vertices.add(aiVertex.z());
		}
	}
	
	private static void processNormals(AIMesh aiMesh, List<Float> normals) {
		AIVector3D.Buffer aiNormals = aiMesh.mNormals();
		while (aiNormals != null && aiNormals.remaining() > 0) {
			AIVector3D aiNormal = aiNormals.get();
			normals.add(aiNormal.x());
			normals.add(aiNormal.y());
			normals.add(aiNormal.z());
		}
	}
	
	private static void processTextCoords(AIMesh aiMesh, List<Float> textures) {
		AIVector3D.Buffer textCoords = aiMesh.mTextureCoords(0);
		int numTextCoords = textCoords != null ? textCoords.remaining() : 0;
		for (int i = 0; i < numTextCoords; i++) {
			AIVector3D textCoord = textCoords.get();
			textures.add(textCoord.x());
			textures.add(1 - textCoord.y());
		}
	}
	
	private static void processIndices(AIMesh aiMesh, List<Integer> indices) {
		int numFaces = aiMesh.mNumFaces();
		AIFace.Buffer aiFaces = aiMesh.mFaces();
		for (int i = 0; i < numFaces; i++) {
			AIFace aiFace = aiFaces.get(i);
			IntBuffer buffer = aiFace.mIndices();
			while (buffer.remaining() > 0) {
				indices.add(buffer.get());
			}
		}
	}
	
	public static Mesh loadMesh(String fileName, int instances) throws Exception {
		if (instances < 1) { throw new Exception("Instances should be greater than 1"); }
		
		List<String> lines = Utils.readAllLines(fileName);
		List<Vector3f> vertices = new ArrayList<>();
		List<Vector2f> textures = new ArrayList<>();
		List<Vector3f> normals = new ArrayList<>();
		List<Face> faces = new ArrayList<>();
		
		for (String line : lines) {
			String[] tokens = line.split("\\s+");
			switch (tokens[0]) {
				case "v" :
					// Geometric vertex
					Vector3f vec3f = new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
					vertices.add(vec3f);
					break;
				case "vt" :
					// Texture coordinate
					Vector2f vec2f = new Vector2f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
					textures.add(vec2f);
					break;
				case "vn" :
					// Vertex normal
					Vector3f vec3fNorm = new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
					normals.add(vec3fNorm);
					break;
				case "f" :
					Face face = new Face(tokens[1], tokens[2], tokens[3]);
					faces.add(face);
					break;
				default :
					// Ignore other lines
					break;
			}
		}
		return reorderLists(vertices, textures, normals, faces, instances);
	}
	
	private static Mesh reorderLists(List<Vector3f> posList, List<Vector2f> textCoordList, List<Vector3f> normList, List<Face> facesList, int instances) {
		List<Integer> indices = new ArrayList<>();
		// Create position array in the order it has been declared
		float[] posArr = new float[posList.size() * 3];
		int i = 0;
		for (Vector3f pos : posList) {
			posArr[i * 3] = pos.x;
			posArr[i * 3 + 1] = pos.y;
			posArr[i * 3 + 2] = pos.z;
			i++;
		}
		float[] textCoordArr = new float[posList.size() * 2];
		float[] normArr = new float[posList.size() * 3];
		
		for (Face face : facesList) {
			IdxGroup[] faceVertexIndices = face.getFaceVertexIndices();
			for (IdxGroup indValue : faceVertexIndices) {
				processFaceVertex(indValue, textCoordList, normList, indices, textCoordArr, normArr);
			}
		}
		int[] indicesArr = Utils.listIntToArray(indices);
		Mesh mesh;
		mesh = new InstancedMesh(posArr, textCoordArr, normArr, indicesArr, instances);
		return mesh;
	}
	
	private static void processFaceVertex(IdxGroup indices, List<Vector2f> textCoordList, List<Vector3f> normList, List<Integer> indicesList, float[] texCoordArr, float[] normArr) {
		int posIndex = indices.idxPos;
		indicesList.add(posIndex);
		
		if (indices.idxTextCoord >= 0) {
			Vector2f textCoord = textCoordList.get(indices.idxTextCoord);
			texCoordArr[posIndex * 2] = textCoord.x;
			texCoordArr[posIndex * 2 + 1] = 1 - textCoord.y;
		}
		if (indices.idxVecNormal >= 0) {
			Vector3f vecNorm = normList.get(indices.idxVecNormal);
			normArr[posIndex * 3] = vecNorm.x;
			normArr[posIndex * 3 + 1] = vecNorm.y;
			normArr[posIndex * 3 + 2] = vecNorm.z;
		}
	}
	
	protected static class Face {
		
		/**
		 * List of idxGroup groups for a face triangle (3 vertices per face).
		 */
		private IdxGroup[] idxGroups = new IdxGroup[3];
		
		public Face(String v1, String v2, String v3) {
			idxGroups = new IdxGroup[3];
			idxGroups[0] = parseLine(v1);
			idxGroups[1] = parseLine(v2);
			idxGroups[2] = parseLine(v3);
		}
		
		private IdxGroup parseLine(String line) {
			IdxGroup idxGroup = new IdxGroup();
			String[] lineTokens = line.split("/");
			int length = lineTokens.length;
			idxGroup.idxPos = Integer.parseInt(lineTokens[0]) - 1;
			if (length > 1) {
				String textCoord = lineTokens[1];
				idxGroup.idxTextCoord = textCoord.length() > 0 ? Integer.parseInt(textCoord) - 1 : IdxGroup.NO_VALUE;
				if (length > 2) {
					idxGroup.idxVecNormal = Integer.parseInt(lineTokens[2]) - 1;
				}
			}
			return idxGroup;
		}
		
		public IdxGroup[] getFaceVertexIndices() {
			return idxGroups;
		}
	}
	
	protected static class IdxGroup {
		public static final int NO_VALUE = -1;
		public int idxPos;
		public int idxTextCoord;
		public int idxVecNormal;
		
		public IdxGroup() {
			idxPos = NO_VALUE;
			idxTextCoord = NO_VALUE;
			idxVecNormal = NO_VALUE;
		}
	}
}
