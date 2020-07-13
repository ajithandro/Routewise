package com.tnedicca.routewise.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.MainMenu;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomTextView;

/**
 * Created by Vishal on 06-02-2017.
 */

public class Help extends Fragment {

    private MainMenu mainMenu;
    private CustomTextView versionNumberHeading;
    private CustomTextView versionNumberText;
    private CustomTextView buildNumberHeading;
    private CustomTextView buildNumberText;
    private CustomTextView contactInfoHeading;
    private CustomTextView contactInfoText;

    private ViewGroup rootView;
    private PackageInfo pInfo;
    private String versionNumber;
    private String buildNumber;
    private RouteWise mInstance;
    private int lineNumber;
    private RouteLog logger;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainMenu = ((MainMenu)getActivity());
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Help.class);

        try {
            pInfo = mainMenu.getPackageManager().getPackageInfo(mainMenu.getPackageName(), 0);
            versionNumber = pInfo.versionName;
            buildNumber = Integer.toString(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        rootView = (ViewGroup) inflater.inflate(R.layout.help, container, false);
        versionNumberHeading = rootView.findViewById(R.id.version_number_heading);
        versionNumberText = rootView.findViewById(R.id.version_number_text);
        buildNumberHeading = rootView.findViewById(R.id.build_number_heading);
        buildNumberText = rootView.findViewById(R.id.build_number_text);
        contactInfoHeading = rootView.findViewById(R.id.contact_info_heading);
        contactInfoText = rootView.findViewById(R.id.contact_info_text);
        versionNumberText.setText(versionNumber);
        buildNumberText.setText(buildNumber);

        return rootView;
    }
}
