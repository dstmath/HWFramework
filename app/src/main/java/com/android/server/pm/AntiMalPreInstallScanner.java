package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.display.Utils;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.AntiMalApkInfo;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import huawei.com.android.server.policy.HwGlobalActionsData;
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
import java.util.Locale;
import libcore.io.IoUtils;
import org.apache.commons.codec.binary.Base64;
import org.xmlpull.v1.XmlPullParser;

public class AntiMalPreInstallScanner {
    private static final int CACHE_SIZE = 1024;
    private static final boolean CHINA_RELEASE_VERSION = false;
    private static final String[] COMPONENT_ARRY = null;
    private static final boolean DEBUG = false;
    private static final String ENCRYPT_ARG = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
    private static final boolean HW_DEBUG = false;
    private static final String KEY_ALGORITHM = "RSA";
    private static final String PATH_SLANT = "/";
    private static final String PKGLIST_SIGN_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArO6tIeIxD78HrazoYkAeKdXhKINVUE1fAwnXb6OiabTYf3qO22wcFoqyKsm2tlaWrDU8+hnxjkZOLIvpcJ0bEDAkICoIRNGBoJzJzN6PyIOyfLd4IOA/bS071jaA5JjLGpMkBKYuhzECnK/pmruKngl3ED/t8HRw44ku1rabcwJjKl8dF4D0ogoosrr8mrwfnQaJpkmTL1oScF/Mr4plkrUdw3Ab00HZoklMVznT+M5KV8DmEjo8PIYkdFlJCwEx4Cj6PXKHfBEGeivyPe2W1/EnYdaREu4GO9ZLBsIhRhS3b7UY5UFsjbYBK23M4zrpZlMVQer4zyqmzefs25BYAwIDAQAB";
    private static final String[] PREINSTALL_APK_DIR = null;
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String SYSAPK_SIGN_PATH = "xml/sign";
    private static final String SYSAPK_WHITE_LIST_PATH = "xml/criticalpro.xml";
    private static final String TAG = "AntiMalPreInstallScanner";
    private static Context mContext;
    private static AntiMalPreInstallScanner mInstance;
    private static boolean mIsOtaBoot;
    private HashMap<String, ApkBasicInfo> mApkInfoList;
    private AntiMalDataManager mDataManager;
    private long mDeviceFirstUseTime;
    private boolean mNeedScan;
    private HashMap<String, AntiMalApkInfo> mOldIllegalApks;
    private AntiMalPreInstallReport mReport;
    private HashMap<String, ArrayList<ApkBasicInfo>> mSysApkWhitelist;

    private static class ApkBasicInfo {
        public boolean mExist;
        public final String[] mHashCode;
        public final String mPackagename;
        public final String mPath;
        public final String mVersion;

