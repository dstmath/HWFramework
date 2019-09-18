package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.pm.BlackListInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class BlackListAppsUtils {
    private static final String APP_BLACKLIST_FILE_NAME = "app_blacklist.xml";
    private static final String DISABLED_APP_FILE_NAME = "disabled_app.xml";
    private static final String TAG = "BlackListAppsUtils";

    public static void writeToXml(XmlSerializer out, BlackListInfo.BlackListApp app) throws IllegalArgumentException, IllegalStateException, IOException {
        if (out != null && app != null) {
            out.startTag(null, "app");
            out.startTag(null, "packageName");
            out.text(app.mPackageName);
            out.endTag(null, "packageName");
            Signature[] signs = app.mSignature;
            boolean versionIsValid = false;
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

    @SuppressLint({"PreferForInArrayList"})
    public static boolean writeBlackListToXml(BlackListInfo disableApp) {
        if (disableApp == null) {
            return false;
        }
        FileOutputStream fis = null;
        try {
            FileOutputStream fis2 = new FileOutputStream(Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", DISABLED_APP_FILE_NAME}), false);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fis2, "utf-8");
            out.startDocument(null, true);
            out.startTag(null, "blacklist");
            XmlUtils.writeIntAttribute(out, "version", disableApp.mVersionCode);
            Iterator<BlackListInfo.BlackListApp> it = disableApp.mBlackList.iterator();
            while (it.hasNext()) {
                writeToXml(out, it.next());
            }
            out.endTag(null, "blacklist");
            out.endDocument();
            fis2.flush();
            try {
                fis2.close();
            } catch (IOException e) {
                Slog.e(TAG, "close OutputStream failed when write blacklist file");
            }
            return true;
        } catch (IOException e2) {
            Slog.e(TAG, "write disabled app file failed due to IOException");
            deleteDisableAppListFile();
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "close OutputStream failed when write blacklist file");
                }
            }
            return false;
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "close OutputStream failed when write blacklist file");
                }
            }
            throw th;
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

    @SuppressLint({"PreferForInArrayList"})
    private static void readBlackListFromXml(BlackListInfo info, File file) {
        if (info != null && file != null && file.exists()) {
            FileInputStream fis = null;
            BlackListInfo.BlackListApp app = null;
            ArrayList<BlackListInfo.BlackListApp> blacklist = new ArrayList<>();
            int version = -1;
            boolean parserError = false;
            try {
                fis = new FileInputStream(file);
                XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
                xpp.setInput(fis, "UTF-8");
                for (int type = xpp.getEventType(); type != 1; type = xpp.next()) {
                    if (type != 0) {
                        switch (type) {
                            case 2:
                                String name = xpp.getName();
                                if (!TextUtils.equals("blacklist", name)) {
                                    if (!TextUtils.equals("app", name)) {
                                        if (!TextUtils.equals("packageName", name) || app == null) {
                                            if (!TextUtils.equals("signature", name) || app == null) {
                                                if (!TextUtils.equals("versionIDStart", name) || app == null) {
                                                    if (!TextUtils.equals("versionIDEnd", name) || app == null) {
                                                        if (TextUtils.equals("hashValue", name) && app != null) {
                                                            app.mHashValue = xpp.nextText();
                                                            break;
                                                        }
                                                    } else {
                                                        try {
                                                            app.mMaxVersionId = Integer.parseInt(xpp.nextText());
                                                            break;
                                                        } catch (NumberFormatException e) {
                                                            parserError = true;
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    try {
                                                        app.mMinVersionId = Integer.parseInt(xpp.nextText());
                                                        break;
                                                    } catch (NumberFormatException e2) {
                                                        parserError = true;
                                                        break;
                                                    }
                                                }
                                            } else {
                                                app.setSignature(xpp.nextText().split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER));
                                                break;
                                            }
                                        } else {
                                            app.mPackageName = xpp.nextText();
                                            break;
                                        }
                                    } else {
                                        app = new BlackListInfo.BlackListApp();
                                        break;
                                    }
                                } else {
                                    try {
                                        version = Integer.parseInt(xpp.getAttributeValue(0));
                                        break;
                                    } catch (NumberFormatException e3) {
                                        parserError = true;
                                        break;
                                    }
                                }
                            case 3:
                                if (TextUtils.equals("app", xpp.getName())) {
                                    blacklist.add(app);
                                    app = null;
                                    break;
                                }
                                break;
                        }
                    }
                    if (parserError) {
                        try {
                            fis.close();
                        } catch (IOException e4) {
                            Slog.e(TAG, "close InputStream failed when read blacklist file");
                        }
                        return;
                    }
                }
                try {
                    fis.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "close InputStream failed when read blacklist file");
                }
            } catch (IOException e6) {
                Slog.e(TAG, "read blacklist file failed due to IOException");
                parserError = true;
                if (fis != null) {
                    fis.close();
                }
            } catch (XmlPullParserException e7) {
                Slog.e(TAG, "read blacklist file failed due to XmlPullParserException");
                if (fis != null) {
                    fis.close();
                }
            } catch (Throwable th) {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e8) {
                        Slog.e(TAG, "close InputStream failed when read blacklist file");
                    }
                }
                throw th;
            }
            if (checkAppInfo(blacklist) && version >= 0 && !parserError) {
                info.mBlackList.clear();
                Iterator<BlackListInfo.BlackListApp> it = blacklist.iterator();
                while (it.hasNext()) {
                    info.mBlackList.add(it.next());
                }
                info.mVersionCode = version;
            }
        }
    }

    public static boolean comparePackage(PackageParser.Package info, BlackListInfo.BlackListApp app) {
        if (info == null || app == null || !TextUtils.equals(info.packageName, app.mPackageName)) {
            return false;
        }
        int versionStart = app.mMinVersionId;
        int versionEnd = app.mMaxVersionId;
        if (((versionStart | versionEnd) != 0) && (info.mVersionCode < versionStart || info.mVersionCode > versionEnd)) {
            return false;
        }
        if (app.mSignature != null && PackageManagerServiceUtils.compareSignatures(info.mSigningDetails.signatures, app.mSignature) != 0) {
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
        byte[] manifest = HwUtils.getManifestFile(file);
        if (manifest.length == 0) {
            return null;
        }
        return sha256(manifest);
    }

    public static String sha256(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return HwUtils.bytesToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            Slog.e(TAG, "get sha256 failed");
            return null;
        }
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

    @SuppressLint({"PreferForInArrayList"})
    private static boolean checkAppInfo(ArrayList<BlackListInfo.BlackListApp> blacklist) {
        if (blacklist == null) {
            return false;
        }
        Iterator<BlackListInfo.BlackListApp> it = blacklist.iterator();
        while (it.hasNext()) {
            BlackListInfo.BlackListApp app = it.next();
            if (TextUtils.isEmpty(app.mPackageName)) {
                return false;
            }
            int count = 0;
            if (app.mSignature != null && app.mSignature.length > 0) {
                count = 0 + 1;
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

    @SuppressLint({"PreferForInArrayList"})
    public static boolean containsApp(ArrayList<BlackListInfo.BlackListApp> blacklist, BlackListInfo.BlackListApp app) {
        if (blacklist == null || app == null) {
            return false;
        }
        Iterator<BlackListInfo.BlackListApp> it = blacklist.iterator();
        while (it.hasNext()) {
            BlackListInfo.BlackListApp blacklistApp = it.next();
            if (TextUtils.equals(blacklistApp.mPackageName, app.mPackageName) && Arrays.equals(blacklistApp.mSignature, app.mSignature) && app.mMinVersionId == blacklistApp.mMinVersionId && app.mMaxVersionId == blacklistApp.mMaxVersionId && TextUtils.equals(app.mHashValue, blacklistApp.mHashValue)) {
                return true;
            }
        }
        return false;
    }
}
