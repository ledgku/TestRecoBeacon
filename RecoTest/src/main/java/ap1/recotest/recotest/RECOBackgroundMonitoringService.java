package ap1.recotest.recotest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;
import com.perples.recosdk.RECOMonitoringListener;
import com.perples.recosdk.RECOServiceConnectListener;

public class RECOBackgroundMonitoringService extends Service implements RECOMonitoringListener, RECOServiceConnectListener{
    private static final String TAG = "RECOBackgroundMonitoringService";

    private static final String RECO_UUID = "24DDF4118CF1440C87CDE368DAF9C93E";

    private PowerManager.WakeLock wakeLock;
    private final IBinder binder = new MyBinder();

    private static long scanDuration = 1*1000L;
    private static long sleepDuration = 10*1000L;
    private static int notificationID = 9999;

    private RECOBeaconManager recoManager;
    private ArrayList<RECOBeaconRegion> regions;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        super.onCreate();

        this.recoManager = RECOBeaconManager.getInstance(getApplicationContext());

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RECOBackgroundRangingWakeLock");
        wakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
        this.stop();
        wakeLock.release();
    }

    public void onTaskRemoved(Intent rootIntent) {
        Log.e("FLAGX : ", ServiceInfo.FLAG_STOP_WITH_TASK + "");
        super.onTaskRemoved(rootIntent);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            restartServiceIntent.setPackage(getPackageName());

            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmService.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
        }
    }

    public void start() {
        this.regions = generateBeaconRegion();
        this.recoManager.setMonitoringListener(this);
        this.recoManager.bind(this);
    }

    public void startMonitoring(ArrayList<RECOBeaconRegion> regions) {
        this.recoManager.setScanPeriod(scanDuration);
        this.recoManager.setSleepPeriod(sleepDuration);

        for(RECOBeaconRegion region : regions) {
            try {
                region.setRegionExpirationTimeMillis(3*1000L);
                this.recoManager.startMonitoringForRegion(region);
            } catch (RemoteException e) {
                Log.i(TAG, "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i(TAG, "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        this.stopMonitoring(this.regions);
        this.unbind();
    }

    public void stopMonitoring(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                this.recoManager.stopMonitoringForRegion(region);
            } catch (RemoteException e) {
                Log.i(TAG, "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i(TAG, "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    private void unbind() {
        try {
            this.recoManager.unbind();
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception");
            e.printStackTrace();
        }
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion() {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<RECOBeaconRegion>();

        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(RECO_UUID, "RECO Sample Region");
        regions.add(recoRegion);

        return regions;
    }

    public class MyBinder extends Binder {
        RECOBackgroundMonitoringService getService() {
            return RECOBackgroundMonitoringService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onServiceConnect() {
        Log.i(TAG, "onServiceConnect");
        if (!this.recoManager.isMonitoringAvailable()) {
            try {
                this.recoManager.setBluetoothOn();
            } catch (RemoteException e) {
                Log.i(TAG, "Remote Exception");
                e.printStackTrace();
            }
        }
        this.startMonitoring(this.regions);
    }

    @Override
    public void didDetermineStateForRegion(RECOBeaconRegionState recoRegionState, RECOBeaconRegion recoRegion) {
        // TODO something what you want after getting this callback

    }

    @Override
    public void didEnterRegion(RECOBeaconRegion recoRegion) {
        Log.i(TAG, "didEnterRegion() region: " + recoRegion.getUniqueIdentifier());

        popupNotification("Enter " + recoRegion.getUniqueIdentifier());
    }

    @Override
    public void didExitRegion(RECOBeaconRegion recoRegion) {
        Log.i(TAG, "didExit() region: " + recoRegion.getUniqueIdentifier());

        popupNotification("Exit " + recoRegion.getUniqueIdentifier());
    }

    @Override
    public void didStartMonitoringForRegion(RECOBeaconRegion recoRegion) {
        Log.i(TAG, "didStartMonitoringForRegion: " + recoRegion.getUniqueIdentifier());
        // TODO something what you want after getting this callback
    }

    private void popupNotification(String msg) {

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.KOREA)
                .format(new Date());
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(msg + " " + currentTime)
                .setContentText(msg);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        builder.setStyle(inboxStyle);
        nm.notify(notificationID, builder.build());
        notificationID = (notificationID - 1) % 1000 + 9000;

    }


}
