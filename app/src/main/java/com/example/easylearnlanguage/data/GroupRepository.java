// com/example/easylearnlanguage/data/GroupRepository.java
package com.example.easylearnlanguage.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupRepository {
    private final GroupDao groupDao;
    private final WordDao wordDao;           // ДОДАНО
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public GroupRepository(Context c){
        AppDatabase db = AppDatabase.get(c);
        groupDao = db.groupDao();
        wordDao  = db.wordDao();             // ДОДАНО
    }

    public LiveData<List<Group>> observeAll(){ return groupDao.observeAll(); }

    public void add(String title, String from, String to, int color){
        io.execute(() ->
                groupDao.insert(new Group(title, from, to, System.currentTimeMillis(), color))
        );
    }

    public void delete(Group g){
        io.execute(() -> groupDao.delete(g));
    }

    // Каскад: спочатку прибираємо слова групи, потім саму групу
    public void deleteCascade(Group g){
        io.execute(() -> {
            wordDao.clearForGroup(g.id);
            groupDao.delete(g);
        });
    }
}
