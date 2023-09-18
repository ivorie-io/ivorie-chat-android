package com.ivoriechat.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

public class RoundedCornersTransform implements Transformation {
    private int mCornerRadius = 4; // default radius is 4
    @Override
    public Bitmap transform(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        int radius = mCornerRadius;
        Bitmap circle;
        circle = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circle);

        // Shader is the based class for objects that return horizontal spans of colors during drawing.
        BitmapShader shader;
        shader = new BitmapShader(originalBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        final int color = 0xff424242;
        paint.setColor(color);
        // paint.setShader(shader);

        Rect rect = new Rect(0, 0, width, height);
        RectF rectF = new RectF(rect);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(originalBitmap, rect, rect, paint);
        originalBitmap.recycle();
        return circle;
    }

    @Override
    public String key() {
        return "rounded_corners";
    }

    // to set the radius for the rounded cornner bitmap
    public void setRadius(int radius) {
        if (radius > 0) {
            mCornerRadius = radius;
        }
    }
}
