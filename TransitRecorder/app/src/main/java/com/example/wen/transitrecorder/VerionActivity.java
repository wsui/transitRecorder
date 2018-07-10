package com.example.wen.transitrecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

public class VerionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verion);

        WebView webView = (WebView) findViewById(R.id.webview_version);
        webView.loadUrl("file:///android_asset/version.html");
    }
}
