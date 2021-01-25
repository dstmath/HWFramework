package com.android.server.hidata.wavemapping.dataprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WifiDataProvider extends Handler {
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final String TAG = ("WMapping." + WifiDataProvider.class.getSimpleName());
    private static WifiDataProvider mWifiInstance = null;
    private boolean isStartCollect = false;
    private Context mCtx;
    private Handler mMachineHandler;
    private long mScanEndTime = 0;
    private WifiManager mWifiManager;
    private HandlerThread mWifiThread = null;
    private ParameterInfo param = null;
    private int receivedPoints = 0;
    private List<ScanResult> wifiList;
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        /* class com.android.server.hidata.wavemapping.dataprovider.WifiDataProvider.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                char c = 65535;
                if (action.hashCode() == 1878357501 && action.equals("android.net.wifi.SCAN_RESULTS")) {
                    c = 0;
                }
                if (c == 0) {
                    if (intent.getBooleanExtra("resultsUpdated", false)) {
                        LogUtil.i(false, "receive new wifi results", new Object[0]);
                        if (System.currentTimeMillis() - WifiDataProvider.this.mScanEndTime < ((long) WifiDataProvider.this.param.getWifiDataSample())) {
                            LogUtil.i(false, "exit due to wifi scan too closed", new Object[0]);
                            return;
                        }
                        WifiDataProvider wifiDataProvider = WifiDataProvider.this;
                        wifiDataProvider.wifiList = wifiDataProvider.mWifiManager.getScanResults();
                        if (WifiDataProvider.this.wifiList == null || WifiDataProvider.this.wifiList.size() == 0) {
                            LogUtil.d(false, "exit due to wifi list is empity", new Object[0]);
                            return;
                        }
                        WifiDataProvider.this.mScanEndTime = System.currentTimeMillis();
                        WifiDataProvider.this.mMachineHandler.sendEmptyMessage(62);
                        WifiDataProvider.this.mMachineHandler.sendEmptyMessage(63);
                        WifiDataProvider.this.mMachineHandler.sendEmptyMessage(64);
                        return;
                    }
                    LogUtil.i(false, "receive duplicated wifi results", new Object[0]);
                }
            }
        }
    };

    private WifiDataProvider(Context ctx, HandlerThread thread, Handler handler) {
        super(thread.getLooper());
        this.mWifiThread = thread;
        this.mWifiManager = (WifiManager) ctx.getSystemService("wifi");
        this.mCtx = ctx;
        LogUtil.i(false, "ParamManager init begin.", new Object[0]);
        this.param = ParamManager.getInstance().getParameterInfo();
        this.mMachineHandler = handler;
    }

    public static WifiDataProvider getInstance(Context context, Handler handler) {
        if (mWifiInstance == null) {
            HandlerThread handlerThread = new HandlerThread("WifiDataProviderThread", 10);
            handlerThread.start();
            mWifiInstance = new WifiDataProvider(context, handlerThread, handler);
        }
        return mWifiInstance;
    }

    public static WifiDataProvider getInstance(Context context) {
        return mWifiInstance;
    }

    public void start() {
        try {
            LogUtil.i(false, " wifidataprovider start ..", new Object[0]);
            IntentFilter intent = new IntentFilter();
            intent.addAction("android.net.wifi.SCAN_RESULTS");
            this.mCtx.registerReceiver(this.wifiReceiver, intent);
        } catch (Exception e) {
            LogUtil.e(false, "start failed by Exception", new Object[0]);
        }
    }

    public void stop() {
        LogUtil.i(false, "wifidataprovider stop", new Object[0]);
        this.mCtx.unregisterReceiver(this.wifiReceiver);
    }

    private String getTime(long time) {
        return new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date(time));
    }

    public void clear() {
        this.receivedPoints = 0;
        this.isStartCollect = false;
    }

    public List<ScanResult> getWifiList() {
        return this.wifiList;
    }

    public void setWifiList(List<ScanResult> wifiList2) {
        this.wifiList = wifiList2;
    }
}
