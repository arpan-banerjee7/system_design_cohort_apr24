package lld.blackjack.player.decorator;

public class InsuranceBet extends BetDecorator {
    private final double insuranceAmount;

    public InsuranceBet(Bet decoratedBet, double insuranceAmount) {
        super(decoratedBet);
        this.insuranceAmount = insuranceAmount;
    }

    @Override
    public double getAmount() {
        // Total bet is original bet + insurance
        return super.getAmount() + insuranceAmount;
    }

    public double getInsuranceAmount() {
        return insuranceAmount;
    }
} 