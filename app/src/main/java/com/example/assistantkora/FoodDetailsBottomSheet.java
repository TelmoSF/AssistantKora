package com.example.assistantkora;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class FoodDetailsBottomSheet extends BottomSheetDialogFragment {

    private int iduser = 0;
    private Context context;

    private static final String ARG_FOOD_NAME = "food_name";
    private static final String ARG_FOOD_CALORIES = "food_calories";
    private static final String ARG_FOOD_FAT = "food_fat";
    private static final String ARG_FOOD_CARBS = "food_carbs";
    private static final String ARG_FOOD_PROTEIN = "food_protein";

    public static FoodDetailsBottomSheet newInstance(Food food) {
        FoodDetailsBottomSheet fragment = new FoodDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_FOOD_NAME, food.getName());
        args.putString(ARG_FOOD_CALORIES, food.getCalorias());
        args.putString(ARG_FOOD_FAT, food.getGordura());
        args.putString(ARG_FOOD_CARBS, food.getCarbohidratos());
        args.putString(ARG_FOOD_PROTEIN, food.getProteina());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_food_details, container, false);

        TextView tvFoodName = view.findViewById(R.id.tv_food_name);
        TextView tvFoodCalories = view.findViewById(R.id.tv_food_calories);
        TextView tvFoodFat = view.findViewById(R.id.tv_food_fat);
        TextView tvFoodCarbs = view.findViewById(R.id.tv_food_carbs);
        TextView tvFoodProtein = view.findViewById(R.id.tv_food_protein);
        EditText etQuantity = view.findViewById(R.id.et_quantity);
        Button btnSubmit = view.findViewById(R.id.btn_submit);

        Bundle args = getArguments();
        if (args != null) {
            tvFoodName.setText(args.getString(ARG_FOOD_NAME));
            tvFoodCalories.setText("Calorias: " + args.getString(ARG_FOOD_CALORIES));
            tvFoodFat.setText("Gordura: " + args.getString(ARG_FOOD_FAT));
            tvFoodCarbs.setText("Carboidratos: " + args.getString(ARG_FOOD_CARBS));
            tvFoodProtein.setText("Proteína: " + args.getString(ARG_FOOD_PROTEIN));
        }

        loadUserDetails();

        btnSubmit.setOnClickListener(v -> {
            Log.d(TAG, "Submit button clicked");
            String quantityStr = etQuantity.getText().toString();
            if (!quantityStr.isEmpty()) {
                double quantity = Double.parseDouble(quantityStr);
                // Calorias por 100g
                double caloriesPer100g = Double.parseDouble(args.getString(ARG_FOOD_CALORIES));
                // Calcular calorias consumidas
                double totalCalories = (caloriesPer100g / 100) * quantity;

                Log.d(TAG, "Calculated total calories: " + totalCalories);
                sendCaloriesToServer(iduser, totalCalories);
            } else {
                Toast.makeText(context, "Por favor, insira a quantidade.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int id = sharedPreferences.getInt("id", 0);
        iduser = id;

        Log.d(TAG, "Loaded User ID: " + iduser);
    }

    private void sendCaloriesToServer(int id, double calories) {
        new Thread(() -> {
            try {
                URL url = new URL("http://kora.us.to/file/saude/add_cal.php"); // Coloque o URL do seu servidor
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String postData = "info_id=" + id + "&calories=" + (int) calories;
                Log.d(TAG, "Post data: " + postData);
                try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                    writer.write(postData);
                    writer.flush();
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Calorias enviadas com sucesso.");
                    openSaudeActivity();
                } else {
                    Log.d(TAG, "Falha ao enviar calorias. Código de resposta: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao enviar calorias: ", e);
            }
        }).start();
    }

    private void openSaudeActivity() {
        Intent intent = new Intent(context, SaudeActivity.class);
        startActivity(intent);
    }
}
