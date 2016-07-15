package zd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

import java.util.HashSet;

public class Waypoint implements Comparable {
    int x;
    int y;
    Waypoint parent;
    double cost;
    double heuristic;

    HashSet<Waypoint> neighbors;

    public Waypoint(int x, int y) {
        this.x = x;
        this.y = y;
        neighbors = new HashSet<Waypoint>();
        cost = 0;
        heuristic = 0;
    }

    void addNeighbor(Waypoint other) {
        this.neighbors.add(other);
    }

    public static void connectWaypoints(Waypoint a, Waypoint b) {
        a.addNeighbor(b);
        b.addNeighbor(a);
    }

    public boolean equals(Object o) {
        Waypoint other = (Waypoint) o;
        return (this.x == other.x && this.y == other.y);
    }

    public int hashCode() {
        return this.x * 31 + this.y;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.red);
        g.fillOval(x - 5, y - 5, 10, 10);
        for (Waypoint n : neighbors) {
            g.drawLine(x, y, n.x, n.y);
        }
    }

    public void drawDebug(Graphics2D g) {
        g.setColor(Color.pink);
        g.fillOval(x - 5, y - 5, 10, 10);
        for (Waypoint n : neighbors) {
            g.drawLine(x, y, n.x, n.y);
        }
    }

    public void setPathProperties(double c, double h, Waypoint p) {
        cost = c;
        heuristic = h;
        parent = p;
    }

    public int compareTo(Object o) {
        Waypoint other = (Waypoint) o;
        double diff = cost + heuristic - other.cost - other.heuristic;
        if (diff < 0) {
            return -1;
        } else if (diff > 0) {
            return 1;
        }
        return 0;
    }
}