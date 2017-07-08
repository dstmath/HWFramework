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
    private Object mCheckLock;
    private Handler mLocalHandler;
    private NetworkQosMonitor mNetworkQosMonitor;
    private Object mQueryPktLock;
    private boolean waitingResponse;

    /* renamed from: com.android.server.wifi.wifipro.HwNetworkPropertyRechecker.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case HwNetworkPropertyRechecker.MSG_REQUEST_NETWORK_CHECK /*101*/:
                    synchronized (HwNetworkPropertyRechecker.this.mCheckLock) {
                        boolean z2;
                        HwNetworkPropertyRechecker.this.checkRunning = true;
                        String startSsid = WifiProCommonUtils.getCurrentSsid(HwNetworkPropertyRechecker.this.mWifiManager);
                        HwNetworkPropertyRechecker hwNetworkPropertyRechecker = HwNetworkPropertyRechecker.this;
                        if (msg.arg1 == 1) {
                            z2 = true;
                        } else {
                            z2 = false;
                        }
                        if (msg.arg2 != 1) {
                            z = false;
                        }
                        int respCode = hwNetworkPropertyRechecker.recheckNetworkProperty(z2, z, true);
                        String endSsid = WifiProCommonUtils.getCurrentSsid(HwNetworkPropertyRechecker.this.mWifiManager);
                        HwNetworkPropertyRechecker.this.LOGW("startSsid = " + startSsid + ", endSsid = " + endSsid);
                        if (startSsid != null && startSsid.equals(endSsid)) {
                            HwNetworkPropertyRechecker.this.mNetworkQosMonitor.notifyNetworkResult(respCode);
                        }
                        HwNetworkPropertyRechecker.this.checkRunning = false;
                        break;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public HwNetworkPropertyRechecker(Context context, WifiManager wifiManager, TelephonyManager telManager, boolean enabled, NetworkAgentInfo agent, NetworkQosMonitor monitor) {
        super(context, wifiManager, telManager, enabled, agent, false);
        this.mLocalHandler = null;
        this.mQueryPktLock = new Object();
        this.mCheckLock = new Object();
        this.mNetworkQosMonitor = monitor;
        this.waitingResponse = false;
        this.checkRunning = false;
        init();
    }

    private void init() {
        HandlerThread handlerThread = new HandlerThread("wifipro_network_rechecker_thread");
        handlerThread.start();
        this.mLocalHandler = new AnonymousClass1(handlerThread.getLooper());
    }

    public void asyncRequestNetworkCheck(int portal, int authen) {
        this.mLocalHandler.sendMessage(Message.obtain(this.mLocalHandler, MSG_REQUEST_NETWORK_CHECK, portal, authen));
    }

    public int syncRequestNetworkCheck(boolean portal, boolean authen) {
        if (this.checkRunning) {
            LOGW("syncRequestNetworkCheck, checkRunning = " + this.checkRunning + ", portal = " + portal);
            return portal ? 302 : 599;
        }
        int recheckNetworkProperty;
        synchronized (this.mCheckLock) {
            recheckNetworkProperty = recheckNetworkProperty(portal, authen, false);
        }
        return recheckNetworkProperty;
    }

    private int recheckNetworkProperty(boolean portal, boolean authen, boolean needSleep) {
        LOGD("ENTER: recheckNetworkProperty, portal = " + portal + ", authen = " + authen + ", needSleep = " + needSleep);
        this.mTcpRxFirstCounter = requestTcpRxPacketsCounter();
        long startTimestamp = System.currentTimeMillis();
        int respCode = isCaptivePortal(true);
        if (WifiProCommonUtils.isWifiConnected(this.mWifiManager) && WifiProCommonUtils.unreachableRespCode(respCode) && this.mTcpRxFirstCounter >= 0 && !this.mIgnoreRxCounter) {
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
        } else if (WifiProCommonUtils.unreachableRespCode(respCode)) {
            if (WifiProCommonUtils.isWifiConnected(this.mWifiManager)) {
                WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
                WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
                if (!(wifiInfo == null || config == null)) {
                    LOGW("recheckNetworkProperty, rssi = " + wifiInfo.getRssi() + ", noInternetAccess = " + config.noInternetAccess);
                    if (wifiInfo.getRssi() >= -75 && !config.noInternetAccess) {
                        if (WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100) && !WifiProCommonUtils.unreachableRespCode(recheckWithBakcupServer(true))) {
                            respCode = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                        }
                    }
                }
            }
        }
        LOGD("LEAVE: recheckNetworkProperty, respCode = " + respCode);
        return respCode;
    }

    public int requestTcpRxPacketsCounter() {
        int rxCounter = -1;
        LOGD("requestTcpRxPacketsCounter, start to request tcp rx packets counter.");
        synchronized (this.mQueryPktLock) {
            try {
                IPQosMonitor monitor = this.mNetworkQosMonitor.startQueryRxPackets();
                if (monitor != null) {
                    monitor.queryPackets(0);
                    this.waitingResponse = true;
                    while (this.waitingResponse) {
                        LOGD("requestTcpRxPacketsCounter, start wait for notify");
                        this.mQueryPktLock.wait();
                    }
                    if (this.mTcpRxCounter >= 0) {
                        rxCounter = this.mTcpRxCounter;
                    }
                    LOGD("tcp rx packets counter received rx = " + this.mTcpRxCounter);
                }
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
}
