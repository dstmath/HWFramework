package com.android.server.security.ukey;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import android.util.Xml;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;

public class UKeyApplicationScanner {
    private static final String APK_NAME_CN = "apkNameCn";
    private static final String APK_NAME_EN = "apkNameEn";
    private static final String CERT_MGR_NAME = "certMgrName";
    private static final int CERT_MGR_RESULT_DISABLE = 0;
    private static final int CERT_MGR_RESULT_ENABLE = 1;
    private static final int CERT_MGR_RESULT_HIDE = 2;
    private static final String CERT_MGR_SIGN = "certMgrSign";
    private static final String CERT_MGR_TYPE_DISABLE = "disable";
    private static final String CERT_MGR_TYPE_HIDE = "hide";
    private static final String KEY_ALGORITHM = "RSA";
    private static final String PACKAGE_NAME = "packageName";
    private static final String SIGN = "sign";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String TAG = "UKeyApplicationScanner";
    private static final String UKEY_APK_WHITE_LIST_NAME = "ukeyapp.xml";
    private static final String UKEY_ID = "ukeyId";
    private static HashMap<String, String> mPackageNameMap = new HashMap();
    private static HashMap<String, UKeyApkInfo> mUKeyApkList = new HashMap();
    private Context mContext;

    public static class UKeyApkInfo {
        public final String mApkNameCn;
        public final String mApkNameEn;
        public final String mCertMgrType;
        public final String[] mCertPkgHashCode;
        public final String[] mHashCode;
        public final String mPackageName;
        public final String mUKeyId;

        UKeyApkInfo(String packageName, String apkNameCn, String apkNameEn, String ukeyId, String[] hashCodeArray, String certMgrType, String[] certPkgSign) {
            this.mPackageName = packageName;
            this.mApkNameCn = apkNameCn;
            this.mApkNameEn = apkNameEn;
            this.mUKeyId = ukeyId;
            this.mHashCode = hashCodeArray;
            this.mCertMgrType = certMgrType;
            this.mCertPkgHashCode = certPkgSign;
        }
    }

    public UKeyApplicationScanner(Context context) {
        this.mContext = context;
    }

