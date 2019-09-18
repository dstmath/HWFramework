package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.net.StringNetworkSpecifier;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.telephony.HwVSimPhoneSwitcher;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class HwVSimNetworkFactory extends NetworkFactory {
    private static final int EVENT_NETWORK_RELEASE = 2;
    private static final int EVENT_NETWORK_REQUEST = 1;
    private static final int REQUEST_LOG_SIZE = 40;
    private static final int TELEPHONY_NETWORK_SCORE = 50;
    public final String LOG_TAG;
    private final DcTracker mDcTracker;
    private final HashMap<NetworkRequest, LocalLog> mDefaultRequests = new HashMap<>();
    private final Handler mInternalHandler;

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwVSimNetworkFactory.this.onNeedNetworkFor(msg);
                    return;
                case 2:
                    HwVSimNetworkFactory.this.onReleaseNetworkFor(msg);
                    return;
                default:
                    return;
            }
        }
    }

    public HwVSimNetworkFactory(HwVSimPhoneSwitcher phoneSwitcher, Looper looper, Context context, int phoneId, DcTracker dcTracker) {
        super(looper, context, "VSimNetworkFactory[" + phoneId + "]", null);
        this.mInternalHandler = new InternalHandler(looper);
        setCapabilityFilter(makeNetworkFilter(phoneId));
        setScoreFilter(50);
        this.LOG_TAG = "VSimNetworkFactory[" + phoneId + "]";
        this.mDcTracker = dcTracker;
        register();
    }

    private NetworkCapabilities makeNetworkFilter(int subscriptionId) {
        NetworkCapabilities nc = new NetworkCapabilities();
        nc.addTransportType(0);
        nc.addCapability(1);
        nc.addCapability(2);
        nc.addCapability(12);
        nc.setNetworkSpecifier(new StringNetworkSpecifier(String.valueOf(subscriptionId)));
        return nc;
    }

    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        log("needNetworkFor - " + networkRequest);
        Message msg = this.mInternalHandler.obtainMessage(1);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    public boolean acceptRequest(NetworkRequest request, int score) {
        boolean defaultMobileEnable = SystemProperties.getBoolean("sys.defaultapn.enabled", true);
        StringBuilder sb = new StringBuilder();
        sb.append("defaultMobileEnable is ");
        sb.append(defaultMobileEnable ? "true" : "false");
        log(sb.toString());
        return defaultMobileEnable;
    }

    /* access modifiers changed from: private */
    public void onNeedNetworkFor(Message msg) {
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        LocalLog localLog = null;
        StringNetworkSpecifier specifier = networkRequest.networkCapabilities.getNetworkSpecifier();
        if (specifier == null || TextUtils.isEmpty(specifier.toString())) {
            localLog = this.mDefaultRequests.get(networkRequest);
            if (localLog == null) {
                localLog = new LocalLog(40);
                localLog.log("created for " + networkRequest);
                this.mDefaultRequests.put(networkRequest, localLog);
            }
        }
        if (localLog != null) {
            localLog.log("onNeedNetworkFor");
            log("onNeedNetworkFor" + " " + networkRequest);
            this.mDcTracker.requestNetwork(networkRequest, localLog);
        }
    }

    public void releaseNetworkFor(NetworkRequest networkRequest) {
        log("releaseNetworkFor - " + networkRequest);
        Message msg = this.mInternalHandler.obtainMessage(2);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    public void onReleaseNetworkFor(Message msg) {
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        LocalLog localLog = null;
        boolean isApplicable = false;
        StringNetworkSpecifier specifier = networkRequest.networkCapabilities.getNetworkSpecifier();
        if (specifier == null || TextUtils.isEmpty(specifier.toString())) {
            localLog = this.mDefaultRequests.remove(networkRequest);
            isApplicable = localLog != null;
        }
        if (localLog != null) {
            if (isApplicable) {
                localLog.log("onReleaseNetworkFor");
                log("onReleaseNetworkFor" + " " + networkRequest);
                this.mDcTracker.releaseNetwork(networkRequest, localLog);
            } else {
                localLog.log("not releasing");
                log("not releasing" + " " + networkRequest);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        new IndentingPrintWriter(writer, "  ").println("HwVSimNetworkFactory:");
    }
}
