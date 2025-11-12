package com.example.easylearnlanguage.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.*;

public class GroupRepository {
    private final GroupDao groupDao;
    private final WordDao  wordDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public GroupRepository(Context c){
        AppDatabase db = AppDatabase.get(c);
        groupDao = db.groupDao();
        wordDao  = db.wordDao();
    }

    public LiveData<List<Group>> observeAll(){ return groupDao.observeAll(); }

    public void add(String title, String from, String to, int color){
        io.execute(() ->
                groupDao.insert(new Group(title, from, to, System.currentTimeMillis(), color))
        );
    }

    public void delete(Group g){ io.execute(() -> groupDao.delete(g)); }

    // каскад: сначала слова группы, потом сама группа
    public void deleteCascade(Group g){
        io.execute(() -> {
            wordDao.clearForGroup(g.id);
            groupDao.delete(g);
        });
    }
}
