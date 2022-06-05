package Solitaire;

import javax.swing.JComponent;

import java.util.ArrayList;
import java.util.List;

import java.awt.Graphics;
import java.awt.Color;

public class Pile extends JComponent {
    public List<Card> cards = new ArrayList<Card>();
    public Coordinate position = new Coordinate((short)0,(short)0);
    public Coordinate offset = new Coordinate((short)0, (short)0);
    
    public static final Color color = new Color(0,225,0,255);
    public Pile() {}
    public Pile(Coordinate position) {
        this.position = position;
    }
    
    public void add(Card card) {
        this.cards.add(card);
        short size = (short)(this.cards.size() - 1);
        short offsetY = (short)(size * this.offset.y);
        short offsetX = (short)(size * this.offset.x);
        
        card.setPosition((short)(this.position.x + offsetX), (short)(this.position.y + offsetY));
    }
    
    public void setOffset(short x, short y) {
        this.offset.x = x;
        this.offset.y = y;
    }
    
    public Card get(int index) {
        return this.cards.get(index);
    }
    
    public byte indexOf(Card card) {
        for(byte i = 0; i < Deck.SIZE; i++) {
            if(card.equals(this.cards.get(i))) 
                return i;
        }
        return -1;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(this.color);
        g.fillRoundRect(this.position.x - 3, this.position.y - 3, Card.WIDTH + 6, Card.HEIGHT + 6, 16, 16);
    }
}