        ApkBasicInfo(String packagename, String path, String[] hashCodeArry, String version) {
            this.mPackagename = packagename;
            this.mPath = path;
            this.mHashCode = hashCodeArry;
            this.mExist = AntiMalPreInstallScanner.HW_DEBUG;
            this.mVersion = version;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.AntiMalPreInstallScanner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.AntiMalPreInstallScanner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.AntiMalPreInstallScanner.<clinit>():void");
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
        this.mSysApkWhitelist = new HashMap();
        this.mApkInfoList = new HashMap();
        this.mOldIllegalApks = new HashMap();
        this.mDataManager = new AntiMalDataManager(mIsOtaBoot);
        this.mNeedScan = this.mDataManager.needScanIllegalApks();
        this.mReport = new AntiMalPreInstallReport(this.mDataManager);
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
        Object obj;
        byte[] data = null;
        AutoCloseable autoCloseable = null;
        AutoCloseable autoCloseable2 = null;
        if (file.exists()) {
            try {
                FileInputStream in = new FileInputStream(file);
                try {
                    ByteArrayOutputStream out2 = new ByteArrayOutputStream(HwGlobalActionsData.FLAG_SILENTMODE_TRANSITING);
                    try {
                        byte[] cache = new byte[CACHE_SIZE];
                        while (true) {
                            int nRead = in.read(cache);
                            if (nRead == -1) {
                                break;
                            }
                            out2.write(cache, 0, nRead);
                        }
                        out2.flush();
                        data = out2.toByteArray();
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(out2);
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        autoCloseable2 = out2;
                        autoCloseable = in;
                        try {
                            Slog.e(TAG, "fileToByte FileNotFoundException " + e);
                            IoUtils.closeQuietly(autoCloseable);
                            IoUtils.closeQuietly(autoCloseable2);
                            return data;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(autoCloseable);
                            IoUtils.closeQuietly(autoCloseable2);
                            throw th;
                        }
                    } catch (IOException e4) {
                        e2 = e4;
                        out = out2;
                        obj = in;
                        Slog.e(TAG, "fileToByte IOException! " + e2);
                        IoUtils.closeQuietly(autoCloseable);
                        IoUtils.closeQuietly(autoCloseable2);
                        return data;
                    } catch (Throwable th3) {
                        th = th3;
                        out = out2;
                        obj = in;
                        IoUtils.closeQuietly(autoCloseable);
                        IoUtils.closeQuietly(autoCloseable2);
                        throw th;
                    }
                } catch (FileNotFoundException e5) {
                    e = e5;
                    obj = in;
                    Slog.e(TAG, "fileToByte FileNotFoundException " + e);
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                    return data;
                } catch (IOException e6) {
                    e2 = e6;
                    obj = in;
                    Slog.e(TAG, "fileToByte IOException! " + e2);
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                    return data;
                } catch (Throwable th4) {
                    th = th4;
                    obj = in;
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                e = e7;
                Slog.e(TAG, "fileToByte FileNotFoundException " + e);
                IoUtils.closeQuietly(autoCloseable);
                IoUtils.closeQuietly(autoCloseable2);
                return data;
            } catch (IOException e8) {
                e2 = e8;
                Slog.e(TAG, "fileToByte IOException! " + e2);
                IoUtils.closeQuietly(autoCloseable);
                IoUtils.closeQuietly(autoCloseable2);
                return data;
            }
        }
        return data;
    }

    private String readFromFile(File file) throws IOException {
        FileNotFoundException e;
        Object obj;
        IOException e2;
        Throwable th;
        StringBuffer readBuf = new StringBuffer();
        AutoCloseable autoCloseable = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            try {
                for (String data = br.readLine(); data != null; data = br.readLine()) {
                    readBuf.append(data);
                }
                IoUtils.closeQuietly(br);
                BufferedReader bufferedReader = br;
            } catch (FileNotFoundException e3) {
                e = e3;
                obj = br;
                Slog.e(TAG, "readFromFile FileNotFoundException :" + e);
                IoUtils.closeQuietly(autoCloseable);
                return readBuf.toString();
            } catch (IOException e4) {
                e2 = e4;
                obj = br;
                try {
                    Slog.e(TAG, "readFromFile IOException :" + e2);
                    IoUtils.closeQuietly(autoCloseable);
                    return readBuf.toString();
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                obj = br;
                IoUtils.closeQuietly(autoCloseable);
                throw th;
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            Slog.e(TAG, "readFromFile FileNotFoundException :" + e);
            IoUtils.closeQuietly(autoCloseable);
            return readBuf.toString();
        } catch (IOException e6) {
            e2 = e6;
            Slog.e(TAG, "readFromFile IOException :" + e2);
            IoUtils.closeQuietly(autoCloseable);
            return readBuf.toString();
        }
        return readBuf.toString();
    }

    private boolean verify(byte[] data, String publicKey, String sign) {
        if (data == null || data.length == 0 || sign == null) {
            Slog.e(TAG, "verify Input invalid!");
            return HW_DEBUG;
        }
        try {
            PublicKey publicK = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(publicKey.getBytes("UTF-8"))));
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicK);
            signature.update(data);
            return signature.verify(Base64.decodeBase64(sign.getBytes("UTF-8")));
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "verify IllegalArgumentException : " + e);
            return HW_DEBUG;
        } catch (UnsupportedEncodingException e2) {
            Slog.e(TAG, "verify UnsupportedEncodingException : " + e2);
            return HW_DEBUG;
        } catch (NoSuchAlgorithmException e3) {
            Slog.e(TAG, "verify NoSuchAlgorithmException : " + e3);
            return HW_DEBUG;
        } catch (InvalidKeySpecException e4) {
            Slog.e(TAG, "verify InvalidKeySpecException : " + e4);
            return HW_DEBUG;
        } catch (InvalidKeyException e5) {
            Slog.e(TAG, "verify InvalidKeyException : " + e5);
            return HW_DEBUG;
        } catch (SignatureException e6) {
            Slog.e(TAG, "verify SignatureException : " + e6);
            return HW_DEBUG;
        }
    }

    private boolean isPreinstallApkDir(String path) {
        if (path == null || path.isEmpty()) {
            return HW_DEBUG;
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
        return HW_DEBUG;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    return HW_DEBUG;
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        IoUtils.closeQuietly(fin);
                    } else if (!(type == 3 || type == 4)) {
                        if (parser.getName().equals(ControlScope.PACKAGE_ELEMENT_KEY)) {
                            String packageName = parser.getAttributeValue(null, MemoryConstant.MEM_POLICY_ACTIONNAME);
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
                        } else {
                            continue;
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
                retVal = HW_DEBUG;
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
            return HW_DEBUG;
        }
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return HW_DEBUG;
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
        return path != null ? path.endsWith(".apk") : HW_DEBUG;
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
        if (pkg == null || !isApkPath(pkg.baseCodePath) || hashcode == null || hashcode.length == 0) {
            if (HW_DEBUG && pkg != null) {
                Slog.d(TAG, "compareHashcode not hashcode : " + pkg.baseCodePath);
            }
            return HW_DEBUG;
        }
        android.content.pm.Signature[] apkSign = pkg.mSignatures;
        if (apkSign == null || apkSign.length == 0) {
            if (HW_DEBUG) {
                Slog.d(TAG, "compareHashcode not apk : " + pkg.baseCodePath);
            }
            return HW_DEBUG;
        }
        String[] apkSignHashAry = new String[apkSign.length];
        int i = 0;
        while (i < apkSign.length) {
            try {
                apkSignHashAry[i] = sha256(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(apkSign[i].toByteArray()))).getSignature());
                i++;
            } catch (CertificateException e) {
                Slog.e(TAG, "compareHashcode E: " + e);
                return HW_DEBUG;
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
                return HW_DEBUG;
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
            int byteValue = bytes[j] & Utils.MAXINUM_TEMPERATURE;
            chars[j * 2] = hexChars[byteValue >>> 4];
            chars[(j * 2) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars).toUpperCase(Locale.US);
    }

    private boolean componentValid(String apkPath) {
        AntiMalComponentInfo aci = this.mDataManager.getComponentByApkPath(apkPath);
        return aci != null ? aci.isVerifyStatusValid() : HW_DEBUG;
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
