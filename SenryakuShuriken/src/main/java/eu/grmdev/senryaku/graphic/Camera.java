package eu.grmdev.senryaku.graphic;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import lombok.Getter;
import lombok.Setter;

public class Camera {
	private @Getter final Vector3f position;
	private @Getter final Vector3f offset;
	private @Getter final Vector3f rotation;
	private @Getter Matrix4f viewMatrix;
	private @Getter @Setter Window window;
	
	public Camera() {
		this(new Vector3f(), new Vector3f());
		viewMatrix = new Matrix4f();
	}
	
	public Camera(Vector3f position, Vector3f rotation) {
		this.position = position;
		this.rotation = rotation;
		offset = new Vector3f();
	}
	
	public Matrix4f updateViewMatrix() {
		Matrix4f vm = Transformation.updateGenericViewMatrix(position, offset, rotation, viewMatrix);
		return vm;
	}
	
	public void movePosition(float offsetX, float offsetY, float offsetZ) {
		if (offsetZ != 0) {
			position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
			position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
		}
		if (offsetX != 0) {
			position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
			position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
		}
		position.y += offsetY;
	}
	
	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}
	
	public void movePosition(Vector3f pos) {
		movePosition(pos.x, pos.y, pos.z);
	}
	
	public void moveRotation(float offsetX, float offsetY, float offsetZ) {
		rotation.x += offsetX;
		rotation.y += offsetY;
		rotation.z += offsetZ;
	}
	
	public void setRotation(float x, float y, float z) {
		rotation.x = x;
		rotation.y = y;
		rotation.z = z;
	}
	
	public Vector3f getOffsetPosition() {
		Vector3f v = new Vector3f();
		position.add(offset, v);
		return v;
	}
}