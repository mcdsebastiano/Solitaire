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
    public List<Card> selected = new ArrayList<Card>();
    public List<Card> origin = new ArrayList<Card>();
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

    public static final byte NUMBER_FOUNDATION_PILES = 4;
    public static final byte NUMBER_FIELD_PILES = 7;
    public static final byte PILE_GAP = 10;

    // The selection algorithm wont work right now if we use an xmargin.
    // It's zero right now, but things should be centered eventually.
    public static final short PLAYABLE_REGION = 620;
    public static final byte YMARGIN = 25;
    public static final byte XMARGIN = 0;

    // Piles
    Pile waste = new Pile(new Coordinate((short)116, (short)YMARGIN));
    Pile stock = new Pile(new Coordinate((short)25, (short)YMARGIN));
    Pile[] foundation = new Pile[NUMBER_FOUNDATION_PILES];
    Pile[] field = new Pile[NUMBER_FIELD_PILES];

    // Game State
    public byte stockCount = 23;
    public byte wasteCount = -1;
    Hand hand;

    public Tableau() {
        this.hand = new Hand();
        for (byte i = 0; i < Deck.SIZE; i++) {
            Card card = new Card((char)(i % 13), (byte)(i / 13));
            Deck.cards.add(card);
        }

        // @FIXME -- don't be lazy
        Collections.shuffle(Deck.cards);

        for (byte i = 0; i < Deck.SIZE; i++) {
            Card card = Deck.cards.get(i);
            stock.add(card);
        }

        for (byte i = 0; i < NUMBER_FOUNDATION_PILES; i++) {
            foundation[i] = new Pile(new Coordinate((short) (350 + (((i - 1) * Card.WIDTH) + PILE_GAP * i)), (short)YMARGIN));
        }

        for (byte i = 0; i < NUMBER_FIELD_PILES; i++) {
            field[i] = new Pile(new Coordinate((short)(XMARGIN + ((i * Card.WIDTH) + PILE_GAP * i)), (short)200));
            field[i].setOffset((short)YMARGIN);
            for (byte k = 0; k < i + 1; k++) {
                Card card = Deck.cards.get(k + NUMBER_FIELD_PILES * i);
                if (k == i)
                    card.flip();
                byte idx = stock.indexOf(card);
                stock.cards.remove(idx);
                field[i].add(card);
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

    public void movePiles(Pile to, List<Card> selected) {
        byte size = (byte)(selected.size());
        for (byte i = 0; i < size; i++) {
            Card card = selected.get(i);
            to.add(card);
        }
    }

    public void mousePressed(MouseEvent e) {
        short mX = (short)e.getX();
        short mY = (short)e.getY();

        // We clicked on the stockpile
        if (stock.contains(mX, mY)) {
            if (stockCount >= 0) {
                // Pile is not empty -- Draw One Card
                hand.selected = stock.cards.subList(stockCount, stockCount + 1);
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
                hand.selected = waste.cards.subList(0, wasteCount + 1);
                hand.selected.forEach(card -> card.flip());
                movePiles(stock, hand.selected);
                wasteCount = -1;
            }
            hand.selected.clear();
            return;
        }

        if (waste.contains(mX, mY)) {
            // We clicked on the wastepile, get the top card.
            Card top = waste.cards.get(wasteCount);
            if (top.contains(mX, mY)) {
                hand.beginningPID = -1;
                hand.xStart = top.position.x;
                hand.yStart = top.position.y;
                hand.xOffset = (short)(mX - top.position.x);
                hand.yOffset = (short)(mY - top.position.y);

                for (Card card : waste.cards)
                    hand.origin.add(card.clone());

                hand.selected = waste.cards.subList((int)wasteCount, wasteCount + 1);
                wasteCount--;
            }
        } else {

            hand.normalX = (float)(PLAYABLE_REGION) / mX;
            hand.beginningPID = (byte)(NUMBER_FIELD_PILES / hand.normalX);

            if (hand.beginningPID >= NUMBER_FIELD_PILES)
                return;

            Card selected = this.getCard(field[hand.beginningPID], mX, mY);

            if (selected == null && hand.beginningPID >= 3)
                selected = this.getCard(foundation[hand.beginningPID - 3], mX, mY);

            if (selected == null)
                return;

            if (selected.isFaceUp()) {
                hand.xStart = selected.position.x;
                hand.yStart = selected.position.y;
                hand.xOffset = (short)(mX - selected.position.x);
                hand.yOffset = (short)(mY - selected.position.y);

                int size = field[hand.beginningPID].cards.size();
                int idx = (int)(field[hand.beginningPID].indexOf(selected));
                hand.selected = field[hand.beginningPID].cards.subList(idx, size);

                for (Card card : field[hand.beginningPID].cards)
                    hand.origin.add(card.clone());

            } else {
                byte idx = field[hand.beginningPID].indexOf(selected);
                if (idx == field[hand.beginningPID].cards.size() - 1)
                    selected.flip();

            }
        }
        this.repaint();
    }

    public void mouseDragged(MouseEvent e) {
        if (!hand.selected.isEmpty()) {
            short mX = (short)e.getX();
            short mY = (short)e.getY();

            for (byte i = 0; i < hand.selected.size(); i++) {
                Card card = hand.selected.get(i);
                card.setPosition((short)(mX - hand.xOffset), (short)(mY - hand.yOffset + (i * YMARGIN)));
            }
            this.repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (!hand.selected.isEmpty()) {
            short mX = (short)e.getX();
            short mY = (short)e.getY();

            hand.normalX =  (float)(PLAYABLE_REGION) / mX;
            hand.endingPID = (byte)(NUMBER_FIELD_PILES / hand.normalX);

            byte idx = 0;
            if (mY >= 200 && hand.endingPID >= 0 && hand.endingPID <= NUMBER_FIELD_PILES && hand.beginningPID != hand.endingPID) {
                this.movePiles(field[hand.endingPID], hand.selected);
            } else if (hand.beginningPID == -1) {
                hand.selected.get(0).setPosition(waste.position.x, waste.position.y);
                waste.cards = new ArrayList<Card>(hand.origin);
                wasteCount++;
            } else {
                byte size = (byte)(hand.selected.size());
                for (byte i = 0; i < size; i++)
                    hand.selected.get(i).setPosition(hand.xStart, (short)(hand.yStart + i * YMARGIN));

                field[hand.beginningPID].cards = new ArrayList<Card>(hand.origin);
            }

            hand.selected.clear();
            hand.origin.clear();
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

        // if (hand.selected != null) {
        // hand.selected.paintComponent(g);
        // }
    }
}
