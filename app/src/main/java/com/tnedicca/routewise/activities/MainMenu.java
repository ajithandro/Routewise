package com.tnedicca.routewise.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.LayoutParams;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.Filler;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.classes.VolleyMultipartRequest;
import com.tnedicca.routewise.classes.ZipManager;
import com.tnedicca.routewise.fragments.Dashboard;
import com.tnedicca.routewise.fragments.Help;
import com.tnedicca.routewise.fragments.InsuranceQuote;
import com.tnedicca.routewise.fragments.MyTripsList;
import com.tnedicca.routewise.fragments.PlanTrip;
import com.tnedicca.routewise.fragments.Settings;
import com.tnedicca.routewise.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aachu on 23-01-2017.
 */
public class MainMenu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, DrawerLayout.DrawerListener {

    public SharedPreferences mPrefer;
    public CustomTextView screenTitle;
    public ImageView infoIcon;
    public int previousMenuId = 0;
    ArrayList<String> volleyArray = new ArrayList<>();
    int lineNumber;
    private Editor edit;
    private ImageView menuIcon;
    private RouteWise mInstance;
    private DrawerLayout menuDrawer;
    private NavigationView navigationView;
    private int mScreenDensity;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mSdk;
    private RelativeLayout mainView;
    private ProgressDialog progressDialog;
    private String mCurrentUser;
    private RouteLog logger;
    private Filler mFiller;
    String[] zipName = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(MainMenu.class);

        mFiller = new Filler(this);
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        mCurrentUser = mPrefer.getString(AppConstant.USER, null);
        progressDialog = new ProgressDialog(MainMenu.this);

