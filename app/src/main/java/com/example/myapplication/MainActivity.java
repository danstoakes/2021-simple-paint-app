package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

public class MainActivity extends AppCompatActivity {

    private PaintView paintView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideUINavigation();

        paintView = findViewById(R.id.paintView);

        paintView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        handleCustomUIElements(View.INVISIBLE);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        handleCustomUIElements(View.VISIBLE);
                        break;
                    }
                }
                paintView.handleMotion(event);
                return true;
            }
        });
    }

    private void hideUINavigation () {
        final View view = getWindow().getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
                }
            }
        });
    }

    private void handleCustomUIElements (int showType) {
        ViewGroup viewGroup = findViewById(R.id.container);

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);

            if (view.getId() != R.id.paintView) {
                view.setVisibility(showType);
            }
        }
    }
}
