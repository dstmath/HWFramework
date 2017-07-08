package com.android.internal.telephony.gsm;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.media.ToneGenerator;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.KeyEvent;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CsgSearch;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.uicc.IccRecords;

public class HwCustGsmServiceStateTrackerImpl extends HwCustGsmServiceStateTracker {
    public static final String ACTION_LTE_CA_STATE = "android.intent.action.LTE_CA_STATE";
    private static final int DIALOG_TIMEOUT = 120000;
    private static final int EVENT_GET_LTE_FREQ_WITH_WLAN_COEX = 63;
    protected static final int EVENT_POLL_STATE_REGISTRATION = 4;
    private static final int FOCUS_BEEP_VOLUME = 100;
    private static final boolean HW_ATT_SHOW_NET_REJ = false;
    private static final boolean IS_DATAONLY_LOCATION_ENABLED = false;
    private static final boolean IS_DELAY_UPDATENAME = false;
    private static final boolean IS_DELAY_UPDATENAME_LAC_NULL = false;
    private static final boolean IS_EMERGENCY_SHOWS_NOSERVICE = false;
    private static final boolean IS_SIM_POWER_DOWN = false;
    private static final String LOG_TAG = "HwCustGsmServiceStateTrackerImpl";
    private static final int MSG_ID_TIMEOUT = 1;
    private static final boolean UPDATE_LAC_CID = false;
    static final boolean VDBG = false;
    private static boolean mIsSupportCsgSearch;
    private int dialogCanceled;
    private boolean is_ext_plmn_sent;
    private CsgSearch mCsgSrch;
    private boolean mIsCaState;
    OnCancelListener mShowRejMsgOnCancelListener;
    OnKeyListener mShowRejMsgOnKeyListener;
    private String mSimRecordVoicemail;
    Handler mTimeoutHandler;
    private ToneGenerator mToneGenerator;
    private String mUlbwDlbwString;
    private AlertDialog networkDialog;
    private int oldRejCode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.<clinit>():void");
    }

    public HwCustGsmServiceStateTrackerImpl(GsmCdmaPhone gsmPhone) {
        super(gsmPhone);
        this.mSimRecordVoicemail = "";
        this.mToneGenerator = null;
        this.networkDialog = null;
        this.oldRejCode = 0;
        this.dialogCanceled = 0;
        this.is_ext_plmn_sent = IS_SIM_POWER_DOWN;
        this.mTimeoutHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwCustGsmServiceStateTrackerImpl.MSG_ID_TIMEOUT /*1*/:
                        if (HwCustGsmServiceStateTrackerImpl.this.dialogCanceled <= 0) {
                            if (HwCustGsmServiceStateTrackerImpl.this.networkDialog != null && HwCustGsmServiceStateTrackerImpl.this.dialogCanceled == 0) {
                                HwCustGsmServiceStateTrackerImpl.this.networkDialog.dismiss();
                                HwCustGsmServiceStateTrackerImpl.this.networkDialog = null;
                                HwCustGsmServiceStateTrackerImpl.this.mToneGenerator = null;
                                break;
                            }
                        }
                        HwCustGsmServiceStateTrackerImpl hwCustGsmServiceStateTrackerImpl = HwCustGsmServiceStateTrackerImpl.this;
                        hwCustGsmServiceStateTrackerImpl.dialogCanceled = hwCustGsmServiceStateTrackerImpl.dialogCanceled - 1;
                }
            }
        };
        this.mShowRejMsgOnCancelListener = new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                HwCustGsmServiceStateTrackerImpl.this.networkDialog = null;
                HwCustGsmServiceStateTrackerImpl.this.dialogCanceled = 0;
            }
        };
        this.mShowRejMsgOnKeyListener = new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (82 == keyCode || dialog == null) {
                    return HwCustGsmServiceStateTrackerImpl.IS_SIM_POWER_DOWN;
                }
                dialog.dismiss();
                HwCustGsmServiceStateTrackerImpl.this.networkDialog = null;
                HwCustGsmServiceStateTrackerImpl.this.mToneGenerator = null;
                HwCustGsmServiceStateTrackerImpl.this.dialogCanceled = 0;
                return true;
            }
        };
        if (CsgSearch.isSupportCsgSearch()) {
            this.mCsgSrch = new CsgSearch(gsmPhone);
        } else {
            this.mCsgSrch = null;
        }
    }

    public void updateRomingVoicemailNumber(ServiceState currentState) {
        Object custRoamingVoicemail = null;
        try {
            custRoamingVoicemail = Systemex.getString(this.mContext.getContentResolver(), "hw_cust_roamingvoicemail");
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception get hw_cust_roamingvoicemail value", e);
        }
        if (currentState != null && !TextUtils.isEmpty(custRoamingVoicemail)) {
            IccRecords mIccRecord = (IccRecords) this.mGsmPhone.mIccRecords.get();
            if (mIccRecord != null) {
                String hplmn = mIccRecord.getOperatorNumeric();
                String rplmn = currentState.getOperatorNumeric();
                String mVoicemailNum = mIccRecord.getVoiceMailNumber();
                String[] plmns = custRoamingVoicemail.split(",");
                if (!TextUtils.isEmpty(hplmn) && !TextUtils.isEmpty(rplmn) && plmns.length == 3 && !TextUtils.isEmpty(plmns[2])) {
                    if (hplmn.equals(plmns[0]) && rplmn.equals(plmns[MSG_ID_TIMEOUT])) {
                        if (!plmns[2].equals(mVoicemailNum)) {
                            this.mSimRecordVoicemail = mVoicemailNum;
                            mIccRecord.setVoiceMailNumber(plmns[2]);
                        }
                    } else if (!TextUtils.isEmpty(this.mSimRecordVoicemail) && plmns[2].equals(mVoicemailNum)) {
                        mIccRecord.setVoiceMailNumber(this.mSimRecordVoicemail);
                        this.mSimRecordVoicemail = "";
                    }
                }
            }
        }
    }

    public void setRadioPower(CommandsInterface ci, boolean enabled) {
        if (IS_SIM_POWER_DOWN && ci != null && this.mGsmPhone != null) {
            boolean bAirplaneMode = Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "airplane_mode_on", 0) == MSG_ID_TIMEOUT ? true : IS_SIM_POWER_DOWN;
            try {
                Rlog.d(LOG_TAG, "Set radio power: " + enabled + ", is airplane mode: " + bAirplaneMode);
                if (enabled) {
                    ci.setSimState(MSG_ID_TIMEOUT, MSG_ID_TIMEOUT, null);
                } else if (bAirplaneMode) {
                    ci.setSimState(MSG_ID_TIMEOUT, 0, null);
                }
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception in setRadioPower", e);
            }
        }
    }

    public String setEmergencyToNoService(ServiceState mSS, String plmn, boolean mEmergencyOnly) {
        if (IS_EMERGENCY_SHOWS_NOSERVICE && mSS.getRadioTechnology() == 14 && mEmergencyOnly) {
            return Resources.getSystem().getText(17040012).toString();
        }
        return plmn;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPsCell(ServiceState mSS, GsmCellLocation mNewCellLoc, String[] states) {
        if (!(mSS == null || mNewCellLoc == null || states == null || !IS_DATAONLY_LOCATION_ENABLED)) {
            boolean mCsOutOfservice = mSS.getVoiceRegState() == MSG_ID_TIMEOUT ? true : IS_SIM_POWER_DOWN;
            int Tac = -1;
            int Ci = -1;
            int Pci = -1;
            try {
                if (states.length >= 6) {
                    Ci = Integer.parseInt(states[5]);
                }
                if (states.length >= 7) {
                    Pci = Integer.parseInt(states[6]);
                }
                if (states.length >= 8) {
                    Tac = Integer.parseInt(states[7]);
                }
            } catch (NumberFormatException e) {
                Rlog.d(LOG_TAG, "error parsing GprsRegistrationState: ");
            }
            if (mCsOutOfservice && Tac >= 0 && Ci >= 0 && Pci >= 0) {
                Rlog.d(LOG_TAG, "data only card use ps cellid to location");
                mNewCellLoc.setLacAndCid(Tac, Ci);
                mNewCellLoc.setPsc(Pci);
            }
        }
    }

    public boolean isInServiceState(int combinedregstate) {
        return (HW_ATT_SHOW_NET_REJ && this.is_ext_plmn_sent && combinedregstate == 0) ? true : IS_SIM_POWER_DOWN;
    }

    public boolean isInServiceState(ServiceState ss) {
        return isInServiceState(getCombinedRegState(ss));
    }

    public void setExtPlmnSent(boolean value) {
        if (HW_ATT_SHOW_NET_REJ) {
            this.is_ext_plmn_sent = value;
        }
    }

    public void custHandlePollStateResult(int what, AsyncResult ar, int[] pollingContext) {
        if (HW_ATT_SHOW_NET_REJ && ar.userObj != pollingContext && ar.exception == null && EVENT_POLL_STATE_REGISTRATION == what) {
            try {
                String[] states = ar.result;
                if (states != null && states.length > 13) {
                    handleNetworkRejection(Integer.parseInt(states[13]));
                }
            } catch (RuntimeException e) {
            }
        }
    }

    public void handleNetworkRejection(int regState, String[] states) {
        if (HW_ATT_SHOW_NET_REJ && states == null) {
            Rlog.d(LOG_TAG, "States is null.");
            return;
        }
        if ((regState == 3 || regState == 13) && states.length >= 14) {
            try {
                handleNetworkRejection(Integer.parseInt(states[13]));
            } catch (NumberFormatException ex) {
                Rlog.e(LOG_TAG, "error parsing regCode: " + ex);
            }
        }
    }

    private void showDialog(String msg, int regCode) {
        if (regCode != this.oldRejCode) {
            if (this.networkDialog != null) {
                this.dialogCanceled += MSG_ID_TIMEOUT;
                this.networkDialog.dismiss();
            }
            this.networkDialog = new Builder(this.mGsmPhone.getContext()).setMessage(msg).setCancelable(true).create();
            this.networkDialog.getWindow().setType(2008);
            this.networkDialog.setOnKeyListener(this.mShowRejMsgOnKeyListener);
            this.networkDialog.setOnCancelListener(this.mShowRejMsgOnCancelListener);
            this.networkDialog.show();
            this.mTimeoutHandler.sendMessageDelayed(this.mTimeoutHandler.obtainMessage(MSG_ID_TIMEOUT), 120000);
            try {
                this.mToneGenerator = new ToneGenerator(MSG_ID_TIMEOUT, FOCUS_BEEP_VOLUME);
                this.mToneGenerator.startTone(28);
            } catch (RuntimeException e) {
                this.mToneGenerator = null;
            }
        }
    }

    private int getCombinedRegState(ServiceState ss) {
        if (ss == null) {
            return MSG_ID_TIMEOUT;
        }
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        if (regState == MSG_ID_TIMEOUT && dataRegState == 0) {
            Rlog.e(LOG_TAG, "getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
            regState = dataRegState;
        }
        return regState;
    }

    private void handleNetworkRejection(int rejCode) {
        Resources r = Resources.getSystem();
        String plmn = r.getText(17040036).toString();
        switch (rejCode) {
            case CSGNetworkList.NAS_SCAN_REJ_IN_RLF /*2*/:
                showDialog(r.getString(33685923), rejCode);
                handleShowLimitedService(plmn);
                break;
            case 3:
                showDialog(r.getString(33685924), rejCode);
                handleShowLimitedService(plmn);
                break;
            case 6:
                showDialog(r.getString(33685925), rejCode);
                handleShowLimitedService(plmn);
                break;
            case 11:
            case 12:
            case 13:
            case 15:
            case 17:
                handleShowLimitedService(" ");
                break;
        }
        this.oldRejCode = rejCode;
    }

    private void handleShowLimitedService(String plmn) {
        Intent intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
        intent.putExtra("showSpn", IS_SIM_POWER_DOWN);
        intent.putExtra("spn", "");
        intent.putExtra("showPlmn", true);
        intent.putExtra("plmn", plmn);
        this.mGsmPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        this.is_ext_plmn_sent = true;
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        if (this.mCsgSrch != null && mIsSupportCsgSearch) {
            this.mCsgSrch.judgeToLaunchCsgPeriodicSearchTimer();
        }
    }

    public boolean isStopUpdateName(boolean SimCardLoaded) {
        Rlog.d(LOG_TAG, " isStopUpdateName: SimCardLoaded = " + SimCardLoaded + ", IS_DELAY_UPDATENAME = " + IS_DELAY_UPDATENAME);
        if (!IS_DELAY_UPDATENAME && !IS_DELAY_UPDATENAME_LAC_NULL) {
            return IS_SIM_POWER_DOWN;
        }
        if ((SimCardLoaded || !IS_DELAY_UPDATENAME) && (this.mGsmPhone == null || this.mGsmPhone.mSST == null || ((this.mGsmPhone.mSST.mCellLoc != null && ((GsmCellLocation) this.mGsmPhone.mSST.mCellLoc).getLac() != -1) || !IS_DELAY_UPDATENAME_LAC_NULL))) {
            return IS_SIM_POWER_DOWN;
        }
        return true;
    }

    public boolean isUpdateLacAndCidCust(ServiceStateTracker sst) {
        Rlog.d(LOG_TAG, "isUpdateLacAndCidCust: Update Lac and Cid when cid is 0, ServiceStateTracker = " + sst + ",UPDATE_LAC_CID = " + UPDATE_LAC_CID);
        return UPDATE_LAC_CID;
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_GET_LTE_FREQ_WITH_WLAN_COEX /*63*/:
                Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX");
                handleGetLteFreqWithWlanCoex((AsyncResult) msg.obj);
                return true;
            default:
                return IS_SIM_POWER_DOWN;
        }
    }

    private void handleGetLteFreqWithWlanCoex(AsyncResult ar) {
        if (!(ar == null || ar.exception != null || this.mGsmPhone == null)) {
            int[] result = ar.result;
            if (result == null) {
                Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX  result is null");
                return;
            }
            int ulbw = result[2];
            int dlbw = result[EVENT_POLL_STATE_REGISTRATION];
            if (!TextUtils.isEmpty(this.mUlbwDlbwString)) {
                String[] ulbwDlbw = this.mUlbwDlbwString.trim().split(";");
                Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX  ulbw =" + ulbw + ", dlbw =" + dlbw + "mUlbwDlbwString =" + this.mUlbwDlbwString);
                boolean isHasCAState = (ulbw <= Integer.parseInt(ulbwDlbw[0]) || dlbw <= Integer.parseInt(ulbwDlbw[MSG_ID_TIMEOUT])) ? IS_SIM_POWER_DOWN : true;
                if (this.mIsCaState != isHasCAState) {
                    this.mIsCaState = isHasCAState;
                    Intent intent = new Intent(ACTION_LTE_CA_STATE);
                    intent.putExtra("subscription", this.mGsmPhone.getSubId());
                    intent.putExtra("LteCAstate", isHasCAState);
                    this.mGsmPhone.getContext().sendBroadcast(intent);
                    Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX  mIsCaState =" + this.mIsCaState);
                }
            }
        }
    }

    public void getLteFreqWithWlanCoex(CommandsInterface ci, ServiceStateTracker sst) {
        if (this.mContext == null || ci == null || sst == null) {
            Rlog.d(LOG_TAG, "getLteFreqWithWlanCoex  error !");
            return;
        }
        try {
            this.mUlbwDlbwString = System.getString(this.mContext.getContentResolver(), "hw_query_lwclash");
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception get hw_query_lwclash value", e);
        }
        Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX  mUlbwDlbwString =" + this.mUlbwDlbwString);
        if (!TextUtils.isEmpty(this.mUlbwDlbwString)) {
            ci.getLteFreqWithWlanCoex(sst.obtainMessage(EVENT_GET_LTE_FREQ_WITH_WLAN_COEX));
        }
    }
}
