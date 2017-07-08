package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.HwTelephony.NumMatchs;
import android.provider.HwTelephony.VirtualNets;
import android.provider.SettingsEx.Systemex;
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
    private static final String HWCFGPOLICYPATH = "hwCfgPolicyPath";
    private static final boolean IS_PRE_POST_PAY = false;
    private static final boolean IS_SUPPORT_LONG_VMNUM = false;
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
    private static boolean waitingForSetupData;
    private int mPhoneId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.GlobalParamsAdaptor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.GlobalParamsAdaptor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GlobalParamsAdaptor.<clinit>():void");
    }

    public GlobalParamsAdaptor(int phoneId) {
        Rlog.d(LOG_TAG, "contructor phoneId = " + phoneId);
        this.mPhoneId = phoneId;
    }

    private boolean arrayContains(String[] array, String value) {
        for (int i = 0; i < array.length; i += NAME_INDEX) {
            if (array[i].equalsIgnoreCase(value)) {
                return true;
            }
        }
        return IS_SUPPORT_LONG_VMNUM;
    }

    public void checkPrePostPay(String currentMccmnc, String currentImsi, Context context) {
        if (currentMccmnc != null && currentImsi != null) {
            String old_imsi_string = null;
            try {
                String prepay_postpay_mccmncs_strings = Systemex.getString(context.getContentResolver(), "prepay_postpay_mccmncs");
                try {
                    old_imsi_string = Systemex.getString(context.getContentResolver(), "old_imsi");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Could not load default locales", e);
                }
                if (prepay_postpay_mccmncs_strings != null) {
                    boolean isContainer = arrayContains(prepay_postpay_mccmncs_strings.split(","), currentMccmnc);
                    boolean isEqual;
                    if (old_imsi_string != null) {
                        isEqual = currentImsi.equals(old_imsi_string.trim());
                    } else {
                        isEqual = IS_SUPPORT_LONG_VMNUM;
                    }
                    Systemex.putString(context.getContentResolver(), "old_imsi", currentImsi);
                    if (isContainer && !r2) {
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
        int i;
        int i2 = NAME_INDEX;
        StringBuilder append = new StringBuilder().append("gsm.sms.max.message.size");
        if (this.mPhoneId == 0) {
            i = NAME_INDEX;
        } else {
            i = 0;
        }
        if (SystemProperties.getInt(append.append(i).toString(), 0) == 0) {
            SystemProperties.set("gsm.sms.max.message.size", Integer.toString(SystemProperties.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0)));
            return;
        }
        int i3 = SystemProperties.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0);
        StringBuilder append2 = new StringBuilder().append("gsm.sms.max.message.size");
        if (this.mPhoneId == 0) {
            i = NAME_INDEX;
        } else {
            i = 0;
        }
        if (i3 < SystemProperties.getInt(append2.append(i).toString(), 0)) {
            SystemProperties.set("gsm.sms.max.message.size", Integer.toString(SystemProperties.getInt("gsm.sms.max.message.size" + this.mPhoneId, 0)));
            return;
        }
        String str = "gsm.sms.max.message.size";
        append = new StringBuilder().append("gsm.sms.max.message.size");
        if (this.mPhoneId != 0) {
            i2 = 0;
        }
        SystemProperties.set(str, Integer.toString(SystemProperties.getInt(append.append(i2).toString(), 0)));
    }

    private void checkMultiSmsToMmsTextThreshold() {
        int i;
        int set_threshold;
        int cur = SystemProperties.getInt("gsm.sms.to.mms.textthreshold" + this.mPhoneId, 0);
        StringBuilder append = new StringBuilder().append("gsm.sms.to.mms.textthreshold");
        if (this.mPhoneId == 0) {
            i = NAME_INDEX;
        } else {
            i = 0;
        }
        int ant = SystemProperties.getInt(append.append(i).toString(), 0);
        Log.d(LOG_TAG, "checkMultiSmsToMmsTextThreshold>>mPhoneId=" + this.mPhoneId + ", cur= " + cur + ", ant= " + ant);
        if ((this.mPhoneId == NAME_INDEX && TelephonyManager.getDefault().getSimState(0) == NAME_INDEX) || (this.mPhoneId == 0 && TelephonyManager.getDefault().getSimState(NAME_INDEX) == NAME_INDEX)) {
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
        String custPlmnsString = Systemex.getString(context.getContentResolver(), "hw_cust_7bit_enabled_mcc");
        if (TextUtils.isEmpty(custPlmnsString) || TextUtils.isEmpty(simMccMnc) || simMccMnc.length() < NUM_MATCH_INDEX) {
            return IS_SUPPORT_LONG_VMNUM;
        }
        String[] custPlmns = custPlmnsString.split(";");
        int i = 0;
        while (i < custPlmns.length) {
            if (simMccMnc.substring(0, NUM_MATCH_INDEX).equals(custPlmns[i]) || simMccMnc.equals(custPlmns[i])) {
                return true;
            }
            i += NAME_INDEX;
        }
        return IS_SUPPORT_LONG_VMNUM;
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
                if (this.mPhoneId == NAME_INDEX && TelephonyManager.getDefault().getSimState(0) == SMS_7BIT_ENABLED_INDEX) {
                    return;
                }
            }
            if (virtualNet.getSms7BitEnabled() >= 0) {
                SystemProperties.set("gsm.sms.7bit.enabled", Integer.toString(virtualNet.getSms7BitEnabled()));
            }
            if (virtualNet.getSmsCodingNational() >= 0) {
                SystemProperties.set("gsm.sms.coding.national", Integer.toString(virtualNet.getSmsCodingNational()));
            }
            Log.d(LOG_TAG, "virtual net: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum", 0) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short", 0) + ", sms7BitEnabled = " + SystemProperties.getBoolean("gsm.sms.7bit.enabled", IS_SUPPORT_LONG_VMNUM) + ", smsCodingNational = " + SystemProperties.getInt("gsm.sms.coding.national", 0) + ", max_message_size = " + SystemProperties.getInt("gsm.sms.max.message.size", 0) + ", sms_to_mms_textthreshold = " + SystemProperties.getInt("gsm.sms.to.mms.textthreshold", 0));
            temp = new int[NAME_INDEX];
            if (SystemProperties.getInt("ro.config.smsCoding_National", 0) != 0) {
                temp[0] = SystemProperties.getInt("ro.config.smsCoding_National", 0);
                GsmAlphabet.setEnabledSingleShiftTables(temp);
            } else if (SystemProperties.getInt("gsm.sms.coding.national", 0) != 0) {
                temp[0] = SystemProperties.getInt("gsm.sms.coding.national", 0);
                GsmAlphabet.setEnabledSingleShiftTables(temp);
            }
            if (!TelephonyManager.getDefault().isMultiSimEnabled() || this.mPhoneId != NAME_INDEX || TelephonyManager.getDefault().getSimState(0) == NAME_INDEX) {
                Intent intentSetVirtualParamDone = new Intent(ACTION_SET_GLOBAL_AUTO_PARAM_DONE);
                intentSetVirtualParamDone.putExtra("mccMnc", currentMccmnc);
                context.sendStickyBroadcast(intentSetVirtualParamDone);
                return;
            }
            return;
        }
        String where = "numeric=\"" + currentMccmnc + "\"";
        try {
            Cursor cursor = context.getContentResolver().query(NumMatchs.CONTENT_URI, new String[]{"_id", SERVER_NAME, VirtualNets.NUMERIC, VirtualNets.NUM_MATCH, VirtualNets.NUM_MATCH_SHORT, VirtualNets.SMS_7BIT_ENABLED, VirtualNets.SMS_CODING_NATIONAL, NumMatchs.IS_VMN_SHORT_CODE, VirtualNets.SMS_MAX_MESSAGE_SIZE, VirtualNets.SMS_To_MMS_TEXTTHRESHOLD}, where, null, NumMatchs.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                Log.e(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: No matched auto match params in db.");
                return;
            }
            cursor.moveToFirst();
            if (cursor.isAfterLast() && is7bitEnabledInCust) {
                if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
                    Log.e(LOG_TAG, "single card mode,enabled card [" + currentMccmnc + "] 7bit switcher");
                    SystemProperties.set("gsm.sms.7bit.enabled", Integer.toString(NAME_INDEX));
                } else if (!(this.mPhoneId == NAME_INDEX && TelephonyManager.getDefault().getSimState(0) == SMS_7BIT_ENABLED_INDEX)) {
                    Log.e(LOG_TAG, "dual card mode,enabled card [" + currentMccmnc + "] 7bit switcher");
                    SystemProperties.set("gsm.sms.7bit.enabled", Integer.toString(NAME_INDEX));
                }
            }
            while (!cursor.isAfterLast()) {
                int numMatch = cursor.getInt(NUM_MATCH_INDEX);
                int numMatchShort = cursor.getInt(NUM_MATCH_SHORT_INDEX);
                int sms7BitEnabled = cursor.getInt(SMS_7BIT_ENABLED_INDEX);
                int smsCodingNational = cursor.getInt(SMS_CODING_NATIONAL_INDEX);
                int is_vmn_short_code = cursor.getInt(MIN_MATCH);
                if (numMatchShort == 0) {
                    numMatchShort = numMatch;
                }
                int maxMessageSize = cursor.getInt(cursor.getColumnIndex(VirtualNets.SMS_MAX_MESSAGE_SIZE));
                int smsToMmsTextThreshold = cursor.getInt(cursor.getColumnIndex(VirtualNets.SMS_To_MMS_TEXTTHRESHOLD));
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
                    if (this.mPhoneId == NAME_INDEX && TelephonyManager.getDefault().getSimState(0) == SMS_7BIT_ENABLED_INDEX) {
                        return;
                    }
                }
                try {
                    SystemProperties.set("gsm.sms.7bit.enabled", Integer.toString(sms7BitEnabled));
                    SystemProperties.set("gsm.sms.coding.national", Integer.toString(smsCodingNational));
                    SystemProperties.set("gsm.hw.matchnum.vmn_shortcode", Integer.toString(is_vmn_short_code));
                    Log.d(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum", 0) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short", 0) + ", sms7BitEnabled = " + SystemProperties.getBoolean("gsm.sms.7bit.enabled", IS_SUPPORT_LONG_VMNUM) + ", smsCodingNational = " + SystemProperties.getInt("gsm.sms.coding.national", 0) + ", max_message_size = " + SystemProperties.getInt("gsm.sms.max.message.size", 0) + ", sms_to_mms_textthreshold = " + SystemProperties.getInt("gsm.sms.to.mms.textthreshold", 0));
                    cursor.moveToNext();
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "SIMRecords:checkGlobalAutoMatchParam: global version cause exception!" + ex.toString());
                } finally {
                    cursor.close();
                }
            }
            cursor.close();
            temp = new int[NAME_INDEX];
            if (SystemProperties.getInt("ro.config.smsCoding_National", 0) != 0) {
                temp[0] = SystemProperties.getInt("ro.config.smsCoding_National", 0);
                GsmAlphabet.setEnabledSingleShiftTables(temp);
            } else if (SystemProperties.getInt("gsm.sms.coding.national", 0) != 0) {
                temp[0] = SystemProperties.getInt("gsm.sms.coding.national", 0);
                GsmAlphabet.setEnabledSingleShiftTables(temp);
            }
            if (!TelephonyManager.getDefault().isMultiSimEnabled() || this.mPhoneId != NAME_INDEX || TelephonyManager.getDefault().getSimState(0) == NAME_INDEX) {
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
            try {
                String custEccNumsStr = Systemex.getString(context.getContentResolver(), "global_cust_ecc_nums");
                if (!(custEccNumsStr == null || custEccNumsStr.equals(""))) {
                    String[] custEccNumsItems = custEccNumsStr.split(";");
                    int i = 0;
                    while (i < custEccNumsItems.length) {
                        String[] custNumItem = custEccNumsItems[i].split(":");
                        if (NUMERIC_INDEX == custNumItem.length && (custNumItem[0].equalsIgnoreCase(currentMccmnc) || custNumItem[0].equalsIgnoreCase(currentMccmnc.substring(0, NUM_MATCH_INDEX)))) {
                            try {
                                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                                    SystemProperties.set("gsm.hw.cust.ecclist" + this.mPhoneId, custNumItem[NAME_INDEX]);
                                } else {
                                    SystemProperties.set("gsm.hw.cust.ecclist", custNumItem[NAME_INDEX]);
                                }
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "Failed to save ril.ecclist to system property", e);
                            }
                        } else {
                            i += NAME_INDEX;
                        }
                    }
                }
            } catch (Exception e2) {
                Log.e(LOG_TAG, "Could not load default locales", e2);
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

    private boolean loadAgpsServer(String currentMccmnc, String filePath) {
        Exception e;
        Throwable th;
        File confFile = new File("/data/cust", "xml/globalAgpsServers-conf.xml");
        if (HWCFGPOLICYPATH.equals(filePath)) {
            try {
                File cfg = HwCfgFilePolicy.getCfgFile("xml/globalAgpsServers-conf.xml", 0);
                if (cfg == null) {
                    return IS_SUPPORT_LONG_VMNUM;
                }
                confFile = cfg;
            } catch (NoClassDefFoundError e2) {
                Log.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
                return IS_SUPPORT_LONG_VMNUM;
            }
        }
        confFile = new File(filePath, globalAgpsServersFileName);
        InputStreamReader inputStreamReader = null;
        try {
            InputStreamReader confreader = new InputStreamReader(new FileInputStream(confFile), Charset.defaultCharset());
            try {
                XmlPullParser confparser = Xml.newPullParser();
                if (confparser != null) {
                    ContentValues row;
                    confparser.setInput(confreader);
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
                    } while (!currentMccmnc.substring(0, NUM_MATCH_INDEX).equals(row.getAsString(MCCMNC)));
                    broadcastAgpsServerConf(row);
                }
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e3) {
                        return IS_SUPPORT_LONG_VMNUM;
                    }
                }
                Log.d(LOG_TAG, "AgpsServer file is successfully load from filePath:" + filePath);
                return true;
            } catch (FileNotFoundException e4) {
                inputStreamReader = confreader;
                Log.e(LOG_TAG, "File not found: '" + confFile.getAbsolutePath() + "'");
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e5) {
                        return IS_SUPPORT_LONG_VMNUM;
                    }
                }
                return IS_SUPPORT_LONG_VMNUM;
            } catch (Exception e6) {
                e = e6;
                inputStreamReader = confreader;
                try {
                    Log.e(LOG_TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e);
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e7) {
                            return IS_SUPPORT_LONG_VMNUM;
                        }
                    }
                    return IS_SUPPORT_LONG_VMNUM;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e8) {
                            return IS_SUPPORT_LONG_VMNUM;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStreamReader = confreader;
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            Log.e(LOG_TAG, "File not found: '" + confFile.getAbsolutePath() + "'");
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return IS_SUPPORT_LONG_VMNUM;
        } catch (Exception e10) {
            e = e10;
            Log.e(LOG_TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e);
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return IS_SUPPORT_LONG_VMNUM;
        }
    }

    private ContentValues getAgpsServerRow(XmlPullParser parser) {
        if (!"agpsServer".equals(parser.getName())) {
            return null;
        }
        ContentValues map = new ContentValues();
        map.put(SERVER_NAME, parser.getAttributeValue(null, SERVER_NAME));
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
        return IS_SUPPORT_LONG_VMNUM;
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
        int[] matchArray = new int[NUM_MATCH_SHORT_INDEX];
        matchArray[0] = SystemProperties.getInt("gsm.hw.matchnum0", -1);
        matchArray[NAME_INDEX] = SystemProperties.getInt("gsm.hw.matchnum.short0", -1);
        matchArray[NUMERIC_INDEX] = SystemProperties.getInt("gsm.hw.matchnum1", -1);
        matchArray[NUM_MATCH_INDEX] = SystemProperties.getInt("gsm.hw.matchnum.short1", -1);
        Arrays.sort(matchArray);
        int numMatch = matchArray[NUM_MATCH_INDEX];
        int numMatchShort = numMatch;
        int i = NUMERIC_INDEX;
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
                custLongVMNumStr = Systemex.getString(context.getContentResolver(), "hw_cust_long_vmNum");
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Failed to load vmNum from SettingsEx", e);
            }
            if (!TextUtils.isEmpty(custLongVMNumStr)) {
                String[] custLongVMNumsItems = custLongVMNumStr.split(";");
                int i = 0;
                while (i < custLongVMNumsItems.length) {
                    String[] custNumItem = custLongVMNumsItems[i].split(":");
                    if (NUMERIC_INDEX == custNumItem.length && custNumItem[0].equalsIgnoreCase(currentMccmnc)) {
                        try {
                            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                                SystemProperties.set("gsm.hw.cust.longvmnum" + this.mPhoneId, custNumItem[NAME_INDEX]);
                            } else {
                                SystemProperties.set("gsm.hw.cust.longvmnum", custNumItem[NAME_INDEX]);
                            }
                        } catch (Exception e2) {
                            Rlog.e(LOG_TAG, "Failed to save long vmNum to system property", e2);
                        }
                    } else {
                        i += NAME_INDEX;
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
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = NumMatchs.CONTENT_URI;
            String[] strArr = new String[SMS_7BIT_ENABLED_INDEX];
            strArr[0] = "_id";
            strArr[NAME_INDEX] = SERVER_NAME;
            strArr[NUMERIC_INDEX] = VirtualNets.NUMERIC;
            strArr[NUM_MATCH_INDEX] = VirtualNets.NUM_MATCH;
            strArr[NUM_MATCH_SHORT_INDEX] = VirtualNets.NUM_MATCH_SHORT;
            Cursor cursor = contentResolver.query(uri, strArr, where, null, NumMatchs.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                Log.e(LOG_TAG, "queryNumberMatchRuleByNetwork: No matched number match params in db.");
                return;
            }
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int numMatch = cursor.getInt(NUM_MATCH_INDEX);
                    int numMatchShort = cursor.getInt(NUM_MATCH_SHORT_INDEX);
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
            SystemProperties.set("gsm.hw.matchnum.roaming", Integer.toString(MIN_MATCH));
            SystemProperties.set("gsm.hw.matchnum.short.roaming", Integer.toString(MIN_MATCH));
            Log.d(LOG_TAG, "checkValidityOfRoamingNumberMatchRule: after validity check numMatch = " + MIN_MATCH + ", numMatchShort = " + MIN_MATCH);
        }
    }
}
