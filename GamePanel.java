package mygame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import mygame.characters.Player;
import mygame.characters.Zombie;
import mygame.items.Item;
import mygame.vocab.Vocabulary;

/**
 * Main game panel for zombie vocab, with Q&A, items, music, and more.
 */
public class GamePanel extends JPanel implements ActionListener {

    // --------------------------------------------
    // FIELDS
    // --------------------------------------------

    private Player player;
    private Zombie zombie;
    private boolean gameOver;
    private int timeCounter;
    private Difficulty difficulty;
    private int zombiesDefeated;

    // Vocabulary from CSV
    private Vocabulary[] vocabList;
    private Vocabulary currentQuestion;
    private String[] answerChoices;
    private final Random rand = new Random();

    // Q&A UI elements
    private JLabel englishWordLabel;
    private JButton choice1Button;
    private JButton choice2Button;
    private JButton restartButton;

    private Timer gameTimer; // Main game loop timer

    // Images
    private Image bgImg;
    private Image playerImg;
    private Image[] playerFrames = new Image[2];
    private Image[] playerShoot = new Image[3];
    private int playerFrameIndex = 0;

    private Image[] zombieFrames = new Image[4];
    private Image zombieImg;
    private int currentFrame = 0;

    // Additional images for animations
    private Image shootImg;
    private Image diezombie;
    private Image playerheal;

    // Some constants for dimensions
    private static final int PLAYER_WIDTH  = 500;
    private static final int PLAYER_HEIGHT = 540;
    private static final int ZOMBIE_WIDTH  = 600;
    private static final int ZOMBIE_HEIGHT = 660;

    // Spacing and approach logic
    private static final int HORIZONTAL_SEPARATION = 200;
    private int currentSeparation;

    // CSV and background path
    private static final String CSV_PATH       = "src/assets/vocab.csv";
    private static final String BG_IMAGE_PATH  = "src/assets/background_2.png";

    // Semi-transparent overlay on game over
    private JPanel gameOverPanel;
    private JButton playAgainBtn;
    private JButton lobbyBtn;

    // Background Music
    private SoundManager bgMusic;

    // Wrong answer HP penalty
    private static final int WRONG_ANSWER_PENALTY = 20;
    private int zombieTimeLimit; // ms before zombie reaches player

    // --- ITEM BAR FIELDS ---
    private JPanel itemBarPanel;          // top-right panel for item icons
    private List<Item> inventory = new ArrayList<>(); // hold items

    // Fonts
    private Font titleFont;
    private Font buttonFont;
    private Font thaiFontButtons;
    private Font statusFont;
    private Font customFont;


    private Image gameOverBgImg;

    // --------------------------------------------
    // CONSTRUCTOR
    // --------------------------------------------



    public GamePanel(Difficulty difficulty) {
        this.difficulty = difficulty;

        // Basic panel setup
        setLayout(null); // We'll position many components manually
        setFocusable(true);
        requestFocus();

        // Start background music
        bgMusic = new SoundManager("/assets/BGM.wav");
        bgMusic.play(true);

        // Build UI components & load data
        createUIComponents();
        loadImages();
        loadCSVData();
        createInventory();     // Creates items based on difficulty
        createItemBarPanel();  // Build top-right item bar

        initGame(); // Set initial stats, pick first question
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf")).deriveFont(24f);  // เปลี่ยนเส้นทางเป็นที่เก็บไฟล์ของคุณ
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        // ~60 FPS loop
        gameTimer = new Timer(16, e -> {
            if (!gameOver) {
                updateGame();
                repaint();
            }
        });
        gameTimer.start();
    }

    // --------------------------------------------
    // CREATE INVENTORY & ITEM BAR
    // --------------------------------------------

