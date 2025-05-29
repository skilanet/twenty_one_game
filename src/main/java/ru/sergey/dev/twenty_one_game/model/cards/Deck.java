package ru.sergey.dev.twenty_one_game.model.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Deck {
    final ArrayList<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        Arrays.stream(Suits.values()).toList().forEach(suit -> cards.addAll(generateDeck(suit)));
    }

    private ArrayList<Card> generateDeck(Suits suit) {
        ArrayList<Card> deck = new ArrayList<>(9);
        for (int i = 10; i > 0; i--) {
            if (i == 5) continue;
            if (i == 4) {
                deck.add(new Card("King", suit, i, getCardImagePath(suit, "king")));
            } else if (i == 3) {
                deck.add(new Card("Queen", suit, i, getCardImagePath(suit, "queen")));
            } else if (i == 2) {
                deck.add(new Card("Jack", suit, i,  getCardImagePath(suit, "jack")));
            } else if (i == 1) {
                deck.add(new Card("Ace", suit, i, getCardImagePath(suit, "ace")));
            } else
                deck.add(new Card(String.valueOf(i), suit, i, getCardImagePath(suit, String.valueOf(i))));
        }
        return deck;
    }

    private String getCardImagePath(Suits suit, String value) {
        return String.format("/images/cards/%s/%s_%s.png",
                suit.toString().toLowerCase(),
                suit.toString().toLowerCase(),
                value.toLowerCase());
    }

    public Card getRandomCard() {
        int index = new Random(System.currentTimeMillis()).nextInt(cards.size());
        Card card = cards.get(index);
        cards.remove(index);
        return card;
    }

}

