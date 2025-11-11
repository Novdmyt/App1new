package com.example.easylearnlanguage.ui.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Group;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.VH> {

    public interface OnClick { void onClick(Group g); }
    private final OnClick cb;
    private final List<Group> items = new ArrayList<>();

    public GroupAdapter(OnClick cb){ this.cb = cb; }

    public void submit(List<Group> data){
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_group, p, false);
        return new VH(view);
    }
    public Group getItem(int position) {           // ДОДАНО
        return (position >= 0 && position < items.size()) ? items.get(position) : null;
    }

    public void removeAt(int position) {           // ДОДАНО (локальне прибрання для плавності)
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void restoreAt(int position, Group g) { // ДОДАНО (для Undo)
        if (position < 0 || position > items.size()) position = items.size();
        items.add(position, g);
        notifyItemInserted(position);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Group g = items.get(pos);
        h.title.setText(g.title);

        // якщо колір заданий (не 0), фарбуємо фон картки
        if (g.color != 0) {
            h.card.setCardBackgroundColor(g.color);
        } else {
            // повернути дефолтний (на випадок reuse ViewHolder)
            h.card.setCardBackgroundColor(
                    h.card.getContext().getColor(android.R.color.transparent));
        }

        h.itemView.setOnClickListener(v -> cb.onClick(g));
    }

    @Override public int getItemCount(){ return items.size(); }

    static class VH extends RecyclerView.ViewHolder{
        MaterialCardView card;
        TextView title;
        VH(@NonNull View itemView){
            super(itemView);
            card  = (MaterialCardView) itemView;
            title = itemView.findViewById(R.id.tvTitle);
        }
    }
}
