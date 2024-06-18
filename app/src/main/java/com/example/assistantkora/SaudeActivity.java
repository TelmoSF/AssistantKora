package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SaudeActivity extends AppCompatActivity {

    private ProgressBar calorieBar;
    private TextView caloriesText;

    private int maxCalories = 3000;
    private int eatenCalories = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saude);

        calorieBar = findViewById(R.id.calorieBar);
        caloriesText = findViewById(R.id.caloriesText);

        // Inicializa a barra de progresso
        calorieBar.setMax(maxCalories);
        updateCalorieBar();

        // Exemplo de consumo de calorias
        consumeCalories(2000);
    }

    private void updateCalorieBar() {
        calorieBar.setProgress(eatenCalories);
        caloriesText.setText(String.valueOf(eatenCalories));
    }

    private void consumeCalories(int calories) {
        eatenCalories += calories;
        if (eatenCalories > maxCalories) {
            eatenCalories = maxCalories;
        }
        updateCalorieBar();
    }
}
