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
        
        boolean wasFound = holder.itemView.isSelected();
        boolean isFound = foundWords.contains(word);
        
        // Change appearance based on whether word is found
        if (isFound) {
            holder.itemView.setSelected(true);
            holder.tvWord.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            // Fade found words to 50% opacity
            holder.itemView.setAlpha(0.5f);
            
            // Animate if newly found
            if (!wasFound) {
                animateFoundWord(holder.itemView);
            }
        } else {
            holder.itemView.setSelected(false);
            holder.tvWord.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            // Full opacity for unfound words
            holder.itemView.setAlpha(1.0f);
        }
        
        // Add subtle fade-in animation for items
        holder.itemView.setAlpha(0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(holder.itemView, "alpha", 0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.setStartDelay(position * 50); // Stagger animations
        fadeIn.start();
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void updateFoundWords(List<String> foundWords) {
        this.foundWords = foundWords;
        notifyDataSetChanged();
    }
    
    private void animateFoundWord(View view) {
        // Scale and fade animation
        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f);
        
        scaleUpX.setDuration(200);
        scaleUpY.setDuration(200);
        scaleDownX.setDuration(200);
        scaleDownY.setDuration(200);
        
        scaleDownX.setStartDelay(200);
        scaleDownY.setStartDelay(200);
        
        animatorSet.playTogether(scaleUpX, scaleUpY);
        animatorSet.play(scaleDownX).after(scaleUpX);
        animatorSet.play(scaleDownY).after(scaleUpY);
        
        animatorSet.start();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
        }
    }
}