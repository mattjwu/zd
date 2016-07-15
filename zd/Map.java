package zd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

public class Map {
    /*
    Map is represented by a Grid of integers
    Currently:
    0 is Empty
    1 is Wall
    */
    int[][] wallGrid;
    /*
    Another grid (twice as large) that represents grid sections that zombies can reach,
    and squares that are blocked point in which direction the nearest open square is
    */
    int[][] groundGrid;

    /*
    Another grid that represents the squares zombies should spread to on clicks
    */
    int[][] spreadGrid;

    static final int OPEN = 0;
    static final int LEFT = 1;
    static final int UP = 2;
    static final int RIGHT = 4;
    static final int DOWN = 8;
    static final int NE = 6;
    static final int NW = 3;
    static final int SW = 9;
    static final int SE = 12;
    static final int CLOSED = -1;

    private static final int GROUNDGRIDDEPTH = 4;

    HashSet<Waypoint> waypoints;
    HashSet<Wall> walls;

    private static final double CHECKLINETHICKNESS = 9.95;

    public Map() {
        try {
            String fileName = "resources/levels/test.txt";
            File file = new File(fileName);
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            int intRead = -1;
            int numRows = 0;
            int numCols = 0;
            
            //Sets number of rows from text file
            while ((intRead = bufferedReader.read()) != '\r') {
                numRows *= 10;
                numRows += (intRead - 48);
            }
            bufferedReader.read(); //Gets rid of \n
            //Sets number of cols from text file
            while ((intRead = bufferedReader.read()) != '\r') {
                numCols *= 10;
                numCols += (intRead - 48);
            }
            bufferedReader.read(); //Gets rid of \n

            wallGrid = new int[numRows][numCols];
            
            walls = new HashSet<>();
            waypoints = new HashSet<>();

            int curRow = 0;
            int curCol = 0;
            while ((intRead = bufferedReader.read()) != -1) {
                switch (intRead) {
                    case '_':
                        wallGrid[curRow][curCol] = 0;
                        curCol += 1;
                        break;
                    case 'W':
                        wallGrid[curRow][curCol] = 1;
                        walls.add(new Wall(curRow, curCol));
                        curCol += 1;
                        break;
                    default:
                        break;
                }
                if (curCol >= numCols) {
                    curCol = 0;
                    curRow += 1;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Something went wrong");
        }

        this.createGroundGrid();
        this.findWaypoints();
        this.connectWaypoints();
    }

    private void createGroundGrid() {
        groundGrid = new int[wallGrid.length * 2][wallGrid[0].length * 2];
        for (int row = 0; row < groundGrid.length; row++) {
            for (int col = 0; col < groundGrid[0].length; col++) {
                if (row == 0 || col == 0 || row == groundGrid.length - 1 ||
                    col == groundGrid[0].length - 1) {
                    groundGrid[row][col] = -1;
                } else {
                    int topRow = (row - 1) / 2;
                    int leftCol = (col - 1) / 2;
                    if (checkBlocked(topRow, leftCol) || checkBlocked(topRow + 1, leftCol) ||
                        checkBlocked(topRow, leftCol + 1) || checkBlocked(topRow + 1, leftCol + 1)) {
                        groundGrid[row][col] = -1;
                    } else {
                        groundGrid[row][col] = 0;
                    }
                }
            }
        }
        for (int i = 0; i < GROUNDGRIDDEPTH; i++) {
            LinkedList<int[]> gridChanges = new LinkedList();
            for (int row = 0; row < groundGrid.length; row++) {
                for (int col = 0; col < groundGrid[0].length; col++) {
                    if (groundGrid[row][col] == CLOSED) {
                        int val = 0;
                        if (getGridVal(row - 1, col) != CLOSED) {
                            val += UP;
                        }
                        if (getGridVal(row, col - 1) != CLOSED) {
                            val += LEFT;
                        }
                        if (getGridVal(row, col + 1) != CLOSED) {
                            val += RIGHT;
                        }
                        if (getGridVal(row + 1, col) != CLOSED) {
                            val += DOWN;
                        }
                        if (val != 0) {
                            gridChanges.add(new int[] {row, col, val});
                        }
                    }
                }
            }
            for (int[] change : gridChanges) {
                groundGrid[change[0]][change[1]] = change[2];
            }
        }
    }

    private int getGridVal(int row, int col) {
        if (row < 0 || col < 0 || row >= groundGrid.length || col >= groundGrid[0].length) {
            return -1;
        }
        return groundGrid[row][col];
    }

    private void findWaypoints() {
        for (Wall w : walls) {
            int row = w.row;
            int col = w.col;

            for (int dRow = -1; dRow < 2; dRow += 2) {
                for (int dCol = -1; dCol < 2; dCol += 2) {
                    int tempRow = row + dRow;
                    int tempCol = col + dCol;
                    if (tempRow >= 0 && tempRow < wallGrid.length &&
                        tempCol >= 0 && tempCol < wallGrid[0].length) {
                        validWaypointHelper(tempRow, tempCol);
                    }
                }
            } 
        }
    }

    private void validWaypointHelper(int row, int col) {
        if (!(checkBlocked(row - 1, col) ||
            checkBlocked(row + 1, col) ||
            checkBlocked(row, col - 1) ||
            checkBlocked(row, col + 1) ||
            checkBlocked(row, col))) {
            Waypoint temp = new Waypoint(col * 20 + 10, row * 20 + 10);
            if (!waypoints.contains(temp)) {
                waypoints.add(temp);
            }
        }
    }

    private void connectWaypoints() {
        for (Waypoint w1 : waypoints) {
            for (Waypoint w2 : waypoints) {
                if (!w1.equals(w2) && !w1.neighbors.contains(w2)) {
                    if (checkStraightLine(w1.x, w1.y, w2.x, w2.y)) {
                        Waypoint.connectWaypoints(w1, w2);
                    }
                }
            }
        }
    }

    private boolean checkBlocked(int row, int col) {
        if (row < 0 || row >= wallGrid.length || col < 0 || col >= wallGrid[0].length) {
            return true;
        }
        if (wallGrid[row][col] == 1) {
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.white);
        for (Wall wall : walls) {
            wall.draw(g);
        }
        /*for (Waypoint w : waypoints) {
            w.draw(g);
        }
        for (int row = 0; row < groundGrid.length; row++) {
            for (int col = 0; col < groundGrid[0].length; col++) {
                int n = groundGrid[row][col];
                switch (n) {
                    case CLOSED:
                        g.setColor(new Color(255, 0, 0, 50));
                        break;
                    case OPEN:
                        g.setColor(new Color(0, 255, 0, 50));
                        break;
                    case LEFT:
                    case UP:
                    case RIGHT:
                    case DOWN:
                        g.setColor(new Color(0, 0, 255, 50));
                        break;
                    case NW:
                    case NE:
                    case SW:
                    case SE:
                        g.setColor(new Color(0, 255, 255, 50));
                        break;
                    default:
                        break;
                }
                g.fillRect(col * 10, row * 10, 10, 10);
            }
        }*/
    }

    boolean checkStraightLine(double x1, double y1, double x2, double y2) {
        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);

        int multiplier = -1;
        if ((y2 > y1 && x2 > x1) || (y2 < y1 && x2 < x1)) {
            multiplier = 1;
        }
        double tx1 = x1 + multiplier * CHECKLINETHICKNESS;
        double ty1 = y1 - CHECKLINETHICKNESS;
        double tx2 = x2 + multiplier * CHECKLINETHICKNESS;
        double ty2 = y2 - CHECKLINETHICKNESS;
        double bx1 = x1 - multiplier * CHECKLINETHICKNESS;
        double by1 = y1 + CHECKLINETHICKNESS;
        double bx2 = x2 - multiplier * CHECKLINETHICKNESS;
        double by2 = y2 + CHECKLINETHICKNESS;

        return (this.checkLineHelper(tx1, ty1, tx2, ty2, dx, dy) &&
                this.checkLineHelper(bx1, by1, bx2, by2, dx, dy));
    }

    private boolean checkLineHelper(double x1, double y1, double x2, double y2, double dx, double dy) {
        /*
        Algorithm adapted from James McNeill
        http://playtechs.blogspot.com/2007/03/raytracing-on-grid.html
        */
        int colStart = (int) Math.floor(x1 / 20);
        int colEnd = (int) Math.floor(x2 / 20);
        int rowStart = (int) Math.floor(y1 / 20);
        int rowEnd = (int) Math.floor(y2 / 20);

        int n = 1;
        int x_inc, y_inc;
        double error;

        if (dx == 0) {
            x_inc = 0;
            error = Double.POSITIVE_INFINITY;
        } else if (x2 > x1) {
            x_inc = 1;
            error = (20 * Math.floor(x1 / 20) + 20 - x1) * dy;
            n += colEnd - colStart;
        } else {
            x_inc = -1;
            error = (x1 - 20 * Math.floor(x1 / 20)) * dy;
            n += colStart - colEnd;
        }

        if (dy == 0) {
            y_inc = 0;
            error = Double.NEGATIVE_INFINITY;
        } else if (y2 > y1) {
            y_inc = 1;
            error -= (20 * Math.floor(y1 / 20) + 20 - y1) * dx;
            n += rowEnd - rowStart;
        } else {
            y_inc = -1;
            error -= (y1 - 20 * Math.floor(y1 / 20)) * dx;
            n += rowStart - rowEnd;
        }
        error /= 20;
        int r = rowStart;
        int c = colStart;
        for (int i = 0; i < n; i++) {
            if (checkBlocked(r, c)) {
                return false;
            }
            if (error > 0) {
                r += y_inc;
                error -= dx;
            } else {
                c += x_inc;
                error += dy;
            }
        }
        return true;
    }
}