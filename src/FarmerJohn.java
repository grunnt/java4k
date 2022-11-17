import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Java4K game - Farmer John & the birds.
 */
public class FarmerJohn extends AbstractGame {

	private static final int MAX_BIRDS = 100;

	private static final int DISPLAY_WIDTH = 800;
	private static final int DISPLAY_HEIGHT = 600;
	private static final int DISPLAY_CENTER_X = 400;

	private static final int STATE_MENU = 0;
	private static final int STATE_GAME = 1;
	private static final int STATE_GAMEOVER = 2;
	private static final int SUBSTATE_LEVELSTART = 0;
	private static final int SUBSTATE_GAME = 1;
	private static final int SUBSTATE_LEVELEND = 2;

	private static final int BIRDTYPE_NONE = -1;
	private static final int BIRDTYPE_NORMAL = 0;
	private static final int BIRDTYPE_STRONG = 1;
	private static final int BIRDTYPE_FAST = 2;
	private static final int BIRDTYPE_SCARY = 3;

	private static final float BIRD_ALIGNMENT_STRENGTH = 0.01f;
	private static final float BIRD_REPULSION_STRENGTH = 1f;
	private static final float BIRD_REPULSION_RANGE_SQ = 6000f;
	private static final float BIRD_ATTRACTION_STRENGTH = 0.01f;
	private static final float BIRD_CENTER_ATTRACTION_STRENGTH = 0.025f;
	private static final float BIRD_FLAP_SPEED = 3f;
	private static final float BIRD_TARGET_AVOID_RANGE_SQ = 4000f;

	private static final byte COUNTDOWN_DURATION = 10;

	private static final int CANNON_MAX_AMMO = 2;
	private static final float CANNON_RELOAD_TIME = 0.8f;
	private static final float CANNON_RANGE_SQ = 400f;
	private static final float CANNON_BEAM_TIME = 0.2f;

	private static final float EXPLOSION_SPEED = 25f;
	private static final float EFFECT_SPEED = 2f;

	private static final int BONUS_NONE = -1;
	private static final int BONUS_AMMO = 0;
	private static final int BONUS_RELOAD = 1;
	private static final float BONUS_CHANCE = 0.2f;
	private static final float BONUS_FALL_SPEED = 150f;

	private static final float DELAY_TIME = 2f;

	private boolean mouseButtonDown;
	private int mouseX;
	private int mouseY;

	private final Random random = new Random();

	private final LinearGradientPaint blueSky = new LinearGradientPaint(0, 0, 0, DISPLAY_HEIGHT, new float[] { 0.3f, 0.6f,
			0.7f, 0.92f, 0.95f, 0.96f, 1f }, new Color[] { new Color(14, 42, 110), new Color(98, 110, 179),
			new Color(218, 129, 94), new Color(239, 104, 0), new Color(32, 64, 32), new Color(64, 200, 64),
			new Color(32, 64, 32) });

	private final LinearGradientPaint titleChrome2 = new LinearGradientPaint(0, 30, 0, 280,
			new float[] { 0.15f, 0.5f, 0.95f }, new Color[] { Color.black, Color.orange, Color.black });
	private final LinearGradientPaint titleChrome = new LinearGradientPaint(0, 350, 0, 615,
			new float[] { 0.2f, 0.5f, 0.95f }, new Color[] { Color.black, Color.red, Color.black });
	private final LinearGradientPaint yellowChrome2 = new LinearGradientPaint(0, 0, 0, 210,
			new float[] { 0.2f, 0.5f, 0.95f }, new Color[] { Color.black, Color.yellow, Color.black });
	private final LinearGradientPaint bulletChrome = new LinearGradientPaint(6, 0, 33, 0, new float[] { 0.1f, 0.5f, 0.9f },
			new Color[] { Color.black, Color.white, Color.black });

	private final Color darkGreen = new Color(0f, 0.4f, 0f);

	private final Stroke thickStroke = new BasicStroke(32f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
	private final Stroke normalStroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
	private final Stroke thinStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);

