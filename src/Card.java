package Solitaire;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

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

    public void set(short x, short y) {
        this.x = x;
        this.y = y;
    }
}

class Card extends JComponent implements Cloneable {
    public Coordinate position = new Coordinate((short)0, (short)0);
    public boolean faceUp;
    public char value;
    public byte suit;

    public static final byte HEIGHT = 100;
    public static final byte WIDTH = 80;

    public Card(char v, byte s) {
        this.setMaximumSize(new java.awt.Dimension(this.WIDTH, this.HEIGHT));
        this.value = v;
        this.suit = s;
    }

    public boolean isOppositeTo(Card card) {
        return (card.isRed() && this.isBlack()) || (card.isBlack() && this.isRed());
    }

    public boolean isSameAs(Card card) {
        return (card.isRed() && this.isRed()) || (card.isBlack() && this.isBlack());
    }

    public boolean isRed() {
        return this.suit == 0 || this.suit == 1;
    }

    public boolean isBlack() {
        return this.suit == 2 || this.suit == 3;
    }

    public boolean equals(Card card)    { return ((card.suit - this.suit) | (card.value - this.value)) == 0;  }
    public Coordinate getPosition()     { return this.position;                                               }
    public boolean isFaceUp()           { return faceUp;                                                      }
    public void flip()                  { this.faceUp = !this.faceUp;                                         }
    public int getX()                   { return this.position.x;                                             }
    public int getY()                   { return this.position.y;                                             }

    public void setPosition(short x, short y) {
        this.position.x = x;
        this.position.y = y;
    }

    public String suitString() {
        return this.suit == 0 ? "\u2666" : // DIAMOND
               this.suit == 1 ? "\u2665" : // HEART
               this.suit == 2 ? "\u2660" : // SPADES
               this.suit == 3 ? "\u2663" : // CLUB
               this.suit + "";
    }

    public String faceValue() {
        return this.value == 0 ? "A" :
               this.value == 10 ? "J" :
               this.value == 11 ? "Q" :
               this.value == 12 ? "K" :
               (byte)(this.value + 1) + "";
    }

    @Override
    public Card clone() {
        Card card = null;
        try {
            card = (Card) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return card;
    }

    @Override
    public boolean contains(int mouseX, int mouseY) {
        if (mouseY >= this.position.y && mouseY <= this.position.y + this.WIDTH && mouseX >= this.position.x && mouseX <= this.position.x + this.WIDTH) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.faceValue() + " of  " + this.suitString();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.faceUp)
            g.setColor(Color.WHITE);
        else
            g.setColor(Color.CYAN);

        g.fillRoundRect(this.position.x, this.position.y, this.WIDTH, this.HEIGHT, 16, 16);
        g.setColor(Color.BLACK);
        g.drawRoundRect(this.position.x, this.position.y, this.WIDTH, this.HEIGHT, 16, 16);

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
