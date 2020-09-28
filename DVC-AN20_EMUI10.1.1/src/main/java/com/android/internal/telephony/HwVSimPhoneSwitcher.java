package com.android.internal.telephony;

import android.content.Context;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimUtilsImpl;
import com.huawei.android.net.MatchAllNetworkSpecifierEx;
import com.huawei.android.net.NetworkCapabilitiesEx;
import com.huawei.android.net.NetworkFactoryEx;
import com.huawei.android.net.NetworkRequestEx;
import com.huawei.android.net.StringNetworkSpecifierEx;
import com.huawei.android.os.RegistrantEx;
import com.huawei.android.os.RegistrantListEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.util.LocalLogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.DcRequestEx;
import com.huawei.internal.util.IndentingPrintWriterEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class HwVSimPhoneSwitcher extends Handler {
    private static final int EVENT_RELEASE_NETWORK = 102;
    private static final int EVENT_REQUEST_NETWORK = 101;
    private static final String LOG_TAG = "VSimPhoneSwitcher";
    private static final int MAX_LOCAL_LOG_LINES = 30;
    private static final boolean REQUESTS_CHANGED = true;
    private static final boolean REQUESTS_UNCHANGED = false;
    private static final int SWITCHER_NETWORK_SCORE = 101;
    private final RegistrantListEx[] mActivePhoneRegistrants;
    private final Context mContext;
    private final LocalLogEx mLocalLog;
    private final int mMaxActivePhones;
    private final int mNumPhones;
    private final PhoneExt mPhone1;
    private final PhoneExt mPhone2;
    private final PhoneState[] mPhoneStates;
    private final List<DcRequestEx> mPrioritizedDcRequests = new ArrayList();
    private final PhoneExt mVSimPhone;

    public HwVSimPhoneSwitcher(int maxActivePhones, int numPhones, Context context, Looper looper, PhoneExt vsimPhone, CommandsInterfaceEx vsimCi, PhoneExt phone1, PhoneExt phone2, CommandsInterfaceEx[] cis) {
        super(looper);
        this.mContext = context;
        this.mMaxActivePhones = maxActivePhones;
        this.mNumPhones = numPhones;
        this.mLocalLog = new LocalLogEx(30);
        this.mVSimPhone = vsimPhone;
        this.mPhone1 = phone1;
        this.mPhone2 = phone2;
        this.mActivePhoneRegistrants = new RegistrantListEx[(numPhones + 1)];
        this.mPhoneStates = new PhoneState[(numPhones + 1)];
        for (int i = 0; i < numPhones + 1; i++) {
            this.mActivePhoneRegistrants[i] = new RegistrantListEx();
            this.mPhoneStates[i] = new PhoneState();
        }
        NetworkCapabilitiesEx netCap = new NetworkCapabilitiesEx();
        netCap.addTransportType(0);
        netCap.addCapability(0);
        netCap.setNetworkSpecifier(new MatchAllNetworkSpecifierEx());
        NetworkFactoryEx networkFactory = new PhoneSwitcherNetworkRequestListener(looper, context, netCap, this);
        networkFactory.setScoreFilter(101);
        networkFactory.register();
        NetworkCapabilitiesEx netCapForVowifi = new NetworkCapabilitiesEx();
        netCapForVowifi.addTransportType(1);
        netCapForVowifi.addCapability(0);
        netCapForVowifi.setNetworkSpecifier(new MatchAllNetworkSpecifierEx());
        NetworkFactoryEx networkFactoryForVowifi = new PhoneSwitcherNetworkRequestListener(looper, context, netCapForVowifi, this);
        networkFactoryForVowifi.setScoreFilter(101);
        networkFactoryForVowifi.register();
        log("VSimPhoneSwitcher started");
    }

    public void handleMessage(Message msg) {
        log("handleMessage, what = " + msg.what);
        switch (msg.what) {
            case 101:
                onRequestNetwork((NetworkRequest) msg.obj);
                return;
            case EVENT_RELEASE_NETWORK /*{ENCODED_INT: 102}*/:
                onReleaseNetwork((NetworkRequest) msg.obj);
                return;
            default:
                return;
        }
    }

    private static class PhoneSwitcherNetworkRequestListener extends NetworkFactoryEx {
        private static final String TAG = "VSimPhoneSwitcherNetworkListener";
        private final HwVSimPhoneSwitcher mPhoneSwitcher;

        public PhoneSwitcherNetworkRequestListener(Looper l, Context c, NetworkCapabilitiesEx nc, HwVSimPhoneSwitcher ps) {
            super(l, c, TAG, nc);
            this.mPhoneSwitcher = ps;
        }

        /* access modifiers changed from: protected */
        public void needNetworkFor(NetworkRequest networkRequest, int score) {
            log("needNetworkFor " + networkRequest + ", " + score);
            Message msg = this.mPhoneSwitcher.obtainMessage(101);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }

        /* access modifiers changed from: protected */
        public void releaseNetworkFor(NetworkRequest networkRequest) {
            log("releaseNetworkFor " + networkRequest);
            Message msg = this.mPhoneSwitcher.obtainMessage(HwVSimPhoneSwitcher.EVENT_RELEASE_NETWORK);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }

        /* access modifiers changed from: protected */
        public void log(String s) {
            RlogEx.d(TAG, s);
        }
    }

    public boolean isPhoneActive(int phoneId) {
        validatePhoneId(phoneId);
        return this.mPhoneStates[phoneId].active;
    }

    public void registerForActivePhoneSwitch(int phoneId, Handler h, int what, Object o) {
        validatePhoneId(phoneId);
        RegistrantEx r = new RegistrantEx(h, what, o);
        this.mActivePhoneRegistrants[phoneId].add(r);
        r.notifyRegistrant();
    }

    public void unregisterForActivePhoneSwitch(int phoneId, Handler h) {
        validatePhoneId(phoneId);
        this.mActivePhoneRegistrants[phoneId].remove(h);
    }

    private void validatePhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mNumPhones + 1) {
            throw new IllegalArgumentException("Invalid PhoneId");
        }
    }

    public int getTopPrioritySubscriptionId() {
        DcRequestEx request;
        int phoneId;
        if (this.mPrioritizedDcRequests.size() <= 0 || (request = this.mPrioritizedDcRequests.get(0)) == null || (phoneId = phoneIdForRequest(request.getNetworkRequest())) < 0 || phoneId >= HwVSimModemAdapter.PHONE_COUNT) {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }
        return SubscriptionManagerEx.getSubIdUsingSlotId(phoneId);
    }

    private void onRequestNetwork(NetworkRequest networkRequest) {
        DcRequestEx dcRequest = new DcRequestEx(networkRequest, this.mContext);
        if (!this.mPrioritizedDcRequests.contains(dcRequest) && dcRequest.getApnType() != 0) {
            this.mPrioritizedDcRequests.add(dcRequest);
            Collections.sort(this.mPrioritizedDcRequests);
            onEvaluate(REQUESTS_CHANGED, "netRequest");
        }
    }

    private void onReleaseNetwork(NetworkRequest networkRequest) {
        if (this.mPrioritizedDcRequests.remove(new DcRequestEx(networkRequest, this.mContext))) {
            onEvaluate(REQUESTS_CHANGED, "netReleased");
        }
    }

    private void onEvaluate(boolean requestsChanged, String reason) {
        StringBuilder sb = new StringBuilder(reason);
        if (!HwVSimUtilsImpl.getInstance().isVSimOn()) {
            log("onEvalute aborted due to vsim is off");
        } else if (requestsChanged) {
            log("evaluating due to " + sb.toString());
            List<Integer> newActivePhones = new ArrayList<>();
            for (DcRequestEx dcRequest : this.mPrioritizedDcRequests) {
                int phoneIdForRequest = phoneIdForRequest(dcRequest.getNetworkRequest());
                if (phoneIdForRequest != -1 && !newActivePhones.contains(Integer.valueOf(phoneIdForRequest))) {
                    newActivePhones.add(Integer.valueOf(phoneIdForRequest));
                    if (newActivePhones.size() >= this.mMaxActivePhones) {
                        break;
                    }
                }
            }
            log(" newActivePhones:");
            Iterator<Integer> it = newActivePhones.iterator();
            while (it.hasNext()) {
                log("  " + it.next());
            }
            for (int phoneId = 0; phoneId < this.mNumPhones; phoneId++) {
                if (!newActivePhones.contains(Integer.valueOf(phoneId))) {
                    deactivate(phoneId);
                }
            }
            if (newActivePhones.size() > 0) {
                deactivateVSim();
            }
            for (Integer num : newActivePhones) {
                activate(num.intValue());
            }
            if (newActivePhones.size() == 0) {
                activateVSim();
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PhoneState {
        public volatile boolean active;

        private PhoneState() {
            this.active = false;
        }
    }

    private void deactivate(int phoneId) {
        this.mPhoneStates[phoneId].active = false;
        log("deactivate " + phoneId);
        if (phoneId == 0) {
            this.mPhone1.getDcTracker().setDataAllowed(false, (Message) null);
        } else {
            this.mPhone2.getDcTracker().setDataAllowed(false, (Message) null);
        }
        this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
    }

    private void deactivateVSim() {
        this.mPhoneStates[this.mVSimPhone.getPhoneId()].active = false;
        log("deactivate vsim");
        this.mVSimPhone.getDcTracker().setDataAllowed(false, (Message) null);
        this.mActivePhoneRegistrants[this.mVSimPhone.getPhoneId()].notifyRegistrants();
    }

    private void activate(int phoneId) {
        this.mPhoneStates[phoneId].active = REQUESTS_CHANGED;
        log("activate " + phoneId);
        if (phoneId == 0) {
            this.mPhone1.getDcTracker().setDataAllowed((boolean) REQUESTS_CHANGED, (Message) null);
            this.mPhone1.updateCurrentCarrierInProvider();
        } else {
            this.mPhone2.getDcTracker().setDataAllowed((boolean) REQUESTS_CHANGED, (Message) null);
            this.mPhone2.updateCurrentCarrierInProvider();
        }
        this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
    }

    private void activateVSim() {
        this.mPhoneStates[this.mVSimPhone.getPhoneId()].active = REQUESTS_CHANGED;
        log("activate vsim");
        this.mVSimPhone.getDcTracker().setDataAllowed((boolean) REQUESTS_CHANGED, (Message) null);
        this.mActivePhoneRegistrants[this.mVSimPhone.getPhoneId()].notifyRegistrants();
    }

    private int phoneIdForRequest(NetworkRequest netRequest) {
        int subId;
        StringNetworkSpecifierEx specifier = new StringNetworkSpecifierEx(new NetworkCapabilitiesEx(NetworkRequestEx.getNetworkCapabilities(netRequest)).getNetworkSpecifier());
        if (specifier.isSpecifierEmpty()) {
            subId = -1;
        } else {
            subId = Integer.parseInt(specifier.toString());
        }
        if (subId == -1) {
            return -1;
        }
        int phoneId = SubscriptionManagerEx.getPhoneId(subId);
        log("phoneIdForRequest, phoneId = " + phoneId);
        return phoneId;
    }

    private void log(String l) {
        RlogEx.d(LOG_TAG, l);
        this.mLocalLog.log(l);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriterEx pw = new IndentingPrintWriterEx(writer, "  ");
        pw.println("VSimPhoneSwitcher:");
        pw.increaseIndent();
        this.mLocalLog.dump(fd, pw, args);
        pw.decreaseIndent();
    }
}
