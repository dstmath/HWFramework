package com.android.server.wifi.HwQoE;

import android.util.wifi.HwHiLog;

public class HwWifiGameNetChrInfo {
    private static final String TAG = "HiDATA_GameNetChrInfo";
    public boolean mAP24gBTCoexist;
    public boolean mAP5gOnly;
    public short mArpRttGeneralDuration;
    public short mArpRttPoorDuration;
    public short mArpRttSmoothDuration;
    public short mBTScan24GCounter;
    public short mNetworkGeneralAvgRtt;
    public short mNetworkGeneralDuration;
    public short mNetworkPoorAvgRtt;
    public short mNetworkPoorDuration;
    public short mNetworkSmoothAvgRtt;
    public short mNetworkSmoothDuration;
    public short mTcpRttBadDuration;
    public short mTcpRttGeneralDuration;
    public short mTcpRttPoorDuration;
    public short mTcpRttSmoothDuration;
    public short mWifiDisCounter;
    public short mWifiRoamingCounter;
    public short mWifiScanCounter;

    public void clean() {
        this.mArpRttSmoothDuration = 0;
        this.mArpRttGeneralDuration = 0;
        this.mArpRttPoorDuration = 0;
        this.mNetworkSmoothDuration = 0;
        this.mNetworkGeneralDuration = 0;
        this.mNetworkPoorDuration = 0;
        this.mTcpRttSmoothDuration = 0;
        this.mTcpRttGeneralDuration = 0;
        this.mTcpRttPoorDuration = 0;
        this.mTcpRttBadDuration = 0;
        this.mWifiRoamingCounter = 0;
        this.mWifiScanCounter = 0;
        this.mWifiDisCounter = 0;
        this.mBTScan24GCounter = 0;
        this.mAP24gBTCoexist = false;
        this.mAP5gOnly = false;
        this.mNetworkSmoothAvgRtt = 0;
        this.mNetworkGeneralAvgRtt = 0;
        this.mNetworkPoorAvgRtt = 0;
    }

    public void setNetworkSmoothAvgRtt(long mNetworkSmoothAvgRtt2) {
        if (mNetworkSmoothAvgRtt2 >= 32767) {
            this.mNetworkSmoothAvgRtt = Short.MAX_VALUE;
        } else {
            this.mNetworkSmoothAvgRtt = (short) ((int) mNetworkSmoothAvgRtt2);
        }
    }

    public void setNetworkGeneralAvgRtt(long mNetworkGeneralAvgRtt2) {
        if (mNetworkGeneralAvgRtt2 >= 32767) {
            this.mNetworkGeneralAvgRtt = Short.MAX_VALUE;
        } else {
            this.mNetworkGeneralAvgRtt = (short) ((int) mNetworkGeneralAvgRtt2);
        }
    }

    public void setNetworkPoorAvgRtt(long mNetworkPoorAvgRtt2) {
        if (mNetworkPoorAvgRtt2 >= 32767) {
            this.mNetworkPoorAvgRtt = Short.MAX_VALUE;
        } else {
            this.mNetworkPoorAvgRtt = (short) ((int) mNetworkPoorAvgRtt2);
        }
    }

    public void setArpRttSmoothDuration(long mArpRttSmoothDuration2) {
        if (mArpRttSmoothDuration2 >= 32767) {
            this.mArpRttSmoothDuration = Short.MAX_VALUE;
        } else {
            this.mArpRttSmoothDuration = (short) ((int) mArpRttSmoothDuration2);
        }
    }

    public void setArpRttGeneralDuration(long mArpRttGeneralDuration2) {
        if (mArpRttGeneralDuration2 >= 32767) {
            this.mArpRttGeneralDuration = Short.MAX_VALUE;
        } else {
            this.mArpRttGeneralDuration = (short) ((int) mArpRttGeneralDuration2);
        }
    }

    public void setArpRttPoorDuration(long mArpRttPoorDuration2) {
        if (mArpRttPoorDuration2 >= 32767) {
            this.mArpRttPoorDuration = Short.MAX_VALUE;
        } else {
            this.mArpRttPoorDuration = (short) ((int) mArpRttPoorDuration2);
        }
    }

    public void setNetworkSmoothDuration(long mNetworkSmoothDuration2) {
        if (mNetworkSmoothDuration2 >= 32767) {
            this.mNetworkSmoothDuration = Short.MAX_VALUE;
        } else {
            this.mNetworkSmoothDuration = (short) ((int) mNetworkSmoothDuration2);
        }
    }

    public void setNetworkGeneralDuration(long mNetworkGeneralDuration2) {
        if (mNetworkGeneralDuration2 >= 32767) {
            this.mNetworkGeneralDuration = Short.MAX_VALUE;
        } else {
            this.mNetworkGeneralDuration = (short) ((int) mNetworkGeneralDuration2);
        }
    }

