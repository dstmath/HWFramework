package com.android.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
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
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.LocationAccessPolicy;
import android.telephony.PhysicalChannelConfig;
import android.telephony.PreciseCallState;
import android.telephony.PreciseDataConnectionState;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.IPhoneStateListener;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstantConversions;
import com.android.internal.telephony.TelephonyPermissions;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.BatteryService;
import com.android.server.am.BatteryStatsService;
import com.android.server.audio.AudioService;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.policy.PhoneWindowManager;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class TelephonyRegistry extends ITelephonyRegistry.Stub {
    private static final boolean DBG = false;
    private static final boolean DBG_LOC = false;
    static final int ENFORCE_COARSE_LOCATION_PERMISSION_MASK = 1040;
    static final int ENFORCE_PHONE_STATE_PERMISSION_MASK = 16396;
    private static final int MSG_UPDATE_DEFAULT_SUB = 2;
    private static final int MSG_USER_SWITCHED = 1;
    static final int PRECISE_PHONE_STATE_PERMISSION_MASK = 6144;
    private static final boolean SHOW_DATA_ACT_ALL_APN = SystemProperties.getBoolean("ro.config.show_data_act_all_apn", false);
    private static final String TAG = "TelephonyRegistry";
    private static final boolean VDBG = false;
    private boolean hasNotifySubscriptionInfoChangedOccurred;
    private final AppOpsManager mAppOps;
    private int mBackgroundCallState;
    private final IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver;
    private boolean[] mCallForwarding;
    private String[] mCallIncomingNumber;
    private int[] mCallState;
    private boolean mCarrierNetworkChangeState;
    private ArrayList<List<CellInfo>> mCellInfo;
    /* access modifiers changed from: private */
    public Bundle[] mCellLocation;
    private ArrayList<String>[] mConnectedApns;
    private final Context mContext;
    private int[] mDataActivationState;
    private int[] mDataActivity;
    private int[] mDataConnectionNetworkType;
    private int[] mDataConnectionState;
    /* access modifiers changed from: private */
    public int mDefaultPhoneId;
    /* access modifiers changed from: private */
    public int mDefaultSubId;
    private int mForegroundCallState;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final LocalLog mLocalLog;
    private boolean[] mMessageWaiting;
    private int mNumPhones;
    private int mOtaspMode;
    private ArrayList<List<PhysicalChannelConfig>> mPhysicalChannelConfigs;
    private PreciseCallState mPreciseCallState;
    private PreciseDataConnectionState mPreciseDataConnectionState;
    /* access modifiers changed from: private */
    public final ArrayList<Record> mRecords = new ArrayList<>();
    private final ArrayList<IBinder> mRemoveList = new ArrayList<>();
    private int mRingingCallState;
    private ServiceState[] mServiceState;
    private SignalStrength[] mSignalStrength;
    private boolean[] mUserMobileDataState;
    private VoLteServiceState mVoLteServiceState;
    private int[] mVoiceActivationState;

    private static class Record {
        IBinder binder;
        IPhoneStateListener callback;
        int callerPid;
        int callerUid;
        String callingPackage;
        Context context;
        TelephonyRegistryDeathRecipient deathRecipient;
        int events;
        IOnSubscriptionsChangedListener onSubscriptionsChangedListenerCallback;
        int phoneId;
        int subId;

        private Record() {
            this.subId = -1;
            this.phoneId = -1;
        }

        /* access modifiers changed from: package-private */
        public boolean matchPhoneStateListenerEvent(int events2) {
            return (this.callback == null || (this.events & events2) == 0) ? false : true;
        }

        /* access modifiers changed from: package-private */
        public boolean matchOnSubscriptionsChangedListener() {
            return this.onSubscriptionsChangedListenerCallback != null;
        }

        /* access modifiers changed from: package-private */
        public boolean canReadCallLog() {
            try {
                return TelephonyPermissions.checkReadCallLog(this.context, this.subId, this.callerPid, this.callerUid, this.callingPackage);
            } catch (SecurityException e) {
                return false;
            }
        }

        public String toString() {
            return "{callingPackage=" + this.callingPackage + " binder=" + this.binder + " callback=" + this.callback + " onSubscriptionsChangedListenererCallback=" + this.onSubscriptionsChangedListenerCallback + " callerUid=" + this.callerUid + " subId=" + this.subId + " phoneId=" + this.phoneId + " events=" + Integer.toHexString(this.events) + "}";
        }
    }

    private class TelephonyRegistryDeathRecipient implements IBinder.DeathRecipient {
        private final IBinder binder;

        TelephonyRegistryDeathRecipient(IBinder binder2) {
            this.binder = binder2;
        }

        public void binderDied() {
            TelephonyRegistry.this.remove(this.binder);
        }
    }

    protected TelephonyRegistry(Context context) {
        this.hasNotifySubscriptionInfoChangedOccurred = false;
        this.mOtaspMode = 1;
        this.mCellInfo = null;
        this.mVoLteServiceState = new VoLteServiceState();
        this.mDefaultSubId = -1;
        this.mDefaultPhoneId = -1;
        this.mRingingCallState = 0;
        this.mForegroundCallState = 0;
        this.mBackgroundCallState = 0;
        this.mPreciseCallState = new PreciseCallState();
        this.mCarrierNetworkChangeState = false;
        this.mLocalLog = new LocalLog(100);
        this.mPreciseDataConnectionState = new PreciseDataConnectionState();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        int numPhones = TelephonyManager.getDefault().getPhoneCount();
                        for (int sub = 0; sub < numPhones; sub++) {
                            TelephonyRegistry.this.notifyCellLocationForSubscriber(sub, TelephonyRegistry.this.mCellLocation[sub]);
                        }
                        return;
                    case 2:
                        int newDefaultPhoneId = msg.arg1;
                        int newDefaultSubId = ((Integer) msg.obj).intValue();
                        synchronized (TelephonyRegistry.this.mRecords) {
                            Iterator it = TelephonyRegistry.this.mRecords.iterator();
                            while (it.hasNext()) {
                                Record r = (Record) it.next();
                                if (r.subId == Integer.MAX_VALUE) {
                                    TelephonyRegistry.this.checkPossibleMissNotify(r, newDefaultPhoneId);
                                }
                            }
                            TelephonyRegistry.this.handleRemoveListLocked();
                        }
                        int unused = TelephonyRegistry.this.mDefaultSubId = newDefaultSubId;
                        int unused2 = TelephonyRegistry.this.mDefaultPhoneId = newDefaultPhoneId;
                        return;
                    default:
                        return;
                }
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    TelephonyRegistry.this.mHandler.sendMessage(TelephonyRegistry.this.mHandler.obtainMessage(1, intent.getIntExtra("android.intent.extra.user_handle", 0), 0));
                } else if (action.equals("android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED")) {
                    Integer newDefaultSubIdObj = new Integer(intent.getIntExtra("subscription", SubscriptionManager.getDefaultSubscriptionId()));
                    int newDefaultPhoneId = intent.getIntExtra("slot", SubscriptionManager.getPhoneId(TelephonyRegistry.this.mDefaultSubId));
                    if (!TelephonyRegistry.this.validatePhoneId(newDefaultPhoneId)) {
                        return;
                    }
                    if (!newDefaultSubIdObj.equals(Integer.valueOf(TelephonyRegistry.this.mDefaultSubId)) || newDefaultPhoneId != TelephonyRegistry.this.mDefaultPhoneId) {
                        TelephonyRegistry.this.mHandler.sendMessage(TelephonyRegistry.this.mHandler.obtainMessage(2, newDefaultPhoneId, 0, newDefaultSubIdObj));
                    }
                }
            }
        };
        CellLocation location = CellLocation.getEmpty();
        this.mContext = context;
        this.mBatteryStats = BatteryStatsService.getService();
        int numPhones = VSimTelephonyRegistry.processVSimPhoneNumbers(TelephonyManager.getDefault().getPhoneCount());
        this.mNumPhones = numPhones;
        this.mConnectedApns = new ArrayList[numPhones];
        this.mCallState = new int[numPhones];
        this.mDataActivity = new int[numPhones];
        this.mDataConnectionState = new int[numPhones];
        this.mDataConnectionNetworkType = new int[numPhones];
        this.mCallIncomingNumber = new String[numPhones];
        this.mServiceState = new ServiceState[numPhones];
        this.mVoiceActivationState = new int[numPhones];
        this.mDataActivationState = new int[numPhones];
        this.mUserMobileDataState = new boolean[numPhones];
        this.mSignalStrength = new SignalStrength[numPhones];
        this.mMessageWaiting = new boolean[numPhones];
        this.mCallForwarding = new boolean[numPhones];
        this.mCellLocation = new Bundle[numPhones];
        this.mCellInfo = new ArrayList<>();
        this.mPhysicalChannelConfigs = new ArrayList<>();
        for (int i = 0; i < numPhones; i++) {
            this.mCallState[i] = 0;
            this.mDataActivity[i] = 0;
            this.mDataConnectionState[i] = -1;
            this.mVoiceActivationState[i] = 0;
            this.mDataActivationState[i] = 0;
            this.mCallIncomingNumber[i] = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            this.mServiceState[i] = new ServiceState();
            this.mSignalStrength[i] = new SignalStrength();
            this.mUserMobileDataState[i] = false;
            this.mMessageWaiting[i] = false;
            this.mCallForwarding[i] = false;
            this.mCellLocation[i] = new Bundle();
            this.mCellInfo.add(i, null);
            this.mConnectedApns[i] = new ArrayList<>();
            this.mPhysicalChannelConfigs.add(i, new ArrayList());
        }
        if (location != null) {
            for (int i2 = 0; i2 < numPhones; i2++) {
                location.fillInNotifierBundle(this.mCellLocation[i2]);
            }
        }
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
    }

    public void systemRunning() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED");
        log("systemRunning register for intents");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public void addOnSubscriptionsChangedListener(String callingPackage, IOnSubscriptionsChangedListener callback) {
        int callingUserId = UserHandle.getCallingUserId();
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        synchronized (this.mRecords) {
            Record r = add(callback.asBinder());
            if (r != null) {
                r.context = this.mContext;
                r.onSubscriptionsChangedListenerCallback = callback;
                r.callingPackage = callingPackage;
                r.callerUid = Binder.getCallingUid();
                r.callerPid = Binder.getCallingPid();
                r.events = 0;
                if (this.hasNotifySubscriptionInfoChangedOccurred) {
                    try {
                        r.onSubscriptionsChangedListenerCallback.onSubscriptionsChanged();
                    } catch (RemoteException e) {
                        remove(r.binder);
                    }
                } else {
                    log("listen oscl: hasNotifySubscriptionInfoChangedOccurred==false no callback");
                }
            }
        }
    }

    public void removeOnSubscriptionsChangedListener(String pkgForDebug, IOnSubscriptionsChangedListener callback) {
        remove(callback.asBinder());
    }

    public void notifySubscriptionInfoChanged() {
        synchronized (this.mRecords) {
            if (!this.hasNotifySubscriptionInfoChangedOccurred) {
                log("notifySubscriptionInfoChanged: first invocation mRecords.size=" + this.mRecords.size());
            }
            this.hasNotifySubscriptionInfoChangedOccurred = true;
            this.mRemoveList.clear();
            Iterator<Record> it = this.mRecords.iterator();
            while (it.hasNext()) {
                Record r = it.next();
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
        listenForSubscriber(HwBootFail.STAGE_BOOT_SUCCESS, pkgForDebug, callback, events, notifyNow);
    }

    public void listenForSubscriber(int subId, String pkgForDebug, IPhoneStateListener callback, int events, boolean notifyNow) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONYREGISTRY_LISTENFORSUBSCRIBER);
        }
        listen(pkgForDebug, callback, events, notifyNow, subId);
    }

    private void listen(String callingPackage, IPhoneStateListener callback, int events, boolean notifyNow, int subId) {
        int callingUserId = UserHandle.getCallingUserId();
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        if (events == 0) {
            remove(callback.asBinder());
        } else if (checkListenerPermission(events, subId, callingPackage, "listen")) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                Record r = add(callback.asBinder());
                if (r != null) {
                    r.context = this.mContext;
                    r.callback = callback;
                    r.callingPackage = callingPackage;
                    r.callerUid = Binder.getCallingUid();
                    r.callerPid = Binder.getCallingPid();
                    if (!SubscriptionManager.isValidSubscriptionId(subId)) {
                        r.subId = HwBootFail.STAGE_BOOT_SUCCESS;
                    } else {
                        r.subId = subId;
                    }
                    r.phoneId = phoneId;
                    r.events = events;
                    if (notifyNow && validatePhoneId(phoneId)) {
                        if ((events & 1) != 0) {
                            try {
                                r.callback.onServiceStateChanged(new ServiceState(this.mServiceState[phoneId]));
                            } catch (RemoteException e) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 2) != 0) {
                            try {
                                int gsmSignalStrength = this.mSignalStrength[phoneId].getGsmSignalStrength();
                                r.callback.onSignalStrengthChanged(gsmSignalStrength == 99 ? -1 : gsmSignalStrength);
                            } catch (RemoteException e2) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 4) != 0) {
                            try {
                                r.callback.onMessageWaitingIndicatorChanged(this.mMessageWaiting[phoneId]);
                            } catch (RemoteException e3) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 8) != 0) {
                            try {
                                r.callback.onCallForwardingIndicatorChanged(this.mCallForwarding[phoneId]);
                            } catch (RemoteException e4) {
                                remove(r.binder);
                            }
                        }
                        if (validateEventsAndUserLocked(r, 16)) {
                            try {
                                if (checkLocationAccess(r)) {
                                    r.callback.onCellLocationChanged(new Bundle(this.mCellLocation[phoneId]));
                                }
                            } catch (RemoteException e5) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 32) != 0) {
                            try {
                                r.callback.onCallStateChanged(this.mCallState[phoneId], getCallIncomingNumber(r, phoneId));
                            } catch (RemoteException e6) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 64) != 0) {
                            try {
                                r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
                            } catch (RemoteException e7) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 128) != 0) {
                            try {
                                r.callback.onDataActivity(this.mDataActivity[phoneId]);
                            } catch (RemoteException e8) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 256) != 0) {
                            try {
                                r.callback.onSignalStrengthsChanged(this.mSignalStrength[phoneId]);
                            } catch (RemoteException e9) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 512) != 0) {
                            try {
                                r.callback.onOtaspChanged(this.mOtaspMode);
                            } catch (RemoteException e10) {
                                remove(r.binder);
                            }
                        }
                        if (validateEventsAndUserLocked(r, 1024)) {
                            try {
                                if (checkLocationAccess(r)) {
                                    r.callback.onCellInfoChanged(this.mCellInfo.get(phoneId));
                                }
                            } catch (RemoteException e11) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 2048) != 0) {
                            try {
                                r.callback.onPreciseCallStateChanged(this.mPreciseCallState);
                            } catch (RemoteException e12) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 4096) != 0) {
                            try {
                                r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState);
                            } catch (RemoteException e13) {
                                remove(r.binder);
                            }
                        }
                        if ((65536 & events) != 0) {
                            try {
                                r.callback.onCarrierNetworkChange(this.mCarrierNetworkChangeState);
                            } catch (RemoteException e14) {
                                remove(r.binder);
                            }
                        }
                        if ((131072 & events) != 0) {
                            try {
                                r.callback.onVoiceActivationStateChanged(this.mVoiceActivationState[phoneId]);
                            } catch (RemoteException e15) {
                                remove(r.binder);
                            }
                        }
                        if ((262144 & events) != 0) {
                            try {
                                r.callback.onDataActivationStateChanged(this.mDataActivationState[phoneId]);
                            } catch (RemoteException e16) {
                                remove(r.binder);
                            }
                        }
                        if ((524288 & events) != 0) {
                            try {
                                r.callback.onUserMobileDataStateChanged(this.mUserMobileDataState[phoneId]);
                            } catch (RemoteException e17) {
                                remove(r.binder);
                            }
                        }
                        if ((1048576 & events) != 0) {
                            try {
                                r.callback.onPhysicalChannelConfigurationChanged(this.mPhysicalChannelConfigs.get(phoneId));
                            } catch (RemoteException e18) {
                                remove(r.binder);
                            }
                        }
                    }
                } else {
                    return;
                }
            }
        } else {
            return;
        }
        return;
        return;
    }

    private String getCallIncomingNumber(Record record, int phoneId) {
        return record.canReadCallLog() ? this.mCallIncomingNumber[phoneId] : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    }

    private Record add(IBinder binder) {
        synchronized (this.mRecords) {
            int N = this.mRecords.size();
            for (int i = 0; i < N; i++) {
                Record r = this.mRecords.get(i);
                if (binder == r.binder) {
                    return r;
                }
            }
            Record r2 = new Record();
            r2.binder = binder;
            r2.deathRecipient = new TelephonyRegistryDeathRecipient(binder);
            try {
                binder.linkToDeath(r2.deathRecipient, 0);
                this.mRecords.add(r2);
                return r2;
            } catch (RemoteException e) {
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void remove(IBinder binder) {
        synchronized (this.mRecords) {
            int recordCount = this.mRecords.size();
            for (int i = 0; i < recordCount; i++) {
                Record r = this.mRecords.get(i);
                if (r.binder == binder) {
                    if (r.deathRecipient != null) {
                        try {
                            binder.unlinkToDeath(r.deathRecipient, 0);
                        } catch (NoSuchElementException e) {
                        }
                    }
                    this.mRecords.remove(i);
                    return;
                }
            }
        }
    }

    public void notifyCallState(int state, String phoneNumber) {
        if (checkNotifyPermission("notifyCallState()")) {
            synchronized (this.mRecords) {
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(32) && r.subId == Integer.MAX_VALUE) {
                        try {
                            r.callback.onCallStateChanged(state, r.canReadCallLog() ? phoneNumber : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastCallStateChanged(state, phoneNumber, -1, -1);
        }
    }

    public void notifyCallStateForPhoneId(int phoneId, int subId, int state, String incomingNumber) {
        if (checkNotifyPermission("notifyCallState()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCallState[phoneId] = state;
                    this.mCallIncomingNumber[phoneId] = incomingNumber;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
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
                this.mLocalLog.log("notifyServiceStateForSubscriber: subId=" + subId + " phoneId=" + phoneId + " state=" + state);
                if (validatePhoneId(phoneId)) {
                    this.mServiceState[phoneId] = state;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(1) && idMatch(r.subId, subId, phoneId)) {
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

    public void notifySimActivationStateChangedForPhoneId(int phoneId, int subId, int activationType, int activationState) {
        if (checkNotifyPermission("notifySimActivationState()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    switch (activationType) {
                        case 0:
                            this.mVoiceActivationState[phoneId] = activationState;
                            break;
                        case 1:
                            this.mDataActivationState[phoneId] = activationState;
                            break;
                        default:
                            return;
                    }
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (activationType == 0) {
                            try {
                                if (r.matchPhoneStateListenerEvent(131072) && idMatch(r.subId, subId, phoneId)) {
                                    r.callback.onVoiceActivationStateChanged(activationState);
                                }
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                        if (activationType == 1 && r.matchPhoneStateListenerEvent(262144) && idMatch(r.subId, subId, phoneId)) {
                            r.callback.onDataActivationStateChanged(activationState);
                        }
                    }
                } else {
                    log("notifySimActivationStateForPhoneId: INVALID phoneId=" + phoneId);
                }
                handleRemoveListLocked();
            }
        }
    }

    public void notifySignalStrengthForPhoneId(int phoneId, int subId, SignalStrength signalStrength) {
        if (checkNotifyPermission("notifySignalStrength()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mSignalStrength[phoneId] = signalStrength;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(256) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onSignalStrengthsChanged(new SignalStrength(signalStrength));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                        if (r.matchPhoneStateListenerEvent(2) && idMatch(r.subId, subId, phoneId)) {
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

    public void notifyCarrierNetworkChange(boolean active) {
        enforceNotifyPermissionOrCarrierPrivilege("notifyCarrierNetworkChange()");
        synchronized (this.mRecords) {
            this.mCarrierNetworkChangeState = active;
            Iterator<Record> it = this.mRecords.iterator();
            while (it.hasNext()) {
                Record r = it.next();
                if (r.matchPhoneStateListenerEvent(65536)) {
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
        notifyCellInfoForSubscriber(HwBootFail.STAGE_BOOT_SUCCESS, cellInfo);
    }

    public void notifyCellInfoForSubscriber(int subId, List<CellInfo> cellInfo) {
        if (checkNotifyPermission("notifyCellInfo()")) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCellInfo.set(phoneId, cellInfo);
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (validateEventsAndUserLocked(r, 1024) && idMatch(r.subId, subId, phoneId) && checkLocationAccess(r)) {
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

    public void notifyPhysicalChannelConfiguration(List<PhysicalChannelConfig> configs) {
        notifyPhysicalChannelConfigurationForSubscriber(HwBootFail.STAGE_BOOT_SUCCESS, configs);
    }

    public void notifyPhysicalChannelConfigurationForSubscriber(int subId, List<PhysicalChannelConfig> configs) {
        if (checkNotifyPermission("notifyPhysicalChannelConfiguration()")) {
            synchronized (this.mRecords) {
                int phoneId = SubscriptionManager.getPhoneId(subId);
                if (validatePhoneId(phoneId)) {
                    this.mPhysicalChannelConfigs.set(phoneId, configs);
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_DEXOPT) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onPhysicalChannelConfigurationChanged(configs);
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

    public void notifyMessageWaitingChangedForPhoneId(int phoneId, int subId, boolean mwi) {
        if (checkNotifyPermission("notifyMessageWaitingChanged()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mMessageWaiting[phoneId] = mwi;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
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

    public void notifyUserMobileDataStateChangedForPhoneId(int phoneId, int subId, boolean state) {
        if (checkNotifyPermission("notifyUserMobileDataStateChanged()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mUserMobileDataState[phoneId] = state;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_FROZEN) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onUserMobileDataStateChanged(state);
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
        notifyCallForwardingChangedForSubscriber(HwBootFail.STAGE_BOOT_SUCCESS, cfi);
    }

    public void notifyCallForwardingChangedForSubscriber(int subId, boolean cfi) {
        if (checkNotifyPermission("notifyCallForwardingChanged()")) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCallForwarding[phoneId] = cfi;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
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
        notifyDataActivityForSubscriber(HwBootFail.STAGE_BOOT_SUCCESS, state);
    }

    public void notifyDataActivityForSubscriber(int subId, int state) {
        if (checkNotifyPermission("notifyDataActivity()")) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mDataActivity[phoneId] = state;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(128) && idMatch(r.subId, subId, phoneId)) {
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

    public void notifyDataConnection(int state, boolean isDataAllowed, String reason, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int networkType, boolean roaming) {
        notifyDataConnectionForSubscriber(HwBootFail.STAGE_BOOT_SUCCESS, state, isDataAllowed, reason, apn, apnType, linkProperties, networkCapabilities, networkType, roaming);
    }

    public void notifyDataConnectionForSubscriber(int subId, int state, boolean isDataAllowed, String reason, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int networkType, boolean roaming) {
        Record r;
        int i = state;
        String str = apnType;
        int i2 = networkType;
        if (checkNotifyPermission("notifyDataConnection()")) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                try {
                    if (validatePhoneId(phoneId)) {
                        boolean modified = false;
                        if (SHOW_DATA_ACT_ALL_APN) {
                            if (i == 2) {
                                if (!this.mConnectedApns[phoneId].contains(str) && !"ims".equals(str) && !"xcap".equals(str) && !"internaldefault".equals(str)) {
                                    this.mConnectedApns[phoneId].add(str);
                                    if (this.mDataConnectionState[phoneId] != i) {
                                        this.mDataConnectionState[phoneId] = i;
                                        modified = true;
                                    }
                                }
                            } else if (this.mConnectedApns[phoneId].remove(str) && this.mConnectedApns[phoneId].isEmpty()) {
                                this.mDataConnectionState[phoneId] = i;
                                modified = true;
                            }
                            if (this.mDataConnectionNetworkType[phoneId] != i2) {
                                this.mDataConnectionNetworkType[phoneId] = i2;
                                modified = true;
                            }
                        } else if (BatteryService.HealthServiceWrapper.INSTANCE_VENDOR.equals(str) && !(this.mDataConnectionState[phoneId] == i && this.mDataConnectionNetworkType[phoneId] == i2)) {
                            modified = true;
                            this.mDataConnectionState[phoneId] = i;
                            this.mDataConnectionNetworkType[phoneId] = i2;
                        }
                        if (modified) {
                            try {
                                String str2 = "onDataConnectionStateChanged(" + this.mDataConnectionState[phoneId] + ", " + this.mDataConnectionNetworkType[phoneId] + ")";
                                log(str2);
                                this.mLocalLog.log(str2);
                                Iterator<Record> it = this.mRecords.iterator();
                                while (it.hasNext()) {
                                    r = it.next();
                                    if (!r.matchPhoneStateListenerEvent(64)) {
                                        int i3 = subId;
                                    } else if (idMatch(r.subId, subId, phoneId)) {
                                        r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
                                    }
                                }
                                int i4 = subId;
                                handleRemoveListLocked();
                            } catch (Throwable th) {
                                th = th;
                                int i5 = subId;
                                throw th;
                            }
                        } else {
                            int i6 = subId;
                        }
                        PreciseDataConnectionState preciseDataConnectionState = new PreciseDataConnectionState(i, i2, str, apn, reason, linkProperties, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        this.mPreciseDataConnectionState = preciseDataConnectionState;
                        Iterator<Record> it2 = this.mRecords.iterator();
                        while (it2.hasNext()) {
                            Record r2 = it2.next();
                            if (r2.matchPhoneStateListenerEvent(4096)) {
                                try {
                                    r2.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState);
                                } catch (RemoteException e) {
                                    this.mRemoveList.add(r2.binder);
                                }
                            }
                        }
                    }
                    handleRemoveListLocked();
                    int i7 = i;
                    String str3 = apn;
                    LinkProperties linkProperties2 = linkProperties;
                    broadcastDataConnectionStateChanged(i7, isDataAllowed, reason, str3, str, linkProperties2, networkCapabilities, roaming, subId);
                    broadcastPreciseDataConnectionStateChanged(i7, i2, str, str3, reason, linkProperties2, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                } catch (RemoteException e2) {
                    this.mRemoveList.add(r.binder);
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }
    }

    public void notifyDataConnectionFailed(String reason, String apnType) {
        notifyDataConnectionFailedForSubscriber(HwBootFail.STAGE_BOOT_SUCCESS, reason, apnType);
    }

    public void notifyDataConnectionFailedForSubscriber(int subId, String reason, String apnType) {
        if (checkNotifyPermission("notifyDataConnectionFailed()")) {
            synchronized (this.mRecords) {
                PreciseDataConnectionState preciseDataConnectionState = new PreciseDataConnectionState(-1, 0, apnType, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, reason, null, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                this.mPreciseDataConnectionState = preciseDataConnectionState;
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(4096)) {
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
            broadcastPreciseDataConnectionStateChanged(-1, 0, apnType, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, reason, null, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        }
    }

    public void notifyCellLocation(Bundle cellLocation) {
        notifyCellLocationForSubscriber(HwBootFail.STAGE_BOOT_SUCCESS, cellLocation);
    }

    public void notifyCellLocationForSubscriber(int subId, Bundle cellLocation) {
        log("notifyCellLocationForSubscriber: subId=" + subId + " cellLocation=" + cellLocation);
        if (checkNotifyPermission("notifyCellLocation()")) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCellLocation[phoneId] = cellLocation;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (validateEventsAndUserLocked(r, 16) && idMatch(r.subId, subId, phoneId) && checkLocationAccess(r)) {
                            try {
                                Bundle bundle = new Bundle(cellLocation);
                                bundle.putInt("SubId", subId);
                                r.callback.onCellLocationChanged(bundle);
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

    public void notifyOtaspChanged(int otaspMode) {
        if (checkNotifyPermission("notifyOtaspChanged()")) {
            synchronized (this.mRecords) {
                this.mOtaspMode = otaspMode;
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(512)) {
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

    public void notifyPreciseCallState(int ringingCallState, int foregroundCallState, int backgroundCallState) {
        if (checkNotifyPermission("notifyPreciseCallState()")) {
            synchronized (this.mRecords) {
                this.mRingingCallState = ringingCallState;
                this.mForegroundCallState = foregroundCallState;
                this.mBackgroundCallState = backgroundCallState;
                PreciseCallState preciseCallState = new PreciseCallState(ringingCallState, foregroundCallState, backgroundCallState, -1, -1);
                this.mPreciseCallState = preciseCallState;
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(2048)) {
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

    public void notifyDisconnectCause(int disconnectCause, int preciseDisconnectCause) {
        if (checkNotifyPermission("notifyDisconnectCause()")) {
            synchronized (this.mRecords) {
                PreciseCallState preciseCallState = new PreciseCallState(this.mRingingCallState, this.mForegroundCallState, this.mBackgroundCallState, disconnectCause, preciseDisconnectCause);
                this.mPreciseCallState = preciseCallState;
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(2048)) {
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

    public void notifyPreciseDataConnectionFailed(String reason, String apnType, String apn, String failCause) {
        if (checkNotifyPermission("notifyPreciseDataConnectionFailed()")) {
            synchronized (this.mRecords) {
                PreciseDataConnectionState preciseDataConnectionState = new PreciseDataConnectionState(-1, 0, apnType, apn, reason, null, failCause);
                this.mPreciseDataConnectionState = preciseDataConnectionState;
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(4096)) {
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

    public void notifyVoLteServiceStateChanged(VoLteServiceState lteState) {
        if (checkNotifyPermission("notifyVoLteServiceStateChanged()")) {
            synchronized (this.mRecords) {
                this.mVoLteServiceState = lteState;
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(16384)) {
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

    public void notifyOemHookRawEventForSubscriber(int subId, byte[] rawData) {
        if (checkNotifyPermission("notifyOemHookRawEventForSubscriber")) {
            synchronized (this.mRecords) {
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(32768) && (r.subId == subId || r.subId == Integer.MAX_VALUE)) {
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

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mRecords) {
                int recordCount = this.mRecords.size();
                pw.println("last known state:");
                pw.increaseIndent();
                for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                    pw.println("Phone Id=" + i);
                    pw.increaseIndent();
                    pw.println("mCallState=" + this.mCallState[i]);
                    pw.println("mCallIncomingNumber=" + this.mCallIncomingNumber[i]);
                    pw.println("mServiceState=" + this.mServiceState[i]);
                    pw.println("mVoiceActivationState= " + this.mVoiceActivationState[i]);
                    pw.println("mDataActivationState= " + this.mDataActivationState[i]);
                    pw.println("mUserMobileDataState= " + this.mUserMobileDataState[i]);
                    pw.println("mSignalStrength=" + this.mSignalStrength[i]);
                    pw.println("mMessageWaiting=" + this.mMessageWaiting[i]);
                    pw.println("mCallForwarding=" + this.mCallForwarding[i]);
                    pw.println("mDataActivity=" + this.mDataActivity[i]);
                    pw.println("mDataConnectionState=" + this.mDataConnectionState[i]);
                    pw.println("mCellLocation=" + this.mCellLocation[i]);
                    pw.println("mCellInfo=" + this.mCellInfo.get(i));
                    pw.decreaseIndent();
                }
                pw.println("mConnectedApns=" + Arrays.toString(this.mConnectedApns));
                pw.println("mPreciseDataConnectionState=" + this.mPreciseDataConnectionState);
                pw.println("mPreciseCallState=" + this.mPreciseCallState);
                pw.println("mCarrierNetworkChangeState=" + this.mCarrierNetworkChangeState);
                pw.println("mRingingCallState=" + this.mRingingCallState);
                pw.println("mForegroundCallState=" + this.mForegroundCallState);
                pw.println("mBackgroundCallState=" + this.mBackgroundCallState);
                pw.println("mVoLteServiceState=" + this.mVoLteServiceState);
                pw.decreaseIndent();
                pw.println("local logs:");
                pw.increaseIndent();
                this.mLocalLog.dump(fd, pw, args);
                pw.decreaseIndent();
                pw.println("registrations: count=" + recordCount);
                pw.increaseIndent();
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    pw.println(it.next());
                }
                pw.decreaseIndent();
            }
        }
    }

    private void broadcastServiceStateChanged(ServiceState state, int phoneId, int subId) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.notePhoneState(state.getState());
        } catch (RemoteException e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        Binder.restoreCallingIdentity(ident);
        Intent intent = new Intent("android.intent.action.SERVICE_STATE");
        intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
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
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        Binder.restoreCallingIdentity(ident);
        Intent intent = new Intent("android.intent.action.SIG_STR");
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
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            this.mBatteryStats.notePhoneOn();
        }
        Binder.restoreCallingIdentity(ident);
        Intent intent = new Intent("android.intent.action.PHONE_STATE");
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, PhoneConstantConversions.convertCallState(state).toString());
        if (subId != -1) {
            intent.setAction("android.intent.action.SUBSCRIPTION_PHONE_STATE");
            intent.putExtra("subscription", subId);
        }
        if (phoneId != -1) {
            intent.putExtra("slot", phoneId);
        }
        intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
        Intent intentWithPhoneNumber = new Intent(intent);
        if (!TextUtils.isEmpty(incomingNumber)) {
            intentWithPhoneNumber.putExtra("incoming_number", incomingNumber);
        }
        this.mContext.sendBroadcastAsUser(intentWithPhoneNumber, UserHandle.ALL, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PHONE_STATE", 51);
        this.mContext.sendBroadcastAsUserMultiplePermissions(intentWithPhoneNumber, UserHandle.ALL, new String[]{"android.permission.READ_PHONE_STATE", "android.permission.READ_CALL_LOG"});
    }

    private void broadcastDataConnectionStateChanged(int state, boolean isDataAllowed, String reason, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, boolean roaming, int subId) {
        Intent intent = new Intent("android.intent.action.ANY_DATA_STATE");
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, PhoneConstantConversions.convertDataState(state).toString());
        if (!isDataAllowed) {
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
            TelephonyPermissions.enforceCallingOrSelfCarrierPrivilege(SubscriptionManager.getDefaultSubscriptionId(), method);
        }
    }

    private boolean checkNotifyPermission(String method) {
        if (checkNotifyPermission()) {
            return true;
        }
        String str = "Modify Phone State Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
        return false;
    }

    private boolean checkNotifyPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0;
    }

    private boolean checkListenerPermission(int events, int subId, String callingPackage, String message) {
        if ((events & ENFORCE_COARSE_LOCATION_PERMISSION_MASK) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION", null);
            if (this.mAppOps.noteOp(0, Binder.getCallingUid(), callingPackage) != 0) {
                return false;
            }
        }
        if ((events & ENFORCE_PHONE_STATE_PERMISSION_MASK) != 0 && !TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subId, callingPackage, message)) {
            return false;
        }
        if ((events & PRECISE_PHONE_STATE_PERMISSION_MASK) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRECISE_PHONE_STATE", null);
        }
        if ((32768 & events) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void handleRemoveListLocked() {
        if (this.mRemoveList.size() > 0) {
            Iterator<IBinder> it = this.mRemoveList.iterator();
            while (it.hasNext()) {
                remove(it.next());
            }
            this.mRemoveList.clear();
        }
    }

    private boolean validateEventsAndUserLocked(Record r, int events) {
        long callingIdentity = Binder.clearCallingIdentity();
        boolean valid = false;
        try {
            if (UserHandle.getUserId(r.callerUid) == ActivityManager.getCurrentUser() && r.matchPhoneStateListenerEvent(events)) {
                valid = true;
            }
            return valid;
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    /* access modifiers changed from: private */
    public boolean validatePhoneId(int phoneId) {
        return phoneId >= 0 && phoneId < this.mNumPhones;
    }

    private static void log(String s) {
        Rlog.d(TAG, s);
    }

    /* access modifiers changed from: package-private */
    public boolean idMatch(int rSubId, int subId, int phoneId) {
        boolean z = false;
        if (subId < 0) {
            if (this.mDefaultPhoneId == phoneId) {
                z = true;
            }
            return z;
        } else if (rSubId == Integer.MAX_VALUE) {
            return true;
        } else {
            if (rSubId == subId) {
                z = true;
            }
            return z;
        }
    }

    private boolean checkLocationAccess(Record r) {
        long token = Binder.clearCallingIdentity();
        try {
            return LocationAccessPolicy.canAccessCellLocation(this.mContext, r.callingPackage, r.callerUid, r.callerPid, false);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: private */
    public void checkPossibleMissNotify(Record r, int phoneId) {
        int events = r.events;
        if ((events & 1) != 0) {
            try {
                r.callback.onServiceStateChanged(new ServiceState(this.mServiceState[phoneId]));
            } catch (RemoteException e) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 256) != 0) {
            try {
                r.callback.onSignalStrengthsChanged(new SignalStrength(this.mSignalStrength[phoneId]));
            } catch (RemoteException e2) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 2) != 0) {
            try {
                int gsmSignalStrength = this.mSignalStrength[phoneId].getGsmSignalStrength();
                r.callback.onSignalStrengthChanged(gsmSignalStrength == 99 ? -1 : gsmSignalStrength);
            } catch (RemoteException e3) {
                this.mRemoveList.add(r.binder);
            }
        }
        if (validateEventsAndUserLocked(r, 1024)) {
            try {
                if (checkLocationAccess(r)) {
                    r.callback.onCellInfoChanged(this.mCellInfo.get(phoneId));
                }
            } catch (RemoteException e4) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((524288 & events) != 0) {
            try {
                r.callback.onUserMobileDataStateChanged(this.mUserMobileDataState[phoneId]);
            } catch (RemoteException e5) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 4) != 0) {
            try {
                r.callback.onMessageWaitingIndicatorChanged(this.mMessageWaiting[phoneId]);
            } catch (RemoteException e6) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 8) != 0) {
            try {
                r.callback.onCallForwardingIndicatorChanged(this.mCallForwarding[phoneId]);
            } catch (RemoteException e7) {
                this.mRemoveList.add(r.binder);
            }
        }
        if (validateEventsAndUserLocked(r, 16)) {
            try {
                if (checkLocationAccess(r)) {
                    r.callback.onCellLocationChanged(new Bundle(this.mCellLocation[phoneId]));
                }
            } catch (RemoteException e8) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 64) != 0) {
            try {
                r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
            } catch (RemoteException e9) {
                this.mRemoveList.add(r.binder);
            }
        }
    }
}
