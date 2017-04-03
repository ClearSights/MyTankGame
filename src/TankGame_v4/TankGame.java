package TankGame_v4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
 * 7. add stage info screen and menuBar
 * 8. add pause/resume function by pressing space
 * 9. add game info display
 * 10. add game info & situation saving function
 */
public class TankGame {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 14));

        MainFrame mainFrame = new MainFrame();
        mainFrame.showUp();
    }
}

class MainFrame extends JFrame implements ActionListener {
    public static final int PANEL_WIDTH = 600;
    public static final int PANEL_HEIGHT = 500;
    public static final int INFO_WIDTH = 150;

    JMenuBar menuBar;
    JMenu menu_game;
    JMenuItem item_newGame, item_exit;

    private StartPanel startPanel;
    private MainPanel mainPanel;
    private Thread mainPanelThread;

    public MainFrame() {
        // menu
        menuBar = new JMenuBar();
        menu_game = new JMenu();
        item_newGame = new JMenuItem();
        item_exit = new JMenuItem();

        menu_game.setText("Game(G)");
        menu_game.setMnemonic('G');
        item_newGame.setText("New Game(N)");
        item_newGame.setMnemonic('N');
        item_exit.setText("Exit(Esc)");

        item_newGame.addActionListener(this);
        item_newGame.setActionCommand("new_game");
        item_exit.addActionListener(this);
        item_exit.setActionCommand("exit");

        menu_game.add(item_newGame);
        menu_game.add(item_exit);
        menuBar.add(menu_game);
        this.setJMenuBar(menuBar);

        // start panel
        startPanel = new StartPanel();
        this.add(startPanel);
    }

    public void showUp() {
        this.setSize(PANEL_WIDTH + INFO_WIDTH, PANEL_HEIGHT);
        this.setResizable(false);
        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "new_game":
                mainPanel = new MainPanel();
                mainPanelThread = new Thread(mainPanel);
                mainPanelThread.start();
                this.addKeyListener(mainPanel);

                this.remove(startPanel);
                this.add(mainPanel);
                this.setVisible(true);
                break;
            case "exit":
                Recorder.saveGame(mainPanel);
                System.exit(0);
                break;
        }
    }
}

class StartPanel extends JPanel {
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, MainFrame.PANEL_WIDTH+MainFrame.INFO_WIDTH, MainFrame.PANEL_HEIGHT);

        // info
        g.setColor(Color.YELLOW);
        Font infoFont = new Font("Helvetica", Font.ITALIC, 48);
        g.setFont(infoFont);
        g.drawString("Stage 1", MainFrame.PANEL_WIDTH/2, MainFrame.PANEL_HEIGHT/2);
    }
}

class MainPanel extends JPanel implements KeyListener, Runnable {
    public static final int INTERVAL = 20;     // display update interval

    private HeroTank heroTank;
    private Vector<EnemyTank> enemyTanks;

    public HeroTank getHeroTank() {
        return heroTank;
    }

    public Vector<EnemyTank> getEnemyTanks() {
        return enemyTanks;
    }

