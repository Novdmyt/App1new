package com.example.easylearnlanguage.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "words",
        indices = {@Index("groupId")}
)
public class Word {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long groupId;    // FK на study_groups.id
    public String front;    // іноземне слово / речення
    public String back;     // переклад
    public long addedAt;    // System.currentTimeMillis()

    public Word(long groupId, String front, String back, long addedAt) {
        this.groupId = groupId;
        this.front = front;
        this.back = back;
        this.addedAt = addedAt;
    }
}
