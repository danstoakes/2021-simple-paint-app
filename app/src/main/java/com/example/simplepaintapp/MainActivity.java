package com.example.simplepaintapp;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private CanvasExporter canvasExporter;
    private CanvasView canvasView;
    private ScaleGestureDetector scaleGestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideUINavigation();

        canvasExporter = new CanvasExporter();
        canvasView = findViewById(R.id.canvasView);

        ScaleHandler scaleHandler = createScaleHandler();
        scaleGestureDetector = new ScaleGestureDetector(MainActivity.this, scaleHandler);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        canvasView.initialise(displayMetrics.widthPixels, displayMetrics.heightPixels);
        canvasView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                scaleGestureDetector.onTouchEvent(event);

                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        handleUIElements(View.INVISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        handleUIElements(View.VISIBLE);
                        break;
                }

                if (event.getPointerCount() == 1 && !scaleGestureDetector.isInProgress())
                {
                    if (canvasView.getPreviousStrokeWidth() == canvasView.getStrokeWidth())
                    {
                        canvasView.handleTouches(event.getX(), event.getY(), event.getAction());
                    } else
                    {
                        canvasView.undo();
                    }
                    canvasView.setPreviousStrokeWidth(canvasView.getStrokeWidth());
                }
                return true;
            }
        });

        ImageButton clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(this);

        ImageButton undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(this);

        ImageButton redoButton = findViewById(R.id.redoButton);
        redoButton.setOnClickListener(this);

        ImageButton styleButton = findViewById(R.id.styleButton);
        styleButton.setOnClickListener(this);

        ImageButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        ImageButton shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(this);
    }

    private void hideUINavigation()
    {
        final View view = getWindow().getDecorView();
        final int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        view.setSystemUiVisibility(flags);
        view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
        {
            @Override
            public void onSystemUiVisibilityChange(int visibility)
            {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                    view.setSystemUiVisibility(flags);
            }
        });
    }

    private ScaleHandler createScaleHandler()
    {
        ImageView penIcon = findViewById(R.id.penSizeIcon);

        ScaleHandler scaleHandler = new ScaleHandler();
        scaleHandler.setOnScaleChangedListener(new ScaleHandler.ScaleChangedListener()
        {
            @Override
            public GradientDrawable onScaleIconRequired()
            {
                GradientDrawable gradientDrawable = (GradientDrawable) penIcon.getBackground();
                gradientDrawable.setColor(canvasView.getColour());

                return gradientDrawable;
            }

            @Override
            public Context getContext()
            {
                return MainActivity.this;
            }

            @Override
            public Resources getContextResources()
            {
                return getResources();
            }

            @Override
            public void onScaleStarted()
            {
                penIcon.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScaleChanged(float scaleFactor)
            {
                if (scaleFactor == ScaleHandler.MIN_WIDTH || scaleFactor == ScaleHandler.MAX_WIDTH)
                    canvasView.setPreviousStrokeWidth(Math.round(scaleFactor) - 1);
                canvasView.setStrokeWidth(Math.round(scaleFactor));

                ViewGroup.LayoutParams params = penIcon.getLayoutParams();
                params.width = (int) scaleFactor;
                params.height = (int) scaleFactor;

                penIcon.setLayoutParams(params);
            }

            @Override
            public void onScaleEnded()
            {
                penIcon.setVisibility(View.GONE);
            }
        });
        return scaleHandler;
    }

    private void handleUIElements (int showType)
    {
        ViewGroup viewGroup = findViewById(R.id.container);

        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {
            View view = viewGroup.getChildAt(i);

            if (view.getId() != R.id.canvasView && view.getId() != R.id.penSizeIcon)
                view.setVisibility(showType);
        }
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();

        if (viewID == R.id.clearButton)
        {
            canvasView.clear();
        } else if (viewID == R.id.undoButton)
        {
            canvasView.undo();
        } else if (viewID == R.id.redoButton)
        {
            canvasView.redo();
        } else if (viewID == R.id.styleButton)
        {
            ColourPickerDialog dialog = new ColourPickerDialog(MainActivity.this, canvasView.getColour());
            dialog.setOnDialogOptionSelectedListener(new ColourPickerDialog.ColourPickerOptionSelectedListener()
            {
                @Override
                public void onColourPickerOptionSelected(int colour)
                {
                    canvasView.setColour(colour);
                }
            });
            dialog.show();
        } else if (viewID == R.id.saveButton)
        {
            canvasExporter.setExportType(CanvasExporter.FLAG_SAVE);
            checkForPermissions();
        } else if (viewID == R.id.shareButton)
        {
            canvasExporter.setExportType(CanvasExporter.FLAG_SHARE);
            checkForPermissions();
        }
    }

    private void requestStoragePermission ()
    {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        ActivityCompat.requestPermissions(this, new String[]{permission},
                CanvasExporter.PERMISSION_WRITE_EXTERNAL_STORAGE);
    }

    private void checkForPermissions()
    {
        int permission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_DENIED)
        {
            boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (shouldShowRationale)
            {
                StorageRationaleDialog dialog = new StorageRationaleDialog(MainActivity.this);
                dialog.setOnStorageRationaleOptionSelectedListener(new StorageRationaleDialog.StorageRationaleOptionSelectedListener()
                {
                    @Override
                    public void onStorageRationaleOptionSelected(boolean allow)
                    {
                        if (allow)
                            requestStoragePermission();
                    }
                });
                dialog.show();
            } else
            {
                requestStoragePermission();
            }
        } else
        {
            exportImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CanvasExporter.PERMISSION_WRITE_EXTERNAL_STORAGE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                exportImage();
            } else
            {
                findViewById(R.id.saveButton).setEnabled(false);
                findViewById(R.id.saveButton).setAlpha(0.5f);
                findViewById(R.id.shareButton).setEnabled(false);
                findViewById(R.id.shareButton).setAlpha(0.5f);
            }
        }
    }

    private void exportImage ()
    {
        if (canvasExporter.getExportType() == CanvasExporter.FLAG_SAVE)
        {
            String fileName = canvasExporter.saveImage(canvasView.getBitmap());

            if (fileName != null)
            {
                MediaScannerConnection.scanFile(
                        MainActivity.this, new String[]{fileName}, null, null);
                Toast.makeText(MainActivity.this, "The image was saved successfully.", Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(MainActivity.this, "There was an error saving the image.", Toast.LENGTH_SHORT).show();
            }
        } else if (canvasExporter.getExportType() == CanvasExporter.FLAG_SHARE)
        {
            shareImage();
        }
    }

    private void shareImage()
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        File image = canvasExporter.getImage(canvasView.getBitmap());

        if (image != null)
        {
            Uri uri = FileProvider.getUriForFile(
                    MainActivity.this,
                    MainActivity.this.getApplicationContext().getPackageName() +
                            ".provider", canvasExporter.getImage(canvasView.getBitmap()));

            intent.putExtra(Intent.EXTRA_STREAM, uri).setType("image/png");

            startActivity(Intent.createChooser(intent, "Share image via"));
        } else
        {
            Toast.makeText(MainActivity.this, "There was an error sharing the image.", Toast.LENGTH_SHORT).show();
        }
    }
}