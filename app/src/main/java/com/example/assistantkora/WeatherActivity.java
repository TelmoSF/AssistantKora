package com.example.assistantkora;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

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

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView Location_Change, Weather_Change, Temp_change, Wind_change, Sens_change, Humididty_change, Dia_mes_hora;
    private ImageView Icon_Image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        Location_Change = findViewById(R.id.name_location_label);
        Weather_Change = findViewById(R.id.weather_state_label);
        Temp_change = findViewById(R.id.degree_text);
        Wind_change = findViewById(R.id.windspeed_text);
        Sens_change = findViewById(R.id.feelslike_text);
        Humididty_change = findViewById(R.id.humidity_text);
        Icon_Image = findViewById(R.id.icon);
        Dia_mes_hora = findViewById(R.id.Dia_mes_hora);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocationAndFetchWeather();
        }

        // Define userInput and url
        //String userInput = "Mem Martins";


        //new WeatherTask().execute(userInput);

        // Update the date and time continuously
        updateTime();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLocationAndFetchWeather();
        }
    }

    private void getLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permissões, se não estiverem concedidas
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String locationStr = location.getLatitude() + "," + location.getLongitude();
                            new WeatherActivity.WeatherTask().execute(locationStr);
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchWeather();
            } else {
                // Permissão negada, mostre uma mensagem ao usuário
            }
        }
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

    // Method to capitalize the first letter of each word in a string
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



    private class WeatherTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String location = params[0];
            try {
                URL url = new URL("http://kora.us.to/file/request/weather.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String requestBody = "localiza=" + location;
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
                e.printStackTrace();
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
                        Glide.with(WeatherActivity.this)
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