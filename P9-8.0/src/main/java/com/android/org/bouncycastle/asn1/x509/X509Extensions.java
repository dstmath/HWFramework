package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Boolean;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class X509Extensions extends ASN1Object {
    public static final ASN1ObjectIdentifier AuditIdentity = new ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.4");
    public static final ASN1ObjectIdentifier AuthorityInfoAccess = new ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.1");
    public static final ASN1ObjectIdentifier AuthorityKeyIdentifier = new ASN1ObjectIdentifier("2.5.29.35");
    public static final ASN1ObjectIdentifier BasicConstraints = new ASN1ObjectIdentifier("2.5.29.19");
    public static final ASN1ObjectIdentifier BiometricInfo = new ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.2");
    public static final ASN1ObjectIdentifier CRLDistributionPoints = new ASN1ObjectIdentifier("2.5.29.31");
    public static final ASN1ObjectIdentifier CRLNumber = new ASN1ObjectIdentifier("2.5.29.20");
    public static final ASN1ObjectIdentifier CertificateIssuer = new ASN1ObjectIdentifier("2.5.29.29");
    public static final ASN1ObjectIdentifier CertificatePolicies = new ASN1ObjectIdentifier("2.5.29.32");
    public static final ASN1ObjectIdentifier DeltaCRLIndicator = new ASN1ObjectIdentifier("2.5.29.27");
    public static final ASN1ObjectIdentifier ExtendedKeyUsage = new ASN1ObjectIdentifier("2.5.29.37");
    public static final ASN1ObjectIdentifier FreshestCRL = new ASN1ObjectIdentifier("2.5.29.46");
    public static final ASN1ObjectIdentifier InhibitAnyPolicy = new ASN1ObjectIdentifier("2.5.29.54");
    public static final ASN1ObjectIdentifier InstructionCode = new ASN1ObjectIdentifier("2.5.29.23");
    public static final ASN1ObjectIdentifier InvalidityDate = new ASN1ObjectIdentifier("2.5.29.24");
    public static final ASN1ObjectIdentifier IssuerAlternativeName = new ASN1ObjectIdentifier("2.5.29.18");
    public static final ASN1ObjectIdentifier IssuingDistributionPoint = new ASN1ObjectIdentifier("2.5.29.28");
    public static final ASN1ObjectIdentifier KeyUsage = new ASN1ObjectIdentifier("2.5.29.15");
    public static final ASN1ObjectIdentifier LogoType = new ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.12");
    public static final ASN1ObjectIdentifier NameConstraints = new ASN1ObjectIdentifier("2.5.29.30");
    public static final ASN1ObjectIdentifier NoRevAvail = new ASN1ObjectIdentifier("2.5.29.56");
    public static final ASN1ObjectIdentifier PolicyConstraints = new ASN1ObjectIdentifier("2.5.29.36");
    public static final ASN1ObjectIdentifier PolicyMappings = new ASN1ObjectIdentifier("2.5.29.33");
    public static final ASN1ObjectIdentifier PrivateKeyUsagePeriod = new ASN1ObjectIdentifier("2.5.29.16");
    public static final ASN1ObjectIdentifier QCStatements = new ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.3");
    public static final ASN1ObjectIdentifier ReasonCode = new ASN1ObjectIdentifier("2.5.29.21");
    public static final ASN1ObjectIdentifier SubjectAlternativeName = new ASN1ObjectIdentifier("2.5.29.17");
    public static final ASN1ObjectIdentifier SubjectDirectoryAttributes = new ASN1ObjectIdentifier("2.5.29.9");
    public static final ASN1ObjectIdentifier SubjectInfoAccess = new ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.11");
    public static final ASN1ObjectIdentifier SubjectKeyIdentifier = new ASN1ObjectIdentifier("2.5.29.14");
    public static final ASN1ObjectIdentifier TargetInformation = new ASN1ObjectIdentifier("2.5.29.55");
    private Hashtable extensions;
    private Vector ordering;

    public static X509Extensions getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static X509Extensions getInstance(Object obj) {
        if (obj == null || (obj instanceof X509Extensions)) {
            return (X509Extensions) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new X509Extensions((ASN1Sequence) obj);
        }
        if (obj instanceof Extensions) {
            return new X509Extensions((ASN1Sequence) ((Extensions) obj).toASN1Primitive());
        }
        if (obj instanceof ASN1TaggedObject) {
            return getInstance(((ASN1TaggedObject) obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public X509Extensions(ASN1Sequence seq) {
        this.extensions = new Hashtable();
        this.ordering = new Vector();
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements()) {
            ASN1Sequence s = ASN1Sequence.getInstance(e.nextElement());
            if (s.size() == 3) {
                this.extensions.put(s.getObjectAt(0), new X509Extension(ASN1Boolean.getInstance(s.getObjectAt(1)), ASN1OctetString.getInstance(s.getObjectAt(2))));
            } else if (s.size() == 2) {
                this.extensions.put(s.getObjectAt(0), new X509Extension(false, ASN1OctetString.getInstance(s.getObjectAt(1))));
            } else {
                throw new IllegalArgumentException("Bad sequence size: " + s.size());
            }
            this.ordering.addElement(s.getObjectAt(0));
        }
    }

    public X509Extensions(Hashtable extensions) {
        this(null, extensions);
    }

    public X509Extensions(Vector ordering, Hashtable extensions) {
        Enumeration e;
        this.extensions = new Hashtable();
        this.ordering = new Vector();
        if (ordering == null) {
            e = extensions.keys();
        } else {
            e = ordering.elements();
        }
        while (e.hasMoreElements()) {
            this.ordering.addElement(ASN1ObjectIdentifier.getInstance(e.nextElement()));
        }
        e = this.ordering.elements();
        while (e.hasMoreElements()) {
            ASN1ObjectIdentifier oid = ASN1ObjectIdentifier.getInstance(e.nextElement());
            this.extensions.put(oid, (X509Extension) extensions.get(oid));
        }
    }

    public X509Extensions(Vector objectIDs, Vector values) {
        this.extensions = new Hashtable();
        this.ordering = new Vector();
        Enumeration e = objectIDs.elements();
        while (e.hasMoreElements()) {
            this.ordering.addElement(e.nextElement());
        }
        int count = 0;
        e = this.ordering.elements();
        while (e.hasMoreElements()) {
            this.extensions.put((ASN1ObjectIdentifier) e.nextElement(), (X509Extension) values.elementAt(count));
            count++;
        }
    }

    public Enumeration oids() {
        return this.ordering.elements();
    }

    public X509Extension getExtension(ASN1ObjectIdentifier oid) {
        return (X509Extension) this.extensions.get(oid);
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        Enumeration e = this.ordering.elements();
        while (e.hasMoreElements()) {
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e.nextElement();
            X509Extension ext = (X509Extension) this.extensions.get(oid);
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(oid);
            if (ext.isCritical()) {
                v.add(ASN1Boolean.TRUE);
            }
            v.add(ext.getValue());
            vec.add(new DERSequence(v));
        }
        return new DERSequence(vec);
    }

    public boolean equivalent(X509Extensions other) {
        if (this.extensions.size() != other.extensions.size()) {
            return false;
        }
        Enumeration e1 = this.extensions.keys();
        while (e1.hasMoreElements()) {
            Object key = e1.nextElement();
            if (!this.extensions.get(key).equals(other.extensions.get(key))) {
                return false;
            }
        }
        return true;
    }

    public ASN1ObjectIdentifier[] getExtensionOIDs() {
        return toOidArray(this.ordering);
    }

    public ASN1ObjectIdentifier[] getNonCriticalExtensionOIDs() {
        return getExtensionOIDs(false);
    }

    public ASN1ObjectIdentifier[] getCriticalExtensionOIDs() {
        return getExtensionOIDs(true);
    }

    private ASN1ObjectIdentifier[] getExtensionOIDs(boolean isCritical) {
        Vector oidVec = new Vector();
        for (int i = 0; i != this.ordering.size(); i++) {
            Object oid = this.ordering.elementAt(i);
            if (((X509Extension) this.extensions.get(oid)).isCritical() == isCritical) {
                oidVec.addElement(oid);
            }
        }
        return toOidArray(oidVec);
    }

    private ASN1ObjectIdentifier[] toOidArray(Vector oidVec) {
        ASN1ObjectIdentifier[] oids = new ASN1ObjectIdentifier[oidVec.size()];
        for (int i = 0; i != oids.length; i++) {
            oids[i] = (ASN1ObjectIdentifier) oidVec.elementAt(i);
        }
        return oids;
    }
}
