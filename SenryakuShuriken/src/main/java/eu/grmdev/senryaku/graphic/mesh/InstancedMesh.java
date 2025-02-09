package eu.grmdev.senryaku.graphic.mesh;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import eu.grmdev.senryaku.core.entity.Entity;
import eu.grmdev.senryaku.graphic.Transformation;

public class InstancedMesh extends Mesh {
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;
	private static final int MATRIX_SIZE_FLOATS = 4 * 4;
	private static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;
	private static final int INSTANCE_SIZE_BYTES = MATRIX_SIZE_BYTES + FLOAT_SIZE_BYTES * 2 + FLOAT_SIZE_BYTES;
	private static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS + 3;
	private final int numInstances;
	private final int instanceDataVBO;
	private FloatBuffer instanceDataBuffer;
	
	public InstancedMesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int numInstances) {
		super(positions, textCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0));
		
		this.numInstances = numInstances;
		
		glBindVertexArray(vaoId);
		
		instanceDataVBO = glGenBuffers();
		vboIdList.add(instanceDataVBO);
		instanceDataBuffer = MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
		glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
		int start = 5;
		int strideStart = 0;
		
		for (int i = 0; i < 4; i++) {
			glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
			glVertexAttribDivisor(start, 1);
			start++;
			strideStart += VECTOR4F_SIZE_BYTES;
		}
		
		glVertexAttribPointer(start, 2, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
		glVertexAttribDivisor(start, 1);
		strideStart += FLOAT_SIZE_BYTES * 2;
		start++;
		
		glVertexAttribPointer(start, 1, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
		glVertexAttribDivisor(start, 1);
		start++;
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	@Override
	protected void initRender() {
		super.initRender();
		int start = 5;
		int numElements = 4 * 2 + 2;
		for (int i = 0; i < numElements; i++) {
			glEnableVertexAttribArray(start + i);
		}
	}
	
	@Override
	protected void endRender() {
		int start = 5;
		int numElements = 4 * 2 + 2;
		for (int i = 0; i < numElements; i++) {
			glDisableVertexAttribArray(start + i);
		}
		super.endRender();
	}
	
	public void renderListInstanced(ConcurrentHashMap<Integer, Entity> entities, Transformation transformation, Matrix4f viewMatrix) {
		renderListInstanced(entities, false, transformation, viewMatrix);
	}
	
	public void renderListInstanced(ConcurrentHashMap<Integer, Entity> entities, boolean billBoard, Transformation transformation, Matrix4f viewMatrix) {
		initRender();
		
		int chunkSize = numInstances;
		int length = entities.size();
		for (int i = 0; i < length; i += chunkSize) {
			int end = Math.min(length, i + chunkSize);
			List<Entity> subList = new ArrayList<>();
			Iterator<Integer> it = entities.keySet().iterator();
			for (int j = 0; j < end; j++) {
				subList.add(entities.get(it.next()));
			}
			renderChunkInstanced(subList, billBoard, transformation, viewMatrix);
		}
		
		endRender();
	}
	
	private void renderChunkInstanced(List<Entity> entities, boolean billBoard, Transformation transformation, Matrix4f viewMatrix) {
		this.instanceDataBuffer.clear();
		int i = 0;
		Texture text = getMaterial().getTexture();
		for (Entity entity : entities) {
			Matrix4f modelMatrix = transformation.buildModelMatrix(entity);
			if (viewMatrix != null && billBoard) {
				viewMatrix.transpose3x3(modelMatrix);
			}
			modelMatrix.get(INSTANCE_SIZE_FLOATS * i, instanceDataBuffer);
			if (text != null) {
				int col = entity.getTextPos() % text.getNumCols();
				int row = entity.getTextPos() / text.getNumCols();
				float textXOffset = (float) col / text.getNumCols();
				float textYOffset = (float) row / text.getNumRows();
				int buffPos = INSTANCE_SIZE_FLOATS * i + MATRIX_SIZE_FLOATS;
				this.instanceDataBuffer.put(buffPos, textXOffset);
				this.instanceDataBuffer.put(buffPos + 1, textYOffset);
			}
			
			int buffPos = INSTANCE_SIZE_FLOATS * i + MATRIX_SIZE_FLOATS + 2;
			this.instanceDataBuffer.put(buffPos, entity.isSelected() ? 1 : 0);
			
			i++;
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
		glBufferData(GL_ARRAY_BUFFER, instanceDataBuffer, GL_DYNAMIC_READ);
		
		glDrawElementsInstanced(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, entities.size());
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void remove() {
		super.remove();
		if (this.instanceDataBuffer != null) {
			MemoryUtil.memFree(this.instanceDataBuffer);
			this.instanceDataBuffer = null;
		}
	}
}
