package com.example.assistantkora;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class BottomSheetWater extends Dialog {

    private Button btnSave;
    private EditText editTextML;

    private int iduser = 0;
    private Context mContext;

    private static final String TAG = "BottomSheetWater";

    public BottomSheetWater(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.water_bottom_sheet);
        btnSave = findViewById(R.id.savewater);
        editTextML = findViewById(R.id.ml);

        loadUserDetails();

        Window window = getWindow();
        if (window != null) {
            window.setDimAmount(0.8f);
        }

        btnSave.setOnClickListener(v -> {
            String ml = editTextML.getText().toString();

            // Verifica se os campos não estão vazios
            if (!ml.isEmpty() && iduser != 0) {
                // Envia os dados via POST
                sendPostRequest(String.valueOf(iduser), ml);
            } else {
                // Mostra uma mensagem de erro
                Toast.makeText(mContext, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPostRequest(String iduser, String ml) {
        new Thread(() -> {
            try {
                URL url = new URL("http://kora.us.to/file/saude/water.php"); // Substitui pela tua URL
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = "id=" + URLEncoder.encode(iduser, "UTF-8") + "&ml=" + URLEncoder.encode(ml, "UTF-8");
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    // Processa a resposta do servidor
                    ((Activity) mContext).runOnUiThread(() -> {
                        Toast.makeText(mContext, "Resposta do servidor: " + response.toString(), Toast.LENGTH_LONG).show();
                        ((Activity) mContext).recreate(); // Recarrega a atividade atual
                        dismiss(); // Fecha o BottomSheet
                    });
                } else {
                    ((Activity) mContext).runOnUiThread(() -> {
                        Toast.makeText(mContext, "Erro na resposta do servidor: " + responseCode, Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                ((Activity) mContext).runOnUiThread(() -> {
                    Toast.makeText(mContext, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        iduser = sharedPreferences.getInt("id", 0);
    }

}
