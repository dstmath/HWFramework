package com.android.org.bouncycastle.asn1.cms;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1Set;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.BERSequence;
import com.android.org.bouncycastle.asn1.BERSet;
import com.android.org.bouncycastle.asn1.BERTaggedObject;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import java.util.Enumeration;

public class SignedData extends ASN1Object {
    private static final ASN1Integer VERSION_1 = new ASN1Integer(1);
    private static final ASN1Integer VERSION_3 = new ASN1Integer(3);
    private static final ASN1Integer VERSION_4 = new ASN1Integer(4);
    private static final ASN1Integer VERSION_5 = new ASN1Integer(5);
    private ASN1Set certificates;
    private boolean certsBer;
    private ContentInfo contentInfo;
    private ASN1Set crls;
    private boolean crlsBer;
    private ASN1Set digestAlgorithms;
    private ASN1Set signerInfos;
    private ASN1Integer version;

    public static SignedData getInstance(Object o) {
        if (o instanceof SignedData) {
            return (SignedData) o;
        }
        if (o != null) {
            return new SignedData(ASN1Sequence.getInstance(o));
        }
        return null;
    }

    public SignedData(ASN1Set digestAlgorithms2, ContentInfo contentInfo2, ASN1Set certificates2, ASN1Set crls2, ASN1Set signerInfos2) {
        this.version = calculateVersion(contentInfo2.getContentType(), certificates2, crls2, signerInfos2);
        this.digestAlgorithms = digestAlgorithms2;
        this.contentInfo = contentInfo2;
        this.certificates = certificates2;
        this.crls = crls2;
        this.signerInfos = signerInfos2;
        this.crlsBer = crls2 instanceof BERSet;
        this.certsBer = certificates2 instanceof BERSet;
    }

    private ASN1Integer calculateVersion(ASN1ObjectIdentifier contentOid, ASN1Set certs, ASN1Set crls2, ASN1Set signerInfs) {
        boolean otherCert = false;
        boolean otherCrl = false;
        boolean attrCertV1Found = false;
        boolean attrCertV2Found = false;
        if (certs != null) {
            Enumeration en = certs.getObjects();
            while (en.hasMoreElements()) {
                Object obj = en.nextElement();
                if (obj instanceof ASN1TaggedObject) {
                    ASN1TaggedObject tagged = ASN1TaggedObject.getInstance(obj);
                    if (tagged.getTagNo() == 1) {
                        attrCertV1Found = true;
                    } else if (tagged.getTagNo() == 2) {
                        attrCertV2Found = true;
                    } else if (tagged.getTagNo() == 3) {
                        otherCert = true;
                    }
                }
            }
        }
        if (otherCert) {
            return new ASN1Integer(5);
        }
        if (crls2 != null) {
            Enumeration en2 = crls2.getObjects();
            while (en2.hasMoreElements()) {
                if (en2.nextElement() instanceof ASN1TaggedObject) {
                    otherCrl = true;
                }
            }
        }
        if (otherCrl) {
            return VERSION_5;
        }
        if (attrCertV2Found) {
            return VERSION_4;
        }
        if (attrCertV1Found) {
            return VERSION_3;
        }
        if (checkForVersion3(signerInfs)) {
            return VERSION_3;
        }
        if (!CMSObjectIdentifiers.data.equals(contentOid)) {
            return VERSION_3;
        }
        return VERSION_1;
    }

    private boolean checkForVersion3(ASN1Set signerInfs) {
        Enumeration e = signerInfs.getObjects();
        while (e.hasMoreElements()) {
            if (SignerInfo.getInstance(e.nextElement()).getVersion().getValue().intValue() == 3) {
                return true;
            }
        }
        return false;
    }

    private SignedData(ASN1Sequence seq) {
        Enumeration e = seq.getObjects();
        this.version = ASN1Integer.getInstance(e.nextElement());
        this.digestAlgorithms = (ASN1Set) e.nextElement();
        this.contentInfo = ContentInfo.getInstance(e.nextElement());
        while (e.hasMoreElements()) {
            ASN1Primitive o = (ASN1Primitive) e.nextElement();
            if (o instanceof ASN1TaggedObject) {
                ASN1TaggedObject tagged = (ASN1TaggedObject) o;
                switch (tagged.getTagNo()) {
                    case 0:
                        this.certsBer = tagged instanceof BERTaggedObject;
                        this.certificates = ASN1Set.getInstance(tagged, false);
                        break;
                    case 1:
                        this.crlsBer = tagged instanceof BERTaggedObject;
                        this.crls = ASN1Set.getInstance(tagged, false);
                        break;
                    default:
                        throw new IllegalArgumentException("unknown tag value " + tagged.getTagNo());
                }
            } else {
                this.signerInfos = (ASN1Set) o;
            }
        }
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public ASN1Set getDigestAlgorithms() {
        return this.digestAlgorithms;
    }

    public ContentInfo getEncapContentInfo() {
        return this.contentInfo;
    }

    public ASN1Set getCertificates() {
        return this.certificates;
    }

    public ASN1Set getCRLs() {
        return this.crls;
    }

    public ASN1Set getSignerInfos() {
        return this.signerInfos;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.version);
        v.add(this.digestAlgorithms);
        v.add(this.contentInfo);
        if (this.certificates != null) {
            if (this.certsBer) {
                v.add(new BERTaggedObject(false, 0, this.certificates));
            } else {
                v.add(new DERTaggedObject(false, 0, this.certificates));
            }
        }
        if (this.crls != null) {
            if (this.crlsBer) {
                v.add(new BERTaggedObject(false, 1, this.crls));
            } else {
                v.add(new DERTaggedObject(false, 1, this.crls));
            }
        }
        v.add(this.signerInfos);
        return new BERSequence(v);
    }
}
