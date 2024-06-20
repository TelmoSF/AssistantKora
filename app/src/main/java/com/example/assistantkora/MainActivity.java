package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView numberTextView;
    private TextView idTextView;
    private String forms; // Adiciona a variável forms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);
        numberTextView = findViewById(R.id.number);
        idTextView = findViewById(R.id.id);

        // Inicialmente carregue os detalhes do usuário
        loadUserDetails();

        ConstraintLayout chatButton = findViewById(R.id.chat_botao);
        ConstraintLayout weatherButton = findViewById(R.id.weatherbutton);
        ConstraintLayout settingsbutton = findViewById(R.id.settingsbutton);
        ConstraintLayout footballbutton = findViewById(R.id.footballbutton);
        ConstraintLayout healthbutton = findViewById(R.id.health_button);
        ConstraintLayout newsbutton = findViewById(R.id.newsbutton);

        newsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                startActivity(intent);
            }
        });

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });

        footballbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FootballActivity.class);
                startActivity(intent);
            }
        });

        settingsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        healthbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("0".equals(forms)) {
                    Intent intent = new Intent(MainActivity.this, HealthActivity.class);
                    startActivity(intent);
                } else if ("1".equals(forms)) {
                    Intent intent = new Intent(MainActivity.this, SaudeActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Recarregue os detalhes do usuário
            loadUserDetails();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregue os detalhes do usuário
        loadUserDetails();
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "Usuário");
        String email = sharedPreferences.getString("email", "Email");
        String number = sharedPreferences.getString("number", "Numero");
        forms = sharedPreferences.getString("forms", "0");
        int id = sharedPreferences.getInt("id", 0);
        usernameTextView.setText(userName);
        emailTextView.setText(email);
        numberTextView.setText(number);
        idTextView.setText(String.valueOf(id));
    }
}
