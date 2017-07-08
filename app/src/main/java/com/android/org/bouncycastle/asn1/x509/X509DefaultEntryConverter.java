package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.DERGeneralizedTime;
import com.android.org.bouncycastle.asn1.DERIA5String;
import com.android.org.bouncycastle.asn1.DERPrintableString;
import com.android.org.bouncycastle.asn1.DERUTF8String;
import java.io.IOException;

public class X509DefaultEntryConverter extends X509NameEntryConverter {
    public ASN1Primitive getConvertedValue(ASN1ObjectIdentifier oid, String value) {
        if (value.length() == 0 || value.charAt(0) != '#') {
            if (value.length() != 0 && value.charAt(0) == '\\') {
                value = value.substring(1);
            }
            if (oid.equals(X509Name.EmailAddress) || oid.equals(X509Name.DC)) {
                return new DERIA5String(value);
            }
            if (oid.equals(X509Name.DATE_OF_BIRTH)) {
                return new DERGeneralizedTime(value);
            }
            if (oid.equals(X509Name.C) || oid.equals(X509Name.SN) || oid.equals(X509Name.DN_QUALIFIER) || oid.equals(X509Name.TELEPHONE_NUMBER)) {
                return new DERPrintableString(value);
            }
            return new DERUTF8String(value);
        }
        try {
            return convertHexEncoded(value, 1);
        } catch (IOException e) {
            throw new RuntimeException("can't recode value for oid " + oid.getId());
        }
    }
}
