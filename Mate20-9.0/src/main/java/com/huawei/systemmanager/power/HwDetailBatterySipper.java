package com.huawei.systemmanager.power;

import com.huawei.pgmng.plug.DetailBatterySipper;

public class HwDetailBatterySipper {
    public static final int BG_CPU_TIME = 1;
    public static final int BG_GPS_TIME = 4;
    public static final int BG_Wl_TIME = 0;
    public static final int FG_CPU_TIME = 2;
    public static final int FG_GPS_TIME = 3;
    public static final int FG_SCREEN_TIME = 5;
    public static final int MOBILE_ENTRY = 11;
    public static final int NET_ENTRY_RX_BYTES = 0;
    public static final int NET_ENTRY_RX_PACKETS = 2;
    public static final int NET_ENTRY_TX_BYTES = 1;
    public static final int NET_ENTRY_TX_PACKETS = 3;
    public static final int WIFI_ENTRY = 12;
    private DetailBatterySipper mLocalDetailBatterySipper = null;

    public static class NetEntryEx {
        private DetailBatterySipper.NetEntry mLocalNetEntry;

        public NetEntryEx(DetailBatterySipper.NetEntry netEntry) {
            this.mLocalNetEntry = netEntry;
        }

        public long getItem(int index) {
            return this.mLocalNetEntry.mItem[index];
        }
    }

    public HwDetailBatterySipper(DetailBatterySipper sipper) {
        this.mLocalDetailBatterySipper = sipper;
    }

    public float getTotalPower() {
        if (this.mLocalDetailBatterySipper == null) {
            return 0.0f;
        }
        return this.mLocalDetailBatterySipper.getTotalPower();
    }

    public float getDistributedPower() {
        if (this.mLocalDetailBatterySipper == null) {
            return 0.0f;
        }
        return this.mLocalDetailBatterySipper.getDistributedPower();
    }

    public int getUid() {
        if (this.mLocalDetailBatterySipper == null) {
            return 0;
        }
        return this.mLocalDetailBatterySipper.mUid;
    }

    public String getName() {
        if (this.mLocalDetailBatterySipper == null) {
            return "";
        }
        return this.mLocalDetailBatterySipper.mName;
    }

    public long getTimeOfSipper(int index) {
        long time = -1;
        if (this.mLocalDetailBatterySipper == null) {
            return -1;
        }
        switch (index) {
            case 0:
                time = this.mLocalDetailBatterySipper.mBgWlTime;
                break;
            case 1:
                time = this.mLocalDetailBatterySipper.mBgCpuTime;
                break;
            case 2:
                time = this.mLocalDetailBatterySipper.mFgCpuTime;
                break;
            case 3:
                time = this.mLocalDetailBatterySipper.mFgGpsTime;
                break;
            case 4:
                time = this.mLocalDetailBatterySipper.mBgGpsTime;
                break;
            case 5:
                time = this.mLocalDetailBatterySipper.mFgScreenTime;
                break;
        }
        return time;
    }

    public NetEntryEx[] getEntryArray(int index) {
        DetailBatterySipper.NetEntry[] netarray = null;
        if (this.mLocalDetailBatterySipper == null) {
            return null;
        }
        if (index == 11) {
            netarray = this.mLocalDetailBatterySipper.mMobileEntry;
        } else if (index == 12) {
            netarray = this.mLocalDetailBatterySipper.mWifiEntry;
        }
        if (netarray == null) {
            return null;
        }
        NetEntryEx[] array = new NetEntryEx[netarray.length];
        for (int i = 0; i < netarray.length; i++) {
            array[i] = new NetEntryEx(netarray[i]);
        }
        return array;
    }
}
