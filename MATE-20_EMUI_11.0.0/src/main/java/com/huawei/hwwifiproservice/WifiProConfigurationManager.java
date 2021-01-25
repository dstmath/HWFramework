package com.huawei.hwwifiproservice;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WifiProConfigurationManager {
    private static final String CONF_FILE_NAME = "wifipro_regexlist.xml";
    private static final boolean DBG = true;
    private static final String TAG = "WiFi_PRO_WifiProConfigurationManager";
    private static final String XML_TAG_APP_NAME = "app_name";
    private static final String XML_TAG_PARSER_REGEX = "auth_code_parser_regex";
    private static final String XML_TAG_PORTAL_SERVER = "portal_check_server";
    private static final String XML_TAG_REGEX_CODE_EXCLUSIVE = "code_exclusive";
    private static final String XML_TAG_REGEX_CODE_EXCLUSIVE_DATE_1 = "code_exclusive_date_1";
    private static final String XML_TAG_REGEX_CODE_EXCLUSIVE_DATE_2 = "code_exclusive_date_2";
    private static final String XML_TAG_REGEX_CODE_EXCLUSIVE_DATE_3 = "code_exclusive_date_3";
    private static final String XML_TAG_REGEX_CODE_NECESSARY = "code_necessary";
    private static final String XML_TAG_REGEX_SMS_BODY_OPT = "sms_body_opt";
    private static final String XML_TAG_REGEX_SMS_NUM_BEGIN = "sms_num_begin";
    private static final String XML_TAG_REGEX_SMS_NUM_LEN = "sms_num_min_len";
    private static final String XML_TAG_WHITE_LIST = "white_list_no_handover";
    private static WifiProConfigurationManager sWifiProConfigurationManager;
    private String mAppName;
    private List<String> mAppWhitelists;
    private Map<String, String> mParserRegexMapLists;

    public static WifiProConfigurationManager createWifiProConfigurationManager(Context context) {
        if (sWifiProConfigurationManager == null) {
            sWifiProConfigurationManager = new WifiProConfigurationManager(context);
        }
        return sWifiProConfigurationManager;
    }

    private WifiProConfigurationManager(Context context) {
        parseConfFile(context);
    }

    private void parseConfFile(Context context) {
        InputStream inputStream = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            inputStream = context.getAssets().open(CONF_FILE_NAME);
            parser.setInput(inputStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType != 2) {
                        if (eventType != 3) {
                            logD("eventType is exception!");
                        }
                    } else if (XML_TAG_WHITE_LIST.equals(parser.getName())) {
                        this.mAppWhitelists = new ArrayList();
                    } else if (XML_TAG_APP_NAME.equals(parser.getName())) {
                        this.mAppName = parser.nextText();
                        this.mAppName = this.mAppName.replaceAll(" ", "");
                        Log.i(TAG, "whitelist app name = " + this.mAppName);
                        if (this.mAppWhitelists != null) {
                            this.mAppWhitelists.add(this.mAppName);
                        }
                    } else if (XML_TAG_PARSER_REGEX.equals(parser.getName())) {
                        this.mParserRegexMapLists = new HashMap();
                    } else if (XML_TAG_REGEX_SMS_BODY_OPT.equals(parser.getName()) || XML_TAG_REGEX_SMS_NUM_BEGIN.equals(parser.getName()) || XML_TAG_REGEX_SMS_NUM_LEN.equals(parser.getName()) || XML_TAG_REGEX_CODE_EXCLUSIVE.equals(parser.getName()) || XML_TAG_REGEX_CODE_NECESSARY.equals(parser.getName()) || XML_TAG_REGEX_CODE_EXCLUSIVE_DATE_1.equals(parser.getName()) || XML_TAG_REGEX_CODE_EXCLUSIVE_DATE_2.equals(parser.getName()) || XML_TAG_REGEX_CODE_EXCLUSIVE_DATE_3.equals(parser.getName())) {
                        putRegexMapList(parser.getName(), parser.nextText());
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (XmlPullParserException e2) {
            Log.e(TAG, e2.getMessage());
        } catch (Throwable th) {
            parseFileFinally(null);
            throw th;
        }
        parseFileFinally(inputStream);
    }

    private void parseFileFinally(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void putRegexMapList(String key, String value) {
        if (this.mParserRegexMapLists != null && !TextUtils.isEmpty(value) && !TextUtils.isEmpty(value)) {
            this.mParserRegexMapLists.put(key, value.replaceAll(" ", ""));
        }
    }

    public Map<String, String> getRegexMapLis() {
        return this.mParserRegexMapLists;
    }

    public List<String> getAppWhitelists() {
        return this.mAppWhitelists;
    }

    private void logD(String info) {
        Log.d(TAG, info);
    }
}
