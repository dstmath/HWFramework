package com.android.server.pc.whiltestrategy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.SystemProperties;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DefaultXmlFileAppStrategy implements AppStrategy {
    private static final boolean COUNTRY_DEMO = "demo".equalsIgnoreCase(SystemProperties.get("ro.hw.country", ""));
    private static final String FILE_HW_SUPPORT_PC_WHITELIST = "hw_pc_white_apps.xml";
    private static final String FILE_POLICY_CLASS_NAME = "com.huawei.cust.HwCfgFilePolicy";
    private static final boolean IS_19_VERSION = SystemProperties.get("ro.product.model", "").contains("19");
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", ""));
    private static final String METHOD_NAME_FOR_FILE = "getCfgFile";
    private static final String PAD_FILE_HW_SUPPORT_PC_WHITELIST = "hw_pc_white_apps_pad.xml";
    private static final String PAD_XML_ELEMENT_APP_GROUP_NORMAL = "hw_support_pc_apps";
    private static final String PAD_XML_ELEMENT_APP_GROUP_WIFI = "hw_support_pc_apps_wifi";
    private static final String TAG = "DefaultXmlFileAppStrategy";
    private static final boolean VENDOR_DEMO = "demo".equalsIgnoreCase(SystemProperties.get("ro.hw.vendor", ""));
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "packageName";
    private static final String XML_ATTRIBUTE_TYPE = "type";
    private static final String XML_ELEMENT_APP_ITEM = "hw_support_pc_app";
    private static final String XML_ELEMENT_SPECIAL_WINDOW_POLICY_APP_ITEM = "hw_special_window_policy_app";
    private boolean mPadWifiVersion = false;
    List<Pair<String, Integer>> mSpecailWindowPolicyAppList = null;
    Map<String, Integer> mWhiteAppList = null;

    public DefaultXmlFileAppStrategy(Context context) {
        loadDefaultWhiteListFromXml(context);
    }

    public List<Pair<String, Integer>> getSpecailWindowPolicyAppList() {
        return this.mSpecailWindowPolicyAppList;
    }

    public Map<String, Integer> getAppList(Context context) {
        return this.mWhiteAppList;
    }

    public int getAppState(String packageName, Context context) {
        if (packageName == null || this.mWhiteAppList == null || !this.mWhiteAppList.containsKey(packageName)) {
            return -1;
        }
        return 1;
    }

    private static File getCfgFile(String fileName, int type) throws Exception, NoClassDefFoundError {
        Class<?> filePolicyClazz = Class.forName(FILE_POLICY_CLASS_NAME);
        return (File) filePolicyClazz.getMethod(METHOD_NAME_FOR_FILE, new Class[]{String.class, Integer.TYPE}).invoke(filePolicyClazz, new Object[]{fileName, Integer.valueOf(type)});
    }

    private static File getCustomizedFileName(String xmlName, int flag) {
        try {
            return getCfgFile("xml/" + xmlName, flag);
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "getCustomizedFileName get layout file exception");
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x007e A[SYNTHETIC, Splitter:B:32:0x007e] */
    private void loadDefaultWhiteListFromXml(Context context) {
        File path;
        if (HwPCUtils.enabledInPad()) {
            path = getCustomizedFileName(PAD_FILE_HW_SUPPORT_PC_WHITELIST, 0);
        } else {
            path = getCustomizedFileName(FILE_HW_SUPPORT_PC_WHITELIST, 0);
        }
        Log.i(TAG, " loadDefaultWhiteListFromXml path:" + path);
        InputStream in = null;
        if (path != null) {
            try {
                if (path.exists()) {
                    in = new FileInputStream(path);
                    if (in != null) {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                Log.e(TAG, "loadDefaultWhiteListFromXml:- IOE while closing stream", e);
                            }
                        }
                        return;
                    }
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    this.mWhiteAppList = new LinkedHashMap();
                    this.mSpecailWindowPolicyAppList = new ArrayList();
                    if (HwPCUtils.enabledInPad()) {
                        this.mPadWifiVersion = isPadWifiVersion(context);
                        parserXMLforPad(parser);
                        if (isDemoVersion()) {
                            this.mWhiteAppList.put("com.gameloft.android.GAND.GloftA8HU", 1);
                            this.mSpecailWindowPolicyAppList.add(new Pair("com.gameloft.android.GAND.GloftA8HU", 6));
                            this.mWhiteAppList.put("com.huawei.experience.toprand.cs.en", 1);
                            this.mSpecailWindowPolicyAppList.add(new Pair("com.huawei.experience.toprand.cs.en", 6));
                            if (VENDOR_DEMO) {
                                this.mWhiteAppList.put("com.huawei.experience.toprand.cs", 1);
                                this.mSpecailWindowPolicyAppList.add(new Pair("com.huawei.experience.toprand.cs", 6));
                            }
                            this.mWhiteAppList.put("com.huawei.retaildemo", 1);
                            this.mSpecailWindowPolicyAppList.add(new Pair("com.huawei.retaildemo", 6));
                            if (IS_19_VERSION) {
                                this.mWhiteAppList.put("com.adsk.sketchbook", 1);
                                this.mSpecailWindowPolicyAppList.add(new Pair("com.adsk.sketchbook", 6));
                                this.mWhiteAppList.put("com.myscript.nebo.huawei", 1);
                                this.mSpecailWindowPolicyAppList.add(new Pair("com.myscript.nebo.huawei", 6));
                            }
                        }
                    } else {
                        parserXML(parser);
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "loadDefaultWhiteListFromXml:- IOE while closing stream", e2);
                        }
                    }
                    return;
                }
            } catch (FileNotFoundException e3) {
                Log.e(TAG, "loadDefaultXml FileNotFoundException error");
                if (in != null) {
                    in.close();
                }
            } catch (XmlPullParserException e4) {
                Log.e(TAG, "loadDefaultXml", e4);
                if (in != null) {
                    in.close();
                }
            } catch (Throwable th) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "loadDefaultWhiteListFromXml:- IOE while closing stream", e5);
                    }
                }
                throw th;
            }
        }
        if (context != null) {
            try {
                in = HwPCUtils.enabledInPad() ? context.getAssets().open(PAD_FILE_HW_SUPPORT_PC_WHITELIST) : context.getAssets().open(FILE_HW_SUPPORT_PC_WHITELIST);
            } catch (Exception e6) {
                Log.e(TAG, "loadDefaultXml", e6);
            }
        }
        if (in != null) {
        }
    }

    private void parserXML(XmlPullParser parser) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    switch (eventType) {
                        case 2:
                            if (!XML_ELEMENT_APP_ITEM.equals(parser.getName())) {
                                if (XML_ELEMENT_SPECIAL_WINDOW_POLICY_APP_ITEM.equals(parser.getName())) {
                                    String pkgName = parser.getAttributeValue(null, "packageName");
                                    int type = 1;
                                    try {
                                        type = Integer.valueOf(parser.getAttributeValue(null, "type"));
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "parserXML error", e);
                                    }
                                    if (pkgName != null) {
                                        this.mSpecailWindowPolicyAppList.add(new Pair<>(pkgName.toLowerCase(Locale.getDefault()), type));
                                    }
                                    break;
                                }
                            } else {
                                String pkgName2 = parser.getAttributeValue(null, "packageName");
                                if (pkgName2 != null) {
                                    this.mWhiteAppList.put(pkgName2.toLowerCase(Locale.getDefault()), 1);
                                }
                                break;
                            }
                            break;
                        case 3:
                            break;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e2) {
            Log.e(TAG, "parserXML", e2);
        }
    }

    private boolean isPadWifiVersion(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        return cm != null && !cm.isNetworkSupported(0);
    }

    private boolean isDemoVersion() {
        return IS_TABLET && (COUNTRY_DEMO || VENDOR_DEMO);
    }

    private void parserXMLforPad(XmlPullParser parser) {
        String currentAppGroup = "";
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    switch (eventType) {
                        case 2:
                            if (!PAD_XML_ELEMENT_APP_GROUP_NORMAL.equals(parser.getName())) {
                                if (!PAD_XML_ELEMENT_APP_GROUP_WIFI.equals(parser.getName())) {
                                    if (!XML_ELEMENT_APP_ITEM.equals(parser.getName())) {
                                        if (XML_ELEMENT_SPECIAL_WINDOW_POLICY_APP_ITEM.equals(parser.getName())) {
                                            String pkgName = parser.getAttributeValue(null, "packageName");
                                            int type = 1;
                                            try {
                                                type = Integer.valueOf(parser.getAttributeValue(null, "type"));
                                            } catch (NumberFormatException e) {
                                                HwPCUtils.log(TAG, "parserXML " + e);
                                            }
                                            if (pkgName != null) {
                                                this.mSpecailWindowPolicyAppList.add(new Pair<>(pkgName.toLowerCase(Locale.getDefault()), type));
                                            }
                                            break;
                                        }
                                    } else if (!PAD_XML_ELEMENT_APP_GROUP_WIFI.equals(currentAppGroup) || this.mPadWifiVersion) {
                                        if (PAD_XML_ELEMENT_APP_GROUP_NORMAL.equals(currentAppGroup) && this.mPadWifiVersion) {
                                            break;
                                        } else {
                                            String pkgName2 = parser.getAttributeValue(null, "packageName");
                                            if (pkgName2 != null) {
                                                this.mWhiteAppList.put(pkgName2.toLowerCase(Locale.getDefault()), 1);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            currentAppGroup = parser.getName();
                            break;
                        case 3:
                            break;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e2) {
            HwPCUtils.log(TAG, "parserXML " + e2);
        }
    }
}
