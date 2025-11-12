package com.example.easylearnlanguage.ui.word;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Word;
import java.util.ArrayList;
import java.util.List;

class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.VH> {

    interface OnSpeak { void onSpeak(Word w, View anchor); }
    private final OnSpeak onSpeak;
    private final List<Word> data = new ArrayList<>();

    DictionaryAdapter(OnSpeak cb){ onSpeak = cb; }

    void submit(List<Word> items){ data.clear(); if(items!=null) data.addAll(items); notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dictionary, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Word w = data.get(pos);
        h.tvFront.setText(w.front);
        h.tvBack.setText(w.back);
        h.btnSpeak.setOnClickListener(v -> { if(onSpeak != null) onSpeak.onSpeak(w, v); });
    }

    @Override public int getItemCount(){ return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvFront, tvBack; ImageButton btnSpeak;
        VH(View v){ super(v); tvFront = v.findViewById(R.id.tvFront); tvBack = v.findViewById(R.id.tvBack); btnSpeak = v.findViewById(R.id.btnSpeak); }
    }
}
