package Solitaire;

import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Color;

class Selection {
    public Coordinate startingPosition = new Coordinate((short)0, (short)0);
    public Coordinate offset = new Coordinate((short)0, (short)0);
    public Pile selection = new Pile();
    public Pile beginningPile;
    public byte beginningPID;
    public byte endingPID;
    public float normalX;
}

public class Tableau extends CardListener {
    public Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public final short SCREENHEIGHT = (short)screenSize.getHeight();
    public final short SCREENWIDTH = (short)screenSize.getWidth();

    public static final byte TOTAL_FOUNDATION_PILES = 4;
    public static final byte TOTAL_FIELD_PILES = 7;
    public static final byte PILE_GAP = 10;

    // NOTE: The selection algorithm wont work right now if we use an xmargin.
    // It's zero right now, but things should be centered eventually.
    public static final short PLAYABLE_REGION = (TOTAL_FIELD_PILES * Card.WIDTH) + (PILE_GAP * TOTAL_FIELD_PILES);
    public static final byte YMARGIN = 25;
    public static final byte XMARGIN = 0;

    // Piles
    Pile waste = new Pile(new Coordinate((short)116, (short)YMARGIN));
    Pile stock = new Pile(new Coordinate((short)25, (short)YMARGIN));
    Pile[] foundation = new Pile[TOTAL_FOUNDATION_PILES];
    Pile[] field = new Pile[TOTAL_FIELD_PILES];

    public List<Card> cache = new ArrayList<Card>();
    public Selection cursor;

    public Tableau() {
        for (byte i = 0; i < Deck.SIZE; i++) {
            Card card = new Card((char)(i % 13), (byte)(i / 13));
            Deck.cards.add(card);  // We need to keep a copy of the entire unmodified deck.
        }

        // @FIXME -- don't be lazy
        Collections.shuffle(Deck.cards);

        // Setup all the piles and deal the cards
        for(byte i = 0; i <Deck.SIZE; i++) 
            this.stock.add(Deck.cards.get(i));

        for (byte i = 0; i < TOTAL_FOUNDATION_PILES; i++)
            this.foundation[i] = new Pile(new Coordinate((short) (350 + (((i - 1) * Card.WIDTH) + PILE_GAP * i)), (short)YMARGIN));

        for (byte i = 0; i < TOTAL_FIELD_PILES; i++) {
            this.field[i] = new Pile(new Coordinate((short)(XMARGIN + ((i * Card.WIDTH) + PILE_GAP * i)), (short)200));
            this.field[i].setOffset((short)YMARGIN);
            for (byte k = 0; k < i + 1; k++) {
                Card card = Deck.cards.get(k + TOTAL_FIELD_PILES * i);

                if (k == i) card.flip();

                byte idx = stock.indexOf(card);
                this.stock.remove(idx);
                this.field[i].add(card);
            }
        }

        this.cursor = new Selection();
    }

    public void returnCards() {
        for (byte i = 0; i < this.cursor.selection.size; i++) {
            this.cursor.selection.get(i).setPosition(
                (short)(this.cursor.startingPosition.x),
                (short)(this.cursor.startingPosition.y  + i * YMARGIN)
            );
        }
        this.cursor.beginningPile.cards = new ArrayList<Card>(this.cache);
        this.clearSelection();
    }

    public void clearSelection() {
        this.cursor.selection.cards.clear();
        this.cursor.selection.size = 0;
        this.cache.clear();
        this.repaint();
    }

    public void selectCards(Pile source, short mX, short mY) {
        Card card = null;
        byte i = 0;

        for (i = (byte)(source.size - 1); i > -1; i--) {
            card = source.cards.get(i);
            short cX = (short)card.getX();
            short cY = (short)card.getY();

            if (mY >= cY && mY <= cY + Card.HEIGHT && mX >= cX && mX <= cX + Card.WIDTH)
                break;
        }

        if (i <= -1) return;

        if (!card.isFaceUp()) {
            if (i == source.size - 1) {
                card.flip();
                this.repaint();
            }
            return;
        }

        this.cursor.selection.cards = source.cards.subList((int)i, (int)source.size);
        this.cursor.selection.size = (byte)(source.size - i);
        this.cursor.offset.x = (short)(mX - card.position.x);
        this.cursor.offset.y = (short)(mY - card.position.y);
        this.cursor.startingPosition.x = card.position.x;
        this.cursor.startingPosition.y = card.position.y;
        this.cursor.beginningPile = source;

        for (Card c : source.cards)
            this.cache.add(c.clone());
    }

    public void buildPile(Pile destination) {
        for (byte i = 0; i < this.cursor.selection.size; i++) {
            Card card = this.cursor.selection.get(i);
            this.cursor.beginningPile.size -= 1;
            destination.add(card);
        }
    }

