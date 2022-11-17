import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Wizzy's escape. A puzzle-platformer.
 */

// Entities with ID (corresponds to sprite sheet y position)
// Player 0
// Player 1
// Player 2
// Player 3
// Player 4
// Player 5
// Spike 6
// Mana 7
// Spell 8
// Monster1 9
// Monster2 10
// Monster3 11
// Door 12
// Rock 13
// Wood 14
// Water 15

//
// Wizzy didn't pay attention in the dark magic classes. Which is a pity, because one of his fellow students did. And he
// took over the University, turning all teachers and students into green slimy monsters! (while Wizzy was taking a
// nap) Not knowing any offensive spells, Wizzy has no chance of beating the Evil Guy. Not that he wants to anyway, he
// just wants to get out! The only spells Wizzy knows are gravity inversion spells, which he thought were rather neat.
// Help him use these spells get out alive!
//
// Instructions: reach the door out of each level room while avoiding the monsters and spikes. Move using W,A,D keys. Q
// = cast inverse gravity for self spell, E = cast inverse gravity for others spell. To cast a spell you one mana
// crystal, which you need to collect (the blue balls, collected crustals are shown in the bottom-right). As a wizard,
// Wizzy has three lives, shown on the bottom-left. Got stuck? Press R to restart the level, losing one life.
//

public class Wizzy extends AbstractGame {

  private static final int IMAGE_WIDTH = 16;
  private static final int IMAGE_HEIGHT = 256;
  private static final int BITS_PER_PIXEL = 4;
  private static final int PIXELS_PER_VALUE = 4;

  private static final int TILE_SIZE = 32;
  private static final int PLAYER_WIDTH = 24;
  private static final int PLAYER_HEIGHT = 28;

  private static final int MAP_HEIGHT = 16;
  private static final int MAP_WIDTH = 16;

  private static final int ID_NONE = 0;
  private static final int ID_PLAYER = 1;
  private static final int ID_SPIKE = 6;
  private static final int ID_MANA = 7;
  private static final int ID_FLYMONSTER = 8;
  private static final int ID_MONSTER = 9;
  private static final int ID_DOOR = 10;

  // Max 2 pixel movement per time step
  private static final float VELOCITY_X_MAX = 125;
  private static final float VELOCITY_Y_MAX = 350;

  private static final int ENTITY_COLLISION_RANGE = 250;

  private static final int RED = 0;
  private static final int GREEN = 1;
  private static final int BLUE = 2;

  private static final float MOVEMENT_X_FORCE = 450f;
  private static final float MOVEMENT_X_SLOWDOWN = 550f;
  private static final float MOVEMENT_X_FLYING_FACTOR = 0.35f;

  private static final float MONSTER_SPEED_X = 50f;
  private static final float FLYMONSTER_SPEED_X = 50f;
  private static final float FLYMONSTER_SPEED_Y = 30f;

  private static final float MOVEMENT_Y_FORCE = 280f;
  private static final float MOVEMENT_Y_GRAVITY = 900f;

  private static final float JUMP_COOLDOWN = 0.25f;
  private static final float SPELL_GRAV_SELF_DURATION = 1.5f;
  private static final float SPELL_GRAV_ALL_DURATION = 1.5f;

  private static final float ANIMATION_INTERVAL = 0.075f;

  private static final int MAX_NUMBER_OF_ENTITIES = 64;
  private static final int ENTITY_NUMBER_PLAYER = 0;
  private static final int MAX_NUMBER_OF_EFFECTS = 150;
  private static final float EFFECT_RANGE = 35;

  private static final int STATE_WELCOME = 0;
  private static final int STATE_NEWLEVEL = 1;
  private static final int STATE_GAME = 2;
  private static final int STATE_LEVELCOMPLETE = 3;
  private static final int STATE_GAMEWON = 4;

  private final boolean[] keyDown = new boolean[255];

  private final Random random = new Random();

