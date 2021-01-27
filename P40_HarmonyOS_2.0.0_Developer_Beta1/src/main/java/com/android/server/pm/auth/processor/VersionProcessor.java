package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class VersionProcessor implements IProcessor {
    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine)) {
            HwAuthLogger.error(IProcessor.TAG, "VPR_RC line is empty!");
            return false;
        } else if (certData == null) {
            HwAuthLogger.error(IProcessor.TAG, "VPR_RC cert is null!");
            return false;
        } else if (!certLine.startsWith("Version:") || certLine.length() <= HwCertification.KEY_VERSION.length() + 1) {
            HwAuthLogger.error(IProcessor.TAG, "VPR_RC error!");
            return false;
        } else {
            certData.setVersion(certLine.substring(HwCertification.KEY_VERSION.length() + 1));
            return true;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        if (hwCert == null) {
            HwAuthLogger.error(IProcessor.TAG, "VPR_PC error cert is null!");
            return false;
        }
        HwCertification.CertificationData certData = hwCert.getCertificationData();
        if (certData == null) {
            return false;
        }
        String version = certData.getVersion();
        if (TextUtils.isEmpty(version)) {
            HwAuthLogger.error(IProcessor.TAG, "VPR_PC error is empty!");
            return false;
        }
        hwCert.setVersion(version);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg == null || hwCert == null) {
            HwAuthLogger.error(IProcessor.TAG, "VPR_VC error package or cert is null!");
            return false;
        } else if (!TextUtils.isEmpty(hwCert.getVersion())) {
            return true;
        } else {
            HwAuthLogger.error(IProcessor.TAG, "VPR_VC error is empty!");
            return false;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (TextUtils.isEmpty(tag) || parser == null || hwCert == null) {
            return false;
        }
        if (HwCertification.KEY_VERSION.equals(tag)) {
            String version = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setVersion(version);
                return true;
            }
        }
        HwAuthLogger.error(IProcessor.TAG, "VPR_PX error!");
        return false;
    }
}
