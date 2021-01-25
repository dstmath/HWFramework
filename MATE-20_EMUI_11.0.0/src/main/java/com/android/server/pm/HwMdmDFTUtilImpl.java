package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.util.IMonitor;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

public class HwMdmDFTUtilImpl {
    public static final short E907504001_PKG_VARCHAR = 0;
    public static final short E907504001_SIGHASH_VARCHAR = 2;
    public static final short E907504001_VERSION_VARCHAR = 1;
    public static final short E907504002_PKG_VARCHAR = 0;
    public static final short E907504002_SIGHASH_VARCHAR = 2;
    public static final short E907504002_VERSION_VARCHAR = 1;
    public static final short E907504003_DOPKGSIGHASH_VARCHAR = 5;
    public static final short E907504003_DOPKGVER_VARCHAR = 4;
    public static final short E907504003_DOPKG_VARCHAR = 3;
    public static final short E907504003_PKG_VARCHAR = 0;
    public static final short E907504003_SIGHASH_VARCHAR = 2;
    public static final short E907504003_VERSION_VARCHAR = 1;
    private static final String LOCALE_REGION_INFOR = "ro.product.locale.region";
    public static final String TAG = "HwMdmDFTUtilImpl";
    private static final int VERSION_CERT_PARSE = 0;
    private static final int VERSION_DO = 1;
    private static final int VERSION_WORK_PROFILE = 2;

    public static void handleMdmDftUploadEvent(int type, HwMdmDFT hwMdmDFT) {
        Log.d(TAG, "IMonitor upload event " + type);
        if (hwMdmDFT != null) {
            switch (type) {
                case HwMdmDFTConst.EID_MDM_DFT_CERT_PARSE_SUCCESS /* 907504001 */:
                    IMonitor.EventStream eventCertParseSuccess = IMonitor.openEventStream((int) HwMdmDFTConst.EID_MDM_DFT_CERT_PARSE_SUCCESS);
                    if (eventCertParseSuccess == null) {
                        Log.e(TAG, "eventCertParseSuccess is null.");
                        return;
                    }
                    HwMdmInstallInfo info = (HwMdmInstallInfo) hwMdmDFT;
                    if (isChinaVersion()) {
                        eventCertParseSuccess.setParam(0, info.getPkg()).setParam(1, info.getVersion()).setParam(2, info.getSighash());
                    } else {
                        eventCertParseSuccess.setParam(1, "0");
                    }
                    IMonitor.sendEvent(eventCertParseSuccess);
                    IMonitor.closeEventStream(eventCertParseSuccess);
                    Log.d(TAG, "IMonitor send eventCertParseSuccess");
                    return;
                case HwMdmDFTConst.EID_MDM_DFT_SET_DEVICEOWNER_SUCCESS /* 907504002 */:
                    IMonitor.EventStream eventDoSuccess = IMonitor.openEventStream((int) HwMdmDFTConst.EID_MDM_DFT_SET_DEVICEOWNER_SUCCESS);
                    if (eventDoSuccess == null) {
                        Log.e(TAG, "eventDoSuccess is null.");
                        return;
                    }
                    HwMdmDoPackageInfo infoDo = (HwMdmDoPackageInfo) hwMdmDFT;
                    if (isChinaVersion()) {
                        eventDoSuccess.setParam(0, infoDo.getPkg()).setParam(1, infoDo.getVersion()).setParam(2, infoDo.getSighash());
                    } else {
                        eventDoSuccess.setParam(1, "1");
                    }
                    IMonitor.sendEvent(eventDoSuccess);
                    IMonitor.closeEventStream(eventDoSuccess);
                    Log.d(TAG, "IMonitor send eventDoSuccess");
                    return;
                case HwMdmDFTConst.EID_MDM_DFT_SET_WORKPROFILE_SUCCESS /* 907504003 */:
                    IMonitor.EventStream eventWpSuccess = IMonitor.openEventStream((int) HwMdmDFTConst.EID_MDM_DFT_SET_WORKPROFILE_SUCCESS);
                    if (eventWpSuccess == null) {
                        Log.e(TAG, "eventWpSuccess is null.");
                        return;
                    }
                    HwMdmWpPackageInfo infoWp = (HwMdmWpPackageInfo) hwMdmDFT;
                    if (isChinaVersion()) {
                        IMonitor.EventStream param = eventWpSuccess.setParam(0, infoWp.getPkg()).setParam(1, infoWp.getVersion()).setParam(2, infoWp.getSighash());
                        IMonitor.EventStream param2 = param.setParam(3, infoWp.getDopkg() + "");
                        IMonitor.EventStream param3 = param2.setParam(4, infoWp.getDopkgver() + "");
                        param3.setParam(5, infoWp.getDopkgsighash() + "");
                    } else {
                        eventWpSuccess.setParam(1, "2").setParam(4, "1");
                    }
                    IMonitor.sendEvent(eventWpSuccess);
                    IMonitor.closeEventStream(eventWpSuccess);
                    Log.d(TAG, "IMonitor send eventWpSuccess");
                    return;
                default:
                    Log.d(TAG, "should not happen");
                    return;
            }
        }
    }

