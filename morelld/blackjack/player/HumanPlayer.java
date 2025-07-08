package lld.blackjack.player;

import lld.blackjack.enums.Action;
import lld.blackjack.model.Hand;
import lld.blackjack.model.Card;
import lld.blackjack.player.strategy.DecisionStrategy;
import lld.blackjack.player.decorator.SimpleBet;
import lld.blackjack.player.decorator.InsuranceBet;

public class HumanPlayer extends Player {
    private final DecisionStrategy strategy;

    public HumanPlayer(double initialBalance, DecisionStrategy strategy) {
        this.balance = initialBalance;
        this.strategy = strategy;
    }

    @Override
    public void placeBet(double amount) {
        if (amount > balance) throw new IllegalArgumentException("Insufficient balance");
        this.bet = new SimpleBet(amount);
        this.balance -= amount;
    }

    public void placeInsurance(double insuranceAmount) {
        if (insuranceAmount > balance) throw new IllegalArgumentException("Insufficient balance for insurance");
        this.bet = new InsuranceBet(this.bet, insuranceAmount);
        this.balance -= insuranceAmount;
    }

    @Override
    public Action decideAction(Card dealerUpCard) {
        return strategy.decide(hand, dealerUpCard);
    }

    @Override
    public void setHand(Hand hand) {
        super.setHand(hand);
    }

    @Override
    public void setBalance(double balance) {
        super.setBalance(balance);
    }
} 