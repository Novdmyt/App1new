package com.example.easylearnlanguage.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GroupDao {

    @Query("SELECT * FROM study_groups ORDER BY createdAt DESC")
    LiveData<List<Group>> observeAll();

    @Insert
    long insert(Group g);

    @Delete
    void delete(Group g);

    @Query("DELETE FROM study_groups")
    void clear();
}
