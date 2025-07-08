package lld.blackjack.factory;

import lld.blackjack.model.Shoe;

public class ShoeFactory {
    public static Shoe createShoe() {
        Shoe shoe = new Shoe(6);
        shoe.shuffle();
        return shoe;
    }
} 