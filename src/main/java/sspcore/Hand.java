package sspcore;

public enum Hand {

    SCHERE,
    STEIN,
    PAPIER;

    static {
        STEIN.beatsHands = SCHERE;
        PAPIER.beatsHands = STEIN;
        SCHERE.beatsHands = PAPIER;
    }

    private Hand beatsHands;

    public boolean beats(Hand hand) {
        if (beatsHands == hand){
            return true;
        }else{
            return false;
        }
    }
}