    public MainPanel() {
        // initialize hero tank
        heroTank = new HeroTank(MainFrame.PANEL_WIDTH /2, MainFrame.PANEL_HEIGHT - Tank.HEIGHT - 30);

        // initialize enemy tanks
        int enemyNum = Recorder.getEnemyNum();
        enemyTanks = new Vector<>();
        for (int i = 0; i < enemyNum; i++) {
            int et_x = MainFrame.PANEL_WIDTH /(2*enemyNum) + i*MainFrame.PANEL_WIDTH /enemyNum;
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

        // game background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, MainFrame.PANEL_WIDTH, MainFrame.PANEL_HEIGHT);

        // info area
        g.setColor(Color.GRAY);
        g.fillRect(MainFrame.PANEL_WIDTH, 0, MainFrame.INFO_WIDTH, MainFrame.PANEL_HEIGHT);

        // draw living hero tank
        if (heroTank.isAlive) {
            drawTank(heroTank, g);
        }

        // draw living enemy tanks
        for (EnemyTank et : enemyTanks) {
            if (et.isAlive) {
                drawTank(et, g);
            }
        }

        // draw living bombs of heroTank
        g.setColor(heroTank.getColor());
        for (Bomb bomb : heroTank.bombs) {
            if (bomb.isAlive) {
                g.drawRect(bomb.getX(), bomb.getY(), 1, 1);
            }
        }

        // draw living bombs of enemy tank
        for (EnemyTank et : enemyTanks) {
            g.setColor(et.getColor());
            for (Bomb bomb : et.bombs) {
                if (bomb.isAlive) {
                    g.drawRect(bomb.getX(), bomb.getY(), 1, 1);
                }
            }
        }

        // show info
        showGameInfo(g);
    }

    /* draw a tank at specific position */
    public void drawTank(Tank tank, Graphics g) {
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

    // show info at specific area
    public void showGameInfo(Graphics g) {
        Font infoFont = new Font("Helvetica", Font.PLAIN, 18);
        g.setFont(infoFont);
        int beginPosText = MainFrame.PANEL_WIDTH + 30;
        int beginPosTank = beginPosText + Tank.WIDTH/2;

        //show score info
        g.drawString("Score:", beginPosText, MainFrame.PANEL_HEIGHT/4);
        g.drawString(Recorder.getScore()+"", beginPosText+60, MainFrame.PANEL_HEIGHT/4);

        // draw enemy tank sign and num
        EnemyTank enemyTankSign = new EnemyTank(beginPosTank, MainFrame.PANEL_HEIGHT/2);
        enemyTankSign.setDirPosNeg(EnemyTank.DIR_NEGATIVE);
        drawTank(enemyTankSign, g);
        g.setColor(enemyTankSign.getColor());
        g.drawString(Recorder.getEnemyNum()+"", beginPosTank+40, MainFrame.PANEL_HEIGHT/2);

        // draw hero tank sign and num
        HeroTank heroTankSign = new HeroTank(beginPosTank, MainFrame.PANEL_HEIGHT*3/4);
        drawTank(heroTankSign, g);
        g.setColor(heroTankSign.getColor());
        g.drawString(Recorder.getHeroLifeNum()+"", beginPosTank+40, MainFrame.PANEL_HEIGHT*3/4);
    }

    // pause game
    public void pauseGame() {
        // toggle pause/resume
        boolean currentState = heroTank.isPaused();
        heroTank.setPaused(!currentState);
        for (Bomb bomb : heroTank.bombs) {
            bomb.setPaused(!currentState);
        }

        for (EnemyTank et : enemyTanks) {
            currentState = et.isPaused();
            et.setPaused(!currentState);
            for (Bomb bomb : et.bombs) {
                bomb.setPaused(!currentState);
            }
        }
    }

    // check if a tank is hit by a bomb of enemy
    public boolean updateHit(Tank tank, Bomb bomb) {
        boolean ret = false;

        if (tank.isAlive && bomb.isAlive) {
            switch (tank.getDirVH()) {
                case Tank.DIR_V:
                    if (Math.abs(bomb.getX()-tank.getX()) < Tank.WIDTH/2
                            && Math.abs(bomb.getY()-tank.getY()) < Tank.HEIGHT/2) {
                        bomb.isAlive = false;
                        tank.isAlive = false;
                        ret = true;
                    }
                    break;
                case Tank.DIR_H:
                    if (Math.abs(bomb.getX()-tank.getX()) < Tank.HEIGHT/2
                            && Math.abs(bomb.getY()-tank.getY()) < Tank.WIDTH/2) {
                        bomb.isAlive = false;
                        tank.isAlive = false;
                        ret = true;
                    }
                    break;
            }
        }
        return ret;
    }

    // check if a bomb has reached the bound
    public boolean checkBombOut(Bomb bomb) {
        if (bomb.isAlive) {
            if (bomb.getX() >= 0 && bomb.getY() >= 0
                    && bomb.getX() <= MainFrame.PANEL_WIDTH
                    && bomb.getY() <= MainFrame.PANEL_HEIGHT) {
                return false;
            }
        }
        bomb.isAlive = false;
        return true;
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
                if (!heroTank.isPaused()) {
                    heroTank.setDirVH(Tank.DIR_V);
                    heroTank.setDirPosNeg(Tank.DIR_NEGATIVE);
                    heroTank.step();
                }
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (!heroTank.isPaused()) {
                    heroTank.setDirVH(Tank.DIR_V);
                    heroTank.setDirPosNeg(Tank.DIR_POSITIVE);
                    heroTank.step();
                }
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (!heroTank.isPaused()) {
                    heroTank.setDirVH(Tank.DIR_H);
                    heroTank.setDirPosNeg(Tank.DIR_NEGATIVE);
                    heroTank.step();
                }
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (!heroTank.isPaused()) {
                    heroTank.setDirVH(Tank.DIR_H);
                    heroTank.setDirPosNeg(Tank.DIR_POSITIVE);
                    heroTank.step();
                }
                break;
            case KeyEvent.VK_J:
                // press J, heroTank shoot bomb
                if (!heroTank.isPaused() && heroTank.bombs.size() < Tank.MAX_BOMB_NUM) {
                    Bomb thisBomb = heroTank.shoot();
                    Thread bombThread = new Thread(thisBomb);
                    bombThread.start();
                }
                break;
            case KeyEvent.VK_SPACE:
                pauseGame();
                break;
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
                Thread.sleep(INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (heroTank.isAlive) {
                // check if bombs of heroTank out of bound
                for (int i = 0; i < heroTank.bombs.size(); i++) {
                    Bomb bomb = heroTank.bombs.get(i);
                    if (checkBombOut(bomb)) {
                        heroTank.bombs.remove(bomb);
                    }
                }

                // check if bombs of heroTank hit enemyTank
                for (int i = 0; i < enemyTanks.size(); i++) {
                    EnemyTank enemyTank = enemyTanks.get(i);
                    if (enemyTank.isAlive) {
                        for (int j = 0; j < heroTank.bombs.size(); j++) {
                            Bomb heroBomb = heroTank.bombs.get(j);

                            if (heroBomb.isAlive) {
                                if (updateHit(enemyTank, heroBomb)) {
                                    heroTank.bombs.remove(heroBomb);
                                    enemyTanks.remove(enemyTank);

                                    Recorder.enemyNumDecrease();
                                    Recorder.addScore();
                                }
                            }
                        }
                    }
                }
            }

            // check cases of bombs of enemyTanks
            for (int i = 0; i < enemyTanks.size(); i++) {
                EnemyTank enemyTank = enemyTanks.get(i);
                for (int j = 0; j < enemyTank.bombs.size(); j++) {
                    Bomb enemyBomb = enemyTank.bombs.get(j);

                    // check if bombs of enemyTank out of bound
                    if (checkBombOut(enemyBomb)) {
                        enemyTank.bombs.remove(enemyBomb);
                        break;
                    }

                    // check if bombs of enemyTank hit heroTank
                    if (heroTank.isAlive && enemyBomb.isAlive) {
                        if (updateHit(heroTank, enemyBomb)) {
                            enemyTank.bombs.remove(enemyBomb);
                            Recorder.heroNumDecrease();
                            break;
                        }
                    }
                }
            }

            repaint();
        }
    }
}

