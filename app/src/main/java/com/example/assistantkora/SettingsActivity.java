package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SettingsActivity extends AppCompatActivity {

    private ImageView settingsBack;
    private TextView profileName;
    private RelativeLayout firstBox, secondBox, thirdBox, fifthBox, sixthBox, profile_container;
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsBack = findViewById(R.id.settings_back);
        firstBox = findViewById(R.id.first_box);
        secondBox = findViewById(R.id.second_box);
        thirdBox = findViewById(R.id.third_box);
        fifthBox = findViewById(R.id.fifth_box);
        profileName = findViewById(R.id.profile_name);
        sixthBox = findViewById(R.id.sixth_box);
        profile_container = findViewById(R.id.profile_container);

        settingsBack.setOnClickListener(v -> finish());

        profile_container.setOnClickListener(view -> {
            BottomSheetName dialog = new BottomSheetName(SettingsActivity.this);
            dialog.show();
        });

        fifthBox.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();

            Intent intent = new Intent(SettingsActivity.this, Login.class);
            startActivity(intent);
            finish();
        });

        firstBox.setOnClickListener(v -> showEmail());
        secondBox.setOnClickListener(v -> showSenha());
        thirdBox.setOnClickListener(v -> showCode());

        sixthBox.setOnClickListener(this::confirmDeleteAccount);

        loadUserDetails();
    }

    private void showEmail() {
        BottomSheetEmail dialog = new BottomSheetEmail(SettingsActivity.this);
        dialog.show();
    }

    private void showSenha() {
        BottomSheetSenha dialog = new BottomSheetSenha(SettingsActivity.this);
        dialog.show();
    }

    private void showCode() {
        BottomSheetCode dialog = new BottomSheetCode(SettingsActivity.this);
        dialog.show();
    }

    public void confirmDeleteAccount(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Exclusão");
        builder.setMessage("Tem certeza que deseja excluir sua conta?");
        builder.setPositiveButton("Sim", (dialogInterface, i) -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            int userId = sharedPreferences.getInt("id", 0);
            new DeleteAccountTask(SettingsActivity.this).execute(String.valueOf(userId));
        });
        builder.setNegativeButton("Não", null);
        builder.show();
    }

    private static class DeleteAccountTask extends AsyncTask<String, Void, String> {
        private final Context mContext;

        public DeleteAccountTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String id = params[0];
                String postData = "id=" + id;

                URL url = new URL("http://kora.us.to/file/settings/delete.php");
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
                    String result = jsonResponse.getString("resposta");
                    Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();

                    // Limpa as preferências do usuário
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    sharedPreferences.edit().clear().apply();

                    // Redireciona para a tela de login
                    Intent intent = new Intent(mContext, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(intent);

                    // Finaliza a SettingsActivity
                    ((SettingsActivity) mContext).finish();

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON response", e);
                    Toast.makeText(mContext, "Error parsing server response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, "No response from server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "Usuário");
        profileName.setText(userName);
    }

    public void onNameChangeSuccess(String newName) {
        // Handle success case
        Log.d(TAG, "Nome alterado para: " + newName);
        profileName.setText(newName);
    }

    public void onNameChangeFailure() {
        // Handle failure case
        Log.d(TAG, "Falha ao alterar o nome.");
    }

}
