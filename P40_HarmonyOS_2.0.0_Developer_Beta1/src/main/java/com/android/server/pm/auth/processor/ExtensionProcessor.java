package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class ExtensionProcessor implements IProcessor {
    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine)) {
            HwAuthLogger.error(IProcessor.TAG, "ET_RC line is null!");
            return false;
        } else if (certData == null) {
            HwAuthLogger.error(IProcessor.TAG, "ET_RC is empty!");
            return false;
        } else if (!certLine.startsWith("Extension:") || certLine.length() <= HwCertification.KEY_EXTENSION.length() + 1) {
            HwAuthLogger.error(IProcessor.TAG, "ET_RC error!");
            return false;
        } else {
            certData.setExtension(certLine.substring(HwCertification.KEY_EXTENSION.length() + 1));
            return true;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        if (hwCert == null) {
            HwAuthLogger.error(IProcessor.TAG, "ET_PC error cert is null!");
            return false;
        }
        HwCertification.CertificationData certData = hwCert.getCertificationData();
        if (certData == null) {
            return false;
        }
        String extension = certData.getExtension();
        if (TextUtils.isEmpty(extension)) {
            HwAuthLogger.error(IProcessor.TAG, "ET_PC error is null!");
            return false;
        }
        hwCert.setExtension(extension);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg == null || hwCert == null) {
            HwAuthLogger.error(IProcessor.TAG, "ET_VC error package or cert is null!");
            return false;
        } else if (!TextUtils.isEmpty(hwCert.getExtension())) {
            return true;
        } else {
            HwAuthLogger.error(IProcessor.TAG, "ET_VC is empty!");
            return false;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (TextUtils.isEmpty(tag) || parser == null || hwCert == null) {
            return false;
        }
        if (HwCertification.KEY_EXTENSION.equals(tag)) {
            String extension = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setExtension(extension);
                return true;
            }
        }
        HwAuthLogger.error(IProcessor.TAG, "ET_PX error!");
        return false;
    }
}
