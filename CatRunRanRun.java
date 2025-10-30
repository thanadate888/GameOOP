import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

// ======================= MAIN GAME CLASS =======================
public class CatRunRanRun extends JPanel implements ActionListener, KeyListener {

    // ===== Player =====
    private int catX = 50, catY = 300;
    private final int catWidth = 80, catHeight = 80;
    private int velocityY = 0;
    private boolean onGround = true;

    // ===== Game State =====
    private final int groundY = 600;
    private int score = 0;
    private boolean gameOver = false;
    private Timer timer;
    private final Random rand = new Random();

    // ===== Assets =====
    private Image catImage;
    private final Image[] bgImages = new Image[4];
    private final Image[] obsImages = new Image[4];

    // ===== Background Scroll =====
    private int bgX1 = 0, bgX2 = 1280;
    private double scrollSpeed = 4.0;

    // ===== Stage Control =====
    private int currentStage = 0;
    private long stageStartTime;
    private static final long STAGE_DURATION = 25_000; // 25 sec
    private double baseSpeed;

    // ===== Obstacles =====
    private final ArrayList<Obstacle> obstacles = new ArrayList<>();
    private int spawnCooldown = 0;

    // ===== Constructor (รับค่าความเร็วจากหน้าเมนู) =====
    public CatRunRanRun(double baseSpeed) {
        this.baseSpeed = baseSpeed;
        this.scrollSpeed = baseSpeed;
        initGame();
    }

    private void initGame() {
        setFocusable(true);
        addKeyListener(this);
        setBackground(Color.white);

        // โหลดภาพ
        catImage = new ImageIcon("pic/cat.png").getImage();

        bgImages[0] = new ImageIcon("background/city.jpg").getImage();
        bgImages[1] = new ImageIcon("background/night.jpg").getImage();
        bgImages[2] = new ImageIcon("background/forest.jpg").getImage();
        bgImages[3] = new ImageIcon("background/sea2.jpg").getImage();

        obsImages[0] = new ImageIcon("pic/cucumber.png").getImage();
        obsImages[1] = new ImageIcon("pic/car.png").getImage();
        obsImages[2] = new ImageIcon("pic/rock.png").getImage();
        obsImages[3] = new ImageIcon("pic/shark.png").getImage();

        timer = new Timer(20, this);
        timer.start();
        stageStartTime = System.currentTimeMillis();
    }

    // ====== วาดหน้าจอ ======
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth(), height = getHeight();

        // Background scroll
        g.drawImage(bgImages[currentStage], bgX1, 0, width, height, null);
        g.drawImage(bgImages[currentStage], bgX2, 0, width, height, null);

        // Ground
        g.setColor(new Color(80, 80, 80));
        g.fillRect(0, groundY, width, 20);

        // Cat
        g.drawImage(catImage, catX, catY, catWidth, catHeight, null);

        // Obstacles
        for (Obstacle obs : obstacles)
            obs.draw(g);

        // Score + Stage
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Stage: " + getStageName(), width - 180, 30);

