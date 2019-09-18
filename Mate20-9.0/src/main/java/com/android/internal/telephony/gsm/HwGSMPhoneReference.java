package com.android.internal.telephony.gsm;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.HwTelephony;
import android.provider.IHwTelephonyEx;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.android.ims.ImsException;
import com.android.internal.telephony.AbstractGsmCdmaPhone;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPhoneReferenceBase;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.PlmnConstants;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.vsim.HwVSimConstants;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Locale;

public class HwGSMPhoneReference extends HwPhoneReferenceBase implements AbstractGsmCdmaPhone.GSMPhoneReference {
    private static final int APNNAME_RULE = 3;
    private static final int APN_RULE = 2;
    private static final int ECC_NOCARD_INDEX = 4;
    private static final int ECC_WITHCARD_INDEX = 3;
    private static final int EVENT_GET_AVALIABLE_NETWORKS_DONE = 1;
    private static final int EVENT_RADIO_ON = 5;
    private static final int GID1_RULE = 1;
    protected static final String LOG_TAG = "HwGSMPhoneReference";
    private static final int MEID_LENGTH = 14;
    private static final int NAME_INDEX = 1;
    private static final int NUMERIC_INDEX = 2;
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
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
    private static final int SEARCH_LENGTH = 2;
    private static final int SPN_RULE = 0;
    public static final int SUB_NONE = -1;
    private static final String USSD_IS_OK_FOR_RELEASE = "is_ussd_ok_for_nw_release";
    private static final int WIFI_HOTSPOT_TYPE = 2;
    private static final int WIFI_OFF_TYPE = 0;
    private static final int WIFI_ON_TYPE = 1;
    private static final boolean isMultiEnable = TelephonyManager.getDefault().isMultiSimEnabled();
    private static boolean mIsQCRilGoDormant = true;
    private static Object mQcRilHook = null;
    private static final boolean mWcdmaVpEnabled = SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true");
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                super.handleMessage(msg);
            } else {
                handleGetAvaliableNetWorksDone(msg);
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: java.util.ArrayList} */
        /* JADX WARNING: Multi-variable type inference failed */
        private void handleGetAvaliableNetWorksDone(Message msg) {
            HwGSMPhoneReference.this.logd("[EVENT_GET_AVALIABLE_NETWORKS_DONE]");
            ArrayList<OperatorInfo> searchResults = null;
            AsyncResult ar = (AsyncResult) msg.obj;
            Message onComplete = (Message) ar.userObj;
            if (ar.exception == null && ar.result != null) {
                searchResults = HwGSMPhoneReference.this.custAvaliableNetworks((ArrayList) ar.result);
            }
            if (searchResults != null) {
                HwGSMPhoneReference.this.logd("[EVENT_GET_NETWORKS_DONE] Populated global version cust names for available networks.");
            } else {
                searchResults = ar.result;
            }
            if (onComplete != null) {
                AsyncResult.forMessage(onComplete, searchResults, ar.exception);
                if (onComplete.getTarget() != null) {
                    onComplete.sendToTarget();
                    return;
                }
                return;
            }
            HwGSMPhoneReference.this.loge("[EVENT_GET_NETWORKS_DONE] In EVENT_GET_NETWORKS_DONE, onComplete is null!");
        }
    };
    private HwCustHwGSMPhoneReference mHwCustHwGSMPhoneReference;
    private HwVpApStatusHandler mHwVpApStatusHandler;
    private int mLteReleaseVersion;
    private String mMeid;
    /* access modifiers changed from: private */
    public GsmCdmaPhone mPhone;
    private int mPhoneId = 0;
    private final PhoneStateListener mPhoneStateListener;
    private int mPrevPowerGrade = -1;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!"android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(intent.getAction()) || !HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT) {
                if (-1 != SystemProperties.getInt("persist.radio.stack_id_0", -1) && "com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
                    int subId = intent.getIntExtra("subscription", -1000);
                    int phoneId = intent.getIntExtra("phone", 0);
                    int status = intent.getIntExtra("operationResult", 1);
                    int state = intent.getIntExtra("newSubState", 0);
                    Rlog.d(HwGSMPhoneReference.LOG_TAG, "Received ACTION_SUBSCRIPTION_SET_UICC_RESULT on subId: " + subId + "phoneId " + phoneId + " status: " + status + "state: " + state);
                    if (status == 0 && state == 1) {
                        CommandsInterface commandsInterface = HwGSMPhoneReference.this.mPhone.mCi;
                        GsmCdmaPhone access$000 = HwGSMPhoneReference.this.mPhone;
                        GsmCdmaPhone unused = HwGSMPhoneReference.this.mPhone;
                        commandsInterface.getDeviceIdentity(access$000.obtainMessage(21));
                    }
                }
                return;
            }
            CommandsInterface commandsInterface2 = HwGSMPhoneReference.this.mPhone.mCi;
            GsmCdmaPhone access$0002 = HwGSMPhoneReference.this.mPhone;
            GsmCdmaPhone unused2 = HwGSMPhoneReference.this.mPhone;
            commandsInterface2.getDeviceIdentity(access$0002.obtainMessage(21));
        }
    };
    private Boolean mSarControlServiceExist = null;
    private IccRecords mSimRecords;
    /* access modifiers changed from: private */
    public int mSlotId = 0;
    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager;
    private String mncForSAR = null;
    private String preOperatorNumeric = "";
    private String redcueSARSpeacialType1 = null;
    private String redcueSARSpeacialType2 = null;
    private String subTag;

    public HwGSMPhoneReference(GsmCdmaPhone phone) {
        super(phone);
        this.mPhone = phone;
        this.subTag = "HwGSMPhoneReference[" + this.mPhone.getPhoneId() + "]";
        this.mPhoneId = this.mPhone.getPhoneId();
        Context mContext = this.mPhone.getContext();
        IntentFilter filter = new IntentFilter("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        mContext.registerReceiver(this.mReceiver, filter);
        this.mSlotId = SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhoneId);
        this.mWifiManager = (WifiManager) this.mPhone.getContext().getSystemService("wifi");
        this.mTelephonyManager = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (mWcdmaVpEnabled) {
            this.mHwVpApStatusHandler = new HwVpApStatusHandler(this.mPhone);
        }
        this.mHwCustHwGSMPhoneReference = (HwCustHwGSMPhoneReference) HwCustUtils.createObj(HwCustHwGSMPhoneReference.class, new Object[]{this.mPhone});
        this.mPhoneStateListener = new PhoneStateListener(Integer.valueOf(this.mSlotId)) {
            public void onServiceStateChanged(ServiceState serviceState) {
                String hplmn = null;
                if (HwGSMPhoneReference.this.mPhone.mIccRecords.get() != null) {
                    hplmn = ((IccRecords) HwGSMPhoneReference.this.mPhone.mIccRecords.get()).getOperatorNumeric();
                }
                if (TelephonyManager.getDefault().getCurrentPhoneType(HwGSMPhoneReference.this.mSlotId) == 1 && SystemProperties.getBoolean("ro.config.hw_eccNumUseRplmn", false)) {
                    if (TelephonyManager.getDefault().isNetworkRoaming(HwGSMPhoneReference.this.mSlotId)) {
                        HwGSMPhoneReference.this.globalEccCustom(serviceState.getOperatorNumeric());
                    } else if (hplmn != null) {
                        HwGSMPhoneReference.this.globalEccCustom(hplmn);
                    }
                }
            }
        };
        startListen();
    }

    public void startListen() {
        this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
    }

    public void setLTEReleaseVersion(int state, Message response) {
        this.mPhone.mCi.setLTEReleaseVersion(state, response);
    }

    public int getLteReleaseVersion() {
        logd("getLteReleaseVersion: " + this.mLteReleaseVersion);
        return this.mLteReleaseVersion;
    }

    public void getCallbarringOption(String facility, String serviceClass, Message response) {
        logd("getCallbarringOption, facility=" + facility + ", serviceClass=" + serviceClass);
        ImsPhone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhoneEx.isUtEnable()) {
            this.mPhone.mCi.queryFacilityLock(facility, "", HwGsmMmiCode.siToServiceClass(serviceClass), response);
            return;
        }
        logd("getCallbarringOption via Ut");
        imsPhone.getCallBarring(facility, response);
    }

    public void setCallbarringOption(String facility, String password, boolean isActivate, String serviceClass, Message response) {
        logd("setCallbarringOption, facility=" + facility + ", isActivate=" + isActivate + ", serviceClass=" + serviceClass);
        ImsPhone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhoneEx.isUtEnable()) {
            this.mPhone.mCi.setFacilityLock(facility, isActivate, password, HwGsmMmiCode.siToServiceClass(serviceClass), response);
            return;
        }
        logd("setCallbarringOption via Ut");
        imsPhone.setCallBarring(facility, isActivate, password, response);
    }

    public void getCallbarringOption(String facility, int serviceClass, Message response) {
        logd("getCallbarringOption, facility=" + facility + ", serviceClass=" + serviceClass);
        ImsPhone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhoneEx.isUtEnable()) {
            this.mPhone.mCi.queryFacilityLock(facility, "", serviceClass, response);
            return;
        }
        logd("getCallbarringOption via Ut");
        imsPhone.getCallBarring(facility, response);
    }

    public void setCallbarringOption(String facility, String password, boolean isActivate, int serviceClass, Message response) {
        logd("setCallbarringOption, facility=" + facility + ", isActivate=" + isActivate + ", serviceClass=" + serviceClass);
        ImsPhone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhoneEx.isUtEnable()) {
            this.mPhone.mCi.setFacilityLock(facility, isActivate, password, serviceClass, response);
            return;
        }
        logd("setCallbarringOption via Ut");
        imsPhone.setCallBarring(facility, isActivate, password, response);
    }

    public void changeBarringPassword(String oldPassword, String newPassword, Message response) {
        this.mPhone.mCi.changeBarringPassword("AB", oldPassword, newPassword, response);
    }

    public Message getCustAvailableNetworksMessage(Message response) {
        return this.mHandler.obtainMessage(1, response);
    }

    private String getSelectedApn() {
        Cursor cursor;
        String apn = null;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        if (mContext == null) {
            return null;
        }
        if (isMultiEnable) {
            cursor = mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) this.mPhone.getSubId()), new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        } else {
            cursor = mContext.getContentResolver().query(PREFERAPN_NO_UPDATE_URI, new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        }
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                apn = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            }
            cursor.close();
        }
        logd("apn:" + apn);
        return apn;
    }

    public String getSelectedApnName() {
        Cursor cursor;
        String apnName = null;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        if (mContext == null) {
            return null;
        }
        if (isMultiEnable) {
            cursor = mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) this.mPhone.getSubId()), new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        } else {
            cursor = mContext.getContentResolver().query(PREFERAPN_NO_UPDATE_URI, new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        }
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                apnName = cursor.getString(cursor.getColumnIndexOrThrow(HwTelephony.NumMatchs.NAME));
            }
            cursor.close();
        }
        logd("apnname" + apnName);
        return apnName;
    }

    public String getCustOperatorName(String rplmn) {
        String mCustOperatorString;
        String str = rplmn;
        if (this.mPhone != null) {
            this.mSimRecords = (IccRecords) this.mPhone.mIccRecords.get();
        }
        String mImsi = this.mSimRecords != null ? this.mSimRecords.getIMSI() : null;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        if (mContext != null) {
            mCustOperatorString = Settings.System.getString(mContext.getContentResolver(), "hw_cust_operator");
        } else {
            mCustOperatorString = null;
        }
        logd("mCustOperatorString" + mCustOperatorString);
        if (TextUtils.isEmpty(mCustOperatorString) || TextUtils.isEmpty(mImsi) || TextUtils.isEmpty(rplmn)) {
            return null;
        }
        String[] items = mCustOperatorString.split(";");
        int maxLength = 0;
        int length = items.length;
        String operatorName = null;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String[] plmns = items[i].split(",");
            if (plmns.length != 3) {
                if (plmns.length == 5) {
                    if (str.equals(plmns[3]) && mImsi.startsWith(plmns[1])) {
                        if (!"0".equals(plmns[0]) || !isSpnGid1ApnnameMatched(plmns[2], 0)) {
                            if (!"1".equals(plmns[0]) || !isSpnGid1ApnnameMatched(plmns[2], 1)) {
                                if (!"2".equals(plmns[0]) || !isSpnGid1ApnnameMatched(plmns[2], 2)) {
                                    if ("3".equals(plmns[0]) && isSpnGid1ApnnameMatched(plmns[2], 3)) {
                                        operatorName = plmns[4];
                                        logd("operatorName changed by Apnname confirg");
                                        break;
                                    }
                                } else {
                                    operatorName = plmns[4];
                                    logd("operatorName changed by Apn confirg");
                                    break;
                                }
                            } else {
                                operatorName = plmns[4];
                                logd("operatorName changed by Gid1 confirg");
                                break;
                            }
                        } else {
                            operatorName = plmns[4];
                            logd("operatorName changed by spn confirg");
                            break;
                        }
                    }
                } else {
                    logd("Wrong length");
                }
                i++;
            } else if (str.equals(plmns[1])) {
                if ((mImsi.startsWith(plmns[0]) || "0".equals(plmns[0])) && plmns[0].length() > maxLength) {
                    String operatorName2 = plmns[2];
                    maxLength = plmns[0].length();
                    logd("operatorName changed" + operatorName2);
                    operatorName = operatorName2;
                }
                i++;
            }
            i++;
        }
        return operatorName;
    }

    public boolean isSpnGid1ApnnameMatched(String spnorgid1orapnname, int rule) {
        if (rule == 0) {
            return handleSpnRule(spnorgid1orapnname);
        }
        if (rule == 1) {
            return handleGid1Rule(spnorgid1orapnname);
        }
        if (rule == 2) {
            return handleApnRule(spnorgid1orapnname);
        }
        if (rule == 3) {
            return handleApnNameRule(spnorgid1orapnname);
        }
        return false;
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

    private boolean enableCustOperatorNameBySpn(String hplmn, String plmn) {
        if (this.mPhone == null) {
            return false;
        }
        Context mContext = this.mPhone.getContext();
        String custOperatorName = null;
        if (TextUtils.isEmpty(plmn) || TextUtils.isEmpty(hplmn)) {
            return false;
        }
        if (mContext != null) {
            custOperatorName = Settings.System.getString(mContext.getContentResolver(), "hw_enable_srchListNameBySpn");
        }
        if (TextUtils.isEmpty(custOperatorName)) {
            return false;
        }
        for (String item : custOperatorName.split(";")) {
            String[] plmns = item.split(",");
            if (2 == plmns.length && hplmn.equals(plmns[0]) && plmn.equals(plmns[1])) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public ArrayList<OperatorInfo> custAvaliableNetworks(ArrayList<OperatorInfo> searchResult) {
        ArrayList<OperatorInfo> searchResult2;
        String radioTechStr;
        boolean HwSearchNetSimUePriority;
        String radioTechStr2;
        String tempName;
        boolean z;
        if (searchResult == null || searchResult.size() == 0) {
            return searchResult;
        }
        int i = 0;
        boolean HwSearchNetSimUePriority2 = SystemProperties.getBoolean("ro.config.srch_net_sim_ue_pri", false);
        if (!HwSearchNetSimUePriority2) {
            searchResult2 = getEonsForAvailableNetworks(searchResult);
        } else {
            searchResult2 = searchResult;
        }
        ArrayList<OperatorInfo> custResults = new ArrayList<>();
        String radioTechStr3 = "";
        int i2 = 0;
        int listSize = searchResult2.size();
        while (i2 < listSize) {
            OperatorInfo operatorInfo = searchResult2.get(i2);
            String plmn = operatorInfo.getOperatorNumericWithoutAct();
            if (plmn != null) {
                int plmnEnd = plmn.indexOf(",");
                if (plmnEnd > 0) {
                    plmn = plmn.substring(i, plmnEnd);
                }
            }
            String longName = null;
            String longNameWithoutAct = null;
            IccRecords iccRecords = (IccRecords) this.mPhone.mIccRecords.get();
            if (iccRecords != null) {
                String hplmn = iccRecords.getOperatorNumeric();
                int lastSpaceIndexInLongName = operatorInfo.getOperatorAlphaLong().lastIndexOf(32);
                if (-1 != lastSpaceIndexInLongName) {
                    radioTechStr = operatorInfo.getOperatorAlphaLong().substring(lastSpaceIndexInLongName);
                    if (!" 2G".equals(radioTechStr) && !" 3G".equals(radioTechStr) && !" 4G".equals(radioTechStr)) {
                        radioTechStr = "";
                    }
                    HwSearchNetSimUePriority = HwSearchNetSimUePriority2;
                    longNameWithoutAct = operatorInfo.getOperatorAlphaLong().substring(0, lastSpaceIndexInLongName);
                } else {
                    HwSearchNetSimUePriority = HwSearchNetSimUePriority2;
                    longNameWithoutAct = operatorInfo.getOperatorAlphaLong();
                }
                int lastSpaceIndexInPlmn = operatorInfo.getOperatorNumeric().lastIndexOf(32);
                if (-1 != lastSpaceIndexInPlmn) {
                    String plmnRadioTechStr = operatorInfo.getOperatorNumeric().substring(lastSpaceIndexInPlmn);
                    if ((" 2G".equals(plmnRadioTechStr) || " 3G".equals(plmnRadioTechStr) || " 4G".equals(plmnRadioTechStr)) && plmn != null) {
                        z = false;
                        plmn = plmn.substring(0, lastSpaceIndexInPlmn);
                    } else {
                        z = false;
                    }
                    boolean z2 = z;
                    boolean z3 = z2;
                }
                if (plmn != null) {
                    tempName = getCustOperatorName(plmn);
                    int i3 = lastSpaceIndexInPlmn;
                    if (this.mHwCustHwGSMPhoneReference != null) {
                        tempName = this.mHwCustHwGSMPhoneReference.getCustOperatorNameBySpn(plmn, tempName);
                    }
                } else {
                    tempName = null;
                }
                if (tempName != null) {
                    longName = tempName.concat(radioTechStr);
                    if (this.mHwCustHwGSMPhoneReference != null) {
                        longName = this.mHwCustHwGSMPhoneReference.modifyTheFormatName(plmn, tempName, radioTechStr);
                    }
                }
                if (("50503".equals(plmn) && "50503".equals(hplmn)) || enableCustOperatorNameBySpn(hplmn, plmn)) {
                    String str = tempName;
                    if ((iccRecords.getDisplayRule(this.mPhone.getServiceState()) & 1) == 1) {
                        String spn = iccRecords.getServiceProviderName();
                        if (spn != null && spn.trim().length() > 0) {
                            longName = spn.concat(radioTechStr);
                        }
                    }
                }
                radioTechStr2 = radioTechStr;
            } else {
                HwSearchNetSimUePriority = HwSearchNetSimUePriority2;
                radioTechStr2 = radioTechStr;
            }
            if (!TextUtils.isEmpty(longNameWithoutAct)) {
                String data = null;
                try {
                    data = Settings.System.getString(this.mPhone.getContext().getContentResolver(), "plmn");
                    logd("plmn config = " + data);
                } catch (Exception e) {
                    loge("Exception when got data value", e);
                }
                if (!TextUtils.isEmpty(data)) {
                    PlmnConstants plmnConstants = new PlmnConstants(data);
                    String longNameCust = plmnConstants.getPlmnValue(plmn, Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry());
                    if (longNameCust == null) {
                        String str2 = data;
                        longNameCust = plmnConstants.getPlmnValue(plmn, "en_US");
                    }
                    StringBuilder sb = new StringBuilder();
                    PlmnConstants plmnConstants2 = plmnConstants;
                    sb.append("longName = ");
                    sb.append(longName);
                    sb.append(", longNameCust = ");
                    sb.append(longNameCust);
                    logd(sb.toString());
                    if (longName == null && longNameCust != null) {
                        longName = longNameCust.concat(radioTechStr2);
                    }
                }
            }
            if (longName != null) {
                custResults.add(new OperatorInfo(longName, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState()));
            } else {
                custResults.add(operatorInfo);
            }
            i2++;
            radioTechStr3 = radioTechStr2;
            HwSearchNetSimUePriority2 = HwSearchNetSimUePriority;
            i = 0;
        }
        if (this.mHwCustHwGSMPhoneReference != null) {
            custResults = this.mHwCustHwGSMPhoneReference.filterActAndRepeatedItems(custResults);
        }
        return custResults;
    }

    private boolean getHwVmNotFromSimValue() {
        boolean valueFromProp = SystemProperties.getBoolean("ro.config.hw_voicemail_sim", false);
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("hw_voicemail_sim", this.mPhoneId, Boolean.class);
        logd("getHwVmNotFromSimValue, phoneId" + this.mPhoneId + ", card:" + valueFromCard + ", prop: " + valueFromProp);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    public String getDefaultVoiceMailAlphaTagText(Context mContext, String ret) {
        if (getHwVmNotFromSimValue() || ret == null || ret.length() == 0 || isCustVoicemailTag(mContext)) {
            return mContext.getText(17039364).toString();
        }
        return ret;
    }

    private boolean isCustVoicemailTag(Context mContext) {
        String carrier;
        String strVMTagNotFromConf = Settings.System.getString(mContext.getContentResolver(), "hw_vmtag_follow_language");
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            carrier = TelephonyManager.getDefault().getSimOperator(this.mPhoneId);
        } else {
            carrier = TelephonyManager.getDefault().getSimOperator();
        }
        if (!TextUtils.isEmpty(strVMTagNotFromConf) && !TextUtils.isEmpty(carrier)) {
            for (String area : strVMTagNotFromConf.split(",")) {
                if (area.equals(carrier)) {
                    logd("voicemail Tag need follow language");
                    return true;
                }
            }
        }
        logd("voicemail Tag not need follow language");
        return false;
    }

    public String getMeid() {
        logd("[HwGSMPhoneReference]getMeid()");
        return this.mMeid;
    }

    public String getPesn() {
        logd("[HwGSMPhoneReference]getPesn()");
        return this.mPhone.getEsn();
    }

    public boolean beforeHandleMessage(Message msg) {
        boolean result;
        logd("beforeHandleMessage what = " + msg.what);
        int i = msg.what;
        if (i == 105) {
            updateReduceSARState();
            result = true;
        } else if (i == 108) {
            logd("onGetLteReleaseVersionDone:");
            handleGetLteReleaseVersionDone(msg);
            result = true;
        } else if (i == 111) {
            logd("beforeHandleMessage handled->EVENT_SET_MODE_TO_AUTO ");
            result = true;
            this.mPhone.setNetworkSelectionModeAutomatic(null);
        } else if (i != 1000) {
            return super.beforeHandleMessage(msg);
        } else {
            if (msg.arg2 == 2) {
                logd("start retry get DEVICE_ID_MASK_ALL");
                this.mPhone.mCi.getDeviceIdentity(this.mPhone.obtainMessage(21, msg.arg1, 0, null));
            } else {
                logd("EVENT_RETRY_GET_DEVICE_ID msg.arg2:" + msg.arg2 + ", error!!");
            }
            result = true;
        }
        return result;
    }

    public void afterHandleMessage(Message msg) {
        logd("afterHandleMessage what = " + msg.what);
        int i = msg.what;
        if (i == 1) {
            this.mPhone.mCi.getDeviceIdentity(this.mPhone.obtainMessage(21));
            logd("[HwGSMPhoneReference]PhoneBaseUtils.EVENT_RADIO_AVAILABLE");
            logd("Radio available, get lte release version");
            this.mPhone.mCi.getLteReleaseVersion(this.mPhone.obtainMessage(108));
        } else if (i != 5) {
            logd("unhandle event");
        } else {
            logd("Radio on, get lte release version");
            this.mPhone.mCi.getLteReleaseVersion(this.mPhone.obtainMessage(108));
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
            this.mPhone.mCi.getClass().getMethod("closeRrc", new Class[0]).invoke(this.mPhone.mCi, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void qcRilGoDormant() {
        try {
            if (mQcRilHook == null) {
                Object[] params = {this.mPhone.getContext()};
                mQcRilHook = Class.forName("com.qualcomm.qcrilhook.QcRilHook").getConstructor(new Class[]{Context.class}).newInstance(params);
            }
            if (mQcRilHook != null) {
                mQcRilHook.getClass().getMethod("qcRilGoDormant", new Class[]{String.class}).invoke(mQcRilHook, new Object[]{""});
                return;
            }
            logd("mQcRilHook is null");
            mIsQCRilGoDormant = false;
        } catch (ClassNotFoundException e) {
            mIsQCRilGoDormant = false;
            loge("the class QcRilHook not exist");
        } catch (NoSuchMethodException e2) {
            mIsQCRilGoDormant = false;
            loge("the class QcRilHook NoSuchMethod: qcRilGoDormant [String]");
        } catch (LinkageError e3) {
            mIsQCRilGoDormant = false;
            loge("class QcRilHook LinkageError", e3);
        } catch (InstantiationException e4) {
            mIsQCRilGoDormant = false;
            loge("class QcRilHook InstantiationException", e4);
        } catch (IllegalAccessException e5) {
            mIsQCRilGoDormant = false;
            loge("class QcRilHook IllegalAccessException", e5);
        } catch (IllegalArgumentException e6) {
            mIsQCRilGoDormant = false;
            loge("class QcRilHook IllegalArgumentException", e6);
        } catch (InvocationTargetException e7) {
            mIsQCRilGoDormant = false;
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
        if (this.mncForSAR == null || this.mncForSAR.equals("FFFFF") || this.mncForSAR.length() < 3) {
            return 0;
        }
        this.mncForSAR = this.mncForSAR.substring(0, 3);
        if (this.redcueSARSpeacialType1.contains(this.mncForSAR)) {
            return 1;
        }
        if (this.redcueSARSpeacialType2.contains(this.mncForSAR)) {
            return 2;
        }
        return 0;
    }

    private int getWifiType(int wifiHotState, int wifiState) {
        if (wifiHotState == 13) {
            return 2;
        }
        if (wifiState == 3) {
            return 1;
        }
        return wifiState == 1 ? 0 : 0;
    }

    private int[][] getGradeData(boolean isReceiveOFF) {
        if (!isReceiveOFF) {
            return new int[][]{new int[]{0, REDUCE_SAR_NORMAL_WIFI_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON}, new int[]{REDUCE_SAR_SPECIAL_TYPE1_WIFI_OFF, REDUCE_SAR_SPECIAL_TYPE1_WIFI_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON}, new int[]{REDUCE_SAR_SPECIAL_TYPE2_WIFI_OFF, REDUCE_SAR_SPECIAL_TYPE2_WIFI_ON, REDUCE_SAR_SPECIAL_TYPE2_HOTSPOT_ON}};
        }
        return new int[][]{new int[]{REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON}, new int[]{REDUCE_SAR_SPECIAL_TYPE1_WIFI_OFF, REDUCE_SAR_SPECIAL_TYPE1_WIFI_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON}, new int[]{REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON}};
    }

    private void setPowerGrade(int powerGrade) {
        if (this.mPrevPowerGrade != powerGrade) {
            this.mPrevPowerGrade = powerGrade;
            logd("updateReduceSARState()    setPowerGrade() : " + powerGrade);
            this.mPhone.mCi.setPowerGrade(powerGrade, null);
        }
    }

    private boolean reduceOldSar() {
        if (!SystemProperties.getBoolean("ro.config.old_reduce_sar", false)) {
            return false;
        }
        int wifiHotState = this.mWifiManager.getWifiApState();
        logd("Radio available, set wifiHotState  " + wifiHotState);
        if (wifiHotState == 13) {
            setPowerGrade(1);
        } else if (wifiHotState == 11) {
            setPowerGrade(0);
        }
        return true;
    }

    public void resetReduceSARPowerGrade() {
        this.mPrevPowerGrade = -1;
    }

    public void updateReduceSARState() {
        int reduceData;
        boolean isReceiveOFF = true;
        if (this.mSarControlServiceExist == null) {
            try {
                this.mSarControlServiceExist = this.mPhone.getContext().getPackageManager().getApplicationInfo("com.huawei.sarcontrolservice", REDUCE_SAR_NORMAL_HOTSPOT_ON) != null;
                logd("updateReduceSARState mSarControlServiceExist=" + this.mSarControlServiceExist);
                if (this.mSarControlServiceExist.booleanValue() && !SystemProperties.getBoolean("ro.config.hw_ReduceSAR", false)) {
                    logd("updateReduceSARState mSarControlServiceExist=true");
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                this.mSarControlServiceExist = false;
                logd("updateReduceSARState mSarControlServiceExist=false");
            }
        } else if (this.mSarControlServiceExist.booleanValue() && !SystemProperties.getBoolean("ro.config.hw_ReduceSAR", false)) {
            return;
        }
        logd("updateReduceSARState()");
        if (!reduceOldSar() && SystemProperties.getBoolean("ro.config.hw_ReduceSAR", false)) {
            AudioManager localAudioManager = (AudioManager) this.mPhone.getContext().getSystemService("audio");
            int cardType = getCardType();
            int wifiType = getWifiType(this.mWifiManager.getWifiApState(), this.mWifiManager.getWifiState());
            int state = TelephonyManager.getDefault().getCallState();
            boolean isWiredHeadsetOn = localAudioManager.isWiredHeadsetOn();
            boolean isSpeakerphoneOn = localAudioManager.isSpeakerphoneOn();
            if (!isWiredHeadsetOn && !isSpeakerphoneOn && state == 2) {
                isReceiveOFF = false;
            }
            if (SystemProperties.getBoolean("persist.gsm.ReceiveTestMode", false)) {
                isReceiveOFF = SystemProperties.getBoolean("persist.gsm.ReceiveTestValue", false);
                logd("hw_ReceiveTestMode = true isReceiveOFF = " + isReceiveOFF);
            }
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
        boolean result = false;
        switch (dialString.charAt(0)) {
            case HwVSimConstants.EVENT_GET_PREFERRED_NETWORK_TYPE_DONE /*48*/:
            case HwVSimConstants.EVENT_ENABLE_VSIM_DONE /*51*/:
            case HwVSimConstants.CMD_DISABLE_VSIM /*52*/:
            case HwVSimConstants.EVENT_DISABLE_VSIM_DONE /*53*/:
                if (dialString.length() == 1) {
                    result = true;
                    break;
                }
                break;
            case HwVSimConstants.EVENT_SET_PREFERRED_NETWORK_TYPE_DONE /*49*/:
            case '2':
                if (dialString.length() <= 2) {
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
        return PhoneNumberUtils.convertPlusByMcc(dialNumber, Integer.parseInt(imsi.substring(0, 3)));
    }

    public boolean isSupportCFT() {
        boolean isSupportCFT = false;
        ImsPhone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone != null) {
            logd("imsPhone is exist, isSupportCFT");
            isSupportCFT = imsPhone.mHwImsPhoneEx.isSupportCFT();
        }
        logd("isSupportCFT=" + isSupportCFT);
        return isSupportCFT;
    }

    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        Message message = onComplete;
        ImsPhone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhoneEx.isUtEnable()) {
            logd("setCallForwardingUncondTimerOption can not go Ut interface, imsPhone=" + imsPhone);
            if (message != null) {
                AsyncResult.forMessage(message, null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                onComplete.sendToTarget();
                return;
            }
            return;
        }
        logd("setCallForwardingUncondTimerOption via Ut");
        imsPhone.mHwImsPhoneEx.setCallForwardingUncondTimerOption(startHour, startMinute, endHour, endMinute, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, message);
    }

    public void setImsSwitch(boolean on) {
        this.mPhone.mCi.setImsSwitch(on);
    }

    public boolean getImsSwitch() {
        return this.mPhone.mCi.getImsSwitch();
    }

    public void processEccNumber(ServiceStateTracker gSST) {
        if (SystemProperties.getBoolean("ro.config.hw_globalEcc", false)) {
            logd("EVENT_SIM_RECORDS_LOADED!!!!");
            SystemProperties.set(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "usim_present");
            String hplmn = TelephonyManager.getDefault().getSimOperator(this.mPhoneId);
            boolean isRoaming = TelephonyManager.getDefault().isNetworkRoaming(this.mPhoneId);
            String rplmn = gSST.mSS.getOperatorNumeric();
            if (TextUtils.isEmpty(hplmn)) {
                logd("received EVENT_SIM_RECORDS_LOADED but not hplmn !!!!");
            } else if (!isRoaming || !SystemProperties.getBoolean("ro.config.hw_eccNumUseRplmn", false)) {
                globalEccCustom(hplmn);
            } else {
                globalEccCustom(rplmn);
            }
        }
    }

    public void globalEccCustom(String operatorNumeric) {
        String str = operatorNumeric;
        String eccListWithCard = null;
        String eccListNoCard = null;
        String forceEccState = SystemProperties.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: operator numeric = " + str + "; preOperatorNumeric = " + this.preOperatorNumeric + ";forceEccState  = " + forceEccState);
        if (!TextUtils.isEmpty(operatorNumeric) && (!str.equals(this.preOperatorNumeric) || !forceEccState.equals("invalid"))) {
            this.preOperatorNumeric = str;
            SystemProperties.set(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
            if (HwTelephonyFactory.getHwPhoneManager().isSupportEccFormVirtualNet()) {
                eccListWithCard = HwTelephonyFactory.getHwPhoneManager().getVirtualNetEccWihCard(this.mPhoneId);
                eccListNoCard = HwTelephonyFactory.getHwPhoneManager().getVirtualNetEccNoCard(this.mPhoneId);
                logd("try to get Ecc form virtualNet ecclist_withcard=" + eccListWithCard + " ecclistNocard=" + eccListNoCard);
            }
            String eccListNoCard2 = eccListNoCard;
            String eccListWithCard2 = eccListWithCard;
            if (virtualNetEccFormCarrier(this.mPhoneId)) {
                int slotId = SubscriptionManager.getSlotIndex(this.mPhoneId);
                try {
                    eccListWithCard2 = (String) HwCfgFilePolicy.getValue("virtual_ecclist_withcard", slotId, String.class);
                    eccListNoCard2 = (String) HwCfgFilePolicy.getValue("virtual_ecclist_nocard", slotId, String.class);
                    logd("try to get Ecc form virtualNet virtual_ecclist from carrier =" + eccListWithCard2 + " ecclistNocard=" + eccListNoCard2);
                } catch (Exception e) {
                    loge("Failed to get ecclist in carrier", e);
                }
            }
            String custEcc = getCustEccList(operatorNumeric);
            if (!TextUtils.isEmpty(custEcc)) {
                String[] custEccArray = custEcc.split(":");
                if (custEccArray.length == 3 && custEccArray[0].equals(str) && !TextUtils.isEmpty(custEccArray[1]) && !TextUtils.isEmpty(custEccArray[2])) {
                    eccListWithCard2 = custEccArray[1];
                    eccListNoCard2 = custEccArray[2];
                }
            }
            if (eccListWithCard2 == null) {
                Cursor cursor = this.mPhone.getContext().getContentResolver().query(IHwTelephonyEx.GlobalMatchs.CONTENT_URI, new String[]{"_id", HwTelephony.NumMatchs.NAME, "numeric", HwTelephony.VirtualNets.ECC_WITH_CARD, HwTelephony.VirtualNets.ECC_NO_CARD}, "numeric= ?", new String[]{str}, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
                if (cursor == null) {
                    logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: No matched emergency numbers in db.");
                    this.mPhone.mCi.requestSetEmergencyNumbers("", "");
                    return;
                }
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        eccListWithCard2 = cursor.getString(3);
                        eccListNoCard2 = cursor.getString(4);
                        cursor.moveToNext();
                    }
                } catch (Exception ex) {
                    logd("[SLOT" + this.mPhoneId + "]globalEccCustom: global version cause exception!" + ex.toString());
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
                cursor.close();
            }
            logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: ecc_withcard = " + eccListWithCard2 + ", ecc_nocard = " + eccListNoCard2);
            String eccListWithCard3 = eccListWithCard2 != null ? eccListWithCard2 : "";
            String eccListNoCard3 = eccListNoCard2 != null ? eccListNoCard2 : "";
            if ((eccListWithCard3 == null || eccListWithCard3.equals("")) && (eccListNoCard3 == null || eccListNoCard3.equals(""))) {
                this.mPhone.mCi.requestSetEmergencyNumbers("", "");
            } else {
                this.mPhone.mCi.requestSetEmergencyNumbers(eccListWithCard3, eccListNoCard3);
            }
        }
    }

    public String getHwCdmaPrlVersion() {
        String prlVersion;
        int subId = this.mPhone.getSubId();
        int simCardState = TelephonyManager.getDefault().getSimState(subId);
        if (5 != simCardState || !HwTelephonyManagerInner.getDefault().isCDMASimCard(subId)) {
            prlVersion = "0";
        } else {
            prlVersion = this.mPhone.mCi.getHwPrlVersion();
        }
        logd("getHwCdmaPrlVersion: prlVersion=" + prlVersion + ", subid=" + subId + ", simState=" + simCardState);
        return prlVersion;
    }

    public String getHwCdmaEsn() {
        String esn;
        int subId = this.mPhone.getSubId();
        int simCardState = TelephonyManager.getDefault().getSimState(subId);
        if (5 != simCardState || !HwTelephonyManagerInner.getDefault().isCDMASimCard(subId)) {
            esn = "0";
        } else {
            esn = this.mPhone.mCi.getHwUimid();
        }
        logd("getHwCdmaEsn: esn=" + esn + ", subid=" + subId + ", simState=" + simCardState);
        return esn;
    }

    public String getVMNumberWhenIMSIChange() {
        if (this.mPhone == null) {
            return null;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext());
        String number = null;
        String mIccId = null;
        if (this.mPhone != null) {
            mIccId = this.mPhone.getIccSerialNumber();
        }
        if (!TextUtils.isEmpty(mIccId)) {
            String temp = sp.getString(mIccId + this.mPhone.getPhoneId(), null);
            if (temp != null) {
                number = temp;
            }
            logd("getVMNumberWhenIMSIChange number= xxxxxx");
        }
        return number;
    }

    public boolean setISMCOEX(String setISMCoex) {
        this.mPhone.mCi.setISMCOEX(setISMCoex, null);
        return true;
    }

    private String getCustEccList(String operatorNumeric) {
        String custEccList = null;
        String matchEccList = "";
        try {
            custEccList = Settings.System.getString(this.mPhone.getContext().getContentResolver(), "hw_cust_emergency_nums");
        } catch (Exception e) {
            loge("Failed to load vmNum from SettingsEx", e);
        }
        if (TextUtils.isEmpty(custEccList) || TextUtils.isEmpty(operatorNumeric)) {
            return matchEccList;
        }
        String[] custEccListItems = custEccList.split(";");
        int i = 0;
        while (true) {
            if (i >= custEccListItems.length) {
                break;
            }
            String[] custItem = custEccListItems[i].split(":");
            if (custItem.length == 3 && custItem[0].equals(operatorNumeric)) {
                matchEccList = custEccListItems[i];
                break;
            }
            i++;
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

    /* access modifiers changed from: private */
    public void logd(String msg) {
        Rlog.d(this.subTag, msg);
    }

    /* access modifiers changed from: private */
    public void loge(String msg) {
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

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        this.mPhone.mCi.notifyCellularCommParaReady(paratype, pathtype, response);
    }

    public void updateWfcMode(Context context, boolean roaming, int subId) throws ImsException {
        HwImsManagerInner.updateWfcMode(context, roaming, subId);
    }

    public boolean isDualImsAvailable() {
        return HwImsManagerInner.isDualImsAvailable();
    }

    public boolean isUssdOkForRelease() {
        boolean ussdIsOkForRelease = false;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        if (mContext == null) {
            return false;
        }
        CarrierConfigManager configLoader = (CarrierConfigManager) mContext.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configLoader != null) {
            b = configLoader.getConfigForSubId(this.mPhone.getSubId());
        }
        if (b != null) {
            ussdIsOkForRelease = b.getBoolean(USSD_IS_OK_FOR_RELEASE);
        }
        logd("isUssdOkForRelease: ussdIsOkForRelease " + ussdIsOkForRelease);
        return ussdIsOkForRelease;
    }

    private void handleGetLteReleaseVersionDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception != null) {
            logd("Error in get lte release version:" + ar.exception);
            return;
        }
        int[] resultint = (int[]) ar.result;
        if (resultint == null) {
            logd("Error in get lte release version: null resultint");
            return;
        }
        if (resultint.length != 0) {
            logd("onGetLteReleaseVersionDone: result=" + resultint[0]);
            this.mLteReleaseVersion = resultint[0];
            if (resultint[0] > 3) {
                this.mLteReleaseVersion = 0;
            }
        }
    }

    private boolean handleSpnRule(String spnOrgId1OrApnName) {
        if (this.mSimRecords == null || spnOrgId1OrApnName == null) {
            return false;
        }
        String spn = this.mSimRecords.getServiceProviderName();
        logd("[EONS] spn = " + spn + ", spnOrgId1OrApnName(gid1 start with 0x) = " + spnOrgId1OrApnName);
        if (!spnOrgId1OrApnName.equals(spn)) {
            return false;
        }
        logd("[EONS] ShowPLMN: use the spn rule");
        return true;
    }

    private boolean handleGid1Rule(String spnOrgId1OrApnName) {
        if (this.mSimRecords == null || spnOrgId1OrApnName == null) {
            return false;
        }
        byte[] gid1 = this.mSimRecords.getGID1();
        if (gid1 == null || gid1.length <= 0 || spnOrgId1OrApnName.length() <= 2 || !spnOrgId1OrApnName.substring(0, 2).equalsIgnoreCase("0x")) {
            return false;
        }
        logd("[EONS] gid1 = " + gid1[0]);
        byte[] gid1valueBytes = IccUtils.hexStringToBytes(spnOrgId1OrApnName.substring(2));
        int i = 0;
        while (i < gid1.length && i < gid1valueBytes.length) {
            if (gid1[i] != gid1valueBytes[i]) {
                return false;
            }
            i++;
        }
        logd("[EONS] ShowPLMN: use the Gid1 rule");
        return true;
    }

    private boolean handleApnRule(String spnOrgId1OrApnName) {
        if (this.mSimRecords == null || spnOrgId1OrApnName == null) {
            return false;
        }
        String apn = getSelectedApn();
        logd("[EONS] apn = " + apn);
        if (!spnOrgId1OrApnName.equals(apn)) {
            return false;
        }
        logd("[EONS] ShowPLMN: use the apn rule");
        return true;
    }

    private boolean handleApnNameRule(String spnOrgId1OrApnName) {
        if (this.mSimRecords == null || spnOrgId1OrApnName == null) {
            return false;
        }
        String apnname = getSelectedApnName();
        logd("[EONS] apnname = " + apnname);
        if (!spnOrgId1OrApnName.equals(apnname)) {
            return false;
        }
        logd("[EONS] ShowPLMN: use the apnname rule");
        return true;
    }
}
