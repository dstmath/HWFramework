package org.bouncycastle.asn1.microsoft;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface MicrosoftObjectIdentifiers {
    public static final ASN1ObjectIdentifier microsoft = new ASN1ObjectIdentifier("1.3.6.1.4.1.311");
    public static final ASN1ObjectIdentifier microsoftAppPolicies = microsoft.branch("21.10");
    public static final ASN1ObjectIdentifier microsoftCaVersion = microsoft.branch("21.1");
    public static final ASN1ObjectIdentifier microsoftCertTemplateV1 = microsoft.branch("20.2");
    public static final ASN1ObjectIdentifier microsoftCertTemplateV2 = microsoft.branch("21.7");
    public static final ASN1ObjectIdentifier microsoftCrlNextPublish = microsoft.branch("21.4");
    public static final ASN1ObjectIdentifier microsoftPrevCaCertHash = microsoft.branch("21.2");
}
