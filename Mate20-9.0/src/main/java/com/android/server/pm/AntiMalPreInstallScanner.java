package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.security.securitydiagnose.AntiMalApkInfo;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.wifipro.WifiProCommonUtils;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;

public class AntiMalPreInstallScanner {
    private static final int CACHE_SIZE = 1024;
    private static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final String[] COMPONENT_ARRY = {"system/etc/", "version/", "product/etc/", "cust/ecota/", "cust/", "preload/etc/", "preas/", "preavs/"};
    private static final boolean DEBUG = false;
    private static final String ENCRYPT_ARG = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
    private static final int EXPECTED_BUFFER_DATA = 360;
    private static final boolean HW_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String KEY_ALGORITHM = "RSA";
    private static final int MAX_DATA = 350;
    private static final String PATH_SLANT = "/";
    private static final String PKGLIST_SIGN_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArO6tIeIxD78HrazoYkAeKdXhKINVUE1fAwnXb6OiabTYf3qO22wcFoqyKsm2tlaWrDU8+hnxjkZOLIvpcJ0bEDAkICoIRNGBoJzJzN6PyIOyfLd4IOA/bS071jaA5JjLGpMkBKYuhzECnK/pmruKngl3ED/t8HRw44ku1rabcwJjKl8dF4D0ogoosrr8mrwfnQaJpkmTL1oScF/Mr4plkrUdw3Ab00HZoklMVznT+M5KV8DmEjo8PIYkdFlJCwEx4Cj6PXKHfBEGeivyPe2W1/EnYdaREu4GO9ZLBsIhRhS3b7UY5UFsjbYBK23M4zrpZlMVQer4zyqmzefs25BYAwIDAQAB";
    private static final String[] PREINSTALL_APK_DIR = {HwDelAppManager.SYSTEM_PRE_DEL_DIR, "/oem/app/", "/version/", "/product/", "/cust/ecota/", "/cust/", "/preload/", "/preas/", "/preavs/"};
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String SYSAPK_SIGN_PATH = "xml/sign";
    private static final String SYSAPK_WHITE_LIST_PATH = "xml/criticalpro.xml";
    private static final String TAG = "AntiMalPreInstallScanner";
    private static Context mContext;
    private static AntiMalPreInstallScanner mInstance;
    private static boolean mIsOtaBoot;
    private HashMap<String, ApkBasicInfo> mApkInfoList = new HashMap<>();
    private AntiMalDataManager mDataManager = new AntiMalDataManager(mIsOtaBoot);
    private long mDeviceFirstUseTime;
    private boolean mNeedScan = this.mDataManager.needScanIllegalApks();
    private HashMap<String, AntiMalApkInfo> mOldIllegalApks = new HashMap<>();
    private AntiMalPreInstallReport mReport = new AntiMalPreInstallReport(this.mDataManager);
    private HashMap<String, ArrayList<ApkBasicInfo>> mSysApkWhitelist = new HashMap<>();

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

