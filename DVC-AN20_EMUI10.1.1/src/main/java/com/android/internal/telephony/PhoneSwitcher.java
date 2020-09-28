package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.MatchAllNetworkSpecifier;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.PhoneCapability;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CellularNetworkValidator;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PhoneSwitcher extends Handler implements IHwPhoneSwitcherInner {
    @VisibleForTesting
    public static int DEFAULT_DATA_OVERRIDE_TIMEOUT_MS = AbstractPhoneBase.SET_TO_AOTO_TIME;
    private static final int DEFAULT_EMERGENCY_PHONE_ID = 0;
    private static final int DEFAULT_NETWORK_CHANGE_TIMEOUT_MS = 5000;
    private static final int DEFAULT_VALIDATION_EXPIRATION_TIME = 2000;
    @VisibleForTesting
    public static int ECBM_DEFAULT_DATA_SWITCH_BASE_TIME_MS = AbstractPhoneBase.SET_TO_AOTO_TIME;
    @VisibleForTesting
    public static final int EVENT_DATA_ENABLED_CHANGED = 114;
    private static final int EVENT_EMERGENCY_TOGGLE = 105;
    private static final int EVENT_MODEM_COMMAND_DONE = 112;
    private static final int EVENT_MODEM_COMMAND_RETRY = 113;
    private static final int EVENT_NETWORK_VALIDATION_DONE = 110;
    private static final int EVENT_OPPT_DATA_SUB_CHANGED = 107;
    private static final int EVENT_OVERRIDE_DDS_FOR_EMERGENCY = 115;
    @VisibleForTesting
    public static final int EVENT_PRECISE_CALL_STATE_CHANGED = 109;
    private static final int EVENT_PRIMARY_DATA_SUB_CHANGED = 101;
    private static final int EVENT_RADIO_AVAILABLE = 108;
    private static final int EVENT_RADIO_CAPABILITY_CHANGED = 106;
    private static final int EVENT_RELEASE_NETWORK = 104;
    private static final int EVENT_REMOVE_DDS_EMERGENCY_OVERRIDE = 116;
    private static final int EVENT_REMOVE_DEFAULT_NETWORK_CHANGE_CALLBACK = 111;
    private static final int EVENT_REQUEST_NETWORK = 103;
    private static final int EVENT_SUBSCRIPTION_CHANGED = 102;
    private static final int HAL_COMMAND_ALLOW_DATA = 1;
    private static final int HAL_COMMAND_PREFERRED_DATA = 2;
    private static final int HAL_COMMAND_UNKNOWN = 0;
    private static final String LOG_TAG = "PhoneSwitcher";
    private static final int MAX_LOCAL_LOG_LINES = 10;
    private static final int MODEM_COMMAND_RETRY_PERIOD_MS = 5000;
    private static final boolean REQUESTS_CHANGED = true;
    private static final boolean REQUESTS_UNCHANGED = false;
    private static final boolean VDBG = false;
    private static PhoneSwitcher sPhoneSwitcher = null;
    private final RegistrantList mActivePhoneRegistrants;
    private final CommandsInterface[] mCommandsInterfaces;
    private ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final BroadcastReceiver mDefaultDataChangedReceiver;
    private final ConnectivityManager.NetworkCallback mDefaultNetworkCallback;
    private EmergencyOverrideRequest mEmergencyOverride;
    private int mHalCommandToUse;
    private Boolean mHasRegisteredDefaultNetworkChangeCallback;
    IHwPhoneSwitcherEx mHwPhoneSwitcherEx;
    private final LocalLog mLocalLog;
    private boolean mManualDdsSwitch;
    @UnsupportedAppUsage
    private int mMaxActivePhones;
    @UnsupportedAppUsage
    private final int mNumPhones;
    private int mOpptDataSubId;
    private int mPhoneIdInVoiceCall;
    @VisibleForTesting
    public final PhoneStateListener mPhoneStateListener;
    private final PhoneState[] mPhoneStates;
    private final int[] mPhoneSubscriptions;
    @UnsupportedAppUsage
    private final Phone[] mPhones;
    @VisibleForTesting
    protected int mPreferredDataPhoneId;
    private int mPreferredDataSubId;
    private int mPrimaryDataSubId;
    private final List<DcRequest> mPrioritizedDcRequests;
    private RadioConfig mRadioConfig;
    private ISetOpportunisticDataCallback mSetOpptSubCallback;
    private final SubscriptionController mSubscriptionController;
    private final IOnSubscriptionsChangedListener mSubscriptionsChangedListener;
    @VisibleForTesting
    public final CellularNetworkValidator.ValidationCallback mValidationCallback;
    private final CellularNetworkValidator mValidator;
    private int requestPhoneCount;

    /* access modifiers changed from: private */
    public static final class EmergencyOverrideRequest {
        int mGnssOverrideTimeMs;
        CompletableFuture<Boolean> mOverrideCompleteFuture;
        boolean mPendingOriginatingCall;
        int mPhoneId;
        boolean mRequiresEcmFinish;

        private EmergencyOverrideRequest() {
            this.mPhoneId = -1;
            this.mGnssOverrideTimeMs = -1;
            this.mRequiresEcmFinish = false;
            this.mPendingOriginatingCall = true;
        }

        /* access modifiers changed from: package-private */
        public boolean isCallbackAvailable() {
            return this.mOverrideCompleteFuture != null;
        }

        /* access modifiers changed from: package-private */
        public void sendOverrideCompleteCallbackResultAndClear(boolean result) {
            if (isCallbackAvailable()) {
                this.mOverrideCompleteFuture.complete(Boolean.valueOf(result));
                this.mOverrideCompleteFuture = null;
            }
        }

        public String toString() {
            return String.format("EmergencyOverrideRequest: [phoneId= %d, overrideMs= %d, hasCallback= %b, ecmFinishStatus= %b]", Integer.valueOf(this.mPhoneId), Integer.valueOf(this.mGnssOverrideTimeMs), Boolean.valueOf(isCallbackAvailable()), Boolean.valueOf(this.mRequiresEcmFinish));
        }
    }

    public /* synthetic */ void lambda$new$0$PhoneSwitcher(boolean validated, int subId) {
        Message.obtain(this, EVENT_NETWORK_VALIDATION_DONE, subId, validated ? 1 : 0).sendToTarget();
    }

    public static PhoneSwitcher getInstance() {
        return sPhoneSwitcher;
    }

    public static PhoneSwitcher make(int maxActivePhones, int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, ITelephonyRegistry tr, CommandsInterface[] cis, Phone[] phones) {
        if (sPhoneSwitcher == null) {
            sPhoneSwitcher = new PhoneSwitcher(maxActivePhones, numPhones, context, subscriptionController, looper, tr, cis, phones);
        }
        return sPhoneSwitcher;
    }

    @VisibleForTesting
    public PhoneSwitcher(int numPhones, Looper looper) {
        super(looper);
        this.mPrioritizedDcRequests = new ArrayList();
        this.mManualDdsSwitch = false;
        this.mValidationCallback = new CellularNetworkValidator.ValidationCallback() {
            /* class com.android.internal.telephony.$$Lambda$PhoneSwitcher$WfAxZbJDpCUxBytiUchQ87aGijQ */

            @Override // com.android.internal.telephony.CellularNetworkValidator.ValidationCallback
            public final void onValidationResult(boolean z, int i) {
                PhoneSwitcher.this.lambda$new$0$PhoneSwitcher(z, i);
            }
        };
        this.mPrimaryDataSubId = -1;
        this.mOpptDataSubId = KeepaliveStatus.INVALID_HANDLE;
        this.mPhoneIdInVoiceCall = -1;
        this.mPreferredDataPhoneId = -1;
        this.mPreferredDataSubId = -1;
        this.mHalCommandToUse = 0;
        this.requestPhoneCount = 0;
        this.mHasRegisteredDefaultNetworkChangeCallback = false;
        this.mDefaultNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.android.internal.telephony.PhoneSwitcher.AnonymousClass1 */

            public void onAvailable(Network network) {
                NetworkCapabilities networkCapabilities = PhoneSwitcher.this.mConnectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null && networkCapabilities.hasTransport(0)) {
                    PhoneSwitcher phoneSwitcher = PhoneSwitcher.this;
                    phoneSwitcher.logDataSwitchEvent(phoneSwitcher.mOpptDataSubId, 2, 0);
                }
                PhoneSwitcher.this.removeDefaultNetworkChangeCallback();
            }
        };
        this.mDefaultDataChangedReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.PhoneSwitcher.AnonymousClass4 */

            public void onReceive(Context context, Intent intent) {
                PhoneSwitcher.this.obtainMessage(101).sendToTarget();
            }
        };
        this.mSubscriptionsChangedListener = new IOnSubscriptionsChangedListener.Stub() {
            /* class com.android.internal.telephony.PhoneSwitcher.AnonymousClass5 */

            public void onSubscriptionsChanged() {
                PhoneSwitcher.this.obtainMessage(102).sendToTarget();
            }
        };
        this.mHwPhoneSwitcherEx = null;
        this.mMaxActivePhones = 0;
        this.mSubscriptionController = null;
        this.mCommandsInterfaces = null;
        this.mContext = null;
        this.mPhoneStates = null;
        this.mPhones = null;
        this.mLocalLog = null;
        this.mActivePhoneRegistrants = null;
        this.mNumPhones = numPhones;
        this.mPhoneSubscriptions = new int[numPhones];
        this.mRadioConfig = RadioConfig.getInstance(this.mContext);
        this.mPhoneStateListener = new PhoneStateListener(looper) {
            /* class com.android.internal.telephony.PhoneSwitcher.AnonymousClass2 */

            public void onPhoneCapabilityChanged(PhoneCapability capability) {
                PhoneSwitcher.this.onPhoneCapabilityChangedInternal(capability);
            }
        };
        this.mValidator = CellularNetworkValidator.getInstance();
    }

    private boolean isPhoneInVoiceCallChanged() {
        Phone phone;
        int oldPhoneIdInVoiceCall = this.mPhoneIdInVoiceCall;
        this.mPhoneIdInVoiceCall = -1;
        Phone[] phoneArr = this.mPhones;
        int length = phoneArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            phone = phoneArr[i];
            if (isCallActive(phone) || isCallActive(phone.getImsPhone())) {
                this.mPhoneIdInVoiceCall = phone.getPhoneId();
            } else {
                i++;
            }
        }
        this.mPhoneIdInVoiceCall = phone.getPhoneId();
        if (this.mPhoneIdInVoiceCall != oldPhoneIdInVoiceCall) {
            return true;
        }
        return false;
    }

    @VisibleForTesting
    public PhoneSwitcher(int maxActivePhones, int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, ITelephonyRegistry tr, CommandsInterface[] cis, Phone[] phones) {
        super(looper);
        this.mPrioritizedDcRequests = new ArrayList();
        this.mManualDdsSwitch = false;
        this.mValidationCallback = new CellularNetworkValidator.ValidationCallback() {
            /* class com.android.internal.telephony.$$Lambda$PhoneSwitcher$WfAxZbJDpCUxBytiUchQ87aGijQ */

            @Override // com.android.internal.telephony.CellularNetworkValidator.ValidationCallback
            public final void onValidationResult(boolean z, int i) {
                PhoneSwitcher.this.lambda$new$0$PhoneSwitcher(z, i);
            }
        };
        this.mPrimaryDataSubId = -1;
        this.mOpptDataSubId = KeepaliveStatus.INVALID_HANDLE;
        this.mPhoneIdInVoiceCall = -1;
        this.mPreferredDataPhoneId = -1;
        this.mPreferredDataSubId = -1;
        this.mHalCommandToUse = 0;
        this.requestPhoneCount = 0;
        this.mHasRegisteredDefaultNetworkChangeCallback = false;
        this.mDefaultNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.android.internal.telephony.PhoneSwitcher.AnonymousClass1 */

            public void onAvailable(Network network) {
                NetworkCapabilities networkCapabilities = PhoneSwitcher.this.mConnectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null && networkCapabilities.hasTransport(0)) {
                    PhoneSwitcher phoneSwitcher = PhoneSwitcher.this;
                    phoneSwitcher.logDataSwitchEvent(phoneSwitcher.mOpptDataSubId, 2, 0);
                }
                PhoneSwitcher.this.removeDefaultNetworkChangeCallback();
            }
        };
        this.mDefaultDataChangedReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.PhoneSwitcher.AnonymousClass4 */

            public void onReceive(Context context, Intent intent) {
                PhoneSwitcher.this.obtainMessage(101).sendToTarget();
            }
        };
        this.mSubscriptionsChangedListener = new IOnSubscriptionsChangedListener.Stub() {
            /* class com.android.internal.telephony.PhoneSwitcher.AnonymousClass5 */

            public void onSubscriptionsChanged() {
                PhoneSwitcher.this.obtainMessage(102).sendToTarget();
            }
        };
        this.mHwPhoneSwitcherEx = null;
        this.mContext = context;
        this.mNumPhones = numPhones;
        this.mPhones = phones;
        this.mPhoneSubscriptions = new int[numPhones];
        this.mMaxActivePhones = maxActivePhones;
        this.mLocalLog = new LocalLog(10);
        this.mHwPhoneSwitcherEx = HwTelephonyFactory.getHwPhoneSwitcherEx(this, numPhones);
        CallManager.getInstance().registerForDisconnect(this, 112, null);
        this.mSubscriptionController = subscriptionController;
        this.mRadioConfig = RadioConfig.getInstance(this.mContext);
        this.mPhoneStateListener = new PhoneStateListener(looper) {
            /* class com.android.internal.telephony.PhoneSwitcher.AnonymousClass3 */

            public void onPhoneCapabilityChanged(PhoneCapability capability) {
                PhoneSwitcher.this.onPhoneCapabilityChangedInternal(capability);
            }
        };
        this.mValidator = CellularNetworkValidator.getInstance();
        this.mActivePhoneRegistrants = new RegistrantList();
        this.mPhoneStates = new PhoneState[numPhones];
        for (int i = 0; i < numPhones; i++) {
            this.mPhoneStates[i] = new PhoneState();
            Phone[] phoneArr = this.mPhones;
            if (phoneArr[i] != null) {
                phoneArr[i].registerForEmergencyCallToggle(this, 105, null);
                this.mPhones[i].registerForPreciseCallStateChanged(this, EVENT_PRECISE_CALL_STATE_CHANGED, null);
                if (this.mPhones[i].getImsPhone() != null) {
                    this.mPhones[i].getImsPhone().registerForPreciseCallStateChanged(this, EVENT_PRECISE_CALL_STATE_CHANGED, null);
                }
                this.mPhones[i].getDataEnabledSettings().registerForDataEnabledChanged(this, 114, null);
            }
        }
        this.mCommandsInterfaces = cis;
        if (numPhones > 0) {
            this.mCommandsInterfaces[0].registerForAvailable(this, 108, null);
        }
        try {
            tr.addOnSubscriptionsChangedListener(context.getOpPackageName(), this.mSubscriptionsChangedListener);
        } catch (RemoteException e) {
        }
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mContext.registerReceiver(this.mDefaultDataChangedReceiver, new IntentFilter("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED"));
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addTransportType(0);
        netCap.addCapability(0);
        netCap.addCapability(1);
        netCap.addCapability(2);
        netCap.addCapability(3);
        netCap.addCapability(4);
        netCap.addCapability(5);
        netCap.addCapability(7);
        netCap.addCapability(8);
        netCap.addCapability(9);
        netCap.addCapability(10);
        netCap.addCapability(13);
        netCap.addCapability(12);
        netCap.addCapability(23);
        this.mHwPhoneSwitcherEx.addNetworkCapability(netCap);
        netCap.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        NetworkFactory networkFactory = new PhoneSwitcherNetworkRequestListener(looper, context, netCap, this);
        networkFactory.setScoreFilter(101);
        networkFactory.register();
        NetworkFactory networkFactoryForVowifi = new PhoneSwitcherNetworkRequestListener(looper, context, this.mHwPhoneSwitcherEx.generateNetCapForVowifi(), this);
        networkFactoryForVowifi.setScoreFilter(101);
        networkFactoryForVowifi.register();
        log("PhoneSwitcher started");
    }

    /* JADX INFO: Multiple debug info for r0v10 int: [D('subId' int), D('phoneId' int)] */
    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void handleMessage(Message msg) {
        boolean commandSuccess = true;
        switch (msg.what) {
            case 101:
                if (onEvaluate(false, "primary data subId changed")) {
                    logDataSwitchEvent(this.mOpptDataSubId, 1, 1);
                    registerDefaultNetworkChangeCallback();
                    return;
                }
                return;
            case 102:
                onEvaluate(false, "subChanged");
                return;
            case EVENT_REQUEST_NETWORK /*{ENCODED_INT: 103}*/:
                onRequestNetwork((NetworkRequest) msg.obj);
                return;
            case 104:
                onReleaseNetwork((NetworkRequest) msg.obj);
                return;
            case 105:
                boolean isInEcm = isInEmergencyCallbackMode();
                if (this.mEmergencyOverride != null) {
                    log("Emergency override - ecbm status = " + isInEcm);
                    if (isInEcm) {
                        removeMessages(116);
                        this.mEmergencyOverride.mRequiresEcmFinish = true;
                    } else if (this.mEmergencyOverride.mRequiresEcmFinish) {
                        sendMessageDelayed(obtainMessage(116), (long) this.mEmergencyOverride.mGnssOverrideTimeMs);
                    }
                }
                onEvaluate(true, "emergencyToggle");
                return;
            case 106:
                sendRilCommands(msg.arg1);
                return;
            case EVENT_OPPT_DATA_SUB_CHANGED /*{ENCODED_INT: 107}*/:
                int subId = msg.arg1;
                if (msg.arg2 != 1) {
                    commandSuccess = false;
                }
                setOpportunisticDataSubscription(subId, commandSuccess, (ISetOpportunisticDataCallback) msg.obj);
                return;
            case 108:
                updateHalCommandToUse();
                onEvaluate(false, "EVENT_RADIO_AVAILABLE");
                return;
            case EVENT_PRECISE_CALL_STATE_CHANGED /*{ENCODED_INT: 109}*/:
                if (enableInCallChange() && isPhoneInVoiceCallChanged()) {
                    EmergencyOverrideRequest emergencyOverrideRequest = this.mEmergencyOverride;
                    if (emergencyOverrideRequest != null && emergencyOverrideRequest.mPendingOriginatingCall) {
                        removeMessages(116);
                        if (this.mPhoneIdInVoiceCall == -1) {
                            sendMessageDelayed(obtainMessage(116), (long) (this.mEmergencyOverride.mGnssOverrideTimeMs + ECBM_DEFAULT_DATA_SWITCH_BASE_TIME_MS));
                            this.mEmergencyOverride.mPendingOriginatingCall = false;
                            break;
                        }
                    }
                } else {
                    return;
                }
                break;
            case EVENT_NETWORK_VALIDATION_DONE /*{ENCODED_INT: 110}*/:
                int subId2 = msg.arg1;
                if (msg.arg2 != 1) {
                    commandSuccess = false;
                }
                onValidationDone(subId2, commandSuccess);
                return;
            case 111:
                removeDefaultNetworkChangeCallback();
                return;
            case 112:
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar == null || ar.exception != null) {
                    commandSuccess = false;
                }
                if (this.mEmergencyOverride != null) {
                    log("Emergency override result sent = " + commandSuccess);
                    this.mEmergencyOverride.sendOverrideCompleteCallbackResultAndClear(commandSuccess);
                    return;
                } else if (!commandSuccess) {
                    int phoneId = ((Integer) ar.userObj).intValue();
                    log("Modem command failed. with exception " + ar.exception);
                    sendMessageDelayed(Message.obtain(this, 113, Integer.valueOf(phoneId)), 5000);
                    return;
                } else {
                    return;
                }
            case 113:
                int phoneId2 = ((Integer) msg.obj).intValue();
                log("Resend modem command on phone " + phoneId2);
                sendRilCommands(phoneId2);
                return;
            case 114:
                break;
            case 115:
                EmergencyOverrideRequest req = (EmergencyOverrideRequest) msg.obj;
                EmergencyOverrideRequest emergencyOverrideRequest2 = this.mEmergencyOverride;
                if (emergencyOverrideRequest2 == null) {
                    this.mEmergencyOverride = req;
                } else if (emergencyOverrideRequest2.mPhoneId != req.mPhoneId) {
                    log("emergency override requested for phone id " + req.mPhoneId + " when there is already an override in place for phone id " + this.mEmergencyOverride.mPhoneId + ". Ignoring.");
                    if (req.isCallbackAvailable()) {
                        req.mOverrideCompleteFuture.complete(false);
                        return;
                    }
                    return;
                } else {
                    if (this.mEmergencyOverride.isCallbackAvailable()) {
                        this.mEmergencyOverride.mOverrideCompleteFuture.complete(false);
                    }
                    this.mEmergencyOverride = req;
                }
                log("new emergency override - " + this.mEmergencyOverride);
                removeMessages(116);
                sendMessageDelayed(obtainMessage(116), (long) DEFAULT_DATA_OVERRIDE_TIMEOUT_MS);
                if (!onEvaluate(false, "emer_override_dds")) {
                    this.mEmergencyOverride.sendOverrideCompleteCallbackResultAndClear(true);
                    return;
                }
                return;
            case 116:
                log("Emergency override removed - " + this.mEmergencyOverride);
                this.mEmergencyOverride = null;
                onEvaluate(false, "emer_rm_override_dds");
                return;
            default:
                log("handleMessage, what=" + msg.what);
                this.mHwPhoneSwitcherEx.handleMessage(this, this.mPhones, msg);
                return;
        }
        if (onEvaluate(false, "EVENT_PRECISE_CALL_STATE_CHANGED")) {
            logDataSwitchEvent(this.mOpptDataSubId, 1, 2);
            registerDefaultNetworkChangeCallback();
        }
    }

    private boolean isEmergency() {
        if (isInEmergencyCallbackMode()) {
            return true;
        }
        Phone[] phoneArr = this.mPhones;
        for (Phone p : phoneArr) {
            if (p != null) {
                if (p.isInEmergencyCall()) {
                    return true;
                }
                Phone imsPhone = p.getImsPhone();
                if (imsPhone != null && imsPhone.isInEmergencyCall()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInEmergencyCallbackMode() {
        Phone[] phoneArr = this.mPhones;
        for (Phone p : phoneArr) {
            if (p != null) {
                if (p.isInEcm()) {
                    return true;
                }
                Phone imsPhone = p.getImsPhone();
                if (imsPhone != null && imsPhone.isInEcm()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class PhoneSwitcherNetworkRequestListener extends NetworkFactory {
        private final PhoneSwitcher mPhoneSwitcher;

        public PhoneSwitcherNetworkRequestListener(Looper l, Context c, NetworkCapabilities nc, PhoneSwitcher ps) {
            super(l, c, "PhoneSwitcherNetworkRequstListener", nc);
            this.mPhoneSwitcher = ps;
        }

        /* access modifiers changed from: protected */
        public void needNetworkFor(NetworkRequest networkRequest, int score) {
            Message msg = this.mPhoneSwitcher.obtainMessage(PhoneSwitcher.EVENT_REQUEST_NETWORK);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }

        /* access modifiers changed from: protected */
        public void releaseNetworkFor(NetworkRequest networkRequest) {
            Message msg = this.mPhoneSwitcher.obtainMessage(104);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }
    }

    private void onRequestNetwork(NetworkRequest networkRequest) {
        DcRequest dcRequest = new DcRequest(networkRequest, this.mContext);
        if (!this.mPrioritizedDcRequests.contains(dcRequest) && dcRequest.apnType != 0) {
            collectRequestNetworkMetrics(networkRequest);
            this.mPrioritizedDcRequests.add(dcRequest);
            Collections.sort(this.mPrioritizedDcRequests);
            onEvaluate(true, "netRequest");
        }
    }

    private void onReleaseNetwork(NetworkRequest networkRequest) {
        if (!this.mPrioritizedDcRequests.remove(new DcRequest(networkRequest, this.mContext))) {
            return;
        }
        if (networkRequest.networkCapabilities.hasCapability(32)) {
            log("releaseNetworkFor internal_default, just return.");
            return;
        }
        onEvaluate(true, "netReleased");
        collectReleaseNetworkMetrics(networkRequest);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeDefaultNetworkChangeCallback() {
        synchronized (this.mHasRegisteredDefaultNetworkChangeCallback) {
            if (this.mHasRegisteredDefaultNetworkChangeCallback.booleanValue()) {
                this.mHasRegisteredDefaultNetworkChangeCallback = false;
                removeMessages(111);
                this.mConnectivityManager.unregisterNetworkCallback(this.mDefaultNetworkCallback);
            }
        }
    }

    private void registerDefaultNetworkChangeCallback() {
        removeDefaultNetworkChangeCallback();
        synchronized (this.mHasRegisteredDefaultNetworkChangeCallback) {
            this.mHasRegisteredDefaultNetworkChangeCallback = true;
            this.mConnectivityManager.registerDefaultNetworkCallback(this.mDefaultNetworkCallback);
            sendMessageDelayed(obtainMessage(111), 5000);
        }
    }

    private void collectRequestNetworkMetrics(NetworkRequest networkRequest) {
        if (this.mNumPhones > 1 && networkRequest.networkCapabilities.hasCapability(0)) {
            TelephonyProto.TelephonyEvent.OnDemandDataSwitch onDemandDataSwitch = new TelephonyProto.TelephonyEvent.OnDemandDataSwitch();
            onDemandDataSwitch.apn = 2;
            onDemandDataSwitch.state = 1;
            TelephonyMetrics.getInstance().writeOnDemandDataSwitch(onDemandDataSwitch);
        }
    }

    private void collectReleaseNetworkMetrics(NetworkRequest networkRequest) {
        if (this.mNumPhones > 1 && networkRequest.networkCapabilities.hasCapability(0)) {
            TelephonyProto.TelephonyEvent.OnDemandDataSwitch onDemandDataSwitch = new TelephonyProto.TelephonyEvent.OnDemandDataSwitch();
            onDemandDataSwitch.apn = 2;
            onDemandDataSwitch.state = 2;
            TelephonyMetrics.getInstance().writeOnDemandDataSwitch(onDemandDataSwitch);
        }
    }

    private boolean onEvaluate(boolean requestsChanged, String reason) {
        StringBuilder sb = new StringBuilder(reason);
        if (isEmergency()) {
            log("onEvaluate for reason " + reason + " aborted due to Emergency");
            return false;
        } else if (VSimUtilsInner.isVSimEnabled() || VSimUtilsInner.isVSimCauseCardReload()) {
            log("onEvalute aborted due to vsim is on");
            return false;
        } else {
            boolean diffDetected = this.mHalCommandToUse != 2 && requestsChanged;
            int primaryDataSubId = this.mSubscriptionController.getDefaultDataSubId();
            if (primaryDataSubId != this.mPrimaryDataSubId) {
                sb.append(" mPrimaryDataSubId ");
                sb.append(this.mPrimaryDataSubId);
                sb.append("->");
                sb.append(primaryDataSubId);
                this.mPrimaryDataSubId = primaryDataSubId;
                this.mManualDdsSwitch = true;
            }
            boolean hasAnyActiveSubscription = false;
            for (int i = 0; i < this.mNumPhones; i++) {
                int sub = this.mSubscriptionController.getSubIdUsingPhoneId(i);
                if (SubscriptionManager.isValidSubscriptionId(sub)) {
                    hasAnyActiveSubscription = true;
                }
                if (sub != this.mPhoneSubscriptions[i]) {
                    sb.append(" phone[");
                    sb.append(i);
                    sb.append("] ");
                    sb.append(this.mPhoneSubscriptions[i]);
                    sb.append("->");
                    sb.append(sub);
                    this.mPhoneSubscriptions[i] = sub;
                    diffDetected = true;
                }
            }
            if (!hasAnyActiveSubscription) {
                transitionToEmergencyPhone();
            }
            int oldPreferredDataPhoneId = this.mPreferredDataPhoneId;
            if (hasAnyActiveSubscription) {
                updatePreferredDataPhoneId();
            }
            if (oldPreferredDataPhoneId != this.mPreferredDataPhoneId) {
                sb.append(" preferred phoneId ");
                sb.append(oldPreferredDataPhoneId);
                sb.append("->");
                sb.append(this.mPreferredDataPhoneId);
                diffDetected = true;
            }
            if (diffDetected) {
                log("evaluating due to " + sb.toString());
                if (this.mHalCommandToUse == 2) {
                    for (int phoneId = 0; phoneId < this.mNumPhones; phoneId++) {
                        this.mPhoneStates[phoneId].active = true;
                    }
                    sendRilCommands(this.mPreferredDataPhoneId);
                } else {
                    List<Integer> newActivePhones = new ArrayList<>();
                    if (this.mMaxActivePhones == this.mPhones.length) {
                        for (int i2 = 0; i2 < this.mMaxActivePhones; i2++) {
                            newActivePhones.add(Integer.valueOf(this.mPhones[i2].getPhoneId()));
                        }
                    } else {
                        for (DcRequest dcRequest : this.mPrioritizedDcRequests) {
                            int phoneIdForRequest = phoneIdForRequest(dcRequest.networkRequest);
                            log("phoneIdForRequest for request:" + dcRequest.networkRequest + ",return " + phoneIdForRequest);
                            if (phoneIdForRequest != -1 && !newActivePhones.contains(Integer.valueOf(phoneIdForRequest))) {
                                newActivePhones.add(Integer.valueOf(phoneIdForRequest));
                                if (newActivePhones.size() >= this.mMaxActivePhones) {
                                    break;
                                }
                            }
                        }
                        if (newActivePhones.size() < this.mMaxActivePhones && newActivePhones.contains(Integer.valueOf(this.mPreferredDataPhoneId)) && SubscriptionManager.isUsableSubIdValue(this.mPreferredDataPhoneId)) {
                            newActivePhones.add(Integer.valueOf(this.mPreferredDataPhoneId));
                        }
                    }
                    for (int phoneId2 = 0; phoneId2 < this.mNumPhones; phoneId2++) {
                        if (!newActivePhones.contains(Integer.valueOf(phoneId2))) {
                            deactivate(phoneId2);
                        }
                    }
                    for (Integer num : newActivePhones) {
                        activate(num.intValue());
                    }
                }
                this.mHwPhoneSwitcherEx.informDdsToQcril(primaryDataSubId);
                notifyPreferredDataSubIdChanged();
                this.mActivePhoneRegistrants.notifyRegistrants();
            }
            Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(getTopPrioritySubscriptionId()));
            if (phone != null) {
                phone.updateCurrentCarrierInProvider();
            }
            if ((PhoneFactory.IS_QCOM_DUAL_LTE_STACK || (SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false) && this.requestPhoneCount == 0 && "defaultChanged".equals(reason))) && !HwModemCapability.isCapabilitySupport(28)) {
                for (int phoneId3 = 0; phoneId3 < this.mNumPhones; phoneId3++) {
                    log("defaultChanged onRadioCapChanged + phoneid =  " + phoneId3);
                    onRadioCapChanged(phoneId3);
                }
            }
            this.mManualDdsSwitch = false;
            return diffDetected;
        }
    }

    /* access modifiers changed from: private */
    public static class PhoneState {
        public volatile boolean active;
        public long lastRequested;

        private PhoneState() {
            this.active = false;
            this.lastRequested = 0;
        }
    }

    @UnsupportedAppUsage
    private void activate(int phoneId) {
        if (!this.mPhoneStates[phoneId].active || (this.mManualDdsSwitch && PhoneFactory.IS_DUAL_VOLTE_SUPPORTED && PhoneFactory.IS_QCOM_DUAL_LTE_STACK)) {
            switchPhone(phoneId, true);
        }
    }

    @UnsupportedAppUsage
    private void deactivate(int phoneId) {
        if (!HwTelephonyFactory.getHwDataConnectionManager().isSwitchingToSlave() && !HwTelephonyFactory.getHwDataConnectionManager().isSlaveActive() && !HwTelephonyFactory.getHwDataConnectionManager().isSwitchingSmartCard()) {
            switchPhone(phoneId, false);
        }
    }

    private void switchPhone(int phoneId, boolean active) {
        PhoneState state = this.mPhoneStates[phoneId];
        if (state.active != active) {
            state.active = active;
            StringBuilder sb = new StringBuilder();
            sb.append(active ? "activate " : "deactivate ");
            sb.append(phoneId);
            log(sb.toString());
            state.lastRequested = System.currentTimeMillis();
            sendRilCommands(phoneId);
        }
    }

    public void onRadioCapChanged(int phoneId) {
        validatePhoneId(phoneId);
        Message msg = obtainMessage(106);
        msg.arg1 = phoneId;
        msg.sendToTarget();
    }

    public void overrideDefaultDataForEmergency(int phoneId, int overrideTimeSec, CompletableFuture<Boolean> dataSwitchResult) {
        validatePhoneId(phoneId);
        Message msg = obtainMessage(115);
        EmergencyOverrideRequest request = new EmergencyOverrideRequest();
        request.mPhoneId = phoneId;
        request.mGnssOverrideTimeMs = overrideTimeSec * 1000;
        request.mOverrideCompleteFuture = dataSwitchResult;
        msg.obj = request;
        msg.sendToTarget();
    }

    private void sendRilCommands(int phoneId) {
        if (SubscriptionManager.isValidPhoneId(phoneId) && phoneId < this.mNumPhones) {
            Message message = Message.obtain(this, 112, Integer.valueOf(phoneId));
            int i = this.mHalCommandToUse;
            if (i != 1 && i != 0) {
                int i2 = this.mPreferredDataPhoneId;
                if (phoneId == i2) {
                    this.mRadioConfig.setPreferredDataModem(i2, message);
                }
            } else if (this.mNumPhones > 1) {
                this.mPhones[phoneId].getDcTracker(1).setDataAllowed(isPhoneActive(phoneId), message);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPhoneCapabilityChangedInternal(PhoneCapability capability) {
        int newMaxActivePhones = ((TelephonyManager) this.mContext.getSystemService("phone")).getNumberOfModemsWithSimultaneousDataConnections();
        if (this.mMaxActivePhones != newMaxActivePhones) {
            this.mMaxActivePhones = newMaxActivePhones;
            log("Max active phones changed to " + this.mMaxActivePhones);
            onEvaluate(false, "phoneCfgChanged");
        }
    }

    private int phoneIdForRequest(NetworkRequest netRequest) {
        int subId = getSubIdFromNetworkRequest(netRequest);
        if (subId == Integer.MAX_VALUE) {
            return this.mPreferredDataPhoneId;
        }
        if (subId == -1) {
            return -1;
        }
        int preferredDataSubId = SubscriptionManager.isValidPhoneId(this.mPreferredDataPhoneId) ? this.mPhoneSubscriptions[this.mPreferredDataPhoneId] : -1;
        if (netRequest.hasCapability(12) && netRequest.hasCapability(13) && subId != preferredDataSubId && subId != this.mValidator.getSubIdInValidation()) {
            return -1;
        }
        for (int i = 0; i < this.mNumPhones; i++) {
            if (this.mPhoneSubscriptions[i] == subId) {
                return i;
            }
        }
        return -1;
    }

    private int getSubIdFromNetworkRequest(NetworkRequest networkRequest) {
        NetworkSpecifier specifier = networkRequest.networkCapabilities.getNetworkSpecifier();
        if (specifier == null) {
            return KeepaliveStatus.INVALID_HANDLE;
        }
        if (!(specifier instanceof StringNetworkSpecifier)) {
            return -1;
        }
        if (5 != new DcRequest(networkRequest, this.mContext).apnType || !this.mManualDdsSwitch || this.mMaxActivePhones == this.mNumPhones) {
            try {
                return Integer.parseInt(((StringNetworkSpecifier) specifier).specifier);
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, "NumberFormatException on " + ((StringNetworkSpecifier) specifier).specifier);
                return -1;
            }
        } else {
            log("specifier is not empty but default data switched use default data sub");
            return this.mPrimaryDataSubId;
        }
    }

    private int getSubIdForDefaultNetworkRequests() {
        if (this.mSubscriptionController.isActiveSubId(this.mOpptDataSubId)) {
            return this.mOpptDataSubId;
        }
        return this.mPrimaryDataSubId;
    }

    private void updatePreferredDataPhoneId() {
        Phone voicePhone = findPhoneById(this.mPhoneIdInVoiceCall);
        EmergencyOverrideRequest emergencyOverrideRequest = this.mEmergencyOverride;
        if (emergencyOverrideRequest != null && findPhoneById(emergencyOverrideRequest.mPhoneId) != null) {
            log("updatePreferredDataPhoneId: preferred data overridden for emergency. phoneId = " + this.mEmergencyOverride.mPhoneId);
            this.mPreferredDataPhoneId = this.mEmergencyOverride.mPhoneId;
        } else if (voicePhone == null || !voicePhone.getDataEnabledSettings().isDataEnabled(17)) {
            int subId = getSubIdForDefaultNetworkRequests();
            int phoneId = -1;
            if (SubscriptionManager.isUsableSubIdValue(subId)) {
                int i = 0;
                while (true) {
                    if (i >= this.mNumPhones) {
                        break;
                    } else if (this.mPhoneSubscriptions[i] == subId) {
                        phoneId = i;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            this.mPreferredDataPhoneId = phoneId;
        } else {
            this.mPreferredDataPhoneId = this.mPhoneIdInVoiceCall;
        }
        this.mPreferredDataSubId = this.mSubscriptionController.getSubIdUsingPhoneId(this.mPreferredDataPhoneId);
    }

    private void transitionToEmergencyPhone() {
        if (this.mPreferredDataPhoneId != 0) {
            log("No active subscriptions: resetting preferred phone to 0 for emergency");
            this.mPreferredDataPhoneId = 0;
        }
        if (this.mPreferredDataSubId != -1) {
            this.mPreferredDataSubId = -1;
            notifyPreferredDataSubIdChanged();
        }
    }

    private Phone findPhoneById(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mNumPhones) {
            return null;
        }
        return this.mPhones[phoneId];
    }

    public boolean shouldApplyNetworkRequest(NetworkRequest networkRequest, int phoneId) {
        validatePhoneId(phoneId);
        if (!isPhoneActive(phoneId) || (this.mSubscriptionController.getSubIdUsingPhoneId(phoneId) == -1 && !isEmergencyNetworkRequest(networkRequest))) {
            return false;
        }
        int phoneIdToHandle = phoneIdForRequest(networkRequest);
        log("phoneIdForRequest for request:" + networkRequest + ",return " + phoneIdToHandle);
        if (phoneId == phoneIdToHandle) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isEmergencyNetworkRequest(NetworkRequest networkRequest) {
        return networkRequest.hasCapability(10);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean isPhoneActive(int phoneId) {
        return this.mPhoneStates[phoneId].active;
    }

    public void registerForActivePhoneSwitch(Handler h, int what, Object o) {
        Registrant r = new Registrant(h, what, o);
        this.mActivePhoneRegistrants.add(r);
        r.notifyRegistrant();
    }

    public void unregisterForActivePhoneSwitch(Handler h) {
        this.mActivePhoneRegistrants.remove(h);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void validatePhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mNumPhones) {
            throw new IllegalArgumentException("Invalid PhoneId");
        }
    }

    private void setOpportunisticDataSubscription(int subId, boolean needValidation, ISetOpportunisticDataCallback callback) {
        if (this.mSubscriptionController.isActiveSubId(subId) || subId == Integer.MAX_VALUE) {
            int subIdToValidate = subId == Integer.MAX_VALUE ? this.mPrimaryDataSubId : subId;
            if (this.mValidator.isValidating() && (!needValidation || subIdToValidate != this.mValidator.getSubIdInValidation())) {
                this.mValidator.stopValidation();
            }
            if (subId == this.mOpptDataSubId) {
                sendSetOpptCallbackHelper(callback, 0);
            } else if (!this.mValidator.isValidationFeatureSupported() || !needValidation) {
                setOpportunisticSubscriptionInternal(subId);
                sendSetOpptCallbackHelper(callback, 0);
            } else {
                logDataSwitchEvent(subId, 1, 3);
                registerDefaultNetworkChangeCallback();
                this.mSetOpptSubCallback = callback;
                this.mValidator.validate(subIdToValidate, 2000, false, this.mValidationCallback);
            }
        } else {
            log("Can't switch data to inactive subId " + subId);
            sendSetOpptCallbackHelper(callback, 2);
        }
    }

    private void sendSetOpptCallbackHelper(ISetOpportunisticDataCallback callback, int result) {
        if (callback != null) {
            try {
                callback.onComplete(result);
            } catch (RemoteException exception) {
                log("RemoteException " + exception);
            }
        }
    }

    private void setOpportunisticSubscriptionInternal(int subId) {
        if (this.mOpptDataSubId != subId) {
            this.mOpptDataSubId = subId;
            if (onEvaluate(false, "oppt data subId changed")) {
                logDataSwitchEvent(this.mOpptDataSubId, 1, 3);
                registerDefaultNetworkChangeCallback();
            }
        }
    }

    private void onValidationDone(int subId, boolean passed) {
        int resultForCallBack;
        StringBuilder sb = new StringBuilder();
        sb.append("onValidationDone: ");
        sb.append(passed ? "passed" : "failed");
        sb.append(" on subId ");
        sb.append(subId);
        log(sb.toString());
        if (!this.mSubscriptionController.isActiveSubId(subId)) {
            log("onValidationDone: subId " + subId + " is no longer active");
            resultForCallBack = 2;
        } else if (!passed) {
            resultForCallBack = 1;
        } else {
            if (this.mSubscriptionController.isOpportunistic(subId)) {
                setOpportunisticSubscriptionInternal(subId);
            } else {
                setOpportunisticSubscriptionInternal(KeepaliveStatus.INVALID_HANDLE);
            }
            resultForCallBack = 0;
        }
        sendSetOpptCallbackHelper(this.mSetOpptSubCallback, resultForCallBack);
        this.mSetOpptSubCallback = null;
    }

    public void trySetOpportunisticDataSubscription(int subId, boolean needValidation, ISetOpportunisticDataCallback callback) {
        StringBuilder sb = new StringBuilder();
        sb.append("Try set opportunistic data subscription to subId ");
        sb.append(subId);
        sb.append(needValidation ? " with " : " without ");
        sb.append("validation");
        log(sb.toString());
        obtainMessage(EVENT_OPPT_DATA_SUB_CHANGED, subId, needValidation ? 1 : 0, callback).sendToTarget();
    }

    private boolean isCallActive(Phone phone) {
        if (phone == null) {
            return false;
        }
        if (phone.getForegroundCall().getState() == Call.State.ACTIVE || phone.getForegroundCall().getState() == Call.State.ALERTING) {
            return true;
        }
        return false;
    }

    private void updateHalCommandToUse() {
        this.mHalCommandToUse = this.mRadioConfig.isSetPreferredDataCommandSupported() ? 2 : 1;
    }

    public int getOpportunisticDataSubscriptionId() {
        return this.mOpptDataSubId;
    }

    public int getPreferredDataPhoneId() {
        return this.mPreferredDataPhoneId;
    }

    @UnsupportedAppUsage
    private void log(String l) {
        Rlog.d(LOG_TAG, l);
        this.mLocalLog.log(l);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logDataSwitchEvent(int subId, int state, int reason) {
        int subId2 = subId == Integer.MAX_VALUE ? this.mPrimaryDataSubId : subId;
        TelephonyProto.TelephonyEvent.DataSwitch dataSwitch = new TelephonyProto.TelephonyEvent.DataSwitch();
        dataSwitch.state = state;
        dataSwitch.reason = reason;
        TelephonyMetrics.getInstance().writeDataSwitch(subId2, dataSwitch);
    }

    private void notifyPreferredDataSubIdChanged() {
        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        try {
            log("notifyPreferredDataSubIdChanged to " + this.mPreferredDataSubId);
            tr.notifyActiveDataSubIdChanged(this.mPreferredDataSubId);
        } catch (RemoteException e) {
        }
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        String str;
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println("PhoneSwitcher:");
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < this.mNumPhones; i++) {
            PhoneState ps = this.mPhoneStates[i];
            c.setTimeInMillis(ps.lastRequested);
            StringBuilder sb = new StringBuilder();
            sb.append("PhoneId(");
            sb.append(i);
            sb.append(") active=");
            sb.append(ps.active);
            sb.append(", lastRequest=");
            if (ps.lastRequested == 0) {
                str = "never";
            } else {
                str = String.format("%tm-%td %tH:%tM:%tS.%tL", c, c, c, c, c, c);
            }
            sb.append(str);
            pw.println(sb.toString());
        }
        pw.increaseIndent();
        this.mLocalLog.dump(fd, pw, args);
        pw.decreaseIndent();
    }

    public int getTopPrioritySubscriptionId() {
        return this.mHwPhoneSwitcherEx.getTopPrioritySubscriptionId(this.mPrioritizedDcRequests, this.mPhoneSubscriptions);
    }

    public boolean isDualPsAllowedForSmartSwitch() {
        return this.mHwPhoneSwitcherEx.isDualPsAllowedForSmartSwitch();
    }

    public int onDataSubChange() {
        onEvaluate(false, "defaultChanged");
        return this.requestPhoneCount;
    }

    @Override // com.android.internal.telephony.IHwPhoneSwitcherInner
    public RegistrantList getActivePhoneRegistrants(int phoneId) {
        return this.mActivePhoneRegistrants;
    }

    @Override // com.android.internal.telephony.IHwPhoneSwitcherInner
    public int phoneIdForRequestForEx(NetworkRequest netRequest, int apnid) {
        return phoneIdForRequest(netRequest);
    }

    @Override // com.android.internal.telephony.IHwPhoneSwitcherInner
    public void resendDataAllowedForEx(int phoneId) {
        onRadioCapChanged(phoneId);
    }

    @Override // com.android.internal.telephony.IHwPhoneSwitcherInner
    public Context getPhoneContext() {
        return this.mContext;
    }

    @Override // com.android.internal.telephony.IHwPhoneSwitcherInner
    public void onDualPsStateChanged(boolean isDualPsAllowed, String reason) {
        int newMaxActivePhones = isDualPsAllowed ? 2 : 1;
        if (this.mMaxActivePhones != newMaxActivePhones) {
            this.mMaxActivePhones = newMaxActivePhones;
            log("Max active phones changed to " + this.mMaxActivePhones);
            onEvaluate(false, reason);
        }
    }

    private boolean enableInCallChange() {
        return false;
    }
}
