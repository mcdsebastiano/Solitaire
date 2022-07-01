package Solitaire;

import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Color;

class Hand {
    public List<Card> source = new ArrayList<Card>();
    public Pile selected = new Pile();
    public byte beginningPID;
    public byte endingPID;
    public float normalX;
    public short yOffset;
    public short xOffset;
    public short xStart;
    public short yStart;
    public Hand() {}
}


public class Tableau extends CardListener {

    public Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public final short SCREENHEIGHT = (short)screenSize.getHeight();
    public final short SCREENWIDTH = (short)screenSize.getWidth();

    public static final byte TOTAL_FOUNDATION_PILES = 4;
    public static final byte TOTAL_FIELD_PILES = 7;
    public static final byte PILE_GAP = 10;

    // The selection algorithm wont work right now if we use an xmargin.
    // It's zero right now, but things should be centered eventually.
    public static final short PLAYABLE_REGION = (TOTAL_FIELD_PILES * Card.WIDTH) + (PILE_GAP * TOTAL_FIELD_PILES);
    public static final byte YMARGIN = 25;
    public static final byte XMARGIN = 0;

    // Piles
    Pile waste = new Pile(new Coordinate((short)116, (short)YMARGIN));
    Pile stock = new Pile(new Coordinate((short)25, (short)YMARGIN));
    Pile[] foundation = new Pile[TOTAL_FOUNDATION_PILES];
    Pile[] field = new Pile[TOTAL_FIELD_PILES];

    // Game State
    public byte stockCount = 51;
    public byte wasteCount = -1;
    public Pile beginningPile;
    public Hand hand;

    public Tableau() {
        this.hand = new Hand();
        for (byte i = 0; i < Deck.SIZE; i++) {
            Card card = new Card((char)(i % 13), (byte)(i / 13));
            Deck.cards.add(card);
        }

        for (byte i = 0; i < TOTAL_FOUNDATION_PILES; i++)
            this.foundation[i] = new Pile(new Coordinate((short) (350 + (((i - 1) * Card.WIDTH) + PILE_GAP * i)), (short)YMARGIN));

        // @FIXME -- don't be lazy
        Collections.shuffle(Deck.cards);

        for (byte i = 0; i < Deck.SIZE; i++) {
            Card card = Deck.cards.get(i);
            this.stock.add(card);
        }

        for (byte i = 0; i < TOTAL_FIELD_PILES; i++) {
            this.field[i] = new Pile(new Coordinate((short)(XMARGIN + ((i * Card.WIDTH) + PILE_GAP * i)), (short)200));
            this.field[i].setOffset((short)YMARGIN);
            for (byte k = 0; k < i + 1; k++) {
                Card card = Deck.cards.get(k + TOTAL_FIELD_PILES * i);

                if (k == i) card.flip();

                byte idx = stock.indexOf(card);
                this.stock.cards.remove(idx);
                this.field[i].add(card);
                this.stockCount--;
            }
        }
    }

    public void selectCards(Pile source, short mX, short mY) {
        byte SIZE = (byte)(source.cards.size() - 1);
        Card card = null;
        byte i = 0;
        for (i = SIZE; i > -1; i--) {
            card = source.cards.get(i);

            short cX = (short)card.getX();
            short cY = (short)card.getY();

            if (mY >= cY && mY <= cY + Card.HEIGHT  && mX >= cX && mX <= cX + Card.WIDTH) {
                break;
            }
        }
        
        if(i > -1) {   
            if(!card.isFaceUp()) {
                if (i == SIZE) {
                    card.flip();
                    this.repaint();
                }
            } else {
                
                this.hand.xStart = card.position.x;
                this.hand.yStart = card.position.y;
                this.hand.xOffset = (short)(mX - card.position.x);
                this.hand.yOffset = (short)(mY - card.position.y);
                
                this.hand.selected.cards = source.cards.subList((int)i, (int)SIZE + 1);

                for (Card c : source.cards)
                    this.hand.source.add(c.clone());

                this.beginningPile = source;
            }
        }
    }

    public void buildPile(Pile to, Pile from) {
        byte size = (byte)(from.cards.size());
        for (byte i = 0; i < size; i++) {
            Card card = from.get(i);
            to.add(card);
        }

        if (this.hand.beginningPID == -1)
            this.wasteCount--;

        this.hand.selected.cards.clear();
        this.hand.source.clear();
        this.repaint();
    }

    public void undoMove() {
        byte size = (byte)hand.selected.cards.size();
        for (byte i = 0; i < size; i++)
            this.hand.selected.get(i).setPosition(hand.xStart, (short)(hand.yStart + i * YMARGIN));

        this.beginningPile.cards = new ArrayList<Card>(hand.source);
        this.hand.selected.cards.clear();
        this.hand.source.clear();
        this.repaint();

    }

