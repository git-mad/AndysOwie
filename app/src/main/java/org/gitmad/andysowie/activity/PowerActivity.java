package org.gitmad.andysowie.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import org.gitmad.andysowie.R;
import org.gitmad.andysowie.service.OwieService;

public class PowerActivity extends Activity {

    private ToggleButton mToggleButton;

    private final BroadcastReceiver startReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mToggleButton.setChecked(true);
        }
    };

    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mToggleButton.setChecked(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power);
        mToggleButton = (ToggleButton) findViewById(R.id.toggle);
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    startService();
                } else {
                    stopService();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(startReceiver, new IntentFilter(OwieService.ACTION_SERVICE_START));
        registerReceiver(stopReceiver, new IntentFilter(OwieService.ACTION_SERVICE_STOP));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mToggleButton.setChecked(isMyServiceRunning(OwieService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(startReceiver);
        unregisterReceiver(stopReceiver);
    }

    private void startService() {
        startService(new Intent(this, OwieService.class));
    }

    private void stopService() {
        stopService(new Intent(this, OwieService.class));
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
