package com.android.server.wifi.ABS;

public class HwABSApInfoData {
    public int mAuth_type;
    public String mBssid;
    public int mIn_black_List;
    public long mLast_connect_time;
    public long mMimo_time;
    public long mSiso_time;
    public String mSsid;
    public int mSwitch_mimo_type;
    public int mSwitch_siso_type;
    public long mTotal_time;

    public HwABSApInfoData(String bssid, String ssid, int switch_mimo_type, int switch_siso_type, int auth_type, int in_black_List, long mimo_time, long siso_time, long total_time, long last_connect_time) {
        if (bssid == null) {
            bssid = "00:00:00:00";
        }
        this.mBssid = bssid;
        if (ssid == null) {
            ssid = "null";
        }
        this.mSsid = ssid;
        this.mSwitch_mimo_type = switch_mimo_type;
        this.mSwitch_siso_type = switch_siso_type;
        this.mAuth_type = auth_type;
        this.mIn_black_List = in_black_List;
        this.mMimo_time = mimo_time;
        this.mSiso_time = siso_time;
        this.mTotal_time = total_time;
        this.mLast_connect_time = last_connect_time;
    }
}
