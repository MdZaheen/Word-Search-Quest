package com.example.wordsearchquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnStartGame, btnLevelSelect, btnHowToPlay, btnExit;
    private LevelManager levelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initLevelManager();
        setupClickListeners();
    }

    private void initViews() {
        btnStartGame = findViewById(R.id.btnStartGame);
        btnLevelSelect = findViewById(R.id.btnLevelSelect);
        btnHowToPlay = findViewById(R.id.btnHowToPlay);
        btnExit = findViewById(R.id.btnExit);
    }

    private void initLevelManager() {
        levelManager = new LevelManager(this);
    }

    private void setupClickListeners() {
        btnStartGame.setOnClickListener(v -> {
            // Start from the first unlocked level
            int currentLevel = levelManager.getCurrentUnlockedLevel();
            startGameActivity(currentLevel);
        });

        btnLevelSelect.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LevelSelectActivity.class);
            startActivity(intent);
        });

        btnHowToPlay.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HowToPlayActivity.class);
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });
    }

    private void startGameActivity(int level) {
        if (level > 20) {
            Toast.makeText(this, R.string.no_more_levels, Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("level", level);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh any UI that might have changed
    }
}
