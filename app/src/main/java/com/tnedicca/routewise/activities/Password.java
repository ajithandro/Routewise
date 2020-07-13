package com.tnedicca.routewise.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.tnedicca.routewise.fragments.FragmentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aachu on 31-01-2017.
 */
@SuppressWarnings({"ResourceType"})
public class Password extends Activity implements View.OnClickListener, View.OnFocusChangeListener {

    public SharedPreferences mPrefer;
    ArrayList<String> volleyArray = new ArrayList<String>();
    private RouteWise mInstance;
    private SharedPreferences.Editor edit;
    private String mCurrentUser;
    private boolean keepMeLoggedIn;
    private boolean mResetSettings;
    private boolean mOldStatus = false;
    private boolean mNewStatus = false;
    private boolean mConfStatus = false;
    private String mOldPass = "";
    private String mNewPass = "";
    private String mConfPass = "";
    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;
    private FragmentView mOldPassView;
    private FragmentView mNewPassView;
    private FragmentView mConfPassView;
    private CustomEditTextView mOldText;
    private CustomEditTextView mNewText;
    private CustomEditTextView mConfText;
    private CustomTextView mOldError;
    private CustomTextView mNewError;
    private CustomTextView mConfError;
    private ImageView mOldImage;
    private ImageView mNewImage;
    private ImageView mConfImage;
    private CustomButton mSubmit;
    private int lineNumber;
    private ProgressDialog progressDialog;
    private RouteLog logger;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Password.class);

        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        Bundle intent = getIntent().getExtras();

        mCurrentUser = intent.getString(AppConstant.USER);
        keepMeLoggedIn = intent.getBoolean(AppConstant.KEEP_ME_LOGGED_IN, false);
        mResetSettings = intent.getBoolean(AppConstant.RESET_SETTINGS, false);
        progressDialog = new ProgressDialog(this);

        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        mOldPassView = (FragmentView) getFragmentManager().findFragmentById(R.id.old_password);
        mNewPassView = (FragmentView) getFragmentManager().findFragmentById(R.id.new_password);
        mConfPassView = (FragmentView) getFragmentManager().findFragmentById(R.id.confirm_password);
        mOldText = mOldPassView.getView().findViewById(R.id.edit_text);
        mNewText = mNewPassView.getView().findViewById(R.id.edit_text);
        mConfText = mConfPassView.getView().findViewById(R.id.edit_text);
        mOldError = mOldPassView.getView().findViewById(R.id.error_msg);
        mNewError = mNewPassView.getView().findViewById(R.id.error_msg);
        mConfError = mConfPassView.getView().findViewById(R.id.error_msg);
        mOldImage = mOldPassView.getView().findViewById(R.id.validation_image);
        mNewImage = mNewPassView.getView().findViewById(R.id.validation_image);
        mConfImage = mConfPassView.getView().findViewById(R.id.validation_image);
        mSubmit = findViewById(R.id.next_button);

        screenTitle.setCustomText(getString(R.string.new_password));
        infoIcon.setVisibility(View.INVISIBLE);
