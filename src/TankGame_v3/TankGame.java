package TankGame_v3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * Created by Insight on 2016-12-17.
 * function:
 * 1. draw hero tank and enemy tanks
 * 2. hero tank can shoot multiple bombs
 * 3. when bombs hit enemy tanks, they disappear together
 * 4. enemy tanks can move randomly within the boundary
 */
public class TankGame {
    public static void main(String[] args) {
        new MainFrame().showUp();
    }
}

class MainFrame extends JFrame {
    public static final int W_WIDTH = 600;
    public static final int W_HEIGHT = 450;

    private MainPanel mainPanel;
    private Thread panelThread;

    public MainFrame() {
        mainPanel = new MainPanel();
        this.add(mainPanel);
        this.addKeyListener(mainPanel);

        panelThread = new Thread(mainPanel);
        panelThread.start();
    }

    public void showUp() {
        this.setSize(W_WIDTH, W_HEIGHT);
        this.setResizable(false);
        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}

class MainPanel extends JPanel implements KeyListener, Runnable {
    private HeroTank heroTank;
    private Vector<EnemyTank> enemyTanks;
    private int enemyNum = 3;

    public MainPanel() {
        // initialize hero tank
        heroTank = new HeroTank(MainFrame.W_WIDTH /2, MainFrame.W_HEIGHT - 2 * Tank.WIDTH);

        // initialize enemy tanks
        enemyTanks = new Vector<>();
        for (int i = 0; i < enemyNum; i++) {
            EnemyTank et = new EnemyTank(MainFrame.W_WIDTH / (2 * enemyNum)
                    + i * MainFrame.W_WIDTH / enemyNum, Tank.HEIGHT / 2);
            enemyTanks.add(et);

            Thread enemyThread = new Thread(et);
            enemyThread.start();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // background settings
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, MainFrame.W_WIDTH, MainFrame.W_HEIGHT);

        // draw tanks
        drawTank(heroTank, g);
        for (int i = 0; i < enemyTanks.size(); i++) {
            EnemyTank enemyTank = enemyTanks.get(i);
            if (enemyTank.isAlive) {
                drawTank(enemyTank, g);
            }
        }

        // draw bombs of heroTank
        g.setColor(heroTank.getColor());
        for (int i = 0; i < heroTank.bombs.size(); i++) {
            Bomb thisBomb = heroTank.bombs.get(i);
            if (thisBomb.isAlive) {
                g.drawRect(thisBomb.getX(), thisBomb.getY(), 1, 1);
            }
        }
    }

    /* draw a tank at fixed position */
    private void drawTank(Tank tank, Graphics g) {
        int x = tank.getX();
        int y = tank.getY();
        g.setColor(tank.getColor());

        int direction = tank.getDirection();
        switch (direction) {
            case Tank.DIR_LEFT:
                g.fill3DRect(x - Tank.WIDTH/2, y - Tank.WIDTH/4, Tank.WIDTH, Tank.WIDTH/2, false);      // center rect
                g.fillOval(x - Tank.WIDTH/4, y - Tank.WIDTH/4, Tank.WIDTH/2, Tank.WIDTH/2);             // oval
                g.fill3DRect(x - Tank.HEIGHT/2, y - Tank.WIDTH/2, Tank.HEIGHT, Tank.WIDTH/4, false);    // upper rect
                g.fill3DRect(x - Tank.HEIGHT/2, y + Tank.WIDTH/4, Tank.HEIGHT, Tank.WIDTH/4, false);    // lower rect
                g.drawLine(x, y, x - Tank.HEIGHT/2, y);
                break;
            case Tank.DIR_RIGHT:
                g.fill3DRect(x - Tank.WIDTH/2, y - Tank.WIDTH/4, Tank.WIDTH, Tank.WIDTH/2, false);
                g.fillOval(x - Tank.WIDTH/4, y - Tank.WIDTH/4, Tank.WIDTH/2, Tank.WIDTH/2);
                g.fill3DRect(x - Tank.HEIGHT/2, y - Tank.WIDTH/2, Tank.HEIGHT, Tank.WIDTH/4, false);
                g.fill3DRect(x - Tank.HEIGHT/2, y + Tank.WIDTH/4, Tank.HEIGHT, Tank.WIDTH/4, false);
                g.drawLine(x, y, x + Tank.HEIGHT/2, y);
                break;
            case Tank.DIR_UP:
                g.fill3DRect(x - Tank.WIDTH/4, y - Tank.WIDTH/2, Tank.WIDTH/2, Tank.WIDTH, false);      // center rect
                g.fillOval(x - Tank.WIDTH/4, y - Tank.WIDTH/4, Tank.WIDTH/2, Tank.WIDTH/2);
                g.fill3DRect(x - Tank.WIDTH/2, y - Tank.HEIGHT/2, Tank.WIDTH/4, Tank.HEIGHT, false);    // left rect
                g.fill3DRect(x + Tank.WIDTH/4, y - Tank.HEIGHT/2, Tank.WIDTH/4, Tank.HEIGHT, false);    // right rect
                // oval
                g.drawLine(x, y, x, y - Tank.HEIGHT/2);
                break;
            case Tank.DIR_DOWN:
                g.fill3DRect(x - Tank.WIDTH/4, y - Tank.WIDTH/2, Tank.WIDTH/2, Tank.WIDTH, false);
                g.fillOval(x - Tank.WIDTH/4, y - Tank.WIDTH/4, Tank.WIDTH/2, Tank.WIDTH/2);
                g.fill3DRect(x - Tank.WIDTH/2, y - Tank.HEIGHT/2, Tank.WIDTH/4, Tank.HEIGHT, false);
                g.fill3DRect(x + Tank.WIDTH/4, y - Tank.HEIGHT/2, Tank.WIDTH/4, Tank.HEIGHT, false);
                g.drawLine(x, y, x, y + Tank.HEIGHT/2);
                break;
        }
    }

