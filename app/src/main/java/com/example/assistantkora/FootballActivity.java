package com.example.assistantkora;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class FootballActivity extends AppCompatActivity {
    private LinearLayout fixturesContainer;
    private final String apiKey = "2a2219a704a5aa9950d5a90ccab7b0548b0c0d2a58682ca3f1833b6484c69864";
    private final String fromDate = "2024-06-14";
    private final String toDate = "2024-06-20";
    private final String url = "https://apiv2.allsportsapi.com/football/?met=Fixtures&APIkey=" + apiKey + "&from=" + fromDate + "&to=" + toDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_football);
        fixturesContainer = findViewById(R.id.fixturesContainer);
        new FetchFixturesTask().execute(url);
    }

    private class FetchFixturesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray fixtures = jsonObject.getJSONArray("result");
                displayFixtures(fixtures);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayFixtures(JSONArray fixtures) throws JSONException {
        HashMap<Integer, String> leagues = new HashMap<>();
        leagues.put(1, "UEFA European Championship");
        leagues.put(175, "Bundesliga");
        leagues.put(168, "Ligue 1");
        leagues.put(152, "Premier League");
        leagues.put(302, "La Liga");
        leagues.put(207, "Serie A");
        leagues.put(266, "Liga Portugal");

        HashMap<Integer, JSONArray> leagueFixtures = new HashMap<>();

        for (int i = 0; i < fixtures.length(); i++) {
            JSONObject event = fixtures.getJSONObject(i);
            int leagueKey = event.getInt("league_key");

            if (!leagues.containsKey(leagueKey)) continue;

            if (!leagueFixtures.containsKey(leagueKey)) {
                leagueFixtures.put(leagueKey, new JSONArray());
            }

            leagueFixtures.get(leagueKey).put(event);
        }

        for (Integer leagueKey : leagues.keySet()) {
            TextView leagueTitle = new TextView(this);
            leagueTitle.setText(leagues.get(leagueKey));
            leagueTitle.setTextSize(24);
            fixturesContainer.addView(leagueTitle);

            JSONArray events = leagueFixtures.get(leagueKey);
            if (events == null || events.length() == 0) {
                TextView noGamesMessage = new TextView(this);
                noGamesMessage.setText("There are no more games.");
                noGamesMessage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                fixturesContainer.addView(noGamesMessage);
            } else {
                for (int j = 0; j < events.length(); j++) {
                    JSONObject event = events.getJSONObject(j);

                    LinearLayout fixtureLayout = new LinearLayout(this);
                    fixtureLayout.setOrientation(LinearLayout.VERTICAL);
                    fixtureLayout.setPadding(10, 10, 10, 10);
                    fixtureLayout.setBackgroundResource(R.drawable.fixture_background);

                    TextView eventTime = new TextView(this);
                    eventTime.setText(event.getString("event_date") + " " + event.getString("event_time"));
                    fixtureLayout.addView(eventTime);

                    LinearLayout teamsLayout = new LinearLayout(this);
                    teamsLayout.setOrientation(LinearLayout.HORIZONTAL);

                    ImageView homeTeamLogo = new ImageView(this);
                    new LoadImageTask(homeTeamLogo).execute(event.getString("home_team_logo"));
                    teamsLayout.addView(homeTeamLogo);

                    TextView teams = new TextView(this);
                    teams.setText(event.getString("event_home_team") + " vs " + event.getString("event_away_team"));
                    teams.setPadding(10, 0, 10, 0);
                    teamsLayout.addView(teams);

                    ImageView awayTeamLogo = new ImageView(this);
                    new LoadImageTask(awayTeamLogo).execute(event.getString("away_team_logo"));
                    teamsLayout.addView(awayTeamLogo);

                    fixtureLayout.addView(teamsLayout);

                    TextView leagueName = new TextView(this);
                    leagueName.setText("League: " + event.getString("league_name"));
                    fixtureLayout.addView(leagueName);

                    TextView venue = new TextView(this);
                    venue.setText("Venue: " + event.getString("event_stadium"));
                    fixtureLayout.addView(venue);

                    String status = event.getString("event_status").isEmpty() ? "To be disputed" : event.getString("event_status");
                    TextView eventStatus = new TextView(this);
                    eventStatus.setText("Status: " + status);
                    fixtureLayout.addView(eventStatus);

                    fixturesContainer.addView(fixtureLayout);
                }
            }
        }
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream input = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}