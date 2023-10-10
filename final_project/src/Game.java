import java.awt.image.BufferedImage;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;
import java.util.Timer;

public class Game extends JFrame {
    private BufferedImage bg, main_png, materials, icon, enemy;
    private boolean bg_set = false, timer_set = false, end = false, initialized = false, killed = false, warning = false;
    private int[][] GameBoard = {
            //0-blank,1-tree,2-final,4-rock,5-items,6-moving,7-guarded
            {-1, 1, 4, 5, 1, 1, 5, 0, 0, 5},
            {0, 0, 0, 5, 0, 0, 0, 0, 0, 0},
            {7, 4, 0, 0, 1, 1, 4, 5, 1, 0},
            {7, 0, 0, 5, 4, 5, 0, 6, 0, 1},
            {4, 5, 0, 0, 5, 1, 0, 7, 0, 0},
            {6, 1, 0, 5, 4, 5, 1, 4, 1, 5},
            {7, 5, 1, 0, 0, 0, 4, 0, 0, 0},
            {0, 7, 7, 5, 0, 0, 1, 0, 0, 0},
            {0, 4, 1, 0, 1, 0, 4, 0, 7, 1},
            {0, 0, 0, 0, 5, 1, 0, 0, 6, 2}
    };
    private Rectangle src_crop = new Rectangle(0, 0, 86, 86);
    private int boardX = 0, boardY = 0, nowX = 0, nowY = 125, origX = 0, origY = 125, grid_px = 90, second = 0, total = 0, points = 0;

    private JLabel time = new JLabel("Time: 0",JLabel.CENTER);
    private JLabel score_board = new JLabel("", JLabel.CENTER);
    private JLabel status = new JLabel("Collect all items and escape this place!", JLabel.CENTER);
    static Font font22 = new Font("Microsoft JhengHei", Font.BOLD, 22);
    private boolean[] move = {true, true, true, true, true};
    private int[] move_count = {0, 0, 0, 0, 0};

    private int[][] guardian_even = {
            {0, 2},
            {0, 6},
            {1, 7},
            {8, 9},
            {7, 4}
    };
    private int[][] guardian_odd = {
            {0, 3},
            {0, 5},
            {2, 7},
            {8, 8},
            {7, 3}
    };
    private Timer timer;
    private TimerTask timer_task;
    private TimerTask[] move_task = new TimerTask[5];
    private Timer[] move_timer = new Timer[5];

    public static void main(String[] args) throws IOException {
        new Game();
    }

    void reset() {
        bg_set = false;
        timer_set = false;
        end = false;
        initialized = false;
        killed = false;
        GameBoard = new int[][]{
                //0-blank,1-tree,2-final,4-rock,5-items,6-moving,7-guarded
                {-1, 1, 4, 5, 1, 1, 5, 0, 0, 5},
                {0, 0, 0, 5, 0, 0, 0, 0, 0, 0},
                {7, 4, 0, 0, 1, 1, 4, 5, 1, 0},
                {7, 0, 0, 5, 4, 5, 0, 6, 0, 1},
                {4, 5, 0, 0, 5, 1, 0, 7, 0, 0},
                {6, 1, 0, 5, 4, 5, 1, 4, 1, 5},
                {7, 5, 1, 0, 0, 0, 4, 0, 0, 0},
                {0, 7, 7, 5, 0, 0, 1, 0, 0, 0},
                {0, 4, 1, 0, 1, 0, 4, 0, 7, 1},
                {0, 0, 0, 0, 5, 1, 0, 0, 6, 2}
        };
        boardX = 0;
        boardY = 0;
        nowX = 0;
        nowY = 125;
        origX = 0;
        origY = 125;
        grid_px = 90;
        second = 0;
        total = 0;
        points = 0;
        src_crop.y = 0;
        move_count = new int[]{0, 0, 0, 0, 0};
        time.setText("Time: 0");
        for (int t = 0; t < move.length; ++t) {
            move[t] = true;
        }
        status.setText("Collect all items and escape this place!");
    }

