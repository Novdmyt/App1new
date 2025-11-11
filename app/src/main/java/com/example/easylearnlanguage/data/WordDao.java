package com.example.easylearnlanguage.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WordDao {

    @Query("SELECT * FROM words WHERE groupId = :groupId ORDER BY addedAt DESC")
    LiveData<List<Word>> observeByGroup(long groupId);

    @Insert
    long insert(Word w);

    @Delete
    void delete(Word w);

    @Query("DELETE FROM words WHERE groupId = :groupId")
    void clearForGroup(long groupId);
}
