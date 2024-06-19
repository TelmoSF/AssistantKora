package com.example.assistantkora;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class BottomSheetBicicleta extends Dialog {

    private int iduser = 0;
    private Context mContext;

    public BottomSheetBicicleta(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bottom_sheet_bicicleta);

        // Carregar detalhes do utilizador
        loadUserDetails();

        EditText editTextTime = findViewById(R.id.time);
        Button buttonSave = findViewById(R.id.save);

        buttonSave.setOnClickListener(v -> {
            String time = editTextTime.getText().toString();
            if (!time.isEmpty()) {
                new PostDataTask().execute(time);
            } else {
                Toast.makeText(getContext(), "Por favor, insira o tempo da caminhada.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class PostDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String time = params[0];
            String response = "";

            try {
                URL url = new URL("http://kora.us.to/file/saude/bicicleta.php"); // Substitua pela URL do seu script PHP
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);

                String postData = "info_id=" + iduser + "&time=" + time;
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                writer.write(postData);
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonResponse = new JSONObject(result);
                double caloriasQueimadas = jsonResponse.getDouble("queimadas");
                Toast.makeText(getContext(), "Calorias queimadas: " + caloriasQueimadas, Toast.LENGTH_SHORT).show();

                // Recarregar a atividade principal
                if (mContext instanceof SaudeActivity) {
                    ((SaudeActivity) mContext).recreate();
                }

                dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Erro ao calcular calorias.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int id = sharedPreferences.getInt("id", 0);
        iduser = id;

        Log.d(TAG, "Loaded User ID: " + iduser);
    }
}
