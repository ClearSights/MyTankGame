package TankGame_v2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * Created by Insight on 2016-12-12.
 */
public class TankGame {
    public static void main(String[] args) {
        new MainFrame().showUp();
    }
}

class MainFrame extends JFrame {
    public static final int width = 800;
    public static final int height = 600;

    private MainPanel mainPanel;

    public MainFrame() {
        mainPanel = new MainPanel();
        this.add(mainPanel);
        this.addKeyListener(mainPanel);
    }

    public void showUp() {
        this.setSize(width, height);
        this.setResizable(false);
        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);

        while (true) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mainPanel.repaint();
        }
    }
}

class MainPanel extends JPanel implements KeyListener {
    private HeroTank heroTank;
    private Vector<EnemyTank> enemyTanks;   // thread safe
    private int enemyNum = 3;

    public MainPanel() {
        // initialization
        heroTank = new HeroTank(400, 550);

        enemyTanks = new Vector<>();
        for (int i = 0; i < enemyNum; i++) {
            EnemyTank et = new EnemyTank(200 + 200 * i, 20);
            enemyTanks.add(et);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // background settings
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);

        // draw tanks
        drawTank(heroTank, g);
        for (int i = 0; i < enemyTanks.size(); i++) {
            drawTank(enemyTanks.get(i), g);
        }

        // draw bomb
        if (heroTank.bomb != null) {
            g.setColor(heroTank.bomb.getColor());
            g.drawRect(heroTank.bomb.getX(), heroTank.bomb.getY(), 1, 1);
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
                g.fill3DRect(x - 10, y - 5, 20, 10, false); // center rect
                g.fill3DRect(x - 15, y - 10, 30, 5, false); // upper rect
                g.fill3DRect(x - 15, y + 5, 30, 5, false);  // lower rect
                g.fillOval(x - 5, y - 5, 10, 10);   // circle
                g.drawLine(x, y, x - 15, y);    // line
                break;
            case Tank.DIR_RIGHT:
                g.fill3DRect(x - 10, y - 5, 20, 10, false); // center rect
                g.fill3DRect(x - 15, y - 10, 30, 5, false); // upper rect
                g.fill3DRect(x - 15, y + 5, 30, 5, false);  // lower rect
                g.fillOval(x - 5, y - 5, 10, 10);   // circle
                g.drawLine(x, y, x + 15, y);    // line
                break;
            case Tank.DIR_UP:
                g.fill3DRect(x - 5, y - 10, 10, 20, false); // center rect
                g.fill3DRect(x - 10, y - 15, 5, 30, false); // left rect
                g.fill3DRect(x + 5, y - 15, 5, 30, false);  // right rect
                g.fillOval(x - 5, y - 5, 10, 10);   // circle
                g.drawLine(x, y, x, y - 15);    // line
                break;
            case Tank.DIR_DOWN:
                g.fill3DRect(x - 5, y - 10, 10, 20, false); // center rect
                g.fill3DRect(x - 10, y - 15, 5, 30, false); // left rect
                g.fill3DRect(x + 5, y - 15, 5, 30, false);  // right rect
                g.fillOval(x - 5, y - 5, 10, 10);   // circle
                g.drawLine(x, y, x, y + 15);    // line
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    // detect movement
    @Override
    public void keyPressed(KeyEvent e) {
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

        if (e.getKeyCode() == KeyEvent.VK_J) {
            heroTank.shoot();

            // let bomb fly
            Thread bombThread = new Thread(heroTank.bomb);
            bombThread.start();
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

