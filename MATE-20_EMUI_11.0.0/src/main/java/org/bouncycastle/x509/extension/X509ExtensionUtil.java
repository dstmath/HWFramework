package org.bouncycastle.x509.extension;

import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.util.Integers;

public class X509ExtensionUtil {
    public static ASN1Primitive fromExtensionValue(byte[] bArr) throws IOException {
        return ASN1Primitive.fromByteArray(((ASN1OctetString) ASN1Primitive.fromByteArray(bArr)).getOctets());
    }

    private static Collection getAlternativeNames(byte[] bArr) throws CertificateParsingException {
        Object aSN1Primitive;
        if (bArr == null) {
            return Collections.EMPTY_LIST;
        }
        try {
            ArrayList arrayList = new ArrayList();
            Enumeration objects = DERSequence.getInstance(fromExtensionValue(bArr)).getObjects();
            while (objects.hasMoreElements()) {
                GeneralName instance = GeneralName.getInstance(objects.nextElement());
                ArrayList arrayList2 = new ArrayList();
                arrayList2.add(Integers.valueOf(instance.getTagNo()));
                switch (instance.getTagNo()) {
                    case 0:
                    case 3:
                    case 5:
                        aSN1Primitive = instance.getName().toASN1Primitive();
                        break;
                    case 1:
                    case 2:
                    case 6:
                        aSN1Primitive = ((ASN1String) instance.getName()).getString();
                        break;
                    case 4:
                        aSN1Primitive = X500Name.getInstance(instance.getName()).toString();
                        break;
                    case 7:
                        aSN1Primitive = DEROctetString.getInstance(instance.getName()).getOctets();
                        break;
                    case 8:
                        aSN1Primitive = ASN1ObjectIdentifier.getInstance(instance.getName()).getId();
                        break;
                    default:
                        throw new IOException("Bad tag number: " + instance.getTagNo());
                }
                arrayList2.add(aSN1Primitive);
                arrayList.add(arrayList2);
            }
            return Collections.unmodifiableCollection(arrayList);
        } catch (Exception e) {
            throw new CertificateParsingException(e.getMessage());
        }
    }

    public static Collection getIssuerAlternativeNames(X509Certificate x509Certificate) throws CertificateParsingException {
        return getAlternativeNames(x509Certificate.getExtensionValue(Extension.issuerAlternativeName.getId()));
    }

    public static Collection getSubjectAlternativeNames(X509Certificate x509Certificate) throws CertificateParsingException {
        return getAlternativeNames(x509Certificate.getExtensionValue(Extension.subjectAlternativeName.getId()));
    }
}
