package com.android.server.wifi.wifipro;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.WifiProCommonUtils;

public class HwNetworkPropertyRechecker extends HwNetworkPropertyChecker {
    public static final int MSG_REQUEST_NETWORK_CHECK = 101;
    private boolean checkRunning;
    private Object mCheckLock = new Object();
    private Handler mLocalHandler = null;
    private NetworkQosMonitor mNetworkQosMonitor;
    private Object mQueryPktLock = new Object();
    private boolean waitingResponse;

    public HwNetworkPropertyRechecker(Context context, WifiManager wifiManager, TelephonyManager telManager, boolean enabled, NetworkAgentInfo agent, NetworkQosMonitor monitor) {
        super(context, wifiManager, telManager, enabled, agent, false);
        this.mNetworkQosMonitor = monitor;
        this.waitingResponse = false;
        this.checkRunning = false;
        init();
    }

    private void init() {
        HandlerThread handlerThread = new HandlerThread("wifipro_network_rechecker_thread");
        handlerThread.start();
        this.mLocalHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:
                        synchronized (HwNetworkPropertyRechecker.this.mCheckLock) {
                            boolean z;
                            boolean z2;
                            HwNetworkPropertyRechecker.this.checkRunning = true;
                            String startSsid = WifiProCommonUtils.getCurrentSsid(HwNetworkPropertyRechecker.this.mWifiManager);
                            boolean isWifiBackground = false;
                            if (msg.obj != null) {
                                isWifiBackground = ((Boolean) msg.obj).booleanValue();
                            }
                            HwNetworkPropertyRechecker hwNetworkPropertyRechecker = HwNetworkPropertyRechecker.this;
                            if (msg.arg1 == 1) {
                                z = true;
                            } else {
                                z = false;
                            }
                            if (msg.arg2 == 1) {
                                z2 = true;
                            } else {
                                z2 = false;
                            }
                            int respCode = hwNetworkPropertyRechecker.recheckNetworkProperty(z, z2, true, isWifiBackground);
                            String endSsid = WifiProCommonUtils.getCurrentSsid(HwNetworkPropertyRechecker.this.mWifiManager);
                            HwNetworkPropertyRechecker.this.LOGW("startSsid = " + startSsid + ", endSsid = " + endSsid);
                            if (startSsid != null && startSsid.equals(endSsid)) {
                                HwNetworkPropertyRechecker.this.mNetworkQosMonitor.notifyNetworkResult(respCode);
                            }
                            HwNetworkPropertyRechecker.this.checkRunning = false;
                        }
                }
                super.handleMessage(msg);
            }
        };
    }

    public void asyncRequestNetworkCheck(int portal, int authen, boolean wifiBackground) {
        this.mLocalHandler.sendMessage(Message.obtain(this.mLocalHandler, 101, portal, authen, Boolean.valueOf(wifiBackground)));
    }

    public int syncRequestNetworkCheck(boolean portal, boolean authen, boolean wifiBackground) {
        if (this.checkRunning) {
            LOGW("syncRequestNetworkCheck, checkRunning = " + this.checkRunning + ", portal = " + portal);
            return portal ? 302 : 599;
        }
        int recheckNetworkProperty;
        synchronized (this.mCheckLock) {
            recheckNetworkProperty = recheckNetworkProperty(portal, authen, false, wifiBackground);
        }
        return recheckNetworkProperty;
    }

    private int recheckNetworkProperty(boolean portal, boolean authen, boolean needSleep, boolean wifiBackground) {
        LOGD("ENTER: recheckNetworkProperty, portal = " + portal + ", authen = " + authen + ", needSleep = " + needSleep + " wifiBackground = " + wifiBackground);
        this.mTcpRxFirstCounter = requestTcpRxPacketsCounter();
        long startTimestamp = System.currentTimeMillis();
        int respCode = isCaptivePortal(true, portal, wifiBackground);
        if (WifiProCommonUtils.isWifiConnected(this.mWifiManager) && WifiProCommonUtils.unreachableRespCode(respCode) && this.mTcpRxFirstCounter >= 0 && (this.mIgnoreRxCounter ^ 1) != 0) {
            if (needSleep) {
                long leftTime = 10000 - (System.currentTimeMillis() - startTimestamp);
                LOGD("recheckNetworkProperty, checking result is unreachable, sleep " + leftTime + " ms, and fetch the second tcp rx packets counter.");
                if (leftTime > 0) {
                    try {
                        Thread.sleep(leftTime);
                    } catch (InterruptedException e) {
                    }
                }
            }
            this.mTcpRxSecondCounter = requestTcpRxPacketsCounter();
            LOGD("firstRx = " + this.mTcpRxFirstCounter + ", secondRx = " + this.mTcpRxSecondCounter);
            if (this.mTcpRxSecondCounter - this.mTcpRxFirstCounter >= 3) {
                if (isMobileHotspot()) {
                    respCode = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                } else if (portal) {
                    respCode = authen ? HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC : 302;
                } else {
                    PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
                    String ssid = WifiProCommonUtils.getCurrentSsid(this.mWifiManager);
                    boolean portalMatched = database.syncQueryPortalNetwork(ssid);
                    respCode = portalMatched ? 302 : HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                    LOGD("recheckNetworkProperty, query portal database, portalMatched = " + portalMatched + ", ssid = " + ssid);
                }
                LOGD("recheckNetworkProperty, network's reachable, rx's increasing,  change 599 --> " + respCode);
            }
        }
        this.mTcpRxFirstCounter = 0;
        this.mTcpRxSecondCounter = 0;
        if (WifiProCommonUtils.isRedirectedRespCode(respCode) && portal && authen) {
            LOGW("recheckNetworkProperty, portal has been authenticated, respCode = " + respCode + " --> 204");
            respCode = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
        } else if (WifiProCommonUtils.unreachableRespCode(respCode) && WifiProCommonUtils.isWifiConnected(this.mWifiManager)) {
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
            if (!(wifiInfo == null || config == null || !needSleep)) {
                LOGW("recheckNetworkProperty, rssi = " + wifiInfo.getRssi() + ", noInternetAccess = " + config.noInternetAccess);
                if (wifiInfo.getRssi() >= -75 && (config.noInternetAccess ^ 1) != 0 && WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100) && !WifiProCommonUtils.unreachableRespCode(recheckWithBakcupServer(true))) {
                    respCode = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                }
            }
        }
        LOGD("LEAVE: recheckNetworkProperty, respCode = " + respCode);
        return respCode;
    }

    public int requestTcpRxPacketsCounter() {
        int rxCounter = -1;
        if (this.mNetworkQosMonitor == null) {
            return 0;
        }
        LOGD("requestTcpRxPacketsCounter, start to request tcp rx packets counter.");
        synchronized (this.mQueryPktLock) {
            try {
                this.mNetworkQosMonitor.startQueryRxPackets();
                this.mNetworkQosMonitor.queryRxPackets();
                this.waitingResponse = true;
                while (this.waitingResponse) {
                    LOGD("requestTcpRxPacketsCounter, start wait for notify");
                    this.mQueryPktLock.wait();
                }
                if (this.mTcpRxCounter >= 0) {
                    rxCounter = this.mTcpRxCounter;
                }
                LOGD("tcp rx packets counter received rx = " + this.mTcpRxCounter);
            } catch (InterruptedException e) {
            }
        }
        this.mTcpRxCounter = 0;
        return rxCounter;
    }

    public void responseTcpRxPacketsCounter(int counter) {
        synchronized (this.mQueryPktLock) {
            this.mTcpRxCounter = counter;
            this.waitingResponse = false;
            LOGD("responseTcpRxPacketsCounter, mQueryPktLock.notifyAll() called, current counter = " + this.mTcpRxCounter);
            this.mQueryPktLock.notifyAll();
        }
    }

    public void release() {
        super.release();
        if (this.mLocalHandler != null) {
            Looper looper = this.mLocalHandler.getLooper();
            if (looper != null && looper != Looper.getMainLooper()) {
                looper.quitSafely();
                LOGD("HwNetworkPropertyRechecker$HandlerThread::Release");
            }
        }
    }
}
