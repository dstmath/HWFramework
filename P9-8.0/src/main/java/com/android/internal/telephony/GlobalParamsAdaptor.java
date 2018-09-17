package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemProperties;
import android.provider.HwTelephony.NumMatchs;
import android.provider.Settings.System;
import android.provider.Telephony.GlobalMatchs;
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
    private static final int IS_VMN_SHORT_CODE_INDEX = 7;
    static final String LOG_TAG = "GlobalParamsAdaptor";
    private static final String MCCMNC = "mccmnc";
    private static final int MIN_MATCH = 7;
    private static final int NAME_INDEX = 1;
    private static final int NUMERIC_INDEX = 2;
    private static final int NUM_MATCH_INDEX = 3;
    private static final int NUM_MATCH_SHORT_INDEX = 4;
    private static final String SERVER_NAME = "name";
    private static final int SMS_7BIT_ENABLED_INDEX = 5;
    private static final int SMS_CODING_NATIONAL_INDEX = 6;
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
        if (currentMccmnc != null && currentImsi != null) {
            String old_imsi_string = null;
            try {
                String prepay_postpay_mccmncs_strings = System.getString(context.getContentResolver(), "prepay_postpay_mccmncs");
                try {
                    old_imsi_string = System.getString(context.getContentResolver(), "old_imsi");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Could not load default locales", e);
                }
                if (prepay_postpay_mccmncs_strings != null) {
                    boolean isContainer = arrayContains(prepay_postpay_mccmncs_strings.split(","), currentMccmnc);
                    int isEqual;
                    if (old_imsi_string != null) {
                        isEqual = currentImsi.equals(old_imsi_string.trim());
                    } else {
                        isEqual = 0;
                    }
                    System.putString(context.getContentResolver(), "old_imsi", currentImsi);
                    if (isContainer && (isEqual ^ 1) != 0) {
                        if (IS_PRE_POST_PAY) {
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
        int i = 1;
        if (SystemProperties.getInt("gsm.sms.max.message.size" + (this.mPhoneId == 0 ? 1 : 0), 0) == 0) {
            SystemProperties.set("gsm.sms.max.message.size", Integer.toString(SystemProperties.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0)));
            return;
        }
        int i2;
        int i3 = SystemProperties.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0);
        StringBuilder append = new StringBuilder().append("gsm.sms.max.message.size");
        if (this.mPhoneId == 0) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        if (i3 < SystemProperties.getInt(append.append(i2).toString(), 0)) {
            SystemProperties.set("gsm.sms.max.message.size", Integer.toString(SystemProperties.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0)));
            return;
        }
        String str = "gsm.sms.max.message.size";
        StringBuilder append2 = new StringBuilder().append("gsm.sms.max.message.size");
        if (this.mPhoneId != 0) {
            i = 0;
        }
        SystemProperties.set(str, Integer.toString(SystemProperties.getInt(append2.append(i).toString(), 0)));
    }

    private void checkMultiSmsToMmsTextThreshold() {
        int i;
        int set_threshold;
        int cur = SystemProperties.getInt("gsm.sms.to.mms.textthreshold" + this.mPhoneId, 0);
        StringBuilder append = new StringBuilder().append("gsm.sms.to.mms.textthreshold");
        if (this.mPhoneId == 0) {
            i = 1;
        } else {
            i = 0;
        }
        int ant = SystemProperties.getInt(append.append(i).toString(), 0);
        Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>mPhoneId=" + this.mPhoneId + ", cur= " + cur + ", ant= " + ant);
        if ((this.mPhoneId == 1 && TelephonyManager.getDefault().getSimState(0) == 1) || (this.mPhoneId == 0 && TelephonyManager.getDefault().getSimState(1) == 1)) {
            Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>#one card on#mPhoneId=" + this.mPhoneId + ", cur = " + cur);
            set_threshold = cur;
        } else {
            Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>#dual card on#mPhoneId=" + this.mPhoneId + ", cur = " + cur + ", ant = " + ant);
            set_threshold = cur == 0 ? ant : cur == -1 ? (ant == -1 || ant == 0) ? cur : ant : (ant == -1 || ant == 0) ? cur : ant < cur ? ant : cur;
        }
        Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold set_threshold= " + set_threshold);
        SystemProperties.set("gsm.sms.to.mms.textthreshold", Integer.toString(set_threshold));
    }

    private boolean isCustPlmn(Context context, String simMccMnc) {
        String custPlmnsString = System.getString(context.getContentResolver(), "hw_cust_7bit_enabled_mcc");
        if (TextUtils.isEmpty(custPlmnsString) || TextUtils.isEmpty(simMccMnc) || simMccMnc.length() < 3) {
            return false;
        }
        String[] custPlmns = custPlmnsString.split(";");
        int i = 0;
        while (i < custPlmns.length) {
            if (simMccMnc.substring(0, 3).equals(custPlmns[i]) || simMccMnc.equals(custPlmns[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    public void checkGlobalAutoMatchParam(String currentMccmnc, Context context) {
        boolean is7bitEnabledInCust = isCustPlmn(context, currentMccmnc);
        int[] temp;
        if (isVirtualNet()) {
            VirtualNet virtualNet = getCurrentVirtualNet();
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                String str;
                int i;
                if (virtualNet.getNumMatch() >= 0) {
                    SystemProperties.set("gsm.hw.matchnum" + this.mPhoneId, Integer.toString(virtualNet.getNumMatch()));
                }
                if (virtualNet.getNumMatchShort() >= 0) {
                    SystemProperties.set("gsm.hw.matchnum.short" + this.mPhoneId, Integer.toString(virtualNet.getNumMatchShort()));
                }
                checkMultiSimNumMatch();
                if (virtualNet.getMaxMessageSize() >= 0) {
                    str = "gsm.sms.max.message.size" + this.mPhoneId;
                    if (virtualNet.getMaxMessageSize() == 0) {
                        i = 0;
                    } else {
                        i = virtualNet.getMaxMessageSize();
                    }
                    SystemProperties.set(str, Integer.toString(i));
                }
                checkMultiSimMaxMessageSize();
                if (virtualNet.getSmsToMmsTextThreshold() >= 0 || virtualNet.getSmsToMmsTextThreshold() == -1) {
                    str = "gsm.sms.to.mms.textthreshold" + this.mPhoneId;
                    if (virtualNet.getSmsToMmsTextThreshold() == 0) {
                        i = 0;
                    } else {
                        i = virtualNet.getSmsToMmsTextThreshold();
                    }
                    SystemProperties.set(str, Integer.toString(i));
                }
                checkMultiSmsToMmsTextThreshold();
            } else {
                if (virtualNet.getNumMatch() >= 0) {
                    SystemProperties.set("gsm.hw.matchnum", Integer.toString(virtualNet.getNumMatch()));
                }
                if (virtualNet.getNumMatchShort() >= 0) {
                    SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(virtualNet.getNumMatchShort()));
                }
                if (virtualNet.getMaxMessageSize() >= 0) {
                    SystemProperties.set("gsm.sms.max.message.size", Integer.toString(virtualNet.getMaxMessageSize()));
                }
                if (virtualNet.getSmsToMmsTextThreshold() >= 0 || virtualNet.getSmsToMmsTextThreshold() == -1) {
                    SystemProperties.set("gsm.sms.to.mms.textthreshold", Integer.toString(virtualNet.getSmsToMmsTextThreshold()));
                }
            }
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                Log.d(LOG_TAG, "checkGlobalAutoMatchParam mPhoneId = " + this.mPhoneId + " simState = " + TelephonyManager.getDefault().getSimState(this.mPhoneId));
                if (this.mPhoneId == 1 && TelephonyManager.getDefault().getSimState(0) == 5) {
                    return;
                }
            }
            if (virtualNet.getSms7BitEnabled() >= 0) {
                SystemProperties.set("gsm.sms.7bit.enabled", Integer.toString(virtualNet.getSms7BitEnabled()));
            }
            if (virtualNet.getSmsCodingNational() >= 0) {
                SystemProperties.set("gsm.sms.coding.national", Integer.toString(virtualNet.getSmsCodingNational()));
            }
            Log.d(LOG_TAG, "virtual net: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum", 0) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short", 0) + ", sms7BitEnabled = " + SystemProperties.getBoolean("gsm.sms.7bit.enabled", false) + ", smsCodingNational = " + SystemProperties.getInt("gsm.sms.coding.national", 0) + ", max_message_size = " + SystemProperties.getInt("gsm.sms.max.message.size", 0) + ", sms_to_mms_textthreshold = " + SystemProperties.getInt("gsm.sms.to.mms.textthreshold", 0));
            temp = new int[1];
            if (SystemProperties.getInt("ro.config.smsCoding_National", 0) != 0) {
                temp[0] = SystemProperties.getInt("ro.config.smsCoding_National", 0);
                GsmAlphabet.setEnabledSingleShiftTables(temp);
            } else if (SystemProperties.getInt("gsm.sms.coding.national", 0) != 0) {
                temp[0] = SystemProperties.getInt("gsm.sms.coding.national", 0);
                GsmAlphabet.setEnabledSingleShiftTables(temp);
            }
            if (!TelephonyManager.getDefault().isMultiSimEnabled() || this.mPhoneId != 1 || TelephonyManager.getDefault().getSimState(0) == 1) {
                Intent intent = new Intent(ACTION_SET_GLOBAL_AUTO_PARAM_DONE);
                intent.putExtra("mccMnc", currentMccmnc);
                context.sendStickyBroadcast(intent);
                return;
            }
            return;
        }
        String where = "numeric=\"" + currentMccmnc + "\"";
        try {
            Cursor cursor = context.getContentResolver().query(NumMatchs.CONTENT_URI, new String[]{"_id", "name", "numeric", "num_match", "num_match_short", "sms_7bit_enabled", "sms_coding_national", NumMatchs.IS_VMN_SHORT_CODE, "max_message_size", "sms_to_mms_textthreshold"}, where, null, NumMatchs.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                Log.e(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: No matched auto match params in db.");
                return;
            }
            try {
                cursor.moveToFirst();
                if (cursor.isAfterLast() && is7bitEnabledInCust) {
                    if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
                        Log.e(LOG_TAG, "single card mode,enabled card [" + currentMccmnc + "] 7bit switcher");
                        SystemProperties.set("gsm.sms.7bit.enabled", Integer.toString(1));
                    } else if (!(this.mPhoneId == 1 && TelephonyManager.getDefault().getSimState(0) == 5)) {
                        Log.e(LOG_TAG, "dual card mode,enabled card [" + currentMccmnc + "] 7bit switcher");
                        SystemProperties.set("gsm.sms.7bit.enabled", Integer.toString(1));
                    }
                }
                while (!cursor.isAfterLast()) {
                    int numMatch = cursor.getInt(3);
                    int numMatchShort = cursor.getInt(4);
                    int sms7BitEnabled = cursor.getInt(5);
                    int smsCodingNational = cursor.getInt(6);
                    int is_vmn_short_code = cursor.getInt(7);
                    if (numMatchShort == 0) {
                        numMatchShort = numMatch;
                    }
                    int maxMessageSize = cursor.getInt(cursor.getColumnIndex("max_message_size"));
                    int smsToMmsTextThreshold = cursor.getInt(cursor.getColumnIndex("sms_to_mms_textthreshold"));
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        String str2;
                        SystemProperties.set("gsm.hw.matchnum" + this.mPhoneId, Integer.toString(numMatch));
                        SystemProperties.set("gsm.hw.matchnum.short" + this.mPhoneId, Integer.toString(numMatchShort));
                        checkMultiSimNumMatch();
                        if (maxMessageSize >= 0) {
                            str2 = "gsm.sms.max.message.size" + this.mPhoneId;
                            if (maxMessageSize == 0) {
                                maxMessageSize = 0;
                            }
                            SystemProperties.set(str2, Integer.toString(maxMessageSize));
                        }
                        checkMultiSimMaxMessageSize();
                        if (smsToMmsTextThreshold >= 0 || smsToMmsTextThreshold == -1) {
                            str2 = "gsm.sms.to.mms.textthreshold" + this.mPhoneId;
                            if (smsToMmsTextThreshold == 0) {
                                smsToMmsTextThreshold = 0;
                            }
                            SystemProperties.set(str2, Integer.toString(smsToMmsTextThreshold));
                        }
                        checkMultiSmsToMmsTextThreshold();
                    } else {
                        SystemProperties.set("gsm.hw.matchnum", Integer.toString(numMatch));
                        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(numMatchShort));
                        SystemProperties.set("gsm.sms.max.message.size", Integer.toString(maxMessageSize));
                        SystemProperties.set("gsm.sms.to.mms.textthreshold", Integer.toString(smsToMmsTextThreshold));
                    }
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        Log.d(LOG_TAG, "checkGlobalAutoMatchParam mPhoneId = " + this.mPhoneId + " simState = " + TelephonyManager.getDefault().getSimState(this.mPhoneId));
                        if (this.mPhoneId == 1 && TelephonyManager.getDefault().getSimState(0) == 5) {
                            return;
                        }
                    }
                    SystemProperties.set("gsm.sms.7bit.enabled", Integer.toString(sms7BitEnabled));
                    SystemProperties.set("gsm.sms.coding.national", Integer.toString(smsCodingNational));
                    SystemProperties.set("gsm.hw.matchnum.vmn_shortcode", Integer.toString(is_vmn_short_code));
                    Log.d(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum", 0) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short", 0) + ", sms7BitEnabled = " + SystemProperties.getBoolean("gsm.sms.7bit.enabled", false) + ", smsCodingNational = " + SystemProperties.getInt("gsm.sms.coding.national", 0) + ", max_message_size = " + SystemProperties.getInt("gsm.sms.max.message.size", 0) + ", sms_to_mms_textthreshold = " + SystemProperties.getInt("gsm.sms.to.mms.textthreshold", 0));
                    cursor.moveToNext();
                }
                cursor.close();
            } catch (Exception ex) {
                Log.e(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: global version cause exception!" + ex.toString());
            } finally {
                cursor.close();
            }
            temp = new int[1];
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
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "checkGlobalAutoMatchParam: unable to open database file.");
        }
    }

    public void checkGlobalEccNum(String currentMccmnc, Context context) {
        if (currentMccmnc != null) {
            String custEccNumsStrFromGlobalMatch = null;
            String custEccNumsStr = null;
            String where = "numeric=\"" + currentMccmnc + "\"";
            Cursor cursor = context.getContentResolver().query(GlobalMatchs.CONTENT_URI, new String[]{"_id", "name", "numeric", "ecc_fake"}, where, null, NumMatchs.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        custEccNumsStrFromGlobalMatch = cursor.getString(3);
                        cursor.moveToNext();
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "global version cause exception!", e);
                } finally {
                    cursor.close();
                }
            }
            if (custEccNumsStrFromGlobalMatch == null || custEccNumsStrFromGlobalMatch.equals("")) {
                try {
                    custEccNumsStr = System.getString(context.getContentResolver(), "global_cust_ecc_nums");
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
            if (custEccNumsStrFromGlobalMatch != null && (custEccNumsStrFromGlobalMatch.equals("") ^ 1) != 0) {
                try {
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        SystemProperties.set("gsm.hw.cust.ecclist" + this.mPhoneId, custEccNumsStrFromGlobalMatch);
                    } else {
                        SystemProperties.set("gsm.hw.cust.ecclist", custEccNumsStrFromGlobalMatch);
                    }
                } catch (Exception e22) {
                    Log.e(LOG_TAG, "Failed to save ril.ecclist to system property", e22);
                }
            } else if (!(custEccNumsStr == null || (custEccNumsStr.equals("") ^ 1) == 0)) {
                String[] custEccNumsItems = custEccNumsStr.split(";");
                int i = 0;
                while (i < custEccNumsItems.length) {
                    String[] custNumItem = custEccNumsItems[i].split(":");
                    if (2 == custNumItem.length && (custNumItem[0].equalsIgnoreCase(currentMccmnc) || custNumItem[0].equalsIgnoreCase(currentMccmnc.substring(0, 3)))) {
                        try {
                            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                                SystemProperties.set("gsm.hw.cust.ecclist" + this.mPhoneId, custNumItem[1]);
                            } else {
                                SystemProperties.set("gsm.hw.cust.ecclist", custNumItem[1]);
                            }
                        } catch (Exception e222) {
                            Log.e(LOG_TAG, "Failed to save ril.ecclist to system property", e222);
                        }
                    } else {
                        i++;
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

    /* JADX WARNING: Removed duplicated region for block: B:58:0x0109 A:{SYNTHETIC, Splitter: B:58:0x0109} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00ff A:{SYNTHETIC, Splitter: B:50:0x00ff} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00cf A:{SYNTHETIC, Splitter: B:39:0x00cf} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean loadAgpsServer(String currentMccmnc, String filePath) {
        Exception e;
        Throwable th;
        File confFile = new File("/data/cust", "xml/globalAgpsServers-conf.xml");
        if (HWCFGPOLICYPATH.equals(filePath)) {
            try {
                File cfg = HwCfgFilePolicy.getCfgFile("xml/globalAgpsServers-conf.xml", 0);
                if (cfg == null) {
                    return false;
                }
                confFile = cfg;
            } catch (NoClassDefFoundError e2) {
                Log.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
                return false;
            }
        }
        confFile = new File(filePath, globalAgpsServersFileName);
        InputStreamReader confreader = null;
        try {
            InputStreamReader confreader2 = new InputStreamReader(new FileInputStream(confFile), Charset.defaultCharset());
            try {
                XmlPullParser confparser = Xml.newPullParser();
                if (confparser != null) {
                    ContentValues row;
                    confparser.setInput(confreader2);
                    XmlUtils.beginDocument(confparser, APGS_SERVERS_DOCUMENT);
                    do {
                        XmlUtils.nextElement(confparser);
                        row = getAgpsServerRow(confparser);
                        if (row != null) {
                            if (currentMccmnc.equals(row.getAsString(MCCMNC))) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } while (!currentMccmnc.substring(0, 3).equals(row.getAsString(MCCMNC)));
                    broadcastAgpsServerConf(row);
                }
                if (confreader2 != null) {
                    try {
                        confreader2.close();
                    } catch (IOException e3) {
                        return false;
                    }
                }
                Log.d(LOG_TAG, "AgpsServer file is successfully load from filePath:" + filePath);
                return true;
            } catch (FileNotFoundException e4) {
                confreader = confreader2;
                Log.e(LOG_TAG, "File not found: '" + confFile.getAbsolutePath() + "'");
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e5) {
                        return false;
                    }
                }
                return false;
            } catch (Exception e6) {
                e = e6;
                confreader = confreader2;
                try {
                    Log.e(LOG_TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e);
                    if (confreader != null) {
                        try {
                            confreader.close();
                        } catch (IOException e7) {
                            return false;
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (confreader != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                confreader = confreader2;
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e8) {
                        return false;
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            Log.e(LOG_TAG, "File not found: '" + confFile.getAbsolutePath() + "'");
            if (confreader != null) {
            }
            return false;
        } catch (Exception e10) {
            e = e10;
            Log.e(LOG_TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e);
            if (confreader != null) {
            }
            return false;
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
        if (waitingForSetupData && IS_PRE_POST_PAY) {
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
        int[] matchArray = new int[]{SystemProperties.getInt("gsm.hw.matchnum0", -1), SystemProperties.getInt("gsm.hw.matchnum.short0", -1), SystemProperties.getInt("gsm.hw.matchnum1", -1), SystemProperties.getInt("gsm.hw.matchnum.short1", -1)};
        Arrays.sort(matchArray);
        int numMatch = matchArray[3];
        int numMatchShort = numMatch;
        int i = 2;
        while (i >= 0) {
            if (matchArray[i] < numMatch && matchArray[i] > 0) {
                numMatchShort = matchArray[i];
            }
            i--;
        }
        if (numMatch >= 0) {
            SystemProperties.set("gsm.hw.matchnum", Integer.toString(numMatch));
        }
        if (numMatchShort >= 0) {
            SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(numMatchShort));
        }
    }

    public void checkCustLongVMNum(String currentMccmnc, Context context) {
        if (IS_SUPPORT_LONG_VMNUM && currentMccmnc != null) {
            Object custLongVMNumStr = null;
            try {
                custLongVMNumStr = System.getString(context.getContentResolver(), "hw_cust_long_vmNum");
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Failed to load vmNum from SettingsEx", e);
            }
            if (!TextUtils.isEmpty(custLongVMNumStr)) {
                String[] custLongVMNumsItems = custLongVMNumStr.split(";");
                int i = 0;
                while (i < custLongVMNumsItems.length) {
                    String[] custNumItem = custLongVMNumsItems[i].split(":");
                    if (2 == custNumItem.length && custNumItem[0].equalsIgnoreCase(currentMccmnc)) {
                        try {
                            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                                SystemProperties.set("gsm.hw.cust.longvmnum" + this.mPhoneId, custNumItem[1]);
                            } else {
                                SystemProperties.set("gsm.hw.cust.longvmnum", custNumItem[1]);
                            }
                        } catch (Exception e2) {
                            Rlog.e(LOG_TAG, "Failed to save long vmNum to system property", e2);
                        }
                    } else {
                        i++;
                    }
                }
            }
        }
    }

    public void queryRoamingNumberMatchRuleByNetwork(String mccmnc, Context context) {
        SystemProperties.set("gsm.hw.matchnum.roaming", Integer.toString(-1));
        SystemProperties.set("gsm.hw.matchnum.short.roaming", Integer.toString(-1));
        String where = "numeric=\"" + mccmnc + "\"";
        try {
            Cursor cursor = context.getContentResolver().query(NumMatchs.CONTENT_URI, new String[]{"_id", "name", "numeric", "num_match", "num_match_short"}, where, null, NumMatchs.DEFAULT_SORT_ORDER);
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
            } finally {
                cursor.close();
            }
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
}
