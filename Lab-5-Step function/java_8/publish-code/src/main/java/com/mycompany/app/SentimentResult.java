package com.mycompany.app;

public class SentimentResult {
    private String Sentiment;
    private SentimentScore SentimentScore;
    private Product product_info;

    public String getSentiment() {
        return Sentiment;
    }

    public void setSentiment(String sentiment) {
        Sentiment = sentiment;
    }

    public com.mycompany.app.SentimentScore getSentimentScore() {
        return SentimentScore;
    }

    public void setSentimentScore(com.mycompany.app.SentimentScore sentimentScore) {
        SentimentScore = sentimentScore;
    }

    public Product getProduct_info() {
        return product_info;
    }

    public void setProduct_info(Product product_info) {
        this.product_info = product_info;
    }
}
