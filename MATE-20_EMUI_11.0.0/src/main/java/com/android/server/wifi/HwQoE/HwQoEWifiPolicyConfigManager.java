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
    private static final int PACKAGE_FILED_LENGTH = 2;
    private static final String XML_TAG_APP_NAME1 = "app_name";
    private static final String XML_TAG_SLEEP_POLICY = "sleep_policy";
    private static final String XML_TAG_SLEEP_TIME = "sleep_time";
    private static final String XML_TAG_VERSION = "version_number";
    private static HwQoEWifiPolicyConfigManager mHwQoEWifiPolicyConfigManager;
    private Map<String, String> mCloudWiFiSleepPolicyMap = new HashMap();
    private Map<String, String> mDefaultWiFiSleepPolicyMap;
    private String mWiFiSleepAppName;
    private String mWiFiSleepTime;

    private HwQoEWifiPolicyConfigManager(Context context) {
        parseConfFile(context);
    }

    public static HwQoEWifiPolicyConfigManager getInstance(Context context) {
        if (mHwQoEWifiPolicyConfigManager == null) {
            mHwQoEWifiPolicyConfigManager = new HwQoEWifiPolicyConfigManager(context);
        }
        return mHwQoEWifiPolicyConfigManager;
    }

    public void updateWifiSleepWhiteList(List<String> packageWhiteList) {
        if (!(packageWhiteList == null || packageWhiteList.isEmpty())) {
            int size = packageWhiteList.size();
            for (int i = 0; i < size; i++) {
                String packageNameAndTime = packageWhiteList.get(i);
                if (!TextUtils.isEmpty(packageNameAndTime)) {
                    String[] strings = packageNameAndTime.split(":", 2);
                    if (strings.length == 2) {
                        String appName = strings[0];
                        String sleepTime = strings[1];
                        HwQoEUtils.logD(false, "Cloud appName : %{public}s, sleepTime: %{public}s", appName, sleepTime);
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

    private void startParseConfFile(XmlPullParser parser) {
        try {
            if (XML_TAG_VERSION.equals(parser.getName())) {
                HwQoEUtils.logD(false, "whitelist VERSION = %{public}s", parser.nextText());
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
            } else {
                HwQoEUtils.logD(false, "XML TAG is not parsed", new Object[0]);
            }
        } catch (IOException e) {
            HwQoEUtils.logD(false, "%{public}s", e.getMessage());
        } catch (XmlPullParserException e2) {
            HwQoEUtils.logD(false, "%{public}s", e2.getMessage());
        }
    }

    private void parseConfFile(Context context) {
        Object[] objArr;
        InputStream inputStream = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            InputStream inputStream2 = context.getAssets().open(CONF_FILE_NAME);
            parser.setInput(inputStream2, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        startParseConfFile(parser);
                    } else if (eventType != 3) {
                    }
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                    return;
                } catch (IOException e) {
                    objArr = new Object[]{e.getMessage()};
                }
            } else {
                return;
            }
            HwQoEUtils.logD(false, "%{public}s", objArr);
        } catch (IOException e2) {
            HwQoEUtils.logD(false, "%{public}s", e2.getMessage());
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    objArr = new Object[]{e3.getMessage()};
                }
            }
        } catch (XmlPullParserException e4) {
            HwQoEUtils.logD(false, "%{public}s", e4.getMessage());
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    objArr = new Object[]{e5.getMessage()};
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    HwQoEUtils.logD(false, "%{public}s", e6.getMessage());
                }
            }
            throw th;
        }
    }

    private void putDefaultWiFiSleepPolicyMap(String key, String value) {
        if (this.mDefaultWiFiSleepPolicyMap != null && !TextUtils.isEmpty(value) && !TextUtils.isEmpty(value)) {
            HwQoEUtils.logD(false, "Default name : %{public}s, time: %{public}s", key, value);
            this.mDefaultWiFiSleepPolicyMap.put(key, value);
        }
    }

    public int queryWifiSleepTime(String appName) {
        int sleepTime = queryCloudAppWifiSleepTime(appName);
        if (sleepTime == -1) {
            return queryDefaultAppWifiSleepTime(appName);
        }
        return sleepTime;
    }

    private boolean checkDefaultAppWifiSleepPolicy(String appName) {
        Map<String, String> map;
        if (TextUtils.isEmpty(appName) || (map = this.mDefaultWiFiSleepPolicyMap) == null || map.size() == 0 || !this.mDefaultWiFiSleepPolicyMap.containsKey(appName)) {
            return true;
        }
        return false;
    }

    private int queryDefaultAppWifiSleepTime(String appName) {
        if (checkDefaultAppWifiSleepPolicy(appName)) {
            return -1;
        }
        String value = this.mDefaultWiFiSleepPolicyMap.get(appName);
        if (TextUtils.isEmpty(value)) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            HwQoEUtils.logD(false, "NumberFormatException e : %{public}s", e.getMessage());
            return -1;
        }
    }

    private boolean checkCloudAppWifiSleepPolicy(String appName) {
        Map<String, String> map;
        if (TextUtils.isEmpty(appName) || (map = this.mCloudWiFiSleepPolicyMap) == null || map.size() == 0 || !this.mCloudWiFiSleepPolicyMap.containsKey(appName)) {
            return true;
        }
        return false;
    }

    public int queryCloudAppWifiSleepTime(String appName) {
        if (checkCloudAppWifiSleepPolicy(appName)) {
            return -1;
        }
        String value = this.mCloudWiFiSleepPolicyMap.get(appName);
        if (TextUtils.isEmpty(value)) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            HwQoEUtils.logD(false, "NumberFormatException e : %{public}s", e.getMessage());
            return -1;
        }
    }
}
