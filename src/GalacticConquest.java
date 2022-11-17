import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Java4K game - Galactic Conquest 4K. Updated for Java4K contest 2013.
 */

//
// There is no choice: exterminate all other races or perish yourself. Colonize or conquer stars to increase your ship
// production. A newly colonized or conquered star needs to build up its infrastructure and will not be very useful
// initially. Keep your well-developed stars safe!
//
// Instructions:
// Left click = select single star (with nothing selected) or move ships from selected star(s) to this star
// Drag with left mouse button down = select multiple stars
// Right click = change number of ships to move from each selected star (all, half, one)
// Mouse over an owned star to see its current infrastructure and build project (ship or starbase).
// C = toggle mouseover star as collection point. New ships move to collection points automagically.
// S = build star base at mouseover star
// H = show/hide balance of powers history graph.
//
// Some tips:
// - Ships move 50% faster between owned stars (because of fancy warp accelerator technology), use this to your
// advantage.
// - Production speed of ships and star bases depends directly on the infrastructure level of the star (i.e. 10%
// infrastructure = only 10% production speed). Harassment to destroy infrastructure helps, even if you cannot hold a
// star!
// - You can support one starbase for each 5 stars conquered (one is free). A starbase provides powerful stationary
// defense, and a 50% production bonus. While building a starbase, the star does not produce ships however.
// - Try to capture chokepoints and enforce these with starbases. Set chokepoints as collection points to reinforce them
// automatically.
// - Be sure to leave some defenses at stars in the second line in case the AI slips through.
// - Higher difficulty means that the AI acts more quickly and produces ships faster. Impossible is really impossible
// :-)
// - Choose action game for a quick battle that requires fast reflexes; choose an epic game for a slow, lengthy fight.
//

public class GalacticConquest extends AbstractGame {

  private static final int DISPLAY_WIDTH = 800;
  private static final int DISPLAY_HEIGHT = 600;

  private static final int NUMBER_OF_STARS = 35;
  private static final int MAX_NUMBER_OF_FLEETS_PER_RACE = 140;
  private static final int NUMBER_OF_RACES = 4;
  private static final int RACE_NEUTRAL = 4;
  private static final int MINIMUM_DISTANCE_BETWEEN_STARS = 55;
  private static final int MINIMUM_DISTANCE_BETWEEN_HOME_STARS = 220;

  private static final float STAR_PRODUCTION_PROGRESS_PER_S = 0.3f;
  private static final float STAR_ROTATION_PROGRESS_PER_S = 0.8f;
  private static final float FLEET_MOVEMENT_PROGRESS_PER_S = 15f;

  private static final int BASE_STARS_PER_BASE = 5;
  private static final int BASE_COSTS = 8;
  private static final float BASE_STRENGTH_PER_COST = 3f;
  private static final float BASE_PRODUCTION_BONUS = 1.50f;

  private static final float HISTORY_INTERVAL = 0.1f;
  private static final int HISTORY_POINTS = 770;

  private static final String[] moveFactorText = new String[]{"All", "Half", "One"};

  private static final int MAX_EXPLOSIONS = 750;
  private static final int PARTS_PER_EXPLOSION = 50;

  private static final float MIN_DRAG_DISTANCE = 20;

  int mouseX = 0;
  int mouseY = 0;
  int mouseOverStar = -1;

  float AI_PRODUCTION_FACTOR = 1;
  float AI_MOVEMENT_DELAY_S = 1;
  float STAR_INFRA_PROGRESS_PER_S = 0.03f;

  // Stars
  int[] starX = new int[NUMBER_OF_STARS];
  int[] starY = new int[NUMBER_OF_STARS];
  int[] starOwner = new int[NUMBER_OF_STARS];
  int[] starShips = new int[NUMBER_OF_STARS];
  float[] starMovementDelay = new float[NUMBER_OF_STARS];
  int[] starSeed = new int[NUMBER_OF_STARS];
  float[] starOrbit = new float[NUMBER_OF_STARS];
  float[] starProduction = new float[NUMBER_OF_STARS];
  float[] starInfra = new float[NUMBER_OF_STARS];
  boolean[] starCollection = new boolean[NUMBER_OF_STARS];
  boolean[] starSelected = new boolean[NUMBER_OF_STARS];
  int collectionIndex = 0;

