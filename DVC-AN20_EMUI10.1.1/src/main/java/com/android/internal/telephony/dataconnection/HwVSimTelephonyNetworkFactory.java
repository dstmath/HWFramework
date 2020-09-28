package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.HwVSimPhoneSwitcher;
import com.huawei.android.net.NetworkCapabilitiesEx;
import com.huawei.android.net.NetworkFactoryEx;
import com.huawei.android.net.NetworkRequestEx;
import com.huawei.android.net.StringNetworkSpecifierEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.util.LocalLogEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.util.IndentingPrintWriterEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class HwVSimTelephonyNetworkFactory extends NetworkFactoryEx {
    private static final int EVENT_ACTIVE_PHONE_SWITCH = 3;
    private static final int EVENT_NETWORK_RELEASE = 2;
    private static final int EVENT_NETWORK_REQUEST = 1;
    private static final boolean RELEASE = false;
    private static final boolean REQUEST = true;
    private static final int REQUEST_LOG_SIZE = 40;
    private static final int TELEPHONY_NETWORK_SCORE = 50;
    public final String LOG_TAG;
    private final DcTrackerEx mDcTracker;
    private final Handler mInternalHandler;
    private boolean mIsActive;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.dataconnection.HwVSimTelephonyNetworkFactory.AnonymousClass1 */

        public void onSubscriptionsChanged() {
            HwVSimTelephonyNetworkFactory.this.log("onSubscriptionsChanged");
            HwVSimTelephonyNetworkFactory.this.onSubIdChange();
        }
    };
    private int mPhoneId;
    private final HwVSimPhoneSwitcher mPhoneSwitcher;
    private final HashMap<NetworkRequest, LocalLogEx> mSpecificRequests = new HashMap<>();
    private int mSubscriptionId;

    public HwVSimTelephonyNetworkFactory(HwVSimPhoneSwitcher phoneSwitcher, Looper looper, Context context, int phoneId, DcTrackerEx dcTracker) {
        super(looper, context, "VSimTelephonyNetFactory[" + phoneId + "]", (NetworkCapabilitiesEx) null);
        this.mInternalHandler = new InternalHandler(looper);
        this.mPhoneId = phoneId;
        this.mSubscriptionId = SubscriptionManagerEx.getSubIdUsingSlotId(this.mPhoneId);
        setCapabilityFilter(makeNetworkFilter(this.mSubscriptionId));
        setScoreFilter(50);
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
        if (subscriptionManager != null) {
            subscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        }
        this.mPhoneSwitcher = phoneSwitcher;
        this.LOG_TAG = "VSimTelephonyNetFactory[" + phoneId + "]";
        this.mDcTracker = dcTracker;
        this.mIsActive = false;
        this.mPhoneSwitcher.registerForActivePhoneSwitch(this.mPhoneId, this.mInternalHandler, 3, null);
        register();
    }

    private NetworkCapabilitiesEx makeNetworkFilter(int subscriptionId) {
        NetworkCapabilitiesEx nc = new NetworkCapabilitiesEx();
        nc.addTransportType(0);
        nc.addCapability(0);
        nc.setNetworkSpecifier(new StringNetworkSpecifierEx(String.valueOf(subscriptionId)));
        log("make network filter for subId = " + subscriptionId);
        return nc;
    }

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwVSimTelephonyNetworkFactory.this.onNeedNetworkFor(msg);
                    return;
                case 2:
                    HwVSimTelephonyNetworkFactory.this.onReleaseNetworkFor(msg);
                    return;
                case 3:
                    HwVSimTelephonyNetworkFactory.this.onActivePhoneSwitch();
                    return;
                default:
                    return;
            }
        }
    }

    private void applyRequests(HashMap<NetworkRequest, LocalLogEx> requestMap, boolean action, String logStr) {
        for (Map.Entry<NetworkRequest, LocalLogEx> entry : requestMap.entrySet()) {
            NetworkRequest networkRequest = entry.getKey();
            LocalLogEx localLog = entry.getValue();
            localLog.log(logStr);
            if (action) {
                this.mDcTracker.requestNetwork(networkRequest, DcTrackerEx.REQUEST_TYPE_NORMAL, (Message) null, localLog);
            } else {
                this.mDcTracker.releaseNetwork(networkRequest, DcTrackerEx.RELEASE_TYPE_NORMAL, localLog);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActivePhoneSwitch() {
        boolean newIsActive = this.mPhoneSwitcher.isPhoneActive(this.mPhoneId);
        if (this.mIsActive != newIsActive) {
            this.mIsActive = newIsActive;
            String logString = "onActivePhoneSwitch(" + this.mIsActive + ")";
            log(logString);
            applyRequests(this.mSpecificRequests, this.mIsActive ? REQUEST : false, logString);
        }
    }

    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        log("needNetworkFor - " + networkRequest);
        Message msg = this.mInternalHandler.obtainMessage(1);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNeedNetworkFor(Message msg) {
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        boolean isApplicable = false;
        LocalLogEx localLog = null;
        if (!new StringNetworkSpecifierEx(new NetworkCapabilitiesEx(NetworkRequestEx.getNetworkCapabilities(networkRequest)).getNetworkSpecifier()).isSpecifierEmpty() && (localLog = this.mSpecificRequests.get(networkRequest)) == null) {
            localLog = new LocalLogEx(40);
            this.mSpecificRequests.put(networkRequest, localLog);
            isApplicable = REQUEST;
        }
        if (localLog != null) {
            if (!this.mIsActive || !isApplicable) {
                localLog.log("not acting");
                log("not acting" + " " + networkRequest);
                return;
            }
            localLog.log("onNeedNetworkFor");
            log("onNeedNetworkFor" + " " + networkRequest);
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
        if (!new StringNetworkSpecifierEx(new NetworkCapabilitiesEx(NetworkRequestEx.getNetworkCapabilities(networkRequest)).getNetworkSpecifier()).isSpecifierEmpty()) {
            localLog = this.mSpecificRequests.remove(networkRequest);
            isApplicable = localLog != null ? REQUEST : false;
        }
        if (localLog != null) {
            if (isApplicable) {
                localLog.log("onReleaseNetworkFor");
                log("onReleaseNetworkFor" + " " + networkRequest);
                this.mDcTracker.releaseNetwork(networkRequest, DcTrackerEx.RELEASE_TYPE_NORMAL, localLog);
                return;
            }
            localLog.log("not releasing");
            log("not releasing" + " " + networkRequest);
        }
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        RlogEx.d(this.LOG_TAG, s);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriterEx pw = new IndentingPrintWriterEx(writer, "  ");
        pw.println(this.LOG_TAG + " mSubId=" + this.mPhoneId);
        pw.println("Specific Requests:");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSubIdChange() {
        int newSubscriptionId = SubscriptionManagerEx.getSubIdUsingSlotId(this.mPhoneId);
        if (this.mSubscriptionId != newSubscriptionId) {
            log("onSubIdChange " + this.mSubscriptionId + "->" + newSubscriptionId);
            this.mSubscriptionId = newSubscriptionId;
            setCapabilityFilter(makeNetworkFilter(this.mSubscriptionId));
        }
    }
}
