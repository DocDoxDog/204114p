package mygame;

import javax.swing.JFrame;

public class GameFrame extends JFrame {
    public GameFrame(Difficulty difficulty) {
        setTitle("Zombie Vocabulary Game");
        setSize(1500, 1200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel(difficulty);
        add(gamePanel);
    }
}
