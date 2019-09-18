package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemProperties;
import android.provider.HwTelephony;
import android.provider.IHwTelephonyEx;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;

public class GlobalParamsAdaptor {
    public static final String ACTION_SET_GLOBAL_AUTO_PARAM_DONE = "android.intent.action.ACTION_SET_GLOBAL_AUTO_PARAM_DONE";
    private static final String APGS_SERVERS_DOCUMENT = "agpsServers";
    private static final String CUSTXMLPATH = "/data/cust/xml/";
    private static final int ECC_FAKE_INDEX = 3;
    private static final String HWCFGPOLICYPATH = "hwCfgPolicyPath";
    private static final boolean IS_PRE_POST_PAY = SystemProperties.getBoolean("ro.config.hw_is_pre_post_pay", false);
    private static final boolean IS_SUPPORT_LONG_VMNUM = SystemProperties.getBoolean("ro.config.hw_support_long_vmNum", false);
    private static final int IS_VMN_SHORT_CODE_INDEX = 1;
    static final String LOG_TAG = "GlobalParamsAdaptor";
    private static final String MCCMNC = "mccmnc";
    private static final int MIN_MATCH = 7;
    private static final int NUM_MATCH_INDEX = 3;
    private static final int NUM_MATCH_SHORT_INDEX = 4;
    private static final String SERVER_NAME = "name";
    private static final String SUPL_PORT = "supl_port";
    private static final String SUPL_URL = "supl_host";
    private static final String SYSTEMXMLPATH = "/system/etc/";
    private static final String globalAgpsServersFileName = "globalAgpsServers-conf.xml";
    private static boolean waitingForSetupData = false;
    private int mPhoneId;

    public GlobalParamsAdaptor(int phoneId) {
        Rlog.d(LOG_TAG, "contructor phoneId = " + phoneId);
        this.mPhoneId = phoneId;
    }

    private boolean arrayContains(String[] array, String value) {
        for (String equalsIgnoreCase : array) {
            if (equalsIgnoreCase.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public void checkPrePostPay(String currentMccmnc, String currentImsi, Context context) {
        boolean isEqual;
        if (currentMccmnc != null && currentImsi != null) {
            String old_imsi_string = null;
            try {
                String prepay_postpay_mccmncs_strings = Settings.System.getString(context.getContentResolver(), "prepay_postpay_mccmncs");
                try {
                    old_imsi_string = Settings.System.getString(context.getContentResolver(), "old_imsi");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Could not load default locales", e);
                }
                if (prepay_postpay_mccmncs_strings != null) {
                    boolean isContainer = arrayContains(prepay_postpay_mccmncs_strings.split(","), currentMccmnc);
                    if (old_imsi_string != null) {
                        isEqual = currentImsi.equals(old_imsi_string.trim());
                    } else {
                        isEqual = false;
                    }
                    Settings.System.putString(context.getContentResolver(), "old_imsi", currentImsi);
                    if (isContainer && !isEqual) {
                        if (true == IS_PRE_POST_PAY) {
                            setWaitingForSetupData(true);
                        } else {
                            tryToActionPrePostPay();
                        }
                    }
                }
            } catch (Exception e2) {
                Log.e(LOG_TAG, "Could not load default locales", e2);
            }
        }
    }

    private boolean isVirtualNet() {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            return VirtualNet.isVirtualNet(this.mPhoneId);
        }
        return VirtualNet.isVirtualNet();
    }

    private VirtualNet getCurrentVirtualNet() {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            return VirtualNet.getCurrentVirtualNet(this.mPhoneId);
        }
        return VirtualNet.getCurrentVirtualNet();
    }

    private void checkMultiSimMaxMessageSize() {
        StringBuilder sb = new StringBuilder();
        sb.append("gsm.sms.max.message.size");
        int i = 1;
        sb.append(this.mPhoneId == 0 ? 1 : 0);
        if (SystemProperties.getInt(sb.toString(), 0) == 0) {
            SystemProperties.set("gsm.sms.max.message.size", Integer.toString(SystemProperties.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0)));
            return;
        }
        int i2 = SystemProperties.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("gsm.sms.max.message.size");
        sb2.append(this.mPhoneId == 0 ? 1 : 0);
        if (i2 < SystemProperties.getInt(sb2.toString(), 0)) {
            SystemProperties.set("gsm.sms.max.message.size", Integer.toString(SystemProperties.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0)));
            return;
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append("gsm.sms.max.message.size");
        if (this.mPhoneId != 0) {
            i = 0;
        }
        sb3.append(i);
        SystemProperties.set("gsm.sms.max.message.size", Integer.toString(SystemProperties.getInt(sb3.toString(), 0)));
    }

