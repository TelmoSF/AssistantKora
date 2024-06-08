package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class teste extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste2);

        webView = findViewById(R.id.webView);

        // Enable JavaScript (if required)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the specific page URL
        webView.loadUrl("http://kora.us.to/file/Teste-Fut/testes.html");
    }
}