    private AntiMalPreInstallScanner() {
        loadOldAntiMalList();
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

    public void systemReady() {
        if (CHINA_RELEASE_VERSION) {
            if (this.mNeedScan) {
                checkDeletedIllegallyApks();
            }
            this.mReport.report(null);
        }
    }

    public List<String> getSysWhiteList() {
        if (this.mApkInfoList != null && !this.mApkInfoList.isEmpty()) {
            return new ArrayList(this.mApkInfoList.keySet());
        }
        if (!CHINA_RELEASE_VERSION) {
            return null;
        }
        long timeStart = System.currentTimeMillis();
        for (String componentPath : COMPONENT_ARRY) {
            if (HW_DEBUG) {
                Log.d(TAG, "getSysWhitelist componentPath " + componentPath);
            }
            File apkListFile = new File(componentPath, SYSAPK_WHITE_LIST_PATH);
            if (apkListFile.exists()) {
                if (HW_DEBUG) {
                    Slog.d(TAG, "getSysWhitelist the white list file = " + apkListFile.getAbsolutePath());
                }
                File signFile = new File(componentPath, SYSAPK_SIGN_PATH);
                if (!signFile.exists()) {
                    if (HW_DEBUG) {
                        Slog.w(TAG, "Apk sign File does not exist");
                    }
                } else if (!verify(fileToByte(apkListFile), PKGLIST_SIGN_PUBLIC_KEY, readFromFile(signFile))) {
                    Slog.w(TAG, "getSysWhitelist System package list verify failed");
                } else if (!parsePackagelist(apkListFile)) {
                    Slog.w(TAG, "getSysWhitelist Sign verified, but parsing whitelist failed");
                }
            } else if (HW_DEBUG) {
                Slog.w(TAG, "Apk List File does not exist");
            }
        }
        if (HW_DEBUG) {
            long end = System.currentTimeMillis();
            Slog.d(TAG, "getSysWhiteList TIME = " + (end - timeStart));
        }
        return new ArrayList(this.mApkInfoList.keySet());
    }

    private void loadOldAntiMalList() {
        ArrayList<AntiMalApkInfo> oldList = this.mDataManager.getOldApkInfoList();
        if (oldList != null) {
            synchronized (this.mOldIllegalApks) {
                Iterator<AntiMalApkInfo> it = oldList.iterator();
                while (it.hasNext()) {
                    AntiMalApkInfo ai = it.next();
                    if (ai.mType == 1 || ai.mType == 2) {
                        this.mOldIllegalApks.put(ai.mPackageName, ai);
                    }
                }
            }
        }
    }

    public void loadSysWhitelist() {
        if (CHINA_RELEASE_VERSION) {
            if (!this.mNeedScan) {
                if (HW_DEBUG) {
                    Slog.d(TAG, "loadSysWhitelist no need load!");
                }
                return;
            }
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
                            if (!verify(fileToByte(apkListFile), PKGLIST_SIGN_PUBLIC_KEY, readFromFile(signFile))) {
                                Slog.e(TAG, "loadSysWhitelist System package list verify failed componentPath = " + componentPath);
                                aci.setVerifyStatus(3);
                                this.mDataManager.addComponentInfo(aci);
                            } else {
                                try {
                                    aci.setVerifyStatus(0);
                                    if (!parsePackagelist(apkListFile)) {
                                        aci.setVerifyStatus(4);
                                        Slog.e(TAG, "loadSysWhitelist Sign verified, but parsing whitelist failed, componentPath = " + componentPath);
                                    }
                                } catch (Exception e) {
                                    e = e;
                                    Slog.e(TAG, "loadSysWhitelist Exception failed: " + e);
                                    aci.setVerifyStatus(3);
                                    this.mDataManager.addComponentInfo(aci);
                                }
                            }
                        } catch (Exception e2) {
                            e = e2;
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
                }
                this.mDataManager.addComponentInfo(aci);
            }
            if (HW_DEBUG) {
                long end = System.currentTimeMillis();
                Slog.d(TAG, "loadSysWhitelist TIME = " + (end - timeStart));
            }
        }
    }

    private byte[] fileToByte(File file) {
        byte[] data = null;
        FileInputStream in = null;
        ByteArrayOutputStream out = null;
        if (file.exists()) {
            try {
                in = new FileInputStream(file);
                out = new ByteArrayOutputStream(2048);
                byte[] cache = new byte[1024];
                while (true) {
                    int read = in.read(cache);
                    int nRead = read;
                    if (read == -1) {
                        break;
                    }
                    out.write(cache, 0, nRead);
                }
                out.flush();
                data = out.toByteArray();
            } catch (FileNotFoundException e) {
                Slog.e(TAG, "fileToByte FileNotFoundException " + e);
            } catch (IOException e2) {
                Slog.e(TAG, "fileToByte IOException! " + e2);
            } catch (Throwable th) {
                IoUtils.closeQuietly(null);
                IoUtils.closeQuietly(null);
                throw th;
            }
            IoUtils.closeQuietly(in);
            IoUtils.closeQuietly(out);
        }
        return data;
    }

    private String readFromFile(File file) {
        StringBuffer readBuf = new StringBuffer(EXPECTED_BUFFER_DATA);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String data = br.readLine();
            while (data != null) {
                if (data.length() < 350) {
                    readBuf.append(data);
                    data = br.readLine();
                }
            }
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "readFromFile FileNotFoundException :" + e);
        } catch (IOException e2) {
            Slog.e(TAG, "readFromFile IOException :" + e2);
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            throw th;
        }
        IoUtils.closeQuietly(br);
        return readBuf.toString();
    }

    private boolean verify(byte[] data, String publicKey, String sign) {
        if (data == null || data.length == 0 || sign == null) {
            Slog.e(TAG, "verify Input invalid!");
            return false;
        }
        try {
            PublicKey publicK = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey.getBytes("UTF-8"))));
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicK);
            signature.update(data);
            return signature.verify(Base64.getDecoder().decode(sign.getBytes("UTF-8")));
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "verify IllegalArgumentException : " + e);
            return false;
        } catch (UnsupportedEncodingException e2) {
            Slog.e(TAG, "verify UnsupportedEncodingException : " + e2);
            return false;
        } catch (NoSuchAlgorithmException e3) {
            Slog.e(TAG, "verify NoSuchAlgorithmException : " + e3);
            return false;
        } catch (InvalidKeySpecException e4) {
            Slog.e(TAG, "verify InvalidKeySpecException : " + e4);
            return false;
        } catch (InvalidKeyException e5) {
            Slog.e(TAG, "verify InvalidKeyException : " + e5);
            return false;
        } catch (SignatureException e6) {
            Slog.e(TAG, "verify SignatureException : " + e6);
            return false;
        }
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

    /* JADX WARNING: Removed duplicated region for block: B:11:0x002e A[Catch:{ FileNotFoundException -> 0x00d6, IOException -> 0x00d4, Exception -> 0x00d2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003a A[SYNTHETIC, Splitter:B:14:0x003a] */
    private boolean parsePackagelist(File whitelist) {
        int type;
        boolean retVal = true;
        String str = null;
        FileInputStream fin = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            try {
                fin = new FileInputStream(whitelist);
                parser.setInput(fin, "UTF-8");
                while (true) {
                    int next = parser.next();
                    type = next;
                    int i = 1;
                    if (next != 2 && type != 1) {
                        Slog.e(TAG, "parsePackagelist");
                    } else if (type == 2) {
                        Slog.e(TAG, "No start tag found in Package white list.");
                        IoUtils.closeQuietly(fin);
                        return false;
                    } else {
                        int outerDepth = parser.getDepth();
                        while (true) {
                            int next2 = parser.next();
                            int type2 = next2;
                            if (next2 == i || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                                break;
                            }
                            if (type2 != 3) {
                                if (type2 != 4) {
                                    if (parser.getName().equals("package")) {
                                        String packageName = parser.getAttributeValue(str, "name");
                                        String path = parser.getAttributeValue(str, HwSecDiagnoseConstant.ANTIMAL_APK_PATH);
                                        String sign = parser.getAttributeValue(str, "ss");
                                        String version = parser.getAttributeValue(str, "version");
                                        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(path)) {
                                            if (!TextUtils.isEmpty(sign)) {
                                                if (isPreinstallApkDir(path)) {
                                                    ApkBasicInfo pbi = new ApkBasicInfo(packageName, path, sign.split(","), version);
                                                    this.mApkInfoList.put(packageName, pbi);
                                                    if (this.mSysApkWhitelist.get(packageName) == null) {
                                                        ArrayList<ApkBasicInfo> plist = new ArrayList<>();
                                                        plist.add(pbi);
                                                        this.mSysApkWhitelist.put(packageName, plist);
                                                    } else {
                                                        ArrayList<ApkBasicInfo> plist2 = this.mSysApkWhitelist.get(packageName);
                                                        plist2.add(pbi);
                                                        this.mSysApkWhitelist.put(packageName, plist2);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            str = null;
                            i = 1;
                        }
                        IoUtils.closeQuietly(fin);
                        return retVal;
                    }
                }
                if (type == 2) {
                }
            } catch (FileNotFoundException e) {
                e = e;
                Slog.e(TAG, "file whitelist cannot be found" + e);
                retVal = false;
                IoUtils.closeQuietly(fin);
                return retVal;
            } catch (IOException e2) {
                e = e2;
                Slog.e(TAG, "failed to parse the whitelist" + e);
                retVal = false;
                IoUtils.closeQuietly(fin);
                return retVal;
            } catch (Exception e3) {
                e = e3;
                Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
                retVal = false;
                IoUtils.closeQuietly(fin);
                return retVal;
            }
        } catch (FileNotFoundException e4) {
            e = e4;
            File file = whitelist;
            Slog.e(TAG, "file whitelist cannot be found" + e);
            retVal = false;
            IoUtils.closeQuietly(fin);
            return retVal;
        } catch (IOException e5) {
            e = e5;
            File file2 = whitelist;
            Slog.e(TAG, "failed to parse the whitelist" + e);
            retVal = false;
            IoUtils.closeQuietly(fin);
            return retVal;
        } catch (Exception e6) {
            e = e6;
            File file3 = whitelist;
            Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
            retVal = false;
            IoUtils.closeQuietly(fin);
            return retVal;
        } catch (Throwable th) {
            th = th;
            IoUtils.closeQuietly(fin);
            throw th;
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
                        AntiMalApkInfo antiMalApkInfo = new AntiMalApkInfo(abi.mPackagename, formatPath(abi.mPath), null, 3, null, null, stringToInt(abi.mVersion));
                        AntiMalApkInfo deletedApkInfo = antiMalApkInfo;
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
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return false;
            }
            for (File f : files) {
                if (f != null && isApkPath(f.getAbsolutePath())) {
                    return true;
                }
            }
        }
        return isApkPath(path);
    }

    private boolean isApkPath(String path) {
        return path != null && path.endsWith(".apk");
    }

    private void markApkExist(PackageParser.Package pkg) {
        if (pkg != null) {
            synchronized (this.mApkInfoList) {
                ApkBasicInfo abi = this.mApkInfoList.get(pkg.packageName);
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
            return HwUtils.bytesToString(md.digest()).toUpperCase(Locale.ENGLISH);
        } catch (NoSuchAlgorithmException e) {
            if (HW_DEBUG) {
                Slog.e(TAG, "get sha256 failed");
            }
            return null;
        }
    }

    private boolean compareHashcode(String[] hashcode, PackageParser.Package pkg) {
        if (pkg == null || !isApkPath(pkg.baseCodePath) || hashcode == null || hashcode.length == 0) {
            if (HW_DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("compareHashcode not hashcode : ");
                sb.append(pkg != null ? pkg.baseCodePath : "null");
                Slog.d(TAG, sb.toString());
            }
            return false;
        }
        android.content.pm.Signature[] apkSign = pkg.mSigningDetails.signatures;
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
            if (!hashcode[j].equals(apkSignHashAry[j])) {
                if (HW_DEBUG) {
                    Slog.d(TAG, "compareHashcode hashcode not equal pkg = " + pkg.baseCodePath + "white apk hash = " + hashcode[j] + " apk hashcod = " + apkSignHashAry[j]);
                }
                return false;
            }
            j++;
        }
        return true;
    }

    private boolean componentValid(String apkPath) {
        AntiMalComponentInfo aci = this.mDataManager.getComponentByApkPath(apkPath);
        if (aci != null) {
            return aci.isVerifyStatusValid();
        }
        return false;
    }

    private void setComponentAntiMalStatus(String path, int bitMask) {
        AntiMalComponentInfo aci = this.mDataManager.getComponentByApkPath(path);
        if (aci != null) {
            aci.setAntiMalStatus(bitMask);
        }
    }

    public int checkIllegalSysApk(PackageParser.Package pkg, int flags) throws PackageManagerException {
        int i = 0;
        if (!CHINA_RELEASE_VERSION) {
            return 0;
        }
        if (pkg == null) {
            Slog.e(TAG, "Invalid input args pkg(null) in checkIllegalSysApk.");
            return 0;
        } else if (!this.mNeedScan) {
            AntiMalApkInfo ai = this.mOldIllegalApks.get(pkg.packageName);
            if (HW_DEBUG && ai != null) {
                Slog.d(TAG, "checkIllegalSysApk no need check legally AI = " + ai);
            }
            if (ai != null) {
                i = ai.mType;
            }
            return i;
        } else {
            markApkExist(pkg);
            if (!componentValid(pkg.baseCodePath)) {
                if (HW_DEBUG) {
                    Log.d(TAG, "checkIllegalSysApk COMPONENT INVALID! path = " + pkg.baseCodePath);
                }
                return 0;
            } else if (!isPreinstallApkDir(pkg.baseCodePath)) {
                return 0;
            } else {
                ArrayList<ApkBasicInfo> pbi = this.mSysApkWhitelist.get(pkg.staticSharedLibName != null ? pkg.manifestPackageName : pkg.packageName);
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
                    ApkBasicInfo apkInfo = it.next();
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
        }
    }
}
