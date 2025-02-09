package eu.grmdev.senryaku.graphic.mesh;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import eu.grmdev.senryaku.Config;
import lombok.Getter;

public class FontTexture {
	private final Font font;
	private final String charSetName;
	private final Map<Character, CharInfo> charMap;
	@Getter
	private Texture texture;
	@Getter
	private int height;
	@Getter
	private int width;
	
	public FontTexture(Font font, String charSetName) throws Exception {
		this.font = font;
		this.charSetName = charSetName;
		charMap = new HashMap<>();
		buildTexture();
	}
	
	public CharInfo getCharInfo(char c) {
		return charMap.get(c);
	}
	
	private String getAllAvailableChars(String charsetName) {
		CharsetEncoder ce = Charset.forName(charsetName).newEncoder();
		StringBuilder result = new StringBuilder();
		for (char c = 0; c < Character.MAX_VALUE; c++) {
			if (ce.canEncode(c)) {
				result.append(c);
			}
		}
		return result.toString();
	}
	
	private void buildTexture() throws Exception {
		// Get font metrics for each character for selected font by using image
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = img.createGraphics();
		g2D.setFont(font);
		FontMetrics fontMetrics = g2D.getFontMetrics();
		
		String allChars = getAllAvailableChars(charSetName);
		this.width = 0;
		this.height = 0;
		for (char c : allChars.toCharArray()) {
			// Get the size for each character and update global image size
			CharInfo charInfo = new CharInfo(width, fontMetrics.charWidth(c));
			charMap.put(c, charInfo);
			width += charInfo.getWidth();
			height = Math.max(height, fontMetrics.getHeight());
		}
		g2D.dispose();
		
		// Create the image associated to the charset
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		g2D = img.createGraphics();
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setFont(font);
		fontMetrics = g2D.getFontMetrics();
		g2D.setColor(Color.WHITE);
		g2D.drawString(allChars, 0, fontMetrics.getAscent());
		g2D.dispose();
		
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			ImageIO.write(img, Config.IMAGE_FORMAT.<String> get(), out);
			out.flush();
			texture = new Texture(ByteBuffer.wrap(out.toByteArray()));
		}
	}
	
	public static class CharInfo {
		@Getter
		private final int startX;
		@Getter
		private final int width;
		
		public CharInfo(int startX, int width) {
			this.startX = startX;
			this.width = width;
		}
	}
}
