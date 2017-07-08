package com.android.internal.telephony;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HwVSimPhoneSwitcher extends Handler {
    private static final int EVENT_RELEASE_NETWORK = 102;
    private static final int EVENT_REQUEST_NETWORK = 101;
    private static final String LOG_TAG = "VSimPhoneSwitcher";
    private static final int MAX_LOCAL_LOG_LINES = 30;
    private static final boolean REQUESTS_CHANGED = true;
    private static final boolean REQUESTS_UNCHANGED = false;
    private static final int SWITCHER_NETWORK_SCORE = 101;
    private final Context mContext;
    private final LocalLog mLocalLog;
    private final int mMaxActivePhones;
    private final int mNumPhones;
    private final Phone mPhone1;
    private final Phone mPhone2;
    private final List<DcRequest> mPrioritizedDcRequests;
    private final Phone mVSimPhone;

    private static class PhoneSwitcherNetworkRequestListener extends NetworkFactory {
        private final HwVSimPhoneSwitcher mPhoneSwitcher;

        public PhoneSwitcherNetworkRequestListener(Looper l, Context c, NetworkCapabilities nc, HwVSimPhoneSwitcher ps) {
            super(l, c, "VSimPhoneSwitcherNetworkListener", nc);
            this.mPhoneSwitcher = ps;
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            log("needNetworkFor " + networkRequest + ", " + score);
            Message msg = this.mPhoneSwitcher.obtainMessage(HwVSimPhoneSwitcher.SWITCHER_NETWORK_SCORE);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            log("releaseNetworkFor " + networkRequest);
            Message msg = this.mPhoneSwitcher.obtainMessage(HwVSimPhoneSwitcher.EVENT_RELEASE_NETWORK);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }
    }

    public HwVSimPhoneSwitcher(int maxActivePhones, int numPhones, Context context, Looper looper, Phone vsimPhone, CommandsInterface vsimCi, Phone phone1, Phone phone2, CommandsInterface[] cis) {
        super(looper);
        this.mPrioritizedDcRequests = new ArrayList();
        this.mContext = context;
        this.mMaxActivePhones = maxActivePhones;
        this.mNumPhones = numPhones;
        this.mLocalLog = new LocalLog(MAX_LOCAL_LOG_LINES);
        this.mVSimPhone = vsimPhone;
        this.mPhone1 = phone1;
        this.mPhone2 = phone2;
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addTransportType(0);
        netCap.addCapability(0);
        netCap.setNetworkSpecifier("*");
        NetworkFactory networkFactory = new PhoneSwitcherNetworkRequestListener(looper, context, netCap, this);
        networkFactory.setScoreFilter(SWITCHER_NETWORK_SCORE);
        networkFactory.register();
        log("VSimPhoneSwitcher started");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SWITCHER_NETWORK_SCORE /*101*/:
                onRequestNetwork((NetworkRequest) msg.obj);
            case EVENT_RELEASE_NETWORK /*102*/:
                onReleaseNetwork((NetworkRequest) msg.obj);
            default:
        }
    }

    public int getTopPrioritySubscriptionId() {
        if (this.mPrioritizedDcRequests.size() > 0) {
            DcRequest request = (DcRequest) this.mPrioritizedDcRequests.get(0);
            if (request != null) {
                int phoneId = phoneIdForRequest(request.networkRequest);
                if (phoneId >= 0 && phoneId < HwVSimModemAdapter.PHONE_COUNT) {
                    return phoneId;
                }
            }
        }
        return SubscriptionManager.getDefaultDataSubscriptionId();
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

    private void onEvaluate(boolean requestsChanged, String reason) {
        StringBuilder sb = new StringBuilder(reason);
        if (HwVSimUtils.isVSimOn()) {
            boolean diffDetected = requestsChanged;
            if (requestsChanged) {
                log("evaluating due to " + sb.toString());
                List<Integer> newActivePhones = new ArrayList();
                for (DcRequest dcRequest : this.mPrioritizedDcRequests) {
                    int phoneIdForRequest = phoneIdForRequest(dcRequest.networkRequest);
                    if (!(phoneIdForRequest == -1 || newActivePhones.contains(Integer.valueOf(phoneIdForRequest)))) {
                        newActivePhones.add(Integer.valueOf(phoneIdForRequest));
                        if (newActivePhones.size() >= this.mMaxActivePhones) {
                            break;
                        }
                    }
                }
                log(" newActivePhones:");
                for (Integer i : newActivePhones) {
                    log("  " + i);
                }
                for (int phoneId = 0; phoneId < this.mNumPhones; phoneId++) {
                    if (!newActivePhones.contains(Integer.valueOf(phoneId))) {
                        deactivate(phoneId);
                    }
                }
                if (newActivePhones.size() > 0) {
                    deactivateVSim();
                }
                for (Integer intValue : newActivePhones) {
                    activate(intValue.intValue());
                }
                if (newActivePhones.size() == 0) {
                    activateVSim();
                }
            }
            return;
        }
        log("onEvalute aborted due to vsim is off");
    }

    private void deactivate(int phoneId) {
        log("deactivate " + phoneId);
        if (phoneId == 0) {
            this.mPhone1.mDcTracker.setDataAllowed(REQUESTS_UNCHANGED, null);
        } else {
            this.mPhone2.mDcTracker.setDataAllowed(REQUESTS_UNCHANGED, null);
        }
    }

    private void deactivateVSim() {
        log("deactivate vsim");
        this.mVSimPhone.mDcTracker.setDataAllowed(REQUESTS_UNCHANGED, null);
    }

    private void activate(int phoneId) {
        log("activate " + phoneId);
        if (phoneId == 0) {
            this.mPhone1.mDcTracker.setDataAllowed(REQUESTS_CHANGED, null);
            this.mPhone1.updateCurrentCarrierInProvider();
            return;
        }
        this.mPhone2.mDcTracker.setDataAllowed(REQUESTS_CHANGED, null);
        this.mPhone2.updateCurrentCarrierInProvider();
    }

    private void activateVSim() {
        log("activate vsim");
        this.mVSimPhone.mDcTracker.setDataAllowed(REQUESTS_CHANGED, null);
    }

    private int phoneIdForRequest(NetworkRequest netRequest) {
        int subId;
        String specifier = netRequest.networkCapabilities.getNetworkSpecifier();
        if (TextUtils.isEmpty(specifier)) {
            subId = -1;
        } else {
            subId = Integer.parseInt(specifier);
        }
        if (subId == -1) {
            return -1;
        }
        int phoneId = subId;
        return subId;
    }

    private void log(String l) {
        Rlog.d(LOG_TAG, l);
        this.mLocalLog.log(l);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println("VSimPhoneSwitcher:");
        pw.increaseIndent();
        this.mLocalLog.dump(fd, pw, args);
        pw.decreaseIndent();
    }
}
