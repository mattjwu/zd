package zd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

public class Wall {
    int row;
    int col;

    public Wall(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean equals(Object o) {
        Wall other = (Wall) o;
        return (this.row == other.row && this.col == other.col);
    }

    public int hashCode() {
        return this.row * 31 + this.col;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.black);
        g.fillRect(col * 20, row * 20, 20, 20);
    }
}