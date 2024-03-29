import java.awt.*;
import java.awt.event.*;

class JavaTetrisBot {
    static int ROWS = 20, COLS = 10;

    public static void press(Robot r, int key, int times) {
        for (int i = 0; i < times; i++) {
            r.keyPress(key);
            r.delay(100); 
            r.keyRelease(key);
            r.delay(100);
        }
    }

    public static void main(String[] args) {
        System.out.println("hi!");
        Robot robot;
        try {
             robot = new Robot();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        robot.delay(1000);
        
        int field[][] = new int[ROWS][COLS];

        while(true) {
            int pieceIndex = getCurrentPiece(robot);

            if (pieceIndex >= 0 && pieceIndex < 7) {
                System.out.printf("current piece: %c\n", pieceTypes[pieceIndex+1]);
                executeBestMove(field, pieceIndex, robot);
                dispField(field);
                robot.delay(200);
            }

            robot.delay(10);
        }      
        
    }

/* Evaluate fitness of placing piece at location on board 
 * (the larger the fitness value, the worse the placement is) 
 * */

    static double getPlacementFitness(int[][] field, Piece p, Field gameField) {
        int width = p.getCurPosWidth(),
            leftpad = p.getCurPosLeftPad();
        
        // count how many edges there are after placement
        int num_edges = 400;

        int[][] difs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (p.isIn(r, c) || field[r][c] > 0) {
                    for (int i = 0; i < difs.length; i++) {
                        int newc = c+difs[i][0],
                            newr = r+difs[i][1];
                        if (newr < 0 || newr >= ROWS) {
                        } else if (newc < 0 || newc >= COLS) {
                            num_edges--;
                        } else if (p.isIn(newr, newc) || field[newr][newc] > 0) {
                            num_edges--;
                        }    
                    }
                }
            }
        }
        
        // find height added
        int max_height = 0;
        
        for (int c = 0; c < width; c++) {
            int h = gameField.heightOfCol(c+leftpad+p.offc)
                    + p.getCurPosColHeight(c);
            if (h > max_height) {
                max_height = h;
            }
        }

        // count # of lines cleared from placement
        int lines_not_cleared = 20;
        
        for (int r = 0; r < ROWS; r++) {
            boolean willbecleared = true;
            for (int c = 0; c < COLS; c++) {
                willbecleared &= (p.isIn(r, c) || field[r][c] > 0);
            }
            if (willbecleared) lines_not_cleared--;
        }

        // count # of spaces created from placement
        int space_count = 0;

        int startCol = p.offc + leftpad, 
            endCol = startCol + width;

        for (int c = startCol; c < endCol; c++) {
            int startRow = p.offr + p.getCurPosColHeight(c - p.offc - leftpad);
            for (int r = startRow; r < ROWS; r++) {
                if (field[r][c] > 0) {
                    break;
                }
                space_count++;
            }
        }

        return space_count*.4 + lines_not_cleared*.3 + max_height*.3 + .01*num_edges;
    }

/* Find and execute best move give a piece and a board */
    
    static class Move {
        int offc, orient;
        Piece p;
        public Move(int offc, int orient, Piece p) {
            this.offc = offc;
            this.orient = orient;
            this.p = p;
        }
    }

    static void executeBestMove(int[][] field, int pieceIndex, Robot robot) {
        Move m = findBest(field, pieceIndex);
        
        int width = m.p.getCurPosWidth(),
            leftpad = m.p.getCurPosLeftPad();
       
        for (int r = m.p.offr; r < m.p.offr + 4; r++) {
            for (int c = m.p.offc + leftpad; c < m.p.offc + leftpad + width; c++) {
                if (r >= 0 && r < 20 && m.p.isIn(r, c)) {
                    field[r][c] = pieceIndex+1;
                }
            }
        }
        
        m.offc = m.offc - pieces[pieceIndex].offc;
        executeMove(m, robot);
    }
    
    static void executeMove(Move m, Robot r) { 
        System.out.println("best move:");
        
        if (m.orient > 0) {
            System.out.println("  rotate "+m.orient);
            press(r, KeyEvent.VK_UP, m.orient);
        }

        if (m.offc > 0) {
            System.out.println("  right "+m.offc);
            press(r, KeyEvent.VK_RIGHT, m.offc);
        } else if (m.offc < 0) {
            System.out.println("  left "+(-m.offc));
            press(r, KeyEvent.VK_LEFT, -m.offc);
        }

        System.out.println("down!");
        press(r, KeyEvent.VK_SPACE, 1);
    }

    static Move findBest(int[][] field, int pieceIndex) {
        Piece p = pieces[pieceIndex].deepcopy();
        Field f = new Field(field);
        
        double best_fitness = 99999;
        int best_offc = -1,
            best_orient = -1,
            best_offr = -1;

        // loop over orientations
        for (int orientIndex = 0; orientIndex < p.positions.length; orientIndex++) {
            int width = p.getCurPosWidth(),
                leftpad = p.getCurPosLeftPad(),
                initColOff = -leftpad, 
                lastColOff = initColOff + COLS - width;
       
            // loop over placements
            for (int c = initColOff; c <= lastColOff; c++) {
                p.offc = c;
                
                int farthestrow = 20;

                // find farthest possible row down
                for (int ic = c+leftpad; ic < width+c+leftpad; ic++) {
                    int pieceHeight = p.getCurPosColHeight(ic - p.offc - leftpad);
                    int fieldHeight = f.heightOfCol(ic);
                    int row = (ROWS - fieldHeight) - pieceHeight;
                    
                    if (row < farthestrow) {
                        farthestrow = row;
                    }
                }

                p.offr = farthestrow;
                
                double fitness = getPlacementFitness(field, p, f);
                if (fitness < best_fitness) {
                    best_fitness = fitness;
                    best_offc = p.offc;
                    best_orient = p.cur_pos;
                    best_offr = p.offr;  
                } 
            }

            p.rotate();
        }

        p.offr = best_offr;
        p.offc = best_offc;
        p.cur_pos = best_orient;

        return new Move(best_offc, best_orient, p);
    }

    static void printPiece(Piece p, int[][] field) {
        System.out.println("+---------------------+");
        int leftpad = p.getCurPosLeftPad(),
            width = p.getCurPosWidth();
        for (int r = -1; r < field.length; r++) {
            System.out.print("|");
            for (int c = 0; c < field[0].length; c++) {
                if (p.offc+leftpad <= c && c < p.offc+leftpad+width && p.isIn(r,c)) {
                    System.out.print(" X");
                } else if (r > -1 && field[r][c] > 0) {
                    System.out.print(" -");
                } else {
                    System.out.print("  ");
                }
            }
            System.out.println(" |");
        }
        System.out.println("+--------------------+");
    }


