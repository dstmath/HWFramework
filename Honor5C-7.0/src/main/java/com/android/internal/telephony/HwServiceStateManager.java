package com.android.internal.telephony;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cdma.HwCdmaServiceStateManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.gsm.HwGsmServiceStateManager;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccCardApplicationUtils;
import java.util.Arrays;
import java.util.Map;

public class HwServiceStateManager extends Handler {
    protected static final int CT_NUM_MATCH_HOME = 11;
    protected static final int CT_NUM_MATCH_ROAMING = 10;
    protected static final int CT_SID_1st_END = 14335;
    protected static final int CT_SID_1st_START = 13568;
    protected static final int CT_SID_2nd_END = 26111;
    protected static final int CT_SID_2nd_START = 25600;
    protected static final int DEFAULT_SID = 0;
    protected static final int DELAYED_TIME_DEFAULT_VALUE = 0;
    protected static final int DELAYED_TIME_NETWORKSTATUS_CS_2G = 0;
    protected static final int DELAYED_TIME_NETWORKSTATUS_CS_3G = 0;
    protected static final int DELAYED_TIME_NETWORKSTATUS_CS_4G = 0;
    protected static final int DELAYED_TIME_NETWORKSTATUS_PS_2G = 0;
    protected static final int DELAYED_TIME_NETWORKSTATUS_PS_3G = 0;
    protected static final int DELAYED_TIME_NETWORKSTATUS_PS_4G = 0;
    protected static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 0;
    protected static final int EVENT_ICC_RECORDS_EONS_UPDATED = 1;
    protected static final int EVENT_RESUME_DATA = 203;
    protected static final int EVENT_SET_PRE_NETWORKTYPE = 202;
    protected static final String INVAILD_PLMN = "1023127-123456-1023456-123127-";
    protected static final boolean IS_CHINATELECOM = false;
    protected static final boolean IS_MULTI_SIM_ENABLED = false;
    protected static final int RESUME_DATA_TIME = 8000;
    protected static final int SET_PRE_NETWORK_TIME = 5000;
    protected static final int SET_PRE_NETWORK_TIME_DELAY = 2000;
    private static final String TAG = "HwServiceStateManager";
    private static Map<Object, HwCdmaServiceStateManager> cdmaServiceStateManagers;
    private static Map<Object, HwGsmServiceStateManager> gsmServiceStateManagers;
    private static final boolean isScreenOffNotUpdateLocation = false;
    private static Map<Object, HwServiceStateManager> serviceStateManagers;
    protected static final UiccCardApplicationUtils uiccCardApplicationUtils = null;
    private static final boolean voice_reg_state_for_ons = false;
    protected int mMainSlot;
    protected int mPendingPreNwType;
    protected Message mPendingsavemessage;
    private Phone mPhoneBase;
    protected boolean mRefreshState;
    private ServiceStateTracker mServiceStateTracker;
    protected boolean mSetPreNwTypeRequested;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwServiceStateManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwServiceStateManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwServiceStateManager.<clinit>():void");
    }

    protected HwServiceStateManager(Phone phoneBase) {
        super(Looper.getMainLooper());
        this.mRefreshState = IS_MULTI_SIM_ENABLED;
        this.mSetPreNwTypeRequested = IS_MULTI_SIM_ENABLED;
        this.mPendingPreNwType = EVENT_DELAY_UPDATE_REGISTER_STATE_DONE;
    }

    public String getPlmn() {
        return "";
    }

    public void sendDualSimUpdateSpnIntent(boolean showSpn, String spn, boolean showPlmn, String plmn) {
    }

    public OnsDisplayParams getOnsDisplayParamsHw(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        return new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
    }

    public HwServiceStateManager(ServiceStateTracker serviceStateTracker, Phone phoneBase) {
        super(Looper.getMainLooper());
        this.mRefreshState = IS_MULTI_SIM_ENABLED;
        this.mSetPreNwTypeRequested = IS_MULTI_SIM_ENABLED;
        this.mPendingPreNwType = EVENT_DELAY_UPDATE_REGISTER_STATE_DONE;
        this.mServiceStateTracker = serviceStateTracker;
        this.mPhoneBase = phoneBase;
    }

    public static synchronized HwServiceStateManager getHwServiceStateManager(ServiceStateTracker serviceStateTracker, Phone phoneBase) {
        HwServiceStateManager hwServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwServiceStateManager = (HwServiceStateManager) serviceStateManagers.get(serviceStateTracker);
            if (hwServiceStateManager == null) {
                hwServiceStateManager = new HwServiceStateManager(serviceStateTracker, phoneBase);
                serviceStateManagers.put(serviceStateTracker, hwServiceStateManager);
            }
        }
        return hwServiceStateManager;
    }

    public static synchronized HwGsmServiceStateManager getHwGsmServiceStateManager(ServiceStateTracker serviceStateTracker, GsmCdmaPhone phone) {
        HwGsmServiceStateManager hwGsmServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwGsmServiceStateManager = (HwGsmServiceStateManager) gsmServiceStateManagers.get(serviceStateTracker);
            if (hwGsmServiceStateManager == null) {
                hwGsmServiceStateManager = new HwGsmServiceStateManager(serviceStateTracker, phone);
                gsmServiceStateManagers.put(serviceStateTracker, hwGsmServiceStateManager);
            }
        }
        return hwGsmServiceStateManager;
    }

    public static synchronized HwCdmaServiceStateManager getHwCdmaServiceStateManager(ServiceStateTracker serviceStateTracker, GsmCdmaPhone phone) {
        HwCdmaServiceStateManager hwCdmaServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwCdmaServiceStateManager = (HwCdmaServiceStateManager) cdmaServiceStateManagers.get(serviceStateTracker);
            if (hwCdmaServiceStateManager == null) {
                hwCdmaServiceStateManager = new HwCdmaServiceStateManager(serviceStateTracker, phone);
                cdmaServiceStateManagers.put(serviceStateTracker, hwCdmaServiceStateManager);
            }
        }
        return hwCdmaServiceStateManager;
    }

    public static synchronized void dispose(ServiceStateTracker serviceStateTracker) {
        synchronized (HwServiceStateManager.class) {
            if (serviceStateTracker == null) {
                return;
            }
            HwGsmServiceStateManager hwGsmServiceStateManager = (HwGsmServiceStateManager) gsmServiceStateManagers.get(serviceStateTracker);
            if (hwGsmServiceStateManager != null) {
                hwGsmServiceStateManager.dispose();
            }
            gsmServiceStateManagers.put(serviceStateTracker, null);
            HwCdmaServiceStateManager hwCdmaServiceStateManager = (HwCdmaServiceStateManager) cdmaServiceStateManagers.get(serviceStateTracker);
            if (hwCdmaServiceStateManager != null) {
                hwCdmaServiceStateManager.dispose();
            }
            cdmaServiceStateManagers.put(serviceStateTracker, null);
        }
    }

    public int getCombinedRegState(ServiceState serviceState) {
        int regState = serviceState.getVoiceRegState();
        int dataRegState = serviceState.getDataRegState();
        if (voice_reg_state_for_ons) {
            return regState;
        }
        if (regState == EVENT_ICC_RECORDS_EONS_UPDATED && dataRegState == 0) {
            Rlog.d(TAG, "getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
            regState = dataRegState;
        }
        return regState;
    }

    public void processCTNumMatch(boolean roaming, UiccCardApplication uiccCardApplication) {
    }

    protected void checkMultiSimNumMatch() {
        int[] matchArray = new int[]{SystemProperties.getInt("gsm.hw.matchnum0", -1), SystemProperties.getInt("gsm.hw.matchnum.short0", -1), SystemProperties.getInt("gsm.hw.matchnum1", -1), SystemProperties.getInt("gsm.hw.matchnum.short1", -1)};
        Arrays.sort(matchArray);
        int numMatch = matchArray[3];
        int numMatchShort = numMatch;
        int i = 2;
        while (i >= 0) {
            if (matchArray[i] < numMatch && matchArray[i] > 0) {
                numMatchShort = matchArray[i];
            }
            i--;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(numMatch));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(numMatchShort));
        Rlog.d(TAG, "checkMultiSimNumMatch: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum", EVENT_DELAY_UPDATE_REGISTER_STATE_DONE) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short", EVENT_DELAY_UPDATE_REGISTER_STATE_DONE));
    }

    protected void setCTNumMatchHomeForSlot(int slotId) {
        if (IS_MULTI_SIM_ENABLED) {
            SystemProperties.set("gsm.hw.matchnum" + slotId, Integer.toString(CT_NUM_MATCH_HOME));
            SystemProperties.set("gsm.hw.matchnum.short" + slotId, Integer.toString(CT_NUM_MATCH_HOME));
            checkMultiSimNumMatch();
            return;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(CT_NUM_MATCH_HOME));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(CT_NUM_MATCH_HOME));
    }

    protected void setCTNumMatchRoamingForSlot(int slotId) {
        if (IS_MULTI_SIM_ENABLED) {
            SystemProperties.set("gsm.hw.matchnum" + slotId, Integer.toString(CT_NUM_MATCH_ROAMING));
            SystemProperties.set("gsm.hw.matchnum.short" + slotId, Integer.toString(CT_NUM_MATCH_ROAMING));
            checkMultiSimNumMatch();
            return;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(CT_NUM_MATCH_ROAMING));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(CT_NUM_MATCH_ROAMING));
    }

    public static boolean isCustScreenOff(GsmCdmaPhone phoneBase) {
        if (!(!isScreenOffNotUpdateLocation || phoneBase == null || phoneBase.getContext() == null)) {
            PowerManager powerManager = (PowerManager) phoneBase.getContext().getSystemService("power");
            if (!(powerManager == null || powerManager.isScreenOn())) {
                Rlog.d(TAG, " ScreenOff do nothing");
                return true;
            }
        }
        return IS_MULTI_SIM_ENABLED;
    }

    public void setOOSFlag(boolean flag) {
    }

    private void setPreferredNetworkType(int networkType, int phoneId, Message response) {
        if (!HwModemCapability.isCapabilitySupport(9) || TelephonyManager.getDefault().getPhoneCount() <= EVENT_ICC_RECORDS_EONS_UPDATED) {
            this.mPhoneBase.mCi.setPreferredNetworkType(networkType, response);
            return;
        }
        Rlog.d(TAG, "PhoneCount > 1");
        HwModemBindingPolicyHandler.getInstance().setPreferredNetworkType(networkType, phoneId, response);
    }

    public void setPreferredNetworkTypeSafely(Phone phoneBase, int networkType, Message response) {
        this.mPhoneBase = phoneBase;
        DcTracker dcTracker = this.mPhoneBase.mDcTracker;
        if (this.mServiceStateTracker == null) {
            Rlog.d(TAG, "mServiceStateTracker is null, it is unexpected!");
        }
        if (networkType != CT_NUM_MATCH_ROAMING) {
            if (this.mSetPreNwTypeRequested) {
                removeMessages(EVENT_SET_PRE_NETWORKTYPE);
                Rlog.d(TAG, "cancel setPreferredNetworkType");
            }
            this.mSetPreNwTypeRequested = IS_MULTI_SIM_ENABLED;
            Rlog.d(TAG, "PreNetworkType is not LTE, setPreferredNetworkType now!");
            setPreferredNetworkType(networkType, this.mPhoneBase.getPhoneId(), response);
        } else if (!this.mSetPreNwTypeRequested) {
            if (dcTracker.isDisconnected()) {
                setPreferredNetworkType(networkType, this.mPhoneBase.getPhoneId(), response);
                Rlog.d(TAG, "data is Disconnected, setPreferredNetworkType now!");
                return;
            }
            dcTracker.setInternalDataEnabled(IS_MULTI_SIM_ENABLED);
            Rlog.d(TAG, "Data is disabled and wait up to 8s to resume data.");
            sendMessageDelayed(obtainMessage(EVENT_RESUME_DATA), 8000);
            this.mPendingsavemessage = response;
            this.mPendingPreNwType = networkType;
            Message msg = Message.obtain(this);
            msg.what = EVENT_SET_PRE_NETWORKTYPE;
            msg.arg1 = networkType;
            msg.obj = response;
            Rlog.d(TAG, "Wait up to 5s for data disconnect to setPreferredNetworkType.");
            sendMessageDelayed(msg, 5000);
            this.mSetPreNwTypeRequested = true;
        }
    }

    public void checkAndSetNetworkType() {
        if (this.mSetPreNwTypeRequested) {
            Rlog.d(TAG, "mSetPreNwTypeRequested is true and wait a few seconds to setPreferredNetworkType");
            removeMessages(EVENT_SET_PRE_NETWORKTYPE);
            Message msg = Message.obtain(this);
            msg.what = EVENT_SET_PRE_NETWORKTYPE;
            msg.arg1 = this.mPendingPreNwType;
            msg.obj = this.mPendingsavemessage;
            sendMessageDelayed(msg, 2000);
            return;
        }
        Rlog.d(TAG, "No need to setPreferredNetworkType");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_SET_PRE_NETWORKTYPE /*202*/:
                if (this.mSetPreNwTypeRequested) {
                    Rlog.d(TAG, "EVENT_SET_PRE_NETWORKTYPE, setPreferredNetworkType now.");
                    setPreferredNetworkType(msg.arg1, this.mPhoneBase.getPhoneId(), (Message) msg.obj);
                    this.mSetPreNwTypeRequested = IS_MULTI_SIM_ENABLED;
                    return;
                }
                Rlog.d(TAG, "No need to setPreferredNetworkType");
            case EVENT_RESUME_DATA /*203*/:
                this.mPhoneBase.mDcTracker.setInternalDataEnabled(true);
                Rlog.d(TAG, "EVENT_RESUME_DATA, resume data now.");
            default:
                Rlog.d(TAG, "Unhandled message with number: " + msg.what);
        }
    }
}
