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
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import java.security.NoSuchProviderException;
import java.util.ArrayList;

public class RoamingBroker {
    private static final boolean DBG = true;
    private static final String IMSI_SAVE_FILE_NAME = "imsi";
    private static final String LOG_TAG = "GSM";
    public static final String PREVIOUS_ICCID = "persist.radio.previousiccid";
    public static final String PREVIOUS_IMSI = "persist.radio.previousimsi";
    public static final String PREVIOUS_OPERATOR = "persist.radio.previousopcode";
    public static final String RB_ACTIVATED = "gsm.RBActivated";
    private static final String RB_ACTIVATED_GLAG_ON = "true";
    private static final int SRB_CONFIG_HPLMN_INDEX = 2;
    private static final int SRB_CONFIG_LENGTH = 3;
    private static ArrayList<RoamingBrokerSequence> mRbSequenceList = new ArrayList<>();
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

    /* access modifiers changed from: private */
    public static class HelperHolder {
        private static RoamingBroker mRoamingBroker0 = new RoamingBroker(0);
        private static RoamingBroker mRoamingBroker1 = new RoamingBroker(1);

        private HelperHolder() {
        }
    }

    /* access modifiers changed from: private */
    public static class RoamingBrokerSequence {
        static final int RB_SEQUENCE_LENGTH = 3;
        String beforeRbMccMnc;
        String name;
        String rbMccMnc;
        String rbVoiceMail;

        private RoamingBrokerSequence() {
            this.name = "";
            this.beforeRbMccMnc = "";
            this.rbMccMnc = "";
            this.rbVoiceMail = "";
        }
    }

    private static void log(String text) {
        Log.d(LOG_TAG, "[RoamingBroker] " + text);
    }

    private String printIccid(String iccid) {
        String str = this.mPreviousIccid;
        if (str == null) {
            return "null";
        }
        if (str.length() <= 6) {
            return "less than 6 digits";
        }
        return this.mPreviousIccid.substring(0, 6) + new String(new char[(this.mPreviousIccid.length() - 6)]).replace((char) 0, '*');
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
        this.mPreviousOp = SystemProperties.get(PREVIOUS_OPERATOR, "");
        log(String.format("Previously saved operator code is %s", this.mPreviousOp));
        log("init state: " + PhoneFactory.getInitState());
        if (PhoneFactory.getInitState()) {
            this.mPreviousIccid = decryptInfo(PhoneFactory.getDefaultPhone().getContext(), PREVIOUS_ICCID);
        } else {
            this.mPreviousIccid = "";
        }
        log(String.format("Previously saved Iccid is %s", printIccid(this.mPreviousIccid)));
    }

