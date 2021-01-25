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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.CallAttributes;
import android.telephony.CallQuality;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.LocationAccessPolicy;
import android.telephony.PhoneCapability;
import android.telephony.PhysicalChannelConfig;
import android.telephony.PreciseCallState;
import android.telephony.PreciseDataConnectionState;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import android.telephony.data.ApnSetting;
import android.telephony.emergency.EmergencyNumber;
import android.telephony.ims.ImsReasonInfo;
import android.util.LocalLog;
import android.util.StatsLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.IPhoneStateListener;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstantConversions;
import com.android.internal.telephony.TelephonyPermissions;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.BatteryService;
import com.android.server.TelephonyRegistry;
import com.android.server.am.BatteryStatsService;
import com.android.server.pm.DumpState;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class TelephonyRegistry extends ITelephonyRegistry.Stub {
    private static final boolean DBG;
    private static final boolean DBG_LOC;
    static final int ENFORCE_COARSE_LOCATION_PERMISSION_MASK = 0;
    static final int ENFORCE_FINE_LOCATION_PERMISSION_MASK = 1040;
    static final int ENFORCE_PHONE_STATE_PERMISSION_MASK = 16777228;
    private static final boolean HW_DEBUG = (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("persist.telephony.test.registry", false));
    private static final boolean IS_NR_SLICES_SUPPORTED = HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported();
    private static final int MSG_UPDATE_DEFAULT_SUB = 2;
    private static final int MSG_USER_SWITCHED = 1;
    static final int PRECISE_PHONE_STATE_PERMISSION_MASK = 6144;
    private static final boolean SHOW_DATA_ACT_ALL_APN = SystemProperties.getBoolean("ro.config.show_data_act_all_apn", false);
    private static final String TAG = "TelephonyRegistry";
    private static final boolean VDBG;
    private int mActiveDataSubId = -1;
    private final AppOpsManager mAppOps;
    private int[] mBackgroundCallState;
    private final IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.TelephonyRegistry.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TelephonyRegistry.VDBG) {
                TelephonyRegistry.log("mBroadcastReceiver: action=" + action);
            }
            if ("android.intent.action.USER_SWITCHED".equals(action)) {
                int userHandle = intent.getIntExtra("android.intent.extra.user_handle", 0);
                if (TelephonyRegistry.DBG) {
                    TelephonyRegistry.log("onReceive: userHandle=" + userHandle);
                }
                TelephonyRegistry.this.mHandler.sendMessage(TelephonyRegistry.this.mHandler.obtainMessage(1, userHandle, 0));
            } else if (action.equals("android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED")) {
                Integer newDefaultSubIdObj = new Integer(intent.getIntExtra("subscription", SubscriptionManager.getDefaultSubscriptionId()));
                int newDefaultPhoneId = intent.getIntExtra("phone", SubscriptionManager.getPhoneId(TelephonyRegistry.this.mDefaultSubId));
                if (TelephonyRegistry.DBG) {
                    TelephonyRegistry.log("onReceive:current mDefaultSubId=" + TelephonyRegistry.this.mDefaultSubId + " current mDefaultPhoneId=" + TelephonyRegistry.this.mDefaultPhoneId + " newDefaultSubId= " + newDefaultSubIdObj + " newDefaultPhoneId=" + newDefaultPhoneId);
                }
                if (!TelephonyRegistry.this.validatePhoneId(newDefaultPhoneId)) {
                    return;
                }
                if (!newDefaultSubIdObj.equals(Integer.valueOf(TelephonyRegistry.this.mDefaultSubId)) || newDefaultPhoneId != TelephonyRegistry.this.mDefaultPhoneId) {
                    TelephonyRegistry.this.mHandler.sendMessage(TelephonyRegistry.this.mHandler.obtainMessage(2, newDefaultPhoneId, 0, newDefaultSubIdObj));
                }
            }
        }
    };
    private CallAttributes[] mCallAttributes;
    private int[] mCallDisconnectCause;
    private boolean[] mCallForwarding;
    private String[] mCallIncomingNumber;
    private int[] mCallNetworkType;
    private int[] mCallPreciseDisconnectCause;
    private CallQuality[] mCallQuality;
    private int[] mCallState;
    private boolean mCarrierNetworkChangeState = false;
    private ArrayList<List<CellInfo>> mCellInfo = null;
    private Bundle[] mCellLocation;
    private ArrayList<String>[] mConnectedApns;
    private final Context mContext;
    private int[] mDataActivationState;
    private int[] mDataActivity;
    private int[] mDataConnectionNetworkType;
    private int[] mDataConnectionState;
    private int mDefaultPhoneId = -1;
    private int mDefaultSubId = -1;
    private Map<Integer, List<EmergencyNumber>> mEmergencyNumberList;
    private int[] mForegroundCallState;
    private final Handler mHandler = new Handler() {
        /* class com.android.server.TelephonyRegistry.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                if (TelephonyRegistry.VDBG) {
                    TelephonyRegistry.log("MSG_USER_SWITCHED userId=" + msg.arg1);
                }
                int numPhones = TelephonyManager.getDefault().getPhoneCount();
                for (int sub = 0; sub < numPhones; sub++) {
                    TelephonyRegistry telephonyRegistry = TelephonyRegistry.this;
                    telephonyRegistry.notifyCellLocationForSubscriber(sub, telephonyRegistry.mCellLocation[sub]);
                }
            } else if (i == 2) {
                int newDefaultPhoneId = msg.arg1;
                int newDefaultSubId = ((Integer) msg.obj).intValue();
                if (TelephonyRegistry.VDBG) {
                    TelephonyRegistry.log("MSG_UPDATE_DEFAULT_SUB:current mDefaultSubId=" + TelephonyRegistry.this.mDefaultSubId + " current mDefaultPhoneId=" + TelephonyRegistry.this.mDefaultPhoneId + " newDefaultSubId= " + newDefaultSubId + " newDefaultPhoneId=" + newDefaultPhoneId);
                }
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
                TelephonyRegistry.this.mDefaultSubId = newDefaultSubId;
                TelephonyRegistry.this.mDefaultPhoneId = newDefaultPhoneId;
                if (TelephonyRegistry.VDBG) {
                    LocalLog localLog = TelephonyRegistry.this.mLocalLog;
                    localLog.log("Default subscription updated: mDefaultPhoneId=" + TelephonyRegistry.this.mDefaultPhoneId + ", mDefaultSubId" + TelephonyRegistry.this.mDefaultSubId);
                }
            }
        }
    };
    private boolean mHasNotifyOpportunisticSubscriptionInfoChangedOccurred = false;
    private boolean mHasNotifySubscriptionInfoChangedOccurred = false;
    private List<ImsReasonInfo> mImsReasonInfo = null;
    private final LocalLog mListenLog = new LocalLog(100);
    private final LocalLog mLocalLog = new LocalLog(100);
    private boolean[] mMessageWaiting;
    private int mNumPhones;
    private int[] mOtaspMode;
    private PhoneCapability mPhoneCapability = null;
    private ArrayList<List<PhysicalChannelConfig>> mPhysicalChannelConfigs;
    private PreciseCallState[] mPreciseCallState;
    private PreciseDataConnectionState[] mPreciseDataConnectionState;
    private int mRadioPowerState = 2;
    private final ArrayList<Record> mRecords = new ArrayList<>();
    private final ArrayList<IBinder> mRemoveList = new ArrayList<>();
    private int[] mRingingCallState;
    private ServiceState[] mServiceState;
    private SignalStrength[] mSignalStrength;
    private int[] mSrvccState;
    private boolean[] mUserMobileDataState;
    private VoLteServiceState mVoLteServiceState = new VoLteServiceState();
    private int[] mVoiceActivationState;

    static {
        boolean z = HW_DEBUG;
        DBG = z;
        DBG_LOC = z;
        VDBG = z;
    }

    /* access modifiers changed from: private */
    public static class Record {
        IBinder binder;
        IPhoneStateListener callback;
        int callerPid;
        int callerUid;
        String callingPackage;
        Context context;
        TelephonyRegistryDeathRecipient deathRecipient;
        int events;
        IOnSubscriptionsChangedListener onOpportunisticSubscriptionsChangedListenerCallback;
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
        public boolean matchOnOpportunisticSubscriptionsChangedListener() {
            return this.onOpportunisticSubscriptionsChangedListenerCallback != null;
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
            return "{callingPackage=" + this.callingPackage + " binder=" + this.binder + " callback=" + this.callback + " onSubscriptionsChangedListenererCallback=" + this.onSubscriptionsChangedListenerCallback + " onOpportunisticSubscriptionsChangedListenererCallback=" + this.onOpportunisticSubscriptionsChangedListenerCallback + " callerUid=" + this.callerUid + " subId=" + this.subId + " phoneId=" + this.phoneId + " events=" + Integer.toHexString(this.events) + "}";
        }
    }

    /* access modifiers changed from: private */
    public class TelephonyRegistryDeathRecipient implements IBinder.DeathRecipient {
        private final IBinder binder;

        TelephonyRegistryDeathRecipient(IBinder binder2) {
            this.binder = binder2;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            if (TelephonyRegistry.DBG) {
                TelephonyRegistry.log("binderDied " + this.binder);
            }
            TelephonyRegistry.this.remove(this.binder);
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public TelephonyRegistry(Context context) {
        CellLocation location = CellLocation.getEmpty();
        this.mContext = context;
        this.mBatteryStats = BatteryStatsService.getService();
        int numPhones = VSimTelephonyRegistry.processVSimPhoneNumbers(TelephonyManager.getDefault().getPhoneCount());
        if (DBG) {
            log("TelephonyRegistry: ctor numPhones=" + numPhones);
        }
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
        this.mSrvccState = new int[numPhones];
        this.mOtaspMode = new int[numPhones];
        this.mPreciseCallState = new PreciseCallState[numPhones];
        this.mForegroundCallState = new int[numPhones];
        this.mBackgroundCallState = new int[numPhones];
        this.mRingingCallState = new int[numPhones];
        this.mCallDisconnectCause = new int[numPhones];
        this.mCallPreciseDisconnectCause = new int[numPhones];
        this.mCallQuality = new CallQuality[numPhones];
        this.mCallNetworkType = new int[numPhones];
        this.mCallAttributes = new CallAttributes[numPhones];
        this.mPreciseDataConnectionState = new PreciseDataConnectionState[numPhones];
        this.mCellInfo = new ArrayList<>();
        this.mImsReasonInfo = new ArrayList();
        this.mPhysicalChannelConfigs = new ArrayList<>();
        this.mEmergencyNumberList = new HashMap();
        for (int i = 0; i < numPhones; i++) {
            this.mCallState[i] = 0;
            this.mDataActivity[i] = 0;
            this.mDataConnectionState[i] = -1;
            this.mVoiceActivationState[i] = 0;
            this.mDataActivationState[i] = 0;
            this.mCallIncomingNumber[i] = "";
            this.mServiceState[i] = new ServiceState();
            this.mSignalStrength[i] = new SignalStrength();
            this.mUserMobileDataState[i] = false;
            this.mMessageWaiting[i] = false;
            this.mCallForwarding[i] = false;
            this.mCellLocation[i] = new Bundle();
            this.mCellInfo.add(i, null);
            this.mImsReasonInfo.add(i, null);
            this.mSrvccState[i] = -1;
            this.mConnectedApns[i] = new ArrayList<>();
            this.mPhysicalChannelConfigs.add(i, new ArrayList());
            this.mOtaspMode[i] = 1;
            this.mCallDisconnectCause[i] = -1;
            this.mCallPreciseDisconnectCause[i] = -1;
            this.mCallQuality[i] = new CallQuality();
            this.mCallAttributes[i] = new CallAttributes(new PreciseCallState(), 0, new CallQuality());
            this.mCallNetworkType[i] = 0;
            this.mPreciseCallState[i] = new PreciseCallState();
            this.mRingingCallState[i] = 0;
            this.mForegroundCallState[i] = 0;
            this.mBackgroundCallState[i] = 0;
            this.mPreciseDataConnectionState[i] = new PreciseDataConnectionState();
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
        int callerUserId = UserHandle.getCallingUserId();
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        if (VDBG) {
            log("listen oscl: E pkg=" + callingPackage + " myUserId=" + UserHandle.myUserId() + " callerUserId=" + callerUserId + " callback=" + callback + " callback.asBinder=" + callback.asBinder());
        }
        synchronized (this.mRecords) {
            Record r = add(callback.asBinder());
            if (r != null) {
                r.context = this.mContext;
                r.onSubscriptionsChangedListenerCallback = callback;
                r.callingPackage = callingPackage;
                r.callerUid = Binder.getCallingUid();
                r.callerPid = Binder.getCallingPid();
                r.events = 0;
                if (DBG) {
                    log("listen oscl:  Register r=" + r);
                }
                if (this.mHasNotifySubscriptionInfoChangedOccurred) {
                    try {
                        if (VDBG) {
                            log("listen oscl: send to r=" + r);
                        }
                        r.onSubscriptionsChangedListenerCallback.onSubscriptionsChanged();
                        if (VDBG) {
                            log("listen oscl: sent to r=" + r);
                        }
                    } catch (RemoteException e) {
                        if (VDBG) {
                            log("listen oscl: remote exception sending to r=" + r + " e=" + e);
                        }
                        remove(r.binder);
                    }
                } else {
                    log("listen oscl: mHasNotifySubscriptionInfoChangedOccurred==false no callback");
                }
            }
        }
    }

    public void removeOnSubscriptionsChangedListener(String pkgForDebug, IOnSubscriptionsChangedListener callback) {
        if (DBG) {
            log("listen oscl: Unregister");
        }
        remove(callback.asBinder());
    }

    public void addOnOpportunisticSubscriptionsChangedListener(String callingPackage, IOnSubscriptionsChangedListener callback) {
        int callerUserId = UserHandle.getCallingUserId();
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        if (VDBG) {
            log("listen ooscl: E pkg=" + callingPackage + " myUserId=" + UserHandle.myUserId() + " callerUserId=" + callerUserId + " callback=" + callback + " callback.asBinder=" + callback.asBinder());
        }
        synchronized (this.mRecords) {
            Record r = add(callback.asBinder());
            if (r != null) {
                r.context = this.mContext;
                r.onOpportunisticSubscriptionsChangedListenerCallback = callback;
                r.callingPackage = callingPackage;
                r.callerUid = Binder.getCallingUid();
                r.callerPid = Binder.getCallingPid();
                r.events = 0;
                if (DBG) {
                    log("listen ooscl:  Register r=" + r);
                }
                if (this.mHasNotifyOpportunisticSubscriptionInfoChangedOccurred) {
                    try {
                        if (VDBG) {
                            log("listen ooscl: send to r=" + r);
                        }
                        r.onOpportunisticSubscriptionsChangedListenerCallback.onSubscriptionsChanged();
                        if (VDBG) {
                            log("listen ooscl: sent to r=" + r);
                        }
                    } catch (RemoteException e) {
                        if (VDBG) {
                            log("listen ooscl: remote exception sending to r=" + r + " e=" + e);
                        }
                        remove(r.binder);
                    }
                } else {
                    log("listen ooscl: hasNotifyOpptSubInfoChangedOccurred==false no callback");
                }
            }
        }
    }

    public void notifySubscriptionInfoChanged() {
        if (VDBG) {
            log("notifySubscriptionInfoChanged:");
        }
        synchronized (this.mRecords) {
            if (!this.mHasNotifySubscriptionInfoChangedOccurred) {
                log("notifySubscriptionInfoChanged: first invocation mRecords.size=" + this.mRecords.size());
            }
            this.mHasNotifySubscriptionInfoChangedOccurred = true;
            this.mRemoveList.clear();
            Iterator<Record> it = this.mRecords.iterator();
            while (it.hasNext()) {
                Record r = it.next();
                if (r.matchOnSubscriptionsChangedListener()) {
                    try {
                        if (VDBG) {
                            log("notifySubscriptionInfoChanged: call osc to r=" + r);
                        }
                        r.onSubscriptionsChangedListenerCallback.onSubscriptionsChanged();
                        if (VDBG) {
                            log("notifySubscriptionInfoChanged: done osc to r=" + r);
                        }
                    } catch (RemoteException e) {
                        if (VDBG) {
                            log("notifySubscriptionInfoChanged: RemoteException r=" + r);
                        }
                        this.mRemoveList.add(r.binder);
                    }
                }
            }
            handleRemoveListLocked();
        }
    }

    public void notifyOpportunisticSubscriptionInfoChanged() {
        if (VDBG) {
            log("notifyOpptSubscriptionInfoChanged:");
        }
        synchronized (this.mRecords) {
            if (!this.mHasNotifyOpportunisticSubscriptionInfoChangedOccurred) {
                log("notifyOpptSubscriptionInfoChanged: first invocation mRecords.size=" + this.mRecords.size());
            }
            this.mHasNotifyOpportunisticSubscriptionInfoChangedOccurred = true;
            this.mRemoveList.clear();
            Iterator<Record> it = this.mRecords.iterator();
            while (it.hasNext()) {
                Record r = it.next();
                if (r.matchOnOpportunisticSubscriptionsChangedListener()) {
                    try {
                        if (VDBG) {
                            log("notifyOpptSubChanged: call oosc to r=" + r);
                        }
                        r.onOpportunisticSubscriptionsChangedListenerCallback.onSubscriptionsChanged();
                        if (VDBG) {
                            log("notifyOpptSubChanged: done oosc to r=" + r);
                        }
                    } catch (RemoteException e) {
                        if (VDBG) {
                            log("notifyOpptSubChanged: RemoteException r=" + r);
                        }
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
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONYREGISTRY_LISTENFORSUBSCRIBER);
        }
        listen(pkgForDebug, callback, events, notifyNow, subId);
    }

    private void listen(String callingPackage, IPhoneStateListener callback, int events, boolean notifyNow, int subId) {
        int i;
        int callerUserId = UserHandle.getCallingUserId();
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        if (VDBG) {
            String str = "listen: E pkg=" + callingPackage + " events=0x" + Integer.toHexString(events) + " notifyNow=" + notifyNow + " subId=" + subId + " myUserId=" + UserHandle.myUserId() + " callerUserId=" + callerUserId;
            this.mListenLog.log(str);
            log(str);
        }
        if (events == 0) {
            if (DBG) {
                log("listen: Unregister");
            }
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
                        r.subId = Integer.MAX_VALUE;
                    } else {
                        r.subId = subId;
                    }
                    r.phoneId = phoneId;
                    r.events = events;
                    if (DBG) {
                        log("listen:  Register r=" + r + " r.subId=" + r.subId + " phoneId=" + phoneId);
                    }
                    if (notifyNow && validatePhoneId(phoneId)) {
                        if ((events & 1) != 0) {
                            try {
                                if (VDBG) {
                                    log("listen: call onSSC state=" + this.mServiceState[phoneId]);
                                }
                                ServiceState rawSs = new ServiceState(this.mServiceState[phoneId]);
                                if (checkFineLocationAccess(r, 29)) {
                                    r.callback.onServiceStateChanged(rawSs);
                                } else if (checkCoarseLocationAccess(r, 29)) {
                                    r.callback.onServiceStateChanged(rawSs.sanitizeLocationInfo(false));
                                } else {
                                    r.callback.onServiceStateChanged(rawSs.sanitizeLocationInfo(true));
                                }
                            } catch (RemoteException e) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 2) != 0) {
                            try {
                                int gsmSignalStrength = this.mSignalStrength[phoneId].getGsmSignalStrength();
                                IPhoneStateListener iPhoneStateListener = r.callback;
                                if (gsmSignalStrength == 99) {
                                    i = -1;
                                } else {
                                    i = gsmSignalStrength;
                                }
                                iPhoneStateListener.onSignalStrengthChanged(i);
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
                                if (DBG_LOC) {
                                    log("listen: mCellLocation = " + this.mCellLocation[phoneId]);
                                }
                                if (checkFineLocationAccess(r, 29)) {
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
                                r.callback.onOtaspChanged(this.mOtaspMode[phoneId]);
                            } catch (RemoteException e10) {
                                remove(r.binder);
                            }
                        }
                        if (validateEventsAndUserLocked(r, 1024)) {
                            try {
                                if (DBG_LOC) {
                                    log("listen: mCellInfo[" + phoneId + "] = " + this.mCellInfo.get(phoneId));
                                }
                                if (checkFineLocationAccess(r, 29)) {
                                    r.callback.onCellInfoChanged(this.mCellInfo.get(phoneId));
                                }
                            } catch (RemoteException e11) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 2048) != 0) {
                            try {
                                r.callback.onPreciseCallStateChanged(this.mPreciseCallState[phoneId]);
                            } catch (RemoteException e12) {
                                remove(r.binder);
                            }
                        }
                        if ((33554432 & events) != 0) {
                            try {
                                r.callback.onCallDisconnectCauseChanged(this.mCallDisconnectCause[phoneId], this.mCallPreciseDisconnectCause[phoneId]);
                            } catch (RemoteException e13) {
                                remove(r.binder);
                            }
                        }
                        if ((134217728 & events) != 0) {
                            try {
                                r.callback.onImsCallDisconnectCauseChanged(this.mImsReasonInfo.get(phoneId));
                            } catch (RemoteException e14) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 4096) != 0) {
                            try {
                                r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState[phoneId]);
                            } catch (RemoteException e15) {
                                remove(r.binder);
                            }
                        }
                        if ((65536 & events) != 0) {
                            try {
                                r.callback.onCarrierNetworkChange(this.mCarrierNetworkChangeState);
                            } catch (RemoteException e16) {
                                remove(r.binder);
                            }
                        }
                        if ((131072 & events) != 0) {
                            try {
                                r.callback.onVoiceActivationStateChanged(this.mVoiceActivationState[phoneId]);
                            } catch (RemoteException e17) {
                                remove(r.binder);
                            }
                        }
                        if ((262144 & events) != 0) {
                            try {
                                r.callback.onDataActivationStateChanged(this.mDataActivationState[phoneId]);
                            } catch (RemoteException e18) {
                                remove(r.binder);
                            }
                        }
                        if ((524288 & events) != 0) {
                            try {
                                r.callback.onUserMobileDataStateChanged(this.mUserMobileDataState[phoneId]);
                            } catch (RemoteException e19) {
                                remove(r.binder);
                            }
                        }
                        if ((1048576 & events) != 0) {
                            try {
                                r.callback.onPhysicalChannelConfigurationChanged(this.mPhysicalChannelConfigs.get(phoneId));
                            } catch (RemoteException e20) {
                                remove(r.binder);
                            }
                        }
                        if ((16777216 & events) != 0) {
                            try {
                                r.callback.onEmergencyNumberListChanged(this.mEmergencyNumberList);
                            } catch (RemoteException e21) {
                                remove(r.binder);
                            }
                        }
                        if ((2097152 & events) != 0) {
                            try {
                                r.callback.onPhoneCapabilityChanged(this.mPhoneCapability);
                            } catch (RemoteException e22) {
                                remove(r.binder);
                            }
                        }
                        if ((4194304 & events) != 0 && TelephonyPermissions.checkReadPhoneStateOnAnyActiveSub(r.context, r.callerPid, r.callerUid, r.callingPackage, "listen_active_data_subid_change")) {
                            try {
                                r.callback.onActiveDataSubIdChanged(this.mActiveDataSubId);
                            } catch (RemoteException e23) {
                                remove(r.binder);
                            }
                        }
                        if ((8388608 & events) != 0) {
                            try {
                                r.callback.onRadioPowerStateChanged(this.mRadioPowerState);
                            } catch (RemoteException e24) {
                                remove(r.binder);
                            }
                        }
                        if ((events & DumpState.DUMP_KEYSETS) != 0) {
                            try {
                                r.callback.onSrvccStateChanged(this.mSrvccState[phoneId]);
                            } catch (RemoteException e25) {
                                remove(r.binder);
                            }
                        }
                        if ((67108864 & events) != 0) {
                            try {
                                r.callback.onCallAttributesChanged(this.mCallAttributes[phoneId]);
                            } catch (RemoteException e26) {
                                remove(r.binder);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getCallIncomingNumber(Record record, int phoneId) {
        return record.canReadCallLog() ? this.mCallIncomingNumber[phoneId] : "";
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
                if (DBG) {
                    log("add new record");
                }
                return r2;
            } catch (RemoteException e) {
                if (VDBG) {
                    log("LinkToDeath remote exception sending to r=" + r2 + " e=" + e);
                }
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void remove(IBinder binder) {
        synchronized (this.mRecords) {
            int recordCount = this.mRecords.size();
            for (int i = 0; i < recordCount; i++) {
                Record r = this.mRecords.get(i);
                if (r.binder == binder) {
                    if (DBG) {
                        log("remove: binder=" + binder + " r.callingPackage " + r.callingPackage + " r.callback " + r.callback);
                    }
                    if (r.deathRecipient != null) {
                        try {
                            binder.unlinkToDeath(r.deathRecipient, 0);
                        } catch (NoSuchElementException e) {
                            if (VDBG) {
                                log("UnlinkToDeath NoSuchElementException sending to r=" + r + " e=" + e);
                            }
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
            if (VDBG) {
                log("notifyCallState: state=" + state + " phoneNumber=" + phoneNumber);
            }
            synchronized (this.mRecords) {
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(32) && r.subId == Integer.MAX_VALUE) {
                        try {
                            r.callback.onCallStateChanged(state, r.canReadCallLog() ? phoneNumber : "");
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
            if (VDBG) {
                log("notifyCallStateForPhoneId: subId=" + subId + " state=" + state + " incomingNumber=" + incomingNumber);
            }
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
        ServiceState stateToSend;
        if (checkNotifyPermission("notifyServiceState()")) {
            synchronized (this.mRecords) {
                if (VDBG) {
                    String str = "notifyServiceStateForSubscriber: subId=" + subId + " phoneId=" + phoneId + " state=" + state;
                    log(str);
                    this.mLocalLog.log(str);
                }
                if (validatePhoneId(phoneId)) {
                    this.mServiceState[phoneId] = state;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (VDBG) {
                            log("notifyServiceStateForSubscriber: r=" + r + " subId=" + subId + " phoneId=" + phoneId + " state=" + state);
                        }
                        if (r.matchPhoneStateListenerEvent(1) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                if (checkFineLocationAccess(r, 29)) {
                                    stateToSend = new ServiceState(state);
                                } else if (checkCoarseLocationAccess(r, 29)) {
                                    stateToSend = state.sanitizeLocationInfo(false);
                                } else {
                                    stateToSend = state.sanitizeLocationInfo(true);
                                }
                                if (DBG) {
                                    log("notifyServiceStateForSubscriber: callback.onSSC r=" + r + " subId=" + subId + " phoneId=" + phoneId + " state=" + state);
                                }
                                r.callback.onServiceStateChanged(stateToSend);
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
            if (VDBG) {
                log("notifySimActivationStateForPhoneId: subId=" + subId + " phoneId=" + phoneId + "type=" + activationType + " state=" + activationState);
            }
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    if (activationType == 0) {
                        this.mVoiceActivationState[phoneId] = activationState;
                    } else if (activationType == 1) {
                        this.mDataActivationState[phoneId] = activationState;
                    } else {
                        return;
                    }
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (VDBG) {
                            log("notifySimActivationStateForPhoneId: r=" + r + " subId=" + subId + " phoneId=" + phoneId + "type=" + activationType + " state=" + activationState);
                        }
                        if (activationType == 0) {
                            try {
                                if (r.matchPhoneStateListenerEvent(DumpState.DUMP_INTENT_FILTER_VERIFIERS) && idMatch(r.subId, subId, phoneId)) {
                                    if (DBG) {
                                        log("notifyVoiceActivationStateForPhoneId: callback.onVASC r=" + r + " subId=" + subId + " phoneId=" + phoneId + " state=" + activationState);
                                    }
                                    r.callback.onVoiceActivationStateChanged(activationState);
                                }
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                        if (activationType == 1 && r.matchPhoneStateListenerEvent(DumpState.DUMP_DOMAIN_PREFERRED) && idMatch(r.subId, subId, phoneId)) {
                            if (DBG) {
                                log("notifyDataActivationStateForPhoneId: callback.onDASC r=" + r + " subId=" + subId + " phoneId=" + phoneId + " state=" + activationState);
                            }
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
            if (VDBG) {
                log("notifySignalStrengthForPhoneId: subId=" + subId + " phoneId=" + phoneId + " signalStrength=" + signalStrength);
            }
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    if (VDBG) {
                        log("notifySignalStrengthForPhoneId: valid phoneId=" + phoneId);
                    }
                    this.mSignalStrength[phoneId] = signalStrength;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (VDBG) {
                            log("notifySignalStrengthForPhoneId: r=" + r + " subId=" + subId + " phoneId=" + phoneId + " ss=" + signalStrength);
                        }
                        if (r.matchPhoneStateListenerEvent(256) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                if (DBG) {
                                    log("notifySignalStrengthForPhoneId: callback.onSsS r=" + r + " subId=" + subId + " phoneId=" + phoneId + " ss=" + signalStrength);
                                }
                                r.callback.onSignalStrengthsChanged(new SignalStrength(signalStrength));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                        if (r.matchPhoneStateListenerEvent(2) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                int gsmSignalStrength = signalStrength.getGsmSignalStrength();
                                int ss = gsmSignalStrength == 99 ? -1 : gsmSignalStrength;
                                if (DBG) {
                                    log("notifySignalStrengthForPhoneId: callback.onSS r=" + r + " subId=" + subId + " phoneId=" + phoneId + " gsmSS=" + gsmSignalStrength + " ss=" + ss);
                                }
                                r.callback.onSignalStrengthChanged(ss);
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
        int[] subIds = Arrays.stream(SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList()).filter($$Lambda$TelephonyRegistry$B6olxgDfuFsbAHNXng6QnHMFZZM.INSTANCE).toArray();
        if (!ArrayUtils.isEmpty(subIds)) {
            synchronized (this.mRecords) {
                this.mCarrierNetworkChangeState = active;
                for (int subId : subIds) {
                    int phoneId = SubscriptionManager.getPhoneId(subId);
                    if (VDBG) {
                        log("notifyCarrierNetworkChange: active=" + active + "subId: " + subId);
                    }
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(65536) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onCarrierNetworkChange(active);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
            return;
        }
        loge("notifyCarrierNetworkChange without carrier privilege");
        throw new SecurityException("notifyCarrierNetworkChange without carrier privilege");
    }

    public void notifyCellInfo(List<CellInfo> cellInfo) {
        notifyCellInfoForSubscriber(Integer.MAX_VALUE, cellInfo);
    }

    public void notifyCellInfoForSubscriber(int subId, List<CellInfo> cellInfo) {
        if (checkNotifyPermission("notifyCellInfo()")) {
            if (VDBG) {
                log("notifyCellInfoForSubscriber: subId=" + subId + " cellInfo=" + cellInfo);
            }
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCellInfo.set(phoneId, cellInfo);
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (validateEventsAndUserLocked(r, 1024) && idMatch(r.subId, subId, phoneId) && checkFineLocationAccess(r, 29)) {
                            try {
                                if (DBG_LOC) {
                                    log("notifyCellInfo: mCellInfo=" + cellInfo + " r=" + r);
                                }
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
        notifyPhysicalChannelConfigurationForSubscriber(Integer.MAX_VALUE, configs);
    }

    public void notifyPhysicalChannelConfigurationForSubscriber(int subId, List<PhysicalChannelConfig> configs) {
        if (checkNotifyPermission("notifyPhysicalChannelConfiguration()")) {
            if (VDBG) {
                log("notifyPhysicalChannelConfiguration: subId=" + subId + " configs=" + configs);
            }
            synchronized (this.mRecords) {
                int phoneId = SubscriptionManager.getPhoneId(subId);
                if (validatePhoneId(phoneId)) {
                    this.mPhysicalChannelConfigs.set(phoneId, configs);
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_DEXOPT) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                if (DBG_LOC) {
                                    log("notifyPhysicalChannelConfiguration: mPhysicalChannelConfigs=" + configs + " r=" + r);
                                }
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
            if (VDBG) {
                log("notifyMessageWaitingChangedForSubscriberPhoneID: subId=" + phoneId + " mwi=" + mwi);
            }
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
            if (VDBG) {
                log("notifyUserMobileDataStateChangedForSubscriberPhoneID: PhoneId=" + phoneId + " subId=" + subId + " state=" + state);
            }
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
        notifyCallForwardingChangedForSubscriber(Integer.MAX_VALUE, cfi);
    }

    public void notifyCallForwardingChangedForSubscriber(int subId, boolean cfi) {
        if (checkNotifyPermission("notifyCallForwardingChanged()")) {
            if (VDBG) {
                log("notifyCallForwardingChangedForSubscriber: subId=" + subId + " cfi=" + cfi);
            }
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
        notifyDataActivityForSubscriber(Integer.MAX_VALUE, state);
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

    public void notifyDataConnection(int state, boolean isDataAllowed, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int networkType, boolean roaming) {
        notifyDataConnectionForSubscriber(Integer.MAX_VALUE, Integer.MAX_VALUE, state, isDataAllowed, apn, apnType, linkProperties, networkCapabilities, networkType, roaming);
    }

    public void notifyDataConnectionForSubscriber(int phoneId, int subId, int state, boolean isDataAllowed, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int networkType, boolean roaming) {
        ArrayList<Record> arrayList;
        Throwable th;
        if (checkNotifyPermission("notifyDataConnection()")) {
            if (VDBG) {
                log("notifyDataConnectionForSubscriber: subId=" + subId + " state=" + state + " isDataAllowed=" + isDataAllowed + "' apn='" + apn + "' apnType=" + apnType + " networkType=" + networkType + " mRecords.size()=" + this.mRecords.size());
            }
            ArrayList<Record> arrayList2 = this.mRecords;
            synchronized (arrayList2) {
                try {
                    if (validatePhoneId(phoneId)) {
                        if (isModified(phoneId, state, apnType, networkType)) {
                            String str = "onDataConnectionStateChanged(" + this.mDataConnectionState[phoneId] + ", " + this.mDataConnectionNetworkType[phoneId] + ")";
                            log(str);
                            this.mLocalLog.log(str);
                            Iterator<Record> it = this.mRecords.iterator();
                            while (it.hasNext()) {
                                Record r = it.next();
                                if (r.matchPhoneStateListenerEvent(64) && idMatch(r.subId, subId, phoneId)) {
                                    try {
                                        if (DBG) {
                                            log("Notify data connection state changed on sub: " + subId);
                                        }
                                        r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
                                    } catch (RemoteException e) {
                                        this.mRemoveList.add(r.binder);
                                    }
                                }
                            }
                            handleRemoveListLocked();
                        }
                        arrayList = arrayList2;
                        try {
                            this.mPreciseDataConnectionState[phoneId] = new PreciseDataConnectionState(state, networkType, ApnSetting.getApnTypesBitmaskFromString(apnType), apn, linkProperties, 0);
                            Iterator<Record> it2 = this.mRecords.iterator();
                            while (it2.hasNext()) {
                                Record r2 = it2.next();
                                if (r2.matchPhoneStateListenerEvent(4096) && idMatch(r2.subId, subId, phoneId)) {
                                    try {
                                        r2.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState[phoneId]);
                                    } catch (RemoteException e2) {
                                        this.mRemoveList.add(r2.binder);
                                    }
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } else {
                        arrayList = arrayList2;
                    }
                    handleRemoveListLocked();
                    broadcastDataConnectionStateChanged(state, isDataAllowed, apn, apnType, linkProperties, networkCapabilities, roaming, subId);
                    broadcastPreciseDataConnectionStateChanged(state, networkType, apnType, apn, linkProperties, 0);
                } catch (Throwable th3) {
                    th = th3;
                    arrayList = arrayList2;
                    throw th;
                }
            }
        }
    }

    private boolean isModified(int phoneId, int state, String apnType, int networkType) {
        boolean modified = false;
        if (SHOW_DATA_ACT_ALL_APN || isSliceApn(apnType)) {
            if (state == 2) {
                if (!this.mConnectedApns[phoneId].contains(apnType) && !"ims".equals(apnType) && !"xcap".equals(apnType) && !"internaldefault".equals(apnType)) {
                    this.mConnectedApns[phoneId].add(apnType);
                    int[] iArr = this.mDataConnectionState;
                    if (iArr[phoneId] != state) {
                        iArr[phoneId] = state;
                        modified = true;
                    }
                }
            } else if (this.mConnectedApns[phoneId].remove(apnType) && this.mConnectedApns[phoneId].isEmpty()) {
                this.mDataConnectionState[phoneId] = state;
                modified = true;
            }
            int[] iArr2 = this.mDataConnectionNetworkType;
            if (iArr2[phoneId] == networkType) {
                return modified;
            }
            iArr2[phoneId] = networkType;
            return true;
        } else if (!BatteryService.HealthServiceWrapper.INSTANCE_VENDOR.equals(apnType)) {
            return false;
        } else {
            if (this.mDataConnectionState[phoneId] == state && this.mDataConnectionNetworkType[phoneId] == networkType) {
                return false;
            }
            this.mDataConnectionState[phoneId] = state;
            this.mDataConnectionNetworkType[phoneId] = networkType;
            return true;
        }
    }

    private boolean isSliceApn(String apnType) {
        return IS_NR_SLICES_SUPPORTED && "snssai".equals(apnType);
    }

    public void notifyDataConnectionFailed(String apnType) {
        notifyDataConnectionFailedForSubscriber(Integer.MAX_VALUE, Integer.MAX_VALUE, apnType);
    }

    public void notifyDataConnectionFailedForSubscriber(int phoneId, int subId, String apnType) {
        if (checkNotifyPermission("notifyDataConnectionFailed()")) {
            if (VDBG) {
                log("notifyDataConnectionFailedForSubscriber: subId=" + subId + " apnType=" + apnType);
            }
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mPreciseDataConnectionState[phoneId] = new PreciseDataConnectionState(-1, 0, ApnSetting.getApnTypesBitmaskFromString(apnType), null, null, 0);
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(4096) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState[phoneId]);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastDataConnectionFailed(apnType, subId);
            broadcastPreciseDataConnectionStateChanged(-1, 0, apnType, null, null, 0);
        }
    }

    public void notifyCellLocation(Bundle cellLocation) {
        notifyCellLocationForSubscriber(Integer.MAX_VALUE, cellLocation);
    }

    public void notifyCellLocationForSubscriber(int subId, Bundle cellLocation) {
        if (checkNotifyPermission("notifyCellLocation()")) {
            if (VDBG) {
                log("notifyCellLocationForSubscriber: subId=" + subId + " cellLocation=" + cellLocation);
            }
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCellLocation[phoneId] = cellLocation;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (validateEventsAndUserLocked(r, 16) && idMatch(r.subId, subId, phoneId) && checkFineLocationAccess(r, 29)) {
                            try {
                                if (DBG_LOC) {
                                    log("notifyCellLocation: cellLocation=" + cellLocation + " r=" + r);
                                }
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

    public void notifyOtaspChanged(int subId, int otaspMode) {
        if (checkNotifyPermission("notifyOtaspChanged()")) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mOtaspMode[phoneId] = otaspMode;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(512) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onOtaspChanged(otaspMode);
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

    public void notifyPreciseCallState(int phoneId, int subId, int ringingCallState, int foregroundCallState, int backgroundCallState) {
        if (checkNotifyPermission("notifyPreciseCallState()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mRingingCallState[phoneId] = ringingCallState;
                    this.mForegroundCallState[phoneId] = foregroundCallState;
                    this.mBackgroundCallState[phoneId] = backgroundCallState;
                    this.mPreciseCallState[phoneId] = new PreciseCallState(ringingCallState, foregroundCallState, backgroundCallState, -1, -1);
                    boolean notifyCallAttributes = true;
                    if (this.mCallQuality == null) {
                        log("notifyPreciseCallState: mCallQuality is null, skipping call attributes");
                        notifyCallAttributes = false;
                    } else {
                        if (this.mPreciseCallState[phoneId].getForegroundCallState() != 1) {
                            this.mCallNetworkType[phoneId] = 0;
                            this.mCallQuality[phoneId] = new CallQuality();
                        }
                        this.mCallAttributes[phoneId] = new CallAttributes(this.mPreciseCallState[phoneId], this.mCallNetworkType[phoneId], this.mCallQuality[phoneId]);
                    }
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(2048) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onPreciseCallStateChanged(this.mPreciseCallState[phoneId]);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                        if (notifyCallAttributes && r.matchPhoneStateListenerEvent(DumpState.DUMP_HANDLE) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onCallAttributesChanged(this.mCallAttributes[phoneId]);
                            } catch (RemoteException e2) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastPreciseCallStateChanged(ringingCallState, foregroundCallState, backgroundCallState);
        }
    }

    public void notifyDisconnectCause(int phoneId, int subId, int disconnectCause, int preciseDisconnectCause) {
        if (checkNotifyPermission("notifyDisconnectCause()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCallDisconnectCause[phoneId] = disconnectCause;
                    this.mCallPreciseDisconnectCause[phoneId] = preciseDisconnectCause;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_APEX) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onCallDisconnectCauseChanged(this.mCallDisconnectCause[phoneId], this.mCallPreciseDisconnectCause[phoneId]);
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

    public void notifyImsDisconnectCause(int subId, ImsReasonInfo imsReasonInfo) {
        if (checkNotifyPermission("notifyImsCallDisconnectCause()")) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mImsReasonInfo.set(phoneId, imsReasonInfo);
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_HWFEATURES) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                if (DBG_LOC) {
                                    log("notifyImsCallDisconnectCause: mImsReasonInfo=" + imsReasonInfo + " r=" + r);
                                }
                                r.callback.onImsCallDisconnectCauseChanged(this.mImsReasonInfo.get(phoneId));
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

    public void notifyPreciseDataConnectionFailed(int phoneId, int subId, String apnType, String apn, int failCause) {
        if (checkNotifyPermission("notifyPreciseDataConnectionFailed()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mPreciseDataConnectionState[phoneId] = new PreciseDataConnectionState(-1, 0, ApnSetting.getApnTypesBitmaskFromString(apnType), apn, null, failCause);
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(4096) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState[phoneId]);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastPreciseDataConnectionStateChanged(-1, 0, apnType, apn, null, failCause);
        }
    }

    public void notifySrvccStateChanged(int subId, int state) {
        if (checkNotifyPermission("notifySrvccStateChanged()")) {
            if (VDBG) {
                log("notifySrvccStateChanged: subId=" + subId + " srvccState=" + state);
            }
            int phoneId = SubscriptionManager.getPhoneId(subId);
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mSrvccState[phoneId] = state;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_KEYSETS) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                if (DBG_LOC) {
                                    log("notifySrvccStateChanged: mSrvccState=" + state + " r=" + r);
                                }
                                r.callback.onSrvccStateChanged(state);
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

    public void notifyOemHookRawEventForSubscriber(int phoneId, int subId, byte[] rawData) {
        if (checkNotifyPermission("notifyOemHookRawEventForSubscriber")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (VDBG) {
                            log("notifyOemHookRawEventForSubscriber:  r=" + r + " subId=" + subId);
                        }
                        if (r.matchPhoneStateListenerEvent(32768) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onOemHookRawEvent(rawData);
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

    public void notifyPhoneCapabilityChanged(PhoneCapability capability) {
        if (checkNotifyPermission("notifyPhoneCapabilityChanged()")) {
            if (VDBG) {
                log("notifyPhoneCapabilityChanged: capability=" + capability);
            }
            synchronized (this.mRecords) {
                this.mPhoneCapability = capability;
                Iterator<Record> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    Record r = it.next();
                    if (r.matchPhoneStateListenerEvent(DumpState.DUMP_COMPILER_STATS)) {
                        try {
                            r.callback.onPhoneCapabilityChanged(capability);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public void notifyActiveDataSubIdChanged(int activeDataSubId) {
        List<Record> copiedRecords;
        if (checkNotifyPermission("notifyActiveDataSubIdChanged()")) {
            if (VDBG) {
                log("notifyActiveDataSubIdChanged: activeDataSubId=" + activeDataSubId);
            }
            synchronized (this.mRecords) {
                copiedRecords = new ArrayList<>(this.mRecords);
            }
            this.mActiveDataSubId = activeDataSubId;
            List<Record> copiedRecords2 = (List) copiedRecords.stream().filter(new Predicate() {
                /* class com.android.server.$$Lambda$TelephonyRegistry$kbHTNHMcu4ep02HSrYzRQeY7ThQ */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return TelephonyRegistry.this.lambda$notifyActiveDataSubIdChanged$1$TelephonyRegistry((TelephonyRegistry.Record) obj);
                }
            }).collect(Collectors.toCollection($$Lambda$OGSS2qx6njxlnp0dnKb4lA3jnw8.INSTANCE));
            synchronized (this.mRecords) {
                for (Record r : copiedRecords2) {
                    if (this.mRecords.contains(r)) {
                        try {
                            r.callback.onActiveDataSubIdChanged(activeDataSubId);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public /* synthetic */ boolean lambda$notifyActiveDataSubIdChanged$1$TelephonyRegistry(Record r) {
        return r.matchPhoneStateListenerEvent(DumpState.DUMP_CHANGES) && TelephonyPermissions.checkReadPhoneStateOnAnyActiveSub(this.mContext, r.callerPid, r.callerUid, r.callingPackage, "notifyActiveDataSubIdChanged");
    }

    public void notifyRadioPowerStateChanged(int phoneId, int subId, int state) {
        if (checkNotifyPermission("notifyRadioPowerStateChanged()")) {
            if (VDBG) {
                log("notifyRadioPowerStateChanged: state= " + state + " subId=" + subId);
            }
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mRadioPowerState = state;
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_VOLUMES) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onRadioPowerStateChanged(state);
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

    public void notifyEmergencyNumberList(int phoneId, int subId) {
        if (checkNotifyPermission("notifyEmergencyNumberList()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mEmergencyNumberList = ((TelephonyManager) this.mContext.getSystemService("phone")).getEmergencyNumberList();
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_SERVICE_PERMISSIONS) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onEmergencyNumberListChanged(this.mEmergencyNumberList);
                                if (VDBG) {
                                    log("notifyEmergencyNumberList: emergencyNumberList= " + this.mEmergencyNumberList);
                                }
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

    public void notifyCallQualityChanged(CallQuality callQuality, int phoneId, int subId, int callNetworkType) {
        if (checkNotifyPermission("notifyCallQualityChanged()")) {
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCallQuality[phoneId] = callQuality;
                    this.mCallNetworkType[phoneId] = callNetworkType;
                    this.mCallAttributes[phoneId] = new CallAttributes(this.mPreciseCallState[phoneId], callNetworkType, callQuality);
                    Iterator<Record> it = this.mRecords.iterator();
                    while (it.hasNext()) {
                        Record r = it.next();
                        if (r.matchPhoneStateListenerEvent(DumpState.DUMP_HANDLE) && idMatch(r.subId, subId, phoneId)) {
                            try {
                                r.callback.onCallAttributesChanged(this.mCallAttributes[phoneId]);
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
                    pw.println("mRingingCallState=" + this.mRingingCallState[i]);
                    pw.println("mForegroundCallState=" + this.mForegroundCallState[i]);
                    pw.println("mBackgroundCallState=" + this.mBackgroundCallState[i]);
                    pw.println("mPreciseCallState=" + this.mPreciseCallState[i]);
                    pw.println("mCallDisconnectCause=" + this.mCallDisconnectCause[i]);
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
                    pw.println("mImsCallDisconnectCause=" + this.mImsReasonInfo.get(i));
                    pw.println("mSrvccState=" + this.mSrvccState[i]);
                    pw.println("mOtaspMode=" + this.mOtaspMode[i]);
                    pw.println("mCallPreciseDisconnectCause=" + this.mCallPreciseDisconnectCause[i]);
                    pw.println("mCallQuality=" + this.mCallQuality[i]);
                    pw.println("mCallAttributes=" + this.mCallAttributes[i]);
                    pw.println("mCallNetworkType=" + this.mCallNetworkType[i]);
                    pw.println("mPreciseDataConnectionState=" + this.mPreciseDataConnectionState[i]);
                    pw.decreaseIndent();
                }
                pw.println("mConnectedApns=" + Arrays.toString(this.mConnectedApns));
                pw.println("mCarrierNetworkChangeState=" + this.mCarrierNetworkChangeState);
                pw.println("mPhoneCapability=" + this.mPhoneCapability);
                pw.println("mActiveDataSubId=" + this.mActiveDataSubId);
                pw.println("mRadioPowerState=" + this.mRadioPowerState);
                pw.println("mEmergencyNumberList=" + this.mEmergencyNumberList);
                pw.println("mDefaultPhoneId=" + this.mDefaultPhoneId);
                pw.println("mDefaultSubId=" + this.mDefaultSubId);
                pw.decreaseIndent();
                pw.println("local logs:");
                pw.increaseIndent();
                this.mLocalLog.dump(fd, pw, args);
                pw.println("listen logs:");
                this.mListenLog.dump(fd, pw, args);
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
        intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId);
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
                StatsLog.write(95, 0);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            this.mBatteryStats.notePhoneOn();
            StatsLog.write(95, 1);
        }
        Binder.restoreCallingIdentity(ident);
        Intent intent = new Intent("android.intent.action.PHONE_STATE");
        intent.putExtra("state", PhoneConstantConversions.convertCallState(state).toString());
        if (subId != -1) {
            intent.setAction("android.intent.action.SUBSCRIPTION_PHONE_STATE");
            intent.putExtra("subscription", subId);
            intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId);
        }
        if (phoneId != -1) {
            intent.putExtra("slot", phoneId);
        }
        intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
        Intent intentWithPhoneNumber = new Intent(intent);
        intentWithPhoneNumber.putExtra("incoming_number", incomingNumber);
        this.mContext.sendBroadcastAsUser(intentWithPhoneNumber, UserHandle.ALL, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PHONE_STATE", 51);
        this.mContext.sendBroadcastAsUserMultiplePermissions(intentWithPhoneNumber, UserHandle.ALL, new String[]{"android.permission.READ_PHONE_STATE", "android.permission.READ_CALL_LOG"});
    }

    private void broadcastDataConnectionStateChanged(int state, boolean isDataAllowed, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, boolean roaming, int subId) {
        Intent intent = new Intent("android.intent.action.ANY_DATA_STATE");
        intent.putExtra("state", PhoneConstantConversions.convertDataState(state).toString());
        if (!isDataAllowed) {
            intent.putExtra("networkUnvailable", true);
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

    private void broadcastDataConnectionFailed(String apnType, int subId) {
        Intent intent = new Intent("android.intent.action.DATA_CONNECTION_FAILED");
        intent.putExtra("apnType", apnType);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void broadcastPreciseCallStateChanged(int ringingCallState, int foregroundCallState, int backgroundCallState) {
        Intent intent = new Intent("android.intent.action.PRECISE_CALL_STATE");
        intent.putExtra("ringing_state", ringingCallState);
        intent.putExtra("foreground_state", foregroundCallState);
        intent.putExtra("background_state", backgroundCallState);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PRECISE_PHONE_STATE");
    }

    private void broadcastPreciseDataConnectionStateChanged(int state, int networkType, String apnType, String apn, LinkProperties linkProperties, int failCause) {
        Intent intent = new Intent("android.intent.action.PRECISE_DATA_CONNECTION_STATE_CHANGED");
        intent.putExtra("state", state);
        intent.putExtra("networkType", networkType);
        if (apnType != null) {
            intent.putExtra("apnType", apnType);
        }
        if (apn != null) {
            intent.putExtra("apn", apn);
        }
        if (linkProperties != null) {
            intent.putExtra("linkProperties", linkProperties);
        }
        intent.putExtra("failCause", failCause);
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
        if (!DBG) {
            return false;
        }
        log("Modify Phone State Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return false;
    }

    private boolean checkNotifyPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0;
    }

    private boolean checkListenerPermission(int events, int subId, String callingPackage, String message) {
        LocationAccessPolicy.LocationPermissionQuery.Builder callingPackage2 = new LocationAccessPolicy.LocationPermissionQuery.Builder().setCallingPackage(callingPackage);
        LocationAccessPolicy.LocationPermissionQuery.Builder locationQueryBuilder = callingPackage2.setMethod(message + " events: " + events).setCallingPid(Binder.getCallingPid()).setCallingUid(Binder.getCallingUid());
        boolean shouldCheckLocationPermissions = false;
        if ((events & 0) != 0) {
            locationQueryBuilder.setMinSdkVersionForCoarse(0);
            shouldCheckLocationPermissions = true;
        }
        if ((events & ENFORCE_FINE_LOCATION_PERMISSION_MASK) != 0) {
            locationQueryBuilder.setMinSdkVersionForFine(29);
            shouldCheckLocationPermissions = true;
        }
        if (shouldCheckLocationPermissions) {
            int i = AnonymousClass3.$SwitchMap$android$telephony$LocationAccessPolicy$LocationPermissionResult[LocationAccessPolicy.checkLocationPermission(this.mContext, locationQueryBuilder.build()).ordinal()];
            if (i == 1) {
                throw new SecurityException("Unable to listen for events " + events + " due to insufficient location permissions.");
            } else if (i == 2) {
                return false;
            }
        }
        if ((ENFORCE_PHONE_STATE_PERMISSION_MASK & events) != 0 && !TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subId, callingPackage, message)) {
            return false;
        }
        if ((events & PRECISE_PHONE_STATE_PERMISSION_MASK) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRECISE_PHONE_STATE", null);
        }
        if ((32768 & events) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
        }
        if ((events & DumpState.DUMP_KEYSETS) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
        }
        if ((33554432 & events) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRECISE_PHONE_STATE", null);
        }
        if ((67108864 & events) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRECISE_PHONE_STATE", null);
        }
        if ((8388608 & events) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
        }
        if ((131072 & events) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
        }
        if ((134217728 & events) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRECISE_PHONE_STATE", null);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.TelephonyRegistry$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$android$telephony$LocationAccessPolicy$LocationPermissionResult = new int[LocationAccessPolicy.LocationPermissionResult.values().length];

        static {
            try {
                $SwitchMap$android$telephony$LocationAccessPolicy$LocationPermissionResult[LocationAccessPolicy.LocationPermissionResult.DENIED_HARD.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$telephony$LocationAccessPolicy$LocationPermissionResult[LocationAccessPolicy.LocationPermissionResult.DENIED_SOFT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemoveListLocked() {
        int size = this.mRemoveList.size();
        if (VDBG) {
            log("handleRemoveListLocked: mRemoveList.size()=" + size);
        }
        if (size > 0) {
            Iterator<IBinder> it = this.mRemoveList.iterator();
            while (it.hasNext()) {
                remove(it.next());
            }
            this.mRemoveList.clear();
        }
    }

    private boolean validateEventsAndUserLocked(Record r, int events) {
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            int foregroundUser = ActivityManager.getCurrentUser();
            boolean valid = UserHandle.getUserId(r.callerUid) == foregroundUser && r.matchPhoneStateListenerEvent(events);
            if (DBG || DBG_LOC) {
                log("validateEventsAndUserLocked: valid=" + valid + " r.callerUid=" + r.callerUid + " foregroundUser=" + foregroundUser + " r.events=" + r.events + " events=" + events);
            }
            return valid;
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean validatePhoneId(int phoneId) {
        boolean valid = phoneId >= 0 && phoneId < this.mNumPhones;
        if (VDBG) {
            log("validatePhoneId: " + valid);
        }
        return valid;
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Rlog.d(TAG, s);
    }

    private static void loge(String s) {
        Rlog.e(TAG, s);
    }

    /* access modifiers changed from: package-private */
    public boolean idMatch(int rSubId, int subId, int phoneId) {
        if (subId < 0) {
            this.mDefaultPhoneId = SubscriptionManager.getSlotIndex(this.mDefaultSubId);
            return VSimTelephonyRegistry.getDefaultPhoneIdForVSim(this.mDefaultPhoneId, rSubId) == phoneId;
        } else if (rSubId == Integer.MAX_VALUE) {
            return true;
        } else {
            return rSubId == subId;
        }
    }

    private boolean checkFineLocationAccess(Record r, int minSdk) {
        return ((Boolean) Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingSupplier(new LocationAccessPolicy.LocationPermissionQuery.Builder().setCallingPackage(r.callingPackage).setCallingPid(r.callerPid).setCallingUid(r.callerUid).setMethod("TelephonyRegistry push").setLogAsInfo(true).setMinSdkVersionForFine(minSdk).build()) {
            /* class com.android.server.$$Lambda$TelephonyRegistry$H1zNBtijaPaJZvAGiWo3ze1HvCk */
            private final /* synthetic */ LocationAccessPolicy.LocationPermissionQuery f$1;

            {
                this.f$1 = r2;
            }

            public final Object getOrThrow() {
                return TelephonyRegistry.this.lambda$checkFineLocationAccess$2$TelephonyRegistry(this.f$1);
            }
        })).booleanValue();
    }

    public /* synthetic */ Boolean lambda$checkFineLocationAccess$2$TelephonyRegistry(LocationAccessPolicy.LocationPermissionQuery query) throws Exception {
        return Boolean.valueOf(LocationAccessPolicy.checkLocationPermission(this.mContext, query) == LocationAccessPolicy.LocationPermissionResult.ALLOWED);
    }

    private boolean checkCoarseLocationAccess(Record r, int minSdk) {
        return ((Boolean) Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingSupplier(new LocationAccessPolicy.LocationPermissionQuery.Builder().setCallingPackage(r.callingPackage).setCallingPid(r.callerPid).setCallingUid(r.callerUid).setMethod("TelephonyRegistry push").setLogAsInfo(true).setMinSdkVersionForCoarse(minSdk).build()) {
            /* class com.android.server.$$Lambda$TelephonyRegistry$KVoaCNOFBlPejFaEBKjW11o5Po */
            private final /* synthetic */ LocationAccessPolicy.LocationPermissionQuery f$1;

            {
                this.f$1 = r2;
            }

            public final Object getOrThrow() {
                return TelephonyRegistry.this.lambda$checkCoarseLocationAccess$3$TelephonyRegistry(this.f$1);
            }
        })).booleanValue();
    }

    public /* synthetic */ Boolean lambda$checkCoarseLocationAccess$3$TelephonyRegistry(LocationAccessPolicy.LocationPermissionQuery query) throws Exception {
        return Boolean.valueOf(LocationAccessPolicy.checkLocationPermission(this.mContext, query) == LocationAccessPolicy.LocationPermissionResult.ALLOWED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkPossibleMissNotify(Record r, int phoneId) {
        int i;
        int events = r.events;
        if ((events & 1) != 0) {
            try {
                if (VDBG) {
                    log("checkPossibleMissNotify: onServiceStateChanged state=" + this.mServiceState[phoneId]);
                }
                r.callback.onServiceStateChanged(new ServiceState(this.mServiceState[phoneId]));
            } catch (RemoteException e) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 256) != 0) {
            try {
                SignalStrength signalStrength = this.mSignalStrength[phoneId];
                if (DBG) {
                    log("checkPossibleMissNotify: onSignalStrengthsChanged SS=" + signalStrength);
                }
                r.callback.onSignalStrengthsChanged(new SignalStrength(signalStrength));
            } catch (RemoteException e2) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 2) != 0) {
            try {
                int gsmSignalStrength = this.mSignalStrength[phoneId].getGsmSignalStrength();
                if (DBG) {
                    log("checkPossibleMissNotify: onSignalStrengthChanged SS=" + gsmSignalStrength);
                }
                IPhoneStateListener iPhoneStateListener = r.callback;
                if (gsmSignalStrength == 99) {
                    i = -1;
                } else {
                    i = gsmSignalStrength;
                }
                iPhoneStateListener.onSignalStrengthChanged(i);
            } catch (RemoteException e3) {
                this.mRemoveList.add(r.binder);
            }
        }
        if (validateEventsAndUserLocked(r, 1024)) {
            try {
                if (DBG_LOC) {
                    log("checkPossibleMissNotify: onCellInfoChanged[" + phoneId + "] = " + this.mCellInfo.get(phoneId));
                }
                if (checkFineLocationAccess(r, 29)) {
                    r.callback.onCellInfoChanged(this.mCellInfo.get(phoneId));
                }
            } catch (RemoteException e4) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((524288 & events) != 0) {
            try {
                if (VDBG) {
                    log("checkPossibleMissNotify: onUserMobileDataStateChanged phoneId=" + phoneId + " umds=" + this.mUserMobileDataState[phoneId]);
                }
                r.callback.onUserMobileDataStateChanged(this.mUserMobileDataState[phoneId]);
            } catch (RemoteException e5) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 4) != 0) {
            try {
                if (VDBG) {
                    log("checkPossibleMissNotify: onMessageWaitingIndicatorChanged phoneId=" + phoneId + " mwi=" + this.mMessageWaiting[phoneId]);
                }
                r.callback.onMessageWaitingIndicatorChanged(this.mMessageWaiting[phoneId]);
            } catch (RemoteException e6) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 8) != 0) {
            try {
                if (VDBG) {
                    log("checkPossibleMissNotify: onCallForwardingIndicatorChanged phoneId=" + phoneId + " cfi=" + this.mCallForwarding[phoneId]);
                }
                r.callback.onCallForwardingIndicatorChanged(this.mCallForwarding[phoneId]);
            } catch (RemoteException e7) {
                this.mRemoveList.add(r.binder);
            }
        }
        if (validateEventsAndUserLocked(r, 16)) {
            try {
                if (DBG_LOC) {
                    log("checkPossibleMissNotify: onCellLocationChanged mCellLocation = " + this.mCellLocation[phoneId]);
                }
                if (checkFineLocationAccess(r, 29)) {
                    r.callback.onCellLocationChanged(new Bundle(this.mCellLocation[phoneId]));
                }
            } catch (RemoteException e8) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 64) != 0) {
            try {
                if (DBG) {
                    log("checkPossibleMissNotify: onDataConnectionStateChanged(mDataConnectionState=" + this.mDataConnectionState[phoneId] + ", mDataConnectionNetworkType=" + this.mDataConnectionNetworkType[phoneId] + ")");
                }
                r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
            } catch (RemoteException e9) {
                this.mRemoveList.add(r.binder);
            }
        }
    }
}
