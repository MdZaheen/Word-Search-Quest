package com.example.wordsearchquest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class WordGridView extends View {

    private char[][] grid;
    private int gridSize;
    private float cellSize;
    private Paint textPaint;
    private Paint backgroundPaint;
    private Paint selectionPaint;
    private Paint foundWordPaint;
    private Paint borderPaint;
    
    private List<WordGridGenerator.WordPlacement> wordPlacements;
    private List<String> foundWords;
    private List<GridCell> selectedCells;
    private List<List<GridCell>> foundWordCells;
    private List<Paint> foundWordPaints; // Different paint for each found word
    
    private boolean isSelecting = false;
    private int startRow = -1, startCol = -1;
    private int endRow = -1, endCol = -1;
    
    private OnWordSelectedListener listener;
    
    public interface OnWordSelectedListener {
        void onWordSelected(String word);
    }
    
    public void setOnWordSelectedListener(OnWordSelectedListener listener) {
        this.listener = listener;
    }

    public WordGridView(Context context) {
        super(context);
        init();
    }

    public WordGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        foundWords = new ArrayList<>();
        selectedCells = new ArrayList<>();
        foundWordCells = new ArrayList<>();
        foundWordPaints = new ArrayList<>();
        
        // Initialize paints
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.letter_text));
        textPaint.setTextSize(48f);
        textPaint.setFakeBoldText(true);
        
        backgroundPaint = new Paint();
        backgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.grid_background));
        
        selectionPaint = new Paint();
        selectionPaint.setColor(ContextCompat.getColor(getContext(), R.color.selected_word));
        selectionPaint.setAlpha(128);
        
        foundWordPaint = new Paint();
        foundWordPaint.setColor(ContextCompat.getColor(getContext(), R.color.found_word));
        foundWordPaint.setAlpha(128);
        
        borderPaint = new Paint();
        borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.grid_border));
        borderPaint.setStrokeWidth(2f);
        borderPaint.setStyle(Paint.Style.STROKE);
    }

    public void setGridData(char[][] grid, List<WordGridGenerator.WordPlacement> wordPlacements) {
        this.grid = grid;
        this.gridSize = grid.length;
        this.wordPlacements = wordPlacements;
        this.foundWords.clear();
        this.foundWordCells.clear();
        this.foundWordPaints.clear();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (gridSize > 0) {
            cellSize = Math.min(w, h) / (float) gridSize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (grid == null || gridSize == 0) return;
        
        // Draw background
        canvas.drawRect(0, 0, cellSize * gridSize, cellSize * gridSize, backgroundPaint);
        
        // Draw found words highlighting with different colors
        for (int i = 0; i < foundWordCells.size(); i++) {
            List<GridCell> wordCells = foundWordCells.get(i);
            Paint paint = i < foundWordPaints.size() ? foundWordPaints.get(i) : foundWordPaint;
            drawWordHighlight(canvas, wordCells, paint);
        }
        
        // Draw current selection
        if (!selectedCells.isEmpty()) {
            drawWordHighlight(canvas, selectedCells, selectionPaint);
        }
        
        // Draw grid lines and letters
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                float x = col * cellSize;
                float y = row * cellSize;
                
                // Draw cell border
                canvas.drawRect(x, y, x + cellSize, y + cellSize, borderPaint);
                
                // Draw letter
                char letter = grid[row][col];
                float textX = x + cellSize / 2;
                float textY = y + cellSize / 2 - ((textPaint.descent() + textPaint.ascent()) / 2);
                canvas.drawText(String.valueOf(letter), textX, textY, textPaint);
            }
        }
    }

    private void drawWordHighlight(Canvas canvas, List<GridCell> cells, Paint paint) {
        if (cells.isEmpty()) return;
        
        // Check if selection is horizontal, vertical, or diagonal
        boolean isHorizontal = cells.size() > 1 && cells.get(0).row == cells.get(cells.size() - 1).row;
        boolean isVertical = cells.size() > 1 && cells.get(0).col == cells.get(cells.size() - 1).col;
        
        if (isHorizontal || isVertical || cells.size() == 1) {
            // Draw a single continuous rounded rectangle for straight lines
            int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
            int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
            
            for (GridCell cell : cells) {
                minRow = Math.min(minRow, cell.row);
                maxRow = Math.max(maxRow, cell.row);
                minCol = Math.min(minCol, cell.col);
                maxCol = Math.max(maxCol, cell.col);
            }
            
            float left = minCol * cellSize + 6;
            float top = minRow * cellSize + 6;
            float right = (maxCol + 1) * cellSize - 6;
            float bottom = (maxRow + 1) * cellSize - 6;
            float cornerRadius = 12f;
            
            Path path = new Path();
            path.addRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, Path.Direction.CW);
            canvas.drawPath(path, paint);
        } else {
            // For diagonal selections, draw individual rounded rectangles for each cell
            float cornerRadius = 8f;
            for (GridCell cell : cells) {
                float left = cell.col * cellSize + 6;
                float top = cell.row * cellSize + 6;
                float right = (cell.col + 1) * cellSize - 6;
                float bottom = (cell.row + 1) * cellSize - 6;
                
                Path cellPath = new Path();
                cellPath.addRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, Path.Direction.CW);
                canvas.drawPath(cellPath, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startSelection(event.getX(), event.getY());
                return true;
                
            case MotionEvent.ACTION_MOVE:
                updateSelection(event.getX(), event.getY());
                return true;
                
            case MotionEvent.ACTION_UP:
                endSelection();
                return true;
        }
        return false;
    }

    private void startSelection(float x, float y) {
        GridCell cell = getCellFromCoordinates(x, y);
        if (cell != null) {
            isSelecting = true;
            startRow = cell.row;
            startCol = cell.col;
            endRow = cell.row;
            endCol = cell.col;
            updateSelectedCells();
        }
    }

    private void updateSelection(float x, float y) {
        if (!isSelecting) return;
        
        GridCell cell = getCellFromCoordinates(x, y);
        if (cell != null && isValidSelection(startRow, startCol, cell.row, cell.col)) {
            endRow = cell.row;
            endCol = cell.col;
            updateSelectedCells();
            invalidate();
        }
    }

    private void endSelection() {
        if (!isSelecting) return;
        
        isSelecting = false;
        String selectedWord = getSelectedWord();
        
        if (selectedWord != null && isValidWord(selectedWord)) {
            markWordAsFound(selectedWord);
            if (listener != null) {
                listener.onWordSelected(selectedWord);
            }
        }
        
        selectedCells.clear();
        invalidate();
    }

    private GridCell getCellFromCoordinates(float x, float y) {
        if (cellSize == 0) return null;
        
        int col = (int) (x / cellSize);
        int row = (int) (y / cellSize);
        
        if (row >= 0 && row < gridSize && col >= 0 && col < gridSize) {
            return new GridCell(row, col);
        }
        return null;
    }

    private boolean isValidSelection(int startRow, int startCol, int endRow, int endCol) {
        int rowDiff = Math.abs(endRow - startRow);
        int colDiff = Math.abs(endCol - startCol);
        
        // Must be horizontal, vertical, or diagonal
        return rowDiff == 0 || colDiff == 0 || rowDiff == colDiff;
    }

    private void updateSelectedCells() {
        selectedCells.clear();
        
        int rowStep = Integer.compare(endRow, startRow);
        int colStep = Integer.compare(endCol, startCol);
        
        int currentRow = startRow;
        int currentCol = startCol;
        
        while (true) {
            selectedCells.add(new GridCell(currentRow, currentCol));
            
            if (currentRow == endRow && currentCol == endCol) {
                break;
            }
            
            currentRow += rowStep;
            currentCol += colStep;
        }
    }

    private String getSelectedWord() {
        if (selectedCells.isEmpty()) return null;
        
        StringBuilder word = new StringBuilder();
        for (GridCell cell : selectedCells) {
            word.append(grid[cell.row][cell.col]);
        }
        
        String forward = word.toString();
        String backward = word.reverse().toString();
        
        // Check both directions
        if (isValidWord(forward)) return forward;
        if (isValidWord(backward)) return backward;
        
        return null;
    }

    private boolean isValidWord(String word) {
        if (wordPlacements == null) return false;
        
        for (WordGridGenerator.WordPlacement placement : wordPlacements) {
            if (placement.word.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    private void markWordAsFound(String word) {
        if (!foundWords.contains(word.toUpperCase())) {
            foundWords.add(word.toUpperCase());
            
            // Create a unique color paint for this word
            Paint wordPaint = createColoredPaint(foundWords.size() - 1);
            foundWordPaints.add(wordPaint);
            
            // Find the word placement and add cells to found words
            for (WordGridGenerator.WordPlacement placement : wordPlacements) {
                if (placement.word.equalsIgnoreCase(word)) {
                    List<GridCell> wordCells = getWordCells(placement);
                    foundWordCells.add(wordCells);
                    break;
                }
            }
        }
    }

    private List<GridCell> getWordCells(WordGridGenerator.WordPlacement placement) {
        List<GridCell> cells = new ArrayList<>();
        
        int rowStep = Integer.compare(placement.endRow, placement.startRow);
        int colStep = Integer.compare(placement.endCol, placement.startCol);
        
        if (rowStep != 0) rowStep = rowStep / Math.abs(rowStep);
        if (colStep != 0) colStep = colStep / Math.abs(colStep);
        
        int currentRow = placement.startRow;
        int currentCol = placement.startCol;
        
        for (int i = 0; i < placement.word.length(); i++) {
            cells.add(new GridCell(currentRow, currentCol));
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return cells;
    }

    public List<String> getFoundWords() {
        return new ArrayList<>(foundWords);
    }

    public boolean isAllWordsFound() {
        return foundWords.size() == wordPlacements.size();
    }
    
    private Paint createColoredPaint(int wordIndex) {
        // Array of vibrant colors for found words
        int[] colors = {
            0xCC39FF14, // Neon Green
            0xCC00D4FF, // Neon Blue  
            0xCCFF073A, // Neon Pink
            0xCCFFFF00, // Neon Yellow
            0xCC8B00FF, // Neon Purple
            0xCCFF6B35, // Neon Orange
            0xCC00FFFF, // Neon Cyan
            0xCCFF1493, // Deep Pink
            0xCC32CD32, // Lime Green
            0xCC1E90FF  // Dodger Blue
        };
        
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(colors[wordIndex % colors.length]);
        paint.setAlpha(180); // Semi-transparent
        return paint;
    }
    
    public void highlightWordOnGrid(String word) {
        if (wordPlacements == null) return;
        
        for (WordGridGenerator.WordPlacement placement : wordPlacements) {
            if (placement.word.equalsIgnoreCase(word)) {
                // Get the word cells
                List<GridCell> wordCells = getWordCells(placement);
                
                // Temporarily show selection
                selectedCells = new ArrayList<>(wordCells);
                invalidate();
                
                // Auto-select the word after a brief delay
                postDelayed(() -> {
                    markWordAsFound(word);
                    selectedCells.clear();
                    invalidate();
                    if (listener != null) {
                        listener.onWordSelected(word);
                    }
                }, 1000); // 1 second delay to show the selection
                
                break;
            }
        }
    }

    private static class GridCell {
        final int row;
        final int col;

        GridCell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}