    /**
     * Based on difficulty:
     * EASY => 2 copies of certain items
     * MEDIUM => 1 copy
     * HARD => 0 items
     */
    private void createInventory() {
        inventory.clear();

        if (this.difficulty == Difficulty.EASY) {
            // For EASY, e.g. 2 copies each
            inventory.add(new Item.MedKit());
            inventory.add(new Item.MedKit());
            inventory.add(new Item.EnergyDrink());
            inventory.add(new Item.EnergyDrink());

        } else if (this.difficulty == Difficulty.MEDIUM) {
            // For MEDIUM, 1 copy each
            inventory.add(new Item.MedKit());
            inventory.add(new Item.EnergyDrink());

        } else if (this.difficulty == Difficulty.HARD) {
            // For HARD => no items
            // (inventory is empty)
        }
    }

    /**
     * Creates a top-right panel for item icons.
     */
    private void createItemBarPanel() {
        itemBarPanel = new JPanel(null); // We'll position item buttons manually
        itemBarPanel.setOpaque(false);

        int xPos = 0;
        for (Item item : inventory) {
            JButton itemBtn = createItemButton(item);
            // Place each button side-by-side
            itemBtn.setBounds(xPos, 0, 60, 60);
            itemBarPanel.add(itemBtn);
            xPos += 70;
        }

        // Add panel to the main panel
        add(itemBarPanel);
    }

        /**
         * Creates a button for a given item (icon + name).
         */



         // ตัวแปรเก็บเวลาเมื่อผู้เล่นตอบคำถาม


