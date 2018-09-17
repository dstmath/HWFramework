package com.android.server.pm;

import android.content.pm.Signature;
import android.os.Environment;
import android.os.FileUtils;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

final class CertCompatSettings {
    private static final String ATTR_CERT = "cert";
    private static final String ATTR_CODEPATH = "codePath";
    private static final String ATTR_FT = "ft";
    private static final String ATTR_HASH = "hash";
    private static final String ATTR_KEY = "key";
    private static final String ATTR_NAME = "name";
    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private static final String SIGNATURE_VERSION_ONE = "HwSignature_V1";
    private static final String SIGNATURE_VERSION_TWO = "HwSignature_V2";
    private static final String TAG = "CertCompatSettings";
    private static final String TAG_ITEM = "item";
    private static final String TAG_VERSIONS = "versions";
    private final File mBackupCompatiblePackageFilename;
    private boolean mCompatAll = false;
    private final File mCompatiblePackageFilename;
    private boolean mFoundSystemCertFile;
    private boolean mFoundWhiteListFile;
    private HashMap<String, Signature[]> mNewSigns = new HashMap();
    private HashMap<String, Signature[]> mOldSigns = new HashMap();
    final HashMap<String, Package> mPackages = new HashMap();
    private final File mSyscertFilename;
    final HashMap<String, WhiteListPackage> mWhiteList = new HashMap();
    private final File mWhiteListFilename;

    static final class Package {
        String certType;
        String codePath;
        String packageName;
        long timeStamp;

        Package(String packageName, String codePath, long timeStamp, String certType) {
            this.packageName = packageName;
            this.codePath = codePath;
            this.timeStamp = timeStamp;
            this.certType = certType;
        }
    }

    private static final class WhiteListPackage {
        List<byte[]> hashList = new ArrayList();
        String packageName;

        WhiteListPackage(String packageName) {
            this.packageName = packageName;
        }
    }

    CertCompatSettings() {
        File securityDir = new File(Environment.getRootDirectory(), "etc/security");
        this.mWhiteListFilename = new File(securityDir, "trusted_app.xml");
        this.mSyscertFilename = new File(securityDir, "hwsyscert.xml");
        File systemDir = new File(Environment.getDataDirectory(), "system");
        this.mCompatiblePackageFilename = new File(systemDir, "certcompat.xml");
        this.mBackupCompatiblePackageFilename = new File(systemDir, "certcompat-backup.xml");
        this.mFoundSystemCertFile = loadTrustedCerts(this.mSyscertFilename);
        this.mFoundWhiteListFile = this.mWhiteListFilename.exists();
    }