	private final int[] triangleX = { 0, 150, 300 };
	private final int[] triangleY = { 200, 0, 200 };

	private final Font bigFont = new Font("Impact", Font.PLAIN, 120);
	private final Font smallFont = new Font("Impact", Font.PLAIN, 32);
	private final Font voiceFont = new Font("Impact", Font.PLAIN, 16);

	private final String[] voices = { "Stay off my land!", "Nasty buggers!", "Don't eat my corn!", "Shoo!", "Go away!" };

	private Clip gunSound;
	private Clip hitSound;
	private Clip bonusSound;

	private final float[] birdX = new float[MAX_BIRDS];
	private final float[] birdY = new float[MAX_BIRDS];
	private final float[] birdVX = new float[MAX_BIRDS];
	private final float[] birdVY = new float[MAX_BIRDS];
	private final float[] birdFlap = new float[MAX_BIRDS];
	private final int[] birdType = new int[MAX_BIRDS];
	private final boolean[] birdExploded = new boolean[MAX_BIRDS];
	private final float[] birdExplode = new float[MAX_BIRDS];

	private final float[] effectX = new float[MAX_BIRDS];
	private final float[] effectY = new float[MAX_BIRDS];
	private final float[] effectVX = new float[MAX_BIRDS];
	float[] effectVY = new float[MAX_BIRDS];
	private final float[] effectProgress = new float[MAX_BIRDS];
	private final Color[] effectColor = new Color[MAX_BIRDS];

	private final int[] beamX = new int[MAX_BIRDS];
	private final int[] beamY = new int[MAX_BIRDS];
	private final float[] beamTime = new float[MAX_BIRDS];

	private final float[] bonusX = new float[MAX_BIRDS];
	private final float[] bonusY = new float[MAX_BIRDS];
	private final float[] bonusType = new float[MAX_BIRDS];

	private final float[] voiceProgress = new float[MAX_BIRDS];
	private final int[] voiceX = new int[MAX_BIRDS];
	private final int[] voiceText = new int[MAX_BIRDS];

	private int gameState = STATE_MENU;
	private int subState = SUBSTATE_LEVELSTART;
	private int highScore = 0;
	private int score = 0;

	private float countDown = 0;
	private int level = 0;
	private float countStart = 1;
	private float avoidStrength = 0;
	private float birdSpeed = 0;
	private int maxCannonAmmo = CANNON_MAX_AMMO;
	private int cannonAmmo = CANNON_MAX_AMMO;
	private float cannonReloadTime = CANNON_RELOAD_TIME;
	private float currentCannonReloadTime = CANNON_RELOAD_TIME;
	private float delayTimer = DELAY_TIME;

	private float farmerHop = 0f;
	private final Random random2 = new Random();

	public FarmerJohn() {
		super("Farmer John & The Birds", 800, 600, true);
	}

	@Override
	protected void setup() throws Exception {

		// Set transparent cursor
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		// Buffer for the audio sample
		byte[] gunSoundData = new byte[3000];
		// Generate a simplistic beep
		float cycle = 0f;
		float cycleStep = 8f / 16000f;
		for (int i = 0; i < gunSoundData.length; i++) {
			gunSoundData[i] = (byte) ((cycle - 0.5f) * random.nextInt(250) * (1f - i / (float) gunSoundData.length));
			cycle = (cycle + cycleStep) % 1f;
			cycleStep += 0f;
		}

		// Buffer for the audio sample
		byte[] hitSoundData = new byte[4000];
		// Generate a simplistic beep
		cycle = 0f;
		cycleStep = 500f / 16000f;
		for (int i = 0; i < hitSoundData.length; i++) {
			hitSoundData[i] = (byte) (Math.sin(cycle * 6.28f) * (64f * (1f - i / (float) hitSoundData.length)));
			cycle = (cycle + cycleStep) % 1f;
			cycleStep += 0.0006f;
		}

		AudioFormat audioFormat8 = new AudioFormat(8000, 8, 1, true, true);
		AudioFormat audioFormat16 = new AudioFormat(16000, 8, 1, true, true);
		gunSound = AudioSystem.getClip();
		gunSound.open(audioFormat8, gunSoundData, 0, gunSoundData.length);
		hitSound = AudioSystem.getClip();
		hitSound.open(audioFormat16, hitSoundData, 0, hitSoundData.length);
		bonusSound = AudioSystem.getClip();
		bonusSound.open(audioFormat8, hitSoundData, 0, hitSoundData.length);


	}

