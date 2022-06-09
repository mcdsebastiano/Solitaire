package Solitaire;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;

public class Game {
    public static void createAndShowGUI() {
        JFrame frame = new JFrame();
        Tableau t = new Tableau();

        frame.getContentPane().addMouseMotionListener(t);
        frame.getContentPane().addMouseListener(t);

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                frame.repaint();
                System.gc();
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 740, 600);
        frame.setVisible(true);
        frame.add(t);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
