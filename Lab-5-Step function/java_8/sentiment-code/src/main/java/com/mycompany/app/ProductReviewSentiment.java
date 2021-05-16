package com.mycompany.app;

public class ProductReviewSentiment {
    private int index;
    private String sentiment;
    private double positive;
    private double neutral;
    private double negative;
    private double mixed;
    private String productId;
    private String reviewBody;
    private String reviewHeadline;
    private String starRating;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public double getPositive() {
        return positive;
    }

    public void setPositive(double positive) {
        this.positive = positive;
    }

    public double getNeutral() {
        return neutral;
    }

    public void setNeutral(double neutral) {
        this.neutral = neutral;
    }

    public double getNegative() {
        return negative;
    }

    public void setNegative(double negative) {
        this.negative = negative;
    }

    public double getMixed() {
        return mixed;
    }

    public void setMixed(double mixed) {
        this.mixed = mixed;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getReviewBody() {
        return reviewBody;
    }

    public void setReviewBody(String reviewBody) {
        this.reviewBody = reviewBody;
    }

    public String getReviewHeadline() {
        return reviewHeadline;
    }

    public void setReviewHeadline(String reviewHeadline) {
        this.reviewHeadline = reviewHeadline;
    }

    public String getStarRating() {
        return starRating;
    }

    public void setStarRating(String starRating) {
        this.starRating = starRating;
    }

    @java.lang.Override
    public java.lang.String toString() {

        return "{" +
                "\"Sentiment\": \"" + this.sentiment + "\"," +
                "\"SentimentScore\": {" +
                "\"Positive\": \"" + this.positive + "\"," +
                "\"Negative\": \"" + this.negative + "\"," +
                "\"Neutral\": \"" + this.neutral + "\"," +
                "\"Mixed\": \"" + this.mixed + "\"" +
                "}," +
                "\"product_info\": {" +
                "\"product_id\": \"" + this.productId + "\"," +
                "\"review_body\": \"" + this.reviewBody + "\"," +
                "\"review_headline\": \"" + this.reviewHeadline + "\"," +
                "\"star_rating\": \"" + this.starRating + "\"" +
                "}}";
    }
}