package com.example.easylearnlanguage.ui.play;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;

import java.util.ArrayList;
import java.util.List;

public class ChoiceAdapter extends RecyclerView.Adapter<ChoiceAdapter.VH> {

    public interface OnChoiceClick {
        void onClick(int position, String text);
    }

    private final List<String> items = new ArrayList<>();
    private final OnChoiceClick onClick;
    private int correctIndex = -1;

    public ChoiceAdapter(OnChoiceClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<String> choices) {
        items.clear();
        if (choices != null) items.addAll(choices);
        notifyDataSetChanged();
    }

    public void setCorrectIndex(int idx) {
        this.correctIndex = idx;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choice, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        String text = items.get(pos);
        h.btn.setText(text);
        h.btn.setOnClickListener(v -> onClick.onClick(h.getAdapterPosition(), text));
        // дизайна не касаемся: без подсветок/цветов ответа
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        Button btn;
        VH(@NonNull View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.btn_choice);
        }
    }
}
