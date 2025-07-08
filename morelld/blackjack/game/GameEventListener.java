package lld.blackjack.game;


import lld.blackjack.model.Card;
import lld.blackjack.model.Hand;
import lld.blackjack.player.Player;

public interface GameEventListener {
    void onCardDealt(Player player, Card card);
    void onHandResolved(Player player, Hand hand);
    // Add more event methods as needed
} 