package com.android.server.appactcontrol;

import android.content.ComponentName;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public class ComponentHiddenScenes implements IAppActScenes {
    private static final String CLASSNAME_SYMBOL_FOR_PARSING = "../";
    private static final String PREINSTALLED_APK_FILE_ATTR = "name";
    private static final String PREINSTALLED_APK_FILE_NODE = "string";
    private static final String PREINSTALLED_APK_LIST_DIR = "/data/system/";
    private static final String PREINSTALLED_APK_LIST_FILE = "preinstalled_app_list_file.xml";
    private static final String STRING_EMPTY = "";
    private static final String TAG = "ComponentHiddenScenes";
    private HashMap<String, HashSet<ComponentName>> componentDefaultMap;
    private HashMap<String, HashSet<ComponentName>> componentDisableMap;
    private final Object lock;
    private HashMap<String, HashSet<String>> xmlDefaultMap;
    private HashMap<String, HashSet<String>> xmlDisableMap;

    private static class ComponentHiddenScenesSingleton {
        private static final ComponentHiddenScenes INSTANCE = new ComponentHiddenScenes();

        private ComponentHiddenScenesSingleton() {
        }
    }

    private ComponentHiddenScenes() {
        this.xmlDisableMap = new HashMap<>();
        this.xmlDefaultMap = new HashMap<>();
        this.componentDisableMap = new HashMap<>();
        this.componentDefaultMap = new HashMap<>();
        this.lock = new Object();
    }

    static ComponentHiddenScenes getInstance() {
        return ComponentHiddenScenesSingleton.INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public HashMap<String, HashSet<ComponentName>> getComponentDisableMap() {
        return this.componentDisableMap;
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public boolean isNeedForbidAppAct(String pkgName, String className, HashMap<String, String> hashMap) {
        return false;
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public void readXmlDataByScenes(int xmlEventType, XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        String nodeName = xmlParser.getName();
        HashMap<String, HashSet<String>> tmpDisableMap = new HashMap<>();
        HashMap<String, HashSet<String>> tmpDefaultMap = new HashMap<>();
        while (true) {
            if (xmlEventType != 3 || "item".equals(nodeName)) {
                xmlEventType = xmlParser.next();
                nodeName = xmlParser.getName();
                if (xmlEventType == 2 && "item".equals(nodeName)) {
                    String pkgName = xmlParser.getAttributeValue(null, AppActConstant.ATTR_PACKAGE_NAME);
                    String className = xmlParser.getAttributeValue(null, AppActConstant.ATTR_CLASS_NAME);
                    String value = xmlParser.getAttributeValue(null, "value");
                    char c = 65535;
                    int hashCode = value.hashCode();
                    if (hashCode != 1544803905) {
                        if (hashCode == 1671308008 && value.equals(AppActConstant.VALUE_DISABLE)) {
                            c = 1;
                        }
                    } else if (value.equals(AppActConstant.VALUE_DEFAULT)) {
                        c = 0;
                    }
                    if (c == 0) {
                        updateMap(tmpDefaultMap, pkgName, className);
                    } else if (c == 1) {
                        updateMap(tmpDisableMap, pkgName, className);
                    }
                }
            } else {
                synchronized (this.lock) {
                    this.xmlDisableMap = tmpDisableMap;
                    this.xmlDefaultMap = tmpDefaultMap;
                }
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setComponentHiddenState() {
        synchronized (this.lock) {
            initComponentMap();
            for (HashSet<ComponentName> defaultSet : this.componentDefaultMap.values()) {
                AppActUtils.setComponentState(defaultSet, false);
            }
            for (HashSet<ComponentName> disableSet : this.componentDisableMap.values()) {
                AppActUtils.setComponentState(disableSet, true);
            }
        }
    }

    private void updateMap(HashMap<String, HashSet<String>> stateMap, String pkgName, String className) {
        HashSet<String> stateSet;
        if (stateMap.containsKey(pkgName)) {
            stateSet = stateMap.get(pkgName);
        } else {
            stateSet = new HashSet<>();
            stateMap.put(pkgName, stateSet);
        }
        stateSet.add(className);
    }

    private void initComponentMap() {
        HashMap<String, HashSet<ComponentName>> tmpComponentDisableMap = new HashMap<>();
        HashMap<String, HashSet<ComponentName>> tmpComponentDefaultMap = new HashMap<>();
        for (String realPkgName : getPreinstalledPkgNameList()) {
            String realPkgNameHashCode = AppActUtils.getHashCodeForString(realPkgName);
            if (this.xmlDisableMap.containsKey(realPkgNameHashCode)) {
                tmpComponentDisableMap.put(realPkgName, getComponentSet(realPkgName, realPkgNameHashCode, true));
            } else if (this.xmlDefaultMap.containsKey(realPkgNameHashCode)) {
                tmpComponentDefaultMap.put(realPkgName, getComponentSet(realPkgName, realPkgNameHashCode, false));
            }
        }
        this.componentDisableMap = tmpComponentDisableMap;
        this.componentDefaultMap = tmpComponentDefaultMap;
    }

    private HashSet<ComponentName> getComponentSet(String realPkgName, String realPkgNameHashCode, boolean isDisable) {
        HashSet<ComponentName> componentNameSet = new HashSet<>();
        Iterator<String> it = (isDisable ? this.xmlDisableMap : this.xmlDefaultMap).get(realPkgNameHashCode).iterator();
        while (it.hasNext()) {
            String realClassName = getRealClassName(realPkgName, it.next());
            if (!TextUtils.isEmpty(realClassName)) {
                componentNameSet.add(new ComponentName(realPkgName, realClassName));
            }
        }
        return componentNameSet;
    }

    private String getRealClassName(String realPkgName, String xmlClassName) {
        String convertXmlClassName = xmlClassName;
        String convertPkgName = realPkgName;
        while (convertXmlClassName.startsWith(CLASSNAME_SYMBOL_FOR_PARSING)) {
            convertXmlClassName = convertXmlClassName.replaceFirst(CLASSNAME_SYMBOL_FOR_PARSING, "");
            if (convertPkgName.lastIndexOf(".") == -1) {
                Log.e(TAG, "getRealClassName error, convertPkgName or convertXmlClassName is invalid");
                return "";
            }
            convertPkgName = convertPkgName.substring(0, convertPkgName.lastIndexOf("."));
        }
        return convertPkgName + convertXmlClassName;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0051, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0056, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0057, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005a, code lost:
        throw r5;
     */
    private List<String> getPreinstalledPkgNameList() {
        List<String> preinstalledPkgNameList = new ArrayList<>();
        try {
            FileInputStream stream = new FileInputStream(new File(PREINSTALLED_APK_LIST_DIR, PREINSTALLED_APK_LIST_FILE));
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(stream, "utf-8");
            int xmlEventType = xmlParser.next();
            while (xmlEventType != 1) {
                xmlEventType = xmlParser.next();
                if (xmlEventType == 2) {
                    String nodeName = xmlParser.getName();
                    if (xmlEventType == 2) {
                        if (PREINSTALLED_APK_FILE_NODE.equals(nodeName)) {
                            preinstalledPkgNameList.add(xmlParser.getAttributeValue(null, "name"));
                        }
                    }
                }
            }
            stream.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "pre-installed apk file is not exist.");
        } catch (XmlPullParserException e2) {
            Log.e(TAG, "XmlPullParserException. parsing pre-installed apk file.");
        } catch (IOException e3) {
            Log.e(TAG, "IOException. parsing pre-installed apk file.");
        } catch (Exception e4) {
            Log.e(TAG, "Exception. parsing pre-installed apk file.");
        }
        return preinstalledPkgNameList;
    }
}
