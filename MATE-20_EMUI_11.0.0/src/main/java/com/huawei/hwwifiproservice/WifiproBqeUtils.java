package com.huawei.hwwifiproservice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class WifiproBqeUtils {
    public static final int BQE_BAD_LEVEL = 1;
    private static final String BQE_BIND_WLAN_FAIL = "com.huawei.wifiprobqeservice.BQE_BIND_WLAN_FAIL";
    private static final int BQE_CHECK_STOP_DELAY = 1000;
    private static final String BQE_CLIENT_PERMISSION = "com.huawei.permission.WIFIPRO_BQE_CLIENT_RECEIVE";
    private static final int BQE_FINISHED_IN_FOUR_SEC = 4000;
    private static final int BQE_FINISHED_IN_SEVEN_SEC = 7000;
    public static final int BQE_GOOD_LEVEL = 3;
    public static final int BQE_GOOD_RTT = 1200;
    public static final int BQE_GOOD_SPEED = 358400;
    private static final String BQE_LOG_TAG = "bqe_client";
    public static final int BQE_MIN_PKTS = 5;
    public static final int BQE_NOT_GOOD_LEVEL = 2;
    public static final int BQE_NOT_GOOD_RTT = 4800;
    public static final int BQE_NOT_GOOD_SPEED = 20480;
    private static final String BQE_NULL_URL = "NULL URL";
    private static final String BQE_RECEIVE_REQUEST = "com.huawei.wifiprobqeservice.BQE_RECEIVE_REQUEST";
    private static final int BQE_REQUEST_DELAY = 2000;
    private static final int BQE_REQUEST_EXPIRE = 7000;
    private static final String BQE_REQUEST_FINISHED = "com.huawei.wifiprobqeservice.BQE_FINISHED";
    private static final String BQE_REQUEST_ONCE = "com.huawei.wifiprobqeservice.START_BQE_ONCE";
    private static final String BQE_REQUEST_URL = "REQUEST_URL";
    private static final String BQE_RESULT_BUNDLE = "BQE_RESULT";
    private static final int BQE_RESULT_FAIL = -1;
    private static final int BQE_RESULT_SUCCESS = 1;
    private static final String BQE_SERVER_PERMISSION = "com.huawei.permission.WIFIPRO_BQE_SERVER_RECEIVE";
    private static final String BQE_SERVICE_EXITED = "com.huawei.wifiprobqeservice.BQE_SERVICE_EXITED";
    private static final String BQE_SERVICE_STARTED = "com.huawei.wifiprobqeservice.BQE_SERVICE_STARTED";
    private static final String BQE_SPEED_BUNDLE = "BQE_SERVICE_SPEED";
    private static final String BQE_STOP_REQUEST = "com.huawei.wifiprobqeservice.STOP_BQE";
    public static final int BQE_UNKNOWN_LEVEL = 0;
    private static final int BQE_UNVALID_TCP_TIME = 5;
    private static final String BUNDLE_URL_FAILED = "BUNDLE_URL_FAILED";
    private static final long BYTE_TO_KB = 1024;
    private static final int CHECK_SPEED_INTERVAL = 1000;
    private static final String DEFAULT_WLAN_IFACE = "wlan0";
    public static final boolean DISABLE_BQE_SERVICE = false;
    private static final int MSG_CHECK_SPEED = 1;
    private static final int MSG_CHECK_START_REQUEST_RECEIVE = 3;
    private static final int MSG_CHECK_STOP_REQUEST_RECEIVE = 5;
    private static final int MSG_REQUEST_EXPIRE = 4;
    private static final int MSG_STOP_BQE_REQUEST = 2;
    private static final int NO_URL_FAILED = -1;
    private static final String REQUEST_INFO = "Request_Info";
    private static final int SECOND_TO_MS = 1000;
    private static final String SERVICE_ACTION = "com.huawei.wifiprobqeservice.BQE_SERVICE";
    private static final String SERVICE_PKG = "com.huawei.wifiprobqeservice";
    private static final int TCP_RTT_INDEX = 0;
    private static final int TCP_RTT_PKTS_INDEX = 1;
    private static final int TCP_RTT_WHEN_INDEX = 2;
    private static final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
    private static WifiproBqeUtils sWifiproBqeUtils = null;
    public boolean isNeedRecheckByWebView = false;
    public boolean isUseOverseaServer = false;
    private long mBestSpeed;
    private List<Handler> mBqeHandlerList = new ArrayList();
    private List<Messenger> mBqeMessengerist = new ArrayList();
    private Handler mBqeMsgHandler;
    private long mBqeStartedTime;
    private ContentResolver mContentResolver;
    private Context mContext;
    private boolean mIsBindWlanFailed = false;
    private boolean mIsOversea = false;
    private boolean mIsReceiverRegistered = false;
    private boolean mIsSendRequest = false;
    private boolean mIsServiceStarted = false;
    private boolean mIsSetExpire = false;
    private boolean mIsStartRequestReceived = false;
    private boolean mIsStopRequestReceived = false;
    private boolean mIsSuspendRequest = false;
    private long mLastSecondLoadRxByte;
    private long mLastSecondLoadTime;
    private final Object mLock = new Object();
    private boolean mOverseaChecked = false;
    private WifiproBqeClientReceiver mReceiver;
    private Intent mServceIntent;
    private int mSuspendRequestInterval = 0;
    private List<Handler> mWebviewHandlerList = new ArrayList();
    private WifiProStatisticsManager mWifiProStatisticsManager;

    private WifiproBqeUtils(Context context) {
        this.mContext = context;
        this.isNeedRecheckByWebView = false;
        this.mServceIntent = new Intent(SERVICE_ACTION).setPackage(SERVICE_PKG);
        initBqeMsgQueue();
        this.mContentResolver = this.mContext.getContentResolver();
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
    }

    public static synchronized WifiproBqeUtils getInstance(Context context) {
        WifiproBqeUtils wifiproBqeUtils;
        synchronized (WifiproBqeUtils.class) {
            if (sWifiproBqeUtils == null) {
                sWifiproBqeUtils = new WifiproBqeUtils(context);
            }
            wifiproBqeUtils = sWifiproBqeUtils;
        }
        return wifiproBqeUtils;
    }

    public void startBqeService() {
        Log.d(BQE_LOG_TAG, "startBqeService, receiverRegistered = " + this.mIsReceiverRegistered);
        if (!this.mIsReceiverRegistered) {
            try {
                this.mContext.startService(this.mServceIntent);
            } catch (SecurityException e) {
                Log.e(BQE_LOG_TAG, "startBqeService() got SecurityException!");
            } catch (IllegalStateException e2) {
                Log.e(BQE_LOG_TAG, "startBqeService() got IllegalStateException!");
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(BQE_SERVICE_STARTED);
            filter.addAction(BQE_RECEIVE_REQUEST);
            filter.addAction(BQE_REQUEST_FINISHED);
            filter.addAction(BQE_SERVICE_EXITED);
            filter.addAction(BQE_BIND_WLAN_FAIL);
            filter.addAction("com.huawei.wifipro.action.ACTION_RESP_WEBVIEW_CHECK_FROM_SERVICE");
            this.mReceiver = new WifiproBqeClientReceiver();
            this.mContext.registerReceiver(this.mReceiver, filter, BQE_CLIENT_PERMISSION, null);
            this.mIsReceiverRegistered = true;
        }
    }

    public void stopBqeService() {
        Log.d(BQE_LOG_TAG, "stopBqeService");
        Object obj = this.mContext.getSystemService("activity");
        ActivityManager am = null;
        if (obj instanceof ActivityManager) {
            am = (ActivityManager) obj;
        }
        if (am != null) {
            am.forceStopPackage(SERVICE_PKG);
            Log.d(BQE_LOG_TAG, "forcestopBqeService");
        } else {
            this.mContext.stopService(this.mServceIntent);
            Log.d(BQE_LOG_TAG, "stopBqeService or class is not match");
        }
        this.mIsServiceStarted = false;
        if (this.mIsReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mIsReceiverRegistered = false;
        }
        this.mOverseaChecked = false;
        resetBqeState();
        resetHandlerList();
    }

    public void resetBqeState() {
        Log.d(BQE_LOG_TAG, "resetBqeState");
        this.mBqeMsgHandler.removeMessages(1);
        this.mBqeMsgHandler.removeMessages(2);
        this.mBqeMsgHandler.removeMessages(3);
        this.mBqeMsgHandler.removeMessages(5);
        this.mBqeMsgHandler.removeMessages(4);
        this.mIsSuspendRequest = false;
        this.mIsSendRequest = false;
        this.mIsStartRequestReceived = false;
        this.mIsSetExpire = false;
        this.mIsStopRequestReceived = false;
        this.mIsBindWlanFailed = false;
        this.mBqeStartedTime = SystemClock.elapsedRealtime();
    }

    public void requestBqeOnce(int interval, Messenger messager) {
        if (messager != null) {
            synchronized (this.mLock) {
                if (this.mBqeMessengerist.size() == 0) {
                    requestBqeOnce(interval);
                }
                this.mBqeMessengerist.add(messager);
            }
        }
    }

    public void requestBqeOnce(int interval, Handler msgHandler) {
        if (msgHandler != null) {
            synchronized (this.mLock) {
                if (this.mBqeHandlerList.size() == 0) {
                    requestBqeOnce(interval);
                }
                this.mBqeHandlerList.add(msgHandler);
            }
        }
    }

    public boolean isBqeServicesStarted() {
        return this.mIsServiceStarted;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestBqeOnce(int interval) {
        Log.d(BQE_LOG_TAG, "requestBqeOnce");
        this.mSuspendRequestInterval = interval;
        this.mBqeMsgHandler.removeMessages(3);
        this.mBqeMsgHandler.removeMessages(2);
        this.mBqeMsgHandler.sendEmptyMessageDelayed(3, 2000);
        if (!this.mIsSetExpire) {
            this.mIsSetExpire = true;
            this.mBqeMsgHandler.sendEmptyMessageDelayed(4, 7000);
        }
        if (!this.mIsServiceStarted) {
            this.mIsSuspendRequest = true;
            Log.d(BQE_LOG_TAG, "service havn't started!");
            startBqeService();
            return;
        }
        resetTcpState();
        if (!this.mOverseaChecked) {
            String operator = null;
            Object obj = this.mContext.getSystemService("phone");
            TelephonyManager telManager = null;
            if (obj instanceof TelephonyManager) {
                telManager = (TelephonyManager) obj;
            }
            if (telManager != null) {
                operator = telManager.getNetworkOperator();
            } else {
                Log.e(BQE_LOG_TAG, "requestBqeOnce:class is not match");
            }
            if (operator == null || operator.length() == 0) {
                if (!"CN".equalsIgnoreCase(WifiProCommonUtils.getProductLocale())) {
                    this.mIsOversea = true;
                }
            } else if (!operator.startsWith("460")) {
                this.mIsOversea = true;
            }
            this.mOverseaChecked = true;
        }
        sendBqeRequestOnce(interval);
    }

    private void sendBqeRequestOnce(int interval) {
        boolean isFirstRequest;
        Intent intent = new Intent(BQE_REQUEST_ONCE);
        intent.setFlags(67108864);
        Bundle bundle = new Bundle();
        if (!this.mIsSendRequest || this.mIsBindWlanFailed) {
            isFirstRequest = true;
        } else {
            isFirstRequest = false;
        }
        bundle.putBooleanArray(REQUEST_INFO, new boolean[]{this.mIsOversea, isFirstRequest});
        intent.putExtras(bundle);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BQE_CLIENT_PERMISSION);
        this.mIsSendRequest = true;
        this.mBqeMsgHandler.removeMessages(1);
        this.mBqeMsgHandler.sendEmptyMessageDelayed(1, 1000);
        this.mBqeMsgHandler.sendEmptyMessageDelayed(2, (long) interval);
    }

    public void stopBqeRequest() {
        if (!this.mIsSendRequest) {
            Log.d(BQE_LOG_TAG, "no BQE request send");
            return;
        }
        Log.d(BQE_LOG_TAG, "stopBqeRequest");
        this.mIsSetExpire = false;
        this.mIsStopRequestReceived = false;
        this.mBqeMsgHandler.removeMessages(4);
        Intent intent = new Intent(BQE_STOP_REQUEST);
        intent.setFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BQE_CLIENT_PERMISSION);
        this.mBqeMsgHandler.sendEmptyMessageDelayed(5, 1000);
    }

    public void resetWlanRtt() {
        Bundle data = new Bundle();
        data.putInt("WIFIPRO_WLAN_BQE_RTT", 1);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 6, data);
    }

    private void resetTcpState() {
        Log.d(BQE_LOG_TAG, "resetTcpState");
        resetWlanRtt();
        this.mLastSecondLoadRxByte = TrafficStats.getRxBytes(WLAN_IFACE);
        this.mLastSecondLoadTime = SystemClock.elapsedRealtime();
        this.mBestSpeed = 0;
    }

    public void queryWlanRtt() {
        int[] result;
        Bundle data = new Bundle();
        data.putInt("WIFIPRO_WLAN_BQE_RTT", 1);
        Bundle resultRtt = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 7, data);
        if (resultRtt != null && (result = resultRtt.getIntArray("resultRtt")) != null) {
            setWlanRtt(result, result.length);
        }
    }

    private void computeQos(int tcpRtt, int tcpRttPkts, int tcpRttWhen) {
        int bqeLevel;
        this.mBqeMsgHandler.removeMessages(1);
        if ((tcpRttPkts <= 5 || tcpRtt == 0 || tcpRttWhen >= 5) && this.mBestSpeed < 20480) {
            bqeLevel = 1;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(3);
        } else {
            if (tcpRtt > 1200) {
                long j = this.mBestSpeed;
                if (j <= 358400) {
                    if (tcpRtt <= 4800 || j > 20480) {
                        bqeLevel = 2;
                        this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(2);
                    } else {
                        bqeLevel = 1;
                        this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(3);
                    }
                }
            }
            bqeLevel = 3;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(1);
        }
        Log.d(BQE_LOG_TAG, "bqeLevel " + bqeLevel + " rtt=" + tcpRtt + ",rtt_pkts=" + tcpRttPkts + " speed=" + this.mBestSpeed);
        notifyBQEResult(bqeLevel, tcpRtt, tcpRttPkts, this.mBestSpeed);
    }

    public void setWlanRtt(int[] ipqos, int len) {
        int tcpRttWhen = 0;
        int tcpRtt = len > 0 ? ipqos[0] : 0;
        int tcpRttPkts = len > 1 ? ipqos[1] : 0;
        if (len > 2) {
            tcpRttWhen = ipqos[2];
        }
        resetWlanRtt();
        computeQos(tcpRtt, tcpRttPkts, tcpRttWhen);
    }

    public void recheckNetworkTypeByWebView(Handler handler, boolean oversea) {
        synchronized (this.mLock) {
            if (this.mWebviewHandlerList.size() == 0) {
                this.isUseOverseaServer = oversea;
                recheckNetworkTypeByWebView(this.isUseOverseaServer);
            }
            this.mWebviewHandlerList.add(handler);
        }
    }

    private void recheckNetworkTypeByWebView(boolean oversea) {
        Log.d("WifiproBqeUtils", "recheckNetworkTypeByWebView:: oversea = " + oversea);
        if (!this.mIsServiceStarted) {
            this.isNeedRecheckByWebView = true;
            Log.d(BQE_LOG_TAG, "service havn't started!");
            startBqeService();
            return;
        }
        Intent intent = new Intent("com.huawei.wifipro.action.ACTION_REQUEST_WEBVIEW_CHECK_TO_SERVICE");
        intent.putExtra("wifipro_flag_oversea", oversea);
        intent.setFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void initBqeMsgQueue() {
        this.mBqeMsgHandler = new Handler() {
            /* class com.huawei.hwwifiproservice.WifiproBqeUtils.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i != 1) {
                    if (i == 2) {
                        WifiproBqeUtils.this.stopBqeRequest();
                    } else if (i != 3) {
                        if (i == 4) {
                            Log.i(WifiproBqeUtils.BQE_LOG_TAG, "request expire!");
                            WifiproBqeUtils.this.resetBqeState();
                            WifiproBqeUtils.this.notifyBQEResult(0, 0, 0, 0);
                        } else if (i == 5 && !WifiproBqeUtils.this.mIsStopRequestReceived) {
                            WifiproBqeUtils.this.stopBqeRequest();
                        }
                    } else if (!WifiproBqeUtils.this.mIsStartRequestReceived) {
                        WifiproBqeUtils wifiproBqeUtils = WifiproBqeUtils.this;
                        wifiproBqeUtils.requestBqeOnce(wifiproBqeUtils.mSuspendRequestInterval);
                    }
                } else if (WifiproBqeUtils.this.mIsSendRequest) {
                    long currentRxByte = TrafficStats.getRxBytes(WifiproBqeUtils.WLAN_IFACE);
                    long currentTime = SystemClock.elapsedRealtime();
                    long duration = currentTime - WifiproBqeUtils.this.mLastSecondLoadTime;
                    long speed = 0;
                    if (duration > 0) {
                        speed = ((currentRxByte - WifiproBqeUtils.this.mLastSecondLoadRxByte) * 1000) / duration;
                    }
                    if (speed > WifiproBqeUtils.this.mBestSpeed) {
                        WifiproBqeUtils.this.mBestSpeed = speed;
                    }
                    WifiproBqeUtils.this.mLastSecondLoadRxByte = currentRxByte;
                    WifiproBqeUtils.this.mLastSecondLoadTime = currentTime;
                    WifiproBqeUtils.this.mBqeMsgHandler.sendEmptyMessageDelayed(1, 1000);
                }
                super.handleMessage(msg);
            }
        };
    }

    public class WifiproBqeClientReceiver extends BroadcastReceiver {
        public WifiproBqeClientReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.d(WifiproBqeUtils.BQE_LOG_TAG, "receive broadcast action is " + action);
                if (WifiproBqeUtils.BQE_REQUEST_FINISHED.equals(action)) {
                    handleBeqRequestFinish(intent);
                } else if (WifiproBqeUtils.BQE_SERVICE_STARTED.equals(action)) {
                    WifiproBqeUtils.this.mIsServiceStarted = true;
                    if (WifiproBqeUtils.this.mIsSuspendRequest) {
                        WifiproBqeUtils.this.mIsSuspendRequest = false;
                        WifiproBqeUtils wifiproBqeUtils = WifiproBqeUtils.this;
                        wifiproBqeUtils.requestBqeOnce(wifiproBqeUtils.mSuspendRequestInterval);
                    }
                    WifiproBqeUtils.this.startWebView();
                } else if (WifiproBqeUtils.BQE_RECEIVE_REQUEST.equals(action)) {
                    WifiproBqeUtils.this.mIsStartRequestReceived = true;
                    WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(3);
                } else if (WifiproBqeUtils.BQE_BIND_WLAN_FAIL.equals(action)) {
                    Log.w(WifiproBqeUtils.BQE_LOG_TAG, "bqe bind wlan failed!Retry");
                    WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(3);
                    WifiproBqeUtils.this.mBqeMsgHandler.sendEmptyMessageDelayed(3, 2000);
                    WifiproBqeUtils.this.mIsBindWlanFailed = true;
                } else if ("com.huawei.wifipro.action.ACTION_RESP_WEBVIEW_CHECK_FROM_SERVICE".equals(action)) {
                    Log.d(WifiproBqeUtils.BQE_LOG_TAG, "ACTION_RESP_WEBVIEW_CHECK_FROM_SERVICE, need resp = " + WifiproBqeUtils.this.isNeedRecheckByWebView);
                    WifiproBqeUtils.this.isNeedRecheckByWebView = false;
                    WifiproBqeUtils.this.notifyWebviewResult(intent.getIntExtra("wifipro_flag_network_type", 100));
                }
            }
        }

        private void handleBeqRequestFinish(Intent intent) {
            long speed;
            if (WifiproBqeUtils.this.mBqeMsgHandler != null && intent != null) {
                WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(2);
                WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(5);
                Bundle bundle = intent.getExtras();
                int bqeResult = -1;
                long serviceBestSpeed = 0;
                if (bundle != null) {
                    bqeResult = bundle.getInt(WifiproBqeUtils.BQE_RESULT_BUNDLE, -1);
                    serviceBestSpeed = bundle.getLong(WifiproBqeUtils.BQE_SPEED_BUNDLE, 0) * WifiproBqeUtils.BYTE_TO_KB;
                }
                long currentTime = SystemClock.elapsedRealtime();
                if (bqeResult != 1) {
                    Log.w(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished, failed!");
                } else {
                    if (WifiproBqeUtils.this.mBestSpeed == 0) {
                        long currentRxByte = TrafficStats.getRxBytes(WifiproBqeUtils.WLAN_IFACE);
                        long duration = currentTime - WifiproBqeUtils.this.mLastSecondLoadTime;
                        if (duration > 0) {
                            speed = ((currentRxByte - WifiproBqeUtils.this.mLastSecondLoadRxByte) * 1000) / duration;
                        } else {
                            speed = 0;
                        }
                        if (speed > WifiproBqeUtils.this.mBestSpeed) {
                            WifiproBqeUtils.this.mBestSpeed = speed;
                        }
                    }
                    if (serviceBestSpeed > WifiproBqeUtils.this.mBestSpeed) {
                        WifiproBqeUtils.this.mBestSpeed = serviceBestSpeed;
                        Log.d(WifiproBqeUtils.BQE_LOG_TAG, "get better service speed " + WifiproBqeUtils.this.mBestSpeed);
                    }
                    if (WifiproBqeUtils.this.mIsSendRequest) {
                        WifiproBqeUtils.this.queryWlanRtt();
                        Log.d(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished, successful!");
                    } else {
                        Log.d(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished, no request!");
                    }
                }
                WifiproBqeUtils.this.mIsStopRequestReceived = true;
                long duration2 = currentTime - WifiproBqeUtils.this.mBqeStartedTime;
                if (duration2 < WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT) {
                    Log.d(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished in 4s");
                } else if (duration2 < 7000) {
                    Log.d(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished in 7s");
                } else {
                    Log.w(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished, more than 7s!");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyBQEResult(int bqeLevel, int tcpRtt, int tcpRttPkts, long bestSpeed) {
        synchronized (this.mLock) {
            Bundle data = new Bundle();
            data.putInt("RTT", tcpRtt);
            data.putInt("RTT_PKTS", tcpRttPkts);
            data.putLong("SPEED", bestSpeed);
            Message messageToFw = Message.obtain();
            messageToFw.what = 10;
            messageToFw.arg1 = 1;
            messageToFw.arg2 = bqeLevel;
            for (Handler handler : this.mBqeHandlerList) {
                if (handler != null) {
                    Message msg = Message.obtain(handler, 10, 1, bqeLevel);
                    msg.setData(data);
                    msg.sendToTarget();
                }
            }
            for (Messenger messenger : this.mBqeMessengerist) {
                if (messenger != null) {
                    try {
                        messenger.send(messageToFw);
                    } catch (RemoteException e) {
                        Log.e(BQE_LOG_TAG, "Exception happended in notifyBQEResult()");
                    }
                }
            }
            this.mBqeHandlerList.clear();
            this.mBqeMessengerist.clear();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyWebviewResult(int type) {
        synchronized (this.mLock) {
            for (Handler handler : this.mWebviewHandlerList) {
                Message.obtain(handler, 16, type, 0).sendToTarget();
            }
            this.mWebviewHandlerList.clear();
        }
    }

    private void resetHandlerList() {
        synchronized (this.mLock) {
            this.mBqeHandlerList.clear();
            this.mWebviewHandlerList.clear();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startWebView() {
        synchronized (this.mLock) {
            if (this.isNeedRecheckByWebView) {
                recheckNetworkTypeByWebView(this.isUseOverseaServer);
                this.isNeedRecheckByWebView = false;
            }
        }
    }
}
