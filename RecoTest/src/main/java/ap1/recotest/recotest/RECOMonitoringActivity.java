package ap1.recotest.recotest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ListView;

import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;
import com.perples.recosdk.RECOMonitoringListener;

public class RECOMonitoringActivity extends RECOActivity implements RECOMonitoringListener {
    private static final String TAG = "RECOMonitoringActivity";

    private RECOMonitoringListAdapter monitoringListAdapter;
    private ListView regionListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_monitoring);

        this.recoManager.setMonitoringListener(this);
        this.recoManager.setScanPeriod(1*1000L);
        this.recoManager.setSleepPeriod(5*1000L);

        this.recoManager.bind(this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        this.monitoringListAdapter = new RECOMonitoringListAdapter(this);
        this.regionListView = (ListView)findViewById(R.id.list_monitoring);
        this.regionListView.setAdapter(monitoringListAdapter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop(this.regions);
        unbind();
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
        start(this.regions);
    }

    @Override
    public void didDetermineStateForRegion(RECOBeaconRegionState recoRegionState, RECOBeaconRegion recoRegion) {
        Log.i(TAG, "didDetermineStateForRegion()");
        Log.i(TAG, "region: " + recoRegion.getUniqueIdentifier() + ", state: " + recoRegionState.toString());

        this.monitoringListAdapter.updateRegion(recoRegion, recoRegionState, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(new Date()));
        this.monitoringListAdapter.notifyDataSetChanged();
    }

    @Override
    public void didEnterRegion(RECOBeaconRegion recoRegion) {
        Log.i(TAG, "didEnterRegion() region:" + recoRegion.getUniqueIdentifier());
        //TODO something you want after entering region
    }

    @Override
    public void didExitRegion(RECOBeaconRegion recoRegion) {
        Log.i(TAG, "didExitRegion() region:" + recoRegion.getUniqueIdentifier());
        // TODO something you want after exiting region
    }

    @Override
    public void didStartMonitoringForRegion(RECOBeaconRegion recoRegion) {
        Log.i(TAG, "didStartMonitoringForRegion: " + recoRegion.getUniqueIdentifier());
        try {
            this.recoManager.requestStateForRegion(recoRegion);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void start(ArrayList<RECOBeaconRegion> regions) {
        Log.i(TAG, "start");

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

    @Override
    protected void stop(ArrayList<RECOBeaconRegion> regions) {
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

}