package com.deckerth.thomas.foobarremotecontroller2.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.deckerth.thomas.foobarremotecontroller2.R;

public class Circle extends View {

    private Paint paint;

    public int color;

    public Circle(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.Circle,
                0, 0);

        int color = Color.GREEN;

        try {
            color = a.getInteger(R.styleable.Circle_circleColor, Color.GREEN);
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        // Draw a rectangle with the specified left, top, right, bottom coordinates
        canvas.drawCircle(height/2, height/2, height/2, paint);
        //canvas.drawCircle(0, 0, 20, paint);
    }
}
