package com.app.traveljournalapp.data.db.model;

import com.app.traveljournalapp.data.db.entity.Journey;

import java.util.List;

public class JourneyResponse {

    private String status;  // "success" or "failure"
    private List<Journey> journeys;  // List of journey objects

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Journey> getJourneys() {
        return journeys;
    }

    public void setJourneys(List<Journey> journeys) {
        this.journeys = journeys;
    }
}
