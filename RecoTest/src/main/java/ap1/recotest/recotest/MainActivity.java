package ap1.recotest.recotest;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();

        if(isBackgroundMonitoringServiceRunning(this)) {
            Switch swc = (Switch)findViewById(R.id.backgroundMonitoringSwitch);
            swc.setChecked(true);
        }

        if(isBackgroundRangingServiceRunning(this)) {
            Switch swc = (Switch)findViewById(R.id.backgroundRangingSwitch);
            swc.setChecked(true);
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    public void onMonitoringSwitchClicked(View v) {
        Switch swc = (Switch)v;
        if(swc.isChecked()) {
            Log.i(TAG, "onMonitoringSwitchClicked off to on");
            Intent intent = new Intent(this, RECOBackgroundMonitoringService.class);
            startService(intent);
        } else {
            Log.i(TAG, "onMonitoringSwitchClicked on to off");
            stopService(new Intent(this, RECOBackgroundMonitoringService.class));
        }
    }

    public void onRangingSwitchClicked(View v) {
        Switch swc = (Switch)v;
        if(swc.isChecked()) {
            Log.i(TAG, "onRangingSwitchClicked off to on");
            startService(new Intent(this, RECOBackgroundRangingService.class));
        } else {
            Log.i(TAG, "onRangingSwitchClicked on to off");
            stopService(new Intent(this, RECOBackgroundRangingService.class));
        }
    }

    public void onButtonClicked(View v) {
        Button btn = (Button)v;
        if(btn.getId() == R.id.monitoringButton) {
            final Intent intent = new Intent(this, RECOMonitoringActivity.class);
            startActivity(intent);
        } else {
            final Intent intent = new Intent(this, RECORangingActivity.class);
            startActivity(intent);
        }
    }

    private static boolean isBackgroundMonitoringServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if(RECOBackgroundMonitoringService.class.getName().equals(runningService.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBackgroundRangingServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if(RECOBackgroundRangingService.class.getName().equals(runningService.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}