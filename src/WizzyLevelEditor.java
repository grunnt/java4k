import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Wizzy Level Editor.
 */
public class WizzyLevelEditor extends AbstractGame {

  private static final int IMAGE_WIDTH = 16;
  private static final int IMAGE_HEIGHT = 256;
  private static final int BITS_PER_PIXEL = 4;
  private static final int PIXELS_PER_VALUE = 4;

  private static final int TILE_SIZE = 32;

  private static final int MAP_HEIGHT = 16;
  private static final int MAP_WIDTH = 16;

  private static final int ID_NONE = 0;
  private static final int ID_PLAYER = 1;
  private static final int ID_SPIKE = 6;
  private static final int ID_MANA = 7;
  private static final int ID_FLYMONSTER = 8;
  private static final int ID_MONSTER = 9;
  private static final int ID_DOOR = 10;
  private static final int ID_CANDLE = 11;
  private static final int ID_BRICK = 12;
  private static final int ID_WOOD = 13;
  private static final int ID_ROCK = 14;

  private static final int RED = 0;
  private static final int GREEN = 1;
  private static final int BLUE = 2;

  private final boolean[] keyDown = new boolean[255];
  private boolean mouseButton;
  private int mouseX;
  private int mouseY;

  int mapY = 0;
  int mapX = 0;

  private final int[][] map = new int[MAP_WIDTH][MAP_HEIGHT];

