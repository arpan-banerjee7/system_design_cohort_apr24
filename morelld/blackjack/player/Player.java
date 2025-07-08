package lld.blackjack.player;

import lld.blackjack.enums.Action;
import lld.blackjack.model.Hand;
import lld.blackjack.model.Card;
import lld.blackjack.player.decorator.Bet;

public abstract class Player {
    protected Hand hand = new Hand();
    protected Bet bet;
    protected double balance;

    // Player's responsibility: decide what to do
    public abstract void placeBet(double amount);
    public abstract Action decideAction(Card dealerUpCard);

    // State management
    public Hand getHand() { return hand; }
    public Bet getBet() { return bet; }
    public double getBalance() { return balance; }
    public void setHand(Hand hand) { this.hand = hand; }
    public void setBalance(double balance) { this.balance = balance; }
    public void resetBet() { this.bet = null; }
    public void setBet(Bet bet) { this.bet = bet; }
} 