package eu.grmdev.senryaku.core.entity;

import java.util.ArrayList;
import java.util.List;

import eu.grmdev.senryaku.core.IGame;
import eu.grmdev.senryaku.core.misc.Utils;
import eu.grmdev.senryaku.graphic.mesh.*;
import lombok.Getter;

public class TextItem extends Entity {
	private static final float ZPOS = 0.0f;
	private static final int VERTICES_PER_QUAD = 4;
	private final FontTexture fontTexture;
	private @Getter String text;
	
	public TextItem(String text, FontTexture fontTexture, IGame game) throws Exception {
		super(game);
		this.text = text;
		this.fontTexture = fontTexture;
		setMesh(buildMesh());
	}
	
	private Mesh buildMesh() {
		List<Float> positions = new ArrayList<>();
		List<Float> textCoords = new ArrayList<>();
		float[] normals = new float[0];
		List<Integer> indices = new ArrayList<>();
		char[] characters = text.toCharArray();
		int numChars = characters.length;
		
		float startx = 0;
		for (int i = 0; i < numChars; i++) {
			FontTexture.CharInfo charInfo = fontTexture.getCharInfo(characters[i]);
			
			// Left Top vertex
			positions.add(startx); // x
			positions.add(0.0f); // y
			positions.add(ZPOS); // z
			textCoords.add((float) charInfo.getStartX() / (float) fontTexture.getWidth());
			textCoords.add(0.0f);
			indices.add(i * VERTICES_PER_QUAD);
			
			// Left Bottom vertex
			positions.add(startx); // x
			positions.add((float) fontTexture.getHeight()); // y
			positions.add(ZPOS); // z
			textCoords.add((float) charInfo.getStartX() / (float) fontTexture.getWidth());
			textCoords.add(1.0f);
			indices.add(i * VERTICES_PER_QUAD + 1);
			
			// Right Bottom vertex
			positions.add(startx + charInfo.getWidth()); // x
			positions.add((float) fontTexture.getHeight()); // y
			positions.add(ZPOS); // z
			textCoords.add((float) (charInfo.getStartX() + charInfo.getWidth()) / (float) fontTexture.getWidth());
			textCoords.add(1.0f);
			indices.add(i * VERTICES_PER_QUAD + 2);
			
			// Right Top vertex
			positions.add(startx + charInfo.getWidth()); // x
			positions.add(0.0f); // y
			positions.add(ZPOS); // z
			textCoords.add((float) (charInfo.getStartX() + charInfo.getWidth()) / (float) fontTexture.getWidth());
			textCoords.add(0.0f);
			indices.add(i * VERTICES_PER_QUAD + 3);
			
			// Add indices for left top and bottom right vertices
			indices.add(i * VERTICES_PER_QUAD);
			indices.add(i * VERTICES_PER_QUAD + 2);
			
			startx += charInfo.getWidth();
		}
		
		float[] posArr = Utils.listToArray(positions);
		float[] textCoordsArr = Utils.listToArray(textCoords);
		int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();
		Mesh mesh = new Mesh(posArr, textCoordsArr, normals, indicesArr);
		mesh.setMaterial(new Material(fontTexture.getTexture()));
		return mesh;
	}
	
	public void setText(String text) {
		this.text = text;
		this.getMesh().deleteBuffers();
		this.setMesh(buildMesh());
	}
}