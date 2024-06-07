package com.example.assistantkora;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText editTextUserInput;
    private static final String TAG = "ChatActivity";
    private FrameLayout layoutSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextUserInput = findViewById(R.id.editTextUserInput);
        layoutSend = findViewById(R.id.layoutSend);
        AppCompatImageView imageBack = findViewById(R.id.imageBack);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInput = editTextUserInput.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    Log.d(TAG, "Utilizador clicou em enviar: " + userInput);

                    messageList.add(new ChatMessage(userInput, true));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewChat.scrollToPosition(messageList.size() - 1);
                    editTextUserInput.getText().clear();

                    // URL do arquivo PHP para processar a pergunta
                    String url = "http://kora.us.to/file/request/chat.php";

                    // Iniciar AsyncTask para fazer a solicitação POST
                    new ChatTask().execute(userInput, url);
                } else {
                    Log.d(TAG, "Campo de entrada do utilizador está vazio.");
                }
            }
        });

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private class ChatTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String userInput = params[0];
            String url = params[1];
            try {
                // Criar a URL e a conexão HttpURLConnection
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

                // Definir o método de solicitação como POST
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Construir o corpo da solicitação
                String requestBody = "pergunta=" + userInput;

                // Enviar a solicitação
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestBody.getBytes());
                outputStream.flush();
                outputStream.close();

                // Ler a resposta
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();

                // Converter a resposta em um objeto JSON
                return new JSONObject(response.toString());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                handleResponse(jsonObject);
            }
        }
    }

    private void handleResponse(JSONObject jsonResponse) {
        try {
            // Extrair a resposta da JSON
            String resposta = jsonResponse.getString("resposta");

            // Adicionar a mensagem ao RecyclerView
            messageList.add(new ChatMessage(resposta, false));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerViewChat.scrollToPosition(messageList.size() - 1);

        } catch (JSONException e) {
            e.printStackTrace();
            // Lidar com erros ao processar a resposta JSON
            messageList.add(new ChatMessage("Erro ao processar a resposta JSON: " + e.getMessage(), false));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerViewChat.scrollToPosition(messageList.size() - 1);
        }
    }
}
