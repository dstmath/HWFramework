package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;
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
                String packageNameAndTime = packageWhiteList.get(i);
                if (!TextUtils.isEmpty(packageNameAndTime)) {
                    String[] strings = packageNameAndTime.split(":", 2);
                    if (strings.length == 2) {
                        String appName = strings[0];
                        String sleepTime = strings[1];
                        HwQoEUtils.logD("Cloud appName : " + appName + " ,sleepTime: " + sleepTime);
                        if (!TextUtils.isEmpty(appName) && !TextUtils.isEmpty(sleepTime)) {
                            this.mCloudWiFiSleepPolicyMap.put(appName, sleepTime);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
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
            InputStream inputStream2 = context.getAssets().open(CONF_FILE_NAME);
            parser.setInput(inputStream2, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        if (XML_TAG_VERSION.equals(parser.getName())) {
                            HwQoEUtils.logD("whitelist VERSION = " + parser.nextText());
                        } else if (XML_TAG_SLEEP_POLICY.equals(parser.getName())) {
                            if (this.mDefaultWiFiSleepPolicyMap == null) {
                                this.mDefaultWiFiSleepPolicyMap = new HashMap();
                            }
                        } else if (XML_TAG_APP_NAME1.equals(parser.getName())) {
                            this.mWiFiSleepAppName = parser.nextText();
                            if (!TextUtils.isEmpty(this.mWiFiSleepAppName)) {
                                this.mWiFiSleepAppName = this.mWiFiSleepAppName.replaceAll(" ", "");
                            }
                        } else if (XML_TAG_SLEEP_TIME.equals(parser.getName())) {
                            this.mWiFiSleepTime = parser.nextText();
                            if (!TextUtils.isEmpty(this.mWiFiSleepTime)) {
                                this.mWiFiSleepTime = this.mWiFiSleepTime.replaceAll(" ", "");
                                putDefaultWiFiSleepPolicyMap(this.mWiFiSleepAppName, this.mWiFiSleepTime);
                            }
                            this.mWiFiSleepAppName = null;
                        }
                    }
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Exception e) {
                    HwQoEUtils.logD(e.getMessage());
                }
            }
        } catch (IOException e2) {
            HwQoEUtils.logD(e2.getMessage());
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (XmlPullParserException e3) {
            HwQoEUtils.logD(e3.getMessage());
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e4) {
                    HwQoEUtils.logD(e4.getMessage());
                }
            }
            throw th;
        }
    }

    private void putDefaultWiFiSleepPolicyMap(String key, String value) {
        if (this.mDefaultWiFiSleepPolicyMap != null && !TextUtils.isEmpty(value) && !TextUtils.isEmpty(value)) {
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
        if (TextUtils.isEmpty(appName) || this.mDefaultWiFiSleepPolicyMap == null || this.mDefaultWiFiSleepPolicyMap.size() == 0 || !this.mDefaultWiFiSleepPolicyMap.containsKey(appName)) {
            return -1;
        }
        String value = this.mDefaultWiFiSleepPolicyMap.get(appName);
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
        if (TextUtils.isEmpty(appName) || this.mCloudWiFiSleepPolicyMap == null || this.mCloudWiFiSleepPolicyMap.size() == 0 || !this.mCloudWiFiSleepPolicyMap.containsKey(appName)) {
            return -1;
        }
        String value = this.mCloudWiFiSleepPolicyMap.get(appName);
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
