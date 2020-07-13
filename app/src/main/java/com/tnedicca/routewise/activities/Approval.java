package com.tnedicca.routewise.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.CustomEditTextView;
import com.tnedicca.routewise.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vishal on 20-01-2017.
 */

public class Approval extends AppCompatActivity implements TextWatcher, View.OnClickListener {

    private RouteWise mInstance;
    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private CustomEditTextView codeEditTextView;
    private CustomTextView incorrectCodeTextView;
    private CustomButton codeRequestButton;
    private String mEmail;
    private ProgressDialog progressDialog;
    private String token;
    private String activationCode;
    private boolean keepMeLoggedIn;
    ArrayList<String> volleyArray = new ArrayList<String>();
    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;
    private Bundle intent;
    private String mailType;
    private boolean mResetScreen;
    private String resetId;
    private boolean isEmail;
    private boolean mResetSettings;
    private int lineNumber;
    private RouteLog logger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.approval);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Approval.class);

        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.verify_identity));
        infoIcon.setVisibility(View.INVISIBLE);
//        backIcon.setOnClickListener(this);

        init();
        if (!BuildConfig.RELEASE) {
//            codeEditTextView.setCustomText(activationCode);
        }
    }

    private void sendVolleyRequest(String requestType) {
        int method;
        String url;
        String tag;
        if (requestType.equals(AppConstant.ACTIVATION)) {
            url = BuildConfig.REST_URL + AppConstant.ACTIVATE_URL;
            tag = AppConstant.ACTIVATION;
            method = Request.Method.POST;
            progressDialog.setMessage(AppConstant.ACTIVATING_MSG);
        } else {
            url = BuildConfig.REST_URL + AppConstant.FORGOT_PASSWORD_URL;
            tag = AppConstant.FORGOT_PASSWORD;
            method = Request.Method.GET;
            progressDialog.setMessage(AppConstant.SENDING_CODE);
        }
        progressDialog.setIndeterminate(true);
        if (mInstance.dataStatus) {
            progressDialog.show();
            makeJsonArryReq(url, method, tag, requestType);
        } else {
            progressDialog.dismiss();
            mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    private void init() {
        mInstance = RouteWise.getInstance();
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        intent = getIntent().getExtras();

        mEmail = intent.getString(AppConstant.USER);
        token = intent.getString(AppConstant.TOKEN);
        activationCode = intent.getString(AppConstant.ACTIVATION_CODE);
        keepMeLoggedIn = intent.getBoolean(AppConstant.KEEP_ME_LOGGED_IN, false);
        mailType = intent.getString(AppConstant.MAIL_TYPE, null);
        mResetScreen = intent.getBoolean(AppConstant.RESET_SCREEN, false);
        mResetSettings = intent.getBoolean(AppConstant.RESET_SETTINGS, false);
        resetId = intent.getString(AppConstant.RESET_ID);
        isEmail = intent.getBoolean(AppConstant.LOGIN_TYPE, false);

        codeEditTextView = findViewById(R.id.codeEditText);
        codeEditTextView.addTextChangedListener(this);
        incorrectCodeTextView = findViewById(R.id.inCorrectCodeTextView);
        codeRequestButton = findViewById(R.id.code_request_button);
        incorrectCodeTextView.setVisibility(View.INVISIBLE);
        codeRequestButton.setVisibility(View.INVISIBLE);
        codeRequestButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(Approval.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final String queryType) {
        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                volleyArray.remove(tag);
//                logger.info( getString(R.string.received_response));
                response(tag, response, 200, queryType);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyArray.remove(tag);
                logger.info( getString(R.string.received_error));
                error(tag, error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                if (queryType.equals(AppConstant.ACTIVATION)) {
                    headers.put(AppConstant.REST_ACCESS_TOKEN, token);
                } else if (queryType.equals(AppConstant.FORGOT_PASSWORD)) {
                    JSONObject tempjson = new JSONObject();
                    try {
                        tempjson.put(AppConstant.EMAIL_ID, resetId);
                        tempjson.put(AppConstant.LOGIN_TYPE_EMAIL, isEmail);
                        tempjson.put(AppConstant.MAIL_TYPE, mailType);
                    } catch (JSONException e) {
                        lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                        logger.error("Error at " + lineNumber, e);
                    }
                    headers.put(AppConstant.REST_ID, mInstance.myLibrary.dataEncrypt(tempjson.toString()));
                }
                return headers;
            }

            @Override
            public int getMethod() {
                return method;
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                String jsonString = null;
                try {
                    jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    JSONArray jsonArray = new JSONArray(jsonString);
                    jsonArray.put(response.statusCode);
                    return Response.success(jsonArray, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                    logger.error("Error at " + lineNumber, e);
                    return Response.error(new ParseError(e));
                } catch (JSONException e) {
                    lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                    logger.error("Error at " + lineNumber, e);
                    return Response.error(new ParseError(e));
                }
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(AppConstant.REST_RETRY_MILLISECONDS, AppConstant.REST_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        RouteWise.getInstance().addToRequestQueue(req, tag);
        volleyArray.add(tag);
    }

    //handles different response codes from Volley
    private void response(String check, JSONArray response, int code, String queryType) {
        mInstance.myLibrary.checkResponse(response, code, this);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            progressDialog.dismiss();
            mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            // handling response for submitting registration data
            if (queryType.equals(AppConstant.ACTIVATION)) {
                JSONObject result = response.optJSONObject(0);
                int status = response.optInt(1);
                String message = result.optString("message");
                if (status == 200) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + " " + getString(R.string.log_message) + " " + getString(R.string.registration_submit_succes) + " " + mEmail);
                } else {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + " for " + mEmail);
                }
                if (check.equals(AppConstant.ACTIVATION)) {
                    displayServerMsgActivation(result, status);
                }
            }
            // handling response for new authorization code request
            else if (queryType.equals(AppConstant.FORGOT_PASSWORD)) {
                JSONObject result = response.optJSONObject(0);
                int status = response.optInt(1);
                String message = result.optString("message");
                if (status == 200) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.log_message) + " for : " + mEmail);
                } else if (status == 202) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.user_not_available) + " for : " + mEmail);
                } else if (status == 203) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.phone_number_not_available));
                }
                if (check.equals(AppConstant.FORGOT_PASSWORD)) {
                    displayServerMsgForgotPassword(result, status);
                }
            }
        }
    }

    // display server message
    private void displayServerMsgActivation(JSONObject result, int code) {
        progressDialog.dismiss();
        switch (code) {
            case AppConstant.RESPONSE_SUCCESS:
            case AppConstant.RESPONSE_GOOGLE_ANONYMUS:
                displayDashboard(result);
                break;
            default:
                logger.info("default executed for displayServerMsgActivation for response code : " + code);
                break;
        }
    }

    // display server message
    private void displayServerMsgForgotPassword(JSONObject result, int code) {
        progressDialog.dismiss();
        switch (code) {
            case AppConstant.RESPONSE_SUCCESS:
            case AppConstant.RESPONSE_GOOGLE_ANONYMUS:
                activationCode = result.optString(AppConstant.ACTIVATION_CODE);
                incorrectCodeTextView.setVisibility(View.INVISIBLE);
                codeRequestButton.setVisibility(View.INVISIBLE);
                break;
            case AppConstant.RESPONSE_USER_NOT_AVAILABLE:
                //String email = result.optString("")
                mInstance.myLibrary.DisplayToast(this, getString(R.string.user_not_available), Toast.LENGTH_SHORT, Gravity.CENTER);
                break;
            case AppConstant.RESPOMSE_PHONE_NUMBER_NOT_AVAILABLE:
                mInstance.myLibrary.DisplayToast(this, getString(R.string.phone_number_not_available), Toast.LENGTH_SHORT, Gravity.CENTER);
                break;
            default:
                logger.info("default executed for displayServerMsgForgotPassword for response code : " + code);
                break;
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error) {
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode, "");
            } else {
                mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    private void displayDashboard(JSONObject result) {
        mInstance.subscribed = true;
        mInstance.tracking_enabled = true;
        mInstance.auto_enable = false;
        edit = mPrefer.edit();
        edit.putString(AppConstant.USER, mEmail);
        edit.putString(AppConstant.TOKEN, token);
        edit.putBoolean(AppConstant.SUBSCRIBED, mInstance.subscribed);
        edit.putBoolean(AppConstant.TRACKING_ENABLED, mInstance.tracking_enabled);
        edit.putBoolean(AppConstant.AUTO_TRACKING, mInstance.auto_enable);
        edit.putBoolean(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
        edit.commit();
        mInstance.myLibrary.getDBAdapter();
        Intent intent = new Intent(Approval.this, MainMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.hashCode() == codeEditTextView.getText().hashCode()) {
            if (codeEditTextView.getText().length() == 6) {
                mInstance.myLibrary.hideKeyBoard(this.getCurrentFocus(), getApplicationContext());
                String enteredCode = codeEditTextView.getCustomText();
                if (enteredCode.equals(activationCode)) {
                    if (mResetScreen) {
                        Intent intent = new Intent(Approval.this, Password.class);
                        intent.putExtra(AppConstant.USER, mEmail);
                        intent.putExtra(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
                        intent.putExtra(AppConstant.RESET_SETTINGS, mResetSettings);
                        startActivity(intent);
                        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                    } else
                        sendVolleyRequest(AppConstant.ACTIVATION);
                } else {
                    incorrectCodeTextView.setVisibility(View.VISIBLE);
                    codeRequestButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
//        if (mResetScreen) {
//            super.onBackPressed();
//        } else {
        // return to the calling activity on back pressed
        Intent returnIntent = new Intent(Approval.this, MainActivity.class);
        returnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(returnIntent);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                onBackPressed();
                break;
            case R.id.code_request_button:
                codeEditTextView.setText("");
                sendVolleyRequest(AppConstant.FORGOT_PASSWORD);
                break;
            default:
                logger.info("default statement executed for onClick for view id : " + v.getId());
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!volleyArray.isEmpty()) {
            for (int i = 0; i < volleyArray.size(); i++) {
                mInstance.getRequestQueue().cancelAll(volleyArray.get(i));
            }
        }
    }
}
