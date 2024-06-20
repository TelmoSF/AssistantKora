package com.example.assistantkora;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BottomSheetFood extends Dialog {

    private Context context;
    private List<Food> foods;

    private int iduser = 0;
    private LinearLayout linearLayout;
    private TextView tvNoFoods;

    public BottomSheetFood(Context context) {
        super(context);
        this.context = context;
        this.foods = new ArrayList<>(); // Inicializa com uma lista vazia
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_sheet_food); // Verifique se o nome do layout está correto

        linearLayout = findViewById(R.id.ll_buttons);
        tvNoFoods = findViewById(R.id.tv_no_foods);
        Button btnAddFood = findViewById(R.id.btn_add_food);

        btnAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFoodDialog(); // Abre o diálogo para adicionar um novo alimento
            }
        });

        loadUserDetails();
        new FetchFoodsTask().execute();
    }

    private void displayFoods() {
        linearLayout.removeAllViews();
        if (foods.isEmpty()) {
            tvNoFoods.setVisibility(View.VISIBLE);
        } else {
            tvNoFoods.setVisibility(View.GONE);
            for (final Food food : foods) {
                Button button = new Button(context);
                button.setText(food.getName());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 16, 0, 0);
                button.setLayoutParams(params);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFoodDetails(food);
                    }
                });
                linearLayout.addView(button);
            }
        }
    }

    private void showFoodDetails(Food food) {
        FoodDetailsBottomSheet foodDetailsBottomSheet = FoodDetailsBottomSheet.newInstance(food);
        foodDetailsBottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), foodDetailsBottomSheet.getTag());
    }

    private void showAddFoodDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_food);

        final EditText etName = dialog.findViewById(R.id.et_name);
        final EditText etCalorias = dialog.findViewById(R.id.et_calorias);
        final EditText etGordura = dialog.findViewById(R.id.et_gordura);
        final EditText etCarbohidratos = dialog.findViewById(R.id.et_carbohidratos);
        final EditText etProteina = dialog.findViewById(R.id.et_proteina);
        Button btnSubmit = dialog.findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                String calorias = etCalorias.getText().toString();
                String gordura = etGordura.getText().toString();
                String carbohidratos = etCarbohidratos.getText().toString();
                String proteina = etProteina.getText().toString();

                if (!name.isEmpty() && !calorias.isEmpty() && !gordura.isEmpty() && !carbohidratos.isEmpty() && !proteina.isEmpty()) {
                    addFood(name, calorias, gordura, carbohidratos, proteina);
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void addFood(String name, String calorias, String gordura, String carbohidratos, String proteina) {
        foods.add(new Food(name, calorias, gordura, carbohidratos, proteina));
        displayFoods();
        new AddFoodTask().execute(name, calorias, gordura, carbohidratos, proteina);
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int id = sharedPreferences.getInt("id", 0);
        iduser = id;

        Log.d(TAG, "Loaded User ID: " + iduser);

    }

    private class FetchFoodsTask extends AsyncTask<Void, Void, List<Food>> {

        @Override
        protected List<Food> doInBackground(Void... voids) {
            List<Food> foodList = new ArrayList<>();
            try {
                URL url = new URL("http://kora.us.to/file/saude/listar_food.php"); // Supondo que esta seja a URL correta
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String postData = "food_id=" + iduser;
                writer.write(postData);
                writer.flush();
                writer.close();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    inputStream.close();

                    Log.d(TAG, "JSON Response: " + result.toString());

                    // Processa a resposta JSON
                    JSONArray jsonArray = new JSONArray(result.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String name = jsonObject.getString("nome");
                        String calorias = jsonObject.getString("calorias");
                        String gordura = jsonObject.getString("gordura");
                        String carbohidratos = jsonObject.getString("carbohidratos");
                        String proteina = jsonObject.getString("proteina");
                        foodList.add(new Food(name, calorias, gordura, carbohidratos, proteina));
                    }

                } else {
                    Log.e(TAG, "Código de resposta HTTP: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao buscar alimentos: ", e);
            }
            return foodList;
        }

        @Override
        protected void onPostExecute(List<Food> foodList) {
            foods.clear();
            foods.addAll(foodList);
            displayFoods();
        }
    }

    private class AddFoodTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL url = new URL("http://kora.us.to/file/saude/add_food.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String postData = "food_id=" + iduser + "&nome=" + params[0] + "&calorias=" + params[1] + "&gordura=" + params[2] + "&carbohidratos=" + params[3] + "&proteina=" + params[4];
                writer.write(postData);
                writer.flush();
                writer.close();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Verifica a resposta do servidor
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    inputStream.close();

                    // Log para depuração
                    Log.d(TAG, "Resposta do servidor: " + result.toString());

                    return result.toString().contains("Novo registro inserido com sucesso");
                } else {
                    Log.e(TAG, "Código de resposta HTTP: " + responseCode);
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao adicionar alimento: ", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(context, "Alimento adicionado com sucesso", Toast.LENGTH_SHORT).show();
                new FetchFoodsTask().execute(); // Atualiza a lista de alimentos após adicionar
            } else {
                Toast.makeText(context, "Erro ao adicionar alimento", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

// Classe auxiliar para armazenar dados dos alimentos
class Food {
    private String name;
    private String calorias;
    private String gordura;
    private String carbohidratos;
    private String proteina;

    public Food(String name, String calorias, String gordura, String carbohidratos, String proteina) {
        this.name = name;
        this.calorias = calorias;
        this.gordura = gordura;
        this.carbohidratos = carbohidratos;
        this.proteina = proteina;
    }

    public String getName() {
        return name;
    }

    public String getCalorias() {
        return calorias;
    }

    public String getGordura() {
        return gordura;
    }

    public String getCarbohidratos() {
        return carbohidratos;
    }

    public String getProteina() {
        return proteina;
    }
}