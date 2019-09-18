package com.android.internal.telephony.dataconnection;

import android.common.HwFrameworkFactory;
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
import android.util.LocalLog;
import com.android.internal.telephony.HwTelephonyFactory;
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
    private final HashMap<NetworkRequest, LocalLog> mDefaultRequests = new HashMap<>();
    private final Handler mInternalHandler;
    private boolean mIsActive;
    private boolean mIsDefault;
    private int mPhoneId;
    private final PhoneSwitcher mPhoneSwitcher;
    private final HashMap<NetworkRequest, LocalLog> mSpecificRequests = new HashMap<>();
    private final SubscriptionController mSubscriptionController;
    private int mSubscriptionId;
    private final SubscriptionMonitor mSubscriptionMonitor;

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (VSimUtilsInner.isVSimOn()) {
                TelephonyNetworkFactory.this.log("not handle message due to vsim is on");
                return;
            }
            switch (msg.what) {
                case 1:
                    TelephonyNetworkFactory.this.onActivePhoneSwitch();
                    break;
                case 2:
                    TelephonyNetworkFactory.this.onSubIdChange();
                    break;
                case 3:
                    TelephonyNetworkFactory.this.onDefaultChange();
                    break;
                case 4:
                    TelephonyNetworkFactory.this.onNeedNetworkFor(msg);
                    break;
                case 5:
                    TelephonyNetworkFactory.this.onReleaseNetworkFor(msg);
                    break;
            }
        }
    }

    public TelephonyNetworkFactory(PhoneSwitcher phoneSwitcher, SubscriptionController subscriptionController, SubscriptionMonitor subscriptionMonitor, Looper looper, Context context, int phoneId, DcTracker dcTracker) {
        super(looper, context, "TelephonyNetworkFactory[" + phoneId + "]", null);
        this.mInternalHandler = new InternalHandler(looper);
        setCapabilityFilter(makeNetworkFilter(subscriptionController, phoneId));
        setScoreFilter(50);
        this.mPhoneSwitcher = phoneSwitcher;
        this.mSubscriptionController = subscriptionController;
        this.mSubscriptionMonitor = subscriptionMonitor;
        this.mPhoneId = phoneId;
        this.LOG_TAG = "TelephonyNetworkFactory[" + phoneId + "]";
        this.mDcTracker = dcTracker;
        this.mIsActive = false;
        this.mPhoneSwitcher.registerForActivePhoneSwitch(this.mPhoneId, this.mInternalHandler, 1, null);
        this.mSubscriptionId = -1;
        this.mSubscriptionMonitor.registerForSubscriptionChanged(this.mPhoneId, this.mInternalHandler, 2, null);
        this.mIsDefault = false;
        this.mSubscriptionMonitor.registerForDefaultDataSubscriptionChanged(this.mPhoneId, this.mInternalHandler, 3, null);
        register();
    }

    private NetworkCapabilities makeNetworkFilter(SubscriptionController subscriptionController, int phoneId) {
        return makeNetworkFilter(subscriptionController.getSubIdUsingPhoneId(phoneId));
    }

    private NetworkCapabilities makeNetworkFilter(int subscriptionId) {
        NetworkCapabilities nc = new NetworkCapabilities();
        nc.addTransportType(0);
        nc.addCapability(0);
        nc.addCapability(1);
        nc.addCapability(2);
        nc.addCapability(3);
        nc.addCapability(4);
        nc.addCapability(5);
        nc.addCapability(7);
        nc.addCapability(8);
        nc.addCapability(9);
        nc.addCapability(10);
        nc.addCapability(13);
        nc.addCapability(12);
        nc.addCapability(23);
        nc.addCapability(24);
        nc.addCapability(25);
        nc.addCapability(26);
        nc.addCapability(27);
        nc.addCapability(28);
        nc.addCapability(29);
        nc.addCapability(30);
        nc.setNetworkSpecifier(new StringNetworkSpecifier(String.valueOf(subscriptionId)));
        return nc;
    }

    private void applyRequests(HashMap<NetworkRequest, LocalLog> requestMap, boolean action, String logStr) {
        for (NetworkRequest networkRequest : requestMap.keySet()) {
            LocalLog localLog = requestMap.get(networkRequest);
            localLog.log(logStr);
            if (action) {
                this.mDcTracker.requestNetwork(networkRequest, localLog);
            } else {
                this.mDcTracker.releaseNetwork(networkRequest, localLog);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onActivePhoneSwitch() {
        boolean newIsActive = this.mPhoneSwitcher.isPhoneActive(this.mPhoneId);
        if (this.mIsActive != newIsActive) {
            this.mIsActive = newIsActive;
            String logString = "onActivePhoneSwitch(" + this.mIsActive + ", " + this.mIsDefault + ")";
            log(logString);
            if (this.mIsDefault) {
                applyRequests(this.mDefaultRequests, this.mIsActive, logString);
            }
            applyRequests(this.mSpecificRequests, this.mIsActive, logString);
        }
    }

    /* access modifiers changed from: private */
    public void onSubIdChange() {
        int newSubscriptionId = this.mSubscriptionController.getSubIdUsingPhoneId(this.mPhoneId);
        if (this.mSubscriptionId != newSubscriptionId) {
            log("onSubIdChange " + this.mSubscriptionId + "->" + newSubscriptionId);
            this.mSubscriptionId = newSubscriptionId;
            setCapabilityFilter(makeNetworkFilter(this.mSubscriptionId));
        }
    }

    /* access modifiers changed from: private */
    public void onDefaultChange() {
        boolean newIsDefault = this.mSubscriptionController.getDefaultDataSubId() == this.mSubscriptionId;
        if (newIsDefault != this.mIsDefault) {
            this.mIsDefault = newIsDefault;
            String logString = "onDefaultChange(" + this.mIsActive + "," + this.mIsDefault + ")";
            log(logString);
            if (this.mIsActive) {
                boolean isSwitchingToSlave = HwTelephonyFactory.getHwDataConnectionManager().isSwitchingToSlave();
                if (!this.mIsDefault && isSwitchingToSlave && HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == this.mPhoneId) {
                    log("isSwitchingToSlave clearDefaultLink not release mDefaultRequests");
                    this.mDcTracker.clearDefaultLink();
                } else if (!this.mIsDefault || !HwTelephonyFactory.getHwDataConnectionManager().isDeactivatingSlaveData() || HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() != this.mPhoneId) {
                    applyRequests(this.mDefaultRequests, this.mIsDefault, logString);
                } else {
                    log("isDeactivatingSlaveData resumeDefaultLink not request mDefaultRequests");
                }
            }
        }
    }

    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        Message msg = this.mInternalHandler.obtainMessage(4);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    public void onNeedNetworkFor(Message msg) {
        LocalLog localLog;
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        boolean isApplicable = false;
        if (networkRequest.networkCapabilities.getNetworkSpecifier() == null) {
            localLog = this.mDefaultRequests.get(networkRequest);
            if (localLog == null) {
                localLog = new LocalLog(40);
                localLog.log("created for " + networkRequest);
                this.mDefaultRequests.put(networkRequest, localLog);
                isApplicable = this.mIsDefault;
            }
        } else {
            localLog = this.mSpecificRequests.get(networkRequest);
            if (localLog == null) {
                localLog = new LocalLog(40);
                this.mSpecificRequests.put(networkRequest, localLog);
                isApplicable = true;
            }
        }
        if (!this.mIsActive || !isApplicable) {
            localLog.log("not acting - isApp=" + isApplicable + ", isAct=" + this.mIsActive);
            log(s + " " + networkRequest);
            return;
        }
        localLog.log("onNeedNetworkFor");
        log("onNeedNetworkFor" + " " + networkRequest);
        this.mDcTracker.requestNetwork(networkRequest, localLog);
    }

    public void releaseNetworkFor(NetworkRequest networkRequest) {
        Message msg = this.mInternalHandler.obtainMessage(5);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    public void onReleaseNetworkFor(Message msg) {
        boolean isApplicable;
        LocalLog localLog;
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        boolean z = false;
        if (networkRequest.networkCapabilities.getNetworkSpecifier() == null) {
            localLog = this.mDefaultRequests.remove(networkRequest);
            if (localLog != null) {
                z = true;
            }
            isApplicable = z;
        } else {
            localLog = this.mSpecificRequests.remove(networkRequest);
            if (localLog != null) {
                z = true;
            }
            isApplicable = z;
        }
        if (!this.mIsActive || !isApplicable) {
            String s = "not releasing - isApp=" + isApplicable + ", isAct=" + this.mIsActive;
            if (localLog != null) {
                localLog.log(s);
            }
            log(s + " " + networkRequest);
            return;
        }
        localLog.log("onReleaseNetworkFor");
        log("onReleaseNetworkFor" + " " + networkRequest);
        this.mDcTracker.releaseNetwork(networkRequest, localLog);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println(this.LOG_TAG + " mSubId=" + this.mSubscriptionId + " mIsActive=" + this.mIsActive + " mIsDefault=" + this.mIsDefault);
        pw.println("Default Requests:");
        pw.increaseIndent();
        for (NetworkRequest nr : this.mDefaultRequests.keySet()) {
            pw.println(nr);
            pw.increaseIndent();
            this.mDefaultRequests.get(nr).dump(fd, pw, args);
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }

    public void resumeDefaultLink() {
        this.mDcTracker.resumeDefaultLink();
    }

    public DcTracker getDcTracker() {
        return this.mDcTracker;
    }

    public boolean acceptRequest(NetworkRequest request, int score) {
        boolean defaultMobileEnable = SystemProperties.getBoolean("sys.defaultapn.enabled", true);
        log("defaultMobileEnable is " + defaultMobileEnable);
        return defaultMobileEnable;
    }
}
