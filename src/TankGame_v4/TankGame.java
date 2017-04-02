package TankGame_v4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * Created by Insight on 2017-04-02.
 * function:
 * 1. draw hero tank and enemy tanks
 * 2. hero tank can shoot multiple bombs
 * 3. when bombs hit enemy tanks, they disappear together
 * 4. enemy tanks can move randomly within the boundary
 * 5. enemy tanks can shoot bombs, heroTank hit by enemyBomb will die
 * 6. enemy tanks can avoid collision with each other
 */
public class TankGame {
    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.showUp();
    }
}

class MainFrame extends JFrame {
    public static final int W_WIDTH = 800;
    public static final int W_HEIGHT = 600;

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
    private int enemyNum = 10;

    public MainPanel() {
        // initialize hero tank
        heroTank = new HeroTank(MainFrame.W_WIDTH /2, MainFrame.W_HEIGHT - 2 * Tank.WIDTH);

        // initialize enemy tanks
        enemyTanks = new Vector<>();
        for (int i = 0; i < enemyNum; i++) {
            int et_x = MainFrame.W_WIDTH/(2*enemyNum) + i*MainFrame.W_WIDTH/enemyNum;
            int et_y = Tank.HEIGHT / 2;
            EnemyTank et = new EnemyTank(et_x, et_y);
            enemyTanks.add(et);
        }

        for (EnemyTank et : enemyTanks) {
            et.passEnemyTanks(enemyTanks);

            Thread enemyThread = new Thread(et);
            enemyThread.start();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, MainFrame.W_WIDTH, MainFrame.W_HEIGHT);

        // draw living tanks
        if (heroTank.isAlive) {
            drawTank(heroTank, g);
        }
        for (int i = 0; i < enemyTanks.size(); i++) {
            EnemyTank enemyTank = enemyTanks.get(i);
            if (enemyTank.isAlive) {
                drawTank(enemyTank, g);
            }
        }

        // draw living bombs of heroTank
        g.setColor(heroTank.getColor());
        for (int i = 0; i < heroTank.bombs.size(); i++) {
            Bomb thisBomb = heroTank.bombs.get(i);
            if (thisBomb.isAlive) {
                g.drawRect(thisBomb.getX(), thisBomb.getY(), 1, 1);
            }
        }

        // draw living bombs of enemy tank
        for (int i = 0; i < enemyTanks.size(); i++) {
            EnemyTank enemyTank = enemyTanks.get(i);
            g.setColor(enemyTank.getColor());
            for (int j = 0; j < enemyTank.bombs.size(); j++) {
                Bomb thisBomb = enemyTank.bombs.get(j);
                if (thisBomb.isAlive) {
                    g.drawRect(thisBomb.getX(), thisBomb.getY(), 1, 1);
                }
            }
        }
    }

    /* draw a tank at specific position */
    private void drawTank(Tank tank, Graphics g) {
        int x = tank.getX();
        int y = tank.getY();
        g.setColor(tank.getColor());

        int dirVH = tank.getDirVH();
        int dirPosNeg = tank.getDirPosNeg();
        switch (dirVH) {
            case Tank.DIR_V:
                g.fill3DRect(x - Tank.WIDTH/4, y - Tank.WIDTH/2, Tank.WIDTH/2, Tank.WIDTH, false);      // center rect
                g.fillOval(x - Tank.WIDTH/4, y - Tank.WIDTH/4, Tank.WIDTH/2, Tank.WIDTH/2);
                g.fill3DRect(x - Tank.WIDTH/2, y - Tank.HEIGHT/2, Tank.WIDTH/4, Tank.HEIGHT, false);    // left rect
                g.fill3DRect(x + Tank.WIDTH/4, y - Tank.HEIGHT/2, Tank.WIDTH/4, Tank.HEIGHT, false);    // right rect
                g.drawLine(x, y, x, y +  dirPosNeg*Tank.HEIGHT/2);
                break;
            case Tank.DIR_H:
                g.fill3DRect(x - Tank.WIDTH/2, y - Tank.WIDTH/4, Tank.WIDTH, Tank.WIDTH/2, false);      // center rect
                g.fillOval(x - Tank.WIDTH/4, y - Tank.WIDTH/4, Tank.WIDTH/2, Tank.WIDTH/2);             // oval
                g.fill3DRect(x - Tank.HEIGHT/2, y - Tank.WIDTH/2, Tank.HEIGHT, Tank.WIDTH/4, false);    // upper rect
                g.fill3DRect(x - Tank.HEIGHT/2, y + Tank.WIDTH/4, Tank.HEIGHT, Tank.WIDTH/4, false);    // lower rect
                g.drawLine(x, y, x + dirPosNeg*Tank.HEIGHT/2, y);
                break;
        }
    }

