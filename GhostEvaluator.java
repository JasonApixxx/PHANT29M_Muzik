import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;

public class GhostEvaluator extends JFrame {

    // Палитра темной темы
    private static final Color BG_DARK = new Color(11, 15, 23);
    private static final Color CARD_BG = new Color(18, 24, 36);
    private static final Color CARD_BORDER = new Color(31, 41, 61);
    private static final Color TEXT_MAIN = new Color(243, 244, 246);
    private static final Color TEXT_MUTED = new Color(156, 163, 175);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);

    // Градации и цвета
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
        setSize(680, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(25, 25, 20, 25));

        // Шапка с названием PHANT29M
        JLabel header = new JLabel("PHANT29M");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setFont(new Font("Segoe UI Black", Font.BOLD, 30));
        header.setForeground(ACCENT_PURPLE);
        root.add(header);
        root.add(Box.createRigidArea(new Dimension(0, 20)));

        // Карточки критериев
        rows.put("Вывоз", new CriterionRow("Вывоз", GRADES_VIVOZ, COLORS_VIVOZ));
        rows.put("Понимание музыки", new CriterionRow("Понимание музыки", GRADES_MUSIC, COLORS_MUSIC));
        rows.put("Лёгкость и репитативность", new CriterionRow("Лёгкость и репитативность", GRADES_REPEAT, COLORS_REPEAT));

        for (CriterionRow row : rows.values()) {
            root.add(row);
            root.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        // Кнопка вердикта
        JButton calcBtn = new JButton("СФОРМИРОВАТЬ ВЕРДИКТ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(121, 40, 202), getWidth(), 0, new Color(255, 0, 128));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        calcBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        calcBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        calcBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setContentAreaFilled(false);
        calcBtn.setBorderPainted(false);
        calcBtn.setFocusPainted(false);
        calcBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calcBtn.addActionListener(e -> triggerVerdictFire());

        root.add(calcBtn);
        root.add(Box.createRigidArea(new Dimension(0, 15)));

        // Зона огня вместо текстового поля
        firePanel = new FirePanel();
        firePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        firePanel.setPreferredSize(new Dimension(600, 180));
        root.add(firePanel);

        add(new JScrollPane(root));
    }

    private void triggerVerdictFire() {
        int vivozVal = rows.get("Вывоз").getValue();
        int musicVal = rows.get("Понимание музыки").getValue();
        int repeatVal = rows.get("Лёгкость и репитативность").getValue();

        // Максимум: 3 + 3 + 2 = 8
        int totalScore = vivozVal + musicVal + repeatVal;
        double percentage = (totalScore / 8.0) * 100.0;

        Color fireColor;
        String verdictText;

        if (percentage >= 85) {
            fireColor = new Color(168, 85, 247); // Фиолетовый (максимум)
            verdictText = "ВЕРЮ // КОСМОС";
        } else if (percentage >= 60) {
            fireColor = new Color(0, 210, 255);  // Голубой
            verdictText = "НЕПЛОХО // ОБЛАКА";
        } else if (percentage >= 35) {
            fireColor = new Color(255, 153, 51); // Оранжевый
            verdictText = "ПОПЫТКА // ВЕЛИК";
        } else {
            fireColor = new Color(255, 51, 102); // Красный
            verdictText = "НЕВЫВОЗ // ДНО";
        }

        firePanel.ignite(fireColor, verdictText);
    }

    // Компонент живого огня
    private static class FirePanel extends JPanel {
        private final List<Particle> particles = new ArrayList<>();
        private final Random random = new Random();
        private Color flameColor = new Color(168, 85, 247);
        private String bannerText = "";
        private boolean active = false;

        public FirePanel() {
            setBackground(CARD_BG);
            setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));

            // Таймер физики и отрисовки частиц (~60 FPS)
            javax.swing.Timer timer = new javax.swing.Timer(16, e -> {
                if (active) {
                    updateParticles();
                    repaint();
                }
            });
            timer.start();
        }

        public void ignite(Color color, String text) {
            this.flameColor = color;
            this.bannerText = text;
            this.active = true;
            particles.clear();
        }

        private void updateParticles() {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            // Порождаем новые частицы огня
            for (int i = 0; i < 7; i++) {
                float px = (float) (w / 2.0 + (random.nextGaussian() * (w * 0.22)));
                float py = h - 10;
                float vx = (float) (random.nextGaussian() * 0.8);
                float vy = -(2.5f + random.nextFloat() * 4.0f);
                float size = 18f + random.nextFloat() * 22f;
                particles.add(new Particle(px, py, vx, vy, size));
            }

            // Обновляем состояние
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.x += p.vx;
                p.y += p.vy;
                p.life -= 0.022f;
                p.size *= 0.965f;
                if (p.life <= 0 || p.size <= 2f) {
                    it.remove();
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!active) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(new Color(75, 85, 99));
                FontMetrics fm = g2.getFontMetrics();
                String tip = "НАЖМИ «СФОРМИРОВАТЬ ВЕРДИКТ» ЧТОБЫ ЗАЖЕЧЬ ОГОНЬ";
                g2.drawString(tip, (getWidth() - fm.stringWidth(tip)) / 2, getHeight() / 2 + 5);
                g2.dispose();
                return;
            }

            // Рендерим пламя частицами
            for (Particle p : particles) {
                float alpha = Math.max(0f, Math.min(1f, p.life));
                // Смешение основного цвета пламени с белым в ядре
                int red = Math.min(255, (int) (flameColor.getRed() + (255 - flameColor.getRed()) * (1f - alpha * 0.5f)));
                int green = Math.min(255, (int) (flameColor.getGreen() + (255 - flameColor.getGreen()) * (1f - alpha * 0.7f)));
                int blue = Math.min(255, (int) (flameColor.getBlue() + (255 - flameColor.getBlue()) * (1f - alpha * 0.5f)));

                g2.setColor(new Color(red, green, blue, (int) (alpha * 170)));
                g2.fillOval((int) (p.x - p.size / 2), (int) (p.y - p.size / 2), (int) p.size, (int) p.size);
            }

            // Неоновый текст вердикта прямо поверх огня
            g2.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(bannerText)) / 2;
            int ty = (getHeight() / 2) + 8;

            // Теневое свечение
            g2.setColor(new Color(11, 15, 23, 200));
            g2.drawString(bannerText, tx + 2, ty + 2);

            // Основной текст в тон пламени
            g2.setColor(Color.WHITE);
            g2.drawString(bannerText, tx, ty);

            g2.dispose();
        }

        private static class Particle {
            float x, y, vx, vy, size, life = 1.0f;

            Particle(float x, float y, float vx, float vy, float size) {
                this.x = x;
                this.y = y;
                this.vx = vx;
                this.vy = vy;
                this.size = size;
            }
        }
    }

    // Панель карточки критерия
    private static class CriterionRow extends JPanel {
        private final JSlider slider;
        private final JLabel valueLabel;
        private final String[] grades;
        private final Color[] colors;

        public CriterionRow(String title, String[] grades, Color[] colors) {
            this.grades = grades;
            this.colors = colors;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARD_BORDER, 1),
                    new EmptyBorder(12, 16, 12, 16)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

            // Название критерия и бейдж
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(TEXT_MAIN);
            top.add(titleLabel, BorderLayout.WEST);

            valueLabel = new JLabel(grades[0].toUpperCase());
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            valueLabel.setForeground(colors[0]);
            top.add(valueLabel, BorderLayout.EAST);
            add(top);
            add(Box.createRigidArea(new Dimension(0, 8)));

            // Ползунок с призраком
            slider = new JSlider(0, grades.length - 1, 0);
            slider.setOpaque(false);
            slider.setUI(new GhostSliderUI(slider, colors));
            slider.addChangeListener(e -> {
                int val = slider.getValue();
                valueLabel.setText(grades[val].toUpperCase());
                valueLabel.setForeground(colors[val]);
            });
            add(slider);
            add(Box.createRigidArea(new Dimension(0, 4)));

            // Подписи градаций
            JPanel ticks = new JPanel(new GridLayout(1, grades.length));
            ticks.setOpaque(false);
            for (int i = 0; i < grades.length; i++) {
                JLabel tick = new JLabel(grades[i]);
                tick.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                tick.setForeground(TEXT_MUTED);
                if (i == 0) tick.setHorizontalAlignment(SwingConstants.LEFT);
                else if (i == grades.length - 1) tick.setHorizontalAlignment(SwingConstants.RIGHT);
                else tick.setHorizontalAlignment(SwingConstants.CENTER);
                ticks.add(tick);
            }
            add(ticks);
        }

        public int getValue() {
            return slider.getValue();
        }
    }

    // Слайдер с бегунком-призраком
    private static class GhostSliderUI extends BasicSliderUI {
        private final Color[] colors;

        public GhostSliderUI(JSlider s, Color[] colors) {
            super(s);
            this.colors = colors;
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(28, 30);
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int trackHeight = 6;
            int trackY = trackRect.y + (trackRect.height - trackHeight) / 2;

            g2.setColor(new Color(30, 41, 59));
            g2.fillRoundRect(trackRect.x, trackY, trackRect.width, trackHeight, 4, 4);

            int fillWidth = thumbRect.x + thumbRect.width / 2 - trackRect.x;
            if (fillWidth > 0) {
                GradientPaint gp = new GradientPaint(
                        trackRect.x, 0, colors[0],
                        trackRect.x + trackRect.width, 0, colors[colors.length - 1]
                );
                g2.setPaint(gp);
                g2.fillRoundRect(trackRect.x, trackY, fillWidth, trackHeight, 4, 4);
            }
            g2.dispose();
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = thumbRect.x;
            int y = thumbRect.y;

            Path2D ghost = new Path2D.Double();
            ghost.moveTo(x + 5, y + 14);
            ghost.curveTo(x + 5, y + 4, x + 23, y + 4, x + 23, y + 14);
            ghost.lineTo(x + 23, y + 25);
            ghost.lineTo(x + 19, y + 22);
            ghost.lineTo(x + 14, y + 26);
            ghost.lineTo(x + 9, y + 22);
            ghost.lineTo(x + 5, y + 25);
            ghost.closePath();

            int currVal = slider.getValue();
            Color activeColor = (currVal < colors.length) ? colors[currVal] : ACCENT_PURPLE;

            g2.setColor(activeColor);
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(ghost);

            g2.setColor(Color.WHITE);
            g2.fill(ghost);

            g2.setColor(BG_DARK);
            g2.fillOval(x + 9, y + 10, 3, 4);
            g2.fillOval(x + 16, y + 10, 3, 4);

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
