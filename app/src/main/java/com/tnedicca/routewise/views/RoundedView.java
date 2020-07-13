package com.tnedicca.routewise.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Aachu on 07-03-2017.
 */

public class RoundedView extends FrameLayout {

    private final float radius;
    private Path path = new Path();
    private RectF rect = new RectF();

    public RoundedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.radius = attrs.getAttributeFloatValue(null, "corner_radius", 0f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // compute the path
        float halfWidth = w / 2f;
        float halfHeight = h / 2f;
        float centerX = halfWidth;
        float centerY = halfHeight;
        path.reset();
        path.addCircle(centerX, centerY, Math.min(halfWidth, halfHeight), Path.Direction.CW);
        path.close();

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }
}
