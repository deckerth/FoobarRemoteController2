package com.deckerth.thomas.foobarremotecontroller2.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.deckerth.thomas.foobarremotecontroller2.R;

public class Rectangle extends View {

    private static final int WIDTH = 10;
    private final Paint paint;

    public Rectangle(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.Rectangle,
                0, 0);

        int color;

        try {
             color = a.getInteger(R.styleable.Rectangle_markerColor, Color.GREEN);
            // Use the parameter as needed
        } finally {
            a.recycle();
        }
        // Initialize a Paint object with desired attributes
        paint = new Paint();
        //paint.setColor(ContextCompat.getColor(context, R.attr.colo));
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL); // Set the style
        paint.setStrokeWidth(10); // Set the stroke width
        setMinimumHeight(100);
        setMinimumWidth(30);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        // Draw a rectangle with the specified left, top, right, bottom coordinates
        canvas.drawRoundRect(0, 10, WIDTH, height - 10, 10, 10, paint);
    }
}
