package com.huawei.systemmanager.power;

import com.huawei.android.pgmng.plug.DetailBatterySipper;

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

    public HwDetailBatterySipper(DetailBatterySipper sipper) {
        this.mLocalDetailBatterySipper = sipper;
    }

    public float getTotalPower() {
        DetailBatterySipper detailBatterySipper = this.mLocalDetailBatterySipper;
        if (detailBatterySipper == null) {
            return 0.0f;
        }
        return detailBatterySipper.getTotalPower();
    }

    public float getDistributedPower() {
        DetailBatterySipper detailBatterySipper = this.mLocalDetailBatterySipper;
        if (detailBatterySipper == null) {
            return 0.0f;
        }
        return detailBatterySipper.getDistributedPower();
    }

    public int getUid() {
        DetailBatterySipper detailBatterySipper = this.mLocalDetailBatterySipper;
        if (detailBatterySipper == null) {
            return 0;
        }
        return detailBatterySipper.mUid;
    }

    public String getName() {
        DetailBatterySipper detailBatterySipper = this.mLocalDetailBatterySipper;
        if (detailBatterySipper == null) {
            return "";
        }
        return detailBatterySipper.mName;
    }

    public long getTimeOfSipper(int index) {
        DetailBatterySipper detailBatterySipper = this.mLocalDetailBatterySipper;
        if (detailBatterySipper == null) {
            return -1;
        }
        if (index == 0) {
            return detailBatterySipper.mBgWlTime;
        }
        if (index == 1) {
            return detailBatterySipper.mBgCpuTime;
        }
        if (index == 2) {
            return detailBatterySipper.mFgCpuTime;
        }
        if (index == 3) {
            return detailBatterySipper.mFgGpsTime;
        }
        if (index == 4) {
            return detailBatterySipper.mBgGpsTime;
        }
        if (index != 5) {
            return -1;
        }
        return detailBatterySipper.mFgScreenTime;
    }

    public static class NetEntryEx {
        private DetailBatterySipper.NetEntry mLocalNetEntry;

        public NetEntryEx(DetailBatterySipper.NetEntry netEntry) {
            this.mLocalNetEntry = netEntry;
        }

        public long getItem(int index) {
            return this.mLocalNetEntry.mItem[index];
        }
    }

    public NetEntryEx[] getEntryArray(int index) {
        DetailBatterySipper.NetEntry[] netarray = null;
        DetailBatterySipper detailBatterySipper = this.mLocalDetailBatterySipper;
        if (detailBatterySipper == null) {
            return null;
        }
        if (index == 11) {
            netarray = detailBatterySipper.mMobileEntry;
        } else if (index == 12) {
            netarray = detailBatterySipper.mWifiEntry;
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
