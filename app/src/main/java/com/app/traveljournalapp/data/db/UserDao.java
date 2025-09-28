package com.app.traveljournalapp.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.app.traveljournalapp.data.db.entity.User;




@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM user WHERE email = :email")
    User getUserByEmail(String email);
}


