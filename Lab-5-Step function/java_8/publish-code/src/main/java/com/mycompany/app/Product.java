package com.mycompany.app;

public class Product {
    private String product_id;
    private String star_rating;
    private String review_body;
    private String review_headline;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getStar_rating() {
        return star_rating;
    }

    public void setStar_rating(String star_rating) {
        this.star_rating = star_rating;
    }

    public String getReview_body() {
        return review_body;
    }

    public void setReview_body(String review_body) {
        this.review_body = review_body;
    }

    public String getReview_headline() {
        return review_headline;
    }

    public void setReview_headline(String review_headline) {
        this.review_headline = review_headline;
    }
}
