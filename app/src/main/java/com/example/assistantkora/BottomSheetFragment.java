package com.example.assistantkora;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = "BottomSheetFragment";

    private TextView Location_Change, Weather_Change, Temp_change, Wind_change, Sens_change, Humididty_change, Dia_mes_hora;

    private ImageView Icon_Image;

    // Factory method to create a new instance of BottomSheetFragment
    public static BottomSheetFragment newInstance(String userInput) {
        Bundle args = new Bundle();
        args.putString("userInput", userInput);
        BottomSheetFragment fragment = new BottomSheetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        // Find your views here
        Location_Change = view.findViewById(R.id.name_location_label);
        Weather_Change = view.findViewById(R.id.weather_state_label);
        Temp_change = view.findViewById(R.id.degree_text);
        Wind_change = view.findViewById(R.id.windspeed_text);
        Sens_change = view.findViewById(R.id.feelslike_text);
        Humididty_change = view.findViewById(R.id.humidity_text);
        Icon_Image = view.findViewById(R.id.icon);
        Dia_mes_hora = view.findViewById(R.id.Dia_mes_hora);

        // Retrieve the user input from arguments
        String userInput = getArguments().getString("userInput");

        // Define url
        String url = "http://kora.us.to/file/request/weather.php";

        // Execute AsyncTask to fetch weather data
        new ChatTask().execute(userInput, url);

        // Update the date and time continuously
        updateTime();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // Remove the listener
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void updateTime() {
        // Create a Calendar instance to get the current date and time in GMT+1 time zone
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        calendar.setTimeInMillis(System.currentTimeMillis() + TimeZone.getTimeZone("GMT+1").getOffset(System.currentTimeMillis()));

        // Format the date and time using the specified format and Portuguese locale
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE MMM dd | HH:mm", new Locale("pt", "BR"));
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Capitalize the first letter of the day of the week and the month
        String formattedDate = capitalizeFirstLetter(dateFormat.format(calendar.getTime()));

        // Set the formatted date and time to the TextView
        Dia_mes_hora.setText(formattedDate);

        // Update the date and time every minute
        Dia_mes_hora.postDelayed(this::updateTime, 60000);
    }

    private String capitalizeFirstLetter(String str) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    private class ChatTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String userInput = params[0];
            String url = params[1];
            try {
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String requestBody = "localiza=" + userInput;

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestBody.getBytes());
                outputStream.flush();
                outputStream.close();

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();

                return new JSONObject(response.toString());

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error in network request", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    if (jsonObject.has("location")) {
                        Location_Change.setText(jsonObject.getString("location"));
                    }
                    if (jsonObject.has("temperature")) {
                        Temp_change.setText(" " + jsonObject.getString("temperature") + "º");
                    }
                    if (jsonObject.has("humidity")) {
                        Humididty_change.setText(jsonObject.getString("humidity") + "%");
                    }
                    if (jsonObject.has("condition_text")) {
                        Weather_Change.setText(jsonObject.getString("condition_text"));
                    }
                    if (jsonObject.has("wind_kph")) {
                        Wind_change.setText(jsonObject.getString("wind_kph") + " km/h");
                    }
                    if (jsonObject.has("feelslike_c")) {
                        Sens_change.setText(jsonObject.getString("feelslike_c") + " °C");
                    }
                    if (jsonObject.has("icon")) {
                        String iconUrl = "http:" + jsonObject.getString("icon").replace("64x64", "128x128");
                        Glide.with(BottomSheetFragment.this)
                                .asBitmap()
                                .load(iconUrl)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                        int width = resource.getWidth();
                                        int height = resource.getHeight();
                                        // Increase size by 25%
                                        int newWidth = (int) (width * 1.25);
                                        int newHeight = (int) (height * 1.25);

                                        // Create a resized bitmap
                                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, newWidth, newHeight, false);

                                        // Set the bitmap to the ImageView
                                        Icon_Image.setImageBitmap(resizedBitmap);
                                    }
                                });
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "JSON Parsing error", e);
                }
            }
        }
    }
}