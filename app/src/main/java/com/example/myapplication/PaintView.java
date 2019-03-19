package com.example.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class PaintView extends View {
    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void handleMotion (MotionEvent motionEvent) {
        // Toast.makeText(getContext(), "made irt here", Toast.LENGTH_LONG).show();
    }
}
