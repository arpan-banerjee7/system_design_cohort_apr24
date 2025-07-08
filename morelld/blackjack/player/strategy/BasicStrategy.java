package lld.blackjack.player.strategy;

import lld.blackjack.enums.Action;
import lld.blackjack.model.Hand;
import lld.blackjack.model.Card;

public class BasicStrategy implements DecisionStrategy {
    @Override
    public Action decide(Hand hand, Card dealerUpCard) {
        // TODO: Implement full basic strategy logic
        int total = hand.getBestTotal();
        // Simplest placeholder: stand on 17+, else hit
        if (total >= 17) return Action.STAND;
        return Action.HIT;
    }
} 