	@Override
	protected void update(float stepS, float frameTimeS, float gameTimeS) {
		if (gameState == STATE_MENU) {

			if (mouseButtonDown) {
				delayTimer = DELAY_TIME;
				countDown = 0;
				level = 0;
				score = 0;
				gameState = STATE_GAME;
				subState = SUBSTATE_LEVELSTART;
				maxCannonAmmo = CANNON_MAX_AMMO;
				cannonAmmo = CANNON_MAX_AMMO;
				cannonReloadTime = CANNON_RELOAD_TIME;

				for (int b = 0; b < MAX_BIRDS; b++) {
					birdType[b] = BIRDTYPE_NONE;
					effectProgress[b] = -1;
					beamTime[b] = 0;
					bonusType[b] = BONUS_NONE;
					voiceProgress[b] = 2f;
				}

				mouseButtonDown = false;
			}

		} else if (gameState == STATE_GAME) {

			cannonReloadTime -= stepS;
			if (cannonReloadTime <= 0f) {
				if (cannonAmmo < maxCannonAmmo) {
					cannonAmmo++;
				}
				cannonReloadTime = currentCannonReloadTime;
			}

			// ****** Update ******
			boolean allDead = true;

			if (mouseButtonDown && cannonAmmo >= 1) {

				// Bird or bonus hit?
				int birdHit = -1;
				int bonusHit = -1;
				float dist = CANNON_RANGE_SQ;
				for (int b = 0; b < MAX_BIRDS; b++) {
					if (birdType[b] != BIRDTYPE_NONE && !birdExploded[b]) {
						float dx = mouseX - birdX[b];
						float dy = mouseY - birdY[b];
						float distanceSq = dx * dx + dy * dy;
						if (distanceSq < dist) {
							birdHit = b;
							dist = distanceSq;
						}
					}
					if (bonusType[b] != BONUS_NONE) {
						float dx = mouseX - bonusX[b];
						float dy = mouseY - bonusY[b];
						float distanceSq = dx * dx + dy * dy;
						if (distanceSq < 225) {
							bonusHit = b;
						}
					}
				}

				// Add some effects
				int count = 15;
				for (int i = 0; i < MAX_BIRDS && count > 0; i++) {
					if (effectProgress[i] == -1) {
						effectX[i] = mouseX;
						effectY[i] = mouseY;
						effectVX[i] = -40 + random.nextFloat() * 80;
						effectVY[i] = -40 + random.nextFloat() * 80;
						effectProgress[i] = 0.0001f;
						float sev = random.nextFloat() * 0.7f + 0.3f;
						float eRed = sev / 4, eGreen = sev / 4, eBlue = sev / 4;
						if (bonusHit != -1) {
							eRed = sev;
							eGreen = 0f;
							eBlue = sev;
							if (bonusType[bonusHit] == BONUS_AMMO) {
								eGreen = sev;
								eBlue = 0f;
							}
						} else if (birdHit != -1) {
							eRed = sev;
						}
						effectColor[i] = new Color(eRed, eGreen, eBlue);
						count--;
					}
				}

				// Play the pew
				gunSound.setFramePosition(0); // rewind to the beginning
				gunSound.start();

				// Add a voice
				if (random.nextFloat() < 0.15f) {
					for (int i = 0; i < MAX_BIRDS; i++) {
						if (voiceProgress[i] >= 1f) {
							voiceProgress[i] = 0;
							voiceX[i] = random.nextInt(100);
							voiceText[i] = random.nextInt(5);
							break;
						}
					}
				}

				if (birdHit != -1) {

					if (birdType[birdHit] == BIRDTYPE_STRONG) {
						birdType[birdHit] = BIRDTYPE_NORMAL;
					} else {

						birdExploded[birdHit] = true;
						birdExplode[birdHit] = 0f;
						score++;

						// Bonus?
						if (random.nextFloat() < BONUS_CHANCE) {
							for (int i = 0; i < MAX_BIRDS; i++) {
								if (bonusType[i] == BONUS_NONE) {
									bonusType[i] = random.nextInt(2);
									bonusX[i] = birdX[birdHit];
									bonusY[i] = birdY[birdHit];
									break;
								}
							}
						}

						// Play the boom
						hitSound.setFramePosition(0); // rewind to the beginning
						hitSound.start();
					}
				}

				// Bonus hit?
				if (bonusHit != -1) {

					if (bonusType[bonusHit] == BONUS_AMMO) {
						maxCannonAmmo++;
					} else if (bonusType[bonusHit] == BONUS_RELOAD) {
						currentCannonReloadTime *= 0.9f;
					}

					bonusType[bonusHit] = BONUS_NONE;

					// Play the boom
					bonusSound.setFramePosition(0); // rewind to the beginning
					bonusSound.start();
				}

				// Beam!
				int iStart = random.nextInt(MAX_BIRDS);
				for (int i = 0; i < MAX_BIRDS; i++) {
					int i2 = (i + iStart) % MAX_BIRDS;
					if (beamTime[i2] <= 0) {
						beamX[i2] = mouseX;
						beamY[i2] = mouseY;
						beamTime[i2] = CANNON_BEAM_TIME;
						break;
					}
				}

				farmerHop = 15;

				cannonAmmo--;
			}
			mouseButtonDown = false;

			farmerHop -= stepS * 50;
			if (farmerHop < 0)
				farmerHop = 0;

			for (int b = 0; b < MAX_BIRDS; b++) {

				voiceProgress[b] += stepS / 5f;

				beamTime[b] -= stepS;
				if (bonusType[b] != BONUS_NONE) {
					bonusY[b] += BONUS_FALL_SPEED * stepS;
					if (bonusY[b] > DISPLAY_HEIGHT)
						bonusType[b] = BONUS_NONE;
				}

				if (birdType[b] != BIRDTYPE_NONE && !birdExploded[b]) {

					allDead = false;

					float forceX = 0f, forceY = 0f, bCount = 0f;
					float velocityX = 0f, velocityY = 0f;
					for (int ob = 0; ob < MAX_BIRDS; ob++) {
						if (birdType[ob] != BIRDTYPE_NONE && !birdExploded[ob]) {
							forceX += (birdX[ob] - birdX[b]) * BIRD_ATTRACTION_STRENGTH;
							forceY += (birdY[ob] - birdY[b]) * BIRD_ATTRACTION_STRENGTH;
							velocityX += birdVX[ob];
							velocityY += birdVY[ob];

							if (birdType[b] != BIRDTYPE_SCARY) {
								float dx = birdX[ob] - birdX[b];
								float dy = birdY[ob] - birdY[b];
								float distanceSq = dx * dx + dy * dy;

								float repulse = BIRD_REPULSION_STRENGTH;
								float repulseRange = BIRD_REPULSION_RANGE_SQ;

								if (birdType[ob] == BIRDTYPE_SCARY) {
									repulse *= 3;
									repulseRange *= 10;
								}

								if (distanceSq < repulseRange) {
									forceX += (birdX[b] - birdX[ob]) * repulse;
									forceY += (birdY[b] - birdY[ob]) * repulse;
								}
							}

							bCount++;
						}
					}

					int centerY = (int) (100 + 300 * (1f - countDown / countStart));

					birdVX[b] += forceX / bCount + (velocityX / bCount) * BIRD_ALIGNMENT_STRENGTH
							+ (DISPLAY_CENTER_X - birdX[b]) * BIRD_CENTER_ATTRACTION_STRENGTH;

					birdVY[b] += forceY / bCount + (velocityY / bCount) * BIRD_ALIGNMENT_STRENGTH
							+ (centerY - birdY[b]) * BIRD_CENTER_ATTRACTION_STRENGTH;

					float dx = mouseX - birdX[b];
					float dy = mouseY - birdY[b];
					float distanceSq = dx * dx + dy * dy;

					float useAvoidStrength = avoidStrength;
					float speedMultiplier = 1f;

					if (birdType[b] == BIRDTYPE_FAST) {
						useAvoidStrength *= 1.25f;
						speedMultiplier *= 2f;
					}

					if (distanceSq < BIRD_TARGET_AVOID_RANGE_SQ) {
						birdVX[b] += (birdX[b] - mouseX) * useAvoidStrength;
						birdVY[b] += (birdY[b] - mouseY) * useAvoidStrength;
					}

					if (birdVX[b] > birdSpeed * speedMultiplier)
						birdVX[b] = birdSpeed * speedMultiplier;
					if (birdVY[b] > birdSpeed * speedMultiplier)
						birdVY[b] = birdSpeed * speedMultiplier;

					if (birdVX[b] < -birdSpeed * speedMultiplier)
						birdVX[b] = -birdSpeed * speedMultiplier;
					if (birdVY[b] < -birdSpeed * speedMultiplier)
						birdVY[b] = -birdSpeed * speedMultiplier;

					birdX[b] += birdVX[b] * stepS;
					birdY[b] += birdVY[b] * stepS;

					dx = mouseX - birdX[b];
					dy = mouseY - birdY[b];
					distanceSq = dx * dx + dy * dy;

					float flapDelta = BIRD_FLAP_SPEED * stepS;
					if (distanceSq < BIRD_TARGET_AVOID_RANGE_SQ) {
						flapDelta *= 2;
					}

					birdFlap[b] = (birdFlap[b] + flapDelta) % 1f;

				}
				birdExplode[b] += EXPLOSION_SPEED * stepS;
			}

			if (subState == SUBSTATE_LEVELSTART) {
				delayTimer -= stepS;

				if (delayTimer <= 0) {
					int nBirds = 1 + (int) ((float) level * 1.5f);
					int strongBirds = nBirds / 4;
					int fastBirds = nBirds / 8;
					int scaryBirds = nBirds / 16;
					int normalBirds = nBirds - strongBirds - fastBirds - scaryBirds;

					for (int b = 0; b < MAX_BIRDS; b++) {

						birdX[b] = random.nextInt(DISPLAY_WIDTH);
						birdY[b] = random.nextInt(30) - 50;
						birdVX[b] = random.nextFloat() * 10f;
						birdVY[b] = random.nextFloat() * 10f;
						birdExploded[b] = false;
						birdFlap[b] = random.nextFloat();
						if (strongBirds > 0) {
							strongBirds--;
							birdType[b] = BIRDTYPE_STRONG;
						} else if (fastBirds > 0) {
							fastBirds--;
							birdType[b] = BIRDTYPE_FAST;
						} else if (scaryBirds > 0) {
							scaryBirds--;
							birdType[b] = BIRDTYPE_SCARY;
						} else if (normalBirds > 0) {
							normalBirds--;
							birdType[b] = BIRDTYPE_NORMAL;
						} else {
							birdType[b] = BIRDTYPE_NONE;
						}

						effectProgress[b] = -1;
						beamTime[b] = 0;
						bonusType[b] = BONUS_NONE;
					}

					countStart = COUNTDOWN_DURATION + level * 1.85f;
					countDown = countStart;

					avoidStrength = 0.8f + level * 0.06f;
					birdSpeed = 45f + level * 3f;

					subState = SUBSTATE_GAME;
					mouseButtonDown = false;
				}
			} else if (subState == SUBSTATE_GAME) {

				countDown -= stepS;
				if (countDown < 0f)
					countDown = 0f;

				if (allDead) {
					// Victory!
					subState = SUBSTATE_LEVELEND;
					delayTimer = DELAY_TIME;
				} else if (countDown <= 0f) {
					gameState = STATE_GAMEOVER;
					delayTimer = DELAY_TIME * 4f;
				}

			} else if (subState == SUBSTATE_LEVELEND) {

				delayTimer -= stepS;

				if (delayTimer <= 0) {
					subState = SUBSTATE_LEVELSTART;
					level++;
					delayTimer = DELAY_TIME;
				}
			}
		} else if (gameState == STATE_GAMEOVER) {

			delayTimer -= stepS;

			if (delayTimer <= 0f) {
				if (score > highScore)
					highScore = score;
				gameState = STATE_MENU;
				mouseButtonDown = false;
			}
		}
	}

