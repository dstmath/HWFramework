package com.android.server.appactcontrol;

import android.util.Log;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwAppActController {
    private static final String TAG = "HwAppActController";
    private static final int VERSION_ITEM_MAX_LENGTH = 3;
    private static final int VERSION_MAX_NUM = 4;
    private static HwAppActController instance = new HwAppActController();
    private HashMap<String, IAppActScenes> mAppActScenesMap;
    private String mCurVersion = "1.0.0.0";

    private HwAppActController() {
        updateAppActControllerData();
    }

    public static HwAppActController getInstance() {
        return instance;
    }

    public boolean isNeedForbidAppAct(String scenes, String pkgName, String className, HashMap<String, String> extra) {
        HashMap<String, IAppActScenes> hashMap = this.mAppActScenesMap;
        if (hashMap == null || hashMap.size() == 0) {
            return false;
        }
        IAppActScenes appActScenes = this.mAppActScenesMap.get(scenes);
        if (appActScenes != null) {
            return appActScenes.isNeedForbidAppAct(pkgName, className, extra);
        }
        Log.e(TAG, "isNeedForbidAppAct the appActScenes is null, invalid scenes");
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateAppActControllerDataByOuc() {
        updateAppActControllerData();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0085, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x008a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008b, code lost:
        r2.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008e, code lost:
        throw r5;
     */
    private void updateAppActControllerData() {
        String[] xmlDir = HwCfgFilePolicy.getDownloadCfgFile(AppActConstant.CFG_FILE_DIR, AppActConstant.CFG_FILE_PATH);
        if (xmlDir == null || !verifyVersion(xmlDir[1]) || !AppActUtils.verifyFile(xmlDir[0])) {
            Log.i(TAG, "updateAppActControllerData xmlDir is null or verify failed");
            return;
        }
        try {
            byte[] inputData = Files.readAllBytes(Paths.get(xmlDir[0], new String[0]));
            if (!AppActXmlRsaSignatureVerify.verifyRsa(inputData, xmlDir[0])) {
                Log.e(TAG, "AppActXmlRsaSignatureVerify verifyRsa failed");
                return;
            }
            initAppActScenesMap();
            try {
                InputStream inputStream = new ByteArrayInputStream(inputData);
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream, "utf-8");
                int xmlEventType = xmlParser.next();
                if (!verifyVersionByXml(xmlParser, xmlDir[1])) {
                    Log.e(TAG, "verifyVersionByXml failed");
                    inputStream.close();
                    return;
                }
                while (xmlEventType != 1) {
                    xmlEventType = xmlParser.next();
                    if (xmlEventType == 2) {
                        readAppActData(xmlEventType, xmlParser);
                    }
                }
                this.mCurVersion = xmlDir[1];
                Log.i(TAG, "updateAppActControllerData success");
                inputStream.close();
            } catch (XmlPullParserException e) {
                Log.e(TAG, "updateAppActControllerData XmlPullParserException");
                this.mAppActScenesMap.clear();
            } catch (IOException e2) {
                Log.e(TAG, "updateAppActControllerData IOException");
                this.mAppActScenesMap.clear();
            } catch (Exception e3) {
                Log.e(TAG, "updateAppActControllerData Exception");
                this.mAppActScenesMap.clear();
            }
        } catch (IOException e4) {
            Log.e(TAG, "updateAppActControllerData read to bytes IOException");
        } catch (Exception e5) {
            Log.e(TAG, "updateAppActControllerData read to bytes Exception");
        }
    }

    private void readAppActData(int xmlEventType, XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        IAppActScenes appActScenes = this.mAppActScenesMap.get(xmlParser.getName());
        if (appActScenes == null) {
            Log.e(TAG, "readAppActData appActScenes is null, invalid xml node");
        } else {
            appActScenes.readXmlDataByScenes(xmlEventType, xmlParser);
        }
    }

    private void initAppActScenesMap() {
        Log.i(TAG, "initAppActScenesMap mAppActScenesMap");
        HashMap<String, IAppActScenes> hashMap = this.mAppActScenesMap;
        if (hashMap == null || hashMap.size() == 0) {
            this.mAppActScenesMap = new HashMap<>();
            this.mAppActScenesMap.put(AppActConstant.PREAS_APP_UPDATE_FORBIDDEN, new PreasAppUpdateScenes());
            this.mAppActScenesMap.put(AppActConstant.APP_UPDATE_FORBIDDEN, new AppUpdateScenes());
            this.mAppActScenesMap.put(AppActConstant.COMPONENT_HIDDEN, ComponentHiddenScenes.getInstance());
            this.mAppActScenesMap.put(AppActConstant.FORBID_ENABLE_COMPONENT, new ForbidEnableComponentScenes());
            this.mAppActScenesMap.put(AppActConstant.BAD_DIALOG_FORBIDDEN, new BadDialogForbiddenScenes());
            this.mAppActScenesMap.put(AppActConstant.PACKAGE_FORBIDDEN, new PackageForbiddenScenes());
            this.mAppActScenesMap.put(AppActConstant.HARMFUL_PACKAGE_FORBIDDEN, new HarmfulPackageForbiddenScenes());
            this.mAppActScenesMap.put(AppActConstant.NOTIFICATION_FORBIDDEN, new NotificationForbiddenScenes());
        }
    }

    private boolean verifyVersion(String newVersion) {
        String curVersionInfo = changeVersionToLongStr(this.mCurVersion);
        String newVersionInfo = changeVersionToLongStr(newVersion);
        try {
            long longCurVersion = Long.parseLong(curVersionInfo);
            long longNewVersion = Long.parseLong(newVersionInfo);
            Log.i(TAG, "curVersion " + longCurVersion + " newVersion " + longNewVersion);
            if (longNewVersion > longCurVersion) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            Log.e(TAG, "version file format is not number type.");
            return false;
        }
    }

    private String changeVersionToLongStr(String versionStr) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] versions = versionStr.split("\\.");
        if (versions.length != 4) {
            Log.e(TAG, "changeVersionToLongStr versions.length is not VERSION_MAX_NUM");
            return "1000000000";
        }
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].length() > 3) {
                Log.e(TAG, "changeVersionToLongStr versions[i].length > VERSION_ITEM_MAX_LENGTH, i is " + i);
                return "1000000000";
            }
            stringBuilder.append(String.format(Locale.ROOT, "%03d", Long.valueOf(Long.parseLong(versions[i]))));
        }
        return stringBuilder.toString();
    }

    private boolean verifyVersionByXml(XmlPullParser xmlParser, String curVersion) {
        if (!AppActConstant.APP_ACT_CONTROL.equals(xmlParser.getName())) {
            Log.e(TAG, "check nodeName failed, the first nodeName is not APP_ACT_CONTROL");
            return false;
        }
        String version = xmlParser.getAttributeValue(null, AppActConstant.VERSION);
        if (curVersion.equals(version)) {
            return true;
        }
        Log.e(TAG, "check version failed, curVersion: " + curVersion + " version: " + version);
        return false;
    }

    public boolean isAppControlPolicyExists() {
        HashMap<String, IAppActScenes> hashMap = this.mAppActScenesMap;
        return (hashMap == null || hashMap.size() == 0) ? false : true;
    }
}