  // Graphics data
  private final int[][] palette = new int[][]{{119, 2, 47}, {188, 0, 221}, {255, 255, 255},
      {0, 0, 0},
      {192, 192, 192}, {128, 128, 128}, {96, 96, 96}, {0, 148, 255}, {0, 191, 35},
      {255, 106, 0}, {127, 51, 0}, {255, 224, 71}, {165, 66, 0}, {38, 38, 38}, {30, 30, 30},
      {45, 45, 45}};
  private final String spriteSheetString = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1111\u0000\u0000\u1000\u1011\u0001\u0000\u1100\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1111\u0111\u0000\u0000\u2220\"\u0000\u0000\u2320\u0023\u0000\u0000\u2220\"\u0000\u0000\u1110\u0011\u0000\u0000\u1111\u0111\u0000\u0000\u1111\u0111\u0000\u0000\u1111\u0111\u0000\u0000\u1330\u0033\u0000\u0000\u3333\u0033\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1111\u0001\u0000\u1000\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1111\u0111\u0000\u0000\u2220\"\u0000\u0000\u2320\u0023\u0000\u0000\u2220\"\u0000\u0000\u1110\u0011\u0000\u0000\u1111\u0111\u0000\u1000\u1111\u1111\u0000\u3000\u1111\u0331\u0000\u3000\u1133\u0331\u0000\u0000\u0033\u0033\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0111\u0000\u0000\u1000\u1111\u0000\u0000\u1100\u1011\u0000\u0000\u1100\u0011\u0000\u0000\u1111\u0111\u0000\u0000\u2220\"\u0000\u0000\u2320\u0023\u0000\u1100\u2220\u1022\u0001\u1000\u1111\u1111\u0000\u0000\u1111\u0111\u0000\u1000\u1111\u1111\u0000\u3000\u1111\u3111\u0000\u3000\u1133\u3331\u0000\u0000\u0033\u0330\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1111\u0000\u0000\u1000\u1101\u0001\u0000\u0000\u1100\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1110\u1111\u0000\u0000\u2200\u0222\u0000\u0000\u3200\u0232\u0000\u0000\u2200\u0222\u0000\u0000\u1100\u0111\u0000\u0000\u1110\u1111\u0000\u0000\u1110\u1111\u0000\u0000\u1110\u1111\u0000\u0000\u3300\u0331\u0000\u0000\u3300\u3333\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1100\u1111\u0000\u0000\u0000\u1100\u0001\u0000\u0000\u1100\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1110\u1111\u0000\u0000\u2200\u0222\u0000\u0000\u3200\u0232\u0000\u0000\u2200\u0222\u0000\u0000\u1100\u0111\u0000\u0000\u1110\u1111\u0000\u0000\u1111\u1111\u0001\u0000\u1330\u1111\u0003\u0000\u1330\u3311\u0003\u0000\u3300\u3300\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1110\u0000\u0000\u0000\u1111\u0001\u0000\u0000\u1101\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1110\u1111\u0000\u0000\u2200\u0222\u0000\u0000\u3200\u0232\u0000\u1000\u2201\u0222\u0011\u0000\u1111\u1111\u0001\u0000\u1110\u1111\u0000\u0000\u1111\u1111\u0001\u0000\u1113\u1111\u0003\u0000\u1333\u3311\u0003\u0000\u0330\u3300\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0020\u0200\u0000\u0000\u0040\u0400\u0000\u0000\u0040\u0400\u0000\u0200\u0050\u0550\u0000\u0400\u0550\u0050\u0020\u4400\u0500\u0060\u0044\u5000\u6600\u5066\u0004\u5000\u6005\u5006\u0000\u0000\u6005\u5506\u0000\u0042\u6066\u0666\u2440\u5440\u6660\u5566\u0045\u5000\u6666\u5666\u0000\u4444\u4444\u4444\u4444\u6664\u6666\u6666\u6666\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u7000\u0007\u0000\u0000\u2700\u0072\u0000\u0000\u2270\u0722\u0000\u0000\u7227\u7667\u0000\u0000\u7727\u7667\u0000\u0000\u6670\u0766\u0000\u0000\u6700\u0076\u0000\u0000\u7000\u0007\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0880\u0000\u0000\u0000\u0888\u0000\u0000\u8000\u0088\u0000\u0000\u8000\b\u0000\u0880\u8800\u0088\u0000\u8880\u2880\u0882\u0000\u8800\u3288\u8823\b\u8000\u3288\u8823\u0088\u0000\u2880\u0882\u0888\u0000\u8800\u0088\u0880\u0000\u8000\b\u0000\u0000\u8880\b\u0000\u8000\u0888\u0000\u0000\u8000\b\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u2000\u0002\u2000\u0002\u2200\u0023\u3200\"\u2000\u0082\u2800\u0002\u0000\u0880\u0880\u0000\u0000\u8880\u0888\u0000\u0000\u8880\u0888\u0000\u0000\u8888\u8888\u0000\u8000\u3238\u8323\b\u8800\u3238\u8323\u0088\u0800\u3238\u8323\u0080\u0000\u8888\u8888\u0000\u0000\u8880\u0888\u0000\u0000\u0000\u0000\u0000\u0000\u9990\u0999\u0000\u9000\uaa99\u99aa\n\u9900\u6aaa\uaaa6\u00a9\ua900\u666a\u6666\u00a9\ua990\u6665\u5666\u0aa9\uaa90\u6656\u4566\u0a94\u6a90\u6566\u4456\u0a94\u6a90\u3666\u4443\u0a94\u6a90\u3666\u4443\u0a94\u6a90\u3666\u4443\u0a94\u6a90\u4566\u4454\u0a94\u6a90\u4456\u4544\u0a94\u6a90\u4445\u5444\u0a94\u5a90\u4444\u4444\u0a95\u9990\u9999\u9999\u0999\u0000\u4000\u0065\u0000\u0000\u4000\u0065\u0000\u4000\u5555\u5555\u0065\u4000\u4069\u4069\u0069\u9300\u939b\u939b\u039b\u3000\u3039\u3039\u0039\u0000\u0003\u0003\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u4444\u4444\u4444\u4444\u4444\u6444\u4444\u6444\u5544\u6655\u5544\u6655\u4544\u6655\u4544\u6655\u5544\u6656\u5544\u6656\u5544\u6655\u5544\u6655\u6644\u6666\u6644\u6666\u6664\u6666\u6664\u6666\u4444\u4444\u4444\u4444\u4444\u6444\u4444\u6444\u5544\u6655\u5544\u6655\u4544\u6655\u4544\u6655\u5544\u6656\u5544\u6656\u5544\u6655\u5544\u6655\u6644\u6666\u6644\u6666\u6664\u6666\u6664\u6666\u9999\u9999\u9999\u9999\u9999\u9999\u9999\ua999\ucc99\ucccc\ucccc\uaacc\u9c99\u9999\u9999\uaac9\uac99\uaaaa\uaaaa\uaaca\ucc99\ucccc\ucccc\uaacc\u9c99\u9999\u9999\uaac9\uac99\uaaaa\uaaaa\uaaca\ucc99\ucccc\ucccc\uaacc\u9c99\u9999\u9999\uaac9\uac99\uaaaa\uaaaa\uaaca\ucc99\ucccc\ucccc\uaacc\u9c99\u9999\u9999\uaac9\ucc99\ucccc\ucccc\uaacc\uaa99\uaaaa\uaaaa\uaaaa\uaaa9\uaaaa\uaaaa\uaaaa\u4444\u4444\u4444\u4444\u4444\u4444\u4444\u6444\u5544\u5555\u5555\u6655\u4544\u4545\u4545\u6655\u5544\u5454\u5454\u6656\u4544\u4545\u6545\u6655\u5544\u5454\u5654\u6656\u4544\u4545\u6565\u6655\u5544\u5454\u5656\u6656\u4544\u6545\u6565\u6655\u5544\u5654\u5656\u6656\u4544\u6565\u6565\u6655\u5544\u5656\u5656\u6656\u5544\u5555\u5555\u6655\u6644\u6666\u6666\u6666\u6664\u6666\u6666\u6666\ueeed\ueeee\ueeee\udeee\ueede\ueeee\ueeee\ufdee\uedee\ueeee\ueeee\uffde\udeee\udddd\udddd\ufffd\udeee\udddd\udddd\ufffd\udeee\udddd\udddd\ufffd\udeee\udddd\udddd\ufffd\udeee\udddd\udddd\ufffd\udeee\udddd\udddd\ufffd\udeee\udddd\udddd\ufffd\udeee\udddd\udddd\ufffd\udeee\udddd\udddd\ufffd\udeee\udddd\udddd\ufffd\ufdee\uffff\uffff\uffdf\uffde\uffff\uffff\ufdff\ufffd\uffff\uffff\udfff";
  private BufferedImage spriteSheet;
  private LinearGradientPaint backPaint;

