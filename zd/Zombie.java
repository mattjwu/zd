package zd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Zombie {
    double x;
    double y;

    int goalX;
    int goalY;

    boolean isSelected;

    private int speed;

    LinkedList<int[]> path;

    Waypoint startDebug;
    Waypoint goalPoint;

    private final static int SIZE = 20;

    public Zombie(int x, int y) {
        this.x = x;
        this.y = y;

        this.goalX = -999;
        this.goalY = -999;

        this.speed = 1;

        path = new LinkedList<>();
        startDebug = new Waypoint(0, 0);
        goalPoint = null;
        isSelected = false;
    }

    public void update() {
        move(speed);
    }

    private void move(double movement) {
        if (goalX != -999) {
            double dist = Game.distance(x, y, goalX, goalY);
            if (movement < dist) {
                this.x += movement * (goalX - x) / dist;
                this.y += movement * (goalY - y) / dist;
            } else {
                x = goalX;
                y = goalY;
                setTempGoal();
                move(movement - dist);
            }
        }
    }

    private void setTempGoal() {
        int[] next = path.pollFirst();
        if (next != null) {
            goalX = next[0];
            goalY = next[1];
        } else {
            goalPoint = null;
            goalX = -999;
            goalY = -999;
        }
    }

    public int compareTo(Zombie other) {
        if (this.y < other.y) {
            return -1;
        } else if (this.y > other.y) {
            return 1;
        } else if (this.x < other.x) {
            return -1;
        } else if (this.x > other.x) {
            return 1;
        }
        return 0;
    }

    public boolean setGoal(int newX, int newY, Map map) {
        //Returns true if a path was successfully found
        int[] trueGoal = findValidPoint(newX, newY, map);
        if (trueGoal[0] == -999) {
            return false;
        }
        findPath(trueGoal[0], trueGoal[1], map);
        setTempGoal();
        return true;
    }

    private int[] findValidPoint(int newX, int newY, Map map) {
        int row = newY / 10;
        int col = newX / 10;
        while (true) {
            int n = map.groundGrid[row][col];

            if (n == Map.OPEN) {
                return new int[] {newX, newY};
            } else if (n == Map.CLOSED) {
                return new int[] {-999, -999};
            }

            if (n == Map.NW || n == Map.NE || n == Map.SW || n == Map.SE) {
                int xOffset = (newX % 10) - 5;
                int yOffset = (newY % 10) - 5;
                boolean xPref = (Math.abs(xOffset) > Math.abs(yOffset));
                
                if (n == Map.NW) {
                    if (xPref) {
                        if (xOffset < 0) {
                            n = Map.LEFT;
                        } else {
                            n = Map.UP;
                        }
                    } else {
                        if (yOffset < 0) {
                            n = Map.UP;
                        } else {
                            n = Map.LEFT;
                        }
                    }
                } else if (n == Map.NE) {
                    if (xPref) {
                        if (xOffset > 0) {
                            n = Map.RIGHT;
                        } else {
                            n = Map.UP;
                        }
                    } else {
                        if (yOffset < 0) {
                            n = Map.UP;
                        } else {
                            n = Map.RIGHT;
                        }
                    }
                } else if (n == Map.SW) {
                    if (xPref) {
                        if (xOffset < 0) {
                            n = Map.LEFT;
                        } else {
                            n = Map.DOWN;
                        }
                    } else {
                        if (yOffset > 0) {
                            n = Map.DOWN;
                        } else {
                            n = Map.LEFT;
                        }
                    }
                } else {
                    if (xPref) {
                        if (xOffset > 0) {
                            n = Map.RIGHT;
                        } else {
                            n = Map.DOWN;
                        }
                    } else {
                        if (yOffset > 0) {
                            n = Map.DOWN;
                        } else {
                            n = Map.RIGHT;
                        }
                    }
                }
            }

            if (n == Map.LEFT) {
                col -= 1;
                newX = col * 10 + 10;
            } else if (n == Map.UP) {
                row -= 1;
                newY = row * 10 + 10;
            } else if (n == Map.RIGHT) {
                col += 1;
                newX = col * 10;
            } else {
                row += 1;
                newY = row * 10;
            }
        }
    }

    private void findPath(int newX, int newY, Map map) {
        //A Star algorithm for finding the shortest path
        PriorityQueue<Waypoint> frontier = new PriorityQueue<>();
        HashSet<Waypoint> closed = new HashSet<>();
        Waypoint start = new Waypoint((int) (x + .5), (int) (y + .5));
        Waypoint goal = new Waypoint(newX, newY);
        goalPoint = goal;

        startDebug = start;

        for (Waypoint w : map.waypoints) {
            if (map.checkStraightLine(x, y, w.x, w.y)) {
                start.addNeighbor(w);
            }
        }
        frontier.add(start);
        Waypoint current = start;
        while (frontier.size() > 0 && (current = frontier.poll()) != goal) {
            closed.add(current);
            if (map.checkStraightLine(current.x, current.y, newX, newY)) {
                double cost = current.cost + Game.distance(current.x, current.y, newX, newY);
                if (frontier.contains(goal) && cost < goal.cost) {
                    frontier.remove(goal);
                }
                if (!frontier.contains(goal)) {
                    goal.cost = cost;
                    goal.heuristic = 0;
                    goal.parent = current;
                    frontier.add(goal);
                }
            } else  {
                for (Waypoint n : current.neighbors) {
                    double cost = current.cost + Game.distance(current.x, current.y, n.x, n.y);
                    double heur = Game.distance(n.x, n.y, newX, newY);
                    if (frontier.contains(n) && cost < n.cost) {
                        frontier.remove(n);
                    }
                    if (!frontier.contains(n) && !closed.contains(n)) {
                        n.cost = cost;
                        n.heuristic = heur;
                        n.parent = current;
                        frontier.add(n);
                    }
                }
            }
        }
        //Reconstruct the path
        path = new LinkedList<>();
        while (current.parent != null) {
            path.addFirst(new int[] {current.x, current.y});
            current = current.parent;
        }
    }

    public void draw(Graphics2D g) {
        /*g.setColor(new Color(255, 255, 255));
        g.fillOval((int) (x + .5 - SIZE / 2), (int) (y + .5 - SIZE / 2),
                    SIZE, SIZE);
        if (isSelected) {
            g.setColor(new Color(100, 100, 100));
        } else {
            g.setColor(new Color(0, 0, 0));
        }
        g.fillArc((int) (x + 1.5 - SIZE / 2), (int) (y + .5 - SIZE / 2),
                    SIZE, SIZE, -90, 180);
        g.fillOval((int) (x + .5 - SIZE / 4), (int) (y + .5),
                    SIZE / 2, SIZE / 2);
        g.setColor(new Color(255, 255, 255));
        g.fillOval((int) (x + .5 - SIZE / 12), (int) (y + .5 + SIZE / 6),
                    SIZE / 6, SIZE / 6);
        g.fillOval((int) (x + .5 - SIZE / 4), (int) (y + .5) - SIZE / 2,
                    SIZE / 2, SIZE / 2);
        if (isSelected) {
            g.setColor(new Color(100, 100, 100));
        } else {
            g.setColor(new Color(0, 0, 0));
        }
        g.fillOval((int) (x + .5 - SIZE / 12), (int) (y + .5 - SIZE / 3),
                    SIZE / 6, SIZE / 6);

        g.drawOval((int) (x + .5 - SIZE / 2), (int) (y + .5 - SIZE / 2),
                    SIZE, SIZE);*/

        
        if (isSelected) {
            g.setColor(new Color(50, 100, 255));
        } else {
            g.setColor(Color.blue);
        }
        g.fillOval((int) (x + .5 - SIZE / 2),
                   (int) (y + .5 - SIZE / 2), SIZE, SIZE);
        g.setColor(Color.green);
        g.fillOval((int) (x + .5 - SIZE / 4),
                   (int) (y + .5 - SIZE / 4), SIZE / 2, SIZE / 2);

        //startDebug.drawDebug(g);
        if (goalPoint != null) {
            goalPoint.drawDebug(g);
        }
        
    }
}