    public void mousePressed(MouseEvent e) {
        short mX = (short)e.getX();
        short mY = (short)e.getY();

        // We clicked on the stockpile
        if (this.stock.contains(mX, mY)) {
            if (this.stockCount >= 0) {
                // Pile is not empty -- Draw One Card
                this.hand.selected.cards = this.stock.cards.subList(this.stockCount, this.stockCount + 1);
                Card top = this.hand.selected.get(0);
                top.flip();

                this.waste.add(top);
                this.wasteCount++;
                this.stockCount--;

            } else {
                // Pile is empty -- Re-stock with all the cards from the waste pile.
                Collections.reverse(this.waste.cards);
                this.stockCount = (byte)(this.waste.cards.size() - 1);
                this.hand.selected.cards = this.waste.cards.subList(0, this.wasteCount + 1);
                this.hand.selected.cards.forEach(card -> card.flip());
                this.buildPile(this.stock, this.hand.selected);
                this.wasteCount = -1;
            }
            this.hand.selected.cards.clear();
            this.repaint();
            return;
        } 
        if (this.wasteCount >= 0 && this.waste.contains(mX, mY)) {
            // We clicked on the wastepile, get the top card.
            Card top = waste.cards.get(this.wasteCount);

            if (top.contains(mX, mY)) {
                this.hand.beginningPID = -1;
                this.selectCards(waste, mX, mY);
            }
            
        } else {
            // We clicked somewhere else
            this.hand.normalX = (float)(PLAYABLE_REGION) / mX;
            this.hand.beginningPID = (byte)(TOTAL_FIELD_PILES / hand.normalX);

            if (this.hand.beginningPID >= TOTAL_FIELD_PILES)
                return;
            
            // did we click on the field?
            this.selectCards(this.field[hand.beginningPID], mX, mY);
            // or did we click on the foundation?
            if (this.hand.selected.cards.isEmpty() && this.hand.beginningPID >= 3) 
                this.selectCards(this.foundation[this.hand.beginningPID - 3], mX, mY);
            
        }
        
    }

    public void mouseDragged(MouseEvent e) {
        if (!this.hand.selected.cards.isEmpty()) {
            short mX = (short)e.getX();
            short mY = (short)e.getY();

            for (byte i = 0; i < this.hand.selected.cards.size(); i++) {
                Card card = this.hand.selected.cards.get(i);
                card.setPosition((short)(mX - this.hand.xOffset), (short)(mY - this.hand.yOffset + (i * YMARGIN)));
            }
            this.repaint();
        }
    }

    // @Cleanup
    public void mouseReleased(MouseEvent e) {
        if (!this.hand.selected.cards.isEmpty()) {

            short mX = (short)e.getX();
            short mY = (short)e.getY();

            this.hand.normalX =  (float)(PLAYABLE_REGION) / mX;
            this.hand.endingPID = (byte)(TOTAL_FIELD_PILES / this.hand.normalX);

            Card first = this.hand.selected.cards.get(0);
            Card last = null;
            byte size = 0;

            //
            // Foundation Rules
            //

            if (mY < 200) {
                // Only make a play for the foundation piles if we are carrying one card.
                if (this.hand.selected.cards.size() > 1 || this.hand.endingPID < 3) {
                    this.undoMove();
                    return;
                }

                // Determine if the Pile is empty before moving.
                size = (byte)this.foundation[hand.endingPID - 3].cards.size();

                if (size > 0) {
                    // The pile isn't empty.
                    // Attempt to build the pile.
                    last = this.foundation[hand.endingPID - 3].get(size - 1);

                    if (first.suit == last.suit && first.value - last.value == 1)
                        this.buildPile(this.foundation[this.hand.endingPID - 3], this.hand.selected);
                    else
                        this.undoMove();

                } else {
                    // The Pile is empty.
                    // Only move to an open slot if the first card is an Ace.
                    if (first.value == 0)
                        this.buildPile(this.foundation[hand.endingPID - 3], this.hand.selected);
                    else
                        this.undoMove();

                }
                return;
            }

            //
            // Field Rules
            //

            if (this.hand.endingPID < 0 || this.hand.endingPID >= TOTAL_FIELD_PILES || this.hand.beginningPID == this.hand.endingPID) {
                this.undoMove();
                return;
            }

            size = (byte)this.field[this.hand.endingPID].cards.size();
            if (size > 0) {

                last = this.field[this.hand.endingPID].get(size - 1);

                if (last.isFaceUp() && last.isOppositeTo(first) && first.value - last.value == -1)
                    this.buildPile(this.field[this.hand.endingPID], this.hand.selected);
                else
                    this.undoMove();


            } else {
                // Only move to an open slot if the first card is a King
                if (first.value == 12)
                    this.buildPile(this.field[this.hand.endingPID], this.hand.selected);
                else
                    this.undoMove();

            }
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, SCREENWIDTH, SCREENHEIGHT);

        this.stock.paintComponent(g);
        this.waste.paintComponent(g);

        for (byte i = 0; i < TOTAL_FOUNDATION_PILES; i++) {
            this.foundation[i].paintComponent(g);
        }

        for (byte i = 0; i < TOTAL_FIELD_PILES; i++) {
            this.field[i].paintComponent(g);
        }

        // if (hand.selected != null) {
        // hand.selected.paintComponent(g);
        // }
    }
}