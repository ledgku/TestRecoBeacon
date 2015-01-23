package ap1.recotest.recotest;

import java.util.ArrayList;
import java.util.Collection;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ListView;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECORangingListener;

public class RECORangingActivity extends RECOActivity implements RECORangingListener{
    private static final String TAG = "RECORangingActivity";

    private RECORangingListAdapter rangingListAdapter;
    private ListView regionListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_ranging);

        this.recoManager.setRangingListener(this);
        this.recoManager.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.rangingListAdapter = new RECORangingListAdapter(this);
        this.regionListView = (ListView)findViewById(R.id.list_ranging);
        this.regionListView.setAdapter(this.rangingListAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop(this.regions);
        unbind();
    }

    private void unbind() {
        try {
            this.recoManager.unbind();
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnect() {
        Log.i(TAG, "onServiceConnect()");
        if(!this.recoManager.isRangingAvailable()) {
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
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoRegion) {
        Log.i(TAG, "didRangeBeaconsInRegion() region: " + recoRegion.getUniqueIdentifier() + ", number of beacons ranged: " + recoBeacons.size());
        this.rangingListAdapter.updateAllBeacons(recoBeacons);
        this.rangingListAdapter.notifyDataSetChanged();

    }

    @Override
    protected void start(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                this.recoManager.startRangingBeaconsInRegion(region);
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

}