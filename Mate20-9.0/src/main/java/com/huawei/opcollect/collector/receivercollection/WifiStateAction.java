package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class WifiStateAction extends Action {
    private static final String TAG = "WifiStateAction";
    private static WifiStateAction sInstance = null;
    private boolean mIsFirstInitialized = true;
    private boolean mIsWifiOn = false;
    private WifiStateBroadcastReceiver mReceiver = null;
    /* access modifiers changed from: private */
    public int mWifiState = -1;

    class WifiStateBroadcastReceiver extends BroadcastReceiver {
        WifiStateBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r("WifiStateAction", "onReceive: " + action);
                if ("android.net.wifi.WIFI_STATE_CHANGED".equalsIgnoreCase(action)) {
                    int unused = WifiStateAction.this.mWifiState = intent.getIntExtra("wifi_state", 1);
                    WifiStateAction.this.perform();
                }
            }
        }
    }

    public static synchronized WifiStateAction getInstance(Context context) {
        WifiStateAction wifiStateAction;
        synchronized (WifiStateAction.class) {
            if (sInstance == null) {
                sInstance = new WifiStateAction(context, "WifiStateAction");
            }
            wifiStateAction = sInstance;
        }
        return wifiStateAction;
    }

    private WifiStateAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_WIFI_ON) + SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_WIFI_OFF));
        OPCollectLog.r("WifiStateAction", "WifiStateAction");
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new WifiStateBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.mContext.registerReceiver(this.mReceiver, intentFilter, null, OdmfCollectScheduler.getInstance().getCtrlHandler());
            WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            String value = SysEventUtil.OFF;
            if (wifiManager != null) {
                int state = wifiManager.getWifiState();
                if (state == 3 || state == 2) {
                    value = SysEventUtil.ON;
                }
            }
            SysEventUtil.collectKVSysEventData("wireless_networks/wifi_status", SysEventUtil.WIFI_STATUS, value);
            OPCollectLog.r("WifiStateAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        if (this.mWifiState == 1) {
            if (this.mIsWifiOn || this.mIsFirstInitialized) {
                SysEventUtil.collectSysEventData(SysEventUtil.EVENT_WIFI_OFF);
                SysEventUtil.collectKVSysEventData("wireless_networks/wifi_status", SysEventUtil.WIFI_STATUS, SysEventUtil.OFF);
                if (this.mIsFirstInitialized) {
                    this.mIsFirstInitialized = false;
                }
                this.mIsWifiOn = false;
                return true;
            }
        } else if (this.mWifiState == 3 && !this.mIsWifiOn) {
            SysEventUtil.collectSysEventData(SysEventUtil.EVENT_WIFI_ON);
            SysEventUtil.collectKVSysEventData("wireless_networks/wifi_status", SysEventUtil.WIFI_STATUS, SysEventUtil.ON);
            this.mIsWifiOn = true;
            return true;
        }
        OPCollectLog.r("WifiStateAction", "ignore transtion or duplicate data.");
        return false;
    }

    public void disable() {
        super.disable();
        if (this.mReceiver != null && this.mContext != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static synchronized void destroyInstance() {
        synchronized (WifiStateAction.class) {
            sInstance = null;
        }
    }

    public void dump(int indentNum, PrintWriter pw) {
        super.dump(indentNum, pw);
        if (pw != null) {
            String indent = String.format("%" + indentNum + "s\\-", new Object[]{" "});
            if (this.mReceiver == null) {
                pw.println(indent + "receiver is null");
            } else {
                pw.println(indent + "receiver not null");
            }
        }
    }
}
