package com.example.wordsearchquest;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private TextView tvLevelTitle, tvScore, tvTime, tvWordsFound;
    private ImageButton btnBack, btnPause;
    private Button btnHint;
    private WordGridView wordGridView;
    private RecyclerView recyclerWordList, recyclerFoundWords;
    private View layoutPauseOverlay;
    
    private LevelManager levelManager;
    private LevelManager.LevelData levelData;
    private WordGridGenerator.GridResult gridResult;
    private WordListAdapter wordListAdapter;
    private FoundWordsAdapter foundWordsAdapter;
    private List<String> foundWords;
    
    private int currentLevel;
    private int currentScore = 0;
    private CountDownTimer gameTimer;
    private int timeRemaining;
    private boolean isPaused = false;
    private int hintsRemaining = 2;
    
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
        btnHint = findViewById(R.id.btnHint);
        wordGridView = findViewById(R.id.wordGridView);
        recyclerWordList = findViewById(R.id.recyclerWordList);
        recyclerFoundWords = findViewById(R.id.recyclerFoundWords);
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
        
        // Setup found words display
        setupFoundWordsDisplay();
        
        // Setup grid selection listener
        setupWordGridListener();
        
        // Initialize hint button
        updateHintButton();
        
        // Add subtle animation to stats bar
        Animation statsAnimation = AnimationUtils.loadAnimation(this, R.anim.stats_pulse);
        findViewById(R.id.layoutGameInfo).startAnimation(statsAnimation);
    }

    private void setupWordGrid() {
        // Generate word grid with actual words
        gridResult = WordGridGenerator.generateGrid(levelData.words, levelData.gridSize);
        
        // Set grid data to the custom view
        wordGridView.setGridData(gridResult.grid, gridResult.wordPlacements);
        
        // Add entrance animation
        Animation gridAnimation = AnimationUtils.loadAnimation(this, R.anim.grid_fade_in);
        wordGridView.startAnimation(gridAnimation);
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
    
    private void setupFoundWordsDisplay() {
        // Create adapter for found words
        foundWordsAdapter = new FoundWordsAdapter(foundWords);
        
        // Setup RecyclerView with LinearLayoutManager (horizontal)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerFoundWords.setLayoutManager(layoutManager);
        recyclerFoundWords.setAdapter(foundWordsAdapter);
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
        
        btnHint.setOnClickListener(v -> {
            useHint();
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
        hintsRemaining = 2;
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
            foundWordsAdapter.updateFoundWords(foundWords);
            
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
        intent.putExtra("wordsFound", foundWords.size());
        intent.putExtra("totalWords", levelData.words.size());
        startActivity(intent);
        finish();
    }

    private void gameOver(boolean isWon) {
        gameComplete(isWon);
    }
    
    private void useHint() {
        if (hintsRemaining <= 0) {
            Toast.makeText(this, "No hints remaining!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Find a random word that hasn't been found yet
        List<String> unfoundWords = new ArrayList<>();
        for (String word : levelData.words) {
            if (!foundWords.contains(word.toUpperCase())) {
                unfoundWords.add(word);
            }
        }
        
        if (unfoundWords.isEmpty()) {
            Toast.makeText(this, "All words already found!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Reveal a random unfound word with visual selection
        String hintWord = unfoundWords.get((int) (Math.random() * unfoundWords.size()));
        
        // Use auto-selection on grid
        wordGridView.highlightWordOnGrid(hintWord);
        
        hintsRemaining--;
        updateHintButton();
        
        Toast.makeText(this, "Hint used! Watch the grid: " + hintWord, Toast.LENGTH_LONG).show();
    }
    
    private void updateHintButton() {
        btnHint.setText(String.format("Hint (%d)", hintsRemaining));
        btnHint.setEnabled(hintsRemaining > 0);
        
        if (hintsRemaining <= 0) {
            btnHint.setAlpha(0.5f);
        } else {
            btnHint.setAlpha(1.0f);
        }
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