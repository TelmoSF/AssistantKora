package com.example.assistantkora;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BottomSheetCode extends Dialog {

    private Context mContext;
    private EditText mEmailEditText;
    private Button mSaveButton;

    private static final String TAG = "BottomSheetCode"; // Tag para logs
    private static final String URL = "http://kora.us.to/file/saude/telefone.php"; // Substitua pelo URL real do seu script PHP

    private int iduser;

    public BottomSheetCode(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_sheet_code);

        mEmailEditText = findViewById(R.id.email);
        mSaveButton = findViewById(R.id.btn_save);

        loadUserDetails();

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String telefone = mEmailEditText.getText().toString().trim();
                if (TextUtils.isEmpty(telefone)) {
                    Toast.makeText(mContext, "Por favor, introduza o número.", Toast.LENGTH_SHORT).show();
                    return;
                }

                enviarNumero(telefone);
            }
        });
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        iduser = sharedPreferences.getInt("id", 0);

        Log.d(TAG, "Loaded User ID: " + iduser);
    }

    private void enviarNumero(final String telefone) {
        Log.d(TAG, "Enviando número: " + telefone); // Adicione esta linha para depuração
        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Resposta do servidor: " + response); // Adicione esta linha para depuração
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if ("success".equals(status)) {
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "Erro: " + message, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "Erro ao processar a resposta.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erro ao enviar código: " + error.getMessage()); // Adicione esta linha para depuração
                        Toast.makeText(mContext, "Erro ao enviar código: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("telemovel", telefone);
                params.put("id", String.valueOf(iduser));
                Log.d(TAG, "Parâmetros: " + params.toString()); // Adicione esta linha para depuração
                return params;
            }
        };

        queue.add(stringRequest);
    }
}
