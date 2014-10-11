package app.com.example.joni.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.com.example.joni.sunshine.data.WeatherContract;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
            implements LoaderManager.LoaderCallbacks<Cursor> {

        // For the forecast view we're showing only a small subset of the stored data.
        // Specify the columns we need.
        private static final String[] FORECAST_COLUMNS = {
                // In this case the id needs to be fully qualified with a table name, since
                // the content provider joins the location & weather tables in the background
                // (both have an _id column)
                // On the one hand, that's annoying.  On the other, you can search the weather table
                // using the location set by the user, which is only in the Location table.
                // So the convenience is worth it.
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE_TEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_TEMP_MAX,
                WeatherContract.WeatherEntry.COLUMN_TEMP_MIN,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
        };

        // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        public static final int COL_WEATHER_ID = 0;
        public static final int COL_WEATHER_DATE = 1;
        public static final int COL_WEATHER_DESC = 2;
        public static final int COL_WEATHER_MAX_TEMP = 3;
        public static final int COL_WEATHER_MIN_TEMP = 4;
        public static final int COL_LOCATION_SETTING = 5;

        private static final int FORECAST_LOADER = 0;
        private static final String HASHTAG = "#SunshineApp";

        private ShareActionProvider shareActionProvider;
        private String forecastData;
        private String mDateString;

        public PlaceholderFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailfragment, menu);
            MenuItem item = menu.findItem(R.id.detail_menu_share);
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createForecastShareIntent());
            }
        }

        private Intent createForecastShareIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, forecastData + " " + HASHTAG);
            return shareIntent;
        }

        private void setShareIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "#Sunshine");

            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(shareIntent);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            TextView forecast = (TextView) rootView.findViewById(R.id.selected_forecast_text);
            Intent intent = getActivity().getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                forecastData = extras.getString(Intent.EXTRA_TEXT);
                forecast.setText(forecastData);
                mDateString = extras.getString(Intent.EXTRA_TEXT);
            }
            return rootView;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String location = Utility.getPreferredLocation(getActivity());
            Uri weatherUri = WeatherContract.WeatherEntry
                    .buildWeatherLocationWithDate(location, mDateString);
            return new CursorLoader(getActivity(), weatherUri, FORECAST_COLUMNS, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            if (cursor != null && cursor.moveToFirst()) {
                boolean isMetric = Utility.isMetric(getActivity());

                // Get data from cursor
                String dateString = cursor.getString(COL_WEATHER_DATE);
                String forecast = cursor.getString(COL_WEATHER_DESC);
                String high = Utility.formatTemperature(
                        cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
                String low = Utility.formatTemperature(
                        cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

                // set data
                String text = String.format("%s - %s - %s/%s", dateString, forecast, high, low);
                ((TextView)getActivity().findViewById(R.id.selected_forecast_text))
                        .setText(text);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }

    }
}
