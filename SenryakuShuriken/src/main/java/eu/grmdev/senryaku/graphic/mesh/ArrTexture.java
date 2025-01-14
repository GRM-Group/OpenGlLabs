package eu.grmdev.senryaku.graphic.mesh;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;

import java.nio.ByteBuffer;

import lombok.Getter;

public class ArrTexture {
	@Getter
	private final int[] ids;
	@Getter
	private final int width;
	@Getter
	private final int height;
	
	public ArrTexture(int numTextures, int width, int height, int pixelFormat) throws Exception {
		ids = new int[numTextures];
		glGenTextures(ids);
		this.width = width;
		this.height = height;
		
		for (int i = 0; i < numTextures; i++) {
			glBindTexture(GL_TEXTURE_2D, ids[i]);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, this.width, this.height, 0, pixelFormat, GL_FLOAT, (ByteBuffer) null);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		}
	}
	
	public void cleanup() {
		for (int id : ids) {
			glDeleteTextures(id);
		}
	}
}
