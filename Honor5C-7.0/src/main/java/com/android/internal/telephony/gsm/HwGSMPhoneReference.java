package com.android.internal.telephony.gsm;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.HwTelephony.NumMatchs;
import android.provider.HwTelephony.VirtualNets;
import android.provider.SettingsEx.Systemex;
import android.provider.Telephony.GlobalMatchs;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractGsmCdmaPhone.GSMPhoneReference;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPhoneReferenceBase;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.PlmnConstants;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.vsim.HwVSimConstants;
import huawei.cust.HwCustUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

public class HwGSMPhoneReference extends HwPhoneReferenceBase implements GSMPhoneReference {
    private static final int APNNAME_RULE = 3;
    private static final int APN_RULE = 2;
    private static final int ECC_NOCARD_INDEX = 4;
    private static final int ECC_WITHCARD_INDEX = 3;
    private static final int EVENT_GET_AVALIABLE_NETWORKS_DONE = 1;
    private static final int GID1_RULE = 1;
    private static final boolean HW_VM_NOT_FROM_SIM = false;
    protected static final String LOG_TAG = "HwGSMPhoneReference";
    private static final int MEID_LENGTH = 14;
    private static final int NAME_INDEX = 1;
    private static final int NUMERIC_INDEX = 2;
    private static final Uri PREFERAPN_NO_UPDATE_URI = null;
    private static final String PROPERTY_GLOBAL_FORCE_TO_SET_ECC = "ril.force_to_set_ecc";
    protected static final String PROP_REDUCE_SAR_SPECIAL_TYPE1 = "ro.config.reduce_sar_type1";
    protected static final String PROP_REDUCE_SAR_SPECIAL_TYPE2 = "ro.config.reduce_sar_type2";
    protected static final String PROP_REDUCE_SAR_SPECIAL_lEVEL = "ro.config.reduce_sar_level";
    public static final int REDUCE_SAR_NORMAL_HOTSPOT_ON = 8192;
    public static final int REDUCE_SAR_NORMAL_WIFI_OFF = 0;
    public static final int REDUCE_SAR_NORMAL_WIFI_ON = 4096;
    public static final int REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON = 20480;
    public static final int REDUCE_SAR_SPECIAL_TYPE1_WIFI_OFF = 12288;
    public static final int REDUCE_SAR_SPECIAL_TYPE1_WIFI_ON = 16384;
    private static final int REDUCE_SAR_SPECIAL_TYPE2_HOTSPOT_ON = 12287;
    private static final int REDUCE_SAR_SPECIAL_TYPE2_WIFI_OFF = 4095;
    private static final int REDUCE_SAR_SPECIAL_TYPE2_WIFI_ON = 8191;
    private static final int SPN_RULE = 0;
    public static final int SUB_NONE = -1;
    private static final boolean isMultiEnable = false;
    private static boolean mIsQCRilGoDormant;
    private static Object mQcRilHook;
    private static final boolean mWcdmaVpEnabled = false;
    private Handler mHandler;
    private HwCustHwGSMPhoneReference mHwCustHwGSMPhoneReference;
    private HwVpApStatusHandler mHwVpApStatusHandler;
    private int mLteReleaseVersion;
    private String mMeid;
    private String mPESN;
    private GsmCdmaPhone mPhone;
    private int mPhoneId;
    private int mPrevPowerGrade;
    private BroadcastReceiver mReceiver;
    private Boolean mSarControlServiceExist;
    private IccRecords mSimRecords;
    private WifiManager mWifiManager;
    private String mncForSAR;
    private String preOperatorNumeric;
    private String redcueSARSpeacialType1;
    private String redcueSARSpeacialType2;
    private String subTag;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwGSMPhoneReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwGSMPhoneReference.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwGSMPhoneReference.<clinit>():void");
    }

    public HwGSMPhoneReference(GsmCdmaPhone phone) {
        super(phone);
        this.mPhoneId = SPN_RULE;
        this.mPrevPowerGrade = SUB_NONE;
        this.redcueSARSpeacialType1 = null;
        this.redcueSARSpeacialType2 = null;
        this.mncForSAR = null;
        this.mSarControlServiceExist = null;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwGSMPhoneReference.NAME_INDEX /*1*/:
                        Object searchResults = null;
                        AsyncResult ar = msg.obj;
                        if (ar.exception == null && ar.result != null) {
                            searchResults = HwGSMPhoneReference.this.custAvaliableNetworks((ArrayList) ar.result);
                        }
                        if (searchResults != null) {
                            HwGSMPhoneReference.this.logd("[EVENT_GET_NETWORKS_DONE] Populated global version cust names for available networks.");
                        } else {
                            ArrayList searchResults2 = ar.result;
                        }
                        Message onComplete = ar.userObj;
                        if (onComplete != null) {
                            AsyncResult.forMessage(onComplete, searchResults, ar.exception);
                            if (onComplete.getTarget() != null) {
                                onComplete.sendToTarget();
                                return;
                            }
                            return;
                        }
                        HwGSMPhoneReference.this.loge("[EVENT_GET_NETWORKS_DONE] In EVENT_GET_NETWORKS_DONE, onComplete is null!");
                    default:
                        super.handleMessage(msg);
                }
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (HwGSMPhoneReference.SUB_NONE != SystemProperties.getInt("persist.radio.stack_id_0", HwGSMPhoneReference.SUB_NONE) && "android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
                    int subId = intent.getIntExtra("subscription", -1000);
                    int phoneId = intent.getIntExtra("phone", HwGSMPhoneReference.SPN_RULE);
                    int status = intent.getIntExtra("operationResult", HwGSMPhoneReference.NAME_INDEX);
                    int state = intent.getIntExtra("newSubState", HwGSMPhoneReference.SPN_RULE);
                    Rlog.d(HwGSMPhoneReference.LOG_TAG, "Received ACTION_SUBSCRIPTION_SET_UICC_RESULT on subId: " + subId + "phoneId " + phoneId + " status: " + status + "state: " + state);
                    if (status == 0 && state == HwGSMPhoneReference.NAME_INDEX) {
                        HwGSMPhoneReference.this.mPhone.mCi.getDeviceIdentity(HwGSMPhoneReference.this.mPhone.obtainMessage(21));
                        HwGSMPhoneReference.this.mPhone.mCi.getIMEI(HwGSMPhoneReference.this.mPhone.obtainMessage(9));
                    }
                }
            }
        };
        this.preOperatorNumeric = "";
        this.mPhone = phone;
        this.subTag = "HwGSMPhoneReference[" + this.mPhone.getPhoneId() + "]";
        this.mPhoneId = this.mPhone.getPhoneId();
        this.mPhone.getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT"));
        this.mWifiManager = (WifiManager) this.mPhone.getContext().getSystemService("wifi");
        if (mWcdmaVpEnabled) {
            this.mHwVpApStatusHandler = new HwVpApStatusHandler(this.mPhone);
        }
        Object[] objArr = new Object[NAME_INDEX];
        objArr[SPN_RULE] = this.mPhone;
        this.mHwCustHwGSMPhoneReference = (HwCustHwGSMPhoneReference) HwCustUtils.createObj(HwCustHwGSMPhoneReference.class, objArr);
    }

    public void setLTEReleaseVersion(boolean state, Message response) {
        this.mPhone.mCi.setLTEReleaseVersion(state, response);
    }

    public int getLteReleaseVersion() {
        logd("getLteReleaseVersion: " + this.mLteReleaseVersion);
        return this.mLteReleaseVersion;
    }

    public void getCallbarringOption(String facility, String serviceClass, Message response) {
        logd("getCallbarringOption, facility=" + facility + ", serviceClass=" + serviceClass);
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhone.isUtEnable()) {
            this.mPhone.mCi.queryFacilityLock(facility, "", HwGsmMmiCode.siToServiceClass(serviceClass), response);
            return;
        }
        logd("getCallbarringOption via Ut");
        imsPhone.getCallBarring(facility, response);
    }

    public void setCallbarringOption(String facility, String password, boolean isActivate, String serviceClass, Message response) {
        logd("setCallbarringOption, facility=" + facility + ", isActivate=" + isActivate + ", serviceClass=" + serviceClass);
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhone.isUtEnable()) {
            this.mPhone.mCi.setFacilityLock(facility, isActivate, password, HwGsmMmiCode.siToServiceClass(serviceClass), response);
            return;
        }
        logd("setCallbarringOption via Ut");
        imsPhone.setCallBarring(facility, isActivate, password, response);
    }

    public void changeBarringPassword(String oldPassword, String newPassword, Message response) {
        this.mPhone.mCi.changeBarringPassword("AB", oldPassword, newPassword, response);
    }

    public Message getCustAvailableNetworksMessage(Message response) {
        return this.mHandler.obtainMessage(NAME_INDEX, response);
    }

    private String getSelectedApn() {
        Context mContext;
        String apn = null;
        if (this.mPhone != null) {
            mContext = this.mPhone.getContext();
        } else {
            mContext = null;
        }
        if (mContext == null) {
            return null;
        }
        Cursor cursor;
        ContentResolver contentResolver;
        Uri withAppendedId;
        String[] strArr;
        if (isMultiEnable) {
            int slotId = this.mPhone.getSubId();
            contentResolver = mContext.getContentResolver();
            withAppendedId = ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId);
            strArr = new String[ECC_WITHCARD_INDEX];
            strArr[SPN_RULE] = "_id";
            strArr[NAME_INDEX] = NumMatchs.NAME;
            strArr[NUMERIC_INDEX] = "apn";
            cursor = contentResolver.query(withAppendedId, strArr, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        } else {
            contentResolver = mContext.getContentResolver();
            withAppendedId = PREFERAPN_NO_UPDATE_URI;
            strArr = new String[ECC_WITHCARD_INDEX];
            strArr[SPN_RULE] = "_id";
            strArr[NAME_INDEX] = NumMatchs.NAME;
            strArr[NUMERIC_INDEX] = "apn";
            cursor = contentResolver.query(withAppendedId, strArr, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        }
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apn = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
        }
        if (cursor != null) {
            cursor.close();
        }
        logd("apn:" + apn);
        return apn;
    }

    public String getSelectedApnName() {
        Context mContext;
        String apnName = null;
        if (this.mPhone != null) {
            mContext = this.mPhone.getContext();
        } else {
            mContext = null;
        }
        if (mContext == null) {
            return null;
        }
        Cursor cursor;
        ContentResolver contentResolver;
        Uri withAppendedId;
        String[] strArr;
        if (isMultiEnable) {
            int slotId = this.mPhone.getSubId();
            contentResolver = mContext.getContentResolver();
            withAppendedId = ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId);
            strArr = new String[ECC_WITHCARD_INDEX];
            strArr[SPN_RULE] = "_id";
            strArr[NAME_INDEX] = NumMatchs.NAME;
            strArr[NUMERIC_INDEX] = "apn";
            cursor = contentResolver.query(withAppendedId, strArr, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        } else {
            contentResolver = mContext.getContentResolver();
            withAppendedId = PREFERAPN_NO_UPDATE_URI;
            strArr = new String[ECC_WITHCARD_INDEX];
            strArr[SPN_RULE] = "_id";
            strArr[NAME_INDEX] = NumMatchs.NAME;
            strArr[NUMERIC_INDEX] = "apn";
            cursor = contentResolver.query(withAppendedId, strArr, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        }
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnName = cursor.getString(cursor.getColumnIndexOrThrow(NumMatchs.NAME));
        }
        if (cursor != null) {
            cursor.close();
        }
        logd("apnname" + apnName);
        return apnName;
    }

    public String getCustOperatorName(String rplmn) {
        if (this.mPhone != null) {
            this.mSimRecords = (IccRecords) this.mPhone.mIccRecords.get();
        }
        String imsi = this.mSimRecords != null ? this.mSimRecords.getIMSI() : null;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        Object string = mContext != null ? Systemex.getString(mContext.getContentResolver(), "hw_cust_operator") : null;
        logd("mCustOperatorString" + string);
        if (TextUtils.isEmpty(string) || TextUtils.isEmpty(imsi)) {
            return null;
        }
        if (TextUtils.isEmpty(rplmn)) {
            return null;
        }
        String[] items = string.split(";");
        String operatorName = null;
        int maxLength = SPN_RULE;
        int length = items.length;
        for (int i = SPN_RULE; i < length; i += NAME_INDEX) {
            String[] plmns = items[i].split(",");
            if (plmns.length == ECC_WITHCARD_INDEX) {
                if (rplmn.equals(plmns[NAME_INDEX]) && ((imsi.startsWith(plmns[SPN_RULE]) || "0".equals(plmns[SPN_RULE])) && plmns[SPN_RULE].length() > maxLength)) {
                    operatorName = plmns[NUMERIC_INDEX];
                    maxLength = plmns[SPN_RULE].length();
                    logd("operatorName changed" + operatorName);
                }
            } else if (plmns.length == 5) {
                if (rplmn.equals(plmns[ECC_WITHCARD_INDEX]) && imsi.startsWith(plmns[NAME_INDEX])) {
                    if (!"0".equals(plmns[SPN_RULE]) || !isSpnGid1ApnnameMatched(plmns[NUMERIC_INDEX], SPN_RULE)) {
                        if (!"1".equals(plmns[SPN_RULE]) || !isSpnGid1ApnnameMatched(plmns[NUMERIC_INDEX], NAME_INDEX)) {
                            if (!"2".equals(plmns[SPN_RULE]) || !isSpnGid1ApnnameMatched(plmns[NUMERIC_INDEX], NUMERIC_INDEX)) {
                                if ("3".equals(plmns[SPN_RULE]) && isSpnGid1ApnnameMatched(plmns[NUMERIC_INDEX], ECC_WITHCARD_INDEX)) {
                                    operatorName = plmns[ECC_NOCARD_INDEX];
                                    logd("operatorName changed by Apnname confirg");
                                    break;
                                }
                            }
                            operatorName = plmns[ECC_NOCARD_INDEX];
                            logd("operatorName changed by Apn confirg");
                            break;
                        }
                        operatorName = plmns[ECC_NOCARD_INDEX];
                        logd("operatorName changed by Gid1 confirg");
                        break;
                    }
                    operatorName = plmns[ECC_NOCARD_INDEX];
                    logd("operatorName changed by spn confirg");
                    break;
                }
            } else {
                logd("Wrong length");
            }
        }
        return operatorName;
    }

    public boolean isSpnGid1ApnnameMatched(String spnorgid1orapnname, int rule) {
        if (this.mSimRecords == null || spnorgid1orapnname == null) {
            return HW_VM_NOT_FROM_SIM;
        }
        if (rule == 0) {
            String spn = this.mSimRecords.getServiceProviderName();
            logd("[EONS] spn = " + spn + ", spnorgid1orapnname(gid1 start with 0x) = " + spnorgid1orapnname);
            if (!spnorgid1orapnname.equals(spn)) {
                return HW_VM_NOT_FROM_SIM;
            }
            logd("[EONS] ShowPLMN: use the spn rule");
            return true;
        } else if (rule == NAME_INDEX) {
            byte[] gid1 = this.mSimRecords.getGID1();
            if (gid1 == null || gid1.length <= 0 || spnorgid1orapnname.length() <= NUMERIC_INDEX || !spnorgid1orapnname.substring(SPN_RULE, NUMERIC_INDEX).equalsIgnoreCase("0x")) {
                return HW_VM_NOT_FROM_SIM;
            }
            logd("[EONS] gid1 = " + gid1[SPN_RULE]);
            byte[] gid1valueBytes = IccUtils.hexStringToBytes(spnorgid1orapnname.substring(NUMERIC_INDEX));
            int i = SPN_RULE;
            while (i < gid1.length && i < gid1valueBytes.length) {
                if (gid1[i] != gid1valueBytes[i]) {
                    return HW_VM_NOT_FROM_SIM;
                }
                i += NAME_INDEX;
            }
            logd("[EONS] ShowPLMN: use the Gid1 rule");
            return true;
        } else if (rule == NUMERIC_INDEX) {
            String apn = getSelectedApn();
            logd("[EONS] apn = " + apn);
            if (!spnorgid1orapnname.equals(apn)) {
                return HW_VM_NOT_FROM_SIM;
            }
            logd("[EONS] ShowPLMN: use the apn rule");
            return true;
        } else if (rule != ECC_WITHCARD_INDEX) {
            return HW_VM_NOT_FROM_SIM;
        } else {
            String apnname = getSelectedApnName();
            logd("[EONS] apnname = " + apnname);
            if (!spnorgid1orapnname.equals(apnname)) {
                return HW_VM_NOT_FROM_SIM;
            }
            logd("[EONS] ShowPLMN: use the apnname rule");
            return true;
        }
    }

    private ArrayList<OperatorInfo> getEonsForAvailableNetworks(ArrayList<OperatorInfo> searchResult) {
        if (HwModemCapability.isCapabilitySupport(5)) {
            return searchResult;
        }
        IccRecords rrecords = (IccRecords) this.mPhone.mIccRecords.get();
        if (rrecords != null) {
            ArrayList<OperatorInfo> eonsNetworkNames = rrecords.getEonsForAvailableNetworks(searchResult);
            if (eonsNetworkNames != null) {
                searchResult = eonsNetworkNames;
            }
        }
        return searchResult;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<OperatorInfo> custAvaliableNetworks(ArrayList<OperatorInfo> searchResult) {
        if (searchResult == null || searchResult.size() == 0) {
            return searchResult;
        }
        if (!SystemProperties.getBoolean("ro.config.srch_net_sim_ue_pri", HW_VM_NOT_FROM_SIM)) {
            searchResult = getEonsForAvailableNetworks(searchResult);
        }
        ArrayList<OperatorInfo> custResults = new ArrayList();
        String radioTechStr = "";
        for (OperatorInfo operatorInfo : searchResult) {
            String plmn = operatorInfo.getOperatorNumericWithoutAct();
            if (plmn != null) {
                int plmnEnd = plmn.indexOf(",");
                if (plmnEnd > 0) {
                    plmn = plmn.substring(SPN_RULE, plmnEnd);
                }
            }
            String str = null;
            String plmnRadioTechStr = "";
            String longNameWithoutAct = null;
            IccRecords iccRecords = (IccRecords) this.mPhone.mIccRecords.get();
            if (iccRecords != null) {
                String hplmn = iccRecords.getOperatorNumeric();
                int lastSpaceIndexInLongName = operatorInfo.getOperatorAlphaLong().lastIndexOf(32);
                if (SUB_NONE != lastSpaceIndexInLongName) {
                    radioTechStr = operatorInfo.getOperatorAlphaLong().substring(lastSpaceIndexInLongName);
                    if (!" 2G".equals(radioTechStr)) {
                        if (!" 3G".equals(radioTechStr)) {
                            if (!" 4G".equals(radioTechStr)) {
                                radioTechStr = "";
                            }
                        }
                    }
                    longNameWithoutAct = operatorInfo.getOperatorAlphaLong().substring(SPN_RULE, lastSpaceIndexInLongName);
                } else {
                    longNameWithoutAct = operatorInfo.getOperatorAlphaLong();
                }
                int lastSpaceIndexInPlmn = operatorInfo.getOperatorNumeric().lastIndexOf(32);
                if (SUB_NONE != lastSpaceIndexInPlmn) {
                    plmnRadioTechStr = operatorInfo.getOperatorNumeric().substring(lastSpaceIndexInPlmn);
                    if (!" 2G".equals(plmnRadioTechStr)) {
                        if (!" 3G".equals(plmnRadioTechStr)) {
                        }
                    }
                    if (plmn != null) {
                        plmn = plmn.substring(SPN_RULE, lastSpaceIndexInPlmn);
                    }
                }
                String str2 = null;
                if (plmn != null) {
                    str2 = getCustOperatorName(plmn);
                    if (this.mHwCustHwGSMPhoneReference != null) {
                        str2 = this.mHwCustHwGSMPhoneReference.getCustOperatorNameBySpn(plmn, str2);
                    }
                }
                if (str2 != null) {
                    str = str2.concat(radioTechStr);
                    if (this.mHwCustHwGSMPhoneReference != null) {
                        str = this.mHwCustHwGSMPhoneReference.modifyTheFormatName(plmn, str2, radioTechStr);
                    }
                }
                if ("50503".equals(plmn)) {
                    if ("50503".equals(hplmn) && (iccRecords.getDisplayRule(plmn) & NAME_INDEX) == NAME_INDEX) {
                        String spn = iccRecords.getServiceProviderName();
                        if (spn != null && spn.trim().length() > 0) {
                            str = spn.concat(radioTechStr);
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(longNameWithoutAct)) {
                Object obj = null;
                try {
                    obj = Systemex.getString(this.mPhone.getContext().getContentResolver(), "plmn");
                    logd("plmn config = " + obj);
                } catch (Exception e) {
                    loge("Exception when got data value", e);
                }
                if (!TextUtils.isEmpty(obj)) {
                    PlmnConstants plmnConstants = new PlmnConstants(obj);
                    String longNameCust = plmnConstants.getPlmnValue(plmn, Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry());
                    if (longNameCust == null) {
                        String DEFAULT_LOCALE = "en_US";
                        longNameCust = plmnConstants.getPlmnValue(plmn, "en_US");
                    }
                    logd("longName = " + str + ", longNameCust = " + longNameCust);
                    if (str == null && longNameCust != null) {
                        str = longNameCust.concat(radioTechStr);
                    }
                }
            }
            if (str != null) {
                custResults.add(new OperatorInfo(str, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState()));
            } else {
                custResults.add(operatorInfo);
            }
        }
        if (this.mHwCustHwGSMPhoneReference != null) {
            custResults = this.mHwCustHwGSMPhoneReference.filterActAndRepeatedItems(custResults);
        }
        return custResults;
    }

    public String getDefaultVoiceMailAlphaTagText(Context mContext, String ret) {
        if (HW_VM_NOT_FROM_SIM || ret == null || ret.length() == 0 || isCustVoicemailTag(mContext)) {
            return mContext.getText(17039364).toString();
        }
        return ret;
    }

    private boolean isCustVoicemailTag(Context mContext) {
        String strVMTagNotFromConf = Systemex.getString(mContext.getContentResolver(), "hw_vmtag_follow_language");
        String carrier = "";
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            carrier = TelephonyManager.getDefault().getSimOperator(this.mPhoneId);
        } else {
            carrier = TelephonyManager.getDefault().getSimOperator();
        }
        if (!(TextUtils.isEmpty(strVMTagNotFromConf) || TextUtils.isEmpty(carrier))) {
            String[] areaArray = strVMTagNotFromConf.split(",");
            int length = areaArray.length;
            for (int i = SPN_RULE; i < length; i += NAME_INDEX) {
                if (areaArray[i].equals(carrier)) {
                    logd("voicemail Tag need follow language");
                    return true;
                }
            }
        }
        logd("voicemail Tag not need follow language");
        return HW_VM_NOT_FROM_SIM;
    }

    public String getMeid() {
        logd("[HwGSMPhoneReference]getMeid()");
        return this.mMeid;
    }

    public String getPesn() {
        logd("[HwGSMPhoneReference]getPesn()");
        return this.mPESN;
    }

    public boolean beforeHandleMessage(Message msg) {
        boolean result;
        logd("beforeHandleMessage what = " + msg.what);
        AsyncResult ar;
        switch (msg.what) {
            case HwVSimConstants.EVENT_SET_APN_READY_DONE /*21*/:
                logd("handleMessage EVENT_GET_DEVICE_IDENTITY_DONE");
                result = true;
                ar = msg.obj;
                if (ar.exception == null) {
                    String[] respId = ar.result;
                    if (respId != null && respId.length >= ECC_NOCARD_INDEX) {
                        logd("handleMessage respId.length = " + respId.length);
                        this.mPESN = respId[NUMERIC_INDEX];
                        this.mMeid = respId[ECC_WITHCARD_INDEX];
                    }
                    if (this.mMeid != null && this.mMeid.length() > MEID_LENGTH) {
                        this.mMeid = this.mMeid.substring(this.mMeid.length() - 14);
                    }
                    logd("[HwGSMPhoneReference]mPESN,mMeid");
                    break;
                }
                this.mPhone.retryGetDeviceId(msg.arg1, NUMERIC_INDEX);
                break;
            case 105:
                updateReduceSARState();
                result = true;
                break;
            case 108:
                logd("onGetLteReleaseVersionDone:");
                ar = (AsyncResult) msg.obj;
                result = true;
                if (ar.exception == null) {
                    int[] resultint = ar.result;
                    if (resultint.length != 0) {
                        logd("onGetLteReleaseVersionDone: result=" + resultint[SPN_RULE]);
                        switch (resultint[SPN_RULE]) {
                            case SPN_RULE /*0*/:
                                this.mLteReleaseVersion = SPN_RULE;
                                break;
                            case NAME_INDEX /*1*/:
                                this.mLteReleaseVersion = NAME_INDEX;
                                break;
                            default:
                                this.mLteReleaseVersion = SUB_NONE;
                                break;
                        }
                    }
                }
                logd("Error in get lte release version:" + ar.exception);
                break;
                break;
            case 111:
                logd("beforeHandleMessage handled->EVENT_SET_MODE_TO_AUTO ");
                result = true;
                this.mPhone.setNetworkSelectionModeAutomatic(null);
                break;
            case HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE /*1000*/:
                if (msg.arg2 == NAME_INDEX) {
                    logd("start retry get DEVICE_ID_MASK_IMEI");
                    this.mPhone.mCi.getIMEI(this.mPhone.obtainMessage(9, msg.arg1, SPN_RULE, null));
                } else if (msg.arg2 == NUMERIC_INDEX) {
                    logd("start retry get DEVICE_ID_MASK_ALL");
                    this.mPhone.mCi.getDeviceIdentity(this.mPhone.obtainMessage(21, msg.arg1, SPN_RULE, null));
                } else {
                    logd("EVENT_RETRY_GET_DEVICE_ID msg.arg2:" + msg.arg2 + ", error!!");
                }
                result = true;
                break;
            default:
                return super.beforeHandleMessage(msg);
        }
        return result;
    }

    public void afterHandleMessage(Message msg) {
        logd("afterHandleMessage what = " + msg.what);
        switch (msg.what) {
            case NAME_INDEX /*1*/:
                this.mPhone.mCi.getDeviceIdentity(this.mPhone.obtainMessage(21));
                logd("[HwGSMPhoneReference]PhoneBaseUtils.EVENT_RADIO_AVAILABLE");
                logd("Radio available, get lte release version");
                this.mPhone.mCi.getLteReleaseVersion(this.mPhone.obtainMessage(108));
            default:
                logd("unhandle event");
        }
    }

    public void closeRrc() {
        if (mIsQCRilGoDormant) {
            qcRilGoDormant();
            if (!mIsQCRilGoDormant) {
                closeRrcInner();
                return;
            }
            return;
        }
        closeRrcInner();
    }

    private void closeRrcInner() {
        try {
            logd("closeRrcInner in GSMPhone");
            this.mPhone.mCi.getClass().getMethod("closeRrc", new Class[SPN_RULE]).invoke(this.mPhone.mCi, new Object[SPN_RULE]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void qcRilGoDormant() {
        try {
            if (mQcRilHook == null) {
                Class c = Class.forName("com.qualcomm.qcrilhook.QcRilHook");
                Class[] paramTypes = new Class[NAME_INDEX];
                paramTypes[SPN_RULE] = Context.class;
                Object[] params = new Object[NAME_INDEX];
                params[SPN_RULE] = this.mPhone.getContext();
                mQcRilHook = c.getConstructor(paramTypes).newInstance(params);
            }
            if (mQcRilHook != null) {
                Class[] clsArr = new Class[NAME_INDEX];
                clsArr[SPN_RULE] = String.class;
                Method qcRilGoDormant = mQcRilHook.getClass().getMethod("qcRilGoDormant", clsArr);
                Object obj = mQcRilHook;
                Object[] objArr = new Object[NAME_INDEX];
                objArr[SPN_RULE] = "";
                qcRilGoDormant.invoke(obj, objArr);
                return;
            }
            logd("mQcRilHook is null");
            mIsQCRilGoDormant = HW_VM_NOT_FROM_SIM;
        } catch (ClassNotFoundException e) {
            mIsQCRilGoDormant = HW_VM_NOT_FROM_SIM;
            loge("the class QcRilHook not exist");
        } catch (NoSuchMethodException e2) {
            mIsQCRilGoDormant = HW_VM_NOT_FROM_SIM;
            loge("the class QcRilHook NoSuchMethod: qcRilGoDormant [String]");
        } catch (LinkageError e3) {
            mIsQCRilGoDormant = HW_VM_NOT_FROM_SIM;
            loge("class QcRilHook LinkageError", e3);
        } catch (InstantiationException e4) {
            mIsQCRilGoDormant = HW_VM_NOT_FROM_SIM;
            loge("class QcRilHook InstantiationException", e4);
        } catch (IllegalAccessException e5) {
            mIsQCRilGoDormant = HW_VM_NOT_FROM_SIM;
            loge("class QcRilHook IllegalAccessException", e5);
        } catch (IllegalArgumentException e6) {
            mIsQCRilGoDormant = HW_VM_NOT_FROM_SIM;
            loge("class QcRilHook IllegalArgumentException", e6);
        } catch (InvocationTargetException e7) {
            mIsQCRilGoDormant = HW_VM_NOT_FROM_SIM;
            loge("class QcRilHook InvocationTargetException", e7);
        }
    }

    public void setMeid(String meid) {
        this.mMeid = meid;
    }

    private int getCardType() {
        this.redcueSARSpeacialType1 = SystemProperties.get(PROP_REDUCE_SAR_SPECIAL_TYPE1, " ");
        this.redcueSARSpeacialType2 = SystemProperties.get(PROP_REDUCE_SAR_SPECIAL_TYPE2, " ");
        this.mncForSAR = SystemProperties.get("reduce.sar.imsi.mnc", "FFFFF");
        if (this.mncForSAR == null || this.mncForSAR.equals("FFFFF") || this.mncForSAR.length() < ECC_WITHCARD_INDEX) {
            return SPN_RULE;
        }
        this.mncForSAR = this.mncForSAR.substring(SPN_RULE, ECC_WITHCARD_INDEX);
        if (this.redcueSARSpeacialType1.contains(this.mncForSAR)) {
            return NAME_INDEX;
        }
        if (this.redcueSARSpeacialType2.contains(this.mncForSAR)) {
            return NUMERIC_INDEX;
        }
        return SPN_RULE;
    }

    private int getWifiType(int wifiHotState, int wifiState) {
        if (wifiHotState == 13) {
            return NUMERIC_INDEX;
        }
        if (wifiState == ECC_WITHCARD_INDEX) {
            return NAME_INDEX;
        }
        return wifiState == NAME_INDEX ? SPN_RULE : SPN_RULE;
    }

    private int[][] getGradeData(boolean isReceiveOFF) {
        if (isReceiveOFF) {
            int[][] iArr = new int[ECC_WITHCARD_INDEX][];
            iArr[SPN_RULE] = new int[]{REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON};
            iArr[NAME_INDEX] = new int[]{REDUCE_SAR_SPECIAL_TYPE1_WIFI_OFF, REDUCE_SAR_SPECIAL_TYPE1_WIFI_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON};
            iArr[NUMERIC_INDEX] = new int[]{REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON};
            return iArr;
        }
        iArr = new int[ECC_WITHCARD_INDEX][];
        iArr[SPN_RULE] = new int[]{SPN_RULE, REDUCE_SAR_NORMAL_WIFI_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON};
        iArr[NAME_INDEX] = new int[]{REDUCE_SAR_SPECIAL_TYPE1_WIFI_OFF, REDUCE_SAR_SPECIAL_TYPE1_WIFI_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON};
        iArr[NUMERIC_INDEX] = new int[]{REDUCE_SAR_SPECIAL_TYPE2_WIFI_OFF, REDUCE_SAR_SPECIAL_TYPE2_WIFI_ON, REDUCE_SAR_SPECIAL_TYPE2_HOTSPOT_ON};
        return iArr;
    }

    private void setPowerGrade(int powerGrade) {
        if (this.mPrevPowerGrade != powerGrade) {
            this.mPrevPowerGrade = powerGrade;
            logd("updateReduceSARState()    setPowerGrade() : " + powerGrade);
            this.mPhone.mCi.setPowerGrade(powerGrade, null);
        }
    }

    private boolean reduceOldSar() {
        if (!SystemProperties.getBoolean("ro.config.old_reduce_sar", HW_VM_NOT_FROM_SIM)) {
            return HW_VM_NOT_FROM_SIM;
        }
        int wifiHotState = this.mWifiManager.getWifiApState();
        logd("Radio available, set wifiHotState  " + wifiHotState);
        if (wifiHotState == 13) {
            setPowerGrade(NAME_INDEX);
        } else if (wifiHotState == 11) {
            setPowerGrade(SPN_RULE);
        }
        return true;
    }

    public void updateReduceSARState() {
        if (this.mSarControlServiceExist == null) {
            try {
                this.mSarControlServiceExist = this.mPhone.getContext().getPackageManager().getApplicationInfo("com.huawei.sarcontrolservice", REDUCE_SAR_NORMAL_HOTSPOT_ON) == null ? Boolean.valueOf(HW_VM_NOT_FROM_SIM) : Boolean.valueOf(true);
                logd("updateReduceSARState mSarControlServiceExist=" + this.mSarControlServiceExist);
                if (this.mSarControlServiceExist.booleanValue()) {
                    logd("updateReduceSARState mSarControlServiceExist=true");
                    return;
                }
            } catch (NameNotFoundException e) {
                this.mSarControlServiceExist = Boolean.valueOf(HW_VM_NOT_FROM_SIM);
                logd("updateReduceSARState mSarControlServiceExist=false");
            }
        } else if (this.mSarControlServiceExist.booleanValue()) {
            return;
        }
        logd("updateReduceSARState()");
        if (!reduceOldSar() && SystemProperties.getBoolean("ro.config.hw_ReduceSAR", HW_VM_NOT_FROM_SIM)) {
            AudioManager localAudioManager = (AudioManager) this.mPhone.getContext().getSystemService("audio");
            int cardType = getCardType();
            int wifiType = getWifiType(this.mWifiManager.getWifiApState(), this.mWifiManager.getWifiState());
            int state = TelephonyManager.getDefault().getCallState();
            boolean isWiredHeadsetOn = localAudioManager.isWiredHeadsetOn();
            boolean isSpeakerphoneOn = localAudioManager.isSpeakerphoneOn();
            boolean isReceiveOFF = (isWiredHeadsetOn || isSpeakerphoneOn || state != NUMERIC_INDEX) ? true : HW_VM_NOT_FROM_SIM;
            int reduceData = getGradeData(isReceiveOFF)[cardType][wifiType];
            logd("updateReduceSARState(); isWiredHeadsetOn=" + isWiredHeadsetOn + ", isSpeakerphoneOn=" + isSpeakerphoneOn + ", state=" + state + ", reduceData=" + reduceData);
            setPowerGrade(reduceData);
        }
    }

    public void dispose() {
        if (mWcdmaVpEnabled && this.mHwVpApStatusHandler != null) {
            this.mHwVpApStatusHandler.dispose();
        }
        this.mPhone.getContext().unregisterReceiver(this.mReceiver);
    }

    public void switchVoiceCallBackgroundState(int state) {
        this.mPhone.mCT.switchVoiceCallBackgroundState(state);
    }

    public boolean isMmiCode(String dialString, UiccCardApplication app) {
        if (GsmMmiCode.newFromDialString(dialString, this.mPhone, app) != null) {
            return true;
        }
        boolean result = HW_VM_NOT_FROM_SIM;
        switch (dialString.charAt(SPN_RULE)) {
            case HwVSimConstants.EVENT_GET_PREFERRED_NETWORK_TYPE_DONE /*48*/:
                if (dialString.length() == NAME_INDEX) {
                    result = true;
                    break;
                }
                break;
            case HwVSimConstants.EVENT_SET_PREFERRED_NETWORK_TYPE_DONE /*49*/:
                if (dialString.length() <= NUMERIC_INDEX) {
                    result = true;
                    break;
                }
                break;
            case HwVSimConstants.EVENT_NETWORK_CONNECTED /*50*/:
                if (dialString.length() <= NUMERIC_INDEX) {
                    result = true;
                    break;
                }
                break;
            case HwVSimConstants.EVENT_ENABLE_VSIM_DONE /*51*/:
                if (dialString.length() == NAME_INDEX) {
                    result = true;
                    break;
                }
                break;
            case HwVSimConstants.CMD_DISABLE_VSIM /*52*/:
                if (dialString.length() == NAME_INDEX) {
                    result = true;
                    break;
                }
                break;
            case HwVSimConstants.EVENT_DISABLE_VSIM_DONE /*53*/:
                if (dialString.length() == NAME_INDEX) {
                    result = true;
                    break;
                }
                break;
        }
        return result;
    }

    public void getPOLCapabilty(Message response) {
        this.mPhone.mCi.getPOLCapabilty(response);
    }

    public void getPreferedOperatorList(Message response) {
        this.mPhone.mCi.getCurrentPOLList(response);
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        this.mPhone.mCi.setPOLEntry(index, numeric, nAct, response);
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public String processPlusSymbol(String dialNumber, String imsi) {
        if (TextUtils.isEmpty(dialNumber) || !dialNumber.startsWith("+") || TextUtils.isEmpty(imsi) || imsi.length() > 15) {
            return dialNumber;
        }
        return PhoneNumberUtils.convertPlusByMcc(dialNumber, Integer.parseInt(imsi.substring(SPN_RULE, ECC_WITHCARD_INDEX)));
    }

    public boolean isUtEnable() {
        return ((ImsPhone) this.mPhone.getImsPhone()).mHwImsPhone.isUtEnable();
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone != null && imsPhone.mHwImsPhone.isUtEnable()) {
            logd("getOutgoingCallerIdDisplay via Ut");
            imsPhone.mHwImsPhone.getOutgoingCallerIdDisplay(onComplete);
        }
    }

    public boolean isSupportCFT() {
        boolean isSupportCFT = HW_VM_NOT_FROM_SIM;
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone != null) {
            logd("imsPhone is exist, isSupportCFT");
            isSupportCFT = imsPhone.mHwImsPhone.isSupportCFT();
        }
        logd("isSupportCFT=" + isSupportCFT);
        return isSupportCFT;
    }

    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhone.isUtEnable()) {
            logd("setCallForwardingUncondTimerOption can not go Ut interface, imsPhone=" + imsPhone);
            if (onComplete != null) {
                AsyncResult.forMessage(onComplete, null, new CommandException(Error.GENERIC_FAILURE));
                onComplete.sendToTarget();
                return;
            }
            return;
        }
        logd("setCallForwardingUncondTimerOption via Ut");
        imsPhone.mHwImsPhone.setCallForwardingUncondTimerOption(startHour, startMinute, endHour, endMinute, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, onComplete);
    }

    public void setImsSwitch(boolean on) {
        this.mPhone.mCi.setImsSwitch(on);
    }

    public boolean getImsSwitch() {
        return this.mPhone.mCi.getImsSwitch();
    }

    public void processEccNunmber(ServiceStateTracker gSST) {
        if (SystemProperties.getBoolean("ro.config.hw_globalEcc", HW_VM_NOT_FROM_SIM)) {
            logd("EVENT_SIM_RECORDS_LOADED!!!!");
            String hlpmn = TelephonyManager.getDefault().getSimOperator(this.mPhoneId);
            if (hlpmn.equals("")) {
                logd("received EVENT_SIM_RECORDS_LOADED but not hplmn !!!!");
            } else {
                globalEccCustom(hlpmn);
            }
        }
    }

    public void globalEccCustom(String operatorNumeric) {
        String str = null;
        String str2 = null;
        String forceEccState = SystemProperties.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: operator numeric = " + operatorNumeric + "; preOperatorNumeric = " + this.preOperatorNumeric + ";forceEccState  = " + forceEccState);
        if (!(operatorNumeric.equals("") || (operatorNumeric.equals(this.preOperatorNumeric) && forceEccState.equals("invalid")))) {
            this.preOperatorNumeric = operatorNumeric;
            SystemProperties.set(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
            if (HwTelephonyFactory.getHwPhoneManager().isSupportEccFormVirtualNet()) {
                str = HwTelephonyFactory.getHwPhoneManager().getVirtualNetEccWihCard(this.mPhoneId);
                str2 = HwTelephonyFactory.getHwPhoneManager().getVirtualNetEccNoCard(this.mPhoneId);
                logd("try to get Ecc form virtualNet ecclist_withcard=" + str + " ecclist_nocard=" + str2);
            }
            String custEcc = getCustEccList(operatorNumeric);
            if (!TextUtils.isEmpty(custEcc)) {
                String[] custEccArray = custEcc.split(":");
                if (custEccArray.length == ECC_WITHCARD_INDEX && custEccArray[SPN_RULE].equals(operatorNumeric) && !TextUtils.isEmpty(custEccArray[NAME_INDEX]) && !TextUtils.isEmpty(custEccArray[NUMERIC_INDEX])) {
                    str = custEccArray[NAME_INDEX];
                    str2 = custEccArray[NUMERIC_INDEX];
                }
            }
            if (str == null) {
                String where = "numeric=\"" + operatorNumeric + "\"";
                Cursor cursor = this.mPhone.getContext().getContentResolver().query(GlobalMatchs.CONTENT_URI, new String[]{"_id", NumMatchs.NAME, VirtualNets.NUMERIC, VirtualNets.ECC_WITH_CARD, VirtualNets.ECC_NO_CARD}, where, null, NumMatchs.DEFAULT_SORT_ORDER);
                if (cursor == null) {
                    logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: No matched emergency numbers in db.");
                    this.mPhone.mCi.requestSetEmergencyNumbers("", "");
                    return;
                }
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        str = cursor.getString(ECC_WITHCARD_INDEX);
                        str2 = cursor.getString(ECC_NOCARD_INDEX);
                        cursor.moveToNext();
                    }
                } catch (Exception ex) {
                    logd("[SLOT" + this.mPhoneId + "]globalEccCustom: global version cause exception!" + ex.toString());
                } finally {
                    cursor.close();
                }
            }
            logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: ecc_withcard = " + str + ", ecc_nocard = " + str2);
            if ((str == null || str.equals("")) && (str2 == null || str2.equals(""))) {
                this.mPhone.mCi.requestSetEmergencyNumbers("", "");
            } else {
                this.mPhone.mCi.requestSetEmergencyNumbers(str, str2);
            }
        }
    }

    public String getHwCdmaPrlVersion() {
        int subId = this.mPhone.getSubId();
        String prlVersion = "0";
        int simCardState = TelephonyManager.getDefault().getSimState(subId);
        if (5 == simCardState && HwTelephonyManagerInner.getDefault().isCDMASimCard(subId)) {
            prlVersion = this.mPhone.mCi.getHwPrlVersion();
        } else {
            prlVersion = "0";
        }
        logd("getHwCdmaPrlVersion: prlVersion=" + prlVersion + ", subid=" + subId + ", simState=" + simCardState);
        return prlVersion;
    }

    public String getHwCdmaEsn() {
        int subId = this.mPhone.getSubId();
        String esn = "0";
        int simCardState = TelephonyManager.getDefault().getSimState(subId);
        if (5 == simCardState && HwTelephonyManagerInner.getDefault().isCDMASimCard(subId)) {
            esn = this.mPhone.mCi.getHwUimid();
        } else {
            esn = "0";
        }
        logd("getHwCdmaEsn: esn=" + esn + ", subid=" + subId + ", simState=" + simCardState);
        return esn;
    }

    public String getVMNumberWhenIMSIChange() {
        if (this.mPhone == null) {
            return null;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext());
        String str = null;
        Object mIccId = null;
        if (this.mPhone != null) {
            mIccId = this.mPhone.getIccSerialNumber();
        }
        if (!TextUtils.isEmpty(mIccId)) {
            String temp = sp.getString(mIccId + this.mPhone.getPhoneId(), null);
            if (temp != null) {
                str = temp;
            }
            logd("getVMNumberWhenIMSIChange number= xxxxxx");
        }
        return str;
    }

    public boolean setISMCOEX(String setISMCoex) {
        this.mPhone.mCi.setISMCOEX(setISMCoex, null);
        return true;
    }

    private String getCustEccList(String operatorNumeric) {
        Object custEccList = null;
        String matchEccList = "";
        try {
            custEccList = Systemex.getString(this.mPhone.getContext().getContentResolver(), "hw_cust_emergency_nums");
        } catch (Exception e) {
            loge("Failed to load vmNum from SettingsEx", e);
        }
        if (TextUtils.isEmpty(custEccList) || TextUtils.isEmpty(operatorNumeric)) {
            return matchEccList;
        }
        String[] custEccListItems = custEccList.split(";");
        for (int i = SPN_RULE; i < custEccListItems.length; i += NAME_INDEX) {
            String[] custItem = custEccListItems[i].split(":");
            if (custItem.length == ECC_WITHCARD_INDEX && custItem[SPN_RULE].equals(operatorNumeric)) {
                matchEccList = custEccListItems[i];
                break;
            }
        }
        return matchEccList;
    }

    public void setImsDomainConfig(int domainType) {
        this.mPhone.mCi.setImsDomainConfig(domainType, null);
    }

    public void getImsDomain(Message response) {
        this.mPhone.mCi.getImsDomain(response);
    }

    public void handleUiccAuth(int auth_type, byte[] rand, byte[] auth, Message response) {
        this.mPhone.mCi.handleUiccAuth(auth_type, rand, auth, response);
    }

    public void handleMapconImsaReq(byte[] Msg) {
        this.mPhone.mCi.handleMapconImsaReq(Msg, null);
    }

    private void logd(String msg) {
        Rlog.d(this.subTag, msg);
    }

    private void loge(String msg) {
        Rlog.e(this.subTag, msg);
    }

    private void loge(String msg, Throwable tr) {
        Rlog.e(this.subTag, msg, tr);
    }

    public void selectCsgNetworkManually(Message response) {
        if (this.mHwCustHwGSMPhoneReference != null) {
            this.mHwCustHwGSMPhoneReference.selectCsgNetworkManually(response);
        }
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        if (this.mHwCustHwGSMPhoneReference != null) {
            this.mHwCustHwGSMPhoneReference.judgeToLaunchCsgPeriodicSearchTimer();
        }
    }

    public void registerForCsgRecordsLoadedEvent() {
        if (this.mHwCustHwGSMPhoneReference != null) {
            this.mHwCustHwGSMPhoneReference.registerForCsgRecordsLoadedEvent();
        }
    }

    public void unregisterForCsgRecordsLoadedEvent() {
        if (this.mHwCustHwGSMPhoneReference != null) {
            this.mHwCustHwGSMPhoneReference.unregisterForCsgRecordsLoadedEvent();
        }
    }
}
