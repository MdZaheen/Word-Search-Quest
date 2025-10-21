package com.example.wordsearchquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private static final String PREFS_NAME = "WordSearchPrefs";
    private static final String KEY_CURRENT_LEVEL = "current_level";
    private static final String KEY_LEVEL_COMPLETED = "level_completed_";
    private static final String KEY_LEVEL_SCORE = "level_score_";
    private static final String KEY_TOTAL_SCORE = "total_score";
    
    private Context context;
    private SharedPreferences prefs;
    
    public LevelManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Initialize first level as unlocked if no progress exists
        if (getCurrentUnlockedLevel() == 0) {
            unlockLevel(1);
        }
    }
    
    public int getCurrentUnlockedLevel() {
        return prefs.getInt(KEY_CURRENT_LEVEL, 1);
    }
    
    public void unlockLevel(int level) {
        SharedPreferences.Editor editor = prefs.edit();
        int currentUnlocked = getCurrentUnlockedLevel();
        if (level > currentUnlocked) {
            editor.putInt(KEY_CURRENT_LEVEL, level);
        }
        editor.apply();
    }
    
    public boolean isLevelCompleted(int level) {
        return prefs.getBoolean(KEY_LEVEL_COMPLETED + level, false);
    }
    
    public void markLevelCompleted(int level, int score) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_LEVEL_COMPLETED + level, true);
        
        // Save score if it's better than previous
        int previousScore = getLevelScore(level);
        if (score > previousScore) {
            editor.putInt(KEY_LEVEL_SCORE + level, score);
        }
        
        // Unlock next level
        if (level < 20) {
            unlockLevel(level + 1);
        }
        
        // Update total score
        updateTotalScore();
        
        editor.apply();
    }
    
    public int getLevelScore(int level) {
        return prefs.getInt(KEY_LEVEL_SCORE + level, 0);
    }
    
    public int getTotalScore() {
        return prefs.getInt(KEY_TOTAL_SCORE, 0);
    }
    
    private void updateTotalScore() {
        int totalScore = 0;
        for (int i = 1; i <= 20; i++) {
            totalScore += getLevelScore(i);
        }
        prefs.edit().putInt(KEY_TOTAL_SCORE, totalScore).apply();
    }
    
    public LevelData getLevelData(int level) {
        try {
            String fileName = getFileNameForLevel(level);
            String jsonString = loadJSONFromAsset(fileName);
            
            if (jsonString != null) {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray levelsArray = jsonObject.getJSONArray("levels");
                
                // Find the specific level
                for (int i = 0; i < levelsArray.length(); i++) {
                    JSONObject levelObj = levelsArray.getJSONObject(i);
                    if (levelObj.getInt("level") == level) {
                        return parseLevelData(levelObj);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("LevelManager", "Error parsing level data: " + e.getMessage());
        }
        
        // Return default data if parsing fails
        return getDefaultLevelData(level);
    }
    
    private String getFileNameForLevel(int level) {
        if (level <= 5) return "words_easy.json";
        else if (level <= 10) return "words_medium.json";
        else if (level <= 15) return "words_hard.json";
        else return "words_expert.json";
    }
    
    private String loadJSONFromAsset(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            
            bufferedReader.close();
            inputStream.close();
            
        } catch (IOException e) {
            Log.e("LevelManager", "Error loading JSON file: " + e.getMessage());
            return null;
        }
        
        return stringBuilder.toString();
    }
    
    private LevelData parseLevelData(JSONObject levelObj) throws JSONException {
        int level = levelObj.getInt("level");
        String difficulty = levelObj.getString("difficulty");
        int gridSize = levelObj.getInt("gridSize");
        int timeLimit = levelObj.getInt("timeLimit");
        
        JSONArray wordsArray = levelObj.getJSONArray("words");
        List<String> words = new ArrayList<>();
        
        for (int i = 0; i < wordsArray.length(); i++) {
            words.add(wordsArray.getString(i));
        }
        
        return new LevelData(level, difficulty, gridSize, timeLimit, words);
    }
    
    private LevelData getDefaultLevelData(int level) {
        List<String> defaultWords = new ArrayList<>();
        defaultWords.add("DEFAULT");
        defaultWords.add("WORD");
        defaultWords.add("LIST");
        
        return new LevelData(level, "Easy", 6, 300, defaultWords);
    }
    
    public void resetProgress() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        // Unlock first level again
        unlockLevel(1);
    }
    
    public static class LevelData {
        public final int level;
        public final String difficulty;
        public final int gridSize;
        public final int timeLimit;
        public final List<String> words;
        
        public LevelData(int level, String difficulty, int gridSize, int timeLimit, List<String> words) {
            this.level = level;
            this.difficulty = difficulty;
            this.gridSize = gridSize;
            this.timeLimit = timeLimit;
            this.words = words;
        }
    }
}