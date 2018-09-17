package com.android.org.bouncycastle.x509;

import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.GeneralName;
import com.android.org.bouncycastle.asn1.x509.GeneralNames;
import com.android.org.bouncycastle.asn1.x509.Holder;
import com.android.org.bouncycastle.asn1.x509.IssuerSerial;
import com.android.org.bouncycastle.asn1.x509.ObjectDigestInfo;
import com.android.org.bouncycastle.asn1.x509.X509Name;
import com.android.org.bouncycastle.jce.PrincipalUtil;
import com.android.org.bouncycastle.jce.X509Principal;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Selector;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.x500.X500Principal;

public class AttributeCertificateHolder implements CertSelector, Selector {
    final Holder holder;

    AttributeCertificateHolder(ASN1Sequence seq) {
        this.holder = Holder.getInstance(seq);
    }

    public AttributeCertificateHolder(X509Principal issuerName, BigInteger serialNumber) {
        this.holder = new Holder(new IssuerSerial(GeneralNames.getInstance(new DERSequence(new GeneralName((X509Name) issuerName))), new ASN1Integer(serialNumber)));
    }

    public AttributeCertificateHolder(X500Principal issuerName, BigInteger serialNumber) {
        this(X509Util.convertPrincipal(issuerName), serialNumber);
    }

    public AttributeCertificateHolder(X509Certificate cert) throws CertificateParsingException {
        try {
            this.holder = new Holder(new IssuerSerial(generateGeneralNames(PrincipalUtil.getIssuerX509Principal(cert)), new ASN1Integer(cert.getSerialNumber())));
        } catch (Exception e) {
            throw new CertificateParsingException(e.getMessage());
        }
    }

    public AttributeCertificateHolder(X509Principal principal) {
        this.holder = new Holder(generateGeneralNames(principal));
    }

    public AttributeCertificateHolder(X500Principal principal) {
        this(X509Util.convertPrincipal(principal));
    }

    public AttributeCertificateHolder(int digestedObjectType, String digestAlgorithm, String otherObjectTypeID, byte[] objectDigest) {
        this.holder = new Holder(new ObjectDigestInfo(digestedObjectType, new ASN1ObjectIdentifier(otherObjectTypeID), new AlgorithmIdentifier(new ASN1ObjectIdentifier(digestAlgorithm)), Arrays.clone(objectDigest)));
    }

    public int getDigestedObjectType() {
        if (this.holder.getObjectDigestInfo() != null) {
            return this.holder.getObjectDigestInfo().getDigestedObjectType().getValue().intValue();
        }
        return -1;
    }

    public String getDigestAlgorithm() {
        if (this.holder.getObjectDigestInfo() != null) {
            return this.holder.getObjectDigestInfo().getDigestAlgorithm().getAlgorithm().getId();
        }
        return null;
    }

    public byte[] getObjectDigest() {
        if (this.holder.getObjectDigestInfo() != null) {
            return this.holder.getObjectDigestInfo().getObjectDigest().getBytes();
        }
        return null;
    }

    public String getOtherObjectTypeID() {
        if (this.holder.getObjectDigestInfo() != null) {
            this.holder.getObjectDigestInfo().getOtherObjectTypeID().getId();
        }
        return null;
    }

    private GeneralNames generateGeneralNames(X509Principal principal) {
        return GeneralNames.getInstance(new DERSequence(new GeneralName((X509Name) principal)));
    }

    private boolean matchesDN(X509Principal subject, GeneralNames targets) {
        GeneralName[] names = targets.getNames();
        for (int i = 0; i != names.length; i++) {
            GeneralName gn = names[i];
            if (gn.getTagNo() == 4) {
                try {
                    if (new X509Principal(gn.getName().toASN1Primitive().getEncoded()).equals(subject)) {
                        return true;
                    }
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    private Object[] getNames(GeneralName[] names) {
        List l = new ArrayList(names.length);
        for (int i = 0; i != names.length; i++) {
            if (names[i].getTagNo() == 4) {
                try {
                    l.add(new X500Principal(names[i].getName().toASN1Primitive().getEncoded()));
                } catch (IOException e) {
                    throw new RuntimeException("badly formed Name object");
                }
            }
        }
        return l.toArray(new Object[l.size()]);
    }

    private Principal[] getPrincipals(GeneralNames names) {
        Object[] p = getNames(names.getNames());
        List l = new ArrayList();
        for (int i = 0; i != p.length; i++) {
            if (p[i] instanceof Principal) {
                l.add(p[i]);
            }
        }
        return (Principal[]) l.toArray(new Principal[l.size()]);
    }

    public Principal[] getEntityNames() {
        if (this.holder.getEntityName() != null) {
            return getPrincipals(this.holder.getEntityName());
        }
        return null;
    }

    public Principal[] getIssuer() {
        if (this.holder.getBaseCertificateID() != null) {
            return getPrincipals(this.holder.getBaseCertificateID().getIssuer());
        }
        return null;
    }

    public BigInteger getSerialNumber() {
        if (this.holder.getBaseCertificateID() != null) {
            return this.holder.getBaseCertificateID().getSerial().getValue();
        }
        return null;
    }

    public Object clone() {
        return new AttributeCertificateHolder((ASN1Sequence) this.holder.toASN1Primitive());
    }

    public boolean match(Certificate cert) {
        boolean z = false;
        if (!(cert instanceof X509Certificate)) {
            return false;
        }
        X509Certificate x509Cert = (X509Certificate) cert;
        try {
            if (this.holder.getBaseCertificateID() != null) {
                if (this.holder.getBaseCertificateID().getSerial().getValue().equals(x509Cert.getSerialNumber())) {
                    z = matchesDN(PrincipalUtil.getIssuerX509Principal(x509Cert), this.holder.getBaseCertificateID().getIssuer());
                }
                return z;
            } else if (this.holder.getEntityName() != null && matchesDN(PrincipalUtil.getSubjectX509Principal(x509Cert), this.holder.getEntityName())) {
                return true;
            } else {
                if (this.holder.getObjectDigestInfo() != null) {
                    try {
                        MessageDigest md = MessageDigest.getInstance(getDigestAlgorithm(), BouncyCastleProvider.PROVIDER_NAME);
                        switch (getDigestedObjectType()) {
                            case 0:
                                md.update(cert.getPublicKey().getEncoded());
                                break;
                            case 1:
                                md.update(cert.getEncoded());
                                break;
                        }
                        if (!Arrays.areEqual(md.digest(), getObjectDigest())) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;
            }
        } catch (CertificateEncodingException e2) {
            return false;
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AttributeCertificateHolder)) {
            return false;
        }
        return this.holder.equals(((AttributeCertificateHolder) obj).holder);
    }

    public int hashCode() {
        return this.holder.hashCode();
    }

    public boolean match(Object obj) {
        if (obj instanceof X509Certificate) {
            return match((Certificate) obj);
        }
        return false;
    }
}
