package com.android.internal.telephony;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.emergency.EmergencyNumber;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.huawei.android.app.ActivityManagerNativeEx;
import com.huawei.android.internal.util.XmlUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.TelephonyEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.GsmAlphabetEx;
import com.huawei.internal.telephony.MccTableEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.emergency.EmergencyNumberEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class GlobalParamsAdaptor {
    public static final String ACTION_SET_GLOBAL_AUTO_PARAM_DONE = "android.intent.action.ACTION_SET_GLOBAL_AUTO_PARAM_DONE";
    private static final String APGS_SERVERS_DOCUMENT = "agpsServers";
    private static final String CUSTXMLPATH = "/data/cust/xml/";
    private static final int DEFAULT_THRESHOLD = 0;
    private static final int DEFAULT_VERSION_NUM = -1;
    private static final int ECC_FAKE_INDEX = 3;
    private static final int ECC_MCC_MNC_INDEX = 0;
    private static final int ECC_MCC_MNC_LEN_MAX = 6;
    private static final int ECC_MCC_MNC_LEN_MIN = 5;
    private static final int ECC_MNC_BEGIN_INDEX = 3;
    private static final int ECC_NUMS_INDEX = 1;
    private static final String ECC_NUMS_SEPARATOR = ",";
    private static final String GLOBAL_AGPS_SERVERS_FILE_NAME = "globalAgpsServers-conf.xml";
    private static final String HWCFGPOLICYPATH = "hwCfgPolicyPath";
    private static final int INITIAL_THRESHHOLD_VALUE = -1;
    private static final boolean IS_PRE_POST_PAY = SystemPropertiesEx.getBoolean("ro.config.hw_is_pre_post_pay", false);
    private static final int IS_VMN_SHORT_CODE_INDEX = 1;
    static final String LOG_TAG = "GlobalParamsAdaptor";
    private static final String MCCMNC = "mccmnc";
    private static final int MCCMNC_LENGTH = 3;
    private static final int MIN_MATCH = 7;
    private static final int NUM_MATCH_INDEX = 3;
    private static final int NUM_MATCH_SHORT_INDEX = 4;
    private static final String SERVER_NAME = "name";
    private static final int SMS_NOT_TO_MMS_TEXTTHRESHOLD = -1;
    private static final String SUPL_PORT = "supl_port";
    private static final String SUPL_URL = "supl_host";
    private static final String SYSTEMXMLPATH = "/system/etc/";
    private static boolean waitingForSetupData = false;
    private int mPhoneId;

    public GlobalParamsAdaptor(int phoneId) {
        RlogEx.i(LOG_TAG, "contructor phoneId = " + phoneId);
        this.mPhoneId = phoneId;
    }

    public static boolean getPrePostPayPreCondition() {
        RlogEx.i(LOG_TAG, "waitingForSetupData = " + waitingForSetupData + " IS_PRE_POST_PAY = " + IS_PRE_POST_PAY);
        return waitingForSetupData && IS_PRE_POST_PAY;
    }

    private static void setWaitingForSetupData(boolean value) {
        Log.d(LOG_TAG, "setWaitingForSetupData, value = " + value);
        waitingForSetupData = value;
    }

    public static void tryToActionPrePostPay() {
        Log.d(LOG_TAG, "broadcast HwTelephonyIntentsInner.ACTION_PRE_POST_PAY");
        Intent intent = new Intent("android.intent.action.ACTION_PRE_POST_PAY");
        intent.addFlags(536870912);
        intent.putExtra("prePostPayState", true);
        ActivityManagerNativeEx.broadcastStickyIntent(intent, (String) null, 0);
    }

    private boolean arrayContains(String[] array, String value) {
        for (String str : array) {
            if (str.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public void checkPrePostPay(String currentMccMnc, String currentImsi, Context context) {
        if (currentMccMnc != null && currentImsi != null && context != null) {
            String prepayPostpayMccMncsStrings = null;
            String oldImsiString = null;
            try {
                prepayPostpayMccMncsStrings = Settings.System.getString(context.getContentResolver(), "prepay_postpay_mccmncs");
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "Could not load default locales1 IllegalArgumentException");
            } catch (Exception e2) {
                Log.e(LOG_TAG, "Could not load default1 locales");
                return;
            }
            try {
                oldImsiString = Settings.System.getString(context.getContentResolver(), "old_imsi");
            } catch (IllegalArgumentException e3) {
                Log.e(LOG_TAG, "Could not load default locales IllegalArgumentException");
            } catch (Exception e4) {
                Log.e(LOG_TAG, "Could not load default locales");
            }
            if (prepayPostpayMccMncsStrings != null) {
                boolean isContainer = arrayContains(prepayPostpayMccMncsStrings.split(ECC_NUMS_SEPARATOR), currentMccMnc);
                boolean isEqual = false;
                if (oldImsiString != null) {
                    isEqual = currentImsi.equals(oldImsiString.trim());
                }
                Settings.System.putString(context.getContentResolver(), "old_imsi", currentImsi);
                if (isContainer && !isEqual) {
                    if (IS_PRE_POST_PAY) {
                        setWaitingForSetupData(true);
                    } else {
                        tryToActionPrePostPay();
                    }
                }
            }
        }
    }

    private void checkMultiSimMaxMessageSize() {
        StringBuilder sb = new StringBuilder();
        sb.append("gsm.sms.max.message.size");
        int i = 1;
        sb.append(this.mPhoneId == 0 ? 1 : 0);
        if (SystemPropertiesEx.getInt(sb.toString(), 0) == 0) {
            SystemPropertiesEx.set("gsm.sms.max.message.size", Integer.toString(SystemPropertiesEx.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0)));
            return;
        }
        int i2 = SystemPropertiesEx.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("gsm.sms.max.message.size");
        sb2.append(this.mPhoneId == 0 ? 1 : 0);
        if (i2 < SystemPropertiesEx.getInt(sb2.toString(), 0)) {
            SystemPropertiesEx.set("gsm.sms.max.message.size", Integer.toString(SystemPropertiesEx.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0)));
            return;
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append("gsm.sms.max.message.size");
        if (this.mPhoneId != 0) {
            i = 0;
        }
        sb3.append(i);
        SystemPropertiesEx.set("gsm.sms.max.message.size", Integer.toString(SystemPropertiesEx.getInt(sb3.toString(), 0)));
    }

    private void checkMultiSmsToMmsTextThreshold() {
        int setThreshold;
        int cur = SystemPropertiesEx.getInt("gsm.sms.to.mms.textthreshold" + this.mPhoneId, 0);
        StringBuilder sb = new StringBuilder();
        sb.append("gsm.sms.to.mms.textthreshold");
        sb.append(this.mPhoneId == 0 ? 1 : 0);
        int ant = SystemPropertiesEx.getInt(sb.toString(), 0);
        Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>mPhoneId=" + this.mPhoneId + ", cur= " + cur + ", ant= " + ant);
        if ((this.mPhoneId == 1 && TelephonyManagerEx.getSimState(0) == 1) || (this.mPhoneId == 0 && TelephonyManagerEx.getSimState(1) == 1)) {
            Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>#one card on#mPhoneId=" + this.mPhoneId + ", cur = " + cur);
            setThreshold = cur;
        } else {
            Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>#dual card on#mPhoneId=" + this.mPhoneId + ", cur = " + cur + ", ant = " + ant);
            if (cur == 0) {
                setThreshold = ant;
            } else if (cur == -1) {
                if (ant == -1 || ant == 0) {
                    setThreshold = cur;
                } else {
                    setThreshold = ant;
                }
            } else if (ant == -1 || ant == 0) {
                setThreshold = cur;
            } else {
                setThreshold = ant < cur ? ant : cur;
            }
        }
        Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold set_threshold= " + setThreshold);
        SystemPropertiesEx.set("gsm.sms.to.mms.textthreshold", Integer.toString(setThreshold));
    }

    private boolean isCustPlmn(Context context, String simMccMnc) {
        if (context == null) {
            return false;
        }
        String custPlmnsString = Settings.System.getString(context.getContentResolver(), "hw_cust_7bit_enabled_mcc");
        if (TextUtils.isEmpty(custPlmnsString) || TextUtils.isEmpty(simMccMnc) || simMccMnc.length() < 3) {
            return false;
        }
        String[] custPlmns = custPlmnsString.split(";");
        for (int i = 0; i < custPlmns.length; i++) {
            if (simMccMnc.substring(0, 3).equals(custPlmns[i]) || simMccMnc.equals(custPlmns[i])) {
                return true;
            }
        }
        return false;
    }

    private void globalAutoMatch() {
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            Integer numMatchSlot = (Integer) HwCfgFilePolicy.getValue("num_match", this.mPhoneId, Integer.class);
            if (numMatchSlot != null) {
                SystemPropertiesEx.set("gsm.hw.matchnum" + this.mPhoneId, numMatchSlot.toString());
                SystemPropertiesEx.set("gsm.hw.matchnum.short" + this.mPhoneId, numMatchSlot.toString());
            }
            Integer numMatchShortSlot = (Integer) HwCfgFilePolicy.getValue("num_match_short", this.mPhoneId, Integer.class);
            if (numMatchShortSlot != null) {
                SystemPropertiesEx.set("gsm.hw.matchnum.short" + this.mPhoneId, numMatchShortSlot.toString());
            }
            checkMultiSimNumMatch();
            Integer maxMessageSizeSlot = (Integer) HwCfgFilePolicy.getValue("max_message_size", this.mPhoneId, Integer.class);
            if (maxMessageSizeSlot != null) {
                SystemPropertiesEx.set("gsm.sms.max.message.size" + this.mPhoneId, maxMessageSizeSlot.toString());
            }
            checkMultiSimMaxMessageSize();
            Integer smsToMmsTextThresholdSlot = (Integer) HwCfgFilePolicy.getValue("sms_to_mms_textthreshold", this.mPhoneId, Integer.class);
            if (smsToMmsTextThresholdSlot != null && smsToMmsTextThresholdSlot.intValue() >= -1) {
                SystemPropertiesEx.set("gsm.sms.to.mms.textthreshold" + this.mPhoneId, smsToMmsTextThresholdSlot.toString());
            }
            checkMultiSmsToMmsTextThreshold();
        } else {
            Integer numMatch = (Integer) HwCfgFilePolicy.getValue("num_match", Integer.class);
            if (numMatch != null) {
                SystemPropertiesEx.set("gsm.hw.matchnum", numMatch.toString());
                SystemPropertiesEx.set("gsm.hw.matchnum.short", numMatch.toString());
            }
            Integer numMatchShort = (Integer) HwCfgFilePolicy.getValue("num_match_short", Integer.class);
            if (numMatchShort != null) {
                SystemPropertiesEx.set("gsm.hw.matchnum.short", numMatchShort.toString());
            }
            Integer maxMessageSize = (Integer) HwCfgFilePolicy.getValue("max_message_size", Integer.class);
            if (maxMessageSize != null) {
                SystemPropertiesEx.set("gsm.sms.max.message.size", maxMessageSize.toString());
            }
            Integer smsToMmsTextThreshold = (Integer) HwCfgFilePolicy.getValue("sms_to_mms_textthreshold", Integer.class);
            if (smsToMmsTextThreshold != null && smsToMmsTextThreshold.intValue() >= -1) {
                SystemPropertiesEx.set("gsm.sms.to.mms.textthreshold", smsToMmsTextThreshold.toString());
            }
        }
        Integer sms7bitEnabled = (Integer) HwCfgFilePolicy.getValue("sms_7bit_enabled", Integer.class);
        if (sms7bitEnabled != null) {
            SystemPropertiesEx.set("gsm.sms.7bit.enabled", sms7bitEnabled.toString());
        }
        Integer smsCodingNational = (Integer) HwCfgFilePolicy.getValue("sms_coding_national", Integer.class);
        if (smsCodingNational != null) {
            SystemPropertiesEx.set("gsm.sms.coding.national", smsCodingNational.toString());
        }
    }

    public void checkGlobalAutoMatchParam(String currentMccmnc, Context context) {
        globalAutoMatch();
        if (currentMccmnc != null && context != null) {
            Cursor cursor = null;
            try {
                Cursor cursor2 = context.getContentResolver().query(HwTelephony.NumMatchs.CONTENT_URI, new String[]{"_id", HwTelephony.NumMatchs.IS_VMN_SHORT_CODE}, "numeric= ?", new String[]{currentMccmnc}, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
                if (cursor2 == null) {
                    Log.e(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: No matched auto match params in db.");
                    if (cursor2 != null) {
                        try {
                            cursor2.close();
                        } catch (SQLException e) {
                            Log.e(LOG_TAG, "close cursor SQLiteException.");
                        }
                    }
                } else {
                    cursor2.moveToFirst();
                    while (!cursor2.isAfterLast()) {
                        int isVmnShortCode = cursor2.getInt(1);
                        if (TelephonyManagerEx.isMultiSimEnabled()) {
                            Log.d(LOG_TAG, "checkGlobalAutoMatchParam mPhoneId = " + this.mPhoneId + " simState = " + TelephonyManagerEx.getSimState(this.mPhoneId));
                            if (this.mPhoneId == 1 && TelephonyManagerEx.getSimState(0) == 5) {
                                Log.d(LOG_TAG, "card 2 ready, card 1 ready, just return, don't go into send broadcast below");
                                try {
                                    cursor2.close();
                                    return;
                                } catch (SQLException e2) {
                                    Log.e(LOG_TAG, "close cursor SQLiteException.");
                                    return;
                                }
                            }
                        }
                        SystemPropertiesEx.set("gsm.hw.matchnum.vmn_shortcode", Integer.toString(isVmnShortCode));
                        Log.d(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: after setprop numMatch = " + SystemPropertiesEx.getInt("gsm.hw.matchnum", 0) + ", numMatchShort = " + SystemPropertiesEx.getInt("gsm.hw.matchnum.short", 0) + ", sms7BitEnabled = " + SystemPropertiesEx.getBoolean("gsm.sms.7bit.enabled", false) + ", smsCodingNational = " + SystemPropertiesEx.getInt("gsm.sms.coding.national", 0) + ", max_message_size = " + SystemPropertiesEx.getInt("gsm.sms.max.message.size", 0) + ", sms_to_mms_textthreshold = " + SystemPropertiesEx.getInt("gsm.sms.to.mms.textthreshold", 0));
                        cursor2.moveToNext();
                    }
                    try {
                        cursor2.close();
                    } catch (SQLException e3) {
                        Log.e(LOG_TAG, "close cursor SQLiteException.");
                    }
                    setGlobalParam(context, currentMccmnc);
                }
            } catch (SQLiteException e4) {
                Log.e(LOG_TAG, "checkGlobalAutoMatchParam: SQLiteException.");
                if (0 != 0) {
                    cursor.close();
                }
            } catch (IllegalArgumentException e5) {
                Log.e(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: global version cause IllegalArgumentException");
                if (0 != 0) {
                    cursor.close();
                }
            } catch (Exception e6) {
                Log.e(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: global version cause exception!");
                if (0 != 0) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        cursor.close();
                    } catch (SQLException e7) {
                        Log.e(LOG_TAG, "close cursor SQLiteException.");
                    }
                }
                throw th;
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0022: APUT  
      (r1v0 'temp' int[] A[D('temp' int[])])
      (0 ??[int, short, byte, char])
      (wrap: int : 0x001e: INVOKE  (r2v8 int) = ("gsm.sms.coding.national"), (0 int) type: STATIC call: com.huawei.android.os.SystemPropertiesEx.getInt(java.lang.String, int):int)
     */
    private void setGlobalParam(Context context, String currentMccmnc) {
        int[] temp = new int[1];
        if (SystemPropertiesEx.getInt("ro.config.smsCoding_National", 0) != 0) {
            temp[0] = SystemPropertiesEx.getInt("ro.config.smsCoding_National", 0);
            GsmAlphabetEx.setEnabledSingleShiftTables(temp);
        } else if (SystemPropertiesEx.getInt("gsm.sms.coding.national", 0) != 0) {
            temp[0] = SystemPropertiesEx.getInt("gsm.sms.coding.national", 0);
            GsmAlphabetEx.setEnabledSingleShiftTables(temp);
        }
        if (!TelephonyManagerEx.isMultiSimEnabled() || this.mPhoneId != 1 || TelephonyManagerEx.getSimState(0) == 1) {
            Intent intentSetGlobalParamDone = new Intent(ACTION_SET_GLOBAL_AUTO_PARAM_DONE);
            intentSetGlobalParamDone.putExtra("mccMnc", currentMccmnc);
            context.sendStickyBroadcast(intentSetGlobalParamDone);
            return;
        }
        Log.d(LOG_TAG, " card 2 ready, card 1 inserted, maybe not ready, don't send broadcast");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0042, code lost:
        if (r10 != null) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005b, code lost:
        if (0 == 0) goto L_0x005e;
     */
    public void checkGlobalEccNum(String currentMccmnc, Context context) {
        if (currentMccmnc != null && context != null) {
            String custEccNumsStrFromGlobalMatch = null;
            String custEccNumsStr = null;
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(TelephonyEx.GlobalMatchs.CONTENT_URI, new String[]{"_id", "name", "numeric", "ecc_fake"}, "numeric= ?", new String[]{currentMccmnc}, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        custEccNumsStrFromGlobalMatch = cursor.getString(3);
                        cursor.moveToNext();
                    }
                }
            } catch (SQLiteException e) {
                Log.e(LOG_TAG, "sql lite exception hanppen!");
            } catch (Exception e2) {
                Log.e(LOG_TAG, "global version cause exception!");
                if (0 != 0) {
                    cursor.close();
                }
                if (custEccNumsStrFromGlobalMatch == null || custEccNumsStrFromGlobalMatch.equals(BuildConfig.FLAVOR)) {
                    try {
                        custEccNumsStr = Settings.System.getString(context.getContentResolver(), "global_cust_ecc_nums");
                    } catch (IllegalArgumentException e3) {
                        Log.e(LOG_TAG, "Could not load default locales IllegalArgumentException");
                    } catch (Exception e4) {
                        Log.e(LOG_TAG, "Could not load default locales");
                        return;
                    }
                }
                if (custEccNumsStrFromGlobalMatch == null || custEccNumsStrFromGlobalMatch.equals(BuildConfig.FLAVOR)) {
                    updateEmergencyByCustItem(custEccNumsStr, currentMccmnc);
                    return;
                }
                try {
                    updateFakeEccEmergencyList(currentMccmnc, custEccNumsStrFromGlobalMatch);
                } catch (IllegalArgumentException e5) {
                    Log.e(LOG_TAG, "Failed to save ril.ecclist to system property IllegalArgumentException");
                } catch (Exception e6) {
                    Log.e(LOG_TAG, "Failed to save ril.ecclist to system property");
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    private void updateEmergencyByCustItem(String custEccNumsStr, String currentMccmnc) {
        String[] custEccNumsItems;
        if (!(custEccNumsStr == null || custEccNumsStr.equals(BuildConfig.FLAVOR))) {
            for (String str : custEccNumsStr.split(";")) {
                String[] custNumItem = str.split(":");
                if (custNumItem.length == 2 && (custNumItem[0].equalsIgnoreCase(currentMccmnc) || custNumItem[0].equalsIgnoreCase(currentMccmnc.substring(0, 3)))) {
                    try {
                        updateFakeEccEmergencyList(custNumItem[0], custNumItem[1]);
                        return;
                    } catch (IllegalArgumentException e) {
                        Log.e(LOG_TAG, "Failed to save ril.ecclist to system property IllegalArgumentException");
                        return;
                    } catch (Exception e2) {
                        Log.e(LOG_TAG, "Failed to save ril.ecclist to system property");
                        return;
                    }
                }
            }
        }
    }

    private boolean isMccMncValid(String mccMnc) {
        if (mccMnc == null) {
            return false;
        }
        if (mccMnc.length() >= 5 && mccMnc.length() <= 6) {
            return true;
        }
        Log.w(LOG_TAG, "wrong mccmnc len = " + mccMnc.length());
        return false;
    }

    private void updateFakeEccEmergencyList(String mccMnc, String eccNumList) {
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            SystemPropertiesEx.set("gsm.hw.cust.ecclist" + this.mPhoneId, eccNumList);
        } else {
            SystemPropertiesEx.set("gsm.hw.cust.ecclist", eccNumList);
        }
        String[] eccNums = eccNumList.split(ECC_NUMS_SEPARATOR);
        List<EmergencyNumber> fakeEccList = new ArrayList<>();
        if (isMccMncValid(mccMnc)) {
            for (String eccNum : eccNums) {
                fakeEccList.add(EmergencyNumberEx.getEmergencyNumber(eccNum, MccTableEx.countryCodeForMcc(mccMnc.substring(0, 3)), BuildConfig.FLAVOR, 0, new ArrayList(), 16, 2));
            }
            PhoneExt currentPhone = PhoneFactoryExt.getPhone(this.mPhoneId);
            if (currentPhone != null) {
                currentPhone.updateFakeEccEmergencyNumberListAndNotify(fakeEccList);
            }
        }
    }

    public void checkAgpsServers(String currentMccmnc) {
        if (currentMccmnc != null) {
            if (loadAgpsServer(currentMccmnc, HWCFGPOLICYPATH)) {
                Log.d(LOG_TAG, "loadAgpsServer from hwCfgPolicyPath sucess");
            } else if (loadAgpsServer(currentMccmnc, CUSTXMLPATH)) {
                Log.d(LOG_TAG, "loadAgpsServer from cust sucess");
            } else if (loadAgpsServer(currentMccmnc, SYSTEMXMLPATH)) {
                Log.d(LOG_TAG, "loadAgpsServer from system/etc sucess");
            } else {
                Log.d(LOG_TAG, "loadAgpsServer faild,can't find globalAgpsServers-conf.xml");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006c, code lost:
        broadcastAgpsServerConf(r6);
     */
    private boolean loadAgpsServer(String currentMccmnc, String filePath) {
        File confFile;
        if (HWCFGPOLICYPATH.equals(filePath)) {
            try {
                File cfg = HwCfgFilePolicy.getCfgFile("xml/globalAgpsServers-conf.xml", 0);
                if (cfg == null) {
                    return false;
                }
                confFile = cfg;
            } catch (NoClassDefFoundError e) {
                Log.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
                return false;
            }
        } else {
            confFile = new File(filePath, GLOBAL_AGPS_SERVERS_FILE_NAME);
        }
        InputStreamReader confreader = null;
        try {
            InputStreamReader confreader2 = new InputStreamReader(new FileInputStream(confFile), Charset.defaultCharset());
            XmlPullParser confparser = Xml.newPullParser();
            if (confparser != null) {
                confparser.setInput(confreader2);
                XmlUtilsEx.beginDocument(confparser, APGS_SERVERS_DOCUMENT);
                while (true) {
                    XmlUtilsEx.nextElement(confparser);
                    ContentValues row = getAgpsServerRow(confparser);
                    if (row == null) {
                        break;
                    } else if (currentMccmnc.equals(row.getAsString(MCCMNC))) {
                        break;
                    } else if (currentMccmnc.substring(0, 3).equals(row.getAsString(MCCMNC))) {
                        break;
                    }
                }
            }
            try {
                confreader2.close();
                Log.d(LOG_TAG, "AgpsServer file is successfully load from filePath.");
                return true;
            } catch (IOException e2) {
                return false;
            }
        } catch (FileNotFoundException e3) {
            Log.e(LOG_TAG, "File not found.");
            if (0 != 0) {
                try {
                    confreader.close();
                } catch (IOException e4) {
                    return false;
                }
            }
            return false;
        } catch (Exception e5) {
            Log.e(LOG_TAG, "Exception while parsing file.");
            if (0 != 0) {
                try {
                    confreader.close();
                } catch (IOException e6) {
                    return false;
                }
            }
            return false;
        } catch (Throwable e7) {
            if (0 != 0) {
                try {
                    confreader.close();
                } catch (IOException e8) {
                    return false;
                }
            }
            throw e7;
        }
    }

    private ContentValues getAgpsServerRow(XmlPullParser parser) {
        if (!"agpsServer".equals(parser.getName())) {
            return null;
        }
        ContentValues map = new ContentValues();
        map.put("name", parser.getAttributeValue(null, "name"));
        map.put(MCCMNC, parser.getAttributeValue(null, MCCMNC));
        map.put(SUPL_PORT, parser.getAttributeValue(null, SUPL_PORT));
        map.put(SUPL_URL, parser.getAttributeValue(null, SUPL_URL));
        return map;
    }

    private void broadcastAgpsServerConf(ContentValues row) {
        if (row != null) {
            Log.d(LOG_TAG, "broadcast HwTelephonyIntentsInner.ACTION_AGPS_SERVERS");
            Intent intent = new Intent("android.intent.action.ACTION_AGPS_SERVERS");
            intent.addFlags(536870912);
            intent.putExtra(SUPL_URL, row.getAsString(SUPL_URL));
            intent.putExtra(SUPL_PORT, row.getAsString(SUPL_PORT));
            ActivityManagerNativeEx.broadcastStickyIntent(intent, (String) null, 0);
        }
    }

    private void checkMultiSimNumMatch() {
        int[] matchArray = {SystemPropertiesEx.getInt("gsm.hw.matchnum0", -1), SystemPropertiesEx.getInt("gsm.hw.matchnum.short0", -1), SystemPropertiesEx.getInt("gsm.hw.matchnum1", -1), SystemPropertiesEx.getInt("gsm.hw.matchnum.short1", -1)};
        Arrays.sort(matchArray);
        int numMatch = matchArray[3];
        int numMatchShort = numMatch;
        for (int i = 2; i >= 0; i--) {
            if (matchArray[i] < numMatch && matchArray[i] > 0) {
                numMatchShort = matchArray[i];
            }
        }
        if (numMatch >= 0) {
            SystemPropertiesEx.set("gsm.hw.matchnum", Integer.toString(numMatch));
        }
        if (numMatchShort >= 0) {
            SystemPropertiesEx.set("gsm.hw.matchnum.short", Integer.toString(numMatchShort));
        }
    }

    public void queryRoamingNumberMatchRuleByNetwork(String mccmnc, Context context) {
        SystemPropertiesEx.set("gsm.hw.matchnum.roaming", Integer.toString(-1));
        SystemPropertiesEx.set("gsm.hw.matchnum.short.roaming", Integer.toString(-1));
        if (mccmnc != null && context != null) {
            try {
                Cursor cursor = context.getContentResolver().query(HwTelephony.NumMatchs.CONTENT_URI, new String[]{"_id", "name", "numeric", "num_match", "num_match_short"}, "numeric= ?", new String[]{mccmnc}, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
                if (cursor == null) {
                    Log.e(LOG_TAG, "queryNumberMatchRuleByNetwork: No matched number match params in db.");
                    return;
                }
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        int numMatch = cursor.getInt(3);
                        int numMatchShort = cursor.getInt(4);
                        if (numMatchShort == 0) {
                            numMatchShort = numMatch;
                        }
                        SystemPropertiesEx.set("gsm.hw.matchnum.roaming", Integer.toString(numMatch));
                        SystemPropertiesEx.set("gsm.hw.matchnum.short.roaming", Integer.toString(numMatchShort));
                        Log.d(LOG_TAG, "queryNumberMatchRuleByNetwork: after setprop numMatch = " + SystemPropertiesEx.getInt("gsm.hw.matchnum.roaming", -1) + ", numMatchShort = " + SystemPropertiesEx.getInt("gsm.hw.matchnum.short.roaming", -1));
                        cursor.moveToNext();
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, "queryNumberMatchRuleByNetwork: global version cause IllegalArgumentException!");
                } catch (Exception e2) {
                    Log.e(LOG_TAG, "queryNumberMatchRuleByNetwork: global version cause exception!");
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
                cursor.close();
            } catch (Exception e3) {
                Log.e(LOG_TAG, "queryNumberMatchRuleByNetwork: unable to open database file.");
            }
        }
    }

    public void checkValidityOfRoamingNumberMatchRule() {
        int numMatch = SystemPropertiesEx.getInt("gsm.hw.matchnum.roaming", -1);
        int numMatchShort = SystemPropertiesEx.getInt("gsm.hw.matchnum.short.roaming", -1);
        Log.d(LOG_TAG, "checkValidityOfRoamingNumberMatchRule: numMatch = " + numMatch + ", numMatchShort = " + numMatchShort);
        if (numMatch <= 0 || numMatchShort <= 0) {
            SystemPropertiesEx.set("gsm.hw.matchnum.roaming", Integer.toString(7));
            SystemPropertiesEx.set("gsm.hw.matchnum.short.roaming", Integer.toString(7));
            Log.d(LOG_TAG, "checkValidityOfRoamingNumberMatchRule: after validity check numMatch = 7, numMatchShort = 7");
        }
    }
}
