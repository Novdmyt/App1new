package com.example.easylearnlanguage.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface GroupDao {
    @Query("SELECT * FROM `groups` ORDER BY title COLLATE NOCASE")
    LiveData<List<Group>> observeAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Group g);

    @Delete
    void delete(Group g);

    @Query("DELETE FROM `groups`")
    void clearAll();

    // NEW: перейменування
    @Query("UPDATE `groups` SET title = :title WHERE id = :id")
    void rename(long id, String title);
}
