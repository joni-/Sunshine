package app.com.example.joni.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Joni on 20.8.2014.
 */
public class ForecastFragment extends Fragment {

    private final static String TAG = ForecastFragment.class.getSimpleName();

    private ListView forecastList;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);

        forecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
        new GetAndShowWeatherDataTask().execute();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            new GetAndShowWeatherDataTask().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetAndShowWeatherDataTask extends AsyncTask {

        private final String TAG = GetAndShowWeatherDataTask.class.getSimpleName();

        private List<String> forecastData;

        @Override
        protected Object doInBackground(Object[] params) {
            Log.v(TAG, "Fetching forecast data");
            forecastData = getForecastData();
            return forecastData;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            Log.v(TAG, "Setting the adapter");

            ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.list_item_forecast,
                    R.id.list_item_forecast_textview, forecastData);
            forecastList.setAdapter(adapter);
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

                URL url = new URL(link);

                Log.i(TAG, String.format("Reading data from %s", link));

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
                Log.e(TAG, "Error while reading data from Open Weather Map", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing BufferedReader stream", e);
                    }
                }
            }

            return jsonResponse;
        }

        private List<String> parseForecastData(String jsonForecast) {
            return new ArrayList<String>(Arrays.asList(new String[]{"TESTI1", "TESTI2", "TESTI3"}));
        }

    }
}
