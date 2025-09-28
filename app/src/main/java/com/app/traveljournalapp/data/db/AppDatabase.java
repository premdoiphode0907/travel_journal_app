package com.app.traveljournalapp.data.db;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.app.traveljournalapp.data.db.entity.Journey;
import com.app.traveljournalapp.data.db.entity.User;


@Database(entities = {User.class, Journey.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    public abstract JourneyDao journeyDao();
}


