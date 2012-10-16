public class Field {
    int[][] table;
    int[] col_heights;

    public Field(int[][] table) {
        this.table = table;
        col_heights = new int[table[0].length];

        for (int c = 0; c < table[0].length; c++) {
            col_heights[c] = 0;
            for (int r = 0; r < table.length; r++) {
            if (table[r][c] > 0) {
                col_heights[c] = table.length - r;
                break;
            }
            }
        }

        int ROWS = table.length, COLS = table[0].length;
        int rowstoshift = 0;
        for (int r = ROWS-1; r >= 0; r--) {
            boolean rowisfull = true;
            
            // if the entire row is full, empty it and increment rowstoshift
            for (int c = 0; c < COLS; c++) {
                rowisfull &= (table[r][c] > 0);
            }

            if (rowisfull) {
                for (int c = 0; c < COLS; c++) {
                    table[r][c] = 0;
                }
                rowstoshift++;
            }
            // otherwise shift the row "up" by rowstoshift
            else if (rowstoshift > 0) {
                for (int c = 0; c < COLS; c++) {
                    table[r+rowstoshift][c] = table[r][c];
                    table[r][c] = 0;
                }
            }
        }

        // decrement every col_height by rowstoshift
        for (int c = 0; c < COLS; c++) {
            col_heights[c] -= rowstoshift;
        }
    }

    public int heightOfCol(int col) {
        return col_heights[col];
    }
}
