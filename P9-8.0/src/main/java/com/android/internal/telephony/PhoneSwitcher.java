package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.MatchAllNetworkSpecifier;
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
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.util.LocalLog;
import com.android.internal.telephony.IOnSubscriptionsChangedListener.Stub;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class PhoneSwitcher extends Handler {
    private static final int ALLOW_DATA_RETRY_DELAY = 15000;
    private static final int EVENT_ALLOW_DATA_RESPONSE = 110;
    private static final int EVENT_DEFAULT_SUBSCRIPTION_CHANGED = 101;
    private static final int EVENT_EMERGENCY_TOGGLE = 105;
    private static final int EVENT_RELEASE_NETWORK = 104;
    private static final int EVENT_REQUEST_NETWORK = 103;
    private static final int EVENT_RESEND_DATA_ALLOWED = 106;
    private static final int EVENT_RETRY_ALLOW_DATA = 111;
    private static final int EVENT_SUBSCRIPTION_CHANGED = 102;
    private static final int EVENT_VOICE_CALL_ENDED = 112;
    private static final String LOG_TAG = "PhoneSwitcher";
    private static final int MAX_CONNECT_FAILURE_COUNT = 10;
    private static final int MAX_LOCAL_LOG_LINES = 30;
    private static final boolean REQUESTS_CHANGED = true;
    private static final boolean REQUESTS_UNCHANGED = false;
    private static final boolean VDBG = false;
    private final RegistrantList[] mActivePhoneRegistrants;
    private int[] mAllowDataFailure;
    private final CommandsInterface[] mCommandsInterfaces;
    private final Context mContext;
    private final BroadcastReceiver mDefaultDataChangedReceiver;
    private int mDefaultDataSubscription;
    private final LocalLog mLocalLog;
    private boolean mManualDdsSwitch;
    private final int mMaxActivePhones;
    private final int mNumPhones;
    private final PhoneState[] mPhoneStates;
    private final int[] mPhoneSubscriptions;
    private final Phone[] mPhones;
    private final List<DcRequest> mPrioritizedDcRequests;
    private final SubscriptionController mSubscriptionController;
    private final IOnSubscriptionsChangedListener mSubscriptionsChangedListener;

    private static class PhoneState {
        public volatile boolean active;
        public long lastRequested;

        /* synthetic */ PhoneState(PhoneState -this0) {
            this();
        }

        private PhoneState() {
            this.active = false;
            this.lastRequested = 0;
        }
    }

    private static class PhoneSwitcherNetworkRequestListener extends NetworkFactory {
        private final PhoneSwitcher mPhoneSwitcher;

        public PhoneSwitcherNetworkRequestListener(Looper l, Context c, NetworkCapabilities nc, PhoneSwitcher ps) {
            super(l, c, "PhoneSwitcherNetworkRequstListener", nc);
            this.mPhoneSwitcher = ps;
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            Message msg = this.mPhoneSwitcher.obtainMessage(PhoneSwitcher.EVENT_REQUEST_NETWORK);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            if (networkRequest.networkCapabilities.hasCapability(26)) {
                log("releaseNetworkFor internal_default, just return.");
                return;
            }
            Message msg = this.mPhoneSwitcher.obtainMessage(104);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }
    }

    public PhoneSwitcher(Looper looper) {
        super(looper);
        this.mPrioritizedDcRequests = new ArrayList();
        this.mManualDdsSwitch = false;
        this.mDefaultDataChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                PhoneSwitcher.this.obtainMessage(101).sendToTarget();
            }
        };
        this.mSubscriptionsChangedListener = new Stub() {
            public void onSubscriptionsChanged() {
                PhoneSwitcher.this.obtainMessage(102).sendToTarget();
            }
        };
        this.mMaxActivePhones = 0;
        this.mSubscriptionController = null;
        this.mPhoneSubscriptions = null;
        this.mCommandsInterfaces = null;
        this.mContext = null;
        this.mPhoneStates = null;
        this.mPhones = null;
        this.mLocalLog = null;
        this.mActivePhoneRegistrants = null;
        this.mNumPhones = 0;
    }

    public PhoneSwitcher(int maxActivePhones, int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, ITelephonyRegistry tr, CommandsInterface[] cis, Phone[] phones) {
        super(looper);
        this.mPrioritizedDcRequests = new ArrayList();
        this.mManualDdsSwitch = false;
        this.mDefaultDataChangedReceiver = /* anonymous class already generated */;
        this.mSubscriptionsChangedListener = /* anonymous class already generated */;
        this.mContext = context;
        this.mNumPhones = numPhones;
        this.mPhones = phones;
        this.mPhoneSubscriptions = new int[numPhones];
        this.mAllowDataFailure = new int[numPhones];
        this.mMaxActivePhones = maxActivePhones;
        this.mLocalLog = new LocalLog(30);
        CallManager.getInstance().registerForDisconnect(this, 112, null);
        this.mSubscriptionController = subscriptionController;
        this.mActivePhoneRegistrants = new RegistrantList[numPhones];
        this.mPhoneStates = new PhoneState[numPhones];
        for (int i = 0; i < numPhones; i++) {
            this.mActivePhoneRegistrants[i] = new RegistrantList();
            this.mPhoneStates[i] = new PhoneState();
            if (this.mPhones[i] != null) {
                this.mPhones[i].registerForEmergencyCallToggle(this, 105, null);
            }
        }
        this.mCommandsInterfaces = cis;
        try {
            tr.addOnSubscriptionsChangedListener(LOG_TAG, this.mSubscriptionsChangedListener);
        } catch (RemoteException e) {
        }
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
        netCap.addCapability(19);
        netCap.addCapability(20);
        netCap.addCapability(21);
        netCap.addCapability(22);
        netCap.addCapability(23);
        netCap.addCapability(24);
        netCap.addCapability(25);
        netCap.addCapability(26);
        netCap.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        NetworkFactory networkFactory = new PhoneSwitcherNetworkRequestListener(looper, context, netCap, this);
        networkFactory.setScoreFilter(101);
        networkFactory.register();
        NetworkCapabilities netCapForVowifi = new NetworkCapabilities();
        netCapForVowifi.addTransportType(1);
        netCapForVowifi.addCapability(0);
        netCapForVowifi.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        NetworkFactory networkFactoryForVowifi = new PhoneSwitcherNetworkRequestListener(looper, context, netCapForVowifi, this);
        networkFactoryForVowifi.setScoreFilter(101);
        networkFactoryForVowifi.register();
        log("PhoneSwitcher started");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 101:
                onEvaluate(false, "defaultChanged");
                return;
            case 102:
                onEvaluate(false, "subChanged");
                return;
            case EVENT_REQUEST_NETWORK /*103*/:
                onRequestNetwork((NetworkRequest) msg.obj);
                return;
            case 104:
                onReleaseNetwork((NetworkRequest) msg.obj);
                return;
            case 105:
                onEvaluate(true, "emergencyToggle");
                return;
            case 106:
                onResendDataAllowed(msg);
                return;
            case EVENT_ALLOW_DATA_RESPONSE /*110*/:
                onAllowDataResponse(msg.arg1, (AsyncResult) msg.obj);
                return;
            case 111:
                onRetryAllowData(msg.arg1);
                return;
            case 112:
                log("EVENT_VOICE_CALL_ENDED");
                int ddsPhoneId = this.mSubscriptionController.getPhoneId(this.mSubscriptionController.getDefaultDataSubId());
                if (SubscriptionManager.isValidPhoneId(ddsPhoneId) && (isAnyVoiceCallActiveOnDevice() ^ 1) != 0 && getConnectFailureCount(ddsPhoneId) > 0) {
                    resendDataAllowed(ddsPhoneId);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void onRetryAllowData(int phoneId) {
        if (this.mSubscriptionController.getPhoneId(this.mSubscriptionController.getDefaultDataSubId()) == phoneId) {
            log("Running retry connect/allow_data");
            resendDataAllowed(phoneId);
            return;
        }
        log("Dds sub changed");
        resetConnectFailureCount(phoneId);
    }

    private void resetConnectFailureCount(int phoneId) {
        this.mAllowDataFailure[phoneId] = 0;
    }

    private void incConnectFailureCount(int phoneId) {
        int[] iArr = this.mAllowDataFailure;
        iArr[phoneId] = iArr[phoneId] + 1;
    }

    private int getConnectFailureCount(int phoneId) {
        return this.mAllowDataFailure[phoneId];
    }

    private void handleConnectMaxFailure(int phoneId) {
        resetConnectFailureCount(phoneId);
        int ddsPhoneId = this.mSubscriptionController.getPhoneId(this.mSubscriptionController.getDefaultDataSubId());
        if (ddsPhoneId > 0 && ddsPhoneId < this.mNumPhones && phoneId == ddsPhoneId) {
            log("ALLOW_DATA retries exhausted on phoneId = " + phoneId);
            enforceDds(ddsPhoneId);
        }
    }

    private void enforceDds(int phoneId) {
        int[] subId = this.mSubscriptionController.getSubId(phoneId);
        log("enforceDds: subId = " + subId[0]);
        this.mSubscriptionController.setDefaultDataSubId(subId[0]);
    }

    private boolean isAnyVoiceCallActiveOnDevice() {
        boolean ret = CallManager.getInstance().getState() != State.IDLE;
        log("isAnyVoiceCallActiveOnDevice: " + ret);
        return ret;
    }

    private void onAllowDataResponse(int phoneId, AsyncResult ar) {
        if (ar.userObj != null) {
            Message message = ar.userObj;
            AsyncResult.forMessage(message, ar.result, ar.exception);
            message.sendToTarget();
        }
        if (ar.exception != null) {
            incConnectFailureCount(phoneId);
            if (isAnyVoiceCallActiveOnDevice()) {
                log("Wait for call end indication");
                return;
            }
            log("Allow_data failed on phoneId = " + phoneId + ", failureCount = " + getConnectFailureCount(phoneId));
            if (getConnectFailureCount(phoneId) >= 10) {
                handleConnectMaxFailure(phoneId);
            } else {
                log("Scheduling retry connect/allow_data");
                if (hasMessages(111, this.mPhones[phoneId])) {
                    log("already has EVENT_RETRY_ALLOW_DATA, phoneId: " + phoneId + ", remove it and reset count");
                    removeMessages(111, this.mPhones[phoneId]);
                    resetConnectFailureCount(phoneId);
                }
                sendMessageDelayed(obtainMessage(111, phoneId, 0, this.mPhones[phoneId]), 15000);
            }
        } else {
            log("Allow_data success on phoneId = " + phoneId);
            resetConnectFailureCount(phoneId);
            this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
        }
    }

    private boolean isEmergency() {
        for (Phone p : this.mPhones) {
            if (p != null && (p.isInEcm() || p.isInEmergencyCall())) {
                return true;
            }
        }
        return false;
    }

    private void onRequestNetwork(NetworkRequest networkRequest) {
        DcRequest dcRequest = new DcRequest(networkRequest, this.mContext);
        if (!this.mPrioritizedDcRequests.contains(dcRequest)) {
            this.mPrioritizedDcRequests.add(dcRequest);
            Collections.sort(this.mPrioritizedDcRequests);
            onEvaluate(true, "netRequest");
        }
    }

    private void onReleaseNetwork(NetworkRequest networkRequest) {
        if (this.mPrioritizedDcRequests.remove(new DcRequest(networkRequest, this.mContext))) {
            onEvaluate(true, "netReleased");
        }
    }

    private int onEvaluate(boolean requestsChanged, String reason, int what, Handler handler) {
        int requestPhoneCount = 0;
        StringBuilder sb = new StringBuilder(reason);
        if (isEmergency()) {
            log("onEvalute aborted due to Emergency");
            return 0;
        } else if ((VSimUtilsInner.isVSimEnabled() || VSimUtilsInner.isVSimCauseCardReload()) && VSimUtilsInner.isPlatformRealTripple()) {
            log("onEvalute aborted due to vsim is on");
            return 0;
        } else {
            boolean diffDetected = requestsChanged;
            int dataSub = this.mSubscriptionController.getDefaultDataSubId();
            if (dataSub != this.mDefaultDataSubscription) {
                sb.append(" default ").append(this.mDefaultDataSubscription).append("->").append(dataSub);
                this.mDefaultDataSubscription = dataSub;
                diffDetected = true;
                this.mManualDdsSwitch = true;
            }
            for (int i = 0; i < this.mNumPhones; i++) {
                int sub = this.mSubscriptionController.getSubIdUsingPhoneId(i);
                if (sub != this.mPhoneSubscriptions[i]) {
                    sb.append(" phone[").append(i).append("] ").append(this.mPhoneSubscriptions[i]);
                    sb.append("->").append(sub);
                    this.mPhoneSubscriptions[i] = sub;
                    diffDetected = true;
                }
            }
            if (diffDetected) {
                int phoneId;
                log("evaluating due to " + sb.toString());
                if (HwModemCapability.isCapabilitySupport(9)) {
                    this.mSubscriptionController.informDdsToQcril(dataSub);
                }
                List<Integer> newActivePhones = new ArrayList();
                for (DcRequest dcRequest : this.mPrioritizedDcRequests) {
                    int phoneIdForRequest = phoneIdForRequest(dcRequest.networkRequest, dcRequest.apnId);
                    if (!(phoneIdForRequest == -1 || newActivePhones.contains(Integer.valueOf(phoneIdForRequest)))) {
                        newActivePhones.add(Integer.valueOf(phoneIdForRequest));
                        if (newActivePhones.size() >= this.mMaxActivePhones) {
                            break;
                        }
                    }
                }
                for (phoneId = 0; phoneId < this.mNumPhones; phoneId++) {
                    if (!newActivePhones.contains(Integer.valueOf(phoneId))) {
                        requestPhoneCount += deactivate(phoneId, what, handler);
                    }
                }
                for (Integer intValue : newActivePhones) {
                    requestPhoneCount += activate(intValue.intValue(), what, handler);
                }
                Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(getTopPrioritySubscriptionId()));
                if (phone != null) {
                    phone.updateCurrentCarrierInProvider();
                }
                if (SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false) && requestPhoneCount == 0 && "defaultChanged".equals(reason)) {
                    for (phoneId = 0; phoneId < this.mNumPhones; phoneId++) {
                        log("defaultChanged resendDataAllowed + phoneid =  " + phoneId);
                        resendDataAllowed(phoneId);
                    }
                }
            }
            this.mManualDdsSwitch = false;
            return requestPhoneCount;
        }
    }

    private int deactivate(int phoneId, int what, Handler handler) {
        PhoneState state = this.mPhoneStates[phoneId];
        if (!state.active || HwTelephonyFactory.getHwDataConnectionManager().isSwitchingToSlave()) {
            return 0;
        }
        state.active = false;
        log("deactivate " + phoneId);
        state.lastRequested = System.currentTimeMillis();
        Object message = null;
        if (handler != null) {
            message = handler.obtainMessage(what);
        }
        this.mPhones[phoneId].mDcTracker.setDataAllowed(false, obtainMessage(EVENT_ALLOW_DATA_RESPONSE, phoneId, 0, message));
        this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
        return 1;
    }

    private int activate(int phoneId, int what, Handler handler) {
        PhoneState state = this.mPhoneStates[phoneId];
        if (state.active) {
            return 0;
        }
        state.active = true;
        log("activate " + phoneId);
        state.lastRequested = System.currentTimeMillis();
        Object message = null;
        if (handler != null) {
            message = handler.obtainMessage(what);
        }
        this.mPhones[phoneId].mDcTracker.setDataAllowed(true, obtainMessage(EVENT_ALLOW_DATA_RESPONSE, phoneId, 0, message));
        this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
        return 1;
    }

    public void resendDataAllowed(int phoneId) {
        validatePhoneId(phoneId);
        Message msg = obtainMessage(106);
        msg.arg1 = phoneId;
        msg.sendToTarget();
    }

    private void onResendDataAllowed(Message msg) {
        int phoneId = msg.arg1;
        this.mCommandsInterfaces[phoneId].setDataAllowed(this.mPhoneStates[phoneId].active, obtainMessage(EVENT_ALLOW_DATA_RESPONSE, phoneId, 0));
    }

    private int phoneIdForRequest(NetworkRequest netRequest, int apnid) {
        int subId;
        NetworkSpecifier specifier = netRequest.networkCapabilities.getNetworkSpecifier();
        if (specifier == null) {
            subId = this.mDefaultDataSubscription;
        } else if (!(specifier instanceof StringNetworkSpecifier)) {
            subId = -1;
        } else if (5 == apnid && this.mManualDdsSwitch) {
            log("specifier is not empty but default data switched use default data sub");
            subId = this.mDefaultDataSubscription;
        } else {
            try {
                subId = Integer.parseInt(((StringNetworkSpecifier) specifier).specifier);
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, "NumberFormatException on " + ((StringNetworkSpecifier) specifier).specifier);
                subId = -1;
            }
        }
        int phoneId = -1;
        if (subId == -1) {
            return -1;
        }
        for (int i = 0; i < this.mNumPhones; i++) {
            if (this.mPhoneSubscriptions[i] == subId) {
                phoneId = i;
                break;
            }
        }
        log("phoneIdForRequest return " + phoneId);
        return phoneId;
    }

    public boolean isPhoneActive(int phoneId) {
        validatePhoneId(phoneId);
        return this.mPhoneStates[phoneId].active;
    }

    public void registerForActivePhoneSwitch(int phoneId, Handler h, int what, Object o) {
        validatePhoneId(phoneId);
        Registrant r = new Registrant(h, what, o);
        this.mActivePhoneRegistrants[phoneId].add(r);
        r.notifyRegistrant();
    }

    public void unregisterForActivePhoneSwitch(int phoneId, Handler h) {
        validatePhoneId(phoneId);
        this.mActivePhoneRegistrants[phoneId].remove(h);
    }

    private void validatePhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mNumPhones) {
            throw new IllegalArgumentException("Invalid PhoneId");
        }
    }

    private void log(String l) {
        Rlog.d(LOG_TAG, l);
        this.mLocalLog.log(l);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println("PhoneSwitcher:");
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < this.mNumPhones; i++) {
            String str;
            PhoneState ps = this.mPhoneStates[i];
            c.setTimeInMillis(ps.lastRequested);
            StringBuilder append = new StringBuilder().append("PhoneId(").append(i).append(") active=").append(ps.active).append(", lastRequest=");
            if (ps.lastRequested == 0) {
                str = "never";
            } else {
                str = String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c});
            }
            pw.println(append.append(str).toString());
        }
        pw.increaseIndent();
        this.mLocalLog.dump(fd, pw, args);
        pw.decreaseIndent();
    }

    public int getTopPrioritySubscriptionId() {
        if (VSimUtilsInner.isVSimOn() && VSimUtilsInner.isPlatformRealTripple()) {
            return VSimUtilsInner.getTopPrioritySubscriptionId();
        }
        if (this.mPrioritizedDcRequests.size() > 0) {
            DcRequest request = (DcRequest) this.mPrioritizedDcRequests.get(0);
            if (request != null) {
                int phoneId = phoneIdForRequest(request.networkRequest, request.apnId);
                if (phoneId >= 0 && phoneId < this.mPhoneSubscriptions.length) {
                    return this.mPhoneSubscriptions[phoneId];
                }
            }
        }
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    public int onDataSubChange(int what, Handler handler) {
        return onEvaluate(false, "defaultChanged", what, handler);
    }

    private void onEvaluate(boolean requestsChanged, String reason) {
        onEvaluate(requestsChanged, reason, 0, null);
    }
}
