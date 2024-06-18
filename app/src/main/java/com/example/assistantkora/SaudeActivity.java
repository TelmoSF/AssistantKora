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
import android.content.Context;

public class SaudeActivity extends AppCompatActivity {

    private ImageButton imagebuttonWater;
    private ProgressBar calorieBar;
    private TextView caloriesText, titleText, metacalorica;

    String iduser = "0";

    String meta = "0";

    int maxCalories = 0;
    private int eatenCalories = 1126;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_saude);

        calorieBar = findViewById(R.id.calorieBar);
        caloriesText = findViewById(R.id.caloriesText);
        titleText = findViewById(R.id.titleText);
        metacalorica = findViewById(R.id.metaCaloricaText);
        imagebuttonWater = findViewById(R.id.waterButton);
        AppCompatImageView health_back = findViewById(R.id.health_back);

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
                BottomSheetName dialog = new BottomSheetName(SaudeActivity.this);
                dialog.show();
            }
        });
    }

    private void updateCalorieBar() {
        calorieBar.setMax(maxCalories);
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

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int id = sharedPreferences.getInt("id", 0);
        iduser = Integer.toString(id);
        // titleText.setText(iduser);
        loadHealthDetails(id);
    }

    public void loadHealthDetails(int id) {
        new AsyncTask<Integer, Void, Integer>() {
            @Override
            protected Integer doInBackground(Integer... params) {
                int id = params[0];
                try {
                    // URL of the PHP script
                    URL url = new URL("http://kora.us.to/file/saude/loadhealth.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Prepare the POST data
                    String postData = "id=" + id;

                    // Send the POST data
                    OutputStream os = conn.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();

                    // Read the response
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getInt("calorias");

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Integer calorias) {
                if (calorias != null) {
                    maxCalories = calorias;
                    meta = Integer.toString(calorias);
                    metacalorica.setText("Meta Calórica\n" +"\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E " +meta);
                    updateCalorieBar();
                } else {
                    // Handle the error
                }
            }
        }.execute(id);
    }
}
