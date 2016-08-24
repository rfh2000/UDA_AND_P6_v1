package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by R.Pendlebury on 06/08/2016.
 */

public class WeatherWatchFace extends CanvasWatchFaceService implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    private GoogleApiClient mGoogleApiClient;

    private static final String WEARABLE_DATA_PATH = "/wearable_data";

    private static final String TAG = "TAG__________WEAR";

    private String dataReceived = "14:37";
//    private String forecast = "OK";
    private String forecast = "";
//    private String high = "10";
    private String high = "";
//    private String low = "5";
    private String low = "";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    public static final String COUNT_PATH = "/count";
    public static final String COUNT_KEY = "count";
    public static final String IMAGE_PATH = "/image";
    public static final String IMAGE_KEY = "photo";

    private Bitmap weatherBitmap;

    @Override
    public Engine onCreateEngine() {
        Log.v(TAG, "onCreateEngine");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return new Engine();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "onConnected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "onConnectionSuspended");
        //Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.v(TAG, "onDataChanged");
//        for (DataEvent event : dataEventBuffer) {
//            if (event.getType() == DataEvent.TYPE_CHANGED) {
//                // DataItem changed
//                DataItem item = event.getDataItem();
//                if (item.getUri().getPath().compareTo("/count") == 0) {
//                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
//                    //updateCount(dataMap.getInt(COUNT_KEY));
//                }
//            } else if (event.getType() == DataEvent.TYPE_DELETED) {
//                // DataItem deleted
//            }
//        }


        // This is the received DataMap that is working when sending a timestamp
//        DataMap dataMap;
//        for (DataEvent event : dataEventBuffer) {
//            // Check the data type
//            if (event.getType() == DataEvent.TYPE_CHANGED) {
//                // Check the data path
//                String path = event.getDataItem().getUri().getPath();
//                if (path.equals(WEARABLE_DATA_PATH)) {}
//                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
//                Log.v("____TAG", "DataMap received on watch: " + dataMap);
//                long time = dataMap.getLong("time");
//                dataReceived = Long.toString(time);
//                Log.v("____TAG", "DataMap value is: " + time);
//            }
//        }

        // This is the received DataMap that is working when sending updated forecast values
        DataMap dataMap;
        for (DataEvent event : dataEventBuffer) {
            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {

                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v(TAG, "DataMap received on watch: " + dataMap);

                    int weatherId = dataMap.getInt("weatherId");
                    String forecastNew = dataMap.getString("forecast");
                    double highNew = dataMap.getDouble("high");
                    double lowNew = dataMap.getDouble("low");

                    Log.v(TAG, "DataMap values are: " + weatherId
                            + "--" + forecastNew
                            + "--" + Double.toString(highNew)
                            + "--" + Double.toString(lowNew));

//                    dataReceived = forecastNew
//                            + "--" + Double.toString(highNew)
//                            + "--" + Double.toString(lowNew);

//                    forecast = forecastNew;
                    high = String.format(getResources().getString(R.string.format_temperature), highNew);
                    low = String.format(getResources().getString(R.string.format_temperature), lowNew);

                    dataReceived = weatherId
                            + "--" + forecast
                            + "--" + high
                            + "--" + low;

//                    Asset profileAsset = dataMapItem.getDataMap().getAsset("weatherIcon");
                    Asset weatherAsset = dataMap.getAsset("weatherIcon");
//                    Bitmap bitmap = loadBitmapFromAsset(weatherAsset);
                    // Loads image on background thread.
//                    new LoadBitmapAsyncTask().execute(weatherAsset);
                }
            }
        }

//        // Loop through the events and get the updated count from the handheld
//        DataMap dataMap;
//        for (DataEvent event : dataEventBuffer) {
//            Uri uri = event.getDataItem().getUri();
//            String path = uri.getPath();
//            if (COUNT_PATH.equals(path)) {
//                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
//                Log.v("____TAG", "DataMap received on watch: " + dataMap);
//                int i = dataMap.getInt(COUNT_KEY);
//                dataReceived = Integer.toString(i);
//                Log.v("____TAG", "DataMap value is: " + dataReceived);
//            }
//        }

    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
//        ConnectionResult result =
//                mGoogleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(100, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            // The Wearable API is unavailable
            Log.v(TAG, "The Wearable API is unavailable");
        }
    }


    private static class EngineHandler extends Handler {
        private final WeakReference<Engine> mWeakReference;

        public EngineHandler(WeatherWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            WeatherWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint, mTimeTextPaint, mDateTextPaint;
        Paint mForecastPaint, mHighTempPaint, mLowTempPaint;
        boolean mAmbient;
        Time mTime;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        int mTapCount;
        float mXOffset, mYOffset;
        float mYOffsetTime, mYOffsetDate, mYOffsetForecast;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WeatherWatchFace.this)
//                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            Resources resources = WeatherWatchFace.this.getResources();

            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            mYOffsetTime = resources.getDimension(R.dimen.digital_y_offset_time);
            mYOffsetDate = resources.getDimension(R.dimen.digital_y_offset_date);
            mYOffsetForecast = resources.getDimension(R.dimen.digital_y_offset_forecast);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));

            mTimeTextPaint = new Paint();
            mTimeTextPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.digital_text));