    private void checkMultiSmsToMmsTextThreshold() {
        int set_threshold;
        int cur = SystemProperties.getInt("gsm.sms.to.mms.textthreshold" + this.mPhoneId, 0);
        StringBuilder sb = new StringBuilder();
        sb.append("gsm.sms.to.mms.textthreshold");
        sb.append(this.mPhoneId == 0 ? 1 : 0);
        int ant = SystemProperties.getInt(sb.toString(), 0);
        Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>mPhoneId=" + this.mPhoneId + ", cur= " + cur + ", ant= " + ant);
        if ((this.mPhoneId == 1 && TelephonyManager.getDefault().getSimState(0) == 1) || (this.mPhoneId == 0 && TelephonyManager.getDefault().getSimState(1) == 1)) {
            Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>#one card on#mPhoneId=" + this.mPhoneId + ", cur = " + cur);
            set_threshold = cur;
        } else {
            Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>#dual card on#mPhoneId=" + this.mPhoneId + ", cur = " + cur + ", ant = " + ant);
            if (cur == 0) {
                set_threshold = ant;
            } else if (cur == -1) {
                if (ant == -1 || ant == 0) {
                    set_threshold = cur;
                } else {
                    set_threshold = ant;
                }
            } else if (ant == -1 || ant == 0) {
                set_threshold = cur;
            } else {
                set_threshold = ant < cur ? ant : cur;
            }
        }
        Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold set_threshold= " + set_threshold);
        SystemProperties.set("gsm.sms.to.mms.textthreshold", Integer.toString(set_threshold));
    }

