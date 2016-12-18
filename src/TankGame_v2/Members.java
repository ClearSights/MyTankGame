package TankGame_v2;

import java.awt.*;

/**
 * Created by Insight on 2016-12-15.
 */
class Tank {
    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;

    private int x = 0;      // x of center
    private int y = 0;      // y of center
    private int speed = 5;
    private int direction;
    private Color color;

    public Bomb bomb = null;

    public Tank(int x, int y) {
        // set tank defaults
        this.x = x;
        this.y = y;
        direction = DIR_UP;     // default direction
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

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
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

    public void moveUp() {
        y -= speed;
    }

    public void moveDown() {
        y += speed;
    }

    public void moveLeft() {
        x -= speed;
    }

    public void moveRight() {
        x += speed;
    }

    public void shoot() {
        // get bomb initial position
        int bombX = 0;
        int bombY = 0;
        switch (direction) {
            case DIR_UP:
                bombX = x;
                bombY = y - 15;
                break;
            case DIR_DOWN:
                bombX = x;
                bombY = y + 15;
                break;
            case DIR_LEFT:
                bombX = x - 15;
                bombY = y;
                break;
            case DIR_RIGHT:
                bombX = x + 15;
                bombY = y;
                break;
        }

        // get bomb instance
        bomb = new Bomb(bombX, bombY, this.direction, this.color);
        bomb.isAlive = true;
    }
}

class HeroTank extends Tank {
    public HeroTank(int x, int y) {
        super(x, y);

        setColor(Color.CYAN);
    }
}

class EnemyTank extends Tank {
    public EnemyTank(int x, int y) {
        super(x, y);

        setColor(Color.YELLOW);
        setDirection(Tank.DIR_DOWN);
    }
}

class Bomb implements Runnable {
    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;

    private int x;
    private int y;
    private int speed = 8;
    private Color color;

    private int direction;

    public boolean isAlive = false;

    public Bomb(int x, int y, int direction, Color color) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDirection() {
        return direction;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // judge whether alive
            if (x < 0 | x > MainFrame.width | y < 0 | y > MainFrame.height) {
                isAlive = false;
                break;
            }

            // if alive, fly
            if (isAlive) {
                switch (direction) {
                    case DIR_UP:
                        y -= speed;
                        break;
                    case DIR_DOWN:
                        y += speed;
                        break;
                    case DIR_LEFT:
                        x -= speed;
                        break;
                    case DIR_RIGHT:
                        x += speed;
                        break;
                }
            }
        }
    }
}