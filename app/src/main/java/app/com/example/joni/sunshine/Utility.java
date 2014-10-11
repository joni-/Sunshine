package app.com.example.joni.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;

import app.com.example.joni.sunshine.data.WeatherContract;

/**
 * Created by joni.nevalainen on 9.10.2014.
 */
public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String selectedCity = prefs.getString(
                context.getString(R.string.pref_city_key),
                context.getString(R.string.pref_city_default));
        return selectedCity;
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String selectedUnits = prefs.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_default));
        return selectedUnits.equals(context.getString(R.string.pref_units_default));
    }

    public static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    public static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }
}
