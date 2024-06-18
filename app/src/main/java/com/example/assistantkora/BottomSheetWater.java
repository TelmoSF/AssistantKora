package com.example.assistantkora;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

public class BottomSheetWater extends Dialog {


    private Button btnSave;

    private Context mContext;

    private static final String TAG = "BottomSheetEmail"; // Tag para logs

    public BottomSheetWater(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.water_bottom_sheet);

        Window window = getWindow();
        if (window != null) {
            window.setDimAmount(0.8f);  // Set dim amount, 0.0 for no dim and 1.0 for full dim
        }



    }

}