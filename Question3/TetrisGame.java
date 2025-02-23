package Question3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class TetrisGame extends JFrame {
    // Game constants
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;
    private static final int PREVIEW_SIZE = 4;

    // Game data structures
    private int[][] gameBoard = new int[BOARD_HEIGHT][BOARD_WIDTH]; // 0 = empty, 1+ = block color
    private Queue<Block> blockQueue = new LinkedList<>();
    private Block currentBlock;
    private Timer timer;
    private int score = 0;

    // GUI components
    private JPanel gamePanel;
    private JPanel previewPanel;
    private JLabel scoreLabel;

    // Tetromino shapes (7 standard shapes)
    private static final int[][][] SHAPES = {
        {{1, 1, 1, 1}}, // I
        {{1, 1}, {1, 1}}, // O
        {{0, 1, 0}, {1, 1, 1}}, // T
        {{1, 0, 0}, {1, 1, 1}}, // L
        {{0, 0, 1}, {1, 1, 1}}, // J
        {{1, 1, 0}, {0, 1, 1}}, // S
        {{0, 1, 1}, {1, 1, 0}}  // Z
    };

    // Block class
    static class Block {
        int[][] shape;
        int color;
        int x, y;

        Block(int[][] shape, int color) {
            this.shape = shape;
            this.color = color;
            this.x = BOARD_WIDTH / 2 - shape[0].length / 2;
            this.y = 0;
        }

        void rotate() {
            int rows = shape.length;
            int cols = shape[0].length;
            int[][] newShape = new int[cols][rows];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    newShape[j][rows - 1 - i] = shape[i][j];
                }
            }
            shape = newShape;
        }
    }

    public TetrisGame() {
        // Frame setup
        setTitle("Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Game panel
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawBlock(g, currentBlock);
            }
        };
        gamePanel.setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));
        add(gamePanel, BorderLayout.CENTER);

        // Preview panel
        previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Block nextBlock = blockQueue.peek();
                if (nextBlock != null) drawPreview(g, nextBlock);
            }
        };
        previewPanel.setPreferredSize(new Dimension(PREVIEW_SIZE * BLOCK_SIZE, PREVIEW_SIZE * BLOCK_SIZE));
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.add(new JLabel("Next Block:"));
        sidePanel.add(previewPanel);

        // Score label
        scoreLabel = new JLabel("Score: 0");
        sidePanel.add(scoreLabel);
        add(sidePanel, BorderLayout.EAST);

        // Control buttons
        JPanel buttonPanel = new JPanel();
        JButton leftButton = new JButton("Left");
        JButton rightButton = new JButton("Right");
        JButton rotateButton = new JButton("Rotate");
        JButton restartButton = new JButton("Restart"); // Added Restart button
        buttonPanel.add(leftButton);
        buttonPanel.add(rightButton);
        buttonPanel.add(rotateButton);
        buttonPanel.add(restartButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        leftButton.addActionListener(e -> moveBlock(-1, 0));
        rightButton.addActionListener(e -> moveBlock(1, 0));
        rotateButton.addActionListener(e -> rotateBlock());
        restartButton.addActionListener(e -> restartGame()); // Restart action

        // Initialize game
        initializeGame();
        timer = new Timer(500, e -> updateGame());
        timer.start();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeGame() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                gameBoard[i][j] = 0;
            }
        }
        blockQueue.clear(); // Clear queue for new game
        currentBlock = generateRandomBlock();
        blockQueue.add(generateRandomBlock());
        score = 0;
        scoreLabel.setText("Score: " + score);
    }

    private void restartGame() {
        timer.stop(); // Stop current game loop
        initializeGame(); // Reset game state
        timer = new Timer(500, e -> updateGame()); // Restart timer
        timer.start();
        gamePanel.repaint();
        previewPanel.repaint();
    }

    private Block generateRandomBlock() {
        Random rand = new Random();
        int index = rand.nextInt(SHAPES.length);
        int[][] shape = new int[SHAPES[index].length][SHAPES[index][0].length];
        for (int i = 0; i < SHAPES[index].length; i++) {
            System.arraycopy(SHAPES[index][i], 0, shape[i], 0, SHAPES[index][i].length);
        }
        return new Block(shape, rand.nextInt(6) + 1); // Colors 1-6
    }

    private void updateGame() {
        if (!moveBlock(0, 1)) {
            placeBlock();
            checkRows();
            currentBlock = blockQueue.poll();
            blockQueue.add(generateRandomBlock());
            if (isGameOver()) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Game Over! Score: " + score);
            }
        }
        gamePanel.repaint();
        previewPanel.repaint();
    }

    private boolean moveBlock(int dx, int dy) {
        int newX = currentBlock.x + dx;
        int newY = currentBlock.y + dy;
        if (!collides(newX, newY, currentBlock.shape)) {
            currentBlock.x = newX;
            currentBlock.y = newY;
            gamePanel.repaint();
            return true;
        }
        return false;
    }

    private void rotateBlock() {
        int[][] originalShape = currentBlock.shape;
        currentBlock.rotate();
        if (collides(currentBlock.x, currentBlock.y, currentBlock.shape)) {
            currentBlock.shape = originalShape; // Revert if collision
        }
        gamePanel.repaint();
    }

    private boolean collides(int x, int y, int[][] shape) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int newX = x + j;
                    int newY = y + i;
                    if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT || (newY >= 0 && gameBoard[newY][newX] != 0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void placeBlock() {
        for (int i = 0; i < currentBlock.shape.length; i++) {
            for (int j = 0; j < currentBlock.shape[i].length; j++) {
                if (currentBlock.shape[i][j] != 0) {
                    gameBoard[currentBlock.y + i][currentBlock.x + j] = currentBlock.color;
                }
            }
        }
    }

    private void checkRows() {
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (gameBoard[i][j] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int k = i; k > 0; k--) {
                    System.arraycopy(gameBoard[k - 1], 0, gameBoard[k], 0, BOARD_WIDTH);
                }
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    gameBoard[0][j] = 0;
                }
                score += 100;
                scoreLabel.setText("Score: " + score);
                i++; // Re-check the same row after shifting
            }
        }
    }

    private boolean isGameOver() {
        for (int j = 0; j < BOARD_WIDTH; j++) {
            if (gameBoard[0][j] != 0) return true;
        }
        return false;
    }

    private void drawBoard(Graphics g) {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (gameBoard[i][j] != 0) {
                    g.setColor(getColor(gameBoard[i][j]));
                    g.fillRect(j * BLOCK_SIZE, i * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
                }
                g.setColor(Color.BLACK);
                g.drawRect(j * BLOCK_SIZE, i * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
            }
        }
    }

    private void drawBlock(Graphics g, Block block) {
        g.setColor(getColor(block.color));
        for (int i = 0; i < block.shape.length; i++) {
            for (int j = 0; j < block.shape[i].length; j++) {
                if (block.shape[i][j] != 0) {
                    g.fillRect((block.x + j) * BLOCK_SIZE, (block.y + i) * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
                }
            }
        }
    }

    private void drawPreview(Graphics g, Block block) {
        g.setColor(getColor(block.color));
        for (int i = 0; i < block.shape.length; i++) {
            for (int j = 0; j < block.shape[i].length; j++) {
                if (block.shape[i][j] != 0) {
                    g.fillRect(j * BLOCK_SIZE, i * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
                }
            }
        }
    }

    private Color getColor(int color) {
        switch (color) {
            case 1: return Color.RED;
            case 2: return Color.GREEN;
            case 3: return Color.BLUE;
            case 4: return Color.YELLOW;
            case 5: return Color.CYAN;
            case 6: return Color.MAGENTA;
            default: return Color.GRAY;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TetrisGame::new);
    }
}