  public WizzyLevelEditor() {
    super("Wizzy Level Editor", 512, 512, false);
  }

  @Override
  protected void setup() {

    String levelString = "\ueeee\ueeee\ueeee\ueeee\u0b0e\u000b\u0001\ue000\u000e\uce00\ucccc\ue0ec\u000e\u0c0d\u0b0b\ue00b\u000e\u0c00\u0000\ue000\u660e\u0c66\u0d0e\ued0d\uce0e\u0ccc\u666c\ue666\u0c0e\u0c00\uccce\ueccc\u0e0e\u0c0e\u7777\uec07\u000e\uce0c\ucccc\uee0e\u000e\f\n\ue000\u000e\uccce\uccce\ue00e\u000e\u0b0b\u0b0c\ue00b\u000e\u0800\f\ue000\u700e\u0077\u990c\ue009\ueeee\ueeee\ueeee\ueeee";
    // Load existing level
    for (int y = 0; y < MAP_HEIGHT; y++) {
      for (int x = 0; x < 4; x++) {
        for (int v = 0; v < 4; v++) {
          int val = (levelString.charAt(y * 4 + x) >>> (v * 4)) & 0x0F;
          map[(x * 4) + v][y] = val;
        }
      }
    }

    // Convert string to sprite sheet
    spriteSheet = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    int inputPos = 0;
    for (int y = 0; y < IMAGE_HEIGHT; y++) {
      for (int x = 0; x < IMAGE_WIDTH; x++) {
        int stringPos = inputPos / PIXELS_PER_VALUE;
        int partPos = inputPos % PIXELS_PER_VALUE;
        int paletteIndex =
            (spriteSheetString.charAt(stringPos) >>> (partPos * BITS_PER_PIXEL)) & 0x0F;
        int val =
            0xff000000 | (palette[paletteIndex][RED] << 16) | (palette[paletteIndex][GREEN] << 8)
                | palette[paletteIndex][BLUE];
        // Transparency
        if (paletteIndex == 0) {
          val = 0;
        }
        spriteSheet.setRGB(x, y, val);
        inputPos++;
      }
    }

    backPaint = new LinearGradientPaint(0, 0, 0, getHeight(), new float[]{0f, 1f},
        new Color[]{Color.black, Color.black});
  }

