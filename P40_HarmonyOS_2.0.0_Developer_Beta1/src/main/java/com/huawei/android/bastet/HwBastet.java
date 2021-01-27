package com.huawei.android.bastet;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoBridge;

public class HwBastet extends BastetManager {
    private static final String BASTET_SERVICE = "BastetService";
    private static final int BST_APP_CONN_TYPE_AP_ERR_MAX = 262143;
    private static final int BST_APP_CONN_TYPE_CP_ERR_CLS = 65536;
    private static final String CHR_APK_PACKEGE_NAME = "com.huawei.android.chr";
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int DEFAULT_MSG_HRT = 262144;
    private static final int DEFAULT_MSG_LPW = 524288;
    private static final int DEFAULT_TCP_RECONN = 100;
    private static final String INTENT_BAETST_ERROR_CHR_REPORT = "com.huawei.intent.action.bastet_error_chr_report";
    private static final int INVALID_FIELD_VALUE = -1;
    private static final int INVALID_INTERVAL = -1;
    private static final int INVALID_PROXY_ID = -1;
    private static final long INVALID_TIME = -1;
    private static final int IPV4_ADDR_LEN = 4;
    private static final int IPV6_ADDR_LEN = 16;
    private static final boolean IS_HEART_BEAT_SYNC_ENABLED = SystemProperties.getBoolean("ro.config.bastet_hb_sync.enabled", true);
    private static final int MAX_SEND_HRT_LENGTH = 5120;
    private static final int MAX_SEND_LPW_LENGTH = 1024;
    private static final String PUSH_PROCESS_NAME = "com.huawei.android.pushagent.PushService";
    private static final String TAG = "HwBastet";
    private static final int TYPE_AP_ERR_CLS = 0;
    private static final int TYPE_AP_NORMAL_CLS = 1;
    private static final int TYPE_AP_NORMAL_EST = 2;
    private static final int TYPE_BEST_CONN_POINT = 255;
    private static final int TYPE_NRT_SEND = 0;
    private static final int TYPE_RECONN_BREAK = 254;
    private static int bastetMsgHrt;
    private static int bastetMsgLpw;
    private static int bastetTcpReconnect;
    private static boolean is3gNetwork = false;
    private static boolean isMobileEnable = false;
    private static ConnectivityManager sConnectivityManager;
    private static TelephonyManager sTelephonyManager = null;
    private boolean isBastetSupport = false;
    private boolean isReconnSwitch = false;
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        /* class com.huawei.android.bastet.HwBastet.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                NetworkInfo info = HwBastet.sConnectivityManager.getActiveNetworkInfo();
                if (info == null || !info.isAvailable()) {
                    boolean unused = HwBastet.isMobileEnable = false;
                    return;
                }
                if (info.getType() == 0) {
                    boolean unused2 = HwBastet.isMobileEnable = true;
                } else {
                    boolean unused3 = HwBastet.isMobileEnable = false;
                }
                boolean unused4 = HwBastet.is3gNetwork = HwBastet.this.is3gNetwork(info.getSubtype());
            }
        }
    };
    private Context mContext;
    private int mHbInterval = -1;
    private int mHbProxyId = -1;
    private byte[] mHbReplys;
    private byte[] mHbSends;
    private int mLastSignalAvailable = 0;
    private ArrayList<Pair<Socket, byte[]>> mNrtCache = new ArrayList<>();
    private long mNrtMinTimeout = INVALID_TIME;
    private boolean mPushReportFlag = false;
    private Socket mSocket;
    private boolean mSupportIpv6 = false;

    public HwBastet(String license, Socket socket, Handler handler, Context context) {
        this.mSocket = socket;
        this.mHandler = handler;
        this.mContext = context;
        initConnectivityService();
        prepareHeartbeatProxy();
        getBastetConstants();
        updataPushReportFlag();
    }

    public HwBastet(String license, Handler handler, Context context) {
        Log.e(TAG, "HwBastet, not provide socket");
        this.mSocket = null;
        this.mHandler = handler;
        this.mContext = context;
        initConnectivityService();
    }

    public void reconnectSwitch(boolean isEnable) throws Exception {
        synchronized (this) {
            if (this.isReconnSwitch != isEnable) {
                setReconnEnable(isEnable);
                this.isReconnSwitch = isEnable;
            }
        }
    }

    public void setAolHeartbeat(int interval, byte[] send, byte[] reply) throws Exception {
        synchronized (this) {
            if (send.length == 0 || reply.length == 0 || interval <= 0) {
                Log.e(TAG, "Invalid heartbeat parameters");
                throw new BastetException("Invalid heartbeat parameters");
            }
            if (interval > 3) {
                this.mHbInterval = 3;
            } else {
                this.mHbInterval = interval;
            }
            this.mHbSends = new byte[send.length];
            System.arraycopy(send, 0, this.mHbSends, 0, send.length);
            this.mHbReplys = new byte[reply.length];
            System.arraycopy(reply, 0, this.mHbReplys, 0, reply.length);
            setHeartbeatProxy();
        }
    }

    public void pauseHeartbeat() throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || !this.isBastetSupport || this.mIBastetManager == null) {
                throw new BastetException();
            }
            this.mIBastetManager.stopBastetProxy(this.mHbProxyId);
        }
    }

    public void resumeHeartbeat() throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || !this.isBastetSupport || this.mIBastetManager == null) {
                throw new BastetException();
            }
            if (IS_HEART_BEAT_SYNC_ENABLED) {
                this.mIBastetManager.notifyElapsedRealTime(this.mHbProxyId, SystemClock.elapsedRealtime());
            }
            if (!isIpv6Socket(this.mSocket)) {
                this.mIBastetManager.startBastetProxy(this.mHbProxyId);
            } else {
                Log.d(TAG, "resumeHeartbeat, startBastetProxyIpv6, mHbProxyId=" + this.mHbProxyId);
                this.mIBastetManager.startBastetProxyIpv6(this.mHbProxyId);
            }
        }
    }

    public void sendNrtData(byte[] data, long timeout, Socket socket) throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || !this.isBastetSupport || this.mIBastetManager == null) {
                sendData(socket, data, 0);
            } else {
                this.mNrtCache.add(new Pair<>(socket, data));
                long elapsedTimeout = timeout + SystemClock.elapsedRealtime();
                if (elapsedTimeout < this.mNrtMinTimeout || this.mNrtMinTimeout == INVALID_TIME) {
                    this.mNrtMinTimeout = elapsedTimeout;
                    this.mIBastetManager.setNrtTime(this.mHbProxyId, timeout);
                }
            }
        }
    }

    public void sendHrtData(byte[] data, Socket socket) throws IOException {
        sendData(socket, data, data.length <= MAX_SEND_HRT_LENGTH ? bastetMsgHrt : 0);
    }

    public void sendLpwData(byte[] data, Socket socket) throws IOException {
        sendData(socket, data, (!is3gNetwork || data.length > 1024) ? 0 : bastetMsgLpw);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.android.bastet.BastetManager
    public void handleProxyMessage(int proxyId, int err, int ext) {
        Log.d(TAG, "HwBastet handleProxyMessage: proxyId=" + proxyId + ", err=" + err + ", ext=" + ext);
        if (proxyId != this.mHbProxyId || err == -10) {
            return;
        }
        if (err != -1) {
            switch (err) {
                case BastetParameters.NETWORK_QUALITY /* -19 */:
                    handleNetworkQuality(ext);
                    return;
                case BastetParameters.PROXY_SEND_NRT /* -18 */:
                    handleProxySendNrt(ext);
                    return;
                case BastetParameters.HEARTBEAT_CYCLE /* -17 */:
                    handleHeartbeatCycle(ext);
                    return;
                case -16:
                    handleProxySocketState(ext);
                    return;
                default:
                    return;
            }
        } else {
            handleBastetAvailable(ext);
        }
    }

    public boolean isIpv6Support() {
        return isBastetSupportIpv6();
    }

    public void prepareHeartbeatProxy(Socket socket) {
        if (socket == null) {
            Log.e(TAG, "socket is null");
            return;
        }
        synchronized (this) {
            this.mSocket = socket;
            if (this.mHbProxyId <= 0) {
                try {
                    if (!isIpv6Socket(socket)) {
                        this.mHbProxyId = this.mIBastetManager.prepareHeartbeatProxy(getIntFd(), this.mIBastetListener);
                    } else if (isBastetSupportIpv6()) {
                        this.mHbProxyId = this.mIBastetManager.prepareHeartbeatProxyIpv6(getIntFd(), this.mIBastetListener);
                    } else {
                        Log.e(TAG, "bastet not support ipv6 heartbeat!");
                        sendMessage(5);
                    }
                } catch (RemoteException e) {
                    this.mContext.unregisterReceiver(this.mConnectivityReceiver);
                }
            }
            this.isBastetSupport = isBastetAvailable();
        }
        getBastetConstants();
        updataPushReportFlag();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.android.bastet.BastetManager
    public void onBastetDied() {
        sendMessage(5);
        sendAllData();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean is3gNetwork(int type) {
        if (type == 3 || type == 15 || type == 17) {
            return true;
        }
        switch (type) {
            case 8:
            case 9:
            case 10:
                return true;
            default:
                return false;
        }
    }

    private void initConnectivityService() {
        Object connectService = this.mContext.getSystemService("connectivity");
        if (connectService instanceof ConnectivityManager) {
            sConnectivityManager = (ConnectivityManager) connectService;
            if (sConnectivityManager == null) {
                Log.e(TAG, "Failed to get connectivity service");
                return;
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mContext.registerReceiver(this.mConnectivityReceiver, filter);
        }
    }

    private int getIntFd() {
        return this.mSocket.getFileDescriptor$().getInt$();
    }

    private FileDescriptor getFd(Socket socket) {
        return (socket != null ? socket : this.mSocket).getFileDescriptor$();
    }

    private void sendData(Socket socket, byte[] data, int flags) throws IOException {
        IoBridge.sendto(getFd(socket), data, 0, data.length, isMobileEnable ? flags : 0, (InetAddress) null, 0);
    }

    private void setReconnEnable(boolean isEnable) throws Exception {
        if (this.mSocket == null || this.mHbProxyId <= 0 || this.mIBastetManager == null) {
            throw new BastetException();
        }
        this.mIBastetManager.setReconnEnable(this.mHbProxyId, isEnable);
    }

    private void prepareHeartbeatProxy() {
        if (this.mIBastetManager == null || this.mSocket == null) {
            Log.e(TAG, "mIBastetManager or mSocket is null");
            return;
        }
        synchronized (this) {
            if (this.mHbProxyId <= 0) {
                try {
                    if (isIpv6Socket(this.mSocket)) {
                        Log.e(TAG, "prepareHeartbeatProxy, ipv6 socket, invoke the prepareHeartbeatProxyIpv6");
                        if (isBastetSupportIpv6()) {
                            this.mHbProxyId = this.mIBastetManager.prepareHeartbeatProxyIpv6(getIntFd(), this.mIBastetListener);
                        } else {
                            Log.e(TAG, "bastet not support ipv6 heartbeat!");
                            sendMessage(5);
                        }
                    } else {
                        Log.e(TAG, "prepareHeartbeatProxy, ipv4 socket, invoke the prepareHeartbeatProxy");
                        this.mHbProxyId = this.mIBastetManager.prepareHeartbeatProxy(getIntFd(), this.mIBastetListener);
                    }
                } catch (RemoteException e) {
                    this.mContext.unregisterReceiver(this.mConnectivityReceiver);
                }
            }
            this.isBastetSupport = isBastetAvailable();
        }
    }

    private void setHeartbeatProxy() throws Exception {
        if (this.mHbProxyId <= 0 || !this.isBastetSupport || this.mIBastetManager == null) {
            throw new BastetException();
        }
        this.mIBastetManager.updateRepeatInterval(this.mHbProxyId, this.mHbInterval);
        this.mIBastetManager.setHeartbeatFixedContent(this.mHbProxyId, this.mHbSends, this.mHbReplys);
        if (IS_HEART_BEAT_SYNC_ENABLED) {
            this.mIBastetManager.notifyElapsedRealTime(this.mHbProxyId, SystemClock.elapsedRealtime());
        }
        if (!isIpv6Socket(this.mSocket)) {
            this.mIBastetManager.startBastetProxy(this.mHbProxyId);
            return;
        }
        Log.d(TAG, "setHeartbeatProxy, startBastetProxyIpv6, mHbProxyId=" + this.mHbProxyId);
        if (isBastetSupportIpv6()) {
            this.mIBastetManager.startBastetProxyIpv6(this.mHbProxyId);
            return;
        }
        Log.e(TAG, "bastet not support ipv6 heartbeat!");
        sendMessage(5);
    }

    private void clearBastetProxy() {
        try {
            if (this.mHbProxyId > 0 && this.mIBastetManager != null) {
                this.mIBastetManager.clearProxyById(this.mHbProxyId);
            }
            this.mHbProxyId = -1;
            this.mContext.unregisterReceiver(this.mConnectivityReceiver);
        } catch (RemoteException e) {
            Log.e(TAG, "clearBastetProxy RemoteException");
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "clearBastetProxy IllegalArgumentException");
        }
    }

    private void sendPushSocketStateToChr(int state) {
        if (this.mContext == null) {
            Log.e(TAG, "sendPushSocketStateToChr fail,mContext is null.");
            return;
        }
        Intent intent = new Intent(INTENT_BAETST_ERROR_CHR_REPORT);
        intent.setPackage(CHR_APK_PACKEGE_NAME);
        Bundle data = new Bundle();
        data.putInt("pushErrSocketState", state);
        intent.putExtras(data);
        this.mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    private void handleProxySocketState(int state) {
        if (65536 <= state && state <= BST_APP_CONN_TYPE_AP_ERR_MAX && this.mPushReportFlag) {
            sendMessage(state);
            sendPushSocketStateToChr(state);
        }
        if (state == 0) {
            synchronized (this) {
                if (!this.isReconnSwitch) {
                    clearBastetProxy();
                }
            }
            clearAllData();
            this.mNrtMinTimeout = INVALID_TIME;
            sendMessage(2);
        } else if (state == 1) {
            clearBastetProxy();
            clearAllData();
            this.mNrtMinTimeout = INVALID_TIME;
        } else if (state == 2) {
            sendMessage(1);
        } else if (state == TYPE_RECONN_BREAK) {
            sendMessage(8);
        } else if (state == 255) {
            sendMessage(7);
            clearBastetProxy();
        }
    }

    private void handleHeartbeatCycle(int cycle) {
        sendMessage(4, cycle);
    }

    private void handleNetworkQuality(int signalLevel) {
        int isAvailable = 0;
        if (signalLevel == 0) {
            isAvailable = 0;
        } else if (signalLevel == 1 || signalLevel == 2 || signalLevel == 3) {
            isAvailable = 1;
        }
        if (this.mLastSignalAvailable != isAvailable) {
            this.mLastSignalAvailable = isAvailable;
            sendMessage(3, isAvailable);
        }
    }

    private void handleBastetAvailable(int available) {
        synchronized (this) {
            Log.d(TAG, "isBastetSupport: " + this.isBastetSupport + ", available: " + available);
            if (available != 0) {
                this.isBastetSupport = true;
            } else {
                this.isBastetSupport = false;
            }
            if (!this.isBastetSupport) {
                sendMessage(5);
            }
        }
    }

    private void handleProxySendNrt(int state) {
        if (state == 0) {
            sendAllData();
            this.mNrtMinTimeout = INVALID_TIME;
        }
    }

    private void sendAllData() {
        synchronized (this) {
            while (!this.mNrtCache.isEmpty()) {
                try {
                    sendData((Socket) this.mNrtCache.get(0).first, (byte[]) this.mNrtCache.get(0).second, 0);
                } catch (IOException e) {
                    Log.e(TAG, "sendAllData IOException");
                }
                this.mNrtCache.remove(0);
            }
        }
    }

    private void clearAllData() {
        Log.d(TAG, "Clear all nrt data");
        synchronized (this) {
            if (!this.mNrtCache.isEmpty()) {
                this.mNrtCache.clear();
            }
        }
    }

    private int getOsConstantsField(String filedName) {
        int value = 0;
        try {
            value = Class.forName("android.system.OsConstants").getDeclaredField(filedName).getInt(null);
        } catch (ClassNotFoundException e) {
            value = -1;
        } catch (NoSuchFieldException e2) {
            Log.d(TAG, "getOsConstantsField NoSuchFieldException");
        } catch (IllegalAccessException e3) {
            Log.d(TAG, "getOsConstantsField IllegalAccessException");
        }
        Log.d(TAG, "OsConstants " + filedName + ": " + value);
        return value;
    }

    private void getBastetConstants() {
        int val = getOsConstantsField("MSG_HRT");
        if (val <= 0) {
            val = 262144;
        }
        bastetMsgHrt = val;
        int val2 = getOsConstantsField("MSG_LPW");
        if (val2 <= 0) {
            val2 = 524288;
        }
        bastetMsgLpw = val2;
        int val3 = getOsConstantsField("TCP_RECONN");
        if (val3 <= 0) {
            val3 = 100;
        }
        bastetTcpReconnect = val3;
    }

    private void updataPushReportFlag() {
        Object obj = this.mContext.getSystemService(FreezeScreenScene.ACTIVITY_PARAM);
        ActivityManager am = null;
        if (obj instanceof ActivityManager) {
            am = (ActivityManager) obj;
        }
        if (am == null) {
            Log.e(TAG, "updataPushReportFlag fail,ActivityManager is null.");
            return;
        }
        List<ActivityManager.RunningAppProcessInfo> processList = am.getRunningAppProcesses();
        if (processList == null) {
            Log.e(TAG, "updataPushReportFlag fail,processList is null.");
            return;
        }
        for (ActivityManager.RunningAppProcessInfo process : processList) {
            if (process.pid == Process.myPid() && PUSH_PROCESS_NAME.equals(process.processName)) {
                this.mPushReportFlag = true;
                return;
            }
        }
    }

    private boolean isIpv6Socket(Socket socket) {
        if (socket == null) {
            Log.e(TAG, "the socket is null");
            return false;
        }
        InetAddress host = socket.getInetAddress();
        if (host == null) {
            Log.e(TAG, "getInetAddress, host is null");
            return false;
        } else if (host.getAddress().length != 16 || isMappedIPv4Address(host.getAddress())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isMappedIPv4Address(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            return false;
        }
        for (int j = 0; j < 10; j++) {
            if (bytes[j] != 0) {
                return false;
            }
        }
        for (int j2 = 10; j2 < 12; j2++) {
            if (bytes[j2] != -1) {
                return false;
            }
        }
        return true;
    }
}