	@Override
	protected void render(Graphics2D g, float frameTimeS, float gameTimeS) {
		g.setPaint(blueSky);
		g.fillRect(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

		// Draw some corn
		random2.setSeed(123);
		g.setStroke(normalStroke);
		for (int c = 0; c < 28; c++) {
			g.setColor(darkGreen);
			int x = c * 30 - random2.nextInt(10);
			int y = 530 - random2.nextInt(10);
			g.drawLine(x, y, c * 30, 600);
			g.fillArc(x, 550, 30, 70, 20, 30);
			g.fillArc(x - 25, 530, 25, 70, 190, 25);
			g.setColor(Color.yellow);
			g.fillOval(x - 2, y - 5, 10, 27 + random2.nextInt(5));
			g.setColor(Color.orange);
			for (int p = 0; p < 10; p++) {
				g.fillRect(x + random2.nextInt(4), y + random2.nextInt(20), 2, 2);
			}
		}

		if (gameState == STATE_MENU) {

			g.rotate(0.1f);
			g.setFont(bigFont);
			g.setPaint(titleChrome2);
			g.drawString("Farmer John", 30, 200);
			g.rotate(-0.2f);
			g.setPaint(titleChrome);
			g.drawString("& the Birds", 200, 540);
			g.rotate(0.1f);

			g.setFont(smallFont);
			g.setColor(Color.white);
			g.drawString("Click to play!", 300, 300);
			g.drawString("Highscore: " + highScore, 300, 350);

		} else if (gameState == STATE_GAME) {

			// Draw level and score
			g.setFont(smallFont);
			g.setColor(Color.white);
			g.drawString("Level: " + level, 10, 35);
			g.drawString("Score: " + score, 600, 35);

			// Draw countdown
			if (subState == STATE_GAME) {
				g.setFont(bigFont);
				g.setColor(new Color(1f, 0.1f, 0.1f, 1f - countDown / countStart));
				g.drawString((int) countDown + "", 300, 350);
			}

			// Draw bonuses
			for (int b = 0; b < MAX_BIRDS; b++) {
				if (bonusType[b] != BONUS_NONE) {
					g.setColor(Color.magenta);
					if (bonusType[b] == BONUS_AMMO)
						g.setColor(Color.orange);
					int bX = (int) bonusX[b], bY = (int) bonusY[b];
					g.fillOval(bX - 10, bY - 10, 20, 20);

					RadialGradientPaint bonusBorder = new RadialGradientPaint(bX - 5, bY - 5, 15, new float[] {
							0.1f, 1f }, new Color[] { new Color(255, 255, 255, 255),
							new Color(255, 255, 255, 0) });
					g.setPaint(bonusBorder);
					g.fillOval(bX - 10, bY - 15, 20, 30);
				}
			}

			// Draw birds
			for (int b = 0; b < MAX_BIRDS; b++) {

				if (birdType[b] != BIRDTYPE_NONE) {
					int bX = (int) birdX[b], bY = (int) birdY[b];
					if (birdExploded[b]) {
						float x = -birdExplode[b];
						bX += 5 * x;
						bY += x * x + x;
					}

					// Draw body
					if (birdType[b] == BIRDTYPE_NORMAL) {
						g.setColor(Color.gray);
					} else if (birdType[b] == BIRDTYPE_SCARY) {
						g.setColor(Color.red);
					} else if (birdType[b] == BIRDTYPE_FAST) {
						g.setColor(Color.yellow);
					} else if (birdType[b] == BIRDTYPE_STRONG) {
						g.setColor(Color.magenta);
					}

					g.setStroke(normalStroke);
					float flapY = (int) (Math.sin(birdFlap[b] * 6.28) * 15f);
					g.drawLine(bX, bY, bX - 35, (int) (bY + flapY));
					g.drawLine(bX, bY, bX + 35, (int) (bY + flapY));
					g.drawLine(bX, bY - 8, bX - 35, (int) (bY + flapY));
					g.drawLine(bX, bY - 8, bX + 35, (int) (bY + flapY));
					g.fillOval(bX - 20, bY - 20, 40, 40);

					RadialGradientPaint shadowBorder = new RadialGradientPaint(bX, bY, 20, new float[] { 0.6f,
							1f }, new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 200) });
					g.setPaint(shadowBorder);
					g.fillOval(bX - 20, bY - 20, 40, 40);

					// Draw nose
					g.setStroke(normalStroke);
					triangleX[0] = bX - 5;
					triangleX[1] = bX + 5;
					triangleX[2] = bX;
					triangleY[0] = bY + 10;
					triangleY[1] = bY + 10;
					triangleY[2] = bY + 20;
					g.setColor(new Color(255, 200, 0));
					g.fillPolygon(triangleX, triangleY, 3);

					// Draw eyes
					g.setColor(Color.white);
					g.fillOval(bX - 12, bY - 4, 16, 16);
					g.fillOval(bX - 2, bY - 4, 14, 16);
					int pupilX = 2;
					int pupilY = 2;
					if (mouseX > bX)
						pupilX = 6;
					if (mouseY > bY)
						pupilY = 6;
					g.setColor(Color.black);
					g.fillOval(bX - 12 + pupilX, bY - 4 + pupilY, 5, 6);
					g.fillOval(bX - 2 + pupilX, bY - 4 + pupilY, 6, 6);

					// Draw hair
					g.setStroke(thinStroke);

					int hairDeltaY1 = -8;
					int hairDeltaY2 = -4;
					float dx = mouseX - birdX[b];
					float dy = mouseY - birdY[b];
					float distanceSq = dx * dx + dy * dy;
					if (distanceSq < BIRD_TARGET_AVOID_RANGE_SQ) {
						hairDeltaY2 = -2;
					}

					g.drawLine(bX - 10, bY + hairDeltaY1, bX - 2, bY + hairDeltaY2);
					g.drawLine(bX + 2, bY + hairDeltaY2, bX + 10, bY + hairDeltaY1);
				}

				// Draw cannon fire line
				g.setStroke(normalStroke);
				if (beamTime[b] > 0f) {
					float alpha = beamTime[b] / CANNON_BEAM_TIME;
					g.setColor(new Color(0.3f, 0.3f, 0.3f, alpha * 0.8f));
					int deltaX = 0;
					if (b % 2 == 1)
						deltaX = -5;
					g.drawLine(410 + deltaX, 585 + (int) farmerHop, beamX[b], beamY[b]);
				}

				// Draw effects
				if (effectProgress[b] != -1) {

					float alpha = 1f - effectProgress[b];
					g.setColor(new Color(effectColor[b].getRed(), effectColor[b].getGreen(), effectColor[b]
							.getBlue(), (int) (255 * alpha)));
					g.fillOval((int) (effectX[b] + effectVX[b] * effectProgress[b] - 20 * alpha),
							(int) (effectY[b] + effectVY[b] * effectProgress[b] - 20 * alpha),
							(int) (40 * alpha), (int) (40 * alpha));

					effectProgress[b] += EFFECT_SPEED * frameTimeS;
					if (effectProgress[b] > 1f)
						effectProgress[b] = -1;
				}
			}

			// Draw gun
			g.setStroke(normalStroke);
			g.setColor(Color.black);
			float deltaX = (410 - mouseX) * 0.1f;
			float deltaY = (585 - mouseY) * 0.1f;
			g.drawLine(410, 585 + (int) farmerHop, (int) (410 - deltaX), (int) (585 - deltaY) + (int) farmerHop);
			g.drawLine(405, 585 + (int) farmerHop, (int) (410 - deltaX) - 5, (int) (585 - deltaY)
					+ (int) farmerHop);

			// Draw farmer
			g.setColor(Color.pink);
			g.fillOval(385, 545 + (int) farmerHop, 30, 30);

			// Overall
			g.setColor(Color.green);
			g.fillRoundRect(378, 570 + (int) farmerHop, 44, 80, 40, 40);

			// Hat
			g.setColor(Color.orange);
			g.fillOval(375, 550 - (int) farmerHop, 50, 10);
			g.fillOval(390, 540 - (int) farmerHop, 20, 20);

			// Voice
			g.setFont(voiceFont);
			for (int b = 0; b < MAX_BIRDS; b++) {
				if (voiceProgress[b] < 1f) {
					g.setColor(new Color(0.2f, 0.2f, 0.2f, 1f - voiceProgress[b]));
					g.drawString(voices[voiceText[b]], 350 + voiceX[b], 550 - voiceProgress[b] * 100);
				}
			}

			// Draw ammo
			for (int b = 0; b < maxCannonAmmo && b < 15; b++) {
				g.setPaint(bulletChrome);
				if (b < cannonAmmo)
					g.fillRect(10, 550 - 35 * b, 20, 30);
				else
					g.fillRect(15, 560 - 35 * b, 10, 15);
			}

			if (subState == SUBSTATE_LEVELSTART) {
				g.setFont(smallFont);
				g.setColor(Color.white);
				g.drawString("Get ready!", 300, 300);

			} else if (subState == SUBSTATE_LEVELEND) {

				g.setFont(smallFont);
				g.setColor(Color.white);
				g.drawString("Level complete!", 300, 300);
			}

		} else if (gameState == STATE_GAMEOVER) {

			// Draw huge bird
			g.setColor(Color.red);
			g.fillOval(-10, -100, 820, 820);
			RadialGradientPaint shadowBorder = new RadialGradientPaint(400, 300, 360, new float[] { 0.6f, 1f },
					new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 200) });
			g.setPaint(shadowBorder);
			g.fillOval(-10, -100, 820, 820);

			g.setStroke(normalStroke);
			triangleX[0] = 270;
			triangleX[1] = 530;
			triangleX[2] = 400;
			triangleY[0] = 400;
			triangleY[1] = 400;
			triangleY[2] = 800;
			g.setColor(new Color(255, 200, 0));
			g.fillPolygon(triangleX, triangleY, 3);

			// Draw eyes
			g.setColor(Color.white);
			g.fillOval(170, 200, 300, 300);
			g.fillOval(330, 200, 300, 300);
			g.setColor(Color.black);
			g.fillOval(270, 300, 100, 100);
			g.fillOval(430, 300, 100, 100);

			g.setStroke(thickStroke);
			g.drawLine(200, 160, 380, 260);
			g.drawLine(440, 260, 640, 150);

			g.setFont(bigFont);
			g.setPaint(yellowChrome2);
			g.drawString("Game", 10, 200);
			g.rotate(0.25f);
			g.drawString("Over!", 400, 200);
			g.rotate(-0.25f);

			g.setFont(smallFont);
			g.setColor(Color.white);
			g.drawString("Level reached: " + level, 80, 540);
			g.drawString("Score: " + score, 80, 570);
			if (score > highScore)
				g.drawString("Highscore!", 500, 555);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseButtonDown = true;
	}

	public static void main(String[] args) {
		try {
			new FarmerJohn().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
