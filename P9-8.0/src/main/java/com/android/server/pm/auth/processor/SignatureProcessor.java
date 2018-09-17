package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.DevicePublicKeyLoader;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.pm.auth.util.CryptionUtils;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.security.PublicKey;
import org.xmlpull.v1.XmlPullParser;

public class SignatureProcessor extends BaseProcessor {
    private static final String SEPARATOR = "\r\n";

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_SIGNATURE)) {
            return false;
        }
        String key = line.substring(HwCertification.KEY_SIGNATURE.length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "readCert:Signature is empty");
            return false;
        }
        rawCert.mSignature = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        CertificationData certData = rawCert.mCertificationData;
        if (certData.mSignature == null || (certData.mSignature.isEmpty() ^ 1) == 0) {
            HwAuthLogger.e("HwCertificationManager", "parserCert:Signature error");
            return false;
        }
        rawCert.setSignature(certData.mSignature);
        return true;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "--Verify signature:" + (cert.isReleased() ? "released cert--" : "debugged cert--"));
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
                HwAuthLogger.i("HwCertificationManager", "verifyCert:verify signature result:" + (result ? "OK" : "not the same"));
            }
            return result;
        } catch (RuntimeException e) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:encounting exception when verifying signature : RuntimeException");
            return false;
        } catch (Exception e2) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:encounting exception when verifying signature");
            return false;
        }
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (!HwCertification.KEY_SIGNATURE.equals(tag)) {
            return false;
        }
        cert.mCertificationData.mSignature = parser.getAttributeValue(null, "value");
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
        if (!(tVersion == null || (tVersion.isEmpty() ^ 1) == 0)) {
            sb.append(HwCertification.KEY_VERSION).append(":").append(tVersion).append(SEPARATOR);
        }
        StringBuffer append = sb.append(HwCertification.KEY_DEVELIOPER).append(":");
        if (tDelveoperKey == null) {
            tDelveoperKey = "";
        }
        append.append(tDelveoperKey).append(SEPARATOR);
        append = sb.append("PackageName").append(":");
        if (tPackageName == null) {
            tPackageName = "";
        }
        append.append(tPackageName).append(SEPARATOR);
        append = sb.append(HwCertification.KEY_PERMISSIONS).append(":");
        if (tPermissionsString == null) {
            tPermissionsString = "";
        }
        append.append(tPermissionsString).append(SEPARATOR);
        if (isRelease) {
            sb.append(HwCertification.KEY_DEVICE_IDS).append(":*").append(SEPARATOR);
        } else {
            append = sb.append(HwCertification.KEY_DEVICE_IDS).append(":");
            if (tDeviceIdsString == null) {
                tDeviceIdsString = "";
            }
            append.append(tDeviceIdsString).append(SEPARATOR);
        }
        sb.append(HwCertification.KEY_VALID_PERIOD).append(":").append(tPeriod).append(SEPARATOR);
        if (isRelease) {
            append = sb.append(HwCertification.KEY_APK_HASH).append(":");
            if (tApkHash == null) {
                tApkHash = "";
            }
            append.append(tApkHash).append(SEPARATOR);
        } else {
            sb.append(HwCertification.KEY_APK_HASH).append(":*").append(SEPARATOR);
        }
        if (!(tCertificate == null || (tCertificate.isEmpty() ^ 1) == 0)) {
            sb.append(HwCertification.KEY_CERTIFICATE).append(":").append(tCertificate).append(SEPARATOR);
        }
        if (!(tExtenstion == null || (tExtenstion.isEmpty() ^ 1) == 0)) {
            sb.append(HwCertification.KEY_EXTENSION).append(":").append(tExtenstion).append(SEPARATOR);
        }
        return sb.toString();
    }
}
