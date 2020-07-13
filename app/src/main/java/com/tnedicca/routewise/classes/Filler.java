package com.tnedicca.routewise.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;
import com.tnedicca.routewise.activities.RouteWise;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Filler {

    private final RouteWise mInstance;
    private final RouteLog logger;
    private Context mContext;

    public Filler(Context context) {
        this.mContext = context;
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(MyLibrary.class);
    }

    public void showSnackBar(String message, final View button, int length) {
        if (mContext instanceof Activity) {
            View view = ((Activity) mContext).findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(view, message, length);
            if (button != null && button instanceof Button) {
                snackbar.setAction(AppConstant.RETRY, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        button.performClick();
                    }
                });
            }
            snackbar.show();
        }
    }

    public void showSnackBar(String message) {
        if (mContext instanceof Activity) {
            View view = ((Activity) mContext).findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    public void showSnackBar(@StringRes int resId, int duration) {
        if (mContext instanceof Activity) {
            View view = ((Activity) mContext).findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(view, resId, duration);
            snackbar.show();
        }
    }

    public void showSnackBar(@StringRes int resId) {
        if (mContext instanceof Activity) {
            View view = ((Activity) mContext).findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(view, resId, Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    public JSONObject getnetwork(Context context) {
        JSONObject result = new JSONObject();
        try {
            if (context == null) {
                result.put(AppConstant.NETWORK_RESPONSE_AVAILABLE, false);
            }

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                    if (capabilities != null) {
                        result.put(AppConstant.NETWORK_RESPONSE_AVAILABLE, true);
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            result.put(AppConstant.MOBILE, true);
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            result.put(AppConstant.WIFI, true);
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            result.put(AppConstant.ETHERNET, true);
                        }
                    } else {
                        result.put(AppConstant.NETWORK_RESPONSE_AVAILABLE, false);
                    }
                } else {
                    try {
                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                            result.put(AppConstant.NETWORK_RESPONSE_AVAILABLE, true);
                            boolean check = activeNetworkInfo.isConnected();
                            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                                result.put(AppConstant.WIFI, true);
                            } else {
                                result.put(AppConstant.MOBILE, true);
                            }
                        } else {
                            result.put(AppConstant.NETWORK_RESPONSE_AVAILABLE, false);
                        }
                    } catch (Exception e) {
                        Log.i("update_statut", "" + e.getMessage());
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}