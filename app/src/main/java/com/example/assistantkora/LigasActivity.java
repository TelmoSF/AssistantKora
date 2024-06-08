package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class LigasActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ligas);

        AppCompatImageView imageFootballBack = findViewById(R.id.imageFootballBack);
        AppCompatImageView jogos = findViewById(R.id.jogos);
        AppCompatImageView fav = findViewById(R.id.fav);
        webView = findViewById(R.id.webView);

        // Enable JavaScript (if required)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the specific page URL
        webView.loadUrl("http://kora.us.to/file/Football/Standings.html");

        imageFootballBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LigasActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        jogos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LigasActivity.this, FootballActivity.class);
                startActivity(intent);
                finish();
            }
        });

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LigasActivity.this, JogadoresActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}