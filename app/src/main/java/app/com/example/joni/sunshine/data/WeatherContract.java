package app.com.example.joni.sunshine.data;

import android.provider.BaseColumns;

/**
 * Created by joni.nevalainen on 4.10.2014.
 */
public class WeatherContract {

    /**
     * Defines contents for weather table
     */
    public static final class WeatherEntry implements BaseColumns {
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
    }


    /**
     * Defines contents for location table
     */
    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";

        public static final String COLUMN_CITY = "city";

        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        // Longitude and latitude as floats
        public static final String COLUMN_LON = "lon";
        public static final String COLUMN_LAT = "lat";


    }
}