        screenTitle = findViewById(R.id.action_bar_title);
        menuIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);
        RelativeLayout infoLayout = findViewById(R.id.info_layout);
        infoLayout.setOnClickListener(this);

        menuDrawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        mainView = findViewById(R.id.main_view);

        screenTitle.setCustomText(getString(R.string.title_dashboard));
        menuIcon.setImageResource(R.drawable.ic_menuicon);
        navigationView.setNavigationItemSelectedListener(this);
        menuDrawer.addDrawerListener(this);

        mSdk = mPrefer.getInt(AppConstant.SDK, 0);
        mScreenDensity = mPrefer.getInt(AppConstant.SCREEN_DENSITY, 0);
        mScreenWidth = mPrefer.getInt(AppConstant.SCREEN_WIDTH, 0);
        mScreenHeight = mPrefer.getInt(AppConstant.SCREEN_HEIGHT, 0);

        LayoutParams params = (LayoutParams) navigationView.getLayoutParams();
        params.width = mScreenWidth / 2;
        navigationView.setLayoutParams(params);

        displaySelectedScreen(R.id.menu_dashboard);
    }

    @Override
    public void onBackPressed() {
        logger.info("Returned back from Dashboard.");
        if (menuDrawer.isDrawerOpen(GravityCompat.START)) {
            menuDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void displaySelectedScreen(int itemId) {
        if (itemId != previousMenuId) {
            //creating fragment object
            Fragment fragment = null;
            //initializing the fragment object which is selected
            switch (itemId) {
                case R.id.menu_dashboard:
                    infoIcon.setVisibility(View.VISIBLE);
                    screenTitle.setCustomText(getString(R.string.title_dashboard));
                    fragment = new Dashboard();
                    break;
                case R.id.menu_my_trips:
                    infoIcon.setVisibility(View.INVISIBLE);
                    screenTitle.setCustomText(getString(R.string.my_trips_title));
                    mInstance.getTripList();
                    fragment = new MyTripsList();
                    break;
                case R.id.menu_plan_trip:
                    infoIcon.setVisibility(View.INVISIBLE);
                    screenTitle.setCustomText(getString(R.string.title_plan_trip));
                    fragment = new PlanTrip();
                    break;
                case R.id.menu_insurance:
                    //Added by Vishal
                    infoIcon.setVisibility(View.INVISIBLE);
                    screenTitle.setCustomText(getString(R.string.title_insurance_quote));
                    fragment = new InsuranceQuote();
                    break;
                case R.id.menu_settings:
                    infoIcon.setVisibility(View.INVISIBLE);
                    screenTitle.setCustomText(getString(R.string.settings_title));
                    fragment = new Settings();
                    break;
                case R.id.menu_help:
                    infoIcon.setVisibility(View.INVISIBLE);
                    screenTitle.setCustomText(getString(R.string.title_help));
                    fragment = new Help();
                    break;
                case R.id.menu_send_logs:
                    showSendLogAlert();
                    break;
                case R.id.menu_logout:
                    logout();
                    break;
                case R.id.menu_delete:
                    delete();
                    break;
                default:
                    logger.info(getString(R.string.onclick_default) + itemId);
                    break;
            }

            //replacing the fragment
            if (fragment != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        }else{
            switch (itemId) {
                case R.id.menu_my_trips:
                    mInstance.getTripList();
                    break;
                default:
                    logger.info(getString(R.string.onclick_default) + itemId);
                    break;
            }

        }
        if (itemId != R.id.menu_logout && itemId != R.id.menu_delete && itemId != R.id.menu_send_logs)
            previousMenuId = itemId;
        menuDrawer.closeDrawer(GravityCompat.START);
    }

    private void showInfo() {
        Intent intent = new Intent(this, Info.class);
        startActivity(intent);
        // Created animation for sliding to next activity
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    private void showSendLogAlert() {
        logger.info("Sending Log Initiated");
        mInstance.myLibrary.alertBox(this, "REPORTING", getString(R.string.send_log_message), "SEND", "CANCEL", (dialog, which) -> {
            progressDialog.setMessage(AppConstant.ZIPPING_MSG);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            sendLogs();
        });
    }

    private void sendLogs() {
        String url = BuildConfig.REST_URL + AppConstant.REPORT_BATTERY_URL;
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    try {
                        stopAndDelete();
                        JSONObject obj = new JSONObject(new String(response.data));
                        boolean result = obj.getBoolean("result");
                        if (result) {
                            mFiller.showSnackBar("Log Sent");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    stopAndDelete();
                    mFiller.showSnackBar("Log Sending Failed : " + error.getMessage());
                }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    String dbFolder = AppConstant.KEY_APP_FOLDER+"/databases";
                    String date = mInstance.zipFileDateFormat.format(new Date());
                    mCurrentUser = mPrefer.getString(AppConstant.USER, null);
                    String fileName = mCurrentUser + "_" + date;
                    zipName[0] = AppConstant.KEY_APP_FOLDER +"/"+ fileName + ".zip";
                    ZipManager zipManager = new ZipManager();
//                    zipManager.zipFolder(inputPath, zipName);
                    zipManager.compressDirectory(new String[]{AppConstant.KEY_LOG_FILE_PATH, dbFolder}, zipName[0]);

                    progressDialog.setMessage(AppConstant.UPLOADING_MSG);
                    params.put("uploadedFile", new DataPart(zipName[0]));
                } catch (Exception e) {
                    stopAndDelete();
                    e.printStackTrace();
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                headers.put(AppConstant.REST_ACCESS_TOKEN, mPrefer.getString(AppConstant.TOKEN, ""));
                return headers;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    private void stopAndDelete() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            File file = new File(zipName[0]);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void logout() {
        logger.info("Logout Initiated");
        mInstance.myLibrary.alertBox(this, "LOGOUT", getString(R.string.logout_message), "LOGOUT", "NO", (dialog, which) -> {
            mInstance.myLibrary.logout();
            showLogin();
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(item.getItemId());
        //make this method blank
        return true;
    }

    private void changeDrawerView() {
        if (menuDrawer.isDrawerOpen(GravityCompat.START))
            menuDrawer.closeDrawer(GravityCompat.START);
        else
            menuDrawer.openDrawer(GravityCompat.START);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_layout:
                changeDrawerView();
                break;
            case R.id.info_layout:
                showInfo();
                break;
//            case R.id.back_icon:
//                changeDrawerView();
//                break;
//            case R.id.info_icon:
//                showInfo();
//                break;
            default:
                logger.info(getString(R.string.onclick_default) + id);
                break;
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        mainView.setTranslationX(slideOffset * navigationView.getWidth());
        menuDrawer.bringChildToFront(drawerView);
        menuDrawer.requestLayout();
    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

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

    //Added by Vishal
    //for deleting account
    private void delete() {
        logger.info("Delete Account Initiated");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainMenu.this);
        alertDialog.setTitle(getString(R.string.delete_title));
        alertDialog.setMessage(getString(R.string.delete_account));
        alertDialog.setPositiveButton(getString(R.string.delete), (dialog, which) -> sendVolleyRequest(AppConstant.QUERY_UNSUBSCRIBE));

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.cancel());

        // Showing Alert Message
        alertDialog.show();
    }

    //create and send volley request
    private void sendVolleyRequest(String requestType) {
        if (requestType.equals(AppConstant.QUERY_UNSUBSCRIBE)) {
            String url = BuildConfig.REST_URL + AppConstant.UNSUBSCRIBE_URL;
            String tag = AppConstant.UNSUBSCRIBE;
            int method = Request.Method.POST;
            progressDialog.setMessage(AppConstant.DELETING_MSG);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            makeJsonArryReq(url, method, tag, requestType);
        }
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag,                                 final String queryType) {
        JsonArrayRequest req = new JsonArrayRequest(url, response -> {
            volleyArray.remove(tag);
//                logger.info(getString(R.string.received_response));
            response(tag, response, 200, queryType);
        }, error -> {
            volleyArray.remove(tag);
            logger.info(getString(R.string.received_error));
            error(tag, error, queryType);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                if (queryType.equals(AppConstant.QUERY_UNSUBSCRIBE)) {
                    headers.put(AppConstant.REST_ACCESS_TOKEN, mPrefer.getString(AppConstant.TOKEN, ""));
                }
                return headers;
            }

            @Override
            public byte[] getBody() {
                return null;
            }

            @Override
            public int getMethod() {
                return method;
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                String jsonString;
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

    //handles diferent response codes from Volley
    private void response(String check, JSONArray response, int code, String queryType) {
        mInstance.myLibrary.checkResponse(response, code, this);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            // handling response for submitting registration data
            if (queryType.equals(AppConstant.QUERY_UNSUBSCRIBE)) {
                logger.info("User Account Deleted");
                JSONObject result = response.optJSONObject(0);
                int status = response.optInt(1);
//                String message = result.optString("message");
                if (status == 200) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.log_message) + " " + getString(R.string.registration_submit_succes));
                } else {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status);
                }
                if (check.equals(AppConstant.UNSUBSCRIBE)) {
                    handleResponse(result, status);
                }
            }
        }
    }

    public void handleResponse(JSONObject result, int status) {
        switch (status) {
            case AppConstant.RESPONSE_SUCCESS:
            case AppConstant.RESPONSE_GOOGLE_ANONYMUS:
                onDeleteSuccess();
                break;
            default:
                logger.info("default statement for handleResonse execute for status code : " + status);
                break;
        }
    }

    //Added by Vishal
    public void onDeleteSuccess() {
        mInstance.myLibrary.DisplayToast(this, AppConstant.UNSUBSCRIBE_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        mInstance.myLibrary.delete(null, false);
        showLogin();
    }

    //Added by Vishal
    //display login screen
    public void showLogin() {
        Intent intent = new Intent(MainMenu.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    //handles error response from Volley
    private void error(String check, VolleyError error, String queryType) {
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                System.out.println(message);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode, queryType);
            } else {
                mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }
}
