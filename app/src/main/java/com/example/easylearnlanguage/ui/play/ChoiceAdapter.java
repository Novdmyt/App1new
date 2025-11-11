package com.example.easylearnlanguage.ui.play;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

class ChoiceAdapter extends RecyclerView.Adapter<ChoiceAdapter.Holder> {

    interface OnChoice {
        void onClick(int position, String text);
    }

    private final OnChoice cb;
    private final List<String> items = new ArrayList<>();

    private int correctIndex = -1;
    private int chosenIndex  = -1;
    private boolean locked   = false;

    ChoiceAdapter(OnChoice cb){ this.cb = cb; }

    void submit(List<String> data){
        items.clear();
        if (data != null) items.addAll(data);
        // новий раунд — скидаємо стан
        correctIndex = -1;
        chosenIndex  = -1;
        locked       = false;
        notifyDataSetChanged();
    }

    void setCorrectIndex(int idx){ this.correctIndex = idx; }

    void reveal(int chosen, int correct){
        this.chosenIndex  = chosen;
        this.correctIndex = correct;
        this.locked       = true;
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choice, parent, false);
        return new Holder(v);
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
        String text = items.get(pos);
        h.btn.setText(text);

        // дефолтний стан
        h.btn.setEnabled(!locked);
        h.btn.setBackgroundTintList(null); // ок — null дозволено
        // ВАЖЛИВО: не викликати setTextColor(null) — це NPE

        if (locked && correctIndex >= 0) {
            if (pos == correctIndex) {
                h.btn.setBackgroundTintList(
                        ContextCompat.getColorStateList(h.btn.getContext(), R.color.teal_700));
            } else if (pos == chosenIndex) {
                h.btn.setBackgroundTintList(
                        ContextCompat.getColorStateList(h.btn.getContext(), R.color.red));
            } else {
                h.btn.setEnabled(false);
            }
        }

        h.btn.setOnClickListener(v -> {
            if (locked) return;
            if (cb != null) cb.onClick(h.getBindingAdapterPosition(), text);
        });
    }

    @Override public int getItemCount(){ return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final MaterialButton btn;
        Holder(@NonNull View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.btn_choice);
        }
    }
}
