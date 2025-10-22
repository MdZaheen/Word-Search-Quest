package com.example.wordsearchquest;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private TextView tvLevelTitle, tvScore, tvTime, tvWordsFound;
    private ImageButton btnBack, btnPause;
    private WordGridView wordGridView;
    private RecyclerView recyclerWordList;
    private View layoutPauseOverlay;
    
    private LevelManager levelManager;
    private LevelManager.LevelData levelData;
    private WordGridGenerator.GridResult gridResult;
    private WordListAdapter wordListAdapter;
    private List<String> foundWords;
    
    private int currentLevel;
    private int currentScore = 0;
    private CountDownTimer gameTimer;
    private int timeRemaining;
    private boolean isPaused = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        getCurrentLevel();
        initViews();
        initLevelManager();
        loadLevelData();
        setupGame();
        setupClickListeners();
        startTimer();
    }

    private void getCurrentLevel() {
        currentLevel = getIntent().getIntExtra("level", 1);
    }

    private void initViews() {
        tvLevelTitle = findViewById(R.id.tvLevelTitle);
        tvScore = findViewById(R.id.tvScore);
        tvTime = findViewById(R.id.tvTime);
        tvWordsFound = findViewById(R.id.tvWordsFound);
        btnBack = findViewById(R.id.btnBack);
        btnPause = findViewById(R.id.btnPause);
        wordGridView = findViewById(R.id.wordGridView);
        recyclerWordList = findViewById(R.id.recyclerWordList);
        layoutPauseOverlay = findViewById(R.id.layoutPauseOverlay);
    }

    private void initLevelManager() {
        levelManager = new LevelManager(this);
    }

    private void loadLevelData() {
        levelData = levelManager.getLevelData(currentLevel);
        timeRemaining = levelData.timeLimit;
    }

    private void setupGame() {
        // Set level title
        tvLevelTitle.setText("Level " + currentLevel);
        
        // Initialize found words list
        foundWords = new ArrayList<>();
        
        // Initialize score and time display
        updateScoreDisplay();
        updateTimeDisplay();
        updateWordsFoundDisplay();
        
        // Generate and display word grid
        setupWordGrid();
        
        // Setup word list
        setupWordList();
        
        // Setup grid selection listener
        setupWordGridListener();
    }

    private void setupWordGrid() {
        // Generate word grid with actual words
        gridResult = WordGridGenerator.generateGrid(levelData.words, levelData.gridSize);
        
        // Set grid data to the custom view
        wordGridView.setGridData(gridResult.grid, gridResult.wordPlacements);
    }

    private void setupWordList() {
        // Create adapter for word list
        wordListAdapter = new WordListAdapter(levelData.words, foundWords);
        
        // Setup RecyclerView with GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
        recyclerWordList.setLayoutManager(layoutManager);
        recyclerWordList.setAdapter(wordListAdapter);
        
        updateWordsFoundDisplay();
    }
    
    private void setupWordGridListener() {
        wordGridView.setOnWordSelectedListener(new WordGridView.OnWordSelectedListener() {
            @Override
            public void onWordSelected(String word) {
                onWordFound(word);
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            pauseTimer();
            finish();
        });

        btnPause.setOnClickListener(v -> {
            if (isPaused) {
                resumeGame();
            } else {
                pauseGame();
            }
        });

        // Pause overlay buttons
        findViewById(R.id.btnResume).setOnClickListener(v -> resumeGame());
        findViewById(R.id.btnRestart).setOnClickListener(v -> restartLevel());
        findViewById(R.id.btnQuitGame).setOnClickListener(v -> {
            pauseTimer();
            finish();
        });
    }

    private void startTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        gameTimer = new CountDownTimer(timeRemaining * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = (int) (millisUntilFinished / 1000);
                updateTimeDisplay();
            }

            @Override
            public void onFinish() {
                gameOver(false);
            }
        };
        
        gameTimer.start();
    }

    private void pauseTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
    }

    private void pauseGame() {
        isPaused = true;
        pauseTimer();
        layoutPauseOverlay.setVisibility(View.VISIBLE);
    }

    private void resumeGame() {
        isPaused = false;
        layoutPauseOverlay.setVisibility(View.GONE);
        startTimer();
    }

    private void restartLevel() {
        currentScore = 0;
        timeRemaining = levelData.timeLimit;
        isPaused = false;
        layoutPauseOverlay.setVisibility(View.GONE);
        
        setupGame();
        startTimer();
    }

    private void updateScoreDisplay() {
        tvScore.setText(String.format(Locale.getDefault(), "%,d", currentScore));
    }

    private void updateTimeDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void updateWordsFoundDisplay() {
        tvWordsFound.setText(String.format(Locale.getDefault(), "%d/%d", foundWords.size(), levelData.words.size()));
    }

    private void addScore(int points) {
        currentScore += points;
        updateScoreDisplay();
    }

    private void onWordFound(String word) {
        // Add word to found words list if not already found
        if (!foundWords.contains(word.toUpperCase())) {
            foundWords.add(word.toUpperCase());
            
            // Add score for found word
            addScore(10);
            
            // Update displays
            updateWordsFoundDisplay();
            wordListAdapter.updateFoundWords(foundWords);
            
            // Show feedback
            Toast.makeText(this, "Word found: " + word + " (+10 points)", Toast.LENGTH_SHORT).show();
            
            // Check if level is complete
            if (foundWords.size() == levelData.words.size()) {
                gameComplete(true);
            }
        }
    }

    private void gameComplete(boolean isWon) {
        pauseTimer();
        
        if (isWon) {
            // Calculate bonus score for remaining time
            int timeBonus = timeRemaining * 5;
            addScore(timeBonus);
            
            // Mark level as completed
            levelManager.markLevelCompleted(currentLevel, currentScore);
        }
        
        // Start result activity
        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
        intent.putExtra("level", currentLevel);
        intent.putExtra("score", currentScore);
        intent.putExtra("timeRemaining", timeRemaining);
        intent.putExtra("isWon", isWon);
        intent.putExtra("wordsFound", 0); // TODO: Pass actual words found count
        intent.putExtra("totalWords", levelData.words.size());
        startActivity(intent);
        finish();
    }

    private void gameOver(boolean isWon) {
        gameComplete(isWon);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPaused && gameTimer != null) {
            pauseGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) {
            gameTimer.cancel();
        }
    }
}