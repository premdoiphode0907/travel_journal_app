package com.app.traveljournalapp.data.db.model;

import java.util.List;

public class MemoryResponse {
    private String status;
    private List<MemoryItem> memories;

    public List<MemoryItem> getMemories() {
        return memories;
    }

    public void setMemories(List<MemoryItem> memories) {
        this.memories = memories;
    }

    public static class MemoryItem {
        private String id;
        private String user_id;
        private String journey_id;
        private String photo_url;
        private String created_at;
        private String journey_title;

        public String getPhotoUrl() {
            return photo_url;
        }

        public String getJourneyTitle() {
            return journey_title;
        }
    }
}
