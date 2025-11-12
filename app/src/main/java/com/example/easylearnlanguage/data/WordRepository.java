package com.example.easylearnlanguage.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.*;

public class WordRepository {
    private final WordDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public WordRepository(Context c) { dao = AppDatabase.get(c).wordDao(); }

    public LiveData<List<Word>> wordsByGroup(long groupId) { return dao.observeByGroup(groupId); }
    public LiveData<List<Word>> all() { return dao.getAll(); }

    public void add(long groupId, String front, String back) {
        io.execute(() -> dao.insert(new Word(groupId, front, back)));
    }
    public void rename(long id, String front, String back) {
        io.execute(() -> dao.rename(id, front, back));
    }

    public void delete(Word w) { io.execute(() -> dao.delete(w)); }

    public void clearForGroup(long groupId) { io.execute(() -> dao.clearForGroup(groupId)); }
}
