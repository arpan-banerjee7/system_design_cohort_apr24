package lld.blackjack.player.decorator;

public class SimpleBet implements Bet {
    private final double amount;

    public SimpleBet(double amount) {
        this.amount = amount;
    }

    @Override
    public double getAmount() {
        return amount;
    }
} 