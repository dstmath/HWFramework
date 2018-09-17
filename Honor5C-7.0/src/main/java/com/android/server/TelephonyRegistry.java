package com.android.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PreciseCallState;
import android.telephony.PreciseDataConnectionState;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import android.text.TextUtils;
import android.text.format.Time;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.DefaultPhoneNotifier;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.IPhoneStateListener;
import com.android.internal.telephony.ITelephonyRegistry.Stub;
import com.android.server.am.BatteryStatsService;
import com.android.server.audio.AudioService;
import com.android.server.policy.PhoneWindowManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TelephonyRegistry extends Stub {
    static final int CHECK_PHONE_STATE_PERMISSION_MASK = 224;
    private static final boolean DBG = false;
    private static final boolean DBG_LOC = false;
    static final int ENFORCE_PHONE_STATE_PERMISSION_MASK = 16396;
    private static final int MSG_UPDATE_DEFAULT_SUB = 2;
    private static final int MSG_USER_SWITCHED = 1;
    static final int PRECISE_PHONE_STATE_PERMISSION_MASK = 6144;
    private static final String TAG = "TelephonyRegistry";
    private static final boolean VDBG = false;
    private boolean hasNotifySubscriptionInfoChangedOccurred;
    private LogSSC[] logSSC;
    private final AppOpsManager mAppOps;
    private int mBackgroundCallState;
    private final IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver;
    private boolean[] mCallForwarding;
    private String[] mCallIncomingNumber;
    private int[] mCallState;
    private boolean mCarrierNetworkChangeState;
    private ArrayList<List<CellInfo>> mCellInfo;
    private Bundle[] mCellLocation;
    private ArrayList<String>[] mConnectedApns;
    private final Context mContext;
    private int[] mDataActivity;
    private String[] mDataConnectionApn;
    private LinkProperties[] mDataConnectionLinkProperties;
    private NetworkCapabilities[] mDataConnectionNetworkCapabilities;
    private int[] mDataConnectionNetworkType;
    private boolean[] mDataConnectionPossible;
    private String[] mDataConnectionReason;
    private int[] mDataConnectionState;
    private int mDefaultPhoneId;
    private int mDefaultSubId;
    private int mForegroundCallState;
    private final Handler mHandler;
    private boolean[] mMessageWaiting;
    private int mNumPhones;
    private int mOtaspMode;
    private PreciseCallState mPreciseCallState;
    private PreciseDataConnectionState mPreciseDataConnectionState;
    private final ArrayList<Record> mRecords;
    private final ArrayList<IBinder> mRemoveList;
    private int mRingingCallState;
    private ServiceState[] mServiceState;
    private SignalStrength[] mSignalStrength;
    private VoLteServiceState mVoLteServiceState;
    private int next;

    private static class LogSSC {
        private int mPhoneId;
        private String mS;
        private ServiceState mState;
        private int mSubId;
        private Time mTime;

        private LogSSC() {
        }

        public void set(Time t, String s, int subId, int phoneId, ServiceState state) {
            this.mTime = t;
            this.mS = s;
            this.mSubId = subId;
            this.mPhoneId = phoneId;
            this.mState = state;
        }

        public String toString() {
            return this.mS + " Time " + this.mTime.toString() + " mSubId " + this.mSubId + " mPhoneId " + this.mPhoneId + "  mState " + this.mState;
        }
    }

    private static class Record {
        IBinder binder;
        IPhoneStateListener callback;
        int callerUserId;
        String callingPackage;
        boolean canReadPhoneState;
        int events;
        IOnSubscriptionsChangedListener onSubscriptionsChangedListenerCallback;
        int phoneId;
        int subId;

        private Record() {
            this.subId = -1;
            this.phoneId = -1;
        }

        boolean matchPhoneStateListenerEvent(int events) {
            return (this.callback == null || (this.events & events) == 0) ? TelephonyRegistry.DBG_LOC : true;
        }

        boolean matchOnSubscriptionsChangedListener() {
            return this.onSubscriptionsChangedListenerCallback != null ? true : TelephonyRegistry.DBG_LOC;
        }

        public String toString() {
            return "{callingPackage=" + this.callingPackage + " binder=" + this.binder + " callback=" + this.callback + " onSubscriptionsChangedListenererCallback=" + this.onSubscriptionsChangedListenerCallback + " callerUserId=" + this.callerUserId + " subId=" + this.subId + " phoneId=" + this.phoneId + " events=" + Integer.toHexString(this.events) + " canReadPhoneState=" + this.canReadPhoneState + "}";
        }
    }

    protected TelephonyRegistry(Context context) {
        int i;
        this.mRemoveList = new ArrayList();
        this.mRecords = new ArrayList();
        this.hasNotifySubscriptionInfoChangedOccurred = DBG_LOC;
        this.mOtaspMode = MSG_USER_SWITCHED;
        this.mCellInfo = null;
        this.mVoLteServiceState = new VoLteServiceState();
        this.mDefaultSubId = -1;
        this.mDefaultPhoneId = -1;
        this.mRingingCallState = 0;
        this.mForegroundCallState = 0;
        this.mBackgroundCallState = 0;
        this.mPreciseCallState = new PreciseCallState();
        this.mCarrierNetworkChangeState = DBG_LOC;
        this.mPreciseDataConnectionState = new PreciseDataConnectionState();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TelephonyRegistry.MSG_USER_SWITCHED /*1*/:
                        int numPhones = TelephonyManager.getDefault().getPhoneCount();
                        for (int sub = 0; sub < numPhones; sub += TelephonyRegistry.MSG_USER_SWITCHED) {
                            TelephonyRegistry.this.notifyCellLocationForSubscriber(sub, TelephonyRegistry.this.mCellLocation[sub]);
                        }
                    case TelephonyRegistry.MSG_UPDATE_DEFAULT_SUB /*2*/:
                        int newDefaultPhoneId = msg.arg1;
                        int newDefaultSubId = ((Integer) msg.obj).intValue();
                        synchronized (TelephonyRegistry.this.mRecords) {
                            for (Record r : TelephonyRegistry.this.mRecords) {
                                if (r.subId == Integer.MAX_VALUE) {
                                    TelephonyRegistry.this.checkPossibleMissNotify(r, newDefaultPhoneId);
                                }
                            }
                            TelephonyRegistry.this.handleRemoveListLocked();
                            break;
                        }
                        TelephonyRegistry.this.mDefaultSubId = newDefaultSubId;
                        TelephonyRegistry.this.mDefaultPhoneId = newDefaultPhoneId;
                    default:
                }
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    TelephonyRegistry.this.mHandler.sendMessage(TelephonyRegistry.this.mHandler.obtainMessage(TelephonyRegistry.MSG_USER_SWITCHED, intent.getIntExtra("android.intent.extra.user_handle", 0), 0));
                } else if (action.equals("android.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED")) {
                    Integer newDefaultSubIdObj = new Integer(intent.getIntExtra("subscription", SubscriptionManager.getDefaultSubscriptionId()));
                    int newDefaultPhoneId = intent.getIntExtra("slot", SubscriptionManager.getPhoneId(TelephonyRegistry.this.mDefaultSubId));
                    if (!TelephonyRegistry.this.validatePhoneId(newDefaultPhoneId)) {
                        return;
                    }
                    if (!newDefaultSubIdObj.equals(Integer.valueOf(TelephonyRegistry.this.mDefaultSubId)) || newDefaultPhoneId != TelephonyRegistry.this.mDefaultPhoneId) {
                        TelephonyRegistry.this.mHandler.sendMessage(TelephonyRegistry.this.mHandler.obtainMessage(TelephonyRegistry.MSG_UPDATE_DEFAULT_SUB, newDefaultPhoneId, 0, newDefaultSubIdObj));
                    }
                }
            }
        };
        this.logSSC = new LogSSC[10];
        this.next = 0;
        CellLocation location = CellLocation.getEmpty();
        this.mContext = context;
        this.mBatteryStats = BatteryStatsService.getService();
        int numPhones = VSimTelephonyRegistry.processVSimPhoneNumbers(TelephonyManager.getDefault().getPhoneCount());
        this.mNumPhones = numPhones;
        this.mCallState = new int[numPhones];
        this.mDataActivity = new int[numPhones];
        this.mConnectedApns = new ArrayList[numPhones];
        this.mDataConnectionState = new int[numPhones];
        this.mDataConnectionNetworkType = new int[numPhones];
        this.mCallIncomingNumber = new String[numPhones];
        this.mServiceState = new ServiceState[numPhones];
        this.mSignalStrength = new SignalStrength[numPhones];
        this.mMessageWaiting = new boolean[numPhones];
        this.mDataConnectionPossible = new boolean[numPhones];
        this.mDataConnectionReason = new String[numPhones];
        this.mDataConnectionApn = new String[numPhones];
        this.mCallForwarding = new boolean[numPhones];
        this.mCellLocation = new Bundle[numPhones];
        this.mDataConnectionLinkProperties = new LinkProperties[numPhones];
        this.mDataConnectionNetworkCapabilities = new NetworkCapabilities[numPhones];
        this.mCellInfo = new ArrayList();
        for (i = 0; i < numPhones; i += MSG_USER_SWITCHED) {
            this.mCallState[i] = 0;
            this.mDataActivity[i] = 0;
            this.mConnectedApns[i] = new ArrayList();
            this.mDataConnectionState[i] = -1;
            this.mCallIncomingNumber[i] = "";
            this.mServiceState[i] = new ServiceState();
            this.mSignalStrength[i] = new SignalStrength();
            this.mMessageWaiting[i] = DBG_LOC;
            this.mCallForwarding[i] = DBG_LOC;
            this.mDataConnectionPossible[i] = DBG_LOC;
            this.mDataConnectionReason[i] = "";
            this.mDataConnectionApn[i] = "";
            this.mCellLocation[i] = new Bundle();
            this.mCellInfo.add(i, null);
        }
        if (location != null) {
            for (i = 0; i < numPhones; i += MSG_USER_SWITCHED) {
                location.fillInNotifierBundle(this.mCellLocation[i]);
            }
        }
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
    }

    public void systemRunning() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED");
        log("systemRunning register for intents");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public void addOnSubscriptionsChangedListener(String callingPackage, IOnSubscriptionsChangedListener callback) {
        int callerUserId = UserHandle.getCallingUserId();
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "addOnSubscriptionsChangedListener");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "addOnSubscriptionsChangedListener");
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                return;
            }
        }
        synchronized (this.mRecords) {
            Record r;
            IBinder b = callback.asBinder();
            int N = this.mRecords.size();
            for (int i = 0; i < N; i += MSG_USER_SWITCHED) {
                r = (Record) this.mRecords.get(i);
                if (b == r.binder) {
                    break;
                }
            }
            r = new Record();
            r.binder = b;
            this.mRecords.add(r);
            r.onSubscriptionsChangedListenerCallback = callback;
            r.callingPackage = callingPackage;
            r.callerUserId = callerUserId;
            r.events = 0;
            r.canReadPhoneState = true;
            if (this.hasNotifySubscriptionInfoChangedOccurred) {
                try {
                    r.onSubscriptionsChangedListenerCallback.onSubscriptionsChanged();
                } catch (RemoteException e2) {
                    remove(r.binder);
                }
            } else {
                log("listen oscl: hasNotifySubscriptionInfoChangedOccurred==false no callback");
            }
        }
    }

    public void removeOnSubscriptionsChangedListener(String pkgForDebug, IOnSubscriptionsChangedListener callback) {
        remove(callback.asBinder());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifySubscriptionInfoChanged() {
        synchronized (this.mRecords) {
            if (!this.hasNotifySubscriptionInfoChangedOccurred) {
                log("notifySubscriptionInfoChanged: first invocation mRecords.size=" + this.mRecords.size());
            }
            this.hasNotifySubscriptionInfoChangedOccurred = true;
            this.mRemoveList.clear();
            for (Record r : this.mRecords) {
                if (r.matchOnSubscriptionsChangedListener()) {
                    try {
                        r.onSubscriptionsChangedListenerCallback.onSubscriptionsChanged();
                    } catch (RemoteException e) {
                        this.mRemoveList.add(r.binder);
                    }
                }
            }
            handleRemoveListLocked();
        }
    }

    public void listen(String pkgForDebug, IPhoneStateListener callback, int events, boolean notifyNow) {
        listenForSubscriber(Integer.MAX_VALUE, pkgForDebug, callback, events, notifyNow);
    }

    public void listenForSubscriber(int subId, String pkgForDebug, IPhoneStateListener callback, int events, boolean notifyNow) {
        listen(pkgForDebug, callback, events, notifyNow, subId);
    }

    private void listen(String callingPackage, IPhoneStateListener callback, int events, boolean notifyNow, int subId) {
        int callerUserId = UserHandle.getCallingUserId();
        if (events != 0) {
            checkListenerPermission(events);
            if ((events & ENFORCE_PHONE_STATE_PERMISSION_MASK) != 0) {
                try {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
                } catch (SecurityException e) {
                    if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                        return;
                    }
                }
            }
            synchronized (this.mRecords) {
                Record r;
                IBinder b = callback.asBinder();
                int N = this.mRecords.size();
                for (int i = 0; i < N; i += MSG_USER_SWITCHED) {
                    r = (Record) this.mRecords.get(i);
                    if (b == r.binder) {
                        break;
                    }
                }
                r = new Record();
                r.binder = b;
                this.mRecords.add(r);
                r.callback = callback;
                r.callingPackage = callingPackage;
                r.callerUserId = callerUserId;
                r.canReadPhoneState = (events & 16620) != 0 ? true : DBG_LOC ? canReadPhoneState(callingPackage) : DBG_LOC;
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    r.subId = subId;
                } else {
                    r.subId = Integer.MAX_VALUE;
                }
                r.phoneId = SubscriptionManager.getPhoneId(r.subId);
                int phoneId = r.phoneId;
                r.events = events;
                if (notifyNow && validatePhoneId(phoneId)) {
                    if ((events & MSG_USER_SWITCHED) != 0) {
                        try {
                            r.callback.onServiceStateChanged(new ServiceState(this.mServiceState[phoneId]));
                        } catch (RemoteException e2) {
                            remove(r.binder);
                        }
                    }
                    if ((events & MSG_UPDATE_DEFAULT_SUB) != 0) {
                        try {
                            int gsmSignalStrength = this.mSignalStrength[phoneId].getGsmSignalStrength();
                            IPhoneStateListener iPhoneStateListener = r.callback;
                            if (gsmSignalStrength == 99) {
                                gsmSignalStrength = -1;
                            }
                            iPhoneStateListener.onSignalStrengthChanged(gsmSignalStrength);
                        } catch (RemoteException e3) {
                            remove(r.binder);
                        }
                    }
                    if ((events & 4) != 0) {
                        try {
                            r.callback.onMessageWaitingIndicatorChanged(this.mMessageWaiting[phoneId]);
                        } catch (RemoteException e4) {
                            remove(r.binder);
                        }
                    }
                    if ((events & 8) != 0) {
                        try {
                            r.callback.onCallForwardingIndicatorChanged(this.mCallForwarding[phoneId]);
                        } catch (RemoteException e5) {
                            remove(r.binder);
                        }
                    }
                    if (validateEventsAndUserLocked(r, 16)) {
                        try {
                            r.callback.onCellLocationChanged(new Bundle(this.mCellLocation[phoneId]));
                        } catch (RemoteException e6) {
                            remove(r.binder);
                        }
                    }
                    if ((events & 32) != 0) {
                        try {
                            r.callback.onCallStateChanged(this.mCallState[phoneId], getCallIncomingNumber(r, phoneId));
                        } catch (RemoteException e7) {
                            remove(r.binder);
                        }
                    }
                    if ((events & 64) != 0) {
                        try {
                            r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
                        } catch (RemoteException e8) {
                            remove(r.binder);
                        }
                    }
                    if ((events & DumpState.DUMP_PACKAGES) != 0) {
                        try {
                            r.callback.onDataActivity(this.mDataActivity[phoneId]);
                        } catch (RemoteException e9) {
                            remove(r.binder);
                        }
                    }
                    if ((events & DumpState.DUMP_SHARED_USERS) != 0) {
                        try {
                            r.callback.onSignalStrengthsChanged(this.mSignalStrength[phoneId]);
                        } catch (RemoteException e10) {
                            remove(r.binder);
                        }
                    }
                    if ((events & DumpState.DUMP_MESSAGES) != 0) {
                        try {
                            r.callback.onOtaspChanged(this.mOtaspMode);
                        } catch (RemoteException e11) {
                            remove(r.binder);
                        }
                    }
                    if (validateEventsAndUserLocked(r, DumpState.DUMP_PROVIDERS)) {
                        try {
                            r.callback.onCellInfoChanged((List) this.mCellInfo.get(phoneId));
                        } catch (RemoteException e12) {
                            remove(r.binder);
                        }
                    }
                    if ((events & DumpState.DUMP_VERIFIERS) != 0) {
                        try {
                            r.callback.onPreciseCallStateChanged(this.mPreciseCallState);
                        } catch (RemoteException e13) {
                            remove(r.binder);
                        }
                    }
                    if ((events & DumpState.DUMP_PREFERRED) != 0) {
                        try {
                            r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState);
                        } catch (RemoteException e14) {
                            remove(r.binder);
                        }
                    }
                    if ((DumpState.DUMP_INSTALLS & events) != 0) {
                        try {
                            r.callback.onCarrierNetworkChange(this.mCarrierNetworkChangeState);
                        } catch (RemoteException e15) {
                            remove(r.binder);
                        }
                    }
                }
            }
        } else {
            remove(callback.asBinder());
        }
    }

    private boolean canReadPhoneState(String callingPackage) {
        boolean canReadPhoneState = true;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") == 0) {
            return true;
        }
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            canReadPhoneState = DBG_LOC;
        }
        if (!canReadPhoneState || this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) == 0) {
            return canReadPhoneState;
        }
        return DBG_LOC;
    }

    private String getCallIncomingNumber(Record record, int phoneId) {
        return record.canReadPhoneState ? this.mCallIncomingNumber[phoneId] : "";
    }

    private void remove(IBinder binder) {
        synchronized (this.mRecords) {
            int recordCount = this.mRecords.size();
            for (int i = 0; i < recordCount; i += MSG_USER_SWITCHED) {
                if (((Record) this.mRecords.get(i)).binder == binder) {
                    this.mRecords.remove(i);
                    return;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyCallState(int state, String incomingNumber) {
        if (checkNotifyPermission("notifyCallState()")) {
            synchronized (this.mRecords) {
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(32) && r.subId == Integer.MAX_VALUE) {
                        try {
                            r.callback.onCallStateChanged(state, r.canReadPhoneState ? incomingNumber : "");
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastCallStateChanged(state, incomingNumber, -1, -1);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyCallStateForPhoneId(int phoneId, int subId, int state, String incomingNumber) {
        if (checkNotifyPermission("notifyCallState()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCallState[phoneId] = state;
                    this.mCallIncomingNumber[phoneId] = incomingNumber;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(32) && r.subId == subId && r.subId != Integer.MAX_VALUE) {
                            try {
                                r.callback.onCallStateChanged(state, getCallIncomingNumber(r, phoneId));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastCallStateChanged(state, incomingNumber, phoneId, subId);
        }
    }

    public void notifyServiceStateForPhoneId(int phoneId, int subId, ServiceState state) {
        if (checkNotifyPermission("notifyServiceState()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mServiceState[phoneId] = state;
                    logServiceStateChanged("notifyServiceStateForSubscriber", subId, phoneId, state);
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(MSG_USER_SWITCHED) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onServiceStateChanged(new ServiceState(state));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                } else {
                    log("notifyServiceStateForSubscriber: INVALID phoneId=" + phoneId);
                }
                handleRemoveListLocked();
            }
            broadcastServiceStateChanged(state, phoneId, subId);
        }
    }

    public void notifySignalStrengthForPhoneId(int phoneId, int subId, SignalStrength signalStrength) {
        if (checkNotifyPermission("notifySignalStrength()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mSignalStrength[phoneId] = signalStrength;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_SHARED_USERS) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onSignalStrengthsChanged(new SignalStrength(signalStrength));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                        if (r.matchPhoneStateListenerEvent(MSG_UPDATE_DEFAULT_SUB) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                int gsmSignalStrength = signalStrength.getGsmSignalStrength();
                                r.callback.onSignalStrengthChanged(gsmSignalStrength == 99 ? -1 : gsmSignalStrength);
                            } catch (RemoteException e2) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                } else {
                    log("notifySignalStrengthForPhoneId: invalid phoneId=" + phoneId);
                }
                handleRemoveListLocked();
            }
            broadcastSignalStrengthChanged(signalStrength, phoneId, subId);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyCarrierNetworkChange(boolean active) {
        enforceNotifyPermissionOrCarrierPrivilege("notifyCarrierNetworkChange()");
        synchronized (this.mRecords) {
            this.mCarrierNetworkChangeState = active;
            for (Record r : this.mRecords) {
                if (r.matchPhoneStateListenerEvent(DumpState.DUMP_INSTALLS)) {
                    try {
                        r.callback.onCarrierNetworkChange(active);
                    } catch (RemoteException e) {
                        this.mRemoveList.add(r.binder);
                    }
                }
            }
            handleRemoveListLocked();
        }
    }

    public void notifyCellInfo(List<CellInfo> cellInfo) {
        notifyCellInfoForSubscriber(Integer.MAX_VALUE, cellInfo);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyCellInfoForSubscriber(int subId, List<CellInfo> cellInfo) {
        if (checkNotifyPermission("notifyCellInfo()")) {
            synchronized (this.mRecords) {
                int phoneId = SubscriptionManager.getPhoneId(subId);
                if (validatePhoneId(phoneId)) {
                    this.mCellInfo.set(phoneId, cellInfo);
                    for (Record r : this.mRecords) {
                        if (validateEventsAndUserLocked(r, DumpState.DUMP_PROVIDERS) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onCellInfoChanged(cellInfo);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyMessageWaitingChangedForPhoneId(int phoneId, int subId, boolean mwi) {
        if (checkNotifyPermission("notifyMessageWaitingChanged()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mMessageWaiting[phoneId] = mwi;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(4) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onMessageWaitingIndicatorChanged(mwi);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public void notifyCallForwardingChanged(boolean cfi) {
        notifyCallForwardingChangedForSubscriber(Integer.MAX_VALUE, cfi);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyCallForwardingChangedForSubscriber(int subId, boolean cfi) {
        if (checkNotifyPermission("notifyCallForwardingChanged()")) {
            synchronized (this.mRecords) {
                int phoneId = SubscriptionManager.getPhoneId(subId);
                if (validatePhoneId(phoneId)) {
                    this.mCallForwarding[phoneId] = cfi;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(8) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onCallForwardingIndicatorChanged(cfi);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public void notifyDataActivity(int state) {
        notifyDataActivityForSubscriber(Integer.MAX_VALUE, state);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyDataActivityForSubscriber(int subId, int state) {
        if (checkNotifyPermission("notifyDataActivity()")) {
            synchronized (this.mRecords) {
                int phoneId = SubscriptionManager.getPhoneId(subId);
                if (validatePhoneId(phoneId)) {
                    this.mDataActivity[phoneId] = state;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_PACKAGES) && (r.subId == subId || r.subId == Integer.MAX_VALUE)) {
                            try {
                                r.callback.onDataActivity(state);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public void notifyDataConnection(int state, boolean isDataConnectivityPossible, String reason, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int networkType, boolean roaming) {
        notifyDataConnectionForSubscriber(Integer.MAX_VALUE, state, isDataConnectivityPossible, reason, apn, apnType, linkProperties, networkCapabilities, networkType, roaming);
    }

    public void notifyDataConnectionForSubscriber(int subId, int state, boolean isDataConnectivityPossible, String reason, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int networkType, boolean roaming) {
        if (checkNotifyPermission("notifyDataConnection()")) {
            synchronized (this.mRecords) {
                int phoneId = SubscriptionManager.getPhoneId(subId);
                if (validatePhoneId(phoneId)) {
                    boolean modified = DBG_LOC;
                    if (state == MSG_UPDATE_DEFAULT_SUB) {
                        if (!(this.mConnectedApns[phoneId].contains(apnType) || "ims".equals(apnType))) {
                            if (!"xcap".equals(apnType)) {
                                this.mConnectedApns[phoneId].add(apnType);
                                if (this.mDataConnectionState[phoneId] != state) {
                                    this.mDataConnectionState[phoneId] = state;
                                    modified = true;
                                }
                            }
                        }
                    } else if (this.mConnectedApns[phoneId].remove(apnType) && this.mConnectedApns[phoneId].isEmpty()) {
                        this.mDataConnectionState[phoneId] = state;
                        modified = true;
                    }
                    this.mDataConnectionPossible[phoneId] = isDataConnectivityPossible;
                    this.mDataConnectionReason[phoneId] = reason;
                    this.mDataConnectionLinkProperties[phoneId] = linkProperties;
                    this.mDataConnectionNetworkCapabilities[phoneId] = networkCapabilities;
                    if (this.mDataConnectionNetworkType[phoneId] != networkType) {
                        this.mDataConnectionNetworkType[phoneId] = networkType;
                        modified = true;
                    }
                    if (modified) {
                        log("onDataConnectionStateChanged(" + this.mDataConnectionState[phoneId] + ", " + this.mDataConnectionNetworkType[phoneId] + ")");
                        for (Record r : this.mRecords) {
                            if (r.matchPhoneStateListenerEvent(64) && idMatch(r.subId, subId, phoneId)) {
                                try {
                                    log("Notify data connection state changed on sub: " + subId);
                                    r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
                                } catch (RemoteException e) {
                                    this.mRemoveList.add(r.binder);
                                }
                            }
                        }
                        handleRemoveListLocked();
                    }
                    this.mPreciseDataConnectionState = new PreciseDataConnectionState(state, networkType, apnType, apn, reason, linkProperties, "");
                    for (Record r2 : this.mRecords) {
                        if (r2.matchPhoneStateListenerEvent(DumpState.DUMP_PREFERRED)) {
                            try {
                                r2.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState);
                            } catch (RemoteException e2) {
                                this.mRemoveList.add(r2.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastDataConnectionStateChanged(state, isDataConnectivityPossible, reason, apn, apnType, linkProperties, networkCapabilities, roaming, subId);
            broadcastPreciseDataConnectionStateChanged(state, networkType, apnType, apn, reason, linkProperties, "");
        }
    }

    public void notifyDataConnectionFailed(String reason, String apnType) {
        notifyDataConnectionFailedForSubscriber(Integer.MAX_VALUE, reason, apnType);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyDataConnectionFailedForSubscriber(int subId, String reason, String apnType) {
        if (checkNotifyPermission("notifyDataConnectionFailed()")) {
            synchronized (this.mRecords) {
                this.mPreciseDataConnectionState = new PreciseDataConnectionState(-1, 0, apnType, "", reason, null, "");
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(DumpState.DUMP_PREFERRED)) {
                        try {
                            r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastDataConnectionFailed(reason, apnType, subId);
            broadcastPreciseDataConnectionStateChanged(-1, 0, apnType, "", reason, null, "");
        }
    }

    public void notifyCellLocation(Bundle cellLocation) {
        notifyCellLocationForSubscriber(Integer.MAX_VALUE, cellLocation);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyCellLocationForSubscriber(int subId, Bundle cellLocation) {
        log("notifyCellLocationForSubscriber: subId=" + subId + " cellLocation=" + cellLocation);
        if (checkNotifyPermission("notifyCellLocation()")) {
            synchronized (this.mRecords) {
                int phoneId = SubscriptionManager.getPhoneId(subId);
                if (validatePhoneId(phoneId)) {
                    this.mCellLocation[phoneId] = cellLocation;
                    for (Record r : this.mRecords) {
                        if (validateEventsAndUserLocked(r, 16) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onCellLocationChanged(new Bundle(cellLocation));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyOtaspChanged(int otaspMode) {
        if (checkNotifyPermission("notifyOtaspChanged()")) {
            synchronized (this.mRecords) {
                this.mOtaspMode = otaspMode;
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(DumpState.DUMP_MESSAGES)) {
                        try {
                            r.callback.onOtaspChanged(otaspMode);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyPreciseCallState(int ringingCallState, int foregroundCallState, int backgroundCallState) {
        if (checkNotifyPermission("notifyPreciseCallState()")) {
            synchronized (this.mRecords) {
                this.mRingingCallState = ringingCallState;
                this.mForegroundCallState = foregroundCallState;
                this.mBackgroundCallState = backgroundCallState;
                this.mPreciseCallState = new PreciseCallState(ringingCallState, foregroundCallState, backgroundCallState, -1, -1);
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(DumpState.DUMP_VERIFIERS)) {
                        try {
                            r.callback.onPreciseCallStateChanged(this.mPreciseCallState);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastPreciseCallStateChanged(ringingCallState, foregroundCallState, backgroundCallState, -1, -1);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyDisconnectCause(int disconnectCause, int preciseDisconnectCause) {
        if (checkNotifyPermission("notifyDisconnectCause()")) {
            synchronized (this.mRecords) {
                this.mPreciseCallState = new PreciseCallState(this.mRingingCallState, this.mForegroundCallState, this.mBackgroundCallState, disconnectCause, preciseDisconnectCause);
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(DumpState.DUMP_VERIFIERS)) {
                        try {
                            r.callback.onPreciseCallStateChanged(this.mPreciseCallState);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastPreciseCallStateChanged(this.mRingingCallState, this.mForegroundCallState, this.mBackgroundCallState, disconnectCause, preciseDisconnectCause);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyPreciseDataConnectionFailed(String reason, String apnType, String apn, String failCause) {
        if (checkNotifyPermission("notifyPreciseDataConnectionFailed()")) {
            synchronized (this.mRecords) {
                this.mPreciseDataConnectionState = new PreciseDataConnectionState(-1, 0, apnType, apn, reason, null, failCause);
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(DumpState.DUMP_PREFERRED)) {
                        try {
                            r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastPreciseDataConnectionStateChanged(-1, 0, apnType, apn, reason, null, failCause);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyVoLteServiceStateChanged(VoLteServiceState lteState) {
        if (checkNotifyPermission("notifyVoLteServiceStateChanged()")) {
            synchronized (this.mRecords) {
                this.mVoLteServiceState = lteState;
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(DumpState.DUMP_KEYSETS)) {
                        try {
                            r.callback.onVoLteServiceStateChanged(new VoLteServiceState(this.mVoLteServiceState));
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyOemHookRawEventForSubscriber(int subId, byte[] rawData) {
        if (checkNotifyPermission("notifyOemHookRawEventForSubscriber")) {
            synchronized (this.mRecords) {
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(DumpState.DUMP_VERSION) && (r.subId == subId || r.subId == Integer.MAX_VALUE)) {
                        try {
                            r.callback.onOemHookRawEvent(rawData);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump telephony.registry from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mRecords) {
            int recordCount = this.mRecords.size();
            pw.println("last known state:");
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i += MSG_USER_SWITCHED) {
                pw.println("  Phone Id=" + i);
                pw.println("  mCallState=" + this.mCallState[i]);
                pw.println("  mCallIncomingNumber=" + this.mCallIncomingNumber[i]);
                pw.println("  mServiceState=" + this.mServiceState[i]);
                pw.println("  mSignalStrength=" + this.mSignalStrength[i]);
                pw.println("  mMessageWaiting=" + this.mMessageWaiting[i]);
                pw.println("  mCallForwarding=" + this.mCallForwarding[i]);
                pw.println("  mDataActivity=" + this.mDataActivity[i]);
                pw.println("  mDataConnectionState=" + this.mDataConnectionState[i]);
                pw.println("  mDataConnectionPossible=" + this.mDataConnectionPossible[i]);
                pw.println("  mDataConnectionReason=" + this.mDataConnectionReason[i]);
                pw.println("  mDataConnectionApn=" + this.mDataConnectionApn[i]);
                pw.println("  mDataConnectionLinkProperties=" + this.mDataConnectionLinkProperties[i]);
                pw.println("  mDataConnectionNetworkCapabilities=" + this.mDataConnectionNetworkCapabilities[i]);
                pw.println("  mCellLocation=" + this.mCellLocation[i]);
                pw.println("  mCellInfo=" + this.mCellInfo.get(i));
            }
            pw.println("registrations: count=" + recordCount);
            for (Record r : this.mRecords) {
                pw.println("  " + r);
            }
        }
    }

    private void broadcastServiceStateChanged(ServiceState state, int phoneId, int subId) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.notePhoneState(state.getState());
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        Intent intent = new Intent("android.intent.action.SERVICE_STATE");
        Bundle data = new Bundle();
        state.fillInNotifierBundle(data);
        intent.putExtras(data);
        intent.putExtra("subscription", subId);
        intent.putExtra("slot", phoneId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void broadcastSignalStrengthChanged(SignalStrength signalStrength, int phoneId, int subId) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.notePhoneSignalStrength(signalStrength);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        Intent intent = new Intent("android.intent.action.SIG_STR");
        intent.addFlags(536870912);
        Bundle data = new Bundle();
        signalStrength.fillInNotifierBundle(data);
        intent.putExtras(data);
        intent.putExtra("subscription", subId);
        intent.putExtra("slot", phoneId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void broadcastCallStateChanged(int state, String incomingNumber, int phoneId, int subId) {
        long ident = Binder.clearCallingIdentity();
        if (state == 0) {
            try {
                this.mBatteryStats.notePhoneOff();
            } catch (RemoteException e) {
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            this.mBatteryStats.notePhoneOn();
        }
        Binder.restoreCallingIdentity(ident);
        Intent intent = new Intent("android.intent.action.PHONE_STATE");
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, DefaultPhoneNotifier.convertCallState(state).toString());
        if (!TextUtils.isEmpty(incomingNumber)) {
            intent.putExtra("incoming_number", incomingNumber);
        }
        if (subId != -1) {
            intent.setAction("android.intent.action.SUBSCRIPTION_PHONE_STATE");
            intent.putExtra("subscription", subId);
        }
        if (phoneId != -1) {
            intent.putExtra("slot", phoneId);
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PHONE_STATE", 51);
    }

    private void broadcastDataConnectionStateChanged(int state, boolean isDataConnectivityPossible, String reason, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, boolean roaming, int subId) {
        Intent intent = new Intent("android.intent.action.ANY_DATA_STATE");
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, DefaultPhoneNotifier.convertDataState(state).toString());
        if (!isDataConnectivityPossible) {
            intent.putExtra("networkUnvailable", true);
        }
        if (reason != null) {
            intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason);
        }
        if (linkProperties != null) {
            intent.putExtra("linkProperties", linkProperties);
            String iface = linkProperties.getInterfaceName();
            if (iface != null) {
                intent.putExtra("iface", iface);
            }
        }
        if (networkCapabilities != null) {
            intent.putExtra("networkCapabilities", networkCapabilities);
        }
        if (roaming) {
            intent.putExtra("networkRoaming", true);
        }
        intent.putExtra("apn", apn);
        intent.putExtra("apnType", apnType);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void broadcastDataConnectionFailed(String reason, String apnType, int subId) {
        Intent intent = new Intent("android.intent.action.DATA_CONNECTION_FAILED");
        intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason);
        intent.putExtra("apnType", apnType);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void broadcastPreciseCallStateChanged(int ringingCallState, int foregroundCallState, int backgroundCallState, int disconnectCause, int preciseDisconnectCause) {
        Intent intent = new Intent("android.intent.action.PRECISE_CALL_STATE");
        intent.putExtra("ringing_state", ringingCallState);
        intent.putExtra("foreground_state", foregroundCallState);
        intent.putExtra("background_state", backgroundCallState);
        intent.putExtra("disconnect_cause", disconnectCause);
        intent.putExtra("precise_disconnect_cause", preciseDisconnectCause);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PRECISE_PHONE_STATE");
    }

    private void broadcastPreciseDataConnectionStateChanged(int state, int networkType, String apnType, String apn, String reason, LinkProperties linkProperties, String failCause) {
        Intent intent = new Intent("android.intent.action.PRECISE_DATA_CONNECTION_STATE_CHANGED");
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, state);
        intent.putExtra("networkType", networkType);
        if (reason != null) {
            intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason);
        }
        if (apnType != null) {
            intent.putExtra("apnType", apnType);
        }
        if (apn != null) {
            intent.putExtra("apn", apn);
        }
        if (linkProperties != null) {
            intent.putExtra("linkProperties", linkProperties);
        }
        if (failCause != null) {
            intent.putExtra("failCause", failCause);
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PRECISE_PHONE_STATE");
    }

    private void enforceNotifyPermissionOrCarrierPrivilege(String method) {
        if (!checkNotifyPermission()) {
            enforceCarrierPrivilege();
        }
    }

    private boolean checkNotifyPermission(String method) {
        if (checkNotifyPermission()) {
            return true;
        }
        String msg = "Modify Phone State Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
        return DBG_LOC;
    }

    private boolean checkNotifyPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0 ? true : DBG_LOC;
    }

    private void enforceCarrierPrivilege() {
        TelephonyManager tm = TelephonyManager.getDefault();
        String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid());
        int i = 0;
        int length = pkgs.length;
        while (i < length) {
            if (tm.checkCarrierPrivilegesForPackage(pkgs[i]) != MSG_USER_SWITCHED) {
                i += MSG_USER_SWITCHED;
            } else {
                return;
            }
        }
        throw new SecurityException("Carrier Privilege Permission Denial: from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
    }

    private void checkListenerPermission(int events) {
        if ((events & 16) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION", null);
        }
        if ((events & DumpState.DUMP_PROVIDERS) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION", null);
        }
        if ((events & ENFORCE_PHONE_STATE_PERMISSION_MASK) != 0) {
            try {
                this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
            } catch (SecurityException e) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
            }
        }
        if ((events & PRECISE_PHONE_STATE_PERMISSION_MASK) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRECISE_PHONE_STATE", null);
        }
        if ((DumpState.DUMP_VERSION & events) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
        }
    }

    private void handleRemoveListLocked() {
        if (this.mRemoveList.size() > 0) {
            for (IBinder b : this.mRemoveList) {
                remove(b);
            }
            this.mRemoveList.clear();
        }
    }

    private boolean validateEventsAndUserLocked(Record r, int events) {
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            boolean matchPhoneStateListenerEvent = r.callerUserId == ActivityManager.getCurrentUser() ? r.matchPhoneStateListenerEvent(events) : DBG_LOC;
            Binder.restoreCallingIdentity(callingIdentity);
            return matchPhoneStateListenerEvent;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    private boolean validatePhoneId(int phoneId) {
        return (phoneId < 0 || phoneId >= this.mNumPhones) ? DBG_LOC : true;
    }

    private static void log(String s) {
        Rlog.d(TAG, s);
    }

    private void logServiceStateChanged(String s, int subId, int phoneId, ServiceState state) {
        if (this.logSSC != null && this.logSSC.length != 0) {
            if (this.logSSC[this.next] == null) {
                this.logSSC[this.next] = new LogSSC();
            }
            Time t = new Time();
            t.setToNow();
            this.logSSC[this.next].set(t, s, subId, phoneId, state);
            int i = this.next + MSG_USER_SWITCHED;
            this.next = i;
            if (i >= this.logSSC.length) {
                this.next = 0;
            }
        }
    }

    private void toStringLogSSC(String prompt) {
        if (this.logSSC == null || this.logSSC.length == 0 || (this.next == 0 && this.logSSC[this.next] == null)) {
            log(prompt + ": logSSC is empty");
            return;
        }
        log(prompt + ": logSSC.length=" + this.logSSC.length + " next=" + this.next);
        int i = this.next;
        if (this.logSSC[i] == null) {
            i = 0;
        }
        do {
            log(this.logSSC[i].toString());
            i += MSG_USER_SWITCHED;
            if (i >= this.logSSC.length) {
                i = 0;
            }
        } while (i != this.next);
        log(prompt + ": ----------------");
    }

    boolean idMatch(int rSubId, int subId, int phoneId) {
        boolean z = true;
        if (subId < 0) {
            if (this.mDefaultPhoneId != phoneId) {
                z = DBG_LOC;
            }
            return z;
        } else if (rSubId == Integer.MAX_VALUE) {
            return true;
        } else {
            if (rSubId != subId) {
                z = DBG_LOC;
            }
            return z;
        }
    }

    private void checkPossibleMissNotify(Record r, int phoneId) {
        int events = r.events;
        if ((events & MSG_USER_SWITCHED) != 0) {
            try {
                r.callback.onServiceStateChanged(new ServiceState(this.mServiceState[phoneId]));
            } catch (RemoteException e) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & DumpState.DUMP_SHARED_USERS) != 0) {
            try {
                r.callback.onSignalStrengthsChanged(new SignalStrength(this.mSignalStrength[phoneId]));
            } catch (RemoteException e2) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & MSG_UPDATE_DEFAULT_SUB) != 0) {
            try {
                int gsmSignalStrength = this.mSignalStrength[phoneId].getGsmSignalStrength();
                IPhoneStateListener iPhoneStateListener = r.callback;
                if (gsmSignalStrength == 99) {
                    gsmSignalStrength = -1;
                }
                iPhoneStateListener.onSignalStrengthChanged(gsmSignalStrength);
            } catch (RemoteException e3) {
                this.mRemoveList.add(r.binder);
            }
        }
        if (validateEventsAndUserLocked(r, DumpState.DUMP_PROVIDERS)) {
            try {
                r.callback.onCellInfoChanged((List) this.mCellInfo.get(phoneId));
            } catch (RemoteException e4) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 4) != 0) {
            try {
                r.callback.onMessageWaitingIndicatorChanged(this.mMessageWaiting[phoneId]);
            } catch (RemoteException e5) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 8) != 0) {
            try {
                r.callback.onCallForwardingIndicatorChanged(this.mCallForwarding[phoneId]);
            } catch (RemoteException e6) {
                this.mRemoveList.add(r.binder);
            }
        }
        if (validateEventsAndUserLocked(r, 16)) {
            try {
                r.callback.onCellLocationChanged(new Bundle(this.mCellLocation[phoneId]));
            } catch (RemoteException e7) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 64) != 0) {
            try {
                r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
            } catch (RemoteException e8) {
                this.mRemoveList.add(r.binder);
            }
        }
    }
}
