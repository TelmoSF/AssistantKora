package com.example.assistantkora;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

public class Registo extends AppCompatActivity {

    private EditText editTextNome, editTextEmail, editTextTelefone, editTextSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registo);

        // Inicialize os EditTexts
        editTextNome = findViewById(R.id.nomeregistarText);
        editTextEmail = findViewById(R.id.emailregistarText);
        editTextTelefone = findViewById(R.id.telemovelText);
        editTextSenha = findViewById(R.id.passeregistarText);

        editTextTelefone.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(9), // Limitar a 9 dígitos
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isDigit(source.charAt(i))) {
                                return "";
                            }
                        }
                        return null;
                    }
                }
        });

        // Obtenha uma referência para a TextView "Entrar"
        TextView entrarTextView = findViewById(R.id.loginTextView);

        // Adicione um OnClickListener à TextView "Entrar"
        entrarTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicie a nova atividade aqui
                Intent intent = new Intent(Registo.this, Login.class);
                startActivity(intent);
            }
        });
    }

    // Método chamado quando o botão de registro é clicado
    public void registerUser(View view) {
        String nome = editTextNome.getText().toString();
        String email = editTextEmail.getText().toString();
        String telefone = editTextTelefone.getText().toString();
        String senha = editTextSenha.getText().toString();

        // Verifique se os campos estão vazios
        if (nome.isEmpty() || email.isEmpty() || telefone.isEmpty() || senha.isEmpty()) {
            Toast.makeText(Registo.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifique se o email tem um formato válido
        if (!isValidEmail(email)) {
            Toast.makeText(Registo.this, "Por favor, insira um email válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifique se o telefone tem um formato válido
        if (!isValidTelefone(telefone)) {
            Toast.makeText(Registo.this, "Por favor, insira um telefone válido (exatamente 9 dígitos numéricos).", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 8) {
            Toast.makeText(Registo.this, "A palavra-passe deve ter no mínimo 8 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }


        // Execute a tarefa de registro em segundo plano
        new RegisterTask().execute(nome, email, telefone, senha);
    }

    // Método para verificar se o email tem um formato válido
    private boolean isValidEmail(String email) {
        String emailPattern = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(emailPattern, email);
    }

    // Método para verificar se o telefone tem um formato válido (exatamente 9 dígitos numéricos)
    private boolean isValidTelefone(String telefone) {
        return telefone.matches("\\d{9}");
    }


    private class RegisterTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String nome = params[0];
            String email = params[1];
            String telefone = params[2];
            String senha = params[3];

            try {
                // URL do seu script PHP para inserção dos dados na base de dados
                URL url = new URL("http://kora.us.to/file/login/configbd.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Parâmetros a serem enviados no corpo da requisição
                String data = "nome=" + nome + "&email=" + email + "&telefone=" + telefone + "&senha=" + senha;
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
                return "Erro ao registar: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Exibir uma mensagem ao usuário com o resultado do registro
            Toast.makeText(Registo.this, result, Toast.LENGTH_SHORT).show();
            // Redirecionar para a tela de login após o registro bem-sucedido
            if (result.equals("Registo realizado com sucesso!")) {
                Intent intent = new Intent(Registo.this, Login.class);
                startActivity(intent);
                finish(); // Termina a MainActivity
            }
        }
    }
}