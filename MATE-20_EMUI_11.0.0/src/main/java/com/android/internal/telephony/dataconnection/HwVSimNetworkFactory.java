package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.HwVSimPhoneSwitcher;
import com.huawei.android.net.NetworkCapabilitiesEx;
import com.huawei.android.net.NetworkFactoryEx;
import com.huawei.android.net.NetworkRequestEx;
import com.huawei.android.net.StringNetworkSpecifierEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.util.LocalLogEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.util.IndentingPrintWriterEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class HwVSimNetworkFactory extends NetworkFactoryEx {
    private static final int EVENT_NETWORK_RELEASE = 2;
    private static final int EVENT_NETWORK_REQUEST = 1;
    private static final int REQUEST_LOG_SIZE = 40;
    private static final int TELEPHONY_NETWORK_SCORE = 50;
    public final String LOG_TAG;
    private final DcTrackerEx mDcTracker;
    private final HashMap<NetworkRequest, LocalLogEx> mDefaultRequests = new HashMap<>();
    private final Handler mInternalHandler;

    public HwVSimNetworkFactory(HwVSimPhoneSwitcher phoneSwitcher, Looper looper, Context context, int phoneId, DcTrackerEx dcTracker) {
        super(looper, context, "VSimNetworkFactory[" + phoneId + "]", (NetworkCapabilitiesEx) null);
        this.mInternalHandler = new InternalHandler(looper);
        setCapabilityFilter(makeNetworkFilter(phoneId));
        setScoreFilter(50);
        this.LOG_TAG = "VSimNetworkFactory[" + phoneId + "]";
        this.mDcTracker = dcTracker;
        register();
    }

    private NetworkCapabilitiesEx makeNetworkFilter(int subscriptionId) {
        NetworkCapabilitiesEx nc = new NetworkCapabilitiesEx();
        nc.addTransportType(0);
        nc.addCapability(1);
        nc.addCapability(2);
        nc.addCapability(12);
        nc.setNetworkSpecifier(new StringNetworkSpecifierEx(String.valueOf(subscriptionId)));
        return nc;
    }

    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        log("needNetworkFor - " + networkRequest);
        Message msg = this.mInternalHandler.obtainMessage(1);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    public boolean acceptRequest(NetworkRequest request, int score) {
        boolean defaultMobileEnable = SystemPropertiesEx.getBoolean("sys.defaultapn.enabled", true);
        StringBuilder sb = new StringBuilder();
        sb.append("defaultMobileEnable is ");
        sb.append(defaultMobileEnable ? "true" : "false");
        log(sb.toString());
        return defaultMobileEnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNeedNetworkFor(Message msg) {
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        LocalLogEx localLog = null;
        if (new StringNetworkSpecifierEx(new NetworkCapabilitiesEx(NetworkRequestEx.getNetworkCapabilities(networkRequest)).getNetworkSpecifier()).isSpecifierEmpty() && (localLog = this.mDefaultRequests.get(networkRequest)) == null) {
            localLog = new LocalLogEx(40);
            localLog.log("created for " + networkRequest);
            this.mDefaultRequests.put(networkRequest, localLog);
        }
        if (localLog != null) {
            localLog.log("onNeedNetworkFor");
            log("onNeedNetworkFor " + networkRequest);
            this.mDcTracker.requestNetwork(networkRequest, DcTrackerEx.REQUEST_TYPE_NORMAL, (Message) null, localLog);
        }
    }

    public void releaseNetworkFor(NetworkRequest networkRequest) {
        log("releaseNetworkFor - " + networkRequest);
        Message msg = this.mInternalHandler.obtainMessage(2);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onReleaseNetworkFor(Message msg) {
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        LocalLogEx localLog = null;
        boolean isApplicable = false;
        if (new StringNetworkSpecifierEx(new NetworkCapabilitiesEx(NetworkRequestEx.getNetworkCapabilities(networkRequest)).getNetworkSpecifier()).isSpecifierEmpty()) {
            localLog = this.mDefaultRequests.remove(networkRequest);
            isApplicable = localLog != null;
        }
        if (localLog != null) {
            if (isApplicable) {
                localLog.log("onReleaseNetworkFor");
                log("onReleaseNetworkFor " + networkRequest);
                this.mDcTracker.releaseNetwork(networkRequest, DcTrackerEx.RELEASE_TYPE_NORMAL, localLog);
                return;
            }
            localLog.log("not releasing");
            log("not releasing " + networkRequest);
        }
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        RlogEx.d(this.LOG_TAG, s);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        new IndentingPrintWriterEx(writer, "  ").println("HwVSimNetworkFactory:");
    }

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
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
}
