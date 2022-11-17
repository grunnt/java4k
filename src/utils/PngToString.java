package utils;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Simple tool for converting a PNG image to a String for use in rendering in Wizzy. Normally you
 * would load a PNG directly of course, but by embedding the image this way we save some space.
 */
public class PngToString {

	private static final int BITS_PER_PIXEL = 4;
	private static final int PIXELS_PER_VALUE = 4;
	private static final int PALETTE_SIZE = 16;

	private static final int RED = 0;
	private static final int GREEN = 1;
	private static final int BLUE = 2;

	private final short[][] palette = new short[PALETTE_SIZE][3];

	public void run(String fileName) throws Exception {
		BufferedImage img = ImageIO.read(new File(fileName));
		int[] rgb = new int[img.getWidth() * img.getHeight()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), rgb, 0, img.getWidth());
		int[] pixelData = new int[img.getWidth() * img.getHeight()];
		int paletteAddIndex = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				// Get color values of this pixel
				int index = y * img.getWidth() + x;
				short R = (short) ((rgb[index] >>> 16) & 0xff); // bitwise shifting
				short G = (short) ((rgb[index] >>> 8) & 0xff);
				short B = (short) (rgb[index] & 0xff);

				// Do we already have this color?
				int paletteIndex = -1;
				for (int p = 0; p < paletteAddIndex; p++) {
					if (palette[p][RED] == R && palette[p][GREEN] == G && palette[p][BLUE] == B) {
						// Existing color, use it
						paletteIndex = p;
						break;
					}
				}

				if (paletteIndex == -1) {
					// New color, add it
					palette[paletteAddIndex][RED] = R;
					palette[paletteAddIndex][GREEN] = G;
					palette[paletteAddIndex][BLUE] = B;
					paletteIndex = paletteAddIndex;
					paletteAddIndex++;
				}

				// Remember this pixel's palette index
				pixelData[index] = paletteIndex;
			}
		}

		System.out.print("int[][] palette = new int[][]{");
		boolean comma = false;
		for (int p = 0; p < PALETTE_SIZE; p++) {
			if (!comma) {
				comma = true;
			} else {
				System.out.print(",");
			}
			System.out.print("{");
			System.out.print(palette[p][RED]);
			System.out.print(",");
			System.out.print(palette[p][GREEN]);
			System.out.print(",");
			System.out.print(palette[p][BLUE]);
			System.out.print("}");
		}
		System.out.println("};");
		System.out.print("String spriteSheetString = \"");
		for (int p = 0; p < pixelData.length / PIXELS_PER_VALUE; p++) {

			// Combine values
			int result = 0;
			for (int v = 0; v < PIXELS_PER_VALUE; v++) {
				int val = pixelData[p * PIXELS_PER_VALUE + v];
				result = result | val << (v * BITS_PER_PIXEL);
			}

			switch (result) {
				case 0x0008 -> System.out.print("\\b");
				case 0x0009 -> System.out.print("\\t");
				case 0x000a -> System.out.print("\\n");
				case 0x000c -> System.out.print("\\f");
				case 0x000d -> System.out.print("\\r");
				case 0x0022 -> System.out.print("\\\"");
				case 0x0027 -> System.out.print("\\'");
				case 0x005c -> System.out.print("\\\\");
				default -> {
					StringBuilder s = new StringBuilder(Integer.toHexString(result));
					while (s.length() < 4) {
						s.insert(0, "0");
					}
					System.out.print("\\u" + s);
				}
			}
		}
		System.out.println("\";");
	}

	public static void main(String[] args) {
		try {
			PngToString converter = new PngToString();
			converter.run(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