  // Ships
  int[][] fleetOrigin = new int[NUMBER_OF_RACES][MAX_NUMBER_OF_FLEETS_PER_RACE];
  int[][] fleetDestination = new int[NUMBER_OF_RACES][MAX_NUMBER_OF_FLEETS_PER_RACE];
  int[][] fleetShips = new int[NUMBER_OF_RACES][MAX_NUMBER_OF_FLEETS_PER_RACE];
  int[][] fleetSeed = new int[NUMBER_OF_RACES][MAX_NUMBER_OF_FLEETS_PER_RACE];
  float[][] fleetProgress = new float[NUMBER_OF_RACES][MAX_NUMBER_OF_FLEETS_PER_RACE];

  float[] xploAngle = new float[MAX_EXPLOSIONS];
  int[] xploX = new int[MAX_EXPLOSIONS];
  int[] xploY = new int[MAX_EXPLOSIONS];
  float[] xploTime = new float[MAX_EXPLOSIONS];

  // Star bases
  int[] starBase = new int[NUMBER_OF_STARS];
  int playerBases = 0;
  int maxBases = 0;

  // Races
  Color[] raceColor = new Color[]{new Color(0.3f, 1f, 0.3f), new Color(1f, 0.3f, 0.3f),
      new Color(1f, 0.3f, 1f), new Color(1f, 1f, 0.3f), new Color(1f, 1f, 1f)};
  Color[] raceColorTransparent = new Color[5];
  RadialGradientPaint[] starPaints = new RadialGradientPaint[5];

  // A history of power
  int[][] powerHistory = new int[NUMBER_OF_RACES][HISTORY_POINTS];
  int[] starsPerPlayer = new int[NUMBER_OF_RACES];
  float historyInterval = 0f;
  int historyIndex = 0;
  boolean historyWrapped = false;
  boolean historyVisible = true;

  // Selection
  int moveFactor = 0;
  boolean isDragging = false;
  int dragStartX = 0, dragStartY = 0;

  // Helpers
  final Random random = new Random();

  // Game state
  int gameState = 0;
  boolean victory = false;

  // Text
  Font bigFont = new Font("Impact", Font.PLAIN, 75);
  final Font smallFont = new Font("Impact", Font.PLAIN, 18);

  AffineTransform transformBuffer = null, transformBuffer2 = null;

  int[] shipShapeX = {0, 2, -2};
  int[] shipShapeY = {0, 6, 6};
  int[] wingShapeX = {0, 4, -4};
  int[] wingShapeY = {0, 5, 5};

  LinearGradientPaint titlePaint = new LinearGradientPaint(0, 120, 0, 200,
      new float[]{0.05f, 0.5f, 0.95f}, new Color[]{Color.black, Color.white, Color.black});
  LinearGradientPaint titleBarPaint = new LinearGradientPaint(0, 100, 800, 100,
      new float[]{0f, 0.5f, 1f},
      new Color[]{Color.black, Color.orange, Color.black});


  Color starColor = new Color(0.2f, 0.2f, 0.2f);

  int speed = 0;
  int difficulty = 0;
  int playerColor = 0;

  float speedFactor = 1;

  public GalacticConquest() {
    super("Galactic Conquest", 800, 600, true);
  }

  @Override
  protected void setup() {
    // Set cooler cursor
    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    for (int r = 0; r <= NUMBER_OF_RACES; r++) {
      raceColorTransparent[r] = new Color(raceColor[r].getRed(), raceColor[r].getGreen(),
          raceColor[r].getBlue(), 20);
      starPaints[r] = new RadialGradientPaint(0, 0, 30, new float[]{0.1f, 0.3f, 0.6f}, new Color[]{
          raceColor[r], raceColorTransparent[r], new Color(1f, 1f, 1f, 0)});
    }
  }

