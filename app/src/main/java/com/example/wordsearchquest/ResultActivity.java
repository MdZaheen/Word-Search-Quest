package com.example.wordsearchquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private ImageView ivResultIcon;
    private TextView tvResultTitle, tvResultMessage, tvFinalScore, tvTimeTaken, tvWordsFoundCount;
    private Button btnNextLevel, btnRetryLevel, btnBackToLevels;
    
    private int currentLevel;
    private int finalScore;
    private int timeRemaining;
    private boolean isWon;
    private int wordsFound;
    private int totalWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        
        getResultData();
        initViews();
        setupResultDisplay();
        setupClickListeners();
    }

    private void getResultData() {
        Intent intent = getIntent();
        currentLevel = intent.getIntExtra("level", 1);
        finalScore = intent.getIntExtra("score", 0);
        timeRemaining = intent.getIntExtra("timeRemaining", 0);
        isWon = intent.getBooleanExtra("isWon", false);
        wordsFound = intent.getIntExtra("wordsFound", 0);
        totalWords = intent.getIntExtra("totalWords", 0);
    }

    private void initViews() {
        ivResultIcon = findViewById(R.id.ivResultIcon);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvTimeTaken = findViewById(R.id.tvTimeTaken);
        tvWordsFoundCount = findViewById(R.id.tvWordsFoundCount);
        btnNextLevel = findViewById(R.id.btnNextLevel);
        btnRetryLevel = findViewById(R.id.btnRetryLevel);
        btnBackToLevels = findViewById(R.id.btnBackToLevels);
    }

    private void setupResultDisplay() {
        if (isWon) {
            // Level completed successfully
            tvResultTitle.setText(R.string.level_complete);
            tvResultMessage.setText(R.string.congratulations);
            ivResultIcon.setImageResource(R.drawable.ic_trophy);
            ivResultIcon.setColorFilter(getColor(R.color.accent));
            
            // Show next level button only if not the last level
            if (currentLevel < 20) {
                btnNextLevel.setVisibility(android.view.View.VISIBLE);
            } else {
                btnNextLevel.setVisibility(android.view.View.GONE);
                tvResultMessage.setText("Congratulations! You've completed all levels!");
            }
            
        } else {
            // Level failed
            tvResultTitle.setText(R.string.game_over);
            tvResultMessage.setText("Time's up! Try again to complete the level.");
            ivResultIcon.setColorFilter(getColor(R.color.error));
            
            // Hide next level button
            btnNextLevel.setVisibility(android.view.View.GONE);
        }

        // Set score
        tvFinalScore.setText(String.format(Locale.getDefault(), "%,d", finalScore));
        
        // Set time taken (original time limit - remaining time)
        LevelManager levelManager = new LevelManager(this);
        LevelManager.LevelData levelData = levelManager.getLevelData(currentLevel);
        int timeTaken = levelData.timeLimit - timeRemaining;
        int minutes = timeTaken / 60;
        int seconds = timeTaken % 60;
        tvTimeTaken.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        
        // Set words found count
        tvWordsFoundCount.setText(String.format(Locale.getDefault(), "%d/%d", wordsFound, totalWords));
    }

    private void setupClickListeners() {
        btnNextLevel.setOnClickListener(v -> {
            if (currentLevel < 20) {
                Intent intent = new Intent(ResultActivity.this, GameActivity.class);
                intent.putExtra("level", currentLevel + 1);
                startActivity(intent);
                finish();
            }
        });

        btnRetryLevel.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, GameActivity.class);
            intent.putExtra("level", currentLevel);
            startActivity(intent);
            finish();
        });

        btnBackToLevels.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, LevelSelectActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Go back to level select instead of game
        Intent intent = new Intent(ResultActivity.this, LevelSelectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}