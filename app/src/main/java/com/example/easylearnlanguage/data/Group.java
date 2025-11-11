package com.example.easylearnlanguage.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "study_groups")
public class Group {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;        // назва набору/групи
    public String fromLang;     // вихідна мова (наприклад "DE")
    public String toLang;       // цільова мова (наприклад "UK")
    public long createdAt;      // System.currentTimeMillis()
    public int color;           // ARGB (можна 0 для дефолту)

    public Group(String title, String fromLang, String toLang, long createdAt, int color) {
        this.title = title;
        this.fromLang = fromLang;
        this.toLang = toLang;
        this.createdAt = createdAt;
        this.color = color;
    }
}
