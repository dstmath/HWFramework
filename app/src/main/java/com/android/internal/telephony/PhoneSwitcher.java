package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
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
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.telephony.IOnSubscriptionsChangedListener.Stub;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class PhoneSwitcher extends Handler {
    private static final int ALLOW_DATA_RETRY_DELAY = 30000;
    private static final int EVENT_ALLOW_DATA_RESPONSE = 110;
    private static final int EVENT_DEFAULT_SUBSCRIPTION_CHANGED = 101;
    private static final int EVENT_EMERGENCY_TOGGLE = 105;
    private static final int EVENT_RELEASE_NETWORK = 104;
    private static final int EVENT_REQUEST_NETWORK = 103;
    private static final int EVENT_RESEND_DATA_ALLOWED = 106;
    private static final int EVENT_RETRY_ALLOW_DATA = 111;
    private static final int EVENT_SUBSCRIPTION_CHANGED = 102;
    private static final String LOG_TAG = "PhoneSwitcher";
    private static final int MAX_CONNECT_FAILURE_COUNT = 5;
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
            Message msg = this.mPhoneSwitcher.obtainMessage(PhoneSwitcher.EVENT_RELEASE_NETWORK);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }
    }

    public PhoneSwitcher(Looper looper) {
        super(looper);
        this.mPrioritizedDcRequests = new ArrayList();
        this.mDefaultDataChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                PhoneSwitcher.this.obtainMessage(PhoneSwitcher.EVENT_DEFAULT_SUBSCRIPTION_CHANGED).sendToTarget();
            }
        };
        this.mSubscriptionsChangedListener = new Stub() {
            public void onSubscriptionsChanged() {
                PhoneSwitcher.this.obtainMessage(PhoneSwitcher.EVENT_SUBSCRIPTION_CHANGED).sendToTarget();
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
        this.mDefaultDataChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                PhoneSwitcher.this.obtainMessage(PhoneSwitcher.EVENT_DEFAULT_SUBSCRIPTION_CHANGED).sendToTarget();
            }
        };
        this.mSubscriptionsChangedListener = new Stub() {
            public void onSubscriptionsChanged() {
                PhoneSwitcher.this.obtainMessage(PhoneSwitcher.EVENT_SUBSCRIPTION_CHANGED).sendToTarget();
            }
        };
        this.mContext = context;
        this.mNumPhones = numPhones;
        this.mPhones = phones;
        this.mPhoneSubscriptions = new int[numPhones];
        this.mAllowDataFailure = new int[numPhones];
        this.mMaxActivePhones = maxActivePhones;
        this.mLocalLog = new LocalLog(MAX_LOCAL_LOG_LINES);
        this.mSubscriptionController = subscriptionController;
        this.mActivePhoneRegistrants = new RegistrantList[numPhones];
        this.mPhoneStates = new PhoneState[numPhones];
        for (int i = 0; i < numPhones; i++) {
            this.mActivePhoneRegistrants[i] = new RegistrantList();
            this.mPhoneStates[i] = new PhoneState();
            if (this.mPhones[i] != null) {
                this.mPhones[i].registerForEmergencyCallToggle(this, EVENT_EMERGENCY_TOGGLE, null);
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
        netCap.addCapability(MAX_CONNECT_FAILURE_COUNT);
        netCap.addCapability(7);
        netCap.addCapability(8);
        netCap.addCapability(9);
        netCap.addCapability(10);
        netCap.addCapability(13);
        netCap.addCapability(12);
        netCap.addCapability(18);
        netCap.addCapability(19);
        netCap.addCapability(20);
        netCap.addCapability(21);
        netCap.addCapability(22);
        netCap.addCapability(23);
        netCap.addCapability(24);
        netCap.setNetworkSpecifier(CharacterSets.MIMENAME_ANY_CHARSET);
        NetworkFactory networkFactory = new PhoneSwitcherNetworkRequestListener(looper, context, netCap, this);
        networkFactory.setScoreFilter(EVENT_DEFAULT_SUBSCRIPTION_CHANGED);
        networkFactory.register();
        log("PhoneSwitcher started");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_DEFAULT_SUBSCRIPTION_CHANGED /*101*/:
                onEvaluate(false, "defaultChanged");
            case EVENT_SUBSCRIPTION_CHANGED /*102*/:
                onEvaluate(false, "subChanged");
            case EVENT_REQUEST_NETWORK /*103*/:
                onRequestNetwork((NetworkRequest) msg.obj);
            case EVENT_RELEASE_NETWORK /*104*/:
                onReleaseNetwork((NetworkRequest) msg.obj);
            case EVENT_EMERGENCY_TOGGLE /*105*/:
                onEvaluate(REQUESTS_CHANGED, "emergencyToggle");
            case EVENT_RESEND_DATA_ALLOWED /*106*/:
                onResendDataAllowed(msg);
            case EVENT_ALLOW_DATA_RESPONSE /*110*/:
                onAllowDataResponse(msg.arg1, (AsyncResult) msg.obj);
            case EVENT_RETRY_ALLOW_DATA /*111*/:
                onRetryAllowData(msg.arg1);
            default:
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
        boolean ret = CallManager.getInstance().getState() != State.IDLE ? REQUESTS_CHANGED : false;
        log("isAnyVoiceCallActiveOnDevice: " + ret);
        return ret;
    }

    private void onAllowDataResponse(int phoneId, AsyncResult ar) {
        if (ar.userObj != null) {
            Message message = ar.userObj;
            AsyncResult.forMessage(message, ar.result, ar.exception);
            message.sendToTarget();
        }
        if (HwModemCapability.isCapabilitySupport(9)) {
            if (ar.exception == null) {
                log("Allow_data success on phoneId = " + phoneId);
                resetConnectFailureCount(phoneId);
                this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
            } else if (isAnyVoiceCallActiveOnDevice()) {
                log("Wait for call end indication");
            } else {
                incConnectFailureCount(phoneId);
                log("Allow_data failed on phoneId = " + phoneId + ", failureCount = " + getConnectFailureCount(phoneId));
                if (getConnectFailureCount(phoneId) >= MAX_CONNECT_FAILURE_COUNT) {
                    handleConnectMaxFailure(phoneId);
                } else {
                    log("Scheduling retry connect/allow_data");
                    if (hasMessages(EVENT_RETRY_ALLOW_DATA, this.mPhones[phoneId])) {
                        log("already has EVENT_RETRY_ALLOW_DATA, phoneId: " + phoneId + ", remove it and reset count");
                        removeMessages(EVENT_RETRY_ALLOW_DATA, this.mPhones[phoneId]);
                        resetConnectFailureCount(phoneId);
                    }
                    sendMessageDelayed(obtainMessage(EVENT_RETRY_ALLOW_DATA, phoneId, 0, this.mPhones[phoneId]), 30000);
                }
            }
        }
    }

    private boolean isEmergency() {
        for (Phone p : this.mPhones) {
            if (p != null && (p.isInEcm() || p.isInEmergencyCall())) {
                return REQUESTS_CHANGED;
            }
        }
        return false;
    }

    private void onRequestNetwork(NetworkRequest networkRequest) {
        DcRequest dcRequest = new DcRequest(networkRequest, this.mContext);
        if (!this.mPrioritizedDcRequests.contains(dcRequest)) {
            this.mPrioritizedDcRequests.add(dcRequest);
            Collections.sort(this.mPrioritizedDcRequests);
            onEvaluate(REQUESTS_CHANGED, "netRequest");
        }
    }

    private void onReleaseNetwork(NetworkRequest networkRequest) {
        if (this.mPrioritizedDcRequests.remove(new DcRequest(networkRequest, this.mContext))) {
            onEvaluate(REQUESTS_CHANGED, "netReleased");
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
            int i = this.mDefaultDataSubscription;
            if (dataSub != r0) {
                int i2 = this.mDefaultDataSubscription;
                sb.append(" default ").append(r0).append("->").append(dataSub);
                this.mDefaultDataSubscription = dataSub;
                diffDetected = REQUESTS_CHANGED;
            }
            int i3 = 0;
            while (true) {
                i = this.mNumPhones;
                if (i3 >= r0) {
                    break;
                }
                int sub = this.mSubscriptionController.getSubIdUsingPhoneId(i3);
                if (sub != this.mPhoneSubscriptions[i3]) {
                    sb.append(" phone[").append(i3).append("] ").append(this.mPhoneSubscriptions[i3]);
                    sb.append("->").append(sub);
                    this.mPhoneSubscriptions[i3] = sub;
                    diffDetected = REQUESTS_CHANGED;
                }
                i3++;
            }
            if (diffDetected) {
                log("evaluating due to " + sb.toString());
                if (HwModemCapability.isCapabilitySupport(9)) {
                    this.mSubscriptionController.informDdsToQcril(dataSub);
                }
                List<Integer> newActivePhones = new ArrayList();
                for (DcRequest dcRequest : this.mPrioritizedDcRequests) {
                    int phoneIdForRequest = phoneIdForRequest(dcRequest.networkRequest);
                    if (phoneIdForRequest != -1) {
                        if (newActivePhones.contains(Integer.valueOf(phoneIdForRequest))) {
                            continue;
                        } else {
                            newActivePhones.add(Integer.valueOf(phoneIdForRequest));
                            if (newActivePhones.size() >= this.mMaxActivePhones) {
                                break;
                            }
                        }
                    }
                }
                int phoneId = 0;
                while (true) {
                    i = this.mNumPhones;
                    if (phoneId >= r0) {
                        break;
                    }
                    if (!newActivePhones.contains(Integer.valueOf(phoneId))) {
                        requestPhoneCount += deactivate(phoneId, what, handler);
                    }
                    phoneId++;
                }
                for (Integer intValue : newActivePhones) {
                    requestPhoneCount += activate(intValue.intValue(), what, handler);
                }
                Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(getTopPrioritySubscriptionId()));
                if (phone != null) {
                    phone.updateCurrentCarrierInProvider();
                }
                if (SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false) && requestPhoneCount == 0) {
                    if ("defaultChanged".equals(reason)) {
                        phoneId = 0;
                        while (true) {
                            i = this.mNumPhones;
                            if (phoneId >= r0) {
                                break;
                            }
                            log("defaultChanged resendDataAllowed + phoneid =  " + phoneId);
                            resendDataAllowed(phoneId);
                            phoneId++;
                        }
                    }
                }
            }
            return requestPhoneCount;
        }
    }

    private int deactivate(int phoneId, int what, Handler handler) {
        PhoneState state = this.mPhoneStates[phoneId];
        if (!state.active) {
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
        state.active = REQUESTS_CHANGED;
        log("activate " + phoneId);
        state.lastRequested = System.currentTimeMillis();
        Object message = null;
        if (handler != null) {
            message = handler.obtainMessage(what);
        }
        this.mPhones[phoneId].mDcTracker.setDataAllowed(REQUESTS_CHANGED, obtainMessage(EVENT_ALLOW_DATA_RESPONSE, phoneId, 0, message));
        this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
        return 1;
    }

    public void resendDataAllowed(int phoneId) {
        validatePhoneId(phoneId);
        Message msg = obtainMessage(EVENT_RESEND_DATA_ALLOWED);
        msg.arg1 = phoneId;
        msg.sendToTarget();
    }

    private void onResendDataAllowed(Message msg) {
        int phoneId = msg.arg1;
        this.mCommandsInterfaces[phoneId].setDataAllowed(this.mPhoneStates[phoneId].active, obtainMessage(EVENT_ALLOW_DATA_RESPONSE, phoneId, 0));
    }

    private int phoneIdForRequest(NetworkRequest netRequest) {
        int subId;
        String specifier = netRequest.networkCapabilities.getNetworkSpecifier();
        if (TextUtils.isEmpty(specifier)) {
            subId = this.mDefaultDataSubscription;
        } else {
            subId = Integer.parseInt(specifier);
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
                int phoneId = phoneIdForRequest(request.networkRequest);
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
