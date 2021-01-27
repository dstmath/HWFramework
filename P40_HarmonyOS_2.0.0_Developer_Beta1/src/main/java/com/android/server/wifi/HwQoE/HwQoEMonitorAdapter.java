package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.server.wifi.ClientModeImpl;

public class HwQoEMonitorAdapter implements IHwQoEMonitorCallback {
    private static final int CHECK_INTERVAL_FOUR = 4;
    private static final int CHECK_INTERVAL_ONE = 1;
    private static final int CHECK_INTERVAL_TWO = 2;
    private static final int DISABLE_COUNT_DEFAULTE = 1;
    private static final boolean IS_IGNORE_REPORT_FIRST_NO_INTERNET = SystemProperties.getBoolean("ro.config.vowifi_ignore_first_no_internet_report", false);
    private static final int MILLISECOND_PER_SECOND = 1000;
    private static final int NETWORK_HAVE_INTERNET = 1;
    private static final int NETWORK_NO_INTERNET = 0;
    private static final String VOWIFI_NETWORK_ACTION = "android.net.wifi.action.VOWIFI_NETWORK_STATE_CHANGED";
    private boolean isHaveInternet = false;
    private boolean isInitCheck = false;
    private boolean isStartMonitor = false;
    private boolean isWaittingCheckResult = false;
    public IHwQoECallback mCallback;
    private HwQoENetWorkInfo mCheckInfo = null;
    private int mCheckResult = 0;
    private int mCheckTimeOut = HwQoEUtils.WIFI_CHECK_TIMEOUT;
    private HwQoEMonitorConfig mConifg;
    private Context mContext;
    private HwQoEJNIAdapter mHwQoEJNIAdapter;
    private HwQoENetWorkInfo mHwQoENetWorkInfo = null;
    private HwQoEUdpServiceImpl mHwQoEUdpServiceImpl;
    private Handler mLocalHandler;
    private int mNetworkDisableCount = 0;
    private int mVoWiFiStatus = 0;

