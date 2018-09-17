package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import org.xmlpull.v1.XmlPullParser;

public abstract class BaseProcessor implements IProcessor {
    public boolean readCert(String line, CertificationData rawCert) {
        return false;
    }

    public boolean parserCert(HwCertification rawCert) {
        return false;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        return false;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        return false;
    }
}