//        backIcon.setOnClickListener(this);
        mSubmit.setOnClickListener(this);

        mOldText.setHint(R.string.old_password_text);
        mNewText.setHint(R.string.new_password_text);
        mConfText.setHint(R.string.confirm_password);
        mOldError.setCustomText("");
        mNewError.setCustomText("");
        mConfError.setCustomText("");
        mOldImage.setImageDrawable(null);
        mNewImage.setImageDrawable(null);
        mConfImage.setImageDrawable(null);
        mOldText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mNewText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mConfText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mOldText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mNewText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mConfText.setImeOptions(EditorInfo.IME_ACTION_GO);
        mOldText.setCustomFont(this, getString(R.string.font_malgun_semi_light));
        mNewText.setCustomFont(this, getString(R.string.font_malgun_semi_light));
        mConfText.setCustomFont(this, getString(R.string.font_malgun_semi_light));
        if (!mResetSettings) {
            mOldPassView.getView().setVisibility(View.GONE);
            LinearLayout.LayoutParams temp = (LinearLayout.LayoutParams) mOldPassView.getView().getLayoutParams();
            int margin = temp.topMargin;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mNewPassView.getView().getLayoutParams();
            params.setMargins(0, margin, 0, 0);
            mNewPassView.getView().setLayoutParams(params);

            mNewText.requestFocus();
        } else {
            mOldText.requestFocus();
            mOldText.setId(103);
            mOldText.setOnFocusChangeListener(this);
        }
        mNewText.setId(101);
        mConfText.setId(102);
        mNewText.setOnFocusChangeListener(this);
        mConfText.setOnFocusChangeListener(this);
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

    private void submit() {
        boolean send = false;
        if (!mResetSettings) {
            send = mNewStatus && mConfStatus;
        } else {
            send = mOldStatus && mNewStatus && mConfStatus;
        }
        if (send)
            buildRequest(false);
    }

    private void buildRequest(boolean change) {
        JSONObject tempjson = new JSONObject();
        try {
            String device_id = mInstance.myLibrary.getUniqueId(this);
            tempjson.put(AppConstant.EMAIL, mCurrentUser);
            if (mResetSettings) {
                tempjson.put(AppConstant.REGISTER_OLD_PASSWORD, mInstance.myLibrary.passwordEncrypter(mOldPass));
            }
            tempjson.put(AppConstant.PASSWORD, mInstance.myLibrary.passwordEncrypter(mNewPass));
            tempjson.put(AppConstant.DEVICE_ID, device_id);
            tempjson.put(AppConstant.CHANGE_DEVICE_ID, change);
            tempjson.put(AppConstant.FORGOT_TYPE, mResetSettings);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
        }
        String id = mInstance.myLibrary.dataEncrypt(tempjson.toString());

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(AppConstant.LOADING);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String url = BuildConfig.REST_URL + AppConstant.FORGOT_PASSWORD_URL;
        int method = Request.Method.POST;
        String tag = AppConstant.RESET_PASS;
        makeJsonArryReq(url, method, tag, id);
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final String id) {
        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                volleyArray.remove(tag);
//                logger.info(getString(R.string.received_response));
                response(tag, response, 200);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyArray.remove(tag);
                logger.info(getString(R.string.received_error));
                error(tag, error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                if (tag.equals(AppConstant.RESET_PASS))
                    headers.put(AppConstant.REST_ID, id);
                return headers;
            }

            @Override
            public int getMethod() {
                return method;
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                String jsonString = null;
                JSONArray jsonArray = new JSONArray();
                try {
                    jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    if (jsonString.isEmpty()) {
                        jsonArray.put("");
                    } else {
                        jsonArray = new JSONArray(jsonString);
                    }
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
    private void response(String check, JSONArray response, int code) {
        progressDialog.dismiss();
        mInstance.myLibrary.checkResponse(response, code, this);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else if (code == AppConstant.RESPONSE_DEVICE_CHANGED) {
            mInstance.myLibrary.alertBox(this, "Register Device", getString(R.string.new_device_message), "AGREE", "DISAGREE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logger.info("Device Changed");
                    buildRequest(true);
                }
            });
        } else {
            JSONObject result = response.optJSONObject(0);
            int status = response.optInt(1);
            if (result != null) {
                String message = result.optString("message");
            }
            if (status == AppConstant.RESPONSE_NO_CHANGE) {
                mInstance.myLibrary.DisplayToast(this, "Nothing to Change", Toast.LENGTH_SHORT, Gravity.CENTER);
                mNewError.setCustomText("Enter different Password");
                mNewImage.setImageResource(R.drawable.ic_erroricon);
            } else if (status == AppConstant.RESPONSE_NO_USER) {
                mInstance.myLibrary.DisplayToast(this, "User not registered", Toast.LENGTH_SHORT, Gravity.CENTER);
            } else if (status == AppConstant.RESPONSE_SUCCESS || status ==  AppConstant.RESPONSE_GOOGLE_ANONYMUS) {
                if (check.equals(AppConstant.RESET_PASS)) {
                    mCurrentUser = result.optString(AppConstant.EMAIL);

                    mInstance.myLibrary.DisplayToast(this, "Password Updated", Toast.LENGTH_SHORT, Gravity.CENTER);
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    }, 1000);
                }
            } else if (status == AppConstant.RESPONSE_PASSWORD_VALIDATION_FAILED) {
                mOldError.setCustomText(getString(R.string.wrong_password));
                mOldImage.setImageResource(R.drawable.ic_erroricon);
            }
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error) {
        progressDialog.dismiss();
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode);
            } else {
                mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    @Override
    protected void onPause() {
        if(handler != null)
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    private void validateAllFields(boolean all, int id) {
        mOldPass = mOldText.getCustomText();
        mNewPass = mNewText.getCustomText();
        mConfPass = mConfText.getCustomText();
        if (all || id == 101) {
            if (mNewPass.isEmpty()) {
                mNewError.setCustomText(getString(R.string.blank_password_msg));
                mNewImage.setImageResource(R.drawable.ic_erroricon);
            } else {
                mNewStatus = mInstance.myLibrary.isValidPasswordFormat(mNewPass);
                if (!mNewStatus) {
                    mNewError.setCustomText(getString(R.string.invalid_password_msg));
                    mNewImage.setImageResource(R.drawable.ic_erroricon);
                } else {
                    mNewError.setCustomText("");
                    mNewImage.setImageResource(R.drawable.ic_noerroricon);
                    if (!mConfPass.isEmpty()) {
                        if (mNewPass.equals(mConfPass)) {
                            mConfStatus = true;
                            mConfError.setCustomText("");
                            mConfImage.setImageResource(R.drawable.ic_noerroricon);
                        } else {
                            mConfStatus = false;
                            mConfError.setCustomText("Password not matching");
                            mConfImage.setImageResource(R.drawable.ic_erroricon);
                        }
                    }
                }
            }
        }
        if (all || id == 102) {
            if (mConfPass.isEmpty()) {
                mConfError.setCustomText(getString(R.string.blank_confirm_password_msg));
                mConfImage.setImageResource(R.drawable.ic_erroricon);
            } else {
                boolean temp = mInstance.myLibrary.isValidPasswordFormat(mConfPass);
                if (!temp) {
                    mConfError.setCustomText(getString(R.string.invalid_password_msg));
                    mConfImage.setImageResource(R.drawable.ic_erroricon);
                } else {
                    if (mNewPass.equals(mConfPass)) {
                        mConfStatus = true;
                        mConfError.setCustomText("");
                        mConfImage.setImageResource(R.drawable.ic_noerroricon);
                    } else {
                        mConfStatus = false;
                        mConfError.setCustomText("Password not matching");
                        mConfImage.setImageResource(R.drawable.ic_erroricon);
                    }
                }
            }
        }
        if (all || id == 103) {
            if (mOldPass.isEmpty()) {
                mOldError.setCustomText(getString(R.string.blank_old_password_msg));
                mOldImage.setImageResource(R.drawable.ic_erroricon);
            } else {
                boolean temp = mInstance.myLibrary.isValidPasswordFormat(mOldPass);
                if (!temp) {
                    mOldError.setCustomText(getString(R.string.invalid_password_msg));
                    mOldImage.setImageResource(R.drawable.ic_erroricon);
                } else {
                    mOldStatus = true;
                    mOldError.setCustomText("");
                    mOldImage.setImageResource(R.drawable.ic_noerroricon);
                }
            }
        }
        if (all) {
            if (!mOldPass.isEmpty() && !mNewPass.isEmpty()) {
                if (mOldPass.equals(mNewPass)) {
                    mNewStatus = false;
                    mNewError.setCustomText("Enter different Password");
                    mNewImage.setImageResource(R.drawable.ic_erroricon);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mResetSettings) {
            super.onBackPressed();
        } else {
            Intent returnIntent = new Intent(this, MainActivity.class);
            returnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(returnIntent);
        }
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_layout:
                onBackPressed();
                break;
            case R.id.next_button:
                validateAllFields(true, 0);
                submit();
                break;
            default:
                logger.info("default statement executed for onClick for view id : " + id);
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            int id = v.getId();
            switch (id) {
                case 101:
                    validateAllFields(false, 101);
                    break;
                case 102:
                    validateAllFields(false, 102);
                    break;
                case 103:
                    validateAllFields(false, 103);
                default:
                    logger.info("default statement executed for onClick for view id : " + id);
                    break;
            }
        }
    }
}
