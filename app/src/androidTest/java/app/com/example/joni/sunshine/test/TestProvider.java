package app.com.example.joni.sunshine.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.test.AndroidTestCase;

import app.com.example.joni.sunshine.data.WeatherContract;
import app.com.example.joni.sunshine.data.WeatherContract.LocationEntry;
import app.com.example.joni.sunshine.data.WeatherContract.WeatherEntry;
import app.com.example.joni.sunshine.data.WeatherDbHelper;

/**
 * Created by joni.nevalainen on 4.10.2014.
 */
public class TestProvider extends AndroidTestCase {

    private static final String TEST_CITY = "Joensuu";
    private static final String TEST_DATE = "20141008";

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation(TEST_CITY));
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(TEST_CITY, TEST_DATE));
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testDeleteLocationProvider() {
        ContentValues locationValues = getLocationValues();
        Uri uri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);
        long locationId = ContentUris.parseId(uri);

        // Delete the inserted record
        int rowsDeleted = mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI, LocationEntry._ID + " = ? ",
                new String[] { String.valueOf(locationId) });
        assertEquals(1, rowsDeleted);
    }

    public void testDeleteWeatherProvider() {
        ContentValues locationValues = getLocationValues();
        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);
        long locationId = ContentUris.parseId(locationUri);

        ContentValues weatherValues = getWeatherData(locationId);
        Uri weatherUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        long weatherId = ContentUris.parseId(weatherUri);

        int rowsDeleted = mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI, WeatherEntry._ID + " = ? ",
                new String[] { String.valueOf(weatherId) }
        );
        assertEquals(1, rowsDeleted);
    }

    public void testInsertReadProvider() throws Throwable {
        ContentValues locationValues = getLocationValues();
        mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);

        Cursor locationCursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        validateCursor(locationCursor, locationValues);

        int locationRowId = locationCursor.getInt(locationCursor.getColumnIndex(LocationEntry._ID));
        ContentValues weatherValues = getWeatherData(locationRowId);
        Uri weatherUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, weatherValues);

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        validateCursor(weatherCursor, weatherValues);

        locationCursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null
        );
        locationValues.put(LocationEntry._ID, locationRowId);
        validateCursor(locationCursor, locationValues);

        weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocation(TEST_CITY),
                null,
                null,
                null,
                null
        );
        validateCursor(weatherCursor, weatherValues);

        weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocationWithDate(TEST_CITY, TEST_DATE),
                null,
                null,
                null,
                null
        );
        validateCursor(weatherCursor, weatherValues);

        locationCursor.close();
        weatherCursor.close();
    }

    /**
     * Checks that all values in the cursors first row match the values defined in contentValues.
     * @param cursor
     * @param contentValues
     */
    private void validateCursor(Cursor cursor, ContentValues contentValues) {
        if (!cursor.moveToFirst()) {
            fail("Cursor is empty.");
        }

        for (String key : contentValues.keySet()) {
            int index = cursor.getColumnIndex(key);
            assertTrue("Column " + key + " was not found in results.", index != -1);
            String error = "Values do not match for field " + key;
            switch (cursor.getType(index)) {
                case Cursor.FIELD_TYPE_STRING:
                    String resultString = cursor.getString(index);
                    assertTrue(error, resultString.equals(contentValues.get(key)));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    int resultInt = cursor.getInt(index);
                    assertTrue(error, resultInt == contentValues.getAsInteger(key));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    float resultFloat = cursor.getFloat(index);
                    assertTrue(error, resultFloat == contentValues.getAsFloat(key));
                    break;
                default:
                    fail("Unknown type for column " + key);
                    break;
            }
        }
    }

    private ContentValues getLocationValues() {
        ContentValues locationValues = new ContentValues();
        locationValues.put(LocationEntry.COLUMN_CITY, TEST_CITY);
        locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, TEST_CITY);
        locationValues.put(LocationEntry.COLUMN_LON, 62.25);
        locationValues.put(LocationEntry.COLUMN_LAT, 29.24);
        return locationValues;
    }

    private ContentValues getWeatherData(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOCATION_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATE_TEXT, TEST_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_TEMP_MAX, 75);
        weatherValues.put(WeatherEntry.COLUMN_TEMP_MIN, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }
}
