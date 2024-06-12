package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView numberTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);
        numberTextView = findViewById(R.id.number);
        loadUserDetails();


        ConstraintLayout chatButton = findViewById(R.id.chat_botao);
        ConstraintLayout weatherButton = findViewById(R.id.weatherbutton);
        ConstraintLayout settingsbutton = findViewById(R.id.settingsbutton);
        ConstraintLayout footballbutton = findViewById(R.id.footballbutton);

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
                // Atualiza a flag isLoggedIn para false nas preferências compartilhadas
                //SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                //SharedPreferences.Editor editor = sharedPreferences.edit();
                //editor.putBoolean("isLoggedIn", false);
                //editor.apply();

                // Redireciona para a Login Activity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                //finish(); // Finaliza a MainActivity para evitar que o usuário volte pressionando o botão "Voltar"
            }
        });



    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "Usuário");
        String email = sharedPreferences.getString("email", "Email");
        String number = sharedPreferences.getString("number", "Numero");
        usernameTextView.setText(userName);
        emailTextView.setText(email);
        numberTextView.setText(number);
    }
}
