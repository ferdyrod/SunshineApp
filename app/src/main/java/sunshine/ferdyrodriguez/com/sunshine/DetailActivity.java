package sunshine.ferdyrodriguez.com.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sunshine.ferdyrodriguez.com.sunshine.data.WeatherContract;
import sunshine.ferdyrodriguez.com.sunshine.data.WeatherContract.WeatherEntry;

public class DetailActivity extends ActionBarActivity {
    private static final int DETAIL_LOADER = 0;
    public static final String DATE_KEY = "forecast_date";
    public static final String LOCATION_KEY = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String TAG = DetailFragment.class.getSimpleName();

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

        public static final String DATE_KEY = "forecast_date";

        private ShareActionProvider mShareActionProvider;
        private String mForecast;
        private String mLocation;

        public static final String[] FORECAST_COLUMS = {
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP
        };

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putString(LOCATION_KEY, mLocation);
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mLocation != null &&
                    !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            Log.v(TAG, "in onCreateOptionsMenu");
            inflater.inflate(R.menu.detailfragment, menu);

            MenuItem menuItem = menu.findItem(R.id.action_share);

            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            if (mForecast != null ) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (savedInstanceState != null ) {
                mLocation = savedInstanceState.getString(LOCATION_KEY);
            }
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            Intent intent = getActivity().getIntent();
            if (intent == null || !intent.hasExtra(DATE_KEY)) {
                return null;
            }
            String dateString = getActivity().getIntent().getStringExtra(DATE_KEY);

            String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

            mLocation = Utility.getPreferredLocation(getActivity());
            Uri weatherUri = WeatherContract.WeatherEntry
                    .buildWeatherLocationWithDate(mLocation, dateString);

            return new CursorLoader(
                    getActivity(),
                    weatherUri,
                    FORECAST_COLUMS,
                    null,
                    null,
                    sortOrder
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
            Log.v(TAG, "In onLoadFinished");
            if (!data.moveToFirst()) { return; }
            String dateString = Utility.formatDate(data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
            ((TextView)getView().findViewById(R.id.detail_date_textview))
                    .setText(dateString);

            String weatherDescription =
                    data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            ((TextView)getView().findViewById(R.id.detail_forecast_textview))
                    .setText(weatherDescription);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature( getActivity(),
                    data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
            ((TextView) getView().findViewById(R.id.detail_high_textview)).setText(high);

            String low = Utility.formatTemperature(getActivity(),
                    data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
            ((TextView) getView().findViewById(R.id.detail_low_textview)).setText(low);

            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            Log.v(TAG, "Forecast String: " + mForecast);

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }
}
