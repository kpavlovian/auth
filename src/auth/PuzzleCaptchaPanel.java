package auth;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Капча-пазл: изображение разбито на 4 фрагмента (2x2), перемешанных случайным образом.
 * Метод сборки — клик по двум фрагментам меняет их местами.
 */
public class PuzzleCaptchaPanel extends JPanel {
    private static final int TILE_COUNT = 4;
    private static final int TILE_SIZE = 110;

    private final BufferedImage[] originalTiles = new BufferedImage[TILE_COUNT];
    /** currentOrder[позиция на поле] = индекс фрагмента, лежащего в этой позиции. */
    private final int[] currentOrder = new int[TILE_COUNT];
    private final TileView[] tileViews = new TileView[TILE_COUNT];
    private int selectedPosition = -1;
    private final JLabel statusLabel = new JLabel(" ");

    public PuzzleCaptchaPanel() throws IOException {
        setLayout(new java.awt.BorderLayout(0, 6));
        loadTiles();

        JPanel board = new JPanel(new GridLayout(2, 2, 2, 2));
        board.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        for (int position = 0; position < TILE_COUNT; position++) {
            tileViews[position] = new TileView(position);
            board.add(tileViews[position]);
        }

        JButton refreshButton = new JButton("Перемешать заново");
        refreshButton.addActionListener(e -> shuffle());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottom.add(refreshButton);

        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        JPanel boardWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        boardWrapper.add(board);

        add(new JLabel("Соберите картинку: кликните на два фрагмента, чтобы поменять их местами",
                JLabel.CENTER), java.awt.BorderLayout.NORTH);
        add(boardWrapper, java.awt.BorderLayout.CENTER);
        JPanel south = new JPanel(new GridLayout(2, 1, 0, 4));
        south.add(statusLabel);
        south.add(bottom);
        add(south, java.awt.BorderLayout.SOUTH);

        shuffle();
    }

    /** Загружает фрагменты 1.png ... 4.png (порядок: слева-направо, сверху-вниз). */
    private void loadTiles() throws IOException {
        for (int i = 0; i < TILE_COUNT; i++) {
            String path = "/captcha/" + (i + 1) + ".png";
            try (InputStream stream = PuzzleCaptchaPanel.class.getResourceAsStream(path)) {
                if (stream == null) {
                    throw new IOException("Не найден файл фрагмента капчи: " + path);
                }
                BufferedImage source = ImageIO.read(stream);
                originalTiles[i] = scale(source, TILE_SIZE);
            }
        }
    }

    private static BufferedImage scale(BufferedImage source, int size) {
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(source, 0, 0, size, size, null);
        g.dispose();
        return result;
    }

    /** Случайно перемешивает фрагменты (гарантированно не оставляя собранную картинку). */
    public final void shuffle() {
        List<Integer> order = new ArrayList<>();
        do {
            order.clear();
            for (int i = 0; i < TILE_COUNT; i++) {
                order.add(i);
            }
            Collections.shuffle(order);
        } while (isOrderSolved(order));
        for (int position = 0; position < TILE_COUNT; position++) {
            currentOrder[position] = order.get(position);
        }
        selectedPosition = -1;
        statusLabel.setText(" ");
        repaint();
    }

    private static boolean isOrderSolved(List<Integer> order) {
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i) != i) {
                return false;
            }
        }
        return true;
    }

    /** Проверка правильности расположения фрагментов. */
    public boolean isSolved() {
        for (int position = 0; position < TILE_COUNT; position++) {
            if (currentOrder[position] != position) {
                return false;
            }
        }
        return true;
    }

    private void onTileClicked(int position) {
        if (selectedPosition < 0) {
            selectedPosition = position;
        } else {
            if (selectedPosition != position) {
                int buffer = currentOrder[selectedPosition];
                currentOrder[selectedPosition] = currentOrder[position];
                currentOrder[position] = buffer;
            }
            selectedPosition = -1;
        }
        statusLabel.setText(" ");
        repaint();
    }

    /** Отдельный фрагмент поля. */
    private class TileView extends JPanel {
        private final int position;

        TileView(int position) {
            this.position = position;
            setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onTileClicked(TileView.this.position);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(originalTiles[currentOrder[position]], 0, 0, null);
            if (position == selectedPosition) {
                g.setColor(new Color(0, 120, 215));
                g.setStroke(new BasicStroke(4));
                g.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
            }
        }
    }
}
