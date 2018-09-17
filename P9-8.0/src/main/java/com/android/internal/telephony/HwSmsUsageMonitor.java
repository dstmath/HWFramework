package com.android.internal.telephony;

import android.content.Context;
import android.os.Environment;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwSmsUsageMonitor extends SmsUsageMonitor {
    private static final boolean ENABLE_CUSTOM_SHORTCODES = SystemProperties.getBoolean("ro.config.custom_short_codes", false);
    private static String FILE_FROM_CUST_DIR = "/data/cust/xml/xml/smsShortcodesList-conf.xml";
    private static String FILE_FROM_SYSTEM_ETC_DIR = "/system/etc/xml/smsShortcodesList-conf.xml";
    private static final String PARAM_SHORTCODE_PATH = "etc/smsShortcodesList-conf.xml";
    private static final String SHORT_CODE_CONFIG_FILE = "xml/smsShortcodesList-conf.xml";
    private static final String TAG = "HwSmsUsageMonitor";
    private static ArrayList<ShortCodeCfg> mshortCodeList;

    static class ShortCodeCfg {
        String numeric;
        String shortcodes;

        ShortCodeCfg() {
        }
    }

    public HwSmsUsageMonitor(Context context) {
        super(context);
        loadShortCodeList();
    }

    /* JADX WARNING: Missing block: B:24:0x003a, code:
            return checkDestination(r6, r7);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected int checkDestinationHw(String destAddress, String countryIso, String simMccmnc) {
        synchronized (this.mSettingsObserverHandler) {
            if (PhoneNumberUtils.isEmergencyNumber(destAddress, countryIso)) {
                Rlog.d(TAG, "isEmergencyNumber");
                return 0;
            } else if (!this.mCheckEnabled.get()) {
                Rlog.d(TAG, "check disabled");
                return 0;
            } else if (ENABLE_CUSTOM_SHORTCODES && isSpecialPattern(destAddress, simMccmnc)) {
                return 1;
            }
        }
    }

    private static BufferedReader getShortCodesFileReader() {
        File confFile = new File(FILE_FROM_SYSTEM_ETC_DIR);
        File sShortCodeCust = new File(FILE_FROM_CUST_DIR);
        File sShortcodeFile = new File(Environment.getRootDirectory(), PARAM_SHORTCODE_PATH);
        try {
            File cfg = HwCfgFilePolicy.getCfgFile(SHORT_CODE_CONFIG_FILE, 0);
            if (cfg != null) {
                confFile = cfg;
                Rlog.d(TAG, "load smsShortcodesList-conf.xml from HwCfgFilePolicy folder");
            } else if (sShortCodeCust.exists()) {
                confFile = sShortCodeCust;
                Rlog.d(TAG, "load smsShortcodesList-conf.xml from cust folder");
            } else {
                confFile = sShortcodeFile;
                Rlog.d(TAG, "load smsShortcodesList-conf.xml from etc folder");
            }
        } catch (NoClassDefFoundError e) {
            Rlog.e(TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
        }
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(confFile), "UTF-8"));
        } catch (FileNotFoundException e2) {
            Rlog.e(TAG, "Can't open " + Environment.getRootDirectory() + "/" + PARAM_SHORTCODE_PATH);
            return null;
        } catch (UnsupportedEncodingException e3) {
            Rlog.d(TAG, "UnsupportedEncodingException Exception");
            return null;
        }
    }

    private static void loadShortCodeList() {
        mshortCodeList = new ArrayList();
        BufferedReader sShortcodeReader = getShortCodesFileReader();
        if (sShortcodeReader == null) {
            Rlog.d(TAG, "loadShortCodeList failed!");
            return;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(sShortcodeReader);
            XmlUtils.beginDocument(parser, "shortCodesList");
            while (true) {
                XmlUtils.nextElement(parser);
                if (parser.getName() == null) {
                    try {
                        sShortcodeReader.close();
                        break;
                    } catch (IOException e) {
                        Rlog.e(TAG, "IOException happen.close failed.");
                    }
                } else {
                    ShortCodeCfg shortcodeConfig = new ShortCodeCfg();
                    shortcodeConfig.numeric = parser.getAttributeValue(null, "numeric");
                    shortcodeConfig.shortcodes = parser.getAttributeValue(null, "codes");
                    Rlog.d(TAG, "getAttributeValue numeric = " + shortcodeConfig.numeric + "getAttributeValue codes = " + shortcodeConfig.shortcodes);
                    mshortCodeList.add(shortcodeConfig);
                }
            }
        } catch (XmlPullParserException e2) {
            Rlog.d(TAG, "Exception in smsShortcodesList parser " + e2);
            try {
                sShortcodeReader.close();
            } catch (IOException e3) {
                Rlog.e(TAG, "IOException happen.close failed.");
            }
        } catch (IOException e4) {
            Rlog.d(TAG, "Exception in smsShortcodesList parser " + e4);
            try {
                sShortcodeReader.close();
            } catch (IOException e5) {
                Rlog.e(TAG, "IOException happen.close failed.");
            }
        } catch (Throwable th) {
            try {
                sShortcodeReader.close();
            } catch (IOException e6) {
                Rlog.e(TAG, "IOException happen.close failed.");
            }
            throw th;
        }
    }

    private boolean isSpecialPattern(String destAdd, String simMccmnc) {
        if (destAdd == null || simMccmnc == null || mshortCodeList == null) {
            return false;
        }
        int listSize = mshortCodeList.size();
        for (int i = 0; i < listSize; i++) {
            String plmn = ((ShortCodeCfg) mshortCodeList.get(i)).numeric;
            if (plmn != null && (plmn.equals(simMccmnc) ^ 1) == 0) {
                String shortcodesStr = ((ShortCodeCfg) mshortCodeList.get(i)).shortcodes;
                if (shortcodesStr != null) {
                    String[] shortcodes = shortcodesStr.split(";");
                    int j = 0;
                    while (j < shortcodes.length) {
                        if (shortcodes[j] == null || !shortcodes[j].equals(destAdd)) {
                            j++;
                        } else {
                            Rlog.d(TAG, "match the free short code ");
                            return true;
                        }
                    }
                    continue;
                } else {
                    continue;
                }
            }
        }
        return false;
    }
}
