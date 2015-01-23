package ap1.recotest.recotest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;
import com.perples.recosdk.RECOMonitoringListener;
import com.perples.recosdk.RECOProximity;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;

public class RECOBackgroundRangingService extends Service implements RECOMonitoringListener, RECORangingListener, RECOServiceConnectListener {
    private static final String TAG = "RECOBackgroundRangingService";

    private static final String RECO_UUID = "24DDF4118CF1440C87CDE368DAF9C93E";

    private static int notificationID = 9999;

    private PowerManager.WakeLock wakeLock;
    private final IBinder binder = new MyBinder();

    private static long scanDuration = 1*1000L;
    private static long sleepDuration = 10*1000L;

    private RECOBeaconManager recoManager;
    private ArrayList<RECOBeaconRegion> monitoringRegions;
    private ArrayList<RECOBeaconRegion> rangingRegions;

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
        Log.i(TAG, "onStartCommand()");
        this.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
        this.stop();
        wakeLock.release();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "onTaskRemoved()");
        super.onTaskRemoved(rootIntent);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            restartServiceIntent.setPackage(getPackageName());

            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmService.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
        }
    }

    private void start() {
        this.monitoringRegions = generateBeaconRegion();
        this.rangingRegions = new ArrayList<RECOBeaconRegion>();

        this.recoManager.setMonitoringListener(this);
        this.recoManager.setRangingListener(this);

        this.recoManager.bind(this);
    }

    private void startMonitoring(ArrayList<RECOBeaconRegion> regions) {
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

    private void stop() {
        this.stopMonitoring(this.monitoringRegions);
        if(!this.rangingRegions.isEmpty()) {
            this.stopRanging(this.rangingRegions);
        }
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

    public void stopRanging(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                this.recoManager.stopRangingBeaconsInRegion(region);
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

    private void checkingBeaconState(Collection<RECOBeacon> beacons, RECOBeaconRegion recoRegion) {
        synchronized (beacons) {
            for(RECOBeacon beacon : beacons) {
                if(beacon.getProximity().equals(RECOProximity.RECOProximityImmediate)) {
                    Log.e(TAG, "BEACON POPUP : " + beacon.getProximity());
                    popupNotification(recoRegion.getUniqueIdentifier() + "is" + beacon.getProximity());


                    if(this.rangingRegions.contains(recoRegion)) {
                        this.rangingRegions.remove(recoRegion);
                    }

                    try {
                        this.recoManager.stopRangingBeaconsInRegion(recoRegion);
                    } catch (RemoteException e) {
                        Log.i(TAG, "Remote Exception");
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        Log.i(TAG, "Null Pointer Exception");
                        e.printStackTrace();
                    }

                    return;
                }

            }
        }
    }

    @Override
    public void onServiceConnect() {
        Log.i(TAG, "onServiceConnect");
        if(!this.recoManager.isMonitoringAvailable() || !this.recoManager.isRangingAvailable()) {
            try {
                this.recoManager.setBluetoothOn();
            } catch (RemoteException e) {
                Log.i(TAG, "Remote Exception");
                e.printStackTrace();
            }
        }
        this.startMonitoring(this.monitoringRegions);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoRegion) {
        Log.e(TAG, "didRangeBeaconsInRegion() region: " + recoRegion.getUniqueIdentifier() + ", number of beacons ranged: " + recoBeacons.size());

        //TODO
        this.checkingBeaconState(recoBeacons, recoRegion);
    }

    @Override
    public void didDetermineStateForRegion(RECOBeaconRegionState recoRegionState, RECOBeaconRegion recoRegion) {
        // TODO something what you want after getting this callback
    }

    @Override
    public void didEnterRegion(RECOBeaconRegion recoRegion) {
        Log.e(TAG, "didEnterRegion() region: " + recoRegion.getUniqueIdentifier());

        this.rangingRegions.add(recoRegion);
        try {
            this.recoManager.startRangingBeaconsInRegion(recoRegion);
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.i(TAG, "Null Pointer Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void didExitRegion(RECOBeaconRegion recoRegion) {
        Log.e(TAG, "didExitRegion() region: " + recoRegion.getUniqueIdentifier());

        if(this.rangingRegions.contains(recoRegion)) {
            this.rangingRegions.remove(recoRegion);
        }

        try {
            this.recoManager.stopRangingBeaconsInRegion(recoRegion);
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.i(TAG, "Null Pointer Exception");
            e.printStackTrace();
        }

    }

    @Override
    public void didStartMonitoringForRegion(RECOBeaconRegion recoRegion) {
        Log.e(TAG, "didStartMonitoringForRegion: " + recoRegion.getUniqueIdentifier());
    }

    public class MyBinder extends Binder {
        RECOBackgroundRangingService getService() {
            return RECOBackgroundRangingService.this;
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
