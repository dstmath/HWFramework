package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.os.Environment;
import android.os.FileUtils;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.appactcontrol.AppActConstant;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public final class CertCompatSettings {
    private static final String ATTR_CERT = "cert";
    private static final String ATTR_CERT_VERSION = "certVersion";
    private static final String ATTR_CODEPATH = "codePath";
    private static final String ATTR_FT = "ft";
    private static final String ATTR_HASH = "hash";
    private static final String ATTR_KEY = "key";
    private static final String ATTR_NAME = "name";
    private static final int CERT_VERSION = 4;
    private static final int INVALID_CERT_VERSION = -1;
    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private static final int SIGNATURE_COUNT = 4;
    private static final String[] SIGNATURE_VERSIONS = {SIGNATURE_VERSION_ONE, SIGNATURE_VERSION_TWO, SIGNATURE_VERSION_THREE, SIGNATURE_VERSION_FOUR};
    private static final String SIGNATURE_VERSION_FOUR = "HwSignature_V4";
    private static final String SIGNATURE_VERSION_ONE = "HwSignature_V1";
    private static final String SIGNATURE_VERSION_THREE = "HwSignature_V3";
    private static final String SIGNATURE_VERSION_TWO = "HwSignature_V2";
    private static final String TAG = "CertCompatSettings";
    private static final String TAG_ITEM = "item";
    private static final String TAG_VERSIONS = "versions";
    private final File mBackupCompatiblePackageFilename;
    private final ArrayList<String> mCertIncompatPackages = new ArrayList<>(Arrays.asList("com.huawei.browser", "com.huawei.webview"));
    private boolean mCompatAll = false;
    private final File mCompatiblePackageFilename;
    private boolean mFoundSystemCertFile;
    private boolean mFoundWhiteListFile;
    private int mLastCertVersion = 0;
    final HashMap<String, Package> mPackages = new HashMap<>();
    private HashMap<String, Signature[]>[] mSigns = {this.mSignsV1, this.mSignsV2, this.mSignsV3, this.mSignsV4};
    private HashMap<String, Signature[]> mSignsV1 = new HashMap<>(4);
    private HashMap<String, Signature[]> mSignsV2 = new HashMap<>(4);
    private HashMap<String, Signature[]> mSignsV3 = new HashMap<>(4);
    private HashMap<String, Signature[]> mSignsV4 = new HashMap<>(4);
    private final File mSyscertFilename;
    final HashMap<String, WhiteListPackage> mWhiteList = new HashMap<>();
    private final File mWhiteListFilename;
    private final int newSignaturesIndex = (this.mSigns.length - 1);
    private final int oldSignaturesIndex = (this.newSignaturesIndex - 1);

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

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x006b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0070, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0071, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0074, code lost:
        throw r4;
     */
    private boolean loadTrustedCerts(File file) {
        FileInputStream str;
        XmlPullParser parser;
        int type;
        if (!file.exists()) {
            Slog.d(TAG, "system cert file not found");
            return false;
        }
        try {
            str = new FileInputStream(file);
            parser = Xml.newPullParser();
            parser.setInput(str, StandardCharsets.UTF_8.name());
            do {
                type = parser.next();
                if (type == 2) {
                    break;
                }
            } while (type != 1);
        } catch (XmlPullParserException e) {
            Slog.e(TAG, "getTrustedCerts error duing to XmlPullParserException");
            return false;
        } catch (IOException e2) {
            Slog.e(TAG, "getTrustedCerts error duing to IOException");
            return false;
        }
        if (type != 2) {
            str.close();
            return false;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type2 = parser.next();
            if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (!(type2 == 3 || type2 == 4)) {
                if (parser.getName().equals("sigs")) {
                    readCerts(parser);
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        str.close();
        return true;
    }

    private void readCerts(XmlPullParser parser) throws XmlPullParserException, IOException {
        String certVersion = parser.getAttributeValue(null, ATTR_NAME);
        boolean isHwCertVersion = false;
        int certIndex = 0;
        int index = 0;
        while (true) {
            String[] strArr = SIGNATURE_VERSIONS;
            if (index >= strArr.length) {
                break;
            } else if (strArr[index].equals(certVersion)) {
                isHwCertVersion = true;
                certIndex = index;
                break;
            } else {
                index++;
            }
        }
        if (isHwCertVersion) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals(ATTR_CERT)) {
                        this.mSigns[certIndex].put(parser.getAttributeValue(null, ATTR_NAME), new Signature[]{new Signature(parser.getAttributeValue(null, ATTR_KEY))});
                    } else {
                        Slog.w(TAG, "Unknown element under <sigs>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }
    }

    private void readWhiteList() {
        int type;
        if (this.mFoundWhiteListFile) {
            FileInputStream str = null;
            try {
                FileInputStream str2 = new FileInputStream(this.mWhiteListFilename);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(str2, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    try {
                        str2.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "close FileInputStream failed");
                    }
                } else {
                    int outerDepth = parser.getDepth();
                    while (true) {
                        int type2 = parser.next();
                        if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                            try {
                                str2.close();
                                return;
                            } catch (IOException e2) {
                                Slog.e(TAG, "close FileInputStream failed");
                                return;
                            }
                        } else if (!(type2 == 3 || type2 == 4)) {
                            if (parser.getName().equals("package")) {
                                readPackageLPw(parser);
                            } else {
                                Slog.w(TAG, "Unknown element under <packages>: " + parser.getName());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                }
            } catch (XmlPullParserException e3) {
                Slog.e(TAG, "readWhiteList error duing to XmlPullParserException");
                if (0 != 0) {
                    str.close();
                }
            } catch (IOException e4) {
                Slog.e(TAG, "readWhiteList error duing to IOException");
                if (0 != 0) {
                    str.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        str.close();
                    } catch (IOException e5) {
                        Slog.e(TAG, "close FileInputStream failed");
                    }
                }
                throw th;
            }
        }
    }

    private void readVersionsLPw(XmlPullParser parser, WhiteListPackage info) throws XmlPullParserException, IOException, IllegalArgumentException {
        if (info != null && parser != null) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals("item")) {
                        info.hashList.add(decodeHash(parser.getAttributeValue(null, ATTR_HASH)));
                    } else {
                        Slog.w(TAG, "Unknown element under <versions>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isUpgrade() {
        int i = this.mLastCertVersion;
        return (i == -1 || i == 4) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean isIncompatPackage(PackageParser.Package pkg) {
        if (!this.mCertIncompatPackages.contains(pkg.packageName) && !pkg.applicationInfo.isStaticSharedLibrary()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean readCertCompatPackages() {
        int type;
        FileInputStream str = null;
        if (this.mBackupCompatiblePackageFilename.exists()) {
            try {
                str = new FileInputStream(this.mBackupCompatiblePackageFilename);
                if (this.mCompatiblePackageFilename.exists()) {
                    Slog.w(TAG, "Cleaning up settings file " + this.mCompatiblePackageFilename);
                    if (!this.mCompatiblePackageFilename.delete()) {
                        Slog.wtf(TAG, "Failed to clean up settings file: " + this.mCompatiblePackageFilename);
                    }
                }
            } catch (IOException e) {
                Slog.e(TAG, "init FileInputStream failed due to IOException");
            } catch (SecurityException e2) {
                Slog.e(TAG, "delete CompatiblePackage File failed due to SecurityException");
            }
        }
        if (str == null) {
            try {
                if (!this.mCompatiblePackageFilename.exists()) {
                    Slog.w(TAG, "No settings file found");
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e3) {
                            Slog.e(TAG, "close the FileInputStream failed");
                        }
                    }
                    return false;
                }
                str = new FileInputStream(this.mCompatiblePackageFilename);
            } catch (XmlPullParserException e4) {
                Slog.e(TAG, "readCompatPackages error duing to XmlPullParserException");
                if (str != null) {
                    str.close();
                }
            } catch (IOException e5) {
                Slog.e(TAG, "readCompatPackages error duing to IOException");
                if (str != null) {
                    str.close();
                }
            } catch (IllegalArgumentException e6) {
                Slog.e(TAG, "readCompatPackages error duing to IllegalArgumentException");
                if (str != null) {
                    try {
                        str.close();
                    } catch (IOException e7) {
                        Slog.e(TAG, "close the FileInputStream failed");
                    }
                }
            } catch (Throwable th) {
                if (str != null) {
                    try {
                        str.close();
                    } catch (IOException e8) {
                        Slog.e(TAG, "close the FileInputStream failed");
                    }
                }
                throw th;
            }
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(str, StandardCharsets.UTF_8.name());
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            Slog.e(TAG, "No start tag found in settings file");
            try {
                str.close();
            } catch (IOException e9) {
                Slog.e(TAG, "close the FileInputStream failed");
            }
            return false;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type2 = parser.next();
            if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                try {
                    str.close();
                } catch (IOException e10) {
                    Slog.e(TAG, "close the FileInputStream failed");
                }
                return true;
            } else if (!(type2 == 3 || type2 == 4)) {
                String tagName = parser.getName();
                if (AppActConstant.VERSION.equals(tagName)) {
                    readCompatVersion(parser);
                } else if ("package".equals(tagName)) {
                    readCompatPackage(parser);
                } else {
                    Slog.w(TAG, "Unknown element under <packages>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        return false;
    }

    private void readCompatVersion(XmlPullParser parser) throws XmlPullParserException, IOException {
        String versionStr = parser.getAttributeValue(null, ATTR_CERT_VERSION);
        if (versionStr != null) {
            try {
                this.mLastCertVersion = Integer.parseInt(versionStr);
            } catch (NumberFormatException e) {
                this.mLastCertVersion = -1;
            }
        }
        Slog.i(TAG, "Last version = " + this.mLastCertVersion + ", current version = 4");
    }

    /* access modifiers changed from: package-private */
    public void readCompatPackage(XmlPullParser parser) throws XmlPullParserException, IOException {
        String packName = parser.getAttributeValue(null, ATTR_NAME);
        String codePath = parser.getAttributeValue(null, ATTR_CODEPATH);
        String timeStamp = parser.getAttributeValue(null, ATTR_FT);
        String certType = parser.getAttributeValue(null, ATTR_CERT);
        if (TextUtils.isEmpty(packName) || TextUtils.isEmpty(codePath) || TextUtils.isEmpty(timeStamp) || TextUtils.isEmpty(certType)) {
            Slog.d(TAG, "invalid compat package, skip it");
            return;
        }
        this.mPackages.put(packName, new Package(packName.intern(), codePath, Long.parseLong(timeStamp), certType));
    }

    private void readPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException, IllegalArgumentException {
        WhiteListPackage info;
        String packageName = parser.getAttributeValue(null, ATTR_NAME);
        if (!TextUtils.isEmpty(packageName)) {
            if (this.mWhiteList.containsKey(packageName)) {
                info = this.mWhiteList.get(packageName);
            } else {
                info = new WhiteListPackage(packageName.intern());
            }
            if (info == null) {
                XmlUtils.skipCurrentTag(parser);
                return;
            }
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals(TAG_VERSIONS)) {
                        readVersionsLPw(parser, info);
                    } else {
                        XmlUtils.skipCurrentTag(parser);
                    }
                    this.mWhiteList.put(info.packageName, info);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void insertCompatPackage(String packageName, PackageSetting ps) {
        if (packageName != null && ps != null) {
            String type = getNewSignTpye(ps.signatures.mSigningDetails.signatures);
            Slog.d(TAG, "insertCompatPackage:" + packageName + " and the codePath is " + ps.codePathString);
            this.mPackages.put(packageName, new Package(packageName, ps.codePathString, ps.timeStamp, type));
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void writeCertCompatPackages() {
        if (this.mCompatiblePackageFilename.exists()) {
            if (!this.mBackupCompatiblePackageFilename.exists()) {
                if (!this.mCompatiblePackageFilename.renameTo(this.mBackupCompatiblePackageFilename)) {
                    return;
                }
            } else if (this.mCompatiblePackageFilename.delete()) {
                Slog.wtf(TAG, "Failed to clean up CompatPackage file: " + this.mCompatiblePackageFilename);
            }
        }
        try {
            FileOutputStream fstr = new FileOutputStream(this.mCompatiblePackageFilename);
            BufferedOutputStream str = new BufferedOutputStream(fstr);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(str, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "packages");
            serializer.startTag(null, AppActConstant.VERSION);
            XmlUtils.writeIntAttribute(serializer, ATTR_CERT_VERSION, 4);
            serializer.endTag(null, AppActConstant.VERSION);
            for (Package info : this.mPackages.values()) {
                serializer.startTag(null, "package");
                XmlUtils.writeStringAttribute(serializer, ATTR_NAME, info.packageName);
                XmlUtils.writeStringAttribute(serializer, ATTR_CODEPATH, info.codePath);
                XmlUtils.writeStringAttribute(serializer, ATTR_FT, String.valueOf(info.timeStamp));
                XmlUtils.writeStringAttribute(serializer, ATTR_CERT, info.certType);
                serializer.endTag(null, "package");
            }
            serializer.endTag(null, "packages");
            serializer.endDocument();
            str.flush();
            FileUtils.sync(fstr);
            if (this.mBackupCompatiblePackageFilename.exists() && !this.mBackupCompatiblePackageFilename.delete()) {
                Slog.wtf(TAG, "Failed to clean up CompatPackage back up file: " + this.mBackupCompatiblePackageFilename);
            }
            FileUtils.setPermissions(this.mCompatiblePackageFilename.toString(), 432, -1, -1);
            IoUtils.closeQuietly(fstr);
        } catch (IOException e) {
            Slog.wtf(TAG, "Unable to write CompatPackage, current changes will be lost at reboot", e);
            IoUtils.closeQuietly((AutoCloseable) null);
            if (this.mCompatiblePackageFilename.exists() && !this.mCompatiblePackageFilename.delete()) {
                Slog.wtf(TAG, "Failed to clean up CompatPackage file: " + this.mCompatiblePackageFilename);
            }
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static final class WhiteListPackage {
        List<byte[]> hashList = new ArrayList();
        String packageName;

        WhiteListPackage(String packageName2) {
            this.packageName = packageName2;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class Package {
        String certType;
        String codePath;
        String packageName;
        long timeStamp;

        Package(String packageName2, String codePath2, long timeStamp2, String certType2) {
            this.packageName = packageName2;
            this.codePath = codePath2;
            this.timeStamp = timeStamp2;
            this.certType = certType2;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSystemSignatureUpdated(Signature[] oldSignature, Signature[] newSignature) {
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

    /* access modifiers changed from: package-private */
    public void removeCertCompatPackage(String name) {
        if (this.mPackages.containsKey(name)) {
            Slog.d(TAG, "Remove package: " + name);
            this.mPackages.remove(name);
        }
    }

    /* access modifiers changed from: package-private */
    public Package getCompatPackage(String name) {
        return this.mPackages.get(name);
    }

    /* access modifiers changed from: package-private */
    public Collection<Package> getALLCompatPackages() {
        return this.mPackages.values();
    }

    /* access modifiers changed from: package-private */
    public boolean isSystemSignatureForWhiteList(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return false;
        }
        for (Signature[] sign : this.mSignsV2.values()) {
            if (PackageManagerServiceUtils.compareSignatures(sign, signs) == 0) {
                return true;
            }
        }
        return isOldSystemSignature(signs);
    }

    /* access modifiers changed from: package-private */
    public boolean isOldSystemSignature(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return false;
        }
        for (Signature[] sign : this.mSigns[this.oldSignaturesIndex].values()) {
            if (PackageManagerServiceUtils.compareSignatures(sign, signs) == 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isLegacySignature(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return false;
        }
        for (int i = 0; i <= this.oldSignaturesIndex; i++) {
            for (Signature[] s : this.mSigns[i].values()) {
                if (PackageManagerServiceUtils.compareSignatures(s, signs) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isCompatAllLegacyPackages() {
        return this.mCompatAll;
    }

    /* access modifiers changed from: package-private */
    public boolean isWhiteListedApp(PackageParser.Package pkg, boolean isBootScan) {
        if (!this.mFoundWhiteListFile) {
            return false;
        }
        if (this.mWhiteList.size() == 0) {
            readWhiteList();
        }
        File file = new File(pkg.baseCodePath);
        String pkgName = pkg.packageName;
        boolean isWhiteListedApp = false;
        if (this.mWhiteList.containsKey(pkgName)) {
            List<byte[]> storedHash = this.mWhiteList.get(pkgName).hashList;
            byte[] computedHash = getSHA256(file);
            Iterator<byte[]> it = storedHash.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (MessageDigest.isEqual(it.next(), computedHash)) {
                        Slog.i(TAG, "found whitelist package:" + pkgName);
                        isWhiteListedApp = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (!isBootScan) {
            this.mWhiteList.clear();
        }
        return isWhiteListedApp;
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mWhiteList.clear();
    }

    /* access modifiers changed from: package-private */
    public String getSignTpyeForWhiteList(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return null;
        }
        for (Map.Entry<String, Signature[]> entry : this.mSignsV2.entrySet()) {
            if (PackageManagerServiceUtils.compareSignatures(signs, entry.getValue()) == 0) {
                return entry.getKey();
            }
        }
        return getOldSignTpye(signs);
    }

    /* access modifiers changed from: package-private */
    public String getOldSignTpye(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return null;
        }
        for (Map.Entry<String, Signature[]> entry : this.mSigns[this.oldSignaturesIndex].entrySet()) {
            if (PackageManagerServiceUtils.compareSignatures(signs, entry.getValue()) == 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getNewSignTpye(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return null;
        }
        for (Map.Entry<String, Signature[]> entry : this.mSigns[this.newSignaturesIndex].entrySet()) {
            if (PackageManagerServiceUtils.compareSignatures(signs, entry.getValue()) == 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Signature[] getOldSign(String type) {
        if (!this.mFoundSystemCertFile) {
            return new Signature[0];
        }
        return this.mSigns[this.oldSignaturesIndex].get(type);
    }

    /* access modifiers changed from: package-private */
    public Signature[] getNewSign(String type) {
        if (!this.mFoundSystemCertFile) {
            return new Signature[0];
        }
        return this.mSigns[this.newSignaturesIndex].get(type);
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

    private byte[] getManifestFile(File apkFile) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        ZipFile zipFile = null;
        InputStream zipInputStream = null;
        try {
            zipFile = new ZipFile(apkFile);
            ZipEntry ze = zipFile.getEntry(MANIFEST_NAME);
            if (ze != null) {
                try {
                    zipInputStream = zipFile.getInputStream(ze);
                    if (zipInputStream != null) {
                        while (true) {
                            int length = zipInputStream.read(b);
                            if (length <= 0) {
                                break;
                            }
                            os.write(b, 0, length);
                        }
                        byte[] byteArray = os.toByteArray();
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                        }
                        return byteArray;
                    }
                    IoUtils.closeQuietly(zipInputStream);
                } catch (IOException e2) {
                    Slog.e(TAG, "get manifest file failed due to IOException");
                } finally {
                    IoUtils.closeQuietly(zipInputStream);
                }
            }
            try {
                zipFile.close();
            } catch (IOException e3) {
            }
        } catch (IOException e4) {
            Slog.e(TAG, " get manifest file failed due to IOException");
            if (zipFile != null) {
                zipFile.close();
            }
        } catch (Throwable th) {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
        return new byte[0];
    }

    private byte[] decodeHash(String hash) throws IllegalArgumentException {
        byte[] input = hash.getBytes(StandardCharsets.UTF_8);
        int N = input.length;
        if (N % 2 == 0) {
            byte[] sig = new byte[(N / 2)];
            int sigIndex = 0;
            int hi = 0;
            while (hi < N) {
                int i = hi + 1;
                sig[sigIndex] = (byte) ((parseHexDigit(input[hi]) << 4) | parseHexDigit(input[i]));
                hi = i + 1;
                sigIndex++;
            }
            return sig;
        }
        throw new IllegalArgumentException("text size " + N + " is not even");
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
