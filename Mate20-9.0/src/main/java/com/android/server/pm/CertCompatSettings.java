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
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.BufferedOutputStream;
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
import java.util.Map;
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
    private HashMap<String, Signature[]> mNewSigns = new HashMap<>();
    private HashMap<String, Signature[]> mOldSigns = new HashMap<>();
    final HashMap<String, Package> mPackages = new HashMap<>();
    private final File mSyscertFilename;
    final HashMap<String, WhiteListPackage> mWhiteList = new HashMap<>();
    private final File mWhiteListFilename;

    static final class Package {
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

    private static final class WhiteListPackage {
        List<byte[]> hashList = new ArrayList();
        String packageName;

        WhiteListPackage(String packageName2) {
            this.packageName = packageName2;
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

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003e A[SYNTHETIC, Splitter:B:14:0x003e] */
    private boolean loadTrustedCerts(File file) {
        FileInputStream str;
        int type;
        if (!file.exists()) {
            Slog.d(TAG, "system cert file not found");
            return false;
        }
        str = null;
        try {
            str = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(str, StandardCharsets.UTF_8.name());
            while (true) {
                int next = parser.next();
                type = next;
                if (next != 2 && type != 1) {
                    Slog.e(TAG, "loadTrustedCerts");
                } else if (type == 2) {
                    closeStream(str);
                    return false;
                } else {
                    int outerDepth = parser.getDepth();
                    while (true) {
                        int next2 = parser.next();
                        int type2 = next2;
                        if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                            closeStream(str);
                        } else if (type2 != 3) {
                            if (type2 != 4) {
                                if (parser.getName().equals("sigs")) {
                                    readCerts(parser);
                                } else {
                                    XmlUtils.skipCurrentTag(parser);
                                }
                            }
                        }
                    }
                    closeStream(str);
                    return true;
                }
            }
            if (type == 2) {
            }
        } catch (XmlPullParserException e) {
            Slog.e(TAG, "getTrustedCerts error duing to XmlPullParserException");
        } catch (IOException e2) {
            Slog.e(TAG, "getTrustedCerts error duing to IOException");
        } catch (Throwable th) {
            closeStream(str);
            throw th;
        }
        closeStream(str);
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
        if (certVersion.equals(SIGNATURE_VERSION_ONE) || certVersion.equals(SIGNATURE_VERSION_TWO)) {
            int outerDepth = parser.getDepth();
            while (true) {
                int next = parser.next();
                int type = next;
                if (next != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0032 A[SYNTHETIC, Splitter:B:11:0x0032] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003f A[SYNTHETIC, Splitter:B:16:0x003f] */
    private void readWhiteList() {
        int type;
        if (this.mFoundWhiteListFile) {
            FileInputStream str = null;
            try {
                str = new FileInputStream(this.mWhiteListFilename);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(str, StandardCharsets.UTF_8.name());
                while (true) {
                    int next = parser.next();
                    type = next;
                    if (next != 2 && type != 1) {
                        Slog.e(TAG, "readWhiteList");
                    } else if (type == 2) {
                        try {
                            str.close();
                        } catch (IOException e) {
                            Slog.e(TAG, "close FileInputStream failed");
                        }
                        return;
                    } else {
                        int outerDepth = parser.getDepth();
                        while (true) {
                            int next2 = parser.next();
                            int type2 = next2;
                            if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                                try {
                                    break;
                                } catch (IOException e2) {
                                    Slog.e(TAG, "close FileInputStream failed");
                                }
                            } else if (type2 != 3) {
                                if (type2 != 4) {
                                    if (parser.getName().equals("package")) {
                                        readPackageLPw(parser);
                                    } else {
                                        Slog.w(TAG, "Unknown element under <packages>: " + parser.getName());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            }
                        }
                        return;
                    }
                }
                if (type == 2) {
                }
            } catch (XmlPullParserException e3) {
                Slog.e(TAG, "readWhiteList error duing to XmlPullParserException");
                if (str != null) {
                    str.close();
                }
            } catch (IOException e4) {
                Slog.e(TAG, "readWhiteList error duing to IOException");
                if (str != null) {
                    str.close();
                }
            } catch (Throwable th) {
                if (str != null) {
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
                int next = parser.next();
                int type = next;
                if (next != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00cd A[SYNTHETIC, Splitter:B:46:0x00cd] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0118 A[SYNTHETIC, Splitter:B:62:0x0118] */
    public boolean readCertCompatPackages() {
        int type;
        FileInputStream str = null;
        if (this.mBackupCompatiblePackageFilename.exists()) {
            try {
                str = new FileInputStream(this.mBackupCompatiblePackageFilename);
                if (this.mCompatiblePackageFilename.exists()) {
                    Slog.w(TAG, "Cleaning up whitelist file " + this.mWhiteListFilename);
                    if (!this.mCompatiblePackageFilename.delete()) {
                        Slog.wtf(TAG, "Failed to clean up CompatPackage back up file: " + this.mBackupCompatiblePackageFilename);
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
                    PackageManagerService.reportSettingsProblem(4, "No whitelist file; creating initial state");
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
                return false;
            } catch (IOException e5) {
                Slog.e(TAG, "readCompatPackages error duing to IOException");
                if (str != null) {
                    str.close();
                }
                return false;
            } catch (IllegalArgumentException e6) {
                Slog.e(TAG, "readCompatPackages error duing to IllegalArgumentException");
                if (str != null) {
                    try {
                        str.close();
                    } catch (IOException e7) {
                        Slog.e(TAG, "close the FileInputStream failed");
                    }
                }
                return false;
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
        while (true) {
            int next = parser.next();
            type = next;
            if (next != 2 && type != 1) {
                Slog.e(TAG, "readCertCompatPackages");
            } else if (type == 2) {
                if (str != null) {
                    try {
                        str.close();
                    } catch (IOException e9) {
                        Slog.e(TAG, "close the FileInputStream failed");
                    }
                }
                return false;
            } else {
                int outerDepth = parser.getDepth();
                while (true) {
                    int next2 = parser.next();
                    int type2 = next2;
                    if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        if (str != null) {
                            try {
                                str.close();
                            } catch (IOException e10) {
                                Slog.e(TAG, "close the FileInputStream failed");
                            }
                        }
                    } else if (type2 != 3) {
                        if (type2 != 4) {
                            if (parser.getName().equals("package")) {
                                readCompatPackage(parser);
                            } else {
                                Slog.w(TAG, "Unknown element under <packages>: " + parser.getName());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                }
                if (str != null) {
                }
                return true;
            }
        }
        if (type == 2) {
        }
    }

    /* access modifiers changed from: package-private */
    public void readCompatPackage(XmlPullParser parser) throws XmlPullParserException, IOException {
        String packName = parser.getAttributeValue(null, "name");
        String codePath = parser.getAttributeValue(null, ATTR_CODEPATH);
        String timeStamp = parser.getAttributeValue(null, ATTR_FT);
        String certType = parser.getAttributeValue(null, "cert");
        if (TextUtils.isEmpty(packName) || TextUtils.isEmpty(codePath) || TextUtils.isEmpty(timeStamp) || TextUtils.isEmpty(certType)) {
            Slog.d(TAG, "invalid compat package, skip it");
            return;
        }
        Package info = new Package(packName, codePath, Long.parseLong(timeStamp), certType);
        this.mPackages.put(packName, info);
    }

    private void readPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException, IllegalArgumentException {
        WhiteListPackage info;
        String packageName = parser.getAttributeValue(null, "name");
        if (!TextUtils.isEmpty(packageName)) {
            if (this.mWhiteList.containsKey(packageName)) {
                info = this.mWhiteList.get(packageName);
            } else {
                info = new WhiteListPackage(packageName);
            }
            if (info != null) {
                int outerDepth = parser.getDepth();
                while (true) {
                    int next = parser.next();
                    int type = next;
                    if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
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

    /* access modifiers changed from: package-private */
    public void insertCompatPackage(String packageName, PackageSetting ps) {
        if (packageName != null && ps != null) {
            String type = getNewSignTpye(ps.signatures.mSigningDetails.signatures);
            Slog.d(TAG, "insertCompatPackage:" + packageName + " and the codePath is " + ps.codePathString);
            HashMap<String, Package> hashMap = this.mPackages;
            Package packageR = new Package(packageName, ps.codePathString, ps.timeStamp, type);
            hashMap.put(packageName, packageR);
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
            FileUtils.sync(fstr);
            if (this.mBackupCompatiblePackageFilename.exists() && !this.mBackupCompatiblePackageFilename.delete()) {
                Slog.wtf(TAG, "Failed to clean up CompatPackage back up file: " + this.mBackupCompatiblePackageFilename);
            }
            FileUtils.setPermissions(this.mCompatiblePackageFilename.toString(), 432, -1, -1);
            IoUtils.closeQuietly(fstr);
        } catch (IOException e) {
            Slog.wtf(TAG, "Unable to write CompatPackage, current changes will be lost at reboot", e);
            IoUtils.closeQuietly(null);
            if (this.mCompatiblePackageFilename.exists() && !this.mCompatiblePackageFilename.delete()) {
                Slog.wtf(TAG, "Failed to clean up CompatPackage file: " + this.mCompatiblePackageFilename);
            }
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            throw th;
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
            Slog.d(TAG, "removeCertCompatPackage" + name);
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
    public boolean isOldSystemSignature(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return false;
        }
        for (Signature[] s : this.mOldSigns.values()) {
            if (PackageManagerServiceUtils.compareSignatures(s, signs) == 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isWhiteListedApp(PackageParser.Package pkg) {
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
        List<byte[]> storedHash = this.mWhiteList.get(pkgName).hashList;
        byte[] computedHash = getSHA256(file);
        for (byte[] b : storedHash) {
            if (MessageDigest.isEqual(b, computedHash)) {
                Slog.i(TAG, "found whitelist package:" + pkgName);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public String getOldSignTpye(Signature[] signs) {
        if (!this.mFoundSystemCertFile) {
            return null;
        }
        for (Map.Entry<String, Signature[]> entry : this.mOldSigns.entrySet()) {
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
        for (Map.Entry<String, Signature[]> entry : this.mNewSigns.entrySet()) {
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
        return this.mOldSigns.get(type);
    }

    /* access modifiers changed from: package-private */
    public Signature[] getNewSign(String type) {
        if (!this.mFoundSystemCertFile) {
            return new Signature[0];
        }
        return this.mNewSigns.get(type);
    }

    private byte[] getSHA256(File file) {
        byte[] manifest = HwUtils.getManifestFile(file);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(manifest);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            Slog.e(TAG, "get sha256 failed");
            return new byte[0];
        }
    }

    private byte[] decodeHash(String hash) throws IllegalArgumentException {
        byte[] input = hash.getBytes(StandardCharsets.UTF_8);
        int len = input.length;
        if (len % 2 == 0) {
            byte[] sig = new byte[(len / 2)];
            int sigIndex = 0;
            int i = 0;
            while (i < len) {
                int i2 = i + 1;
                sig[sigIndex] = (byte) ((parseHexDigit(input[i]) << 4) | parseHexDigit(input[i2]));
                i = i2 + 1;
                sigIndex++;
            }
            return sig;
        }
        throw new IllegalArgumentException("text size " + len + " is not even");
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
