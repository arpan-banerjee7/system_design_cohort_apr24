package lld.blackjack.player.decorator;

public abstract class BetDecorator implements Bet {
    protected final Bet decoratedBet;

    public BetDecorator(Bet decoratedBet) {
        this.decoratedBet = decoratedBet;
    }

    @Override
    public double getAmount() {
        return decoratedBet.getAmount();
    }
} 