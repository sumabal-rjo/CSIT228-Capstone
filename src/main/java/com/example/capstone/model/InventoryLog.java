package com.example.capstone.model;

public class InventoryLog extends BaseEntity {

    private int productId;
    private String productName;
    private int userId;
    private String userName;
    private String changeType;
    private int quantityBefore;
    private int quantityChange;
    private int quantityAfter;
    private String note;
    private String loggedAt;

    public int getLogId() {
        return id;
    }

    public void setLogId(int logId) {
        this.id = logId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public int getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(int quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    public int getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(int quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(String loggedAt) {
        this.loggedAt = loggedAt;
    }

    @Override
    public String getDisplayLabel() {
        return changeType + " - " + productName;
    }
}
