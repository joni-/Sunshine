package app.com.example.joni.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import app.com.example.joni.sunshine.data.WeatherContract;
import app.com.example.joni.sunshine.data.WeatherContract.WeatherEntry;
import app.com.example.joni.sunshine.data.WeatherContract.LocationEntry;

/**
 * Created by Joni on 20.8.2014.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE_TEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_TEMP_MAX,
            WeatherEntry.COLUMN_TEMP_MIN,
            LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

    final static String SELECTED_FORECAST_KEY = "selected_forecast";

    private final static String TAG = ForecastFragment.class.getSimpleName();

    private ListView forecastList;
    private CursorAdapter forecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        forecastList = (ListView) rootView.findViewById(R.id.listview_forecast);

        Uri uri = WeatherEntry.buildWeatherLocationWithStartDate(
                Utility.getPreferredLocation(getActivity()),
                WeatherContract.getDbDateString(new Date()));

        Cursor forecastCursor = getActivity().getContentResolver().query(
                uri,
                FORECAST_COLUMNS,
                null,
                null,
                null);

        forecastAdapter = new ForecastAdapter(getActivity(), forecastCursor, 0);
        forecastList.setAdapter(forecastAdapter);

        forecastList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                CursorAdapter adapter = (CursorAdapter) adapterView.getAdapter();
                Cursor cursor = adapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    // Construct string for detail view
                    boolean isMetric = Utility.isMetric(getActivity());
                    String date = Utility.formatDate(cursor.getString(COL_WEATHER_DATE));
                    String forecast = cursor.getString(COL_WEATHER_DESC);
                    String high = Utility.formatTemperature(
                            cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
                    String low = Utility.formatTemperature(
                            cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
                    String forecastString = String.format(
                            "%s - %s - %s/%s", date, forecast, high, low);

                    // Open detail view
                    String dateString = cursor.getString(COL_WEATHER_DATE);
                    Intent i = new Intent(getActivity(), DetailActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, dateString);
                    startActivity(i);
                }
            }
        });

        refreshWeatherData();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !Utility.getPreferredLocation(getActivity()).equals(mLocation)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    private void refreshWeatherData() {
        FetchWeatherTask weatherDataTask = new FetchWeatherTask(getActivity());
        weatherDataTask.execute(Utility.getPreferredLocation(getActivity()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refreshWeatherData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String startDate = WeatherContract.getDbDateString(new Date());
        String sortOrder = WeatherEntry.COLUMN_DATE_TEXT + " ASC";
        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);
        return new CursorLoader(
                getActivity(),
                weatherLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        forecastAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        forecastAdapter.swapCursor(null);
    }
}
