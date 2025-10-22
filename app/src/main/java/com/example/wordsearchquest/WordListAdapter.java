package com.example.wordsearchquest;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordViewHolder> {

    private List<String> words;
    private List<String> foundWords;

    public WordListAdapter(List<String> words, List<String> foundWords) {
        this.words = words;
        this.foundWords = foundWords;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        String word = words.get(position);
        holder.tvWord.setText(word);
        
        // Change appearance based on whether word is found
        if (foundWords.contains(word)) {
            holder.tvWord.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.found_word_text));
            holder.itemView.setSelected(true);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.found_word));
        } else {
            holder.tvWord.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.word_list_text));
            holder.itemView.setSelected(false);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.background_dark));
        }
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void updateFoundWords(List<String> foundWords) {
        this.foundWords = foundWords;
        notifyDataSetChanged();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
        }
    }
}