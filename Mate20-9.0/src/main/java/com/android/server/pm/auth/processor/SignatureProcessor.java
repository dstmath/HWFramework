package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import com.android.server.pm.auth.DevicePublicKeyLoader;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.pm.auth.util.CryptionUtils;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.security.PublicKey;
import org.xmlpull.v1.XmlPullParser;

public class SignatureProcessor extends BaseProcessor {
    private static final String SEPARATOR = "\r\n";

    public boolean readCert(String line, HwCertification.CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_SIGNATURE)) {
            return false;
        }
        String key = line.substring(HwCertification.KEY_SIGNATURE.length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "SN_RC is empty");
            return false;
        }
        rawCert.mSignature = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        HwCertification.CertificationData certData = rawCert.mCertificationData;
        if (certData.mSignature == null || certData.mSignature.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "SN_PC error");
            return false;
        }
        rawCert.setSignature(certData.mSignature);
        return true;
    }

    public boolean verifyCert(PackageParser.Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHwFlow()) {
            StringBuilder sb = new StringBuilder();
            sb.append("SN_VC start ");
            sb.append(cert.isReleased() ? "released cert--" : "debugged cert--");
            HwAuthLogger.i("HwCertificationManager", sb.toString());
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
            if (HwAuthLogger.getHwFlow()) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("SN_VC result:");
                sb2.append(result ? "OK" : "not the same");
                HwAuthLogger.i("HwCertificationManager", sb2.toString());
            }
            return result;
        } catch (RuntimeException e) {
            HwAuthLogger.e("HwCertificationManager", "SN_VC RuntimeException when encounting");
            return false;
        } catch (Exception e2) {
            HwAuthLogger.e("HwCertificationManager", "SN_VC exception when encounting");
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
        if (tVersion != null && !tVersion.isEmpty()) {
            sb.append(HwCertification.KEY_VERSION);
            sb.append(":");
            sb.append(tVersion);
            sb.append(SEPARATOR);
        }
        sb.append(HwCertification.KEY_DEVELIOPER);
        sb.append(":");
        sb.append(tDelveoperKey == null ? "" : tDelveoperKey);
        sb.append(SEPARATOR);
        sb.append("PackageName");
        sb.append(":");
        sb.append(tPackageName == null ? "" : tPackageName);
        sb.append(SEPARATOR);
        sb.append(HwCertification.KEY_PERMISSIONS);
        sb.append(":");
        sb.append(tPermissionsString == null ? "" : tPermissionsString);
        sb.append(SEPARATOR);
        if (!isRelease) {
            sb.append(HwCertification.KEY_DEVICE_IDS);
            sb.append(":");
            sb.append(tDeviceIdsString == null ? "" : tDeviceIdsString);
            sb.append(SEPARATOR);
        } else {
            sb.append(HwCertification.KEY_DEVICE_IDS);
            sb.append(":*");
            sb.append(SEPARATOR);
        }
        sb.append(HwCertification.KEY_VALID_PERIOD);
        sb.append(":");
        sb.append(tPeriod);
        sb.append(SEPARATOR);
        if (isRelease) {
            sb.append(HwCertification.KEY_APK_HASH);
            sb.append(":");
            sb.append(tApkHash == null ? "" : tApkHash);
            sb.append(SEPARATOR);
        } else {
            sb.append(HwCertification.KEY_APK_HASH);
            sb.append(":*");
            sb.append(SEPARATOR);
        }
        if (tCertificate != null && !tCertificate.isEmpty()) {
            sb.append(HwCertification.KEY_CERTIFICATE);
            sb.append(":");
            sb.append(tCertificate);
            sb.append(SEPARATOR);
        }
        if (tExtenstion != null && !tExtenstion.isEmpty()) {
            sb.append(HwCertification.KEY_EXTENSION);
            sb.append(":");
            sb.append(tExtenstion);
            sb.append(SEPARATOR);
        }
        return sb.toString();
    }
}
