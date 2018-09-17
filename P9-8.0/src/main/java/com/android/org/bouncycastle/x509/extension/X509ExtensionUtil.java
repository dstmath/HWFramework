package com.android.org.bouncycastle.x509.extension;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1String;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x509.GeneralName;
import com.android.org.bouncycastle.asn1.x509.X509Extension;
import com.android.org.bouncycastle.util.Integers;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class X509ExtensionUtil {
    public static ASN1Primitive fromExtensionValue(byte[] encodedValue) throws IOException {
        return ASN1Primitive.fromByteArray(((ASN1OctetString) ASN1Primitive.fromByteArray(encodedValue)).getOctets());
    }

    public static Collection getIssuerAlternativeNames(X509Certificate cert) throws CertificateParsingException {
        return getAlternativeNames(cert.getExtensionValue(X509Extension.issuerAlternativeName.getId()));
    }

    public static Collection getSubjectAlternativeNames(X509Certificate cert) throws CertificateParsingException {
        return getAlternativeNames(cert.getExtensionValue(X509Extension.subjectAlternativeName.getId()));
    }

    private static Collection getAlternativeNames(byte[] extVal) throws CertificateParsingException {
        if (extVal == null) {
            return Collections.EMPTY_LIST;
        }
        try {
            Collection temp = new ArrayList();
            Enumeration it = ASN1Sequence.getInstance(fromExtensionValue(extVal)).getObjects();
            while (it.hasMoreElements()) {
                GeneralName genName = GeneralName.getInstance(it.nextElement());
                List list = new ArrayList();
                list.add(Integers.valueOf(genName.getTagNo()));
                switch (genName.getTagNo()) {
                    case 0:
                    case 3:
                    case 5:
                        list.add(genName.getName().toASN1Primitive());
                        break;
                    case 1:
                    case 2:
                    case 6:
                        list.add(((ASN1String) genName.getName()).getString());
                        break;
                    case 4:
                        list.add(X500Name.getInstance(genName.getName()).toString());
                        break;
                    case 7:
                        list.add(ASN1OctetString.getInstance(genName.getName()).getOctets());
                        break;
                    case 8:
                        list.add(ASN1ObjectIdentifier.getInstance(genName.getName()).getId());
                        break;
                    default:
                        throw new IOException("Bad tag number: " + genName.getTagNo());
                }
                temp.add(list);
            }
            return Collections.unmodifiableCollection(temp);
        } catch (Exception e) {
            throw new CertificateParsingException(e.getMessage());
        }
    }
}
