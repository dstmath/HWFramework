package com.android.server.pm;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.SplitNotificationUtils;
import android.util.Xml;
import com.android.server.wifipro.WifiProCommonUtils;
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

public class MultiWinWhiteListManager {
    private static final String FILE_MULTIWINDOW_WHITELIST = "multiwindow_whitelist_apps.xml";
    private static final boolean ISCHINAAREA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    static final String TAG = "MultiWinWhiteListManager";
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "package_name";
    private static final String XML_ELEMENT_APP_FORCED_PORTRAIT_ITEM = "mw_app_forced_portrait";
    private static final String XML_ELEMENT_APP_ITEM = "mw_app";
    private static final String XML_ELEMENT_APP_LIST = "multiwindow_whitelist";
    private static final String XML_FREEFORM_IMS_CHINA_ITEM = "freeform_instant_app_china";
    private static final String XML_FREEFORM_IMS_OVERSEA_ITEM = "freeform_instant_app_oversea";
    private static final String XML_ONE_SPLIT_SCREEN_IMS_ITEM = "mw_app_instant_msg";
    private static final String XML_ONE_SPLIT_SCREEN_VIDEO_ITEM = "mw_app_video";
    private static List<String> mMwPortraitWhiteListPkgNames = new ArrayList();
    private static List<String> sMultiWinWhiteListPkgNames = new ArrayList();

    private static class SingletonHolder {
        /* access modifiers changed from: private */
        public static final MultiWinWhiteListManager INSTANCE = new MultiWinWhiteListManager();

        private SingletonHolder() {
        }
    }

    public static MultiWinWhiteListManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0031  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0040 A[SYNTHETIC, Splitter:B:21:0x0040] */
    public void loadMultiWinWhiteList(Context aContext) {
        File configFile = HwPackageManagerUtils.getCustomizedFileName(FILE_MULTIWINDOW_WHITELIST, 0);
        InputStream inputStream = null;
        if (configFile != null) {
            try {
                if (configFile.exists()) {
                    inputStream = new FileInputStream(configFile);
                    if (inputStream != null) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                Slog.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream", e);
                            }
                        }
                        return;
                    }
                    parseMultiWindowWhiteListXml(aContext, inputStream);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            Slog.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream", e2);
                        }
                    }
                    return;
                }
            } catch (FileNotFoundException e3) {
                Log.e(TAG, "loadMultiWinWhiteList", e3);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e4) {
                Log.e(TAG, "loadMultiWinWhiteList", e4);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Slog.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream", e5);
                    }
                }
                throw th;
            }
        }
        Flog.i(205, "Multi Window white list taken from default configuration");
        inputStream = aContext.getAssets().open(FILE_MULTIWINDOW_WHITELIST);
        if (inputStream != null) {
        }
    }

    private void parseMultiWindowWhiteListXml(Context aContext, InputStream inputStream) {
        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream, null);
            for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                if (xmlEventType == 2) {
                    String packageName = xmlParser.getAttributeValue(null, "package_name");
                    if (packageName != null) {
                        packageName = packageName.toLowerCase(Locale.ENGLISH);
                    }
                    if (XML_ELEMENT_APP_ITEM.equals(xmlParser.getName())) {
                        sMultiWinWhiteListPkgNames.add(packageName);
                        Flog.i(205, "Multiwindow whitelist package name: [" + packageName + "]");
                    } else if (XML_ELEMENT_APP_FORCED_PORTRAIT_ITEM.equals(xmlParser.getName())) {
                        mMwPortraitWhiteListPkgNames.add(packageName);
                        Flog.i(205, "Multiwindow portrait whitelist package name: [" + packageName + "]");
                    } else if (XML_ONE_SPLIT_SCREEN_VIDEO_ITEM.equals(xmlParser.getName())) {
                        SplitNotificationUtils.getInstance(aContext).addPkgName(packageName, 1);
                        Flog.i(205, "one split screen video whitelist package name: [" + packageName + "]");
                    } else if (XML_ONE_SPLIT_SCREEN_IMS_ITEM.equals(xmlParser.getName())) {
                        SplitNotificationUtils.getInstance(aContext).addPkgName(packageName, 2);
                        Flog.i(205, "one split screen IMS whitelist package name: [" + packageName + "]");
                    } else if (XML_FREEFORM_IMS_CHINA_ITEM.equals(xmlParser.getName()) && ISCHINAAREA) {
                        SplitNotificationUtils.getInstance(aContext).addPkgName(packageName, 3);
                        Flog.i(205, "floating window IMS china whitelist package name: [" + packageName + "]");
                    } else if (!XML_FREEFORM_IMS_OVERSEA_ITEM.equals(xmlParser.getName()) || ISCHINAAREA) {
                        Log.e(TAG, "parseMultiWindowWhiteListXml, xmlParser.getName() error");
                    } else {
                        SplitNotificationUtils.getInstance(aContext).addPkgName(packageName, 3);
                        Flog.i(205, "floating window IMS oversea whitelist package name: [" + packageName + "]");
                    }
                } else if (xmlEventType != 3 || !XML_ELEMENT_APP_LIST.equals(xmlParser.getName())) {
                    Log.e(TAG, "parseMultiWindowWhiteListXml, other xmlEventType");
                } else {
                    return;
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "loadMultiWinWhiteList", e);
        } catch (IOException e2) {
            Log.e(TAG, "loadMultiWinWhiteList", e2);
        }
    }

    public boolean isInMultiWinWhiteList(String packageName) {
        if (packageName == null || sMultiWinWhiteListPkgNames.size() == 0 || !sMultiWinWhiteListPkgNames.contains(packageName.toLowerCase(Locale.ENGLISH))) {
            return false;
        }
        return true;
    }

    public boolean isInMWPortraitWhiteList(String packageName) {
        if (packageName == null || mMwPortraitWhiteListPkgNames.size() == 0 || !mMwPortraitWhiteListPkgNames.contains(packageName.toLowerCase(Locale.ENGLISH))) {
            return false;
        }
        return true;
    }
}
