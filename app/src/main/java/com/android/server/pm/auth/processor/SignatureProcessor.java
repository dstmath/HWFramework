package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.DevicePublicKeyLoader;
import com.android.server.pm.auth.HwCertXmlHandler;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.pm.auth.util.CryptionUtils;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.security.PublicKey;
import org.xmlpull.v1.XmlPullParser;

public class SignatureProcessor extends BaseProcessor {
    private static final String SEPARATOR = "\r\n";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_SIGNATURE)) {
            return false;
        }
        String key = line.substring(HwCertification.KEY_SIGNATURE.length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "readCert:Signature is empty");
            return false;
        }
        rawCert.mSignature = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        CertificationData certData = rawCert.mCertificationData;
        if (certData.mSignature == null || certData.mSignature.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "parserCert:Signature error");
            return false;
        }
        rawCert.setSignature(certData.mSignature);
        return true;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i(Utils.TAG, "--Verify signature:" + (cert.isReleased() ? "released cert--" : "debugged cert--"));
        }
        String orginal = cert.mCertificationData.mSignature;
        if (orginal == null || orginal.isEmpty()) {
            return false;
        }
        try {
            byte[] digestFromFileText = CryptionUtils.sha256(generatePartlyContent(cert.isReleased(), cert).getBytes("UTF-8"));
            PublicKey pubKey = DevicePublicKeyLoader.getPublicKey(HwCertificationManager.getIntance().getContext());
            if (pubKey == null) {
                return false;
            }
            boolean result = CryptionUtils.verify(digestFromFileText, pubKey, Utils.stringToBytes(orginal));
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i(Utils.TAG, "verifyCert:verify signature result:" + (result ? "OK" : "not the same"));
            }
            return result;
        } catch (RuntimeException e) {
            HwAuthLogger.e(Utils.TAG, "verifyCert:encounting exception when verifying signature : RuntimeException");
            return false;
        } catch (Exception e2) {
            HwAuthLogger.e(Utils.TAG, "verifyCert:encounting exception when verifying signature");
            return false;
        }
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (!HwCertification.KEY_SIGNATURE.equals(tag)) {
            return false;
        }
        cert.mCertificationData.mSignature = parser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE);
        return true;
    }

    private String generatePartlyContent(boolean isRelease, HwCertification rawCert) {
        StringBuffer sb = new StringBuffer();
        String tDelveoperKey = rawCert.getDelveoperKey();
        String tPackageName = rawCert.getPackageName();
        String tPermissionsString = rawCert.mCertificationData.mPermissionsString;
        String tDeviceIdsString = rawCert.mCertificationData.mDeviceIdsString;
        String tPeriod = rawCert.mCertificationData.mPeriodString;
        String tApkHash = rawCert.getApkHash();
        String tCertificate = rawCert.getCertificate();
        String tVersion = rawCert.getVersion();
        String tExtenstion = rawCert.getExtenstion();
        if (!(tVersion == null || tVersion.isEmpty())) {
            sb.append("Version:" + tVersion).append(SEPARATOR);
        }
        StringBuilder append = new StringBuilder().append("DeveloperKey:");
        if (tDelveoperKey == null) {
            tDelveoperKey = AppHibernateCst.INVALID_PKG;
        }
        sb.append(append.append(tDelveoperKey).toString()).append(SEPARATOR);
        append = new StringBuilder().append("PackageName:");
        if (tPackageName == null) {
            tPackageName = AppHibernateCst.INVALID_PKG;
        }
        sb.append(append.append(tPackageName).toString()).append(SEPARATOR);
        append = new StringBuilder().append("Permissions:");
        if (tPermissionsString == null) {
            tPermissionsString = AppHibernateCst.INVALID_PKG;
        }
        sb.append(append.append(tPermissionsString).toString()).append(SEPARATOR);
        if (isRelease) {
            sb.append("DeviceIds:*").append(SEPARATOR);
        } else {
            append = new StringBuilder().append("DeviceIds:");
            if (tDeviceIdsString == null) {
                tDeviceIdsString = AppHibernateCst.INVALID_PKG;
            }
            sb.append(append.append(tDeviceIdsString).toString()).append(SEPARATOR);
        }
        sb.append("ValidPeriod:" + tPeriod).append(SEPARATOR);
        if (isRelease) {
            append = new StringBuilder().append("ApkHash:");
            if (tApkHash == null) {
                tApkHash = AppHibernateCst.INVALID_PKG;
            }
            sb.append(append.append(tApkHash).toString()).append(SEPARATOR);
        } else {
            sb.append("ApkHash:*").append(SEPARATOR);
        }
        if (!(tCertificate == null || tCertificate.isEmpty())) {
            sb.append("Certificate:" + tCertificate).append(SEPARATOR);
        }
        if (!(tExtenstion == null || tExtenstion.isEmpty())) {
            sb.append("Extension:" + tExtenstion).append(SEPARATOR);
        }
        return sb.toString();
    }
}
