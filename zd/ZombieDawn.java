package zd;

import java.awt.EventQueue;
import javax.swing.JFrame;


public class ZombieDawn extends JFrame {

    public ZombieDawn() {
        this.setTitle("Zombie Dawn"); //sets caption

        this.setSize(800, 600); //size of window

        this.setLocationRelativeTo(null); //centers window

        this.setResizable(false);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.add(new Framework());

        this.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ZombieDawn();
            }
        });
    }
}