import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;

/**
 * This class implements a BDD logic for the n-queens problem
 *
 *  @author Hugo, David, Weisi
 *  @version 01.04.2019
 */
public class BDDLogic implements IQueensLogic {
    private int size;
    private int[][] board;
    private BDDFactory fact;
    private BDD True;
    private BDD False;
    private BDD queen;

    /**
     * Initializes the quadratic board with the given size and initializes the board according to the rules
     * of the n-queen problem.
     *
     * @param size The size of the board ( i.e. size = #rows = #columns)
     */
    @Override
    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];

        // A BDDFactory is used to produce the variables and constants (True and False) that we are going to use.
        // We can use it to create BDD class with the same condition from the BDDFactory
        // It needs to be initialized with the number of variables to be used.
        this.fact = JFactory.init(2000000, 200000); // The two numbers represents the node number and the cache size.
        // Not so important - see project description for a useful size.
        // The number of variables to be used.
        this.fact.setVarNum(size * size);
        // Not necessary, just for make the code clear
        this.True = fact.one();
        this.False = fact.zero();
        // Init the final BDD as True, so the rest of BDD rules need to be calculate by "and".
        this.queen = True;
        // Create the restrict rules
        restrictRules();

        updateBoard();
    }

    /**
     * Create the restrict rules
     * Rule1: each row has at least one queen (cannot be empty)
     * Rule2,3,4: Check the row,col,diagonal restricting of each position
     */
    private void restrictRules() {

        // Rule1: each row has at least one queen (cannot be empty), queen's assignment issue
        for (int row = 0; row < size; row++) {
            BDD bddAss = this.False;

            for (int col = 0; col < size; col++) {
                bddAss = bddAss.or(this.fact.ithVar(position(col, row)));
            }

            //bddAss must be true
            this.queen = this.queen.and(bddAss);
        }

        // Rule2,3,4: Check the row,col,diagonal restricting of each position
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                // init the board of the current position as False
                BDD bddRCD_board = this.False;
                bddRCD_board = bddRCD_board.or(this.fact.nithVar(position(col, row)));
//                bddRCD_board.printSet();

                // Create a BDD for Row_Col_Diagonal rules FOR THE CURRENT POSITION
                BDD bddRCD = this.True;
                // the board is a square, so just loop the size
                for (int i = 0; i < size; i++) {
                    if (i != row) { // it is not itself
                        // same row cannot has other queen
                        bddRCD = bddRCD.and(this.fact.nithVar(position(col, i)));
                        if (row + col - i > 0 && row + col - i < size) {
                            // diagonal from top-left to bottom-right cannot has other queen
                            bddRCD = bddRCD.and(this.fact.nithVar(position(row + col - i, i)));
                        }
                    }
                    if (i != col) {
                        // same col cannot has other queen
                        bddRCD = bddRCD.and(this.fact.nithVar(position(i, row)));
                        if (row - col + i > 0 && row - col + i < size) {
                            // diagonal from top-right to bottom-left cannot has other queen
                            bddRCD = bddRCD.and(this.fact.nithVar(position(i, row - col + i)));
                        }
                    }
                }
                //
                bddRCD_board = bddRCD_board.or(bddRCD);
                this.queen = this.queen.and(bddRCD_board);
            }
        }
    }

    /**
     * Return the position in a BDD
     *
     * @param row
     * @param col
     * @return the position in a BDD
     */
    private int position(int col, int row) {
        return row * this.size + col;
    }

    /**
     * update the board
     */
    private void updateBoard() {
        // check each position (after insert or start the game)
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                // isZero()
                // BDD.restrict():
                // Restrict a set of variables to constant values.
                // Restricts the variables in this BDD to constant true if they are included in their positive form in var,
                // and constant false if they are included in their negative form.
                if (this.queen.restrict(this.fact.ithVar(position(col, row))).equals(this.False)) {
                    this.board[col][row] = -1;
                }
            }
        }

        // It only one empty position in a row, then mark as 1 (has to be a Queen)
        for (int row = 0; row < size; row++) {
            int count = 0;
            int y = 0;
            int x = 0;
            for (int col = 0; col < size; col++) {
                if (this.board[col][row] == 0) {
                    y = col;
                    x = row;
                    count++;
                }
            }
            if (count == 1) {
                this.board[y][x] = 1;
            }
        }
    }

    /**
     * Return a representation of the board where each entry [c][r] is either
     * 1 : a queen is or must be present in column c and row r,
     * -1 : a queen cannot be present in column c and row r
     * 0 : a queen has not yet been placed in column c and row r. It does not have to
     * be there but it is allowed to be there.
     * Columns are counted from left to right (starting with 0),
     * and rows are counted from top to bottom (counting from 0).
     */
    @Override
    public int[][] getBoard() {
        return board;
    }

    /**
     * Inserts a queen at the specified position and updates the rest of the board accordingly,
     * that is afterwards the board specifies where there _must_ be queens and where there _cannot_ be queens.
     */
    @Override
    public void insertQueen(int col, int row) {

        if (board[col][row] == -1 || board[col][row] == 1) {
            return;
        }
        // set a queen in this position
        board[col][row] = 1;
        // implement the decision in the BDD
        this.queen = this.queen.restrict(this.fact.ithVar(position(col, row)));

        updateBoard();
    }
}