  private final int[][] palette = new int[][]{{119, 2, 47}, {188, 0, 221}, {255, 255, 255},
      {0, 0, 0},
      {192, 192, 192}, {128, 128, 128}, {96, 96, 96}, {0, 148, 255}, {0, 191, 35},
      {255, 106, 0}, {127, 51, 0}, {255, 224, 71}, {165, 66, 0}, {38, 38, 38}, {30, 30, 30},
      {45, 45, 45}};
  private final static String spriteSheetString = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1111\u0000\u0000\u1000\u1011\u0001\u0000\u1100\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1111\u0111\u0000\u0000\u2220\"\u0000\u0000\u2320\u0023\u0000\u0000\u2220\"\u0000\u0000\u1110\u0011\u0000\u0000\u1111\u0111\u0000\u0000\u1111\u0111\u0000\u0000\u1111\u0111\u0000\u0000\u1330\u0033\u0000\u0000\u3333\u0033\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1111\u0001\u0000\u1000\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1111\u0111\u0000\u0000\u2220\"\u0000\u0000\u2320\u0023\u0000\u0000\u2220\"\u0000\u0000\u1110\u0011\u0000\u0000\u1111\u0111\u0000\u1000\u1111\u1111\u0000\u3000\u1111\u0331\u0000\u3000\u1133\u0331\u0000\u0000\u0033\u0033\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0111\u0000\u0000\u1000\u1111\u0000\u0000\u1100\u1011\u0000\u0000\u1100\u0011\u0000\u0000\u1111\u0111\u0000\u0000\u2220\"\u0000\u0000\u2320\u0023\u0000\u1100\u2220\u1022\u0001\u1000\u1111\u1111\u0000\u0000\u1111\u0111\u0000\u1000\u1111\u1111\u0000\u3000\u1111\u3111\u0000\u3000\u1133\u3331\u0000\u0000\u0033\u0330\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1111\u0000\u0000\u1000\u1101\u0001\u0000\u0000\u1100\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1110\u1111\u0000\u0000\u2200\u0222\u0000\u0000\u3200\u0232\u0000\u0000\u2200\u0222\u0000\u0000\u1100\u0111\u0000\u0000\u1110\u1111\u0000\u0000\u1110\u1111\u0000\u0000\u1110\u1111\u0000\u0000\u3300\u0331\u0000\u0000\u3300\u3333\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1100\u1111\u0000\u0000\u0000\u1100\u0001\u0000\u0000\u1100\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1110\u1111\u0000\u0000\u2200\u0222\u0000\u0000\u3200\u0232\u0000\u0000\u2200\u0222\u0000\u0000\u1100\u0111\u0000\u0000\u1110\u1111\u0000\u0000\u1111\u1111\u0001\u0000\u1330\u1111\u0003\u0000\u1330\u3311\u0003\u0000\u3300\u3300\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u1110\u0000\u0000\u0000\u1111\u0001\u0000\u0000\u1101\u0011\u0000\u0000\u1100\u0011\u0000\u0000\u1110\u1111\u0000\u0000\u2200\u0222\u0000\u0000\u3200\u0232\u0000\u1000\u2201\u0222\u0011\u0000\u1111\u1111\u0001\u0000\u1110\u1111\u0000\u0000\u1111\u1111\u0001\u0000\u1113\u1111\u0003\u0000\u1333\u3311\u0003\u0000\u0330\u3300\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0020\u0200\u0000\u0000\u0040\u0400\u0000\u0000\u0040\u0400\u0000\u0200\u0050\u0550\u0000\u0400\u0550\u0050\u0020\u4400\u0500\u0060\u0044\u5000\u6600\u5066\u0004\u5000\u6005\u5006\u0000\u0000\u6005\u5506\u0000\u0042\u6066\u0666\u2440\u5440\u6660\u5566\u0045\u5000\u6666\u5666\u0000\u4444\u4444\u4444\u4444\u6664\u6666\u6666\u6666\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u7000\u0007\u0000\u0000\u2700\u0072\u0000\u0000\u2270\u0722\u0000\u0000\u7227\u7667\u0000\u0000\u7727\u7667\u0000\u0000\u6670\u0766\u0000\u0000\u6700\u0076\u0000\u0000\u7000\u0007\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0880\u0000\u0000\u0000\u0888\u0000\u0000\u8000\u0088\u0000\u0000\u8000\b\u0000\u0880\u8800\u0088\u0000\u8880\u2880\u0882\u0000\u8800\u3288\u8823\b\u8000\u3288\u8823\u0088\u0000\u2880\u0882\u0888\u0000\u8800\u0088\u0880\u0000\u8000\b\u0000\u0000\u8880\b\u0000\u8000\u0888\u0000\u0000\u8000\b\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u2000\u0002\u2000\u0002\u2200\u0023\u3200\"\u2000\u0082\u2800\u0002\u0000\u0880\u0880\u0000\u0000\u8880\u0888\u0000\u0000\u8880\u0888\u0000\u0000\u8888\u8888\u0000\u8000\u3238\u8323\b\u8800\u3238\u8323\u0088\u0800\u3238\u8323\u0080\u0000\u8888\u8888\u0000\u0000\u8880\u0888\u0000\u0000\u0000\u0000\u0000\u0000\u9990\u0999\u0000\u9000\uaa99\u99aa\n\u9900\u6aaa\uaaa6\u00a9\ua900\u666a\u6666\u00a9\ua990\u6665\u5666\u0aa9\uaa90\u6656\u4566\u0a94\u6a90\u6566\u4456\u0a94\u6a90\u3666\u4443\u0a94\u6a90\u3666\u4443\u0a94\u6a90\u3666\u4443\u0a94\u6a90\u4566\u4454\u0a94\u6a90\u4456\u4544\u0a94\u6a90\u4445\u5444\u0a94\u5a90\u4444\u4444\u0a95\u9990\u9999\u9999\u0999\u0000\u4000\u0065\u0000\u0000\u4000\u0065\u0000\u4000\u5555\u5555\u0065\u4000\u4069\u4069\u0069\u9300\u939b\u939b\u039b\u3000\u3039\u3039\u0039\u0000\u0003\u0003\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u4444\u4444\u4444\u4444\u4444\u6444\u4444\u6444\u5544\u6655\u5544\u6655\u4544\u6655\u4544\u6655\u5544\u6656\u5544\u6656\u5544\u6655\u5544\u6655\u6644\u6666\u6644\u6666\u6664\u6666\u6664\u6666\u4444\u4444\u4444\u4444\u4444\u6444\u4444\u6444\u5544\u6655\u5544\u6655\u4544\u6655\u4544\u6655\u5544\u6656\u5544\u6656\u5544\u6655\u5544\u6655\u6644\u6666\u6644\u6666\u6664\u6666\u6664\u6666\u9999\u9999\u9999\u9999\u9999\u9999\u9999\ua999\ucc99\ucccc\ucccc\uaacc\u9c99\u9999\u9999\uaac9\uac99\uaaaa\uaaaa\uaaca\ucc99\ucccc\ucccc\uaacc\u9c99\u9999\u9999\uaac9\uac99\uaaaa\uaaaa\uaaca\ucc99\ucccc\ucccc\uaacc\u9c99\u9999\u9999\uaac9\uac99\uaaaa\uaaaa\uaaca\ucc99\ucccc\ucccc\uaacc\u9c99\u9999\u9999\uaac9\ucc99\ucccc\ucccc\uaacc\uaa99\uaaaa\uaaaa\uaaaa\uaaa9\uaaaa\uaaaa\uaaaa\u4444\u4444\u4444\u4444\u4444\u4444\u4444\u6444\u5544\u5555\u5555\u6655\u4544\u4545\u4545\u6655\u5544\u5454\u5454\u6656\u4544\u4545\u6545\u6655\u5544\u5454\u5654\u6656\u4544\u4545\u6565\u6655\u5544\u5454\u5656\u6656\u4544\u6545\u6565\u6655\u5544\u5654\u5656\u6656\u4544\u6565\u6565\u6655\u5544\u5656\u5656\u6656\u5544\u5555\u5555\u6655\u6644\u6666\u6666\u6666\u6664\u6666\u6666\u6666\ueeed\ueeee\ueeee\udeee\udede\udddd\udddd\ufded\uedee\ueeee\ueeee\uffde\udede\uddde\ueddd\ufdfd\ueede\ueeed\udeee\ufdff\udede\udede\ufded\ufdfd\udede\uedee\uffde\ufdfd\udede\udede\ufdfd\ufdfd\udede\udede\ufdfd\ufdfd\udede\ufdee\uffdf\ufdfd\udede\udfde\ufdfd\ufdfd\ueede\ufffd\udfff\ufdff\udede\udddf\ufddd\ufdfd\ufdee\uffff\uffff\uffdf\udfde\udddd\udddd\ufdfd\ufffd\uffff\uffff\udfff";

