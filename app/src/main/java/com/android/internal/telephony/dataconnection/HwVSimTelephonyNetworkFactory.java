package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.telephony.HwVSimPhoneSwitcher;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class HwVSimTelephonyNetworkFactory extends NetworkFactory {
    private static final int EVENT_NETWORK_RELEASE = 2;
    private static final int EVENT_NETWORK_REQUEST = 1;
    private static final int REQUEST_LOG_SIZE = 40;
    private static final int TELEPHONY_NETWORK_SCORE = 50;
    public final String LOG_TAG;
    private final DcTracker mDcTracker;
    private final Handler mInternalHandler;
    private int mPhoneId;
    private final HashMap<NetworkRequest, LocalLog> mSpecificRequests;

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwVSimTelephonyNetworkFactory.EVENT_NETWORK_REQUEST /*1*/:
                    HwVSimTelephonyNetworkFactory.this.onNeedNetworkFor(msg);
                case HwVSimTelephonyNetworkFactory.EVENT_NETWORK_RELEASE /*2*/:
                    HwVSimTelephonyNetworkFactory.this.onReleaseNetworkFor(msg);
                default:
            }
        }
    }

    public HwVSimTelephonyNetworkFactory(HwVSimPhoneSwitcher phoneSwitcher, Looper looper, Context context, int phoneId, DcTracker dcTracker) {
        super(looper, context, "VSimTelphonyNetFactory[" + phoneId + "]", null);
        this.mSpecificRequests = new HashMap();
        this.mInternalHandler = new InternalHandler(looper);
        setCapabilityFilter(makeNetworkFilter(phoneId));
        setScoreFilter(TELEPHONY_NETWORK_SCORE);
        this.mPhoneId = phoneId;
        this.LOG_TAG = "VSimTelephonyNetFactory[" + phoneId + "]";
        this.mDcTracker = dcTracker;
        register();
    }

    private NetworkCapabilities makeNetworkFilter(int subscriptionId) {
        NetworkCapabilities nc = new NetworkCapabilities();
        nc.addTransportType(0);
        nc.addCapability(0);
        nc.setNetworkSpecifier(String.valueOf(subscriptionId));
        return nc;
    }

    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        log("needNetworkFor - " + networkRequest);
        Message msg = this.mInternalHandler.obtainMessage(EVENT_NETWORK_REQUEST);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    private void onNeedNetworkFor(Message msg) {
        NetworkRequest networkRequest = msg.obj;
        boolean isApplicable = false;
        LocalLog localLog = null;
        if (!TextUtils.isEmpty(networkRequest.networkCapabilities.getNetworkSpecifier())) {
            localLog = (LocalLog) this.mSpecificRequests.get(networkRequest);
            if (localLog == null) {
                localLog = new LocalLog(REQUEST_LOG_SIZE);
                this.mSpecificRequests.put(networkRequest, localLog);
                isApplicable = true;
            }
        }
        if (localLog != null) {
            String s;
            if (isApplicable) {
                s = "onNeedNetworkFor";
                localLog.log(s);
                log(s + " " + networkRequest);
                this.mDcTracker.requestNetwork(networkRequest, localLog);
            } else {
                s = "not acting";
                localLog.log(s);
                log(s + " " + networkRequest);
            }
        }
    }

    public void releaseNetworkFor(NetworkRequest networkRequest) {
        log("releaseNetworkFor - " + networkRequest);
        Message msg = this.mInternalHandler.obtainMessage(EVENT_NETWORK_RELEASE);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    private void onReleaseNetworkFor(Message msg) {
        NetworkRequest networkRequest = msg.obj;
        LocalLog localLog = null;
        boolean isApplicable = false;
        if (!TextUtils.isEmpty(networkRequest.networkCapabilities.getNetworkSpecifier())) {
            localLog = (LocalLog) this.mSpecificRequests.remove(networkRequest);
            isApplicable = localLog != null;
        }
        if (localLog != null) {
            String s;
            if (isApplicable) {
                s = "onReleaseNetworkFor";
                localLog.log(s);
                log(s + " " + networkRequest);
                this.mDcTracker.releaseNetwork(networkRequest, localLog);
            } else {
                s = "not releasing";
                localLog.log(s);
                log(s + " " + networkRequest);
            }
        }
    }

    protected void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println(this.LOG_TAG + " mSubId=" + this.mPhoneId);
        pw.println("Specific Requests:");
    }
}
