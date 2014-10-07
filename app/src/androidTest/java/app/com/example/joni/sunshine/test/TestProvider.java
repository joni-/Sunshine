package app.com.example.joni.sunshine.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.test.AndroidTestCase;

import app.com.example.joni.sunshine.data.WeatherContract.LocationEntry;
import app.com.example.joni.sunshine.data.WeatherContract.WeatherEntry;
import app.com.example.joni.sunshine.data.WeatherDbHelper;

/**
 * Created by joni.nevalainen on 4.10.2014.
 */
public class TestProvider extends AndroidTestCase {
    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "Joensuu";
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation(testLocation));
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20141007";
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadDb() throws Throwable {
        WeatherDbHelper dbHelper = new WeatherDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues locationValues = getLocationValues();
        db.insert(LocationEntry.TABLE_NAME, null, locationValues);

        Cursor locationCursor = db.rawQuery("SELECT * FROM " + LocationEntry.TABLE_NAME, null);
        validateCursor(locationCursor, locationValues);

        int locationRowId = locationCursor.getInt(locationCursor.getColumnIndex(LocationEntry._ID));
        ContentValues weatherValues = getWeatherData(locationRowId);
        db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

        Cursor weatherCursor = db.rawQuery("SELECT * FROM " + WeatherEntry.TABLE_NAME, null);
        validateCursor(weatherCursor, weatherValues);

        dbHelper.close();
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
        locationValues.put(LocationEntry.COLUMN_CITY, "Joensuu");
        locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, "Dummy");
        locationValues.put(LocationEntry.COLUMN_LON, 62.25);
        locationValues.put(LocationEntry.COLUMN_LAT, 29.24);
        return locationValues;
    }

    private ContentValues getWeatherData(int locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOCATION_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATE_TEXT, "20141205");
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
