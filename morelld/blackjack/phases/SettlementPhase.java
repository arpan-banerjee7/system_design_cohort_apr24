package lld.blackjack.phases;

import lld.blackjack.enums.PhaseType;
import lld.blackjack.game.GameEngine;
import lld.blackjack.model.Hand;
import lld.blackjack.player.Dealer;
import lld.blackjack.player.HumanPlayer;


public class SettlementPhase implements GamePhase {
    @Override
    public void enter(GameEngine engine) {
        System.out.println("[SettlementPhase] Settling bets...");
    }

    @Override
    public void handle(GameEngine engine) {
        Dealer dealer = engine.getDealer();
        int dealerTotal = dealer.getHand().getBestTotal();
        boolean dealerBusted = dealer.getHand().isBusted();
        var players = engine.getPlayers();
        for (int idx = 0; idx < players.size(); idx++) {
            HumanPlayer player = players.get(idx);
            int playerTotal = player.getHand().getBestTotal();
            boolean playerBusted = player.getHand().isBusted();
            String playerLabel = "Player " + (idx + 1);
            if (playerBusted) {
                System.out.println(playerLabel + " busted earlier. No settlement.");
                System.out.println(playerLabel + " final hand: " + player.getHand().getCards() + " (" + playerTotal + ")");
                continue;
            }
            if (player.getHand().isBlackjack() && !dealer.getHand().isBlackjack()) {
                System.out.println(playerLabel + " has blackjack! Wins 3:2");
                player.setBalance(player.getBalance() + player.getBet().getAmount() * 2.5);
            } else if (dealerBusted || playerTotal > dealerTotal) {
                System.out.println(playerLabel + " wins! Gets back bet plus winnings (2x bet total).");
                player.setBalance(player.getBalance() + player.getBet().getAmount() * 2);
            } else if (playerTotal == dealerTotal) {
                System.out.println("Push. " + playerLabel + " gets bet back.");
                player.setBalance(player.getBalance() + player.getBet().getAmount());
            } else {
                System.out.println(playerLabel + " loses.");
            }
            System.out.println(playerLabel + " final hand: " + player.getHand().getCards() + " (" + playerTotal + ")");
        }
        System.out.println("Dealer final hand: " + dealer.getHand().getCards() + " (" + dealerTotal + ")");
        // Clear hands for next round
        for (HumanPlayer player : engine.getPlayers()) player.setHand(new Hand());
        dealer.setHand(new Hand());
        for (HumanPlayer player : engine.getPlayers()) player.resetBet();
        dealer.resetBet();
        System.out.println("Final balances after settlement:");
        for (int i = 0; i < players.size(); i++) {
            HumanPlayer player = players.get(i);
            System.out.println("Player " + (i + 1) + " final balance: " + player.getBalance());
        }
        System.out.println("[SettlementPhase] Complete.");
        System.out.println();
    }

    @Override
    public GamePhase next(GameEngine engine) {
        return null; // End of round
    }

    @Override
    public PhaseType getPhaseType() { return PhaseType.SETTLEMENT; }
} 