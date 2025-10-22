package com.example.wordsearchquest;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoundWordsAdapter extends RecyclerView.Adapter<FoundWordsAdapter.FoundWordViewHolder> {

    private List<String> foundWords;
    private int[] colors = {
        0xCC00D4FF, // Blue
        0xCC39FF14, // Green  
        0xCCFF073A, // Red
        0xCCFFFF00, // Yellow
        0xCC8B00FF, // Purple
        0xCCFF6B35  // Orange
    };

    public FoundWordsAdapter(List<String> foundWords) {
        this.foundWords = foundWords;
    }

    @NonNull
    @Override
    public FoundWordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_found_word, parent, false);
        return new FoundWordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoundWordViewHolder holder, int position) {
        String word = foundWords.get(position);
        holder.tvFoundWord.setText(word);
        
        // Set different color for each word
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(50f);
        background.setColor(colors[position % colors.length]);
        holder.itemView.setBackground(background);
    }

    @Override
    public int getItemCount() {
        return foundWords.size();
    }

    public void updateFoundWords(List<String> foundWords) {
        this.foundWords = foundWords;
        notifyDataSetChanged();
    }

    static class FoundWordViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoundWord;

        FoundWordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoundWord = itemView.findViewById(R.id.tvFoundWord);
        }
    }
}