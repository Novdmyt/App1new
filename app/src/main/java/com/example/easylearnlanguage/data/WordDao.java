package com.example.easylearnlanguage.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface WordDao {

    @Query("SELECT * FROM words WHERE groupId = :groupId ORDER BY id DESC")
    LiveData<List<Word>> observeByGroup(long groupId);

    @Query("SELECT * FROM words ORDER BY front COLLATE NOCASE")
    LiveData<List<Word>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Word word);

    @Delete
    void delete(Word word);

    @Query("DELETE FROM words WHERE groupId = :groupId")
    void clearForGroup(long groupId);

    @Query("UPDATE words SET front = :front, back = :back WHERE id = :id")
    void rename(long id, String front, String back);

}