    private void loadRbSequenceMap() {
        mRbSequenceList.clear();
        try {
            String data = Settings.System.getString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "roamingBrokerSequenceList");
            log("get raw RB list with voicemail" + data);
            if (data != null) {
                String[] buffer = data.split("\\|");
                for (String s : buffer) {
                    if (s != null) {
                        String[] tmp = s.split(",");
                        if (tmp.length < 3) {
                            log("RB list contains invalid config: " + s);
                        } else {
                            RoamingBrokerSequence rbs = new RoamingBrokerSequence();
                            rbs.name = tmp[0];
                            rbs.beforeRbMccMnc = tmp[1];
                            rbs.rbMccMnc = tmp[2];
                            if (tmp.length > 3) {
                                rbs.rbVoiceMail = tmp[3];
                            }
                            mRbSequenceList.add(rbs);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            log("Failed to load RB Sequence list IllegalArgumentException.");
        } catch (Exception e2) {
            log("Failed to load RB Sequence list.");
        }
    }

    private void unloadRbSequenceMap() {
        mRbSequenceList.clear();
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
            if (checkSpecRbState()) {
                log("set roamingbroker state on and set PREVIOUS_OPERATOR " + this.mPreviousOp);
                SystemProperties.set(RB_ACTIVATED + this.mSlotId, RB_ACTIVATED_GLAG_ON);
                SystemProperties.set(PREVIOUS_OPERATOR + this.mSlotId, this.mPreviousOp);
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
            SystemProperties.set(RB_ACTIVATED + this.mSlotId, "");
            Context context = PhoneFactory.getDefaultPhone().getContext();
            this.isHaveSetData = false;
            if (!this.mCurrentOp.equals(this.mPreviousOp)) {
                setDataByDifferentOp(context);
            } else if (!this.mCurrentIccid.equals(this.mPreviousIccid)) {
                encryptInfo(context, PREVIOUS_ICCID + this.mSlotId, this.mCurrentIccid);
                encryptImsi(context, PREVIOUS_IMSI + this.mSlotId, this.mCurrentImsi);
                this.mPreviousIccid = this.mCurrentIccid;
                log(String.format("different sim card with same operatorCode %s. Set iccId: %s for roaming broker", this.mPreviousOp, printIccid(this.mPreviousIccid)));
            }
        }
    }

    private void setDataByDifferentOp(Context context) {
        if (!this.mCurrentIccid.equals(this.mPreviousIccid)) {
            SystemProperties.set(PREVIOUS_OPERATOR + this.mSlotId, this.mCurrentOp);
            encryptInfo(context, PREVIOUS_ICCID + this.mSlotId, this.mCurrentIccid);
            encryptImsi(context, PREVIOUS_IMSI + this.mSlotId, this.mCurrentImsi);
            this.mPreviousIccid = this.mCurrentIccid;
            this.mPreviousOp = this.mCurrentOp;
            log(String.format("different sim card. Set operatorCode: %s, iccId: %s for roaming broker", this.mPreviousOp, printIccid(this.mPreviousIccid)));
        } else if (!isValidRbSequence()) {
            SystemProperties.set(PREVIOUS_OPERATOR + this.mSlotId, this.mCurrentOp);
            encryptImsi(context, PREVIOUS_IMSI + this.mSlotId, this.mCurrentImsi);
            this.mPreviousOp = this.mCurrentOp;
            log(String.format("same sim card. Set operatorCode: %s for roaming broker", this.mPreviousOp));
        } else {
            SystemProperties.set(RB_ACTIVATED + this.mSlotId, RB_ACTIVATED_GLAG_ON);
        }
    }

    private void encryptInfo(Context context, String encryptTag, String sensitiveInfo) {
        encryptInfo(PreferenceManager.getDefaultSharedPreferences(context), encryptTag, sensitiveInfo);
    }

    private void encryptImsi(Context context, String encryptTag, String sensitiveInfo) {
        encryptInfo(context.getSharedPreferences(IMSI_SAVE_FILE_NAME, 0), encryptTag, sensitiveInfo);
    }

    private void encryptInfo(SharedPreferences sp, String encryptTag, String sensitiveInfo) {
        SharedPreferences.Editor editor = sp.edit();
        try {
            sensitiveInfo = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), sensitiveInfo);
        } catch (NoSuchProviderException e) {
            log("HwAESCryptoUtil encryptInfo NoSuchProviderException");
        } catch (Exception e2) {
            log("HwAESCryptoUtil encryptInfo excepiton");
        }
        editor.putString(encryptTag, sensitiveInfo);
        editor.apply();
    }

    private String decryptInfo(Context context, String encryptTag) {
        return decryptInfo(PreferenceManager.getDefaultSharedPreferences(context), encryptTag);
    }

    private String decryptImsi(Context context, String encryptTag) {
        return decryptInfo(context.getSharedPreferences(IMSI_SAVE_FILE_NAME, 0), encryptTag);
    }

    private String decryptInfo(SharedPreferences sp, String encryptTag) {
        String sensitiveInfo = sp.getString(encryptTag, "");
        if ("".equals(sensitiveInfo)) {
            return sensitiveInfo;
        }
        try {
            return HwAESCryptoUtil.decrypt(HwFullNetworkManager.getInstance().getMasterPassword(), sensitiveInfo);
        } catch (NoSuchProviderException e) {
            log("HwAESCryptoUtil decryptInfo NoSuchProviderException");
            return sensitiveInfo;
        } catch (Exception e2) {
            log("HwAESCryptoUtil decryptInfo excepiton");
            return sensitiveInfo;
        }
    }

    private boolean checkSpecRbState() {
        RoamingBrokerSequence rbs;
        boolean bRbActived = false;
        int i = 0;
        if (isRoamingBrokerActivated(Integer.valueOf(this.mSlotId)) || this.mCurrentOp == null || this.mCurrentIccid == null) {
            return false;
        }
        try {
            log("checkSpecRbState mCurrentOp " + this.mCurrentOp);
            String specIccRBSeqList = Settings.System.getString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "specIccidRBSeqList");
            log("get raw RB list with specific Iccid " + specIccRBSeqList);
            if (TextUtils.isEmpty(specIccRBSeqList)) {
                return false;
            }
            String[] custArrays = specIccRBSeqList.trim().split(";");
            String hplmn = null;
            int length = custArrays.length;
            boolean bIccidMatch = false;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    break;
                }
                String[] items = custArrays[i2].split(":");
                if (items.length >= 2) {
                    if (this.mCurrentOp.equals(items[i])) {
                        String[] iccids = items[1].split(",");
                        int length2 = iccids.length;
                        int i3 = i;
                        while (true) {
                            if (i3 >= length2) {
                                break;
                            }
                            if (this.mCurrentIccid.startsWith(iccids[i3])) {
                                bIccidMatch = true;
                                break;
                            }
                            i3++;
                        }
                        if (bIccidMatch) {
                            if (items.length >= 3) {
                                hplmn = items[2];
                            }
                        }
                    }
                }
                i2++;
                i = 0;
            }
            log("checkSpecRbState return IccidMatch " + bIccidMatch);
            if (!bIccidMatch) {
                return false;
            }
            loadRbSequenceMap();
            if (mRbSequenceList == null) {
                return false;
            }
            int i4 = 0;
            int listSize = mRbSequenceList.size();
            while (true) {
                if (i4 >= listSize) {
                    break;
                }
                rbs = mRbSequenceList.get(i4);
                if (rbs != null) {
                    if (rbs.rbMccMnc != null) {
                        if (this.mCurrentOp.equals(rbs.rbMccMnc)) {
                            if (hplmn == null || hplmn.equals(rbs.beforeRbMccMnc)) {
                                bRbActived = true;
                                log("match spefic Iccid Rbcfg and rbSeqList together and set PreviousOp: " + rbs.beforeRbMccMnc + " and set PreviousIccid");
                                this.mVoicemail = rbs.rbVoiceMail;
                                this.mPreviousOp = rbs.beforeRbMccMnc;
                                this.mPreviousIccid = this.mCurrentIccid;
                            }
                        }
                    }
                }
                i4++;
            }
            bRbActived = true;
            log("match spefic Iccid Rbcfg and rbSeqList together and set PreviousOp: " + rbs.beforeRbMccMnc + " and set PreviousIccid");
            this.mVoicemail = rbs.rbVoiceMail;
            this.mPreviousOp = rbs.beforeRbMccMnc;
            this.mPreviousIccid = this.mCurrentIccid;
            unloadRbSequenceMap();
            return bRbActived;
        } catch (Exception e) {
            log("Failed to load spefic iccid RB Sequence list.");
        }
    }

    private boolean isValidRbSequence() {
        boolean result = false;
        if (this.mPreviousOp == null || this.mCurrentOp == null) {
            return false;
        }
        loadRbSequenceMap();
        ArrayList<RoamingBrokerSequence> arrayList = mRbSequenceList;
        if (arrayList != null) {
            int i = 0;
            int listSize = arrayList.size();
            while (true) {
                if (i >= listSize) {
                    break;
                }
                RoamingBrokerSequence rbs = mRbSequenceList.get(i);
                if (this.mCurrentOp.equals(rbs.rbMccMnc) && this.mPreviousOp.equals(rbs.beforeRbMccMnc)) {
                    result = true;
                    this.mVoicemail = rbs.rbVoiceMail;
                    log(rbs.name + " Roaming broker is activated");
                    break;
                }
                i++;
            }
            unloadRbSequenceMap();
        }
        log("isValidRbSequence: " + result);
        return result;
    }

    public static boolean isRoamingBrokerActivated() {
        boolean result = RB_ACTIVATED_GLAG_ON.equals(SystemProperties.get("gsm.RBActivated0"));
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
        this.mPreviousOp = SystemProperties.get(PREVIOUS_OPERATOR + slotId, "");
        log(String.format("Previously saved operator code is %s", this.mPreviousOp));
        log("init state: " + PhoneFactory.getInitState());
        if (PhoneFactory.getInitState()) {
            Context context = PhoneFactory.getDefaultPhone().getContext();
            this.mPreviousIccid = decryptInfo(context, PREVIOUS_ICCID + slotId);
        } else {
            this.mPreviousIccid = "";
        }
        log(String.format("Previously saved Iccid is %s", printIccid(this.mPreviousIccid)));
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
            if (checkSpecRbState()) {
                log("set roamingbroker state on and set PREVIOUS_OPERATOR " + this.mPreviousOp);
                SystemProperties.set(RB_ACTIVATED + this.mSlotId, RB_ACTIVATED_GLAG_ON);
                SystemProperties.set(PREVIOUS_OPERATOR + this.mSlotId, this.mPreviousOp);
            }
            this.isIccidSet = false;
            this.isImsiSet = false;
        }
    }

    public boolean isRoamingBrokerActivated(Integer slotId) {
        boolean result = RB_ACTIVATED_GLAG_ON.equals(SystemProperties.get(RB_ACTIVATED + slotId));
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
        return decryptImsi(context, PREVIOUS_IMSI + this.mSlotId);
    }

    public String getRBOperatorNumeric(Integer slotId) {
        return getDefault(slotId).mPreviousOp;
    }

    public String getRBVoicemail(Integer slotId) {
        return getDefault(slotId).mVoicemail;
    }
}