    // check if a tank is hit by a bomb of enemy
    private void updateHit(Tank tank, Bomb bomb) {
        if (tank.isAlive && bomb.isAlive) {
            // check if heroTank's bomb hit enemy tanks
            switch (tank.getDirVH()) {
                case Tank.DIR_V:
                    if (bomb.getX()-tank.getX() < Tank.WIDTH/2
                            && bomb.getX()-tank.getX() > -Tank.WIDTH/2
                            && bomb.getY()-tank.getY() < Tank.HEIGHT/2
                            && bomb.getY()-tank.getY() > -Tank.HEIGHT/2) {
                        bomb.isAlive = false;
                        tank.isAlive = false;
                    }
                    break;
                case Tank.DIR_H:
                    if (bomb.getX()-tank.getX() < Tank.HEIGHT/2
                            && bomb.getX()-tank.getX() > -Tank.HEIGHT/2
                            && bomb.getY()-tank.getY() < Tank.WIDTH/2
                            && bomb.getY()-tank.getY() > -Tank.WIDTH/2) {
                        bomb.isAlive = false;
                        tank.isAlive = false;
                    }
                    break;
            }
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
                heroTank.setDirVH(Tank.DIR_V);
                heroTank.setDirPosNeg(Tank.DIR_NEGATIVE);
                heroTank.step();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                heroTank.setDirVH(Tank.DIR_V);
                heroTank.setDirPosNeg(Tank.DIR_POSITIVE);
                heroTank.step();
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                heroTank.setDirVH(Tank.DIR_H);
                heroTank.setDirPosNeg(Tank.DIR_NEGATIVE);
                heroTank.step();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                heroTank.setDirVH(Tank.DIR_H);
                heroTank.setDirPosNeg(Tank.DIR_POSITIVE);
                heroTank.step();
                break;
        }

        // press J, heroTank shoot bomb
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
    // check hits, remove dead objects and update display
    public void run() {
        while (true) {
            try {
                Thread.sleep(16);       // display update interval
            } catch (Exception e) {
                e.printStackTrace();
            }

            // check if bombs of heroTank hit enemyTank
            for (int i = 0; i < enemyTanks.size(); i++) {
                EnemyTank enemyTank = enemyTanks.get(i);

                if (enemyTank.isAlive) {
                    for (int j = 0; j < heroTank.bombs.size(); j++) {
                        Bomb heroBomb = heroTank.bombs.get(j);

                        if (heroBomb.isAlive) {
                            updateHit(enemyTank, heroBomb);
                        }

                        // remove dead heroBomb and enemyTank
                        if (!heroBomb.isAlive) {
                            heroTank.bombs.remove(heroBomb);
                        }
                        if (!enemyTank.isAlive) {
                            enemyTanks.remove(enemyTank);
                        }
                    }
                }
            }

            // check if bombs of enemyTank hit heroTank
            if (heroTank.isAlive) {
                for (int i = 0; i < enemyTanks.size(); i++) {
                    EnemyTank enemyTank = enemyTanks.get(i);
                    for (int j = 0; j < enemyTank.bombs.size(); j++) {
                        Bomb enemyBomb = enemyTank.bombs.get(j);

                        if (enemyBomb.isAlive) {
                            updateHit(heroTank, enemyBomb);
                        }

                        // remove dead enemy bombs
                        if (!enemyBomb.isAlive) {
                            enemyTank.bombs.remove(enemyBomb);
                        }
                    }
                }
            }

            repaint();
        }
    }
}

