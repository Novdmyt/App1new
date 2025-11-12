package com.example.easylearnlanguage.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class Group {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String title;
    @NonNull public String from;
    @NonNull public String to;
    public long createdAt;
    public int color;

    public Group(@NonNull String title, @NonNull String from, @NonNull String to,
                 long createdAt, int color) {
        this.title = title;
        this.from = from;
        this.to = to;
        this.createdAt = createdAt;
        this.color = color;
    }
}
