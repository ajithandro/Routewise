package com.tnedicca.routewise.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.Library;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.classes.TypeFaceProvider;

/**
 * Created by new on 18-01-2017.
 */

public class CustomAutoCompleteTextView extends AutoCompleteTextView {

    private RouteWise mInstance;
    private int lineNumber;
    private RouteLog logger;

    public CustomAutoCompleteTextView(Context context) {
        super(context);
    }

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.FontText);
        String customFont = a.getString(R.styleable.FontText_face);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    private void init(Context context, AttributeSet attrs) {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(CustomAutoCompleteTextView.class);
        setCustomFont(context, attrs);
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf = null;
        try {
            //tf = Typeface.createFromAsset(ctx.getAssets(), asset);
            tf = TypeFaceProvider.getTypeFace(ctx, asset);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
            if (asset != null)
                logger.info("Font Not Found " + asset);
            return false;
        }
        setTypeface(tf);
        return true;
    }

    public String getCustomText() {
        Editable text = super.getText();
        boolean check = false;
        boolean show = false;
        String temp_text = null;
        if (text != null)
            check = true;
        if (check) {
            temp_text = text.toString();
            if (!temp_text.equals("")) {
                temp_text = Library.getReplaceString(temp_text, false);
                show = true;
            }
        }
        if (show) {
            return temp_text;
        } else {
            return super.getText().toString();
        }
    }

    public void setCustomText(String text) {
        boolean check = false;
        boolean show = false;
        if (text != null && !text.equals(""))
            check = true;
        if (check) {
            text = text.replaceAll(AppConstant.REPLACE_COMMA, AppConstant.ORIGINAL_COMMA);
            text = text.replaceAll(AppConstant.UNDO_LINEBREAK, AppConstant.ORIGINAL_LINEBREAK);
            text = text.replaceAll(AppConstant.REPLACE_LINEBREAK, AppConstant.ORIGINAL_LINEBREAK);
            text = text.replaceAll(AppConstant.ORIGINAL_QUOTES_STRING, AppConstant.ORIGINAL_QUOTES);
            show = true;
        }
        if (show) {
            super.setText(text);
        } else {
            super.setText(text);
        }
    }
}