	private BufferedImage spriteSheet;
	private final Font bigFont = new Font("Impact", Font.PLAIN, 75);
	private final Font smallFont = new Font("Impact", Font.PLAIN, 12);

	private Clip spellSound;
	private Clip jumpSound;
	private Clip stepSound;

  // Moving Entities
	private final float[] entityX = new float[MAX_NUMBER_OF_ENTITIES];
	private final float[] entityY = new float[MAX_NUMBER_OF_ENTITIES];
	private final int[] entityType = new int[MAX_NUMBER_OF_ENTITIES];
	private final float[] entityVX = new float[MAX_NUMBER_OF_ENTITIES];
	private final float[] entityVY = new float[MAX_NUMBER_OF_ENTITIES];
	private final boolean[] entityGrounded = new boolean[MAX_NUMBER_OF_ENTITIES];
	private final float[] entityAntiGravTime = new float[MAX_NUMBER_OF_ENTITIES];

	private final float[] effectX = new float[MAX_NUMBER_OF_EFFECTS];
	private final float[] effectY = new float[MAX_NUMBER_OF_EFFECTS];
	private final float[] effectAngle = new float[MAX_NUMBER_OF_EFFECTS];
	private final float[] effectProgress = new float[MAX_NUMBER_OF_EFFECTS];

  // Player character
	private int walkAnimation = 0;
	private float walkAnimationDelay = 0;
	private float jumpCooldown = 0;
	private float spellJumpDuration = 0;
	private boolean playerDead = false;

