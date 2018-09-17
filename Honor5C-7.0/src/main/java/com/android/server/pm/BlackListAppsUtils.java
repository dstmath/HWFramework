package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import android.content.pm.Signature;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.display.Utils;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class BlackListAppsUtils {
    private static final String APP_BLACKLIST_FILE_NAME = "app_blacklist.xml";
    private static final String DISABLED_APP_FILE_NAME = "disabled_app.xml";
    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private static final String TAG = "BlackListAppsUtils";

    public static void writeToXml(XmlSerializer out, BlackListApp app) throws IllegalArgumentException, IllegalStateException, IOException {
        boolean versionIsValid = false;
        if (out != null && app != null) {
            out.startTag(null, "app");
            out.startTag(null, PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
            out.text(app.mPackageName);
            out.endTag(null, PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
            Signature[] signs = app.mSignature;
            if (!(signs == null || signs.length == 0)) {
                out.startTag(null, "signature");
                StringBuilder signsString = new StringBuilder();
                for (Signature sigStr : signs) {
                    if (!TextUtils.isEmpty(signsString)) {
                        signsString.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    }
                    signsString.append(sigStr.toCharsString());
                }
                out.text(signsString.toString());
                out.endTag(null, "signature");
            }
            if ((app.mMinVersionId | app.mMaxVersionId) != 0) {
                versionIsValid = true;
            }
            if (versionIsValid) {
                out.startTag(null, "versionIDs");
                out.startTag(null, "versionIDStart");
                out.text(String.valueOf(app.mMinVersionId));
                out.endTag(null, "versionIDStart");
                out.startTag(null, "versionIDEnd");
                out.text(String.valueOf(app.mMaxVersionId));
                out.endTag(null, "versionIDEnd");
                out.endTag(null, "versionIDs");
            }
            if (!TextUtils.isEmpty(app.mHashValue)) {
                out.startTag(null, "hashValue");
                out.text(app.mHashValue);
                out.endTag(null, "hashValue");
            }
            out.endTag(null, "app");
        }
    }

    public static boolean writeBlackListToXml(BlackListInfo disableApp) {
        Throwable th;
        if (disableApp == null) {
            return false;
        }
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fis = new FileOutputStream(Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", DISABLED_APP_FILE_NAME}), false);
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fis, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "blacklist");
                XmlUtils.writeIntAttribute(out, "version", disableApp.mVersionCode);
                for (BlackListApp app : disableApp.mBlackList) {
                    writeToXml(out, app);
                }
                out.endTag(null, "blacklist");
                out.endDocument();
                fis.flush();
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "close OutputStream failed when write blacklist file");
                    }
                }
                return true;
            } catch (IOException e2) {
                fileOutputStream = fis;
                try {
                    Slog.e(TAG, "write disabled app file failed due to IOException");
                    deleteDisableAppListFile();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                            Slog.e(TAG, "close OutputStream failed when write blacklist file");
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e4) {
                            Slog.e(TAG, "close OutputStream failed when write blacklist file");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fis;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            Slog.e(TAG, "write disabled app file failed due to IOException");
            deleteDisableAppListFile();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        }
    }

    public static boolean deleteDisableAppListFile() {
        File file = Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", DISABLED_APP_FILE_NAME});
        if (file == null || !file.exists()) {
            return false;
        }
        return file.delete();
    }

    public static void readDisableAppList(BlackListInfo info) {
        readBlackListFromXml(info, Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", DISABLED_APP_FILE_NAME}));
    }

    public static void readBlackList(BlackListInfo info) {
        readBlackListFromXml(info, Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc", "xml", APP_BLACKLIST_FILE_NAME}));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void readBlackListFromXml(BlackListInfo info, File file) {
        Throwable th;
        if (info != null && file != null && file.exists()) {
            FileInputStream fileInputStream = null;
            ArrayList<BlackListApp> blacklist = new ArrayList();
            int version = -1;
            boolean parserError = false;
            try {
                FileInputStream fis = new FileInputStream(file);
                try {
                    XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
                    xpp.setInput(fis, "UTF-8");
                    int type = xpp.getEventType();
                    BlackListApp app = null;
                    while (type != 1) {
                        BlackListApp app2;
                        switch (type) {
                            case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                                app2 = app;
                                break;
                            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                                try {
                                    String name = xpp.getName();
                                    if (!TextUtils.equals("blacklist", name)) {
                                        if (!TextUtils.equals("app", name)) {
                                            if (TextUtils.equals(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, name) && app != null) {
                                                app.mPackageName = xpp.nextText();
                                                app2 = app;
                                                break;
                                            }
                                            if (TextUtils.equals("signature", name) && app != null) {
                                                try {
                                                    app.setSignature(xpp.nextText().split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER));
                                                    app2 = app;
                                                    break;
                                                } catch (Exception e) {
                                                    parserError = true;
                                                    app2 = app;
                                                    break;
                                                }
                                            }
                                            if (TextUtils.equals("versionIDStart", name) && app != null) {
                                                try {
                                                    app.mMinVersionId = Integer.parseInt(xpp.nextText());
                                                    app2 = app;
                                                    break;
                                                } catch (NumberFormatException e2) {
                                                    parserError = true;
                                                    app2 = app;
                                                    break;
                                                }
                                            }
                                            if (TextUtils.equals("versionIDEnd", name) && app != null) {
                                                try {
                                                    app.mMaxVersionId = Integer.parseInt(xpp.nextText());
                                                    app2 = app;
                                                    break;
                                                } catch (NumberFormatException e3) {
                                                    parserError = true;
                                                    app2 = app;
                                                    break;
                                                }
                                            }
                                            if (TextUtils.equals("hashValue", name) && app != null) {
                                                app.mHashValue = xpp.nextText();
                                                app2 = app;
                                                break;
                                            }
                                            app2 = app;
                                            break;
                                        }
                                        app2 = new BlackListApp();
                                        break;
                                    }
                                    try {
                                        version = Integer.parseInt(xpp.getAttributeValue(0));
                                        app2 = app;
                                        break;
                                    } catch (NumberFormatException e4) {
                                        parserError = true;
                                        app2 = app;
                                        break;
                                    }
                                } catch (IOException e5) {
                                    app2 = app;
                                    fileInputStream = fis;
                                    break;
                                } catch (XmlPullParserException e6) {
                                    app2 = app;
                                    fileInputStream = fis;
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                    fileInputStream = fis;
                                    break;
                                }
                                break;
                            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                                if (!TextUtils.equals("app", xpp.getName())) {
                                    app2 = app;
                                    break;
                                }
                                blacklist.add(app);
                                app2 = null;
                                break;
                            default:
                                app2 = app;
                                break;
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e7) {
                            Slog.e(TAG, "close InputStream failed when read blacklist file");
                        }
                    }
                    fileInputStream = fis;
                } catch (IOException e8) {
                    fileInputStream = fis;
                } catch (XmlPullParserException e9) {
                    fileInputStream = fis;
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = fis;
                }
            } catch (IOException e10) {
                Slog.e(TAG, "read blacklist file failed due to IOException");
                parserError = true;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e11) {
                        Slog.e(TAG, "close InputStream failed when read blacklist file");
                    }
                }
                info.mBlackList.clear();
                for (BlackListApp blacklistApp : blacklist) {
                    info.mBlackList.add(blacklistApp);
                }
                info.mVersionCode = version;
            } catch (XmlPullParserException e12) {
                try {
                    Slog.e(TAG, "read blacklist file failed due to XmlPullParserException");
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e13) {
                            Slog.e(TAG, "close InputStream failed when read blacklist file");
                        }
                    }
                    info.mBlackList.clear();
                    for (BlackListApp blacklistApp2 : blacklist) {
                        info.mBlackList.add(blacklistApp2);
                    }
                    info.mVersionCode = version;
                } catch (Throwable th4) {
                    th = th4;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e14) {
                            Slog.e(TAG, "close InputStream failed when read blacklist file");
                        }
                    }
                    throw th;
                }
            }
            if (checkAppInfo(blacklist) && version >= 0 && !parserError) {
                info.mBlackList.clear();
                for (BlackListApp blacklistApp22 : blacklist) {
                    info.mBlackList.add(blacklistApp22);
                }
                info.mVersionCode = version;
            }
        }
    }

    public static boolean comparePackage(Package info, BlackListApp app) {
        if (info == null || app == null || !TextUtils.equals(info.packageName, app.mPackageName)) {
            return false;
        }
        boolean versionIsValid;
        int versionStart = app.mMinVersionId;
        int versionEnd = app.mMaxVersionId;
        if ((versionStart | versionEnd) != 0) {
            versionIsValid = true;
        } else {
            versionIsValid = false;
        }
        if (versionIsValid && (info.mVersionCode < versionStart || info.mVersionCode > versionEnd)) {
            return false;
        }
        if (app.mSignature != null && PackageManagerService.compareSignatures(info.mSignatures, app.mSignature) != 0) {
            return false;
        }
        if (!TextUtils.isEmpty(app.mHashValue)) {
            if (!TextUtils.equals(app.mHashValue, getSHA256(new File(info.applicationInfo.sourceDir)))) {
                return false;
            }
        }
        Slog.d(TAG, "find blacklist apk : " + app.mPackageName);
        return true;
    }

    private static String getSHA256(File file) {
        byte[] manifest = getManifestFile(file);
        if (manifest.length == 0) {
            return null;
        }
        return sha256(manifest);
    }

    private static byte[] getManifestFile(File apkFile) {
        Throwable th;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] b = new byte[HwGlobalActionsData.FLAG_SILENTMODE_NORMAL];
        ZipFile zipFile = null;
        boolean catchFlag = false;
        InputStream zipInputStream = null;
        try {
            ZipFile zipFile2 = new ZipFile(apkFile);
            try {
                byte[] toByteArray;
                zipInputStream = zipFile2.getInputStream(zipFile2.getEntry(MANIFEST_NAME));
                if (zipInputStream != null) {
                    while (true) {
                        int length = zipInputStream.read(b);
                        if (length <= 0) {
                            break;
                        }
                        os.write(b, 0, length);
                    }
                    catchFlag = true;
                }
                if (zipInputStream != null) {
                    try {
                        zipInputStream.close();
                    } catch (IOException e) {
                    }
                }
                if (zipFile2 != null) {
                    try {
                        zipFile2.close();
                    } catch (IOException e2) {
                    }
                }
                if (catchFlag) {
                    toByteArray = os.toByteArray();
                } else {
                    toByteArray = new byte[0];
                }
                return toByteArray;
            } catch (IOException e3) {
                zipFile = zipFile2;
                try {
                    Slog.e(TAG, " get manifest file failed due to IOException");
                    if (zipInputStream != null) {
                        try {
                            zipInputStream.close();
                        } catch (IOException e4) {
                        }
                    }
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e5) {
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (zipInputStream != null) {
                        try {
                            zipInputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e7) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                zipFile = zipFile2;
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
                throw th;
            }
        } catch (IOException e8) {
            Slog.e(TAG, " get manifest file failed due to IOException");
            if (zipInputStream != null) {
                zipInputStream.close();
            }
            if (zipFile != null) {
                zipFile.close();
            }
            return null;
        }
    }

    public static String sha256(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return bytesToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            Slog.e(TAG, "get sha256 failed");
            return null;
        }
    }

    private static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] hexChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int byteValue = bytes[j] & Utils.MAXINUM_TEMPERATURE;
            chars[j * 2] = hexChars[byteValue >>> 4];
            chars[(j * 2) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars);
    }

    public static boolean isBlackListUpdate(BlackListInfo blacklist, BlackListInfo disableApp) {
        int blacklistVersion = blacklist.mVersionCode;
        int disableAppVersion = disableApp.mVersionCode;
        if (disableAppVersion == -1) {
            Slog.d(TAG, "blacklist update, version = " + blacklistVersion);
            return true;
        } else if (blacklistVersion < disableAppVersion) {
            Slog.d(TAG, "blacklist downgrade: from " + disableAppVersion + " to " + blacklistVersion);
            return true;
        } else if (blacklistVersion > disableAppVersion) {
            Slog.d(TAG, "blacklist upgrade: from " + disableAppVersion + " to " + blacklistVersion);
            return true;
        } else {
            Slog.d(TAG, "blacklist version = " + blacklistVersion);
            return false;
        }
    }

    private static boolean checkAppInfo(ArrayList<BlackListApp> blacklist) {
        if (blacklist == null) {
            return false;
        }
        for (BlackListApp app : blacklist) {
            if (TextUtils.isEmpty(app.mPackageName)) {
                return false;
            }
            int count = 0;
            if (app.mSignature != null && app.mSignature.length > 0) {
                count = 1;
            }
            if (app.mMaxVersionId >= 0 && app.mMinVersionId >= 0 && app.mMaxVersionId >= app.mMinVersionId && (app.mMinVersionId | app.mMaxVersionId) != 0) {
                count++;
            }
            if (!TextUtils.isEmpty(app.mHashValue)) {
                count++;
                continue;
            }
            if (count < 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsApp(ArrayList<BlackListApp> blacklist, BlackListApp app) {
        if (blacklist == null || app == null) {
            return false;
        }
        for (BlackListApp blacklistApp : blacklist) {
            if (TextUtils.equals(blacklistApp.mPackageName, app.mPackageName) && Arrays.equals(blacklistApp.mSignature, app.mSignature) && app.mMinVersionId == blacklistApp.mMinVersionId && app.mMaxVersionId == blacklistApp.mMaxVersionId && TextUtils.equals(app.mHashValue, blacklistApp.mHashValue)) {
                return true;
            }
        }
        return false;
    }
}
