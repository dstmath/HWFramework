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
import android.system.OsConstants;
import android.telephony.HwVSimManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import com.huawei.chrfile.client.NcMetricConstant;
import huawei.android.app.admin.HwDeviceAdminInfo;
import huawei.android.telephony.wrapper.HuaweiTelephonyManagerWrapper;
import huawei.android.telephony.wrapper.MSimConstantsWrapper;
import huawei.android.view.HwMotionEvent;
import huawei.com.android.internal.widget.HwFragmentContainer;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import libcore.io.IoBridge;
import libcore.io.Libcore;
import libcore.io.Os;

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
    private static boolean mIs3gNetwork;
    private static boolean mMobileEnable;
    private static TelephonyManager mTelephonyManager;
    private boolean mBastetSupport;
    private BroadcastReceiver mConnectivityReceiver;
    private Context mContext;
    private int mHbInterval;
    private int mHbProxyId;
    private byte[] mHbReply;
    private byte[] mHbSend;
    private int mLastSignalAvailable;
    private String mLicense;
    private ArrayList<Pair<Socket, byte[]>> mNrtCache;
    private long mNrtMinTimeout;
    private boolean mReconnSwitch;
    private Socket mSocket;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.bastet.HwBastet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.bastet.HwBastet.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.bastet.HwBastet.<clinit>():void");
    }

    public HwBastet(String license, Socket socket, Handler handler, Context context) {
        this.mNrtMinTimeout = INVALID_TIME;
        this.mBastetSupport = false;
        this.mReconnSwitch = false;
        this.mHbProxyId = -1;
        this.mHbInterval = -1;
        this.mLastSignalAvailable = TYPE_NRT_SEND;
        this.mNrtCache = new ArrayList();
        this.mConnectivityReceiver = new BroadcastReceiver() {
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
        this.mLicense = license;
        this.mSocket = socket;
        mHandler = handler;
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
            System.arraycopy(send, TYPE_NRT_SEND, this.mHbSend, TYPE_NRT_SEND, send.length);
            this.mHbReply = new byte[reply.length];
            System.arraycopy(reply, TYPE_NRT_SEND, this.mHbReply, TYPE_NRT_SEND, reply.length);
            setHeartbeatProxy();
        }
    }

    public void pauseHeartbeat() throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || !this.mBastetSupport || mIBastetManager == null) {
                throw new Exception();
            }
            mIBastetManager.stopBastetProxy(this.mHbProxyId);
        }
    }

    public void resumeHeartbeat() throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || !this.mBastetSupport || mIBastetManager == null) {
                throw new Exception();
            }
            mIBastetManager.startBastetProxy(this.mHbProxyId);
        }
    }

    public void sendNrtData(byte[] data, long timeout, Socket socket) throws Exception {
        synchronized (this) {
            if (this.mHbProxyId <= 0 || !this.mBastetSupport || mIBastetManager == null) {
                sendData(socket, data, TYPE_NRT_SEND);
            } else {
                this.mNrtCache.add(new Pair(socket, data));
                long elapsedTimeout = timeout + SystemClock.elapsedRealtime();
                if (elapsedTimeout < this.mNrtMinTimeout || this.mNrtMinTimeout == INVALID_TIME) {
                    this.mNrtMinTimeout = elapsedTimeout;
                    mIBastetManager.setNrtTime(this.mHbProxyId, timeout);
                }
            }
        }
    }

    public void sendHrtData(byte[] data, Socket socket) throws IOException {
        sendData(socket, data, data.length <= MAX_SEND_HRT_LENGTH ? BASTET_MSG_HRT : TYPE_NRT_SEND);
    }

    public void sendLpwData(byte[] data, Socket socket) throws IOException {
        int i = (!mIs3gNetwork || data.length > MAX_SEND_LPW_LENGTH) ? TYPE_NRT_SEND : BASTET_MSG_LPW;
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
                case MSimConstantsWrapper.SUBNone /*-1*/:
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
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
            case HwMotionEvent.TOOL_TYPE_BEZEL /*8*/:
            case HwDeviceAdminInfo.USES_POLICY_SET_MDM_EMAIL /*9*/:
            case HuaweiTelephonyManagerWrapper.SINGLE_MODE_SIM_CARD /*10*/:
            case NcMetricConstant.WIFI_METRIC_ID /*15*/:
            case HwVSimManager.NETWORK_TYPE_TDS /*17*/:
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
            i = TYPE_NRT_SEND;
        }
        IoBridge.sendto(fd, data, TYPE_NRT_SEND, length, i, null, TYPE_NRT_SEND);
    }

    private void setReconnEnable(boolean enable) throws Exception {
        int i = TYPE_NRT_SEND;
        if (this.mSocket == null || this.mHbProxyId <= 0 || mIBastetManager == null) {
            throw new Exception();
        }
        Os os = Libcore.os;
        FileDescriptor fd = getFd(this.mSocket);
        int i2 = OsConstants.IPPROTO_TCP;
        int i3 = BASTET_TCP_RECONN;
        if (enable) {
            i = TYPE_AP_NORMAL_CLS;
        }
        os.setsockoptInt(fd, i2, i3, i);
        mIBastetManager.setReconnEnable(this.mHbProxyId, enable);
    }

    private void prepareHeartbeatProxy() {
        synchronized (this) {
            if (this.mHbProxyId <= 0) {
                try {
                    this.mHbProxyId = mIBastetManager.prepareHeartbeatProxy(getIntFd(), this.mIBastetListener);
                } catch (Exception e) {
                    this.mContext.unregisterReceiver(this.mConnectivityReceiver);
                }
            }
            this.mBastetSupport = isBastetAvailable();
        }
    }

    private void setHeartbeatProxy() throws Exception {
        if (this.mHbProxyId <= 0 || !this.mBastetSupport || mIBastetManager == null) {
            throw new Exception();
        }
        mIBastetManager.updateRepeatInterval(this.mHbProxyId, this.mHbInterval);
        mIBastetManager.setHeartbeatFixedContent(this.mHbProxyId, this.mHbSend, this.mHbReply);
        mIBastetManager.startBastetProxy(this.mHbProxyId);
    }

    private void clearBastetProxy() {
        try {
            if (this.mHbProxyId > 0 && mIBastetManager != null) {
                mIBastetManager.clearProxyById(this.mHbProxyId);
            }
            this.mHbProxyId = -1;
            this.mContext.unregisterReceiver(this.mConnectivityReceiver);
        } catch (RemoteException e) {
        } catch (IllegalArgumentException e2) {
        }
    }

    private void handleProxySocketState(int state) {
        switch (state) {
            case TYPE_NRT_SEND /*0*/:
                synchronized (this) {
                    if (!this.mReconnSwitch) {
                        clearBastetProxy();
                    }
                    break;
                }
                clearAllData();
                this.mNrtMinTimeout = INVALID_TIME;
                sendMessage(TYPE_AP_NORMAL_EST);
            case TYPE_AP_NORMAL_CLS /*1*/:
                clearBastetProxy();
                clearAllData();
                this.mNrtMinTimeout = INVALID_TIME;
            case TYPE_AP_NORMAL_EST /*2*/:
                sendMessage(TYPE_AP_NORMAL_CLS);
            case TYPE_RECONN_BREAK /*254*/:
                sendMessage(8);
            case TYPE_BEST_CONN_POINT /*255*/:
                sendMessage(7);
                clearBastetProxy();
            default:
        }
    }

    private void handleHeartbeatCycle(int cycle) {
        sendMessage(4, cycle);
    }

    private void handleNetworkQuality(int signalLevel) {
        int isAvailable = TYPE_NRT_SEND;
        switch (signalLevel) {
            case TYPE_NRT_SEND /*0*/:
                isAvailable = TYPE_NRT_SEND;
                break;
            case TYPE_AP_NORMAL_CLS /*1*/:
            case TYPE_AP_NORMAL_EST /*2*/:
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                isAvailable = TYPE_AP_NORMAL_CLS;
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
            case TYPE_NRT_SEND /*0*/:
                sendAllData();
                this.mNrtMinTimeout = INVALID_TIME;
            default:
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendAllData() {
        synchronized (this) {
            while (true) {
                if (this.mNrtCache.isEmpty()) {
                } else {
                    try {
                        sendData(((Pair) this.mNrtCache.get(TYPE_NRT_SEND)).first, ((Pair) this.mNrtCache.get(TYPE_NRT_SEND)).second, TYPE_NRT_SEND);
                    } catch (IOException e) {
                    }
                    this.mNrtCache.remove(TYPE_NRT_SEND);
                }
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
            val = DEFAULT_MSG_HRT;
        }
        BASTET_MSG_HRT = val;
        val = getOsConstantsField("MSG_LPW");
        if (val <= 0) {
            val = DEFAULT_MSG_LPW;
        }
        BASTET_MSG_LPW = val;
        val = getOsConstantsField("TCP_RECONN");
        if (val <= 0) {
            val = DEFAULT_TCP_RECONN;
        }
        BASTET_TCP_RECONN = val;
    }
}
