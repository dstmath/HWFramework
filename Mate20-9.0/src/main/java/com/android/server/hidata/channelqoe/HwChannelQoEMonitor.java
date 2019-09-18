package com.android.server.hidata.channelqoe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.hidata.appqoe.HwAPPQoEUserAction;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwChannelQoEMonitor implements IChannelQoECallback {
    private static final int MESSAGE_MEASURE = 65280;
    private static final int MESSAGE_START = 65281;
    private static final int MESSAGE_STOP = 65282;
    private static final int MESSAGE_STOP_ALL = 65283;
    private static final int START_DELAY = 10000;
    private static final String TAG = "HiDATA_ChannelQoE_Monitor";
    private static final int THRESHOLD_RSSI_JUDGEMENT = 15;
    private static final int WIFI_CHLOAD_THRESHOLD = 200;
    private static final int WIFI_MONITOR_INTERVAL_X = 3000;
    private static final int WIFI_MONITOR_INTERVAL_Y = 60000;
    private static final int WIFI_MONITOR_TIMES = 2;
    private static final int WIFI_QUALITY_BAD = 2;
    private static final int WIFI_QUALITY_GOOD = 1;
    private static final int WIFI_QUALITY_UNKNOW = 3;
    private static final int WIFI_RSSI_THRESHOLD = -65;
    private boolean chipsetWithChload;
    /* access modifiers changed from: private */
    public int judge_chload;
    /* access modifiers changed from: private */
    public int judge_rssi;
    private int lastChload;
    private BroadcastReceiver mBroadcastReceiver = new ChQoeBroadcastReceiver();
    /* access modifiers changed from: private */
    public String mBssid;
    /* access modifiers changed from: private */
    public HwChannelQoEMonitor mChannelQoEMonitor;
    /* access modifiers changed from: private */
    public int mCounter;
    /* access modifiers changed from: private */
    public int mCursor;
    /* access modifiers changed from: private */
    public List<HwChannelQoEAppInfo> mMonitorCallbackList;
    private Context mMonitorContext;
    private Handler mMonitorHandler;
    /* access modifiers changed from: private */
    public int preRssi = 0;
    private int sendDelay;

    private class ChQoeBroadcastReceiver extends BroadcastReceiver {
        private ChQoeBroadcastReceiver() {
        }

        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            HwChannelQoEMonitor hwChannelQoEMonitor = HwChannelQoEMonitor.this;
            hwChannelQoEMonitor.logE("ChQoeBroadcastReceiver receive broadcast: " + action);
            if (!"com.android.server.hidata.arbitration.HwArbitrationStateMachine".equals(action)) {
                HwChannelQoEMonitor.this.logE("ChQoeBroadcastReceiver receive broadcast: com.android.server.hidata.arbitration.HwArbitrationStateMachine");
            } else if (801 != intent.getIntExtra("MPLinkSuccessNetworkKey", 802)) {
                HwChannelQoEMonitor.this.logE("network type is not CELL.");
            } else {
                WifiInfo info = HwChannelQoEMonitor.this.getWifiInfo();
                if (info == null || -127 == info.getRssi()) {
                    int unused = HwChannelQoEMonitor.this.preRssi = -1;
                    HwChannelQoEMonitor.this.logE("there is no wifi or invalid value, set preRssi -1.");
                } else {
                    int unused2 = HwChannelQoEMonitor.this.preRssi = info.getRssi();
                    HwChannelQoEMonitor hwChannelQoEMonitor2 = HwChannelQoEMonitor.this;
                    hwChannelQoEMonitor2.logE("set preRssi " + HwChannelQoEMonitor.this.preRssi);
                }
            }
        }
    }

    public HwChannelQoEMonitor(Context context) {
        this.mMonitorContext = context;
        this.mChannelQoEMonitor = this;
        this.mMonitorHandler = new Handler() {
            public void handleMessage(Message msg) {
                HwChannelQoEMonitor hwChannelQoEMonitor = HwChannelQoEMonitor.this;
                hwChannelQoEMonitor.logE("handleMessage:" + String.valueOf(msg.what));
                switch (msg.what) {
                    case HwChannelQoEMonitor.MESSAGE_MEASURE /*65280*/:
                        String currentBssid = HwChannelQoEMonitor.this.getWifiBssid();
                        if (currentBssid == null) {
                            HwChannelQoEMonitor.this.logE("wifi isn't connected, reset good_times and will Re-Measure in 3000 milliseconds");
                            HwChannelQoEMonitor.this.resetGoodTimes();
                            String unused = HwChannelQoEMonitor.this.mBssid = null;
                            sendEmptyMessageDelayed(HwChannelQoEMonitor.MESSAGE_MEASURE, 3000);
                            return;
                        }
                        if (HwChannelQoEMonitor.this.mBssid == null) {
                            HwChannelQoEMonitor.this.logE("First time or wifi disconnected.");
                            String unused2 = HwChannelQoEMonitor.this.mBssid = currentBssid;
                        }
                        if (!currentBssid.equals(HwChannelQoEMonitor.this.mBssid)) {
                            HwChannelQoEMonitor.this.logE("Bssid is changed. reset good times.");
                            String unused3 = HwChannelQoEMonitor.this.mBssid = currentBssid;
                            HwChannelQoEMonitor.this.resetGoodTimes();
                        }
                        int quality = HwChannelQoEMonitor.this.getWifiQuality();
                        if (1 == quality) {
                            for (HwChannelQoEAppInfo appInfo : HwChannelQoEMonitor.this.mMonitorCallbackList) {
                                HwChannelQoEManager.getInstance().queryChannelQuality(appInfo.mUID, appInfo.mScence, appInfo.mNetwork, appInfo.mQci, HwChannelQoEMonitor.this.mChannelQoEMonitor);
                            }
                            int unused4 = HwChannelQoEMonitor.this.mCounter = HwChannelQoEMonitor.this.mMonitorCallbackList.size();
                            int unused5 = HwChannelQoEMonitor.this.mCursor = 0;
                            break;
                        } else if (2 == quality) {
                            HwChannelQoEMonitor hwChannelQoEMonitor2 = HwChannelQoEMonitor.this;
                            hwChannelQoEMonitor2.logE("wifi quality bad. reset good times and will Re-Measure in " + String.valueOf(3000) + " milliseconds");
                            HwChannelQoEMonitor.this.resetGoodTimes();
                            sendEmptyMessageDelayed(HwChannelQoEMonitor.MESSAGE_MEASURE, 3000);
                            break;
                        } else if (3 == quality) {
                            HwChannelQoEMonitor hwChannelQoEMonitor3 = HwChannelQoEMonitor.this;
                            hwChannelQoEMonitor3.logE("wifi quality WIFI_QUALITY_UNKNOW, will Re-Measure in " + String.valueOf(3000) + " milliseconds");
                            sendEmptyMessageDelayed(HwChannelQoEMonitor.MESSAGE_MEASURE, 3000);
                            break;
                        } else {
                            HwChannelQoEMonitor.this.logE("there is no wifi network available.");
                            return;
                        }
                    case HwChannelQoEMonitor.MESSAGE_START /*65281*/:
                        HwChannelQoEAppInfo info = (HwChannelQoEAppInfo) msg.obj;
                        HwChannelQoEMonitor.this.mMonitorCallbackList.add(info);
                        HwCHQciConfig config = HwCHQciManager.getInstance().getChQciConfig(info.mQci);
                        int unused6 = HwChannelQoEMonitor.this.judge_chload = config.mChload;
                        int unused7 = HwChannelQoEMonitor.this.judge_rssi = config.mRssi;
                        if (!hasMessages(HwChannelQoEMonitor.MESSAGE_MEASURE)) {
                            sendEmptyMessage(HwChannelQoEMonitor.MESSAGE_MEASURE);
                            break;
                        } else {
                            HwChannelQoEMonitor.this.logE("startMonitor already running.");
                            break;
                        }
                    case HwChannelQoEMonitor.MESSAGE_STOP /*65282*/:
                        int UID = msg.arg1;
                        Iterator it = HwChannelQoEMonitor.this.mMonitorCallbackList.iterator();
                        while (true) {
                            if (it.hasNext()) {
                                HwChannelQoEAppInfo appInfo2 = (HwChannelQoEAppInfo) it.next();
                                if (appInfo2.mUID == UID) {
                                    HwChannelQoEMonitor.this.mMonitorCallbackList.remove(appInfo2);
                                }
                            }
                        }
                        if (HwChannelQoEMonitor.this.mMonitorCallbackList.isEmpty()) {
                            HwChannelQoEMonitor.this.stopRunning();
                            break;
                        }
                        break;
                    case HwChannelQoEMonitor.MESSAGE_STOP_ALL /*65283*/:
                        if (!HwChannelQoEMonitor.this.mMonitorCallbackList.isEmpty()) {
                            HwChannelQoEMonitor.this.mMonitorCallbackList.clear();
                        }
                        HwChannelQoEMonitor.this.stopRunning();
                        break;
                    default:
                        HwChannelQoEMonitor.this.logE("unknown message.");
                        break;
                }
            }
        };
        this.mMonitorCallbackList = new ArrayList();
        this.sendDelay = 60000;
        this.mBssid = null;
        this.lastChload = -1;
        this.judge_chload = 200;
        this.judge_rssi = -65;
        logE("new HwChannelQoEMonitor.");
        logE("WIFI_CHLOAD_THRESHOLD:" + String.valueOf(200));
        logE("WIFI_RSSI_THRESHOLD:" + String.valueOf(-65));
        logE("START_DELAY" + String.valueOf(10000));
        logE("WIFI_MONITOR_TIMES" + String.valueOf(2));
        this.chipsetWithChload = isChipHasChload();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.server.hidata.arbitration.HwArbitrationStateMachine");
        this.mMonitorContext.registerReceiver(this.mBroadcastReceiver, intentFilter, "com.huawei.hidata.permission.MPLINK_START_CHECK", null);
    }

    private boolean alreadyExist(int UID) {
        if (this.mMonitorCallbackList.isEmpty()) {
            return false;
        }
        for (HwChannelQoEAppInfo appInfo : this.mMonitorCallbackList) {
            if (appInfo.mUID == UID) {
                return true;
            }
        }
        return false;
    }

    public void startMonitor(HwChannelQoEAppInfo appQoeInfo) {
        if (alreadyExist(appQoeInfo.mUID)) {
            logE("startMonitor alreadyExist: " + String.valueOf(appQoeInfo.mUID));
            return;
        }
        Message msg = Message.obtain();
        msg.what = MESSAGE_START;
        msg.obj = appQoeInfo;
        this.mMonitorHandler.sendMessageDelayed(msg, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        logE("startMonitor will start in 10000 milliseconds.");
    }

    public void stopMonitor(int UID) {
        logE("stopMonitor: " + String.valueOf(UID));
        Message msg = Message.obtain();
        msg.what = MESSAGE_STOP;
        msg.arg1 = UID;
        this.mMonitorHandler.sendMessage(msg);
    }

    public void stopAll() {
        logE("stopAll");
        this.mMonitorHandler.sendEmptyMessage(MESSAGE_STOP_ALL);
    }

    /* access modifiers changed from: private */
    public void stopRunning() {
        logE("stopRunning");
        this.lastChload = -1;
        this.judge_chload = 200;
        this.judge_rssi = -65;
        this.mMonitorHandler.removeMessages(MESSAGE_MEASURE);
        this.mMonitorHandler.removeMessages(MESSAGE_START);
    }

    /* access modifiers changed from: private */
    public String getWifiBssid() {
        WifiManager mWManager = (WifiManager) this.mMonitorContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
        if (mWManager == null) {
            logE("can't find wifi manager.");
            return null;
        }
        WifiInfo info = mWManager.getConnectionInfo();
        if (info != null) {
            return info.getBSSID();
        }
        logE("there is no wifi connected.");
        return null;
    }

    /* access modifiers changed from: private */
    public int getWifiQuality() {
        if (!this.chipsetWithChload) {
            return judgeWithoutChload();
        }
        return judgeWithChload();
    }

    private int judgeWithoutChload() {
        WifiInfo wifiInfo = getWifiInfo();
        if (wifiInfo == null) {
            logE("judgeWithoutChload get null Wifi Info.");
            return -1;
        }
        int currentRssi = wifiInfo.getRssi();
        logE("current rssi " + currentRssi);
        if ((this.preRssi >= this.judge_rssi || currentRssi < this.judge_rssi) && (this.preRssi < this.judge_rssi || currentRssi - this.preRssi < 15)) {
            logE("judgeWithoutChload return bad.");
            return 2;
        }
        logE("judgeWithoutChload return good.");
        return 1;
    }

    private int judgeWithChload() {
        WifiInfo info = getWifiInfo();
        if (info == null) {
            logE("judgeWithChload: there is no wifi connected.");
            return -1;
        }
        int rssi = info.getRssi();
        int chload = info.getChload();
        logE("judgeWithChload. RSSI is " + String.valueOf(rssi));
        logE("judgeWithChload. CHLoad is " + String.valueOf(chload));
        if (-1 != chload) {
            this.lastChload = chload;
        } else if (this.lastChload == -1) {
            logE("last time the chload is also -1.");
            return 3;
        } else {
            logE("last time the chload is not -1. last is " + this.lastChload);
            chload = this.lastChload;
        }
        if (rssi < this.judge_rssi || chload > this.judge_chload) {
            return 2;
        }
        return 1;
    }

    /* access modifiers changed from: private */
    public WifiInfo getWifiInfo() {
        WifiManager mWManager = (WifiManager) this.mMonitorContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
        if (mWManager == null) {
            logE("getWifiInfo: can't find wifi manager.");
            return null;
        }
        WifiInfo info = mWManager.getConnectionInfo();
        if (info != null) {
            return info;
        }
        logE("getWifiInfo: there is no wifi connected.");
        return null;
    }

    /* access modifiers changed from: private */
    public void logE(String info) {
        Log.e(TAG, info);
    }

    public void onChannelQuality(int UID, int sense, int network, int label) {
        this.mCursor++;
        logE("onChannelQuality enter. " + String.valueOf(this.mCursor));
        if (this.mMonitorCallbackList.isEmpty()) {
            logE("onChannelQuality callback list is empty, maybe monitor has been stopped.");
            return;
        }
        for (HwChannelQoEAppInfo appInfo : this.mMonitorCallbackList) {
            if (appInfo.mUID == UID) {
                if (label == 0) {
                    appInfo.good_times++;
                    logE(String.valueOf(appInfo.mUID) + " good times is " + String.valueOf(appInfo.good_times));
                    if (appInfo.good_times > 2) {
                        logE("3 times for good measure. will stop monitor for UID:" + UID);
                        appInfo.callback.onWifiLinkQuality(appInfo.mUID, appInfo.mScence, 0);
                        stopMonitor(UID);
                    }
                    logE("set delay time to 3 sec.");
                    this.sendDelay = 3000;
                } else {
                    logE("wifi rtt result is bad, anyway set good times to 0.");
                    appInfo.good_times = 0;
                    this.sendDelay = 60000;
                }
            }
        }
        if (this.mCounter == this.mCursor) {
            logE("all done, will Re-Measure in " + this.sendDelay + " milliseconds");
            this.mMonitorHandler.sendEmptyMessageDelayed(MESSAGE_MEASURE, (long) this.sendDelay);
            this.sendDelay = 60000;
        }
    }

    public void onWifiLinkQuality(int UID, int sense, int label) {
    }

    public void onCellPSAvailable(boolean isOK, int reason) {
    }

    public void onCurrentRtt(int rtt) {
    }

    /* access modifiers changed from: private */
    public void resetGoodTimes() {
        for (HwChannelQoEAppInfo info : this.mMonitorCallbackList) {
            info.good_times = 0;
        }
    }

    private boolean isChipHasChload() {
        String chipset = SystemProperties.get(HwAPPQoEUserAction.CHIPSET_TYPE_PROP, "none");
        logE("isChipHasChload, chipset = " + chipset);
        if (chipset == null || !chipset.contains("1103")) {
            return false;
        }
        return true;
    }
}
