package com.android.server.wifi.wifipro;

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
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifi.HwQoE.HwQoEJNIAdapter;
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
    private static final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
    private static WifiproBqeUtils mWifiproBqeUtils = null;
    private long mBestSpeed;
    private List<Handler> mBqeHandlerList = new ArrayList();
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
    private Object mLock = new Object();
    public boolean mNeedRecheckByWebView = false;
    private boolean mOverseaChecked = false;
    private WifiproBqeClientReceiver mReceiver;
    private Intent mServceIntent;
    private int mSuspendRequestInterval = 0;
    public boolean mUseOverseaServer = false;
    private List<Handler> mWebviewHandlerList = new ArrayList();
    private WifiProStatisticsManager mWifiProStatisticsManager;

    public class WifiproBqeClientReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            long currentTime = SystemClock.elapsedRealtime();
            String action = intent.getAction();
            Log.d(WifiproBqeUtils.BQE_LOG_TAG, "receive broadcast action is " + action);
            if (WifiproBqeUtils.BQE_REQUEST_FINISHED.equals(action)) {
                long duration;
                WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(2);
                WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(5);
                Bundle bundle = intent.getExtras();
                int bqeResult = -1;
                long serviceBestSpeed = 0;
                if (bundle != null) {
                    bqeResult = bundle.getInt(WifiproBqeUtils.BQE_RESULT_BUNDLE, -1);
                    serviceBestSpeed = bundle.getLong(WifiproBqeUtils.BQE_SPEED_BUNDLE, 0) * WifiproBqeUtils.BYTE_TO_KB;
                }
                if (bqeResult != 1) {
                    Log.w(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished, failed!");
                } else {
                    if (WifiproBqeUtils.this.mBestSpeed == 0) {
                        long currentRxByte = TrafficStats.getRxBytes(WifiproBqeUtils.WLAN_IFACE);
                        duration = currentTime - WifiproBqeUtils.this.mLastSecondLoadTime;
                        long speed = 0;
                        if (duration > 0) {
                            speed = ((currentRxByte - WifiproBqeUtils.this.mLastSecondLoadRxByte) * 1000) / duration;
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
                duration = currentTime - WifiproBqeUtils.this.mBqeStartedTime;
                if (duration < 4000) {
                    Log.d(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished in 4s");
                } else if (duration < 7000) {
                    Log.d(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished in 7s");
                } else {
                    Log.w(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished, more than 7s!");
                }
            } else if (WifiproBqeUtils.BQE_SERVICE_STARTED.equals(action)) {
                WifiproBqeUtils.this.mIsServiceStarted = true;
                if (WifiproBqeUtils.this.mIsSuspendRequest) {
                    WifiproBqeUtils.this.mIsSuspendRequest = false;
                    WifiproBqeUtils.this.requestBqeOnce(WifiproBqeUtils.this.mSuspendRequestInterval);
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
                Log.d("WifiproBqeUtils", "ACTION_RESP_WEBVIEW_CHECK_FROM_SERVICE, need resp = " + WifiproBqeUtils.this.mNeedRecheckByWebView);
                WifiproBqeUtils.this.mNeedRecheckByWebView = false;
                WifiproBqeUtils.this.notifyWebviewResult(intent.getIntExtra("wifipro_flag_network_type", 100));
            }
        }
    }

    private WifiproBqeUtils(Context context) {
        this.mContext = context;
        this.mNeedRecheckByWebView = false;
        this.mServceIntent = new Intent(SERVICE_ACTION).setPackage(SERVICE_PKG);
        initBqeMsgQueue();
        this.mContentResolver = this.mContext.getContentResolver();
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
    }

    public static synchronized WifiproBqeUtils getInstance(Context context) {
        WifiproBqeUtils wifiproBqeUtils;
        synchronized (WifiproBqeUtils.class) {
            if (mWifiproBqeUtils == null) {
                mWifiproBqeUtils = new WifiproBqeUtils(context);
            }
            wifiproBqeUtils = mWifiproBqeUtils;
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
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        if (am != null) {
            am.forceStopPackage(SERVICE_PKG);
            Log.d(BQE_LOG_TAG, "forcestopBqeService");
        } else {
            this.mContext.stopService(this.mServceIntent);
            Log.d(BQE_LOG_TAG, "stopBqeService");
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

    public void requestBqeOnce(int interval, Handler msgHandler) {
        synchronized (this.mLock) {
            if (this.mBqeHandlerList.size() == 0) {
                requestBqeOnce(interval);
            }
            this.mBqeHandlerList.add(msgHandler);
        }
    }

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
        if (this.mIsServiceStarted) {
            boolean isFirstRequest;
            resetTcpState();
            if (!this.mOverseaChecked) {
                String operator = null;
                TelephonyManager telManager = (TelephonyManager) this.mContext.getSystemService("phone");
                if (telManager != null) {
                    operator = telManager.getNetworkOperator();
                }
                if (operator == null || operator.length() == 0) {
                    this.mIsOversea = true;
                } else if (!operator.startsWith("460")) {
                    this.mIsOversea = true;
                }
                this.mOverseaChecked = true;
            }
            Intent intent = new Intent(BQE_REQUEST_ONCE);
            intent.setFlags(67108864);
            Bundle bundle = new Bundle();
            if (!this.mIsSendRequest || (this.mIsBindWlanFailed ^ 1) == 0) {
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
            return;
        }
        this.mIsSuspendRequest = true;
        Log.d(BQE_LOG_TAG, "service havn't started!");
        startBqeService();
    }

    public void stopBqeRequest() {
        if (this.mIsSendRequest) {
            Log.d(BQE_LOG_TAG, "stopBqeRequest");
            this.mIsSetExpire = false;
            this.mIsStopRequestReceived = false;
            this.mBqeMsgHandler.removeMessages(4);
            Intent intent = new Intent(BQE_STOP_REQUEST);
            intent.setFlags(67108864);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BQE_CLIENT_PERMISSION);
            this.mBqeMsgHandler.sendEmptyMessageDelayed(5, 1000);
            return;
        }
        Log.d(BQE_LOG_TAG, "no BQE request send");
    }

    public void resetWlanRtt() {
        HwQoEJNIAdapter.getInstance().resetRtt(1);
    }

    private void resetTcpState() {
        Log.d(BQE_LOG_TAG, "resetTcpState");
        resetWlanRtt();
        this.mLastSecondLoadRxByte = TrafficStats.getRxBytes(WLAN_IFACE);
        this.mLastSecondLoadTime = SystemClock.elapsedRealtime();
        this.mBestSpeed = 0;
    }

    public void queryWlanRtt() {
        int[] result = HwQoEJNIAdapter.getInstance().queryBQERttResult(1);
        if (result != null) {
            setWlanRtt(result, result.length);
        }
    }

    private void computeQos(int tcp_rtt, int tcp_rtt_pkts, int tcp_rtt_when) {
        int bqeLevel;
        this.mBqeMsgHandler.removeMessages(1);
        if ((tcp_rtt_pkts <= 5 || tcp_rtt == 0 || tcp_rtt_when >= 5) && this.mBestSpeed < 20480) {
            bqeLevel = 1;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(3);
        } else if (tcp_rtt <= BQE_GOOD_RTT || this.mBestSpeed > 358400) {
            bqeLevel = 3;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(1);
        } else if (tcp_rtt <= BQE_NOT_GOOD_RTT || this.mBestSpeed > 20480) {
            bqeLevel = 2;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(2);
        } else {
            bqeLevel = 1;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(3);
        }
        Log.d(BQE_LOG_TAG, "bqeLevel " + bqeLevel + " rtt=" + tcp_rtt + ",rtt_pkts=" + tcp_rtt_pkts + " speed=" + this.mBestSpeed);
        notifyBQEResult(bqeLevel, tcp_rtt, tcp_rtt_pkts, this.mBestSpeed);
    }

    public void setWlanRtt(int[] ipqos, int len) {
        int tcp_rtt = len > 0 ? ipqos[0] : 0;
        int tcp_rtt_pkts = len > 1 ? ipqos[1] : 0;
        int tcp_rtt_when = len > 2 ? ipqos[2] : 0;
        resetWlanRtt();
        computeQos(tcp_rtt, tcp_rtt_pkts, tcp_rtt_when);
    }

    public void recheckNetworkTypeByWebView(Handler handler, boolean oversea) {
        synchronized (this.mLock) {
            if (this.mWebviewHandlerList.size() == 0) {
                this.mUseOverseaServer = oversea;
                recheckNetworkTypeByWebView(this.mUseOverseaServer);
            }
            this.mWebviewHandlerList.add(handler);
        }
    }

    private void recheckNetworkTypeByWebView(boolean oversea) {
        Log.d("WifiproBqeUtils", "recheckNetworkTypeByWebView:: oversea = " + oversea);
        if (this.mIsServiceStarted) {
            Intent intent = new Intent("com.huawei.wifipro.action.ACTION_REQUEST_WEBVIEW_CHECK_TO_SERVICE");
            intent.putExtra("wifipro_flag_oversea", oversea);
            intent.setFlags(67108864);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            return;
        }
        this.mNeedRecheckByWebView = true;
        Log.d(BQE_LOG_TAG, "service havn't started!");
        startBqeService();
    }

    private void initBqeMsgQueue() {
        this.mBqeMsgHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (WifiproBqeUtils.this.mIsSendRequest) {
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
                            break;
                        }
                        break;
                    case 2:
                        WifiproBqeUtils.this.stopBqeRequest();
                        break;
                    case 3:
                        if (!WifiproBqeUtils.this.mIsStartRequestReceived) {
                            WifiproBqeUtils.this.requestBqeOnce(WifiproBqeUtils.this.mSuspendRequestInterval);
                            break;
                        }
                        break;
                    case 4:
                        Log.i(WifiproBqeUtils.BQE_LOG_TAG, "request expire!");
                        WifiproBqeUtils.this.resetBqeState();
                        WifiproBqeUtils.this.notifyBQEResult(0, 0, 0, 0);
                        break;
                    case 5:
                        if (!WifiproBqeUtils.this.mIsStopRequestReceived) {
                            WifiproBqeUtils.this.stopBqeRequest();
                            break;
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void notifyBQEResult(int bqeLevel, int tcp_rtt, int tcp_rtt_pkts, long bestSpeed) {
        synchronized (this.mLock) {
            for (Handler handler : this.mBqeHandlerList) {
                Message msg = Message.obtain(handler, 10, 1, bqeLevel);
                Bundle data = new Bundle();
                data.putInt("RTT", tcp_rtt);
                data.putInt("RTT_PKTS", tcp_rtt_pkts);
                data.putLong("SPEED", bestSpeed);
                msg.setData(data);
                msg.sendToTarget();
            }
            this.mBqeHandlerList.clear();
        }
    }

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

    private void startWebView() {
        synchronized (this.mLock) {
            if (this.mNeedRecheckByWebView) {
                recheckNetworkTypeByWebView(this.mUseOverseaServer);
                this.mNeedRecheckByWebView = false;
            }
        }
    }
}