    private JButton createItemButton(Item item) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource(item.getImagePath()));
            Image raw = icon.getImage();
            Image scaled = raw.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaled);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JButton btn = new JButton(item.getItemName(), icon);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        // SINGLE-USE LOGIC: remove item from 'inventory' & remove button after use
        btn.addActionListener(e -> {
            System.out.println("Using " + item.getItemName());
            item.useItem(player);

            // 1) Remove item from the list so we don't see it again
            inventory.remove(item);

            // 2) Remove the button from the panel
            itemBarPanel.remove(btn);

            // 3) Refresh the panel so it redraws without the used item
            itemBarPanel.revalidate();
            itemBarPanel.repaint();
            playerImg = playerheal;
            lastAnswerTime = System.currentTimeMillis();
            repaint();
        });

        return btn;
    }


    // --------------------------------------------
    // CSV / VOCAB LOADING
    // --------------------------------------------

    private void loadCSVData() {
        vocabList = readVocabularyFromCSV(CSV_PATH);
        if (vocabList == null || vocabList.length == 0) {
            System.err.println("No vocab found in CSV. Using fallback data.");
            vocabList = new Vocabulary[] {
                new Vocabulary("Apple", "แอปเปิ้ล", new String[]{})
            };
        }
    }

    private Vocabulary[] readVocabularyFromCSV(String csvPath) {
        List<Vocabulary> list = new ArrayList<>();
        File file = new File(csvPath);
        if (!file.exists()) {
            System.err.println("CSV file not found: " + csvPath);
            return null;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String eng = parts[0].trim();
                    String thai = parts[1].trim();
                    Vocabulary v = new Vocabulary(eng, thai, new String[]{});
                    list.add(v);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list.isEmpty() ? null : list.toArray(new Vocabulary[0]);
    }

    // --------------------------------------------
    // CREATE UI COMPONENTS & FONTS
    // --------------------------------------------

    private void createUIComponents() {
        // Setup fonts
        titleFont = new Font("Arial", Font.BOLD, 42);
        buttonFont = new Font("Arial", Font.BOLD, 20);

        thaiFontButtons = new Font("Angsana New", Font.BOLD, 28);
        if (!isFontAvailable("Angsana New")) {
            thaiFontButtons = new Font("TH Sarabun New", Font.BOLD, 28);
            if (!isFontAvailable("TH Sarabun New")) {
                thaiFontButtons = new Font("Tahoma", Font.BOLD, 28);
            }
        }

        statusFont = new Font("Arial", Font.PLAIN, 18);

        // Q&A label
        englishWordLabel = new JLabel("English Word: ???", SwingConstants.CENTER);
        englishWordLabel.setForeground(Color.WHITE);
        englishWordLabel.setFont(titleFont);
        add(englishWordLabel);

        // Answer choice buttons
        choice1Button = createAnswerButton("Choice1", 0, 0);
        choice2Button = createAnswerButton("Choice2", 0, 0);

        // Restart button (hidden until game over)
        restartButton = new JButton("Play Again");
        restartButton.setFont(buttonFont);
        restartButton.setBounds(0, 0, 150, 40);
        restartButton.addActionListener(e -> {
            initGame();
            restartButton.setVisible(false);
        });
        restartButton.setVisible(false);
        add(restartButton);

        // Reposition on window resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionButtons();
            }
        });
    }

    private boolean isFontAvailable(String fontName) {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font f : fonts) {
            if (f.getFontName().equals(fontName)) {
                return true;
            }
        }
        return false;
    }

    private JButton createAnswerButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setFont(thaiFontButtons);
        btn.setBounds(x, y, 180, 50);
        btn.addActionListener(this);
        add(btn);
        return btn;
    }

    // --------------------------------------------
    // REPOSITION UI ON RESIZE
    // --------------------------------------------

    private void repositionButtons() {
        int panelWidth  = getWidth();
        int panelHeight = getHeight();

        // Q&A buttons
        int buttonWidth   = 250;
        int buttonHeight  = 100;
        int buttonSpacing = 200;

        // English word label top-center
        int labelWidth  = 1000;
        int labelHeight = 180;
        englishWordLabel.setBounds(
            (panelWidth - labelWidth) / 2,
            50,
            labelWidth,
            labelHeight
        );

        // Position Q&A at the bottom center
        int totalButtonsWidth = (buttonWidth * 2) + buttonSpacing;
        int startX = (panelWidth - totalButtonsWidth) / 2;
        int buttonY = panelHeight - 180;

        choice1Button.setBounds(startX, buttonY, buttonWidth, buttonHeight);
        choice2Button.setBounds(startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight);

        // Restart button in center if game over
        int rbWidth  = 200;
        int rbHeight = 60;
        restartButton.setBounds(
            (panelWidth - rbWidth) / 2,
            panelHeight / 2 + 50,
            rbWidth,
            rbHeight
        );

        // Position itemBarPanel top-right
        if (itemBarPanel != null) {
            // each item is ~70 wide
            int itemBarWidth  = inventory.size() * 70;
            int itemBarHeight = 60;
            int barX = panelWidth - (itemBarWidth + 20); // 20 px from right
            int barY = 10; // 10 px from top
            itemBarPanel.setBounds(barX, barY, itemBarWidth, itemBarHeight);
        }
    }

    // --------------------------------------------
    // LOAD IMAGES
    // --------------------------------------------

    private void loadImages() {
        bgImg = loadSingleImage(BG_IMAGE_PATH);

        // Player frames
        playerFrames[0] = loadAndScaleImage("src/assets/player.png",  PLAYER_WIDTH, PLAYER_HEIGHT);
        playerFrames[1] = loadAndScaleImage("src/assets/player1.png", PLAYER_WIDTH, PLAYER_HEIGHT);
        playerImg = playerFrames[0];

        playerShoot[0] = loadAndScaleImage("src/assets/shoot.png",  PLAYER_WIDTH, PLAYER_HEIGHT);
        playerShoot[1] = loadAndScaleImage("src/assets/shoot.png",  PLAYER_WIDTH, PLAYER_HEIGHT);
        playerShoot[2] = loadAndScaleImage("src/assets/shoot.png",  PLAYER_WIDTH, PLAYER_HEIGHT);
        

        // Zombie frames
        zombieFrames[0] = loadAndScaleImage("src/assets/Zombie.png",  ZOMBIE_WIDTH, ZOMBIE_HEIGHT);
        zombieFrames[1] = loadAndScaleImage("src/assets/Zombie1.png", ZOMBIE_WIDTH, ZOMBIE_HEIGHT);
        zombieFrames[2] = loadAndScaleImage("src/assets/Zombie2.png", ZOMBIE_WIDTH, ZOMBIE_HEIGHT);
        zombieFrames[3] = loadAndScaleImage("src/assets/Zombie3.png", ZOMBIE_WIDTH, ZOMBIE_HEIGHT);
        zombieImg = zombieFrames[0];

        playerheal = loadAndScaleImage("src/assets/heal.png", ZOMBIE_WIDTH, ZOMBIE_HEIGHT);
        diezombie = loadAndScaleImage("src/assets/diezom.png", ZOMBIE_WIDTH, ZOMBIE_HEIGHT);
        shootImg  = loadAndScaleImage("src/assets/shoot.png",  PLAYER_WIDTH, PLAYER_HEIGHT);

        gameOverBgImg = loadAndScaleImage("src/assets/over.png", 800, 600);

    }

    private Image loadSingleImage(String path) {
        File f = new File(path);
        if (!f.exists()) {
            System.err.println("Image not found: " + path);
            return null;
        }
        try {
            return ImageIO.read(f);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Image loadAndScaleImage(String path, int w, int h) {
        Image raw = loadSingleImage(path);
        if (raw != null) {
            return raw.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        }
        return null;
    }

    // --------------------------------------------
    // GAME INITIALIZATION
    // --------------------------------------------

    private void initGame() {
        gameOver = false;
        timeCounter = 0;
        zombiesDefeated = 0;

        // Time limit
        zombieTimeLimit = (difficulty == Difficulty.HARD) ? 5000 :
                          (difficulty == Difficulty.MEDIUM) ? 10000 :
                                                               15000;

        currentSeparation = HORIZONTAL_SEPARATION;

        // Player HP
        int pHp = (difficulty == Difficulty.HARD)   ? 40 :
                  (difficulty == Difficulty.MEDIUM) ? 80 :
                                                      120;
        player = new Player("Hero", pHp, 0, 0, 0);

        // Minimal zombie HP
        zombie = new Zombie("Zombie", 1, 0, 0, 0);

        // Hide restart
        if (restartButton != null) {
            restartButton.setVisible(false);
        }
        enableAnswerButtons(true);

        loadNewQuestion();
    }

    private void enableAnswerButtons(boolean enable) {
        choice1Button.setEnabled(enable);
        choice2Button.setEnabled(enable);
    }

    /**
     * Picks a random "correct" and a random "wrong" from vocab, then assigns them
     * to choice1Button and choice2Button (shuffled).
     */
    private void loadNewQuestion() {
        if (vocabList == null || vocabList.length == 0) {
            englishWordLabel.setText("No vocab data!");
            choice1Button.setText("(No data)");
            choice2Button.setText("(No data)");
            return;
        }

        // Pick random correct
        int correctIndex = rand.nextInt(vocabList.length);
        Vocabulary correctV = vocabList[correctIndex];
        String correctWord = correctV.getCorrectTranslation();

        // Pick distinct wrong
        String wrongWord = "(No data)";
        if (vocabList.length > 1) {
            int wrongIndex;
            do {
                wrongIndex = rand.nextInt(vocabList.length);
            } while (wrongIndex == correctIndex);
            wrongWord = vocabList[wrongIndex].getCorrectTranslation();
        }

        // Build answers
        answerChoices = new String[]{ correctWord, wrongWord };
        currentQuestion = new Vocabulary(correctV.getEnglishWord(), correctWord, new String[]{});

        // Shuffle the 2 answers
        for (int i = 0; i < answerChoices.length; i++) {
            int j = rand.nextInt(answerChoices.length);
            String tmp = answerChoices[i];
            answerChoices[i] = answerChoices[j];
            answerChoices[j] = tmp;
        }

        englishWordLabel.setText(currentQuestion.getEnglishWord());
        choice1Button.setText(answerChoices[0]);
        choice2Button.setText(answerChoices[1]);

        // Some styling
        choice1Button.setBackground(Color.BLACK);
        choice1Button.setForeground(Color.WHITE);
        choice1Button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        choice1Button.setFocusPainted(false);

        choice2Button.setBackground(Color.BLACK);
        choice2Button.setForeground(Color.WHITE);
        choice2Button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        choice2Button.setFocusPainted(false);
    }

    // --------------------------------------------
    // MAIN GAME LOOP
    // --------------------------------------------



    
    private void updateGame() {

        if (System.currentTimeMillis() - lastAnswerTime < ANSWER_DELAY) {

            return;
        }



        player.update();
        zombie.update();

        timeCounter += 16;

        float progressPercent = (float) timeCounter / zombieTimeLimit;
        currentSeparation = Math.max(0, (int)(HORIZONTAL_SEPARATION * (1 - progressPercent)));


        // Animate zombie frames
        if (timeCounter % 64 == 0) {
            currentFrame = (currentFrame + 1) % 4;
            zombieImg = zombieFrames[currentFrame];
        }

        // Animate player frames
        if (timeCounter % 20 == 0) {
            playerFrameIndex = (playerFrameIndex + 1) % 2;
            playerImg = playerFrames[playerFrameIndex];
        }

        // If zombie reaches the player
        if (currentSeparation <= 0) {
            player.takeDamage(20);
            timeCounter = 0;
            currentSeparation = HORIZONTAL_SEPARATION;

            if (!player.isAlive()) {
                gameOver = true;
                showGameOver();
            } else {
                loadNewQuestion();
            }
        }
    }

    // --------------------------------------------
    // BUTTON ACTION
    // --------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        if (e.getSource() == choice1Button) {
            checkAnswer(answerChoices[0]);
        } else if (e.getSource() == choice2Button) {
            checkAnswer(answerChoices[1]);
        } else if (e.getSource() == restartButton) {
            initGame();
        }
    }


    private long lastAnswerTime = 0;  // ตัวแปรเก็บเวลาเมื่อผู้เล่นตอบคำถาม
    private static final long ANSWER_DELAY =  220;  // หน่วงเวลา 2 วินาทีที่ภาพจะค้าง
    
    private void checkAnswer(String selected) {
        String correct = currentQuestion.getCorrectTranslation();
        if (selected.equals(correct)) {
            // 1) Determine how many points to award
            int points = 0;
            if (this.difficulty == Difficulty.EASY) {
                points = 10;
            } else if (this.difficulty == Difficulty.MEDIUM) {
                points = 20;
            } else if (this.difficulty == Difficulty.HARD) {
                points = 30;
            }
    
            // 2) Increase score
            player.increaseScore(points);
    
            // 3) Reset the zombie approach, animate, etc.
            zombiesDefeated++;
            timeCounter = 0;
            currentSeparation = HORIZONTAL_SEPARATION;
    

            playerImg = shootImg;
            zombieImg = diezombie;
    
  
            lastAnswerTime = System.currentTimeMillis();
    
            repaint(); 
    


        loadNewQuestion();
    
           

        } else {
            // Wrong => damage
            player.takeDamage(WRONG_ANSWER_PENALTY);
            if (!player.isAlive()) {
                gameOver = true;
                showGameOver();
                return;
            }
            timeCounter = 0;
            currentSeparation = HORIZONTAL_SEPARATION;
            loadNewQuestion();
        }
        repaint();
    }


    // --------------------------------------------
    // GAME OVER
    // --------------------------------------------
    private Image gameOverBackground;
    private void showGameOver() {
        gameOver = true;
        enableAnswerButtons(false);

        // Stop background music
        if (bgMusic != null) {
            bgMusic.stop();
        }

        int finalScore = player.getScore();
        // Check & save best score
        if (finalScore > Main.bestScore) {
            Main.bestScore = finalScore;
            // Save to CSV if needed
            HighScoreManager.writeBestScore(Main.bestScore);
        }
 

    
        // Create overlay
        gameOverPanel = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (gameOverBgImg != null) {
                g.drawImage(gameOverBgImg, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        gameOverPanel.setBounds(0, 0, getWidth(), getHeight());


        // "GAME OVER" label
        JLabel gameOverLabel = new JLabel("  GAME OVER! " );

        gameOverLabel.setFont(customFont.deriveFont(Font.BOLD, 34));
        gameOverLabel.setForeground(Color.WHITE);
        // Position it in the center of the panel
        gameOverLabel.setBounds((getWidth() - 500) / 2, getHeight()/2 - 100, 500, 50);
        gameOverPanel.add(gameOverLabel);

        JLabel Score = new JLabel("Score: " + finalScore, SwingConstants.CENTER);
        Score.setFont(customFont.deriveFont(Font.BOLD, 28));
        Score.setForeground(Color.WHITE);
        Score.setBounds((getWidth() - 500)  / 2, getHeight()/2 - 180, 500, 50);
        gameOverPanel.add(Score);

        // "Play Again"
        playAgainBtn = new JButton("Play Again");
        playAgainBtn.setFont(customFont.deriveFont(Font.BOLD, 10));
        playAgainBtn.setBounds(getWidth()/2 - 200, getHeight()/2, 150, 40);
        playAgainBtn.addActionListener(e -> {
            remove(gameOverPanel);
            gameOverPanel = null;
            initGame();
            repaint();
        });
        gameOverPanel.add(playAgainBtn);

        // "Back to Lobby"
        lobbyBtn = new JButton("Lobby");
        lobbyBtn.setFont(customFont.deriveFont(Font.BOLD, 10));
        lobbyBtn.setBounds(getWidth()/2 + 50, getHeight()/2, 150, 40);
        lobbyBtn.addActionListener(e -> {
            LobbyScreen newLobby = new LobbyScreen();
            newLobby.setBestScore(Main.bestScore);
            newLobby.setVisible(true);

            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topFrame != null) {
                topFrame.dispose();
            }
        });
        gameOverPanel.add(lobbyBtn);

        // Add overlay
        add(gameOverPanel);
        setComponentZOrder(gameOverPanel, 0);
        repaint();
    }

    // --------------------------------------------
    // RENDER
    // --------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        if (bgImg != null) {
            g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Display player HP, score, etc.
        g2.setFont(statusFont);
        g2.setColor(Color.GREEN);
        g2.drawString("Player HP: " + player.getHp(), 20, 70);

        g2.setColor(Color.YELLOW);
        g2.drawString("Score: " + player.getScore(), 20, 130);
        g2.drawString("Zombies Defeated: " + zombiesDefeated, 20, 160);

        // Zombie approach bar
        if (!gameOver) {
            float progressPercent = (float) timeCounter / zombieTimeLimit;
            int barWidth = 200;
            int barHeight = 20;

            // background
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(20, 190, barWidth, barHeight);

            // fill
            g2.setColor(new Color(255, (int)(255*(1 - progressPercent)), 0));
            g2.fillRect(20, 190, (int)(barWidth * progressPercent), barHeight);

            g2.setColor(Color.WHITE);
            g2.drawRect(20, 190, barWidth, barHeight);
            g2.drawString("Zombie Approach", 20 + barWidth + 10, 190 + 15);
        }

        // If game over, optionally draw a message
        if (gameOver) {
            g2.setColor(Color.YELLOW);
            g2.setFont(titleFont);
            String gameOverText = "GAME OVER! Final Score: " + player.getScore();
            int textWidth = g2.getFontMetrics().stringWidth(gameOverText);
            g2.drawString(gameOverText, getWidth()/2 - textWidth/2, getHeight()/2 - 50);
            g2.drawString("Zombies Defeated: " + zombiesDefeated,
                          getWidth()/2 - textWidth/2,
                          getHeight()/2);
        }

        // Draw player & zombie
        int panelW = getWidth();
        int panelH = getHeight();
        int centerX = panelW / 2;
        int centerY = panelH / 2;

        drawCharacter(g2, playerImg, centerX - HORIZONTAL_SEPARATION, centerY,
                      PLAYER_WIDTH, PLAYER_HEIGHT);

        drawCharacter(g2, zombieImg, centerX + currentSeparation, centerY,
                      ZOMBIE_WIDTH, ZOMBIE_HEIGHT);
    }

    /**
     * Helper to draw a character (img) centered at (centerX, centerY) 
     * but shifted down 150 pixels.
     */
    private void drawCharacter(Graphics2D g2, Image img,
                               int centerX, int centerY,
                               int w, int h) {
        if (img == null) return;
        int x = centerX - w / 2;
        int y = centerY - h / 2 + 150; 
        g2.drawImage(img, x, y, w, h, this);
    }
}
