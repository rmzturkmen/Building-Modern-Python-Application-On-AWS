package com.mycompany.app;

import com.amazonaws.services.comprehend.model.Entity;

import java.util.ArrayList;
import java.util.List;

public class ProductReviewTag {
    private List<Entity> entities = new ArrayList<>();
    private String productId;
    private String reviewBody;
    private String reviewHeadline;
    private String starRating;

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
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

        String productReviewTag = "{" +
                "\"Entities\": [";

        for (int i = 0; i < this.entities.size(); i++) {
            productReviewTag += "{ \"Score\": \"" + this.entities.get(i).getScore() + "\"," +
                    "\"Type\": \"" + this.entities.get(i).getType() + "\"," +
                    "\"Text\": \"" + this.entities.get(i).getText() + "\"," +
                    "\"BeginOffset\": \"" + this.entities.get(i).getBeginOffset() + "\"," +
                    "\"EndOffset\": \"" + this.entities.get(i).getEndOffset() + "\"" +
                    "}";
            if(i != this.entities.size()-1) {
                productReviewTag += ",";
            }
        }

        productReviewTag += "]," +
                "\"product_info\": {" +
                "\"product_id\": \"" + this.productId + "\"," +
                "\"review_body\": \"" + this.reviewBody + "\"," +
                "\"review_headline\": \"" + this.reviewHeadline + "\"," +
                "\"star_rating\": \"" + this.starRating + "\"" +
                "}}";
        return productReviewTag;
    }
}