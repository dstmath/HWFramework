package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.telephony.PhoneSwitcher;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.SubscriptionMonitor;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class TelephonyNetworkFactory extends NetworkFactory {
    protected static final boolean DBG = true;
    private static final int EVENT_ACTIVE_PHONE_SWITCH = 1;
    private static final int EVENT_DEFAULT_SUBSCRIPTION_CHANGED = 3;
    private static final int EVENT_NETWORK_RELEASE = 5;
    private static final int EVENT_NETWORK_REQUEST = 4;
    private static final int EVENT_SUBSCRIPTION_CHANGED = 2;
    private static final boolean RELEASE = false;
    private static final boolean REQUEST = true;
    private static final int REQUEST_LOG_SIZE = 40;
    private static final int TELEPHONY_NETWORK_SCORE = 50;
    public final String LOG_TAG;
    private final DcTracker mDcTracker;
    private final HashMap<NetworkRequest, LocalLog> mDefaultRequests;
    private final Handler mInternalHandler;
    private boolean mIsActive;
    private boolean mIsDefault;
    private NetworkRequest mNetworkRequestRejectByWifi;
    private int mPhoneId;
    private final PhoneSwitcher mPhoneSwitcher;
    private final HashMap<NetworkRequest, LocalLog> mSpecificRequests;
    private final SubscriptionController mSubscriptionController;
    private int mSubscriptionId;
    private final SubscriptionMonitor mSubscriptionMonitor;

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (VSimUtilsInner.isVSimOn() && VSimUtilsInner.isPlatformRealTripple()) {
                TelephonyNetworkFactory.this.log("not handle message due to vsim is on");
                return;
            }
            switch (msg.what) {
                case TelephonyNetworkFactory.EVENT_ACTIVE_PHONE_SWITCH /*1*/:
                    TelephonyNetworkFactory.this.onActivePhoneSwitch();
                    break;
                case TelephonyNetworkFactory.EVENT_SUBSCRIPTION_CHANGED /*2*/:
                    TelephonyNetworkFactory.this.onSubIdChange();
                    break;
                case TelephonyNetworkFactory.EVENT_DEFAULT_SUBSCRIPTION_CHANGED /*3*/:
                    TelephonyNetworkFactory.this.onDefaultChange();
                    break;
                case TelephonyNetworkFactory.EVENT_NETWORK_REQUEST /*4*/:
                    TelephonyNetworkFactory.this.onNeedNetworkFor(msg);
                    break;
                case TelephonyNetworkFactory.EVENT_NETWORK_RELEASE /*5*/:
                    TelephonyNetworkFactory.this.onReleaseNetworkFor(msg);
                    break;
            }
        }
    }

    public TelephonyNetworkFactory(PhoneSwitcher phoneSwitcher, SubscriptionController subscriptionController, SubscriptionMonitor subscriptionMonitor, Looper looper, Context context, int phoneId, DcTracker dcTracker) {
        super(looper, context, "TelephonyNetworkFactory[" + phoneId + "]", null);
        this.mDefaultRequests = new HashMap();
        this.mSpecificRequests = new HashMap();
        this.mNetworkRequestRejectByWifi = null;
        this.mInternalHandler = new InternalHandler(looper);
        setCapabilityFilter(makeNetworkFilter(subscriptionController, phoneId));
        setScoreFilter(TELEPHONY_NETWORK_SCORE);
        this.mPhoneSwitcher = phoneSwitcher;
        this.mSubscriptionController = subscriptionController;
        this.mSubscriptionMonitor = subscriptionMonitor;
        this.mPhoneId = phoneId;
        this.LOG_TAG = "TelephonyNetworkFactory[" + phoneId + "]";
        this.mDcTracker = dcTracker;
        this.mIsActive = RELEASE;
        this.mPhoneSwitcher.registerForActivePhoneSwitch(this.mPhoneId, this.mInternalHandler, EVENT_ACTIVE_PHONE_SWITCH, null);
        this.mSubscriptionId = -1;
        this.mSubscriptionMonitor.registerForSubscriptionChanged(this.mPhoneId, this.mInternalHandler, EVENT_SUBSCRIPTION_CHANGED, null);
        this.mIsDefault = RELEASE;
        this.mSubscriptionMonitor.registerForDefaultDataSubscriptionChanged(this.mPhoneId, this.mInternalHandler, EVENT_DEFAULT_SUBSCRIPTION_CHANGED, null);
        register();
    }

    private NetworkCapabilities makeNetworkFilter(SubscriptionController subscriptionController, int phoneId) {
        return makeNetworkFilter(subscriptionController.getSubIdUsingPhoneId(phoneId));
    }

    private NetworkCapabilities makeNetworkFilter(int subscriptionId) {
        NetworkCapabilities nc = new NetworkCapabilities();
        nc.addTransportType(0);
        nc.addCapability(0);
        nc.addCapability(EVENT_ACTIVE_PHONE_SWITCH);
        nc.addCapability(EVENT_SUBSCRIPTION_CHANGED);
        nc.addCapability(EVENT_DEFAULT_SUBSCRIPTION_CHANGED);
        nc.addCapability(EVENT_NETWORK_REQUEST);
        nc.addCapability(EVENT_NETWORK_RELEASE);
        nc.addCapability(7);
        nc.addCapability(8);
        nc.addCapability(9);
        nc.addCapability(10);
        nc.addCapability(13);
        nc.addCapability(12);
        nc.addCapability(18);
        nc.addCapability(19);
        nc.addCapability(20);
        nc.addCapability(21);
        nc.addCapability(22);
        nc.addCapability(23);
        nc.addCapability(24);
        nc.setNetworkSpecifier(String.valueOf(subscriptionId));
        return nc;
    }

    private void applyRequests(HashMap<NetworkRequest, LocalLog> requestMap, boolean action, String logStr) {
        for (NetworkRequest networkRequest : requestMap.keySet()) {
            LocalLog localLog = (LocalLog) requestMap.get(networkRequest);
            localLog.log(logStr);
            if (action) {
                this.mDcTracker.requestNetwork(networkRequest, localLog);
            } else {
                this.mDcTracker.releaseNetwork(networkRequest, localLog);
            }
        }
    }

    private void onActivePhoneSwitch() {
        boolean z = REQUEST;
        boolean newIsActive = this.mPhoneSwitcher.isPhoneActive(this.mPhoneId);
        if (this.mIsActive != newIsActive) {
            this.mIsActive = newIsActive;
            String logString = "onActivePhoneSwitch(" + this.mIsActive + ", " + this.mIsDefault + ")";
            log(logString);
            if (this.mIsDefault) {
                boolean z2;
                HashMap hashMap = this.mDefaultRequests;
                if (this.mIsActive) {
                    z2 = REQUEST;
                } else {
                    z2 = RELEASE;
                }
                applyRequests(hashMap, z2, logString);
            }
            HashMap hashMap2 = this.mSpecificRequests;
            if (!this.mIsActive) {
                z = RELEASE;
            }
            applyRequests(hashMap2, z, logString);
        }
    }

    private void onSubIdChange() {
        int newSubscriptionId = this.mSubscriptionController.getSubIdUsingPhoneId(this.mPhoneId);
        if (this.mSubscriptionId != newSubscriptionId) {
            log("onSubIdChange " + this.mSubscriptionId + "->" + newSubscriptionId);
            this.mSubscriptionId = newSubscriptionId;
            setCapabilityFilter(makeNetworkFilter(this.mSubscriptionId));
        }
    }

    private void onDefaultChange() {
        boolean newIsDefault = this.mSubscriptionController.getDefaultDataSubId() == this.mSubscriptionId ? REQUEST : RELEASE;
        if (newIsDefault != this.mIsDefault) {
            this.mIsDefault = newIsDefault;
            String logString = "onDefaultChange(" + this.mIsActive + "," + this.mIsDefault + ")";
            log(logString);
            if (this.mIsActive) {
                boolean z;
                HashMap hashMap = this.mDefaultRequests;
                if (this.mIsDefault) {
                    z = REQUEST;
                } else {
                    z = RELEASE;
                }
                applyRequests(hashMap, z, logString);
            }
        }
    }

    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        Message msg = this.mInternalHandler.obtainMessage(EVENT_NETWORK_REQUEST);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    private void onNeedNetworkFor(Message msg) {
        LocalLog localLog;
        NetworkRequest networkRequest = msg.obj;
        boolean z = RELEASE;
        if (TextUtils.isEmpty(networkRequest.networkCapabilities.getNetworkSpecifier())) {
            localLog = (LocalLog) this.mDefaultRequests.get(networkRequest);
            if (localLog == null) {
                localLog = new LocalLog(REQUEST_LOG_SIZE);
                localLog.log("created for " + networkRequest);
                this.mDefaultRequests.put(networkRequest, localLog);
                z = this.mIsDefault;
                boolean defaultMobileEnable = SystemProperties.getBoolean("sys.defaultapn.enabled", REQUEST);
                log("defaultMobileEnable is " + (defaultMobileEnable ? "true" : "false"));
                log("networkRequest = " + networkRequest);
                if (!(defaultMobileEnable || networkRequest.networkCapabilities.hasTransport(0))) {
                    log("onNeedNetworkFor set isApplicable false");
                    z = RELEASE;
                    if (networkRequest.requestId == EVENT_ACTIVE_PHONE_SWITCH) {
                        this.mNetworkRequestRejectByWifi = networkRequest;
                        this.mDefaultRequests.remove(networkRequest);
                    }
                }
            }
        } else {
            localLog = (LocalLog) this.mSpecificRequests.get(networkRequest);
            if (localLog == null) {
                localLog = new LocalLog(REQUEST_LOG_SIZE);
                this.mSpecificRequests.put(networkRequest, localLog);
                z = REQUEST;
            }
        }
        if (this.mIsActive && z) {
            String s = "onNeedNetworkFor";
            localLog.log(s);
            log(s + " " + networkRequest);
            this.mDcTracker.requestNetwork(networkRequest, localLog);
            return;
        }
        s = "not acting - isApp=" + z + ", isAct=" + this.mIsActive;
        localLog.log(s);
        log(s + " " + networkRequest);
    }

    public void releaseNetworkFor(NetworkRequest networkRequest) {
        Message msg = this.mInternalHandler.obtainMessage(EVENT_NETWORK_RELEASE);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    private void onReleaseNetworkFor(Message msg) {
        LocalLog localLog;
        boolean isApplicable;
        NetworkRequest networkRequest = msg.obj;
        if (TextUtils.isEmpty(networkRequest.networkCapabilities.getNetworkSpecifier())) {
            localLog = (LocalLog) this.mDefaultRequests.remove(networkRequest);
            isApplicable = localLog != null ? this.mIsDefault : RELEASE;
        } else {
            localLog = (LocalLog) this.mSpecificRequests.remove(networkRequest);
            isApplicable = localLog != null ? REQUEST : RELEASE;
        }
        if (this.mIsActive && isApplicable) {
            String s = "onReleaseNetworkFor";
            localLog.log(s);
            log(s + " " + networkRequest);
            this.mDcTracker.releaseNetwork(networkRequest, localLog);
            return;
        }
        s = "not releasing - isApp=" + isApplicable + ", isAct=" + this.mIsActive;
        if (localLog != null) {
            localLog.log(s);
        }
        log(s + " " + networkRequest);
    }

    protected void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    public void reconnectDefaultRequestRejectByWifi() {
        log("enter reconnectDefaultRequestRejectByWifi=" + this.mNetworkRequestRejectByWifi);
        if (this.mNetworkRequestRejectByWifi != null) {
            needNetworkFor(this.mNetworkRequestRejectByWifi, 0);
            this.mNetworkRequestRejectByWifi = null;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println(this.LOG_TAG + " mSubId=" + this.mSubscriptionId + " mIsActive=" + this.mIsActive + " mIsDefault=" + this.mIsDefault);
        pw.println("Default Requests:");
        pw.increaseIndent();
        for (NetworkRequest nr : this.mDefaultRequests.keySet()) {
            pw.println(nr);
            pw.increaseIndent();
            ((LocalLog) this.mDefaultRequests.get(nr)).dump(fd, pw, args);
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }
}
