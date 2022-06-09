
package Solitaire;

import java.awt.event.MouseEvent;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Color;
enum PileType {
    STOCK,
    FIELD,
    FOUNDATION
};


public class Tableau extends CardListener {

    public Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public final short SCREENHEIGHT = (short)screenSize.getHeight();
    public final short SCREENWIDTH = (short)screenSize.getWidth();
    
    public static final byte NUMBER_FOUNDATION_PILES = 4;
    public static final byte NUMBER_FIELD_PILES = 7;
    public static final byte PILE_GAP = 10;

    // The selection algorithm wont work right now if we use an xmargin.
    // It's zero right now, but things should be centered eventually.
    public static final short PLAYABLE_REGION = 620;
    public static final byte XMARGIN = 0;
    public static final byte YMARGIN = 25;

    Pile waste = new Pile(new Coordinate((short)116, (short)YMARGIN));
    Pile stock = new Pile(new Coordinate((short)25, (short)YMARGIN));

    Pile[] foundation = new Pile[NUMBER_FOUNDATION_PILES];
    Pile[] field = new Pile[NUMBER_FIELD_PILES];

    public Card cardInHand = null;
    public byte stockIdx = 23;
    public byte wasteIdx = -1;
    public short mXOffset;
    public short mYOffset;

    public Coordinate dragStart;

    public Tableau() {
        byte j = 0;
        for (byte i = 1; i <= Deck.SIZE; i++) {
            int mod = i % 13;
            Card card = new Card((char)mod, mod == 0 ? j++ : j);
            Deck.cards.add(card);
        }

        // @FIXME -- don't be lazy
        Collections.shuffle(Deck.cards);

        for (byte i = 0; i < Deck.SIZE; i++) {
            Card card = Deck.cards.get(i);
            stock.add(card);
        }

        for (byte i = 0; i < NUMBER_FOUNDATION_PILES; i++) {
            foundation[i] = new Pile(new Coordinate((short) (400 + (((i - 1) * Card.WIDTH) + PILE_GAP * i)), (short)25));
        }
        
        byte dealt = 0;
        for (byte i = 0; i < NUMBER_FIELD_PILES; i++) {
            field[i] = new Pile(new Coordinate((short)(XMARGIN + ((i * Card.WIDTH) + PILE_GAP * i)), (short)200));
            field[i].setOffset((short)25);
            for (byte k = 0; k < i + 1; k++) {
                Card card = Deck.cards.get(dealt++);
                if (k == i)
                    card.flip();
                int idx = stock.indexOf(card);
                stock.cards.remove(idx);
                field[i].add(card);
            }
        }
    }

    public Card getCard(Pile source, short mX, short mY) {
        byte SIZE = (byte)(source.cards.size() - 1);
        for (byte i = SIZE; i >= 0; i--) {
            Card card = source.cards.get(i);
            
            // @FIXME this shouldn't be here when we need 
            // to start flipping cards it will be moved elsewhere.
            if (!card.isFaceUp())  continue;
            
            short cX = (short)card.getX();
            short cY = (short)card.getY();

            if (mY >= cY && mY <= cY + Card.HEIGHT  && mX >= cX && mX <= cX + Card.WIDTH) {
                return card;
            }
        }
        return null;
    }

    public void movePiles(Pile to, Pile from, Card card, byte idx) {
        from.remove(idx);
        to.add(card);
    }

    public void mousePressed(MouseEvent e) {
        short mX = (short)e.getX();
        short mY = (short)e.getY();

        // We clicked on the stockpile
        if (stock.contains(mX, mY)) {
            if (stockIdx >= 0) {
                // Pile is not empty -- Draw One Card
                Card last = stock.cards.get(stockIdx);
                last.flip();

                movePiles(waste, stock, last, stockIdx);
                stockIdx--;
                wasteIdx++;

            } else {
                // Pile is empty -- Re-stock with all the cards from the waste pile.
                for (int i = wasteIdx; i >= 0 ; i--) {
                    Card top = waste.cards.get(i);
                    top.flip();

                    movePiles(stock, waste, top, wasteIdx);
                    wasteIdx--;
                    stockIdx++;
                }
            }
            return;
        }

        // We clicked on the wastepile
        if (waste.contains(mX, mY)) {
            // Grab the top card.
            Card top = waste.cards.get(wasteIdx);
            if (top.contains(mX, mY)) {
                this.cardInHand = top;
                dragStart = new Coordinate(cardInHand.position.x, cardInHand.position.y);
                this.mXOffset = (short)(mX - this.cardInHand.position.x);
                this.mYOffset = (short)(mY - this.cardInHand.position.y);
            }
            return;
        }

        float ratio = (float)(PLAYABLE_REGION) / mX;
        short pileIdx = (short)(NUMBER_FIELD_PILES / ratio);
        if(pileIdx >= NUMBER_FIELD_PILES) {
            return;
        }
        
        Card selected;
        selected = this.getCard(field[pileIdx], mX, mY);
        
        if (selected == null && pileIdx >= 3) {
            selected = this.getCard(foundation[pileIdx - 3], mX, mY);    
        }  
        
        if (selected == null)
            return;

        this.cardInHand = selected;
        dragStart = new Coordinate(cardInHand.position.x, cardInHand.position.y);
        this.mXOffset = (short)(mX - this.cardInHand.position.x);
        this.mYOffset = (short)(mY - this.cardInHand.position.y);

        this.repaint();
    }


    public void mouseDragged(MouseEvent e) {
        if (this.cardInHand != null) {
            short mX = (short)e.getX();
            short mY = (short)e.getY();
            this.cardInHand.setPosition((short)(mX - this.mXOffset), (short)(mY - this.mYOffset));
            this.repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (this.cardInHand != null) {
            this.cardInHand.setPosition(dragStart.x, dragStart.y);
            this.cardInHand = null;
        }
        this.repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, SCREENWIDTH, SCREENHEIGHT);

        this.stock.paintComponent(g);
        this.waste.paintComponent(g);
        
        for (byte i = 0; i < NUMBER_FOUNDATION_PILES; i++) {
            this.foundation[i].paintComponent(g);
        }

        for (byte i = 0; i < NUMBER_FIELD_PILES; i++) {
            this.field[i].paintComponent(g);
        }

        if (this.cardInHand != null) {
            this.cardInHand.paintComponent(g);
        }
    }
}
