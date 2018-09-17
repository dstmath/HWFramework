package huawei.com.android.internal.telephony;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.HwAllInOneController;
import com.android.internal.telephony.PhoneFactory;
import java.util.ArrayList;

public class RoamingBroker {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "GSM";
    public static final String PreviousIccId = "persist.radio.previousiccid";
    public static final String PreviousImsi = "persist.radio.previousimsi";
    public static final String PreviousOperator = "persist.radio.previousopcode";
    public static final String RBActivated = "gsm.RBActivated";
    private static final String RBActivated_flag_on = "true";
    private static ArrayList<RoamingBrokerSequence> mRBSequenceList = new ArrayList();
    private boolean isHaveSetData;
    private boolean isIccidSet;
    private boolean isImsiSet;
    private String mCurrentIccid;
    private String mCurrentImsi;
    private String mCurrentOp;
    private String mPreviousIccid;
    private String mPreviousOp;
    private int mSlotId;
    private String mVoicemail;

    private static class HelperHolder {
        private static RoamingBroker mRoamingBroker0 = new RoamingBroker();
        private static RoamingBroker mRoamingBroker1 = new RoamingBroker(1, null);

        private HelperHolder() {
        }
    }

    private static class RoamingBrokerSequence {
        static final int RBSequenceLength = 3;
        String before_rb_mccmnc;
        String name;
        String rb_mccmnc;
        String rb_voicemail;

        /* synthetic */ RoamingBrokerSequence(RoamingBrokerSequence -this0) {
            this();
        }

        private RoamingBrokerSequence() {
            this.name = "";
            this.before_rb_mccmnc = "";
            this.rb_mccmnc = "";
            this.rb_voicemail = "";
        }
    }

    /* synthetic */ RoamingBroker(int slotId, RoamingBroker -this1) {
        this(slotId);
    }

    private static void log(String text) {
        Log.d(LOG_TAG, "[RoamingBroker] " + text);
    }

    private static void loge(String text, Exception e) {
        Log.e(LOG_TAG, "[RoamingBroker] " + text + e);
    }

    private String printIccid(String iccid) {
        if (this.mPreviousIccid == null) {
            return "null";
        }
        if (this.mPreviousIccid.length() <= 6) {
            return "less than 6 digits";
        }
        return this.mPreviousIccid.substring(0, 6) + new String(new char[(this.mPreviousIccid.length() - 6)]).replace(0, '*');
    }

    private RoamingBroker() {
        this.mCurrentImsi = null;
        this.mSlotId = 0;
        this.mPreviousIccid = null;
        this.mPreviousOp = null;
        this.mCurrentIccid = null;
        this.mCurrentOp = null;
        this.mVoicemail = null;
        this.isHaveSetData = false;
        this.isIccidSet = false;
        this.isImsiSet = false;
        this.mPreviousOp = SystemProperties.get(PreviousOperator, "");
        log(String.format("Previously saved operator code is %s", new Object[]{this.mPreviousOp}));
        log("init state: " + PhoneFactory.getInitState());
        if (PhoneFactory.getInitState()) {
            this.mPreviousIccid = decryptInfo(PhoneFactory.getDefaultPhone().getContext(), PreviousIccId);
        } else {
            this.mPreviousIccid = "";
        }
        log(String.format("Previously saved Iccid is %s", new Object[]{printIccid(this.mPreviousIccid)}));
    }

