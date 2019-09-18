package com.android.server.wifi.ABS;

public class HwABSApInfoData implements Comparable {
    public int mAuth_type;
    public String mBssid;
    public int mContinuous_failure_times;
    public int mFailed_times;
    public int mIn_black_List;
    public long mLast_connect_time;
    public int mReassociate_times;
    public String mSsid;
    public int mSwitch_mimo_type;
    public int mSwitch_siso_type;

    public HwABSApInfoData(String bssid, String ssid, int switch_mimo_type, int switch_siso_type, int auth_type, int in_black_List, int reassociate_times, int failed_times, int continuous_failure_times, long last_connect_time) {
        this.mBssid = bssid != null ? bssid : "00:00:00:00";
        this.mSsid = ssid != null ? ssid : "null";
        this.mSwitch_mimo_type = switch_mimo_type;
        this.mSwitch_siso_type = switch_siso_type;
        this.mAuth_type = auth_type;
        this.mIn_black_List = in_black_List;
        this.mReassociate_times = reassociate_times;
        this.mFailed_times = failed_times;
        this.mContinuous_failure_times = continuous_failure_times;
        this.mLast_connect_time = last_connect_time;
    }

    public int compareTo(Object obj) {
        return Long.compare(this.mLast_connect_time, ((HwABSApInfoData) obj).mLast_connect_time);
    }

    public int hashCode() {
        return Long.valueOf(this.mLast_connect_time).hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass() && ((HwABSApInfoData) obj).mLast_connect_time == this.mLast_connect_time) {
            return true;
        }
        return false;
    }
}
