package com.tnedicca.routewise.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.InsuranceForm;
import com.tnedicca.routewise.activities.MainMenu;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.CustomWebView;

/**
 * Created by new on 30-01-2017.
 */

public class InsuranceQuote extends Fragment implements View.OnClickListener {

    private CustomButton noThanksButton;
    private CustomButton continueButton;
    private MainMenu mainMenu;
    private RouteWise mInstance;

    private ViewGroup rootView;
    private CustomWebView quote;
    private ProgressBar mProgress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = RouteWise.getInstance();
        mainMenu = ((MainMenu) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        rootView = (ViewGroup) inflater.inflate(R.layout.insurance_quote, container, false);
        noThanksButton = rootView.findViewById(R.id.no_thanks_button);
        continueButton = rootView.findViewById(R.id.continue_button);
        mProgress = rootView.findViewById(R.id.progress);
        quote = rootView.findViewById(R.id.quote);

        quote.setWebViewClient(new myWebClient());
        quote.getSettings().setJavaScriptEnabled(true);

        String text = getString(R.string.quote);
        text = text.replace("®", "<sup><small>®</small></sup>");
        quote.setText(text);
        noThanksButton.setOnClickListener(this);
        continueButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.no_thanks_button:
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                ft.replace(R.id.content_frame, new Dashboard());
                ft.commit();
                mainMenu.previousMenuId = R.id.menu_dashboard;
                mainMenu.screenTitle.setCustomText(getString(R.string.title_dashboard));
                mainMenu.infoIcon.setVisibility(View.VISIBLE);
                break;
            case R.id.continue_button:
                Intent intent = new Intent(this.getActivity(), InsuranceForm.class);
                startActivity(intent);
                mainMenu.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mProgress.setVisibility(View.VISIBLE);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgress.setVisibility(View.GONE);
        }
    }
}
