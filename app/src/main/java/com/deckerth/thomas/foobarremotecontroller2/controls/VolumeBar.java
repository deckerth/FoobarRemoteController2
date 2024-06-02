package com.deckerth.thomas.foobarremotecontroller2.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.deckerth.thomas.foobarremotecontroller2.R;
import com.deckerth.thomas.foobarremotecontroller2.TitleDetailHostActivity;
import com.deckerth.thomas.foobarremotecontroller2.model.VolumeControl;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistViewModel;

public class VolumeBar extends View {
    private Paint paint;

    private Integer percent = 50;
    private Integer backgroundColor = Color.GREEN;
    private Integer foregroundColor = Color.BLUE;

    public VolumeBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        // Initialize a Paint object with desired attributes
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL); // Set the style
        paint.setStrokeWidth(10); // Set the stroke width

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.VolumeBar,
                0, 0);

        try {
            backgroundColor = a.getInteger(R.styleable.VolumeBar_volumeBackgroundColor, Color.GREEN);
            foregroundColor = a.getInteger(R.styleable.VolumeBar_volumeForegroundColor, Color.BLUE);
            // Use the parameter as needed
        } finally {
            a.recycle();
        }
    }
    public void setVolumePercent(int percent) {
        this.percent = percent;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        // Draw a rectangle with the specified left, top, right, bottom coordinates
        double heightMarkedValue = (double)height * (double)percent / 100.0;
        int heightMarked = (int)Math.round(heightMarkedValue);
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(0, 0, width, height, 90, 90, paint);
        paint.setColor(foregroundColor);
        canvas.drawRoundRect(0, height - heightMarked, width,height , 90, 90, paint);

        int iconWidth = width/3*2;
        int halfIconWidth = iconWidth / 2;
        int horizontalCenter = width / 2;
        Rect destRect = new Rect(horizontalCenter-halfIconWidth, height-iconWidth-30, horizontalCenter+halfIconWidth, height-30);
        canvas.drawBitmap(TitleDetailHostActivity.FOOBAR_BITMAP, null, destRect, null);
    }
}
