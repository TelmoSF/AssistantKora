package com.example.assistantkora;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BottomSheetEmail extends Dialog {

    private EditText etEmail, etNEmail;
    private Button btnSave;

    public BottomSheetEmail(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.email_bottom_sheet);

        // Dim the background
        Window window = getWindow();
        if (window != null) {
            window.setDimAmount(0.8f);  // Set dim amount, 0.0 for no dim and 1.0 for full dim
        }

        etEmail = findViewById(R.id.email);
        etNEmail = findViewById(R.id.nemail);
        btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> {
            // Get email and nemail values
            String email = etEmail.getText().toString();
            String novo_email = etNEmail.getText().toString();

            // Perform POST request in background
            new SendPostRequest(getContext()).execute(email, novo_email);

            // Dismiss dialog
            dismiss();
        });
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

                // Construct the POST data
                String postData = "id=11&email=" + email + "&nemail=" + novo_email;

                // Set up the connection
                URL url = new URL("http://kora.us.to/file/settings/change_email.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                // Write data to the connection output stream
                try (OutputStream outputStream = urlConnection.getOutputStream()) {
                    byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(postDataBytes);
                }

                // Get the response from the server
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                // Close connection
                urlConnection.disconnect();

                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            // Handle the JSON response here and display it in a bubble pop-up
            if (response != null) {
                // Display the response using Toast.makeText
                Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
            } else {
                // Handle the case where no response is received
            }
        }
    }
}
