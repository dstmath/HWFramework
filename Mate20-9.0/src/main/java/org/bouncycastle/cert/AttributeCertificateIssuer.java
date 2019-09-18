package org.bouncycastle.cert;

import java.util.ArrayList;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AttCertIssuer;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.V2Form;
import org.bouncycastle.util.Selector;

public class AttributeCertificateIssuer implements Selector {
    final ASN1Encodable form;

    public AttributeCertificateIssuer(X500Name x500Name) {
        this.form = new V2Form(new GeneralNames(new GeneralName(x500Name)));
    }

    public AttributeCertificateIssuer(AttCertIssuer attCertIssuer) {
        this.form = attCertIssuer.getIssuer();
    }

    private boolean matchesDN(X500Name x500Name, GeneralNames generalNames) {
        GeneralName[] names = generalNames.getNames();
        for (int i = 0; i != names.length; i++) {
            GeneralName generalName = names[i];
            if (generalName.getTagNo() == 4 && X500Name.getInstance(generalName.getName()).equals(x500Name)) {
                return true;
            }
        }
        return false;
    }

    public Object clone() {
        return new AttributeCertificateIssuer(AttCertIssuer.getInstance(this.form));
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AttributeCertificateIssuer)) {
            return false;
        }
        return this.form.equals(((AttributeCertificateIssuer) obj).form);
    }

    public X500Name[] getNames() {
        GeneralName[] names = (this.form instanceof V2Form ? ((V2Form) this.form).getIssuerName() : (GeneralNames) this.form).getNames();
        ArrayList arrayList = new ArrayList(names.length);
        for (int i = 0; i != names.length; i++) {
            if (names[i].getTagNo() == 4) {
                arrayList.add(X500Name.getInstance(names[i].getName()));
            }
        }
        return (X500Name[]) arrayList.toArray(new X500Name[arrayList.size()]);
    }

    public int hashCode() {
        return this.form.hashCode();
    }

    public boolean match(Object obj) {
        boolean z = false;
        if (!(obj instanceof X509CertificateHolder)) {
            return false;
        }
        X509CertificateHolder x509CertificateHolder = (X509CertificateHolder) obj;
        if (this.form instanceof V2Form) {
            V2Form v2Form = (V2Form) this.form;
            if (v2Form.getBaseCertificateID() != null) {
                if (v2Form.getBaseCertificateID().getSerial().getValue().equals(x509CertificateHolder.getSerialNumber()) && matchesDN(x509CertificateHolder.getIssuer(), v2Form.getBaseCertificateID().getIssuer())) {
                    z = true;
                }
                return z;
            }
            if (matchesDN(x509CertificateHolder.getSubject(), v2Form.getIssuerName())) {
                return true;
            }
        } else {
            if (matchesDN(x509CertificateHolder.getSubject(), (GeneralNames) this.form)) {
                return true;
            }
        }
        return false;
    }
}
