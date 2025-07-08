package lld.blackjack.config;

public class RuleConfig {
    private static RuleConfig instance;
    private final boolean hitSoft17;
    private final boolean allowLateSurrender;
    private final double blackjackPayout;
    private final int numDecks;

    private RuleConfig(int numDecks, boolean hitSoft17, boolean allowLateSurrender, double blackjackPayout) {
        this.numDecks = numDecks;
        this.hitSoft17 = hitSoft17;
        this.allowLateSurrender = allowLateSurrender;
        this.blackjackPayout = blackjackPayout;
    }

    public static RuleConfig getInstance(int numDecks, boolean hitSoft17, boolean allowLateSurrender, double blackjackPayout) {
        if (instance == null) {
            instance = new RuleConfig(numDecks, hitSoft17, allowLateSurrender, blackjackPayout);
        }
        return instance;
    }

    public boolean isHitSoft17() { return hitSoft17; }
    public boolean isAllowLateSurrender() { return allowLateSurrender; }
    public double getBlackjackPayout() { return blackjackPayout; }
    public int getNumDecks() { return numDecks; }
} 