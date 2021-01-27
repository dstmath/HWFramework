package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class CertificateProcessor implements IProcessor {
    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine)) {
            HwAuthLogger.error(IProcessor.TAG, "CF_RC line is empty!");
            return false;
        } else if (certData == null) {
            HwAuthLogger.error(IProcessor.TAG, "CF_RC cert is empty!");
            return false;
        } else if (!certLine.startsWith("Certificate:") || certLine.length() <= HwCertification.KEY_CERTIFICATE.length() + 1) {
            HwAuthLogger.error(IProcessor.TAG, "CF_RC error!");
            return false;
        } else {
            certData.setCertificate(certLine.substring(HwCertification.KEY_CERTIFICATE.length() + 1));
            return true;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        if (hwCert == null) {
            HwAuthLogger.error(IProcessor.TAG, "CF_PC cert is null!");
            return false;
        }
        HwCertification.CertificationData certData = hwCert.getCertificationData();
        if (certData == null) {
            return false;
        }
        String certificate = certData.getCertificate();
        if (TextUtils.isEmpty(certificate)) {
            HwAuthLogger.error(IProcessor.TAG, "CF_PC is empty!");
            return false;
        } else if (!HwCertification.isContainsCertificateType(certificate)) {
            HwAuthLogger.error(IProcessor.TAG, "CF_PC not in reasonable range!");
            return false;
        } else {
            hwCert.setCertificate(certData.getCertificate());
            return true;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg == null || hwCert == null) {
            HwAuthLogger.error(IProcessor.TAG, "CF_VC error or package is null!");
            return false;
        }
        String certificate = hwCert.getCertificate();
        if (TextUtils.isEmpty(certificate)) {
            HwAuthLogger.error(IProcessor.TAG, "CF_VC is empty!");
            return false;
        }
        HwCertification.CertificationData certData = hwCert.getCertificationData();
        if (certData == null || certData.getPermissionsString() == null) {
            HwAuthLogger.error(IProcessor.TAG, "CF_VC cert data or permission is null!");
            return false;
        } else if (!certificate.equals("null") || !"null".equals(certData.getPermissionsString())) {
            return true;
        } else {
            HwAuthLogger.error(IProcessor.TAG, "CF_VC permission is not allowed!");
            return false;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (TextUtils.isEmpty(tag) || parser == null || hwCert == null) {
            return false;
        }
        if (HwCertification.KEY_CERTIFICATE.equals(tag)) {
            String certificate = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setCertificate(certificate);
                return true;
            }
        }
        HwAuthLogger.error(IProcessor.TAG, "CF_PX error!");
        return false;
    }
}
