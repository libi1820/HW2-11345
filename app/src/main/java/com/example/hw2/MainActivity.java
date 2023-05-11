package com.example.hw2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.hw2.Interfaces.StepCallback;
import com.example.hw2.Logic.GameManager;
import com.example.hw2.Utilities.StepDetector;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    private final int SEC = 1000;
    private final int SLOW_DELAY = 1500;
    private final int FAST_DELAY = 700;
    private int delay;
    private final int ROWS = 6;
    private final int COLS = 5;
    private final int LIFE = 3;
    private int score;
    private Timer timer;
    float temp = 0;
    private MediaPlayer sound;
    private TextView main_TXT_score;
    private AppCompatImageView main_IMG_background;
    private ShapeableImageView[] main_IMG_hearts;
    private ShapeableImageView[] main_IMG_dogs;
    private ShapeableImageView[][] main_IMG_ball;
    private ShapeableImageView[][] main_IMG_newHearts;
    private ExtendedFloatingActionButton game_BTN_left;
    private ExtendedFloatingActionButton game_BTN_right;
    private MaterialTextView main_LBL_stepsX;
    private MaterialTextView main_LBL_stepsY;

    private GameManager gameManager = new GameManager(LIFE, ROWS, COLS);;
    GeneralFunctions generalFunctions;
    private eGameMode gameMode;
    private StepDetector stepDetector;
    private boolean isStartGame = false;

    private enum TIMER_STATUS {
        OFF,
        RUNNING,
    }

    private TIMER_STATUS timer_status = TIMER_STATUS.OFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setButtons();
        Intent previousIntent = getIntent();
        String enum_name = previousIntent.getExtras().getString(DataManager.GAME_MODE);
        gameMode = eGameMode.valueOf(enum_name);
        generalFunctions = new GeneralFunctions(this);
        if (gameMode == eGameMode.SLOW_ARROWS || gameMode == eGameMode.FAST_ARROWS) {
            initViewArrows();
        } else if (gameMode == eGameMode.SENSOR) {
            initViewSensor();
            stepDetector.start();
        }
        sound=MediaPlayer.create(this,R.raw.dog_barking);
        score = 0;
        viewDog();
        startGame();
    }

    private void initViewSensor() {
        delay = SLOW_DELAY;
        game_BTN_left.setVisibility(View.INVISIBLE);
        game_BTN_right.setVisibility(View.INVISIBLE);
        stepDetector = new StepDetector(this, new StepCallback() {
            @Override
            public void left() {
                clicked(0);
            }

            @Override
            public void right() {
                clicked(1);
            }
        });
    }

    private void initViewArrows() {
        setButtons();
        if (gameMode == eGameMode.SLOW_ARROWS)
            delay = SLOW_DELAY;
        else if (gameMode == eGameMode.FAST_ARROWS)
            delay = FAST_DELAY;
    }


    /*private void startTimer() {
        if (timer_status == TIMER_STATUS.OFF) {
            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                public void run() {
                    handler.postDelayed(this, delay);
                    gameManager.randomBall();
                }
            }, SEC);

            handler.postDelayed(new Runnable() {
                public void run() {
                    handler.postDelayed(this,delay+700);
                    gameManager.randomHeart();
                    gameManager.updateBoard();
                    refreshUI();
                }
            } ,SEC);
            timer_status = TIMER_STATUS.RUNNING;
        }
    }*/

    private void startTimer() {
        Handler handler = new Handler();
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                gameManager.randomBall();
                handler.postDelayed(this, delay);
            }
        };
        handler.post(runnable1);
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                gameManager.randomHeart();
                gameManager.updateBoard();
                refreshUI();
                handler.postDelayed(this, delay+600);
            }
        };
        handler.post(runnable2);
    }

    private void startGame() {
        isStartGame = true;
        startTimer();
    }

    private void stopTimer() {
        timer_status = TIMER_STATUS.OFF;
    }

    private void refreshUI() {
        score++;
        main_TXT_score.setText(" "+score);
        viewBoard();
        if (gameManager.isCrashed()) {
            sound.start();
            gameManager.crash();
            if (gameManager.isLose()) {
                stopTimer();
                openScorePage(score);
            } else {
                generalFunctions.toast("Lost Life!");
                generalFunctions.vibrate();
                for (int i = gameManager.getLife(); i < LIFE; i++) {
                    main_IMG_hearts[i].setVisibility(View.INVISIBLE);
                }
            }
        } else if (gameManager.isLife()) {
            gameManager.addLife();
            for (int i = 0; i < gameManager.getLife(); i++) {
                main_IMG_hearts[i].setVisibility(View.VISIBLE);
            }
        }
    }

    private void viewBoard() {
        int[][] board = gameManager.getBallBoard();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board[i][j] == 1) {
                    main_IMG_ball[i][j].setVisibility(View.VISIBLE);
                } else {
                    main_IMG_ball[i][j].setVisibility(View.INVISIBLE);
                }
                if (board[i][j] == 2) {
                    main_IMG_newHearts[i][j].setVisibility(View.VISIBLE);
                } else {
                    main_IMG_newHearts[i][j].setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void setButtons() {
        game_BTN_left.setOnClickListener(view -> clicked(0));
        game_BTN_right.setOnClickListener(view -> clicked(1));
    }

    private void clicked(int move) {
        int dogInd = gameManager.getDogIndex();
        if (move == 0) {
            dogInd--;
        } else if (move == 1) {
            dogInd++;
        }
        if (dogInd >= 0 && dogInd < COLS) {
            gameManager.moveDog(dogInd);
            viewDog();
        }
    }

    private void viewDog() {
        int dogInd = gameManager.getDogIndex();
        for (int i = 0; i < COLS; i++) {
            if (i == dogInd) {
                main_IMG_dogs[i].setVisibility(View.VISIBLE);
            } else {
                main_IMG_dogs[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    private void findViews() {
        main_IMG_background = findViewById(R.id.main_IMG_background);
        main_IMG_hearts = new ShapeableImageView[]{
                findViewById(R.id.main_IMG_heart01),
                findViewById(R.id.main_IMG_heart02),
                findViewById(R.id.main_IMG_heart03)};
        main_IMG_dogs = new ShapeableImageView[]{
                findViewById(R.id.main_IMG_dog1),
                findViewById(R.id.main_IMG_dog2),
                findViewById(R.id.main_IMG_dog3),
                findViewById(R.id.main_IMG_dog4),
                findViewById(R.id.main_IMG_dog5)};
        game_BTN_left = findViewById(R.id.game_BTN_left);
        game_BTN_right = findViewById(R.id.game_BTN_right);
        main_IMG_ball = new ShapeableImageView[ROWS][];
        main_IMG_ball[0] = new ShapeableImageView[]{findViewById(R.id.main_IMG_ball1), findViewById(R.id.main_IMG_ball2), findViewById(R.id.main_IMG_ball3), findViewById(R.id.main_IMG_ball4), findViewById(R.id.main_IMG_ball5)};
        main_IMG_ball[1] = new ShapeableImageView[]{findViewById(R.id.main_IMG_ball6), findViewById(R.id.main_IMG_ball7), findViewById(R.id.main_IMG_ball8), findViewById(R.id.main_IMG_ball9), findViewById(R.id.main_IMG_ball10)};
        main_IMG_ball[2] = new ShapeableImageView[]{findViewById(R.id.main_IMG_ball11), findViewById(R.id.main_IMG_ball12), findViewById(R.id.main_IMG_ball13), findViewById(R.id.main_IMG_ball14), findViewById(R.id.main_IMG_ball15)};
        main_IMG_ball[3] = new ShapeableImageView[]{findViewById(R.id.main_IMG_ball16), findViewById(R.id.main_IMG_ball17), findViewById(R.id.main_IMG_ball18), findViewById(R.id.main_IMG_ball19), findViewById(R.id.main_IMG_ball20)};
        main_IMG_ball[4] = new ShapeableImageView[]{findViewById(R.id.main_IMG_ball21), findViewById(R.id.main_IMG_ball22), findViewById(R.id.main_IMG_ball23), findViewById(R.id.main_IMG_ball24), findViewById(R.id.main_IMG_ball25)};
        main_IMG_ball[5] = new ShapeableImageView[]{findViewById(R.id.main_IMG_ball26), findViewById(R.id.main_IMG_ball27), findViewById(R.id.main_IMG_ball28), findViewById(R.id.main_IMG_ball29), findViewById(R.id.main_IMG_ball30)};
        main_IMG_newHearts = new ShapeableImageView[ROWS][];
        main_IMG_newHearts[0] = new ShapeableImageView[]{findViewById(R.id.main_IMG_heart1), findViewById(R.id.main_IMG_heart2), findViewById(R.id.main_IMG_heart3), findViewById(R.id.main_IMG_heart4), findViewById(R.id.main_IMG_heart5)};
        main_IMG_newHearts[1] = new ShapeableImageView[]{findViewById(R.id.main_IMG_heart6), findViewById(R.id.main_IMG_heart7), findViewById(R.id.main_IMG_heart8), findViewById(R.id.main_IMG_heart9), findViewById(R.id.main_IMG_heart10)};
        main_IMG_newHearts[2] = new ShapeableImageView[]{findViewById(R.id.main_IMG_heart11), findViewById(R.id.main_IMG_heart12), findViewById(R.id.main_IMG_heart13), findViewById(R.id.main_IMG_heart14), findViewById(R.id.main_IMG_heart15)};
        main_IMG_newHearts[3] = new ShapeableImageView[]{findViewById(R.id.main_IMG_heart16), findViewById(R.id.main_IMG_heart17), findViewById(R.id.main_IMG_heart18), findViewById(R.id.main_IMG_heart19), findViewById(R.id.main_IMG_heart20)};
        main_IMG_newHearts[4] = new ShapeableImageView[]{findViewById(R.id.main_IMG_heart21), findViewById(R.id.main_IMG_heart22), findViewById(R.id.main_IMG_heart23), findViewById(R.id.main_IMG_heart24), findViewById(R.id.main_IMG_heart25)};
        main_IMG_newHearts[5] = new ShapeableImageView[]{findViewById(R.id.main_IMG_heart26), findViewById(R.id.main_IMG_heart27), findViewById(R.id.main_IMG_heart28), findViewById(R.id.main_IMG_heart29), findViewById(R.id.main_IMG_heart30)};
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                main_IMG_ball[i][j].setVisibility(View.INVISIBLE);
                main_IMG_newHearts[i][j].setVisibility(View.INVISIBLE);
            }
        }
        main_TXT_score = findViewById(R.id.main_TXT_score);
    }

    private void openScorePage(int score) {
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra(ScoreActivity.KEY_SCORE, score);
        startActivity(intent);
        finish();
    }



}