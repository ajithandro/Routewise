package com.tnedicca.routewise.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.util.AttributeSet;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.classes.TypeFaceProvider;

/**
 * Created by Aachu on 30-01-2017.
 */
public class CustomRadioButton extends AppCompatRadioButton {

    private Context context;
    private RouteWise mInstance;
    private AttributeSet attrs;
    private int defStyle;
    private int lineNumber;
    private RouteLog logger;

    public CustomRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.attrs = attrs;
        this.defStyle = defStyle;
        init();
    }

    public CustomRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        init();
    }

    public CustomRadioButton(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(CustomRadioButton.class);
        setCustomFont(context, attrs);
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