    public void attemptMove(Pile destination, boolean conditional) {
        if (conditional == true) {
            this.buildPile(destination);

            if (this.cursor.beginningPID > -1)
                this.cursor.beginningPile.height -= (short)((this.cursor.selection.size) * YMARGIN);

            this.clearSelection();
        } else {
            this.returnCards();
        }
    }

    public void mousePressed(MouseEvent e) {
        short mX = (short)e.getX();
        short mY = (short)e.getY();

        if (this.stock.contains(mX, mY)) {
            // We clicked on the stockpile
            if (this.stock.size > 0) {
                // Pile is not empty -- Draw one card, move to Waste Pile and flip.
                this.cursor.selection.cards = this.stock.cards.subList(this.stock.size - 1, this.stock.size);
                Card top = this.cursor.selection.get(0);
                this.stock.size -= 1;
                this.waste.add(top);
                top.flip();
            } else if (this.stock.size == 0) {
                // Pile is empty -- Reverse, flip and re-stock with all the cards from the waste pile.
                Collections.reverse(this.waste.cards);
                this.cursor.selection.cards = this.waste.cards.subList(0, this.waste.size);
                this.cursor.selection.cards.forEach(card -> card.flip());
                this.cursor.selection.size = (byte)this.waste.size;
                this.cursor.beginningPile = this.waste;
                this.buildPile(this.stock);
            }
            this.clearSelection();

        } else if (this.waste.size > 0 && this.waste.contains(mX, mY)) {
            // We clicked on the wastepile, get the top card.
            if (this.waste.cards.get(this.waste.size - 1).contains(mX, mY)) {
                this.selectCards(waste, mX, mY);
                this.cursor.beginningPID = -1;
            }
        } else {
            // We clicked somewhere else
            this.cursor.normalX = (float)(PLAYABLE_REGION) / mX;
            this.cursor.beginningPID = (byte)(TOTAL_FIELD_PILES / this.cursor.normalX);

            if (this.cursor.beginningPID >= TOTAL_FIELD_PILES)
                return;

            // Did we click on the field?
            this.selectCards(this.field[this.cursor.beginningPID], mX, mY);
            // or did we click on the foundation?
            if (this.cursor.selection.cards.isEmpty() && this.cursor.beginningPID >= 3)
                this.selectCards(this.foundation[this.cursor.beginningPID - 3], mX, mY);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (!this.cursor.selection.cards.isEmpty()) {
            short mX = (short)e.getX();
            short mY = (short)e.getY();

            this.cursor.normalX =  (float)(PLAYABLE_REGION) / mX;
            this.cursor.endingPID = (byte)(TOTAL_FIELD_PILES / this.cursor.normalX);

            Card first = this.cursor.selection.cards.get(0);
            Card last = null;

            if (mY < 200) {
                this.cursor.endingPID -= 3;
                if (this.cursor.endingPID < 0 || this.cursor.endingPID >= TOTAL_FOUNDATION_PILES || this.cursor.selection.size > 1) {
                    this.returnCards();
                    return;
                }

                if (this.foundation[this.cursor.endingPID].size == 0) {
                    this.attemptMove(this.foundation[this.cursor.endingPID], first.value == 0);
                    return;
                }

                last = this.foundation[this.cursor.endingPID].get(this.foundation[this.cursor.endingPID].size - 1);
                this.attemptMove(this.foundation[this.cursor.endingPID], first.suit == last.suit && first.value - last.value == 1);

            } else {

                if (this.cursor.endingPID < 0 || this.cursor.endingPID >= TOTAL_FIELD_PILES) {
                    this.returnCards();
                    return;
                }

                if (this.field[this.cursor.endingPID].size == 0) {
                    this.attemptMove(this.field[this.cursor.endingPID], first.value == 12);
                    return;
                }

                last = this.field[this.cursor.endingPID].get(this.field[this.cursor.endingPID].size - 1);
                this.attemptMove(this.field[this.cursor.endingPID], last.isFaceUp() && last.isOppositeTo(first) && first.value - last.value == -1);
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (!this.cursor.selection.cards.isEmpty()) {
            short mX = (short)e.getX();
            short mY = (short)e.getY();

            for (byte i = 0; i < this.cursor.selection.size; i++) {
                this.cursor.selection.cards.get(i).setPosition(
                    (short)(mX - this.cursor.offset.x),
                    (short)(mY - this.cursor.offset.y + (i * YMARGIN))
                );
            }
            this.repaint();
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            // TODO: Implement a proper undo feature.
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, SCREENWIDTH, SCREENHEIGHT);

        this.stock.paintComponent(g);
        this.waste.paintComponent(g);

        for (byte i = 0; i < TOTAL_FOUNDATION_PILES; i++)
            this.foundation[i].paintComponent(g);

        for (byte i = 0; i < TOTAL_FIELD_PILES; i++)
            this.field[i].paintComponent(g);

        // if (this.cursor.selection != null) {
        // this.cursor.selection.paintComponent(g);
        // }
    }
}