    public Game() throws IOException {
        readImg();
        setLayout(null);
        setResizable(false);
        setIconImage(icon);

        setSize(900, 1040);
        setLocationRelativeTo(null);
        setVisible(true);

        initLabels();

        add(time);
        add(status);
        add(score_board);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!end) {
                    int k = e.getKeyCode();
                    if (!timer_set) {
                        if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_LEFT || k == KeyEvent.VK_UP || k == KeyEvent.VK_RIGHT
                                || k == KeyEvent.VK_S || k == KeyEvent.VK_W || k == KeyEvent.VK_A || k == KeyEvent.VK_D) {
                            timer = new Timer();
                            timer_task = new TimerTask() {
                                @Override
                                public void run() {
                                    ++second;
                                    time.setText("Time: " + second);
                                    Toolkit.getDefaultToolkit().sync();
                                }
                            };
                            timer.scheduleAtFixedRate(timer_task, 1000, 1000);
                            timer_set = true;
                        }
                    }
                    status.setText("Collect all items and escape this place!");
                    if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) {
                        if (nowY < 1000 - grid_px && available(boardX, boardY + 1)) {
                            nowY += grid_px;
                            ++boardY;
                            repaint();
                        } else {
                            status.setText("這邊沒有路！");
                        }
                        src_crop.y = 0;
                    }
                    if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) {
                        if (nowY > 125 && available(boardX, boardY - 1)) {
                            nowY -= grid_px;
                            --boardY;
                            repaint();
                        } else {
                            status.setText("這邊沒有路！");
                        }
                        src_crop.y = 260;
                    }
                    if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) {
                        if (nowX < 900 - grid_px && available(boardX + 1, boardY)) {
                            nowX += grid_px;
                            ++boardX;
                            repaint();
                        } else {
                            status.setText("這邊沒有路！");
                        }
                        src_crop.y = 174;
                    }
                    if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) {
                        if (nowX > 0 && available(boardX - 1, boardY)) {
                            nowX -= grid_px;
                            --boardX;
                            repaint();
                        } else {
                            status.setText("這邊沒有路！");
                        }
                        src_crop.y = 86;
                    }
                }
            }
        });

        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }

                    @Override
                    public void windowIconified(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );

    }

    void initLabels() {
        time.setBounds(3, 6, 150, 80);
        time.setOpaque(true);
        time.setBackground(Color.orange);
        time.setFont(font22);
        //time.setAlignment(Label.CENTER);

        status.setBounds(160, 6, 563, 80);
        status.setOpaque(true);
        status.setBackground(Color.white);
        status.setFont(font22);
        //status.setAlignment(Label.CENTER);

        score_board.setBounds(730, 6, 150, 80);
        score_board.setOpaque(true);
        score_board.setBackground(Color.orange);
        score_board.setFont(font22);
        //score_board.setAlignment(Label.CENTER);
    }

    void setMoving() {
        int randSec, base_ms = 500, rand_ms = 300;

        for (int t = 0; t < 5; ++t) {
            randSec = base_ms + (int) (Math.random() * rand_ms);
            final int n = t;
            move_task[n] = new TimerTask() {
                @Override
                public void run() {
                    move[n] = true;
                    ++move_count[n];
                    repaint();
                }
            };
            move_timer[n] = new Timer();
            move_timer[n].scheduleAtFixedRate(move_task[n], randSec, randSec);
        }
    }

    void readImg() throws IOException {
        bg = ImageIO.read(new File("src/grass.png"));
        main_png = ImageIO.read(new File("src/main.png"));
        materials = ImageIO.read(new File("src/material.png"));
        icon = ImageIO.read(new File("src/icon.png"));
        enemy = ImageIO.read(new File("src/Enemy.png"));
    }

    boolean available(int Y, int X) {
        if (GameBoard[X][Y] == 5) {
            ++points;
            score_board.setText("Score: " + points + "/" + total);
            status.setText("太棒了！還有" + (total - points) + "個物品要收集～加油！");
            GameBoard[X][Y] = 0;
        }
        if (GameBoard[X][Y] == 2) {
            if (points == total) {
                end(true);
            } else {
                status.setText("喔...是不是少了什麼呢？還有" + (total - points) + "個物品！");
                warning = true;
            }
        }
        if (GameBoard[X][Y] == 7) {
            ++points;
            score_board.setText("Score: " + points + "/" + total);
            status.setText("太棒了！還有" + (total - points) + "個物品要收集～加油！");
            GameBoard[X][Y] = 6;
        }
        for (int t = 0; t < 5; ++t) {
            if (move_count[t] % 2 == 0) {
                if (X == guardian_even[t][1] && Y == guardian_even[t][0]) {
                    move[t] = true; //make sure the enemy eliminate the main character
                    end(false);
                }
            } else {
                if (X == guardian_odd[t][1] && Y == guardian_odd[t][0]) {
                    move[t] = true; //make sure the enemy eliminate the main character
                    end(false);
                }
            }
        }
        int current = GameBoard[X][Y];
        return current != 1 && current != 4;
    }

    void end(boolean pass) {
        end = true;
        timer.cancel();
        for (int t = 0; t < 5; ++t) {
            move_timer[t].cancel();
        }
        if (pass) {
            status.setText("恭喜～你成功過關了！");
        } else {
            status.setText("You're DEAD...太大意了");
            killed = true;
        }
    }

    @Override
    public void paint(Graphics g) {
        int width = this.getWidth();
        int height = this.getHeight();
        int img_w = 450;
        int img_h = 450;
        // Draw the previously loaded image to Component.
        if (!bg_set) {
            super.paint(g);
            setMoving();
            for (int h = 0; h < 2; ++h) {
                for (int w = 0; w < 2; ++w) {
                    if (h == 0) {
                        g.drawImage(bg, img_w * w, 125, img_w * w + img_w, 125 + img_h, 0, 62, img_w, 62 + img_h, null);

                    } else {
                        g.drawImage(bg, img_w * w, img_h * h + 125, null);
                    }
                }
            }
            bg_set = true;

            int[][] tree_crop = {
                    {256, 192},
                    {288, 192}
            };

            int[][] rock_crop = {
                    {352, 192},
                    {320, 192},
            };

            int[][] item_crop = {
                    {96, 160},
                    {128, 160},
                    {192, 160},
                    {224, 160},
                    {256, 160},
                    {192, 192}
            };

            int[][] item_crop2 = {
                    {320, 32},
                    {0, 64},
                    {64, 64},
                    {256, 64},
                    {320, 64},
                    {352, 64},
                    {0, 96},
                    {32, 96},
                    {128, 96},
                    {160, 96},
                    {192, 96},
                    {256, 96},
                    {320, 96},
                    {352, 96},
                    {0, 128},
                    {32, 128},
                    {64, 128},
                    {96, 128},
                    {128, 128},
                    {160, 128},
                    {192, 128},
                    {256, 128}
            };

            for (int n = 0; n < 10; ++n) {
                for (int t = 0; t < 10; ++t) {
                    int grid_x = n * grid_px, grid_y = t * grid_px + 125;
                    if (GameBoard[t][n] == 0) {
                        int rand = (int) (Math.random() * 50);
                        if (rand < item_crop.length) {
                            ++total;
                            GameBoard[t][n] = 5;
                            g.drawImage(materials, grid_x, grid_y, grid_x + grid_px, grid_y + grid_px, item_crop[rand][0], item_crop[rand][1], item_crop[rand][0] + 32, item_crop[rand][1] + 32, null);
                        }
                    } else if (GameBoard[t][n] == 1) {
                        int rand = (int) (Math.random() * tree_crop.length);
                        g.drawImage(materials, grid_x, grid_y, grid_x + grid_px, grid_y + grid_px, tree_crop[rand][0], tree_crop[rand][1], tree_crop[rand][0] + 32, tree_crop[rand][1] + 32, null);
                    } else if (GameBoard[t][n] == 4) {
                        int rand = (int) (Math.random() * rock_crop.length);
                        g.drawImage(materials, grid_x, grid_y, grid_x + grid_px, grid_y + grid_px, rock_crop[rand][0], rock_crop[rand][1], rock_crop[rand][0] + 32, rock_crop[rand][1] + 32, null);
                    } else if (GameBoard[t][n] == 5) {
                        int rand = (int) (Math.random() * item_crop2.length);
                        ++total;
                        g.drawImage(materials, grid_x, grid_y, grid_x + grid_px, grid_y + grid_px, item_crop2[rand][0], item_crop2[rand][1], item_crop2[rand][0] + 32, item_crop2[rand][1] + 32, null);
                    } else if (GameBoard[t][n] == 7) {
                        ++total;
                    }
                }
            }
            score_board.setText("Score: " + points + "/" + total);
        }

        if (origY != nowY || origX != nowX || !initialized) {
            g.clearRect(origX, origY, grid_px, grid_px);
            if (origY - 125 < 450) {
                g.drawImage(bg, origX, origY, origX + grid_px, origY + grid_px, origX % img_w, (origY - 125) % img_h + 62, origX % img_w + grid_px, (origY - 125) % img_h + grid_px + 62, null);
            } else {
                g.drawImage(bg, origX, origY, origX + grid_px, origY + grid_px, origX % img_w, (origY - 125) % img_h, origX % img_w + grid_px, (origY - 125) % img_h + grid_px, null);
            }
            g.clearRect(nowX, nowY, grid_px, grid_px);
            if (nowY - 125 < 450) {
                g.drawImage(bg, nowX, nowY, nowX + grid_px, nowY + grid_px, nowX % img_w, (nowY - 125) % img_h + 62, nowX % img_w + grid_px, (nowY - 125) % img_h + grid_px + 62, null);
            } else {
                g.drawImage(bg, nowX, nowY, nowX + grid_px, nowY + grid_px, nowX % img_w, (nowY - 125) % img_h, nowX % img_w + grid_px, (nowY - 125) % img_h + grid_px, null);
            }

            // paint the exit icon here every time, to prevent it from being overwritten
            g.drawImage(materials, 9 * grid_px, 9 * grid_px + 125, 10 * grid_px, 10 * grid_px + 125, 288, 0, 288 + 32, 32, null);

            g.drawImage(main_png, nowX, nowY, nowX + grid_px, nowY + grid_px, 0, src_crop.y, src_crop.width, src_crop.y + src_crop.height, null);
            origX = nowX;
            origY = nowY;
            initialized = true;
        }
        paintEnemy(g);
        score_board.paintImmediately(score_board.getVisibleRect());
        time.paintImmediately(time.getVisibleRect());
        status.paintImmediately(status.getVisibleRect());
        Toolkit.getDefaultToolkit().sync(); //without this, it'll be lagging
        if(warning)
        {
            warning = false;
            JOptionPane.showMessageDialog(this, "要收集完所有物品才能離開這裡！\n還少了"+(total-points)+"個東西，再檢查看看吧！", "哎呀！", JOptionPane.WARNING_MESSAGE);
        }
        if (end) {
            if (killed) {
                JOptionPane.showMessageDialog(null, "你被士兵抓得正著，沒有辦法逃離了\n下回一定要再小心一點！", "喔不！", JOptionPane.ERROR_MESSAGE);
                reset();
                paint(g);
            } else {
                String level;
                if(second <= 20)
                    level = "跟鬼一樣";
                else if(second <= 25)
                    level = "飛毛腿";
                else if(second <= 30)
                    level = "普普通通";
                else if(second <= 35)
                    level = "勉強及格";
                else if(second <= 40)
                    level = "有點遲鈍";
                else level = "阿嬤都比你強...";
                JOptionPane.showMessageDialog(null, "恭喜過關！花費"+second+"秒\n等級："+level+"\n看看還能不能更好吧！", "太棒了！" , JOptionPane.INFORMATION_MESSAGE);
                reset();
                paint(g);
            }
        }
    }

    void paintEnemy(Graphics g) {
        int grid_x, clear_x, board_x, last_x;
        int grid_y, clear_y, board_y, last_y;
        boolean even = false;

        for (int t = 0; t < 5; ++t) {
            if (move[t]) {
                if (move_count[t] % 2 == 0) // even
                {
                    even = true;
                    board_x = guardian_even[t][1];
                    board_y = guardian_even[t][0];
                    last_x = guardian_odd[t][1];
                    last_y = guardian_odd[t][0];
                    grid_x = guardian_even[t][0] * grid_px;
                    grid_y = guardian_even[t][1] * grid_px + 125;
                    clear_x = guardian_odd[t][0] * grid_px;
                    clear_y = guardian_odd[t][1] * grid_px + 125;
                } else {
                    even = false;
                    board_x = guardian_odd[t][1];
                    board_y = guardian_odd[t][0];
                    last_x = guardian_even[t][1];
                    last_y = guardian_even[t][0];
                    grid_x = guardian_odd[t][0] * grid_px;
                    grid_y = guardian_odd[t][1] * grid_px + 125;
                    clear_x = guardian_even[t][0] * grid_px;
                    clear_y = guardian_even[t][1] * grid_px + 125;
                }
                g.clearRect(clear_x, clear_y, grid_px, grid_px);
                if (clear_y - 125 < 450) {
                    g.drawImage(bg, clear_x, clear_y, clear_x + grid_px, clear_y + grid_px, clear_x % 450, (clear_y - 125) % 450 + 62, clear_x % 450 + grid_px, (clear_y - 125) % 450 + grid_px + 62, null);
                } else {
                    g.drawImage(bg, clear_x, clear_y, clear_x + grid_px, clear_y + grid_px, clear_x % 450, (clear_y - 125) % 450, clear_x % 450 + grid_px, (clear_y - 125) % 450 + grid_px, null);
                }
                int[][] item_crop = {
                        {128, 64},
                        {160, 64},
                        {192, 64},
                        {128, 64},
                        {160, 64},
                        {128, 64},
                        {192, 64},
                        {128, 64},
                        {192, 64},
                        {160, 64}
                };
                if (GameBoard[last_x][last_y] == 7) {
                    int item = 2 * t;
                    if (even) {
                        item = 2 * t + 1;
                    }
                    g.drawImage(materials, clear_x, clear_y, clear_x + grid_px, clear_y + grid_px, item_crop[item][0], item_crop[item][1], item_crop[item][0] + 32, item_crop[item][1] + 32, null);
                }
                if (GameBoard[board_x][board_y] == 7) {
                    int item = 2 * t + 1;
                    if (even) {
                        item = 2 * t;
                    }
                    g.drawImage(materials, grid_x, grid_y, grid_x + grid_px, grid_y + grid_px, item_crop[item][0], item_crop[item][1], item_crop[item][0] + 32, item_crop[item][1] + 32, null);
                }
                g.drawImage(enemy, grid_x, grid_y, grid_x + grid_px, grid_y + grid_px, 109, 40, 209, 175, null);
                move[t] = false;

                if (grid_x == nowX && grid_y == nowY && !end) {
                    end(false);
                }
            }
        }
    }
}
