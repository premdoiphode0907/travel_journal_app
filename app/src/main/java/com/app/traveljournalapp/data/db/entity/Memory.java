package com.app.traveljournalapp.data.db.entity;

public class Memory {
    private String id;
    private String user_id;
    private String journey_id;
    private String photo_url;
    private String created_at;
    private String journey_title; // add if backend returns, else pass separately

    public String getPhotoUrl(){ return photo_url; }
    public String getJourneyTitle(){ return journey_title != null ? journey_title : ""; }
}