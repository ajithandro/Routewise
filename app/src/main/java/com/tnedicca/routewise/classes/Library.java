package com.tnedicca.routewise.classes;

import com.tnedicca.routewise.activities.RouteWise;


import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by Aachu on 07-01-2017.
 */
public class Library {

    private static final RouteWise mInstance = RouteWise.getInstance();

    public static String getReplaceString(String string, boolean rest) {
        if (string != null) {
            string = string.replaceAll(AppConstant.ORIGINAL_COMMA, AppConstant.REPLACE_COMMA);
            string = string.replaceAll(AppConstant.UNDO_LINEBREAK, AppConstant.REPLACE_LINEBREAK);
            string = string.replaceAll(AppConstant.ORIGINAL_QUOTES, AppConstant.ORIGINAL_QUOTES_STRING);
            if (rest) {
                return decodeString(string);
            } else
                return string;
        }
        return string;
    }

    public static String getOriginalString(String string, boolean rest) {
        if (string != null) {
            string = string.replaceAll(AppConstant.REPLACE_COMMA, AppConstant.ORIGINAL_COMMA);
            string = string.replaceAll(AppConstant.UNDO_LINEBREAK, AppConstant.ORIGINAL_LINEBREAK);
            string = string.replaceAll(AppConstant.REPLACE_LINEBREAK, AppConstant.ORIGINAL_LINEBREAK);
            string = string.replaceAll(AppConstant.ORIGINAL_QUOTES_STRING, AppConstant.ORIGINAL_QUOTES);
            if (rest) {
                return encodeString(string);
            } else
                return string;
        }
        return string;
    }

    public static String encodeString(String string) {
        String encodedString = null;
        try {
            encodedString = URLEncoder.encode(string, "UTF-8");
            encodedString = encodedString.replaceAll(AppConstant.ENCODE_ORIGINAL_PLUS, AppConstant.ENCODE_PLUS);
        } catch (Exception e) {
            e.printStackTrace();
            return string;
        }
        return encodedString;
    }

    public static String decodeString(String string) {
        String decodedString = null;
        try {
            decodedString = URLDecoder.decode(string, "UTF-8");
            decodedString = decodedString.replaceAll(AppConstant.ENCODE_PLUS, AppConstant.ENCODE_ORIGINAL_PLUS);
        } catch (Exception e) {
            e.printStackTrace();
            return string;
        }
        return decodedString;
    }
}