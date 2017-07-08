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
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.Log;

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
    private static final String COUNTRY_CODE_CN = "460";
    private static final String DEFAULT_WLAN_IFACE = "wlan0";
    private static final int MSG_CHECK_SPEED = 1;
    private static final int MSG_CHECK_START_REQUEST_RECEIVE = 3;
    private static final int MSG_CHECK_STOP_REQUEST_RECEIVE = 5;
    private static final int MSG_REQUEST_EXPIRE = 4;
    private static final int MSG_STOP_BQE_REQUEST = 2;
    private static final int NO_URL_FAILED = -1;
    private static final String OPERATOR_COUNTRY_KEY = "WIFI_PRO_OPERATOR_COUNTRY_KEY";
    private static final String REQUEST_INFO = "Request_Info";
    private static final int SECOND_TO_MS = 1000;
    private static final String SERVICE_ACTION = "com.huawei.wifiprobqeservice.BQE_SERVICE";
    private static final String SERVICE_PKG = "com.huawei.wifiprobqeservice";
    private static final String WLAN_IFACE = null;
    private long mBestSpeed;
    private Handler mBqeMsgHandler;
    private long mBqeStartedTime;
    private ContentResolver mContentResolver;
    private Context mContext;
    private boolean mIsBindWlanFailed;
    private boolean mIsMsgHandlerInited;
    private boolean mIsOversea;
    private boolean mIsReceiverRegistered;
    private boolean mIsSendRequest;
    private boolean mIsServiceStarted;
    private boolean mIsSetExpire;
    private boolean mIsStartRequestReceived;
    private boolean mIsStopRequestReceived;
    private boolean mIsSuspendRequest;
    private long mLastSecondLoadRxByte;
    private long mLastSecondLoadTime;
    public boolean mNeedRecheckByWebView;
    private boolean mOverseaChecked;
    private Handler mQosMonitorHandler;
    private WifiproBqeClientReceiver mReceiver;
    private Intent mServceIntent;
    private int mSuspendRequestInterval;
    public boolean mUseOverseaServer;
    private WifiProStatisticsManager mWifiProStatisticsManager;

    public class WifiproBqeClientReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            long currentTime = SystemClock.elapsedRealtime();
            String action = intent.getAction();
            Log.d(WifiproBqeUtils.BQE_LOG_TAG, "receive broadcast action is " + action);
            Bundle bundle;
            if (WifiproBqeUtils.BQE_REQUEST_FINISHED.equals(action)) {
                long duration;
                WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(WifiproBqeUtils.MSG_STOP_BQE_REQUEST);
                WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(WifiproBqeUtils.MSG_CHECK_STOP_REQUEST_RECEIVE);
                bundle = intent.getExtras();
                int bqeResult = WifiproBqeUtils.NO_URL_FAILED;
                int urlFailedIndex = WifiproBqeUtils.NO_URL_FAILED;
                long serviceBestSpeed = 0;
                if (bundle != null) {
                    bqeResult = bundle.getInt(WifiproBqeUtils.BQE_RESULT_BUNDLE, WifiproBqeUtils.NO_URL_FAILED);
                    urlFailedIndex = bundle.getInt(WifiproBqeUtils.BUNDLE_URL_FAILED, WifiproBqeUtils.NO_URL_FAILED);
                    serviceBestSpeed = bundle.getLong(WifiproBqeUtils.BQE_SPEED_BUNDLE, 0) * WifiproBqeUtils.BYTE_TO_KB;
                }
                if (urlFailedIndex != WifiproBqeUtils.NO_URL_FAILED) {
                    if (WifiproBqeUtils.this.mIsOversea) {
                        WifiproBqeUtils.this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(urlFailedIndex + 10);
                    } else {
                        WifiproBqeUtils.this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(urlFailedIndex + 7);
                    }
                }
                if (bqeResult != WifiproBqeUtils.MSG_CHECK_SPEED) {
                    Log.w(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished, failed!");
                    WifiproBqeUtils.this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(15);
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
                    WifiproBqeUtils.this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(WifiproBqeUtils.MSG_REQUEST_EXPIRE);
                    return;
                } else if (duration < 7000) {
                    Log.d(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished in 7s");
                    WifiproBqeUtils.this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(WifiproBqeUtils.MSG_CHECK_STOP_REQUEST_RECEIVE);
                    return;
                } else {
                    Log.w(WifiproBqeUtils.BQE_LOG_TAG, "bqe finished, more than 7s!");
                    WifiproBqeUtils.this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(6);
                    return;
                }
            }
            if (WifiproBqeUtils.BQE_SERVICE_STARTED.equals(action)) {
                WifiproBqeUtils.this.mIsServiceStarted = true;
                if (WifiproBqeUtils.this.mIsSuspendRequest) {
                    WifiproBqeUtils.this.mIsSuspendRequest = false;
                    WifiproBqeUtils.this.requestBqeOnce(WifiproBqeUtils.this.mSuspendRequestInterval);
                }
                if (WifiproBqeUtils.this.mNeedRecheckByWebView) {
                    WifiproBqeUtils.this.recheckNetworkTypeByWebView(WifiproBqeUtils.this.mUseOverseaServer);
                    return;
                }
                return;
            }
            if (WifiproBqeUtils.BQE_RECEIVE_REQUEST.equals(action)) {
                WifiproBqeUtils.this.mIsStartRequestReceived = true;
                WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(WifiproBqeUtils.MSG_CHECK_START_REQUEST_RECEIVE);
                return;
            }
            if (WifiproBqeUtils.BQE_BIND_WLAN_FAIL.equals(action)) {
                if (WifiproBqeUtils.this.mIsMsgHandlerInited) {
                    Log.w(WifiproBqeUtils.BQE_LOG_TAG, "bqe bind wlan failed!Retry");
                    WifiproBqeUtils.this.mBqeMsgHandler.removeMessages(WifiproBqeUtils.MSG_CHECK_START_REQUEST_RECEIVE);
                    WifiproBqeUtils.this.mBqeMsgHandler.sendEmptyMessageDelayed(WifiproBqeUtils.MSG_CHECK_START_REQUEST_RECEIVE, 2000);
                    WifiproBqeUtils.this.mIsBindWlanFailed = true;
                    WifiproBqeUtils.this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(14);
                    return;
                }
                return;
            }
            if ("com.huawei.wifipro.action.ACTION_RESP_WEBVIEW_CHECK_FROM_SERVICE".equals(action)) {
                Log.d("WifiproBqeUtils", "ACTION_RESP_WEBVIEW_CHECK_FROM_SERVICE, need resp = " + WifiproBqeUtils.this.mNeedRecheckByWebView);
                if (WifiproBqeUtils.this.mNeedRecheckByWebView) {
                    WifiproBqeUtils.this.mNeedRecheckByWebView = false;
                    WifiproBqeUtils.this.mUseOverseaServer = false;
                    int type = intent.getIntExtra("wifipro_flag_network_type", 100);
                    Message.obtain(WifiproBqeUtils.this.mQosMonitorHandler, 16, type, WifiproBqeUtils.BQE_UNKNOWN_LEVEL).sendToTarget();
                    return;
                }
                return;
            }
            if ("com.huawei.wifipro.ACTION_NOTIFY_WIFI_SECURITY_STATUS".equals(action)) {
                bundle = intent.getExtras();
                Message.obtain(WifiproBqeUtils.this.mQosMonitorHandler, 18, bundle).sendToTarget();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.WifiproBqeUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.WifiproBqeUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WifiproBqeUtils.<clinit>():void");
    }

    public WifiproBqeUtils(Context context, Handler msgHandler) {
        this.mIsReceiverRegistered = false;
        this.mIsMsgHandlerInited = false;
        this.mIsServiceStarted = false;
        this.mIsSuspendRequest = false;
        this.mIsSendRequest = false;
        this.mIsStartRequestReceived = false;
        this.mIsStopRequestReceived = false;
        this.mIsSetExpire = false;
        this.mIsOversea = false;
        this.mOverseaChecked = false;
        this.mIsBindWlanFailed = false;
        this.mSuspendRequestInterval = BQE_UNKNOWN_LEVEL;
        this.mNeedRecheckByWebView = false;
        this.mUseOverseaServer = false;
        this.mContext = context;
        this.mQosMonitorHandler = msgHandler;
        this.mNeedRecheckByWebView = false;
        this.mServceIntent = new Intent(SERVICE_ACTION).setPackage(SERVICE_PKG);
        if (!this.mIsMsgHandlerInited) {
            this.mIsMsgHandlerInited = true;
            initBqeMsgQueue();
        }
        this.mContentResolver = this.mContext.getContentResolver();
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
    }

    public void startBqeService() {
        Log.d(BQE_LOG_TAG, "startBqeService");
        this.mContext.startService(this.mServceIntent);
        if (!this.mIsReceiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BQE_SERVICE_STARTED);
            filter.addAction(BQE_RECEIVE_REQUEST);
            filter.addAction(BQE_REQUEST_FINISHED);
            filter.addAction(BQE_SERVICE_EXITED);
            filter.addAction(BQE_BIND_WLAN_FAIL);
            filter.addAction("com.huawei.wifipro.action.ACTION_RESP_WEBVIEW_CHECK_FROM_SERVICE");
            filter.addAction("com.huawei.wifipro.ACTION_NOTIFY_WIFI_SECURITY_STATUS");
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
    }

    public void resetBqeState() {
        Log.d(BQE_LOG_TAG, "resetBqeState");
        if (this.mIsMsgHandlerInited) {
            this.mBqeMsgHandler.removeMessages(MSG_CHECK_SPEED);
            this.mBqeMsgHandler.removeMessages(MSG_STOP_BQE_REQUEST);
            this.mBqeMsgHandler.removeMessages(MSG_CHECK_START_REQUEST_RECEIVE);
            this.mBqeMsgHandler.removeMessages(MSG_CHECK_STOP_REQUEST_RECEIVE);
            this.mBqeMsgHandler.removeMessages(MSG_REQUEST_EXPIRE);
        }
        this.mIsSuspendRequest = false;
        this.mIsSendRequest = false;
        this.mIsStartRequestReceived = false;
        this.mIsSetExpire = false;
        this.mIsStopRequestReceived = false;
        this.mIsBindWlanFailed = false;
        this.mBqeStartedTime = SystemClock.elapsedRealtime();
    }

    public void requestBqeOnce(int interval) {
        Log.d(BQE_LOG_TAG, "requestBqeOnce");
        this.mSuspendRequestInterval = interval;
        if (this.mIsMsgHandlerInited) {
            this.mBqeMsgHandler.removeMessages(MSG_CHECK_START_REQUEST_RECEIVE);
            this.mBqeMsgHandler.removeMessages(MSG_STOP_BQE_REQUEST);
            this.mBqeMsgHandler.sendEmptyMessageDelayed(MSG_CHECK_START_REQUEST_RECEIVE, 2000);
            if (!this.mIsSetExpire) {
                this.mIsSetExpire = true;
                this.mBqeMsgHandler.sendEmptyMessageDelayed(MSG_REQUEST_EXPIRE, 7000);
            }
        }
        if (this.mIsServiceStarted) {
            boolean isFirstRequest;
            resetTcpState();
            if (!this.mOverseaChecked) {
                String operator = Secure.getString(this.mContentResolver, OPERATOR_COUNTRY_KEY);
                if (!(operator == null || operator.length() == 0 || operator.startsWith(COUNTRY_CODE_CN))) {
                    this.mIsOversea = true;
                }
                this.mOverseaChecked = true;
            }
            Intent intent = new Intent(BQE_REQUEST_ONCE);
            intent.setFlags(67108864);
            Bundle bundle = new Bundle();
            if (!this.mIsSendRequest || this.mIsBindWlanFailed) {
                isFirstRequest = true;
            } else {
                isFirstRequest = false;
            }
            String str = REQUEST_INFO;
            boolean[] zArr = new boolean[MSG_STOP_BQE_REQUEST];
            zArr[BQE_UNKNOWN_LEVEL] = this.mIsOversea;
            zArr[MSG_CHECK_SPEED] = isFirstRequest;
            bundle.putBooleanArray(str, zArr);
            intent.putExtras(bundle);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BQE_CLIENT_PERMISSION);
            this.mIsSendRequest = true;
            if (this.mIsMsgHandlerInited) {
                this.mBqeMsgHandler.removeMessages(MSG_CHECK_SPEED);
                this.mBqeMsgHandler.sendEmptyMessageDelayed(MSG_CHECK_SPEED, 1000);
                this.mBqeMsgHandler.sendEmptyMessageDelayed(MSG_STOP_BQE_REQUEST, (long) interval);
            }
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
            this.mBqeMsgHandler.removeMessages(MSG_REQUEST_EXPIRE);
            Intent intent = new Intent(BQE_STOP_REQUEST);
            intent.setFlags(67108864);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BQE_CLIENT_PERMISSION);
            if (this.mIsMsgHandlerInited) {
                this.mBqeMsgHandler.sendEmptyMessageDelayed(MSG_CHECK_STOP_REQUEST_RECEIVE, 1000);
            }
            return;
        }
        Log.d(BQE_LOG_TAG, "no BQE request send");
    }

    public void resetWlanRtt() {
        if (this.mQosMonitorHandler != null) {
            Message.obtain(this.mQosMonitorHandler, 8, MSG_CHECK_SPEED, BQE_UNKNOWN_LEVEL).sendToTarget();
        }
    }

    private void resetTcpState() {
        Log.d(BQE_LOG_TAG, "resetTcpState");
        resetWlanRtt();
        this.mLastSecondLoadRxByte = TrafficStats.getRxBytes(WLAN_IFACE);
        this.mLastSecondLoadTime = SystemClock.elapsedRealtime();
        this.mBestSpeed = 0;
    }

    public void queryWlanRtt() {
        if (this.mQosMonitorHandler != null) {
            Message.obtain(this.mQosMonitorHandler, 9, MSG_CHECK_SPEED, BQE_UNKNOWN_LEVEL).sendToTarget();
        }
    }

    private void computeQos(int tcp_rtt, int tcp_rtt_pkts, int tcp_rtt_when) {
        int bqeLevel;
        if (this.mIsMsgHandlerInited) {
            this.mBqeMsgHandler.removeMessages(MSG_CHECK_SPEED);
        }
        if ((tcp_rtt_pkts <= MSG_CHECK_STOP_REQUEST_RECEIVE || tcp_rtt == 0 || tcp_rtt_when >= MSG_CHECK_STOP_REQUEST_RECEIVE) && this.mBestSpeed < 20480) {
            bqeLevel = MSG_CHECK_SPEED;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(MSG_CHECK_START_REQUEST_RECEIVE);
        } else if (tcp_rtt <= BQE_GOOD_RTT || this.mBestSpeed > 358400) {
            bqeLevel = MSG_CHECK_START_REQUEST_RECEIVE;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(MSG_CHECK_SPEED);
        } else if (tcp_rtt <= BQE_NOT_GOOD_RTT || this.mBestSpeed > 20480) {
            bqeLevel = MSG_STOP_BQE_REQUEST;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(MSG_STOP_BQE_REQUEST);
        } else {
            bqeLevel = MSG_CHECK_SPEED;
            this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(MSG_CHECK_START_REQUEST_RECEIVE);
        }
        Log.d(BQE_LOG_TAG, "bqeLevel " + bqeLevel + " rtt=" + tcp_rtt + ",rtt_pkts=" + tcp_rtt_pkts + " speed=" + this.mBestSpeed);
        Message.obtain(this.mQosMonitorHandler, 10, MSG_CHECK_SPEED, bqeLevel).sendToTarget();
    }

    public void setWlanRtt(int[] ipqos, int len) {
        int tcp_rtt = len > 0 ? ipqos[BQE_UNKNOWN_LEVEL] : BQE_UNKNOWN_LEVEL;
        int tcp_rtt_pkts = len > MSG_CHECK_SPEED ? ipqos[MSG_CHECK_SPEED] : BQE_UNKNOWN_LEVEL;
        int tcp_rtt_when = len > MSG_STOP_BQE_REQUEST ? ipqos[MSG_STOP_BQE_REQUEST] : BQE_UNKNOWN_LEVEL;
        resetWlanRtt();
        computeQos(tcp_rtt, tcp_rtt_pkts, tcp_rtt_when);
    }

    public void setRecheckFlagByWebView(boolean oversea) {
        this.mNeedRecheckByWebView = true;
        this.mUseOverseaServer = oversea;
    }

    public void recheckNetworkTypeByWebView(boolean oversea) {
        Log.d("WifiproBqeUtils", "recheckNetworkTypeByWebView:: begin, oversea = " + oversea);
        Intent intent = new Intent("com.huawei.wifipro.action.ACTION_REQUEST_WEBVIEW_CHECK_TO_SERVICE");
        intent.putExtra("wifipro_flag_oversea", oversea);
        intent.setFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void queryWifiSecurity(String ssid, String bssid) {
        Log.d(BQE_LOG_TAG, "queryWifiSecurity, ssid = " + ssid + ", bssid = " + bssid);
        if (ssid != null && bssid != null) {
            Intent intent = new Intent("com.huawei.wifipro.ACTION_QUERY_WIFI_SECURITY");
            intent.setFlags(67108864);
            intent.putExtra("com.huawei.wifipro.FLAG_SSID", ssid);
            intent.putExtra("com.huawei.wifipro.FLAG_BSSID", bssid);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.wifipro.permission.WIFI_SECURITY_CHECK");
        }
    }

    private void initBqeMsgQueue() {
        this.mBqeMsgHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WifiproBqeUtils.MSG_CHECK_SPEED /*1*/:
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
                            WifiproBqeUtils.this.mBqeMsgHandler.sendEmptyMessageDelayed(WifiproBqeUtils.MSG_CHECK_SPEED, 1000);
                            break;
                        }
                        break;
                    case WifiproBqeUtils.MSG_STOP_BQE_REQUEST /*2*/:
                        WifiproBqeUtils.this.stopBqeRequest();
                        break;
                    case WifiproBqeUtils.MSG_CHECK_START_REQUEST_RECEIVE /*3*/:
                        if (!WifiproBqeUtils.this.mIsStartRequestReceived) {
                            WifiproBqeUtils.this.requestBqeOnce(WifiproBqeUtils.this.mSuspendRequestInterval);
                            break;
                        }
                        break;
                    case WifiproBqeUtils.MSG_REQUEST_EXPIRE /*4*/:
                        Log.i(WifiproBqeUtils.BQE_LOG_TAG, "request expire!");
                        WifiproBqeUtils.this.resetBqeState();
                        WifiproBqeUtils.this.mWifiProStatisticsManager.updateBqeSvcChrStatistic(6);
                        Message.obtain(WifiproBqeUtils.this.mQosMonitorHandler, 10, WifiproBqeUtils.MSG_CHECK_SPEED, WifiproBqeUtils.BQE_UNKNOWN_LEVEL).sendToTarget();
                        break;
                    case WifiproBqeUtils.MSG_CHECK_STOP_REQUEST_RECEIVE /*5*/:
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
}
