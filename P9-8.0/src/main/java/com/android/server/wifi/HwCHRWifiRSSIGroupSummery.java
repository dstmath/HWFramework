package com.android.server.wifi;

import android.util.Log;
import com.huawei.device.connectivitychrlog.CSubRSSIGROUP_EVENT;
import com.huawei.device.connectivitychrlog.CSubRSSIGROUP_EVENT_EX;
import java.util.ArrayList;
import java.util.List;

public class HwCHRWifiRSSIGroupSummery {
    private static final String TAG = "HwCHRWifiRSSIGroupSummery";
    private List<HwCHRWifiRSSIGroup> rssi_groups = new ArrayList(4);

    public HwCHRWifiRSSIGroupSummery() {
        this.rssi_groups.add(new HwCHRWifiRSSIGroup(1));
        this.rssi_groups.add(new HwCHRWifiRSSIGroup(2));
        this.rssi_groups.add(new HwCHRWifiRSSIGroup(3));
        this.rssi_groups.add(new HwCHRWifiRSSIGroup(4));
    }

    private HwCHRWifiRSSIGroup getRssiGroup(int rssi) {
        int listSize = this.rssi_groups.size();
        for (int i = 0; i < listSize; i++) {
            if (((HwCHRWifiRSSIGroup) this.rssi_groups.get(i)).isMatchByRSSI(rssi)) {
                return (HwCHRWifiRSSIGroup) this.rssi_groups.get(i);
            }
        }
        return null;
    }

    public void updateArpSummery(boolean succ, int spendTime, int rssi) {
        HwCHRWifiRSSIGroup g = getRssiGroup(rssi);
        if (g == null) {
            Log.e(TAG, "updatArpSummery mRSSI:" + rssi + "  g is null");
            return;
        }
        if (succ) {
            g.add_rtt_lan_succ(spendTime);
        } else {
            g.add_rtt_lan_failures();
        }
        Log.e(TAG, "updatArpSummery mRSSI:" + rssi + "  " + g);
    }

    public void updateTcpSummery(long rtts, long duration, int rssi) {
        HwCHRWifiRSSIGroup g = getRssiGroup(rssi);
        if (g == null) {
            Log.e(TAG, "updateTcpSummery mRSSI:" + rssi + "  g is null");
            return;
        }
        g.add_rtt_tcp((int) rtts, (int) duration);
        Log.e(TAG, "updateTcpSummery mRSSI:" + rssi + "  " + g);
    }

    public void addRelationAps(boolean isSameFreq, int rssi) {
        HwCHRWifiRSSIGroup g = getRssiGroup(rssi);
        if (g == null) {
            Log.e(TAG, "addRelationAps mRSSI:" + rssi + "  g is null");
        } else {
            g.add_area_freq(isSameFreq);
        }
    }

    public HwCHRWifiRSSIGroupSummery newRSSIGroup() {
        HwCHRWifiRSSIGroupSummery result = new HwCHRWifiRSSIGroupSummery();
        result.rssi_groups.clear();
        result.rssi_groups.add(((HwCHRWifiRSSIGroup) this.rssi_groups.get(0)).copy());
        result.rssi_groups.add(((HwCHRWifiRSSIGroup) this.rssi_groups.get(1)).copy());
        result.rssi_groups.add(((HwCHRWifiRSSIGroup) this.rssi_groups.get(2)).copy());
        result.rssi_groups.add(((HwCHRWifiRSSIGroup) this.rssi_groups.get(3)).copy());
        return result;
    }

    public void resetRSSIGroup() {
        int listSize = this.rssi_groups.size();
        for (int i = 0; i < listSize; i++) {
            ((HwCHRWifiRSSIGroup) this.rssi_groups.get(i)).reset();
        }
    }

    public List<CSubRSSIGROUP_EVENT> getRSSIGroupCHR() {
        List<CSubRSSIGROUP_EVENT> result = new ArrayList();
        int listSize = this.rssi_groups.size();
        for (int i = 0; i < listSize; i++) {
            result.add(((HwCHRWifiRSSIGroup) this.rssi_groups.get(i)).getRSSIGroupEventCHR());
        }
        return result;
    }

    public List<CSubRSSIGROUP_EVENT_EX> getRSSIGroupEXCHR() {
        List<CSubRSSIGROUP_EVENT_EX> result = new ArrayList();
        int listSize = this.rssi_groups.size();
        for (int i = 0; i < listSize; i++) {
            result.add(((HwCHRWifiRSSIGroup) this.rssi_groups.get(i)).getRSSIGroupEXEventCHR());
        }
        return result;
    }
}
