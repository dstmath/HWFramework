package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import com.android.server.pm.auth.HwCertification;
import org.xmlpull.v1.XmlPullParser;

public interface IProcessor {
    public static final String TAG = "HwCertificationManager";

    boolean parseXmlTag(String str, XmlPullParser xmlPullParser, HwCertification hwCertification);

    boolean parserCert(HwCertification hwCertification);

    boolean readCert(String str, HwCertification.CertificationData certificationData);

    boolean verifyCert(PackageParser.Package packageR, HwCertification hwCertification);
}
