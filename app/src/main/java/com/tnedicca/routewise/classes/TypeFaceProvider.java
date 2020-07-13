package com.tnedicca.routewise.classes;

import android.content.Context;
import android.graphics.Typeface;


import java.util.Hashtable;

/**
 * Created by new on 16-02-2017.
 */

public class TypeFaceProvider {
    private static Hashtable<String, Typeface> sTypeFaces = new Hashtable<String, Typeface>(4);

    public static Typeface getTypeFace(Context context, String fileName) {
        try {
            Typeface tempTypeface = sTypeFaces.get(fileName);
            if (tempTypeface == null) {
                tempTypeface = Typeface.createFromAsset(context.getAssets(), fileName);
                sTypeFaces.put(fileName, tempTypeface);
            }
            return tempTypeface;
        } catch (Exception e) {
            return null;
        }
    }
}
