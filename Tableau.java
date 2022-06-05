package Solitaire;

import java.awt.event.MouseEvent;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Toolkit;

public class Tableau extends CardListener {
    
    public Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public final short SCREENWIDTH = (short)screenSize.getWidth();
    public final short SCREENHEIGHT = (short)screenSize.getHeight();
    
    public static final byte NUMBER_FOUNDATION_PILES = 4;
    public static final byte NUMBER_FIELD_PILES = 7;

    Pile[] foundation = new Pile[NUMBER_FOUNDATION_PILES];
    Pile[] field = new Pile[NUMBER_FIELD_PILES];

    Pile stock = new Pile(new Coordinate((short)25, (short)25));
    Pile waste = new Pile(new Coordinate((short)116,(short)25));

    public Card cardInHand = null;
    public short mXOffset;
    public short mYOffset;

    public Tableau() {
        byte j = 0;
        for (byte i = 1; i <= Deck.SIZE; i++) {
            int mod = i % 13;
            Card card = new Card((char)mod, mod == 0 ? j++ : j);
            Deck.cards.add(card);
            stock.add(card);
        }
        
        for(byte i = 0; i < NUMBER_FOUNDATION_PILES; i++) {
            foundation[i] = new Pile(new Coordinate((short) (400 + ((i * Card.WIDTH) + 10 * i)), (short)25));
        }
        
        for(byte i = 0; i < NUMBER_FIELD_PILES; i++) {
            field[i] = new Pile(new Coordinate((short)(25 + ((i * Card.WIDTH) + 10 * i)), (short)200));
            field[i].setOffset((short)0,(short)25);
        }
    }

    public Card getCard(short mouseX, short mouseY) {
        for (byte i = Deck.SIZE - 1; i >= 0; i--) {
            Card card = Deck.cards.get(i);
            short cX = (short)card.getX();
            short cY = (short)card.getY();
            
            if (mouseY >= cY && mouseY <= cY + card.HEIGHT  && mouseX >= cX && mouseX <= cX + card.WIDTH) {
                return card;
            }
        }
        return null;
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

        int index = Deck.cards.indexOf(this.cardInHand);
        Deck.cards.remove(index);
        Deck.cards.add(this.cardInHand);

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
        if (this.cardInHand != null) {
            this.cardInHand = null;
        }
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 
        SCREENWIDTH, SCREENHEIGHT);
        
        for(byte i = 0; i < NUMBER_FOUNDATION_PILES; i++) {
            this.foundation[i].paintComponent(g);
        }
        
        this.stock.paintComponent(g);
        this.waste.paintComponent(g);

        for (byte i = 0; i < Deck.SIZE; i++) {
            Card card = Deck.cards.get(i);

            if (this.cardInHand != null && this.cardInHand.equals(card))
                continue;

            card.paintComponent(g);
        }

        if (this.cardInHand != null)
            this.cardInHand.paintComponent(g);
    }

}
