package ap1.recotest.recotest;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOServiceConnectListener;

public abstract class RECOActivity extends Activity implements RECOServiceConnectListener {
    protected RECOBeaconManager recoManager;
    protected ArrayList<RECOBeaconRegion> regions;

    private static final String RECO_UUID = "24DDF4118CF1440C87CDE368DAF90001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.recoManager = RECOBeaconManager.getInstance(getApplicationContext());
        this.regions = generateBeaconRegion();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion() {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<RECOBeaconRegion>();

        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(RECO_UUID, "RECO Sample Region");
        regions.add(recoRegion);

        return regions;
    }

    protected abstract void start(ArrayList<RECOBeaconRegion> regions);
    protected abstract void stop(ArrayList<RECOBeaconRegion> regions);
}