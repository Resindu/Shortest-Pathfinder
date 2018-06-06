
package testalgocw;


/**
 *creatd by : Katapodi Kankanamge Resindu Navoda
 * uow no: w1654203
 *registration no:2016352
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;
import javax.swing.*;
import javax.swing.event.*;
 
 
public class TestAlgoCW {
 
    public static JFrame gameFrame;  // The main form of the program
    
    public static void main(String[] args) {
        int width  = 693;
        int height = 545;
       gameFrame = new JFrame("Shortest path finder");
        gameFrame.setContentPane(new GamePanel(width,height));
        gameFrame.pack();
        gameFrame.setResizable(false);
 
        // the form is located in the center of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();
        double ScreenHeight = screenSize.getHeight();
        int x = ((int)screenWidth-width)/2;
        int y = ((int)ScreenHeight-height)/2;
 
       gameFrame.setLocation(x,y);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       gameFrame.setVisible(true);
    } // end main()
     
    /**
      *  contents of the main form
      * and contains all the functionality of the program.
      */
    public static class GamePanel extends JPanel {
         
        
        private class Cell {
            int row;   // the row number of the cell(row 0 is the top)
            int col;   // the column number of the cell (Column 0 is the left)
            int g;     // the value of the function g of A* 
            int h;     // the value of the function h of A* 
            int f;     // the value of the function h of A* 
            int dist;  // the distance of the cell from the initial position of the robot
             double h_cost; //heurastic cost          
            Cell prev; // Each state corresponds to a cell
                       // and each state has a predecessor which
                       // is stored in this variable
             
            public Cell(int row, int col){
               this.row = row;
               this.col = col;
            }
        } 
       
        /**
         * Auxiliary class that specifies that the cells will be sorted
         * according their 'f' field
         */
        private class CellComparatorByF implements Comparator<Cell>{
            @Override
            public int compare(Cell cell1, Cell cell2){
                return cell1.f-cell2.f;
            }
        } 
 
        
        private class MouseHandler implements MouseListener, MouseMotionListener {
            private int cur_row, cur_col, cur_val;
            @Override
            public void mousePressed(MouseEvent evt) {
                int row = (evt.getY() - 10) / squareSize;
                int col = (evt.getX() - 10) / squareSize;
                if (row >= 0 && row < rows && col >= 0 && col < columns && !searching && !found) {
                    cur_row = row;
                    cur_col = col;
                    cur_val = grid[row][col];
                  
                }
                repaint();
            }
 
            @Override
            public void mouseDragged(MouseEvent evt) {
                int row = (evt.getY() - 10) / squareSize;
                int col = (evt.getX() - 10) / squareSize;
                if (row >= 0 && row < rows && col >= 0 && col < columns && !searching && !found){
                    if ((row*columns+col != cur_row*columns+cur_col) && (cur_val == ROBOT || cur_val == TARGET)){
                        int new_val = grid[row][col];
                        if (new_val == EMPTY){
                            grid[row][col] = cur_val;
                            if (cur_val == ROBOT) {
                                robotStart.row = row;
                                robotStart.col = col;
                            } else {
                                targetPos.row = row;
                                targetPos.col = col;
                            }
                            grid[cur_row][cur_col] = new_val;
                            cur_row = row;
                            cur_col = col;
                            if (cur_val == ROBOT) {
                                robotStart.row = cur_row;
                                robotStart.col = cur_col;
                            } else {
                                targetPos.row = cur_row;
                                targetPos.col = cur_col;
                            }
                            cur_val = grid[row][col];
                        }
                              
                    }
                }
                repaint();
            }
 
            @Override
            public void mouseReleased(MouseEvent evt) { }
            @Override
            public void mouseEntered(MouseEvent evt) { }
            @Override
            public void mouseExited(MouseEvent evt) { }
            @Override
            public void mouseMoved(MouseEvent evt) { }
            @Override
            public void mouseClicked(MouseEvent evt) { }
             
        }
         
        
        // When the user presses a button performs the corresponding functionality       
        private class ActionHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String cmd = evt.getActionCommand();
               
                    
                
                 if (cmd.equals("Animation") && !endOfSearch) {
                   
                    searching = true;
                    message.setText(msgFindPath);                   
                    timer.setDelay(delay);
                    timer.start();
                }
            }
        } 
    
        /**
         * The class that is responsible for the animation
         */
        private class RepaintAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent evt) {
                
                //  If OPEN SET = [], then terminate. There is no solution.
              
                    expandNode();
                    if (found) {
                        timer.stop();
                        endOfSearch = true;
                        plotRoute();
                    }
                
                repaint();
            }
        } 
       
       
        private class  MyGame {
            private int dimensionX, dimensionY; // dimension of maze
            private int gridDimensionX, gridDimensionY; // dimension of output grid
            private char[][] gameGrid; // output grid
            private Cell[][] cells; // 2d array of Cells
            private Random random = new Random(); // The random object
 
            // initialize with x and y the same
            public  MyGame(int aDimension) {
                // Initialize
                this(aDimension, aDimension);
            }
            // constructor
            public  MyGame(int xDimension, int yDimension) {
                dimensionX = xDimension;
                dimensionY = yDimension;
                gridDimensionX = xDimension * 2 + 1;
                gridDimensionY = yDimension * 2 + 1;
                gameGrid = new char[gridDimensionX][gridDimensionY];
                init();
               generateGame();
            }
 
            private void init() {
                // create cells
                cells = new Cell[dimensionX][dimensionY];
                for (int x = 0; x < dimensionX; x++) {
                    for (int y = 0; y < dimensionY; y++) {
                        cells[x][y] = new Cell(x, y, false); // create cell (see Cell constructor)
                    }
                }
            }
 
            // inner class to represent a cell
            private class Cell {
                int x, y; // coordinates
                // cells this cell is connected to
                ArrayList<Cell> neighbors = new ArrayList<>();
                // impassable cell
                boolean wall = true;
                // if true, has yet to be used in generation
                boolean open = true;
                // construct Cell at x, y
                Cell(int x, int y) {
                    this(x, y, true);
                }
                // construct Cell at x, y and with whether it isWall
                Cell(int x, int y, boolean isWall) {
                    this.x = x;
                    this.y = y;
                    this.wall = isWall;
                }
                // add a neighbor to this cell, and this cell as a neighbor to the other
                void addNeighbor(Cell other) {
                    if (!this.neighbors.contains(other)) { // avoid duplicates
                        this.neighbors.add(other);
                    }
                    if (!other.neighbors.contains(this)) { // avoid duplicates
                        other.neighbors.add(this);
                    }
                }
                // used in updateGrid()
                boolean isCellBelowNeighbor() {
                    return this.neighbors.contains(new Cell(this.x, this.y + 1));
                }
                // used in updateGrid()
                boolean isCellRightNeighbor() {
                    return this.neighbors.contains(new Cell(this.x + 1, this.y));
                }
                // useful Cell equivalence
                @Override
                public boolean equals(Object other) {
                    if (!(other instanceof Cell)) return false;
                    Cell otherCell = (Cell) other;
                    return (this.x == otherCell.x && this.y == otherCell.y);
                }
 
                // should be    @Override
               
 
            }
            // generate from upper left (In computing the y increases down often)
            private void generateGame() {
                generateGame(0, 0);
            }
            // generate the maze from coordinates x, y
            private void generateGame(int x, int y) {
                generateGame(getCell(x, y)); // generate from Cell
            }
            private void generateGame(Cell startAt) {
                // don't generate from cell not there
                if (startAt == null) return;
                startAt.open = false; // indicate cell closed for generation
                ArrayList<Cell> cellsList = new ArrayList<>();
                cellsList.add(startAt);
 
                while (!cellsList.isEmpty()) {
                    Cell cell;
                    
                   
                    cell = cellsList.remove(cellsList.size() - 1);
                    // for collection
                    ArrayList<Cell> neighbors = new ArrayList<>();
                    // cells that could potentially be neighbors
                    Cell[] potentialNeighbors = new Cell[]{
                        getCell(cell.x + 1, cell.y),
                        getCell(cell.x, cell.y + 1),
                        getCell(cell.x - 1, cell.y),
                        getCell(cell.x, cell.y - 1)
                    };
                    for (Cell other : potentialNeighbors) {
                        // skip if outside, is a wall or is not opened
                        if (other==null || other.wall || !other.open) continue;
                        neighbors.add(other);
                    }
                    if (neighbors.isEmpty()) continue;
                    // get random cell
                    Cell selected = neighbors.get(random.nextInt(neighbors.size()));
                    // add as neighbor
                    selected.open = false; // indicate cell closed for generation
                    cell.addNeighbor(selected);
                    cellsList.add(cell);
                    cellsList.add(selected);
                }
                updateGrid();
            }
            // used to get a Cell at x, y; returns null out of bounds
            public Cell getCell(int x, int y) {
                try {
                    return cells[x][y];
                } catch (ArrayIndexOutOfBoundsException e) { // catch out of bounds
                    return null;
                }
            }
            // draw the maze
            public void updateGrid() {
                char backChar = ' ', wallChar = 'X', cellChar = ' ' ,wallChar2 = 'Y',wallChar3 = 'Z',wallChar4 = 'W' ;
                
                // build walls
                for (int x = 0; x < gridDimensionX; x ++) {
                    for (int y = 0; y < gridDimensionY; y ++) {
                       
                          gameGrid[20][20] = wallChar; //LAST ROW
                                        gameGrid[20][19] = wallChar;
                                         gameGrid[20][18] = wallChar;
                                         gameGrid[20][20] = wallChar; //LAST ROW
                                      gameGrid[20][19] = wallChar;
                                       gameGrid[20][18] = wallChar;
                                        gameGrid[20][17] = wallChar;
                                        gameGrid[20][16] = wallChar;
                                          gameGrid[20][15] = wallChar;
                                           gameGrid[20][14] = wallChar;
                                             gameGrid[20][13] = wallChar;
                                           gameGrid[20][12] = wallChar;
                                             
                                     gameGrid[20][20] = wallChar; //LAST COLUMN
                                       gameGrid[19][20] = wallChar;
                                      gameGrid[18][20] = wallChar;
                                        gameGrid[17][20] = wallChar;
                                         gameGrid[16][20] = wallChar;
                                         gameGrid[15][20] = wallChar;
                                           gameGrid[14][20] = wallChar;
                                           
                                      gameGrid[19][20] = wallChar; //LAST ROW
                                    gameGrid[19][19] = wallChar;
                                      gameGrid[19][18] = wallChar;
                                        gameGrid[19][17] = wallChar;
                                        gameGrid[19][16] = wallChar;
                                         gameGrid[19][15] = wallChar;
                                         gameGrid[19][14] = wallChar;
                                           gameGrid[19][13] = wallChar;
                                             gameGrid[19][12] = wallChar;
                                             
                                     gameGrid[18][20] = wallChar; //LAST ROW
                                     gameGrid[18][19] = wallChar;
                                       gameGrid[18][18] = wallChar;
                                         gameGrid[18][17] = wallChar;
                                         gameGrid[18][16] = wallChar;
                                         gameGrid[18][15] = wallChar;
                                            gameGrid[18][14] = wallChar;
                                           
                                          gameGrid[17][20] = wallChar; //LAST ROW
                                      gameGrid[17][19] = wallChar;
                                       gameGrid[17][18] = wallChar;
                                      gameGrid[17][17] = wallChar;
                                        gameGrid[17][16] =wallChar;
                                       gameGrid[17][15] = wallChar;
                                           gameGrid[17][14] = wallChar;
                                           
                                              
                                           gameGrid[16][20] = wallChar; //LAST ROW
                                       gameGrid[16][19] = wallChar;
                                      gameGrid[16][18] = wallChar;
                                       gameGrid[16][15] = wallChar;
                                         gameGrid[16][14] = wallChar;
                                          gameGrid[16][13] = wallChar;
                                          
                                              
                                           gameGrid[15][20] = wallChar; //LAST ROW
                                       gameGrid[15][13] = wallChar;
                                        gameGrid[15][12] = wallChar;
                                       
                                           
                                       gameGrid[14][20] =wallChar; //LAST ROW
                                     gameGrid[14][12] = wallChar;
                                       
                                    //   gameGrid[2][5] = wallChar2;
                                    //  gameGrid[2][6] = wallChar2;
                                     //  gameGrid[2][7] = wallChar2;
                                      //   gameGrid[2][8] = wallChar2;
                                          gameGrid[2][9] = wallChar2;
                                           gameGrid[2][10] = wallChar2;
                                            gameGrid[2][11] = wallChar2;
                                            gameGrid[3][9] = wallChar2;
                                           gameGrid[3][10] = wallChar2;
                                            gameGrid[3][11] = wallChar2;
                                            gameGrid[3][12] = wallChar2;
                                           gameGrid[4][10] = wallChar2;
                                            gameGrid[4][11] = wallChar2;
                                            gameGrid[4][12] = wallChar2;
                                            
                                             gameGrid[8][3] = wallChar2;
                                              //gameGrid[8][4] = wallChar2;
                                               gameGrid[9][3] = wallChar2;
                                                gameGrid[9][4] = wallChar2;
                                                 gameGrid[9][2] = wallChar2;
                                                  gameGrid[10][5] = wallChar2;
                                                 gameGrid[10][4] = wallChar2;
                                                 gameGrid[10][3] = wallChar2;
                                                 gameGrid[10][2] = wallChar2;
                                                 gameGrid[10][1] = wallChar2;
                                                 gameGrid[11][2] = wallChar2;
                                                  gameGrid[11][6] = wallChar2;
                                          // gameGrid[2][4] = wallChar2;
                                            gameGrid[2][3] = wallChar2;
                                      
                                             gameGrid[1][0] = wallChar3;
                                      gameGrid[2][0] = wallChar3;
                                       gameGrid[3][0] = wallChar3;
                                        gameGrid[6][0] = wallChar3;
                                        
                                         gameGrid[1][1] = wallChar3;
                                          gameGrid[2][1] = wallChar3;
                                           gameGrid[3][1] = wallChar3;
                                             gameGrid[5][1] = wallChar3;
                                             
                                              gameGrid[2][2] = wallChar3;
                                          gameGrid[3][2] = wallChar3;
                                           gameGrid[4][2] = wallChar3;
                                             gameGrid[5][2] = wallChar3;
                                             
                                             gameGrid[1][3] = wallChar3;
                                          gameGrid[2][3] = wallChar3;
                                           gameGrid[3][3] = wallChar3;
                                             gameGrid[3][4] = wallChar3;
                                            gameGrid[3][5] = wallChar3;
                                            // gameGrid[3][6] = wallChar3;
                                           
                                        //   gameGrid[1][4] = wallChar3;
                                          gameGrid[2][4] = wallChar3;
                                           gameGrid[2][5] = wallChar3;
                                          
                                           //gameGrid[1][5] = wallChar3;
                                         // gameGrid[2][5] = wallChar3;
                                          
                                          gameGrid[15][2] = wallChar3;
                                          gameGrid[15][3] = wallChar3;
                                          gameGrid[15][4] = wallChar3;
                                           gameGrid[15][5] = wallChar3;
                                           
                                          gameGrid[14][0] = wallChar3;
                                          gameGrid[14][1] = wallChar3;                                      
                                         gameGrid[14][2] = wallChar3;
                                         gameGrid[14][3] = wallChar3;
                                          gameGrid[14][4] = wallChar3;
                                         gameGrid[14][5] = wallChar3;
                                          gameGrid[14][6] = wallChar3;                                      
                                        gameGrid[14][7] = wallChar3;
                                         gameGrid[14][8] = wallChar3;
                                          gameGrid[14][9] = wallChar3;
                                          
                                           gameGrid[13][2] = wallChar3;
                                         gameGrid[13][3] = wallChar3;
                                          gameGrid[13][4] = wallChar3;
                                          gameGrid[13][5] = wallChar3;
                                         gameGrid[13][6] = wallChar3;                                      
                                          gameGrid[13][7] = wallChar3;
                                          gameGrid[13][8] = wallChar3;
                                          gameGrid[13][9] = wallChar3;
                                         gameGrid[13][10] = wallChar3;
                                          
                                          gameGrid[12][6] = wallChar3;
                                         gameGrid[12][7] = wallChar3;
                                          gameGrid[12][8] = wallChar3;
                                          gameGrid[12][9] = wallChar3;
                                          gameGrid[12][10] = wallChar3;
                                          gameGrid[11][8] = wallChar3;
                                          
                                           gameGrid[8][20] = wallChar3;
                                      gameGrid[8][19] = wallChar3;
                                      
                                      gameGrid[9][20] = wallChar3;
                                      gameGrid[9][19] = wallChar3;
                                       gameGrid[9][18] = wallChar3;
                                       
                                       gameGrid[10][19] = wallChar3;
                                         gameGrid[10][18] = wallChar3;                            
                                       gameGrid[10][17] = wallChar3;
                                         gameGrid[10][16] = wallChar3;                                         
                                        gameGrid[10][15] = wallChar3;
                                         gameGrid[10][14] = wallChar3;
                                           gameGrid[10][13] = wallChar3;
                                            
                                            gameGrid[11][16] = wallChar3;
                                             gameGrid[11][15] = wallChar3;
                                         gameGrid[11][14] = wallChar3;
                                            gameGrid[11][13] = wallChar3;
                                            
                                              gameGrid[12][15] = wallChar3;
                                         gameGrid[12][14] = wallChar3;
                                            gameGrid[12][13] = wallChar3;
                                            
                                                    gameGrid[18][0] = wallChar4;
                                      gameGrid[18][1] = wallChar4;
                                      
                                       gameGrid[17][1] = wallChar4;
                                        gameGrid[17][2] = wallChar4;
                                         gameGrid[17][3] = wallChar4;
                                          gameGrid[17][4] = wallChar4;
                                          gameGrid[17][5] = wallChar4;
                                           gameGrid[17][10] = wallChar4;
                                             gameGrid[16][11] = wallChar4;
                                              gameGrid[11][17] = wallChar4;
                                             gameGrid[15][11] = wallChar4;
                                            gameGrid[15][10] = wallChar4;
                                             
                                              gameGrid[15][11] = wallChar4;
                                             gameGrid[15][10] = wallChar4;
                                             
                                             gameGrid[12][1] = wallChar4;
                                             gameGrid[12][2] =wallChar4;
                                             
                                             gameGrid[11][1] =wallChar4;                                                                                          
                                          //   gameGrid[11][2] = wallChar4;
                                             gameGrid[11][3] =wallChar4;
                                             gameGrid[11][4] = wallChar4;
                                               gameGrid[11][5] = wallChar4;                                                                                         
                                           // gameGrid[11][6] = wallChar4;
                                             gameGrid[11][7] = wallChar4;
                                          //  gameGrid[11][8] = wallChar4;

                                             gameGrid[10][1] = wallChar4;                                                                                         
                                           //  gameGrid[10][2] = wallChar4;
                                            // gameGrid[10][3] = wallChar4;
                                           //  gameGrid[10][4] = wallChar4;
                                            //  gameGrid[10][5] = wallChar4;                                                                                        
                                            gameGrid[10][6] = wallChar4;
                                             gameGrid[10][7] = wallChar4;
                                          // gameGrid[10][8] = wallChar4;
                                             
                                             gameGrid[9][2] = wallChar4;
                                           //  gameGrid[9][3] =wallChar4;
                                            // gameGrid[9][4] = wallChar4;
                                              gameGrid[9][5] = wallChar4;                                                                                       
                                              gameGrid[9][6] = wallChar4;
                                            // gameGrid[9][7] = wallChar4;
                                             
                                             gameGrid[8][1] = wallChar4;
                                              gameGrid[8][3] = wallChar4;
                                           //  gameGrid[9][3] = wallChar4;
                                              
                                              gameGrid[7][1] = wallChar4;
                                               gameGrid[7][4] = wallChar4;
                                                gameGrid[7][5] = wallChar4;
                                                 gameGrid[7][16] = wallChar4;
                                                  gameGrid[7][17] = wallChar4;
                                                  
                                                  gameGrid[6][16] = wallChar4;
                                                  gameGrid[6][17] = wallChar4;
                                                gameGrid[6][18] = wallChar4;
                                                 gameGrid[6][14] = wallChar4;
                                                  gameGrid[6][13] =wallChar4;
                                                 gameGrid[6][12] = wallChar4;
                                                 gameGrid[6][11] = wallChar4;
                                                gameGrid[6][10] = wallChar4;
                                                gameGrid[6][9] = wallChar4;
                                                gameGrid[6][5] = wallChar4;
                                               gameGrid[6][4] = wallChar4;
                                                gameGrid[5][18] = wallChar4;
                                                gameGrid[5][17] = wallChar4;
                                              
                                                  gameGrid[5][14] = wallChar4;
                                                   gameGrid[5][13] = wallChar4;
                                                   gameGrid[5][12] = wallChar4;
                                                     gameGrid[5][11] = wallChar4;
                                                      gameGrid[5][10] = wallChar4;
                                                     gameGrid[5][9] = wallChar4;
                                                     gameGrid[5][8] = wallChar4;
                                                       
                                               
                                                  gameGrid[4][14] = wallChar4;
                                                   gameGrid[4][13] =wallChar4;
                                                   // gameGrid[4][12] = wallChar4;
                                                   //  gameGrid[4][11] = wallChar4;
                                                   //      gameGrid[4][10] = wallChar4;
                                                     gameGrid[4][9] = wallChar4;
                                                     gameGrid[4][8] = wallChar4;
                                                  
                                                     gameGrid[3][14] = wallChar4;
                                                   gameGrid[3][13] = wallChar4;
                                                  //  gameGrid[3][12] = wallChar4;
                                                  //   gameGrid[3][11] = wallChar4;
                                                   //      gameGrid[3][10] = wallChar4;
                                                   // gameGrid[3][9] = wallChar4;
                                                     gameGrid[3][8] = wallChar4;
                                                     
                                                     
                                                   
                                                   gameGrid[2][13] = wallChar4;
                                                    gameGrid[2][12] = wallChar4;
                                                   //  gameGrid[2][11] = wallChar4;
                                                   //     gameGrid[2][10] = wallChar4;
                                                  //   gameGrid[2][9] = wallChar4;
                                                    gameGrid[2][8] = wallChar4;
                                                     
                                                     
                                                   gameGrid[1][13] = wallChar4;
                                                    gameGrid[1][12] = wallChar4;
                                                     gameGrid[1][11] = wallChar4;
                                                        gameGrid[1][10] = wallChar4;
                                                     gameGrid[1][9] = wallChar4;
                                                     gameGrid[1][8] = wallChar4;
                                                      gameGrid[1][7] = wallChar4;
                                                      
                                                        gameGrid[0][13] = wallChar4;
                                                gameGrid[0][11] = wallChar4;
                                         
                                          
                                   
                    }
                }
           
             
                 
                // We create a clean grid ...
                searching = false;
                endOfSearch = false;
                fillGrid();
                // ... and copy into it the positions of obstacles
                // created by the maze construction algorithm
                for (int x = 0; x < gridDimensionX; x++) {
                    for (int y = 0; y < gridDimensionY; y++) {
                        if (gameGrid[x][y] == wallChar || gameGrid[x][y] == backChar && grid[x][y] != ROBOT && grid[x][y] != TARGET){
                            grid[x][y] = OBST;
                            
                        }
                         if (gameGrid[x][y] == wallChar2 || gameGrid[x][y] == backChar && grid[x][y] != ROBOT && grid[x][y] != TARGET){
                            grid[x][y] = OBST2;
                            
                        }
                         if (gameGrid[x][y] == wallChar3 || gameGrid[x][y] == backChar && grid[x][y] != ROBOT && grid[x][y] != TARGET){
                            grid[x][y] = OBST3;
                            
                        }
                          if (gameGrid[x][y] == wallChar4 || gameGrid[x][y] == backChar && grid[x][y] != ROBOT && grid[x][y] != TARGET){
                            grid[x][y] = OBST4;
                            
                        }
                    }
                }
            }
        } // end nested class MyMaze
         
        
         
        private final static int
            INFINITY = Integer.MAX_VALUE, // The representation of the infinite
            EMPTY    = 0,  // empty cell
            OBST4    = 1,   //cell with darkest shade of grey
            OBST3    = 2,   //cell with light shade of grey
            OBST2    = 3, //cell with midle shade of grey
            OBST     = 4,  // cell with white shade of 
            ROBOT    = 5,  // the position of the robot
            TARGET   = 6,  // the position of the target
            FRONTIER = 7,  // cells that form the frontier (OPEN SET)
            CLOSED   = 8,  // cells that form the CLOSED SET
            ROUTE    = 9;  // cells that form the robot-to-target path
         
        // Messages to the user
        private final static String
            msgStart =
                "click  'Start'",
            msgFindPath =
                "Click  'Find Path'",
            msgNoSolution =
                "There is no path to the target !!!";
 
        
         
        JTextField rowsField, columnsField;
         
        int rows    = 21,           // the number of rows of the grid
            columns = 21,           // the number of columns of the grid
            squareSize = 500/rows;  // the cell size in pixels
         
 
      //  int arrowSize = squareSize/2; // the size of the tip of the arrow
                                      
        ArrayList<Cell> openSet   = new ArrayList();// the OPEN SET
        ArrayList<Cell> closedSet = new ArrayList();// the CLOSED SET
        ArrayList<Cell> graph     = new ArrayList();// the set of vertices of the graph
                                                   
          
        Cell robotStart; // the initial position of the starting node
        Cell targetPos;  // the position of the target
       
        JLabel message;  // message to the user
         
        
 
        int[][] grid;        // the grid
        boolean found;       // flag that the goal was found
        boolean searching;   // flag that the search is in progress
        boolean endOfSearch; // flag that the search came to an end
        int delay;           // time delay of animation
        int expanded;        // the number of nodes that have been expanded
         
        // the object that controls the path finding
        RepaintAction action = new RepaintAction();
         
        // the Timer which governs the execution speed of the path finding
        Timer timer;
     
        //creating the panel
        public GamePanel(int width, int height) {
       
            setLayout(null);
             
            MouseHandler listener = new MouseHandler();
            addMouseListener(listener);
            addMouseMotionListener(listener);
 
            setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.blue));
 
            setPreferredSize( new Dimension(width,height) );
 
            grid = new int[rows][columns];
 
            // We create the contents of the panel
 
            message = new JLabel(msgStart, JLabel.CENTER);
            message.setForeground(Color.blue);
            message.setFont(new Font("Helvetica",Font.PLAIN,16));
 
           
 
            rowsField = new JTextField();
            rowsField.setText(Integer.toString(rows));
 
           
            columnsField = new JTextField();
            columnsField.setText(Integer.toString(columns));
 
           
 
            JButton startButton = new JButton("Start");
           startButton.addActionListener(new ActionHandler());
            startButton.setBackground(Color.lightGray);
            startButton.setToolTipText
                    ("Start the game");
            startButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                   startButtonActionPerformed(evt);
                }
            });
 
           
 
            
            JButton animationButton = new JButton("Animation");
            animationButton.addActionListener(new ActionHandler());
            animationButton.setBackground(Color.lightGray);
            animationButton.setToolTipText
                    ("The search is performed automatically");
 
           
             
           
             
       
 
            
 
            // we add the contents of the panel
            add(message);
        
            add(startButton);
       
            add(animationButton);
        
 
           
            message.setBounds(0, 515, 500, 23);
         
            rowsField.setBounds(665, 5, 25, 25);
        
            columnsField.setBounds(665, 35, 25, 25);
        
            startButton.setBounds(520, 95, 170, 25);
          
            animationButton.setBounds(520, 185, 170, 25);
         
            // create the timer
            timer = new Timer(delay, action);
             
            //  attach to cells in the grid initial values.
           
            fillGrid();
 
        } 
 
    
        /**
         * Function executed if the user presses the button "Start"
         */
        private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
            initializeGrid(true);
       
     }
     
   
        private void initializeGrid(Boolean makegame) {                                           
          
            squareSize = 500/(rows > columns ? rows : columns);
          
            grid = new int[rows][columns];
            robotStart = new Cell(rows-2,1);
            targetPos = new Cell(1,columns-2);
          
            
         
            if (makegame) {
                MyGame game = new  MyGame(rows/2,columns/2);
            } else {
                fillGrid();
            }
        } 
         
       
      
        /**
         * Expands a node and creates his successors
         */
        private void expandNode(){
          
                Cell current;
               
                  
                    //  Remove  from OPEN SET,
                   // (sort first OPEN SET list with respect to 'f')
                    Collections.sort(openSet, new CellComparatorByF());
                    current = openSet.remove(0);
                
                //  and add it to CLOSED SET.
                closedSet.add(0,current);
                // Update the color of the cell
                grid[current.row][current.col] = CLOSED;
                // If the selected node is the target ...
                if (current.row == targetPos.row && current.col == targetPos.col) {
                    // ... then terminate etc
                    Cell last = targetPos;
                    last.prev = current.prev;
                    closedSet.add(last);
                    found = true;
                    return;
                }
                // Count nodes that have been expanded.
                expanded++;
              
                //  Create the successors based on actions
               
                ArrayList<Cell> succesors;
                succesors = createSuccesors(current, false);
              
                //  For each successor ,
                for (Cell cell: succesors){
                    
                        int dxg = current.col-cell.col;
                        int dyg = current.row-cell.row;
                        int dxh = targetPos.col-cell.col;
                        int dyh = targetPos.row-cell.row;
                       
                     
                           
                            cell.g = current.g+Math.abs(dxg)+Math.abs(dyg);
                            cell.h = Math.abs(dxh)+Math.abs(dyh);
                            
                            cell.f = cell.g+cell.h;
                    
                        int openIndex   = isInList(openSet,cell);
                        int closedIndex = isInList(closedSet,cell);
                        if (openIndex == -1 && closedIndex == -1) {
                            // ... then add in the OPEN SET ...
                            // ... evaluated as f()
                            openSet.add(cell);
                            // Update the color of the cell
                            grid[cell.row][cell.col] = FRONTIER;
                        // Else ...
                        } else {
                            // ... if already belongs to the OPEN SET, then ...
                            if (openIndex > -1){
                                // ... compare the new value assessment with the old one. 
                                // If old <= new ...
                                if (openSet.get(openIndex).f <= cell.f) {
                                    // ... then eject the new node .
                                    // (ie do nothing for this node).
                                // Else, ...
                                } else {
                                    // ... remove the element  from the list
                                    // to which it belongs ...
                                    openSet.remove(openIndex);
                                    // ... and add the item  to the OPEN SET.
                                    openSet.add(cell);
                                    // Update the color of the cell
                                    grid[cell.row][cell.col] = FRONTIER;
                                }
                            // ... if already belongs to the CLOSED SET, then ...
                            } else {
                                // ... compare the new value assessment with the old one. 
                                // If old <= new ...
                                if (closedSet.get(closedIndex).f <= cell.f) {
                                    // ... then eject the new node  .
                                    // (ie do nothing for this node).
                                // Else, ...
                                } else {
                                    // ... remove the element  from the list
                                    // to which it belongs ...
                                    closedSet.remove(closedIndex);
                                    // ... and add the item  to the OPEN SET.
                                    openSet.add(cell);
                                    // Update the color of the cell
                                    grid[cell.row][cell.col] = FRONTIER;
                                }
                            }
                        }
                   // }
                }
            
        } 
         
        /**
         * Creates the successors of a cell
         * 
         * current       the cell for which we ask successors
         *  makeConnected flag that indicates that we are interested only on the coordinates
         *                      of cells and not on the label 'dist' (concerns only Dijkstra's)
         * @return              the successors of the cell as a list
         */
        private ArrayList<Cell> createSuccesors(Cell current, boolean makeConnected){
            int r = current.row;
            int c = current.col;
            //  create an empty list for the successors of the current cell.
            ArrayList<Cell> temp = new ArrayList<>();
            
             
            
            // and the up-side cell is not an obstacle ...
            if (r > 0 && grid[r-1][c] != OBST &&
                    
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                  
                          isInList(openSet,new Cell(r-1,c)) == -1 &&
                          isInList(closedSet,new Cell(r-1,c)) == -1){
                Cell cell = new Cell(r-1,c);
               
                    // update the pointer of the up-side cell so it points the current one ...
                    cell.prev = current;
                    // and add the up-side cell to the successors of the current one. 
                    temp.add(cell);
                 
            }
           
            // If not at the rightmost limit of the grid
            // and the right-side cell is not an obstacle ...
            if (c < columns-1 && grid[r][c+1] != OBST &&
                   
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                   
                          isInList(openSet,new Cell(r,c+1)) == -1 &&
                          isInList(closedSet,new Cell(r,c+1)) == -1){
                Cell cell = new Cell(r,c+1);
               
                    // ... update the pointer of the right-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the right-side cell to the successors of the current one. 
                    temp.add(cell);
                
            }
            
            // If not at the lowermost limit of the grid
            // and the down-side cell is not an obstacle ...
            if (r < rows-1 && grid[r+1][c] != OBST &&
                   
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                  
                          isInList(openSet,new Cell(r+1,c)) == -1 &&
                          isInList(closedSet,new Cell(r+1,c)) == -1) {
                Cell cell = new Cell(r+1,c);
              
                   // ... update the pointer of the down-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the down-side cell to the successors of the current one. 
                    temp.add(cell);
                }
            
        
            // If not at the leftmost limit of the grid
            // and the left-side cell is not an obstacle ...
            if (c > 0 && grid[r][c-1] != OBST && 
                   
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                
                          isInList(openSet,new Cell(r,c-1)) == -1 &&
                          isInList(closedSet,new Cell(r,c-1)) == -1) {
                Cell cell = new Cell(r,c-1);
              
                   // ... update the pointer of the left-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the left-side cell to the successors of the current one. 
                    temp.add(cell);
                
            }
            
           
           
            return temp;
        } // end createSuccesors()
         
      
         // Returns the index of the cell 'current' in the list 'list'
         
        // list    the list in which we seek
         // current the cell we are looking for
        //       the index of the cell in the list
       //              if the cell is not found returns -1
       
        private int isInList(ArrayList<Cell> list, Cell current){
            int index = -1;
            for (int i = 0 ; i < list.size(); i++) {
                if (current.row == list.get(i).row && current.col == list.get(i).col) {
                    index = i;
                    break;
                }
            }
            return index;
        } // end isInList()
         
      
         // Returns the predecessor of cell 'current' in list 'list'
         
       // list      the list in which seek
      // current   the cell  looking for
        
        private Cell findPrev(ArrayList<Cell> list, Cell current){
            int index = isInList(list, current);
            return list.get(index).prev;
        } // end findPrev()
         
   
         // Returns the distance between two cells
       
         // u the first cell
       // v the other cell
      //distance between the cells u and v
         
         private double MdistBetween(Cell u, Cell v){
            double distM;
            double dx = u.col-v.col;
            double dy = u.row-v.row;
                         
                distM = Math.abs(dx)+Math.abs(dy);
            
            return distM;
        } 
        
        
        //euclidean distance
         private double EdistBetween(Cell u, Cell v){
           double distx;
            double disty;
            double distE;
            int dx = u.col-v.col;
            int dy = u.row-v.row;
            
              
                distx = Math.abs(dx);
                   disty =  Math.abs(dy);
                 distE = Math.sqrt(distx*distx+disty*disty);
            return distE;
            
       
        } 
         //Chebyshev distance 
         private double CdistBetween(Cell u, Cell v){
           double distx;
            double disty;
            double distC;
            int dx = u.col-v.col;
            int dy = u.row-v.row;
            
               
                
                distx = Math.abs(dx);
                   disty =  Math.abs(dy);
                 distC = Math.max( distx, disty);
            return distC;
            
            
          
 
        } 
         //heuristic cost
         
         private double COST(Cell u, Cell v){
             double total_cost;
            double costM; // manhattan cost-+
            double costE;
             double costC;
            double dx = u.col-v.col;
            double dy = u.row-v.row;
                         
                costM = Math.abs(dx)+Math.abs(dy);
            
          
                  costE=Math.sqrt(Math.abs(dx)*Math.abs(dx)+Math.abs(dy)*Math.abs(dy));
           
                 costC = Math.max(  Math.abs(dx),  Math.abs(dy));
           
                 double h_cost=costC+costE+costM;
           
            return h_cost;
            
            
          
 
        } 
        //  double h_cost= costC+ costE+costM
                     
        /**
         * Calculates the path from the target to the initial position
         * of the robot, counts the corresponding steps
         * and measures the distance traveled.
         */
        private void plotRoute(){
            searching = false;
            endOfSearch = true;
            int steps = 0;
            double distance = 0;
            int index = isInList(closedSet,targetPos);
            Cell cur = closedSet.get(index);
            grid[cur.row][cur.col]= TARGET;
            do {
                steps++;
                
                    distance++;
                
                cur = cur.prev;
                grid[cur.row][cur.col] = ROUTE;
            } while (!(cur.row == robotStart.row && cur.col == robotStart.col));
            grid[robotStart.row][robotStart.col]=ROBOT;
            String msg;
            msg = String.format(" Distance: %.3f",
                     distance); 
            message.setText(msg);
           
        } 
         
       
        private void fillGrid() {
            if (searching || endOfSearch){ 
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        if (grid[r][c] == FRONTIER || grid[r][c] == CLOSED || grid[r][c] == ROUTE) {
                            grid[r][c] = EMPTY;
                        }
                        if (grid[r][c] == ROBOT){
                            robotStart = new Cell(r,c);
                        }
                        if (grid[r][c] == TARGET){
                            targetPos = new Cell(r,c);
                        }
                    }
                }
                searching = false;
            } else {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        grid[r][c] = EMPTY;
                    }
                }
                robotStart = new Cell(rows-2,1);
                targetPos = new Cell(1,columns-2);
            }
         
                robotStart.g = 0;
                robotStart.h = 0;
                robotStart.f = 0;
           // }
            expanded = 0;
            found = false;
            searching = false;
            endOfSearch = false;
          
           
            //  OPEN SET: = [So], CLOSED SET: = []
            openSet.removeAll(openSet);
            openSet.add(robotStart);
            closedSet.removeAll(closedSet);
          
            grid[targetPos.row][targetPos.col] = TARGET; 
            grid[robotStart.row][robotStart.col] = ROBOT;
          //  message.setText(msgDrawAndSelect);
            timer.stop();
            repaint();
             
        } // end fillGrid()
 
        /**
          * Appends to the list containing the nodes of the graph only
          * the cells belonging to the same connected component with node v.      
          * v    the starting node
          */
        private void findConnectedComponent(Cell v){
            Stack<Cell> stack;
            stack = new Stack();
            ArrayList<Cell> succesors;
            stack.push(v);
            graph.add(v);
            while(!stack.isEmpty()){
                v = stack.pop();
                succesors = createSuccesors(v, true);
                for (Cell c: succesors) {
                    if (isInList(graph, c) == -1){
                        stack.push(c);
                        graph.add(c);
                    }
                }
            }
        } 
         
    
        /**
         * paints the grid
         */
        @Override
        public void paintComponent(Graphics g) {
 
            super.paintComponent(g);  // Fills the background color.
 
            g.setColor(Color.DARK_GRAY);
            g.fillRect(10, 10, columns*squareSize+1, rows*squareSize+1);
 
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (grid[r][c] == EMPTY) {
                        g.setColor(Color.WHITE);
                    } else if (grid[r][c] == ROBOT) {
                        g.setColor(Color.RED);
                    } else if (grid[r][c] == TARGET) {
                        g.setColor(Color.GREEN);
                    } else if (grid[r][c] == OBST) {
                        g.setColor(Color.BLACK);
                      } else if (grid[r][c] == OBST2) {
                      g.setColor(Color.decode("#FFD700")); 
                      } else if (grid[r][c] == OBST3) {
                      g.setColor(Color.decode("#808080")); 
                       } else if (grid[r][c] == OBST4) {
                      g.setColor(Color.decode("#C0C0C0"));
                    } else if (grid[r][c] == FRONTIER) {
                        g.setColor(Color.GREEN);
                    } else if (grid[r][c] == CLOSED) {
                        g.setColor(Color.WHITE);
                    } else if (grid[r][c] == ROUTE) {
                        g.setColor(Color.YELLOW);
                    }
                    g.fillRect(11 + c*squareSize, 11 + r*squareSize, squareSize - 1, squareSize - 1);
                }
            }
            
           
        } 
         
       
         
    } 
} 