package com.example.assistantkora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class HealthActivity extends AppCompatActivity {

    private EditText pesoEditText, alturaEditText, idadeEditText, pesoDesejadoEditText;
    private RadioGroup generoRadioGroup, exercicioRadioGroup;
    private Button enviarButton;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);

        // Initialize views
        pesoEditText = findViewById(R.id.pesoEditText);
        alturaEditText = findViewById(R.id.alturaEditText);
        idadeEditText = findViewById(R.id.idadeEditText);
        pesoDesejadoEditText = findViewById(R.id.pesoDesejadoEditText);
        generoRadioGroup = findViewById(R.id.generoRadioGroup);
        exercicioRadioGroup = findViewById(R.id.exercicioRadioGroup);
        enviarButton = findViewById(R.id.enviarButton);

        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserDetails();

                String peso = pesoEditText.getText().toString();
                String altura = alturaEditText.getText().toString();
                String idade = idadeEditText.getText().toString();
                String pesoDesejado = pesoDesejadoEditText.getText().toString();

                int selectedGeneroId = generoRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedGenero = findViewById(selectedGeneroId);
                String genero = selectedGenero != null ? selectedGenero.getText().toString() : "";

                int selectedExercicioId = exercicioRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedExercicio = findViewById(selectedExercicioId);
                String exercicio = selectedExercicio != null ? selectedExercicio.getText().toString() : "";

                if (peso.isEmpty() || altura.isEmpty() || idade.isEmpty() || genero.isEmpty() || exercicio.isEmpty() || pesoDesejado.isEmpty()) {
                    Toast.makeText(HealthActivity.this, "Por favor, preencha todos os campos.", Toast.LENGTH_LONG).show();
                    return;
                }

                sendFormData(peso, altura, idade, genero, exercicio, pesoDesejado, userId);
            }
        });
    }

    private void sendFormData(String peso, String altura, String idade, String genero, String exercicio, String pesoDesejado, int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://kora.us.to/file/saude/forms.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);

                    String postData = "info_id=" + id + "&peso=" + peso + "&altura=" + altura + "&idade=" + idade +
                            "&genero=" + genero + "&exercicio=" + exercicio + "&pesoDesejado=" + pesoDesejado;

                    OutputStream os = conn.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // Log da resposta do servidor
                        Log.d("HealthActivity", "Server Response: " + response.toString());

                        // Parse the JSON response
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        runOnUiThread(() -> {
                            if ("success".equals(status)) {
                                // Atualizar SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("forms", "1");
                                editor.apply();

                                // Navegar para a próxima atividade
                                Intent intent = new Intent(HealthActivity.this, SaudeActivity.class);
                                startActivity(intent);
                                Toast.makeText(HealthActivity.this, "Dados enviados com sucesso! " + message, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(HealthActivity.this, "Erro: " + message, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(HealthActivity.this, "Erro ao enviar dados: " + responseCode, Toast.LENGTH_SHORT).show());
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(HealthActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("id", 0);
    }
}
