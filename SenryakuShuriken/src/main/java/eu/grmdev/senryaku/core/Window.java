package eu.grmdev.senryaku.core;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import eu.grmdev.senryaku.Main.Config;
import lombok.Getter;
import lombok.Setter;

public class Window {
	private @Getter final String title;
	private int width;
	private int height;
	private @Getter long windowHandle;
	private boolean resized;
	private @Getter @Setter boolean vSync;
	private @Getter final WindowOptions windowOptions;
	private @Getter final Matrix4f projectionMatrix;
	
	public Window(String title, boolean vSync, WindowOptions opts) {
		this.title = title;
		this.width = opts.width;
		this.height = opts.height;
		this.vSync = vSync;
		this.resized = false;
		this.windowOptions = opts;
		this.projectionMatrix = new Matrix4f();
	}
	
	public void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		
		if (!glfwInit()) { throw new IllegalStateException("Unable to initialize GLFW"); }
		createWindow();
		setupCallbacks();
		if (!windowOptions.maximized) {
			centerWindow();
		}
		
		glfwMakeContextCurrent(windowHandle);
		
		if (isVSync()) {
			glfwSwapInterval(1);
		}
		
		GL.createCapabilities();
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_STENCIL_TEST);
		if (windowOptions.showTriangles) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		}
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		if (windowOptions.cullFace) {
			glEnable(GL_CULL_FACE);
			glCullFace(GL_BACK);
		}
		glfwShowWindow(windowHandle);
	}
	
	/**
	 * Centers window on monitor
	 */
	private void centerWindow() {
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(windowHandle, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
	}
	
	/**
	 * Adds callbacks to FramebufferSize and Key events
	 */
	private void setupCallbacks() {
		glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
			this.width = width;
			this.height = height;
			this.setResized(true);
		});
		
		glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(window, true);
			}
		});
	}
	
	private void createWindow() {
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		if (windowOptions.compatibleProfile) {
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
		} else {
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		}
		if (windowOptions.antialiasing) {
			glfwWindowHint(GLFW_SAMPLES, 4);
		}
		
		if (width == 0 || height == 0) {
			width = 100;
			height = 100;
		}
		int max;
		if (windowOptions.maximized) {
			max = GLFW_TRUE;
		} else {
			max = GLFW_FALSE;
		}
		glfwWindowHint(GLFW_MAXIMIZED, max);
		
		windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
		if (windowHandle == NULL) { throw new RuntimeException("Failed to create the GLFW window"); }
	}
	
	public void restoreState() {
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_STENCIL_TEST);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		if (windowOptions.cullFace) {
			glEnable(GL_CULL_FACE);
			glCullFace(GL_BACK);
		}
	}
	
	public void setWindowTitle(String title) {
		glfwSetWindowTitle(windowHandle, title);
	}
	
	public Matrix4f updateProjectionMatrix() {
		float aspectRatio = (float) width / (float) height;
		return projectionMatrix.setPerspective(Config.FOV, aspectRatio, Config.Z_NEAR, Config.Z_FAR);
	}
	
	public static Matrix4f updateProjectionMatrix(Matrix4f matrix, int width, int height) {
		float aspectRatio = (float) width / (float) height;
		return matrix.setPerspective(Config.FOV, aspectRatio, Config.Z_NEAR, Config.Z_FAR);
	}
	
	public void setClearColor(float r, float g, float b, float alpha) {
		glClearColor(r, g, b, alpha);
	}
	
	public boolean isKeyPressed(int keyCode) {
		return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
	}
	
	public boolean windowShouldClose() {
		return glfwWindowShouldClose(windowHandle);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public boolean isResized() {
		return resized;
	}
	
	public void setResized(boolean resized) {
		this.resized = resized;
	}
	
	public void update() {
		glfwSwapBuffers(windowHandle);
		glfwPollEvents();
	}
	
	public static class WindowOptions {
		public int width;
		public int height;
		public boolean cullFace;
		public boolean showTriangles;
		public boolean showFps;
		public boolean compatibleProfile;
		public boolean antialiasing;
		public boolean frustumCulling;
		public boolean maximized;
	}
}