package app.com.example.joni.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import app.com.example.joni.sunshine.data.WeatherContract;
import app.com.example.joni.sunshine.data.WeatherProvider;

import static app.com.example.joni.sunshine.data.WeatherContract.*;

/**
 * Created by joni.nevalainen on 9.10.2014.
 */
public class FetchWeatherTask extends AsyncTask<String, String[], String[]> {
    private final String TAG = FetchWeatherTask.class.getSimpleName();

    private String locationQuery;

    private Context mContext;

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    @Override
    protected String[] doInBackground(String... params) {
        Log.v(TAG, "Fetching forecast data");
        if (params == null || params.length == 0) {
            return null;
        }
        locationQuery = params[0];
        String[] forecastData = getForecastData();
        return forecastData;
    }

    private String[] getForecastData() {
        String jsonForecast = readForecastData();
        String[] forecastData = parseForecastData(jsonForecast);
        return forecastData;
    }

    private String readForecastData() {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonResponse = null;

        try {
            String link = UriHelper.getAPILinkByQuery(locationQuery);

            URL url = new URL(link);

            Log.v(TAG, String.format("Reading data from %s", link));

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
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
            Log.v(TAG, String.format("Response from %s: %s", new Object[]{link, jsonResponse}));
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

    private String[] parseForecastData(String jsonForecast) {
        try {
            return getWeatherDataFromJson(jsonForecast, 7);
        } catch (JSONException e) {
            Log.e(TAG, "Error while parsing weather json.", e);
            return new String[0];
        }
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_COORD_LAT = "lat";
        final String OWM_COORD_LON = "lon";

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";

        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJSON = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJSON.getString(OWM_CITY_NAME);
        JSONObject coordJSON = cityJSON.getJSONObject(OWM_COORD);
        double lat = coordJSON.getDouble(OWM_COORD_LAT);
        double lon = coordJSON.getDouble(OWM_COORD_LON);

        Log.v(TAG, cityName + ", lat: " + lat + " lon: " + lon);
        long locationId = addlocation(locationQuery, cityName, lat, lon);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(numDays);

        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {

            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            dateTime = dayForecast.getLong(OWM_DATETIME);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mContext.getApplicationContext());

            String selectedUnits = prefs.getString(
                    mContext.getString(R.string.pref_units_key),
                    mContext.getString(R.string.pref_units_default));
            if (!selectedUnits.equals(mContext.getString(R.string.pref_units_default))) {
                // Use Fahrenheit
                high = high * 9.0/5.0 + 32;
                low = low * 9.0/5.0 + 32;
            }

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherEntry.COLUMN_LOCATION_KEY, locationId);
            weatherValues.put(WeatherEntry.COLUMN_DATE_TEXT,
                    WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_TEMP_MAX, high);
            weatherValues.put(WeatherEntry.COLUMN_TEMP_MIN, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);

            String highAndLow = formatHighLows(high, low);
            String day = getReadableDateString(dateTime);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        addWeatherValues(cVVector);

        return resultStrs;
    }

    private void addWeatherValues(Vector<ContentValues> cVVector) {
        if (cVVector.size() > 0) {
            Log.v(TAG, "Inserting " + cVVector.size() + " weather entries");
            int rowsInserted = mContext.getContentResolver().bulkInsert(
                    WeatherEntry.CONTENT_URI,
                    cVVector.toArray(new ContentValues[cVVector.size()])
            );
            Log.v(TAG, "Finished inserting " + rowsInserted + " entries to database");
        }
    }

    private long addlocation(String locationSetting, String cityName, double lat, double lon) {
        Log.v(TAG, "Inserting location " + locationSetting + " to database.");
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[] { locationSetting },
                null
        );
        long id;
        if (cursor.getCount() != 0) {
            Log.v(TAG, "Location was already in the database.");
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(LocationEntry._ID);
            id = cursor.getLong(index);
        } else {
            Log.v(TAG, "Location do not exist in the database, doing the insertion.");
            ContentValues values = new ContentValues();
            values.put(LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            values.put(LocationEntry.COLUMN_CITY, cityName);
            values.put(LocationEntry.COLUMN_LAT, lat);
            values.put(LocationEntry.COLUMN_LON, lon);
            Uri uri = mContext.getContentResolver().insert(
                    LocationEntry.CONTENT_URI,
                    values
            );
            id = ContentUris.parseId(uri);
        }
        cursor.close();
        return id;
    }
}