        // Game Over
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(Color.red);
            g.drawString("Game Over!", width / 2 - 120, height / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.setColor(Color.white);
            g.drawString("Press R to Restart", width / 2 - 90, height / 2 + 40);
        }
    }

    // ====== Loop หลักของเกม ======
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // Gravity
            catY += velocityY;
            if (catY >= groundY - catHeight) {
                catY = groundY - catHeight;
                onGround = true;
                velocityY = 0;
            } else
                velocityY += 1;

            // Scroll background
            int width = getWidth();
            bgX1 -= (int) scrollSpeed;
            bgX2 -= (int) scrollSpeed;
            if (bgX1 + width <= 0)
                bgX1 = bgX2 + width;
            if (bgX2 + width <= 0)
                bgX2 = bgX1 + width;

            // Spawn obstacles
            if (spawnCooldown <= 0) {
                if (rand.nextInt(100) < 4) {
                    boolean special = rand.nextInt(100) < 15;
                    obstacles.add(new Obstacle(getWidth(), groundY, 60, 60, obsImages[currentStage], special));
                    spawnCooldown = 60;
                }
            } else
                spawnCooldown--;

            // Move obstacles + Collision
            Rectangle catHitbox = new Rectangle(catX + 10, catY + 10, catWidth - 20, catHeight - 15);
            for (int i = 0; i < obstacles.size(); i++) {
                Obstacle obs = obstacles.get(i);
                obs.move(scrollSpeed);

                if (catHitbox.intersects(obs.getBounds())) {
                    gameOver = true;
                    timer.stop();
                }
                if (obs.x + obs.width < 0) {
                    obstacles.remove(i);
                    i--;
                    score++;
                }
            }

            // Stage cycle
            long elapsed = System.currentTimeMillis() - stageStartTime;
            if (elapsed > STAGE_DURATION) {
                currentStage++;
                if (currentStage >= 4) {
                    currentStage = 0;
                    baseSpeed *= 1.2; // +20% per loop
                }
                scrollSpeed = baseSpeed + currentStage;
                obstacles.clear();
                stageStartTime = System.currentTimeMillis();
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && onGround) {
            velocityY = -20;
            onGround = false;
        } else if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // ====== Reset ======
    private void resetGame() {
        catY = groundY - catHeight;
        score = 0;
        obstacles.clear();
        spawnCooldown = 0;
        gameOver = false;
        currentStage = 0;
        scrollSpeed = baseSpeed;
        bgX1 = 0;
        bgX2 = getWidth();
        stageStartTime = System.currentTimeMillis();
        timer.start();
    }

    // ===== Stage name =====
    private String getStageName() {
        return switch (currentStage) {
            case 0 -> "City";
            case 1 -> "Night";
            case 2 -> "Forest";
            default -> "Sea";
        };
    }

    // ====== MAIN ======
    public static void main(String[] args) {
        new StartMenu();
    }

    // ======================= OBSTACLE INNER CLASS =======================
    // ======================= OBSTACLE CLASS =======================
    static class Obstacle {
        int x, y, width, height;
        final Image img;
        final boolean special;
        private int bounceDir = -1;

        public Obstacle(int startX, int groundY, int w, int h, Image image, boolean specialType) {
            this.special = specialType;

            // 🔹 ถ้าเป็นอุปสรรคพิเศษ → ขนาดสุ่มระหว่าง 60% - 90%
            if (special) {
                double scale = 0.6 + Math.random() * 0.3; // 0.6 - 0.9
                this.width = (int) (w * scale);
                this.height = (int) (h * scale);
            } else {
                this.width = w;
                this.height = h;
            }

            this.x = startX;
            this.y = groundY - this.height; // ให้อยู่ติดพื้นตามขนาดใหม่
            this.img = image;
        }

        // 🔹 การเคลื่อนไหวของอุปสรรค
        public void move(double speed) {
            x -= (int) (speed * (special ? 1.6 : 1.4));
            if (special) {
                y += bounceDir * 3;
                if (y < 450 || y > 600 - height)
                    bounceDir *= -1;
            }
        }

        // 🔹 กรอบชน
        public Rectangle getBounds() {
            return new Rectangle(x + 5, y + 5, width - 10, height - 10);
        }

        // 🔹 วาดอุปสรรค
        public void draw(Graphics g) {
            g.drawImage(img, x, y, width, height, null);
            if (special) {
                g.setColor(new Color(255, 255, 0, 100)); // เฉดเหลืองโปร่ง
                g.fillRect(x, y, width, height);
            }
        }
    }

}

// ======================= START MENU =======================
class StartMenu extends JFrame implements ActionListener {
    private final JComboBox<String> difficultyBox;

    public StartMenu() {
        setTitle("Cat Run Ran Run");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(null);

        // Background
        JLabel bg = new JLabel(new ImageIcon("background/city.jpg"));
        bg.setBounds(0, 0, 700, 500);
        add(bg);

        // Title
        JLabel title = new JLabel("Cat Run Ran Run");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setBounds(230, 80, 400, 50);
        bg.add(title);

        // Difficulty
        JLabel diffLabel = new JLabel("Select Difficulty:");
        diffLabel.setFont(new Font("Arial", Font.BOLD, 18));
        diffLabel.setForeground(Color.WHITE);
        diffLabel.setBounds(270, 170, 200, 30);
        bg.add(diffLabel);

        String[] diff = { "Easy", "Medium", "Hard" };
        difficultyBox = new JComboBox<>(diff);
        difficultyBox.setBounds(280, 210, 120, 30);
        bg.add(difficultyBox);

        // Start button
        JButton startBtn = new JButton("Start Game");
        startBtn.setBounds(270, 280, 150, 40);
        startBtn.addActionListener(this);
        bg.add(startBtn);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        double baseSpeed = switch ((String) difficultyBox.getSelectedItem()) {
            case "Easy" -> 3.0;
            case "Medium" -> 5.0;
            case "Hard" -> 7.0;
            default -> 3.0;
        };

        dispose(); // ปิดหน้าเมนู
        JFrame frame = new JFrame("Cat Run Ran Run");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.add(new CatRunRanRun(baseSpeed));
        frame.setVisible(true);
    }
}
