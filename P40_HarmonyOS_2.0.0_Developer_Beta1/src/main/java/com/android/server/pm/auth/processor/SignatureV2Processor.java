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

public class SignatureV2Processor implements IProcessor {
    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine) || certData == null) {
            HwAuthLogger.error(IProcessor.TAG, "read cert null!");
            return false;
        } else if (!certLine.startsWith(HwCertification.KEY_SIGNATURE2) || HwCertification.KEY_SIGNATURE2.length() + 1 > certLine.length() - 1) {
            return false;
        } else {
            String signatureV2 = certLine.substring(HwCertification.KEY_SIGNATURE2.length() + 1);
            if (TextUtils.isEmpty(signatureV2)) {
                HwAuthLogger.error(IProcessor.TAG, "SN2_RC is empty!");
                return false;
            }
            certData.setSignatureV2(signatureV2);
            return true;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        if (hwCert == null) {
            HwAuthLogger.error(IProcessor.TAG, "parse cert null!");
            return false;
        }
        HwCertification.CertificationData certData = hwCert.getCertificationData();
        if (certData == null) {
            return false;
        }
        String signatureV2 = certData.getSignatureV2();
        if (TextUtils.isEmpty(signatureV2)) {
            HwAuthLogger.error(IProcessor.TAG, "parser signature v2 is null, package name is " + hwCert.getPackageName());
            return false;
        }
        hwCert.setSignatureV2(signatureV2);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        HwCertification.CertificationData certData;
        if (hwCert == null || pkg == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String signatureV2 = certData.getSignatureV2();
        if (TextUtils.isEmpty(signatureV2)) {
            HwAuthLogger.error(IProcessor.TAG, "This is old cert, no signature v2!");
            return false;
        }
        HwCertificationManager certificationManager = HwCertificationManager.getInstance();
        if (certificationManager == null) {
            return false;
        }
        if (certificationManager.checkMdmCertBlacklist(signatureV2)) {
            HwAuthLogger.info(IProcessor.TAG, "signature v2 contained in blacklist, need to return false.");
            return false;
        }
        try {
            byte[] digestBytes = EncryptionUtils.sha256(SignatureV1Processor.generatePartlyContent(hwCert.isReleased(), hwCert).getBytes(StandardCharsets.UTF_8.name()));
            PublicKey publicKey = DevicePublicKeyLoader.getPublicKeyForBase64(DevicePublicKeyLoader.EMUI10_PK);
            if (publicKey == null) {
                HwAuthLogger.error(IProcessor.TAG, "SN2_VC pubKey is null!");
                return false;
            }
            try {
                boolean isVerified = EncryptionUtils.verify(digestBytes, publicKey, Utils.stringToHexBytes(signatureV2));
                StringBuilder sb = new StringBuilder();
                sb.append("SN2_VC result:");
                sb.append(isVerified ? "OK" : "not the same");
                HwAuthLogger.info(IProcessor.TAG, sb.toString());
                return isVerified;
            } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
                HwAuthLogger.error(IProcessor.TAG, "signature v2 verify Exception!");
                return false;
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e2) {
            HwAuthLogger.error(IProcessor.TAG, "sha256 has Exception!");
            return false;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (!TextUtils.isEmpty(tag) && hwCert != null && parser != null && HwCertification.KEY_SIGNATURE2.equals(tag)) {
            String signatureV2 = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setSignatureV2(signatureV2);
                return true;
            }
        }
        return false;
    }
}
