package TankGame_v3;

import java.awt.*;
import java.util.Vector;

/**
 * Created by Insight on 2016-12-15.
 */
class Tank {
    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;

    public static final int MAX_BOMB_NUM = 5;
    public static final int WIDTH = 24;
    public static final int HEIGHT = 36;

    int x = 0;      // x of center
    int y = 0;      // y of center
    int speed = 2;
    int direction;
    Color color;

    public boolean isAlive = true;
    public Vector<Bomb> bombs = new Vector<>();

    public Tank(int x, int y) {
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

    public Bomb shoot() {
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

        // add bomb instance
        Bomb newBomb = new Bomb(bombX, bombY, this.direction, this.color);
        newBomb.isAlive = true;
        bombs.add(newBomb);
        return newBomb;
    }
}

class HeroTank extends Tank {
    public HeroTank(int x, int y) {
        super(x, y);

        setColor(Color.CYAN);
    }
}

class EnemyTank extends Tank implements Runnable {
    private int MARCH_COUNT = 5;    // number of continuous march sequence
    private int MARCH_INTERVAL = 150;    // interval of consequent marches

    public EnemyTank(int x, int y) {
        super(x, y);

        setColor(Color.YELLOW);
        setDirection(Tank.DIR_DOWN);
    }

    @Override
    public void run() {
        while (isAlive) {
            // move
            switch (getDirection()) {
                case Tank.DIR_UP:
                    for (int i = 0; i < MARCH_COUNT; i++) {
                        if (y >= 15) {
                            y -= speed;
                        }
                        try {
                            Thread.sleep(MARCH_INTERVAL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case Tank.DIR_DOWN:
                    for (int i = 0; i < MARCH_COUNT; i++) {
                        if (y + 15 < MainFrame.W_HEIGHT) {
                            y += speed;
                        }
                        try {
                            Thread.sleep(MARCH_INTERVAL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case Tank.DIR_LEFT:
                    for (int i = 0; i < MARCH_COUNT; i++) {
                        if (x > 15) {
                            x -= speed;
                        }
                        try {
                            Thread.sleep(MARCH_INTERVAL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case Tank.DIR_RIGHT:
                    for (int i = 0; i < MARCH_COUNT; i++) {
                        if (x + 15 < MainFrame.W_WIDTH) {
                            x += speed;
                        }
                        try {
                            Thread.sleep(MARCH_INTERVAL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }

            // change direction
            direction = (int)(4 * Math.random());

            // shoot bomb
            if (bombs.size() < MAX_BOMB_NUM) {
                Bomb bomb = shoot();
                Thread bombThread = new Thread(bomb);
                bombThread.start();
            }
        }
    }
}

class Bomb implements Runnable {
    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;

    private int x;
    private int y;
    private int speed = 5;
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
            if (x < 0 | x > MainFrame.W_WIDTH | y < 0 | y > MainFrame.W_HEIGHT) {
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