  // Map data
	private final String[] levelDataString = {
      // Level 1: introduction
      "\ueeee\ueeee\ueeee\ueeee\u0b0e\u00b0\u0b00\ue0b0\u007e\u0000\u0000\ue000\u000e\u0000\u0000\ue000\u707e\u0000\u0000\ue000\u000e\u0070\u0000\ue000\u707e\uc000\f\ue000\u000e\uc070\ucccc\ue0cc\u707e\uc000\f\ue000\u000e\uc070\uee0c\ueeee\u700e\uc000\u0e0c\ue0bb\u000e\uc070\u0e0c\ue000\u00de\ucd00\u0e0c\ue00e\ud00e\uc000\u0e0c\ueeee\udd1e\ucd0d\u900c\uea00\ueeee\ueeee\ueeee\ueeee",
      "\ueeee\ueeee\ueeee\ueeee\ub00e\u0b00\u00b0\ue00b\u000e\u0000\u0000\ue000\u000e\u0000\u0000\uea00\ue00e\u0000\u0000\uecc0\ue00e\u0000\u0000\ue000\ue00e\u6666\u6666\ue666\ue00e\ueeee\ueeee\ueeee\ue07e\ue000\ue000\ue000\u07de\u0000\u0000\ue000\u0dde\u0000\u0000\ue000\uedde\ue090\ue090\ue000\ueeee\ueeee\ueeee\ue700\u000e\u00e0\ue000\ued00\ue01e\u9000\u0079\uedd0\ueeee\ueeee\ueeee\ueeee",
      "\ueeee\ueeee\ueeee\ueeee\u000e\u0c00\uc0cc\ue000\u771e\u900d\u0900\ue0d0\ueeee\ueeee\ueeee\ue0ee\u0e0e\u0000\u00e0\ue0e0\u000e\u0000\ue000\ue0e0\u0d0e\u0090\ue0d9\ue0e0\ucc0e\ucccc\ueccc\ue0e0\ub00e\ub0b0\ue0b0\ue0e0\u000e\u0000\ue000\ue0e0\u000e\u0000\uea00\ue0e0\u0c0e\u0c0c\uec0c\ue0e0\u000e\u0000\ue000\ue0e0\u000e\u0000\ue000\ue0e0\u666e\u6666\ue666\ue707\ueeee\ueeee\ueeee\ueeee",
      "\ueeee\ueeee\ueeee\ueeee\u0b0e\u000b\u0001\ue000\u000e\uce00\ucccc\ue0ec\u000e\u0c0d\u0b0b\ue00b\u000e\u0c00\u0000\ue000\u660e\u0c66\u0d0e\ued0d\uce0e\u0ccc\u666c\ue666\u0c0e\u0c00\uccce\ueccc\u0e0e\u0c0e\u7777\uee07\u000e\uce0c\ucccc\ue00e\u000e\f\n\ue000\u000e\uccce\uccce\ue00e\u000e\u0b0b\u0b0c\ue00b\u000e\u0800\f\ue000\u700e\u0077\u990c\ue009\ueeee\ueeee\ueeee\ueeee",
      "\ueeee\ueeee\ueeee\ueeee\u000e\u0000\u0000\ue000\uc0ae\u0000\u0000\ue080\ub0ce\u00c0\u0000\ue000\u00be\uc0b0\u0000\ue000\u000e\ub000\uc0c0\ue000\u000e\u0000\ub0b0\ue000\u000e\u0000\u0000\ue000\u000e\u0000\u0000\ue000\u000e\u0000\u0000\ue070\u000e\u0000\u0000\ue0c0\u001e\u0000\uc000\ue000\uc0ee\uc0c0\u00c0\ue000\u000e\u0000\u0000\ue000\u666e\u6666\u6666\ue666\ueeee\ueeee\ueeee\ueeee",
      "\ueeee\ueeee\ueeee\ueeee\u090e\u0900\u0b00\ue00b\u0c0e\u0c00\u000e\uea00\u000e\u0000\u0c0e\uec0c\u000e\u0000\u000e\ue080\u000e\u0700\u666e\ue666\u000e\udddd\udddd\ueddd\ud00e\ub00d\ub0b0\ue0b0\u90de\u0000\u0000\ue000\uddde\udddd\udddd\ue000\u000e\u0b00\u0b0b\ued00\u000e\u0001\u0000\uedd0\u080e\udddd\udddd\ueddd\u000e\u0000\u0000\ue000\u666e\u6666\u6666\ue666\ueeee\ueeee\ueeee\ueeee",
      "\ueeee\ueeee\ueeee\ueeee\u701e\uc007\u00b0\ue0b0\uccce\uc0ec\u0e00\ue00d\u000e\uc00b\u0c00\ued70\u0e0e\uc000\u0c0e\ue0d0\u0c0e\uc008\udc80\ue000\u0c0e\uc000\udc0a\ue00d\uce0e\ueccc\ucecc\ue0ec\ucc0e\ucccc\u0b0c\ue00b\ucc0e\ucccc\f\ue000\uce0e\ucecc\ud00c\ued00\ubc0e\ubcbc\u00dc\ue000\u8c0e\u8c8c\u0d0c\ue000\u0e0e\u0e0e\u000e\ue00d\u000e\u0000\ud000\ue66d\ueeee\ueeee\ueeee\ueeee"};
	private int level = 0;

