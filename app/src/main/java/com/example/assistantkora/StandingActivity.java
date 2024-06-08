package com.example.assistantkora;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StandingActivity extends AppCompatActivity {

    private static final String API_KEY = "2a2219a704a5aa9950d5a90ccab7b0548b0c0d2a58682ca3f1833b6484c69864";
    private static final String[] LEAGUES = {"Primeira Liga", "La Liga", "Premier League", "Serie A", "Bundesliga", "Ligue 1"};
    private static final String[] COUNTRIES = {"Portugal", "Spain", "England", "Italy", "Germany", "France"};
    private static final String BASE_URL = "https://apiv2.allsportsapi.com/football/?&met=Standings&leagueId=";
    private TableLayout standingsTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standing);

        standingsTableLayout = findViewById(R.id.standings_table_layout);
        fetchStandings();
    }

    private void fetchStandings() {
        for (int i = 0; i < LEAGUES.length; i++) {
            new FetchStandingsTask().execute(BASE_URL + getLeagueId(i) + "&APIkey=" + API_KEY, LEAGUES[i]);
        }
    }

    private String getLeagueId(int index) {
        switch (index) {
            case 0:
                return "266"; // Primeira Liga
            case 1:
                return "302"; // La Liga
            case 2:
                return "152"; // Premier League
            case 3:
                return "207"; // Serie A
            case 4:
                return "175"; // Bundesliga
            case 5:
                return "168"; // Ligue 1
            default:
                return "";
        }
    }

    private class FetchStandingsTask extends AsyncTask<String, Void, List<JSONObject>> {
        @Override
        protected List<JSONObject> doInBackground(String... urls) {
            List<JSONObject> standingsList = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray standingsArray = jsonObject.getJSONObject("result").getJSONArray("total");
                for (int i = 0; i < standingsArray.length(); i++) {
                    JSONObject teamObject = standingsArray.getJSONObject(i);
                    standingsList.add(teamObject);
                }
            } catch (IOException | JSONException e) {
                Log.e("FetchStandingsTask", "Error fetching standings", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return standingsList;
        }

        @Override
        protected void onPostExecute(List<JSONObject> standingsList) {
            super.onPostExecute(standingsList);
            displayStandings(standingsList);
        }
    }

    private void displayStandings(List<JSONObject> standingsList) {
        for (JSONObject standing : standingsList) {
            TableRow row = new TableRow(this);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 8, 0, 8); // Add some margin between rows
            row.setLayoutParams(layoutParams);

            try {
                // Position
                TextView position = new TextView(this);
                position.setText(String.valueOf(standing.getInt("standing_place")));
                position.setPadding(16, 8, 16, 8); // Padding for better readability
                position.setTextColor(Color.BLACK); // Set text color
                position.setBackgroundResource(R.drawable.cell_background); // Add background drawable
                row.addView(position);

                // Team Logo
                ImageView logo = new ImageView(this);
                Glide.with(this).load(standing.getString("team_logo")).into(logo);
                row.addView(logo);

                // Team Name
                TextView teamName = new TextView(this);
                teamName.setText(standing.getString("standing_team"));
                teamName.setPadding(16, 8, 16, 8);
                teamName.setTextColor(Color.BLACK);
                teamName.setBackgroundResource(R.drawable.cell_background);
                row.addView(teamName);

                // Played, Won, Drawn, Lost
                addStandingDetail(row, String.valueOf(standing.getInt("standing_P")));
                addStandingDetail(row, String.valueOf(standing.getInt("standing_W")));
                addStandingDetail(row, String.valueOf(standing.getInt("standing_D")));
                addStandingDetail(row, String.valueOf(standing.getInt("standing_L")));

                // Goals For, Goals Against, Goal Difference, Points
                addStandingDetail(row, String.valueOf(standing.getInt("standing_F")));
                addStandingDetail(row, String.valueOf(standing.getInt("standing_A")));
                addStandingDetail(row, String.valueOf(standing.getInt("standing_GD")));
                addStandingDetail(row, String.valueOf(standing.getInt("standing_PTS")));

                // Set background color based on standing type
                String placeType = standing.getString("standing_place_type");
                if (placeType.contains("Promotion - Champions League")) {
                    row.setBackgroundColor(getResources().getColor(R.color.champions_league));
                } else if (placeType.contains("Promotion - Europa League")) {
                    row.setBackgroundColor(getResources().getColor(R.color.europa_league));
                } else if (placeType.contains("Promotion - Conference League")) {
                    row.setBackgroundColor(getResources().getColor(R.color.conference_league));
                }

                standingsTableLayout.addView(row);
            } catch (JSONException e) {
                Log.e("DisplayStandings", "Error parsing JSON", e);
            }
        }
    }

    // Helper method to add standing detail TextView to TableRow
    private void addStandingDetail(TableRow row, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundResource(R.drawable.cell_background);
        row.addView(textView);
    }
}
