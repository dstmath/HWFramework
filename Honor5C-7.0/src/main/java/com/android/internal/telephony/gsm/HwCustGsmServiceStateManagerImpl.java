package com.android.internal.telephony.gsm;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.OnsDisplayParams;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.HashSet;

public class HwCustGsmServiceStateManagerImpl extends HwCustGsmServiceStateManager {
    private static final String ACTION_RAC_CHANGED = "com.huawei.android.intent.action.RAC_CHANGED";
    private static final boolean HWDBG = true;
    private static final boolean IS_VIDEOTRON = false;
    private static final String LOG_TAG = "HwCustGsmServiceStateManagerImpl";
    private static final String[] NetWorkPlmnListATT = null;
    private static final String[] NetWorkPlmnListTMO = null;
    private static final boolean PS_CLEARCODE = false;
    private static final String RogersMccmnc = "302720";
    private static final boolean UseUSA_gsmroaming_cmp = false;
    private static final String VideotronMccmnc = "302500,302510,302520";
    private static final String[] roamingPlmnListATT = null;
    private static final String[] roamingPlmnListTMO = null;
    private boolean mIsActualRoaming;
    private int mOldRac;
    private int mRac;
    private HashSet<String> mShowNetnameMcc;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwCustGsmServiceStateManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwCustGsmServiceStateManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwCustGsmServiceStateManagerImpl.<clinit>():void");
    }

    public HwCustGsmServiceStateManagerImpl(ServiceStateTracker sst, GsmCdmaPhone gsmPhone) {
        super(sst, gsmPhone);
        this.mShowNetnameMcc = new HashSet();
        this.mIsActualRoaming = UseUSA_gsmroaming_cmp;
        this.mRac = -1;
        this.mOldRac = -1;
    }

    public boolean SetRoamingStateForOperatorCustomization(ServiceState currentState, boolean ParaRoamingState) {
        if (currentState == null || currentState.getState() != 0) {
            return ParaRoamingState;
        }
        String hplmn = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
        }
        String regplmn = currentState.getOperatorNumeric();
        log("Roaming customaziton for vendor hplmn = " + hplmn + "  regplmn=" + regplmn);
        if (hplmn == null || regplmn == null || 5 > regplmn.length()) {
            return ParaRoamingState;
        }
        if (("302490".equals(hplmn) || "22288".equals(hplmn) || "22201".equals(hplmn)) && "302".equals(regplmn.substring(0, 3))) {
            return UseUSA_gsmroaming_cmp;
        }
        return ParaRoamingState;
    }

    private void log(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public OnsDisplayParams SetOnsDisplayCustomization(OnsDisplayParams odp, ServiceState currentState) {
        OnsDisplayParams ons = odp;
        if (currentState != null && currentState.getState() == 0) {
            String str = null;
            String regplmn = currentState.getOperatorNumeric();
            String spnRes = odp.mSpn;
            IccRecords iccRecords = (IccRecords) this.mGsmPhone.mIccRecords.get();
            if (iccRecords != null) {
                str = iccRecords.getOperatorNumeric();
                if (!TextUtils.isEmpty(iccRecords.getServiceProviderName())) {
                    spnRes = iccRecords.getServiceProviderName();
                }
            }
            Rlog.d(LOG_TAG, "SetOnsDisplayCustomization for vendor hplmn = " + str + "  regplmn=" + regplmn + "  spn=" + spnRes);
            if ("65507".equals(str) && !TextUtils.isEmpty(regplmn) && !TextUtils.isEmpty(spnRes) && "FNB".equalsIgnoreCase(spnRes.trim())) {
                odp.mSpn = "FNB";
                odp.mShowSpn = HWDBG;
                odp.mShowPlmn = UseUSA_gsmroaming_cmp;
            }
            if ("20404".equals(str) && !TextUtils.isEmpty(spnRes) && "ziggo.dataxs.mob".equalsIgnoreCase(spnRes.trim())) {
                odp.mSpn = "Ziggo";
            }
            if (isShowNetnameByMcc(str, regplmn) && !TextUtils.isEmpty(currentState.getOperatorAlphaLong())) {
                odp.mPlmn = currentState.getOperatorAlphaLong();
                odp.mShowPlmn = HWDBG;
                odp.mShowSpn = UseUSA_gsmroaming_cmp;
            }
            if (!TextUtils.isEmpty(regplmn) && regplmn.equals(str) && isShowNetOnly(str)) {
                odp.mShowPlmn = HWDBG;
                odp.mShowSpn = UseUSA_gsmroaming_cmp;
            }
            if (isEplmnShowSpnPlus(str, regplmn, spnRes)) {
                if (!spnRes.contains("+")) {
                    odp.mSpn = spnRes + "+";
                }
                odp.mShowPlmn = UseUSA_gsmroaming_cmp;
                odp.mShowSpn = HWDBG;
            }
            if (isDualLinePlmn(str, regplmn, spnRes)) {
                if (str.equals(regplmn)) {
                    odp.mSpn = "o2 - de " + spnRes;
                } else {
                    odp.mSpn = "o2 - de+ " + spnRes;
                }
                odp.mShowPlmn = UseUSA_gsmroaming_cmp;
                odp.mShowSpn = HWDBG;
            }
        }
        return odp;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isShowNetnameByMcc(String hplmn, String regplmn) {
        if (TextUtils.isEmpty(hplmn) || hplmn.length() < 3 || TextUtils.isEmpty(regplmn) || regplmn.length() < 3) {
            return UseUSA_gsmroaming_cmp;
        }
        String hmcc = hplmn.substring(0, 3);
        String rmcc = regplmn.substring(0, 3);
        if (isMccForShowNetname(hmcc) && hmcc.equals(rmcc)) {
            return HWDBG;
        }
        return UseUSA_gsmroaming_cmp;
    }

    private boolean isMccForShowNetname(String currentMccmnc) {
        if (this.mShowNetnameMcc.size() == 0) {
            String strMcc = Systemex.getString(this.mContext.getContentResolver(), "hw_mcc_show_netname");
            if (strMcc != null) {
                String[] mcc = strMcc.split(",");
                for (String trim : mcc) {
                    this.mShowNetnameMcc.add(trim.trim());
                }
            }
        }
        return this.mShowNetnameMcc.contains(currentMccmnc);
    }

    public boolean notUseVirtualName(String mImsi) {
        if (TextUtils.isEmpty(mImsi)) {
            return UseUSA_gsmroaming_cmp;
        }
        String custPlmnsString = System.getString(this.mContext.getContentResolver(), "hw_notUseVirtualNetCust");
        if (!TextUtils.isEmpty(custPlmnsString)) {
            String[] custPlmns = custPlmnsString.split(";");
            for (String startsWith : custPlmns) {
                if (mImsi.startsWith(startsWith)) {
                    Rlog.d(LOG_TAG, "Imsi matched,did not use the virtualnets.xml name");
                    return HWDBG;
                }
            }
        }
        return UseUSA_gsmroaming_cmp;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isEplmnShowSpnPlus(String hplmn, String rplmn, String spn) {
        if (TextUtils.isEmpty(hplmn) || TextUtils.isEmpty(rplmn) || TextUtils.isEmpty(spn) || isDualLinePlmn(hplmn, rplmn, spn)) {
            return UseUSA_gsmroaming_cmp;
        }
        String custPlmnString = System.getString(this.mContext.getContentResolver(), "hw_eplmn_show_spnplus");
        if (!TextUtils.isEmpty(custPlmnString)) {
            String[] custEplmnArray = custPlmnString.split(";");
            for (String split : custEplmnArray) {
                String[] custEplmnArrayPlmns = split.split(",");
                if (!hplmn.equals(rplmn) && isContained(hplmn, custEplmnArrayPlmns) && isContained(rplmn, custEplmnArrayPlmns)) {
                    Rlog.d(LOG_TAG, "isEplmnShowSpnPlus hplmn:" + hplmn + "|rplmn:" + rplmn);
                    return HWDBG;
                }
            }
        }
        return UseUSA_gsmroaming_cmp;
    }

    private boolean isDualLinePlmn(String hplmn, String rplmn, String spn) {
        if (!"26207".equals(hplmn) || ((!"26207".equals(rplmn) && !"26203".equals(rplmn)) || (!"Private".equals(spn) && !"Business".equals(spn)))) {
            return UseUSA_gsmroaming_cmp;
        }
        Rlog.d(LOG_TAG, "isDualLinePlmn");
        return HWDBG;
    }

    private boolean isContained(String plmn, String[] plmnArray) {
        if (TextUtils.isEmpty(plmn) || plmnArray == null) {
            return UseUSA_gsmroaming_cmp;
        }
        for (String s : plmnArray) {
            if (plmn.equals(s)) {
                return HWDBG;
            }
        }
        return UseUSA_gsmroaming_cmp;
    }

    private boolean isShowNetOnly(String hplmn) {
        String custPlmnString = System.getString(this.mContext.getContentResolver(), "hw_show_netname_only");
        if (TextUtils.isEmpty(custPlmnString) || TextUtils.isEmpty(hplmn) || !isContained(hplmn, custPlmnString.split(","))) {
            return UseUSA_gsmroaming_cmp;
        }
        Rlog.d(LOG_TAG, "isShowNetOnly hplmn:" + hplmn);
        return HWDBG;
    }

    public void storeModemRoamingStatus(boolean roaming) {
        if (IS_VIDEOTRON) {
            this.mIsActualRoaming = roaming;
        }
    }

    public OnsDisplayParams getGsmOnsDisplayParamsForVideotron(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        OnsDisplayParams onsDisplayParams = null;
        if (!IS_VIDEOTRON) {
            return null;
        }
        if (!(this.mGsmPhone.mIccRecords.get() == null || this.mGsst.mSS == null)) {
            String hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumericEx(this.mCr, "hw_ons_hplmn_ex");
            String regplmn = this.mGsst.mSS.getOperatorNumeric();
            Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsForVideotron hplmn:" + hplmn + "|regplmn:" + regplmn);
            Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsForVideotron  mIsActualRoaming: " + this.mIsActualRoaming + " Roaming: " + this.mGsst.mSS.getRoaming());
            if (!(TextUtils.isEmpty(hplmn) || TextUtils.isEmpty(regplmn) || VideotronMccmnc.indexOf(hplmn) == -1 || !RogersMccmnc.equals(regplmn))) {
                showSpn = HWDBG;
                showPlmn = UseUSA_gsmroaming_cmp;
                rule = 1;
                if (this.mIsActualRoaming || this.mGsst.mSS.getRoaming()) {
                    spn = "Videotron PRTNR1";
                } else {
                    spn = "Videotron";
                }
                Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsForVideotron spn:" + spn + " showSpn :" + HWDBG + " rule:" + 1);
            }
            onsDisplayParams = new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
        }
        return onsDisplayParams;
    }

    public boolean checkIsInternationalRoaming(boolean roaming, ServiceState currentState) {
        boolean GsmRoaming = roaming;
        String SimNumeric = null;
        Rlog.d("GsmServiceStateTracker", "Before checkIsInternationalRoaming, roaming is " + roaming);
        if (currentState != null && UseUSA_gsmroaming_cmp && currentState.getState() == 0 && roaming) {
            if (this.mGsmPhone.mIccRecords.get() != null) {
                SimNumeric = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            }
            if (currentState.getOperatorNumeric() == null || SimNumeric == null) {
                Rlog.d("GsmServiceStateTracker", "Invalid operatorNumeric or SimNumeric");
                return roaming;
            } else if (isPlmnOfOperator(SimNumeric, NetWorkPlmnListATT)) {
                String actingHplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getActingHplmn();
                if (!(actingHplmn == null || "".equals(actingHplmn))) {
                    SimNumeric = actingHplmn;
                }
                if (isPlmnOfOperator(SimNumeric, NetWorkPlmnListATT) && isAmericanNetwork(currentState.getOperatorNumeric(), roamingPlmnListATT)) {
                    GsmRoaming = UseUSA_gsmroaming_cmp;
                }
            } else if (isPlmnOfOperator(SimNumeric, NetWorkPlmnListTMO) && isAmericanNetwork(currentState.getOperatorNumeric(), roamingPlmnListTMO)) {
                GsmRoaming = UseUSA_gsmroaming_cmp;
            }
        }
        Rlog.d("GsmServiceStateTracker", "After checkIsInternationalRoaming, roaming is " + GsmRoaming);
        return GsmRoaming;
    }

    public boolean isPlmnOfOperator(String plmn, String[] operatorPlmnList) {
        boolean isPlmnOfOperator = UseUSA_gsmroaming_cmp;
        if (plmn == null || operatorPlmnList == null) {
            return UseUSA_gsmroaming_cmp;
        }
        for (String opreatorPlmn : operatorPlmnList) {
            if (plmn.equals(opreatorPlmn)) {
                isPlmnOfOperator = HWDBG;
                break;
            }
        }
        return isPlmnOfOperator;
    }

    private boolean isAmericanNetwork(String plmn, String[] roamingPlmnList) {
        int i = 0;
        boolean isAmericanNetwork = UseUSA_gsmroaming_cmp;
        if (plmn == null || roamingPlmnList == null) {
            return UseUSA_gsmroaming_cmp;
        }
        if (5 <= plmn.length()) {
            int AmericanMCC = Integer.parseInt(plmn.substring(0, 3));
            if (AmericanMCC < 310 || AmericanMCC > 316) {
                isAmericanNetwork = UseUSA_gsmroaming_cmp;
            } else {
                isAmericanNetwork = HWDBG;
            }
        }
        if (isAmericanNetwork) {
            int length = roamingPlmnList.length;
            while (i < length) {
                if (plmn.equals(roamingPlmnList[i])) {
                    isAmericanNetwork = UseUSA_gsmroaming_cmp;
                    break;
                }
                i++;
            }
        }
        return isAmericanNetwork;
    }

    public IntentFilter getCustIntentFilter(IntentFilter filter) {
        if (PS_CLEARCODE) {
            filter.addAction(ACTION_RAC_CHANGED);
        }
        return filter;
    }

    public int handleBroadcastReceived(Context context, Intent intent, int rac) {
        if (!PS_CLEARCODE || intent == null || !intent.getAction().equals(ACTION_RAC_CHANGED)) {
            return rac;
        }
        this.mRac = ((Integer) intent.getExtra("rac", Integer.valueOf(-1))).intValue();
        if (this.mRac == -1 || this.mOldRac == this.mRac) {
            return rac;
        }
        log("CLEARCODE mRac = " + this.mRac + " , mOldRac = " + this.mOldRac);
        this.mOldRac = this.mRac;
        return this.mRac;
    }

    public boolean skipPlmnUpdateFromCust() {
        return SystemProperties.getBoolean("ro.config.display_pnn_name", UseUSA_gsmroaming_cmp);
    }
}
