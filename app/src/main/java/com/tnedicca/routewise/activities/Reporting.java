package com.tnedicca.routewise.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;

import com.tnedicca.routewise.classes.RouteLog;

import java.io.File;
import java.util.Date;

/**
 * Created by Vishal on 02-01-2017.
 */

public class Reporting extends Activity {

    private int lineNumber;
    private RouteWise mInstance;
    private RouteLog logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        android.os.Debug.waitForDebugger();
        super.onCreate(savedInstanceState);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Reporting.class);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setFinishOnTouchOutside(false);
        try {
            String destPath = AppConstant.KEY_LOG_FILE_PATH + AppConstant.KEY_LOG_RUN_TIME;
            File f = new File(destPath);
            if (!f.exists()) {
                f.mkdirs();
            }
            Date currentDate = new Date();
            String date = mInstance.dateFormat.format(currentDate);

            String filePath = destPath + "/LogCat-" + date + ".txt";
            Runtime.getRuntime().exec(new String[]{"logcat", "-f", filePath, "Exception:V", "*:S"});
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
        // Displaying a alert dialog box to get cofirmation from the user
        AlertDialog.Builder stoppedDialog = new AlertDialog.Builder(Reporting.this);

        stoppedDialog.setTitle(R.string.app_name).setCancelable(false).setMessage("Oops, something went wrong !")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent restart = new Intent(Reporting.this, Launcher.class);
                        restart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(restart);
                    }
                });
        stoppedDialog.show();
    }
}
