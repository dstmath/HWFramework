package com.android.internal.telephony.gsm;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.HwPhoneNumberUtils;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPhoneReferenceBase;
import com.android.internal.telephony.IHwGsmCdmaPhoneInner;
import com.android.internal.telephony.IServiceStateTrackerInner;
import com.android.internal.telephony.PlmnConstants;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.huawei.android.net.wifi.WifiManagerEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.TelephonyEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.MmiCodeExt;
import com.huawei.internal.telephony.NetworkCallBackInteface;
import com.huawei.internal.telephony.OperatorInfoEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.IccUtilsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.utils.HwPartResourceUtils;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Locale;

public class HwGSMPhoneReference extends HwPhoneReferenceBase {
    private static final int APNNAME_RULE = 3;
    private static final int APN_RULE = 2;
    private static final int ECC_NOCARD_INDEX = 4;
    private static final int ECC_WITHCARD_INDEX = 3;
    private static final int EVENT_GET_AVAILABLE_NETWORKS_DONE = 1;
    private static final int EVENT_RADIO_ON = 5;
    private static final int EVENT_UPLOAD_AVAILABLE_NETWORKS_DONE = 2;
    private static final int GID1_RULE = 1;
    private static final int INVALID_INDEX = -1;
    private static final boolean IS_HW_VM_NOT_FROM_SIM = SystemPropertiesEx.getBoolean("ro.config.hw_voicemail_sim", false);
    private static final boolean IS_MULTI_ENABLE = TelephonyManagerEx.isMultiSimEnabled();
    private static final boolean IS_WCDMA_VP_ENABLED = SystemPropertiesEx.get("ro.hwpp.wcdma_voice_preference", "false").equals("true");
    private static final String LOG_TAG = "HwGSMPhoneReference";
    private static final int MANUAL_SEARCH_NETWORK_FAIL = 1;
    private static final int MANUAL_SEARCH_NETWORK_SUCCESS = 0;
    private static final int MEID_LENGTH = 14;
    private static final int NAME_INDEX = 1;
    private static final int NUMERIC_INDEX = 2;
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final String PROPERTY_GLOBAL_FORCE_TO_SET_ECC = "ril.force_to_set_ecc";
    private static final String PROP_REDUCE_SAR_SPECIAL_LEVEL = "ro.config.reduce_sar_level";
    private static final String PROP_REDUCE_SAR_SPECIAL_TYPE1 = "ro.config.reduce_sar_type1";
    private static final String PROP_REDUCE_SAR_SPECIAL_TYPE2 = "ro.config.reduce_sar_type2";
    private static final int REDUCE_SAR_NORMAL_HOTSPOT_ON = 8192;
    private static final int REDUCE_SAR_NORMAL_WIFI_OFF = 0;
    private static final int REDUCE_SAR_NORMAL_WIFI_ON = 4096;
    private static final int REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON = 20480;
    private static final int REDUCE_SAR_SPECIAL_TYPE1_WIFI_OFF = 12288;
    private static final int REDUCE_SAR_SPECIAL_TYPE1_WIFI_ON = 16384;
    private static final int REDUCE_SAR_SPECIAL_TYPE2_HOTSPOT_ON = 12287;
    private static final int REDUCE_SAR_SPECIAL_TYPE2_WIFI_OFF = 4095;
    private static final int REDUCE_SAR_SPECIAL_TYPE2_WIFI_ON = 8191;
    private static final int SEARCH_LENGTH = 2;
    private static final int SPN_RULE = 0;
    private static final int SUB_NONE = -1;
    private static final String USSD_IS_OK_FOR_RELEASE = "is_ussd_ok_for_nw_release";
    private static final int WIFI_HOTSPOT_TYPE = 2;
    private static final int WIFI_OFF_TYPE = 0;
    private static final int WIFI_ON_TYPE = 1;
    private static boolean mIsQCRilGoDormant = true;
    private static Object mQcRilHook = null;
    private Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.gsm.HwGSMPhoneReference.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                handleGetAvailableNetWorksDone(msg);
            } else if (i != 2) {
                super.handleMessage(msg);
            } else {
                handleUploadAvailableNetWorksDone(msg);
            }
        }

        private void handleGetAvailableNetWorksDone(Message msg) {
            HwGSMPhoneReference.this.logd("[EVENT_GET_AVAILABLE_NETWORKS_DONE]");
            ArrayList<OperatorInfoEx> searchResults = null;
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            Message onComplete = (Message) ar.getUserObj();
            ArrayList<OperatorInfoEx> searchResultsFromMessage = OperatorInfoEx.from(ar.getResult());
            if (ar.getException() == null && ar.getResult() != null) {
                searchResults = HwGSMPhoneReference.this.custAvailableNetworks(searchResultsFromMessage);
            }
            if (searchResults == null || searchResults.size() == 0) {
                searchResults = searchResultsFromMessage;
            } else {
                HwGSMPhoneReference.this.logd("[EVENT_GET_NETWORKS_DONE] Populated global version cust names for available networks.");
            }
            if (onComplete != null) {
                AsyncResultEx.forMessage(onComplete, OperatorInfoEx.convertToOperatorInfo(searchResults), ar.getException());
                if (onComplete.getTarget() != null) {
                    onComplete.sendToTarget();
                    return;
                }
                return;
            }
            HwGSMPhoneReference.this.loge("[EVENT_GET_NETWORKS_DONE] In EVENT_GET_NETWORKS_DONE, onComplete is null!");
        }

        private void handleUploadAvailableNetWorksDone(Message msg) {
            HwGSMPhoneReference.this.logd("[handleUploadAvailableNetWorksDone]");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            int result = 0;
            if (!(ar == null || ar.getException() == null)) {
                HwGSMPhoneReference.this.logd("handleUploadAvailableNetWorksDone exception");
                result = 1;
            }
            if (HwGSMPhoneReference.this.mNetworkCallBackInterface != null) {
                HwGSMPhoneReference.this.mNetworkCallBackInterface.endUploadAvailableNetworks(result);
                HwGSMPhoneReference.this.mNetworkCallBackInterface = null;
            }
        }
    };
    private HwCustHwGSMPhoneReference mHwCustHwGSMPhoneReference;
    private HwVpApStatusHandler mHwVpApStatusHandler;
    private int mLteReleaseVersion;
    private String mMeid;
    private NetworkCallBackInteface mNetworkCallBackInterface;
    private int mPrevPowerGrade = -1;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.gsm.HwGSMPhoneReference.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                if (!"android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(intent.getAction()) || !HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT) {
                    boolean switchDone = false;
                    if ("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(intent.getAction())) {
                        int mainSlot = HwTelephonyManager.getDefault().getDefault4GSlotId();
                        int radioState = HwGSMPhoneReference.this.mPhoneExt.getCi().getRadioState();
                        if (intent.getIntExtra("HW_SWITCH_SLOT_STEP", 1) == 1 && mainSlot == HwGSMPhoneReference.this.mPhoneId) {
                            switchDone = true;
                        }
                        HwGSMPhoneReference.this.logd("ACTION_HW_SWITCH_SLOT_DONE radio state:" + radioState + " switch done:" + switchDone);
                        if (switchDone && HwGSMPhoneReference.this.mPhoneExt.getPhoneType() == 1 && radioState == 1) {
                            HwGSMPhoneReference.this.mPhoneExt.getCi().getLteReleaseVersion(HwGSMPhoneReference.this.mPhoneExt.obtainMessage(108));
                        }
                    } else if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction()) && SystemPropertiesEx.getInt("persist.radio.stack_id_0", -1) != -1) {
                        int subId = intent.getIntExtra("subscription", -1000);
                        int phoneId = intent.getIntExtra("phone", 0);
                        int status = intent.getIntExtra("operationResult", 1);
                        int state = intent.getIntExtra("newSubState", 0);
                        RlogEx.i(HwGSMPhoneReference.LOG_TAG, "Received ACTION_SUBSCRIPTION_SET_UICC_RESULT on subId: " + subId + "phoneId " + phoneId + " status: " + status + "state: " + state);
                        if (status == 0 && state == 1) {
                            CommandsInterfaceEx ci = HwGSMPhoneReference.this.mPhoneExt.getCi();
                            PhoneExt phoneExt = HwGSMPhoneReference.this.mPhoneExt;
                            PhoneExt unused = HwGSMPhoneReference.this.mPhoneExt;
                            ci.getDeviceIdentity(phoneExt.obtainMessage(21));
                        }
                    }
                } else {
                    HwGSMPhoneReference.this.mPhoneExt.getCi().getDeviceIdentity(HwGSMPhoneReference.this.mPhoneExt.obtainMessage(21));
                }
            }
        }
    };
    private Boolean mSarControlServiceExist = null;
    private IccRecordsEx mSimRecordsEx;
    private WifiManager mWifiManager;
    private String mncForSAR = null;
    private String preOperatorNumeric = BuildConfig.FLAVOR;
    private String redcueSARSpeacialType1 = null;
    private String redcueSARSpeacialType2 = null;
    private String subTag = ("HwGSMPhoneReference[" + this.mPhoneId + "]");

    public HwGSMPhoneReference(IHwGsmCdmaPhoneInner hwGsmCdmaPhoneInner, PhoneExt phoneExt) {
        super(hwGsmCdmaPhoneInner, phoneExt);
        IntentFilter filter = new IntentFilter("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (IS_WCDMA_VP_ENABLED) {
            this.mHwVpApStatusHandler = new HwVpApStatusHandler(hwGsmCdmaPhoneInner);
        }
        this.mHwCustHwGSMPhoneReference = (HwCustHwGSMPhoneReference) HwCustUtils.createObj(HwCustHwGSMPhoneReference.class, new Object[]{this.mPhoneExt});
    }

    public void setLTEReleaseVersion(int state, Message response) {
        this.mPhoneExt.getCi().setLTEReleaseVersion(state, response);
    }

    public int getLteReleaseVersion() {
        logd("getLteReleaseVersion: " + this.mLteReleaseVersion);
        return this.mLteReleaseVersion;
    }

    public void getCallbarringOption(String facility, String serviceClass, Message response) {
        logd("getCallbarringOption, facility=" + facility + ", serviceClass=" + serviceClass);
        this.mPhoneExt.getCallbarringOption(facility, HwGsmMmiCode.siToServiceClass(serviceClass), response);
    }

    public void setCallbarringOption(String facility, String password, boolean isActivate, String serviceClass, Message response) {
        this.mPhoneExt.setCallbarringOption(facility, password, isActivate, HwGsmMmiCode.siToServiceClass(serviceClass), response);
    }

    public void getCallbarringOption(String facility, int serviceClass, Message response) {
        logd("getCallbarringOption, facility=" + facility + ", serviceClass=" + serviceClass);
        this.mPhoneExt.getCallbarringOption(facility, serviceClass, response);
    }

    public void setCallbarringOption(String facility, String password, boolean isActivate, int serviceClass, Message response) {
        logd("setCallbarringOption, facility=" + facility + ", isActivate=" + isActivate + ", serviceClass=" + serviceClass);
        this.mPhoneExt.setCallbarringOption(facility, password, isActivate, serviceClass, response);
    }

    public void changeBarringPassword(String oldPassword, String newPassword, Message response) {
        this.mPhoneExt.getCi().changeBarringPassword("AB", oldPassword, newPassword, response);
    }

    public Message getCustAvailableNetworksMessage(Message response) {
        return this.mHandler.obtainMessage(1, response);
    }

    private String getSelectedApn() {
        Cursor cursor;
        Context mContext = this.mPhoneExt != null ? this.mPhoneExt.getContext() : null;
        if (mContext == null) {
            return null;
        }
        Cursor cursor2 = null;
        String apn = null;
        try {
            if (IS_MULTI_ENABLE) {
                cursor = mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) this.mPhoneExt.getPhoneId()), new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
            } else {
                cursor = mContext.getContentResolver().query(PREFERAPN_NO_UPDATE_URI, new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
            }
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                apn = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (SQLException e) {
                    loge("getSelectedApn close SQLiteException.");
                }
            }
        } catch (SQLException e2) {
            loge("getSelectedApn SQLiteException.");
            if (0 != 0) {
                cursor2.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    cursor2.close();
                } catch (SQLException e3) {
                    loge("getSelectedApn close SQLiteException.");
                }
            }
            throw th;
        }
        logd("apn:" + apn);
        return apn;
    }

    public String getSelectedApnName() {
        Cursor cursor;
        String apnName = null;
        Context mContext = this.mHwGsmCdmaPhoneInner != null ? this.mPhoneExt.getContext() : null;
        if (mContext == null) {
            return null;
        }
        if (IS_MULTI_ENABLE) {
            cursor = mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) this.mPhoneExt.getPhoneId()), new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
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
        if (this.mHwGsmCdmaPhoneInner != null) {
            this.mSimRecordsEx = this.mPhoneExt.getIccRecords();
        }
        IccRecordsEx iccRecordsEx = this.mSimRecordsEx;
        String imsi = iccRecordsEx != null ? iccRecordsEx.getIMSI() : null;
        Context context = this.mHwGsmCdmaPhoneInner != null ? this.mPhoneExt.getContext() : null;
        String custOperatorString = context != null ? Settings.System.getString(context.getContentResolver(), "hw_cust_operator") : null;
        logd("mCustOperatorString" + custOperatorString);
        if (TextUtils.isEmpty(custOperatorString) || TextUtils.isEmpty(imsi) || TextUtils.isEmpty(rplmn)) {
            return null;
        }
        int maxLength = 0;
        String operatorName = null;
        for (String item : custOperatorString.split(";")) {
            String[] plmns = item.split(",");
            if (plmns.length == 3) {
                if (rplmn.equals(plmns[1]) && ((imsi.startsWith(plmns[0]) || "0".equals(plmns[0])) && plmns[0].length() > maxLength)) {
                    operatorName = plmns[2];
                    maxLength = plmns[0].length();
                    logd("operatorName changed" + operatorName);
                }
            } else if (plmns.length == 5) {
                operatorName = getOperatorNameFromFiveParaConf(imsi, rplmn, plmns, operatorName);
            } else {
                logd("Wrong length");
            }
        }
        return operatorName;
    }

    private String getOperatorNameFromFiveParaConf(String imsi, String rplmn, String[] plmns, String operatorName) {
        String tempOperratorName = operatorName;
        if (!rplmn.equals(plmns[3]) || !imsi.startsWith(plmns[1])) {
            return tempOperratorName;
        }
        if ("0".equals(plmns[0]) && isSpnGid1ApnnameMatched(plmns[2], 0)) {
            tempOperratorName = plmns[4];
            logd("operatorName changed by spn confirg");
        }
        if ("1".equals(plmns[0]) && isSpnGid1ApnnameMatched(plmns[2], 1)) {
            tempOperratorName = plmns[4];
            logd("operatorName changed by Gid1 confirg");
        }
        if ("2".equals(plmns[0]) && isSpnGid1ApnnameMatched(plmns[2], 2)) {
            tempOperratorName = plmns[4];
            logd("operatorName changed by Apn confirg");
        }
        if (!"3".equals(plmns[0]) || !isSpnGid1ApnnameMatched(plmns[2], 3)) {
            return tempOperratorName;
        }
        String tempOperratorName2 = plmns[4];
        logd("operatorName changed by Apnname confirg");
        return tempOperratorName2;
    }

    public boolean isSpnGid1ApnnameMatched(String spnOrGid1OrApnName, int rule) {
        if (rule == 0) {
            return handleSpnRule(spnOrGid1OrApnName);
        }
        if (rule == 1) {
            return handleGid1Rule(spnOrGid1OrApnName);
        }
        if (rule == 2) {
            return handleApnRule(spnOrGid1OrApnName);
        }
        if (rule == 3) {
            return handleApnNameRule(spnOrGid1OrApnName);
        }
        return false;
    }

    private ArrayList<OperatorInfoEx> getEonsForAvailableNetworks(ArrayList<OperatorInfoEx> searchResult) {
        IccRecordsEx iccRecordsEx;
        ArrayList<OperatorInfoEx> eonsNetworkNames;
        return (HwModemCapability.isCapabilitySupport(5) || (iccRecordsEx = this.mPhoneExt.getIccRecords()) == null || (eonsNetworkNames = iccRecordsEx.getEonsForAvailableNetworks(searchResult)) == null) ? searchResult : eonsNetworkNames;
    }

    private boolean enableCustOperatorNameBySpn(String hplmn, String plmn) {
        if (this.mHwGsmCdmaPhoneInner == null) {
            return false;
        }
        Context context = this.mPhoneExt.getContext();
        String custOperatorName = null;
        if (TextUtils.isEmpty(plmn) || TextUtils.isEmpty(hplmn)) {
            return false;
        }
        if (context != null) {
            custOperatorName = Settings.System.getString(context.getContentResolver(), "hw_enable_srchListNameBySpn");
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
    /* access modifiers changed from: public */
    private ArrayList<OperatorInfoEx> custAvailableNetworks(ArrayList<OperatorInfoEx> searchResult) {
        ArrayList<OperatorInfoEx> searchResult2;
        int plmnEnd;
        if (searchResult == null || searchResult.size() == 0) {
            return searchResult;
        }
        int i = 0;
        if (!SystemPropertiesEx.getBoolean("ro.config.srch_net_sim_ue_pri", false)) {
            searchResult2 = getEonsForAvailableNetworks(searchResult);
        } else {
            searchResult2 = searchResult;
        }
        ArrayList<OperatorInfoEx> custResults = new ArrayList<>();
        String radioTechStr = BuildConfig.FLAVOR;
        int i2 = 0;
        int listSize = searchResult2.size();
        while (i2 < listSize) {
            OperatorInfoEx operatorInfo = searchResult2.get(i2);
            String plmn = operatorInfo.getOperatorNumericWithoutAct();
            if (plmn != null && (plmnEnd = plmn.indexOf(",")) > 0) {
                plmn = plmn.substring(i, plmnEnd);
            }
            String longName = null;
            String longNameWithoutAct = null;
            IccRecordsEx iccRecords = this.mPhoneExt.getIccRecords();
            if (iccRecords != null) {
                String hplmn = iccRecords.getOperatorNumeric();
                int lastSpaceIndexInLongName = operatorInfo.getOperatorAlphaLong().lastIndexOf(32);
                if (lastSpaceIndexInLongName != -1) {
                    radioTechStr = getRadioTechStr(operatorInfo, lastSpaceIndexInLongName);
                    longNameWithoutAct = operatorInfo.getOperatorAlphaLong().substring(i, lastSpaceIndexInLongName);
                } else {
                    longNameWithoutAct = operatorInfo.getOperatorAlphaLong();
                }
                plmn = getPlmnByLastSpaceIndex(plmn, operatorInfo);
                longName = getLongNameByCustOperator(plmn, hplmn, null, radioTechStr);
            }
            if (!TextUtils.isEmpty(longNameWithoutAct)) {
                longName = getLongNameWithoutAck(longName, plmn, radioTechStr);
            }
            if (longName != null) {
                custResults.add(OperatorInfoEx.makeOperatorInfoEx(longName, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState(), operatorInfo.getLevel()));
            } else {
                custResults.add(operatorInfo);
            }
            i2++;
            i = 0;
        }
        HwCustHwGSMPhoneReference hwCustHwGSMPhoneReference = this.mHwCustHwGSMPhoneReference;
        if (hwCustHwGSMPhoneReference != null) {
            return hwCustHwGSMPhoneReference.filterActAndRepeatedItems(custResults);
        }
        return custResults;
    }

    private String getRadioTechStr(OperatorInfoEx operatorInfo, int lastSpaceIndexInLongName) {
        String radioTechStr = operatorInfo.getOperatorAlphaLong().substring(lastSpaceIndexInLongName);
        if (!isRadioTechStr(radioTechStr)) {
            return BuildConfig.FLAVOR;
        }
        return radioTechStr;
    }

    private String getLongNameWithoutAck(String oriLongName, String plmn, String radioTechStr) {
        String data = null;
        try {
            data = Settings.System.getString(this.mPhoneExt.getContext().getContentResolver(), "plmn");
            logd("plmn config = " + data);
        } catch (IllegalArgumentException e) {
            loge("IllegalArgumentException when got data value");
        } catch (Exception e2) {
            loge("Exception when got data value");
        }
        if (TextUtils.isEmpty(data)) {
            return oriLongName;
        }
        PlmnConstants plmnConstants = new PlmnConstants(data);
        String longNameCust = plmnConstants.getPlmnValue(plmn, Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry());
        if (longNameCust == null) {
            longNameCust = plmnConstants.getPlmnValue(plmn, "en_US");
        }
        logd("longName = " + oriLongName + ", longNameCust = " + longNameCust);
        if (oriLongName != null || longNameCust == null) {
            return oriLongName;
        }
        return longNameCust.concat(radioTechStr);
    }

    private String getPlmnByLastSpaceIndex(String oriPlmn, OperatorInfoEx operatorInfo) {
        int lastSpaceIndexInPlmn;
        if (operatorInfo == null || (lastSpaceIndexInPlmn = operatorInfo.getOperatorNumeric().lastIndexOf(32)) == -1 || !isRadioTechStr(operatorInfo.getOperatorNumeric().substring(lastSpaceIndexInPlmn)) || oriPlmn == null) {
            return oriPlmn;
        }
        return oriPlmn.substring(0, lastSpaceIndexInPlmn);
    }

    private boolean isRadioTechStr(String plmnRadioTechStr) {
        return " 2G".equals(plmnRadioTechStr) || " 3G".equals(plmnRadioTechStr) || " 4G".equals(plmnRadioTechStr) || " 5G".equals(plmnRadioTechStr);
    }

    private String getLongNameByCustOperator(String plmn, String hplmn, String oriLongName, String radioTechStr) {
        String spn;
        String tempName = null;
        String longName = oriLongName;
        if (plmn != null) {
            tempName = getCustOperatorName(plmn);
            HwCustHwGSMPhoneReference hwCustHwGSMPhoneReference = this.mHwCustHwGSMPhoneReference;
            if (hwCustHwGSMPhoneReference != null) {
                tempName = hwCustHwGSMPhoneReference.getCustOperatorNameBySpn(plmn, tempName);
            }
        }
        if (tempName != null) {
            longName = tempName.concat(radioTechStr);
            HwCustHwGSMPhoneReference hwCustHwGSMPhoneReference2 = this.mHwCustHwGSMPhoneReference;
            if (hwCustHwGSMPhoneReference2 != null) {
                longName = hwCustHwGSMPhoneReference2.modifyTheFormatName(plmn, tempName, radioTechStr);
            }
        }
        if ((!"50503".equals(plmn) || !"50503".equals(hplmn)) && !enableCustOperatorNameBySpn(hplmn, plmn)) {
            return longName;
        }
        int rule = this.mPhoneExt.getServiceStateTracker() != null ? this.mPhoneExt.getServiceStateTracker().getCarrierNameDisplayBitmask(this.mPhoneExt.getServiceState()) : 0;
        IccRecordsEx iccRecords = this.mPhoneExt.getIccRecords();
        if ((rule & 1) != 1 || iccRecords == null || (spn = iccRecords.getServiceProviderName()) == null || spn.trim().length() <= 0) {
            return longName;
        }
        return spn.concat(radioTechStr);
    }

    private boolean getHwVmNotFromSimValue() {
        boolean valueFromProp = IS_HW_VM_NOT_FROM_SIM;
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("hw_voicemail_sim", this.mPhoneId, Boolean.class);
        logd("getHwVmNotFromSimValue, phoneId" + this.mPhoneId + ", card:" + valueFromCard + ", prop: " + valueFromProp);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    public String getDefaultVoiceMailAlphaTagText(Context mContext, String ret) {
        if (mContext == null) {
            return ret;
        }
        if (getHwVmNotFromSimValue() || ret == null || ret.length() == 0 || isCustVoicemailTag(mContext)) {
            return mContext.getText(HwPartResourceUtils.getResourceId("defaultVoiceMailAlphaTag")).toString();
        }
        return ret;
    }

    private boolean isCustVoicemailTag(Context mContext) {
        String carrier;
        String strVMTagNotFromConf = Settings.System.getString(mContext.getContentResolver(), "hw_vmtag_follow_language");
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            carrier = TelephonyManagerEx.getSimOperatorNumericForPhone(this.mPhoneId);
        } else {
            carrier = TelephonyManagerEx.getDefault().getSimOperator();
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

    public void setMeid(String meid) {
        this.mMeid = meid;
    }

    public String getPesn() {
        logd("[HwGSMPhoneReference]getPesn()");
        return this.mPhoneExt.getEsn();
    }

    @Override // com.android.internal.telephony.HwPhoneReferenceBase
    public boolean beforeHandleMessage(Message msg) {
        logd("beforeHandleMessage what = " + msg.what);
        int i = msg.what;
        if (i == 105) {
            updateReduceSARState();
            return true;
        } else if (i == 108) {
            logd("onGetLteReleaseVersionDone:");
            handleGetLteReleaseVersionDone(msg);
            return true;
        } else if (i == 111) {
            logd("beforeHandleMessage handled->EVENT_SET_MODE_TO_AUTO ");
            this.mPhoneExt.setNetworkSelectionModeAutomatic((Message) null);
            return true;
        } else if (i == 118) {
            logd("beforeHandleMessage EVENT_UPLOAD_OPERATOR_INFO");
            handleAvailableNetWorksUpload(msg);
            return true;
        } else if (i != 1000) {
            return super.beforeHandleMessage(msg);
        } else {
            if (msg.arg2 == 2) {
                logd("start retry get DEVICE_ID_MASK_ALL");
                this.mPhoneExt.getCi().getDeviceIdentity(this.mPhoneExt.obtainMessage(21, msg.arg1, 0, (Object) null));
            } else {
                logd("EVENT_RETRY_GET_DEVICE_ID msg.arg2:" + msg.arg2 + ", error!!");
            }
            return true;
        }
    }

    private void handleAvailableNetWorksUpload(Message msg) {
        ArrayList<OperatorInfoEx> searchResults = null;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            loge("[EVENT_UPLOAD_OPERATOR_INFO] error, msg obj is null");
            return;
        }
        ArrayList<OperatorInfoEx> searchResultsFromMessage = OperatorInfoEx.from(ar.getResult());
        if (ar.getException() == null && ar.getResult() != null) {
            searchResults = custAvailableNetworks(searchResultsFromMessage);
        }
        if (searchResults == null || searchResults.size() == 0) {
            searchResults = searchResultsFromMessage;
        } else {
            logd("[EVENT_UPLOAD_OPERATOR_INFO] Populated global version cust names for available networks.");
        }
        NetworkCallBackInteface networkCallBackInteface = this.mNetworkCallBackInterface;
        if (networkCallBackInteface != null) {
            networkCallBackInteface.showAvailableNetworks(OperatorInfoEx.convertToOperatorInfo(searchResults));
        } else {
            loge("[EVENT_UPLOAD_OPERATOR_INFO] error, callback obj is null!");
        }
    }

    public void afterHandleMessage(Message msg) {
        logd("afterHandleMessage what = " + msg.what);
        int i = msg.what;
        if (i == 1) {
            this.mPhoneExt.getCi().getDeviceIdentity(this.mPhoneExt.obtainMessage(21));
            logd("[HwGSMPhoneReference]PhoneBaseUtils.EVENT_RADIO_AVAILABLE");
            logd("Radio available, get lte release version");
            this.mPhoneExt.getCi().getLteReleaseVersion(this.mPhoneExt.obtainMessage(108));
        } else if (i != 5) {
            logd("unhandle event");
        } else {
            logd("Radio on, get lte release version");
            this.mPhoneExt.getCi().getLteReleaseVersion(this.mPhoneExt.obtainMessage(108));
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
        this.mPhoneExt.getCi().closeRrc();
    }

    private void qcRilGoDormant() {
        try {
            if (mQcRilHook == null) {
                mQcRilHook = Class.forName("com.qualcomm.qcrilhook.QcRilHook").getConstructor(Context.class).newInstance(this.mPhoneExt.getContext());
            }
            if (mQcRilHook != null) {
                mQcRilHook.getClass().getMethod("qcRilGoDormant", String.class).invoke(mQcRilHook, BuildConfig.FLAVOR);
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

    private int getCardType() {
        this.redcueSARSpeacialType1 = SystemPropertiesEx.get(PROP_REDUCE_SAR_SPECIAL_TYPE1, " ");
        this.redcueSARSpeacialType2 = SystemPropertiesEx.get(PROP_REDUCE_SAR_SPECIAL_TYPE2, " ");
        this.mncForSAR = SystemPropertiesEx.get("reduce.sar.imsi.mnc", "FFFFF");
        String str = this.mncForSAR;
        if (str == null || str.equals("FFFFF") || this.mncForSAR.length() < 3) {
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

    private int[][] getGradeData(boolean isReceiveOff) {
        return !isReceiveOff ? new int[][]{new int[]{0, REDUCE_SAR_NORMAL_WIFI_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON}, new int[]{REDUCE_SAR_SPECIAL_TYPE1_WIFI_OFF, REDUCE_SAR_SPECIAL_TYPE1_WIFI_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON}, new int[]{REDUCE_SAR_SPECIAL_TYPE2_WIFI_OFF, REDUCE_SAR_SPECIAL_TYPE2_WIFI_ON, REDUCE_SAR_SPECIAL_TYPE2_HOTSPOT_ON}} : new int[][]{new int[]{REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON}, new int[]{REDUCE_SAR_SPECIAL_TYPE1_WIFI_OFF, REDUCE_SAR_SPECIAL_TYPE1_WIFI_ON, REDUCE_SAR_NORMAL_HOTSPOT_ON}, new int[]{REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON, REDUCE_SAR_SPECIAL_TYPE1_HOTSPOT_ON}};
    }

    private void setPowerGrade(int powerGrade) {
        if (this.mPrevPowerGrade != powerGrade) {
            this.mPrevPowerGrade = powerGrade;
            logd("updateReduceSARState()    setPowerGrade() : " + powerGrade);
            this.mPhoneExt.getCi().setPowerGrade(powerGrade, (Message) null);
        }
    }

    private boolean reduceOldSar() {
        if (!SystemPropertiesEx.getBoolean("ro.config.old_reduce_sar", false)) {
            return false;
        }
        int wifiHotState = WifiManagerEx.getWifiApState(this.mWifiManager);
        logd("Radio available, set wifiHotState  " + wifiHotState);
        if (wifiHotState == 13) {
            setPowerGrade(1);
        } else if (wifiHotState == 11) {
            setPowerGrade(0);
        } else {
            loge("reduceOldSar, hot state is wrong, do nothing");
        }
        return true;
    }

    public void resetReduceSARPowerGrade() {
        this.mPrevPowerGrade = -1;
    }

    public void updateReduceSARState() {
        Boolean bool = this.mSarControlServiceExist;
        boolean isReceiveOFF = true;
        if (bool == null) {
            try {
                this.mSarControlServiceExist = this.mPhoneExt.getContext().getPackageManager().getApplicationInfo("com.huawei.sarcontrolservice", REDUCE_SAR_NORMAL_HOTSPOT_ON) != null;
                logd("updateReduceSARState mSarControlServiceExist=" + this.mSarControlServiceExist);
                if (this.mSarControlServiceExist.booleanValue() && !SystemPropertiesEx.getBoolean("ro.config.hw_ReduceSAR", false)) {
                    logd("updateReduceSARState mSarControlServiceExist=true");
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                this.mSarControlServiceExist = false;
                logd("updateReduceSARState mSarControlServiceExist=false");
            }
        } else if (bool.booleanValue() && !SystemPropertiesEx.getBoolean("ro.config.hw_ReduceSAR", false)) {
            return;
        }
        logd("updateReduceSARState()");
        if (!reduceOldSar() && SystemPropertiesEx.getBoolean("ro.config.hw_ReduceSAR", false)) {
            AudioManager localAudioManager = (AudioManager) this.mPhoneExt.getContext().getSystemService("audio");
            int cardType = getCardType();
            int wifiType = getWifiType(WifiManagerEx.getWifiApState(this.mWifiManager), this.mWifiManager.getWifiState());
            int state = TelephonyManagerEx.getDefault().getCallState();
            boolean isWiredHeadsetOn = localAudioManager.isWiredHeadsetOn();
            boolean isSpeakerphoneOn = localAudioManager.isSpeakerphoneOn();
            if (!isWiredHeadsetOn && !isSpeakerphoneOn && state == 2) {
                isReceiveOFF = false;
            }
            if (SystemPropertiesEx.getBoolean("persist.gsm.ReceiveTestMode", false)) {
                isReceiveOFF = SystemPropertiesEx.getBoolean("persist.gsm.ReceiveTestValue", false);
                logd("hw_ReceiveTestMode = true isReceiveOFF = " + isReceiveOFF);
            }
            int reduceData = getGradeData(isReceiveOFF)[cardType][wifiType];
            logd("updateReduceSARState(); isWiredHeadsetOn=" + isWiredHeadsetOn + ", isSpeakerphoneOn=" + isSpeakerphoneOn + ", state=" + state + ", reduceData=" + reduceData);
            setPowerGrade(reduceData);
        }
    }

    public void dispose() {
        HwVpApStatusHandler hwVpApStatusHandler;
        if (IS_WCDMA_VP_ENABLED && (hwVpApStatusHandler = this.mHwVpApStatusHandler) != null) {
            hwVpApStatusHandler.dispose();
        }
        this.mPhoneExt.getContext().unregisterReceiver(this.mReceiver);
    }

    public void switchVoiceCallBackgroundState(int state) {
        this.mHwGsmCdmaPhoneInner.gsmCdmaPhoneSwitchVoiceCallBackgroundState(state);
    }

    public boolean isMmiCode(String dialString, UiccCardApplicationEx app) {
        if (dialString == null || app == null) {
            return false;
        }
        MmiCodeExt mmiCode = new MmiCodeExt();
        mmiCode.setGsmMmiCode(dialString, this.mHwGsmCdmaPhoneInner, app);
        if (!mmiCode.isNull()) {
            return true;
        }
        switch (dialString.charAt(0)) {
            case '0':
            case '3':
            case '4':
            case '5':
                if (dialString.length() == 1) {
                    return true;
                }
                return false;
            case '1':
            case '2':
                if (dialString.length() <= 2) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public void getPOLCapabilty(Message response) {
        this.mPhoneExt.getCi().getPOLCapabilty(response);
    }

    public void getPreferedOperatorList(Message response) {
        this.mPhoneExt.getCi().getCurrentPOLList(response);
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        this.mPhoneExt.getCi().setPOLEntry(index, numeric, nAct, response);
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public String processPlusSymbol(String dialNumber, String imsi) {
        if (TextUtils.isEmpty(dialNumber) || !dialNumber.startsWith("+") || TextUtils.isEmpty(imsi) || imsi.length() > 15) {
            return dialNumber;
        }
        int mccNum = -1;
        try {
            mccNum = Integer.parseInt(imsi.substring(0, 3));
        } catch (NumberFormatException e) {
            loge("processPlusSymbol NumberFormatException");
        }
        return HwPhoneNumberUtils.convertPlusByMcc(dialNumber, mccNum);
    }

    public boolean isSupportCFT() {
        boolean isSupportCFTFlag = this.mPhoneExt.isSupportCFT();
        logd("isSupportCFT=" + isSupportCFTFlag);
        return isSupportCFTFlag;
    }

    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        this.mPhoneExt.setCallForwardingUncondTimerOption(startHour, startMinute, endHour, endMinute, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, onComplete);
    }

    public boolean getImsSwitch() {
        return this.mPhoneExt.getCi().getImsSwitch();
    }

    public void setImsSwitch(boolean on) {
        this.mPhoneExt.getCi().setImsSwitch(on);
    }

    public void processEccNumber(IServiceStateTrackerInner gSst) {
        boolean isUseRplmn = false;
        if (SystemPropertiesEx.getBoolean("ro.config.hw_globalEcc", false)) {
            logd("EVENT_SIM_RECORDS_LOADED!!!!");
            SystemPropertiesEx.set(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "usim_present");
            String hplmn = TelephonyManagerEx.getSimOperatorNumericForPhone(this.mPhoneId);
            boolean isRegHomeState = isRegisteredHomeNetworkForTransportType(this.mPhoneId, 1);
            String rplmn = getRplmn();
            if (SystemPropertiesEx.getBoolean("ro.config.hw_eccNumUseRplmn", false) && !TextUtils.isEmpty(rplmn) && !isRegHomeState) {
                isUseRplmn = true;
            }
            if (TextUtils.isEmpty(hplmn)) {
                logd("received EVENT_SIM_RECORDS_LOADED but not hplmn !!!!");
            } else if (isUseRplmn) {
                logd("processEccNumber: Use Rplmn, isRegHomeState= " + isRegHomeState + ", rplmn=" + rplmn);
                globalEccCustom(rplmn);
            } else {
                globalEccCustom(hplmn);
            }
        }
    }

    @Override // com.android.internal.telephony.HwPhoneReferenceBase
    public void globalEccCustom(String operatorNumeric) {
        String eccListWithCard = null;
        String eccListNoCard = null;
        String forceEccState = SystemPropertiesEx.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        boolean isRegStateChanged = this.mNetworkRegState != getCombinedNetworkRegState(this.mPhoneId);
        logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: operator numeric = " + operatorNumeric + "; preOperatorNumeric = " + this.preOperatorNumeric + ";forceEccState  = " + forceEccState + ", isRegStateChanged=" + isRegStateChanged);
        if (TextUtils.isEmpty(operatorNumeric)) {
            return;
        }
        if (!operatorNumeric.equals(this.preOperatorNumeric) || !forceEccState.equals("invalid") || isRegStateChanged) {
            this.preOperatorNumeric = operatorNumeric;
            SystemPropertiesEx.set(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
            if ((TextUtils.isEmpty(getRplmn()) || isRegisteredHomeNetworkForTransportType(this.mPhoneId, 1)) && virtualNetEccFormCarrier(this.mPhoneId)) {
                int slotId = this.mPhoneId;
                try {
                    eccListWithCard = (String) HwCfgFilePolicy.getValue("virtual_ecclist_withcard", slotId, String.class);
                    eccListNoCard = (String) HwCfgFilePolicy.getValue("virtual_ecclist_nocard", slotId, String.class);
                    logd("globalEccCustom: Registered Home State, Use VirtualNet Ecc eccListWithCard=" + eccListWithCard + " ecclistNocard=" + eccListNoCard);
                } catch (ClassCastException e) {
                    loge("Failed to get ecclist in carrier ClassCastException");
                } catch (Exception e2) {
                    loge("Failed to get ecclist in carrier");
                }
            }
            String custEcc = getCustEccList(operatorNumeric);
            if (!TextUtils.isEmpty(custEcc)) {
                String[] custEccArray = custEcc.split(":");
                if (custEccArray.length == 3 && custEccArray[0].equals(operatorNumeric) && !TextUtils.isEmpty(custEccArray[1]) && !TextUtils.isEmpty(custEccArray[2])) {
                    eccListWithCard = custEccArray[1];
                    eccListNoCard = custEccArray[2];
                }
            }
            setEmergencyByEccList(eccListWithCard, eccListNoCard, operatorNumeric);
        }
    }

    private void setEmergencyByEccList(String oriEccListWithCard, String oriEccListNoCard, String operatorNumeric) {
        String eccListWithCard = oriEccListWithCard;
        String eccListNoCard = oriEccListNoCard;
        if (eccListWithCard == null) {
            Cursor cursor = null;
            try {
                cursor = this.mPhoneExt.getContext().getContentResolver().query(TelephonyEx.GlobalMatchs.CONTENT_URI, new String[]{"_id", HwTelephony.NumMatchs.NAME, "numeric", HwTelephony.VirtualNets.ECC_WITH_CARD, HwTelephony.VirtualNets.ECC_NO_CARD}, "numeric= ?", new String[]{operatorNumeric}, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
            } catch (SQLException e) {
                loge("Query CONTENT_URI error.");
            }
            if (cursor == null) {
                logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: No matched emergency numbers in db.");
                this.mPhoneExt.getCi().requestSetEmergencyNumbers(BuildConfig.FLAVOR, BuildConfig.FLAVOR);
                return;
            }
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    eccListWithCard = cursor.getString(3);
                    eccListNoCard = cursor.getString(4);
                    cursor.moveToNext();
                }
            } catch (Exception e2) {
                logd("[SLOT" + this.mPhoneId + "]globalEccCustom: global version cause exception!");
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
            cursor.close();
        }
        logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: ecc_withcard = " + eccListWithCard + ", ecc_nocard = " + eccListNoCard);
        String eccListWithCard2 = eccListWithCard != null ? eccListWithCard : BuildConfig.FLAVOR;
        String eccListNoCard2 = eccListNoCard != null ? eccListNoCard : BuildConfig.FLAVOR;
        if (!eccListWithCard2.equals(BuildConfig.FLAVOR) || !eccListNoCard2.equals(BuildConfig.FLAVOR)) {
            this.mPhoneExt.getCi().requestSetEmergencyNumbers(eccListWithCard2, eccListNoCard2);
        } else {
            this.mPhoneExt.getCi().requestSetEmergencyNumbers(BuildConfig.FLAVOR, BuildConfig.FLAVOR);
        }
    }

    public String getHwCdmaPrlVersion() {
        String prlVersion;
        int slotId = this.mPhoneExt.getPhoneId();
        int simCardState = TelephonyManagerEx.getDefault().getSimState(slotId);
        if (simCardState != 5 || !HwTelephonyManagerInner.getDefault().isCDMASimCard(slotId)) {
            prlVersion = "0";
        } else {
            prlVersion = this.mPhoneExt.getCi().getHwPrlVersion();
        }
        logd("getHwCdmaPrlVersion: prlVersion=" + prlVersion + ", slotId=" + slotId + ", simState=" + simCardState);
        return prlVersion;
    }

    public String getHwCdmaEsn() {
        String esn;
        int slotId = this.mPhoneExt.getPhoneId();
        int simCardState = TelephonyManagerEx.getDefault().getSimState(slotId);
        if (simCardState != 5 || !HwTelephonyManagerInner.getDefault().isCDMASimCard(slotId)) {
            esn = "0";
        } else {
            esn = this.mPhoneExt.getCi().getHwUimid();
        }
        logd("getHwCdmaEsn: esn=" + esn + ", slotId=" + slotId + ", simState=" + simCardState);
        return esn;
    }

    public String getVMNumberWhenIMSIChange() {
        if (this.mHwGsmCdmaPhoneInner == null || this.mPhoneExt == null) {
            return null;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mPhoneExt.getContext());
        String number = null;
        String mIccId = this.mPhoneExt.getIccSerialNumber();
        if (!TextUtils.isEmpty(mIccId)) {
            String temp = sp.getString(mIccId + this.mPhoneExt.getPhoneId(), null);
            if (temp != null) {
                number = temp;
            }
            logd("getVMNumberWhenIMSIChange number= xxxxxx");
        }
        return number;
    }

    public boolean setISMCOEX(String setISMCoex) {
        this.mPhoneExt.getCi().setISMCOEX(setISMCoex, (Message) null);
        return true;
    }

    private String getCustEccList(String operatorNumeric) {
        String custEccList = null;
        try {
            custEccList = Settings.System.getString(this.mPhoneExt.getContext().getContentResolver(), "hw_cust_emergency_nums");
        } catch (IllegalArgumentException e) {
            loge("Failed to load vmNum from SettingsEx IllegalArgumentException");
        } catch (Exception e2) {
            loge("Failed to load vmNum from SettingsEx");
        }
        if (TextUtils.isEmpty(custEccList) || TextUtils.isEmpty(operatorNumeric)) {
            return BuildConfig.FLAVOR;
        }
        String[] custEccListItems = custEccList.split(";");
        for (int i = 0; i < custEccListItems.length; i++) {
            String[] custItem = custEccListItems[i].split(":");
            if (custItem.length == 3 && custItem[0].equals(operatorNumeric)) {
                return custEccListItems[i];
            }
        }
        return BuildConfig.FLAVOR;
    }

    public void setImsDomainConfig(int domainType) {
        this.mPhoneExt.getCi().setImsDomainConfig(domainType, (Message) null);
    }

    public void getImsDomain(Message response) {
        this.mPhoneExt.getCi().getImsDomain(response);
    }

    public void handleUiccAuth(int auth_type, byte[] rand, byte[] auth, Message response) {
        this.mPhoneExt.getCi().handleUiccAuth(auth_type, rand, auth, response);
    }

    public void handleMapconImsaReq(byte[] Msg) {
        this.mPhoneExt.getCi().handleMapconImsaReq(Msg, (Message) null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String msg) {
        RlogEx.i(this.subTag, msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String msg) {
        RlogEx.e(this.subTag, msg);
    }

    private void loge(String msg, Throwable tr) {
        RlogEx.e(this.subTag, msg, tr);
    }

    public void selectCsgNetworkManually(Message response) {
        HwCustHwGSMPhoneReference hwCustHwGSMPhoneReference = this.mHwCustHwGSMPhoneReference;
        if (hwCustHwGSMPhoneReference != null) {
            hwCustHwGSMPhoneReference.selectCsgNetworkManually(response);
        }
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        HwCustHwGSMPhoneReference hwCustHwGSMPhoneReference = this.mHwCustHwGSMPhoneReference;
        if (hwCustHwGSMPhoneReference != null) {
            hwCustHwGSMPhoneReference.judgeToLaunchCsgPeriodicSearchTimer();
        }
    }

    public void registerForCsgRecordsLoadedEvent() {
        HwCustHwGSMPhoneReference hwCustHwGSMPhoneReference = this.mHwCustHwGSMPhoneReference;
        if (hwCustHwGSMPhoneReference != null) {
            hwCustHwGSMPhoneReference.registerForCsgRecordsLoadedEvent();
        }
    }

    public void unregisterForCsgRecordsLoadedEvent() {
        HwCustHwGSMPhoneReference hwCustHwGSMPhoneReference = this.mHwCustHwGSMPhoneReference;
        if (hwCustHwGSMPhoneReference != null) {
            hwCustHwGSMPhoneReference.unregisterForCsgRecordsLoadedEvent();
        }
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        this.mPhoneExt.getCi().notifyCellularCommParaReady(paratype, pathtype, response);
    }

    public boolean isDualImsAvailable() {
        return HwImsManagerInner.isDualImsAvailable();
    }

    public boolean isUssdOkForRelease() {
        boolean ussdIsOkForRelease = false;
        Context mContext = this.mHwGsmCdmaPhoneInner != null ? this.mPhoneExt.getContext() : null;
        if (mContext == null) {
            return false;
        }
        CarrierConfigManager configLoader = (CarrierConfigManager) mContext.getSystemService("carrier_config");
        PersistableBundle bundle = null;
        if (configLoader != null) {
            bundle = configLoader.getConfigForSubId(this.mPhoneExt.getSubId());
        }
        if (bundle != null) {
            ussdIsOkForRelease = bundle.getBoolean(USSD_IS_OK_FOR_RELEASE);
        }
        logd("isUssdOkForRelease: ussdIsOkForRelease " + ussdIsOkForRelease);
        return ussdIsOkForRelease;
    }

    private void handleGetLteReleaseVersionDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            logd("Error in get lte release version.");
            return;
        }
        int[] resultint = (int[]) ar.getResult();
        if (resultint == null) {
            logd("Error in get lte release version: null resultint");
        } else if (resultint.length != 0) {
            logd("onGetLteReleaseVersionDone: result=" + resultint[0]);
            this.mLteReleaseVersion = resultint[0];
        }
    }

    private boolean handleSpnRule(String spnOrgId1OrApnName) {
        IccRecordsEx iccRecordsEx = this.mSimRecordsEx;
        if (iccRecordsEx == null || spnOrgId1OrApnName == null) {
            return false;
        }
        String spn = iccRecordsEx.getServiceProviderName();
        logd("[EONS] spn = " + spn + ", spnOrgId1OrApnName(gid1 start with 0x) = " + spnOrgId1OrApnName);
        if (!spnOrgId1OrApnName.equals(spn)) {
            return false;
        }
        logd("[EONS] ShowPLMN: use the spn rule");
        return true;
    }

    private boolean handleGid1Rule(String spnOrgId1OrApnName) {
        byte[] gid1;
        IccRecordsEx iccRecordsEx = this.mSimRecordsEx;
        if (iccRecordsEx == null || spnOrgId1OrApnName == null || (gid1 = iccRecordsEx.getGID1()) == null || gid1.length <= 0 || spnOrgId1OrApnName.length() <= 2 || !spnOrgId1OrApnName.substring(0, 2).equalsIgnoreCase("0x")) {
            return false;
        }
        logd("[EONS] gid1 = " + ((int) gid1[0]));
        byte[] gid1valueBytes = IccUtilsEx.hexStringToBytes(spnOrgId1OrApnName.substring(2));
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
        if (this.mSimRecordsEx == null || spnOrgId1OrApnName == null) {
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
        if (this.mSimRecordsEx == null || spnOrgId1OrApnName == null) {
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

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.HwPhoneReferenceBase
    public boolean isCurrentPhoneType() {
        return TelephonyManagerEx.getCurrentPhoneType(this.mSubId) == 1;
    }

    public void startUploadAvailableNetworks(Object obj) {
        if (obj instanceof NetworkCallBackInteface) {
            this.mNetworkCallBackInterface = (NetworkCallBackInteface) obj;
            this.mPhoneExt.getCi().getAvailableNetworks(this.mHandler.obtainMessage(2));
            return;
        }
        loge("invalid callback object, obj is null " + (obj instanceof Handler));
    }

    public void stopUploadAvailableNetworks() {
        this.mPhoneExt.getCi().setNetworkSelectionModeManual("00000,-1", (Message) null);
        this.mNetworkCallBackInterface = null;
    }
}
