package app.com.example.joni.sunshine.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by joni.nevalainen on 4.10.2014.
 */
public class WeatherContract {
    public static final String CONTENT_AUTHORITY = "com.example.joni.sunshine.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    /**
     * Defines contents for weather table
     */
    public static final class WeatherEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_WEATHER).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static final String TABLE_NAME = "weather";
        // Foreign key to location entry
        public static final String COLUMN_LOCATION_KEY = "location_id";
        // Date in format yyyy-MM-dd
        public static final String COLUMN_DATE_TEXT = "date";
        // Weather id from the API (defines the icon to be used)
        public static final String COLUMN_WEATHER_ID = "weather_id";

        // Short description of the weather
        public static final String COLUMN_SHORT_DESC = "short_desc";

        // Min and max temperatures for the day (as floats)
        public static final String COLUMN_TEMP_MIN = "min";
        public static final String COLUMN_TEMP_MAX = "max";

        // Humidity as float to represent percentage
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_PRESSURE = "pressure";

        // Windspeed as float
        public static final String COLUMN_WIND_SPEED = "wind_speed";

        public static final String COLUMN_DEGREES = "degrees";


        public static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate(
                String locationSetting, String startDate) {
            return CONTENT_URI.buildUpon()
                    .appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATE_TEXT, startDate).build();
        }

        public static Uri buildWeatherLocationWithDate(
                String locationSetting, String date) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
        }

        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_DATE_TEXT);
        }
    }


    /**
     * Defines contents for location table
     */
    public static final class LocationEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String TABLE_NAME = "location";

        public static final String COLUMN_CITY = "city";

        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        // Longitude and latitude as floats
        public static final String COLUMN_LON = "lon";
        public static final String COLUMN_LAT = "lat";


        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final String DATE_FORMAT = "yyyyMMdd";
    public static String getDbDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }
    public static Date getDateFromDb(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
