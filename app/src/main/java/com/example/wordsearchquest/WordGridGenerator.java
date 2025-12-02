package com.example.wordsearchquest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordGridGenerator {
    
    public static class WordPlacement {
        public final String word;
        public final int startRow;
        public final int startCol;
        public final int endRow;
        public final int endCol;
        public final Direction direction;
        
        public WordPlacement(String word, int startRow, int startCol, int endRow, int endCol, Direction direction) {
            this.word = word;
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
            this.direction = direction;
        }
    }
    
    public enum Direction {
        HORIZONTAL, VERTICAL, DIAGONAL_DOWN, DIAGONAL_UP
    }
    
    public static class GridResult {
        public final char[][] grid;
        public final List<WordPlacement> wordPlacements;
        
        public GridResult(char[][] grid, List<WordPlacement> wordPlacements) {
            this.grid = grid;
            this.wordPlacements = wordPlacements;
        }
    }
    
    private static final Random random = new Random();
    
    public static GridResult generateGrid(List<String> words, int gridSize) {
        GridResult bestResult = null;
        int maxGridAttempts = 10;
        
        for (int attempt = 0; attempt < maxGridAttempts; attempt++) {
            GridResult result = tryGenerateGrid(words, gridSize);
            
            // If all words placed, return immediately
            if (result.wordPlacements.size() == words.size()) {
                return result;
            }
            
            // Keep track of the best result (most words placed)
            if (bestResult == null || result.wordPlacements.size() > bestResult.wordPlacements.size()) {
                bestResult = result;
            }
        }
        
        return bestResult;
    }

    private static GridResult tryGenerateGrid(List<String> words, int gridSize) {
        char[][] grid = new char[gridSize][gridSize];
        List<WordPlacement> wordPlacements = new ArrayList<>();
        
        // Initialize grid with empty spaces
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = ' ';
            }
        }
        
        // Place words in the grid
        // Sort words by length (longest first) to improve placement success
        List<String> sortedWords = new ArrayList<>(words);
        sortedWords.sort((s1, s2) -> Integer.compare(s2.length(), s1.length()));
        
        for (String word : sortedWords) {
            if (word.length() <= gridSize) {
                WordPlacement placement = placeWordInGrid(grid, word, gridSize);
                if (placement != null) {
                    wordPlacements.add(placement);
                }
            }
        }
        
        // Fill empty spaces with random letters
        fillEmptySpaces(grid, gridSize);
        
        return new GridResult(grid, wordPlacements);
    }
    
    private static WordPlacement placeWordInGrid(char[][] grid, String word, int gridSize) {
        int maxAttempts = 500; // Increased attempts
        Direction[] directions = Direction.values();
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Direction direction = directions[random.nextInt(directions.length)];
            
            int[] start = getRandomStartPosition(word, gridSize, direction);
            if (start == null) continue;
            
            int startRow = start[0];
            int startCol = start[1];
            
            if (canPlaceWord(grid, word, startRow, startCol, direction)) {
                placeWord(grid, word, startRow, startCol, direction);
                
                int[] end = getEndPosition(startRow, startCol, word.length() - 1, direction);
                return new WordPlacement(word, startRow, startCol, end[0], end[1], direction);
            }
        }
        
        return null; // Could not place word
    }
    
    private static int[] getRandomStartPosition(String word, int gridSize, Direction direction) {
        int maxRow, maxCol;
        
        switch (direction) {
            case HORIZONTAL:
                maxRow = gridSize;
                maxCol = gridSize - word.length() + 1;
                break;
            case VERTICAL:
                maxRow = gridSize - word.length() + 1;
                maxCol = gridSize;
                break;
            case DIAGONAL_DOWN:
            case DIAGONAL_UP:
                maxRow = gridSize - word.length() + 1;
                maxCol = gridSize - word.length() + 1;
                break;
            default:
                return null;
        }
        
        if (maxRow <= 0 || maxCol <= 0) return null;
        
        int row = random.nextInt(maxRow);
        int col = random.nextInt(maxCol);
        
        // For diagonal up, adjust starting row
        if (direction == Direction.DIAGONAL_UP) {
            row = word.length() - 1 + random.nextInt(gridSize - word.length() + 1);
        }
        
        return new int[]{row, col};
    }
    
    private static boolean canPlaceWord(char[][] grid, String word, int startRow, int startCol, Direction direction) {
        for (int i = 0; i < word.length(); i++) {
            int[] pos = getCharPosition(startRow, startCol, i, direction);
            int row = pos[0];
            int col = pos[1];
            
            if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length) {
                return false;
            }
            
            char currentChar = grid[row][col];
            char wordChar = word.charAt(i);
            
            if (currentChar != ' ' && currentChar != wordChar) {
                return false;
            }
        }
        
        return true;
    }
    
    private static void placeWord(char[][] grid, String word, int startRow, int startCol, Direction direction) {
        for (int i = 0; i < word.length(); i++) {
            int[] pos = getCharPosition(startRow, startCol, i, direction);
            grid[pos[0]][pos[1]] = word.charAt(i);
        }
    }
    
    private static int[] getCharPosition(int startRow, int startCol, int index, Direction direction) {
        switch (direction) {
            case HORIZONTAL:
                return new int[]{startRow, startCol + index};
            case VERTICAL:
                return new int[]{startRow + index, startCol};
            case DIAGONAL_DOWN:
                return new int[]{startRow + index, startCol + index};
            case DIAGONAL_UP:
                return new int[]{startRow - index, startCol + index};
            default:
                return new int[]{startRow, startCol};
        }
    }
    
    private static int[] getEndPosition(int startRow, int startCol, int length, Direction direction) {
        return getCharPosition(startRow, startCol, length, direction);
    }
    
    private static void fillEmptySpaces(char[][] grid, int gridSize) {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (grid[i][j] == ' ') {
                    grid[i][j] = (char) ('A' + random.nextInt(26));
                }
            }
        }
    }
}