    private boolean isCustPlmn(Context context, String simMccMnc) {
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
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            Integer num_match_slot = (Integer) HwCfgFilePolicy.getValue("num_match", this.mPhoneId, Integer.class);
            if (num_match_slot != null) {
                SystemProperties.set("gsm.hw.matchnum" + this.mPhoneId, num_match_slot.toString());
                SystemProperties.set("gsm.hw.matchnum.short" + this.mPhoneId, num_match_slot.toString());
            }
            Integer num_match_short_slot = (Integer) HwCfgFilePolicy.getValue("num_match_short", this.mPhoneId, Integer.class);
            if (num_match_short_slot != null) {
                SystemProperties.set("gsm.hw.matchnum.short" + this.mPhoneId, num_match_short_slot.toString());
            }
            checkMultiSimNumMatch();
            Integer max_message_size_slot = (Integer) HwCfgFilePolicy.getValue("max_message_size", this.mPhoneId, Integer.class);
            if (max_message_size_slot != null) {
                SystemProperties.set("gsm.sms.max.message.size" + this.mPhoneId, max_message_size_slot.toString());
            }
            checkMultiSimMaxMessageSize();
            Integer sms_to_mms_textthreshold_slot = (Integer) HwCfgFilePolicy.getValue("sms_to_mms_textthreshold", this.mPhoneId, Integer.class);
            if (sms_to_mms_textthreshold_slot != null && sms_to_mms_textthreshold_slot.intValue() >= -1) {
                SystemProperties.set("gsm.sms.to.mms.textthreshold" + this.mPhoneId, sms_to_mms_textthreshold_slot.toString());
            }
            checkMultiSmsToMmsTextThreshold();
        } else {
            Integer num_match = (Integer) HwCfgFilePolicy.getValue("num_match", Integer.class);
            if (num_match != null) {
                SystemProperties.set("gsm.hw.matchnum", num_match.toString());
                SystemProperties.set("gsm.hw.matchnum.short", num_match.toString());
            }
            Integer num_match_short = (Integer) HwCfgFilePolicy.getValue("num_match_short", Integer.class);
            if (num_match_short != null) {
                SystemProperties.set("gsm.hw.matchnum.short", num_match_short.toString());
            }
            Integer max_message_size = (Integer) HwCfgFilePolicy.getValue("max_message_size", Integer.class);
            if (max_message_size != null) {
                SystemProperties.set("gsm.sms.max.message.size", max_message_size.toString());
            }
            Integer sms_to_mms_textthreshold = (Integer) HwCfgFilePolicy.getValue("sms_to_mms_textthreshold", Integer.class);
            if (sms_to_mms_textthreshold != null && sms_to_mms_textthreshold.intValue() >= -1) {
                SystemProperties.set("gsm.sms.to.mms.textthreshold", sms_to_mms_textthreshold.toString());
            }
        }
        Integer sms_7bit_enabled = (Integer) HwCfgFilePolicy.getValue("sms_7bit_enabled", Integer.class);
        if (sms_7bit_enabled != null) {
            SystemProperties.set("gsm.sms.7bit.enabled", sms_7bit_enabled.toString());
        }
        Integer sms_coding_national = (Integer) HwCfgFilePolicy.getValue("sms_coding_national", Integer.class);
        if (sms_coding_national != null) {
            SystemProperties.set("gsm.sms.coding.national", sms_coding_national.toString());
        }
    }

    public void checkGlobalAutoMatchParam(String currentMccmnc, Context context) {
        globalAutoMatch();
        try {
            Cursor cursor = context.getContentResolver().query(HwTelephony.NumMatchs.CONTENT_URI, new String[]{"_id", HwTelephony.NumMatchs.IS_VMN_SHORT_CODE}, "numeric= ?", new String[]{currentMccmnc}, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                Log.e(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: No matched auto match params in db.");
                return;
            }
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int is_vmn_short_code = cursor.getInt(1);
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        Log.d(LOG_TAG, "checkGlobalAutoMatchParam mPhoneId = " + this.mPhoneId + " simState = " + TelephonyManager.getDefault().getSimState(this.mPhoneId));
                        if (this.mPhoneId == 1 && TelephonyManager.getDefault().getSimState(0) == 5) {
                            Log.d(LOG_TAG, "card 2 ready, card 1 ready, just return, don't go into send broadcast below");
                            cursor.close();
                            return;
                        }
                    }
                    SystemProperties.set("gsm.hw.matchnum.vmn_shortcode", Integer.toString(is_vmn_short_code));
                    Log.d(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum", 0) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short", 0) + ", sms7BitEnabled = " + SystemProperties.getBoolean("gsm.sms.7bit.enabled", false) + ", smsCodingNational = " + SystemProperties.getInt("gsm.sms.coding.national", 0) + ", max_message_size = " + SystemProperties.getInt("gsm.sms.max.message.size", 0) + ", sms_to_mms_textthreshold = " + SystemProperties.getInt("gsm.sms.to.mms.textthreshold", 0));
                    cursor.moveToNext();
                }
            } catch (Exception ex) {
                Log.e(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: global version cause exception!" + ex.toString());
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
            cursor.close();
            int[] temp = new int[1];
            if (SystemProperties.getInt("ro.config.smsCoding_National", 0) != 0) {
                temp[0] = SystemProperties.getInt("ro.config.smsCoding_National", 0);
                GsmAlphabet.setEnabledSingleShiftTables(temp);
            } else if (SystemProperties.getInt("gsm.sms.coding.national", 0) != 0) {
                temp[0] = SystemProperties.getInt("gsm.sms.coding.national", 0);
                GsmAlphabet.setEnabledSingleShiftTables(temp);
            }
            if (!TelephonyManager.getDefault().isMultiSimEnabled() || this.mPhoneId != 1 || TelephonyManager.getDefault().getSimState(0) == 1) {
                Intent intentSetGlobalParamDone = new Intent(ACTION_SET_GLOBAL_AUTO_PARAM_DONE);
                intentSetGlobalParamDone.putExtra("mccMnc", currentMccmnc);
                context.sendStickyBroadcast(intentSetGlobalParamDone);
                return;
            }
            Log.d(LOG_TAG, " card 2 ready, card 1 inserted, maybe not ready, don't send broadcast");
        } catch (Exception e) {
            Log.e(LOG_TAG, "checkGlobalAutoMatchParam: unable to open database file.");
        }
    }

    public void checkGlobalEccNum(String currentMccmnc, Context context) {
        if (currentMccmnc != null) {
            String custEccNumsStrFromGlobalMatch = null;
            String custEccNumsStr = null;
            Cursor cursor = context.getContentResolver().query(IHwTelephonyEx.GlobalMatchs.CONTENT_URI, new String[]{"_id", "name", "numeric", "ecc_fake"}, "numeric= ?", new String[]{currentMccmnc}, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        custEccNumsStrFromGlobalMatch = cursor.getString(3);
                        cursor.moveToNext();
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "global version cause exception!", e);
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
                cursor.close();
            }
            if (custEccNumsStrFromGlobalMatch == null || custEccNumsStrFromGlobalMatch.equals("")) {
                try {
                    custEccNumsStr = Settings.System.getString(context.getContentResolver(), "global_cust_ecc_nums");
                } catch (Exception e2) {
                    Log.e(LOG_TAG, "Could not load default locales", e2);
                    return;
                }
            }
            try {
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    SystemProperties.set("gsm.hw.cust.ecclist" + this.mPhoneId, null);
                } else {
                    SystemProperties.set("gsm.hw.cust.ecclist", null);
                }
            } catch (IllegalArgumentException e3) {
                Log.e(LOG_TAG, "Failed to save ril.ecclist to system property", e3);
            }
            if (custEccNumsStrFromGlobalMatch != null && !custEccNumsStrFromGlobalMatch.equals("")) {
                try {
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        SystemProperties.set("gsm.hw.cust.ecclist" + this.mPhoneId, custEccNumsStrFromGlobalMatch);
                    } else {
                        SystemProperties.set("gsm.hw.cust.ecclist", custEccNumsStrFromGlobalMatch);
                    }
                } catch (Exception e4) {
                    Log.e(LOG_TAG, "Failed to save ril.ecclist to system property", e4);
                }
            } else if (custEccNumsStr != null && !custEccNumsStr.equals("")) {
                String[] custEccNumsItems = custEccNumsStr.split(";");
                int i = 0;
                while (true) {
                    if (i >= custEccNumsItems.length) {
                        break;
                    }
                    String[] custNumItem = custEccNumsItems[i].split(":");
                    if (2 != custNumItem.length || (!custNumItem[0].equalsIgnoreCase(currentMccmnc) && !custNumItem[0].equalsIgnoreCase(currentMccmnc.substring(0, 3)))) {
                        i++;
                    } else {
                        try {
                            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                                SystemProperties.set("gsm.hw.cust.ecclist" + this.mPhoneId, custNumItem[1]);
                            } else {
                                SystemProperties.set("gsm.hw.cust.ecclist", custNumItem[1]);
                            }
                        } catch (Exception e5) {
                            Log.e(LOG_TAG, "Failed to save ril.ecclist to system property", e5);
                        }
                    }
                }
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

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    private boolean loadAgpsServer(String currentMccmnc, String filePath) {
        File confFile;
        ContentValues row;
        new File("/data/cust", "xml/globalAgpsServers-conf.xml");
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
            confFile = new File(filePath, globalAgpsServersFileName);
        }
        InputStreamReader confreader = null;
        try {
            InputStreamReader confreader2 = new InputStreamReader(new FileInputStream(confFile), Charset.defaultCharset());
            XmlPullParser confparser = Xml.newPullParser();
            if (confparser != null) {
                confparser.setInput(confreader2);
                XmlUtils.beginDocument(confparser, APGS_SERVERS_DOCUMENT);
                while (true) {
                    XmlUtils.nextElement(confparser);
                    row = getAgpsServerRow(confparser);
                    if (row != null) {
                        if (currentMccmnc.equals(row.getAsString(MCCMNC))) {
                            break;
                        } else if (currentMccmnc.substring(0, 3).equals(row.getAsString(MCCMNC))) {
                            break;
                        }
                    }
                }
                broadcastAgpsServerConf(row);
            }
            try {
                confreader2.close();
                Log.d(LOG_TAG, "AgpsServer file is successfully load from filePath:" + filePath);
                return true;
            } catch (IOException e2) {
            }
            return false;
        } catch (FileNotFoundException e3) {
            Log.e(LOG_TAG, "File not found: '" + confFile.getAbsolutePath() + "'");
            if (confreader != null) {
                confreader.close();
            }
            return false;
        } catch (Exception e4) {
            Log.e(LOG_TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e4);
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e5) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (confreader != null) {
                confreader.close();
            }
            throw th;
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
            ActivityManagerNative.broadcastStickyIntent(intent, null, 0);
        }
    }

    public static boolean getPrePostPayPreCondition() {
        Log.d(LOG_TAG, "waitingForSetupData = " + waitingForSetupData);
        Log.d(LOG_TAG, "IS_PRE_POST_PAY = " + IS_PRE_POST_PAY);
        if (true == waitingForSetupData && true == IS_PRE_POST_PAY) {
            return true;
        }
        return false;
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
        ActivityManagerNative.broadcastStickyIntent(intent, null, 0);
    }

    private void checkMultiSimNumMatch() {
        int[] matchArray = {SystemProperties.getInt("gsm.hw.matchnum0", -1), SystemProperties.getInt("gsm.hw.matchnum.short0", -1), SystemProperties.getInt("gsm.hw.matchnum1", -1), SystemProperties.getInt("gsm.hw.matchnum.short1", -1)};
        Arrays.sort(matchArray);
        int numMatch = matchArray[3];
        int numMatchShort = numMatch;
        for (int i = 2; i >= 0; i--) {
            if (matchArray[i] < numMatch && matchArray[i] > 0) {
                numMatchShort = matchArray[i];
            }
        }
        if (numMatch >= 0) {
            SystemProperties.set("gsm.hw.matchnum", Integer.toString(numMatch));
        }
        if (numMatchShort >= 0) {
            SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(numMatchShort));
        }
    }

    public void checkCustLongVMNum(String currentMccmnc, Context context) {
        if (getHwSupportLongVmnum() && currentMccmnc != null && !loadCustLongVmnumFromCard()) {
            String custLongVMNumStr = null;
            try {
                custLongVMNumStr = Settings.System.getString(context.getContentResolver(), "hw_cust_long_vmNum");
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Failed to load vmNum from SettingsEx", e);
            }
            if (!TextUtils.isEmpty(custLongVMNumStr)) {
                String[] custLongVMNumsItems = custLongVMNumStr.split(";");
                int i = 0;
                while (true) {
                    if (i >= custLongVMNumsItems.length) {
                        break;
                    }
                    String[] custNumItem = custLongVMNumsItems[i].split(":");
                    if (2 == custNumItem.length && custNumItem[0].equalsIgnoreCase(currentMccmnc)) {
                        setPropForCustLongVmnum(custNumItem[1]);
                        break;
                    }
                    i++;
                }
            }
        }
    }

    public void queryRoamingNumberMatchRuleByNetwork(String mccmnc, Context context) {
        SystemProperties.set("gsm.hw.matchnum.roaming", Integer.toString(-1));
        SystemProperties.set("gsm.hw.matchnum.short.roaming", Integer.toString(-1));
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
                    SystemProperties.set("gsm.hw.matchnum.roaming", Integer.toString(numMatch));
                    SystemProperties.set("gsm.hw.matchnum.short.roaming", Integer.toString(numMatchShort));
                    Log.d(LOG_TAG, "queryNumberMatchRuleByNetwork: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum.roaming", -1) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short.roaming", -1));
                    cursor.moveToNext();
                }
            } catch (Exception ex) {
                Log.e(LOG_TAG, "queryNumberMatchRuleByNetwork: global version cause exception!" + ex.toString());
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "queryNumberMatchRuleByNetwork: unable to open database file.");
        }
    }

    public void checkValidityOfRoamingNumberMatchRule() {
        int numMatch = SystemProperties.getInt("gsm.hw.matchnum.roaming", -1);
        int numMatchShort = SystemProperties.getInt("gsm.hw.matchnum.short.roaming", -1);
        Log.d(LOG_TAG, "checkValidityOfRoamingNumberMatchRule: numMatch = " + numMatch + ", numMatchShort = " + numMatchShort);
        if (numMatch <= 0 || numMatchShort <= 0) {
            SystemProperties.set("gsm.hw.matchnum.roaming", Integer.toString(7));
            SystemProperties.set("gsm.hw.matchnum.short.roaming", Integer.toString(7));
            Log.d(LOG_TAG, "checkValidityOfRoamingNumberMatchRule: after validity check numMatch = " + 7 + ", numMatchShort = " + 7);
        }
    }

    private boolean getHwSupportLongVmnum() {
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("hw_support_long_vmNum", this.mPhoneId, Boolean.class);
        boolean valueFromProp = IS_SUPPORT_LONG_VMNUM;
        Rlog.d(LOG_TAG, "getHwSupportLongVmnum, card:" + valueFromCard + ", prop:" + valueFromProp + ", mPhoneId: " + this.mPhoneId);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    private boolean loadCustLongVmnumFromCard() {
        String longVmNum = (String) HwCfgFilePolicy.getValue("hw_cust_long_vmNum", this.mPhoneId, String.class);
        if (longVmNum == null) {
            return false;
        }
        Rlog.d(LOG_TAG, "loadCustLongVmnumFromCard: longVmNum's length:" + longVmNum.length() + ", mPhoneId: " + this.mPhoneId);
        setPropForCustLongVmnum(longVmNum);
        return true;
    }

    private void setPropForCustLongVmnum(String longVmnum) {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            SystemProperties.set("gsm.hw.cust.longvmnum" + this.mPhoneId, longVmnum);
            return;
        }
        SystemProperties.set("gsm.hw.cust.longvmnum", longVmnum);
    }
}
