package com.example.assistantkora;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BottomSheetEmail extends Dialog {

    private EditText etEmail, etNEmail;
    private Button btnSave;

    private Context mContext;

    String iduser = "0";

    private static final String TAG = "BottomSheetEmail"; // Tag para logs

    public BottomSheetEmail(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.email_bottom_sheet);

        // Initialize views
        etEmail = findViewById(R.id.email);
        etNEmail = findViewById(R.id.nemail);
        btnSave = findViewById(R.id.btn_save);

        loadUserDetails();

        btnSave.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String novo_email = etNEmail.getText().toString();

            Log.d(TAG, "Email: " + email);
            Log.d(TAG, "Novo Email: " + novo_email);
            Log.d(TAG, "User ID: " + iduser);

            new SendPostRequest(mContext).execute(email, novo_email, iduser);

            // Update shared preferences with new email
            updateEmailInSharedPreferences(novo_email);

            dismiss();
        });

        Log.d(TAG, "BottomSheetEmail onCreate");
    }

    private static class SendPostRequest extends AsyncTask<String, Void, String> {
        private Context mContext;

        public SendPostRequest(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String email = params[0];
                String novo_email = params[1];
                String id = params[2];

                Log.d(TAG, "Email in AsyncTask: " + email);
                Log.d(TAG, "Novo Email in AsyncTask: " + novo_email);
                Log.d(TAG, "User ID in AsyncTask: " + id);

                String postData = "id=" + id + "&email=" + email + "&nemail=" + novo_email;

                URL url = new URL("http://kora.us.to/file/settings/email.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                try (OutputStream outputStream = urlConnection.getOutputStream()) {
                    byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(postDataBytes);
                }

                StringBuilder response = new StringBuilder();
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                urlConnection.disconnect();

                Log.d(TAG, "Response: " + response.toString());

                return response.toString();

            } catch (Exception e) {
                Log.e(TAG, "Error in AsyncTask", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String resposta = jsonResponse.getString("resposta");
                    Toast.makeText(mContext, resposta, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Server response: " + resposta);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON response", e);
                    Toast.makeText(mContext, "Error parsing server response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, "No response from server", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "No response from server");
            }
        }
    }

    private void updateEmailInSharedPreferences(String newEmail) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", newEmail);
        editor.apply();
        Log.d(TAG, "Updated email in SharedPreferences: " + newEmail);
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "Email");
        int id = sharedPreferences.getInt("id", 0);
        iduser = Integer.toString(id);

        Log.d(TAG, "Loaded User ID: " + iduser);
        Log.d(TAG, "Loaded email from SharedPreferences: " + email);
    }
}
