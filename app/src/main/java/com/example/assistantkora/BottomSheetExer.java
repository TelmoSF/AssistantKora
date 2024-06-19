package com.example.assistantkora;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

public class BottomSheetExer extends Dialog {

    private Button btnCaminhada, btnCorrida, btnBicicleta;
    private Context mContext;

    public BottomSheetExer(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bottom_sheet_exer);

        btnCaminhada = findViewById(R.id.caminhada);
        btnCorrida = findViewById(R.id.corrida);
        btnBicicleta = findViewById(R.id.bicicleta);

        btnCaminhada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetCaminhada dialog = new BottomSheetCaminhada(mContext);
                dialog.show();
            }
        });

        btnCorrida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetCorrida dialog = new BottomSheetCorrida(mContext);
                dialog.show();
            }
        });

        btnBicicleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetBicicleta dialog = new BottomSheetBicicleta(mContext);
                dialog.show();
            }
        });
    }
}
