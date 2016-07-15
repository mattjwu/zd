package zd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

import java.util.List;

public class Selection {
    private int startX;
    private int startY;

    private int endX;
    private int endY;

    private boolean rectangleSelection;
    private static final int DRAGRADIUS = 10;

    public Selection(int x, int y) {
        startX = x;
        startY = y;
        endX = x;
        endY = y;
        rectangleSelection = false;
    }

    public void changeEnd(int x, int y) {
        endX = x;
        endY = y;
        int dx = endX - startX;
        int dy = endY - startY;
        if (!rectangleSelection) {
            if (dx * dx + dy * dy > DRAGRADIUS * DRAGRADIUS) {
                rectangleSelection = true;
            }
        }
    }

    public void draw(Graphics2D g) {
        if (rectangleSelection) {
            int x = Math.min(startX, endX);
            int y = Math.min(startY, endY);
            int width = Math.abs(endX - startX);
            int height = Math.abs(endY - startY);
            g.setColor(new Color(127, 127, 127, 127));
            g.fillRect(x, y, width, height);
        }
    }

    public void selectZombies(List<Zombie> zombies) {
        if (rectangleSelection) {
            for (Zombie z : zombies) {
                if (insideSelection(z)) {
                    z.isSelected = true;
                }
            }
        } else {
            for (int i = zombies.size() - 1; i >= 0; i--) {
                Zombie z = zombies.get(i);
                if (Game.distance(z.x, z.y, endX, endY) < 10) {
                    z.isSelected = true;
                    break;
                }
            }
        }
    }

    private boolean insideSelection(Zombie z) {
        return (Math.min(startX, endX) <= z.x && z.x <= Math.max(startX, endX) &&
                Math.min(startY, endY) <= z.y && z.y <= Math.max(startY, endY));
    }
}
