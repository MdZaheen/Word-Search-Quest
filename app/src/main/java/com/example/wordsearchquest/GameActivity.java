package com.example.wordsearchquest;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private TextView tvLevelTitle, tvScore, tvTime, tvWordsFound;
    private ImageButton btnBack, btnPause;
    private GridLayout gridLetters;
    private RecyclerView recyclerWordList;
    private View layoutPauseOverlay;
    
    private LevelManager levelManager;
    private LevelManager.LevelData levelData;
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
        gridLetters = findViewById(R.id.gridLetters);
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
        
        // Initialize score and time display
        updateScoreDisplay();
        updateTimeDisplay();
        updateWordsFoundDisplay();
        
        // TODO: Generate and display word grid
        setupWordGrid();
        
        // TODO: Setup word list
        setupWordList();
    }

    private void setupWordGrid() {
        // Clear existing grid
        gridLetters.removeAllViews();
        
        // Set grid dimensions
        gridLetters.setColumnCount(levelData.gridSize);
        gridLetters.setRowCount(levelData.gridSize);
        
        // TODO: Generate actual word grid with word placement
        // For now, create a simple grid with placeholder letters
        for (int row = 0; row < levelData.gridSize; row++) {
            for (int col = 0; col < levelData.gridSize; col++) {
                TextView letterView = createLetterView((char)('A' + (row * levelData.gridSize + col) % 26));
                gridLetters.addView(letterView);
            }
        }
    }

    private TextView createLetterView(char letter) {
        TextView textView = new TextView(this);
        
        // Set layout parameters
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        params.setMargins(2, 2, 2, 2);
        textView.setLayoutParams(params);
        
        // Set appearance
        textView.setText(String.valueOf(letter));
        textView.setTextSize(18);
        textView.setBackgroundColor(getColor(R.color.grid_background));
        textView.setTextColor(getColor(R.color.letter_text));
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setPadding(4, 4, 4, 4);
        
        // TODO: Add touch listeners for word selection
        
        return textView;
    }

    private void setupWordList() {
        // TODO: Create and set adapter for word list RecyclerView
        updateWordsFoundDisplay();
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
        // TODO: Update with actual found words count
        tvWordsFound.setText(String.format(Locale.getDefault(), "0/%d", levelData.words.size()));
    }

    private void addScore(int points) {
        currentScore += points;
        updateScoreDisplay();
    }

    private void checkLevelComplete() {
        // TODO: Check if all words are found
        // For now, just show a toast
        Toast.makeText(this, "Word found! +10 points", Toast.LENGTH_SHORT).show();
        addScore(10);
        
        // Check if level is complete (placeholder logic)
        // gameComplete(true);
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