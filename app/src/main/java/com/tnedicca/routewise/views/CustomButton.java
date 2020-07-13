package com.tnedicca.routewise.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.MainActivity;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.classes.TypeFaceProvider;

/**
 * Created by new on 03-01-2017.
 */

public class CustomButton extends Button {

    private RouteWise mInstance;
    private int lineNumber;
    private RouteLog logger;

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public CustomButton(Context context) {
        super(context);
        init(context, null);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(CustomButton.class);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.FontText);
        String customFont = a.getString(R.styleable.FontText_face);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf = null;
        try {
            //tf = Typeface.createFromAsset(ctx.getAssets(), asset);
            tf = TypeFaceProvider.getTypeFace(ctx, asset);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error( "Error at " + lineNumber, e);
            if (asset != null)
                logger.info("Font Not Found " + asset);
            return false;
        }

        setTypeface(tf);
        return true;
    }
}
