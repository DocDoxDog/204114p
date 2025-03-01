package mygame;
import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;


public class LobbyScreen extends JFrame {
    private JLabel bestScoreLabel, modeLabel;
    private JButton easyBtn, mediumBtn, hardBtn;
    private int bestScore;
    private SoundManager lobbyMusic;
    private JButton soundToggleBtn;
    
    private boolean isSoundOn = true;  // start with sound enabled



    private Image backgroundImg, logoImg;



    private Image zombieImg;
    private Image[] zombieFrames = new Image[4]; // อาร์เรย์เก็บ 4 เฟรม
    private int currentFrame = 0;
    private Font customFont;

    public LobbyScreen() {
        setTitle("Lobby");
        setSize(1500, 1200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf")).deriveFont(24f);  // เปลี่ยนเส้นทางเป็นที่เก็บไฟล์ของคุณ
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        // 1) Create/load your buttons
        // 1) Create/load your buttons
        easyBtn = createStyledButton("Easy", new Color(76, 175, 80));
        mediumBtn = createStyledButton("Medium", new Color(255, 193, 7));
        hardBtn = createStyledButton("Hard", new Color(244, 67, 54));

        lobbyMusic = new SoundManager("/assets/BGM.wav");
        lobbyMusic.play(true);
        initListeners();    

    
        // โหลดภาพพื้นหลัง
        backgroundImg = loadImage("/assets/bag1.png");
        logoImg = loadImage("/assets/logo.png");
    
        // โหลดภาพซอมบี้
        zombieFrames[0] = loadImage("/assets/Zombie.png");
        zombieFrames[1] = loadImage("/assets/Zombie1.png");
        zombieFrames[2] = loadImage("/assets/Zombie2.png");
        zombieFrames[3] = loadImage("/assets/Zombie3.png");

        
    
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImg != null) {
                    g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
    
                // แสดงภาพซอมบี้ที่ตำแหน่งที่ต้องการ
                if (zombieFrames[currentFrame] != null) {
                    int zombieWidth = 800;  // กำหนดขนาดภาพซอมบี้
                    int zombieHeight = 800;
                    int x = (getWidth() - zombieWidth) / 2;  // ตำแหน่งแนวนอนกลางจอ
                    int y = getHeight() - zombieHeight - 20 -100; // ตำแหน่งแนวตั้งติดขอบล่าง
                    g.drawImage(zombieFrames[currentFrame], x, y, zombieWidth, zombieHeight, this);
                }
            }
        };

        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

