package ap1.recotest.recotest;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.perples.recosdk.RECOBeacon;

public class RECORangingListAdapter extends BaseAdapter {
    private ArrayList<RECOBeacon> rangedBeacons;
    private LayoutInflater layoutInflater;

    public RECORangingListAdapter(Context context) {
        super();
        this.rangedBeacons = new ArrayList<RECOBeacon>();
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void updateBeacon(RECOBeacon beacon) {
        synchronized (this.rangedBeacons) {
            if(this.rangedBeacons.contains(beacon)) {
                this.rangedBeacons.remove(beacon);
            }
            this.rangedBeacons.add(beacon);
        }
    }

    public void updateAllBeacons(Collection<RECOBeacon> beacons) {
        synchronized (beacons) {
            this.rangedBeacons = new ArrayList<RECOBeacon>(beacons);
        }
    }

    public void clear() {
        this.rangedBeacons.clear();
    }

    @Override
    public int getCount() {
        return this.rangedBeacons.size();
    }

    @Override
    public Object getItem(int position) {
        return this.rangedBeacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_ranging_beacon, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.recoProximityUuid = (TextView)convertView.findViewById(R.id.recoProximityUuid);
            viewHolder.recoMajor = (TextView)convertView.findViewById(R.id.recoMajor);
            viewHolder.recoMinor = (TextView)convertView.findViewById(R.id.recoMinor);
            viewHolder.recoRssi = (TextView)convertView.findViewById(R.id.recoRssi);
            viewHolder.recoProximity = (TextView)convertView.findViewById(R.id.recoProximity);
            viewHolder.recoAccuracy = (TextView)convertView.findViewById(R.id.recoAccuracy);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        RECOBeacon recoBeacon = this.rangedBeacons.get(position);

        String proximityUuid = recoBeacon.getProximityUuid();

        viewHolder.recoProximityUuid.setText(String.format("%s-%s-%s-%s-%s", proximityUuid.substring(0, 8), proximityUuid.substring(8, 12), proximityUuid.substring(12, 16), proximityUuid.substring(16, 20), proximityUuid.substring(20) ));
        viewHolder.recoMajor.setText(recoBeacon.getMajor() + "");
        viewHolder.recoMinor.setText(recoBeacon.getMinor() + "");
        viewHolder.recoRssi.setText(recoBeacon.getRssi() + "");
        viewHolder.recoProximity.setText(recoBeacon.getProximity() + "");
        viewHolder.recoAccuracy.setText(String.format("%.2f", recoBeacon.getAccuracy()));

        return convertView;
    }

    static class ViewHolder {
        TextView recoProximityUuid;
        TextView recoMajor;
        TextView recoMinor;
        TextView recoRssi;
        TextView recoProximity;
        TextView recoAccuracy;
    }

}