package com.android.internal.telephony;

import android.os.Environment;
import android.util.Xml;
import com.huawei.android.internal.util.XmlUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.PhoneNumberUtilsEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.PhoneExt;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwSmsUsageMonitorEx extends DefaultHwSmsUsageMonitorEx {
    private static final boolean ENABLE_CUSTOM_SHORTCODES = SystemPropertiesEx.getBoolean("ro.config.custom_short_codes", false);
    private static String FILE_FROM_CUST_DIR = "/data/cust/xml/xml/smsShortcodesList-conf.xml";
    private static String FILE_FROM_SYSTEM_ETC_DIR = "/system/etc/xml/smsShortcodesList-conf.xml";
    private static final String PARAM_SHORTCODE_PATH = "etc/smsShortcodesList-conf.xml";
    private static final String SHORT_CODE_CONFIG_FILE = "xml/smsShortcodesList-conf.xml";
    private static final int SMS_CATEGORY_FREE_SHORT_CODE = 1;
    private static final int SMS_CATEGORY_NOT_SHORT_CODE = 0;
    private static final String TAG = "HwSmsUsageMonitorEx";
    private static ArrayList<ShortCodeCfg> mshortCodeList;
    PhoneExt mPhone;
    private ISmsUsageMonitorInner mSmsUsageMonitor;

    public HwSmsUsageMonitorEx(ISmsUsageMonitorInner smsUsageMonitorInner, PhoneExt phoneExt) {
        this.mSmsUsageMonitor = smsUsageMonitorInner;
        loadShortCodeList();
        this.mPhone = phoneExt;
    }

    private static BufferedReader getShortCodesFileReader() {
        File confFile = new File(FILE_FROM_SYSTEM_ETC_DIR);
        File sShortCodeCust = new File(FILE_FROM_CUST_DIR);
        File sShortcodeFile = new File(Environment.getRootDirectory(), PARAM_SHORTCODE_PATH);
        try {
            File cfg = HwCfgFilePolicy.getCfgFile(SHORT_CODE_CONFIG_FILE, 0);
            if (cfg != null) {
                confFile = cfg;
                RlogEx.d(TAG, "load smsShortcodesList-conf.xml from HwCfgFilePolicy folder");
            } else if (sShortCodeCust.exists()) {
                confFile = sShortCodeCust;
                RlogEx.d(TAG, "load smsShortcodesList-conf.xml from cust folder");
            } else {
                confFile = sShortcodeFile;
                RlogEx.d(TAG, "load smsShortcodesList-conf.xml from etc folder");
            }
        } catch (NoClassDefFoundError e) {
            RlogEx.e(TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
        }
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(confFile), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e2) {
            RlogEx.e(TAG, "Can't open etc/smsShortcodesList-conf.xml");
            return null;
        }
    }

    private static void loadShortCodeList() {
        mshortCodeList = new ArrayList<>();
        BufferedReader sShortcodeReader = getShortCodesFileReader();
        if (sShortcodeReader == null) {
            RlogEx.d(TAG, "loadShortCodeList failed!");
            return;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(sShortcodeReader);
            XmlUtilsEx.beginDocument(parser, "shortCodesList");
            while (true) {
                XmlUtilsEx.nextElement(parser);
                if (parser.getName() == null) {
                    try {
                        sShortcodeReader.close();
                        return;
                    } catch (IOException e) {
                        RlogEx.e(TAG, "IOException happen.close failed.");
                        return;
                    }
                } else {
                    ShortCodeCfg shortcodeConfig = new ShortCodeCfg();
                    shortcodeConfig.numeric = parser.getAttributeValue(null, "numeric");
                    shortcodeConfig.shortcodes = parser.getAttributeValue(null, "codes");
                    RlogEx.d(TAG, "getAttributeValue numeric = " + shortcodeConfig.numeric + "getAttributeValue codes = " + shortcodeConfig.shortcodes);
                    mshortCodeList.add(shortcodeConfig);
                }
            }
        } catch (XmlPullParserException e2) {
            RlogEx.d(TAG, "XmlPullParserException in smsShortcodesList parser ");
            sShortcodeReader.close();
        } catch (IOException e3) {
            RlogEx.d(TAG, "IOException in smsShortcodesList parser ");
            sShortcodeReader.close();
        } catch (Throwable th) {
            try {
                sShortcodeReader.close();
            } catch (IOException e4) {
                RlogEx.e(TAG, "IOException happen.close failed.");
            }
            throw th;
        }
    }

    public int checkDestinationHw(String destAddress, String countryIso, String simMccmnc) {
        synchronized (this.mSmsUsageMonitor.getmSettingsObserverHandlerHw()) {
            if (PhoneNumberUtilsEx.isEmergencyNumber(destAddress, countryIso)) {
                RlogEx.d(TAG, "isEmergencyNumber");
                return 0;
            } else if (!this.mSmsUsageMonitor.getmCheckEnabledHw().get()) {
                RlogEx.d(TAG, "check disabled");
                return 0;
            } else if (getShortcodesCust(destAddress)) {
                return 1;
            } else {
                if (!ENABLE_CUSTOM_SHORTCODES || !isSpecialPattern(destAddress, simMccmnc)) {
                    return this.mSmsUsageMonitor.checkDestination(destAddress, countryIso);
                }
                return 1;
            }
        }
    }

    private boolean getShortcodesCust(String destAddress) {
        int slotId = this.mPhone.getPhoneId();
        RlogEx.d(TAG, "getShortcodesCust, slotId:" + slotId);
        try {
            Boolean shortCodeSwitch = (Boolean) HwCfgFilePolicy.getValue("custom_short_codes_switch", slotId, Boolean.class);
            if (shortCodeSwitch != null) {
                if (shortCodeSwitch.booleanValue()) {
                    String shortCodesList = (String) HwCfgFilePolicy.getValue("sms_short_codes_free", slotId, String.class);
                    if (shortCodesList == null) {
                        return false;
                    }
                    RlogEx.d(TAG, "shortCodesList:" + shortCodesList);
                    String[] shortcodes = shortCodesList.split(";");
                    int n = shortcodes.length;
                    for (int j = 0; j < n; j++) {
                        if (shortcodes[j] != null && shortcodes[j].equals(destAddress)) {
                            RlogEx.d(TAG, "match the free short code ");
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
        } catch (NoClassDefFoundError e) {
            RlogEx.e(TAG, "Failed to get Shortcodes in carrier NoClassDefFoundError.");
        } catch (RuntimeException e2) {
            RlogEx.e(TAG, "Failed to get Shortcodes in carrier.");
        }
    }

    private boolean isDestAddrMatchShortCode(String destAddr, String shortcode) {
        if (destAddr.length() != shortcode.length()) {
            return false;
        }
        char[] destAddArray = destAddr.toCharArray();
        char[] shortcodeArray = shortcode.toCharArray();
        for (int i = 0; i < shortcodeArray.length; i++) {
            if (!(shortcodeArray[i] == '*' || destAddArray[i] == shortcodeArray[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isSpecialPattern(String destAdd, String simMccmnc) {
        ArrayList<ShortCodeCfg> arrayList;
        String shortcodesStr;
        if (destAdd == null || simMccmnc == null || (arrayList = mshortCodeList) == null) {
            return false;
        }
        int listSize = arrayList.size();
        for (int i = 0; i < listSize; i++) {
            String plmn = mshortCodeList.get(i).numeric;
            if (!(plmn == null || !plmn.equals(simMccmnc) || (shortcodesStr = mshortCodeList.get(i).shortcodes) == null)) {
                String[] shortcodes = shortcodesStr.split(";");
                for (int j = 0; j < shortcodes.length; j++) {
                    if (shortcodes[j] != null && isDestAddrMatchShortCode(destAdd, shortcodes[j])) {
                        RlogEx.d(TAG, "match the free short code ");
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static class ShortCodeCfg {
        String numeric;
        String shortcodes;

        private ShortCodeCfg() {
        }
    }
}
