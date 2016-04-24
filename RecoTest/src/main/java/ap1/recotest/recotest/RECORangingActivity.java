package ap1.recotest.recotest;

import java.util.ArrayList;
import java.util.Collection;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ListView;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECORangingListener;

public class RECORangingActivity extends RECOActivity implements RECORangingListener{
    private static final String TAG = "RECORangingActivity";

    private RECORangingListAdapter rangingListAdapter;
    private ListView regionListView;

    SensorManager sm;
    SensorEventListener accL;
    SensorEventListener magL;
    Sensor accSensor;
    Sensor magSensor;
    TextView ax, ay, az;
    TextView mx, my, mz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_ranging);

        this.recoManager.setRangingListener(this);
        this.recoManager.bind(this);
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);    // SensorManager 인스턴스를 가져옴
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);    // 가속도 센서
        magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accL = new accListener();       // 가속도 센서 리스너 인스턴스
        magL = new magListener();
        ax = (TextView)findViewById(R.id.acc_x);
        ay = (TextView)findViewById(R.id.acc_y);
        az = (TextView)findViewById(R.id.acc_z);
        mx = (TextView)findViewById(R.id.mag_x);
        my = (TextView)findViewById(R.id.mag_y);
        mz = (TextView)findViewById(R.id.mag_z);

    }

    @Override
    protected void onResume() {
        super.onResume();

        this.rangingListAdapter = new RECORangingListAdapter(this);
        this.regionListView = (ListView)findViewById(R.id.list_ranging);
        this.regionListView.setAdapter(this.rangingListAdapter);

        sm.registerListener(accL, accSensor, SensorManager.SENSOR_DELAY_NORMAL);    // 가속도 센서 리스너 오브젝트를 등록
        sm.registerListener(magL, magSensor, SensorManager.SENSOR_DELAY_NORMAL);    // 가속도 센서 리스너 오브젝트를 등록
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

    private class accListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {  // 가속도 센서 값이 바뀔때마다 호출됨
            ax.setText(Float.toString(event.values[0]));
            ay.setText(Float.toString(event.values[1]));
            az.setText(Float.toString(event.values[2]));
            Log.i("SENSOR", "Acceleration changed.");
            Log.i("SENSOR", "  Acceleration X: " + event.values[0]
                    + ", Acceleration Y: " + event.values[1]
                    + ", Acceleration Z: " + event.values[2]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class magListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            mx.setText(Float.toString(event.values[0]));
            my.setText(Float.toString(event.values[1]));
            mz.setText(Float.toString(event.values[2]));
            Log.i("SENSOR", "Magnetic changed.");
            Log.i("SENSOR", "  Magnetic X: " + event.values[0]
                    + ", Magnetic Y: " + event.values[1]
                    + ", Magnetic Z: " + event.values[2]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
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