package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.DevicePublicKeyLoader;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.pm.auth.util.EncryptionUtils;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import org.xmlpull.v1.XmlPullParser;

public class SignatureV1Processor implements IProcessor {
    private static final String WINDOWS_LINE_SEPARATOR = "\r\n";

    public static String generatePartlyContent(boolean isRelease, HwCertification hwCert) {
        String str = "";
        if (hwCert == null) {
            return str;
        }
        StringBuilder strBuilder = new StringBuilder();
        String version = hwCert.getVersion();
        if (!TextUtils.isEmpty(version)) {
            strBuilder.append(HwCertification.KEY_VERSION);
            strBuilder.append(AwarenessInnerConstants.COLON_KEY);
            strBuilder.append(version);
            strBuilder.append(WINDOWS_LINE_SEPARATOR);
        }
        String packageName = hwCert.getPackageName();
        String developerKey = hwCert.getDeveloperKey();
        HwCertification.CertificationData certData = hwCert.getCertificationData();
        if (certData == null) {
            return str;
        }
        String permissionsString = certData.getPermissionsString();
        strBuilder.append(HwCertification.KEY_DEVELOPER);
        strBuilder.append(AwarenessInnerConstants.COLON_KEY);
        strBuilder.append(developerKey == null ? str : developerKey);
        strBuilder.append(WINDOWS_LINE_SEPARATOR);
        strBuilder.append(HwCertification.KEY_PACKAGE_NAME);
        strBuilder.append(AwarenessInnerConstants.COLON_KEY);
        strBuilder.append(packageName == null ? str : packageName);
        strBuilder.append(WINDOWS_LINE_SEPARATOR);
        strBuilder.append(HwCertification.KEY_PERMISSIONS);
        strBuilder.append(AwarenessInnerConstants.COLON_KEY);
        if (permissionsString != null) {
            str = permissionsString;
        }
        strBuilder.append(str);
        strBuilder.append(WINDOWS_LINE_SEPARATOR);
        return generatePartlyContentInner(strBuilder, isRelease, hwCert, certData);
    }

    private static String generatePartlyContentInner(StringBuilder strBuilder, boolean isRelease, HwCertification hwCert, HwCertification.CertificationData certData) {
        String str = "";
        if (!isRelease) {
            String deviceIdsString = certData.getDeviceIdsString();
            strBuilder.append(HwCertification.KEY_DEVICE_IDS);
            strBuilder.append(AwarenessInnerConstants.COLON_KEY);
            strBuilder.append(deviceIdsString == null ? str : deviceIdsString);
            strBuilder.append(WINDOWS_LINE_SEPARATOR);
        } else if (hwCert.isCustomized()) {
            strBuilder.append(HwCertification.KEY_DEVICE_IDS);
            strBuilder.append(":#");
            strBuilder.append(WINDOWS_LINE_SEPARATOR);
        } else {
            strBuilder.append(HwCertification.KEY_DEVICE_IDS);
            strBuilder.append(":*");
            strBuilder.append(WINDOWS_LINE_SEPARATOR);
        }
        String period = certData.getPeriodString();
        strBuilder.append(HwCertification.KEY_VALID_PERIOD);
        strBuilder.append(AwarenessInnerConstants.COLON_KEY);
        strBuilder.append(period);
        strBuilder.append(WINDOWS_LINE_SEPARATOR);
        if (isRelease) {
            String apkHash = hwCert.getApkHash();
            strBuilder.append(HwCertification.KEY_APK_HASH);
            strBuilder.append(AwarenessInnerConstants.COLON_KEY);
            if (apkHash != null) {
                str = apkHash;
            }
            strBuilder.append(str);
            strBuilder.append(WINDOWS_LINE_SEPARATOR);
        } else {
            strBuilder.append(HwCertification.KEY_APK_HASH);
            strBuilder.append(":*");
            strBuilder.append(WINDOWS_LINE_SEPARATOR);
        }
        String certificate = hwCert.getCertificate();
        String extension = hwCert.getExtension();
        if (!TextUtils.isEmpty(certificate)) {
            strBuilder.append(HwCertification.KEY_CERTIFICATE);
            strBuilder.append(AwarenessInnerConstants.COLON_KEY);
            strBuilder.append(certificate);
            strBuilder.append(WINDOWS_LINE_SEPARATOR);
        }
        if (!TextUtils.isEmpty(extension)) {
            strBuilder.append(HwCertification.KEY_EXTENSION);
            strBuilder.append(AwarenessInnerConstants.COLON_KEY);
            strBuilder.append(extension);
            strBuilder.append(WINDOWS_LINE_SEPARATOR);
        }
        return strBuilder.toString();
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine) || certData == null || !certLine.startsWith(HwCertification.KEY_SIGNATURE) || certLine.length() <= HwCertification.KEY_SIGNATURE.length() + 1) {
            return false;
        }
        String signatureV1 = certLine.substring(HwCertification.KEY_SIGNATURE.length() + 1);
        if (TextUtils.isEmpty(signatureV1)) {
            HwAuthLogger.error(IProcessor.TAG, "SN_RC is empty!");
            return false;
        }
        certData.setSignatureV1(signatureV1);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        HwCertification.CertificationData certData;
        if (hwCert == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String signatureV1 = certData.getSignatureV1();
        if (!TextUtils.isEmpty(signatureV1)) {
            hwCert.setSignatureV1(signatureV1);
            return true;
        }
        HwAuthLogger.error(IProcessor.TAG, "SN_PC error!");
        return false;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        HwCertification.CertificationData certData;
        if (pkg == null || hwCert == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String signatureV1 = certData.getSignatureV1();
        if (TextUtils.isEmpty(signatureV1)) {
            return false;
        }
        String contentFromText = generatePartlyContent(hwCert.isReleased(), hwCert);
        HwCertificationManager certificationManager = HwCertificationManager.getInstance();
        if (certificationManager == null) {
            return false;
        }
        if (certificationManager.checkMdmCertBlacklist(signatureV1)) {
            HwAuthLogger.info(IProcessor.TAG, "signature v1 contains in blacklist, need to return false.");
            return false;
        }
        try {
            byte[] digestBytes = EncryptionUtils.sha256(contentFromText.getBytes(StandardCharsets.UTF_8.name()));
            PublicKey publicKey = DevicePublicKeyLoader.getPublicKey(HwCertificationManager.getInstance().getContext());
            if (publicKey == null) {
                return false;
            }
            boolean isVerified = EncryptionUtils.verify(digestBytes, publicKey, Utils.stringToHexBytes(signatureV1));
            StringBuilder sb = new StringBuilder();
            sb.append("SN_VC result:");
            sb.append(isVerified ? "OK" : "not the same");
            HwAuthLogger.info(IProcessor.TAG, sb.toString());
            return isVerified;
        } catch (NoSuchAlgorithmException e) {
            HwAuthLogger.error(IProcessor.TAG, "SN_VC no such algorithm!");
            return false;
        } catch (UnsupportedEncodingException e2) {
            HwAuthLogger.error(IProcessor.TAG, "SN_VC unsupported encoding!");
            return false;
        } catch (InvalidKeyException e3) {
            HwAuthLogger.error(IProcessor.TAG, "SN_VC invalid key!");
            return false;
        } catch (SignatureException e4) {
            HwAuthLogger.error(IProcessor.TAG, "SN_VC signature error!");
            return false;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (!TextUtils.isEmpty(tag) && parser != null && hwCert != null && HwCertification.KEY_SIGNATURE.equals(tag)) {
            String signatureV1 = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setSignatureV1(signatureV1);
                return true;
            }
        }
        return false;
    }
}
