package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SettingsActivity extends AppCompatActivity {

    private ImageView settingsBack;
    private RelativeLayout firstBox, secondBox, thirdBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsBack = findViewById(R.id.settings_back);
        firstBox = findViewById(R.id.first_box);
        secondBox = findViewById(R.id.second_box);
        thirdBox = findViewById(R.id.third_box);

        settingsBack.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        firstBox.setOnClickListener(v -> {
            BottomSheetEmail dialog = new BottomSheetEmail(SettingsActivity.this);
            dialog.show();
        });

        secondBox.setOnClickListener(v -> {
            BottomSheetEmail dialog = new BottomSheetEmail(SettingsActivity.this);
            dialog.show();
        });

        thirdBox.setOnClickListener(v -> {
            BottomSheetEmail dialog = new BottomSheetEmail(SettingsActivity.this);
            dialog.show();
        });
    }
}
