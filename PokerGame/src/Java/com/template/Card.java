package com.template;

public class Card {
    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    private String rank;
    private int value;
    private Suit suit;

//constructor
    public Card(String rank, int value, Suit suit) {
        this.rank = rank;
        this.value = value;
        this.suit = suit;
    }

    public String getRank() {
        return rank;
    }

    public int getValue() {
        return value;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}