  @Override
  protected void update(float stepS, float frameTimeS, float gameTimeS) {

    int mapX = mouseX / TILE_SIZE;
    int mapY = mouseY / TILE_SIZE;

    if (mouseButton) {
      mouseButton = false;
      map[mapX][mapY] = ID_NONE;
    }

    if (keyDown[49]) {
      map[mapX][mapY] = ID_ROCK;

    } else if (keyDown[50]) {
      map[mapX][mapY] = ID_BRICK;

    } else if (keyDown[51]) {
      map[mapX][mapY] = ID_WOOD;

    } else if (keyDown[52]) {
      map[mapX][mapY] = ID_CANDLE;

    } else if (keyDown[53]) {
      map[mapX][mapY] = ID_SPIKE;

    } else if (keyDown[54]) {
      map[mapX][mapY] = ID_DOOR;

    } else if (keyDown[55]) {
      map[mapX][mapY] = ID_MANA;

    } else if (keyDown[56]) {
      map[mapX][mapY] = ID_FLYMONSTER;

    } else if (keyDown[57]) {
      map[mapX][mapY] = ID_MONSTER;

    } else if (keyDown[48]) {
      map[mapX][mapY] = ID_PLAYER;
    }

    if (keyDown[115]) {
      keyDown[115] = false;
      // Output generated level string
      System.out.print("\"");
      for (int y = 0; y < MAP_HEIGHT; y++) {
        for (int x = 0; x < MAP_WIDTH / 4; x++) {

          // Combine values
          int result = 0;
          int startX = x * 4;
          for (int v = 0; v < 4; v++) {
            int val = map[startX + v][y];
            result = result | val << (v * 4);
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
      }
      System.out.println("\"");
    }
  }

  @Override
  protected void render(Graphics2D g, float frameTimeS, float gameTimeS) {
    // Clear background
    g.setPaint(backPaint);
    g.fillRect(0, 0, getWidth(), getHeight());

    // Draw blocks
    for (int x = 0; x < MAP_WIDTH; x++) {
      for (int y = 0; y < MAP_HEIGHT; y++) {
        if (map[x][y] != ID_NONE) {
          g.drawImage(spriteSheet, x * TILE_SIZE, y * TILE_SIZE, x * TILE_SIZE + TILE_SIZE,
              TILE_SIZE
                  * y + TILE_SIZE, 0, 16 * map[x][y], 16, 16 * (map[x][y] + 1), null);
        }
      }
    }

    // Draw square for editing
    g.setColor(Color.white);
    g.drawRect(mapX * TILE_SIZE, mapY * TILE_SIZE, 32, 32);
  }

  @Override
  public void keyPressed(KeyEvent e) {
    keyDown[e.getKeyCode()] = true;
  }

  @Override
  public void keyReleased(KeyEvent e) {
    keyDown[e.getKeyCode()] = false;
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    mouseButton = true;
  }

  public static void main(String[] args) {
    try {
      new WizzyLevelEditor().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
