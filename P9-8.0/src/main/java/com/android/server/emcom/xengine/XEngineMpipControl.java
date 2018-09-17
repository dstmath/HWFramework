package com.android.server.emcom.xengine;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.emcom.EmcomThread;
import com.android.server.emcom.daemon.DaemonCommand;
import com.android.server.emcom.policy.HicomPolicyManager;
import com.android.server.emcom.util.EMCOMConstants;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

public class XEngineMpipControl implements EMCOMConstants {
    private static final int CALLING_DELAYED = 5000;
    private static final String DATA_STATE = "DISCONNECTED";
    private static final int DATA_STATUS_AVAILABLE = 0;
    private static final int DATA_STATUS_ONLOST = 1;
    private static final int DATA_STATUS_UNAVAILABLE = 2;
    private static final int MAX_MPIP_ACTIVE_TIMES = 5;
    private static final String MPIP_APN_FAILED_REASON = "apnFailed";
    private static final String MPIP_APN_TYPE = "internaldefault";
    public static final int MPIP_FORBIDDEN = 2;
    public static final int MPIP_PENDING = 3;
    public static final int MPIP_START = 0;
    public static final int MPIP_STOP = 1;
    private static final long ONE_HOUR_TimeMillis = 3600000;
    private static final String OPERATOR_CHINA = "460";
    private static final int SUB_0 = 0;
    private static final int SUB_1 = 1;
    private static final String TAG = "XEngineMpipControl";
    private static volatile XEngineMpipControl xEngineMpipControl;
    private ArrayList<IMpipStatusCallback> mCallbacks = new ArrayList();
    private volatile ConnectivityManager mConnMnger;
    private Context mContext;
    private DaemonCommand mDaemonCommand;
    private int[] mDataConnectState;
    private Handler mHandler;
    private String mIfName;
    private boolean[] mIsInCalling;
    private boolean mMpipAppListNotEmpty;
    private int mMpipPdnStatus;
    private NetworkCallback[] mNetworkCallback;
    private NetworkRequest mNetworkRequest;
    private PhoneStateListener[] mPhoneStateListener;
    private int[] mSubDataStatus;
    private int mSubNumber;
    private TelephonyManager mTeleMnger;
    private Queue<Long> mpipActiveTimeQueue;

    public interface IMpipStatusCallback {
        void onMpipStatusChanged(int i);
    }