  // Convert string to level setup
	private final int[][] map = new int[MAP_WIDTH][MAP_HEIGHT];

	private int mana = 0;

	private int state = STATE_WELCOME;

  // Collision detection stuff
	private final Rectangle rect1 = new Rectangle();
	private final Rectangle rect2 = new Rectangle();

  public Wizzy() {
    super("Wizzy's Escape", 512, 532, false);
  }

  @Override
  protected void setup() throws Exception {
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

    // Setup audio
    // Buffer for the audio sample
    byte[] stepSoundData = new byte[3000];
    byte[] jumpSoundData = new byte[3000];
    // Generate a simplistic beep
    float cycle = 0f;
    float cycleStep = 8f / 16000f;
    for (int i = 0; i < stepSoundData.length; i++) {
      stepSoundData[i] = (byte) ((cycle - 0.5f) * random.nextInt(16) * (1f
          - i / (float) stepSoundData.length));
      cycle = (cycle + cycleStep) % 1f;
      cycleStep += 0f;
    }
    for (int i = 0; i < jumpSoundData.length; i++) {
      jumpSoundData[i] = (byte) (Math.sin(6.28f * (i / (40f * (1f - i / 5000f)))) * 32f);
    }
    AudioFormat audioFormat = new AudioFormat(16000, 8, 1, true, true);
    AudioFormat audioFormatSlow = new AudioFormat(8000, 8, 1, true, true);
    spellSound = AudioSystem.getClip();
    jumpSound = AudioSystem.getClip();
    stepSound = AudioSystem.getClip();
    spellSound.open(audioFormat, jumpSoundData, 0, jumpSoundData.length);
    stepSound.open(audioFormat, stepSoundData, 0, stepSoundData.length);
    jumpSound.open(audioFormatSlow, stepSoundData, 0, stepSoundData.length);


  }

