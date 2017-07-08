package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Date;
import sun.security.util.DerOutputStream;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.SerialNumber;
import sun.security.x509.SubjectKeyIdentifierExtension;

class AdaptableX509CertSelector extends X509CertSelector {
    private Date endDate;
    private boolean isSKIDSensitive;
    private boolean isSNSensitive;
    private Date startDate;

    AdaptableX509CertSelector() {
        this.isSKIDSensitive = false;
        this.isSNSensitive = false;
    }

    void setValidityPeriod(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    void parseAuthorityKeyIdentifierExtension(AuthorityKeyIdentifierExtension akidext) throws IOException {
        if (akidext != null) {
            KeyIdentifier akid = (KeyIdentifier) akidext.get(SubjectKeyIdentifierExtension.KEY_ID);
            if (akid != null && (this.isSKIDSensitive || getSubjectKeyIdentifier() == null)) {
                DerOutputStream derout = new DerOutputStream();
                derout.putOctetString(akid.getIdentifier());
                super.setSubjectKeyIdentifier(derout.toByteArray());
                this.isSKIDSensitive = true;
            }
            SerialNumber asn = (SerialNumber) akidext.get(AuthorityKeyIdentifierExtension.SERIAL_NUMBER);
            if (asn == null) {
                return;
            }
            if (this.isSNSensitive || getSerialNumber() == null) {
                super.setSerialNumber(asn.getNumber());
                this.isSNSensitive = true;
            }
        }
    }

    public boolean match(Certificate cert) {
        if (!(cert instanceof X509Certificate)) {
            return false;
        }
        X509Certificate xcert = (X509Certificate) cert;
        int version = xcert.getVersion();
        if (version < 3) {
            if (this.startDate != null) {
                try {
                    xcert.checkValidity(this.startDate);
                } catch (CertificateException e) {
                    return false;
                }
            }
            if (this.endDate != null) {
                try {
                    xcert.checkValidity(this.endDate);
                } catch (CertificateException e2) {
                    return false;
                }
            }
        }
        if (this.isSKIDSensitive && (version < 3 || xcert.getExtensionValue("2.5.29.14") == null)) {
            setSubjectKeyIdentifier(null);
        }
        if (this.isSNSensitive && version < 3) {
            setSerialNumber(null);
        }
        return super.match(cert);
    }

    public Object clone() {
        AdaptableX509CertSelector copy = (AdaptableX509CertSelector) super.clone();
        if (this.startDate != null) {
            copy.startDate = (Date) this.startDate.clone();
        }
        if (this.endDate != null) {
            copy.endDate = (Date) this.endDate.clone();
        }
        return copy;
    }
}
