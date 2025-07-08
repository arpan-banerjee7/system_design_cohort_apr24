package lld.blackjack.phases;

import lld.blackjack.enums.PhaseType;
import lld.blackjack.game.GameEngine;
import lld.blackjack.player.Dealer;
import lld.blackjack.player.HumanPlayer;
import lld.blackjack.model.Hand;
import lld.blackjack.player.decorator.Bet;
import lld.blackjack.player.decorator.InsuranceBet;

public class DealerBlackjackSettlementPhase implements GamePhase {
    @Override
    public void enter(GameEngine engine) {
        System.out.println("[DealerBlackjackSettlementPhase] Dealer has blackjack! All players lose unless they also have blackjack.");
    }

    @Override
    public void handle(GameEngine engine) {
        Dealer dealer = engine.getDealer();
        var players = engine.getPlayers();
        for (int idx = 0; idx < players.size(); idx++) {
            HumanPlayer p = players.get(idx);
            String playerLabel = "Player " + (idx + 1);
            Bet bet = p.getBet();
            double insurancePayout = 0;
            if (bet instanceof InsuranceBet) {
                double insuranceAmount = ((InsuranceBet) bet).getInsuranceAmount();
                insurancePayout = insuranceAmount * 2; // Insurance pays 2:1
                p.setBalance(p.getBalance() + insurancePayout);
                System.out.println(playerLabel + " wins insurance! Insurance payout: " + insurancePayout);
            }
            if (p.getHand().isBlackjack()) {
                System.out.println(playerLabel + " has blackjack too. Push.");
                p.setBalance(p.getBalance() + bet.getAmount());
            } else {
                System.out.println(playerLabel + " loses.");
            }
            System.out.println(playerLabel + " hand: " + p.getHand().getCards());
            System.out.println(playerLabel + " balance: " + p.getBalance());
        }
        System.out.println("Dealer hand: " + dealer.getHand().getCards());
        // Clear hands for next round
        for (HumanPlayer p : engine.getPlayers()) p.setHand(new Hand());
        dealer.setHand(new Hand());
        for (HumanPlayer p : engine.getPlayers()) p.resetBet();
        dealer.resetBet();
        System.out.println("Final balances after settlement:");
        for (int i = 0; i < players.size(); i++) {
            HumanPlayer p = players.get(i);
            System.out.println("Player " + (i + 1) + " final balance: " + p.getBalance());
        }
    }

    @Override
    public GamePhase next(GameEngine engine) {
        return null; // End of round
    }

    @Override
    public PhaseType getPhaseType() { return PhaseType.DEALER_BLACKJACK_SETTLEMENT; }
} 