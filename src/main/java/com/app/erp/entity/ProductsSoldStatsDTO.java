package com.app.erp.entity;

import java.util.ArrayList;
import java.util.List;

public class ProductsSoldStatsDTO {
    private List<String> months;
    private List<Integer> counts;

    public ProductsSoldStatsDTO() {
        this.months = new ArrayList<>();
        this.counts = new ArrayList<>();
    }

    public List<String> getMonths() {
        return months;
    }

    public void setMonths(List<String> months) {
        this.months = months;
    }

    public List<Integer> getCounts() {
        return counts;
    }

    public void setCounts(List<Integer> counts) {
        this.counts = counts;
    }
}