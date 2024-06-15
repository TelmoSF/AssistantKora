package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class JogadoresActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jogadores);

        AppCompatImageView imageFootballBack = findViewById(R.id.imageFootballBack);
        AppCompatImageView ligas = findViewById(R.id.ligas);
        AppCompatImageView jogos = findViewById(R.id.jogos);
        webView = findViewById(R.id.webView);

        // Enable JavaScript (if required)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the specific page URL
        webView.loadUrl("http://kora.us.to/file/Football/Players.html");

        imageFootballBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JogadoresActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        jogos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JogadoresActivity.this, FootballActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ligas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JogadoresActivity.this, LigasActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}