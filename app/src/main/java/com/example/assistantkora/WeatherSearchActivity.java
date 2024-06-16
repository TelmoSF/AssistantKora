package com.example.assistantkora;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;


public class WeatherSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_search);

        final EditText editText = findViewById(R.id.editText);
        AppCompatImageView btn_back = findViewById(R.id.btn_back);
        ImageView openBottomSheetButton = findViewById(R.id.openBottomSheetButton);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherSearchActivity.this, WeatherActivity.class);
                startActivity(intent);
                finish();
            }
        });

        openBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = editText.getText().toString().trim();
                if (!searchText.isEmpty()) {
                    // Pass user input to BottomSheetFragment
                    BottomSheetFragment bottomSheetFragment = BottomSheetFragment.newInstance(searchText);
                    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                }
            }
        });
    }
}