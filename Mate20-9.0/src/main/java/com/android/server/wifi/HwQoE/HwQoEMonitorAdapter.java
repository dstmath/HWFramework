package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.android.server.wifi.WifiStateMachine;

public class HwQoEMonitorAdapter implements IHwQoEMonitorCallback {
    private static final String VOWIFI_NETWORK_ACTION = "android.net.wifi.action.VOWIFI_NETWORK_STATE_CHANGED";
    /* access modifiers changed from: private */
    public int NETWORK_HAVE_INTERNET = 1;
    /* access modifiers changed from: private */
    public int NETWORK_NO_INTERNET = 0;
    /* access modifiers changed from: private */
    public boolean isHaveInternet = false;
    /* access modifiers changed from: private */
    public boolean isInitCheck = false;
    private boolean isStartMonitor = false;
    /* access modifiers changed from: private */
    public boolean isWaittingCheckResult = false;
    public IHwQoECallback mCallback;
    /* access modifiers changed from: private */
    public HwQoENetWorkInfo mCheckInfo = null;
    /* access modifiers changed from: private */
    public int mCheckResult = 0;
    private int mCheckTimeOut = 4000;
    private HwQoEMonitorConfig mConifg;
    private Context mContext;
    /* access modifiers changed from: private */
    public HwQoEJNIAdapter mHwQoEJNIAdapter;
    /* access modifiers changed from: private */
    public HwQoENetWorkInfo mHwQoENetWorkInfo = null;
    /* access modifiers changed from: private */
    public HwQoENetWorkMonitor mHwQoENetWorkMonitor;
    private HwQoEUdpServiceImpl mHwQoEUdpServiceImpl;
    private Handler mLocalHandler;
    private int mNetworkDisableCount = 0;
    private int mVoWIFIStatus = 0;

    public HwQoEMonitorAdapter(Context context, WifiStateMachine wifiStateMachine, HwQoEMonitorConfig conifg, IHwQoECallback callback) {
        this.mContext = context;
        this.mConifg = conifg;
        this.mCallback = callback;
        initQoEAdapter();
        this.mHwQoENetWorkMonitor = new HwQoENetWorkMonitor(context, wifiStateMachine, 4, this);
        this.mHwQoEUdpServiceImpl = new HwQoEUdpServiceImpl(context, this);
        this.mHwQoEJNIAdapter = HwQoEJNIAdapter.getInstance();
    }

