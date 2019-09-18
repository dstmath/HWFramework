package com.huawei.android.bastet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import libcore.io.IoBridge;

public class HwBastet extends BastetManager {
    private static int BASTET_MSG_HRT = 0;
    private static int BASTET_MSG_LPW = 0;
    private static final String BASTET_SERVICE = "BastetService";
    private static int BASTET_TCP_RECONN = 0;
    private static final int DEFAULT_MSG_HRT = 262144;
    private static final int DEFAULT_MSG_LPW = 524288;
    private static final int DEFAULT_TCP_RECONN = 100;
    private static final long INVALID_TIME = -1;
    private static final int MAX_SEND_HRT_LENGTH = 5120;
    private static final int MAX_SEND_LPW_LENGTH = 1024;
    private static final String TAG = "HwBastet";
    private static final int TYPE_AP_ERR_CLS = 0;
    private static final int TYPE_AP_NORMAL_CLS = 1;
    private static final int TYPE_AP_NORMAL_EST = 2;
    private static final int TYPE_BEST_CONN_POINT = 255;
    private static final int TYPE_NRT_SEND = 0;
    private static final int TYPE_RECONN_BREAK = 254;
    /* access modifiers changed from: private */
    public static ConnectivityManager mConnectivityManager;
    /* access modifiers changed from: private */
    public static boolean mIs3gNetwork = false;
    /* access modifiers changed from: private */
    public static boolean mMobileEnable = false;
    private static TelephonyManager mTelephonyManager = null;
    private boolean mBastetSupport = false;
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                NetworkInfo info = HwBastet.mConnectivityManager.getActiveNetworkInfo();
                boolean z = false;
                if (info == null || !info.isAvailable()) {
                    boolean unused = HwBastet.mMobileEnable = false;
                    return;
                }
                if (info.getType() == 0) {
                    z = true;
                }
                boolean unused2 = HwBastet.mMobileEnable = z;
                boolean unused3 = HwBastet.mIs3gNetwork = HwBastet.this.is3gNetwork(info.getSubtype());
            }
        }
    };
    private Context mContext;
    private int mHbInterval = -1;
    private int mHbProxyId = -1;
    private byte[] mHbReply;
    private byte[] mHbSend;
    private int mLastSignalAvailable = 0;
    private ArrayList<Pair<Socket, byte[]>> mNrtCache = new ArrayList<>();
    private long mNrtMinTimeout = INVALID_TIME;
    private boolean mReconnSwitch = false;
    private Socket mSocket;

    public HwBastet(String license, Socket socket, Handler handler, Context context) {
        this.mSocket = socket;
        this.mHandler = handler;
        this.mContext = context;
        initConnectivityService();
        prepareHeartbeatProxy();
        getBastetConstants();
    }

    public void reconnectSwitch(boolean enable) throws Exception {
        synchronized (this) {
            if (this.mReconnSwitch != enable) {
                setReconnEnable(enable);
                this.mReconnSwitch = enable;
            }
        }
    }

    public void setAolHeartbeat(int interval, byte[] send, byte[] reply) throws Exception {
        synchronized (this) {
            if (send.length == 0 || reply.length == 0 || interval <= 0) {
                Log.e(TAG, "Invalid heartbeat parameters");
                throw new Exception();
            }
            if (interval > 3) {
                this.mHbInterval = 3;
            } else {
                this.mHbInterval = interval;
            }
            this.mHbSend = new byte[send.length];
            System.arraycopy(send, 0, this.mHbSend, 0, send.length);
            this.mHbReply = new byte[reply.length];
            System.arraycopy(reply, 0, this.mHbReply, 0, reply.length);
            setHeartbeatProxy();
        }
    }

    public void pauseHeartbeat() throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || !this.mBastetSupport || this.mIBastetManager == null) {
                throw new Exception();
            }
            this.mIBastetManager.stopBastetProxy(this.mHbProxyId);
        }
    }

    public void resumeHeartbeat() throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || !this.mBastetSupport || this.mIBastetManager == null) {
                throw new Exception();
            }
            this.mIBastetManager.startBastetProxy(this.mHbProxyId);
        }
    }

    public void sendNrtData(byte[] data, long timeout, Socket socket) throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || !this.mBastetSupport || this.mIBastetManager == null) {
                sendData(socket, data, 0);
            } else {
                this.mNrtCache.add(new Pair(socket, data));
                long elapsedTimeout = timeout + SystemClock.elapsedRealtime();
                if (elapsedTimeout < this.mNrtMinTimeout || this.mNrtMinTimeout == INVALID_TIME) {
                    this.mNrtMinTimeout = elapsedTimeout;
                    this.mIBastetManager.setNrtTime(this.mHbProxyId, timeout);
                }
            }
        }
    }

    public void sendHrtData(byte[] data, Socket socket) throws IOException {
        sendData(socket, data, data.length <= MAX_SEND_HRT_LENGTH ? BASTET_MSG_HRT : 0);
    }

    public void sendLpwData(byte[] data, Socket socket) throws IOException {
        sendData(socket, data, (!mIs3gNetwork || data.length > 1024) ? 0 : BASTET_MSG_LPW);
    }

    /* access modifiers changed from: protected */
    public void handleProxyMessage(int proxyId, int err, int ext) {
        Log.d(TAG, "HwBastet handleProxyMessage: proxyId=" + proxyId + ", err=" + err + ", ext=" + ext);
        if (proxyId == this.mHbProxyId && err != -10) {
            if (err != -1) {
                switch (err) {
                    case BastetParameters.NETWORK_QUALITY:
                        handleNetworkQuality(ext);
                        break;
                    case BastetParameters.PROXY_SEND_NRT:
                        handleProxySendNrt(ext);
                        break;
                    case BastetParameters.HEARTBEAT_CYCLE:
                        handleHeartbeatCycle(ext);
                        break;
                    case BastetParameters.PROXY_SOCKET_STATE:
                        handleProxySocketState(ext);
                        break;
                }
            } else {
                handleBastetAvailable(ext);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onBastetDied() {
        sendMessage(5);
        sendAllData();
    }

    /* access modifiers changed from: private */
    public boolean is3gNetwork(int type) {
        if (!(type == 3 || type == 15 || type == 17)) {
            switch (type) {
                case 8:
                case 9:
                case 10:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    private void initConnectivityService() {
        mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (mConnectivityManager == null) {
            Log.e(TAG, "Failed to get connectivity service");
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mConnectivityReceiver, filter);
    }

    private int getIntFd() {
        return this.mSocket.getFileDescriptor$().getInt$();
    }

    private FileDescriptor getFd(Socket socket) {
        return (socket != null ? socket : this.mSocket).getFileDescriptor$();
    }

    private void sendData(Socket socket, byte[] data, int flags) throws IOException {
        IoBridge.sendto(getFd(socket), data, 0, data.length, mMobileEnable ? flags : 0, null, 0);
    }

    private void setReconnEnable(boolean enable) throws Exception {
        if (this.mSocket == null || this.mHbProxyId <= 0 || this.mIBastetManager == null) {
            throw new Exception();
        }
        this.mIBastetManager.setReconnEnable(this.mHbProxyId, enable);
    }

    private void prepareHeartbeatProxy() {
        synchronized (this) {
            if (this.mHbProxyId <= 0) {
                try {
                    this.mHbProxyId = this.mIBastetManager.prepareHeartbeatProxy(getIntFd(), this.mIBastetListener);
                } catch (Exception e) {
                    this.mContext.unregisterReceiver(this.mConnectivityReceiver);
                }
            }
            this.mBastetSupport = isBastetAvailable();
        }
    }

    private void setHeartbeatProxy() throws Exception {
        if (this.mHbProxyId <= 0 || !this.mBastetSupport || this.mIBastetManager == null) {
            throw new Exception();
        }
        this.mIBastetManager.updateRepeatInterval(this.mHbProxyId, this.mHbInterval);
        this.mIBastetManager.setHeartbeatFixedContent(this.mHbProxyId, this.mHbSend, this.mHbReply);
        this.mIBastetManager.startBastetProxy(this.mHbProxyId);
    }

    private void clearBastetProxy() {
        try {
            if (this.mHbProxyId > 0 && this.mIBastetManager != null) {
                this.mIBastetManager.clearProxyById(this.mHbProxyId);
            }
            this.mHbProxyId = -1;
            this.mContext.unregisterReceiver(this.mConnectivityReceiver);
        } catch (RemoteException | IllegalArgumentException e) {
        }
    }

    private void handleProxySocketState(int state) {
        switch (state) {
            case 0:
                synchronized (this) {
                    if (!this.mReconnSwitch) {
                        clearBastetProxy();
                    }
                }
                clearAllData();
                this.mNrtMinTimeout = INVALID_TIME;
                sendMessage(2);
                return;
            case 1:
                clearBastetProxy();
                clearAllData();
                this.mNrtMinTimeout = INVALID_TIME;
                return;
            case 2:
                sendMessage(1);
                return;
            default:
                switch (state) {
                    case TYPE_RECONN_BREAK /*254*/:
                        sendMessage(8);
                        return;
                    case 255:
                        sendMessage(7);
                        clearBastetProxy();
                        return;
                    default:
                        return;
                }
        }
    }

    private void handleHeartbeatCycle(int cycle) {
        sendMessage(4, cycle);
    }

    private void handleNetworkQuality(int signalLevel) {
        int isAvailable = 0;
        switch (signalLevel) {
            case 0:
                isAvailable = 0;
                break;
            case 1:
            case 2:
            case 3:
                isAvailable = 1;
                break;
        }
        if (this.mLastSignalAvailable != isAvailable) {
            this.mLastSignalAvailable = isAvailable;
            sendMessage(3, isAvailable);
        }
    }

    private void handleBastetAvailable(int available) {
        synchronized (this) {
            Log.d(TAG, "mBastetSupport: " + this.mBastetSupport + ", available: " + available);
            this.mBastetSupport = available != 0;
            if (!this.mBastetSupport) {
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
        int value;
        try {
            value = Class.forName("android.system.OsConstants").getDeclaredField(filedName).getInt(null);
        } catch (Exception e) {
            value = -1;
            e.printStackTrace();
        }
        Log.d(TAG, "OsConstants " + filedName + ": " + value);
        return value;
    }

    private void getBastetConstants() {
        int val = getOsConstantsField("MSG_HRT");
        if (val <= 0) {
            val = 262144;
        }
        BASTET_MSG_HRT = val;
        int val2 = getOsConstantsField("MSG_LPW");
        if (val2 <= 0) {
            val2 = 524288;
        }
        BASTET_MSG_LPW = val2;
        int val3 = getOsConstantsField("TCP_RECONN");
        if (val3 <= 0) {
            val3 = 100;
        }
        BASTET_TCP_RECONN = val3;
    }
}
