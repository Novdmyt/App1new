package com.example.easylearnlanguage.ui.word;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.example.easylearnlanguage.data.*;
import java.util.List;

public class WordsViewModel extends AndroidViewModel {
    private final WordRepository repo;
    public WordsViewModel(@NonNull Application app) { super(app); repo = new WordRepository(app); }
    public LiveData<List<Word>> wordsByGroup(long gid) { return repo.wordsByGroup(gid); }
    public LiveData<List<Word>> allWords() { return repo.all(); }
    public void add(long gid, String front, String back){ repo.add(gid, front, back); }
}
