package com.mycompany.app;

public class SentimentScore {

    private String Positive;
    private String Negative;
    private String Neutral;
    private String Mixed;

    public String getPositive() {
        return Positive;
    }

    public void setPositive(String positive) {
        Positive = positive;
    }

    public String getNegative() {
        return Negative;
    }

    public void setNegative(String negative) {
        Negative = negative;
    }

    public String getNeutral() {
        return Neutral;
    }

    public void setNeutral(String neutral) {
        Neutral = neutral;
    }

    public String getMixed() {
        return Mixed;
    }

    public void setMixed(String mixed) {
        Mixed = mixed;
    }
}
