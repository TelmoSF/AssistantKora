package com.example.assistantkora;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log; // Import para logs
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

public class BottomSheetName extends Dialog {

    private EditText etEmail;
    private Button btnSave;

    private Context mContext;

    String iduser = "0";

    private static final String TAG = "BottomSheetName";

    public BottomSheetName(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bottom_sheet_name);

        // Dim the background
        Window window = getWindow();
        if (window != null) {
            window.setDimAmount(0.8f);
        }

        etEmail = findViewById(R.id.nName);
        btnSave = findViewById(R.id.btn_save);

        loadUserDetails();

        btnSave.setOnClickListener(v -> {
            String email = etEmail.getText().toString();

            Log.d(TAG, "Email: " + email);
            Log.d(TAG, "User ID: " + iduser);

            // Perform POST request in background
            new SendPostRequest(mContext, email, iduser).execute();

            // Dismiss dialog
            dismiss();
        });
    }

    private static class SendPostRequest extends AsyncTask<Void, Void, String> {
        private Context mContext;
        private String email;
        private String id;

        public SendPostRequest(Context context, String email, String id) {
            mContext = context;
            this.email = email;
            this.id = id;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Log.d(TAG, "Email in AsyncTask: " + email);
                Log.d(TAG, "User ID in AsyncTask: " + id);

                // Construct the POST data
                String postData = "id=" + id + "&nome=" + email;

                // Set up the connection
                URL url = new URL("http://kora.us.to/file/settings/name.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                // Write data to the connection output stream
                try (OutputStream outputStream = urlConnection.getOutputStream()) {
                    byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(postDataBytes);
                }

                // Get the response from the server
                StringBuilder response = new StringBuilder();
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                // Close connection
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
            // Handle the JSON response here and display it in a bubble pop-up
            if (response != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String resposta = jsonResponse.getString("resposta");
                    Toast.makeText(mContext, resposta, Toast.LENGTH_SHORT).show();

                    // Return value based on response
                    if ("Nome Alterado".equals(resposta)) {
                        // Atualiza o nome no SharedPreferences
                        SharedPreferences sharedPreferences = mContext.getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userName", email); // Salva o novo nome
                        editor.apply();

                        // Notifica a SettingsActivity sobre a alteração de nome bem-sucedida
                        ((SettingsActivity) mContext).onNameChangeSuccess(email);
                    } else {
                        ((SettingsActivity) mContext).onNameChangeFailure();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON response", e);
                    Toast.makeText(mContext, "Error parsing server response", Toast.LENGTH_SHORT).show();
                    ((SettingsActivity) mContext).onNameChangeFailure();
                }
            } else {
                Toast.makeText(mContext, "No response from server", Toast.LENGTH_SHORT).show();
                ((SettingsActivity) mContext).onNameChangeFailure();
            }
        }
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "Email");
        int id = sharedPreferences.getInt("id", 0);
        iduser = Integer.toString(id);

        Log.d(TAG, "Loaded User ID: " + iduser);
    }
}