    public static String getCertificateSHA256Fingerprint(Context context, String pkg) {
        PackageManager pm;
        Signature[] signatures;
        if (context == null || (pm = context.getPackageManager()) == null) {
            return "";
        }
        try {
            PackageInfo packageInfo = pm.getPackageInfo(pkg, 64);
            if (!(packageInfo == null || (signatures = packageInfo.signatures) == null)) {
                if (signatures.length > 0) {
                    return getSha256FingerData(signatures[0].toByteArray());
                }
            }
            return "";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getCertificateSHA256Fingerprint: Exception");
            return null;
        }
    }

    private static String getSha256FingerData(byte[] cert) {
        try {
            return byte2HexFormatted(MessageDigest.getInstance("SHA256").digest(((X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(cert))).getEncoded()));
        } catch (CertificateException e) {
            Log.e(TAG, "getSha256FingerData: CertificateException");
            return null;
        } catch (NoSuchAlgorithmException e2) {
            Log.e(TAG, "getSha256FingerData: NoSuchAlgorithmException");
            return null;
        } catch (RuntimeException e3) {
            throw e3;
        } catch (Exception e4) {
            Log.e(TAG, "getSha256FingerData catch exception");
            return null;
        }
    }

    private static String byte2HexFormatted(byte[] arr) {
        if (arr == null) {
            return null;
        }
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) {
                h = "0" + h;
            } else if (l > 2) {
                h = h.substring(l - 2, l);
            }
            str.append(h.toUpperCase());
            if (i < arr.length - 1) {
                str.append(':');
            }
        }
        return str.toString();
    }

    public static void getMdmInstallInfoDft(Context mContext, PackageParser.Package pkg) {
        Signature[] signatures;
        String sign;
        if (pkg != null && mContext != null) {
            HwMdmInstallInfo mdmInstallInfo = new HwMdmInstallInfo();
            mdmInstallInfo.setPkg(pkg.packageName);
            mdmInstallInfo.setVersion(pkg.mVersionName);
            if (mContext.getPackageManager() != null && pkg.mSigningDetails != null && (signatures = pkg.mSigningDetails.signatures) != null && (sign = getSha256FingerData(signatures[0].toByteArray())) != null) {
                mdmInstallInfo.setSighash(sign);
                handleMdmDftUploadEvent(HwMdmDFTConst.EID_MDM_DFT_CERT_PARSE_SUCCESS, mdmInstallInfo);
            }
        }
    }

    private static boolean isChinaVersion() {
        String propCode = SystemPropertiesEx.get("ro.product.locale.region");
        String country = Locale.CHINA.getCountry();
        if (country == null || !country.equalsIgnoreCase(propCode)) {
            return false;
        }
        return true;
    }
}
