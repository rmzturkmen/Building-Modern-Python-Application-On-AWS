package com.mycompany.app;

import java.util.ArrayList;
import java.util.List;

public class ProductReviewSentimentTag {
    private String sentiment;
    private String positive;
    private String neutral;
    private String negative;
    private String mixed;
    private String productId;
    private String reviewBody;
    private String reviewHeadline;
    private String starRating;
    private List<Entity> entities = new ArrayList<>();

    ProductReviewSentimentTag(SentimentResult sentiment, TagResult tag) {
        this.reviewBody = tag.getProduct_info().getReview_body();
        this.reviewHeadline = tag.getProduct_info().getReview_headline();
        this.starRating = tag.getProduct_info().getStar_rating();
        this.sentiment = sentiment.getSentiment();
        this.positive = sentiment.getSentimentScore().getPositive();
        this.negative = sentiment.getSentimentScore().getNegative();
        this.neutral = sentiment.getSentimentScore().getNeutral();
        this.mixed = sentiment.getSentimentScore().getMixed();
        this.productId = tag.getProduct_info().getProduct_id();
        if(tag.getEntities() != null) {
            this.entities = tag.getEntities();
        }
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getPositive() {
        return positive;
    }

    public void setPositive(String positive) {
        this.positive = positive;
    }

    public String getNeutral() {
        return neutral;
    }

    public void setNeutral(String neutral) {
        this.neutral = neutral;
    }

    public String getNegative() {
        return negative;
    }

    public void setNegative(String negative) {
        this.negative = negative;
    }

    public String getMixed() {
        return mixed;
    }

    public void setMixed(String mixed) {
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

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }
}
