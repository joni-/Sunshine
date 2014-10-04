package app.com.example.joni.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import app.com.example.joni.sunshine.data.WeatherContract.WeatherEntry;
import app.com.example.joni.sunshine.data.WeatherContract.LocationEntry;

/**
 * Created by joni.nevalainen on 4.10.2014.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sunshine.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + "("
                + LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

                + LocationEntry.COLUMN_CITY + " TEXT NOT NULL, "
                + LocationEntry.COLUMN_LOCATION_SETTING + " TEXT NOT NULL, "
                + LocationEntry.COLUMN_LON + " REAL NOT NULL, "
                + LocationEntry.COLUMN_LAT + " REAL NOT NULL);";

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + "("
                + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

                + WeatherEntry.COLUMN_LOCATION_KEY + " INTEGER NOT NULL, "
                + WeatherEntry.COLUMN_DATE_TEXT + " TEXT NOT NULL, "
                + WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, "
                + WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, "

                + WeatherEntry.COLUMN_TEMP_MIN + " REAL NOT NULL, "
                + WeatherEntry.COLUMN_TEMP_MAX + " REAL NOT NULL, "

                + WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, "
                + WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, "
                + WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, "
                + WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, "

                // Foreign key to location table
                + "FOREIGN KEY (" + WeatherEntry.COLUMN_LOCATION_KEY + ") REFERENCES "
                + LocationEntry.TABLE_NAME + "(" + LocationEntry._ID + "), "

                // Ensure that there exist only one entry for date-location pair
                + "UNIQUE (" + WeatherEntry.COLUMN_DATE_TEXT + ", "
                + WeatherEntry.COLUMN_LOCATION_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
