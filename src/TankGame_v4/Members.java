package TankGame_v4;

import java.awt.*;
import java.util.Vector;

/**
 * Created by Insight on 2017-04-02.
 */
class Tank {
    public static final int DIR_POSITIVE = 1;   // right
    public static final int DIR_NEGATIVE = -1;  // left
    public static final int DIR_V = 2;          // vertical
    public static final int DIR_H = -2;         // horizontal

    public static final int MAX_BOMB_NUM = 5;
    public static final int WIDTH = 24;
    public static final int HEIGHT = 36;

    int x = 0;      // x of center
    int y = 0;      // y of center
    int speed = 2;
    int dirVH;
    int dirPosNeg;
    Color color;

    public boolean isAlive = true;
    public Vector<Bomb> bombs = new Vector<>();

    public Tank(int x, int y) {
        this.x = x;
        this.y = y;
        dirVH = DIR_V;
        dirPosNeg = DIR_NEGATIVE;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public int getDirVH() {
        return dirVH;
    }

    public int getDirPosNeg() {
        return dirPosNeg;
    }

    public void setDirVH(int dirVH) {
        this.dirVH = dirVH;
    }

    public void setDirPosNeg(int dirPosNeg) {
        this.dirPosNeg = dirPosNeg;
    }

    // move one step
    public void step() {
        if (dirVH == DIR_V) {
            y += speed * dirPosNeg;
        } else if (dirVH == DIR_H) {
            x += speed * dirPosNeg;
        }
    }

    public Bomb shoot() {
        // get bomb initial position
        int bombX = 0;
        int bombY = 0;
        switch (dirVH) {
            case DIR_V:
                bombX = x;
                bombY = y + dirPosNeg*HEIGHT/2;
                break;
            case DIR_H:
                bombX = x + dirPosNeg*HEIGHT/2;
                bombY = y;
                break;
        }

        // add bomb instance
        Bomb newBomb = new Bomb(bombX, bombY, this.dirVH, this.dirPosNeg, this.color);
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
    private int CONTINUOUS_STEP_NUM = 5;    // number of continuous march sequence
    private int STEP_INTERVAL = 150;        // interval of consequent marches
    private Vector<EnemyTank> panelEnemyTanks;

    public EnemyTank(int x, int y) {
        super(x, y);

        setColor(Color.YELLOW);
        // initial direction -- down
        setDirVH(DIR_V);
        setDirPosNeg(DIR_POSITIVE);
    }

    public void passEnemyTanks(Vector<EnemyTank> ets) {
        panelEnemyTanks = ets;
    }

    // check collision with other enemyTanks
    public boolean detectCollision() {
        if (panelEnemyTanks != null) {
            for (EnemyTank et : panelEnemyTanks) {
                if (et != this) {
                    if (et.dirVH == DIR_V && this.dirVH == DIR_V) {
                        if (Math.abs(et.x-this.x) <= Tank.WIDTH && Math.abs(et.y - this.y) <= Tank.HEIGHT) {
                            return true;
                        }
                    } else if (et.dirVH == DIR_H && this.dirVH == DIR_H) {
                        if (Math.abs(et.x - this.x) <= Tank.HEIGHT && Math.abs(et.y - this.y) <= Tank.WIDTH) {
                            return true;
                        }
                    } else if (et.dirVH * this.dirVH < 0) {
                        // if their directions are orthogonal
                        int spacing = Tank.WIDTH/2 + Tank.HEIGHT/2;
                        if (Math.abs(et.x - this.x) <= spacing && Math.abs(et.y - this.y) <= spacing) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void run() {
        while (isAlive) {
            switch (dirVH) {
                case DIR_V:
                    for (int i = 0; i < CONTINUOUS_STEP_NUM; i++) {
                        // up or down
                        if ((dirPosNeg==DIR_NEGATIVE && y>=HEIGHT/2+speed)
                                || (dirPosNeg==DIR_POSITIVE && y+HEIGHT/2+speed<=MainFrame.W_HEIGHT)) {
                            if (!detectCollision()) {
                                y += speed * dirPosNeg;
                            }
                        }
                        try {
                            Thread.sleep(STEP_INTERVAL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case DIR_H:
                    for (int i = 0; i < CONTINUOUS_STEP_NUM; i++) {
                        // left or right
                        if ((dirPosNeg==DIR_NEGATIVE && x>=WIDTH/2+speed)    // up
                                || (dirPosNeg==DIR_POSITIVE && x+WIDTH/2+speed<=MainFrame.W_WIDTH)) {
                            if (!detectCollision()) {
                                x += speed * dirPosNeg;
                            }
                        }
                        try {
                            Thread.sleep(STEP_INTERVAL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }

            // next direction
            if (Math.random() >= 0.5) {
                dirPosNeg = DIR_POSITIVE;
            } else {
                dirPosNeg = DIR_NEGATIVE;
            }
            if (Math.random() >= 0.5) {
                dirVH = DIR_V;
            } else {
                dirVH = DIR_H;
            }

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
    public static final int DIR_POSITIVE = 1;   // right
    public static final int DIR_NEGATIVE = -1;  // left
    public static final int DIR_V = 2;          // vertical
    public static final int DIR_H = -2;         // horizontal

    private int x;
    private int y;

    private int speed = 5;
    private Color color;
    private int dirVH;
    private int dirPosNeg;

    public boolean isAlive = false;

    public Bomb(int x, int y, int dirVH, int dirPosNeg, Color color) {
        this.x = x;
        this.y = y;
        this.dirVH = dirVH;
        this.dirPosNeg = dirPosNeg;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDirVH() {
        return dirVH;
    }

    public int getDirPosNeg() {
        return dirPosNeg;
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
            } else {
                switch (dirVH) {
                    case DIR_V:
                        y += speed * dirPosNeg;
                        break;
                    case DIR_H:
                        x += speed * dirPosNeg;
                        break;
                }
            }
        }
    }
}