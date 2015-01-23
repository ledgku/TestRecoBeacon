package ap1.recotest.recotest;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;

public class RECOMonitoringListAdapter extends BaseAdapter {
    private HashMap<RECOBeaconRegion, RECOBeaconRegionState> monitoredRegions;
    private HashMap<RECOBeaconRegion, String> lastUpdateTime;
    private ArrayList<RECOBeaconRegion> monitoredRegionLists;

    private LayoutInflater layoutInflater;

    public RECOMonitoringListAdapter(Context context) {
        super();
        this.monitoredRegions = new HashMap<RECOBeaconRegion, RECOBeaconRegionState>();
        this.lastUpdateTime = new HashMap<RECOBeaconRegion, String>();
        this.monitoredRegionLists = new ArrayList<RECOBeaconRegion>();

        this.layoutInflater = LayoutInflater.from(context);
    }

    public void updateRegion(RECOBeaconRegion recoRegion, RECOBeaconRegionState recoState, String updateTime) {
        this.monitoredRegions.put(recoRegion, recoState);
        this.lastUpdateTime.put(recoRegion, updateTime);
        if(!this.monitoredRegionLists.contains(recoRegion)) {
            this.monitoredRegionLists.add(recoRegion);
        }
    }

    public void clear() {
        this.monitoredRegions.clear();
    }

    @Override
    public int getCount() {
        return this.monitoredRegions.size();
    }

    @Override
    public Object getItem(int position) {
        return this.monitoredRegions.get(this.monitoredRegionLists.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_monitoring_region, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.recoRegionID = (TextView)convertView.findViewById(R.id.region_uniqueID);
            viewHolder.recoRegionState = (TextView)convertView.findViewById(R.id.region_state);
            viewHolder.recoRegionTime = (TextView)convertView.findViewById(R.id.region_update_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        RECOBeaconRegion recoRegion = this.monitoredRegionLists.get(position);
        RECOBeaconRegionState recoState = this.monitoredRegions.get(recoRegion);

        String recoRegionUniqueID = recoRegion.getUniqueIdentifier();
        String recoRegionState = recoState.toString();
        String recoUpdateTime = this.lastUpdateTime.get(recoRegion);

        viewHolder.recoRegionID.setText(recoRegionUniqueID);
        viewHolder.recoRegionState.setText(recoRegionState);
        viewHolder.recoRegionTime.setText(recoUpdateTime);

        return convertView;
    }

    static class ViewHolder {
        TextView recoRegionID;
        TextView recoRegionState;
        TextView recoRegionTime;
    }
}