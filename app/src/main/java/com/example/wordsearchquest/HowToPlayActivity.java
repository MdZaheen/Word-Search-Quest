package com.example.wordsearchquest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class HowToPlayActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnGotIt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);
        
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnGotIt = findViewById(R.id.btnGotIt);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnGotIt.setOnClickListener(v -> {
            finish();
        });
    }
}