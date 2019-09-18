package com.android.server.wifi.HwQoE;

public class HiDataTracfficInfo {
    public int mAPPType;
    public long mDuration;
    public String mIMSI;
    public long mThoughtput;
    public long mTimestamp;

    public static class HiDataApInfo {
        public int mApType;
        public int mAppType;
        public int mAuthType;
        public int mBlackCount;
        public String mSsid;

        public HiDataApInfo() {
            this.mSsid = "none";
            this.mAuthType = 0;
            this.mApType = 0;
            this.mAppType = 0;
            this.mBlackCount = 0;
        }

        public HiDataApInfo(String ssid, int authType, int apType, int appType, int blackCount) {
            this.mSsid = ssid;
            this.mAuthType = authType;
            this.mApType = apType;
            this.mAppType = appType;
            this.mBlackCount = blackCount;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("ssid: ");
            sb.append(this.mSsid);
            sb.append(" ,authType: ");
            sb.append(this.mAuthType);
            sb.append(" apType: ");
            sb.append(this.mApType);
            sb.append(" , appType: ");
            sb.append(this.mAppType);
            sb.append(" blackCount ");
            sb.append(this.mBlackCount);
            return sb.toString();
        }
    }

    public static class WeChatMobileTrafficInfo {
        long avgTraffic;
        int counter;
        long totalTime;
        long totalTraffic;
        int wechaType;

        public void clean() {
            this.counter = 0;
            this.wechaType = 0;
            this.totalTime = 0;
            this.avgTraffic = 0;
            this.totalTraffic = 0;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("counter: ");
            sb.append(this.counter);
            sb.append(" ,wechaType: ");
            sb.append(this.wechaType);
            sb.append(" totalTime: ");
            sb.append(this.totalTime);
            sb.append(" s, avgTraffic: ");
            sb.append(this.avgTraffic);
            sb.append(" Bytes, totalTraffic: ");
            sb.append(this.totalTraffic);
            sb.append(" Bytes");
            return sb.toString();
        }
    }

    public HiDataTracfficInfo() {
        this.mIMSI = "none";
        this.mAPPType = 0;
        this.mThoughtput = 0;
        this.mTimestamp = 0;
        this.mDuration = 0;
    }

    public HiDataTracfficInfo(String dataImsi, int type, long traffic, long duration) {
        this.mIMSI = dataImsi;
        this.mAPPType = type;
        this.mThoughtput = traffic;
        this.mDuration = duration;
        this.mTimestamp = System.currentTimeMillis();
    }
}
