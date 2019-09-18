package com.android.internal.telephony.dataconnection;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.emcom.EmcomManager;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PcoData;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Xml;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.HwServiceStateManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwCustDcTrackerImpl extends HwCustDcTracker {
    static final String APNS_OPKEY_CONFIG_FILE = "apns-opkey.xml";
    static final String APN_OPKEY_INFO = "apnOpkeyInfo";
    static final String APN_OPKEY_INFO_APN = "apn";
    static final String APN_OPKEY_INFO_NUMERIC = "numeric";
    static final String APN_OPKEY_INFO_OPKEY = "opkey";
    static final String APN_OPKEY_INFO_USER = "user";
    static final String APN_OPKEY_LAST_IMSI = "apn_opkey_last_imsi";
    static final String APN_OPKEY_LIST_DOCUMENT = "apnOpkeyNodes";
    static final String APN_OPKEY_NODE = "apnOpkeyNode";
    private static final int CAUSE_BY_DATA = 0;
    private static final int CAUSE_BY_ROAM = 1;
    private static final int CUSTOMIZED_ROAMING_APN_LEN = 4;
    private static final String CUST_ADD_APEC_APN_BUILD = SystemProperties.get("ro.config.add_spec_apn_build", "");
    private static final boolean CUST_ENABLE_OTA_BIP = SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false);
    private static final boolean DBG = true;
    private static final String DEFAULT_PCO_DATA = "-2;-2";
    private static final int DEFAULT_PCO_VALUE = -2;
    private static final boolean DISABLE_SIM2_DATA = SystemProperties.getBoolean("ro.config.hw_disable_sim2_data", false);
    private static final String DOCOMO_FOTA_APN = "open-dm2.dcm-dm.ne.jp";
    private static final String ENABLE_SES_CHECK_KEY = "enable_ses_check";
    private static final int EXPLICIT_DROP = -1;
    private static final int EXPLICIT_KEEP = 1;
    private static final boolean HW_SIM_ACTIVATION = SystemProperties.getBoolean("ro.config.hw_sim_activation", false);
    private static final int IMPLICIT_KEEP = 0;
    private static final int IMS_PCO_TYPE = 1;
    private static final int INTERNET_PCO_TYPE = 3;
    private static final boolean IS_DOCOMO;
    private static final boolean IS_US_CHANNEL = (SystemProperties.get("ro.config.hw_opta", "0").equals("567") && SystemProperties.get("ro.config.hw_optb", "0").equals("840"));
    private static final boolean IS_VERIZON;
    private static final String KOREA_MCC = "450";
    private static final String NOT_ROAMING_STR = "1";
    private static final int OPEN_SERVICE_PDP_CREATE_WAIT_MILLIS = 60000;
    private static final int PCO_CONTENT_LENGTH = 4;
    private static final String PCO_DATA = "pco_data";
    private static final int RADIO_TECH_ALL = 0;
    private static final int RADIO_TECH_GU = 255;
    private static final String ROAMING_STR = "2";
    private static final String SES_APN_KEY = "ses_apn";
    private static final String SES_REQUEST_APN_TYPE_KEY = "ses_request_network";
    private static final String SPLIT = ";";
    private static final int SWITCH_ON = 1;
    private static final String TAG = "HwCustDcTrackerImpl";
    private static final String VERIZON_ICCID = "891480";
    private String LGU_PLMN = "45006";
    protected ArrayList<ApnOpkeyInfos> apnOpkeyInfosList = null;
    private boolean hadSentBoardCastToUI = false;
    DcTracker mDcTracker;
    private String mPLMN = "";
    private Context mPhoneContext;
    private ContentResolver mResolver;
    private String mSesApn;
    private BroadcastReceiver mSimStateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()) && HwCustDcTrackerImpl.this.isValidPhoneParams()) {
                int subId = 0;
                if (intent.getExtra("subscription") != null) {
                    subId = intent.getIntExtra("subscription", HwCustDcTrackerImpl.EXPLICIT_DROP);
                }
                if (subId != HwCustDcTrackerImpl.this.mSubId.intValue()) {
                    HwCustDcTrackerImpl.this.log("mSimStateChangedReceiver: not current subId, do nothing.");
                } else {
                    HwCustDcTrackerImpl.this.clearApnOpkeyIfNeed(intent.getStringExtra("ss"));
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Integer mSubId = 0;

    private static class ApnOpkeyInfos {
        String apn;
        String opkey;
        String user;

        private ApnOpkeyInfos() {
            this.apn = "";
            this.user = "";
            this.opkey = "";
        }
    }

    static {
        boolean equals = SystemProperties.get("ro.config.hw_opta", "0").equals("341");
        boolean z = DBG;
        IS_DOCOMO = equals && SystemProperties.get("ro.config.hw_optb", "0").equals("392");
        if (!SystemProperties.get("ro.config.hw_opta", "0").equals("389") || !SystemProperties.get("ro.config.hw_optb", "0").equals("840")) {
            z = false;
        }
        IS_VERIZON = z;
    }

    public HwCustDcTrackerImpl(DcTracker dcTracker) {
        super(dcTracker);
        this.mDcTracker = dcTracker;
        this.mPhoneContext = this.mDcTracker.mPhone.getContext();
        if (HW_SIM_ACTIVATION) {
            this.mResolver = this.mDcTracker.mPhone.getContext().getContentResolver();
            Settings.Global.putString(this.mResolver, PCO_DATA, DEFAULT_PCO_DATA);
            log("setting default pco values.");
        }
        registerSimStateChangedReceiver();
        if (isValidPhoneParams()) {
            this.mSubId = Integer.valueOf(this.mDcTracker.mPhone.getSubId());
        }
    }

    public boolean canKeepApn(String requestedApnType, ApnSetting apnSetting, boolean isPreferredApn) {
        Phone phone = this.mDcTracker.mPhone;
        boolean currentRoaming = phone.getServiceState().getRoaming();
        int radioTech = phone.getServiceState().getRilDataRadioTechnology();
        int canKeepApnResult = canKeepApnByServiceState(apnSetting, radioTech, currentRoaming);
        boolean z = false;
        if (canKeepApnResult != 0) {
            if (canKeepApnResult == 1) {
                z = true;
            }
            return z;
        }
        int canKeepApnResult2 = canKeepApnForSES(apnSetting, requestedApnType);
        if (canKeepApnResult2 != 0) {
            if (canKeepApnResult2 == 1) {
                z = true;
            }
            return z;
        } else if (!isPreferredApn || !hasBetterApnByBearer(apnSetting, this.mDcTracker.getAllApnList(), requestedApnType, radioTech)) {
            return DBG;
        } else {
            return false;
        }
    }

    private String getLguPlmn() {
        log("getLguPlmn LGU_PLMN is " + this.LGU_PLMN);
        return this.LGU_PLMN;
    }

    public void checkPLMN(String plmn) {
        if (CUST_ENABLE_OTA_BIP) {
            log("checkPLMN plmn = " + plmn);
            String oldPLMN = this.mPLMN;
            this.mPLMN = plmn;
            if (TextUtils.isEmpty(this.mPLMN) || (this.mPLMN.equals(oldPLMN) && this.hadSentBoardCastToUI)) {
                log("checkPLMN newPLMN:" + this.mPLMN + " oldPLMN:" + oldPLMN + " hadSentBoardCastToUI:" + this.hadSentBoardCastToUI);
                return;
            }
            log("checkPLMN mIsPseudoIMSI = " + getmIsPseudoImsi());
            if (getmIsPseudoImsi()) {
                if (this.mPLMN.equals(getLguPlmn())) {
                    Intent intent = new Intent();
                    intent.setAction("com.android.telephony.isopencard");
                    this.mDcTracker.mPhone.getContext().sendBroadcast(intent);
                    log("sendbroadcast OTA_ISOPEN_CARD_ACTION");
                    this.hadSentBoardCastToUI = DBG;
                } else if (!this.mPLMN.startsWith(KOREA_MCC)) {
                    Intent intent2 = new Intent();
                    intent2.setAction("com.android.telephony.roamingpseudo");
                    this.mDcTracker.mPhone.getContext().sendBroadcast(intent2);
                    log("sendbroadcast ROAMING_PSEUDO_ACTION");
                    this.hadSentBoardCastToUI = DBG;
                }
            }
        }
    }

    public void onOtaAttachFailed(ApnContext apnContext) {
        if (CUST_ENABLE_OTA_BIP) {
            log("onOtaAttachFailed sendbroadcast OTA_OPEN_SERVICE_ACTION, but Phone.OTARESULT.NETWORKFAIL");
            apnContext.setState(DctConstants.State.FAILED);
            this.mDcTracker.mPhone.notifyDataConnection("apnFailed", apnContext.getApnType());
            apnContext.setDataConnectionAc(null);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.open_service_result");
            intent.putExtra("result_code", 1);
            this.mDcTracker.mPhone.getContext().sendBroadcast(intent);
        }
    }

    public boolean getmIsPseudoImsi() {
        boolean isPseudoIMSI = false;
        if (Integer.parseInt(SystemProperties.get("gsm.sim.card.type", "-1")) == INTERNET_PCO_TYPE) {
            isPseudoIMSI = DBG;
        }
        log("getmIsPSendoIms: mIsPseudoIMSI = " + isPseudoIMSI);
        return isPseudoIMSI;
    }

    public void sendOTAAttachTimeoutMsg(ApnContext apnContext, boolean retValue) {
        if (CUST_ENABLE_OTA_BIP && "bip0".equals(apnContext.getApnType()) && retValue) {
            log("trySetupData: open service and setupData return true");
            this.mDcTracker.sendMessageDelayed(this.mDcTracker.obtainMessage(271146, apnContext), 60000);
        }
    }

    public void openServiceStart(UiccController uiccController) {
        if (CUST_ENABLE_OTA_BIP) {
            if (uiccController != null && getmIsPseudoImsi()) {
                UiccCard uiccCard = uiccController.getUiccCard(0);
                if (uiccCard != null) {
                    CatService catService = uiccCard.getCatService();
                    if (catService != null) {
                        catService.onOtaCommand(0);
                        log("onDataSetupComplete: Open Service!");
                    } else {
                        log("onDataSetupComplete:catService is null when Open Service!");
                    }
                }
            }
            if (this.mDcTracker.hasMessages(271146)) {
                this.mDcTracker.removeMessages(271146);
            }
        }
    }

    public String getPlmn() {
        if (CUST_ENABLE_OTA_BIP) {
            return this.mPLMN;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void log(String string) {
        Rlog.d(TAG, "[" + this.mSubId + "]" + string);
    }

    public boolean isDocomoApn(ApnSetting preferredApn) {
        if (!IS_DOCOMO || (preferredApn != null && "spmode.ne.jp".equals(preferredApn.apn))) {
            return false;
        }
        return DBG;
    }

    public ApnSetting getDocomoApn(ApnSetting preferredApn) {
        if (preferredApn != null && !ArrayUtils.contains(preferredApn.types, "dun")) {
            int len = preferredApn.types.length;
            String[] types = new String[(len + 1)];
            System.arraycopy(preferredApn.types, 0, types, 0, len);
            types[len] = "dun";
            preferredApn.types = types;
        }
        log("getDocomoApn end   preferredApn:" + preferredApn);
        return preferredApn;
    }

    public boolean isCanHandleType(ApnSetting apnSetting, String requestedApnType) {
        if (!IS_DOCOMO || apnSetting == null || !"fota".equalsIgnoreCase(requestedApnType) || DOCOMO_FOTA_APN.equals(apnSetting.apn)) {
            return DBG;
        }
        return false;
    }

    public boolean isDocomoTetheringApn(ApnSetting apnSetting, String type) {
        if (!IS_DOCOMO || apnSetting == null || !"dcmtrg.ne.jp".equals(apnSetting.apn) || (!"default".equalsIgnoreCase(type) && !"supl".equalsIgnoreCase(type))) {
            return false;
        }
        return DBG;
    }

    public void savePcoData(PcoData pcoData) {
        if (HW_SIM_ACTIVATION) {
            if (pcoData.contents == null || pcoData.contents.length != 4) {
                log("pco content illegal,not handle.");
                return;
            }
            String[] pcoValues = getPcoValueFromSetting();
            log("pco setting values is : " + Arrays.toString(pcoValues));
            String imsPcoValueSetting = pcoValues[0];
            String internetPcoValueSetting = pcoValues[1];
            boolean isNeedRefresh = DBG;
            int i = pcoData.cid;
            if (i == 1) {
                String imsPcoValue = getPcoValueFromContent(pcoData.contents);
                if (imsPcoValueSetting.equals(imsPcoValue)) {
                    isNeedRefresh = false;
                } else {
                    imsPcoValueSetting = imsPcoValue;
                }
            } else if (i != INTERNET_PCO_TYPE) {
                isNeedRefresh = false;
                log("not handle : pco data cid is : " + pcoData.cid);
            } else {
                String internetPcoValue = getPcoValueFromContent(pcoData.contents);
                if (internetPcoValueSetting.equals(internetPcoValue)) {
                    isNeedRefresh = false;
                } else {
                    internetPcoValueSetting = internetPcoValue;
                }
            }
            if (isNeedRefresh) {
                String pcoValue = imsPcoValueSetting.concat(SPLIT).concat(internetPcoValueSetting);
                Settings.Global.putString(this.mResolver, PCO_DATA, pcoValue);
                log("refresh pco setting data is : " + pcoValue);
            } else {
                log("not need to refresh pco data.");
            }
        }
    }

    private String getPcoValueFromContent(byte[] contents) {
        int pcoValue = -2;
        if (contents != null && contents.length == 4) {
            try {
                pcoValue = contents[INTERNET_PCO_TYPE];
            } catch (Exception e) {
                log("Exception: transform pco value error " + e.toString());
            }
        }
        log("content pco value is : " + pcoValue);
        return String.valueOf(pcoValue);
    }

    private String[] getPcoValueFromSetting() {
        String[] pcoValues = Settings.Global.getString(this.mResolver, PCO_DATA).split(SPLIT);
        if (pcoValues != null && pcoValues.length == 2) {
            return pcoValues;
        }
        return new String[]{String.valueOf(-2), String.valueOf(-2)};
    }

    private boolean isVerizonSim(Context context) {
        if (context == null) {
            return false;
        }
        return ("" + ((TelephonyManager) context.getSystemService("phone")).getSimSerialNumber()).startsWith(VERIZON_ICCID);
    }

    public boolean isRoamDisallowedByCustomization(ApnContext apnContext) {
        if (apnContext == null || ((!IS_VERIZON && !IS_US_CHANNEL) || !isVerizonSim(this.mDcTracker.mPhone.getContext()) || !"mms".equals(apnContext.getApnType()) || !this.mDcTracker.mPhone.getServiceState().getDataRoaming() || this.mDcTracker.getDataRoamingEnabled())) {
            return false;
        }
        return DBG;
    }

    public void setDataOrRoamOn(int cause) {
        ServiceStateTracker sst = this.mDcTracker.mPhone.getServiceStateTracker();
        if (sst != null && sst.returnObject() != null && sst.returnObject().isDataOffbyRoamAndData()) {
            if (cause == 0) {
                if (SystemProperties.get("gsm.isuser.setdata", "true").equals("true")) {
                    this.mDcTracker.mPhone.mCi.setMobileDataEnable(1, null);
                } else {
                    SystemProperties.set("gsm.isuser.setdata", "true");
                }
            }
            if (cause == 1) {
                this.mDcTracker.mPhone.mCi.setRoamingDataEnable(1, null);
            }
        }
    }

    public boolean isDataDisableBySim2() {
        return DISABLE_SIM2_DATA;
    }

    public boolean isDataDisable(int subId) {
        return isDataDisableByNonAis(subId);
    }

    private boolean isDataDisableByNonAis(int subId) {
        if (!HwTelephonyManagerInner.getDefault().isCustomAis() || HwTelephonyManagerInner.getDefault().isAISCard(subId) || HwTelephonyManagerInner.getDefault().isAisCustomDisable()) {
            return false;
        }
        log("ais custom version but not ais card, disable the data.");
        return DBG;
    }

    public boolean addSpecifiedApnSwitch() {
        if (!TextUtils.isEmpty(CUST_ADD_APEC_APN_BUILD)) {
            return DBG;
        }
        return false;
    }

    public boolean addSpecifiedApnToWaitingApns(DcTracker dcTracker, ApnSetting preferredApn, ApnSetting apn) {
        if (!CUST_ADD_APEC_APN_BUILD.contains(dcTracker.getOperatorNumeric()) || preferredApn == null || !preferredApn.carrier.equals(apn.carrier)) {
            return false;
        }
        return DBG;
    }

    public boolean isSmartMpEnable() {
        return EmcomManager.getInstance().isSmartMpEnable();
    }

    private void loge(String s) {
        Rlog.e(TAG, "[" + this.mSubId + "]" + s);
    }

    private void registerSimStateChangedReceiver() {
        IntentFilter filter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        if (isValidPhoneParams()) {
            this.mDcTracker.mPhone.getContext().registerReceiver(this.mSimStateChangedReceiver, filter);
        }
    }

    public void dispose() {
        if (!isValidPhoneParams()) {
            log("Invalid phone params when dispose, do nothing.");
            return;
        }
        if (this.mSimStateChangedReceiver != null) {
            this.mDcTracker.mPhone.getContext().unregisterReceiver(this.mSimStateChangedReceiver);
            this.mSimStateChangedReceiver = null;
        }
    }

    private FileInputStream getApnOpkeyCfgFileInputStream() {
        FileInputStream fileIn = null;
        try {
            File confFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/%s", new Object[]{APNS_OPKEY_CONFIG_FILE}), 0);
            if (confFile == null) {
                log("getApnOpkeyCfgFileInputStream: apns-opkey.xml not exists.");
                return null;
            }
            fileIn = new FileInputStream(confFile);
            return fileIn;
        } catch (NoClassDefFoundError e) {
            loge("getApnOpkeyCfgFileInputStream: NoClassDefFoundError occurs.");
        } catch (FileNotFoundException e2) {
            loge("getApnOpkeyCfgFileInputStream: FileNotFoundException occurs.");
        } catch (Exception e3) {
            loge("getApnOpkeyCfgFileInputStream: Exception occurs.");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x012f, code lost:
        loge("loadMatchedApnOpkeyList: fileInputStream  close error");
     */
    private void loadMatchedApnOpkeyList(String numeric) {
        if (this.apnOpkeyInfosList != null) {
            log("loadMatchedApnOpkeyList:load ApnOpkeyList only once for same SIM card, return;");
        } else if (TextUtils.isEmpty(numeric)) {
            loge("loadMatchedApnOpkeyList: numeric is null, return.");
        } else {
            FileInputStream fileIn = getApnOpkeyCfgFileInputStream();
            if (fileIn == null) {
                loge("loadMatchedApnOpkeyList: fileIn is null, retrun.");
                return;
            }
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileIn, null);
                XmlUtils.beginDocument(parser, APN_OPKEY_LIST_DOCUMENT);
                while (true) {
                    int next = parser.next();
                    int eventType = next;
                    if (1 != next) {
                        if (2 == eventType && APN_OPKEY_NODE.equalsIgnoreCase(parser.getName())) {
                            String apnOpkeyInfoNumeric = parser.getAttributeValue(null, APN_OPKEY_INFO_NUMERIC);
                            if (numeric.equals(apnOpkeyInfoNumeric)) {
                                log("loadMatchedApnOpkeyList: parser " + parser.getName() + " begin, numeric=" + numeric + ", apnOpkeyInfoNumeric=" + apnOpkeyInfoNumeric);
                                this.apnOpkeyInfosList = new ArrayList<>();
                                while (true) {
                                    int next2 = parser.next();
                                    int eventType2 = next2;
                                    if (1 == next2) {
                                        break;
                                    }
                                    log("loadMatchedApnOpkeyList: parser " + parser.getName() + ", eventType=" + eventType2);
                                    if (2 != eventType2 || !APN_OPKEY_INFO.equalsIgnoreCase(parser.getName())) {
                                        if (INTERNET_PCO_TYPE == eventType2 && APN_OPKEY_NODE.equalsIgnoreCase(parser.getName())) {
                                            log("loadMatchedApnOpkeyList: parser " + parser.getName() + " end.");
                                            break;
                                        }
                                        log("loadMatchedApnOpkeyList: skip this line node.");
                                    } else {
                                        ApnOpkeyInfos apnOpkeyInfos = new ApnOpkeyInfos();
                                        apnOpkeyInfos.apn = parser.getAttributeValue(null, APN_OPKEY_INFO_APN);
                                        apnOpkeyInfos.user = parser.getAttributeValue(null, APN_OPKEY_INFO_USER);
                                        apnOpkeyInfos.opkey = parser.getAttributeValue(null, APN_OPKEY_INFO_OPKEY);
                                        this.apnOpkeyInfosList.add(apnOpkeyInfos);
                                    }
                                }
                                log("loadMatchedApnOpkeyList: parser config xml end.");
                            }
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                loge("loadMatchedApnOpkeyList: Some errors occur when parsering apns-opkey.xml");
                fileIn.close();
            } catch (IOException e2) {
                loge("loadMatchedApnOpkeyList: Can't find apns-opkey.ml.");
                fileIn.close();
            } catch (Throwable th) {
                try {
                    fileIn.close();
                } catch (IOException e3) {
                    loge("loadMatchedApnOpkeyList: fileInputStream  close error");
                }
                throw th;
            }
        }
    }

    public boolean isValidPhoneParams() {
        if (this.mDcTracker == null || this.mDcTracker.mPhone == null || this.mDcTracker.mPhone.getContext() == null) {
            return false;
        }
        return DBG;
    }

    public String getOpKeyByActivedApn(String activedNumeric, String activedApn, String activedUser) {
        if (TextUtils.isEmpty(activedNumeric)) {
            loge("getOpKeyByActivedApn: numeric is null, return.");
            return null;
        }
        loadMatchedApnOpkeyList(activedNumeric);
        if (this.apnOpkeyInfosList != null && this.apnOpkeyInfosList.size() > 0) {
            int i = 0;
            int size = this.apnOpkeyInfosList.size();
            while (i < size) {
                ApnOpkeyInfos avInfo = this.apnOpkeyInfosList.get(i);
                if (avInfo.apn == null || !avInfo.apn.equals(activedApn) || avInfo.user == null || !avInfo.user.equals(activedUser)) {
                    i++;
                } else {
                    log("getOpKeyByActivedApn: return matched apn opkey = " + avInfo.opkey);
                    return avInfo.opkey;
                }
            }
        }
        return null;
    }

    public void setApnOpkeyToSettingsDB(String activedApnOpkey) {
        if (isValidPhoneParams()) {
            ContentResolver contentResolver = this.mDcTracker.mPhone.getContext().getContentResolver();
            Settings.System.putString(contentResolver, "sim_apn_opkey" + this.mSubId, activedApnOpkey);
        }
    }

    /* access modifiers changed from: private */
    public void clearApnOpkeyIfNeed(String simState) {
        boolean needClearApnOpkey = false;
        if ("ABSENT".equals(simState)) {
            needClearApnOpkey = DBG;
        } else if ("IMSI".equals(simState)) {
            if (!isValidPhoneParams() || this.mDcTracker.mPhone.mIccRecords == null || this.mDcTracker.mPhone.mIccRecords.get() == null) {
                needClearApnOpkey = DBG;
            } else {
                String currentImsi = ((IccRecords) this.mDcTracker.mPhone.mIccRecords.get()).getIMSI();
                HwPhoneManager hwPhoneManager = HwTelephonyFactory.getHwPhoneManager();
                Context context = this.mDcTracker.mPhone.getContext();
                String oldImsi = hwPhoneManager.decryptInfo(context, APN_OPKEY_LAST_IMSI + this.mSubId);
                if (currentImsi == null) {
                    log("clearApnOpkeyIfNeed: currentImsi is null, maybe error occurs, clear apn opkey.");
                    needClearApnOpkey = DBG;
                } else if (!currentImsi.equals(oldImsi)) {
                    log("clearApnOpkeyIfNeed: diffrent SIM card, clear apn opkey.");
                    needClearApnOpkey = DBG;
                    HwPhoneManager hwPhoneManager2 = HwTelephonyFactory.getHwPhoneManager();
                    Context context2 = this.mDcTracker.mPhone.getContext();
                    hwPhoneManager2.encryptInfo(context2, currentImsi, APN_OPKEY_LAST_IMSI + this.mSubId);
                }
            }
        }
        if (needClearApnOpkey) {
            log("clearApnOpkeyIfNeed: INTENT_VALUE_ICC_" + simState + ", clear APN opkey.");
            setApnOpkeyToSettingsDB(null);
            this.apnOpkeyInfosList = null;
        }
    }

    public ArrayList<ApnSetting> sortApnListByBearer(ArrayList<ApnSetting> list, String requestedApnType, int radioTech) {
        if (list == null || list.size() <= 1) {
            return list;
        }
        if (!"xcap".equals(requestedApnType) && !"mms".equals(requestedApnType)) {
            return list;
        }
        list.sort(new Comparator<ApnSetting>() {
            public int compare(ApnSetting apn1, ApnSetting apn2) {
                if (apn1.bearerBitmask != 0 && apn2.bearerBitmask == 0) {
                    return HwCustDcTrackerImpl.EXPLICIT_DROP;
                }
                if (apn1.bearerBitmask != 0 || apn2.bearerBitmask == 0) {
                    return 0;
                }
                return 1;
            }
        });
        return list;
    }

    public boolean hasBetterApnByBearer(ApnSetting curApnSetting, ArrayList<ApnSetting> list, String requestedApnType, int radioTech) {
        if (list == null || list.size() < 1 || curApnSetting == null) {
            return false;
        }
        if (!"mms".equals(requestedApnType) && !"xcap".equals(requestedApnType)) {
            return false;
        }
        int apnListSize = list.size();
        int i = 0;
        while (i < apnListSize) {
            ApnSetting apn = list.get(i);
            if (apn.equals(curApnSetting) || !apn.canHandleType(requestedApnType) || curApnSetting.bearerBitmask != 0 || !ServiceState.bitmaskHasTech(apn.bearerBitmask, radioTech) || apn.bearerBitmask == 0) {
                i++;
            } else {
                log("found other better apn with non-zero bearer bitmask");
                return DBG;
            }
        }
        return false;
    }

    public void tryRestartRadioWhenPrefApnChange(ApnSetting curPreferredApn, ApnSetting oldPreferredApn) {
        if (canRestartRadioWhenPrefApnChanged()) {
            GsmCdmaPhone gsmCdmaPhone = this.mDcTracker.mPhone;
            boolean isPreApnChanged = (curPreferredApn == null || curPreferredApn.equals(oldPreferredApn)) ? false : DBG;
            boolean rejFlag = false;
            ServiceStateTracker sst = gsmCdmaPhone.getServiceStateTracker();
            int dataRadioTech = gsmCdmaPhone.getServiceState().getRilDataRadioTechnology();
            int nwMode = Settings.Global.getInt(this.mDcTracker.mPhone.getContext().getContentResolver(), "preferred_network_mode", 9);
            TelephonyManager tm = TelephonyManager.getDefault();
            if (sst != null) {
                rejFlag = HwServiceStateManager.getHwGsmServiceStateManager(sst, gsmCdmaPhone).getRejFlag();
            }
            log("onApnChanged nwMode = " + nwMode + " callState = " + tm.getCallState() + " dataRadioTech = " + dataRadioTech + " isPreApnChanged = " + isPreApnChanged + " rejFlag = " + rejFlag);
            if (rejFlag && !ServiceState.isLte(dataRadioTech) && isPreApnChanged && tm != null && tm.getCallState() == 0 && isNetworkModeEnableLTE(nwMode)) {
                log("onApnChanged begin-restartRadio ");
                HwServiceStateManager.getHwGsmServiceStateManager(sst, gsmCdmaPhone).clearRejFlag();
                this.mDcTracker.mPhone.mCi.setRadioPower(false, null);
            }
        }
    }

    private boolean canRestartRadioWhenPrefApnChanged() {
        try {
            Boolean isRadioOffChangePreferApn = (Boolean) HwCfgFilePolicy.getValue("hw_radio_off_change_preferapn", this.mDcTracker.mPhone.getSubId(), Boolean.class);
            if (isRadioOffChangePreferApn != null && isRadioOffChangePreferApn.booleanValue()) {
                return DBG;
            }
        } catch (Exception e) {
            Rlog.e(TAG, "read hw_radio_off_change_preferapn error ");
        }
        return false;
    }

    public void tryClearRejFlag() {
        ServiceStateTracker sst = this.mDcTracker.mPhone.getServiceStateTracker();
        if (sst != null) {
            HwServiceStateManager.getHwGsmServiceStateManager(sst, this.mDcTracker.mPhone).clearRejFlag();
        }
    }

    private boolean isNetworkModeEnableLTE(int iNetworkMode) {
        if (iNetworkMode == 9 || iNetworkMode == 11 || iNetworkMode == 12 || iNetworkMode == 8 || iNetworkMode == 10) {
            return DBG;
        }
        return false;
    }

    private int canKeepApnByServiceState(ApnSetting apnSetting, int radioTech, boolean currentRoaming) {
        char c;
        ApnSetting apnSetting2 = apnSetting;
        int i = radioTech;
        char c2 = 65535;
        if (apnSetting2 == null) {
            return EXPLICIT_DROP;
        }
        String customizedRoamingApnStr = Settings.System.getString(this.mPhoneContext.getContentResolver(), "hw_customized_roaming_apn");
        char c3 = 0;
        if (TextUtils.isEmpty(customizedRoamingApnStr)) {
            return 0;
        }
        String[] customizedRoamingApns = customizedRoamingApnStr.split(SPLIT);
        String operator = this.mDcTracker.getOperatorNumeric();
        int length = customizedRoamingApns.length;
        int i2 = 0;
        while (i2 < length) {
            String customizedRoamingApn = customizedRoamingApns[i2];
            if (!TextUtils.isEmpty(customizedRoamingApn)) {
                String[] customizedRoamingApnSetting = customizedRoamingApn.split(":");
                if (customizedRoamingApnSetting.length == 4) {
                    String mccmnc = customizedRoamingApnSetting[c3];
                    String carrier = customizedRoamingApnSetting[1];
                    String apn = customizedRoamingApnSetting[2];
                    String roaming = customizedRoamingApnSetting[INTERNET_PCO_TYPE];
                    if (mccmnc.equals(operator)) {
                        if (!carrier.equals(apnSetting2.carrier) || !apn.equals(apnSetting2.apn)) {
                            c = 65535;
                            i2++;
                            c2 = c;
                            c3 = 0;
                        } else {
                            boolean isApnBearerSupportedByRadioTech = apnSetting2.bearer == i || (RADIO_TECH_GU == apnSetting2.bearer && 14 != i);
                            if (!roaming.equals(currentRoaming ? ROAMING_STR : NOT_ROAMING_STR) || (apnSetting2.bearer != 0 && !isApnBearerSupportedByRadioTech)) {
                                return EXPLICIT_DROP;
                            }
                            return 1;
                        }
                    }
                }
            }
            c = c2;
            i2++;
            c2 = c;
            c3 = 0;
        }
        return 0;
    }

    private int canKeepApnForSES(ApnSetting apnSetting, String apnType) {
        int i = EXPLICIT_DROP;
        if (apnSetting == null) {
            return EXPLICIT_DROP;
        }
        if (!isDataAllowedForSES(apnType)) {
            return 0;
        }
        if (TextUtils.isEmpty(this.mSesApn)) {
            this.mSesApn = Settings.Global.getString(this.mPhoneContext.getContentResolver(), SES_APN_KEY);
            if (TextUtils.isEmpty(this.mSesApn)) {
                Settings.Global.putString(this.mPhoneContext.getContentResolver(), SES_REQUEST_APN_TYPE_KEY, "");
                return 0;
            }
        }
        if (this.mSesApn.equals(apnSetting.apn)) {
            i = 1;
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public boolean isDataAllowedForSES(String apnType) {
        boolean z = false;
        if (TextUtils.isEmpty(apnType)) {
            return false;
        }
        PersistableBundle pb = null;
        Object configService = this.mPhoneContext.getSystemService("carrier_config");
        if (configService instanceof CarrierConfigManager) {
            pb = ((CarrierConfigManager) configService).getConfigForSubId(this.mDcTracker.mPhone.getSubId());
        }
        if (pb == null) {
            pb = CarrierConfigManager.getDefaultConfig();
        }
        if (pb.getBoolean(ENABLE_SES_CHECK_KEY, false) && apnType.equals(Settings.Global.getString(this.mPhoneContext.getContentResolver(), SES_REQUEST_APN_TYPE_KEY))) {
            z = DBG;
        }
        return z;
    }
}
