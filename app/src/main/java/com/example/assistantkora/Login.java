package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    private EditText editTextEmail, editTextSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Verificar se o usuário já está logado
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Se o usuário já estiver logado, redirecionar para a MainActivity
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finaliza a atividade de login
            return; // Interrompe a execução do onCreate para evitar que a interface de login seja exibida
        }

        // Inicialize os EditTexts
        editTextEmail = findViewById(R.id.emailregistarText);
        editTextSenha = findViewById(R.id.passeregistarText);

        // Obtenha uma referência para a TextView "Crie"
        TextView criarContaTextView = findViewById(R.id.criarContaTextView);

        // Adicione um OnClickListener à TextView "Crie"
        criarContaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicie a nova atividade aqui
                Intent intent = new Intent(Login.this, Registo.class);
                startActivity(intent);
            }
        });
    }

    public void fazerLogin(View view) {
        String email = editTextEmail.getText().toString();
        String senha = editTextSenha.getText().toString();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(Login.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar se o email tem um formato válido de acordo com o metodo em baixo
        if (!isValidEmail(email)) {
            Toast.makeText(Login.this, "Por favor, insira um email válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        new LoginTask().execute(email, senha);
    }

    // Método que verifica se o email está no formato
    private boolean isValidEmail(String email) {
        String emailPattern = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(emailPattern, email);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String senha = params[1];

            try {
                // URL do seu script PHP para verificar as credenciais na base de dados
                URL url = new URL("http://kora.us.to/file/login/verificar_login.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Parâmetros a serem enviados no corpo da requisição
                String data = "email=" + email + "&senha=" + senha;
                byte[] postData = data.getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postData.length));

                // Escrever os parâmetros no corpo da requisição
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData);
                }

                // Ler a resposta do servidor
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Retornar a resposta do servidor
                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "Erro ao fazer login: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // Verificar se o resultado é um JSON válido
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Se não for um JSON válido, exiba a mensagem de erro diretamente
                    Toast.makeText(Login.this, result, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar se o login foi bem-sucedido
                boolean success = jsonObject.getBoolean("success");
                String message = jsonObject.getString("message");

                if (success) {
                    // Login bem-sucedido, redirecionar para a próxima tela
                    int id = jsonObject.getInt("id");
                    String userName = jsonObject.getString("userName");
                    String email = jsonObject.getString("email");
                    String number = jsonObject.getString("number");

                    // Salvar o nome do usuário nas preferências compartilhadas
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userName", userName);
                    editor.putString("email", email);
                    editor.putString("number", number);
                    editor.putInt("id", id);
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Finaliza a atividade de login para que o usuário não possa voltar para ela pressionando o botão "Voltar"
                } else {
                    // Login falhou
                    Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Imprimir detalhes sobre a exceção no Logcat
                Log.e("Login", "Erro ao fazer login: " + e.getMessage(), e);
                // Exibir mensagem de erro para o usuário
                Toast.makeText(Login.this, "Erro ao fazer login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
