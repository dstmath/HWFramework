package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.security.securitydiagnose.AntiMalApkInfo;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import libcore.io.IoUtils;
import org.apache.commons.codec.binary.Base64;
import org.xmlpull.v1.XmlPullParser;

public class AntiMalPreInstallScanner {
    private static final int CACHE_SIZE = 1024;
    private static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final String[] COMPONENT_ARRY = new String[]{"system/etc/", "version/", "product/etc/", "cust/"};
    private static final boolean DEBUG = false;
    private static final String ENCRYPT_ARG = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
    private static final boolean HW_DEBUG;
    private static final String KEY_ALGORITHM = "RSA";
    private static final String PATH_SLANT = "/";
    private static final String PKGLIST_SIGN_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArO6tIeIxD78HrazoYkAeKdXhKINVUE1fAwnXb6OiabTYf3qO22wcFoqyKsm2tlaWrDU8+hnxjkZOLIvpcJ0bEDAkICoIRNGBoJzJzN6PyIOyfLd4IOA/bS071jaA5JjLGpMkBKYuhzECnK/pmruKngl3ED/t8HRw44ku1rabcwJjKl8dF4D0ogoosrr8mrwfnQaJpkmTL1oScF/Mr4plkrUdw3Ab00HZoklMVznT+M5KV8DmEjo8PIYkdFlJCwEx4Cj6PXKHfBEGeivyPe2W1/EnYdaREu4GO9ZLBsIhRhS3b7UY5UFsjbYBK23M4zrpZlMVQer4zyqmzefs25BYAwIDAQAB";
    private static final String[] PREINSTALL_APK_DIR = new String[]{"/system/", "/oem/app/", "/version/", "/product/", "/cust/"};
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String SYSAPK_SIGN_PATH = "xml/sign";
    private static final String SYSAPK_WHITE_LIST_PATH = "xml/criticalpro.xml";
    private static final String TAG = "AntiMalPreInstallScanner";
    private static Context mContext;
    private static AntiMalPreInstallScanner mInstance;
    private static boolean mIsOtaBoot;
    private HashMap<String, ApkBasicInfo> mApkInfoList = new HashMap();
    private AntiMalDataManager mDataManager = new AntiMalDataManager(mIsOtaBoot);
    private long mDeviceFirstUseTime;
    private boolean mNeedScan = this.mDataManager.needScanIllegalApks();
    private HashMap<String, AntiMalApkInfo> mOldIllegalApks = new HashMap();
    private AntiMalPreInstallReport mReport = new AntiMalPreInstallReport(this.mDataManager);
    private HashMap<String, ArrayList<ApkBasicInfo>> mSysApkWhitelist = new HashMap();

    private static class ApkBasicInfo {
        public boolean mExist = false;
        public final String[] mHashCode;
        public final String mPackagename;
        public final String mPath;
        public final String mVersion;

        ApkBasicInfo(String packagename, String path, String[] hashCodeArry, String version) {
            this.mPackagename = packagename;
            this.mPath = path;
            this.mHashCode = hashCodeArry;
            this.mVersion = version;
        }
    }

