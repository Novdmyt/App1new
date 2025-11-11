package com.example.easylearnlanguage.ui.word;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Word;
import java.util.ArrayList;
import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.VH> {
    private final List<Word> items = new ArrayList<>();

    public void submit(List<Word> data){
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_word, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Word w = items.get(pos);
        h.front.setText(w.front);
        h.back.setText(w.back);
    }

    @Override public int getItemCount(){ return items.size(); }

    static class VH extends RecyclerView.ViewHolder{
        TextView front, back;
        VH(@NonNull View itemView){
            super(itemView);
            front = itemView.findViewById(R.id.tvFront);
            back  = itemView.findViewById(R.id.tvBack);
        }
    }
}
