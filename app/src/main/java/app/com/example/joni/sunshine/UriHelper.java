package app.com.example.joni.sunshine;

import android.net.Uri;

import org.apache.http.auth.AUTH;

/**
 * Created by Joni on 21.8.2014.
 */
public class UriHelper {

    private static final String PROTOCOL = "http";
    private static final String AUTHORITY = "api.openweathermap.org";
    private static final String API_VERSION = "2.5";

    private static final String DEFAULT_CITY = "Joensuu";
    private static final String DEFAULT_UNITS = "metric";
    private static final int DEFAULT_DAY_COUNT = 7;

    public static String getDefaultAPILink() {
        return constructDefaultLink();
    }

    public static String getAPILinkByQuery(String query) {
        return constructAPILink(query, DEFAULT_UNITS, DEFAULT_DAY_COUNT);
    }

    private static String constructDefaultLink() {
        return constructAPILink(DEFAULT_CITY, DEFAULT_UNITS, DEFAULT_DAY_COUNT);
    }

    private static String constructAPILink(String city, String units, int dayCount) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(PROTOCOL);
        uriBuilder.authority(AUTHORITY);
        uriBuilder.appendPath("data");
        uriBuilder.appendPath(API_VERSION);
        uriBuilder.appendPath("forecast");
        uriBuilder.appendPath("daily");
        uriBuilder.appendQueryParameter("q", city);
        uriBuilder.appendQueryParameter("mode", "json");
        uriBuilder.appendQueryParameter("units", units);
        uriBuilder.appendQueryParameter("cnt", Integer.toString(dayCount));
        return uriBuilder.build().toString();
    }
}
