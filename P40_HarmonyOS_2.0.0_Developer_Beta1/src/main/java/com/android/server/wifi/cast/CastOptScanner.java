package com.android.server.wifi.cast;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.WorkSource;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import java.util.List;

public class CastOptScanner {
    private static final int CMD_START_STA_FULL_SCAN = 102;
    private static final int CMD_START_STA_SCAN = 101;
    private static final int INVALID_FREQ = 0;
    private static final int MAX_SCAN_COUNT = 3;
    private static final int SCAN_FULL_CHANNEL_INTERVAL = 1000;
    private static final int SCAN_INTERVAL = 10000;
    private static final String TAG = "CastOptScanner";
    protected static final int TRIGGER_SCAN_AS_SCREEN_ON = 2;
    protected static final int TRIGGER_SCAN_AS_STA_DISCONNECT = 1;
    private static CastOptScanner sCastOptScanner = null;
    private CastOptMonitor mCastOptMonitor = null;
    private ClientModeImpl mClientModeImpl = null;
    private Context mContext = null;
    private Handler mHandler = null;
    private boolean mIsScreenOn = false;
    private Looper mLooper = null;
    private int mScanCountWhenDisconnect = 0;
    private CastOptScanListener mScanListener = null;
    private WifiScanner mScanner = null;
    private List<Integer> mSupportChannleList = null;
    private WifiConfigManager mWifiConfigManager = null;
    private WifiInjector mWifiInjector = null;
    private WifiManager mWifiManager = null;

    private CastOptScanner(Context context, Looper looper, CastOptMonitor castOptMonitor) {
        this.mContext = context;
        this.mCastOptMonitor = castOptMonitor;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mWifiInjector = WifiInjector.getInstance();
        this.mClientModeImpl = this.mWifiInjector.getClientModeImpl();
        this.mWifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        this.mScanner = this.mWifiInjector.getWifiScanner();
        this.mScanListener = new CastOptScanListener();
        initHandler(looper);
    }

    /* access modifiers changed from: private */
    public class CastOptScanListener implements WifiScanner.ScanListener {
        private CastOptScanListener() {
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            HwHiLog.e(CastOptScanner.TAG, false, "scan failure received. reason: %{public}d", new Object[]{Integer.valueOf(reason)});
        }

