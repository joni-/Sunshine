package app.com.example.joni.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.net.Uri;

/**
 * Created by Joni on 7.10.2014.
 */
public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        uriMatcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);
        uriMatcher.addURI(authority, WeatherContract.PATH_LOCATION + "/#", LOCATION_ID);

        return uriMatcher;
    }

    private static final SQLiteQueryBuilder sWeatherByLocationQueryBuilder;

    static {
        sWeatherByLocationQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN "
                        + WeatherContract.LocationEntry.TABLE_NAME
                        + " ON " + WeatherContract.WeatherEntry.TABLE_NAME
                        + "." + WeatherContract.WeatherEntry.COLUMN_LOCATION_KEY
                        + " = "
                        + WeatherContract.LocationEntry.TABLE_NAME
                        + "." + WeatherContract.LocationEntry._ID
        );
    }

    private static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME + "."
                    + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    private static final String sLocationSettingWithStartDate =
            WeatherContract.LocationEntry.TABLE_NAME + "."
                    + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
                    + " = ? AND "
                    + WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_DATE_TEXT + " >= ? ";

    private static final String sLocationSettingWithDate =
            WeatherContract.LocationEntry.TABLE_NAME + "."
                    + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
                    + " = ? AND "
                    + WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_DATE_TEXT + " = ? ";

    private WeatherDbHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor = null;
        switch (uriMatcher.match(uri)) {
            case WEATHER_WITH_LOCATION_AND_DATE:
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            case WEATHER_WITH_LOCATION:
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            case WEATHER:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case LOCATION:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case LOCATION_ID:
                final long locationId = ContentUris.parseId(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        WeatherContract.LocationEntry._ID + " = " + locationId,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[] { locationSetting};
        } else {
            selection = sLocationSettingWithStartDate;
            selectionArgs = new String[] { locationSetting, startDate };
        }

        return sWeatherByLocationQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String date = WeatherContract.WeatherEntry.getDateFromUri(uri);
        return sWeatherByLocationQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingWithDate,
                new String[] { locationSetting, date },
                null,
                null,
                sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return WeatherContract.LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri = null;
        switch (uriMatcher.match(uri)) {
            case WEATHER:
                long weatherId = mOpenHelper.getWritableDatabase().insert(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        null,
                        values
                );
                if (weatherId > 0)
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(weatherId);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            case LOCATION:
                long locationId = mOpenHelper.getWritableDatabase().insert(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        null,
                        values
                );
                if (locationId > 0)
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(locationId);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;
        switch (uriMatcher.match(uri)) {
            case WEATHER:
                rowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case LOCATION:
                rowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        switch (uriMatcher.match(uri)) {
            case WEATHER:
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            case LOCATION:
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