    private void initQoEAdapter() {
        HandlerThread handlerThread = new HandlerThread("HwQoEAdapter monior Thread");
        handlerThread.start();
        this.mLocalHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 112) {
                    HwQoEUtils.logD("QOE_MSG_WIFI_CHECK_TIMEOUT isWaittingCheckResult = " + HwQoEMonitorAdapter.this.isWaittingCheckResult + " isInitCheck = " + HwQoEMonitorAdapter.this.isInitCheck);
                    if (HwQoEMonitorAdapter.this.isWaittingCheckResult) {
                        boolean unused = HwQoEMonitorAdapter.this.isWaittingCheckResult = false;
                        HwQoENetWorkInfo result = HwQoEMonitorAdapter.this.mHwQoEJNIAdapter.queryPeriodData();
                        result.mDnsFailCount = HwQoEMonitorAdapter.this.mHwQoENetWorkMonitor.getDnsFaileCount();
                        if (HwQoEMonitorAdapter.this.isInitCheck) {
                            boolean unused2 = HwQoEMonitorAdapter.this.isInitCheck = false;
                            HwQoEMonitorAdapter.this.initCheckResult(HwQoEMonitorAdapter.this.mCheckInfo, result);
                        } else {
                            HwQoEMonitorAdapter.this.processCheckResult(HwQoEMonitorAdapter.this.mCheckInfo, result);
                        }
                        HwQoENetWorkInfo unused3 = HwQoEMonitorAdapter.this.mCheckInfo = null;
                    }
                } else if (i != 118) {
                    switch (i) {
                        case 101:
                            HwQoEUtils.logD("QOE_MSG_UPDATE_TCP_INFO isWaittingCheckResult = " + HwQoEMonitorAdapter.this.isWaittingCheckResult);
                            if (!HwQoEMonitorAdapter.this.isWaittingCheckResult) {
                                HwQoEMonitorAdapter.this.detectNetworkQuality(HwQoEMonitorAdapter.this.mHwQoENetWorkInfo, false);
                                break;
                            }
                            break;
                        case 102:
                            HwQoEUtils.logD("QOE_MSG_MONITOR_UPDATE_UDP_INFO isWaittingCheckResult = " + HwQoEMonitorAdapter.this.isWaittingCheckResult);
                            if (!HwQoEMonitorAdapter.this.isWaittingCheckResult && HwQoEMonitorAdapter.this.isHaveInternet) {
                                HwQoEMonitorAdapter.this.detectNetworkQuality(null, true);
                                break;
                            }
                        case 103:
                            HwQoEUtils.logD("QOE_MSG_HAVE_INTERNET");
                            if (HwQoEMonitorAdapter.this.isWaittingCheckResult) {
                                int unused4 = HwQoEMonitorAdapter.this.mCheckResult = HwQoEMonitorAdapter.this.NETWORK_HAVE_INTERNET;
                                break;
                            }
                            break;
                        case 104:
                            HwQoEUtils.logD("QOE_MSG_NO_INTERNET");
                            if (HwQoEMonitorAdapter.this.isWaittingCheckResult) {
                                int unused5 = HwQoEMonitorAdapter.this.mCheckResult = HwQoEMonitorAdapter.this.NETWORK_NO_INTERNET;
                                break;
                            }
                            break;
                    }
                } else {
                    HwQoEUtils.logD("QOE_MSG_MONITOR_START");
                    HwQoEMonitorAdapter.this.startMonitorMessage();
                }
                super.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    public void processCheckResult(HwQoENetWorkInfo firstInfo, HwQoENetWorkInfo secInfo) {
        HwQoENetWorkInfo info = new HwQoENetWorkInfo();
        info.mTcpTxPacket = secInfo.mTcpTxPacket - firstInfo.mTcpTxPacket;
        info.mTcpRxPacket = secInfo.mTcpRxPacket - firstInfo.mTcpRxPacket;
        info.mDnsFailCount = secInfo.mDnsFailCount - firstInfo.mDnsFailCount;
        HwQoEUtils.logD("processCheckResult mTcpTxPacket = " + info.mTcpTxPacket + " info.mTcpRxPacket = " + info.mTcpRxPacket + " info.mDnsFailCount = " + info.mDnsFailCount);
        if (this.isHaveInternet) {
            if (!detectTcpNetworkAvailable(info) && this.mCheckResult == this.NETWORK_NO_INTERNET) {
                try {
                    HwQoEUtils.logD("processCheckResult callback have no internet");
                    this.mCallback.onNetworkStateChange(this.NETWORK_NO_INTERNET);
                } catch (RemoteException e) {
                    HwQoEUtils.logE("processCheckResult error " + e.toString());
                }
                sendNetworkBroadcast(this.NETWORK_NO_INTERNET);
                this.isHaveInternet = false;
            }
        } else if (detectTcpNetworkAvailable(info) && this.mCheckResult == this.NETWORK_HAVE_INTERNET) {
            try {
                HwQoEUtils.logD("processCheckResult callback have internet");
                this.mCallback.onNetworkStateChange(this.NETWORK_HAVE_INTERNET);
            } catch (RemoteException e2) {
                HwQoEUtils.logE("processCheckResult error " + e2.toString());
            }
            sendNetworkBroadcast(this.NETWORK_HAVE_INTERNET);
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
        HwQoEUtils.logD("haveInternetProcess isInternet = " + isInternet + " noUdpAccess = " + noUdpAccess + " mNetworkDisableCount = " + this.mNetworkDisableCount);
        if (this.mVoWIFIStatus == 1) {
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

    private void haveNoInternetProcess(HwQoENetWorkInfo info, boolean isUDPAvailable) {
        boolean isInternet;
        if (info == null) {
            isInternet = !isUDPAvailable;
        } else if (info.mTcpRxPacket > 0) {
            isInternet = true;
        } else {
            isInternet = false;
        }
        HwQoEUtils.logD("haveNoInternetProcess isUDPAvailable = " + isUDPAvailable + " isInternet = " + isInternet);
        if (isInternet) {
            startNetworkChecking();
        }
    }

    /* access modifiers changed from: private */
    public void detectNetworkQuality(HwQoENetWorkInfo info, boolean noUdpAccess) {
        HwQoEUtils.logD("detectNetworkQuality noUdpAccess = " + noUdpAccess);
        if (this.isHaveInternet) {
            haveInternetProcess(info, noUdpAccess);
        } else {
            haveNoInternetProcess(info, noUdpAccess);
        }
        HwQoEUtils.logE("***************************period end ***************************");
    }

    private boolean detectTcpNetworkAvailable(HwQoENetWorkInfo info) {
        HwQoEUtils.logE("detectTcpNetworkAvailable info.mPeriodTcpRxPacket = " + info.mTcpRxPacket + " info.mPeriodTcpTxPacket = " + info.mTcpTxPacket + " info.mPeriodDnsFailCount = " + info.mDnsFailCount);
        if (0 != info.mTcpRxPacket || (info.mTcpTxPacket <= 4 && info.mDnsFailCount <= 0 && (this.mConifg.mPeriod > 2 || info.mTcpTxPacket <= 0))) {
            return true;
        }
        return false;
    }

    public void onNetworkInfoUpdate(HwQoENetWorkInfo info) {
        HwQoEUtils.logE("onNetworkInfoUpdate");
        if (info != null) {
            this.mHwQoENetWorkInfo = info;
            this.mLocalHandler.sendEmptyMessage(101);
        }
    }

    public void onUDPInternetAccessStatusChange(boolean noUdpAccess) {
        if (noUdpAccess) {
            HwQoEUtils.logD("onUDPInternetAccessStatusChange, noUdpAccess:" + noUdpAccess);
            this.mLocalHandler.sendEmptyMessage(102);
        } else if (this.isWaittingCheckResult) {
            HwQoEUtils.logE("onUDPInternetAccessStatusChange have internet now");
            this.isWaittingCheckResult = false;
            this.mLocalHandler.removeMessages(4000);
        }
    }

    public void startMonitor() {
        this.mLocalHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_MONITOR_START);
    }

    /* access modifiers changed from: private */
    public void startMonitorMessage() {
        this.mHwQoENetWorkMonitor.startMonitor();
        if (this.mVoWIFIStatus == 1) {
            this.mHwQoEUdpServiceImpl.setUDPInternetAccessMonitorEnabled(true);
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
        this.mHwQoENetWorkMonitor.stopMonitor();
        this.mHwQoEUdpServiceImpl.setUDPInternetAccessMonitorEnabled(false);
    }

    public void updateVOWIFIState(int state) {
        this.mVoWIFIStatus = state;
        if (this.mVoWIFIStatus == 1) {
            HwQoEUtils.logE("updateVOWIFIState use new mPeriod = " + this.mConifg.mPeriod);
            if (this.mConifg.mPeriod == 2) {
                this.mCheckTimeOut = 1000;
                this.mHwQoEUdpServiceImpl.setInterval(2000);
            } else if (this.mConifg.mPeriod == 1) {
                this.mCheckTimeOut = 2000;
                this.mHwQoEUdpServiceImpl.setInterval(1000);
            } else if (this.mConifg.mPeriod == 8) {
                this.mCheckTimeOut = 8000;
                this.mHwQoEUdpServiceImpl.setInterval(2000);
            } else {
                this.mCheckTimeOut = 4000;
                this.mHwQoEUdpServiceImpl.setInterval(2000);
            }
            this.mHwQoEUdpServiceImpl.setUDPInternetAccessMonitorEnabled(true);
        } else {
            this.mCheckTimeOut = 4000;
            this.mHwQoEUdpServiceImpl.setUDPInternetAccessMonitorEnabled(false);
        }
        HwQoEUtils.logD("updateVOWIFIState mVoWIFIStatus:" + this.mVoWIFIStatus + " mCheckTimeOut:" + this.mCheckTimeOut + " mPeriod:" + this.mConifg.mPeriod);
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
        this.mCheckInfo.mDnsFailCount = this.mHwQoENetWorkMonitor.getDnsFaileCount();
        this.mCheckResult = 0;
        new HwQoENetworkChecker(this.mContext, this.mLocalHandler).start();
        this.mLocalHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_CHECK_TIMEOUT, (long) this.mCheckTimeOut);
    }

    /* access modifiers changed from: private */
    public void initCheckResult(HwQoENetWorkInfo firstInfo, HwQoENetWorkInfo secInfo) {
        HwQoENetWorkInfo info = new HwQoENetWorkInfo();
        info.mTcpTxPacket = secInfo.mTcpTxPacket - firstInfo.mTcpTxPacket;
        info.mTcpRxPacket = secInfo.mTcpRxPacket - firstInfo.mTcpRxPacket;
        info.mDnsFailCount = secInfo.mDnsFailCount - firstInfo.mDnsFailCount;
        HwQoEUtils.logD("initCheckResult mTcpTxPacket = " + info.mTcpTxPacket + " info.mTcpRxPacket = " + info.mTcpRxPacket + " info.mDnsFailCount = " + info.mDnsFailCount);
        if (!detectTcpNetworkAvailable(info) || this.mCheckResult != this.NETWORK_HAVE_INTERNET) {
            this.isHaveInternet = false;
            return;
        }
        try {
            HwQoEUtils.logD("initCheckResult callback have internet");
            this.mCallback.onNetworkStateChange(this.NETWORK_HAVE_INTERNET);
        } catch (RemoteException e) {
            HwQoEUtils.logE("initCheckResult error " + e.toString());
        }
        sendNetworkBroadcast(this.NETWORK_HAVE_INTERNET);
        this.isHaveInternet = true;
    }

    public void release() {
        if (this.mLocalHandler != null) {
            Looper looper = this.mLocalHandler.getLooper();
            if (!(looper == null || looper == Looper.getMainLooper())) {
                looper.quitSafely();
                HwQoEUtils.logD("HwQoEMonitorAdapter$HandlerThread::Release");
            }
        }
        this.mHwQoENetWorkMonitor.release();
        this.mHwQoEUdpServiceImpl.release();
    }
}
