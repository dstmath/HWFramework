package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1Set;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.BERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import java.util.Enumeration;

public class SignedData extends ASN1Object implements PKCSObjectIdentifiers {
    private ASN1Set certificates;
    private ContentInfo contentInfo;
    private ASN1Set crls;
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

    public SignedData(ASN1Integer _version, ASN1Set _digestAlgorithms, ContentInfo _contentInfo, ASN1Set _certificates, ASN1Set _crls, ASN1Set _signerInfos) {
        this.version = _version;
        this.digestAlgorithms = _digestAlgorithms;
        this.contentInfo = _contentInfo;
        this.certificates = _certificates;
        this.crls = _crls;
        this.signerInfos = _signerInfos;
    }

    public SignedData(ASN1Sequence seq) {
        Enumeration e = seq.getObjects();
        this.version = (ASN1Integer) e.nextElement();
        this.digestAlgorithms = (ASN1Set) e.nextElement();
        this.contentInfo = ContentInfo.getInstance(e.nextElement());
        while (e.hasMoreElements()) {
            ASN1Primitive o = (ASN1Primitive) e.nextElement();
            if (o instanceof ASN1TaggedObject) {
                ASN1TaggedObject tagged = (ASN1TaggedObject) o;
                switch (tagged.getTagNo()) {
                    case 0:
                        this.certificates = ASN1Set.getInstance(tagged, false);
                        break;
                    case 1:
                        this.crls = ASN1Set.getInstance(tagged, false);
                        break;
                    default:
                        throw new IllegalArgumentException("unknown tag value " + tagged.getTagNo());
                }
            }
            this.signerInfos = (ASN1Set) o;
        }
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public ASN1Set getDigestAlgorithms() {
        return this.digestAlgorithms;
    }

    public ContentInfo getContentInfo() {
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
            v.add(new DERTaggedObject(false, 0, this.certificates));
        }
        if (this.crls != null) {
            v.add(new DERTaggedObject(false, 1, this.crls));
        }
        v.add(this.signerInfos);
        return new BERSequence(v);
    }
}
