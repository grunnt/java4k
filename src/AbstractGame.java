import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

/**
 * Base class for making simple games using plain Java / Swing. This is a replacement for the
 * Applet originally used for the Java4K games.
 *
 * Call start() to run the game, or nothing will happen.
 */
public abstract class AbstractGame extends JFrame implements MouseListener, MouseMotionListener,
    KeyListener {

  public static final double UPDATE_DURATION_S = 1.0 / 60.0;

  private final BufferStrategy bufferStrategy;
  private final BufferedImage screenImage;
  private final Graphics2D screenGraphics;
  private double frameTime = 0;
  private double gameTime = 0;

  public AbstractGame(String title, int width, int height, boolean renderSmooth) {
    // Setup window
    setTitle(title);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setIgnoreRepaint(true);
    setResizable(false);

    // Setup our canvas which we will render on
    Canvas canvas = new Canvas();
    canvas.setPreferredSize(new Dimension(width, height));
    canvas.addMouseListener(this);
    canvas.addMouseMotionListener(this);
    canvas.addKeyListener(this);

    // Show the window
    add(canvas);
    pack();
    setVisible(true);

    // Optimize rendering
    canvas.setIgnoreRepaint(true);
    canvas.createBufferStrategy(2);
    bufferStrategy = canvas.getBufferStrategy();
    screenImage = GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .getDefaultScreenDevice()
        .getDefaultConfiguration()
        .createCompatibleImage(800, 600);
    screenGraphics = screenImage.createGraphics();
    if (renderSmooth) {
      screenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      screenGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY);
    }
  }

  protected abstract void setup() throws Exception;

  protected abstract void update(float stepS, float frameTimeS, float gameTimeS);

  protected abstract void render(Graphics2D g, float frameTimeS, float gameTimeS);

  /**
   * Start the game. This will first call "setup" and then repeatedly call
   * "update" and "render" in an infinite loop, until the window is closed.
   *
   * @throws Exception
   */
  public final void start() throws Exception {
    setup();

    new Thread(() -> {
      // Game loop based on "Fix Your Timestep!" by Gaffer On Games
      double accumulator = 0;
      double currentTime = now();
      while (true) {
        double newTime = now();
        frameTime = newTime - currentTime;
        gameTime += frameTime;
        currentTime = newTime;
        accumulator += frameTime;

        // Do updates in fixed time steps
        while (accumulator >= UPDATE_DURATION_S) {
          update((float) UPDATE_DURATION_S, (float) frameTime, (float) gameTime);
          accumulator -= UPDATE_DURATION_S;
        }

        // An render everything in one go
        render(screenGraphics, (float) frameTime, (float) gameTime);

        // Now swap the buffers to the result becomes visible
        displayRender();
      }
    }).start();
  }

  private double now() {
    return (double)System.nanoTime() / 1_000_000_000;
  }

  private void displayRender() {
    Graphics graphics = bufferStrategy.getDrawGraphics();
    try {
      graphics.drawImage(screenImage, 0, 0, null);
      if (!bufferStrategy.contentsLost()) {
        bufferStrategy.show();
      }
    } finally {
      graphics.dispose();
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {
    // Do nothing, can be overridden
  }

  @Override
  public void keyPressed(KeyEvent e) {
    // Do nothing, can be overridden
  }

  @Override
  public void keyReleased(KeyEvent e) {
    // Do nothing, can be overridden
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    // Do nothing, can be overridden
  }

  @Override
  public void mousePressed(MouseEvent e) {
    // Do nothing, can be overridden
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    // Do nothing, can be overridden
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    // Do nothing, can be overridden
  }

  @Override
  public void mouseExited(MouseEvent e) {
    // Do nothing, can be overridden
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    // Do nothing, can be overridden
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    // Do nothing, can be overridden
  }
}