    public void setNetworkPoorDuration(long mNetworkPoorDuration2) {
        if (mNetworkPoorDuration2 >= 32767) {
            this.mNetworkPoorDuration = Short.MAX_VALUE;
        } else {
            this.mNetworkPoorDuration = (short) ((int) mNetworkPoorDuration2);
        }
    }

    public void setTcpRttSmoothDuration(long mTcpRttSmoothDuration2) {
        if (mTcpRttSmoothDuration2 >= 32767) {
            this.mTcpRttSmoothDuration = Short.MAX_VALUE;
        } else {
            this.mTcpRttSmoothDuration = (short) ((int) mTcpRttSmoothDuration2);
        }
    }

    public void setTcpRttGeneralDuration(long mTcpRttGeneralDuration2) {
        if (mTcpRttGeneralDuration2 >= 32767) {
            this.mTcpRttGeneralDuration = Short.MAX_VALUE;
        } else {
            this.mTcpRttGeneralDuration = (short) ((int) mTcpRttGeneralDuration2);
        }
    }

    public void setTcpRttPoorDuration(long mTcpRttPoorDuration2) {
        if (mTcpRttPoorDuration2 >= 32767) {
            this.mTcpRttPoorDuration = Short.MAX_VALUE;
        } else {
            this.mTcpRttPoorDuration = (short) ((int) mTcpRttPoorDuration2);
        }
    }

    public void setTcpRttBadDuration(long mTcpRttBadDuration2) {
        if (mTcpRttBadDuration2 >= 32767) {
            this.mTcpRttBadDuration = Short.MAX_VALUE;
        } else {
            this.mTcpRttBadDuration = (short) ((int) mTcpRttBadDuration2);
        }
    }

    public void setWifiRoamingCounter(short mWifiRoamingCounter2) {
        this.mWifiRoamingCounter = mWifiRoamingCounter2;
    }

    public void setWifiScanCounter(short mWifiScanCounter2) {
        this.mWifiScanCounter = mWifiScanCounter2;
    }

    public void setWifiDisCounter(short mWifiDisCounter2) {
        this.mWifiDisCounter = mWifiDisCounter2;
    }

    public void setBTScan24GCounter(short mBTScan24GCounter2) {
        this.mBTScan24GCounter = mBTScan24GCounter2;
    }

    public void setAP5gOnly(boolean mAP5gOnly2) {
        this.mAP5gOnly = mAP5gOnly2;
    }

    public void setAP24gBTCoexist(boolean mAP24gBTCoexist2) {
        this.mAP24gBTCoexist = mAP24gBTCoexist2;
    }

    public void chrInfoDump() {
        StringBuffer buffer = new StringBuffer("Game CHR Info : ");
        buffer.append("mAP24gBTCoexist: ");
        buffer.append(this.mAP24gBTCoexist);
        buffer.append("mAP5gOnly: ");
        buffer.append(this.mAP5gOnly);
        buffer.append(", GameRttSmoothDuration: ");
        buffer.append((int) this.mNetworkSmoothDuration);
        buffer.append(" s, GameRttGeneralDuration: ");
        buffer.append((int) this.mNetworkGeneralDuration);
        buffer.append(" s, GameRttPoorDuration: ");
        buffer.append((int) this.mNetworkPoorDuration);
        buffer.append(" s, ArpRttSmoothDuration: ");
        buffer.append((int) this.mArpRttSmoothDuration);
        buffer.append(" s, ArpRttGeneralDuration: ");
        buffer.append((int) this.mArpRttGeneralDuration);
        buffer.append(" s, ArpRttPoorDuration: ");
        buffer.append((int) this.mArpRttPoorDuration);
        buffer.append(" s, TcpRttSmoothDuration: ");
        buffer.append((int) this.mTcpRttSmoothDuration);
        buffer.append(" s, TcpRttGeneralDuration: ");
        buffer.append((int) this.mTcpRttGeneralDuration);
        buffer.append(" s, TcpRttPoorDuration: ");
        buffer.append((int) this.mTcpRttPoorDuration);
        buffer.append(" s, TcpRttBadDuration: ");
        buffer.append((int) this.mTcpRttBadDuration);
        buffer.append(" s, WifiDisCounter: ");
        buffer.append((int) this.mWifiDisCounter);
        buffer.append(" , WifiScanCounter: ");
        buffer.append((int) this.mWifiScanCounter);
        buffer.append(" , WifiRoamingCounter: ");
        buffer.append((int) this.mWifiRoamingCounter);
        buffer.append(", GameRttSmoothAvgRtt: ");
        buffer.append((int) this.mNetworkSmoothAvgRtt);
        buffer.append(" ms, GameRttGeneralAvgRtt: ");
        buffer.append((int) this.mNetworkGeneralAvgRtt);
        buffer.append(" ms, GameRttPoorAvgRtt: ");
        buffer.append((int) this.mNetworkPoorAvgRtt);
        logD(false, "%{public}s", buffer.toString());
    }

    private void logD(boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.d(TAG, isFmtStrPrivate, info, args);
    }
}