  @Override
  protected void update(float stepS, float frameTimeS, float gameTimeS) {

		int animationDirection = 0;
		if (state == STATE_WELCOME) {
      if (keyDown[KeyEvent.VK_SPACE]) {
        keyDown[KeyEvent.VK_SPACE] = false;

        level = 0;
        state = STATE_NEWLEVEL;
      }

    } else if (state == STATE_NEWLEVEL) {

      for (int e = 0; e < MAX_NUMBER_OF_ENTITIES; e++) {
        entityType[e] = ID_NONE;
      }

      // Setup level
      int entityCounter = 1;
      for (int y = 0; y < MAP_HEIGHT; y++) {
        for (int x = 0; x < 4; x++) {
          for (int v = 0; v < 4; v++) {
            int val = (levelDataString[level].charAt(y * 4 + x) >>> (v * 4)) & 0x0F;
            int ex = (x * 4 + v) * 32 + 16;
            int ey = y * 32 + 16;
            if (val >= 6 && val <= 11) {
              entityX[entityCounter] = ex;
              entityY[entityCounter] = ey;
              entityVX[entityCounter] = 0;
							if (val == ID_MONSTER) {
								entityVX[entityCounter] = MONSTER_SPEED_X;
							}
              entityVY[entityCounter] = 0;
              entityType[entityCounter] = val;
              val = ID_NONE;
              entityCounter++;
            } else if (val == ID_PLAYER) {
              entityType[ENTITY_NUMBER_PLAYER] = ID_PLAYER;
              entityVX[ENTITY_NUMBER_PLAYER] = 0;
              entityVY[ENTITY_NUMBER_PLAYER] = 0;
              entityX[ENTITY_NUMBER_PLAYER] = ex;
              entityY[ENTITY_NUMBER_PLAYER] = ey;
              val = ID_NONE;
            }

            entityAntiGravTime[entityCounter] = 0f;
            map[(x * 4) + v][y] = val;
          }
        }
      }

      walkAnimation = 0;
      walkAnimationDelay = 0;
      spellJumpDuration = 0;
      mana = 0;

      playerDead = false;

      state = STATE_GAME;

    } else if (state == STATE_GAME) {

      if (playerDead) {

        entityX[ENTITY_NUMBER_PLAYER] += 75 * stepS;
        entityVY[ENTITY_NUMBER_PLAYER] += MOVEMENT_Y_GRAVITY * stepS;
        entityY[ENTITY_NUMBER_PLAYER] += entityVY[ENTITY_NUMBER_PLAYER] * stepS;

        if (entityY[ENTITY_NUMBER_PLAYER] > getHeight()) {
          state = STATE_NEWLEVEL;
          playerDead = false;
        }

      } else {

        spellJumpDuration -= stepS;

        float speedFactor = MOVEMENT_X_FLYING_FACTOR;
				if (entityGrounded[ENTITY_NUMBER_PLAYER]) {
					speedFactor = 1f;
				}

        if (keyDown[KeyEvent.VK_RIGHT]) {

          // D (right)
          animationDirection = 1;

          entityVX[ENTITY_NUMBER_PLAYER] += MOVEMENT_X_FORCE * stepS * speedFactor;

        } else if (keyDown[KeyEvent.VK_LEFT]) {

          // A (left)
          animationDirection = -1;

          entityVX[ENTITY_NUMBER_PLAYER] -= MOVEMENT_X_FORCE * stepS * speedFactor;

        } else {


          if (entityGrounded[ENTITY_NUMBER_PLAYER]) {
            // Slow down horizontal movement when on the ground
            if (entityVX[ENTITY_NUMBER_PLAYER] > 0f) {
              entityVX[ENTITY_NUMBER_PLAYER] -= MOVEMENT_X_SLOWDOWN * stepS;
							if (entityVX[ENTITY_NUMBER_PLAYER] < 0f) {
								entityVX[ENTITY_NUMBER_PLAYER] = 0;
							}
            } else if (entityVX[ENTITY_NUMBER_PLAYER] < 0f) {
              entityVX[ENTITY_NUMBER_PLAYER] += MOVEMENT_X_SLOWDOWN * stepS;
							if (entityVX[ENTITY_NUMBER_PLAYER] > 0f) {
								entityVX[ENTITY_NUMBER_PLAYER] = 0;
							}
            }
          }
        }

        if (entityGrounded[ENTITY_NUMBER_PLAYER]) {
          if (animationDirection != 0) {
            walkAnimationDelay -= stepS;
            if (walkAnimationDelay <= 0) {

              // Play the sound
              stepSound.setFramePosition(0); // rewind to the beginning
              stepSound.start();

              if (animationDirection == -1) {
								if (walkAnimation == 0) {
									walkAnimation = 1;
								} else {
									walkAnimation = 0;
								}
              } else {
								if (walkAnimation == 3) {
									walkAnimation = 4;
								} else {
									walkAnimation = 3;
								}
              }
              walkAnimationDelay = ANIMATION_INTERVAL;
            }
          } else {
						if (walkAnimation == 1 || walkAnimation == 2) {
							walkAnimation = 0;
						}
						if (walkAnimation == 4 || walkAnimation == 5) {
							walkAnimation = 3;
						}
          }
        } else {
					if (entityVX[ENTITY_NUMBER_PLAYER] > 0) {
						walkAnimation = 5;
					} else {
						walkAnimation = 2;
					}
        }

        if (entityGrounded[ENTITY_NUMBER_PLAYER]) {
          jumpCooldown -= stepS;

          if (keyDown[KeyEvent.VK_UP]) {
            // W key (jump)
            if (jumpCooldown <= 0) {
              jumpSound.setFramePosition(0); // rewind to the beginning
              jumpSound.start();
							if (spellJumpDuration > 0f) {
								entityVY[ENTITY_NUMBER_PLAYER] = MOVEMENT_Y_FORCE;
							} else {
								entityVY[ENTITY_NUMBER_PLAYER] = -MOVEMENT_Y_FORCE;
							}
              jumpCooldown = JUMP_COOLDOWN;
            }
          }
        }

        if ((keyDown[KeyEvent.VK_Z] || keyDown[KeyEvent.VK_X]) && mana >= 1) {
          // A spell was cast
          spellSound.setFramePosition(0); // rewind to the beginning
          spellSound.start();
          if (keyDown[KeyEvent.VK_Z]) {
            keyDown[KeyEvent.VK_Z] = false;
            spellJumpDuration = SPELL_GRAV_SELF_DURATION;
          } else if (keyDown[KeyEvent.VK_X]) {
            keyDown[KeyEvent.VK_X] = false;
            // Inverse gravity
            for (int oe = 0; oe < MAX_NUMBER_OF_ENTITIES; oe++) {
              if (oe != ENTITY_NUMBER_PLAYER && entityType[oe] > 6 && entityType[oe] < 10) {
                entityAntiGravTime[oe] = SPELL_GRAV_ALL_DURATION;
              }
            }
          }
          mana--;

          // Some effects
          int effectCount = 25;
          for (int ef = 0; ef < MAX_NUMBER_OF_EFFECTS && effectCount > 0; ef++) {
            if (effectProgress[ef] > 1f) {
              effectProgress[ef] = 0f;
              effectAngle[ef] = random.nextFloat() * 6.28f;
              effectX[ef] = entityX[ENTITY_NUMBER_PLAYER];
              effectY[ef] = entityY[ENTITY_NUMBER_PLAYER];
              effectCount--;
            }
          }
        }

        // Update effects
        for (int e = 0; e < MAX_NUMBER_OF_EFFECTS; e++) {
          if (effectProgress[e] < 1f) {
            effectProgress[e] += stepS * 4f;
          }
        }

        // Update entities
        for (int e = 0; e < MAX_NUMBER_OF_ENTITIES; e++) {
          if (entityType[e] != ID_NONE) {
            entityAntiGravTime[e] -= stepS;

            // Attraction to playah for monsters
            if (entityType[e] == ID_FLYMONSTER) {
							if (entityY[ENTITY_NUMBER_PLAYER] < entityY[e] || (entityAntiGravTime[e] > 0)) {
								entityVY[e] = -FLYMONSTER_SPEED_Y;
							} else {
								entityVY[e] = FLYMONSTER_SPEED_Y;
							}
							if (entityX[ENTITY_NUMBER_PLAYER] < entityX[e]) {
								entityVX[e] = -FLYMONSTER_SPEED_X;
							} else {
								entityVX[e] = FLYMONSTER_SPEED_X;
							}
            } else if (entityAntiGravTime[e] > 0
								|| (e == ENTITY_NUMBER_PLAYER && spellJumpDuration > 0f))
						// Inverse gravity
						{
							entityVY[e] -= MOVEMENT_Y_GRAVITY * stepS;
						} else {
							// Gravity
							if (entityType[e] < 10) {
								entityVY[e] += MOVEMENT_Y_GRAVITY * stepS;
							}
						}

            // Limit velocities
						if (entityVX[e] > VELOCITY_X_MAX) {
							entityVX[e] = VELOCITY_X_MAX;
						}
						if (entityVX[e] < -VELOCITY_X_MAX) {
							entityVX[e] = -VELOCITY_X_MAX;
						}
						if (entityVY[e] > VELOCITY_Y_MAX) {
							entityVY[e] = VELOCITY_Y_MAX;
						}
						if (entityVY[e] < -VELOCITY_Y_MAX) {
							entityVY[e] = -VELOCITY_Y_MAX;
						}

            // Update position
            for (int dir = 0; dir < 2; dir++) {

              // Do updating and collision detection one direction at a time
              if (dir == 0) {
                entityX[e] += entityVX[e] * stepS;
              } else {
                entityY[e] += entityVY[e] * stepS;
              }

              // Is there a collision with a map block?
              for (int xd = -1; xd <= 1; xd++) {
                for (int yd = -1; yd <= 1; yd++) {
                  int mapX = (int) (entityX[e] / TILE_SIZE) + xd;
                  int mapY = (int) (entityY[e] / TILE_SIZE) + yd;
                  // No collision detection outside of map
									if (mapX >= MAP_WIDTH || mapY >= MAP_HEIGHT) {
										break;
									}
                  // Is there anything at this tile location?
                  if (map[mapX][mapY] != ID_NONE) {
                    // Does the entity collide with this tile?
                    rect1.setBounds(mapX * TILE_SIZE, mapY * TILE_SIZE, TILE_SIZE,
                        TILE_SIZE);
                    rect2.setBounds((int) entityX[e] - 12, (int) entityY[e] - 13,
                        PLAYER_WIDTH, PLAYER_HEIGHT);
                    Rectangle2D.intersect(rect1, rect2, rect2);
                    if (!rect2.isEmpty()) {
                      // A collision with this tile happened
                      if (dir == 0) {
												if (entityVX[e] > 0) {
													entityX[e] -= rect2.width;
												} else {
													entityX[e] += rect2.width;
												}
												if (entityType[e] == ID_MONSTER) {
													entityVX[e] = -entityVX[e];
												} else {
													entityVX[e] = 0;
												}
                      } else {
												if (entityVY[e] > 0) {
													entityY[e] -= rect2.height;
												} else {
													entityY[e] += rect2.height;
												}
                        entityVY[e] = 0;
                      }
                    }
                  }
                }
              }
            }

            // Is this entity resting on the ground?
            int mapX = (int) (entityX[e] / TILE_SIZE);
            int mapY = (int) (entityY[e] / TILE_SIZE);
            if (mapX >= 0 && mapX < MAP_WIDTH) {
              if (spellJumpDuration > 0f && mapY > 0) {
                entityGrounded[e] = map[mapX][mapY - 1] != ID_NONE;
              } else if (mapY < (MAP_HEIGHT - 1)) {
                entityGrounded[e] = map[mapX][mapY + 1] != ID_NONE;
              }
            }

            // Collisions between entities
            for (int oe = 0; oe < MAX_NUMBER_OF_ENTITIES; oe++) {
              float dx = entityX[oe] - entityX[e];
              float dy = entityY[oe] - entityY[e];
              float distanceSq = dx * dx + dy * dy;
              if (distanceSq < ENTITY_COLLISION_RANGE) {
                if (entityType[e] == ID_PLAYER) {
                  if (entityType[oe] == ID_MANA) {
                    entityType[oe] = ID_NONE;
                    mana++;
                  } else if (entityType[oe] == ID_SPIKE || entityType[oe] == ID_FLYMONSTER
                      || entityType[oe] == ID_MONSTER) {
                    // Die!
                    playerDead = true;
                    jumpSound.setFramePosition(0); // rewind to the beginning
                    jumpSound.start();
                    entityVY[ENTITY_NUMBER_PLAYER] = -200;
                  } else if (entityType[oe] == ID_DOOR) {
                    // Reached a door, level complete!
                    state = STATE_LEVELCOMPLETE;
                  }
                }
              }
            }
          }
        }
      }

      if (keyDown[KeyEvent.VK_R]) {
        keyDown[KeyEvent.VK_R] = false;
        state = STATE_NEWLEVEL;
      }

    } else if (state == STATE_LEVELCOMPLETE) {

      if (keyDown[KeyEvent.VK_SPACE]) {
        keyDown[KeyEvent.VK_SPACE] = false;
        level++;
        if (level >= levelDataString.length) {
          state = STATE_GAMEWON;
        } else {
          state = STATE_NEWLEVEL;
        }
      }

    } else if (state == STATE_GAMEWON) {

      if (keyDown[KeyEvent.VK_SPACE]) {
        keyDown[KeyEvent.VK_SPACE] = false;
        state = STATE_WELCOME;
      }
    }
  }

