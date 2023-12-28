package com.schaubeck.watertrial.utils;

import android.content.Context;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;

public class PathVectorDrawable extends AppCompatImageView {

    public PathVectorDrawable(Context context) {
        super(context);
    }

    public void setPathColor(String pathName, int color) {
        Drawable drawable = getDrawable();
        if (drawable instanceof VectorDrawable) {
            VectorDrawable vectorDrawable = (VectorDrawable) drawable;
        }
    }

}