/* Determining starting piece */    

    
    static int STARTPIECE_FIRSTCOL = 3;

    static int getCurrentPiece(Robot robot) {
        int topleftx = STARTX+DIFX/2,
            toplefty = STARTY+DIFY/2;

        int pieceType = -1;

        int c = 4;
        for (int r = 0; r < 1; r++) {
            Color color = robot.getPixelColor(topleftx+DIFX*c, toplefty+DIFY*r);
            pieceType = getSquareType(color.getRGB()&0xFFFFFF);
            if (pieceType > 0 && pieceType < BLOCK_INDEX+1) {
                break;
            } 
        }

        return pieceType-1;
    }




/* Converting the field to an array */

    static int //STARTX = 6, STARTY = 52,
               //STARTX = 106, STARTY = 304,
               //STARTX = 298, STARTY = 289, 
               STARTX = 400, STARTY = 440,
               DIFX = 18, DIFY = 18;

    static int BLOCK_INDEX = 7;
    static int[] block_colors = {
        0xB9B9B9, 0xBABABA, 0xBBBBBB, 0xBCBCBC
    };

    static int STARTER_ROW = 2;

    public static void getField(Robot robot, int[][] field) {
        int topleftx = STARTX+DIFX/2, toplefty = STARTY+DIFY/2;

        for (int r = STARTER_ROW; r < field.length; r++) {
            for (int c = 0; c < field[r].length; c++) {
                Color color = robot.getPixelColor(topleftx+c*DIFX,
                                                  toplefty+r*DIFY);
                field[r][c] = getSquareType(color.getRGB()&0xFFFFFF);
            }
        }
    }

    public static int getSquareType(int color) {
        for (int i = 0; i < pieces.length; i++) {
            if (color == pieces[i].color) {
                return i+1;
            }
        }

        for (int i = 0; i < block_colors.length; i++) {
            if (color == block_colors[i]) {
                return BLOCK_INDEX+1;
            }
        } 

        return 0;
    }

    static char pieceTypes[] = {
        ' ', 'Y', 'L', 'O', 'B', 'P', 'G', 'R', '-'
    }; 

    public static void dispField(int[][] field) {
        System.out.println("+---------------------+");
        for (int r = ROWS/2; r < field.length; r++) {
            System.out.print("|");
            for (int c = 0; c < field[r].length; c++) {
                System.out.print(" "+pieceTypes[field[r][c]]);
            }
            System.out.println(" |");
        }
        System.out.println("+---------------------+");
    }
    
    static Piece[] pieces = {
        new Piece(0xFFC225, Piece.piece_configs[0], -1, 4),
        new Piece(0x32BEFA, Piece.piece_configs[1], -1, 3),
        new Piece(0xFF7E25, Piece.piece_configs[2], -1, 3),
        new Piece(0x4464E9, Piece.piece_configs[3], -1, 3),
        new Piece(0xD24CAD, Piece.piece_configs[4], -1, 3),
        new Piece(0x7CD424, Piece.piece_configs[5], -1, 3),
        new Piece(0xFA325A, Piece.piece_configs[6], -1, 3)
    };
}
