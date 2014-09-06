package sunshine.ferdyrodriguez.com.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import sunshine.ferdyrodriguez.com.sunshine.data.WeatherContract;

/**
 * Created by ferdy.rodriguez on 31/08/2014.
 */
public class Utility {

    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getFriendlyDayString(Context context, String dateStr) {

        Date todayDate = new Date();
        String todayStr = WeatherContract.getDbDateString(todayDate);
        Date inputDate = WeatherContract.getDateFromDb(dateStr);

        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            return context.getString(R.string.full_format_date,
                    today,
                    getFormattedMonthDay(context, dateStr));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = WeatherContract.getDbDateString(cal.getTime());

            if (dateStr.compareTo(weekFutureString) < 0 ) {
                return getDayName(context, dateStr);
            } else {
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    private static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();

            if (WeatherContract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();

                if (WeatherContract.getDbDateString(tomorrowDate).equals(dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getFormattedMonthDay(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM dd");
            String monthdayString = monthDayFormat.format(inputDate);
            return monthdayString;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
