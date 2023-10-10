// No ChatGPT involved in this code
import java.awt.desktop.AboutEvent;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class Game
{
    static String now = "X";
    static Frame window = new Frame("井字棋遊戲");
    static Font font = new Font("Noto Sans CJK TC",Font.BOLD, 30);
    static Font big = new Font("Noto Sans CJK TC",Font.BOLD, 70);
    static Button restart_b = new Button("restart");
    static Label status = new Label(now+"先來！");
    static Label [] board = new Label[9];
    static int [] record = new int [9];
    static boolean end = false;

    public static void main(String [] args)
    {
        initWindow();
        int height = window.getHeight();
        int width = window.getWidth();
        restart_b.setBounds(75, height-80, width - 150, 60);
        restart_b.setFont(font);
        restart_b.setBackground(Color.decode("#E5E7E9"));

        restart_b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(int t = 0; t < 9; t++)
                {
                    board[t].setText("");
                    record[t] = 0;
                    now = "X";
                    status.setBackground(Color.decode("#F5B041"));
                    status.setText(now+"先來！");
                    end = false;
                }
            }
        });

        status.setBounds(0,40, width, 80);
        status.setAlignment(Label.CENTER);
        status.setFont(font);
        status.setBackground(Color.decode("#F5B041"));
        window.add(restart_b);
        window.add(status);

        initBoard();

        window.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
    }
    static void initWindow()
    {
        window.setSize(510,710);
        window.setBackground(Color.decode("#000000"));
        window.setLayout(null);
        window.setLocation(700,100);
        window.setVisible(true);
        window.setResizable(false);
    }
    static void initBoard()
    {
        for(int t = 0; t < 9; ++t)
        {
            board[t] = new Label("");
            board[t].setAlignment(Label.CENTER);
            board[t].setFont(big);
            board[t].setBackground(Color.white);
            board[t].setBounds(15 + (165 * (t % 3)), 135 + (165 * (t / 3)), 150, 150);
            final int a = t;
            board[t].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(!end && record[a] == 0)
                    {
                        board[a].setText(now);
                        if(now == "X") {
                            record[a] = 1;
                            now = "O";
                        }
                        else {
                            record[a] = 2;
                            now = "X";
                        }
                        status.setText("換"+now+"了");
                        checkStatus();
                    }
                }
            });

            window.add(board[t]);
        }
    }

    static void checkStatus()
    {
        String [] player = new String[]{"", "X", "O"};
        for(int t = 1; t <= 2; ++t)
        {
            if(win(t))
            {
                status.setBackground(Color.decode("#73FF62"));
                status.setText("恭喜"+player[t]+"贏了！");
                end = true;
            }
        }
        if(!end)
        {
            boolean full = true;
            for(int t = 0; t < 9; ++ t)
            {
                if(record[t] == 0)
                {
                    full = false; // one or more grids not used
                    break;
                }
            }
            if(full) // if !end but full
            {
                status.setBackground(Color.decode("#73FF62"));
                status.setText("和局！再玩一次吧～");
            }
        }
    }

    static boolean win(int player)
    {
        return (record[0] == player && record[1] == player && record[2] == player)
                ||(record[3] == player && record[4] == player && record[5] == player)
                ||(record[6] == player && record[7] == player && record[8] == player)
                ||(record[0] == player && record[3] == player && record[6] == player)
                ||(record[1] == player && record[4] == player && record[7] == player)
                ||(record[2] == player && record[5] == player && record[8] == player)
                ||(record[0] == player && record[4] == player && record[8] == player)
                ||(record[2] == player && record[4] == player && record[6] == player);
    }
}
