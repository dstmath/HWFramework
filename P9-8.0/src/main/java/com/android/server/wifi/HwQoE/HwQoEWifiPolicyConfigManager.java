package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;
import com.android.server.wifi.HwCHRWifiCPUUsage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwQoEWifiPolicyConfigManager {
    private static final String CONF_FILE_NAME = "wifi_policy.xml";
    private static final String XML_TAG_APP_NAME1 = "app_name";
    private static final String XML_TAG_SLEEP_POLICY = "sleep_policy";
    private static final String XML_TAG_SLEEP_TIME = "sleep_time";
    private static final String XML_TAG_VERSION = "version_number";
    private static HwQoEWifiPolicyConfigManager mHwQoEWifiPolicyConfigManager;
    private Map<String, String> mCloudWiFiSleepPolicyMap = new HashMap();
    private Map<String, String> mDefaultWiFiSleepPolicyMap;
    private String mWiFiSleepAppName;
    private String mWiFiSleepTime;

    public static HwQoEWifiPolicyConfigManager getInstance(Context context) {
        if (mHwQoEWifiPolicyConfigManager == null) {
            mHwQoEWifiPolicyConfigManager = new HwQoEWifiPolicyConfigManager(context);
        }
        return mHwQoEWifiPolicyConfigManager;
    }

    public void updateWifiSleepWhiteList(List<String> packageWhiteList) {
        if (packageWhiteList != null && !packageWhiteList.isEmpty()) {
            int size = packageWhiteList.size();
            for (int i = 0; i < size; i++) {
                String packageNameAndTime = (String) packageWhiteList.get(i);
                if (!TextUtils.isEmpty(packageNameAndTime)) {
                    String[] strings = packageNameAndTime.split(HwQoEUtils.SEPARATOR, 2);
                    if (strings.length == 2) {
                        String appName = strings[0];
                        String sleepTime = strings[1];
                        HwQoEUtils.logD("Cloud appName : " + appName + " ,sleepTime: " + sleepTime);
                        if (!TextUtils.isEmpty(appName) && !TextUtils.isEmpty(sleepTime)) {
                            this.mCloudWiFiSleepPolicyMap.put(appName, sleepTime);
                        } else {
                            return;
                        }
                    }
                    return;
                }
            }
        }
    }

    private HwQoEWifiPolicyConfigManager(Context context) {
        parseConfFile(context);
    }

    private void parseConfFile(Context context) {
        InputStream inputStream = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            inputStream = context.getAssets().open(CONF_FILE_NAME);
            parser.setInput(inputStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                switch (eventType) {
                    case 2:
                        if (!"version_number".equals(parser.getName())) {
                            if (!XML_TAG_SLEEP_POLICY.equals(parser.getName())) {
                                if (!XML_TAG_APP_NAME1.equals(parser.getName())) {
                                    if (!XML_TAG_SLEEP_TIME.equals(parser.getName())) {
                                        break;
                                    }
                                    this.mWiFiSleepTime = parser.nextText();
                                    if (!TextUtils.isEmpty(this.mWiFiSleepTime)) {
                                        this.mWiFiSleepTime = this.mWiFiSleepTime.replaceAll(HwCHRWifiCPUUsage.COL_SEP, "");
                                        putDefaultWiFiSleepPolicyMap(this.mWiFiSleepAppName, this.mWiFiSleepTime);
                                    }
                                    this.mWiFiSleepAppName = null;
                                    break;
                                }
                                this.mWiFiSleepAppName = parser.nextText();
                                if (!TextUtils.isEmpty(this.mWiFiSleepAppName)) {
                                    this.mWiFiSleepAppName = this.mWiFiSleepAppName.replaceAll(HwCHRWifiCPUUsage.COL_SEP, "");
                                    break;
                                }
                                break;
                            } else if (this.mDefaultWiFiSleepPolicyMap != null) {
                                break;
                            } else {
                                this.mDefaultWiFiSleepPolicyMap = new HashMap();
                                break;
                            }
                        }
                        HwQoEUtils.logD("whitelist VERSION = " + parser.nextText());
                        break;
                    default:
                        break;
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    HwQoEUtils.logD(e.getMessage());
                }
            }
        } catch (IOException e2) {
            HwQoEUtils.logD(e2.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e3) {
                    HwQoEUtils.logD(e3.getMessage());
                }
            }
        } catch (XmlPullParserException e4) {
            HwQoEUtils.logD(e4.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e32) {
                    HwQoEUtils.logD(e32.getMessage());
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e322) {
                    HwQoEUtils.logD(e322.getMessage());
                }
            }
        }
    }

    private void putDefaultWiFiSleepPolicyMap(String key, String value) {
        if (this.mDefaultWiFiSleepPolicyMap != null && (TextUtils.isEmpty(value) ^ 1) != 0 && (TextUtils.isEmpty(value) ^ 1) != 0) {
            HwQoEUtils.logD("Default name : " + key + " ,time: " + value);
            this.mDefaultWiFiSleepPolicyMap.put(key, value);
        }
    }

    public int queryWifiSleepTime(String appName) {
        int sleepTime = queryCloudAppWifiSleepTime(appName);
        if (-1 == sleepTime) {
            return queryDefaultAppWifiSleepTime(appName);
        }
        return sleepTime;
    }

    private int queryDefaultAppWifiSleepTime(String appName) {
        int sleepTime = -1;
        if (TextUtils.isEmpty(appName) || this.mDefaultWiFiSleepPolicyMap == null || this.mDefaultWiFiSleepPolicyMap.size() == 0 || (this.mDefaultWiFiSleepPolicyMap.containsKey(appName) ^ 1) != 0) {
            return sleepTime;
        }
        String value = (String) this.mDefaultWiFiSleepPolicyMap.get(appName);
        if (!TextUtils.isEmpty(value)) {
            try {
                sleepTime = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                HwQoEUtils.logD("NumberFormatException e : " + e.toString());
            }
        }
        return sleepTime;
    }

    public int queryCloudAppWifiSleepTime(String appName) {
        int sleepTime = -1;
        if (TextUtils.isEmpty(appName) || this.mCloudWiFiSleepPolicyMap == null || this.mCloudWiFiSleepPolicyMap.size() == 0 || (this.mCloudWiFiSleepPolicyMap.containsKey(appName) ^ 1) != 0) {
            return sleepTime;
        }
        String value = (String) this.mCloudWiFiSleepPolicyMap.get(appName);
        if (!TextUtils.isEmpty(value)) {
            try {
                sleepTime = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                HwQoEUtils.logD("NumberFormatException e : " + e.toString());
            }
        }
        return sleepTime;
    }
}
