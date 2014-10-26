package app.com.example.joni.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    public static String getFriendlyDayString(Context context, String dateString) {
        Calendar weekInFuture = Calendar.getInstance();
        weekInFuture.setTime(new Date());
        weekInFuture.add(Calendar.DATE, 7);

        Calendar inputCalendar = Calendar.getInstance();
        inputCalendar.setTime(WeatherContract.getDateFromDb(dateString));

        if (inputCalendar.before(weekInFuture)) {
            return getDayString(context, dateString);
        } else {
            return getDateStringWithMonth(dateString);
        }
    }

    private static String getDateStringWithMonth(String dateString) {
        return new SimpleDateFormat("EEE MMM dd").format(WeatherContract.getDateFromDb(dateString));
    }

    private static String getDayString(Context context, String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat(WeatherContract.DATE_FORMAT);
        try {
            Date inputDate = sdf.parse(dateString);
            Date todayDate = new Date();

            if (WeatherContract.getDbDateString(todayDate).equals(dateString)) {
                // Today
                return context.getString(R.string.today);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);

                if (WeatherContract.getDbDateString(cal.getTime()).equals(dateString)) {
                    // Tomorrow
                    return context.getString(R.string.tomorrow);
                } else {
                    // Weekday
                    return new SimpleDateFormat("EEEE").format(inputDate);
                }
            }
        } catch (Exception e) {
            return "";
        }
    }
}
