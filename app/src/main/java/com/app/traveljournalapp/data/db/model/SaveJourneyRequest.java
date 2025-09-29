package com.app.traveljournalapp.data.db.model;

public class SaveJourneyRequest {

    private String action = "save_journey";
    private String user_id;
    private String title;
    private String date;
    private String address;
    private String description;

    public SaveJourneyRequest(String user_id, String title, String date, String address, String description) {
        this.user_id = user_id;
        this.title = title;
        this.date = date;
        this.address = address;
        this.description = description;
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
