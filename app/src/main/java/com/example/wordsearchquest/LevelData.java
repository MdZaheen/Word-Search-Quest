package com.example.wordsearchquest;

import java.util.Arrays;
import java.util.List;

public class LevelData {
    public int levelNumber;
    public String category;
    public char[][] grid;
    public List<String> words;
    
    public LevelData(int levelNumber, String category, char[][] grid, List<String> words) {
        this.levelNumber = levelNumber;
        this.category = category;
        this.grid = grid;
        this.words = words;
    }
    
    // Level 2 - Foods data from the photo
    public static LevelData getLevel2() {
        char[][] grid = {
            {'P', 'M', 'A', 'M', 'T', 'S'},
            {'M', 'U', 'N', 'M', 'R', 'P'},
            {'B', 'S', 'A', 'E', 'O', 'I'},
            {'E', 'H', 'N', 'L', 'U', 'H'},
            {'E', 'R', 'A', 'O', 'T', 'C'},
            {'F', 'O', 'B', 'N', 'N', 'N'},
            {'P', 'O', 'A', 'O', 'S', 'O'},
            {'O', 'M', 'T', 'R', 'U', 'B'}
        };
        
        List<String> words = Arrays.asList(
            "MELON", "CHIPS", "BEEF", "BANANA", "MUSHROOM", "TROUT"
        );
        
        return new LevelData(2, "Foods", grid, words);
    }
    
    // Get grid dimensions
    public int getRows() {
        return grid.length;
    }
    
    public int getCols() {
        return grid[0].length;
    }
    
    // Get letter at position
    public char getLetter(int row, int col) {
        if (row >= 0 && row < getRows() && col >= 0 && col < getCols()) {
            return grid[row][col];
        }
        return ' ';
    }
}