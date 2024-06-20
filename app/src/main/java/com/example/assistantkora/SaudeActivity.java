package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import android.content.SharedPreferences;

public class SaudeActivity extends AppCompatActivity {

    private ImageButton imagebuttonWater, imagebuttonExercicio, imagebuttonFood;
    private ProgressBar calorieBar;
    private TextView caloriesText, titleText, metacalorica, waterText;

    String iduser = "0";
    String meta = "0";
    private String forms;

    int maxCalories = 0;
    private int eatenCalories = 1126;
    private int waterDrank = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saude);

        calorieBar = findViewById(R.id.calorieBar);
        caloriesText = findViewById(R.id.caloriesText);
        titleText = findViewById(R.id.titleText);
        metacalorica = findViewById(R.id.metaCaloricaText);
        imagebuttonWater = findViewById(R.id.waterButton);
        imagebuttonExercicio = findViewById(R.id.exercicio_btn);
        imagebuttonFood = findViewById(R.id.food_btn);
        AppCompatImageView health_back = findViewById(R.id.health_back);
        waterText = findViewById(R.id.waterText);

        loadUserDetails();

        health_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SaudeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imagebuttonWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetWater dialog = new BottomSheetWater(SaudeActivity.this);
                dialog.show();
            }
        });

        imagebuttonExercicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetExer dialog = new BottomSheetExer(SaudeActivity.this);
                dialog.show();
            }
        });

        imagebuttonFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetFood dialog = new BottomSheetFood(SaudeActivity.this);
                dialog.show();
            }
        });
    }

    private void updateCalorieBar() {
        calorieBar.setMax(maxCalories);
        calorieBar.setProgress(eatenCalories);
        caloriesText.setText(String.valueOf(eatenCalories));
    }

    private void updateWaterText() {
        waterText.setText("Água consumida: \n " + waterDrank + "L");
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int id = sharedPreferences.getInt("id", 0);
        iduser = Integer.toString(id);
        forms = sharedPreferences.getString("forms", "0");

        if ("0".equals(forms)) {
            openHealthActivity();
        } else {
            loadHealthDetails(id);
        }
    }

    private void openHealthActivity() {
        Intent intent = new Intent(SaudeActivity.this, HealthActivity.class);
        startActivity(intent);
        finish();
    }

    public void loadHealthDetails(int id) {
        new LoadHealthDetailsTask().execute(id);
    }

    private class LoadHealthDetailsTask extends AsyncTask<Integer, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Integer... params) {
            int id = params[0];
            try {
                URL url = new URL("http://kora.us.to/file/saude/loadhealth.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "id=" + id;
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                return new JSONObject(response.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonResponse) {
            if (jsonResponse != null) {
                try {
                    maxCalories = jsonResponse.getInt("calorias");
                    waterDrank = jsonResponse.getInt("water");
                    int minCal = jsonResponse.getInt("mincal");
                    eatenCalories = minCal;
                    meta = Integer.toString(maxCalories);
                    metacalorica.setText("Meta Calórica\n" + "\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E " + meta);
                    updateCalorieBar();
                    updateWaterText();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Handle the error
            }
        }
    }
}
