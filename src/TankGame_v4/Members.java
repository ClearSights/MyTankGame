package TankGame_v4;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.*;
import java.util.Vector;

/**
 * Created by Insight on 2017-04-02.
 */
class Tank {
    public static final int DIR_POSITIVE = 1;   // right
    public static final int DIR_NEGATIVE = -1;  // left
    public static final int DIR_V = 2;          // vertical
    public static final int DIR_H = -2;         // horizontal

    public static final int MAX_BOMB_NUM = 6;
    public static final int WIDTH = 24;
    public static final int HEIGHT = 36;

    private int x = 0;      // x of center
    private int y = 0;      // y of center
    private int speed = 3;
    private int dirVH;
    private int dirPosNeg;
    private Color color;
    private boolean isPaused = false;

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

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public int getSpeed() {
        return speed;
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

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
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
    private int STEP_INTERVAL = 80;        // interval of consequent marches
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
                    if (et.getDirVH() == DIR_V && this.getDirVH() == DIR_V) {
                        if (Math.abs(et.getX()-this.getX()) <= Tank.WIDTH
                                && Math.abs(et.getY() - this.getY()) <= Tank.HEIGHT) {
                            return true;
                        }
                    } else if (et.getDirVH() == DIR_H && this.getDirVH() == DIR_H) {
                        if (Math.abs(et.getX() - this.getX()) <= Tank.HEIGHT
                                && Math.abs(et.getY() - this.getY()) <= Tank.WIDTH) {
                            return true;
                        }
                    } else if (et.getDirVH() * this.getDirVH() < 0) {
                        // if their directions are orthogonal
                        int spacing = Tank.WIDTH/2 + Tank.HEIGHT/2;
                        if (Math.abs(et.getX() - this.getX()) <= spacing
                                && Math.abs(et.getY() - this.getY()) <= spacing) {
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
            switch (getDirVH()) {
                case DIR_V:
                    for (int i = 0; i < CONTINUOUS_STEP_NUM; i++) {
                        // up or down
                        if (!isPaused()) {
                            if ((getDirPosNeg()==DIR_POSITIVE && getY()+HEIGHT/2+getSpeed()<=MainFrame.PANEL_HEIGHT)
                                    || (getDirPosNeg()==DIR_NEGATIVE && getY()>=HEIGHT/2+getSpeed())) {
                                if (!detectCollision()) {
                                    setY(getY() + getSpeed() * getDirPosNeg());
                                }
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
                        if (!isPaused()) {
                            // left or right
                            if ((getDirPosNeg()==DIR_POSITIVE && getX()+WIDTH/2+getSpeed()<=MainFrame.PANEL_WIDTH)
                                    || (getDirPosNeg()==DIR_NEGATIVE && getX()>=WIDTH/2+getSpeed())) {
                                if (!detectCollision()) {
                                    setX(getX() + getSpeed() * getDirPosNeg());
                                }
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
            if (!isPaused()) {
                if (Math.random() >= 0.4) {
                    setDirPosNeg(DIR_POSITIVE);
                } else {
                    setDirPosNeg(DIR_NEGATIVE);
                }
                if (Math.random() >= 0.4) {
                    setDirVH(DIR_V);
                } else {
                    setDirVH(DIR_H);
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
}

class Bomb implements Runnable {
    public static final int DIR_POSITIVE = 1;   // right
    public static final int DIR_NEGATIVE = -1;  // left
    public static final int DIR_V = 2;          // vertical
    public static final int DIR_H = -2;         // horizontal
    public static final int STEP_INTERVAL = 80;

    private int x;
    private int y;
    private int speed = 5;
    private Color color;
    private int dirVH;
    private int dirPosNeg;
    private boolean isPaused = false;

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

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(STEP_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // judge whether alive
            if (x < 0 || x > MainFrame.PANEL_WIDTH || y < 0 || y > MainFrame.PANEL_HEIGHT) {
                isAlive = false;
                break;
            } else {
                if (!isPaused) {
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
}

class Recorder {
    private static int enemyNum = 5;
    private static int heroLifeNum = 3;
    private static int score = 0;

    private static FileWriter fw;
    private static BufferedWriter bw;
    private static FileReader fr;
    private static BufferedReader br;
    private static String saveFileName = "save.txt";

    private static HeroTank heroTank = null;
    private static Vector<EnemyTank> enemyTanks = null;

    public static int getEnemyNum() {
        return enemyNum;
    }

    public static int getHeroLifeNum() {
        return heroLifeNum;
    }

    public static int getScore() {
        return score;
    }

    public static void setEnemyNum(int enemyNum) {
        Recorder.enemyNum = enemyNum;
    }

    public static void setHeroLifeNum(int heroLifeNum) {
        Recorder.heroLifeNum = heroLifeNum;
    }

    public static void setScore(int score) {
        Recorder.score = score;
    }

    public static HeroTank getHeroTank() {
        return heroTank;
    }

    public static Vector<EnemyTank> getEnemyTanks() {
        return enemyTanks;
    }

    public static void enemyNumDecrease() {
        enemyNum--;
    }

    public static void heroNumDecrease() {
        heroLifeNum--;
    }

    public static void addScore() {
        score++;
    }

    // save game settings and current situation
    public static void saveGame(MainPanel mainPanel) {
        try {
            fw = new FileWriter(saveFileName);
            bw = new BufferedWriter(fw);
            StringBuilder builder = new StringBuilder();

            mainPanel.pauseGame();
            builder.append(heroLifeNum + "\t" + enemyNum + "\t" + score + "\n");

            // details about heroTank
//            HeroTank heroTank = mainPanel.getHeroTank();
            heroTank = mainPanel.getHeroTank();
            builder.append(saveTankInfo(heroTank));

            // details about enemy tanks
//            Vector<EnemyTank> enemyTanks = mainPanel.getEnemyTanks();
            enemyTanks = mainPanel.getEnemyTanks();
            for (EnemyTank et : enemyTanks) {
                if (et.isAlive) {
                    builder.append(saveTankInfo(et));
                }
            }

            bw.write(builder.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                bw.close();
                fw.close();
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }

    private static String saveTankInfo(Tank tank) {
        String info = tank.getX() + "\t" + tank.getY() + "\t"
                + tank.getDirVH() + "\t" + tank.getDirPosNeg() + "\n";
        return info;
    }

    private static String saveBombInfo(Bomb bomb) {
        String info = bomb.getX() + "\t" + bomb.getY() + "\t"
                + bomb.getDirVH() + "\t" + bomb.getDirPosNeg() + "\n";
        return info;
    }

    public static void loadGame() {
        try {
            fr = new FileReader(saveFileName);
            br = new BufferedReader(fr);

            // first line
            String line = br.readLine();
            String[] params = line.split("\t");
            setHeroLifeNum(Integer.parseInt(params[0]));
            setEnemyNum(Integer.parseInt(params[1]));
            setScore(Integer.parseInt(params[2]));

            // second line, hero tank
            line = br.readLine();
            params = line.split("\t");
            int x = Integer.parseInt(params[0]);
            int y = Integer.parseInt(params[1]);
            heroTank = new HeroTank(x, y);
            heroTank.setDirVH(Integer.parseInt(params[2]));
            heroTank.setDirPosNeg(Integer.parseInt(params[3]));

            // enemy tanks
            enemyTanks = new Vector<>();
            for (int i = 0; i < enemyNum; i++) {
                if ((line = br.readLine()) != null) {
                    params = line.split("\t");
                    int enemyX = Integer.parseInt(params[0]);
                    int enemyY = Integer.parseInt(params[1]);
                    EnemyTank et = new EnemyTank(enemyX, enemyY);
                    et.setDirVH(Integer.parseInt(params[2]));
                    et.setDirPosNeg(Integer.parseInt(params[3]));
                    enemyTanks.add(et);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }
}

class SoundPlayer implements Runnable {
    AudioInputStream audioInputStream;
    AudioFormat format;
    SourceDataLine sourceDataLine;
    File audioFile;

    int bufferSize = 1024;
    byte[] b = new byte[bufferSize];
    int len = 0;

    public SoundPlayer(File file) {
        audioFile = file;
    }

    @Override
    public void run() {
        try {
            audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            format = audioInputStream.getFormat();
            sourceDataLine = AudioSystem.getSourceDataLine(format);
            sourceDataLine.open(format, bufferSize);
            sourceDataLine.start();

            while ((len = audioInputStream.read(b)) > 0) {
                sourceDataLine.write(b, 0, len);
            }

            audioInputStream.close();
            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}