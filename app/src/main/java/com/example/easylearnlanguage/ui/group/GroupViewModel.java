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

    public GroupViewModel(@NonNull Application app){
        super(app);
        repo = new GroupRepository(app);
    }

    public LiveData<List<Group>> groups(){ return repo.observeAll(); }

    public void add(String title, String from, String to, int color){ repo.add(title, from, to, color); }

    public void delete(Group g){ repo.delete(g); }

    public void deleteCascade(Group g){ repo.deleteCascade(g); }

    // NEW
    public void rename(long id, String title){ repo.rename(id, title); }
}