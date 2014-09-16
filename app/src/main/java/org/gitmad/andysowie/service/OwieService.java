package org.gitmad.andysowie.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;

import org.gitmad.andysowie.R;
import org.gitmad.andysowie.activity.PowerActivity;

public class OwieService extends Service implements SensorEventListener {

    public static final String ACTION_SERVICE_START
            = "org.gitmad.andysowie.service.ACTION_SERVICE_START";
    public static final String ACTION_SERVICE_STOP
            = "org.gitmad.andysowie.service.ACTION_SERVICE_STOP";

    private static final int NOTIFICATION_ID = 1;
    private static final int START_REQUEST_CODE = 2;
    private static final int STOP_REQUEST_CODE = 3;
    private static final String STOP_FILTER = "org.gitmad.andysowie.service.STOP_FILTER";

    private static final float ZERO_APPROXIMATE
            = 1.25f;
    private static final float GRAVITY_APPROXIMATE
            = SensorManager.GRAVITY_EARTH - ZERO_APPROXIMATE;

    private SoundPool mSoundPool;
    private int mSoundId;
    private SensorManager mSensorManager;
    private boolean mIsFalling;

    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            context.stopService(new Intent(context, OwieService.class));
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        mSoundId = mSoundPool.load(this, R.raw.ow, 1);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        registerReceiver(stopReceiver, new IntentFilter(STOP_FILTER));
        sendBroadcast(new Intent(ACTION_SERVICE_START));
        mIsFalling = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSoundPool.release();
        mSensorManager.unregisterListener(this);
        unregisterReceiver(stopReceiver);
        sendBroadcast(new Intent(ACTION_SERVICE_STOP));
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        final Intent startIntent = new Intent(this, PowerActivity.class);
        final PendingIntent startActivityPendingIntent
                = PendingIntent.getActivity(this, START_REQUEST_CODE,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Intent stopServiceIntent = new Intent(STOP_FILTER);
        final PendingIntent stopServicePendingIntent
                = PendingIntent.getBroadcast(this, STOP_REQUEST_CODE,
                stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_MIN)
                .addAction(R.drawable.ic_launcher,
                        getString(R.string.stop_service), stopServicePendingIntent)
                .setContentIntent(startActivityPendingIntent);
        final Notification notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void getAccelerometer(SensorEvent event) {
        final float z = event.values[2];

        if (z <= ZERO_APPROXIMATE && !mIsFalling) {
            mIsFalling = true;
        } else if (mIsFalling && z >= GRAVITY_APPROXIMATE) {
            mIsFalling = false;
            mSoundPool.play(mSoundId, 0.99f, 0.99f, 0, 0, 0);
        }
    }
}
