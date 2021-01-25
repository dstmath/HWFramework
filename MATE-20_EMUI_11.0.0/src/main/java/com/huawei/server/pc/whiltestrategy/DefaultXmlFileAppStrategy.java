package com.huawei.server.pc.whiltestrategy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;
import com.huawei.android.net.ConnectivityManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwpartpowerofficeservices.BuildConfig;
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
    private static final String FILE_HW_SUPPORT_PC_WHITELIST = "hw_pc_white_apps.xml";
    private static final String FILE_POLICY_CLASS_NAME = "com.huawei.cust.HwCfgFilePolicy";
    private static final boolean IS_19_VERSION = SystemPropertiesEx.get("ro.product.model", BuildConfig.FLAVOR).contains("19");
    private static final boolean IS_COUNTRY_DEMO = "demo".equalsIgnoreCase(SystemPropertiesEx.get("ro.hw.country", BuildConfig.FLAVOR));
    private static final boolean IS_TABLET = "tablet".equals(SystemPropertiesEx.get("ro.build.characteristics", BuildConfig.FLAVOR));
    private static final boolean IS_VENDOR_DEMO = "demo".equalsIgnoreCase(SystemPropertiesEx.get("ro.hw.vendor", BuildConfig.FLAVOR));
    private static final String METHOD_NAME_FOR_FILE = "getCfgFile";
    private static final String PAD_FILE_HW_SUPPORT_PC_WHITELIST = "hw_pc_white_apps_pad.xml";
    private static final String PAD_XML_ELEMENT_APP_GROUP_NORMAL = "hw_support_pc_apps";
    private static final String PAD_XML_ELEMENT_APP_GROUP_WIFI = "hw_support_pc_apps_wifi";
    private static final int SPECIAL_PACKAGE_TYPE_PAD_FULLSCREEN = 6;
    private static final String TAG = "DefaultXmlFileAppStrategy";
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "packageName";
    private static final String XML_ATTRIBUTE_TYPE = "type";
    private static final String XML_ELEMENT_APP_ITEM = "hw_support_pc_app";
    private static final String XML_ELEMENT_SPECIAL_WINDOW_POLICY_APP_ITEM = "hw_special_window_policy_app";
    private boolean isPadWifiVersion = false;
    List<Pair<String, Integer>> mSpecailWindowPolicyAppList = null;
    Map<String, Integer> mWhiteAppList = null;

    public DefaultXmlFileAppStrategy(Context context) {
        loadDefaultWhiteListFromXml(context);
    }

    public List<Pair<String, Integer>> getSpecailWindowPolicyAppList() {
        return this.mSpecailWindowPolicyAppList;
    }

    @Override // com.huawei.server.pc.whiltestrategy.AppStrategy
    public Map<String, Integer> getAppList(Context context) {
        return this.mWhiteAppList;
    }

    @Override // com.huawei.server.pc.whiltestrategy.AppStrategy
    public int getAppState(String packageName, Context context) {
        Map<String, Integer> map;
        if (packageName == null || (map = this.mWhiteAppList) == null || !map.containsKey(packageName)) {
            return -1;
        }
        return 1;
    }

    private static File getCfgFile(String fileName, int type) throws Exception, NoClassDefFoundError {
        Class<?> filePolicyClazz = Class.forName(FILE_POLICY_CLASS_NAME);
        return (File) filePolicyClazz.getMethod(METHOD_NAME_FOR_FILE, String.class, Integer.TYPE).invoke(filePolicyClazz, fileName, Integer.valueOf(type));
    }

    private static File getCustomizedFileName(String xmlName, int flag) {
        try {
            return getCfgFile("xml/" + xmlName, flag);
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "HwCfgFilePolicy Exception");
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0062  */
    private void loadDefaultWhiteListFromXml(Context context) {
        File path;
        if (HwPCUtils.enabledInPad()) {
            path = getCustomizedFileName(PAD_FILE_HW_SUPPORT_PC_WHITELIST, 0);
        } else {
            path = getCustomizedFileName(FILE_HW_SUPPORT_PC_WHITELIST, 0);
        }
        Log.i(TAG, "loadDefaultWhiteListFromXml");
        InputStream in = null;
        if (path != null) {
            try {
                if (path.exists()) {
                    in = new FileInputStream(path);
                    if (in == null) {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(in, null);
                        this.mWhiteAppList = new LinkedHashMap();
                        this.mSpecailWindowPolicyAppList = new ArrayList();
                        if (HwPCUtils.enabledInPad()) {
                            this.isPadWifiVersion = isPadWifiVersion(context);
                            parserXmlforPad(parser);
                            if (isDemoVersion()) {
                                parseXmlForPadInDemoVersion();
                            }
                        } else {
                            parserXml(parser);
                        }
                        try {
                            in.close();
                            return;
                        } catch (IOException e) {
                            Log.e(TAG, "loadDefaultWhiteListFromXml:- IOE while closing stream");
                            return;
                        }
                    } else if (in != null) {
                        try {
                            in.close();
                            return;
                        } catch (IOException e2) {
                            Log.e(TAG, "loadDefaultWhiteListFromXml:- IOE while closing stream");
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } catch (FileNotFoundException e3) {
                Log.e(TAG, "loadDefaultXml FileNotFoundException error");
                if (0 != 0) {
                    in.close();
                    return;
                }
                return;
            } catch (XmlPullParserException e4) {
                Log.e(TAG, "loadDefaultXml XmlPullParserException");
                if (0 != 0) {
                    in.close();
                    return;
                }
                return;
            } catch (IOException e5) {
                Log.e(TAG, "loadDefaultXml IOException");
                if (0 != 0) {
                    in.close();
                    return;
                }
                return;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e6) {
                        Log.e(TAG, "loadDefaultWhiteListFromXml:- IOE while closing stream");
                    }
                }
                throw th;
            }
        }
        if (context != null) {
            if (HwPCUtils.enabledInPad()) {
                in = context.getAssets().open(PAD_FILE_HW_SUPPORT_PC_WHITELIST);
            } else {
                in = context.getAssets().open(FILE_HW_SUPPORT_PC_WHITELIST);
            }
        }
        if (in == null) {
        }
    }

    private void parseXmlForPadInDemoVersion() {
        this.mWhiteAppList.put("com.gameloft.android.GAND.GloftA8HU", 1);
        List<Pair<String, Integer>> list = this.mSpecailWindowPolicyAppList;
        Integer valueOf = Integer.valueOf((int) SPECIAL_PACKAGE_TYPE_PAD_FULLSCREEN);
        list.add(new Pair<>("com.gameloft.android.GAND.GloftA8HU", valueOf));
        this.mWhiteAppList.put("com.huawei.experience.toprand.cs.en", 1);
        this.mSpecailWindowPolicyAppList.add(new Pair<>("com.huawei.experience.toprand.cs.en", valueOf));
        if (IS_VENDOR_DEMO) {
            this.mWhiteAppList.put("com.huawei.experience.toprand.cs", 1);
            this.mSpecailWindowPolicyAppList.add(new Pair<>("com.huawei.experience.toprand.cs", valueOf));
        }
        this.mWhiteAppList.put("com.huawei.retaildemo", 1);
        this.mSpecailWindowPolicyAppList.add(new Pair<>("com.huawei.retaildemo", valueOf));
        if (IS_19_VERSION) {
            this.mWhiteAppList.put("com.adsk.sketchbook", 1);
            this.mSpecailWindowPolicyAppList.add(new Pair<>("com.adsk.sketchbook", valueOf));
            this.mWhiteAppList.put("com.myscript.nebo.huawei", 1);
            this.mSpecailWindowPolicyAppList.add(new Pair<>("com.myscript.nebo.huawei", valueOf));
        }
    }

    private void parserXml(XmlPullParser parser) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType != 2) {
                        if (eventType != 3) {
                        }
                    } else if (XML_ELEMENT_APP_ITEM.equals(parser.getName())) {
                        String pkgName = parser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                        if (pkgName != null) {
                            this.mWhiteAppList.put(pkgName.toLowerCase(Locale.ROOT), 1);
                        }
                    } else if (XML_ELEMENT_SPECIAL_WINDOW_POLICY_APP_ITEM.equals(parser.getName())) {
                        String pkgName2 = parser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                        int type = 1;
                        try {
                            type = Integer.valueOf(parser.getAttributeValue(null, XML_ATTRIBUTE_TYPE));
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "parserXml occurs numberformat error");
                        }
                        if (pkgName2 != null) {
                            this.mSpecailWindowPolicyAppList.add(new Pair<>(pkgName2.toLowerCase(Locale.ROOT), type));
                        }
                    } else {
                        Log.e(TAG, "parserXml getName maybe has other type");
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e2) {
            Log.e(TAG, "parserXml Exception");
        } catch (IOException e3) {
            Log.e(TAG, "parserXml IOException");
        }
    }

    private boolean isPadWifiVersion(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        return cm != null && !ConnectivityManagerEx.isNetworkSupported(0, cm);
    }

    private boolean isDemoVersion() {
        return IS_TABLET && (IS_COUNTRY_DEMO || IS_VENDOR_DEMO);
    }

    private void parserXmlforPad(XmlPullParser parser) {
        String pkgName;
        String currentAppGroup = BuildConfig.FLAVOR;
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        if (!PAD_XML_ELEMENT_APP_GROUP_NORMAL.equals(parser.getName())) {
                            if (!PAD_XML_ELEMENT_APP_GROUP_WIFI.equals(parser.getName())) {
                                if (XML_ELEMENT_APP_ITEM.equals(parser.getName())) {
                                    if ((!PAD_XML_ELEMENT_APP_GROUP_WIFI.equals(currentAppGroup) || this.isPadWifiVersion) && ((!PAD_XML_ELEMENT_APP_GROUP_NORMAL.equals(currentAppGroup) || !this.isPadWifiVersion) && (pkgName = parser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME)) != null)) {
                                        this.mWhiteAppList.put(pkgName.toLowerCase(Locale.ROOT), 1);
                                    }
                                } else if (XML_ELEMENT_SPECIAL_WINDOW_POLICY_APP_ITEM.equals(parser.getName())) {
                                    String pkgName2 = parser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                                    int type = 1;
                                    try {
                                        type = Integer.valueOf(parser.getAttributeValue(null, XML_ATTRIBUTE_TYPE));
                                    } catch (NumberFormatException e) {
                                        HwPCUtils.log(TAG, "parserXml for pad occures numberformat exception");
                                    }
                                    if (pkgName2 != null) {
                                        this.mSpecailWindowPolicyAppList.add(new Pair<>(pkgName2.toLowerCase(Locale.ROOT), type));
                                    }
                                } else {
                                    Log.e(TAG, "parserXml for pad getName maybe has other type");
                                }
                            }
                        }
                        currentAppGroup = parser.getName();
                    } else if (eventType != 3) {
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e2) {
            HwPCUtils.log(TAG, "parserXmlforPad  Exception");
        } catch (IOException e3) {
            HwPCUtils.log(TAG, "parserXmlforPad IOException");
        }
    }
}