  @Override
  protected void update(float stepS, float frameTimeS, float gameTimeS) {
    random.setSeed((long) gameTimeS);

    // Stuff common to game over and in-game states

    // Determine whether the mouse is hovering over a star
    mouseOverStar = -1;
    for (int s = 0; s < NUMBER_OF_STARS; s++) {
      if (Point2D.distance(starX[s], starY[s], mouseX, mouseY) < 20) {
        mouseOverStar = s;
      }
    }

    // Count number of player stars and starbases
    for (int r = 0; r < NUMBER_OF_RACES; r++) {
      starsPerPlayer[r] = 0;
    }
    playerBases = 0;
    for (int s = 0; s < NUMBER_OF_STARS; s++) {
      if (starOwner[s] != RACE_NEUTRAL) {
        starsPerPlayer[starOwner[s]]++;
        if (starOwner[s] == playerColor) {
          if (starBase[s] > -1) {
            playerBases++;
          }
        }
      }
    }

    // Remember history
    historyInterval -= stepS;
    if (historyInterval <= 0) {
      for (int r = 0; r < NUMBER_OF_RACES; r++) {
        powerHistory[r][historyIndex] = starsPerPlayer[r];
      }
      historyIndex++;
      if (historyIndex >= HISTORY_POINTS) {
        historyWrapped = true;
        historyIndex = 0;
      }
      historyInterval = HISTORY_INTERVAL;
    }

    // Move fleets
    for (int r = 0; r < NUMBER_OF_RACES; r++) {
      for (int fl = 0; fl < MAX_NUMBER_OF_FLEETS_PER_RACE; fl++) {
        if (fleetShips[r][fl] > 0) {

          float fleetSpeed = FLEET_MOVEMENT_PROGRESS_PER_S;
          if (starOwner[fleetDestination[r][fl]] == r) {
            // Ships move faster between owned stars due to fancy fast gate tech
            fleetSpeed = fleetSpeed * 1.5f;
          }

          // Fleets move the same speed over any distance
          fleetProgress[r][fl] += (fleetSpeed / Point2D.distance(starX[fleetOrigin[r][fl]],
              starY[fleetOrigin[r][fl]], starX[fleetDestination[r][fl]],
              starY[fleetDestination[r][fl]]))
              * stepS;

          // Any ships moving in opposite direction that we will intercept?
          for (int er = 0; er < NUMBER_OF_RACES; er++) {
            if (er != r) {
              for (int efl = 0; efl < MAX_NUMBER_OF_FLEETS_PER_RACE; efl++) {
                if (fleetShips[er][efl] > 0
                    && fleetOrigin[er][efl] == fleetDestination[r][fl]
                    && fleetDestination[er][efl] == fleetOrigin[r][fl]) {
                  int x1 = starX[fleetOrigin[r][fl]]
                      + (int) ((starX[fleetDestination[r][fl]] - starX[fleetOrigin[r][fl]])
                      * fleetProgress[r][fl]);
                  int y1 = starY[fleetOrigin[r][fl]]
                      + (int) ((starY[fleetDestination[r][fl]] - starY[fleetOrigin[r][fl]])
                      * fleetProgress[r][fl]);
                  if (Point2D
                      .distance(
                          x1,
                          y1,
                          starX[fleetOrigin[er][efl]]
                              + (int) (
                              (starX[fleetDestination[er][efl]] - starX[fleetOrigin[er][efl]])
                                  * fleetProgress[er][efl]),
                          starY[fleetOrigin[er][efl]]
                              + (int) (
                              (starY[fleetDestination[er][efl]] - starY[fleetOrigin[er][efl]])
                                  * fleetProgress[er][efl])) < 5) {

                    int c = PARTS_PER_EXPLOSION;
                    for (int e = 0; e < MAX_EXPLOSIONS; e++) {
                      if (xploTime[e] <= 0f) {
                        xploX[e] = x1;
                        xploY[e] = y1;
                        xploTime[e] = random.nextFloat() * 0.5f + 0.5f;
                        xploAngle[e] = random.nextFloat() * 6.28f;
                        if (c-- <= 0) {
                          break;
                        }
                      }
                    }

                    // Battle!
                    int esh = fleetShips[er][efl];
                    fleetShips[er][efl] -= fleetShips[r][fl];
                    fleetShips[r][fl] -= esh;
                  }
                }
              }
            }
          }

          if (fleetProgress[r][fl] >= 1f) {

            // Arrived! Check what will happen now
            if (starOwner[fleetDestination[r][fl]] != r) {

              if (starOwner[fleetDestination[r][fl]] != RACE_NEUTRAL) {
                int c = PARTS_PER_EXPLOSION;
                for (int e = 0; e < MAX_EXPLOSIONS; e++) {
                  if (xploTime[e] <= 0f) {
                    xploX[e] = starX[fleetDestination[r][fl]];
                    xploY[e] = starY[fleetDestination[r][fl]];
                    xploAngle[e] = random.nextFloat() * 6.28f;
                    xploAngle[e] = random.nextFloat() * 6.28f;
                    xploTime[e] = random.nextFloat() * 0.5f + 0.5f;
                    if (c-- <= 0) {
                      break;
                    }
                  }
                }
              }

              // Fight enemy ships, if any
              if (fleetShips[r][fl] > starShips[fleetDestination[r][fl]]) {
                // Yeehaw, we victorious!
                fleetShips[r][fl] -= starShips[fleetDestination[r][fl]];

                boolean vict = true;
                if (starBase[fleetDestination[r][fl]] > 0) {
                  // Theres a starbase here, attack it!
                  if (fleetShips[r][fl] > starBase[fleetDestination[r][fl]]
                      * BASE_STRENGTH_PER_COST) {
                    // Again we are victorious! We are unstoppable! Bow before our
                    // might!
                    fleetShips[r][fl] -= starBase[fleetDestination[r][fl]]
                        * BASE_STRENGTH_PER_COST;
                  } else {
                    // Damn star bases...
                    starBase[fleetDestination[r][fl]] -= fleetShips[r][fl]
                        / BASE_STRENGTH_PER_COST;
                    vict = false;
                  }
                }

                if (vict) {
                  starOwner[fleetDestination[r][fl]] = r;
                  starShips[fleetDestination[r][fl]] = fleetShips[r][fl] - 1;
                  starInfra[fleetDestination[r][fl]] = 0;
                  starBase[fleetDestination[r][fl]] = -1;
                }
              } else {
                // We lose, but perhaps today is a good day to die
                starShips[fleetDestination[r][fl]] -= fleetShips[r][fl];
              }
            } else {
              // Place the fleet in orbit
              starShips[fleetDestination[r][fl]] += fleetShips[r][fl];
            }
            fleetShips[r][fl] = 0;
          }
        }
      }
    }

    // Update production and star orbit rotation
    for (int s = 0; s < NUMBER_OF_STARS; s++) {

      starOrbit[s] += STAR_ROTATION_PROGRESS_PER_S * stepS;

      if (starOwner[s] != RACE_NEUTRAL) {

        starInfra[s] += STAR_INFRA_PROGRESS_PER_S * stepS;
        if (starInfra[s] > 1) {
          starInfra[s] = 1;
        }

        float aP = (STAR_PRODUCTION_PROGRESS_PER_S * starInfra[s]) * stepS;
        if (starOwner[s] != playerColor) {
          aP *= AI_PRODUCTION_FACTOR;
        }
        if (starBase[s] > -1) {
          aP *= BASE_PRODUCTION_BONUS;
        }
        starProduction[s] += aP;

        if (starProduction[s] >= 1f) {

          if (starBase[s] > -1 && starBase[s] < BASE_COSTS) {
            // Star base production ongoing
            starBase[s]++;

          } else {

            starShips[s]++;

            if (starOwner[s] == playerColor) {

              // Find a collection point
              int nextCollection = -1;

              for (int i = 0; i < NUMBER_OF_STARS; i++) {
                int testIndex = (collectionIndex + i + 1) % NUMBER_OF_STARS;
                if (starCollection[testIndex]) {
                  nextCollection = testIndex;
                  break;
                }
              }

              if (nextCollection != -1) {
                collectionIndex = nextCollection;
                // A collection point is set for the player, so move new production there
                // Find available fleet
                for (int fl = 0; fl < MAX_NUMBER_OF_FLEETS_PER_RACE; fl++) {
                  if (fleetShips[playerColor][fl] <= 0) {
                    // There is a fleet available, so lets move
                    fleetOrigin[playerColor][fl] = s;
                    fleetDestination[playerColor][fl] = collectionIndex;
                    fleetProgress[playerColor][fl] = 0;
                    fleetShips[playerColor][fl] = 1;
                    starShips[s]--;
                    break;
                  }
                }
              }
            }
          }
          starProduction[s] = 0f;
        }
      }
    }

    // Update explosions
    for (int e = 0; e < MAX_EXPLOSIONS; e++) {
      xploTime[e] -= (stepS * 0.5f) / speedFactor;
    }

    // Do AI actions on stars
    for (int s = 0; s < NUMBER_OF_STARS; s++) {
      if (starOwner[s] != playerColor && starOwner[s] != RACE_NEUTRAL) {

        starMovementDelay[s] -= stepS;
        if (starMovementDelay[s] <= 0f) {
          starMovementDelay[s] = AI_MOVEMENT_DELAY_S;

          // Give AI ships that are in orbit new orders
          if (starShips[s] > 0) {

            // Great, lets find a nice place to go
            int closest = -1;
            float clDist = 99999999;
            for (int s2 = 0; s2 < NUMBER_OF_STARS; s2++) {

              if (s2 != s) {
                float dist = (float) Point2D.distance(starX[s], starY[s], starX[s2],
                    starY[s2]);
                if (starOwner[s2] != starOwner[s]) {
                  // Yay, enemy or neutral planet, lets attack this one!
                  if (dist < clDist) {
                    closest = s2;
                    clDist = dist;
                  }
                }
              }
            }

            if (closest != -1) {
              // Find available fleet
              for (int sh = 0; sh < MAX_NUMBER_OF_FLEETS_PER_RACE; sh++) {
                if (fleetShips[starOwner[s]][sh] <= 0) {
                  // There is a fleet available, so lets move
                  fleetOrigin[starOwner[s]][sh] = s;
                  fleetDestination[starOwner[s]][sh] = closest;
                  fleetProgress[starOwner[s]][sh] = 0;
                  fleetShips[starOwner[s]][sh] = starShips[s];
                  starShips[s] = 0;
                  break;
                }
              }
            }
          }
        }
      }
    }

    if (gameState == 1) {

      maxBases = (starsPerPlayer[playerColor] / BASE_STARS_PER_BASE) + 1;

      // Is the game finished?
      if (starsPerPlayer[playerColor] == NUMBER_OF_STARS) {
        // All stars under control, victory!
        victory = true;
        gameState = 2;
      }
      if (starsPerPlayer[playerColor] == 0) {
        // Lost the game
        victory = false;
        gameState = 2;
        // Disable all fleets
        for (int sh = 0; sh < MAX_NUMBER_OF_FLEETS_PER_RACE; sh++) {
          fleetShips[playerColor][sh] = 0;
        }
      }

    } else {

      historyVisible = true;
    }
  }

