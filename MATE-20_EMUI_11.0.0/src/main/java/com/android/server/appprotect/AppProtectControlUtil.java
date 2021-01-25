package com.android.server.appprotect;

import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.pm.HwPackageManagerServiceUtils;
import com.huawei.android.app.ActivityManagerEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AppProtectControlUtil {
    private static final String TAG = "AppProtectControlUtil";
    private static final int VERSION_ITEM_MAX_LENGTH = 3;
    private static final int VERSION_MAX_NUM = 4;
    private static boolean sIsFileExist = false;
    private String curVersion;
    HashMap<String, HashSet<String>> forbidDisablehashMap;
    HashSet<String> forbidUninstallSet;
    HashSet<String> forbidUpdateSet;

    private AppProtectControlUtil() {
        this.forbidDisablehashMap = new HashMap<>();
        this.forbidUninstallSet = new HashSet<>();
        this.forbidUpdateSet = new HashSet<>();
        this.curVersion = "1.0.0.0";
        readFromXml();
        if (HwPackageManagerServiceUtils.DEBUG_FLAG) {
            Log.i(TAG, "forbidUninstallSet is " + this.forbidUninstallSet + ",forbidUpdateSet is " + this.forbidUpdateSet + ",forbidDisablehashMap is " + this.forbidDisablehashMap);
        }
    }

    private static class Holder {
        private static final AppProtectControlUtil sInstance = new AppProtectControlUtil();

        private Holder() {
        }
    }

    public static AppProtectControlUtil getInstance() {
        return Holder.sInstance;
    }

    public boolean isNeedForbidHarmfulAppDisableApp(String callingPackageName, String targetPackageName) {
        if (!sIsFileExist) {
            return false;
        }
        if (TextUtils.isEmpty(targetPackageName)) {
            Log.i(TAG, "targetPackageName is null!");
            return false;
        }
        String tmpCallingPackageName = callingPackageName;
        if (TextUtils.isEmpty(callingPackageName)) {
            tmpCallingPackageName = ActivityManagerEx.getPackageNameForPid(Binder.getCallingPid());
            Log.i(TAG, "tmpCallingPackageName is " + tmpCallingPackageName);
        }
        if (TextUtils.isEmpty(tmpCallingPackageName)) {
            Log.i(TAG, "tmpCallingPackageName is null!");
            return false;
        }
        String encryptionCallingPackageName = AppProtectEncryptionUtil.getHashCodeForPackageName(tmpCallingPackageName);
        if (TextUtils.isEmpty(encryptionCallingPackageName)) {
            Log.i(TAG, "encryptionCallingPackageName is null!");
            return false;
        }
        HashSet<String> foribdDisabletargetPkgSet = this.forbidDisablehashMap.get(encryptionCallingPackageName);
        if (foribdDisabletargetPkgSet == null) {
            Log.i(TAG, "the calling application is not configured in xml!");
            return false;
        } else if (foribdDisabletargetPkgSet.size() == 0) {
            Log.i(TAG, "protect app disable, encryptionCallingPackageName is " + encryptionCallingPackageName);
            return true;
        } else {
            Log.i(TAG, "targetPackageName: " + targetPackageName + " will not be disable!");
            return foribdDisabletargetPkgSet.contains(targetPackageName);
        }
    }

    public boolean isNeedForbidHarmfulAppUpdateApp(String packageName, String updateSource) {
        if (!sIsFileExist) {
            return false;
        }
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(updateSource)) {
            Log.i(TAG, "packageName or updateSource is null!");
            return false;
        }
        String originUpdatePackage = AppProtectUtil.getInstallerPackageName(packageName);
        if (TextUtils.isEmpty(originUpdatePackage) || !AppProtectActionConstant.ORIGIN_UPDATE_PACKAGE.equals(originUpdatePackage)) {
            Log.i(TAG, "originUpdatePackage is null or does not meet the interception conditions!");
            return false;
        }
        String encryptionInstallerPackageName = AppProtectEncryptionUtil.getHashCodeForPackageName(updateSource);
        if (TextUtils.isEmpty(encryptionInstallerPackageName)) {
            Log.i(TAG, "encryptionInstallerPackageName is null!");
            return false;
        }
        Log.i(TAG, "protect app update, encryptionInstallerPackageName is " + encryptionInstallerPackageName);
        return this.forbidUpdateSet.contains(encryptionInstallerPackageName);
    }

    public boolean isNeedForbidHarmfulAppSlientDeleteApp(String deletePackageName) {
        if (!sIsFileExist) {
            return false;
        }
        if (TextUtils.isEmpty(deletePackageName)) {
            Log.i(TAG, "deletePackageName is null!");
            return false;
        }
        String originUpdatePackage = AppProtectUtil.getInstallerPackageName(deletePackageName);
        if (TextUtils.isEmpty(originUpdatePackage) || !AppProtectActionConstant.ORIGIN_UPDATE_PACKAGE.equals(originUpdatePackage)) {
            Log.i(TAG, "originUpdatePackage is null or does not meet the interception conditions!");
            return false;
        }
        String callingPackageName = ActivityManagerEx.getPackageNameForPid(Binder.getCallingPid());
        if (TextUtils.isEmpty(callingPackageName)) {
            Log.i(TAG, "callingPackageName is null!");
            return false;
        }
        String encryptionCallingPackageName = AppProtectEncryptionUtil.getHashCodeForPackageName(callingPackageName);
        if (TextUtils.isEmpty(encryptionCallingPackageName)) {
            Log.i(TAG, "encryptionCallingPackageName is null!");
            return false;
        }
        Log.i(TAG, "protect app delete, encryptionCallingPackageName is " + encryptionCallingPackageName);
        return this.forbidUninstallSet.contains(encryptionCallingPackageName);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0072, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0077, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0078, code lost:
        r3.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007b, code lost:
        throw r5;
     */
    public final void readFromXml() {
        sIsFileExist = false;
        String[] xmlDir = HwCfgFilePolicy.getDownloadCfgFile(AppProtectActionConstant.CFG_FILE_DIR, AppProtectActionConstant.CFG_FILE_PATH);
        if (xmlDir == null || !verifyVersion(xmlDir[1]) || !AppProtectUtil.verifyFile(xmlDir[0])) {
            Log.e(TAG, "readFromXml xmlDir is null or verify file failed!");
            return;
        }
        try {
            byte[] inputData = Files.readAllBytes(Paths.get(xmlDir[0], new String[0]));
            if (!AppProtectXmlRsaSignatureVerify.verifyRsa(inputData, xmlDir[0])) {
                Log.e(TAG, "AppProtectXmlRsaSignatureVerify verifyRsa failed");
                return;
            }
            try {
                InputStream inputStream = new ByteArrayInputStream(inputData);
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(inputStream, "utf-8");
                int xmlEventType = xmlPullParser.next();
                if (!verifyVersionByXml(xmlPullParser, xmlDir[1])) {
                    Log.e(TAG, "verifyVersionByXml failed");
                    try {
                        inputStream.close();
                    } catch (FileNotFoundException | XmlPullParserException e) {
                        Log.e(TAG, "readFromXml FileNotFoundException or XmlPullParserException");
                    }
                } else {
                    readAppProtectLw(xmlPullParser, xmlEventType, xmlDir[1]);
                    inputStream.close();
                    sIsFileExist = true;
                }
            } catch (IOException e2) {
                Log.e(TAG, "readFromXml IOException");
            } catch (Exception e3) {
                Log.e(TAG, "readFromXml Exception");
            }
        } catch (IOException e4) {
            Log.e(TAG, "integrity check read to bytes IOException");
        } catch (Exception e5) {
            Log.e(TAG, "integrity check read to bytes Exception");
        }
    }

    private void readAppProtectLw(XmlPullParser xmlPullParser, int eventType, String version) throws XmlPullParserException, IOException {
        int xmlEventType = eventType;
        while (xmlEventType != 1) {
            xmlEventType = xmlPullParser.next();
            String nodeName = xmlPullParser.getName();
            if (xmlEventType == 2) {
                if (AppProtectActionConstant.APP_PROTECT_A.equals(nodeName)) {
                    readAppProtectABLw(xmlPullParser, AppProtectActionConstant.APP_PROTECT_A);
                } else if (AppProtectActionConstant.APP_PROTECT_B.equals(nodeName)) {
                    readAppProtectABLw(xmlPullParser, AppProtectActionConstant.APP_PROTECT_B);
                } else if (AppProtectActionConstant.APP_PROTECT_C.equals(nodeName)) {
                    readAppProtectCLw(xmlPullParser);
                }
                this.curVersion = version;
            }
        }
        Log.i(TAG, "readFromXml success, curVersion is " + this.curVersion);
    }

    private void readAppProtectABLw(XmlPullParser parser, String nodeName) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        HashSet<String> tmpHashSet = new HashSet<>();
        while (true) {
            int event = parser.next();
            if (event == 1 || (event == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (!(event == 3 || event == 4)) {
                String packageName = parser.getAttributeValue(null, AppProtectActionConstant.ATTR_A);
                Log.i(TAG, "readAppProtectABLw packageName is " + packageName);
                tmpHashSet.add(packageName);
            }
        }
        if (AppProtectActionConstant.APP_PROTECT_A.equals(nodeName)) {
            this.forbidUninstallSet = tmpHashSet;
        } else if (AppProtectActionConstant.APP_PROTECT_B.equals(nodeName)) {
            this.forbidUpdateSet = tmpHashSet;
        }
    }

    private void readAppProtectCLw(XmlPullParser parser) throws XmlPullParserException, IOException {
        HashSet<String> disabledHashSet;
        int outerDepth = parser.getDepth();
        HashMap<String, HashSet<String>> tempForbidDisablehashMap = new HashMap<>();
        while (true) {
            int event = parser.next();
            if (event == 1 || (event == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (!(event == 3 || event == 4)) {
                String packageName = parser.getAttributeValue(null, AppProtectActionConstant.ATTR_A);
                if (tempForbidDisablehashMap.containsKey(packageName)) {
                    disabledHashSet = tempForbidDisablehashMap.get(packageName);
                } else {
                    disabledHashSet = new HashSet<>();
                    tempForbidDisablehashMap.put(packageName, disabledHashSet);
                }
                readTargetPkgLw(parser, disabledHashSet);
            }
        }
        this.forbidDisablehashMap = tempForbidDisablehashMap;
    }

    private void readTargetPkgLw(XmlPullParser parser, HashSet<String> disbledHashSet) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int event = parser.next();
            if (event == 1) {
                return;
            }
            if (event == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(event == 3 || event == 4 || !AppProtectActionConstant.NODE_TARGET_PACKGE_NAME.equals(parser.getName()))) {
                disbledHashSet.add(parser.getAttributeValue(null, "value"));
            }
        }
    }

    private boolean verifyVersion(String newVersion) {
        String curVersionInfo = changeVersionToLongStr(this.curVersion);
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

    private boolean verifyVersionByXml(XmlPullParser xmlParser, String txtVersion) {
        if (TextUtils.isEmpty(txtVersion)) {
            Log.e(TAG, "version.txt is not exsit!");
            return false;
        } else if (!AppProtectActionConstant.APP_PROTECT_ROOT.equals(xmlParser.getName())) {
            Log.e(TAG, "check nodeName failed, the first nodeName is not root");
            return false;
        } else {
            String version = xmlParser.getAttributeValue(null, AppActConstant.VERSION);
            if (txtVersion.equals(version)) {
                return true;
            }
            Log.e(TAG, "check version failed, txtVersion: " + txtVersion + " version: " + version);
            return false;
        }
    }
}