//            mTimeTextPaint.setTextSize(resources.getDimensionPixelSize
//                    (R.dimen.digital_time_text_size_round));

            mDateTextPaint = new Paint();
            mDateTextPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.digital_text));
//            mDateTextPaint.setTextSize(resources.getDimensionPixelSize
//                    (R.dimen.digital_date_text_size_round));

            mForecastPaint = new Paint();
            mForecastPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.digital_text));
//            mForecastPaint.setTextSize(resources.getDimensionPixelSize
//                    (R.dimen.digital_forecast_text_size_round));

            mHighTempPaint = new Paint();
            mHighTempPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.digital_text));
//            mHighTempPaint.setUnderlineText(true);

            mLowTempPaint = new Paint();
            mLowTempPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.digital_text));
//            mLowTempPaint.setUnderlineText(true);

            //mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);

            mTime = new Time();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WeatherWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WeatherWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = WeatherWatchFace.this.getResources();
            boolean isRound = insets.isRound();

            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);

            float timeTextSize = resources.getDimension(isRound
                    ? R.dimen.digital_time_text_size_round : R.dimen.digital_time_text_size);

            float dateTextSize = resources.getDimension(isRound
                    ? R.dimen.digital_date_text_size_round : R.dimen.digital_date_text_size);

            float forecastTextSize = resources.getDimension(isRound
                    ? R.dimen.digital_forecast_text_size_round : R.dimen.digital_forecast_text_size);

            float highTempTextSize = resources.getDimension(isRound
                    ? R.dimen.digital_high_text_size_round : R.dimen.digital_high_text_size);

            float lowTempTextSize = resources.getDimension(isRound
                    ? R.dimen.digital_low_text_size_round : R.dimen.digital_low_text_size);

            mTimeTextPaint.setTextSize(timeTextSize);
            mDateTextPaint.setTextSize(dateTextSize);
            mForecastPaint.setTextSize(forecastTextSize);
            mHighTempPaint.setTextSize(highTempTextSize);
            mLowTempPaint.setTextSize(lowTempTextSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTimeTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Resources resources = WeatherWatchFace.this.getResources();
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    mTapCount++;
                    mBackgroundPaint.setColor(resources.getColor(mTapCount % 2 == 0 ?
                            R.color.background : R.color.background2));
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            Calendar calendar = Calendar.getInstance();
            //SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy");
            String date = format.format(calendar.getTimeInMillis());
            //String extra;

            Resources resources = WeatherWatchFace.this.getResources();

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.art_clear);
//            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 40, 40, false);

            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            mTime.setToNow();
            String timeText = mAmbient
                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);

            // Add a line to show the time
            canvas.drawText(timeText, bounds.centerX() - mTimeTextPaint.measureText(timeText)/2,
                    mYOffsetTime, mTimeTextPaint);

            // Add another line to show the date
            canvas.drawText(date, bounds.centerX() - mDateTextPaint.measureText(date)/2,
                    mYOffsetDate, mDateTextPaint);

            // Draw a line to separate date & time from forecast
            canvas.drawLine(bounds.centerX() - 40,
                    getResources().getDimension(R.dimen.digital_y_offset_line),
                    bounds.centerX() + 40,
                    getResources().getDimension(R.dimen.digital_y_offset_line),
                    mDateTextPaint);

//            // Add another line to show the forecast
//            canvas.drawText(dataReceived, bounds.centerX() - mForecastPaint.measureText(dataReceived)/2,
//                    mYOffsetForecast, mForecastPaint);

            // Add another line to show the forecast
//            canvas.drawText(forecast, bounds.centerX() - 80,
//                    mYOffsetForecast, mForecastPaint);

            // Only show weather if data has been received from
            if (!high.equals("")) {
                // Add another line to show the weather icon
                canvas.drawBitmap(resizedBitmap, bounds.centerX() - 80, mYOffsetForecast - 32, mForecastPaint);

                // Add another line to show the high temp
                canvas.drawText(high, bounds.centerX() - mForecastPaint.measureText(high)/2,
                        mYOffsetForecast, mHighTempPaint);

                // Add another line to show the low temp
                canvas.drawText(low, bounds.centerX() + 50,
                        mYOffsetForecast, mLowTempPaint);
            }

        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }

    /*
    * Extracts {@link android.graphics.Bitmap} data from the
    * {@link com.google.android.gms.wearable.Asset}
    */
    private class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Asset... params) {
            if (params.length > 0) {

                Asset asset = params[0];
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        mGoogleApiClient, asset).await().getInputStream();

                if (assetInputStream == null) {
                    Log.w(TAG, "Requested an unknown Asset.");
                    return null;
                }
                return BitmapFactory.decodeStream(assetInputStream);

            } else {
                Log.e(TAG, "Asset must be non-null");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {
                Log.d(TAG, "Setting the weather icon image...");
//                mAssetFragment.setBackgroundImage(bitmap);
                weatherBitmap = bitmap;
            }
        }
    }
}
