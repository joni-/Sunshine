package app.com.example.joni.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
}
