package TankGame_v1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by Insight on 2016-12-12.
 */
public class drawTank {
    public static void main(String[] args) {
        new MainFrame();
    }
}

class MainFrame extends JFrame {
    private int width = 800;
    private int height = 600;

    public MainFrame() {
        MainPanel myPanel = new MainPanel();
        this.add(myPanel);
        this.addKeyListener(myPanel);

        this.setSize(width, height);
        this.setResizable(false);
        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}

class MainPanel extends JPanel implements KeyListener {
    Hero hero;
    Enemy enemy;

    // construction method
    public MainPanel() {
        hero = new Hero(100, 100);
        enemy = new Enemy(150, 100);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);

        drawTank(hero, g);
    }

    // draw a tank at fixed position
    private void drawTank(Tank tank, Graphics g) {
        int x = tank.getX();
        int y = tank.getY();
        int direction = tank.getDirection();

        switch (direction) {
            case 0:
                g.setColor(tank.getColor());
                g.fill3DRect(x, y, 5, 30, false);
                g.fill3DRect(x + 15, y, 5, 30, false);
                g.fill3DRect(x + 5, y + 5, 10, 20, false);
                g.fillOval(x + 5, y + 10, 10, 10);
                g.drawLine(x + 10, y + 15, x + 10, y);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    // detect movement
    @Override
    public void keyPressed(KeyEvent e) {
        int step = hero.getStep();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                hero.setY(hero.getY() - step);
                break;
            case KeyEvent.VK_DOWN:
                hero.setY(hero.getY() + step);
                break;
            case KeyEvent.VK_LEFT:
                hero.setX(hero.getX() - step);
                break;
            case KeyEvent.VK_RIGHT:
                hero.setX(hero.getX() + step);
                break;
        }

        // update position
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

class Tank {
    private int x = 0;
    private int y = 0;
    private int step = 2;
    private int direction = 0;
    private Color color;

    public Tank(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Tank(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}

class Hero extends Tank {
    public Hero(int x, int y) {
        super(x, y);

        setColor(Color.CYAN);
    }
}

class Enemy extends Tank {
    public Enemy(int x, int y) {
        super(x, y);

        setColor(Color.YELLOW);
    }
}