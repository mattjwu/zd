package zd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.awt.RenderingHints;
import javax.swing.JPanel;
import java.util.Timer;
import java.util.TimerTask;



public class Framework extends JPanel {

    private Timer timer;
    private Game game;
    private boolean ingame = true;

    //For the record I have no clue why these two goons need to be out here
    private boolean[] mouseState = new boolean[3];
    private boolean[] keyboardState = new boolean[525];

    public Framework() {
        this.addMouseListener(new MouseHandler());
        this.addMouseMotionListener(new MouseHandler());
        this.addKeyListener(new KeyHandler());

        this.setDoubleBuffered(true);
        this.setFocusable(true);

        //Using an easy built-in game loop thing for now
        timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(), 1000, 10);

        this.setBackground(new Color(210, 210, 210));

        game = new Game();
    }


    private void gameLoop() {
        
        game.update();

        repaint();
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        if (ingame) {
            game.draw(g2d);
        }
    }

    private void drawObjects(Graphics2D g) {
        //g.drawImage(stuff I need to draw);
        //object.draw(g); for a bunch of objects
    }




    private class ScheduleTask extends TimerTask {

        @Override
        public void run() {
            gameLoop();
        }
    }

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                if (mouseState[2] == false) {
                    game.movementClick(e);
                }
            } else if (e.getButton() == MouseEvent.BUTTON1) {
                if (mouseState[0] == false) {
                    game.selectionStart(e);
                }
            }
            mouseState[e.getButton() - 1] = true;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (mouseState[0]) {
                game.adjustSelection(e);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (mouseState[0] == true) {
                    game.takeSelection();
                }
            }
            mouseState[e.getButton() - 1] = false;
        }
    }

    private class KeyHandler extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            keyboardState[e.getKeyCode()] = true;
            //dostuff
        }

        public void keyReleased(KeyEvent e) {
            keyboardState[e.getKeyCode()] = false;
            //dostuff
        }
    }
}
