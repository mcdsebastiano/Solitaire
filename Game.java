package Solitaire;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import javax.swing.JFrame;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;

class Coordinate {
    public short x;
    public short y;

    public Coordinate(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "{ X: " + x + ", Y: " + y + " }\n";
    }
}

class Card extends JComponent implements Comparator<Card> {

    private boolean faceUp;
    private char value;
    private byte suit;

    public static final int WIDTH = 80;
    public static final int HEIGHT = 100;
    
    public final Dimension SIZE = new Dimension(this.WIDTH, this.HEIGHT);

    public Coordinate position = new Coordinate((short)0, (short)0);
    // public Point position = new Point(0,0);
    
    public Dimension getPreferredSize() {
        return this.SIZE;
    }

    public Card(char v, byte s) {
        this.setMaximumSize(new Dimension(this.WIDTH, this.HEIGHT));
        this.value = v;
        this.suit = s;
    }
    
    public int compare(Card c1, Card c2) {
        return c1.value - c2.value;
    }

    public boolean equals(Card card) {
        return ((card.suit - this.suit) | (card.value - this.value)) == 0;
    }

    public void flip() {
        if (faceUp)
            return;

        this.faceUp = true;
    }

    public Coordinate getPosition() {
        return this.position;
    }

    public int getX() {
        return this.position.x;
    }

    public int getY() {
        return this.position.y;
    }
    
    public void setPosition(short x, short y) {
        this.position.x = x;
        this.position.y = y;
    }


    public String toString() {
        return (byte)(this.value) + " of  " + this.suit;
    }

    public String suitString() {
        return this.suit == 0 ? "\u2666" :
               this.suit == 1 ? "\u2665" :
               this.suit == 2 ? "\u2660" :
               this.suit == 3 ? "\u2663" :
               this.suit + "";
    }

    public String faceValue() {
        return this.value == 1 ? "A" :
               this.value == 11 ? "J" :
               this.value == 12 ? "Q" :
               this.value == 0 ? "K" :
               (byte)(this.value) + "";
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.faceUp)
            g.setColor(Color.WHITE);
        else
            g.setColor(Color.CYAN);

        g.fillRoundRect(this.position.x, this.position.y, this.WIDTH, this.HEIGHT, 12, 12);
        
        if (this.faceUp) {
            Font font = new Font("TimesRoman", Font.PLAIN, 36);
            g.setFont(font);

            if (this.suit < 2)
                g.setColor(Color.RED);
            else
                g.setColor(Color.BLACK);

            String faceValue = this.faceValue();
            String suitString = this.suitString();

            g.drawString(faceValue, this.position.x + 10, this.position.y + 35);
            g.drawString(suitString, this.position.x + this.WIDTH - 35, this.position.y + 35);
        }
    }

}

class Pile {
    public List<Card> cards = new ArrayList<Card>();
    public Pile() {}
}

class Deck extends Pile {
    
    public static final byte SIZE = 52;
    
    public Deck() {}
    
    public Card get(int index) {
        return this.cards.get(index);
    }
}

class Tableau extends JComponent implements MouseInputListener {

    public Deck deck = new Deck();
    
    // Piles 
    Pile[] foundation = new Pile[4];
    Pile[] field = new Pile[7];
    
    Pile stock = new Pile();
    Pile waste = new Pile();
    
    public Card cardInHand = null;
    public short mXOffset;
    public short mYOffset;
    
    public Tableau() {
        byte j = 0;
        for (short i = 1; i <= Deck.SIZE; i++) {
            int mod = (i % 13);
            Card card = new Card((char)mod, mod == 0 ? j++ : j);
            this.deck.cards.add(card);
            this.add(card);
        }
    }
    
    public Card getCard(short mouseX, short mouseY) {
        for (short i = Deck.SIZE - 1; i >= 0; i--) {
            Card card = this.deck.get(i);
            short cX = (short)card.getX();
            short cY = (short)card.getY();
            
            if (mouseY >= cY && mouseY <= cY + card.HEIGHT  && mouseX >= cX && mouseX <= cX + card.WIDTH) {
                return card;
            }
        }
        return null;
    }
    
    public void paint(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(0,0,800,600);
        
        for(int i = 0; i < Deck.SIZE; i++) {
            Card card = this.deck.get(i);
            
            if(this.cardInHand != null && this.cardInHand.equals(card)) 
                continue;
            
            card.paintComponent(g);
        }
        
        if(this.cardInHand != null)
            this.cardInHand.paintComponent(g);
        
    }
    
    public void mousePressed(MouseEvent e) {
        short mX = (short)e.getX();
        short mY = (short)e.getY();
        
        Card selected = this.getCard(mX, mY);
        if (selected == null)
            return;

        this.cardInHand = selected;
        this.cardInHand.flip();
        
        this.mXOffset = (short)(mX - this.cardInHand.position.x);
        this.mYOffset = (short)(mY - this.cardInHand.position.y);
        
        int index = this.deck.cards.indexOf(this.cardInHand);
        this.deck.cards.remove(index);
        this.deck.cards.add(this.cardInHand);
        
        this.repaint();
    }

    public void mouseDragged(MouseEvent e) {
        if (this.cardInHand != null) {
            short cX = (short)e.getX();
            short cY = (short)e.getY();
            this.cardInHand.setPosition((short)(cX - this.mXOffset), (short)(cY - this.mYOffset));
            this.repaint();
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if(this.cardInHand != null) {
            this.cardInHand = null;
        }
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

}

public class Game {
    public static void createAndShowGUI() {
        JFrame frame = new JFrame();
        Tableau t = new Tableau();
        
        frame.getContentPane().addMouseMotionListener(t);
        frame.getContentPane().addMouseListener(t);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 800, 600);
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
