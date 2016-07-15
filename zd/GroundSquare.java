package zd;

public class GroundSquare implements Comparable {
    int row;
    int col;
    int pr;

    public GroundSquare(int r, int c, int p) {
        row = r;
        col = c;
        pr = p;
    }

    public boolean equals(Object o) {
        GroundSquare other = (GroundSquare) o;
        return (row == other.row && col == other.col);
    }

    public int hashCode() {
        return row * 31 + col;
    }

    public int compareTo(Object o) {
        GroundSquare other = (GroundSquare) o;
        return pr - other.pr;
    }
}
