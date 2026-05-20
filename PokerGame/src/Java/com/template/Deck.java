package com.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        reset();
    }

    public void reset() {
        cards.clear();
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

        for (Card.Suit suit : Card.Suit.values()) {
            for (int i = 0; i < ranks.length; i++) {
                cards.add(new Card(ranks[i], values[i], suit));
            }
        }
        Collections.shuffle(cards);
    }

    public Card dealCard() {
        if (cards.isEmpty()) {
            reset();
        }
        return cards.remove(0);
    }
}