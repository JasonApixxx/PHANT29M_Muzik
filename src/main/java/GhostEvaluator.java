import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.util.*;

public class GhostEvaluator extends JFrame {

    private static final Color BG_DARK = new Color(11, 15, 23);
    private static final Color CARD_BG = new Color(18, 24, 36);
    private static final Color CARD_BORDER = new Color(31, 41, 61);
    private static final Color TEXT_MAIN = new Color(243, 244, 246);
    private static final Color TEXT_MUTED = new Color(156, 163, 175);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);

    private static final String[] GRADES_VIVOZ = {"невывоз", "попытка", "неплохо", "верю"};
    private static final Color[] COLORS_VIVOZ = {
            new Color(255, 51, 102),
            new Color(255, 153, 51),
            new Color(0, 210, 255),
            new Color(168, 85, 247)
    };

    private static final String[] GRADES_MUSIC = {"дно", "земля", "облака", "космос"};
    private static final Color[] COLORS_MUSIC = {
            new Color(255, 51, 102),
            new Color(255, 153, 51),
            new Color(0, 210, 255),
            new Color(168, 85, 247)
    };

    private static final String[] GRADES_REPEAT = {"уницикл", "велик", "квадрацикл"};
    private static final Color[] COLORS_REPEAT = {
            new Color(255, 51, 102),
            new Color(255, 153, 51),
            new Color(168, 85, 247)
    };

    private final Map<String, CriterionRow> rows = new LinkedHashMap<>();
    private FirePanel firePanel;

    public GhostEvaluator() {
        setTitle("PHANT29M");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Полноэкранный режим при старте
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(600, 750));
        getContentPane().setBackground(BG_DARK);

        initUI();
        initFullscreenToggle();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(BG_DARK);
        mainWrapper.setBorder(new EmptyBorder(20, 30, 25, 30));

        // Верхний контейнер с карточками и кнопкой
        JPanel controlsContainer = new JPanel();
        controlsContainer.setLayout(new BoxLayout(controlsContainer, BoxLayout.Y_AXIS));
        controlsContainer.setOpaque(false);
        controlsContainer.setMaximumSize(new Dimension(680, 520));

        // Шапка с заголовком PHANT29M
        JLabel header = new JLabel("PHANT29M");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setFont(new Font("Segoe UI Black", Font.BOLD, 32));
        header.setForeground(ACCENT_PURPLE);
        controlsContainer.add(header);
        controlsContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // Карточки критериев (слайдеры только обновляют текст статуса)
        rows.put("Вывоз", new CriterionRow("Вывоз", GRADES_VIVOZ, COLORS_VIVOZ, 3));
        rows.put("Понимание музыки", new CriterionRow("Понимание музыки", GRADES_MUSIC, COLORS_MUSIC, 3));
        rows.put("Лёгкость и репитативность", new CriterionRow("Лёгкость и репитативность", GRADES_REPEAT, COLORS_REPEAT, 2));

        for (CriterionRow row : rows.values()) {
            controlsContainer.add(row);
            controlsContainer.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        // Кнопка вердикта, которая зажигает огонь
        JButton calcBtn = new JButton("СФОРМИРОВАТЬ ВЕРДИКТ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(130, 45, 235), getWidth(), 0, new Color(255, 20, 120));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        calcBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        calcBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        calcBtn.setPreferredSize(new Dimension(680, 48));
        calcBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setContentAreaFilled(false);
        calcBtn.setBorderPainted(false);
        calcBtn.setFocusPainted(false);
        calcBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calcBtn.addActionListener(e -> triggerVerdictFire());

        controlsContainer.add(calcBtn);
        controlsContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        // Центрирование блока карточек
        JPanel topCenterWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        topCenterWrapper.setOpaque(false);
        topCenterWrapper.add(controlsContainer);
        mainWrapper.add(topCenterWrapper, BorderLayout.NORTH);

        // Панель огня внизу
        JPanel fireContainer = new JPanel(new BorderLayout());
        fireContainer.setBackground(CARD_BG);
        fireContainer.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));

        firePanel = new FirePanel();
        fireContainer.add(firePanel, BorderLayout.CENTER);
        mainWrapper.add(fireContainer, BorderLayout.CENTER);

        add(mainWrapper, BorderLayout.CENTER);
    }

    private void initFullscreenToggle() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F11) {
                    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    if (gd.getFullScreenWindow() == GhostEvaluator.this) {
                        gd.setFullScreenWindow(null);
                        setExtendedState(JFrame.MAXIMIZED_BOTH);
                    } else {
                        dispose();
                        setUndecorated(true);
                        gd.setFullScreenWindow(GhostEvaluator.this);
                        setVisible(true);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    if (gd.getFullScreenWindow() == GhostEvaluator.this) {
                        dispose();
                        setUndecorated(false);
                        gd.setFullScreenWindow(null);
                        setExtendedState(JFrame.MAXIMIZED_BOTH);
                        setVisible(true);
                    }
                }
            }
        });
    }

    private void triggerVerdictFire() {
        int vivozVal = rows.get("Вывоз").getValue();
        int musicVal = rows.get("Понимание музыки").getValue();
        int repeatVal = rows.get("Лёгкость и репитативность").getValue();

        int totalScore = vivozVal + musicVal + repeatVal;
        firePanel.igniteWithScore(totalScore);
    }

    // Процедурный огонь, активирующийся только по кнопке
    private static class FirePanel extends JPanel implements ActionListener {
        private final int FIRE_W = 160;
        private final int FIRE_H = 180;
        private final int[][] fire = new int[FIRE_W][FIRE_H];
        private final Random rand = new Random();
        private final javax.swing.Timer timer;
        private int currentScore = 0;
        private boolean isActive = false;

        public FirePanel() {
            setOpaque(false);
            timer = new javax.swing.Timer(25, this);
        }

        public void igniteWithScore(int score) {
            this.currentScore = score;
            this.isActive = true;
            if (!timer.isRunning()) {
                timer.start();
            }
            repaint();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isActive) return;

            int threshold = 12;
            for (int x = 0; x < FIRE_W; x++) {
                fire[x][FIRE_H - 1] = (rand.nextInt(100) > threshold) ? (190 + rand.nextInt(65)) : 0;
            }

            for (int y = 0; y < FIRE_H - 1; y++) {
                for (int x = 1; x < FIRE_W - 1; x++) {
                    int decay = rand.nextInt(2);
                    int total = fire[x - 1][y + 1] + fire[x][y + 1] + fire[x + 1][y + 1] + fire[x][y];
                    int val = (total / 4) - decay;
                    fire[x][y] = Math.max(0, val);
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (!isActive) {
                // Плейсхолдер до клика по кнопке
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(new Color(65, 75, 95));
                FontMetrics fm = g2.getFontMetrics();
                String tip = "НАЖМИТЕ «СФОРМИРОВАТЬ ВЕРДИКТ» ДЛЯ ГЕНЕРАЦИИ ОГНЯ";
                g2.drawString(tip, (w - fm.stringWidth(tip)) / 2, (h / 2) + 5);
                g2.dispose();
                return;
            }

            // Фоновая аура для черного огня (счет 0)
            if (currentScore == 0) {
                GradientPaint bgAura = new GradientPaint(
                        0, h, new Color(45, 25, 65),
                        0, 0, new Color(18, 24, 36)
                );
                g2.setPaint(bgAura);
                g2.fillRect(0, 0, w, h);
            }

            double cellW = (double) w / FIRE_W;
            double cellH = (double) h / FIRE_H;

            for (int y = 0; y < FIRE_H; y++) {
                for (int x = 0; x < FIRE_W; x++) {
                    int val = fire[x][y];
                    if (val > 8) {
                        g2.setColor(getColorForScore(val, currentScore));
                        int px = (int) (x * cellW);
                        int py = (int) (y * cellH);
                        int pw = (int) Math.ceil(cellW);
                        int ph = (int) Math.ceil(cellH);
                        g2.fillRect(px, py, pw, ph);
                    }
                }
            }
            g2.dispose();
        }

        private Color getColorForScore(int val, int score) {
            float norm = Math.min(1.0f, val / 255.0f);

            if (score == 0) {
                int black = (int) ((1.0f - norm) * 20);
                int alpha = (int) (norm * 245);
                return new Color(black, black, black + 4, alpha);
            } else if (score == 8) {
                int bright = (int) (norm * 255);
                return new Color(bright, bright, bright, Math.min(255, (int)(val * 2.5f)));
            } else {
                float[] hueSteps = {0.0f, 0.0f, 0.82f, 0.65f, 0.50f, 0.33f, 0.18f, 0.10f};
                float hue = hueSteps[score];
                float brightness = 0.45f + (score / 8.0f) * 0.55f;
                Color base = Color.getHSBColor(hue, 1.0f - (norm * 0.22f), Math.min(1.0f, norm * brightness * 1.3f));
                return new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.min(255, val * 2));
            }
        }
    }

    // Карточка критерия
    private static class CriterionRow extends JPanel {
        private final JSlider slider;
        private final JLabel valueLabel;
        private final String[] grades;
        private final Color[] colors;

        public CriterionRow(String title, String[] grades, Color[] colors, int defaultVal) {
            this.grades = grades;
            this.colors = colors;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARD_BORDER, 1),
                    new EmptyBorder(12, 16, 12, 16)
            ));
            setMaximumSize(new Dimension(680, 115));
            setPreferredSize(new Dimension(680, 115));

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(TEXT_MAIN);
            top.add(titleLabel, BorderLayout.WEST);

            valueLabel = new JLabel(grades[defaultVal].toUpperCase());
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            valueLabel.setForeground(colors[defaultVal]);
            top.add(valueLabel, BorderLayout.EAST);
            add(top);
            add(Box.createRigidArea(new Dimension(0, 8)));

            slider = new JSlider(0, grades.length - 1, defaultVal);
            slider.setOpaque(false);
            slider.setSnapToTicks(true);
            slider.setPaintTicks(false);
            slider.setPaintLabels(false);

            slider.setUI(new GhostSliderUI(slider, colors, grades));
            slider.addChangeListener(e -> {
                int val = slider.getValue();
                valueLabel.setText(grades[val].toUpperCase());
                valueLabel.setForeground(colors[val]);
            });
            add(slider);
        }

        public int getValue() {
            return slider.getValue();
        }
    }

    // Слайдер с точным выравниванием бегунка строго над текстом
    private static class GhostSliderUI extends BasicSliderUI {
        private final Color[] colors;
        private final String[] labels;
        private static final int THUMB_W = 24;
        private static final int THUMB_H = 28;

        public GhostSliderUI(JSlider s, Color[] colors, String[] labels) {
            super(s);
            this.colors = colors;
            this.labels = labels;
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(super.getPreferredSize(c).width, 54);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(THUMB_W, THUMB_H);
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int trackH = 4;
            int cy = trackRect.y + (trackRect.height / 2) - (trackH / 2);

            g2.setColor(new Color(30, 41, 59));
            g2.fillRoundRect(trackRect.x, cy, trackRect.width, trackH, trackH, trackH);

            int thumbCenter = thumbRect.x + (thumbRect.width / 2);
            int fillWidth = Math.max(0, thumbCenter - trackRect.x);

            if (fillWidth > 0) {
                GradientPaint gp = new GradientPaint(
                        trackRect.x, cy, colors[0],
                        trackRect.x + trackRect.width, cy, colors[colors.length - 1]
                );
                g2.setPaint(gp);
                g2.fillRoundRect(trackRect.x, cy, fillWidth, trackH, trackH, trackH);
            }
            g2.dispose();
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = thumbRect.x;
            int y = thumbRect.y + 1;

            Path2D ghost = new Path2D.Double();
            ghost.moveTo(x + 4, y + 23);
            ghost.curveTo(x + 1, y + 17, x + 1, y + 6, x + 12, y + 2);
            ghost.curveTo(x + 23, y + 6, x + 23, y + 17, x + 20, y + 23);
            ghost.lineTo(x + 16, y + 20);
            ghost.lineTo(x + 12, y + 23);
            ghost.lineTo(x + 8, y + 20);
            ghost.closePath();

            g2.setColor(Color.WHITE);
            g2.fill(ghost);

            g2.setColor(new Color(20, 15, 35));
            g2.fillOval(x + 7, y + 9, 3, 4);
            g2.fillOval(x + 14, y + 9, 3, 4);

            g2.dispose();
        }

        @Override
        public void paintLabels(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            int textY = trackRect.y + trackRect.height + 16;
            int min = slider.getMinimum();
            int max = slider.getMaximum();

            for (int i = min; i <= max; i++) {
                int posX = xPositionForValue(i);
                String labelText = labels[i];
                int strW = fm.stringWidth(labelText);

                g2.setColor(TEXT_MUTED);
                g2.drawString(labelText, posX - (strW / 2), textY);
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GhostEvaluator app = new GhostEvaluator();
            app.setVisible(true);
        });
    }
}