    private class MpipControlV1Handler extends Handler {
        public MpipControlV1Handler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int subId;
            switch (msg.what) {
                case 0:
                    subId = msg.arg1;
                    Log.d(XEngineMpipControl.TAG, "MESSAGE_CALL_STATE_IDLE received the subId is " + subId + " ,mDataConnectState[subId] is " + XEngineMpipControl.this.mDataConnectState[subId]);
                    if (!XEngineMpipControl.this.mIsInCalling[subId] && XEngineMpipControl.this.mDataConnectState[subId] != 2) {
                        XEngineMpipControl.this.stopMpipFunction(subId, 1);
                        return;
                    }
                    return;
                case 1:
                    subId = msg.arg1;
                    Log.d(XEngineMpipControl.TAG, "MESSAGE_TO_HANDLE_DATA_DISCONNECTED  received the subId = " + subId + " ,mIsInCalling[subId] is " + XEngineMpipControl.this.mIsInCalling[subId]);
                    if (!XEngineMpipControl.this.mIsInCalling[subId] && XEngineMpipControl.this.mDataConnectState[subId] != 2) {
                        XEngineMpipControl.this.stopMpipFunction(subId, 1);
                        return;
                    }
                    return;
                default:
                    Log.e(XEngineMpipControl.TAG, "Unknown message what for EmcomHandler");
                    return;
            }
        }
    }

    private class NetworkRequestCallback extends NetworkCallback {
        private int mSubId = 0;

        public NetworkRequestCallback(int subId) {
            this.mSubId = subId;
        }

        public void onAvailable(Network network) {
            super.onAvailable(network);
            XEngineMpipControl.this.mSubDataStatus[this.mSubId] = 0;
            Log.d(XEngineMpipControl.TAG, "NetworkCallbackListener.onAvailable: network=" + network + " the card is: " + this.mSubId);
            synchronized (XEngineMpipControl.this) {
                XEngineMpipControl.this.setMpipPdnStatus(0);
                LinkProperties lp = XEngineMpipControl.this.mConnMnger.getLinkProperties(network);
                if (lp != null) {
                    XEngineMpipControl.this.mIfName = lp.getInterfaceName();
                    if (XEngineMpipControl.this.mMpipAppListNotEmpty && XEngineMpipControl.this.mIfName != null) {
                        XEngineMpipControl.this.sendStartMpipToDaemond(XEngineMpipControl.this.mIfName);
                    }
                }
            }
        }

        public void onLost(Network network) {
            super.onLost(network);
            XEngineMpipControl.this.mSubDataStatus[this.mSubId] = 1;
            Log.d(XEngineMpipControl.TAG, "NetworkCallbackListener.onLost: network=" + network);
            synchronized (XEngineMpipControl.this) {
                XEngineMpipControl.this.stopMpipFunction(this.mSubId, 1);
            }
        }

        public void onUnavailable() {
            super.onUnavailable();
            XEngineMpipControl.this.mSubDataStatus[this.mSubId] = 2;
            Log.d(XEngineMpipControl.TAG, "NetworkCallbackListener.onUnavailable");
            synchronized (XEngineMpipControl.this) {
                XEngineMpipControl.this.stopMpipFunction(this.mSubId, 2);
                XEngineMpipControl.this.unregisterPhoneListener();
            }
        }
    }

    private XEngineMpipControl(Context context) {
        this.mContext = context;
        this.mHandler = new MpipControlV1Handler(EmcomThread.getInstanceLooper());
        this.mSubNumber = TelephonyManager.getDefault().getPhoneCount();
        if (this.mSubNumber < 2) {
            this.mSubNumber = 2;
        }
        this.mNetworkCallback = new NetworkRequestCallback[this.mSubNumber];
        this.mConnMnger = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mNetworkRequest = null;
        this.mMpipPdnStatus = 1;
        this.mSubDataStatus = new int[this.mSubNumber];
        this.mIsInCalling = new boolean[this.mSubNumber];
        this.mDataConnectState = new int[this.mSubNumber];
        for (int i = 0; i < this.mSubNumber; i++) {
            this.mSubDataStatus[i] = 2;
            this.mIsInCalling[i] = false;
            this.mDataConnectState[i] = -1;
        }
        this.mMpipAppListNotEmpty = false;
        this.mIfName = null;
        this.mDaemonCommand = DaemonCommand.getInstance();
        this.mTeleMnger = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mpipActiveTimeQueue = new LinkedList();
    }

    public static XEngineMpipControl getInstance(Context context) {
        if (xEngineMpipControl == null) {
            synchronized (XEngineMpipControl.class) {
                if (xEngineMpipControl == null) {
                    xEngineMpipControl = new XEngineMpipControl(context);
                }
            }
        }
        return xEngineMpipControl;
    }

    private void setMpipPdnStatus(int status) {
        Log.d(TAG, "setMpipPdnStatus." + status);
        if (2 == this.mMpipPdnStatus) {
            Log.d(TAG, "setMpipPdnStatus current state is forbidden, not allow not change");
            return;
        }
        this.mMpipPdnStatus = status;
        switch (status) {
            case 0:
            case 1:
            case 2:
                respondCallbackFunctions(status);
                break;
        }
    }

    public void onNotifyInterfaceFailure() {
        Log.d(TAG, "onNotifyInterfaceFailure.");
    }

    private boolean isCurrentNetWorkSuited(int networkType, int subId) {
        if (isCDMASimCard(subId)) {
            Log.d(TAG, "mpip doesn't support cdma card");
            return false;
        }
        String strOperator = this.mTeleMnger.getNetworkOperator(SubscriptionManager.getDefaultDataSubscriptionId());
        if (strOperator == null) {
            Log.i(TAG, "strOperator is null.");
            return false;
        } else if (strOperator.length() < "460".length()) {
            Log.d(TAG, "strOperator is too short.");
            return false;
        } else if (strOperator.substring(0, "460".length()).equals("460") && (networkType == 13 || networkType == 19)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCDMASimCard(int subId) {
        return HwTelephonyManagerInner.getDefault().isCDMASimCard(subId);
    }

    private PhoneStateListener getPhoneStateListener(final int sub) {
        return new PhoneStateListener(Integer.valueOf(sub)) {
            private int mSub = sub;

            public void onDataConnectionStateChanged(int state, int networkType) {
                switch (state) {
                    case 0:
                        XEngineMpipControl.this.mDataConnectState[this.mSub] = 0;
                        XEngineMpipControl.this.mHandler.removeMessages(1);
                        Log.d(XEngineMpipControl.TAG, "The ready to stop mpip sim card is: " + this.mSub);
                        if (XEngineMpipControl.this.mSubDataStatus[1 - this.mSub] != 0) {
                            XEngineMpipControl.this.sendStopMpipToDaemond();
                        }
                        if (XEngineMpipControl.this.mIsInCalling[this.mSub]) {
                            Log.d(XEngineMpipControl.TAG, " The phone is in calling, do not deactive second pdn");
                            return;
                        }
                        Message msg = XEngineMpipControl.this.mHandler.obtainMessage();
                        msg.what = 1;
                        msg.arg1 = this.mSub;
                        XEngineMpipControl.this.mHandler.sendMessageDelayed(msg, 5000);
                        return;
                    case 2:
                        XEngineMpipControl.this.mDataConnectState[this.mSub] = 2;
                        Log.d(XEngineMpipControl.TAG, "The ready to start pdn sim card is: " + this.mSub);
                        if (XEngineMpipControl.this.mMpipPdnStatus == 0 && XEngineMpipControl.this.mSubDataStatus[1 - this.mSub] == 0) {
                            XEngineMpipControl.this.sendStopMpipToDaemond();
                        }
                        if (XEngineMpipControl.this.isCurrentNetWorkSuited(networkType, this.mSub)) {
                            XEngineMpipControl.this.startMpipEstablish(this.mSub);
                            return;
                        }
                        return;
                    default:
                        Log.d(XEngineMpipControl.TAG, "unrelated state");
                        return;
                }
            }

            public void onCallStateChanged(int state, String incomingNumber) {
                Log.d(XEngineMpipControl.TAG, "CustomPhoneStateListener state: " + state + " incomingNumber: " + incomingNumber);
                switch (state) {
                    case 0:
                        Log.d(XEngineMpipControl.TAG, "phone state CALL_STATE_IDLE");
                        XEngineMpipControl.this.mIsInCalling[this.mSub] = false;
                        XEngineMpipControl.this.mHandler.removeMessages(0);
                        if (XEngineMpipControl.this.mDataConnectState[this.mSub] == 2) {
                            XEngineMpipControl.this.sendStartMpipToDaemond(XEngineMpipControl.this.mIfName);
                            return;
                        }
                        Message msg = XEngineMpipControl.this.mHandler.obtainMessage();
                        msg.what = 0;
                        msg.arg1 = this.mSub;
                        XEngineMpipControl.this.mHandler.sendMessageDelayed(msg, 5000);
                        return;
                    case 1:
                        Log.d(XEngineMpipControl.TAG, "phone state CALL_STATE_RINGING");
                        XEngineMpipControl.this.mIsInCalling[this.mSub] = true;
                        return;
                    case 2:
                        Log.d(XEngineMpipControl.TAG, "phone state CALL_STATE_OFFHOOK");
                        XEngineMpipControl.this.mIsInCalling[this.mSub] = true;
                        return;
                    default:
                        Log.e(XEngineMpipControl.TAG, "error call state");
                        return;
                }
            }
        };
    }

    private void registerPhoneListener() {
        this.mPhoneStateListener = new PhoneStateListener[this.mSubNumber];
        if (this.mTeleMnger == null) {
            Log.d(TAG, "registerPhoneListener:mPhone = null");
            return;
        }
        for (int i = 0; i < this.mSubNumber; i++) {
            this.mPhoneStateListener[i] = getPhoneStateListener(i);
            this.mTeleMnger.listen(this.mPhoneStateListener[i], 97);
        }
    }

    private void unregisterPhoneListener() {
        this.mSubNumber = TelephonyManager.getDefault().getPhoneCount();
        if (this.mTeleMnger == null) {
            Log.d(TAG, "unregisterPhoneListener:mPhone = null");
            return;
        }
        for (int i = 0; i < this.mSubNumber; i++) {
            if (this.mPhoneStateListener[i] != null) {
                this.mTeleMnger.listen(this.mPhoneStateListener[i], 0);
            }
        }
    }

    public void updateMpipUidList(ArrayList<Integer> UidList) {
        int uidlistsize = UidList.size();
        int[] UidRange = new int[uidlistsize];
        for (int i = 0; i < uidlistsize; i++) {
            UidRange[i] = ((Integer) UidList.get(i)).intValue();
            Log.d(TAG, "the supported multi-path app is: " + UidRange[i]);
        }
        if (uidlistsize == 0) {
            Log.d(TAG, "Received Mpip Uid List is empty from HicomPolicyManager ");
            this.mMpipAppListNotEmpty = false;
        } else {
            this.mMpipAppListNotEmpty = true;
        }
        sendMpipAppListToDaemond(UidRange);
        if (this.mMpipPdnStatus == 0 && this.mIfName != null && this.mMpipAppListNotEmpty) {
            sendStartMpipToDaemond(this.mIfName);
        }
    }

    public void startMpip() {
        if (this.mMpipPdnStatus == 2) {
            Log.d(TAG, "MPIP can not use unless reboot to retry.");
        } else if (isVpnConnected()) {
            Log.d(TAG, "MPIP can not use because VPN is connected.");
            setMpipPdnStatus(2);
        } else {
            registerPhoneListener();
        }
    }

    public void handleVpnStatus(NetworkInfo networkInfo) {
        if (networkInfo != null && networkInfo.getType() == 17) {
            Log.d(TAG, "vpn started, the Mpip should be disabled if not WIFI connected.");
            if (!HicomPolicyManager.getInstance().isWifiConnected()) {
                int subId = -1;
                if (this.mSubDataStatus[0] == 0) {
                    subId = 0;
                } else if (this.mSubDataStatus[1] == 0) {
                    subId = 1;
                } else {
                    Log.d(TAG, "data disconnected, do not need to disable Mpip.");
                }
                if (!(subId == -1 || this.mMpipPdnStatus == 2)) {
                    stopMpipFunction(subId, 2);
                }
            }
        }
    }

    public static boolean isVpnConnected() {
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                for (NetworkInterface intf : Collections.list(niList)) {
                    if (intf.isUp() && intf.getInterfaceAddresses().size() != 0 && "tun0".equals(intf.getName())) {
                        return true;
                    }
                }
            }
        } catch (Throwable th) {
            Log.e(TAG, "network interface get failed.");
        }
        return false;
    }

    public void onDataStateChanged(String apnType, String dataState, String reason, int subId) {
        if (subId >= 0 && subId <= 1) {
            if (apnType == null || dataState == null || reason == null) {
                Log.d(TAG, "Invalid param");
                return;
            }
            if (apnType.equals(MPIP_APN_TYPE) && dataState.equals(DATA_STATE) && reason.equals(MPIP_APN_FAILED_REASON)) {
                this.mSubDataStatus[subId] = 2;
                Log.d(TAG, "onDataStateChanged.failed");
                synchronized (this) {
                    Log.d(TAG, "send stop mpip to daemon after pdn setup failure, and report forbidden to HicomPolicyManager ");
                    stopMpipFunction(subId, 2);
                    unregisterPhoneListener();
                }
            }
        }
    }

    private void startMpipEstablish(int SubId) {
        if (this.mMpipPdnStatus == 3 || this.mMpipPdnStatus == 0) {
            Log.d(TAG, "startMpipEstablish mMpipPdnStatus is pending or start, the card is : " + SubId);
        } else if (this.mMpipPdnStatus == 2) {
            Log.d(TAG, "MPIP is Forbiden ");
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            if (this.mpipActiveTimeQueue != null) {
                if (this.mpipActiveTimeQueue.size() < 5) {
                    Log.d(TAG, "mpipActiveTimeQueue.size " + this.mpipActiveTimeQueue.size());
                } else if (currentTimeMillis - ((Long) this.mpipActiveTimeQueue.element()).longValue() < 3600000) {
                    Log.d(TAG, "Active second pdn beyond five times in an hour");
                    return;
                } else if (this.mpipActiveTimeQueue.poll() == null) {
                    Log.e(TAG, "poll element from mpipActiveTimeQueue fail");
                }
                if (!this.mpipActiveTimeQueue.offer(Long.valueOf(currentTimeMillis))) {
                    Log.e(TAG, "add element to mpipActiveTimeQueue fail");
                }
            }
            this.mNetworkCallback[SubId] = new NetworkRequestCallback(SubId);
            this.mNetworkRequest = new Builder().addTransportType(0).addCapability(26).setNetworkSpecifier(Integer.toString(SubId)).build();
            this.mConnMnger.requestNetwork(this.mNetworkRequest, this.mNetworkCallback[SubId], 0, this.mNetworkRequest.legacyType, this.mHandler);
            setMpipPdnStatus(3);
        }
    }

    public void stopMpip() {
        stopMpipFunction(0, 1);
        stopMpipFunction(1, 1);
        unregisterPhoneListener();
    }

    public void stopMpipFunction(int subId, int reason) {
        int subIdOne = subId;
        if (subId < 0 || subId > 1) {
            Log.e(TAG, "Invalid subId when stop MP function.");
            return;
        }
        int subIdAnother;
        if (subId == 0) {
            subIdAnother = 1;
        } else {
            subIdAnother = 0;
        }
        this.mSubDataStatus[subId] = 2;
        if (this.mSubDataStatus[subIdAnother] != 0) {
            if (2 == reason) {
                setMpipPdnStatus(2);
            } else {
                setMpipPdnStatus(1);
            }
            this.mIfName = null;
            sendStopMpipToDaemond();
        }
        if (this.mNetworkCallback[subId] != null) {
            try {
                this.mConnMnger.unregisterNetworkCallback(this.mNetworkCallback[subId]);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Unregister network callback exception");
            }
        } else {
            Log.e(TAG, "XEngineProcessor release failed due to null networkcallback.");
        }
    }

    private void sendMpipAppListToDaemond(int[] UidRange) {
        Message m = this.mHandler.obtainMessage(13);
        if (this.mDaemonCommand != null) {
            if (UidRange.length == 0) {
                sendStopMpipToDaemond();
            }
            Log.d(TAG, "sendmpiplist.");
            this.mDaemonCommand.exeConfigMpip(UidRange, m);
        }
    }

    private void sendStartMpipToDaemond(String ifName) {
        Message m = this.mHandler.obtainMessage(14);
        if (this.mDaemonCommand != null && ifName != null) {
            Log.d(TAG, "send mpip start.");
            this.mDaemonCommand.exeStartMpip(ifName, m);
        }
    }

    private void sendStopMpipToDaemond() {
        Message m = this.mHandler.obtainMessage(15);
        if (this.mDaemonCommand != null) {
            this.mDaemonCommand.exeStopMpip(m);
        }
    }

    public void registerMpipStatusCallback(IMpipStatusCallback callback) {
        if (callback == null || this.mCallbacks == null) {
            Log.e(TAG, "null callback or mCallbackList");
            return;
        }
        this.mCallbacks.add(callback);
        Log.d(TAG, "added a callback function");
    }

    public void unRegisterMpipStatusCallback(IMpipStatusCallback callback) {
        if (callback == null || this.mCallbacks == null) {
            Log.e(TAG, "null callback or mCallbackList");
            return;
        }
        this.mCallbacks.remove(callback);
        Log.d(TAG, "removed a callback function");
    }

    private void respondCallbackFunctions(int status) {
        Log.d(TAG, "notify new result to all callback functions : status= " + status);
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            ((IMpipStatusCallback) this.mCallbacks.get(i)).onMpipStatusChanged(status);
        }
    }
}
