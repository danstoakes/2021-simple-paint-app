package com.example.myapplication;

import android.graphics.Path;

public class Draw {

    public int colour;
    public int strokeWidth;
    public Path path;

    public Draw (int colour, int strokeWidth, Path path) {
        this.colour = colour;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}