    // judge if an enemy is hit by a bomb
    private void updateLivingInfo(Tank enemyTank, Bomb bomb) {
        switch (enemyTank.getDirection()) {
            case Tank.DIR_UP:
            case Tank.DIR_DOWN:
                if (bomb.getX()-enemyTank.getX() < Tank.WIDTH/2
                        && bomb.getX()-enemyTank.getX() > -Tank.WIDTH/2
                        && bomb.getY()-enemyTank.getY() < Tank.HEIGHT/2
                        && bomb.getY()-enemyTank.getY() > -Tank.HEIGHT/2) {
                    bomb.isAlive = false;
                    enemyTank.isAlive = false;
                }
                break;
            case Tank.DIR_LEFT:
            case Tank.DIR_RIGHT:
                if (bomb.getX()-enemyTank.getX() < Tank.HEIGHT/2
                        && bomb.getX()-enemyTank.getX() > -Tank.HEIGHT/2
                        && bomb.getY()-enemyTank.getY() < Tank.WIDTH/2
                        && bomb.getY()-enemyTank.getY() > -Tank.WIDTH/2) {
                    bomb.isAlive = false;
                    enemyTank.isAlive = false;
                }
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    // detect movement
    @Override
    public void keyPressed(KeyEvent e) {
        // monitor direction change
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                heroTank.setDirection(Tank.DIR_UP);
                heroTank.moveUp();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                heroTank.setDirection(Tank.DIR_DOWN);
                heroTank.moveDown();
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                heroTank.setDirection(Tank.DIR_LEFT);
                heroTank.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                heroTank.setDirection(Tank.DIR_RIGHT);
                heroTank.moveRight();
                break;
        }

        // press J, shoot bomb
        if (e.getKeyCode() == KeyEvent.VK_J) {
            if (heroTank.bombs.size() < Tank.MAX_BOMB_NUM) {
                Bomb thisBomb = heroTank.shoot();
                Thread bombThread = new Thread(thisBomb);
                bombThread.start();
            }
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);  // display update interval
            } catch (Exception e) {
                e.printStackTrace();
            }

            // update living info of bombs and enemy tanks
            for (int i = 0; i < enemyTanks.size(); i++) {
                EnemyTank enemyTank = enemyTanks.get(i);

                if (enemyTank.isAlive) {
                    for (int j = 0; j < heroTank.bombs.size(); j++) {
                        Bomb thisBomb = heroTank.bombs.get(j);

                        if (thisBomb.isAlive) {
                            updateLivingInfo(enemyTank, thisBomb);
                        }
                    }
                }
            }

            // find and remove those dead
            for (int i = 0; i < enemyTanks.size(); i++) {
                EnemyTank et = enemyTanks.get(i);
                if (!et.isAlive) {
                    enemyTanks.remove(et);
                }
            }
            for (int i = 0; i < heroTank.bombs.size(); i++) {
                Bomb thisBomb = heroTank.bombs.get(i);
                if (!thisBomb.isAlive) {
                    heroTank.bombs.remove(thisBomb);
                }
            }

            repaint();
        }
    }
}

