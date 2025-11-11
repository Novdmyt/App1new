package com.example.easylearnlanguage.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = { Group.class, Word.class },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract GroupDao groupDao();
    public abstract WordDao wordDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    ctx.getApplicationContext(),
                                    AppDatabase.class,
                                    "easylearn.db"
                            )
                            // Поки розробка — перезбираємо схему при зміні версії
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
