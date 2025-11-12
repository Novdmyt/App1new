package com.example.easylearnlanguage.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "words")
public class Word {
    @PrimaryKey(autoGenerate = true) public long id;
    public long groupId;
    @NonNull public String front; // иностранное слово (можно с артиклем)
    @NonNull public String back;  // перевод

    public Word(long groupId, @NonNull String front, @NonNull String back) {
        this.groupId = groupId;
        this.front = front;
        this.back  = back;
    }
}
