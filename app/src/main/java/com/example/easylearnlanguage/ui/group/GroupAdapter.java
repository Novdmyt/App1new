package com.example.easylearnlanguage.ui.group;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Group;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.VH> {

    public interface OnItemClick { void onClick(Group g); }
    public interface OnItemLongClick { void onLongClick(View anchor, Group g, int position); }

    private final List<Group> data = new ArrayList<>();
    private final OnItemClick onClick;
    private OnItemLongClick onLongClick;

    public GroupAdapter(OnItemClick click){ this.onClick = click; }
    public void setOnLongClick(OnItemLongClick cb){ this.onLongClick = cb; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Group g = data.get(position);
        h.title.setText(g.title);

        // Колір
        final Context ctx = h.itemView.getContext();
        final int outline = resolveAttrColor(ctx, com.google.android.material.R.attr.colorOutline);

        if (g.color == 0) {
            // Без кольору: прозорий фон, сірий обвід, кружок — outline
            h.card.setCardBackgroundColor(ColorStateList.valueOf(0x00FFFFFF));
            h.card.setStrokeColor(outline);
            h.colorDot.setBackgroundTintList(ColorStateList.valueOf(outline));
        } else {
            // З кольором: мʼякий фон, обвід/кружок у колір групи
            int bg = withAlpha(g.color, 0.12f);
            h.card.setCardBackgroundColor(ColorStateList.valueOf(bg));
            h.card.setStrokeColor(g.color);
            h.colorDot.setBackgroundTintList(ColorStateList.valueOf(g.color));
        }

        h.itemView.setOnClickListener(v -> { if (onClick != null) onClick.onClick(g); });
        h.itemView.setOnLongClickListener(v -> {
            if (onLongClick != null) onLongClick.onLongClick(v, g, h.getBindingAdapterPosition());
            return true;
        });
    }

    @Override public int getItemCount() { return data.size(); }

    // -------- публічні методи --------
    public void submit(List<Group> items){
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public Group getItem(int pos){
        if (pos < 0 || pos >= data.size()) return null;
        return data.get(pos);
    }

    public void removeAt(int pos){
        if (pos < 0 || pos >= data.size()) return;
        data.remove(pos);
        notifyItemRemoved(pos);
    }

    // -------- ViewHolder --------
    static class VH extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final View colorDot;
        final TextView title;
        VH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            colorDot = itemView.findViewById(R.id.colorDot);
            title = itemView.findViewById(R.id.tvTitle);
        }
    }

    // -------- helpers --------
    private static int withAlpha(int color, float alpha){
        int a = Math.round(255 * alpha);
        return (color & 0x00FFFFFF) | (a << 24);
    }
    private static int resolveAttrColor(Context ctx, int attr){
        TypedValue tv = new TypedValue();
        ctx.getTheme().resolveAttribute(attr, tv, true);
        return tv.data;
    }
}
