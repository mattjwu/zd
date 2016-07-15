package zd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import java.awt.event.KeyEvent;

import java.awt.event.MouseEvent;

public class Game {
    private ArrayList<Zombie> zombies;
    private Map map;
    private Selection selection;

    private PriorityQueue<GroundSquare> frontier;
    private HashSet<GroundSquare> closed;

    public Game() {
        zombies = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            zombies.add(new Zombie(50, i * 20 + 20));
        }
        map = new Map();
        selection = null;

        frontier = null;
        closed = null;
    }

    public void update() {
        for (Zombie z : zombies) {
            z.update();
        }
    }

    public void draw(Graphics2D g) {
        map.draw(g);
        sortZombies();
        for (Zombie z : zombies) {
            z.draw(g);
        }
        if (selection != null) {
            selection.draw(g);
        }
    }

    private void sortZombies() {
        for (int i = 1; i < zombies.size(); i++) {
            int index = i;
            boolean finished = false;
            Zombie current = zombies.get(index);
            while (!finished) {
                Zombie before = zombies.get(index - 1);
                if (current.compareTo(before) < 0) {
                    zombies.set(index, before);
                    zombies.set(index - 1, current);
                    index -= 1;
                    if (index == 0) {
                        finished = true;
                    }
                } else {
                    finished = true;
                }
            }
        }
    }

    public void movementClick(MouseEvent e) {
        frontier = null;
        closed = null;
        int x = e.getX();
        int y = e.getY();
        int initX = x;
        int initY = y;
        int mod2 = 0;
        int[] dir = new int[4];
        for (Zombie z : zombies) {
            if (z.isSelected) {
                if (!z.setGoal(x, y, map)) {
                    break;
                }
                if (frontier == null) {
                    frontier = new PriorityQueue<>();
                    closed = new HashSet<>();
                    int row, col;
                    if (z.path.isEmpty()) {
                        row = z.goalY / 10;
                        col = z.goalX / 10;
                    } else {
                        int[] p = z.path.getLast();
                        row = p[1] / 10;
                        col = p[0] / 10;
                    }
                    if (map.groundGrid[row][col] != map.OPEN) {
                        if (map.groundGrid[row - 1][col] != map.OPEN &&
                            map.groundGrid[row][col - 1] != map.OPEN) {
                            row -= 1;
                            col -= 1;
                        } else {
                            if (map.groundGrid[row - 1][col] == map.OPEN) {
                                row -= 1;
                            } else {
                                col -= 1;
                            }
                        }
                    }

                    mod2 = (row + col) % 2;

                    closed.add(new GroundSquare(row, col, 0));
                    /*int xOffset = (x % 10) - 5;
                    int yOffset = (y % 10) - 5;
                    boolean xPref = (Math.abs(xOffset) > Math.abs(yOffset));

                    if (xPref) {
                        if (xOffset < 0) {
                            dir[0] = map.LEFT;
                            dir[3] = map.RIGHT;
                        } else {
                            dir[0] = map.RIGHT;
                            dir[3] = map.LEFT;
                        }
                        if (yOffset < 0) {
                            dir[1] = map.UP;
                            dir[2] = map.DOWN;
                        } else {
                            dir[1] = map.DOWN;
                            dir[2] = map.UP;
                        }
                    } else {
                        if (xOffset < 0) {
                            dir[1] = map.LEFT;
                            dir[2] = map.RIGHT;
                        } else {
                            dir[1] = map.RIGHT;
                            dir[2] = map.LEFT;
                        }
                        if (yOffset < 0) {
                            dir[0] = map.UP;
                            dir[3] = map.DOWN;
                        } else {
                            dir[0] = map.DOWN;
                            dir[3] = map.UP;
                        }
                    }
                    for (int i = 0; i < 4; i++) {
                        int a = dir[i];
                        int dc = - (a % 2 - (a / 4) % 2);
                        int dr = - ((a / 2) % 2 - a / 8);
                        if (map.groundGrid[row + dr][col + dc] == map.OPEN) {
                            GroundSquare temp = new GroundSquare(row + dr, col + dc, i + 1);
                            if (!frontier.contains(temp)) {
                                frontier.add(temp);
                            }
                        }
                    }*/
                    for (int dr = -1; dr < 2; dr += 2) {
                        for (int dc = -1; dc < 2; dc += 2) {
                            if (map.groundGrid[row + dr][col + dc] == map.OPEN) {
                                int tempY = (row + dr) * 10 + 5;
                                int tempX = (col + dc) * 10 + 5;
                                int dx = (initX - tempX);
                                int dy = (initY - tempY);
                                int p = dx * dx + dy * dy;
                                GroundSquare temp = new GroundSquare(row + dr, col + dc, p);
                                if (!frontier.contains(temp)) {
                                    frontier.add(temp);
                                }
                            }
                        }
                    }
                }
                boolean setGoal = false;
                while (!setGoal) {
                    GroundSquare sq = frontier.poll();
                    if ((sq.col + sq.row) % 2 == mod2) {
                        x = sq.col * 10 + 2 + (int) (Math.random() * 7);
                        y = sq.row * 10 + 2 + (int) (Math.random() * 7);
                        setGoal = true;
                    }
                    closed.add(sq);
                    for (int dr = -1; dr < 2; dr += 2) {
                        for (int dc = -1; dc < 2; dc += 2) {
                            if (map.groundGrid[sq.row + dr][sq.col + dc] == map.OPEN) {
                                int tempY = (sq.row + dr) * 10 + 5;
                                int tempX = (sq.col + dc) * 10 + 5;
                                int dx = (initX - tempX);
                                int dy = (initY - tempY);
                                int p = dx * dx + dy * dy;
                                GroundSquare temp = new GroundSquare(sq.row + dr, sq.col + dc, p);
                                if (!closed.contains(temp) && map.groundGrid[temp.row][temp.col] == map.OPEN) {
                                    if (!frontier.contains(temp)) {
                                        frontier.add(temp);
                                    }
                                }
                            }
                        }
                    }
                    /*for (int i = 0; i < 4; i++) {
                        int a = dir[i];
                        int dc = - (a % 2 - (a / 4) % 2);
                        int dr = - ((a / 2) % 2 - a / 8);
                        GroundSquare temp = new GroundSquare(sq.row + dr, sq.col + dc, sq.pr + 5);
                        if (!closed.contains(temp) && map.groundGrid[temp.row][temp.col] == map.OPEN) {
                            if (!frontier.contains(temp)) {
                                frontier.add(temp);
                            }
                        }
                    }*/
                }
            }
        }
    }

    public void selectionStart(MouseEvent e) {
        for (Zombie z : zombies) {
            z.isSelected = false;
        }
        int x = e.getX();
        int y = e.getY();
        selection = new Selection(x, y);
    }

    public void adjustSelection(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        selection.changeEnd(x, y);
    }

    public void takeSelection() {
        if (selection != null) {
            selection.selectZombies(zombies);
            selection = null;
        }
    }

    static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
