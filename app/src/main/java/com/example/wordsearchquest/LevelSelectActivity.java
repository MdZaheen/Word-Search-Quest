package com.example.wordsearchquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LevelSelectActivity extends AppCompatActivity {

    private GridLayout gridLevels;
    private ImageButton btnBack;
    private LevelManager levelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);
        
        initViews();
        initLevelManager();
        setupLevelButtons();
        setupClickListeners();
    }

    private void initViews() {
        gridLevels = findViewById(R.id.gridLevels);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initLevelManager() {
        levelManager = new LevelManager(this);
    }

    private void setupLevelButtons() {
        int currentUnlocked = levelManager.getCurrentUnlockedLevel();
        
        // Clear existing buttons
        gridLevels.removeAllViews();
        
        // Create 20 level buttons
        for (int level = 1; level <= 20; level++) {
            Button levelButton = createLevelButton(level, currentUnlocked);
            gridLevels.addView(levelButton);
        }
    }

    private Button createLevelButton(int level, int currentUnlocked) {
        Button button = new Button(this);
        
        // Set button properties
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 120; // Fixed height for consistency
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        params.setMargins(12, 12, 12, 12);
        button.setLayoutParams(params);
        
        // Set text and styling based on level status
        button.setText(String.valueOf(level));
        button.setTextSize(18); // Larger text for better visibility
        button.setPadding(16, 24, 16, 24);
        button.setMinHeight(120); // Ensure minimum height
        
        if (level <= currentUnlocked) {
            // Level is unlocked
            if (levelManager.isLevelCompleted(level)) {
                // Level is completed
                button.setBackgroundResource(R.drawable.bg_level_item);
                button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.level_completed));
                button.setTextColor(ContextCompat.getColor(this, R.color.text_white));
            } else {
                // Level is available but not completed
                button.setBackgroundResource(R.drawable.bg_level_item);
                button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.level_unlocked));
                button.setTextColor(ContextCompat.getColor(this, R.color.text_white));
            }
            
            // Set click listener for unlocked levels
            button.setOnClickListener(v -> startLevel(level));
            button.setEnabled(true);
            
        } else {
            // Level is locked
            button.setBackgroundResource(R.drawable.bg_level_item);
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.level_locked));
            button.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            button.setEnabled(false);
            button.setText("🔒");
        }
        
        return button;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void startLevel(int level) {
        Intent intent = new Intent(LevelSelectActivity.this, GameActivity.class);
        intent.putExtra("level", level);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh level buttons in case progress changed
        setupLevelButtons();
    }
}