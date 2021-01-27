package huawei.com.android.internal.telephony;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwparttelephony.BuildConfig;
import com.huawei.internal.telephony.PhoneFactoryExt;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RoamingBroker {
    private static final boolean DBG = true;
    private static final int ICCID_LEN = 6;
    private static final int ICCID_RB_CFG = 2;
    private static final String IMSI_SAVE_FILE_NAME = "imsi";
    private static final String LOG_TAG = "GSM";
    public static final String PREVIOUS_ICCID = "persist.radio.previousiccid";
    public static final String PREVIOUS_IMSI = "persist.radio.previousimsi";
    public static final String PREVIOUS_OPERATOR = "persist.radio.previousopcode";
    private static final int RBS_BEFORE_RB_MCCMNC_INDEX = 1;
    private static final int RBS_NAME_INDEX = 0;
    private static final int RBS_RB_MCCMNC_INDEX = 2;
    private static final int RBS_RB_VOICE_MAIL_INDEX = 3;
    public static final String RB_ACTIVATED = "gsm.RBActivated";
    private static final String RB_ACTIVATED_GLAG_ON = "true";
    private static final String ROAM_BROKER_CUST_FILE_PATH = "carrier/network/xml/roamingbroker.xml";
    private static final int SRB_CONFIG_HPLMN_INDEX = 2;
    private static final int SRB_CONFIG_LENGTH = 3;
    private static List<RoamingBrokerSequence> mRbSequenceList = new ArrayList();
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
        this.mPreviousOp = SystemPropertiesEx.get(PREVIOUS_OPERATOR, BuildConfig.FLAVOR);
        log(String.format("Previously saved operator code is %s", this.mPreviousOp));
        log("init state: " + PhoneFactoryExt.getInitState());
        if (PhoneFactoryExt.getInitState()) {
            this.mPreviousIccid = decryptInfo(PhoneFactoryExt.getDefaultPhone().getContext(), PREVIOUS_ICCID);
        } else {
            this.mPreviousIccid = BuildConfig.FLAVOR;
        }
        log(String.format("Previously saved Iccid is %s", printIccid(this.mPreviousIccid)));
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
        this.mPreviousOp = SystemPropertiesEx.get(PREVIOUS_OPERATOR + slotId, BuildConfig.FLAVOR);
        log(String.format("Previously saved operator code is %s", this.mPreviousOp));
        log("init state: " + PhoneFactoryExt.getInitState());
        if (PhoneFactoryExt.getInitState()) {
            Context context = PhoneFactoryExt.getDefaultPhone().getContext();
            this.mPreviousIccid = decryptInfo(context, PREVIOUS_ICCID + slotId);
        } else {
            this.mPreviousIccid = BuildConfig.FLAVOR;
        }
        log(String.format("Previously saved Iccid is %s", printIccid(this.mPreviousIccid)));
    }

    private static void log(String text) {
        Log.d(LOG_TAG, "[RoamingBroker] " + text);
    }

    public static RoamingBroker getDefault() {
        return HelperHolder.mRoamingBroker0;
    }

    public static RoamingBroker getDefault(Integer slotId) {
        if (slotId.intValue() == 0) {
            return HelperHolder.mRoamingBroker0;
        }
        return HelperHolder.mRoamingBroker1;
    }

    public static boolean isRoamingBrokerActivated() {
        boolean result = RB_ACTIVATED_GLAG_ON.equals(SystemPropertiesEx.get("gsm.RBActivated0"));
        log("isRoamingBrokerActivated returns " + result);
        return result;
    }

    public static String updateSelectionForRoamingBroker(String selection) {
        log("updateSelection for: " + selection);
        if (selection == null) {
            return BuildConfig.FLAVOR;
        }
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

    private void loadRbSequenceMap() {
        String data;
        mRbSequenceList.clear();
        try {
            Optional<String> rbSequenceListConf = getRbSeqListConf("roamingBrokerSequenceList");
            if (rbSequenceListConf.isPresent()) {
                data = rbSequenceListConf.get();
            } else {
                data = Settings.System.getString(PhoneFactoryExt.getDefaultPhone().getContext().getContentResolver(), "roamingBrokerSequenceList");
            }
            log("get raw RB list with voicemail" + data);
            if (data != null) {
                String[] buffer = data.split("\\|");
                for (String str : buffer) {
                    if (str != null) {
                        String[] tmp = str.split(",");
                        if (tmp.length < 3) {
                            log("RB list contains invalid config: " + str);
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

    public void setOperator(String operatorCode) {
        log("received operatorCode of value: " + operatorCode);
        if (operatorCode != null && !operatorCode.equals(this.mCurrentOp)) {
            this.mCurrentOp = operatorCode;
        }
    }

    public void setIccId(String iccId) {
        log("Previous Iccid value: " + printIccid(iccId));
        boolean isNeedClrIccid = false;
        this.isIccidSet = DBG;
        if (iccId != null && !iccId.equals(this.mCurrentIccid)) {
            this.mCurrentIccid = iccId;
            if (this.isHaveSetData) {
                setData();
                this.isHaveSetData = false;
                isNeedClrIccid = DBG;
            }
        }
        if (this.isIccidSet && this.isImsiSet) {
            log("check specfic Iccid for romaing broker state");
            if (checkSpecRbState()) {
                log("set roamingbroker state on and set PREVIOUS_OPERATOR " + this.mPreviousOp);
                SystemPropertiesEx.set(RB_ACTIVATED + this.mSlotId, RB_ACTIVATED_GLAG_ON);
                SystemPropertiesEx.set(PREVIOUS_OPERATOR + this.mSlotId, this.mPreviousOp);
            }
            this.isIccidSet = false;
            this.isImsiSet = false;
        }
        if (isNeedClrIccid) {
            this.mCurrentIccid = null;
        }
    }

    private void setData() {
        if (this.mCurrentIccid != null && this.mCurrentOp != null && this.mCurrentImsi != null) {
            SystemPropertiesEx.set(RB_ACTIVATED + this.mSlotId, BuildConfig.FLAVOR);
            Context context = PhoneFactoryExt.getDefaultPhone().getContext();
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
            SystemPropertiesEx.set(PREVIOUS_OPERATOR + this.mSlotId, this.mCurrentOp);
            encryptInfo(context, PREVIOUS_ICCID + this.mSlotId, this.mCurrentIccid);
            encryptImsi(context, PREVIOUS_IMSI + this.mSlotId, this.mCurrentImsi);
            this.mPreviousIccid = this.mCurrentIccid;
            this.mPreviousOp = this.mCurrentOp;
            log(String.format("different sim card. Set operatorCode: %s, iccId: %s for roaming broker", this.mPreviousOp, printIccid(this.mPreviousIccid)));
        } else if (!isValidRbSequence()) {
            SystemPropertiesEx.set(PREVIOUS_OPERATOR + this.mSlotId, this.mCurrentOp);
            encryptImsi(context, PREVIOUS_IMSI + this.mSlotId, this.mCurrentImsi);
            this.mPreviousOp = this.mCurrentOp;
            log(String.format("same sim card. Set operatorCode: %s for roaming broker", this.mPreviousOp));
        } else {
            SystemPropertiesEx.set(RB_ACTIVATED + this.mSlotId, RB_ACTIVATED_GLAG_ON);
        }
    }

    private void encryptInfo(Context context, String encryptTag, String sensitiveInfo) {
        encryptInfo(PreferenceManager.getDefaultSharedPreferences(context), encryptTag, sensitiveInfo);
    }

    private void encryptInfo(SharedPreferences sp, String encryptTag, String sensitiveInfo) {
        SharedPreferences.Editor editor = sp.edit();
        String sensitiveInfoEncrypted = null;
        try {
            sensitiveInfoEncrypted = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), sensitiveInfo);
        } catch (NoSuchProviderException e) {
            log("HwAESCryptoUtil encryptInfo NoSuchProviderException");
        } catch (Exception e2) {
            log("HwAESCryptoUtil encryptInfo excepiton");
        }
        editor.putString(encryptTag, sensitiveInfoEncrypted);
        editor.apply();
    }

    private void encryptImsi(Context context, String encryptTag, String sensitiveInfo) {
        encryptInfo(context.getSharedPreferences("imsi", 0), encryptTag, sensitiveInfo);
    }

    private String decryptImsi(Context context, String encryptTag) {
        return decryptInfo(context.getSharedPreferences("imsi", 0), encryptTag);
    }

    private String decryptInfo(Context context, String encryptTag) {
        return decryptInfo(PreferenceManager.getDefaultSharedPreferences(context), encryptTag);
    }

    private String decryptInfo(SharedPreferences sp, String encryptTag) {
        String sensitiveInfo = sp.getString(encryptTag, BuildConfig.FLAVOR);
        if (BuildConfig.FLAVOR.equals(sensitiveInfo)) {
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
        String specIccRbSeqList;
        String specIccRbSeqList2;
        boolean isRbActived = false;
        int i = 0;
        if (isNoNeedProcess()) {
            return false;
        }
        try {
            Optional<String> specIccRbSeqListConf = getRbSeqListConf("specIccidRBSeqList");
            if (specIccRbSeqListConf.isPresent()) {
                specIccRbSeqList = specIccRbSeqListConf.get();
            } else {
                specIccRbSeqList = Settings.System.getString(PhoneFactoryExt.getDefaultPhone().getContext().getContentResolver(), "specIccidRBSeqList");
            }
            log("checkSpecRbState mCurrentOp " + this.mCurrentOp + "get raw RB specific Iccid list" + specIccRbSeqList);
            if (TextUtils.isEmpty(specIccRbSeqList)) {
                return false;
            }
            String[] custArrays = specIccRbSeqList.trim().split(";");
            String hplmn = null;
            int length = custArrays.length;
            boolean isIccidMatch = false;
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
                        while (true) {
                            if (i >= length2) {
                                specIccRbSeqList2 = specIccRbSeqList;
                                break;
                            }
                            specIccRbSeqList2 = specIccRbSeqList;
                            if (this.mCurrentIccid.startsWith(iccids[i])) {
                                isIccidMatch = true;
                                break;
                            }
                            i++;
                            specIccRbSeqList = specIccRbSeqList2;
                        }
                        if (!isIccidMatch) {
                            i2++;
                            specIccRbSeqList = specIccRbSeqList2;
                            i = 0;
                        } else if (items.length >= 3) {
                            hplmn = items[2];
                        }
                    }
                }
                specIccRbSeqList2 = specIccRbSeqList;
                i2++;
                specIccRbSeqList = specIccRbSeqList2;
                i = 0;
            }
            log("checkSpecRbState return IccidMatch " + isIccidMatch);
            if (!isIccidMatch) {
                return false;
            }
            loadRbSequenceMap();
            if (mRbSequenceList == null) {
                return false;
            }
            int i3 = 0;
            int listSize = mRbSequenceList.size();
            while (true) {
                if (i3 >= listSize) {
                    break;
                }
                RoamingBrokerSequence rbs = mRbSequenceList.get(i3);
                if (!isNeedContinue(rbs, hplmn)) {
                    isRbActived = DBG;
                    log("match spefic Iccid Rbcfg and rbSeqList together and set PreviousOp: " + rbs.beforeRbMccMnc);
                    this.mVoicemail = rbs.rbVoiceMail;
                    this.mPreviousOp = rbs.beforeRbMccMnc;
                    this.mPreviousIccid = this.mCurrentIccid;
                    break;
                }
                i3++;
            }
            unloadRbSequenceMap();
            return isRbActived;
        } catch (IllegalArgumentException e) {
            log("Failed to load spefic iccid RB Sequence list. occured IllegalArgumentException");
        } catch (Exception e2) {
            log("Failed to load spefic iccid RB Sequence list.");
        }
    }

    private boolean isNoNeedProcess() {
        if (isRoamingBrokerActivated(Integer.valueOf(this.mSlotId)) || this.mCurrentOp == null || this.mCurrentIccid == null) {
            return DBG;
        }
        return false;
    }

    private boolean isNeedContinue(RoamingBrokerSequence rbs, String hplmn) {
        if (rbs == null || rbs.rbMccMnc == null || !this.mCurrentOp.equals(rbs.rbMccMnc)) {
            return DBG;
        }
        if (hplmn == null || hplmn.equals(rbs.beforeRbMccMnc)) {
            return false;
        }
        return DBG;
    }

    private boolean isValidRbSequence() {
        boolean result = false;
        if (this.mPreviousOp == null || this.mCurrentOp == null) {
            return false;
        }
        loadRbSequenceMap();
        List<RoamingBrokerSequence> list = mRbSequenceList;
        if (list != null) {
            int i = 0;
            int listSize = list.size();
            while (true) {
                if (i >= listSize) {
                    break;
                }
                RoamingBrokerSequence rbs = mRbSequenceList.get(i);
                if (this.mCurrentOp.equals(rbs.rbMccMnc) && this.mPreviousOp.equals(rbs.beforeRbMccMnc)) {
                    result = DBG;
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

    public boolean isRoamingBrokerActivated(Integer slotId) {
        boolean result = RB_ACTIVATED_GLAG_ON.equals(SystemPropertiesEx.get(RB_ACTIVATED + slotId));
        log("###isRoamingBrokerActivated returns " + result);
        return result;
    }

    public String updateSelectionForRoamingBroker(String selection, int slotId) {
        log("###updateSelection for: " + selection);
        if (selection == null) {
            return BuildConfig.FLAVOR;
        }
        String result = selection.replaceAll("numeric[ ]*=[ ]*[\"|']" + getDefault(Integer.valueOf(slotId)).mCurrentOp + "[\"|']", "numeric=\"" + getDefault(Integer.valueOf(slotId)).mPreviousOp + "\"");
        StringBuilder sb = new StringBuilder();
        sb.append("###updated Selection: ");
        sb.append(result);
        log(sb.toString());
        return result;
    }

    public String getRBOperatorNumeric(Integer slotId) {
        return getDefault(slotId).mPreviousOp;
    }

    public static String getRBVoicemail(Integer slotId) {
        return getDefault(slotId).mVoicemail;
    }

    public void setImsi(String imsi) {
        this.isImsiSet = DBG;
        if (imsi != null && !imsi.equals(this.mCurrentImsi)) {
            this.mCurrentImsi = imsi;
            this.isHaveSetData = DBG;
            setData();
        }
        if (this.isIccidSet && this.isImsiSet) {
            log("check specfic Iccid for romaing broker state");
            if (checkSpecRbState()) {
                log("set roamingbroker state on and set PREVIOUS_OPERATOR " + this.mPreviousOp);
                SystemPropertiesEx.set(RB_ACTIVATED + this.mSlotId, RB_ACTIVATED_GLAG_ON);
                SystemPropertiesEx.set(PREVIOUS_OPERATOR + this.mSlotId, this.mPreviousOp);
            }
            this.isIccidSet = false;
            this.isImsiSet = false;
        }
    }

    public String getRBImsi() {
        Context context = PhoneFactoryExt.getDefaultPhone().getContext();
        return decryptImsi(context, PREVIOUS_IMSI + this.mSlotId);
    }

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
            this.name = BuildConfig.FLAVOR;
            this.beforeRbMccMnc = BuildConfig.FLAVOR;
            this.rbMccMnc = BuildConfig.FLAVOR;
            this.rbVoiceMail = BuildConfig.FLAVOR;
        }
    }

    public Optional<String> getRBSeqListVal() {
        return getRbSeqListConf("roamingBrokerSequenceList");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0050, code lost:
        log("InputStream close has IO exception");
     */
    private Optional<String> getRbSeqListConf(String configName) {
        String configValue = null;
        InputStream inputStream = null;
        try {
            File globalCustFile = HwCfgFilePolicy.getCfgFile(ROAM_BROKER_CUST_FILE_PATH, 0, this.mSlotId);
            if (globalCustFile != null) {
                if (globalCustFile.exists()) {
                    InputStream inputStream2 = new FileInputStream(globalCustFile);
                    XmlPullParser xmlParser = Xml.newPullParser();
                    xmlParser.setInput(inputStream2, "UTF-8");
                    int xmlEventType = xmlParser.next();
                    while (true) {
                        if (xmlEventType != 1) {
                            String parserName = xmlParser.getName();
                            if (xmlEventType == 2 && TextUtils.equals(parserName, configName)) {
                                configValue = xmlParser.getAttributeValue(null, "key");
                                break;
                            }
                            xmlEventType = xmlParser.next();
                        }
                    }
                    log("read data from roamingbroker.xml success");
                    return TextUtils.isEmpty(configValue) ? Optional.empty() : Optional.of(configValue);
                }
            }
            Optional<String> empty = Optional.empty();
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log("InputStream close has IO exception");
                }
            }
            return empty;
        } catch (XmlPullParserException e2) {
            log("Failed to load roamingbroker.xml");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (FileNotFoundException e3) {
            log("Couldn`t find roamingbroker.xml");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (IOException e4) {
            log("IOException occurred");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (NoClassDefFoundError e5) {
            log("getRbSeqListConf: NoClassDefFoundError");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    log("InputStream close has IO exception");
                }
            }
            throw th;
        }
    }
}
