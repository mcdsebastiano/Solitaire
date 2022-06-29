package Solitaire;

import javax.swing.JComponent;

import java.util.ArrayList;
import java.util.List;

import java.awt.Graphics;
import java.awt.Color;

public class Pile extends JComponent {
    public Coordinate position = new Coordinate((short)-Card.WIDTH,(short)-Card.HEIGHT);
    public static final Color color = new Color(0,160,0,255);
    public List<Card> cards = new ArrayList<Card>();
    public short offset = 0;
    
    public short width;
    public short height;
    
    public Pile() {
        this.width = Card.WIDTH;
        this.height = Card.HEIGHT;
    }
    
    public Pile(Coordinate position) {
        this.position = position;
        this.width = Card.WIDTH;
        this.height = Card.HEIGHT;
    }
    
    public boolean contains(short mouseX, short mouseY) {
        if (mouseY >= this.position.y && mouseY <= this.position.y + this.height && mouseX >= this.position.x && mouseX <= this.position.x + this.width) {
            return true;
        }
        return false;
    }
    
    public void add(Card card) {
        this.cards.add(card);
        short size = (short)(this.cards.size() - 1);
        short yOffset = (short)(size * this.offset);
        this.height = size > 0 ? (short)(this.height + this.offset) : Card.HEIGHT;
        card.setPosition(this.position.x, (short)(this.position.y + yOffset));
    }
    
    public void remove(int index) {
        this.cards.remove(index);
        short size = (short)(this.cards.size() - 1);
        this.height -= size > Card.HEIGHT ? Card.HEIGHT - this.offset : 0;
    }
    
    public void setOffset(short y) { 
        this.offset = y;
    }
    
    public Card get(int index) {
        return this.cards.get(index);
    }
    
    public byte indexOf(Card card) {
        for(byte i = 0; i < this.cards.size(); i++) {
            if(card.equals(this.cards.get(i))) 
                return i;
        }
        return -1;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(this.position.y < 200) {
            g.setColor(this.color);
            g.fillRoundRect(this.position.x - 3, this.position.y - 3, Card.WIDTH + 6, Card.HEIGHT + 6, 16, 16);
        }
        cards.forEach(card -> card.paintComponent(g));
    }
}
