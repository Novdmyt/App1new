package com.example.easylearnlanguage.ui.group;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.easylearnlanguage.data.Group;
import com.example.easylearnlanguage.data.GroupRepository;

import java.util.List;

public class GroupViewModel extends AndroidViewModel {

    private final GroupRepository repo;

    public GroupViewModel(@NonNull Application app) {
        super(app);
        repo = new GroupRepository(app);
    }

    public LiveData<List<Group>> groups() {
        return repo.observeAll();
    }

    public void add(String title, String from, String to, int color) {
        repo.add(title, from, to, color);
    }

    /** Видаляє групу разом з усіма її словами */
    public void deleteCascade(Group g) {
        repo.deleteCascade(g);
    }
}
