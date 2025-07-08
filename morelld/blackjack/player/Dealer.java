package lld.blackjack.player;


import lld.blackjack.config.RuleConfig;
import lld.blackjack.enums.Action;
import lld.blackjack.enums.Rank;
import lld.blackjack.model.Shoe;
import lld.blackjack.model.Card;
import lld.blackjack.model.Hand;

public class Dealer extends Player {
    private final RuleConfig config;

    public Dealer(RuleConfig config) {
        this.config = config;
    }

    @Override
    public void placeBet(double amount) {
        // Dealer does not bet
    }

    @Override
    public Action decideAction(Card dealerUpCard) {
        int total = hand.getBestTotal();
        boolean soft17 = total == 17 && hand.getCards().stream().anyMatch(c -> c.getRank() == Rank.ACE);
        if (total < 17 || (soft17 && config.isHitSoft17())) {
            return Action.HIT;
        }
        return Action.STAND;
    }

    public void playDealerHand(Shoe shoe) {
        while (true) {
            Action action = decideAction(null);
            if (action == Action.HIT) {
                hand.addCard(shoe.draw());
            } else break;
        }
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