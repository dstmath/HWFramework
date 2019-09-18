package huawei.com.android.internal.telephony;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import java.util.ArrayList;

public class RoamingBroker {
    private static final boolean DBG = true;
    private static final String IMSI_SAVE_FILE_NAME = "imsi";
    private static final String LOG_TAG = "GSM";
    public static final String PreviousIccId = "persist.radio.previousiccid";
    public static final String PreviousImsi = "persist.radio.previousimsi";
    public static final String PreviousOperator = "persist.radio.previousopcode";
    public static final String RBActivated = "gsm.RBActivated";
    private static final String RBActivated_flag_on = "true";
    private static final int SRB_CONFIG_HPLMN_INDEX = 2;
    private static final int SRB_CONFIG_LENGTH = 3;
    private static ArrayList<RoamingBrokerSequence> mRBSequenceList = new ArrayList<>();
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
        /* access modifiers changed from: private */
        public static RoamingBroker mRoamingBroker0 = new RoamingBroker(0);
        /* access modifiers changed from: private */
        public static RoamingBroker mRoamingBroker1 = new RoamingBroker(1);

        private HelperHolder() {
        }
    }

    private static class RoamingBrokerSequence {
        static final int RBSequenceLength = 3;
        String before_rb_mccmnc;
        String name;
        String rb_mccmnc;
        String rb_voicemail;

        private RoamingBrokerSequence() {
            this.name = "";
            this.before_rb_mccmnc = "";
            this.rb_mccmnc = "";
            this.rb_voicemail = "";
        }
    }

    private static void log(String text) {
        Log.d(LOG_TAG, "[RoamingBroker] " + text);
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
            String data = Settings.System.getString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "roamingBrokerSequenceList");
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
            log("Failed to load RB Sequence list.");
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
        if (operatorCode != null && !operatorCode.equals(this.mCurrentOp)) {
            this.mCurrentOp = operatorCode;
        }
    }

    public void setIccId(String IccId) {
        log("Previous Iccid value: " + printIccid(IccId));
        boolean bNeedClrIccid = false;
        this.isIccidSet = true;
        if (IccId != null && !IccId.equals(this.mCurrentIccid)) {
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
            this.isHaveSetData = false;
            if (!this.mCurrentOp.equals(this.mPreviousOp)) {
                if (!this.mCurrentIccid.equals(this.mPreviousIccid)) {
                    SystemProperties.set(PreviousOperator + this.mSlotId, this.mCurrentOp);
                    encryptInfo(context, PreviousIccId + this.mSlotId, this.mCurrentIccid);
                    encryptImsi(context, PreviousImsi + this.mSlotId, this.mCurrentImsi);
                    this.mPreviousIccid = this.mCurrentIccid;
                    this.mPreviousOp = this.mCurrentOp;
                    log(String.format("different sim card. Set operatorCode: %s, iccId: %s for roaming broker", new Object[]{this.mPreviousOp, printIccid(this.mPreviousIccid)}));
                } else if (!isValidRBSequence()) {
                    SystemProperties.set(PreviousOperator + this.mSlotId, this.mCurrentOp);
                    encryptImsi(context, PreviousImsi + this.mSlotId, this.mCurrentImsi);
                    this.mPreviousOp = this.mCurrentOp;
                    log(String.format("same sim card. Set operatorCode: %s for roaming broker", new Object[]{this.mPreviousOp}));
                } else {
                    SystemProperties.set(RBActivated + this.mSlotId, RBActivated_flag_on);
                }
            } else if (!this.mCurrentIccid.equals(this.mPreviousIccid)) {
                encryptInfo(context, PreviousIccId + this.mSlotId, this.mCurrentIccid);
                encryptImsi(context, PreviousImsi + this.mSlotId, this.mCurrentImsi);
                this.mPreviousIccid = this.mCurrentIccid;
                log(String.format("different sim card with same operatorCode %s. Set iccId: %s for roaming broker", new Object[]{this.mPreviousOp, printIccid(this.mPreviousIccid)}));
            }
        }
    }

    private void encryptInfo(Context context, String encryptTag, String sensitiveInfo) {
        encryptInfo(PreferenceManager.getDefaultSharedPreferences(context), encryptTag, sensitiveInfo);
    }

    private void encryptImsi(Context context, String encryptTag, String sensitiveInfo) {
        encryptInfo(context.getSharedPreferences("imsi", 0), encryptTag, sensitiveInfo);
    }

    private void encryptInfo(SharedPreferences sp, String encryptTag, String sensitiveInfo) {
        SharedPreferences.Editor editor = sp.edit();
        try {
            sensitiveInfo = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, sensitiveInfo);
        } catch (Exception e) {
            log("HwAESCryptoUtil encryptInfo excepiton");
        }
        editor.putString(encryptTag, sensitiveInfo);
        editor.apply();
    }

    private String decryptInfo(Context context, String encryptTag) {
        return decryptInfo(PreferenceManager.getDefaultSharedPreferences(context), encryptTag);
    }

    private String decryptImsi(Context context, String encryptTag) {
        return decryptInfo(context.getSharedPreferences("imsi", 0), encryptTag);
    }

    private String decryptInfo(SharedPreferences sp, String encryptTag) {
        String sensitiveInfo = sp.getString(encryptTag, "");
        if ("".equals(sensitiveInfo)) {
            return sensitiveInfo;
        }
        try {
            return HwAESCryptoUtil.decrypt(HwFullNetworkConstants.MASTER_PASSWORD, sensitiveInfo);
        } catch (Exception e) {
            log("HwAESCryptoUtil decryptInfo excepiton");
            return sensitiveInfo;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ae, code lost:
        if (3 > r10.length) goto L_0x00b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b3, code lost:
        r6 = r10[2];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b4, code lost:
        r3 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00b7, code lost:
        r3 = r4;
     */
    private boolean checkSpecRBState() {
        boolean bIccidMatch;
        RoamingBrokerSequence rbs;
        boolean bIccidMatch2;
        boolean bRbActived = false;
        int i = 0;
        if (isRoamingBrokerActivated(Integer.valueOf(this.mSlotId))) {
            return false;
        }
        if (this.mCurrentOp == null || this.mCurrentIccid == null) {
            return false;
        }
        try {
            log("checkSpecRBState mCurrentOp " + this.mCurrentOp);
            String specIccRBSeqList = Settings.System.getString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "specIccidRBSeqList");
            log("get raw RB list with specific Iccid " + specIccRBSeqList);
            if (TextUtils.isEmpty(specIccRBSeqList)) {
                return false;
            }
            String[] custArrays = specIccRBSeqList.trim().split(";");
            String hplmn = null;
            int length = custArrays.length;
            boolean bIccidMatch3 = false;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    bIccidMatch = bIccidMatch3;
                    break;
                }
                try {
                    String[] items = custArrays[i2].split(":");
                    if (items.length >= 2) {
                        if (this.mCurrentOp.equals(items[i])) {
                            String[] iccids = items[1].split(",");
                            int length2 = iccids.length;
                            int i3 = i;
                            while (true) {
                                if (i3 >= length2) {
                                    bIccidMatch2 = bIccidMatch3;
                                    break;
                                } else if (this.mCurrentIccid.startsWith(iccids[i3])) {
                                    bIccidMatch2 = true;
                                    break;
                                } else {
                                    i3++;
                                }
                            }
                            if (bIccidMatch2) {
                                break;
                            }
                            bIccidMatch3 = bIccidMatch2;
                        }
                    }
                    i2++;
                    i = 0;
                } catch (Exception e) {
                    boolean z = bIccidMatch3;
                    log("Failed to load spefic iccid RB Sequence list.");
                    return bRbActived;
                }
            }
            log("checkSpecRBState return IccidMatch " + bIccidMatch);
            if (!bIccidMatch) {
                return false;
            }
            loadRBSequenceMap();
            if (mRBSequenceList == null) {
                return false;
            }
            int i4 = 0;
            int list_size = mRBSequenceList.size();
            while (true) {
                if (i4 >= list_size) {
                    break;
                }
                rbs = mRBSequenceList.get(i4);
                if (rbs != null) {
                    if (rbs.rb_mccmnc != null) {
                        if (this.mCurrentOp.equals(rbs.rb_mccmnc)) {
                            if (hplmn == null || hplmn.equals(rbs.before_rb_mccmnc)) {
                                bRbActived = true;
                                log("match spefic Iccid Rbcfg and rbSeqList together and set PreviousOp: " + rbs.before_rb_mccmnc + " and set PreviousIccid");
                                this.mVoicemail = rbs.rb_voicemail;
                                this.mPreviousOp = rbs.before_rb_mccmnc;
                                this.mPreviousIccid = this.mCurrentIccid;
                            }
                        }
                    }
                }
                i4++;
            }
            bRbActived = true;
            log("match spefic Iccid Rbcfg and rbSeqList together and set PreviousOp: " + rbs.before_rb_mccmnc + " and set PreviousIccid");
            this.mVoicemail = rbs.rb_voicemail;
            this.mPreviousOp = rbs.before_rb_mccmnc;
            this.mPreviousIccid = this.mCurrentIccid;
            unloadRBSequenceMap();
            return bRbActived;
        } catch (Exception e2) {
            log("Failed to load spefic iccid RB Sequence list.");
            return bRbActived;
        }
    }

    private boolean isValidRBSequence() {
        boolean result = false;
        if (!(this.mPreviousOp == null || this.mCurrentOp == null)) {
            loadRBSequenceMap();
            if (mRBSequenceList != null) {
                int i = 0;
                int list_size = mRBSequenceList.size();
                while (true) {
                    if (i >= list_size) {
                        break;
                    }
                    RoamingBrokerSequence rbs = mRBSequenceList.get(i);
                    if (this.mCurrentOp.equals(rbs.rb_mccmnc) && this.mPreviousOp.equals(rbs.before_rb_mccmnc)) {
                        result = true;
                        this.mVoicemail = rbs.rb_voicemail;
                        log(rbs.name + " Roaming broker is activated");
                        break;
                    }
                    i++;
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
        StringBuilder sb = new StringBuilder();
        sb.append("updated Selection: ");
        sb.append(result);
        log(sb.toString());
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
            Context context = PhoneFactory.getDefaultPhone().getContext();
            this.mPreviousIccid = decryptInfo(context, PreviousIccId + slotId);
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
        if (Imsi != null && !Imsi.equals(this.mCurrentImsi)) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("###updated Selection: ");
        sb.append(result);
        log(sb.toString());
        return result;
    }

    public String getRBImsi() {
        Context context = PhoneFactory.getDefaultPhone().getContext();
        return decryptImsi(context, PreviousImsi + this.mSlotId);
    }

    public String getRBOperatorNumeric(Integer slotId) {
        return getDefault(slotId).mPreviousOp;
    }

    public String getRBVoicemail(Integer slotId) {
        return getDefault(slotId).mVoicemail;
    }
}
