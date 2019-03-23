package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private PaintView paintView;
    private ScaleGestureDetector mScaleDetector;
    private int previousStrokeWidth = 15;
    private final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideUINavigation();

        paintView = findViewById(R.id.paintView);

        mScaleDetector = new ScaleGestureDetector(MainActivity.this, new ScaleListener());

        DisplayMetrics displayMetrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }

        paintView.initialise(displayMetrics);

        paintView.setOnTouchListener(new View.OnTouchListener() {


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);

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

                if (event.getPointerCount() == 1 && !mScaleDetector.isInProgress()) {
                    if (previousStrokeWidth == paintView.getStrokeWidth()) {
                        paintView.handleMotion(event);
                    } else {
                        paintView.undo();
                    }
                    previousStrokeWidth = paintView.getStrokeWidth();
                }

                return true;
            }
        });

        ImageButton clear = findViewById(R.id.clear_button);
        clear.setOnClickListener(this);

        ImageButton undo = findViewById(R.id.undo_button);
        undo.setOnClickListener(this);

        ImageButton redo = findViewById(R.id.redo_button);
        redo.setOnClickListener(this);

        ImageButton style = findViewById(R.id.style_button);
        style.setOnClickListener(this);

        ImageButton save = findViewById(R.id.save_button);
        save.setOnClickListener(this);

        ImageButton share = findViewById(R.id.share_button);
        share.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.clear_button: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Are you sure you want to start again?")
                        .setIcon(R.drawable.ic_report_problem_black_24dp)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                paintView.clear();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

                break;
            }
            case R.id.undo_button: {
                paintView.undo();
                break;
            }
            case R.id.redo_button: {
                paintView.redo();
                break;
            }
            case R.id.style_button: {
                openColourPicker();
                break;
            }
            case R.id.save_button: {
                paintView.setRequestedFunction("save");
                requestStoragePermission();
                break;
            }
            case R.id.share_button: {
                paintView.setRequestedFunction("share");
                requestStoragePermission();
                break;
            }
        }
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

            if (view.getId() != R.id.paintView && view.getId() != R.id.pen_size_icon) {
                view.setVisibility(showType);
            }
        }
    }

    private void requestStoragePermission () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Paint needs permission to save artwork.", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (paintView.getRequestedFunction().equals("save")) {
                paintView.saveImage();
            } else {
                shareDrawing();
            }
        }
    }

    private void openColourPicker () {
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, Color.RED, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                Toast.makeText(MainActivity.this, "Couldn't load colour picker.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                paintView.setColour(color);
            }
        });
        ambilWarnaDialog.show();
    }

    private void shareDrawing () {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri = FileProvider.getUriForFile(
                MainActivity.this,
                MainActivity.this.getApplicationContext().getPackageName() +
                        ".provider", paintView.getImage());

        intent.putExtra(Intent.EXTRA_STREAM, uri)
                .setType("image/png");

        startActivity(Intent.createChooser(intent, "Share image via"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    switch (paintView.getRequestedFunction()) {
                        case "save": {
                            paintView.saveImage();
                            break;
                        }
                        case "share": {
                            shareDrawing();
                            break;
                        }
                    }

                } else {
                    findViewById(R.id.save_button).setEnabled(false);
                    findViewById(R.id.share_button).setEnabled(false);
                }
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private ImageView imageView = findViewById(R.id.pen_size_icon);
        private float mScaleFactor = 15.0f;
        public boolean lastAction = true;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            GradientDrawable gradientDrawable = (GradientDrawable) imageView.getBackground();
            gradientDrawable.setColor(paintView.getColour());
            float px = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1f,
                    getResources().getDisplayMetrics()
            );
            gradientDrawable.setStroke((int) px, Color.parseColor("#444444"));
            imageView.setVisibility(View.VISIBLE);

            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            mScaleFactor = Math.max(5.0f, Math.min(mScaleFactor, 100.0f));

            paintView.setStrokeWidth(Math.round(mScaleFactor));
            ViewGroup.LayoutParams params = imageView.getLayoutParams();

            params.width = (int) mScaleFactor;
            params.height = (int) mScaleFactor;

            imageView.setLayoutParams(params);

            // invalidate();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            imageView.setVisibility(View.GONE);
        }
    }
}