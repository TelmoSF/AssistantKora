package com.example.assistantkora;



import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;



public class WeatherSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_search);

        final EditText editText = findViewById(R.id.editText);
        ImageView openBottomSheetButton = findViewById(R.id.openBottomSheetButton);

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