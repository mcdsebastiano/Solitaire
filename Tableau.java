package Solitaire;

import java.awt.event.MouseEvent;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

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
    Pile beginningPile;
    Hand hand;

    public Tableau() {
        this.hand = new Hand();
        for (byte i = 0; i < Deck.SIZE; i++) {
            Card card = new Card((char)(i % 13), (byte)(i / 13));
            Deck.cards.add(card);
        }

        for (byte i = 0; i < TOTAL_FOUNDATION_PILES; i++)
            foundation[i] = new Pile(new Coordinate((short) (350 + (((i - 1) * Card.WIDTH) + PILE_GAP * i)), (short)YMARGIN));

        // @FIXME -- don't be lazy
        Collections.shuffle(Deck.cards);

        for (byte i = 0; i < Deck.SIZE; i++) {
            Card card = Deck.cards.get(i);
            stock.add(card);
        }

        for (byte i = 0; i < TOTAL_FIELD_PILES; i++) {
            field[i] = new Pile(new Coordinate((short)(XMARGIN + ((i * Card.WIDTH) + PILE_GAP * i)), (short)200));
            field[i].setOffset((short)YMARGIN);
            for (byte k = 0; k < i + 1; k++) {
                Card card = Deck.cards.get(k + TOTAL_FIELD_PILES * i);

                if (k == i) card.flip();

                byte idx = stock.indexOf(card);
                stock.cards.remove(idx);
                field[i].add(card);
                stockCount--;
            }
        }
    }

    public Card getCard(Pile source, short mX, short mY) {
        byte SIZE = (byte)(source.cards.size() - 1);
        for (byte i = SIZE; i >= 0; i--) {
            Card card = source.cards.get(i);

            short cX = (short)card.getX();
            short cY = (short)card.getY();

            if (mY >= cY && mY <= cY + Card.HEIGHT  && mX >= cX && mX <= cX + Card.WIDTH) {
                return card;
            }
        }
        return null;
    }

    public void buildPile(Pile to, Pile from) {
        byte size = (byte)(from.cards.size());
        for (byte i = 0; i < size; i++) {
            Card card = from.get(i);
            to.add(card);
        }

        if (hand.beginningPID == -1)
            wasteCount--;

        hand.selected.cards.clear();
        hand.source.clear();
        this.repaint();
    }

    public void undoMove() {
        byte size = (byte)(hand.selected.cards.size());
        for (byte i = 0; i < size; i++)
            hand.selected.get(i).setPosition(hand.xStart, (short)(hand.yStart + i * YMARGIN));

        beginningPile.cards = new ArrayList<Card>(hand.source);
        hand.selected.cards.clear();
        hand.source.clear();
        this.repaint();

    }

    public void grabCards(Pile source, byte from, byte to) {
        hand.selected.cards = source.cards.subList((int)from, (int)to);

        for (Card card : source.cards)
            hand.source.add(card.clone());

        beginningPile = source;
    }

    public void mousePressed(MouseEvent e) {
        short mX = (short)e.getX();
        short mY = (short)e.getY();

        // We clicked on the stockpile
        if (stock.contains(mX, mY)) {
            if (stockCount >= 0) {
                // Pile is not empty -- Draw One Card
                hand.selected.cards = stock.cards.subList(stockCount, stockCount + 1);
                Card top = hand.selected.get(0);
                top.flip();

                waste.add(top);
                wasteCount++;
                stockCount--;

            } else {
                // Pile is empty -- Re-stock with all the cards from the waste pile.
                // NOTE: The order that the expressions are executed in is important here.
                Collections.reverse(waste.cards);
                stockCount = (byte)(waste.cards.size() - 1);
                hand.selected.cards = waste.cards.subList(0, wasteCount + 1);
                hand.selected.cards.forEach(card -> card.flip());
                buildPile(stock, hand.selected);
                wasteCount = -1;
            }
            hand.selected.cards.clear();
            this.repaint();
            return;
        }

        if (wasteCount >= 0 && waste.contains(mX, mY)) {
            // We clicked on the wastepile, get the top card.
            Card top = waste.cards.get(wasteCount);
            if (top.contains(mX, mY)) {
                hand.beginningPID = -1;
                hand.xStart = top.position.x;
                hand.yStart = top.position.y;
                hand.xOffset = (short)(mX - top.position.x);
                hand.yOffset = (short)(mY - top.position.y);

                this.grabCards(waste, wasteCount, (byte)(wasteCount + 1));

            }
        } else {

            hand.normalX = (float)(PLAYABLE_REGION) / mX;
            hand.beginningPID = (byte)(TOTAL_FIELD_PILES / hand.normalX);

            if (hand.beginningPID >= TOTAL_FIELD_PILES)
                return;

            Card selected = this.getCard(field[hand.beginningPID], mX, mY);

            boolean foundation = false;
            if (selected == null && hand.beginningPID >= 3) {
                selected = this.getCard(this.foundation[hand.beginningPID - 3], mX, mY);
                foundation = true;
            }

            if (selected == null)
                return;

            if (selected.isFaceUp()) {
                // grab cards
                hand.xStart = selected.position.x;
                hand.yStart = selected.position.y;
                hand.xOffset = (short)(mX - selected.position.x);
                hand.yOffset = (short)(mY - selected.position.y);

                byte size = 0;
                byte idx = 0;
                // @Cleanup
                if (foundation) {

                    size = (byte)this.foundation[hand.beginningPID - 3].cards.size();
                    idx = (this.foundation[hand.beginningPID - 3].indexOf(selected));
                    
                    this.grabCards(this.foundation[hand.beginningPID - 3], idx, size);

                } else {

                    size = (byte)field[hand.beginningPID].cards.size();
                    idx = (field[hand.beginningPID].indexOf(selected));
                    
                    this.grabCards(this.field[hand.beginningPID], idx, size);
                }

            } else {
                byte idx = field[hand.beginningPID].indexOf(selected);
                if (idx == field[hand.beginningPID].cards.size() - 1)
                    selected.flip();

            }
        }
        this.repaint();
    }

    public void mouseDragged(MouseEvent e) {
        if (!hand.selected.cards.isEmpty()) {
            short mX = (short)e.getX();
            short mY = (short)e.getY();

            for (byte i = 0; i < hand.selected.cards.size(); i++) {
                Card card = hand.selected.cards.get(i);
                card.setPosition((short)(mX - hand.xOffset), (short)(mY - hand.yOffset + (i * YMARGIN)));
            }
            this.repaint();
        }
    }
    
    // @Cleanup
    public void mouseReleased(MouseEvent e) {
        if (!hand.selected.cards.isEmpty()) {

            short mX = (short)e.getX();
            short mY = (short)e.getY();

            hand.normalX =  (float)(PLAYABLE_REGION) / mX;
            hand.endingPID = (byte)(TOTAL_FIELD_PILES / hand.normalX);

            Card first = hand.selected.cards.get(0);
            Card last = null;

            byte size = 0;

            if (mY < 200) {
                
                if (hand.selected.cards.size() > 1) {
                    undoMove();
                    return;
                }


                if (hand.endingPID <= 2) {
                    undoMove();
                    return;
                }

                size = (byte)foundation[hand.endingPID - 3].cards.size();

                if (size > 0) {

                    last = foundation[hand.endingPID - 3].get(size - 1);

                    if ((first.suit == last.suit) && (last.value - first.value == -1))
                        this.buildPile(foundation[hand.endingPID - 3], hand.selected);
                    else
                        undoMove();

                } else {

                    if (first.value == 0)
                        this.buildPile(foundation[hand.endingPID - 3], hand.selected);
                    else
                        undoMove();


                }
                return;
            }


            if (hand.endingPID < 0 || hand.endingPID >= TOTAL_FIELD_PILES || hand.beginningPID == hand.endingPID) {
                undoMove();
                return;
            }

            size = (byte)field[hand.endingPID].cards.size();

            if (size > 0) {

                last = field[hand.endingPID].get(size - 1);

                if (last.isFaceUp() && last.isOppositeTo(first) && (first.value - last.value == -1))
                    this.buildPile(field[hand.endingPID], hand.selected);
                else
                    undoMove();


            } else {

                if (first.value == 12) 
                    this.buildPile(field[hand.endingPID], hand.selected);
                else 
                    undoMove();

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

        if (hand.selected != null) {
            hand.selected.paintComponent(g);
        }
    }
}