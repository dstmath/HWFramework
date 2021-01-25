package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.DevicePublicKeyLoader;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.pm.auth.util.EncryptionUtils;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import org.xmlpull.v1.XmlPullParser;

public class SignatureV3Processor implements IProcessor {
    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine) || certData == null) {
            HwAuthLogger.error(IProcessor.TAG, "readCert V3 cert data is null!");
            return false;
        } else if (!certLine.startsWith(HwCertification.KEY_SIGNATURE3) || HwCertification.KEY_SIGNATURE3.length() + 1 > certLine.length() - 1) {
            return false;
        } else {
            String signatureV3 = certLine.substring(HwCertification.KEY_SIGNATURE3.length() + 1);
            if (TextUtils.isEmpty(signatureV3)) {
                HwAuthLogger.error(IProcessor.TAG, "readCert signature V3 is empty!");
                return false;
            }
            certData.setSignatureV3(signatureV3);
            return true;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        if (hwCert == null) {
            HwAuthLogger.error(IProcessor.TAG, "parseCert V3 cert is null!");
            return false;
        }
        HwCertification.CertificationData certData = hwCert.getCertificationData();
        if (certData == null) {
            return false;
        }
        String signatureV3 = certData.getSignatureV3();
        if (TextUtils.isEmpty(signatureV3)) {
            HwAuthLogger.error(IProcessor.TAG, "parseCert signature V3 is empty, package name is " + hwCert.getPackageName());
            return false;
        }
        hwCert.setSignatureV3(signatureV3);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        HwCertification.CertificationData certData;
        if (hwCert == null || pkg == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String signatureV3 = certData.getSignatureV3();
        if (TextUtils.isEmpty(signatureV3)) {
            HwAuthLogger.error(IProcessor.TAG, "verifyCert signature V3 is empty!");
            return false;
        }
        HwCertificationManager certificationManager = HwCertificationManager.getInstance();
        if (certificationManager == null) {
            return false;
        }
        if (certificationManager.checkMdmCertBlacklist(signatureV3)) {
            HwAuthLogger.info(IProcessor.TAG, "verifyCert signature V3 contained in blacklist, need to return false.");
            return false;
        }
        try {
            byte[] digestBytes = EncryptionUtils.sha384(SignatureV1Processor.generatePartlyContent(hwCert.isReleased(), hwCert).getBytes(StandardCharsets.UTF_8.name()));
            PublicKey publicKey = DevicePublicKeyLoader.getPublicKeyForBase64(DevicePublicKeyLoader.EMUI11_PK);
            if (publicKey == null) {
                HwAuthLogger.error(IProcessor.TAG, "verifyCert signature V3 public key is null!");
                return false;
            }
            try {
                boolean isVerified = EncryptionUtils.verifySignatureV3(digestBytes, publicKey, Utils.stringToHexBytes(signatureV3));
                StringBuilder sb = new StringBuilder();
                sb.append("verifyCert signature V3 result: ");
                sb.append(isVerified ? "OK" : "not the same");
                HwAuthLogger.info(IProcessor.TAG, sb.toString());
                return isVerified;
            } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
                HwAuthLogger.error(IProcessor.TAG, "verifyCert signature V3 has Exception!");
                return false;
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e2) {
            HwAuthLogger.error(IProcessor.TAG, "verifyCert sha256 has Exception!");
            return false;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (!TextUtils.isEmpty(tag) && hwCert != null && parser != null && HwCertification.KEY_SIGNATURE3.equals(tag)) {
            String signatureV3 = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setSignatureV3(signatureV3);
                return true;
            }
        }
        return false;
    }
}