    private void loadRBSequenceMap() {
        mRBSequenceList.clear();
        try {
            String data = System.getString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "roamingBrokerSequenceList");
            log("get raw RB list with voicemail" + data);
            if (data != null) {
                for (String s : data.split("\\|")) {
                    if (s != null) {
                        String[] tmp = s.split(",");
                        if (tmp.length >= 3) {
                            RoamingBrokerSequence rbs = new RoamingBrokerSequence();
                            rbs.name = tmp[0];
                            rbs.before_rb_mccmnc = tmp[1];
                            rbs.rb_mccmnc = tmp[2];
                            if (tmp.length > 3) {
                                rbs.rb_voicemail = tmp[3];
                            }
                            mRBSequenceList.add(rbs);
                        } else {
                            log("RB list contains invalid config: " + s);
                        }
                    }
                }
            }
        } catch (Exception e) {
            loge("Failed to load RB Sequence list. ", e);
        }
    }

    private void unloadRBSequenceMap() {
        mRBSequenceList.clear();
    }

    public static RoamingBroker getDefault() {
        return HelperHolder.mRoamingBroker0;
    }

    public void setOperator(String operatorCode) {
        log("received operatorCode of value: " + operatorCode);
        if (operatorCode != null && (operatorCode.equals(this.mCurrentOp) ^ 1) != 0) {
            this.mCurrentOp = operatorCode;
        }
    }

    public void setIccId(String IccId) {
        log("Previous Iccid value: " + printIccid(IccId));
        boolean bNeedClrIccid = false;
        this.isIccidSet = true;
        if (!(IccId == null || (IccId.equals(this.mCurrentIccid) ^ 1) == 0)) {
            this.mCurrentIccid = IccId;
            if (this.isHaveSetData) {
                setData();
                this.isHaveSetData = false;
                bNeedClrIccid = true;
            }
        }
        if (this.isIccidSet && this.isImsiSet) {
            log("check specfic Iccid for romaing broker state");
            if (checkSpecRBState()) {
                log("set roamingbroker state on and set PreviousOperator " + this.mPreviousOp);
                SystemProperties.set(RBActivated + this.mSlotId, RBActivated_flag_on);
                SystemProperties.set(PreviousOperator + this.mSlotId, this.mPreviousOp);
            }
            this.isIccidSet = false;
            this.isImsiSet = false;
        }
        if (bNeedClrIccid) {
            this.mCurrentIccid = null;
        }
    }

    private void setData() {
        if (this.mCurrentIccid != null && this.mCurrentOp != null && this.mCurrentImsi != null) {
            SystemProperties.set(RBActivated + this.mSlotId, "");
            Context context = PhoneFactory.getDefaultPhone().getContext();
            Editor editor = context.getSharedPreferences("imsi", 0).edit();
            this.isHaveSetData = false;
            if (this.mCurrentOp.equals(this.mPreviousOp)) {
                if (!this.mCurrentIccid.equals(this.mPreviousIccid)) {
                    encryptInfo(this.mCurrentIccid, context, PreviousIccId + this.mSlotId);
                    editor.putString(PreviousImsi + this.mSlotId, this.mCurrentImsi);
                    editor.commit();
                    this.mPreviousIccid = this.mCurrentIccid;
                    log(String.format("different sim card with same operatorCode %s. Set iccId: %s for roaming broker", new Object[]{this.mPreviousOp, printIccid(this.mPreviousIccid)}));
                }
            } else if (!this.mCurrentIccid.equals(this.mPreviousIccid)) {
                SystemProperties.set(PreviousOperator + this.mSlotId, this.mCurrentOp);
                encryptInfo(this.mCurrentIccid, context, PreviousIccId + this.mSlotId);
                editor.putString(PreviousImsi + this.mSlotId, this.mCurrentImsi);
                editor.commit();
                this.mPreviousIccid = this.mCurrentIccid;
                this.mPreviousOp = this.mCurrentOp;
                log(String.format("different sim card. Set operatorCode: %s, iccId: %s for roaming broker", new Object[]{this.mPreviousOp, printIccid(this.mPreviousIccid)}));
            } else if (isValidRBSequence()) {
                SystemProperties.set(RBActivated + this.mSlotId, RBActivated_flag_on);
            } else {
                SystemProperties.set(PreviousOperator + this.mSlotId, this.mCurrentOp);
                editor.putString(PreviousImsi + this.mSlotId, this.mCurrentImsi);
                editor.commit();
                this.mPreviousOp = this.mCurrentOp;
                log(String.format("same sim card. Set operatorCode: %s for roaming broker", new Object[]{this.mPreviousOp}));
            }
        }
    }

    private void encryptInfo(String sensitiveinfo, Context context, String encryptTag) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        try {
            sensitiveinfo = HwAESCryptoUtil.encrypt(HwAllInOneController.MASTER_PASSWORD, sensitiveinfo);
        } catch (Exception ex) {
            log("HwAESCryptoUtil encrypt excepiton:" + ex.getMessage());
        }
        editor.putString(encryptTag, sensitiveinfo);
        editor.apply();
    }

    private String decryptInfo(Context context, String encryptTag) {
        String sensitiveinfo = PreferenceManager.getDefaultSharedPreferences(context).getString(encryptTag, "");
        if ("".equals(sensitiveinfo)) {
            return sensitiveinfo;
        }
        try {
            return HwAESCryptoUtil.decrypt(HwAllInOneController.MASTER_PASSWORD, sensitiveinfo);
        } catch (Exception ex) {
            log("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
            return sensitiveinfo;
        }
    }

    private boolean checkSpecRBState() {
        boolean bRbActived = false;
        boolean bIccidMatch = false;
        if (isRoamingBrokerActivated(Integer.valueOf(this.mSlotId))) {
            return false;
        }
        if (this.mCurrentOp == null || this.mCurrentIccid == null) {
            return false;
        }
        try {
            log("checkSpecRBState mCurrentOp " + this.mCurrentOp);
            String specIccRBSeqList = System.getString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "specIccidRBSeqList");
            log("get raw RB list with specific Iccid " + specIccRBSeqList);
            if (TextUtils.isEmpty(specIccRBSeqList)) {
                return false;
            }
            String[] custArrays = specIccRBSeqList.trim().split(";");
            int i = 0;
            int length = custArrays.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                String[] items = custArrays[i2].split(":");
                if (items.length >= 2) {
                    if (this.mCurrentOp.equals(items[0])) {
                        for (String iccid : items[1].split(",")) {
                            if (this.mCurrentIccid.startsWith(iccid)) {
                                bIccidMatch = true;
                                break;
                            }
                        }
                        if (bIccidMatch) {
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                i = i2 + 1;
            }
            log("checkSpecRBState return IccidMatch " + bIccidMatch);
            if (!bIccidMatch) {
                return false;
            }
            loadRBSequenceMap();
            if (mRBSequenceList == null) {
                return false;
            }
            int list_size = mRBSequenceList.size();
            for (int i3 = 0; i3 < list_size; i3++) {
                RoamingBrokerSequence rbs = (RoamingBrokerSequence) mRBSequenceList.get(i3);
                if (rbs != null && rbs.rb_mccmnc != null && this.mCurrentOp.equals(rbs.rb_mccmnc)) {
                    bRbActived = true;
                    log("match spefic Iccid Rbcfg and rbSeqList together and set PreviousOp: " + rbs.before_rb_mccmnc + " and set PreviousIccid");
                    this.mVoicemail = rbs.rb_voicemail;
                    this.mPreviousOp = rbs.before_rb_mccmnc;
                    this.mPreviousIccid = this.mCurrentIccid;
                    break;
                }
            }
            unloadRBSequenceMap();
            return bRbActived;
        } catch (Exception e) {
            loge("Failed to load spefic iccid RB Sequence list.", e);
        }
    }

    private boolean isValidRBSequence() {
        boolean result = false;
        if (!(this.mPreviousOp == null || this.mCurrentOp == null)) {
            loadRBSequenceMap();
            if (mRBSequenceList != null) {
                int list_size = mRBSequenceList.size();
                for (int i = 0; i < list_size; i++) {
                    RoamingBrokerSequence rbs = (RoamingBrokerSequence) mRBSequenceList.get(i);
                    if (this.mCurrentOp.equals(rbs.rb_mccmnc) && this.mPreviousOp.equals(rbs.before_rb_mccmnc)) {
                        result = true;
                        this.mVoicemail = rbs.rb_voicemail;
                        log(rbs.name + " Roaming broker is activated");
                        break;
                    }
                }
                unloadRBSequenceMap();
            }
        }
        log("isValidRBSequence: " + result);
        return result;
    }

    public static boolean isRoamingBrokerActivated() {
        boolean result = RBActivated_flag_on.equals(SystemProperties.get("gsm.RBActivated0"));
        log("isRoamingBrokerActivated returns " + result);
        return result;
    }

    public static String updateSelectionForRoamingBroker(String selection) {
        log("updateSelection for: " + selection);
        String result = selection.replaceAll("numeric[ ]*=[ ]*[\"|']" + getDefault().mCurrentOp + "[\"|']", "numeric=\"" + getDefault().mPreviousOp + "\"");
        log("updated Selection: " + result);
        return result;
    }

    public static String getRBOperatorNumeric() {
        return getDefault().mPreviousOp;
    }

    public static String getRBVoicemail() {
        return getDefault().mVoicemail;
    }

    private RoamingBroker(int slotId) {
        this.mCurrentImsi = null;
        this.mSlotId = 0;
        this.mPreviousIccid = null;
        this.mPreviousOp = null;
        this.mCurrentIccid = null;
        this.mCurrentOp = null;
        this.mVoicemail = null;
        this.isHaveSetData = false;
        this.isIccidSet = false;
        this.isImsiSet = false;
        this.mSlotId = slotId;
        log("###RoamingBroker init,mSlotId = " + slotId);
        this.mPreviousOp = SystemProperties.get(PreviousOperator + slotId, "");
        log(String.format("Previously saved operator code is %s", new Object[]{this.mPreviousOp}));
        log("init state: " + PhoneFactory.getInitState());
        if (PhoneFactory.getInitState()) {
            this.mPreviousIccid = decryptInfo(PhoneFactory.getDefaultPhone().getContext(), PreviousIccId + slotId);
        } else {
            this.mPreviousIccid = "";
        }
        log(String.format("Previously saved Iccid is %s", new Object[]{printIccid(this.mPreviousIccid)}));
    }

    public static RoamingBroker getDefault(Integer slotId) {
        if (slotId.intValue() == 0) {
            return HelperHolder.mRoamingBroker0;
        }
        return HelperHolder.mRoamingBroker1;
    }

    public void setImsi(String Imsi) {
        this.isImsiSet = true;
        if (!(Imsi == null || (Imsi.equals(this.mCurrentImsi) ^ 1) == 0)) {
            this.mCurrentImsi = Imsi;
            this.isHaveSetData = true;
            setData();
        }
        if (this.isIccidSet && this.isImsiSet) {
            log("check specfic Iccid for romaing broker state");
            if (checkSpecRBState()) {
                log("set roamingbroker state on and set PreviousOperator " + this.mPreviousOp);
                SystemProperties.set(RBActivated + this.mSlotId, RBActivated_flag_on);
                SystemProperties.set(PreviousOperator + this.mSlotId, this.mPreviousOp);
            }
            this.isIccidSet = false;
            this.isImsiSet = false;
        }
    }

    public boolean isRoamingBrokerActivated(Integer slotId) {
        boolean result = RBActivated_flag_on.equals(SystemProperties.get(RBActivated + slotId));
        log("###isRoamingBrokerActivated returns " + result);
        return result;
    }

    public String updateSelectionForRoamingBroker(String selection, int slotId) {
        log("###updateSelection for: " + selection);
        String result = selection.replaceAll("numeric[ ]*=[ ]*[\"|']" + getDefault(Integer.valueOf(slotId)).mCurrentOp + "[\"|']", "numeric=\"" + getDefault(Integer.valueOf(slotId)).mPreviousOp + "\"");
        log("###updated Selection: " + result);
        return result;
    }

    public String getRBImsi() {
        return PhoneFactory.getDefaultPhone().getContext().getSharedPreferences("imsi", 0).getString(PreviousImsi + this.mSlotId, null);
    }

    public String getRBOperatorNumeric(Integer slotId) {
        return getDefault(slotId).mPreviousOp;
    }

    public String getRBVoicemail(Integer slotId) {
        return getDefault(slotId).mVoicemail;
    }
}
