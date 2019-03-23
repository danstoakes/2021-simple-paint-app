package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PaintView extends View {
    public static int BRUSH_SIZE = 15;
    public static final int DEFAULT_COLOUR = Color.RED;
    public static final int DEFAULT_BG_COLOUR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;

    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private int currentColour;
    private int backgroundColour = DEFAULT_BG_COLOUR;
    private int strokeWidth = BRUSH_SIZE;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private String requestedFunction;

    public ArrayList<Draw> paths = new ArrayList<>();
    public ArrayList<Draw> undo = new ArrayList<>();

    public PaintView (Context context) {
        this(context, null);
    }

    public PaintView (Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOUR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
    }

    public void initialise (DisplayMetrics displayMetrics) {
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColour = DEFAULT_COLOUR;
        strokeWidth = BRUSH_SIZE;
    }

    public void setRequestedFunction(String newFunction) {
        requestedFunction = newFunction;
    }

    public String getRequestedFunction() {
        return requestedFunction;
    }

    @Override
    public void onDraw (Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(backgroundColour);

        for (Draw draw : paths) {

            mPaint.setColor(draw.colour);
            mPaint.setStrokeWidth(draw.strokeWidth);
            mPaint.setMaskFilter(null);

            mCanvas.drawPath(draw.path, mPaint);

        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void touchStart (float x, float y) {
        mPath = new Path();

        Draw draw = new Draw(currentColour, strokeWidth, mPath);
        paths.add(draw);

        mPath.reset();
        mPath.moveTo(x, y);

        mX = x;
        mY = y;
    }

    private void touchMove (float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);

            mX = x;
            mY = y;
        }
    }

    private void touchUp () {
        mPath.lineTo(mX, mY);
    }

    public void handleMotion (MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchStart(x, y);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                touchUp();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                touchMove(x, y);
                invalidate();
                break;
            }
        }
    }

    public void clear () {
        backgroundColour = DEFAULT_BG_COLOUR;

        paths.clear();
        invalidate();
    }

    public void undo () {
        if (paths.size() > 0) {
            undo.add(paths.remove(paths.size() - 1));
            invalidate();
        } else {
            Toast.makeText(getContext(), "Perform action to undo.", Toast.LENGTH_LONG).show();
        }
    }

    public void redo () {
        if (undo.size() > 0) {
            paths.add(undo.remove(undo.size() - 1));
            invalidate();
        } else {
            Toast.makeText(getContext(), "Perform action to redo.", Toast.LENGTH_LONG).show();
        }
    }

    public void setStrokeWidth (int newWidth) {
        strokeWidth = newWidth;
    }

    public int getStrokeWidth () {
        return strokeWidth;
    }

    public void setColour (int colour) {
        currentColour = colour;
    }

    public int getColour () {
        return currentColour;
    }

    public void saveImage () {
        int count = 0;

        File sdCardDirectory = Environment.getExternalStorageDirectory();
        File subDirectory = new File(sdCardDirectory.toString() + "/Pictures/Paint");

        if (subDirectory.exists()) {
            File[] existingImages = subDirectory.listFiles();
            for (File file : existingImages) {
                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                    count++;
                }
            }
        } else {
            if (subDirectory.mkdir()) {
                Toast.makeText(getContext(), "Couldn't make directory.", Toast.LENGTH_LONG).show();
            }
        }

        if (subDirectory.exists()) {
            File image = new File(subDirectory, "/drawing_" + (count + 1) + ".png");
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(image);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                fileOutputStream.flush();
                fileOutputStream.close();

                Toast.makeText(getContext(), "Successfully saved to camera roll.", Toast.LENGTH_LONG).show();

                MediaScannerConnection.scanFile(getContext(), new String[]{image.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }

                });
                // image.setReadable(true, false);
            } catch (FileNotFoundException e) {
                Toast.makeText(getContext(), "Could not save to camera roll.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Could not save to camera roll.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public File getImage () {
        File sdCardDirectory = Environment.getExternalStorageDirectory();
        File subDirectory = new File(sdCardDirectory.toString() + "/Pictures/Paint");
        File image = new File(subDirectory, "/shared_" + Math.random() + ".png");
        FileOutputStream fileOutputStream;

        try{
            fileOutputStream = new FileOutputStream(image);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(), "Could not share the image.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Could not share the image.", Toast.LENGTH_LONG).show();
        }
        return image;
    }
}