        public void onResults(WifiScanner.ScanData[] scanDatas) {
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPeriodChanged(int periodInMs) {
        }
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            /* class com.android.server.wifi.cast.CastOptScanner.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 101) {
                    CastOptScanner.this.startScan();
                } else if (i != 102) {
                    HwHiLog.i(CastOptScanner.TAG, false, "unknown msg", new Object[0]);
                } else {
                    int scanFreq = msg.arg1;
                    CastOptScanner.this.startSingleChannelScan(scanFreq);
                    if (CastOptScanner.this.mSupportChannleList != null && CastOptScanner.this.mSupportChannleList.contains(Integer.valueOf(scanFreq))) {
                        if (CastOptScanner.this.mSupportChannleList.indexOf(Integer.valueOf(scanFreq)) == CastOptScanner.this.mSupportChannleList.size() - 1) {
                            HwHiLog.i(CastOptScanner.TAG, false, "has scan last channel of mSupportChannleList", new Object[0]);
                            return;
                        }
                        CastOptScanner.this.mHandler.sendMessageDelayed(CastOptScanner.this.mHandler.obtainMessage(102, ((Integer) CastOptScanner.this.mSupportChannleList.get(CastOptScanner.this.mSupportChannleList.indexOf(Integer.valueOf(scanFreq)) + 1)).intValue(), 0), 1000);
                    }
                }
            }
        };
    }

    protected static CastOptScanner createCastOptScanner(Context context, Looper looper, CastOptMonitor castOptMonitor) {
        if (sCastOptScanner == null) {
            sCastOptScanner = new CastOptScanner(context, looper, castOptMonitor);
        }
        return sCastOptScanner;
    }

    protected static CastOptScanner getInstance() {
        return sCastOptScanner;
    }

    private boolean isSuppOnDisconnectedState() {
        WifiInfo info = this.mClientModeImpl.getWifiInfo();
        if (info == null || !info.getSupplicantState().equals(SupplicantState.DISCONNECTED)) {
            return false;
        }
        return true;
    }

    private boolean isAllowScan() {
        if (!isSuppOnDisconnectedState()) {
            HwHiLog.i(TAG, false, "supplicant state is not disconnnted", new Object[0]);
            return false;
        } else if (!this.mIsScreenOn) {
            return false;
        } else {
            CastOptManager castOptManager = CastOptManager.getInstance();
            if (!castOptManager.isCastOptScenes()) {
                HwHiLog.i(TAG, false, "is out of cast opt scene, don't trigger scan", new Object[0]);
                return false;
            } else if (!castOptManager.isInP2pSharing()) {
                return true;
            } else {
                HwHiLog.i(TAG, false, "is in p2psharing state, don't trigger scan", new Object[0]);
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void triggerScan(int triggerType) {
        if (!isAllowScan()) {
            HwHiLog.i(TAG, false, "not allow scan in triggerScan", new Object[0]);
            return;
        }
        List<WifiConfiguration> savedNetworks = this.mWifiManager.getConfiguredNetworks();
        if (savedNetworks == null || savedNetworks.size() == 0) {
            HwHiLog.i(TAG, false, "don't have saved network", new Object[0]);
        } else if (triggerType == 1 || triggerType == 2) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(101));
        } else {
            HwHiLog.i(TAG, false, "trigger type is not support: %{public}d", new Object[]{Integer.valueOf(triggerType)});
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startScan() {
        if (!isAllowScan()) {
            HwHiLog.i(TAG, false, "not allow scan", new Object[0]);
            return;
        }
        int i = this.mScanCountWhenDisconnect;
        if (i <= 3) {
            this.mScanCountWhenDisconnect = i + 1;
            int i2 = this.mScanCountWhenDisconnect;
            if (i2 == 1) {
                startSingleChannelScan(this.mCastOptMonitor.getP2pFrequency());
                Handler handler = this.mHandler;
                handler.sendMessageDelayed(handler.obtainMessage(101), 10000);
            } else if (i2 == 2) {
                startSingleChannelScan(this.mCastOptMonitor.getLastStaFrequency());
                Handler handler2 = this.mHandler;
                handler2.sendMessageDelayed(handler2.obtainMessage(101), 10000);
            } else if (i2 == 3) {
                startFullChannelScan();
            } else {
                HwHiLog.i(TAG, false, "mScanCountWhenDisconnect is: %{public}d", new Object[]{Integer.valueOf(i2)});
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSingleChannelScan(int scanFreq) {
        if (scanFreq == 0) {
            HwHiLog.i(TAG, false, "scan freq is invalid", new Object[0]);
        } else if (isAllowScan()) {
            HwHiLog.i(TAG, false, "scan single channel : %{public}d", new Object[]{Integer.valueOf(scanFreq)});
            WifiScanner.ScanSettings scanSettings = new WifiScanner.ScanSettings();
            List<WifiScanner.ScanSettings.HiddenNetwork> hiddenNetworkList = this.mWifiConfigManager.retrieveHiddenNetworkList();
            if (hiddenNetworkList != null) {
                scanSettings.hiddenNetworks = (WifiScanner.ScanSettings.HiddenNetwork[]) hiddenNetworkList.toArray(new WifiScanner.ScanSettings.HiddenNetwork[hiddenNetworkList.size()]);
            }
            scanSettings.type = 2;
            scanSettings.band = 0;
            scanSettings.reportEvents = 3;
            scanSettings.numBssidsPerScan = 0;
            scanSettings.channels = new WifiScanner.ChannelSpec[]{new WifiScanner.ChannelSpec(scanFreq)};
            this.mScanner.startScan(scanSettings, this.mScanListener, new WorkSource((int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM));
        }
    }

    private void startFullChannelScan() {
        this.mSupportChannleList = this.mScanner.getAvailableChannels(7);
        List<Integer> list = this.mSupportChannleList;
        if (list == null) {
            HwHiLog.i(TAG, false, "mSupportChannleList is null", new Object[0]);
            return;
        }
        int scanFreq = list.get(0).intValue();
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(102, scanFreq, 0));
    }

    /* access modifiers changed from: protected */
    public void resetScanner() {
        if (this.mHandler.hasMessages(102)) {
            this.mHandler.removeMessages(102);
        }
        this.mScanCountWhenDisconnect = 0;
    }

    /* access modifiers changed from: protected */
    public void setScreenOn(boolean isScreenOn) {
        this.mIsScreenOn = isScreenOn;
    }
}
