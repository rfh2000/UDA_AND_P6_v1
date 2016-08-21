package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
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
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

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
    private String forecast = "";
    private String high = "";
    private String low = "";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    public static final String COUNT_PATH = "/count";
    public static final String COUNT_KEY = "count";
    public static final String IMAGE_PATH = "/image";
    public static final String IMAGE_KEY = "photo";

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

                    String forecastNew = dataMap.getString("forecast");
                    double highNew = dataMap.getDouble("high");
                    double lowNew = dataMap.getDouble("low");

                    Log.v(TAG, "DataMap values are: " + forecastNew
                            + "--" + Double.toString(highNew)
                            + "--" + Double.toString(lowNew));

//                    dataReceived = forecastNew
//                            + "--" + Double.toString(highNew)
//                            + "--" + Double.toString(lowNew);

                    forecast = forecastNew;
                    high = String.format(getResources().getString(R.string.format_temperature), highNew);
                    low = String.format(getResources().getString(R.string.format_temperature), lowNew);

                    dataReceived = forecast
                            + "--" + high
                            + "--" + low;
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
        Paint mBackgroundPaint, mTimeTextPaint, mDateTextPaint, mForecastPaint;
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

            mTimeTextPaint.setTextSize(timeTextSize);
            mDateTextPaint.setTextSize(dateTextSize);
            mForecastPaint.setTextSize(forecastTextSize);
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

            // Add another line to show the forecast
            canvas.drawText(dataReceived, bounds.centerX() - mForecastPaint.measureText(dataReceived)/2,
                    mYOffsetForecast, mForecastPaint);

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
}
