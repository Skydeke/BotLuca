package sspcore;

import net.dv8tion.jda.core.entities.User;

public class Player {

    private Hand hand;
    private User player;

    public Player(Hand hand, User player){
        this.hand = hand;
        this.player = player;
    }

    public Hand getHand() {
        return hand;
    }

    public User getUser() {
        return player;
    }
}
