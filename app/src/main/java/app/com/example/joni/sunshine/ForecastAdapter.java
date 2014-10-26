package app.com.example.joni.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by joni.nevalainen on 25.10.2014.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Weather icon
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.drawable.ic_launcher);

        // Date
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Utility.getFriendlyDayString(context, dateString));

        // Description
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView descView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        descView.setText(description);

        boolean isMetric = Utility.isMetric(context);

        // Min
        double min = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView minView = (TextView) view.findViewById(R.id.list_item_low_textview);
        minView.setText(Utility.formatTemperature(min, isMetric));

        // Max
        double max = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView maxView = (TextView) view.findViewById(R.id.list_item_high_textview);
        maxView.setText(Utility.formatTemperature(max, isMetric));
    }
}
