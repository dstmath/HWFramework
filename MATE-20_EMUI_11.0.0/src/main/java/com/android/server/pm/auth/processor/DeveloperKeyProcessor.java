package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class DeveloperKeyProcessor implements IProcessor {
    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine) || certData == null || !certLine.startsWith(HwCertification.KEY_DEVELOPER) || certLine.length() <= HwCertification.KEY_DEVELOPER.length() + 1) {
            return false;
        }
        String developerKey = certLine.substring(HwCertification.KEY_DEVELOPER.length() + 1);
        if (TextUtils.isEmpty(developerKey)) {
            HwAuthLogger.error(IProcessor.TAG, "DK_RC is empty!");
            return false;
        }
        certData.setDeveloperKey(developerKey);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        HwCertification.CertificationData certData;
        if (hwCert == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String developerKey = certData.getDeveloperKey();
        if (!TextUtils.isEmpty(developerKey)) {
            hwCert.setDeveloperKey(developerKey);
            return true;
        }
        HwAuthLogger.error(IProcessor.TAG, "DK_PC error!");
        return false;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg == null || hwCert == null) {
            return false;
        }
        String developerKey = hwCert.getDeveloperKey();
        if (TextUtils.isEmpty(developerKey)) {
            return false;
        }
        List<Signature> signatures = null;
        List<Signature> pastSignatures = null;
        if (pkg.mSigningDetails.signatures != null) {
            signatures = new ArrayList<>(Arrays.asList(pkg.mSigningDetails.signatures));
        }
        if (pkg.mSigningDetails.pastSigningCertificates != null) {
            pastSignatures = new ArrayList<>(Arrays.asList(pkg.mSigningDetails.pastSigningCertificates));
        }
        if (signatures != null) {
            int size = signatures.size();
            for (int i = 0; i < size; i++) {
                Signature signature = signatures.get(i);
                if (signature != null && developerKey.equals(signature.toCharsString())) {
                    return true;
                }
            }
        }
        if (pastSignatures != null) {
            int size2 = pastSignatures.size();
            for (int i2 = 0; i2 < size2; i2++) {
                Signature signature2 = pastSignatures.get(i2);
                if (signature2 != null && developerKey.equals(signature2.toCharsString())) {
                    return true;
                }
            }
        }
        HwAuthLogger.error(IProcessor.TAG, "DK_VC not same!");
        return false;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (!TextUtils.isEmpty(tag) && parser != null && hwCert != null && HwCertification.KEY_DEVELOPER.equals(tag)) {
            String developerKey = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setDeveloperKey(developerKey);
                return true;
            }
        }
        return false;
    }
}