    public HwQoEMonitorAdapter(Context context, ClientModeImpl wifiStateMachine, HwQoEMonitorConfig conifg, IHwQoECallback callback) {
        this.mContext = context;
        this.mConifg = conifg;
        this.mCallback = callback;
        this.mHwQoEUdpServiceImpl = new HwQoEUdpServiceImpl(context, this);
        this.mHwQoEJNIAdapter = HwQoEJNIAdapter.getInstance();
        initQoEAdapter();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiCheckTimeout() {
        HwQoEUtils.logD(false, "isWaittingCheckResult= %{public}s isInitCheck = %{public}s", String.valueOf(this.isWaittingCheckResult), String.valueOf(this.isInitCheck));
        if (this.isWaittingCheckResult) {
            this.isWaittingCheckResult = false;
            HwQoENetWorkInfo result = this.mHwQoEJNIAdapter.queryPeriodData();
            if (this.isInitCheck) {
                this.isInitCheck = false;
                initCheckResult(this.mCheckInfo, result);
            } else {
                processCheckResult(this.mCheckInfo, result);
            }
            this.mCheckInfo = null;
        }
    }

    private void initQoEAdapter() {
        HandlerThread handlerThread = new HandlerThread("HwQoEAdapter monior Thread");
        handlerThread.start();
        this.mLocalHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.HwQoE.HwQoEMonitorAdapter.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 112) {
                    HwQoEMonitorAdapter.this.handleWifiCheckTimeout();
                } else if (i != 118) {
                    switch (i) {
                        case 101:
                            HwQoEUtils.logD(false, "QOE_MSG_UPDATE_TCP_INFO isWaittingCheckResult = %{public}s", String.valueOf(HwQoEMonitorAdapter.this.isWaittingCheckResult));
                            if (!HwQoEMonitorAdapter.this.isWaittingCheckResult) {
                                HwQoEMonitorAdapter hwQoEMonitorAdapter = HwQoEMonitorAdapter.this;
                                hwQoEMonitorAdapter.detectNetworkQuality(hwQoEMonitorAdapter.mHwQoENetWorkInfo, false);
                                break;
                            }
                            break;
                        case 102:
                            HwQoEUtils.logD(false, "QOE_MSG_MONITOR_UPDATE_UDP_INFO isWaittingCheckResult = %{public}s", String.valueOf(HwQoEMonitorAdapter.this.isWaittingCheckResult));
                            if (!HwQoEMonitorAdapter.this.isWaittingCheckResult && HwQoEMonitorAdapter.this.isHaveInternet) {
                                HwQoEMonitorAdapter.this.detectNetworkQuality(null, true);
                                break;
                            }
                        case 103:
                            HwQoEUtils.logD(false, "QOE_MSG_HAVE_INTERNET", new Object[0]);
                            if (HwQoEMonitorAdapter.this.isWaittingCheckResult) {
                                removeMessages(HwQoEUtils.WIFI_CHECK_TIMEOUT);
                                HwQoEMonitorAdapter.this.mCheckResult = 1;
                                break;
                            }
                            break;
                        case HwQoEUtils.QOE_MSG_MONITOR_NO_INTERNET /* 104 */:
                            HwQoEUtils.logD(false, "QOE_MSG_NO_INTERNET", new Object[0]);
                            if (HwQoEMonitorAdapter.this.isWaittingCheckResult) {
                                removeMessages(HwQoEUtils.WIFI_CHECK_TIMEOUT);
                                HwQoEMonitorAdapter.this.mCheckResult = 0;
                                break;
                            }
                            break;
                    }
                } else {
                    HwQoEUtils.logD(false, "QOE_MSG_MONITOR_START", new Object[0]);
                    HwQoEMonitorAdapter.this.startMonitorMessage();
                }
                super.handleMessage(msg);
            }
        };
    }

    private void processCheckResult(HwQoENetWorkInfo firstInfo, HwQoENetWorkInfo secInfo) {
        HwQoENetWorkInfo info = new HwQoENetWorkInfo();
        info.mTcpTxPacket = secInfo.mTcpTxPacket - firstInfo.mTcpTxPacket;
        info.mTcpRxPacket = secInfo.mTcpRxPacket - firstInfo.mTcpRxPacket;
        info.mDnsFailCount = secInfo.mDnsFailCount - firstInfo.mDnsFailCount;
        HwQoEUtils.logD(false, "processCheckResult mTcpTxPacket = %{public}s info.mTcpRxPacket = %{public}s info.mDnsFailCount = %{public}d", String.valueOf(info.mTcpTxPacket), String.valueOf(info.mTcpRxPacket), Integer.valueOf(info.mDnsFailCount));
        if (this.isHaveInternet) {
            if (!detectTcpNetworkAvailable(info) && this.mCheckResult == 0) {
                try {
                    HwQoEUtils.logD(false, "processCheckResult callback have no internet", new Object[0]);
                    this.mCallback.onNetworkStateChange(0);
                } catch (RemoteException e) {
                    HwQoEUtils.logE(false, "processCheckResult error %{public}s", e.getMessage());
                }
                sendNetworkBroadcast(0);
                this.isHaveInternet = false;
            }
        } else if (detectTcpNetworkAvailable(info) && this.mCheckResult == 1) {
            try {
                HwQoEUtils.logD(false, "processCheckResult callback have internet", new Object[0]);
                this.mCallback.onNetworkStateChange(1);
            } catch (RemoteException e2) {
                HwQoEUtils.logE(false, "processCheckResult error %{public}s", e2.getMessage());
            }
            sendNetworkBroadcast(1);
            this.isHaveInternet = true;
        }
    }

    private void haveInternetProcess(HwQoENetWorkInfo info, boolean noUdpAccess) {
        boolean isInternet;
        int disableCount;
        if (info == null) {
            isInternet = !noUdpAccess;
        } else if (detectTcpNetworkAvailable(info)) {
            isInternet = true;
        } else {
            isInternet = false;
        }
        HwQoEUtils.logD(false, "haveInternetProcess isInternet = %{public}s noUdpAccess = %{public}s mNetworkDisableCount = %{public}d", String.valueOf(isInternet), String.valueOf(noUdpAccess), Integer.valueOf(this.mNetworkDisableCount));
        if (this.mVoWiFiStatus == 1) {
            disableCount = 1;
        } else {
            disableCount = 2;
        }
        if (!isInternet) {
            this.mNetworkDisableCount++;
            if (this.mNetworkDisableCount >= disableCount) {
                startNetworkChecking();
                return;
            }
            return;
        }
        this.mNetworkDisableCount = 0;
    }

    private void haveNoInternetProcess(HwQoENetWorkInfo info, boolean isUdpAvailable) {
        boolean isInternet;
        if (info == null) {
            isInternet = !isUdpAvailable;
        } else if (info.mTcpRxPacket > 0) {
            isInternet = true;
        } else {
            isInternet = false;
        }
        HwQoEUtils.logD(false, "haveNoInternetProcess isUdpAvailable = %{public}s isInternet = %{public}s", String.valueOf(isUdpAvailable), String.valueOf(isInternet));
        if (isInternet) {
            startNetworkChecking();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void detectNetworkQuality(HwQoENetWorkInfo info, boolean noUdpAccess) {
        HwQoEUtils.logD(false, "detectNetworkQuality noUdpAccess = %{public}s", Boolean.valueOf(noUdpAccess));
        if (this.isHaveInternet) {
            haveInternetProcess(info, noUdpAccess);
        } else {
            haveNoInternetProcess(info, noUdpAccess);
        }
        HwQoEUtils.logD(false, "************************** period end **************************", new Object[0]);
    }

    private boolean detectTcpNetworkAvailable(HwQoENetWorkInfo info) {
        HwQoEUtils.logE(false, "detectTcpNetworkAvailable info.mPeriodTcpRxPacket = %{public}s info.mPeriodTcpTxPacket = %{public}s info.mPeriodDnsFailCount = %{public}d", String.valueOf(info.mTcpRxPacket), String.valueOf(info.mTcpTxPacket), Integer.valueOf(info.mDnsFailCount));
        if (info.mTcpRxPacket == 0) {
            if (info.mTcpTxPacket > 4 || info.mDnsFailCount > 0) {
                return false;
            }
            if (this.mConifg.mPeriod <= 2 && info.mTcpTxPacket > 0) {
                return false;
            }
        }
        return true;
    }

    @Override // com.android.server.wifi.HwQoE.IHwQoEMonitorCallback
    public void onNetworkInfoUpdate(HwQoENetWorkInfo info) {
        HwQoEUtils.logE(false, "onNetworkInfoUpdate", new Object[0]);
        if (info != null) {
            this.mHwQoENetWorkInfo = info;
            this.mLocalHandler.sendEmptyMessage(101);
        }
    }

    @Override // com.android.server.wifi.HwQoE.IHwQoEMonitorCallback
    public void onUDPInternetAccessStatusChange(boolean noUdpAccess) {
        HwQoEUtils.logD(false, "onUDPInternetAccessStatusChange, noUdpAccess:%{public}s", String.valueOf(noUdpAccess));
        if (noUdpAccess) {
            this.mLocalHandler.sendEmptyMessage(102);
        }
    }

    public void startMonitor() {
        this.mLocalHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_MONITOR_START);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startMonitorMessage() {
        if (this.mVoWiFiStatus == 1) {
            this.mHwQoEUdpServiceImpl.setUdpInternetAccessMonitorEnabled(true);
        }
        if (!this.isStartMonitor) {
            this.isStartMonitor = true;
            this.isInitCheck = true;
            startNetworkChecking();
        }
    }

    public void stopMonitor() {
        this.isStartMonitor = false;
        this.isHaveInternet = false;
        this.mHwQoEUdpServiceImpl.setUdpInternetAccessMonitorEnabled(false);
    }

    public void updateVoWiFiState(int state) {
        this.mVoWiFiStatus = state;
        if (this.mVoWiFiStatus == 1) {
            if (this.mConifg.mPeriod == 2) {
                this.mCheckTimeOut = 1000;
                this.mHwQoEUdpServiceImpl.setInterval(2000);
            } else if (this.mConifg.mPeriod == 1) {
                this.mCheckTimeOut = 2000;
                this.mHwQoEUdpServiceImpl.setInterval(1000);
            } else {
                this.mCheckTimeOut = HwQoEUtils.WIFI_CHECK_TIMEOUT;
                this.mHwQoEUdpServiceImpl.setInterval(2000);
            }
            this.mHwQoEUdpServiceImpl.setUdpInternetAccessMonitorEnabled(true);
        } else {
            this.mHwQoEUdpServiceImpl.setUdpInternetAccessMonitorEnabled(false);
        }
        HwQoEUtils.logD(false, "updateVoWiFiState mVoWiFiStatus:%{public}d mCheckTimeOut:%{public}d mPeriod:%{public}d", Integer.valueOf(this.mVoWiFiStatus), Integer.valueOf(this.mCheckTimeOut), Integer.valueOf(this.mConifg.mPeriod));
    }

    public void updateCallback(IHwQoECallback callback) {
        if (callback != null) {
            this.mCallback = callback;
        }
    }

    private void sendNetworkBroadcast(int state) {
        Intent intent = new Intent(VOWIFI_NETWORK_ACTION);
        intent.putExtra("state", state);
        this.mContext.sendBroadcast(intent, "android.permission.ACCESS_WIFI_STATE");
    }

    private void startNetworkChecking() {
        this.isWaittingCheckResult = true;
        this.mCheckInfo = this.mHwQoEJNIAdapter.queryPeriodData();
        this.mCheckResult = 0;
        new HwQoENetworkChecker(this.mContext, this.mLocalHandler).start();
        this.mLocalHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_CHECK_TIMEOUT, (long) this.mCheckTimeOut);
    }

    private void initCheckResult(HwQoENetWorkInfo firstInfo, HwQoENetWorkInfo secInfo) {
        HwQoENetWorkInfo info = new HwQoENetWorkInfo();
        info.mTcpTxPacket = secInfo.mTcpTxPacket - firstInfo.mTcpTxPacket;
        info.mTcpRxPacket = secInfo.mTcpRxPacket - firstInfo.mTcpRxPacket;
        info.mDnsFailCount = secInfo.mDnsFailCount - firstInfo.mDnsFailCount;
        HwQoEUtils.logD(false, "initCheckResult mTcpTxPacket = %{public}s info.mTcpRxPacket = %{public}s info.mDnsFailCount = %{public}d", String.valueOf(info.mTcpTxPacket), String.valueOf(info.mTcpRxPacket), Integer.valueOf(info.mDnsFailCount));
        if (!detectTcpNetworkAvailable(info) || this.mCheckResult != 1) {
            this.isHaveInternet = false;
            if (!IS_IGNORE_REPORT_FIRST_NO_INTERNET) {
                try {
                    HwQoEUtils.logD(false, "initCheckResult callback have no internet", new Object[0]);
                    this.mCallback.onNetworkStateChange(0);
                } catch (RemoteException e) {
                    HwQoEUtils.logE(false, "initCheckResult error!", new Object[0]);
                }
                sendNetworkBroadcast(0);
                return;
            }
            return;
        }
        try {
            HwQoEUtils.logD(false, "initCheckResult callback have internet", new Object[0]);
            this.mCallback.onNetworkStateChange(1);
        } catch (RemoteException e2) {
            HwQoEUtils.logE(false, "initCheckResult error %{public}s", e2.getMessage());
        }
        sendNetworkBroadcast(1);
        this.isHaveInternet = true;
    }

    public void release() {
        Looper looper;
        Handler handler = this.mLocalHandler;
        if (!(handler == null || (looper = handler.getLooper()) == null || looper == Looper.getMainLooper())) {
            looper.quitSafely();
            HwQoEUtils.logD(false, "HwQoEMonitorAdapter$HandlerThread::Release", new Object[0]);
        }
        this.mHwQoEUdpServiceImpl.release();
    }
}
