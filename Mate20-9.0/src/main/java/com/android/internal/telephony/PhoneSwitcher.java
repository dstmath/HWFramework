package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.MatchAllNetworkSpecifier;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.net.StringNetworkSpecifier;
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
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class PhoneSwitcher extends Handler implements IHwPhoneSwitcherInner {
    private static final int EVENT_DEFAULT_SUBSCRIPTION_CHANGED = 101;
    private static final int EVENT_EMERGENCY_TOGGLE = 105;
    private static final int EVENT_RELEASE_NETWORK = 104;
    private static final int EVENT_REQUEST_NETWORK = 103;
    private static final int EVENT_RESEND_DATA_ALLOWED = 106;
    private static final int EVENT_SUBSCRIPTION_CHANGED = 102;
    private static final String LOG_TAG = "PhoneSwitcher";
    private static final int MAX_LOCAL_LOG_LINES = 30;
    private static final boolean REQUESTS_CHANGED = true;
    private static final boolean REQUESTS_UNCHANGED = false;
    private static final boolean VDBG = false;
    private final RegistrantList[] mActivePhoneRegistrants;
    private final CommandsInterface[] mCommandsInterfaces;
    private final Context mContext;
    private final BroadcastReceiver mDefaultDataChangedReceiver;
    private int mDefaultDataSubscription;
    IHwPhoneSwitcherEx mHwPhoneSwitcherEx;
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

    @VisibleForTesting
    public PhoneSwitcher(Looper looper) {
        super(looper);
        this.mPrioritizedDcRequests = new ArrayList();
        this.mManualDdsSwitch = false;
        this.mDefaultDataChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                PhoneSwitcher.this.obtainMessage(101).sendToTarget();
            }
        };
        this.mSubscriptionsChangedListener = new IOnSubscriptionsChangedListener.Stub() {
            public void onSubscriptionsChanged() {
                PhoneSwitcher.this.obtainMessage(102).sendToTarget();
            }
        };
        this.mHwPhoneSwitcherEx = null;
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
        this.mDefaultDataChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                PhoneSwitcher.this.obtainMessage(101).sendToTarget();
            }
        };
        this.mSubscriptionsChangedListener = new IOnSubscriptionsChangedListener.Stub() {
            public void onSubscriptionsChanged() {
                PhoneSwitcher.this.obtainMessage(102).sendToTarget();
            }
        };
        this.mHwPhoneSwitcherEx = null;
        this.mHwPhoneSwitcherEx = HwTelephonyFactory.getHwPhoneSwitcherEx(this, numPhones);
        this.mContext = context;
        this.mNumPhones = numPhones;
        this.mPhones = phones;
        this.mPhoneSubscriptions = new int[numPhones];
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
            tr.addOnSubscriptionsChangedListener(context.getOpPackageName(), this.mSubscriptionsChangedListener);
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
            default:
                log("handleMessage, what=" + msg.what);
                this.mHwPhoneSwitcherEx.handleMessage(this, this.mPhones, msg);
                return;
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
        if (!this.mPrioritizedDcRequests.contains(dcRequest) && dcRequest.apnId != -1) {
            this.mPrioritizedDcRequests.add(dcRequest);
            Collections.sort(this.mPrioritizedDcRequests);
            onEvaluate(true, "netRequest");
        }
    }

    private void onReleaseNetwork(NetworkRequest networkRequest) {
        if (this.mPrioritizedDcRequests.remove(new DcRequest(networkRequest, this.mContext))) {
            if (networkRequest.networkCapabilities.hasCapability(30)) {
                log("releaseNetworkFor internal_default, just return.");
                return;
            }
            onEvaluate(true, "netReleased");
        }
    }

    private int onEvaluate(boolean requestsChanged, String reason, int what, Handler handler) {
        int requestPhoneCount = 0;
        StringBuilder sb = new StringBuilder(reason);
        if (isEmergency()) {
            log("onEvalute aborted due to Emergency");
            return 0;
        } else if (VSimUtilsInner.isVSimEnabled() || VSimUtilsInner.isVSimCauseCardReload()) {
            log("onEvalute aborted due to vsim is on");
            return 0;
        } else {
            boolean diffDetected = requestsChanged;
            int dataSub = this.mSubscriptionController.getDefaultDataSubId();
            if (dataSub != this.mDefaultDataSubscription) {
                sb.append(" default ");
                sb.append(this.mDefaultDataSubscription);
                sb.append("->");
                sb.append(dataSub);
                this.mDefaultDataSubscription = dataSub;
                diffDetected = true;
                this.mManualDdsSwitch = true;
            }
            boolean diffDetected2 = diffDetected;
            for (int i = 0; i < this.mNumPhones; i++) {
                int sub = this.mSubscriptionController.getSubIdUsingPhoneId(i);
                if (sub != this.mPhoneSubscriptions[i]) {
                    sb.append(" phone[");
                    sb.append(i);
                    sb.append("] ");
                    sb.append(this.mPhoneSubscriptions[i]);
                    sb.append("->");
                    sb.append(sub);
                    this.mPhoneSubscriptions[i] = sub;
                    diffDetected2 = true;
                }
            }
            if (diffDetected2) {
                log("evaluating due to " + sb.toString());
                this.mHwPhoneSwitcherEx.informDdsToQcril(dataSub);
                List<Integer> newActivePhones = new ArrayList<>();
                for (DcRequest dcRequest : this.mPrioritizedDcRequests) {
                    int phoneIdForRequest = phoneIdForRequest(dcRequest.networkRequest, dcRequest.apnId);
                    if (phoneIdForRequest != -1 && !newActivePhones.contains(Integer.valueOf(phoneIdForRequest))) {
                        newActivePhones.add(Integer.valueOf(phoneIdForRequest));
                        if (newActivePhones.size() >= this.mMaxActivePhones) {
                            break;
                        }
                    }
                }
                int requestPhoneCount2 = 0;
                for (int phoneId = 0; phoneId < this.mNumPhones; phoneId++) {
                    if (!newActivePhones.contains(Integer.valueOf(phoneId))) {
                        requestPhoneCount2 += deactivate(phoneId, what, handler);
                    }
                }
                for (Integer intValue : newActivePhones) {
                    requestPhoneCount2 += activate(intValue.intValue(), what, handler);
                }
                Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(getTopPrioritySubscriptionId()));
                if (phone != null) {
                    phone.updateCurrentCarrierInProvider();
                }
                if ((PhoneFactory.IS_QCOM_DUAL_LTE_STACK || (SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false) && requestPhoneCount2 == 0 && "defaultChanged".equals(reason))) && !HuaweiTelephonyConfigs.isMTKPlatform()) {
                    for (int phoneId2 = 0; phoneId2 < this.mNumPhones; phoneId2++) {
                        log("defaultChanged resendDataAllowed + phoneid =  " + phoneId2);
                        resendDataAllowed(phoneId2);
                    }
                }
                requestPhoneCount = requestPhoneCount2;
            }
            this.mManualDdsSwitch = false;
            return requestPhoneCount;
        }
    }

    private int deactivate(int phoneId, int what, Handler handler) {
        PhoneState state = this.mPhoneStates[phoneId];
        if (!state.active || HwTelephonyFactory.getHwDataConnectionManager().isSwitchingToSlave() || HwTelephonyFactory.getHwDataConnectionManager().isSlaveActive() || HwTelephonyFactory.getHwDataConnectionManager().isSwitchingSmartCard()) {
            return 0;
        }
        state.active = false;
        log("deactivate " + phoneId);
        state.lastRequested = System.currentTimeMillis();
        if (this.mNumPhones > 1) {
            Message message = null;
            if (handler != null) {
                message = handler.obtainMessage(what);
            }
            this.mPhones[phoneId].mDcTracker.setDataAllowed(false, obtainMessage(IHwPhoneSwitcherEx.EVENT_ALLOW_DATA_RESPONSE, phoneId, 0, message));
        }
        this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
        return 1;
    }

    private int activate(int phoneId, int what, Handler handler) {
        PhoneState state = this.mPhoneStates[phoneId];
        if (state.active && (!this.mManualDdsSwitch || !PhoneFactory.IS_DUAL_VOLTE_SUPPORTED || !PhoneFactory.IS_QCOM_DUAL_LTE_STACK)) {
            return 0;
        }
        state.active = true;
        log("activate " + phoneId);
        state.lastRequested = System.currentTimeMillis();
        if (this.mNumPhones > 1) {
            Message message = null;
            if (handler != null) {
                message = handler.obtainMessage(what);
            }
            this.mPhones[phoneId].mDcTracker.setDataAllowed(true, obtainMessage(IHwPhoneSwitcherEx.EVENT_ALLOW_DATA_RESPONSE, phoneId, 0, message));
        }
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
        if (this.mNumPhones > 1) {
            this.mPhones[phoneId].mDcTracker.setDataAllowed(this.mPhoneStates[phoneId].active, obtainMessage(IHwPhoneSwitcherEx.EVENT_ALLOW_DATA_RESPONSE, phoneId, 0));
        }
    }

    private int phoneIdForRequest(NetworkRequest netRequest, int apnid) {
        int subId;
        StringNetworkSpecifier networkSpecifier = netRequest.networkCapabilities.getNetworkSpecifier();
        if (networkSpecifier == null) {
            subId = this.mDefaultDataSubscription;
        } else if ((networkSpecifier instanceof StringNetworkSpecifier) == 0) {
            subId = -1;
        } else if (5 != apnid || !this.mManualDdsSwitch || this.mMaxActivePhones == this.mNumPhones) {
            try {
                subId = Integer.parseInt(networkSpecifier.specifier);
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, "NumberFormatException on " + networkSpecifier.specifier);
                subId = -1;
            }
        } else {
            log("specifier is not empty but default data switched use default data sub");
            subId = this.mDefaultDataSubscription;
        }
        int phoneId = -1;
        if (subId == -1) {
            return -1;
        }
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
        log("phoneIdForRequest for request:" + netRequest + ",return " + phoneId);
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
                str = String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c});
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

    public int onDataSubChange(int what, Handler handler) {
        return onEvaluate(false, "defaultChanged", what, handler);
    }

    private void onEvaluate(boolean requestsChanged, String reason) {
        onEvaluate(requestsChanged, reason, 0, null);
    }

    public RegistrantList getActivePhoneRegistrants(int phoneId) {
        return this.mActivePhoneRegistrants[phoneId];
    }

    public int phoneIdForRequestForEx(NetworkRequest netRequest, int apnid) {
        return phoneIdForRequest(netRequest, apnid);
    }

    public void resendDataAllowedForEx(int phoneId) {
        resendDataAllowed(phoneId);
    }
}
