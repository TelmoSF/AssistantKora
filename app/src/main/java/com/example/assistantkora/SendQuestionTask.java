package com.example.assistantkora;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendQuestionTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        String question = params[0];
        String response = "";

        try {
            // URL do serviço PHP
            URL url = new URL("http://kora.us.to/file/chat.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Parâmetros da solicitação
            String postData = "pergunta=" + question;
            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();

            // Ler a resposta do servidor
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response += line;
            }
            reader.close();

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        // Aqui você pode processar a resposta JSON
        // Por exemplo, você pode atualizar a interface do usuário com a resposta
        // ou passá-la para uma função que atualiza o chat com a resposta recebida.
    }
}
