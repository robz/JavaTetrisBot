import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class PracticeTetrisGame
{
    static int DIFX = 18, DIFY = 18, STARTX = 0, STARTY = 40,
               ROWS = 20, COLS = 10;

    static JLabel guiGrid[][];

    static Piece[] pieces = {
        new Piece(0xFFC225, Piece.piece_configs[0], -1, 4),
        new Piece(0x32BEFA, Piece.piece_configs[1], -1, 3),
        new Piece(0xFF7E25, Piece.piece_configs[2], -1, 3),
        new Piece(0x4464E9, Piece.piece_configs[3], -1, 3),
        new Piece(0xD24CAD, Piece.piece_configs[4], -1, 3),
        new Piece(0x7CD424, Piece.piece_configs[5], -1, 3),
        new Piece(0xFA325A, Piece.piece_configs[6], -1, 3)
    };

    public static void main(String[] args) {
        JLabel guiGrid[][] = new JLabel[ROWS][COLS];
        int field[][] = new int[ROWS][COLS];
        
        TetrisGame game = new TetrisGame(field, guiGrid);

        TetrisPlayer p = new TetrisPlayer(game);

        JFrame frame = new JFrame();
        frame.setContentPane(new TetrisPane(p, guiGrid));
        frame.setBounds(0, 20, DIFX*COLS, DIFY*ROWS);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setVisible(true);

        game.start();
    }

    public static class TetrisGame extends Thread {
        int field[][];
        JLabel guiGrid[][];
        Piece curPiece;
        int pieceIndex;
        
        public TetrisGame(int[][] field, JLabel[][] guiGrid) {
            this.field = field;
            this.guiGrid = guiGrid;
            makeNewPiece();    
        }

        public void makeNewPiece() {
            pieceIndex = (int)(Math.random()*7);
            curPiece = pieces[pieceIndex].deepcopy();
        }

        public void redraw() {
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    Color color = Color.white;
                    if (field[r][c] > 0) {
                        color = pieces[field[r][c]-1].colorObj;
                    } 
                    guiGrid[r][c].setBackground(color);
                }
            }

            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    int row = r + curPiece.offr,
                        col = c + curPiece.offc;
                    if (row >= 0 && row < ROWS && col >=0 && col < COLS &&
                            curPiece.fills(r,c)) {
                        guiGrid[row][col].setBackground(curPiece.colorObj);
                    } 
                }
            }
        }

        public void drop() {
            
        }

        public void rotate() {

        }

        public void left() {

        }

        public void right() {

        }

        public boolean pieceStopped() {
            int offr = curPiece.offr,
                offc = curPiece.offc;

            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    if (curPiece.fills(r,c)) {
                        if (r+offr+1 >= ROWS) {
                            return true;
                        }
                        if (r+offr+1 >= 0 && c+offc >= 0 && c+offc < COLS &&
                                field[r+offr+1][c+offc] > 0) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        public void copyPieceToField() {
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    int wr = r+curPiece.offr, wc = c+curPiece.offc;
                    if (wr >= 0 && wc >= 0 && wr < ROWS && wc < COLS &&
                            curPiece.fills(r,c)) {
                        field[curPiece.offr+r][curPiece.offc+c] = pieceIndex+1;
                    }
                }
            }
        }

        public void run() {
            boolean gameOver = false;

            while (!gameOver) {
                redraw();
                
                try{ sleep(100); } catch (Exception ex) { 
                    ex.printStackTrace();
                    System.exit(0);
                }

                if (pieceStopped()) {
                    copyPieceToField();
                    makeNewPiece();
                    gameOver = pieceStopped();
                }
            }

            System.out.println("game over dude!");
        }
    }

    public static class TetrisPlayer implements KeyListener {
        boolean pressed;
        TetrisGame game;
        
        public TetrisPlayer(TetrisGame game) {
            pressed = false;
            this.game = game;
        }
        
        public void keyPressed(KeyEvent e) {
            if (pressed) return;
            
            int keycode = e.getKeyCode();
        
            if (keycode == KeyEvent.VK_SPACE) {
                game.drop();
            } else if (keycode == KeyEvent.VK_UP) {
                game.rotate();
            } else if (keycode == KeyEvent.VK_LEFT) {
                game.left();
            } else if (keycode == KeyEvent.VK_RIGHT) {
                game.right();
            }

            pressed = true;
        }

        public void keyReleased(KeyEvent e) {
            pressed = false;
        }

        public void keyTyped(KeyEvent e) {}
    }

    public static class TetrisPane extends JPanel {
        public TetrisPane(KeyListener k, JLabel guiGrid[][]) {
            setLayout(new GridLayout(ROWS,COLS));

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    guiGrid[r][c] = new JLabel();
                    guiGrid[r][c].setBackground(Color.white);
                    guiGrid[r][c].setOpaque(true);
                    add(guiGrid[r][c]);
                }
            }

            setFocusable(true);
            addKeyListener(k);
        }        
    }
}
import javax.swing.*;
import java.awt.event.*;
