package com.android.server.security.ukey;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import android.util.Xml;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
    private static HashMap<String, String> mPackageNameMap = new HashMap<>();
    private static HashMap<String, UKeyApkInfo> mUKeyApkList = new HashMap<>();
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

    private boolean parsePackagelist() {
        boolean retVal;
        int type;
        int i;
        boolean retVal2;
        boolean retVal3;
        boolean z;
        String[] certMgrSignArry;
        boolean retVal4 = true;
        String str = null;
        InputStream fin = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            try {
                fin = this.mContext.getAssets().open(UKEY_APK_WHITE_LIST_NAME);
                parser.setInput(fin, "UTF-8");
                boolean z2 = false;
                do {
                    int next = parser.next();
                    type = next;
                    i = 1;
                    if (next == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    try {
                        Slog.i(TAG, "No start tag found in Package white list.");
                        IoUtils.closeQuietly(fin);
                        return false;
                    } catch (FileNotFoundException e) {
                        e = e;
                        boolean z3 = retVal4;
                        Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
                        retVal = false;
                        IoUtils.closeQuietly(fin);
                        return retVal;
                    } catch (IOException | IndexOutOfBoundsException | XmlPullParserException e2) {
                        e = e2;
                        boolean z4 = retVal4;
                        Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
                        retVal = false;
                        IoUtils.closeQuietly(fin);
                        return retVal;
                    } catch (Throwable th) {
                        th = th;
                        boolean z5 = retVal4;
                        IoUtils.closeQuietly(fin);
                        throw th;
                    }
                } else {
                    int outerDepth = parser.getDepth();
                    while (true) {
                        int next2 = parser.next();
                        int type2 = next2;
                        if (next2 == i) {
                            retVal2 = retVal4;
                            break;
                        }
                        if (type2 == 3) {
                            if (parser.getDepth() <= outerDepth) {
                                retVal2 = retVal4;
                                break;
                            }
                        }
                        if (type2 == 3) {
                            retVal3 = retVal4;
                            z = z2;
                        } else if (type2 == 4) {
                            retVal3 = retVal4;
                            z = z2;
                        } else if (parser.getName().equals("package")) {
                            String packageName = parser.getAttributeValue(str, "packageName");
                            String apkNameCn = parser.getAttributeValue(str, APK_NAME_CN);
                            String apkNameEn = parser.getAttributeValue(str, APK_NAME_EN);
                            String ukeyId = parser.getAttributeValue(str, UKEY_ID);
                            String sign = parser.getAttributeValue(str, SIGN);
                            String certName = parser.getAttributeValue(str, CERT_MGR_NAME);
                            String certMgrApkSign = parser.getAttributeValue(str, CERT_MGR_SIGN);
                            StringBuilder sb = new StringBuilder();
                            retVal3 = retVal4;
                            try {
                                sb.append("PARSER from xml packageName : ");
                                sb.append(packageName);
                                Slog.i(TAG, sb.toString());
                                if (!TextUtils.isEmpty(packageName)) {
                                    if (!TextUtils.isEmpty(sign)) {
                                        String[] signArry = sign.split(",");
                                        if (certMgrApkSign == null) {
                                            z = false;
                                            certMgrSignArry = new String[0];
                                        } else {
                                            z = false;
                                            certMgrSignArry = certMgrApkSign.split(",");
                                        }
                                        String certName2 = certName;
                                        String str2 = sign;
                                        UKeyApkInfo uKeyApkInfo = new UKeyApkInfo(packageName, apkNameCn, apkNameEn, ukeyId, signArry, certName2, certMgrSignArry);
                                        mUKeyApkList.put(packageName, uKeyApkInfo);
                                        mPackageNameMap.put(packageName, packageName);
                                        if (!TextUtils.isEmpty(certName2)) {
                                            mPackageNameMap.put(certName2, packageName);
                                        }
                                    }
                                }
                                z = false;
                            } catch (FileNotFoundException e3) {
                                e = e3;
                                Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
                                retVal = false;
                                IoUtils.closeQuietly(fin);
                                return retVal;
                            } catch (IOException | IndexOutOfBoundsException | XmlPullParserException e4) {
                                e = e4;
                                Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
                                retVal = false;
                                IoUtils.closeQuietly(fin);
                                return retVal;
                            }
                        } else {
                            retVal3 = retVal4;
                            z = z2;
                        }
                        z2 = z;
                        retVal4 = retVal3;
                        str = null;
                        i = 1;
                    }
                    IoUtils.closeQuietly(fin);
                    retVal = retVal2;
                    return retVal;
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                boolean z6 = retVal4;
                Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
                retVal = false;
                IoUtils.closeQuietly(fin);
                return retVal;
            } catch (IOException | IndexOutOfBoundsException | XmlPullParserException e6) {
                e = e6;
                boolean z7 = retVal4;
                Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
                retVal = false;
                IoUtils.closeQuietly(fin);
                return retVal;
            } catch (Throwable th2) {
                th = th2;
                boolean z8 = retVal4;
                IoUtils.closeQuietly(fin);
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            boolean z62 = retVal4;
            Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
            retVal = false;
            IoUtils.closeQuietly(fin);
            return retVal;
        } catch (IOException | IndexOutOfBoundsException | XmlPullParserException e8) {
            e = e8;
            boolean z72 = retVal4;
            Slog.e(TAG, "parsePackagelist Error reading Pkg white list: " + e);
            retVal = false;
            IoUtils.closeQuietly(fin);
            return retVal;
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(fin);
            throw th;
        }
    }

    public boolean isWhiteListedUKeyApp(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        String realPkgName = getRealUKeyPkgName(packageName);
        UKeyApkInfo apkSign = mUKeyApkList.get(realPkgName);
        if (apkSign == null || apkSign.mPackageName == null) {
            return false;
        }
        try {
            return compareHashcode(apkSign.mHashCode, this.mContext.getPackageManager().getPackageInfo(realPkgName, 64).signatures);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public Bundle getUKeyApkInfoData(String packageName) {
        UKeyApkInfo uKeyApkInfo = mUKeyApkList.get(packageName);
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
        return mUKeyApkList.get(packageName);
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
        String sig;
        String sig2;
        if (s1.length != s2.length) {
            return false;
        }
        if (s1.length == 1) {
            return s1[0].equals(s2[0]);
        }
        ArraySet<String> set1 = new ArraySet<>();
        int length = s1.length;
        int i = 0;
        while (i < length) {
            Slog.e(TAG, "sig 1 = " + sig2);
            set1.add(sig2);
            i++;
        }
        ArraySet<String> set2 = new ArraySet<>();
        int length2 = s2.length;
        int i2 = 0;
        while (i2 < length2) {
            Slog.e(TAG, "sig 2 = " + sig);
            set2.add(sig);
            i2++;
        }
        if (set1.equals(set2)) {
            return true;
        }
        return false;
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
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int byteValue = bytes[j] & 255;
            chars[j * 2] = hexChars[byteValue >>> 4];
            chars[(j * 2) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars).toUpperCase(Locale.US);
    }

    public String getRealUKeyPkgName(String packageName) {
        return mPackageNameMap.get(packageName);
    }

    private int compareCertMgrApkResult(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return 0;
        }
        UKeyApkInfo apkSign = mUKeyApkList.get(packageName);
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
            return result;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.i(TAG, "ukey cert manager apk" + apkSign.mCertMgrType + " does not install!!!");
            return 0;
        }
    }
}
