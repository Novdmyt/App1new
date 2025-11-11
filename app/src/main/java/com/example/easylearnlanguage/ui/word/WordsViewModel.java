package com.example.easylearnlanguage.ui.word;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.easylearnlanguage.data.Word;
import com.example.easylearnlanguage.data.WordRepository;

import java.util.List;

public class WordsViewModel extends AndroidViewModel {
    private final WordRepository repo;

    public WordsViewModel(@NonNull Application app) {
        super(app);
        repo = new WordRepository(app);
    }

    public LiveData<List<Word>> wordsByGroup(long groupId) { return repo.wordsByGroup(groupId); }
    public void add(long groupId, String front, String back){ repo.add(groupId, front, back); }
    public void delete(Word w){ repo.delete(w); }
    public void clear(long groupId){ repo.clearForGroup(groupId); }
}
