package com.tnedicca.routewise.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.TypeFaceProvider;

/**
 * Created by Aachu on 22-03-2017.
 */

public class CustomWebView extends WebView {
    private RouteWise mInstance;
    private int lineNumber;
    private boolean justify;
    private String fontFace;
    private float fontSize;
    private String fontColor;

    public CustomWebView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mInstance = RouteWise.getInstance();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontText);
        fontFace = a.getString(R.styleable.FontText_face);
        String textSize = a.getString(R.styleable.FontText_textSize);
        justify = a.getBoolean(R.styleable.FontText_justify, false);
        String color = a.getString(R.styleable.FontText_textColor);

        if (color != null && color.length() > 3)
            fontColor = color.substring(3);
        fontSize = 10;
        try {
            if (textSize.length() > 2) {
                fontSize = Float.parseFloat(textSize.substring(0, textSize.length() - 2));
            } else {
                fontSize = Float.parseFloat(textSize);
            }
        } catch (Exception e) {
        }
    }

    public void setText(String text) {
        String style = "<style type=\"text/css\">";
        if (fontFace != null)
            style = style + "@font-face {font-family: MyFont; src: url(\"file:///android_asset/" + fontFace + "\")}body {font-family: MyFont;";
        else
            style = style + "body {";

        if (justify)
            style = style + "text-align: justify;";
//        style = style + "text-align: justify; word-break: break-all;";

        if (fontColor != null)
            style = style + "color: #" + fontColor + ";";

        style = style + "font-size: " + fontSize + ";}";
        String pish = "<html><head>" + style + "</style></head><body>";

        String pas = "</body></html>";
        text = text.replaceAll("\\n", "<br/>");
        String myHtmlString = pish + text + pas;
        loadDataWithBaseURL(null, myHtmlString, "text/html", "UTF-8", null);
    }
}