    // เรียกใช้งาน Timer เพื่อสลับเฟรมทุกๆ 200 มิลลิวินาที
    Timer timer = new Timer(200, e -> {
        currentFrame = (currentFrame + 1) % 4;  // เปลี่ยนเฟรมไปเรื่อยๆ โดยวนกลับที่ 0 เมื่อถึงเฟรมสุดท้าย
        repaint();  // เรียก `paintComponent` เพื่ออัพเดตการแสดงผล
    });
    timer.start(); 
        // **🔸 Panel สำหรับโลโก้ด้านบน**
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (logoImg != null) {
                    int imgWidth = 480;
                    int imgHeight = 480;
                    int x = (getWidth() - imgWidth) / 2;
                    int y = -80;
                    g.drawImage(logoImg, x, y, imgWidth, imgHeight, this);
                }
            }
        };
        logoPanel.setPreferredSize(new Dimension(800, 240));
        logoPanel.setOpaque(false);

        // **🔸 Panel ล่างสำหรับคะแนนและปุ่ม**
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);

        // Label คะแนน
        bestScoreLabel = new JLabel("Best Score: 0");
        bestScoreLabel.setFont(customFont.deriveFont(Font.BOLD, 20f));
        bestScoreLabel.setForeground(Color.YELLOW);
        bestScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Label โหมด
        modeLabel = new JLabel("Mode");
        modeLabel.setFont(customFont.deriveFont(Font.BOLD, 24f));
        modeLabel.setForeground(Color.WHITE);
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

       // ปุ่ม
        easyBtn = createStyledButton("Easy", new Color(76, 175, 80));  
        mediumBtn = createStyledButton("Medium", new Color(255, 193, 7)); 
        hardBtn = createStyledButton("Hard", new Color(244, 67, 54)); 

        // ปรับขนาดปุ่ม
        Dimension buttonSize = new Dimension(180, 50);  // ขนาดปุ่มที่เรียวขึ้น
        easyBtn.setPreferredSize(buttonSize);
        mediumBtn.setPreferredSize(buttonSize);
        hardBtn.setPreferredSize(buttonSize);

        easyBtn.setMaximumSize(buttonSize);
        mediumBtn.setMaximumSize(buttonSize);
        hardBtn.setMaximumSize(buttonSize);

        easyBtn.setBackground(Color.BLACK);
        mediumBtn.setBackground(Color.BLACK);
        hardBtn.setBackground(Color.BLACK);

        easyBtn.setForeground(Color.WHITE);
        mediumBtn.setForeground(Color.WHITE);
        hardBtn.setForeground(Color.WHITE);

        easyBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        mediumBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        hardBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // เปิดใช้งานการเปลี่ยนสีเมื่อเมาส์ไปโดน
        easyBtn.setRolloverEnabled(true);
        mediumBtn.setRolloverEnabled(true);
        hardBtn.setRolloverEnabled(true);

        // เปลี่ยนสีพื้นหลังเมื่อเมาส์วาง
        easyBtn.setContentAreaFilled(true);
        mediumBtn.setContentAreaFilled(true);
        hardBtn.setContentAreaFilled(true);

        // ใช้ MouseListener เพื่อเปลี่ยนสีเมื่อเมาส์ไปวาง
        easyBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                easyBtn.setBackground(Color.GRAY);
                easyBtn.setForeground(Color.BLACK);  // เปลี่ยนตัวหนังสือเป็นสีดำเมื่อเมาส์ไปโดน
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                easyBtn.setBackground(Color.BLACK);  // กลับเป็นสีดำเมื่อเมาส์ออก
                easyBtn.setForeground(Color.WHITE);  // กลับเป็นตัวหนังสือสีขาว
            }
        });

        mediumBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mediumBtn.setBackground(Color.GRAY);
                mediumBtn.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mediumBtn.setBackground(Color.BLACK);
                mediumBtn.setForeground(Color.WHITE);
            }
        });

        hardBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hardBtn.setBackground(Color.GRAY);
                hardBtn.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hardBtn.setBackground(Color.BLACK);
                hardBtn.setForeground(Color.WHITE);
            }
        });

        // ปรับการจัดตำแหน่งของปุ่มให้ขึ้นมาจากขอบล่าง
        bottomPanel.add(bestScoreLabel);
        bottomPanel.add(Box.createVerticalStrut(40));  // เพิ่มระยะห่างจากขอบล่าง
        bottomPanel.add(modeLabel);
        bottomPanel.add(Box.createVerticalStrut(30));
        bottomPanel.add(easyBtn);
        bottomPanel.add(Box.createVerticalStrut(20));
        bottomPanel.add(mediumBtn);
        bottomPanel.add(Box.createVerticalStrut(20));
        bottomPanel.add(hardBtn);
        bottomPanel.add(Box.createVerticalStrut(40));

        // **เพิ่ม Panel ลงใน MainPanel**
        mainPanel.add(logoPanel, BorderLayout.NORTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // เรียกใช้งาน method ที่จะเล่นเพลงพื้นหลัง

        initListeners();
    }


    // **🔹 Method โหลดรูป**
    private Image loadImage(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Image not found: " + path);
            return null;
        }
    }

    // **🔹 Method สำหรับสร้างปุ่มที่มีสไตล์**
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 16f));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

   
    
    private void initListeners() {
        easyBtn.addActionListener(e -> startGame(Difficulty.EASY));
        mediumBtn.addActionListener(e -> startGame(Difficulty.MEDIUM));
        hardBtn.addActionListener(e -> startGame(Difficulty.HARD));
    }

    private void startGame(Difficulty difficulty) {
        if (lobbyMusic != null) {
            lobbyMusic.stop();
        }

        GameFrame gameFrame = new GameFrame(difficulty);
        gameFrame.setVisible(true);
        dispose(); // ปิด Lobby
    }

    public void setBestScore(int newScore) {
    // Only update if newScore is bigger than what we already have
    if (newScore > this.bestScore) {
        this.bestScore = newScore;
    }
    bestScoreLabel.setText("Best Score: " + this.bestScore);
}


}
