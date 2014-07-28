package sunshine.ferdyrodriguez.com.sunshine;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import sunshine.ferdyrodriguez.com.sunshine.data.WeatherContract.LocationEntry;
import sunshine.ferdyrodriguez.com.sunshine.data.WeatherContract.WeatherEntry;
import sunshine.ferdyrodriguez.com.sunshine.data.WeatherDbHelper;

/**
 * Created by apple on 27/07/14.
 */
public class TestProvider extends AndroidTestCase {

    public static final String TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testInsertReadProvider() {
        String testName = "North Pole";
        String testLocationSetting = "99705";
        Double testLatitude = 62.772;
        Double testLongitude = -147.355;

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(LocationEntry.COLUMN_LOC_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        assertTrue(locationRowId != -1);
        Log.d(TAG, "New row id: " + locationRowId);

        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOC_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        Cursor cursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

/*        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );*/



        if (cursor.moveToFirst()) {
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOC_SETTING);
            String location = cursor.getString(locationIndex);

            int nameIndex = cursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME);
            String name = cursor.getString(nameIndex);

            int latitudeIndex = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT);
            Double latitude = cursor.getDouble(latitudeIndex);

            int longitudeIndex = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG);
            Double longitude = cursor.getDouble(longitudeIndex);

            assertEquals(testName, name);
            assertEquals(testLocationSetting, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

            long weatherRowId;
            weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
            assertTrue(weatherRowId != -1);

            Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            if (weatherCursor.moveToFirst()) {
                assertEquals(weatherCursor.getInt(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY)), locationRowId);
                assertEquals(weatherCursor.getString(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)), "20141205");
                assertEquals(weatherCursor.getDouble(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES)), 1.1);
                assertEquals(weatherCursor.getDouble(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY)), 1.2);
                assertEquals(weatherCursor.getDouble(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE)), 1.3);
                assertEquals(weatherCursor.getInt(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), 75);
                assertEquals(weatherCursor.getInt(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), 65);
                assertEquals(weatherCursor.getString(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC)), "Asteroids");
                assertEquals(weatherCursor.getDouble(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED)), 5.5);
                assertEquals(weatherCursor.getInt(
                        weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID)), 321);
            } else {
                fail("No weather data returned!");
            }

            weatherCursor.close();

        } else {
            fail("No values returned :(");
        }

        cursor.close();
        dbHelper.close();
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }
}
