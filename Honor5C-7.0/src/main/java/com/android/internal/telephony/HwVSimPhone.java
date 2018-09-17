package com.android.internal.telephony;

import android.content.Context;
import android.telephony.Rlog;
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
    HwVSimUiccController mVsimUiccController;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwVSimPhone.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwVSimPhone.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwVSimPhone.<clinit>():void");
    }

    public HwVSimPhone(Context context, CommandsInterface ci, PhoneNotifier notifier) {
        super(context, ci, notifier, SUB_VSIM, 1, TelephonyComponentFactory.getInstance());
        this.mVsimUiccController = null;
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

    public boolean getDataRoamingEnabled() {
        return false;
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
        return SUB_VSIM;
    }

    public void updateDataConnectionTracker() {
        if (HWDBG) {
            logd("updateDataConnectionTracker");
        }
        this.mDcTracker.updateForVSim();
        this.mDcTracker.setInternalDataEnabled(HWLOGW_E);
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
