package com.mycompany.app;

import java.util.ArrayList;
import java.util.List;

public class TagResult {
    private List<Entity> Entities = new ArrayList<>();
    private Product product_info;

    public List<Entity> getEntities() {
        return Entities;
    }

    public void setEntities(List<Entity> entities) {
        Entities = entities;
    }

    public Product getProduct_info() {
        return product_info;
    }

    public void setProduct_info(Product product_info) {
        this.product_info = product_info;
    }
}