    public void loadUKeyApkWhitelist() {
        mUKeyApkList.clear();
        if (parsePackagelist()) {
            Slog.i(TAG, "parsing whitelist succeeded!!!");
        } else {
            Slog.i(TAG, "parsing whitelist failed!!!");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x0177 A:{Splitter: B:1:0x0003, PHI: r13 , ExcHandler: org.xmlpull.v1.XmlPullParserException (r12_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0177 A:{Splitter: B:1:0x0003, PHI: r13 , ExcHandler: org.xmlpull.v1.XmlPullParserException (r12_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:45:0x0177, code:
            r12 = move-exception;
     */
    /* JADX WARNING: Missing block: B:47:?, code:
            android.util.Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + r12);
     */
    /* JADX WARNING: Missing block: B:48:0x0194, code:
            r16 = false;
            libcore.io.IoUtils.closeQuietly(r13);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parsePackagelist() {
        boolean retVal = true;
        AutoCloseable fin = null;
        try {
            int type;
            XmlPullParser parser = Xml.newPullParser();
            fin = this.mContext.getAssets().open(UKEY_APK_WHITE_LIST_NAME);
            parser.setInput(fin, "UTF-8");
            do {
                type = parser.next();
                if (type == 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                Slog.i(TAG, "No start tag found in Package white list.");
                IoUtils.closeQuietly(fin);
                return false;
            }
            int outerDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    IoUtils.closeQuietly(fin);
                } else if (!(type == 3 || type == 4 || !parser.getName().equals("package"))) {
                    String packageName = parser.getAttributeValue(null, "packageName");
                    String apkNameCn = parser.getAttributeValue(null, APK_NAME_CN);
                    String apkNameEn = parser.getAttributeValue(null, APK_NAME_EN);
                    String ukeyId = parser.getAttributeValue(null, UKEY_ID);
                    String sign = parser.getAttributeValue(null, SIGN);
                    String certName = parser.getAttributeValue(null, CERT_MGR_NAME);
                    String certMgrApkSign = parser.getAttributeValue(null, CERT_MGR_SIGN);
                    Slog.i(TAG, "PARSER from xml packageName : " + packageName);
                    if (!(TextUtils.isEmpty(packageName) || TextUtils.isEmpty(sign))) {
                        String[] certMgrSignArry;
                        String[] signArry = sign.split(",");
                        if (certMgrApkSign == null) {
                            certMgrSignArry = new String[0];
                        } else {
                            certMgrSignArry = certMgrApkSign.split(",");
                        }
                        mUKeyApkList.put(packageName, new UKeyApkInfo(packageName, apkNameCn, apkNameEn, ukeyId, signArry, certName, certMgrSignArry));
                        mPackageNameMap.put(packageName, packageName);
                        if (!TextUtils.isEmpty(certName)) {
                            mPackageNameMap.put(certName, packageName);
                        }
                    }
                }
            }
            IoUtils.closeQuietly(fin);
            return retVal;
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
            retVal = false;
            IoUtils.closeQuietly(fin);
        } catch (Exception e2) {
        } catch (Throwable th) {
            IoUtils.closeQuietly(fin);
            throw th;
        }
    }

    public boolean isWhiteListedUKeyApp(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        String realPkgName = getRealUKeyPkgName(packageName);
        UKeyApkInfo apkSign = (UKeyApkInfo) mUKeyApkList.get(realPkgName);
        if (apkSign == null || apkSign.mPackageName == null) {
            return false;
        }
        try {
            return compareHashcode(apkSign.mHashCode, this.mContext.getPackageManager().getPackageInfo(realPkgName, 64).signatures);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public Bundle getUKeyApkInfoData(String packageName) {
        UKeyApkInfo uKeyApkInfo = (UKeyApkInfo) mUKeyApkList.get(packageName);
        if (uKeyApkInfo == null || uKeyApkInfo.mPackageName == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString("packageName", uKeyApkInfo.mPackageName);
        bundle.putString(APK_NAME_CN, uKeyApkInfo.mApkNameCn);
        bundle.putString(APK_NAME_EN, uKeyApkInfo.mApkNameEn);
        if (compareCertMgrApkResult(uKeyApkInfo.mPackageName) == 0) {
            bundle.putString(CERT_MGR_NAME, CERT_MGR_TYPE_DISABLE);
        } else if (TextUtils.isEmpty(uKeyApkInfo.mCertMgrType)) {
            bundle.putString(CERT_MGR_NAME, uKeyApkInfo.mPackageName);
        } else {
            bundle.putString(CERT_MGR_NAME, uKeyApkInfo.mCertMgrType);
        }
        return bundle;
    }

    public UKeyApkInfo getUKeyApkInfo(String packageName) {
        if (mUKeyApkList == null) {
            return null;
        }
        return (UKeyApkInfo) mUKeyApkList.get(packageName);
    }

    private boolean compareHashcode(String[] hashcode, Signature[] signatures) {
        if (signatures == null || signatures.length == 0) {
            return false;
        }
        String[] apkSignHashAry = new String[signatures.length];
        int i = 0;
        while (i < signatures.length) {
            try {
                apkSignHashAry[i] = sha256(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(signatures[i].toByteArray()))).getSignature());
                i++;
            } catch (CertificateException e) {
                Slog.e(TAG, "compareHashcode Error !!!");
                return false;
            }
        }
        return compareSignatures(hashcode, apkSignHashAry);
    }

    private boolean compareSignatures(String[] s1, String[] s2) {
        boolean z = true;
        if (s1.length != s2.length) {
            return false;
        }
        if (s1.length == 1) {
            if (!s1[0].equals(s2[0])) {
                z = false;
            }
            return z;
        }
        ArraySet<String> set1 = new ArraySet();
        for (String sig : s1) {
            Slog.e(TAG, "sig 1 = " + sig);
            set1.add(sig);
        }
        ArraySet<String> set2 = new ArraySet();
        for (String sig2 : s2) {
            Slog.e(TAG, "sig 2 = " + sig2);
            set2.add(sig2);
        }
        return set1.equals(set2);
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
            return null;
        }
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

    public String getRealUKeyPkgName(String packageName) {
        return (String) mPackageNameMap.get(packageName);
    }

    private int compareCertMgrApkResult(String packageName) {
        int i = 1;
        if (packageName == null || packageName.isEmpty()) {
            return 0;
        }
        UKeyApkInfo apkSign = (UKeyApkInfo) mUKeyApkList.get(packageName);
        if (apkSign == null) {
            return 0;
        }
        if (TextUtils.isEmpty(apkSign.mCertMgrType)) {
            return 1;
        }
        if (CERT_MGR_TYPE_DISABLE.equals(apkSign.mCertMgrType)) {
            return 0;
        }
        if (CERT_MGR_TYPE_HIDE.equals(apkSign.mCertMgrType)) {
            return 2;
        }
        try {
            boolean result = compareHashcode(apkSign.mCertPkgHashCode, this.mContext.getPackageManager().getPackageInfo(apkSign.mCertMgrType, 64).signatures);
            Slog.i(TAG, "The compare Hashcode result of cert manager apk " + apkSign.mCertMgrType + "result is " + result);
            if (!result) {
                i = 0;
            }
            return i;
        } catch (NameNotFoundException e) {
            Slog.i(TAG, "ukey cert manager apk" + apkSign.mCertMgrType + " does not install!!!");
            return 0;
        }
    }
}
