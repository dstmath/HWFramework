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
    private static ConnectivityManager mConnectivityManager;
    private static boolean mIs3gNetwork = false;
    private static boolean mMobileEnable = false;
    private static TelephonyManager mTelephonyManager = null;
    private boolean mBastetSupport = false;
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean z = false;
            String action = intent.getAction();
            if (action != null && action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                NetworkInfo info = HwBastet.mConnectivityManager.getActiveNetworkInfo();
                if (info == null || !info.isAvailable()) {
                    HwBastet.mMobileEnable = false;
                    return;
                }
                if (info.getType() == 0) {
                    z = true;
                }
                HwBastet.mMobileEnable = z;
                HwBastet.mIs3gNetwork = HwBastet.this.is3gNetwork(info.getSubtype());
            }
        }
    };
    private Context mContext;
    private int mHbInterval = -1;
    private int mHbProxyId = -1;
    private byte[] mHbReply;
    private byte[] mHbSend;
    private int mLastSignalAvailable = 0;
    private ArrayList<Pair<Socket, byte[]>> mNrtCache = new ArrayList();
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
            if (this.mReconnSwitch == enable) {
                return;
            }
            setReconnEnable(enable);
            this.mReconnSwitch = enable;
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
            if (this.mHbProxyId <= 0 || (this.mBastetSupport ^ 1) != 0 || this.mIBastetManager == null) {
                throw new Exception();
            }
            this.mIBastetManager.stopBastetProxy(this.mHbProxyId);
        }
    }

    public void resumeHeartbeat() throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || (this.mBastetSupport ^ 1) != 0 || this.mIBastetManager == null) {
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
        int i = (!mIs3gNetwork || data.length > 1024) ? 0 : BASTET_MSG_LPW;
        sendData(socket, data, i);
    }

    protected void handleProxyMessage(int proxyId, int err, int ext) {
        Log.d(TAG, "HwBastet handleProxyMessage: proxyId=" + proxyId + ", err=" + err + ", ext=" + ext);
        if (proxyId == this.mHbProxyId) {
            switch (err) {
                case BastetParameters.NETWORK_QUALITY /*-19*/:
                    handleNetworkQuality(ext);
                    break;
                case BastetParameters.PROXY_SEND_NRT /*-18*/:
                    handleProxySendNrt(ext);
                    break;
                case BastetParameters.HEARTBEAT_CYCLE /*-17*/:
                    handleHeartbeatCycle(ext);
                    break;
                case BastetParameters.PROXY_SOCKET_STATE /*-16*/:
                    handleProxySocketState(ext);
                    break;
                case -1:
                    handleBastetAvailable(ext);
                    break;
            }
        }
    }

    protected void onBastetDied() {
        sendMessage(5);
        sendAllData();
    }

    private boolean is3gNetwork(int type) {
        switch (type) {
            case 3:
            case 8:
            case 9:
            case 10:
            case 15:
            case 17:
                return true;
            default:
                return false;
        }
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
        int i;
        FileDescriptor fd = getFd(socket);
        int length = data.length;
        if (mMobileEnable) {
            i = flags;
        } else {
            i = 0;
        }
        IoBridge.sendto(fd, data, 0, length, i, null, 0);
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
        return;
    }

    private void setHeartbeatProxy() throws Exception {
        if (this.mHbProxyId <= 0 || (this.mBastetSupport ^ 1) != 0 || this.mIBastetManager == null) {
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
        } catch (RemoteException e) {
        } catch (IllegalArgumentException e2) {
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
        boolean z = false;
        synchronized (this) {
            Log.d(TAG, "mBastetSupport: " + this.mBastetSupport + ", available: " + available);
            if (available != 0) {
                z = true;
            }
            this.mBastetSupport = z;
            if (!this.mBastetSupport) {
                sendMessage(5);
            }
        }
    }

    private void handleProxySendNrt(int state) {
        switch (state) {
            case 0:
                sendAllData();
                this.mNrtMinTimeout = INVALID_TIME;
                return;
            default:
                return;
        }
    }

    private void sendAllData() {
        synchronized (this) {
            while (!this.mNrtCache.isEmpty()) {
                try {
                    sendData(((Pair) this.mNrtCache.get(0)).first, ((Pair) this.mNrtCache.get(0)).second, 0);
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
        val = getOsConstantsField("MSG_LPW");
        if (val <= 0) {
            val = 524288;
        }
        BASTET_MSG_LPW = val;
        val = getOsConstantsField("TCP_RECONN");
        if (val <= 0) {
            val = 100;
        }
        BASTET_TCP_RECONN = val;
    }
}
