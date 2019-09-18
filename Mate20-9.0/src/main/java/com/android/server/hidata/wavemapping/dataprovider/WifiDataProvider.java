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
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WifiDataProvider extends Handler {
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final String TAG = ("WMapping." + WifiDataProvider.class.getSimpleName());
    private static WifiDataProvider mWifiInstance = null;
    private boolean isStartCluster = false;
    private Context mCtx;
    /* access modifiers changed from: private */
    public Handler mMachineHandler;
    /* access modifiers changed from: private */
    public long mScanEndTime = 0;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    private HandlerThread mWifiThread = null;
    /* access modifiers changed from: private */
    public ParameterInfo param = null;
    private int receivedPoints = 0;
    private boolean startCollect = false;
    /* access modifiers changed from: private */
    public List<ScanResult> wifiList;
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                char c = 65535;
                if (action.hashCode() == 1878357501 && action.equals("android.net.wifi.SCAN_RESULTS")) {
                    c = 0;
                }
                if (c == 0) {
                    if (intent.getBooleanExtra("resultsUpdated", false)) {
                        LogUtil.i("receive new wifi results");
                        if (System.currentTimeMillis() - WifiDataProvider.this.mScanEndTime < ((long) WifiDataProvider.this.param.getWifiDataSample())) {
                            LogUtil.i("exit due to wifi scan too closed");
                        } else {
                            List unused = WifiDataProvider.this.wifiList = WifiDataProvider.this.mWifiManager.getScanResults();
                            if (WifiDataProvider.this.wifiList == null || WifiDataProvider.this.wifiList.size() == 0) {
                                LogUtil.d("exit due to wifi list is empity");
                            } else {
                                long unused2 = WifiDataProvider.this.mScanEndTime = System.currentTimeMillis();
                                WifiDataProvider.this.mMachineHandler.sendEmptyMessage(62);
                                WifiDataProvider.this.mMachineHandler.sendEmptyMessage(63);
                                WifiDataProvider.this.mMachineHandler.sendEmptyMessage(64);
                            }
                        }
                    } else {
                        LogUtil.i("receive duplicated wifi results");
                    }
                }
            }
        }
    };

    public List<ScanResult> getWifiList() {
        return this.wifiList;
    }

    public void setWifiList(List<ScanResult> wifiList2) {
        this.wifiList = wifiList2;
    }

    private WifiDataProvider(Context ctx, HandlerThread thread, Handler handler) {
        super(thread.getLooper());
        this.mWifiThread = thread;
        this.mWifiManager = (WifiManager) ctx.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
        this.mCtx = ctx;
        LogUtil.i("ParamManager init begin.");
        this.param = ParamManager.getInstance().getParameterInfo();
        this.mMachineHandler = handler;
    }

    public static WifiDataProvider getInstance(Context aContext, Handler handler) {
        if (mWifiInstance == null) {
            HandlerThread lWifiThread = new HandlerThread("WifiDataProviderThread", 10);
            lWifiThread.start();
            mWifiInstance = new WifiDataProvider(aContext, lWifiThread, handler);
        }
        return mWifiInstance;
    }

    public static WifiDataProvider getInstance(Context aContext) {
        return mWifiInstance;
    }

    public void start() {
        try {
            LogUtil.i(" wifidataprovider start ..");
            IntentFilter intent = new IntentFilter();
            intent.addAction("android.net.wifi.SCAN_RESULTS");
            this.mCtx.registerReceiver(this.wifiReceiver, intent);
        } catch (Exception e) {
            LogUtil.e("start:" + e.getMessage());
        }
    }

    public void stop() {
        LogUtil.i("wifidataprovider stop");
        this.mCtx.unregisterReceiver(this.wifiReceiver);
    }

    private String getTime(long time) {
        return new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date(time));
    }

    public void clear() {
        this.receivedPoints = 0;
        this.startCollect = false;
    }
}