  @Override
  protected void render(Graphics2D g, float frameTimeS, float gameTimeS) {
    // Clear background
    g.drawImage(spriteSheet, -20, -10, 532, 532, 0, 240, 16, 256, null);
    g.setColor(new Color(1f, 1f, 1f));

    if (state == STATE_WELCOME) {

      g.setFont(bigFont);
      g.drawString("Wizzy4K", 140, 100);
      g.setFont(smallFont);

      g.drawImage(spriteSheet, 100, 100, 412, 412, 0, 80, 16, 96, null);

    } else if (state == STATE_GAME) {

      g.drawString("Room " + (level + 1), 10, 526);

      // Draw blocks
      for (int x = 0; x < MAP_WIDTH; x++) {
        for (int y = 0; y < MAP_HEIGHT; y++) {
					if (map[x][y] != 0) {
						g.drawImage(spriteSheet, x * TILE_SIZE, y * TILE_SIZE, x * TILE_SIZE + TILE_SIZE,
								TILE_SIZE * y + TILE_SIZE, 0, 16 * map[x][y], 16, 16 * (map[x][y] + 1), null);
					}
        }
      }

      // Draw entities (but not player)
      for (int e = 1; e < MAX_NUMBER_OF_ENTITIES; e++) {
        if (entityType[e] != ID_NONE) {
          int sheetIndex = entityType[e];
          g.drawImage(spriteSheet, (int) entityX[e] - 16, (int) entityY[e] - 16,
              (int) entityX[e] + 16, (int) entityY[e] + 16, 0, 16 * sheetIndex, 16,
              16 * (sheetIndex + 1), null);
        }
      }

      // Draw mana
      for (int m = 0; m < mana; m++) {
        g.drawImage(spriteSheet, 490 - 12 * m, 515, 506 - 12 * m, 531, 0, 112, 16, 128, null);
      }

      // Draw player sprite
      int yD1 = -16, yD2 = 16;
      if (spellJumpDuration > 0) {
        yD1 = 16;
        yD2 = -16;
      }
      g.drawImage(spriteSheet, (int) entityX[ENTITY_NUMBER_PLAYER] - 16,
          (int) entityY[ENTITY_NUMBER_PLAYER] + yD1, (int) entityX[ENTITY_NUMBER_PLAYER] + 16,
          (int) entityY[ENTITY_NUMBER_PLAYER] + yD2, 0, 16 * walkAnimation, 16,
          16 * (walkAnimation + 1), null);

      // Draw effects
      for (int e = 0; e < MAX_NUMBER_OF_EFFECTS; e++) {
        if (effectProgress[e] < 1f) {
          int halfSize = (int) (16f * (1f - effectProgress[e]));
          int x = (int) (effectX[e] + Math.cos(effectAngle[e]) * EFFECT_RANGE * effectProgress[e]);
          int y = (int) (effectY[e] + Math.sin(effectAngle[e]) * EFFECT_RANGE * effectProgress[e]);
          g.drawImage(spriteSheet, x - halfSize, y - halfSize, x + halfSize, y + halfSize, 0, 112,
              16, 128, null);
        }
      }

    } else if (state == STATE_LEVELCOMPLETE) {

      g.setFont(bigFont);
      g.drawString("Room done!", 75, 250);
      g.setFont(smallFont);

    } else if (state == STATE_GAMEWON) {

      g.setFont(bigFont);
      g.drawString("Escape!", 135, 250);
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    keyDown[e.getKeyCode()] = true;
  }

  @Override
  public void keyReleased(KeyEvent e) {
    keyDown[e.getKeyCode()] = false;
  }

  public static void main(String[] args) {
    try {
      new Wizzy().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
