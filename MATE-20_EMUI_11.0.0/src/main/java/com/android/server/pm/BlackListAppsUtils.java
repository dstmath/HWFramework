package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.pm.BlackListInfo;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class BlackListAppsUtils {
    private static final String APP_BLACKLIST_FILE_NAME = "app_blacklist.xml";
    private static final int BYTE_TO_HEX_MASK = 255;
    private static final int BYTE_TO_HEX_MASK_LOW = 15;
    private static final int BYTE_TO_HEX_NUMBER = 2;
    private static final int BYTE_TO_HEX_OFFSET = 4;
    private static final int DEFAULT_SIZE = 10;
    private static final String DIR_ETC = "etc";
    private static final String DIR_SYSTEM = "system";
    private static final String DIR_XML = "xml";
    private static final String DISABLED_APP_FILE_NAME = "disabled_app.xml";
    private static final int INVALID_VERSION = -1;
    private static final boolean IS_HW_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int KB_BYTE_NUMBER = 1024;
    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private static final String TAG = "BlackListAppsUtils";
    private static final String TAG_APP = "app";
    private static final String TAG_BLACKLIST = "blacklist";
    private static final String TAG_HASH_VALUE = "hashValue";
    private static final String TAG_PACKAGE_NAME = "packageName";
    private static final String TAG_SIGNATURE = "signature";
    private static final String TAG_VERSION_IDS = "versionIDs";
    private static final String TAG_VERSION_ID_END = "versionIDEnd";
    private static final String TAG_VERSION_ID_START = "versionIDStart";

    private BlackListAppsUtils() {
    }

    public static void writeToXml(XmlSerializer out, BlackListInfo.BlackListApp app) throws IllegalArgumentException, IllegalStateException, IOException {
        if (!(out == null || app == null)) {
            out.startTag(null, TAG_APP);
            out.startTag(null, "packageName");
            out.text(app.getPackageName());
            out.endTag(null, "packageName");
            Signature[] signatures = app.getSignatures();
            boolean isVersionValid = false;
            if (!(signatures == null || signatures.length == 0)) {
                out.startTag(null, TAG_SIGNATURE);
                StringBuilder signsString = new StringBuilder();
                for (Signature signature : signatures) {
                    if (!TextUtils.isEmpty(signsString)) {
                        signsString.append(AwarenessInnerConstants.SEMI_COLON_KEY);
                    }
                    signsString.append(signature.toCharsString());
                }
                out.text(signsString.toString());
                out.endTag(null, TAG_SIGNATURE);
            }
            if ((app.getMinVersionId() | app.getMaxVersionId()) != 0) {
                isVersionValid = true;
            }
            if (isVersionValid) {
                out.startTag(null, TAG_VERSION_IDS);
                out.startTag(null, TAG_VERSION_ID_START);
                out.text(String.valueOf(app.getMinVersionId()));
                out.endTag(null, TAG_VERSION_ID_START);
                out.startTag(null, TAG_VERSION_ID_END);
                out.text(String.valueOf(app.getMaxVersionId()));
                out.endTag(null, TAG_VERSION_ID_END);
                out.endTag(null, TAG_VERSION_IDS);
            }
            if (!TextUtils.isEmpty(app.getHashValue())) {
                out.startTag(null, TAG_HASH_VALUE);
                out.text(app.getHashValue());
                out.endTag(null, TAG_HASH_VALUE);
            }
            out.endTag(null, TAG_APP);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0063, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0064, code lost:
        $closeResource(r0, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0067, code lost:
        throw r4;
     */
    @SuppressLint({"PreferForInArrayList"})
    public static boolean writeBlackListToXml(BlackListInfo blackListInfo) {
        if (blackListInfo == null) {
            return false;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Environment.buildPath(Environment.getDataDirectory(), new String[]{DIR_SYSTEM, DISABLED_APP_FILE_NAME}), false);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, "UTF-8");
            out.startDocument(null, true);
            out.startTag(null, TAG_BLACKLIST);
            XmlUtils.writeIntAttribute(out, AppActConstant.VERSION, blackListInfo.getVersionCode());
            ArrayList<BlackListInfo.BlackListApp> apps = blackListInfo.getBlacklistApps();
            int size = apps.size();
            for (int i = 0; i < size; i++) {
                writeToXml(out, apps.get(i));
            }
            out.endTag(null, TAG_BLACKLIST);
            out.endDocument();
            fileOutputStream.flush();
            $closeResource(null, fileOutputStream);
            return true;
        } catch (IOException e) {
            Slog.e(TAG, "write disabled app file failed due to IOException");
            deleteDisableAppListFile();
            return false;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public static boolean deleteDisableAppListFile() {
        File file = Environment.buildPath(Environment.getDataDirectory(), new String[]{DIR_SYSTEM, DISABLED_APP_FILE_NAME});
        if (file == null || !file.exists()) {
            return false;
        }
        return file.delete();
    }

    public static void readDisableAppList(BlackListInfo info) {
        readBlackListFromXml(info, Environment.buildPath(Environment.getDataDirectory(), new String[]{DIR_SYSTEM, DISABLED_APP_FILE_NAME}));
    }

    public static void readBlackList(BlackListInfo info) {
        readBlackListFromXml(info, Environment.buildPath(Environment.getRootDirectory(), new String[]{DIR_ETC, DIR_XML, APP_BLACKLIST_FILE_NAME}));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002a, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002d, code lost:
        throw r3;
     */
    @SuppressLint({"PreferForInArrayList"})
    private static void readBlackListFromXml(BlackListInfo info, File file) {
        if (info != null && file != null && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
                xmlPullParser.setInput(fileInputStream, "UTF-8");
                parseBlacklistXml(info, xmlPullParser);
                $closeResource(null, fileInputStream);
            } catch (IOException e) {
                Slog.e(TAG, "read blacklist file failed due to IOException");
            } catch (XmlPullParserException e2) {
                Slog.e(TAG, "read blacklist file failed due to XmlPullParserException");
            }
        }
    }

    private static void parseBlacklistXml(BlackListInfo info, XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        ArrayList<BlackListInfo.BlackListApp> blackListApps = new ArrayList<>(10);
        BlackListInfo.BlackListApp app = null;
        int version = -1;
        int type = xmlPullParser.getEventType();
        while (type != 1) {
            if (type != 0) {
                if (type == 2) {
                    String name = xmlPullParser.getName();
                    if (TextUtils.equals(TAG_APP, name)) {
                        app = new BlackListInfo.BlackListApp();
                    } else if (TextUtils.equals(TAG_BLACKLIST, name)) {
                        try {
                            version = Integer.parseInt(xmlPullParser.getAttributeValue(0));
                        } catch (NumberFormatException e) {
                            return;
                        }
                    } else if (handleStartTag(xmlPullParser, app)) {
                        Slog.w(TAG, "invalid start tag!");
                    } else {
                        return;
                    }
                } else if (type == 3 && TextUtils.equals(TAG_APP, xmlPullParser.getName()) && app != null) {
                    blackListApps.add(app);
                    app = null;
                }
            }
            type = xmlPullParser.next();
        }
        checkAppInfo(blackListApps, info, version);
    }

    private static boolean handleStartTag(XmlPullParser xmlPullParser, BlackListInfo.BlackListApp app) throws XmlPullParserException, IOException {
        String name = xmlPullParser.getName();
        if (TextUtils.isEmpty(name) || app == null) {
            return false;
        }
        char c = 65535;
        switch (name.hashCode()) {
            case -124266520:
                if (name.equals(TAG_VERSION_ID_END)) {
                    c = 3;
                    break;
                }
                break;
            case 300767491:
                if (name.equals(TAG_HASH_VALUE)) {
                    c = 4;
                    break;
                }
                break;
            case 852067375:
                if (name.equals(TAG_VERSION_ID_START)) {
                    c = 2;
                    break;
                }
                break;
            case 908759025:
                if (name.equals("packageName")) {
                    c = 0;
                    break;
                }
                break;
            case 1073584312:
                if (name.equals(TAG_SIGNATURE)) {
                    c = 1;
                    break;
                }
                break;
        }
        if (c == 0) {
            app.setPackageName(xmlPullParser.nextText());
            if (!TextUtils.isEmpty(app.getPackageName())) {
                app.setPackageName(app.getPackageName().intern());
            }
        } else if (c != 1) {
            if (c != 2) {
                if (c != 3) {
                    if (c == 4) {
                        app.setHashValue(xmlPullParser.nextText());
                    }
                } else if (!setMaxVersionId(xmlPullParser.nextText(), app)) {
                    return false;
                }
            } else if (!setMinVersionId(xmlPullParser.nextText(), app)) {
                return false;
            }
        } else if (!setSignatures(xmlPullParser.nextText(), app)) {
            return false;
        }
        return true;
    }

    private static boolean setSignatures(String signsString, BlackListInfo.BlackListApp app) {
        try {
            app.setSignatures(signsString.split(AwarenessInnerConstants.SEMI_COLON_KEY));
            return true;
        } catch (PatternSyntaxException e) {
            Slog.e(TAG, "setSignatures syntax error!");
            return false;
        }
    }

    private static boolean setMinVersionId(String versionIdStart, BlackListInfo.BlackListApp app) {
        try {
            app.setMinVersionId(Integer.parseInt(versionIdStart));
            return true;
        } catch (NumberFormatException e) {
            Slog.e(TAG, "setMinVersionId incorrect number format!");
            return false;
        }
    }

    private static boolean setMaxVersionId(String versionIdEnd, BlackListInfo.BlackListApp app) {
        try {
            app.setMaxVersionId(Integer.parseInt(versionIdEnd));
            return true;
        } catch (NumberFormatException e) {
            Slog.e(TAG, "setMaxVersionId incorrect number format!");
            return false;
        }
    }

    private static void setBlacklistApps(BlackListInfo info, ArrayList<BlackListInfo.BlackListApp> blackListApps, int version) {
        ArrayList<BlackListInfo.BlackListApp> apps = info.getBlacklistApps();
        apps.clear();
        int size = blackListApps.size();
        for (int i = 0; i < size; i++) {
            apps.add(blackListApps.get(i));
        }
        info.setVersionCode(version);
    }

    public static boolean comparePackage(PackageParser.Package info, BlackListInfo.BlackListApp app) {
        if (info == null || app == null || !TextUtils.equals(info.packageName, app.getPackageName())) {
            return false;
        }
        int versionStart = app.getMinVersionId();
        int versionEnd = app.getMaxVersionId();
        if (((versionStart | versionEnd) != 0) && (info.mVersionCode < versionStart || info.mVersionCode > versionEnd)) {
            return false;
        }
        if (app.getSignatures() != null && PackageManagerServiceUtils.compareSignatures(info.mSigningDetails.signatures, app.getSignatures()) != 0) {
            return false;
        }
        if (!TextUtils.isEmpty(app.getHashValue())) {
            if (!TextUtils.equals(app.getHashValue(), getSha256(new File(info.applicationInfo.sourceDir)))) {
                return false;
            }
        }
        if (IS_HW_DEBUG) {
            Slog.d(TAG, "find blacklist apk : " + app.getPackageName());
        }
        return true;
    }

    private static String getSha256(File file) {
        byte[] bytes = getManifestFile(file);
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return sha256(bytes);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006b, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006c, code lost:
        $closeResource(r8, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006f, code lost:
        throw r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0072, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0073, code lost:
        $closeResource(r7, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0076, code lost:
        throw r8;
     */
    private static byte[] getManifestFile(File apkFile) {
        byte[] bytes = new byte[1024];
        InputStream zipInputStream = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipFile zipFile = new ZipFile(apkFile);
            ZipEntry entry = zipFile.getEntry(MANIFEST_NAME);
            if (entry == null) {
                byte[] bArr = new byte[0];
                $closeResource(null, zipFile);
                $closeResource(null, outputStream);
                if (0 != 0) {
                    try {
                        zipInputStream.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "getManifestFile failed to close InputStream!");
                    }
                }
                return bArr;
            }
            InputStream zipInputStream2 = zipFile.getInputStream(entry);
            if (zipInputStream2 != null) {
                while (true) {
                    int length = zipInputStream2.read(bytes);
                    if (length <= 0) {
                        break;
                    }
                    outputStream.write(bytes, 0, length);
                }
                byte[] byteArray = outputStream.toByteArray();
                $closeResource(null, zipFile);
                $closeResource(null, outputStream);
                try {
                    zipInputStream2.close();
                } catch (IOException e2) {
                    Slog.e(TAG, "getManifestFile failed to close InputStream!");
                }
                return byteArray;
            }
            $closeResource(null, zipFile);
            $closeResource(null, outputStream);
            if (zipInputStream2 != null) {
                try {
                    zipInputStream2.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "getManifestFile failed to close InputStream!");
                }
            }
            return new byte[0];
        } catch (IOException e4) {
            Slog.e(TAG, "get manifest file failed due to IOException");
            if (0 != 0) {
                zipInputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    zipInputStream.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "getManifestFile failed to close InputStream!");
                }
            }
            throw th;
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
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * 2)];
        for (int i = 0; i < bytes.length; i++) {
            int byteValue = bytes[i] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY;
            chars[i * 2] = hexChars[byteValue >>> 4];
            chars[(i * 2) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars);
    }

    public static boolean isBlackListUpdate(BlackListInfo blackListInfo, BlackListInfo disabledApps) {
        int versionCode = blackListInfo.getVersionCode();
        int disabledAppsVersion = disabledApps.getVersionCode();
        if (disabledAppsVersion == -1) {
            if (IS_HW_DEBUG) {
                Slog.d(TAG, "blacklist update, version = " + versionCode);
            }
            return true;
        } else if (versionCode < disabledAppsVersion) {
            if (IS_HW_DEBUG) {
                Slog.d(TAG, "blacklist downgrade: from " + disabledAppsVersion + " to " + versionCode);
            }
            return true;
        } else if (versionCode > disabledAppsVersion) {
            if (IS_HW_DEBUG) {
                Slog.d(TAG, "blacklist upgrade: from " + disabledAppsVersion + " to " + versionCode);
            }
            return true;
        } else if (!IS_HW_DEBUG) {
            return false;
        } else {
            Slog.d(TAG, "blacklist version = " + versionCode);
            return false;
        }
    }

    @SuppressLint({"PreferForInArrayList"})
    private static void checkAppInfo(ArrayList<BlackListInfo.BlackListApp> blackListApps, BlackListInfo info, int version) {
        if (blackListApps != null) {
            int size = blackListApps.size();
            for (int i = 0; i < size; i++) {
                BlackListInfo.BlackListApp app = blackListApps.get(i);
                if (!TextUtils.isEmpty(app.getPackageName())) {
                    int count = 0;
                    if (app.getSignatures() != null && app.getSignatures().length > 0) {
                        count = 0 + 1;
                    }
                    if (app.getMaxVersionId() >= 0 && app.getMinVersionId() >= 0 && app.getMaxVersionId() >= app.getMinVersionId() && (app.getMinVersionId() | app.getMaxVersionId()) != 0) {
                        count++;
                    }
                    if (!TextUtils.isEmpty(app.getHashValue())) {
                        count++;
                    }
                    if (count < 1) {
                        return;
                    }
                } else {
                    return;
                }
            }
            if (version > 0) {
                setBlacklistApps(info, blackListApps, version);
            }
        }
    }

    @SuppressLint({"PreferForInArrayList"})
    public static boolean containsApp(ArrayList<BlackListInfo.BlackListApp> blackListApps, BlackListInfo.BlackListApp app) {
        if (blackListApps == null || app == null) {
            return false;
        }
        int size = blackListApps.size();
        for (int i = 0; i < size; i++) {
            if (blackListApps.get(i).equals(app)) {
                return true;
            }
        }
        return false;
    }
}
