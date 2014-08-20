package app.com.example.joni.sunshine;

import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static String TAG = "PlaceholderFragment";

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            List<String> forecastData = getForecastData();
            ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.list_item_forecast,
                    R.id.list_item_forecast_textview, forecastData);
            ListView forecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
            forecastList.setAdapter(adapter);

            return rootView;
        }

        private List<String> getForecastData() {
            String jsonForecast = readForecastData();
            List<String> forecastData = parseForecastData(jsonForecast);
            return forecastData;
        }

        private String readForecastData() {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonResponse = null;

            try {
                String link = "http://api.openweathermap.org/data/2.5/forecast/daily?q=Joensuu&mode=json&units=metric&cnt=7";

                // TODO: Remove this hack and create ASyncTask for downloading the weather data
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                URL url = new URL(link);

                Log.i(PlaceholderFragment.TAG, String.format("Reading data from %s", link));

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    jsonResponse = null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                jsonResponse = buffer.toString();
            } catch (IOException e) {
                Log.e(PlaceholderFragment.TAG, "Error while reading data from Open Weather Map", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(PlaceholderFragment.TAG, "Error closing BufferedReader stream", e);
                    }
                }
            }

            return jsonResponse;
        }

        private List<String> parseForecastData(String jsonForecast) {
            return new ArrayList<String>();
        }
    }
}
