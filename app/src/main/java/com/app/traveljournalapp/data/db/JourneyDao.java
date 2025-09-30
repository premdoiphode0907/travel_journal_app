package com.app.traveljournalapp.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.app.traveljournalapp.data.db.entity.Journey;

import java.util.List;

@Dao
public interface JourneyDao {

    // Insert a journey into the database
    @Insert
    void insert(Journey journey);

    // Get all journeys from the database
    @Query("SELECT * FROM journeys")
    List<Journey> getAllJourneys();

    @Query("SELECT * FROM journeys WHERE id = :journeyId")
    Journey getJourneyById(long journeyId);
}
