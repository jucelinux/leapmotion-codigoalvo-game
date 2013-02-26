package codigoalvo.leap.alvogame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import codigoalvo.gameobject.*;
import com.leapmotion.leap.Controller;

public class Board extends Frame implements ActionListener {

	private Controller leapController;
	private LeapListener leapListener;
	private Timer drawTimer;
	private Position position = new Position();
	private int frameRate = 50;
	private int cursorSize = 20;
	private int score = 0;
	private int grabedObjects = 0;
	private Vector<BaseGameObject> gameObjects = new Vector<>();
	private BaseGameObject lastObject;

	public Board() {
		addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent arg0) {}

			public void keyReleased(KeyEvent arg0) {}

			public void keyPressed(KeyEvent key) {
				if (key.getKeyCode() == KeyEvent.VK_ESCAPE) {
					LeapAlvoGame.restoreOriginalDisplayMode();
					System.exit(0);
				}
			}
		});
		for (int i = 0; i < 10; i++) {
			gameObjects.add(new SolidGameObject((int)(Math.random() * 1000), (int)(Math.random() * 500), 30, 30));
		}
		setBackground(Color.BLACK);
		setUndecorated(true);
		drawTimer = new Timer(frameRate, this);
		drawTimer.start();

		leapListener = new LeapListener(position);
		leapController = new Controller();

		leapController.addListener(leapListener);
	}

	public void paint(Graphics g) {
		super.paint(g);
		desenha2(g);
		g.dispose();
	}

	private void checaColisoes() {
		Rectangle curRect = new Rectangle((int)(position.getX() - cursorSize / 2), (int)(position.getY() - cursorSize / 2), cursorSize, cursorSize);

		synchronized (gameObjects) {
			
			if (lastObject == null) {
				for (BaseGameObject gameObj : gameObjects) {
					if (gameObj.getBounds().intersects(curRect)) {
						lastObject = gameObj;
						lastObject.stop();
						grabedObjects = 1;;
						gameObjects.remove(gameObj);
						break;
					}
				}
			} else {
				for (BaseGameObject gameObj : gameObjects) {
					if (gameObj.getBounds().intersects(lastObject.getBounds())) {
						if (((TipoObjeto)gameObj.getTipo()) == ((TipoObjeto)lastObject.getTipo())) {
							grabedObjects++;
							lastObject.setVisible(false);
							lastObject = gameObj;
							lastObject.stop();
							gameObjects.remove(gameObj);
						} else {
							grabedObjects *= -1;
							releaseObject();
						}
						break;
					}
				}
			}
		}
	}
	
	protected void desenhaCursor(Graphics2D g2d) {
		if (position.getZ() < 0) {
			if (lastObject == null) {
				g2d.setStroke(new BasicStroke(2));
				g2d.setColor(Color.GREEN);
				g2d.drawOval(position.getX(), position.getY(), cursorSize, cursorSize);
			} else {
				lastObject.setX(position.getX()-lastObject.getWidth());
				lastObject.setY(position.getY()-lastObject.getHeight());
				lastObject.drawObject(g2d);
			}
		} else {
			releaseObject();
		}
	}
	
	public void releaseObject() {
		if (lastObject != null) {
			lastObject.setVisible(false);
			score += (100 * (grabedObjects)); //TODO: Colocar um multiplicador
			grabedObjects = 0;
			lastObject = null;
		}
	}

	private void desenhaScore(Graphics2D g2d) {
		String msg = "codigoalvo";
		Font small = new Font("Helvetica", Font.BOLD, 24);
		Font big = new Font("Courier", Font.BOLD, 72);
		FontMetrics metr = this.getFontMetrics(small);
		g2d.setColor(Color.white);
		g2d.setFont(small);
		g2d.drawString(msg, LeapAlvoGame.currentDisplayMode.getWidth() - 50 - metr.stringWidth(msg), 40);
		g2d.setFont(big);
		g2d.drawString(String.valueOf(score), 30, 50 + 30);

	}

	private void desenhaObjetos(Graphics2D g2d) {
		synchronized (gameObjects) {
			for (BaseGameObject gameObj : gameObjects) {
				gameObj.move();
				if (gameObj.getX() < 0 || (gameObj.getX() + gameObj.getWidth()) > LeapAlvoGame.currentDisplayMode.getWidth())
					gameObj.setSpeedX(gameObj.getSpeedX() * -1);

				if (gameObj.getY() < 0 || (gameObj.getY() + gameObj.getHeight()) > LeapAlvoGame.currentDisplayMode.getHeight())
					gameObj.setSpeedY(gameObj.getSpeedY() * -1);
				gameObj.drawObject(g2d);
				// System.out.println(gameObj.toString());
			}
		}
	}

	protected void desenha2(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHints(rh);
		desenhaObjetos(g2d);
		desenhaCursor(g2d);
		desenhaScore(g2d);
		// Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().createImage(""), new Point(), null);
		Toolkit.getDefaultToolkit().sync();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		checaColisoes();
		repaint();
		newObjects();
	}

	private void newObjects() {
		int minObjs = (int)(Math.random()*5)+1;
		if (gameObjects.size() < minObjs) {
			int qtdNewObjs = (int)(Math.random()*9)+1;
			for (int i = 0; i < qtdNewObjs; i++) {
				gameObjects.add(new SolidGameObject((int)(Math.random() * 1000), (int)(Math.random() * 500), 30, 30));
			}
		}
    }

}