    private boolean loadTrustedCerts(File file) {
        Throwable th;
        if (file.exists()) {
            FileInputStream str = null;
            try {
                FileInputStream str2 = new FileInputStream(file);
                try {
                    int type;
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(str2, StandardCharsets.UTF_8.name());
                    do {
                        type = parser.next();
                        if (type == 2) {
                            break;
                        }
                    } while (type != 1);
                    if (type != 2) {
                        closeStream(str2);
                        return false;
                    }
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                            closeStream(str2);
                        } else if (!(type == 3 || type == 4)) {
                            if (parser.getName().equals("sigs")) {
                                readCerts(parser);
                            } else {
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                    closeStream(str2);
                    return true;
                } catch (XmlPullParserException e) {
                    str = str2;
                } catch (IOException e2) {
                    str = str2;
                } catch (Throwable th2) {
                    th = th2;
                    str = str2;
                }
            } catch (XmlPullParserException e3) {
                try {
                    Slog.e(TAG, "getTrustedCerts error duing to XmlPullParserException");
                    closeStream(str);
                    return false;
                } catch (Throwable th3) {
                    th = th3;
                    closeStream(str);
                    throw th;
                }
            } catch (IOException e4) {
                Slog.e(TAG, "getTrustedCerts error duing to IOException");
                closeStream(str);
                return false;
            }
        }
        Slog.d(TAG, "system cert file not found");
        return false;
    }

    private void closeStream(FileInputStream str) {
        if (str != null) {
            try {
                str.close();
            } catch (IOException e) {
                Slog.e(TAG, "close FileInputStream failed");
            }
        }
    }

    private void readCerts(XmlPullParser parser) throws XmlPullParserException, IOException {
        String certVersion = parser.getAttributeValue(null, "name");
        if (certVersion.equals(SIGNATURE_VERSION_ONE) || (certVersion.equals(SIGNATURE_VERSION_TWO) ^ 1) == 0) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                    if (!(type == 3 || type == 4)) {
                        if (parser.getName().equals("cert")) {
                            String signType = parser.getAttributeValue(null, "name");
                            String sign = parser.getAttributeValue(null, "key");
                            if (certVersion.equals(SIGNATURE_VERSION_ONE)) {
                                this.mOldSigns.put(signType, new Signature[]{new Signature(sign)});
                            } else if (certVersion.equals(SIGNATURE_VERSION_TWO)) {
                                this.mNewSigns.put(signType, new Signature[]{new Signature(sign)});
                            }
                        } else {
                            Slog.w(TAG, "Unknown element under <sigs>: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b1 A:{SYNTHETIC, Splitter: B:53:0x00b1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readWhiteList() {
        Throwable th;
        if (this.mFoundWhiteListFile) {
            FileInputStream str = null;
            try {
                FileInputStream str2 = new FileInputStream(this.mWhiteListFilename);
                try {
                    int type;
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(str2, StandardCharsets.UTF_8.name());
                    do {
                        type = parser.next();
                        if (type == 2) {
                            break;
                        }
                    } while (type != 1);
                    if (type != 2) {
                        if (str2 != null) {
                            try {
                                str2.close();
                            } catch (IOException e) {
                                Slog.e(TAG, "close FileInputStream failed");
                            }
                        }
                        return;
                    }
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                            if (str2 != null) {
                                try {
                                    str2.close();
                                } catch (IOException e2) {
                                    Slog.e(TAG, "close FileInputStream failed");
                                }
                            }
                        } else if (!(type == 3 || type == 4)) {
                            if (parser.getName().equals("package")) {
                                readPackageLPw(parser);
                            } else {
                                Slog.w(TAG, "Unknown element under <packages>: " + parser.getName());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                    if (str2 != null) {
                    }
                } catch (XmlPullParserException e3) {
                    str = str2;
                } catch (IOException e4) {
                    str = str2;
                } catch (Throwable th2) {
                    th = th2;
                    str = str2;
                }
            } catch (XmlPullParserException e5) {
                try {
                    Slog.e(TAG, "readWhiteList error duing to XmlPullParserException");
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e6) {
                            Slog.e(TAG, "close FileInputStream failed");
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e7) {
                            Slog.e(TAG, "close FileInputStream failed");
                        }
                    }
                    throw th;
                }
            } catch (IOException e8) {
                Slog.e(TAG, "readWhiteList error duing to IOException");
                if (str != null) {
                    try {
                        str.close();
                    } catch (IOException e9) {
                        Slog.e(TAG, "close FileInputStream failed");
                    }
                }
            }
        }
    }

    private void readVersionsLPw(XmlPullParser parser, WhiteListPackage info) throws XmlPullParserException, IOException, IllegalArgumentException {
        if (info != null && parser != null) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                    if (!(type == 3 || type == 4)) {
                        if (parser.getName().equals(TAG_ITEM)) {
                            info.hashList.add(decodeHash(parser.getAttributeValue(null, ATTR_HASH)));
                        } else {
                            Slog.w(TAG, "Unknown element under <versions>: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:0x017c A:{SYNTHETIC, Splitter: B:100:0x017c} */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x005a A:{SYNTHETIC, Splitter: B:11:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x00b1 A:{SYNTHETIC, EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00c6 A:{SYNTHETIC, Splitter: B:44:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x005a A:{SYNTHETIC, Splitter: B:11:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x00b1 A:{SYNTHETIC, EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00c6 A:{SYNTHETIC, Splitter: B:44:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x005a A:{SYNTHETIC, Splitter: B:11:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x00b1 A:{SYNTHETIC, EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00c6 A:{SYNTHETIC, Splitter: B:44:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x005a A:{SYNTHETIC, Splitter: B:11:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x00b1 A:{SYNTHETIC, EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  , EDGE_INSN: B:117:0x00b1->B:35:0x00b1 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00c6 A:{SYNTHETIC, Splitter: B:44:0x00c6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean readCertCompatPackages() {
        FileInputStream str;
        XmlPullParser parser;
        int type;
        Throwable th;
        FileInputStream str2 = null;
        if (this.mBackupCompatiblePackageFilename.exists()) {
            try {
                str = new FileInputStream(this.mBackupCompatiblePackageFilename);
                try {
                    if (this.mCompatiblePackageFilename.exists()) {
                        Slog.w(TAG, "Cleaning up whitelist file " + this.mWhiteListFilename);
                        if (!this.mCompatiblePackageFilename.delete()) {
                            Slog.wtf(TAG, "Failed to clean up CompatPackage back up file: " + this.mBackupCompatiblePackageFilename);
                        }
                    }
                } catch (IOException e) {
                    str2 = str;
                    Slog.e(TAG, "init FileInputStream failed due to IOException");
                    str = str2;
                    if (str == null) {
                    }
                    parser = Xml.newPullParser();
                    parser.setInput(str2, StandardCharsets.UTF_8.name());
                    do {
                        type = parser.next();
                        if (type == 2) {
                        }
                    } while (type != 1);
                    if (type != 2) {
                    }
                } catch (SecurityException e2) {
                    str2 = str;
                    Slog.e(TAG, "delete CompatiblePackage File failed due to SecurityException");
                    str = str2;
                    if (str == null) {
                    }
                    parser = Xml.newPullParser();
                    parser.setInput(str2, StandardCharsets.UTF_8.name());
                    do {
                        type = parser.next();
                        if (type == 2) {
                        }
                    } while (type != 1);
                    if (type != 2) {
                    }
                }
            } catch (IOException e3) {
                Slog.e(TAG, "init FileInputStream failed due to IOException");
                str = str2;
                if (str == null) {
                }
                parser = Xml.newPullParser();
                parser.setInput(str2, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                    }
                } while (type != 1);
                if (type != 2) {
                }
            } catch (SecurityException e4) {
                Slog.e(TAG, "delete CompatiblePackage File failed due to SecurityException");
                str = str2;
                if (str == null) {
                }
                parser = Xml.newPullParser();
                parser.setInput(str2, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                    }
                } while (type != 1);
                if (type != 2) {
                }
            }
        } else {
            str = null;
        }
        if (str == null) {
            try {
                if (this.mCompatiblePackageFilename.exists()) {
                    str2 = new FileInputStream(this.mCompatiblePackageFilename);
                } else {
                    PackageManagerService.reportSettingsProblem(4, "No whitelist file; creating initial state");
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e5) {
                            Slog.e(TAG, "close the FileInputStream failed");
                        }
                    }
                    return false;
                }
            } catch (XmlPullParserException e6) {
                str2 = str;
                try {
                    Slog.e(TAG, "readCompatPackages error duing to XmlPullParserException");
                    if (str2 != null) {
                        try {
                            str2.close();
                        } catch (IOException e7) {
                            Slog.e(TAG, "close the FileInputStream failed");
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (str2 != null) {
                    }
                    throw th;
                }
            } catch (IOException e8) {
                str2 = str;
                Slog.e(TAG, "readCompatPackages error duing to IOException");
                if (str2 != null) {
                    try {
                        str2.close();
                    } catch (IOException e9) {
                        Slog.e(TAG, "close the FileInputStream failed");
                    }
                }
                return false;
            } catch (IllegalArgumentException e10) {
                str2 = str;
                Slog.e(TAG, "readCompatPackages error duing to IllegalArgumentException");
                if (str2 != null) {
                    try {
                        str2.close();
                    } catch (IOException e11) {
                        Slog.e(TAG, "close the FileInputStream failed");
                    }
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                str2 = str;
                if (str2 != null) {
                    try {
                        str2.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "close the FileInputStream failed");
                    }
                }
                throw th;
            }
        }
        str2 = str;
        try {
            parser = Xml.newPullParser();
            parser.setInput(str2, StandardCharsets.UTF_8.name());
            do {
                type = parser.next();
                if (type == 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                if (str2 != null) {
                    try {
                        str2.close();
                    } catch (IOException e13) {
                        Slog.e(TAG, "close the FileInputStream failed");
                    }
                }
                return false;
            }
            int outerDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                    if (!(type == 3 || type == 4)) {
                        if (parser.getName().equals("package")) {
                            readCompatPackage(parser);
                        } else {
                            Slog.w(TAG, "Unknown element under <packages>: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
            }
            if (str2 != null) {
                try {
                    str2.close();
                } catch (IOException e14) {
                    Slog.e(TAG, "close the FileInputStream failed");
                }
            }
            return true;
        } catch (XmlPullParserException e15) {
        } catch (IOException e16) {
        } catch (IllegalArgumentException e17) {
        }
    }

    void readCompatPackage(XmlPullParser parser) throws XmlPullParserException, IOException {
        String packName = parser.getAttributeValue(null, "name");
        String codePath = parser.getAttributeValue(null, ATTR_CODEPATH);
        String timeStamp = parser.getAttributeValue(null, ATTR_FT);
        String certType = parser.getAttributeValue(null, "cert");
        if (TextUtils.isEmpty(packName) || TextUtils.isEmpty(codePath) || TextUtils.isEmpty(timeStamp) || TextUtils.isEmpty(certType)) {
            Slog.d(TAG, "invalid compat package, skip it");
            return;
        }
        this.mPackages.put(packName, new Package(packName, codePath, Long.parseLong(timeStamp), certType));
    }

    private void readPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException, IllegalArgumentException {
        String packageName = parser.getAttributeValue(null, "name");
        if (!TextUtils.isEmpty(packageName)) {
            WhiteListPackage info;
            if (this.mWhiteList.containsKey(packageName)) {
                info = (WhiteListPackage) this.mWhiteList.get(packageName);
            } else {
                info = new WhiteListPackage(packageName);
            }
            if (info != null) {
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        break;
                    } else if (!(type == 3 || type == 4)) {
                        if (parser.getName().equals(TAG_VERSIONS)) {
                            readVersionsLPw(parser, info);
                        } else {
                            XmlUtils.skipCurrentTag(parser);
                        }
                        this.mWhiteList.put(info.packageName, info);
                    }
                }
            } else {
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    void insertCompatPackage(String packageName, PackageSetting ps) {
        if (packageName != null && ps != null) {
            String type = getNewSignTpye(ps.signatures.mSignatures);
            Slog.d(TAG, "insertCompatPackage:" + packageName + " and the codePath is " + ps.codePathString);
            this.mPackages.put(packageName, new Package(packageName, ps.codePathString, ps.timeStamp, type));
        }
    }

    void writeCertCompatPackages() {
        IOException e;
        Throwable th;
        Object fstr;
        if (this.mCompatiblePackageFilename.exists()) {
            if (this.mBackupCompatiblePackageFilename.exists()) {
                if (this.mCompatiblePackageFilename.delete()) {
                    Slog.wtf(TAG, "Failed to clean up CompatPackage file: " + this.mCompatiblePackageFilename);
                }
            } else if (!this.mCompatiblePackageFilename.renameTo(this.mBackupCompatiblePackageFilename)) {
                return;
            }
        }
        AutoCloseable fstr2 = null;
        try {
            FileOutputStream fstr3 = new FileOutputStream(this.mCompatiblePackageFilename);
            try {
                BufferedOutputStream str = new BufferedOutputStream(fstr3);
                try {
                    XmlSerializer serializer = new FastXmlSerializer();
                    serializer.setOutput(str, StandardCharsets.UTF_8.name());
                    serializer.startDocument(null, Boolean.valueOf(true));
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                    serializer.startTag(null, HwSecDiagnoseConstant.MALAPP_APK_PACKAGES);
                    for (Package info : this.mPackages.values()) {
                        serializer.startTag(null, "package");
                        XmlUtils.writeStringAttribute(serializer, "name", info.packageName);
                        XmlUtils.writeStringAttribute(serializer, ATTR_CODEPATH, info.codePath);
                        XmlUtils.writeStringAttribute(serializer, ATTR_FT, String.valueOf(info.timeStamp));
                        XmlUtils.writeStringAttribute(serializer, "cert", info.certType);
                        serializer.endTag(null, "package");
                    }
                    serializer.endTag(null, HwSecDiagnoseConstant.MALAPP_APK_PACKAGES);
                    serializer.endDocument();
                    str.flush();
                    FileUtils.sync(fstr3);
                    if (this.mBackupCompatiblePackageFilename.exists() && !this.mBackupCompatiblePackageFilename.delete()) {
                        Slog.wtf(TAG, "Failed to clean up CompatPackage back up file: " + this.mBackupCompatiblePackageFilename);
                    }
                    FileUtils.setPermissions(this.mCompatiblePackageFilename.toString(), 432, -1, -1);
                    IoUtils.closeQuietly(fstr3);
                } catch (IOException e2) {
                    e = e2;
                    fstr2 = fstr3;
                    try {
                        Slog.wtf(TAG, "Unable to write CompatPackage, current changes will be lost at reboot", e);
                        IoUtils.closeQuietly(fstr2);
                        Slog.wtf(TAG, "Failed to clean up CompatPackage file: " + this.mCompatiblePackageFilename);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(fstr2);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fstr = fstr3;
                    IoUtils.closeQuietly(fstr2);
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                fstr = fstr3;
                Slog.wtf(TAG, "Unable to write CompatPackage, current changes will be lost at reboot", e);
                IoUtils.closeQuietly(fstr2);
                if (this.mCompatiblePackageFilename.exists() && !this.mCompatiblePackageFilename.delete()) {
                    Slog.wtf(TAG, "Failed to clean up CompatPackage file: " + this.mCompatiblePackageFilename);
                }
            } catch (Throwable th4) {
                th = th4;
                fstr = fstr3;
                IoUtils.closeQuietly(fstr2);
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            Slog.wtf(TAG, "Unable to write CompatPackage, current changes will be lost at reboot", e);
            IoUtils.closeQuietly(fstr2);
            Slog.wtf(TAG, "Failed to clean up CompatPackage file: " + this.mCompatiblePackageFilename);
        }
    }

    boolean isSystemSignatureUpdated(Signature[] oldSignature, Signature[] newSignature) {
        if (!this.mFoundSystemCertFile) {
            return false;
        }
        String oldSign = getOldSignTpye(oldSignature);
        String newSign = getNewSignTpye(newSignature);
        if (oldSign == null || newSign == null) {
            return false;
        }
        return oldSign.equals(newSign);
    }

    void removeCertCompatPackage(String name) {
        if (this.mPackages.containsKey(name)) {
            Slog.d(TAG, "removeCertCompatPackage" + name);
            this.mPackages.remove(name);
        }
    }

    Package getCompatPackage(String name) {
        return (Package) this.mPackages.get(name);
    }

    Collection<Package> getALLCompatPackages() {
        return this.mPackages.values();
    }

    boolean isOldSystemSignature(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return false;
        }
        for (Signature[] s : this.mOldSigns.values()) {
            if (PackageManagerService.compareSignatures(s, signs) == 0) {
                return true;
            }
        }
        return false;
    }

    boolean isWhiteListedApp(android.content.pm.PackageParser.Package pkg) {
        if (this.mCompatAll) {
            return true;
        }
        if (!this.mFoundWhiteListFile) {
            return false;
        }
        if (this.mWhiteList.size() == 0) {
            readWhiteList();
        }
        File file = new File(pkg.baseCodePath);
        String pkgName = pkg.packageName;
        if (!this.mWhiteList.containsKey(pkgName)) {
            return false;
        }
        List<byte[]> storedHash = ((WhiteListPackage) this.mWhiteList.get(pkgName)).hashList;
        byte[] computedHash = getSHA256(file);
        for (byte[] b : storedHash) {
            if (MessageDigest.isEqual(b, computedHash)) {
                Slog.i(TAG, "found whitelist package:" + pkgName);
                return true;
            }
        }
        return false;
    }

    String getOldSignTpye(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return null;
        }
        for (Entry<String, Signature[]> entry : this.mOldSigns.entrySet()) {
            if (PackageManagerService.compareSignatures(signs, (Signature[]) entry.getValue()) == 0) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    String getNewSignTpye(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return null;
        }
        for (Entry<String, Signature[]> entry : this.mNewSigns.entrySet()) {
            if (PackageManagerService.compareSignatures(signs, (Signature[]) entry.getValue()) == 0) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    Signature[] getOldSign(String type) {
        if (this.mFoundSystemCertFile) {
            return (Signature[]) this.mOldSigns.get(type);
        }
        return new Signature[0];
    }

    Signature[] getNewSign(String type) {
        if (this.mFoundSystemCertFile) {
            return (Signature[]) this.mNewSigns.get(type);
        }
        return new Signature[0];
    }

    private byte[] getSHA256(File file) {
        byte[] manifest = getManifestFile(file);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(manifest);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            Slog.e(TAG, "get sha256 failed");
            return new byte[0];
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0071 A:{SYNTHETIC, Splitter: B:48:0x0071} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] getManifestFile(File apkFile) {
        Throwable th;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        ZipFile zipFile = null;
        AutoCloseable zipInputStream = null;
        try {
            ZipFile zipFile2 = new ZipFile(apkFile);
            try {
                ZipEntry ze = zipFile2.getEntry("META-INF/MANIFEST.MF");
                if (ze != null) {
                    byte[] toByteArray;
                    try {
                        zipInputStream = zipFile2.getInputStream(ze);
                        if (zipInputStream != null) {
                            while (true) {
                                int length = zipInputStream.read(b);
                                if (length <= 0) {
                                    break;
                                }
                                os.write(b, 0, length);
                            }
                            toByteArray = os.toByteArray();
                            if (zipFile2 != null) {
                                try {
                                    zipFile2.close();
                                } catch (IOException e) {
                                }
                            }
                            return toByteArray;
                        }
                        IoUtils.closeQuietly(zipInputStream);
                    } catch (IOException e2) {
                        toByteArray = TAG;
                        Slog.e(toByteArray, "get manifest file failed due to IOException");
                    } finally {
                        IoUtils.closeQuietly(zipInputStream);
                    }
                }
                if (zipFile2 != null) {
                    try {
                        zipFile2.close();
                    } catch (IOException e3) {
                    }
                }
                zipFile = zipFile2;
            } catch (IOException e4) {
                zipFile = zipFile2;
            } catch (Throwable th2) {
                th = th2;
                zipFile = zipFile2;
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        } catch (IOException e6) {
            try {
                Slog.e(TAG, " get manifest file failed due to IOException");
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException e7) {
                    }
                }
                return new byte[0];
            } catch (Throwable th3) {
                th = th3;
                if (zipFile != null) {
                }
                throw th;
            }
        }
        return new byte[0];
    }

    private byte[] decodeHash(String hash) throws IllegalArgumentException {
        byte[] input = hash.getBytes(StandardCharsets.UTF_8);
        int N = input.length;
        if (N % 2 != 0) {
            throw new IllegalArgumentException("text size " + N + " is not even");
        }
        byte[] sig = new byte[(N / 2)];
        int i = 0;
        int sigIndex = 0;
        while (i < N) {
            int i2 = i + 1;
            int hi = parseHexDigit(input[i]);
            i = i2 + 1;
            int sigIndex2 = sigIndex + 1;
            sig[sigIndex] = (byte) ((hi << 4) | parseHexDigit(input[i2]));
            sigIndex = sigIndex2;
        }
        return sig;
    }

    private int parseHexDigit(int nibble) throws IllegalArgumentException {
        if (48 <= nibble && nibble <= 57) {
            return nibble - 48;
        }
        if (97 <= nibble && nibble <= 102) {
            return (nibble - 97) + 10;
        }
        if (65 <= nibble && nibble <= 70) {
            return (nibble - 65) + 10;
        }
        throw new IllegalArgumentException("Invalid character " + nibble + " in hex string");
    }
}