  @Override
  protected void render(Graphics2D g, float frameTimeS, float gameTimeS) {
    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));

    // Clear background (common to all states)
    g.setColor(Color.black);
    g.fillRect(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

    random.setSeed(32);
    g.setColor(starColor);
    for (int s = 0; s < 5000; s++) {
      g.fillRect(random.nextInt(DISPLAY_WIDTH), random.nextInt(DISPLAY_HEIGHT), 1, 1);
    }
    random.setSeed((long) gameTimeS);

    if (gameState == 0) {

      // Title screen
      g.setFont(bigFont);
      g.setPaint(titlePaint);
      g.drawString("Galactic Conquest 4k", 70, 200);
      g.setPaint(titleBarPaint);
      g.drawLine(70, 200, 730, 200);
      g.setColor(Color.orange);
      g.setFont(smallFont);
      g.drawString("www.basvs.dev", 20, 575);
      g.setColor(Color.darkGray);
      g.fillRoundRect(130, 303 + 25 * difficulty, 125, 20, 10, 10);
      g.fillRoundRect(330, 303 + 25 * speed, 115, 20, 10, 10);
      g.fillRoundRect(530, 303 + 25 * playerColor, 105, 20, 10, 10);
      g.setColor(Color.green);
      g.drawString("Easy", 150, 320);
      g.drawString("Normal", 150, 345);
      g.drawString("Hard", 150, 370);
      g.drawString("Impossible", 150, 395);
      g.drawString("Action", 350, 320);
      g.drawString("Epic", 350, 345);

      g.drawString("Go!", 360, 490);

      for (int c = 0; c < NUMBER_OF_RACES; c++) {
        g.setColor(raceColor[c]);
        g.fillRoundRect(560, 305 + c * 25, 16, 16, 10, 10);
      }


    } else {

      // Render history
      if (historyVisible) {
        float stepX = 1;
        for (int r = 0; r < NUMBER_OF_RACES; r++) {
          g.setColor(raceColorTransparent[r]);
          int lastX = 12, lastY = powerHistory[r][0] * 8;
          if (historyWrapped) {
            lastY = powerHistory[r][(historyIndex + 1) % HISTORY_POINTS] * 8;
          }
          for (int h = 0; (historyWrapped && h < HISTORY_POINTS)
              || (!historyWrapped && h < historyIndex); h++) {
            int index = h;
            if (historyWrapped) {
              index = (index + historyIndex) % HISTORY_POINTS;
            }
            int x = (int) (14f + h * stepX);
            int y = powerHistory[r][index] * 8;
            g.drawLine(lastX, 560 - lastY, x, 560 - y);
            lastX = x;
            lastY = y;
          }
        }
      }

      for (int e = 0; e < MAX_EXPLOSIONS; e++) {
        if (xploTime[e] > 0f) {
          g.setColor(new Color(1f, 1f, 1f, xploTime[e]));
          float dist = (1f - xploTime[e]) * 20;
          g.fillOval((int) (Math.sin(xploAngle[e]) * dist) + xploX[e],
              (int) (Math.cos(xploAngle[e]) * dist) + xploY[e], 4, 4);
        }
      }

      // Render stars, starbases and ships in orbit
      g.setFont(smallFont);
      for (int s = 0; s < NUMBER_OF_STARS; s++) {

        transformBuffer = g.getTransform();
        g.translate(starX[s], starY[s]);
        if (starSelected[s]) {
          g.setColor(Color.white);
          g.fillOval(-7, -7, 14, 14);
        }
        g.setPaint(starPaints[starOwner[s]]);
        g.fillOval(-20, -20, 40, 40);

        if (starOwner[s] != RACE_NEUTRAL) {
          // Render ships in orbit
          random.setSeed(starSeed[s]);
          for (int sh = 0; sh < starShips[s] && sh < 200; sh++) {
            transformBuffer2 = g.getTransform();
            float range = 12f + random.nextFloat() * 12f;
            float angle = random.nextFloat() * 6.28f;
            g.translate((int) (range * Math.cos(starOrbit[s] + angle)) - 1,
                (int) (range * Math.sin(starOrbit[s] + angle)) - 1);
            g.rotate(starOrbit[s] + angle + 3.14f);
            g.setColor(Color.gray);
            g.fillPolygon(wingShapeX, wingShapeY, 3);
            g.setColor(raceColor[starOwner[s]]);
            g.fillPolygon(shipShapeX, shipShapeY, 3);
            g.setTransform(transformBuffer2);
          }

          // Render starbase
          if (starBase[s] > -1) {
            g.setColor(Color.white);
            g.drawString("S", -4, +7);
          }
        }
        g.setTransform(transformBuffer);
      }

      // Render fleets
      for (int r = 0; r < NUMBER_OF_RACES; r++) {
        for (int sh = 0; sh < MAX_NUMBER_OF_FLEETS_PER_RACE; sh++) {
          if (fleetShips[r][sh] > 0) {
            int dx = starX[fleetDestination[r][sh]] - starX[fleetOrigin[r][sh]];
            float dy = starY[fleetDestination[r][sh]] - starY[fleetOrigin[r][sh]];
            float fleetDir = (float) Math.atan2(dy, dx);
            random.setSeed(fleetSeed[r][sh]);
            for (int i = 0; i < fleetShips[r][sh] && i < 50; i++) {
              transformBuffer = g.getTransform();
              g.translate((int) (starX[fleetOrigin[r][sh]] + dx * fleetProgress[r][sh]) - 6
                  + random.nextInt(12), starY[fleetOrigin[r][sh]]
                  + (int) (dy * fleetProgress[r][sh]) - 6 + random.nextInt(12));
              g.rotate(fleetDir + 1.57f);
              g.setColor(Color.gray);
              g.fillPolygon(wingShapeX, wingShapeY, 3);
              g.setColor(raceColor[r]);
              g.fillPolygon(shipShapeX, shipShapeY, 3);
              g.setTransform(transformBuffer);
            }
          }
        }
      }

      if (gameState == 1) {

        // Render star control info
        for (int s = 0; s < NUMBER_OF_STARS; s++) {

          g.setColor(raceColor[playerColor]);
          if (mouseOverStar == s) {
            g.drawOval(starX[s] - 30, starY[s] - 30, 60, 60);
          }

          // Render collection circle
          if (starCollection[s]) {
            g.drawOval(starX[s] - 10, starY[s] - 10, 20, 20);
          }
        }

        // Render dragging frame
        if (isDragging) {
          int x1 = dragStartX;
          int x2 = mouseX;
          if (x2 < x1) {
            x1 = mouseX;
            x2 = dragStartX;
          }
          int y1 = dragStartY;
          int y2 = mouseY;
          if (y2 < y1) {
            y1 = mouseY;
            y2 = dragStartY;
          }

          g.setColor(raceColor[playerColor]);
          g.drawRect(x1, y1, x2 - x1, y2 - y1);
        }

        // Render a menu bar
        g.setColor(raceColorTransparent[playerColor]);
        g.fillRoundRect(10, 560, 780, 30, 10, 10);
        g.setColor(raceColor[playerColor]);

        // Render selection counter
        g.drawString(moveFactorText[moveFactor], 20, 550);

        // Render starbase counter
        g.setColor(Color.orange);
        g.drawString("Bases", 630, 582);
        g.drawString(String.valueOf(playerBases), 690, 582);
        g.drawString("of", 720, 582);
        g.drawString(String.valueOf(maxBases), 760, 582);

        // Render production progress
        if (mouseOverStar != -1 && starOwner[mouseOverStar] == playerColor) {

          // Building infrastructure
          g.drawString("Infra: ", 20, 582);
          float factor;
          if (starBase[mouseOverStar] > -1 && starBase[mouseOverStar] < BASE_COSTS) {
            g.drawString("Starbase: ", 260, 582);
            factor = ((float) starBase[mouseOverStar] + starProduction[mouseOverStar]) / BASE_COSTS;
          } else {
            g.drawString("Ship: ", 260, 582);
            factor = starProduction[mouseOverStar];
          }
          g.setColor(Color.darkGray);
          g.fillRoundRect(110, 569, 100, 15, 10, 10);
          g.fillRoundRect(360, 569, 100, 15, 10, 10);
          g.setColor(Color.white);
          g.fillRoundRect(110, 569, (int) (100 * starInfra[mouseOverStar]), 15, 10, 10);
          g.fillRoundRect(360, 569, (int) (100 * factor), 15, 10, 10);
        }
      } else {

        historyVisible = true;

        g.setFont(bigFont);
        // Post-game
        g.setFont(smallFont);
        if (victory) {
          g.setColor(Color.green);
          g.drawString("Victory!", 100, 582);
        } else {
          g.setColor(Color.red);
          g.drawString("Defeat!", 100, 582);
        }
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {

    int mouseX = e.getX();
    int mouseY = e.getY();

    if (gameState == 0) {

      if (mouseX >= 130 && mouseX <= 255 && mouseY > 303 && mouseY < 403) {
        difficulty = (int) Math.floor((mouseY - 303) / 25);
      }
      if (mouseX >= 330 && mouseX <= 455 && mouseY > 303 && mouseY < 345) {
        speed = (int) Math.floor((mouseY - 303) / 25);
      }
      if (mouseX >= 530 && mouseX <= 655 && mouseY > 303 && mouseY < 395) {
        playerColor = (int) Math.floor((mouseY - 303) / 25);
      }

      if (mouseY >= 465 && mouseX > 340 && mouseX < 420) {
        // Go! Start the game
        AI_PRODUCTION_FACTOR = 1.2f;
        AI_MOVEMENT_DELAY_S = 0.5f;

        if (difficulty == 0) {
          AI_PRODUCTION_FACTOR = 0.2f;
          AI_MOVEMENT_DELAY_S = 4f;
        } else if (difficulty == 1) {
          AI_PRODUCTION_FACTOR = 0.5f;
          AI_MOVEMENT_DELAY_S = 2.5f;
        } else if (difficulty == 2) {
          AI_PRODUCTION_FACTOR = 0.8f;
          AI_MOVEMENT_DELAY_S = 1f;
        }

        speedFactor = 1f;
        STAR_INFRA_PROGRESS_PER_S = 0.03f;
        if (speed == 1) {
          speedFactor = 0.5f;
          STAR_INFRA_PROGRESS_PER_S = 0.015f;
        }

        // Generate some stars
        for (int s = 0; s < NUMBER_OF_STARS; s++) {
          boolean duplicate;
          do {
            starX[s] = 40 + random.nextInt(DISPLAY_WIDTH - 80);
            starY[s] = 40 + random.nextInt(DISPLAY_HEIGHT - 120);
            starProduction[s] = 1;
            starInfra[s] = 0;
            starOrbit[s] = 0;
            starOwner[s] = RACE_NEUTRAL;
            starSeed[s] = random.nextInt(256);
            starShips[s] = 0;
            starMovementDelay[s] = random.nextFloat() * AI_MOVEMENT_DELAY_S;
            starBase[s] = -1;
            starCollection[s] = false;
            starSelected[s] = false;

            duplicate = false;
            for (int o = 0; o < s; o++) {
              if (Point2D.distance(starX[s], starY[s], starX[o], starY[o])
                  < MINIMUM_DISTANCE_BETWEEN_STARS) {
                duplicate = true;
              }
            }
          } while (duplicate);
        }
        // Assign home stars and reset fleets
        for (int r = 0; r < NUMBER_OF_RACES; r++) {
          for (int sh = 0; sh < MAX_NUMBER_OF_FLEETS_PER_RACE; sh++) {
            fleetShips[r][sh] = 0;
            fleetSeed[r][sh] = random.nextInt(256);
          }

          boolean duplicate;
          do {
            int newStar = random.nextInt(NUMBER_OF_STARS);
            duplicate = true;
            if (starOwner[newStar] == RACE_NEUTRAL) {
              // This star is still unoccupied, find out how close the nearest enemy homestar
              // is
              duplicate = false;
              for (int o = 0; o < NUMBER_OF_STARS; o++) {
                if (starOwner[o] != RACE_NEUTRAL && o != newStar) {
                  if (Point2D
                      .distance(starX[newStar], starY[newStar], starX[o], starY[o])
                      < MINIMUM_DISTANCE_BETWEEN_HOME_STARS) {
                    duplicate = true;
                  }
                }
              }
            }
            if (!duplicate) {
              starOwner[newStar] = r;
              starInfra[newStar] = 1;
            }
          } while (duplicate);

        }

        // Mouse was clicked, so start game
        historyVisible = true;
        isDragging = false;
        historyIndex = 0;
        historyInterval = HISTORY_INTERVAL;
        historyWrapped = false;
        gameState = 1;
      }
    } else if (gameState == 1) {
      if (e.getButton() == MouseEvent.BUTTON3) {
        moveFactor = (moveFactor + 1) % 3;
      } else {
        if (mouseOverStar != -1) {
          boolean anySelected = false;
          // A star was clicked, send selected ships to this star, if any
          for (int s = 0; s < NUMBER_OF_STARS; s++) {

            if (s != mouseOverStar) {

              if (starSelected[s]) {
                anySelected = true;

                if (starOwner[s] == playerColor && starShips[s] > 0) {

                  int shipsToMove = starShips[s];
                  if (moveFactor == 1) {
                    shipsToMove = (int) Math.ceil(shipsToMove / 2f);
                  } else if (moveFactor == 2) {
                    shipsToMove = 1;
                  }

                  // Find available fleet
                  for (int sh = 0; sh < MAX_NUMBER_OF_FLEETS_PER_RACE; sh++) {
                    if (fleetShips[playerColor][sh] <= 0) {
                      // There is a fleet available, so lets move
                      fleetOrigin[playerColor][sh] = s;
                      fleetDestination[playerColor][sh] = mouseOverStar;
                      fleetProgress[playerColor][sh] = 0;
                      fleetShips[playerColor][sh] = shipsToMove;
                      starShips[s] -= shipsToMove;
                      break;
                    }
                  }
                }
                starSelected[s] = false;
              }
            }
          }

          if (moveFactor == 0) {
            deselectAll();
          }

          // Were none selected? Then select the star that was clicked
          if (!anySelected && starOwner[mouseOverStar] == playerColor) {
            starSelected[mouseOverStar] = true;
          }

        } else {

          deselectAll();
        }
      }
    } else {
      gameState = 0;
    }
  }

  private void deselectAll() {
    // Deselect all
    for (int s = 0; s < NUMBER_OF_STARS; s++) {
      starSelected[s] = false;
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    int mouseX = e.getX();
    int mouseY = e.getY();
    if (isDragging) {
      isDragging = false;

      // Select stars within drag area
      int x1 = dragStartX;
      int x2 = mouseX;
      if (x2 < x1) {
        x1 = mouseX;
        x2 = dragStartX;
      }
      int y1 = dragStartY;
      int y2 = mouseY;
      if (y2 < y1) {
        y1 = mouseY;
        y2 = dragStartY;
      }

      // Dragged minimum distance?
      if (Point2D.distance(x1, y1, x2, y2) >= MIN_DRAG_DISTANCE) {
        // Select stars within drag area
        for (int s = 0; s < NUMBER_OF_STARS; s++) {
          starSelected[s] = (starX[s] > x1 && starX[s] < x2 && starY[s] > y1 && starY[s] < y2)
              && starOwner[s] == playerColor;
        }
      }
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();

    if (!isDragging) {
      isDragging = true;
      dragStartX = e.getX();
      dragStartY = e.getY();
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();

    if (keyCode == KeyEvent.VK_H) {
      historyVisible = !historyVisible;
    }

    // Handle collection point setting and starbase construction
    if (keyCode == KeyEvent.VK_S) {
      // Build starbase, if possible
      if (mouseOverStar != -1 && starOwner[mouseOverStar] == playerColor
          && playerBases < maxBases && starProduction[mouseOverStar] >= 0) {
        // We can build a starbase here
        starBase[mouseOverStar] = 0;
        starProduction[mouseOverStar] = 0;
      }
    }

    if (keyCode == KeyEvent.VK_C && mouseOverStar != -1) {
      // Toggle collection point for this star
      starCollection[mouseOverStar] = !starCollection[mouseOverStar];
    }
  }

  public static void main(String[] args) throws Exception {
    new GalacticConquest().start();
  }
}
