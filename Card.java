package Solitaire;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

class Card extends JComponent implements Comparator<Card> {

    private boolean faceUp;
    private char value;
    private byte suit;

    public static final byte WIDTH = 80;
    public static final byte HEIGHT = 100;

    public Coordinate position = new Coordinate((short)0, (short)0);

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
    
    public String toString() {
        return (byte)(this.value) + " of  " + this.suit;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.faceUp)
            g.setColor(Color.WHITE);
        else
            g.setColor(Color.CYAN);

        g.fillRoundRect(this.position.x, this.position.y, this.WIDTH, this.HEIGHT, 12, 12);
        g.setColor(Color.BLACK);
        g.drawRoundRect(this.position.x, this.position.y, this.WIDTH, this.HEIGHT, 12, 12);

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
