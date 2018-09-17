package com.android.internal.telephony;

import android.content.Context;
import android.telephony.Rlog;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.uicc.HwVSimIccCardProxy;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwVSimPhone extends GsmCdmaPhone {
    private static boolean HWDBG = false;
    private static final boolean HWLOGW_E = true;
    static final String LOG_TAG = "VSimPhone";
    private static final int SUB_VSIM = 2;
    private HwVSimIccCardProxy mIccCardProxy;
    HwVSimUiccController mVsimUiccController = null;

    static {
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(LOG_TAG, 3) : false : true;
        HWDBG = isLoggable;
    }

    public HwVSimPhone(Context context, CommandsInterface ci, PhoneNotifier notifier) {
        super(context, ci, notifier, 2, 1, TelephonyComponentFactory.getInstance());
        this.mIccCardProxy = new HwVSimIccCardProxy(context, ci);
        this.mVsimUiccController = HwVSimUiccController.getInstance();
        this.mVsimUiccController.registerForIccChanged(this, 30, null);
        if (HWDBG) {
            logd("VSimPhone: constructor: sub = " + this.mPhoneId);
        }
    }

    public void dispose() {
        super.dispose();
        this.mVsimUiccController.unregisterForIccChanged(this);
    }

    public State getState() {
        return State.IDLE;
    }

    public void setDataEnabled(boolean enable) {
        this.mDcTracker.setDataEnabled(enable);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("VSimPhone extends:");
        super.dump(fd, pw, args);
    }

    protected void logd(String s) {
        Rlog.d(LOG_TAG, "[VSimPhone] " + s);
    }

    public IccCard getIccCard() {
        return this.mIccCardProxy;
    }

    public int getSubId() {
        return 2;
    }

    public void updateDataConnectionTracker() {
        if (HWDBG) {
            logd("updateDataConnectionTracker");
        }
        this.mDcTracker.updateForVSim();
        this.mDcTracker.setInternalDataEnabled(true);
    }

    public void sendSubscriptionSettings(boolean restoreNetworkSelection) {
        if (HWDBG) {
            logd("sendSubscriptionSettings: do nothing for vsim");
        }
    }

    protected void onUpdateIccAvailability() {
        if (HWDBG) {
            logd("E onUpdateIccAvailability");
        }
        if (this.mVsimUiccController == null) {
            if (HWDBG) {
                logd("X onUpdateIccAvailability mUiccController null");
            }
            return;
        }
        UiccCardApplication newUiccApplication = this.mVsimUiccController.getUiccCardApplication(1);
        UiccCardApplication app = (UiccCardApplication) this.mUiccApplication.get();
        if (app != newUiccApplication) {
            if (app != null) {
                this.mIccRecords.set(null);
                this.mUiccApplication.set(null);
            }
            if (newUiccApplication != null) {
                if (HWDBG) {
                    logd("New Uicc application found");
                }
                this.mUiccApplication.set(newUiccApplication);
                this.mIccRecords.set(newUiccApplication.getIccRecords());
            }
        }
        if (HWDBG) {
            logd("X onUpdateIccAvailability");
        }
    }
}