    static {
        boolean z;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 4);
        } else {
            z = false;
        }
        HW_DEBUG = z;
    }

    public static void init(Context contxt, boolean isOtaBoot) {
        mContext = contxt;
        mIsOtaBoot = isOtaBoot;
        if (HW_DEBUG) {
            Slog.d(TAG, "init isOtaBoot = " + isOtaBoot);
        }
    }

    public static AntiMalPreInstallScanner getInstance() {
        AntiMalPreInstallScanner antiMalPreInstallScanner;
        synchronized (AntiMalPreInstallScanner.class) {
            if (mInstance == null) {
                mInstance = new AntiMalPreInstallScanner();
            }
            antiMalPreInstallScanner = mInstance;
        }
        return antiMalPreInstallScanner;
    }

    private AntiMalPreInstallScanner() {
        loadOldAntiMalList();
    }

    public void systemReady() {
        if (CHINA_RELEASE_VERSION) {
            if (this.mNeedScan) {
                checkDeletedIllegallyApks();
            }
            this.mReport.report(null);
        }
    }

    public List<String> getSysWhiteList() {
        if (this.mApkInfoList != null && (this.mApkInfoList.isEmpty() ^ 1) != 0) {
            return new ArrayList(this.mApkInfoList.keySet());
        }
        if (!CHINA_RELEASE_VERSION) {
            return null;
        }
        long timeStart = System.currentTimeMillis();
        String[] strArr = COMPONENT_ARRY;
        int i = 0;
        int length = strArr.length;
        while (i < length) {
            String componentPath = strArr[i];
            if (HW_DEBUG) {
                Log.d(TAG, "getSysWhitelist componentPath " + componentPath);
            }
            File apkListFile = new File(componentPath, SYSAPK_WHITE_LIST_PATH);
            if (apkListFile.exists()) {
                if (HW_DEBUG) {
                    Slog.d(TAG, "getSysWhitelist the white list file = " + apkListFile.getAbsolutePath());
                }
                File signFile = new File(componentPath, SYSAPK_SIGN_PATH);
                if (signFile.exists()) {
                    if (!verify(fileToByte(apkListFile), PKGLIST_SIGN_PUBLIC_KEY, readFromFile(signFile))) {
                        Slog.w(TAG, "getSysWhitelist System package list verify failed");
                        return null;
                    } else if (parsePackagelist(apkListFile)) {
                        i++;
                    } else {
                        Slog.w(TAG, "getSysWhitelist Sign verified, but parsing whitelist failed");
                        return null;
                    }
                }
                if (HW_DEBUG) {
                    Slog.w(TAG, "Apk sign File does not exist");
                }
                return null;
            }
            if (HW_DEBUG) {
                Slog.w(TAG, "Apk List File does not exist");
            }
            return null;
        }
        if (HW_DEBUG) {
            Slog.d(TAG, "getSysWhiteList TIME = " + (System.currentTimeMillis() - timeStart));
        }
        return new ArrayList(this.mApkInfoList.keySet());
    }

    private void loadOldAntiMalList() {
        ArrayList<AntiMalApkInfo> oldList = this.mDataManager.getOldApkInfoList();
        if (oldList != null) {
            synchronized (this.mOldIllegalApks) {
                for (AntiMalApkInfo ai : oldList) {
                    if (ai.mType == 1 || ai.mType == 2) {
                        this.mOldIllegalApks.put(ai.mPackageName, ai);
                    }
                }
            }
        }
    }

    public void loadSysWhitelist() {
        if (!CHINA_RELEASE_VERSION) {
            return;
        }
        if (this.mNeedScan) {
            long timeStart = System.currentTimeMillis();
            for (String componentPath : COMPONENT_ARRY) {
                if (HW_DEBUG) {
                    Log.d(TAG, "loadSysWhitelist componentPath " + componentPath);
                }
                File apkListFile = new File(componentPath, SYSAPK_WHITE_LIST_PATH);
                AntiMalComponentInfo aci = new AntiMalComponentInfo(componentPath);
                if (apkListFile.exists()) {
                    if (HW_DEBUG) {
                        Slog.d(TAG, "loadSysWhitelist the white list file = " + apkListFile.getAbsolutePath());
                    }
                    File signFile = new File(componentPath, SYSAPK_SIGN_PATH);
                    if (signFile.exists()) {
                        try {
                            if (verify(fileToByte(apkListFile), PKGLIST_SIGN_PUBLIC_KEY, readFromFile(signFile))) {
                                aci.setVerifyStatus(0);
                                if (!parsePackagelist(apkListFile)) {
                                    aci.setVerifyStatus(4);
                                    Slog.e(TAG, "loadSysWhitelist Sign verified, but parsing whitelist failed, componentPath = " + componentPath);
                                }
                                this.mDataManager.addComponentInfo(aci);
                            } else {
                                Slog.e(TAG, "loadSysWhitelist System package list verify failed componentPath = " + componentPath);
                                aci.setVerifyStatus(3);
                                this.mDataManager.addComponentInfo(aci);
                            }
                        } catch (Exception e) {
                            Slog.e(TAG, "loadSysWhitelist Exception failed: " + e);
                            aci.setVerifyStatus(3);
                            this.mDataManager.addComponentInfo(aci);
                        }
                    } else {
                        Slog.e(TAG, "loadSysWhitelist sign not exist! componentPath = " + componentPath);
                        aci.setVerifyStatus(2);
                        this.mDataManager.addComponentInfo(aci);
                    }
                } else {
                    Slog.e(TAG, "loadSysWhitelist criticalpro not exist. componentPath = " + componentPath);
                    aci.setVerifyStatus(1);
                    this.mDataManager.addComponentInfo(aci);
                }
            }
            if (HW_DEBUG) {
                Slog.d(TAG, "loadSysWhitelist TIME = " + (System.currentTimeMillis() - timeStart));
            }
            return;
        }
        if (HW_DEBUG) {
            Slog.d(TAG, "loadSysWhitelist no need load!");
        }
    }

    private byte[] fileToByte(File file) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        Object out;
        Object in;
        byte[] data = null;
        AutoCloseable in2 = null;
        AutoCloseable out2 = null;
        if (file.exists()) {
            try {
                FileInputStream in3 = new FileInputStream(file);
                try {
                    ByteArrayOutputStream out3 = new ByteArrayOutputStream(2048);
                    try {
                        byte[] cache = new byte[1024];
                        while (true) {
                            int nRead = in3.read(cache);
                            if (nRead == -1) {
                                break;
                            }
                            out3.write(cache, 0, nRead);
                        }
                        out3.flush();
                        data = out3.toByteArray();
                        IoUtils.closeQuietly(in3);
                        IoUtils.closeQuietly(out3);
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        out2 = out3;
                        in2 = in3;
                        try {
                            Slog.e(TAG, "fileToByte FileNotFoundException " + e);
                            IoUtils.closeQuietly(in2);
                            IoUtils.closeQuietly(out2);
                            return data;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(in2);
                            IoUtils.closeQuietly(out2);
                            throw th;
                        }
                    } catch (IOException e4) {
                        e2 = e4;
                        out = out3;
                        in2 = in3;
                        Slog.e(TAG, "fileToByte IOException! " + e2);
                        IoUtils.closeQuietly(in2);
                        IoUtils.closeQuietly(out2);
                        return data;
                    } catch (Throwable th3) {
                        th = th3;
                        out = out3;
                        in2 = in3;
                        IoUtils.closeQuietly(in2);
                        IoUtils.closeQuietly(out2);
                        throw th;
                    }
                } catch (FileNotFoundException e5) {
                    e = e5;
                    in2 = in3;
                    Slog.e(TAG, "fileToByte FileNotFoundException " + e);
                    IoUtils.closeQuietly(in2);
                    IoUtils.closeQuietly(out2);
                    return data;
                } catch (IOException e6) {
                    e2 = e6;
                    in2 = in3;
                    Slog.e(TAG, "fileToByte IOException! " + e2);
                    IoUtils.closeQuietly(in2);
                    IoUtils.closeQuietly(out2);
                    return data;
                } catch (Throwable th4) {
                    th = th4;
                    in2 = in3;
                    IoUtils.closeQuietly(in2);
                    IoUtils.closeQuietly(out2);
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                e = e7;
                Slog.e(TAG, "fileToByte FileNotFoundException " + e);
                IoUtils.closeQuietly(in2);
                IoUtils.closeQuietly(out2);
                return data;
            } catch (IOException e8) {
                e2 = e8;
                Slog.e(TAG, "fileToByte IOException! " + e2);
                IoUtils.closeQuietly(in2);
                IoUtils.closeQuietly(out2);
                return data;
            }
        }
        return data;
    }

    private String readFromFile(File file) {
        FileNotFoundException e;
        Object br;
        IOException e2;
        Throwable th;
        StringBuffer readBuf = new StringBuffer();
        AutoCloseable br2 = null;
        try {
            BufferedReader br3 = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            try {
                for (String data = br3.readLine(); data != null; data = br3.readLine()) {
                    readBuf.append(data);
                }
                IoUtils.closeQuietly(br3);
                BufferedReader bufferedReader = br3;
            } catch (FileNotFoundException e3) {
                e = e3;
                br2 = br3;
                Slog.e(TAG, "readFromFile FileNotFoundException :" + e);
                IoUtils.closeQuietly(br2);
                return readBuf.toString();
            } catch (IOException e4) {
                e2 = e4;
                br2 = br3;
                try {
                    Slog.e(TAG, "readFromFile IOException :" + e2);
                    IoUtils.closeQuietly(br2);
                    return readBuf.toString();
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(br2);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                br2 = br3;
                IoUtils.closeQuietly(br2);
                throw th;
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            Slog.e(TAG, "readFromFile FileNotFoundException :" + e);
            IoUtils.closeQuietly(br2);
            return readBuf.toString();
        } catch (IOException e6) {
            e2 = e6;
            Slog.e(TAG, "readFromFile IOException :" + e2);
            IoUtils.closeQuietly(br2);
            return readBuf.toString();
        }
        return readBuf.toString();
    }

    private boolean verify(byte[] data, String publicKey, String sign) {
        if (data == null || data.length == 0 || sign == null) {
            Slog.e(TAG, "verify Input invalid!");
            return false;
        }
        try {
            PublicKey publicK = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(publicKey.getBytes("UTF-8"))));
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicK);
            signature.update(data);
            return signature.verify(Base64.decodeBase64(sign.getBytes("UTF-8")));
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "verify IllegalArgumentException : " + e);
        } catch (UnsupportedEncodingException e2) {
            Slog.e(TAG, "verify UnsupportedEncodingException : " + e2);
        } catch (NoSuchAlgorithmException e3) {
            Slog.e(TAG, "verify NoSuchAlgorithmException : " + e3);
        } catch (InvalidKeySpecException e4) {
            Slog.e(TAG, "verify InvalidKeySpecException : " + e4);
        } catch (InvalidKeyException e5) {
            Slog.e(TAG, "verify InvalidKeyException : " + e5);
        } catch (SignatureException e6) {
            Slog.e(TAG, "verify SignatureException : " + e6);
        }
        return false;
    }

    private boolean isPreinstallApkDir(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        String apkPath = path;
        if (!path.startsWith(PATH_SLANT)) {
            apkPath = PATH_SLANT + path;
        }
        for (String str : PREINSTALL_APK_DIR) {
            if (apkPath.contains(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean parsePackagelist(File whitelist) {
        Exception e;
        Throwable th;
        boolean retVal = true;
        AutoCloseable autoCloseable = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            FileInputStream fin = new FileInputStream(whitelist);
            try {
                int type;
                parser.setInput(fin, "UTF-8");
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    Slog.e(TAG, "No start tag found in Package white list.");
                    IoUtils.closeQuietly(fin);
                    return false;
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        IoUtils.closeQuietly(fin);
                    } else if (!(type == 3 || type == 4 || !parser.getName().equals("package"))) {
                        String packageName = parser.getAttributeValue(null, "name");
                        String path = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_PATH);
                        String sign = parser.getAttributeValue(null, "ss");
                        String version = parser.getAttributeValue(null, "version");
                        if (!(TextUtils.isEmpty(packageName) || TextUtils.isEmpty(path) || TextUtils.isEmpty(sign) || !isPreinstallApkDir(path))) {
                            ApkBasicInfo pbi = new ApkBasicInfo(packageName, path, sign.split(","), version);
                            this.mApkInfoList.put(packageName, pbi);
                            ArrayList<ApkBasicInfo> plist;
                            if (this.mSysApkWhitelist.get(packageName) == null) {
                                plist = new ArrayList();
                                plist.add(pbi);
                                this.mSysApkWhitelist.put(packageName, plist);
                            } else {
                                plist = (ArrayList) this.mSysApkWhitelist.get(packageName);
                                plist.add(pbi);
                                this.mSysApkWhitelist.put(packageName, plist);
                            }
                        }
                    }
                }
                IoUtils.closeQuietly(fin);
                return retVal;
            } catch (Exception e2) {
                e = e2;
                autoCloseable = fin;
            } catch (Throwable th2) {
                th = th2;
                autoCloseable = fin;
            }
        } catch (Exception e3) {
            e = e3;
            try {
                Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
                retVal = false;
                IoUtils.closeQuietly(autoCloseable);
                return retVal;
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(autoCloseable);
                throw th;
            }
        }
    }

    private int stringToInt(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(str);
    }

    private void checkDeletedIllegallyApks() {
        synchronized (this.mApkInfoList) {
            for (ApkBasicInfo abi : this.mApkInfoList.values()) {
                if (!abi.mExist) {
                    if (checkApkExist(abi.mPath)) {
                        abi.mExist = true;
                    } else {
                        AntiMalApkInfo deletedApkInfo = new AntiMalApkInfo(abi.mPackagename, formatPath(abi.mPath), null, 3, null, null, stringToInt(abi.mVersion));
                        if (HW_DEBUG) {
                            Log.d(TAG, "checkDeletedIllegallyApks AntiMalApkInfo : " + deletedApkInfo);
                        }
                        setComponentAntiMalStatus(abi.mPath, 4);
                        this.mDataManager.addAntiMalApkInfo(deletedApkInfo);
                    }
                }
            }
        }
    }

    private String formatPath(String path) {
        if (path == null || !path.startsWith(PATH_SLANT)) {
            return path;
        }
        return path.substring(1, path.length());
    }

    private boolean checkApkExist(String path) {
        int i = 0;
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return false;
            }
            int length = files.length;
            while (i < length) {
                File f = files[i];
                if (f != null && isApkPath(f.getAbsolutePath())) {
                    return true;
                }
                i++;
            }
        }
        return isApkPath(path);
    }

    private boolean isApkPath(String path) {
        return path != null ? path.endsWith(".apk") : false;
    }

    private void markApkExist(Package pkg) {
        if (pkg != null) {
            synchronized (this.mApkInfoList) {
                ApkBasicInfo abi = (ApkBasicInfo) this.mApkInfoList.get(pkg.packageName);
                if (abi != null) {
                    abi.mExist = true;
                }
            }
        }
    }

    private String sha256(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return bytesToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            if (HW_DEBUG) {
                Slog.e(TAG, "get sha256 failed");
            }
            return null;
        }
    }

    private boolean compareHashcode(String[] hashcode, Package pkg) {
        if (pkg == null || (isApkPath(pkg.baseCodePath) ^ 1) != 0 || hashcode == null || hashcode.length == 0) {
            if (HW_DEBUG) {
                Slog.d(TAG, "compareHashcode not hashcode : " + (pkg != null ? pkg.baseCodePath : "null"));
            }
            return false;
        }
        android.content.pm.Signature[] apkSign = pkg.mSignatures;
        if (apkSign == null || apkSign.length == 0) {
            if (HW_DEBUG) {
                Slog.d(TAG, "compareHashcode not apk : " + pkg.baseCodePath);
            }
            return false;
        }
        String[] apkSignHashAry = new String[apkSign.length];
        int i = 0;
        while (i < apkSign.length) {
            try {
                apkSignHashAry[i] = sha256(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(apkSign[i].toByteArray()))).getSignature());
                i++;
            } catch (CertificateException e) {
                Slog.e(TAG, "compareHashcode E: " + e);
                return false;
            }
        }
        int j = 0;
        while (j < hashcode.length && j < apkSignHashAry.length) {
            if (hashcode[j].equals(apkSignHashAry[j])) {
                j++;
            } else {
                if (HW_DEBUG) {
                    Slog.d(TAG, "compareHashcode hashcode not equal pkg = " + pkg.baseCodePath + "white apk hash = " + hashcode[j] + " apk hashcod = " + apkSignHashAry[j]);
                }
                return false;
            }
        }
        return true;
    }

    private String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] hexChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int byteValue = bytes[j] & 255;
            chars[j * 2] = hexChars[byteValue >>> 4];
            chars[(j * 2) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars).toUpperCase(Locale.US);
    }

    private boolean componentValid(String apkPath) {
        AntiMalComponentInfo aci = this.mDataManager.getComponentByApkPath(apkPath);
        return aci != null ? aci.isVerifyStatusValid() : false;
    }

    private void setComponentAntiMalStatus(String path, int bitMask) {
        AntiMalComponentInfo aci = this.mDataManager.getComponentByApkPath(path);
        if (aci != null) {
            aci.setAntiMalStatus(bitMask);
        }
    }

    public int checkIllegalSysApk(Package pkg, int flags) throws PackageManagerException {
        int i = 0;
        if (!CHINA_RELEASE_VERSION) {
            return 0;
        }
        if (pkg == null) {
            Slog.e(TAG, "Invalid input args pkg(null) in checkIllegalSysApk.");
            return 0;
        } else if (this.mNeedScan) {
            markApkExist(pkg);
            if (!componentValid(pkg.baseCodePath)) {
                if (HW_DEBUG) {
                    Log.d(TAG, "checkIllegalSysApk COMPONENT INVALID! path = " + pkg.baseCodePath);
                }
                return 0;
            } else if (!isPreinstallApkDir(pkg.baseCodePath)) {
                return 0;
            } else {
                ArrayList<ApkBasicInfo> pbi = (ArrayList) this.mSysApkWhitelist.get(pkg.packageName);
                if (pbi == null) {
                    AntiMalApkInfo illegalApkInfo = new AntiMalApkInfo(pkg, 1);
                    this.mDataManager.addAntiMalApkInfo(illegalApkInfo);
                    if (HW_DEBUG) {
                        Slog.d(TAG, "checkIllegalSysApk Add illegally AntiMalApkInfo : " + illegalApkInfo);
                    }
                    setComponentAntiMalStatus(pkg.baseCodePath, 1);
                    return 1;
                }
                Iterator<ApkBasicInfo> it = pbi.iterator();
                while (it.hasNext()) {
                    ApkBasicInfo apkInfo = (ApkBasicInfo) it.next();
                    if (apkInfo.mPackagename != null && apkInfo.mPath != null && pkg.baseCodePath.contains(apkInfo.mPath) && compareHashcode(apkInfo.mHashCode, pkg)) {
                        return 0;
                    }
                }
                AntiMalApkInfo modifiedApkInfo = new AntiMalApkInfo(pkg, 2);
                if (HW_DEBUG) {
                    Slog.d(TAG, "checkIllegalSysApk Add modify AntiMalApkInfo : " + modifiedApkInfo);
                }
                this.mDataManager.addAntiMalApkInfo(modifiedApkInfo);
                return 2;
            }
        } else {
            AntiMalApkInfo ai = (AntiMalApkInfo) this.mOldIllegalApks.get(pkg.packageName);
            if (HW_DEBUG && ai != null) {
                Slog.d(TAG, "checkIllegalSysApk no need check legally AI = " + ai);
            }
            if (ai != null) {
                i = ai.mType;
            }
            return i;
        }
    }
}
