package lld.blackjack.model;

import java.util.ArrayList;
import java.util.List;
import lld.blackjack.enums.Rank;

public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return cards;
    }

    public int getBestTotal() {
        int total = 0;
        int aces = 0;
        for (Card c : cards) {
            total += c.getRank().getValue();
            if (c.getRank() == Rank.ACE) aces++;
        }
        while (total > 21 && aces > 0) {
            total -= 10; // Count Ace as 1 instead of 11
            aces--;
        }
        return total;
    }

    public boolean isBusted() {
        return getBestTotal() > 21;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getBestTotal() == 21;
    }
} 