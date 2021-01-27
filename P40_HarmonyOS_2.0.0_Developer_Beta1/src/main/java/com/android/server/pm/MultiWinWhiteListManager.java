package com.android.server.pm;

import android.content.Context;
import android.util.Flog;
import android.util.Log;
import android.util.SplitNotificationUtilsEx;
import android.util.Xml;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MultiWinWhiteListManager extends DefaultMultiWinWhiteListManager {
    private static final String FILE_MULTIWINDOW_WHITELIST = "multiwindow_whitelist_apps.xml";
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    static final String TAG = "MultiWinWhiteListManager";
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "package_name";
    private static final String XML_ELEMENT_APP_FORCED_PORTRAIT_ITEM = "mw_app_forced_portrait";
    private static final String XML_ELEMENT_APP_ITEM = "mw_app";
    private static final String XML_ELEMENT_APP_LIST = "multiwindow_whitelist";
    private static final String XML_FREEFORM_IMS_CHINA_ITEM = "freeform_instant_app_china";
    private static final String XML_FREEFORM_IMS_OVERSEA_ITEM = "freeform_instant_app_oversea";
    private static final String XML_ONE_SPLIT_SCREEN_IMS_ITEM = "mw_app_instant_msg";
    private static final String XML_ONE_SPLIT_SCREEN_VIDEO_ITEM = "mw_app_video";
    private static List<String> sMWPortraitWhiteListPkgNames = new ArrayList();
    private static List<String> sMultiWinWhiteListPkgNames = new ArrayList();

    private static class SingletonHolder {
        private static final MultiWinWhiteListManager INSTANCE = new MultiWinWhiteListManager();

        private SingletonHolder() {
        }
    }

    public static MultiWinWhiteListManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0030  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003b  */
    public void loadMultiWinWhiteList(Context context) {
        InputStream inputStream;
        if (context != null) {
            File configFile = HwPackageManagerUtils.getCustomizedFileName(FILE_MULTIWINDOW_WHITELIST, 0);
            InputStream inputStream2 = null;
            if (configFile != null) {
                try {
                    if (configFile.exists()) {
                        inputStream = new FileInputStream(configFile);
                        if (inputStream == null) {
                            XmlPullParser xmlParser = Xml.newPullParser();
                            xmlParser.setInput(inputStream, null);
                            SplitNotificationUtilsEx utils = SplitNotificationUtilsEx.getInstance(context);
                            for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                                if (xmlEventType == 2) {
                                    String packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                                    if (packageName != null) {
                                        packageName = packageName.toLowerCase(Locale.ROOT).intern();
                                    }
                                    addXmlParserName(xmlParser.getName(), packageName, utils);
                                } else if (xmlEventType != 3) {
                                    continue;
                                } else if (!XML_ELEMENT_APP_LIST.equals(xmlParser.getName())) {
                                }
                            }
                            try {
                                inputStream.close();
                                return;
                            } catch (IOException e) {
                                SlogEx.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream");
                                return;
                            }
                        } else if (inputStream != null) {
                            try {
                                inputStream.close();
                                return;
                            } catch (IOException e2) {
                                SlogEx.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream");
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                } catch (FileNotFoundException e3) {
                    Log.e(TAG, "read multi_window whitelist error");
                    if (0 != 0) {
                        inputStream2.close();
                        return;
                    }
                    return;
                } catch (XmlPullParserException e4) {
                    Log.e(TAG, "loadMultiWinWhiteList XmlPullParserException");
                    if (0 != 0) {
                        inputStream2.close();
                        return;
                    }
                    return;
                } catch (IOException e5) {
                    Log.e(TAG, "loadMultiWinWhiteList IOException");
                    if (0 != 0) {
                        inputStream2.close();
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            inputStream2.close();
                        } catch (IOException e6) {
                            SlogEx.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream");
                        }
                    }
                    throw th;
                }
            }
            Flog.i(205, "Multi Window white list taken from default configuration");
            inputStream = context.getAssets().open(FILE_MULTIWINDOW_WHITELIST);
            if (inputStream == null) {
            }
        }
    }

    private void addXmlParserName(String xmlParserName, String packageName, SplitNotificationUtilsEx utils) {
        if (XML_ELEMENT_APP_ITEM.equals(xmlParserName)) {
            sMultiWinWhiteListPkgNames.add(packageName);
            Flog.i(205, "Multiwindow whitelist package name: [" + packageName + "]");
        } else if (XML_ELEMENT_APP_FORCED_PORTRAIT_ITEM.equals(xmlParserName)) {
            sMWPortraitWhiteListPkgNames.add(packageName);
            Flog.i(205, "Multiwindow portrait whitelist package name: [" + packageName + "]");
        } else if (XML_ONE_SPLIT_SCREEN_VIDEO_ITEM.equals(xmlParserName)) {
            utils.addPkgName(packageName, 1);
            Flog.i(205, "one split screen video whitelist package name: [" + packageName + "]");
        } else if (XML_ONE_SPLIT_SCREEN_IMS_ITEM.equals(xmlParserName)) {
            utils.addPkgName(packageName, 2);
            Flog.i(205, "one split screen IMS whitelist package name: [" + packageName + "]");
        } else if (XML_FREEFORM_IMS_CHINA_ITEM.equals(xmlParserName) && IS_CHINA_AREA) {
            utils.addPkgName(packageName, 3);
            Flog.i(205, "floating window IMS china whitelist package name: [" + packageName + "]");
        } else if (XML_FREEFORM_IMS_OVERSEA_ITEM.equals(xmlParserName) && !IS_CHINA_AREA) {
            utils.addPkgName(packageName, 3);
            Flog.i(205, "floating window IMS oversea whitelist package name: [" + packageName + "]");
        }
    }

    public boolean isInMultiWinWhiteList(String packageName) {
        if (packageName == null || sMultiWinWhiteListPkgNames.size() == 0 || !sMultiWinWhiteListPkgNames.contains(packageName.toLowerCase(Locale.ROOT))) {
            return false;
        }
        return true;
    }

    public boolean isInMWPortraitWhiteList(String packageName) {
        if (packageName == null || sMWPortraitWhiteListPkgNames.size() == 0 || !sMWPortraitWhiteListPkgNames.contains(packageName.toLowerCase(Locale.ROOT))) {
            return false;
        }
        return true;
    }
}
