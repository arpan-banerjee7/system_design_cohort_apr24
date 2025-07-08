package lld.blackjack.model;

import lld.blackjack.enums.Rank;
import lld.blackjack.enums.Suit;

import java.util.*;

public class Shoe implements Iterable<Card> {
    private final Deque<Card> cards = new ArrayDeque<>();

    public Shoe(int numDecks) {
        for (int i = 0; i < numDecks; i++) {
            for (Suit s : Suit.values())
                for (Rank r : Rank.values())
                    cards.add(new Card(s, r));
        }
    }

    public void shuffle() {
        List<Card> tmp = new ArrayList<>(cards);
        Collections.shuffle(tmp);
        cards.clear();
        cards.addAll(tmp);
    }

    public Card draw() {
        if (cards.isEmpty()) throw new IllegalStateException("Shoe is empty");
        return cards.poll();
    }

    public List<Card> drawAll() {
        List<Card> all = new ArrayList<>(cards);
        cards.clear();
        return all;
    }

    @Override
    public Iterator<Card> iterator() {
        return List.copyOf(cards).iterator();
    }

    // For deterministic tests only
    public void setCards(List<Card> newCards) {
        cards.clear();
        cards.addAll(newCards);
    }
}