package com.example.hw2;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MenuActivity extends AppCompatActivity {

    private MaterialButton menu_BTN_slowMode;
    private MaterialButton menu_BTN_fastMode;
    private MaterialButton menu_BTN_sensorMode;
    private MaterialButton menu_BTN_topTen;
    private eGameMode gameMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);
        findViews();
        initViews();
    }

    private void initViews() {
        menu_BTN_slowMode.setOnClickListener(v -> {
            gameMode = eGameMode.SLOW_ARROWS;
            startGame();
        });

        menu_BTN_fastMode.setOnClickListener(v -> {
            gameMode = eGameMode.FAST_ARROWS;
            startGame();
        });

        menu_BTN_sensorMode.setOnClickListener(v -> {
            gameMode = eGameMode.SENSOR;
            startGame();
        });

        menu_BTN_topTen.setOnClickListener(v -> openRecords());


    }


    private void findViews() {
        menu_BTN_slowMode = findViewById(R.id.menu_BTN_slowMode);
        menu_BTN_fastMode = findViewById(R.id.menu_BTN_fastMode);
        menu_BTN_sensorMode = findViewById(R.id.menu_BTN_sensorMode);
        menu_BTN_topTen = findViewById(R.id.menu_BTN_topTen);
    }

    private void startGame() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(DataManager.GAME_MODE, gameMode.name());
        startActivity(intent);
        finish();
    }

    private void openRecords() {
        Intent intent = new Intent(this, RecordsActivity.class);
        startActivity(intent);
        finish();
    }

}
