package lld.blackjack.player.strategy;

import lld.blackjack.enums.Action;
import lld.blackjack.model.Hand;
import lld.blackjack.model.Card;

public interface DecisionStrategy {
    Action decide(Hand hand, Card dealerUpCard);
} 