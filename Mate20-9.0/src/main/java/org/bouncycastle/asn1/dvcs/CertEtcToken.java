package org.bouncycastle.asn1.dvcs;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.ess.ESSCertID;
import org.bouncycastle.asn1.ocsp.CertID;
import org.bouncycastle.asn1.ocsp.CertStatus;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.smime.SMIMECapabilities;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.asn1.x509.Extension;

public class CertEtcToken extends ASN1Object implements ASN1Choice {
    public static final int TAG_ASSERTION = 3;
    public static final int TAG_CAPABILITIES = 8;
    public static final int TAG_CERTIFICATE = 0;
    public static final int TAG_CRL = 4;
    public static final int TAG_ESSCERTID = 1;
    public static final int TAG_OCSPCERTID = 6;
    public static final int TAG_OCSPCERTSTATUS = 5;
    public static final int TAG_OCSPRESPONSE = 7;
    public static final int TAG_PKISTATUS = 2;
    private static final boolean[] explicit = {false, true, false, true, false, true, false, false, true};
    private Extension extension;
    private int tagNo;
    private ASN1Encodable value;

    public CertEtcToken(int i, ASN1Encodable aSN1Encodable) {
        this.tagNo = i;
        this.value = aSN1Encodable;
    }

    private CertEtcToken(ASN1TaggedObject aSN1TaggedObject) {
        ASN1Encodable aSN1Encodable;
        this.tagNo = aSN1TaggedObject.getTagNo();
        switch (this.tagNo) {
            case 0:
                aSN1Encodable = Certificate.getInstance(aSN1TaggedObject, false);
                break;
            case 1:
                aSN1Encodable = ESSCertID.getInstance(aSN1TaggedObject.getObject());
                break;
            case 2:
                aSN1Encodable = PKIStatusInfo.getInstance(aSN1TaggedObject, false);
                break;
            case 3:
                aSN1Encodable = ContentInfo.getInstance(aSN1TaggedObject.getObject());
                break;
            case 4:
                aSN1Encodable = CertificateList.getInstance(aSN1TaggedObject, false);
                break;
            case 5:
                aSN1Encodable = CertStatus.getInstance(aSN1TaggedObject.getObject());
                break;
            case 6:
                aSN1Encodable = CertID.getInstance(aSN1TaggedObject, false);
                break;
            case 7:
                aSN1Encodable = OCSPResponse.getInstance(aSN1TaggedObject, false);
                break;
            case 8:
                aSN1Encodable = SMIMECapabilities.getInstance(aSN1TaggedObject.getObject());
                break;
            default:
                throw new IllegalArgumentException("Unknown tag: " + this.tagNo);
        }
        this.value = aSN1Encodable;
    }

    public CertEtcToken(Extension extension2) {
        this.tagNo = -1;
        this.extension = extension2;
    }

    public static CertEtcToken[] arrayFromSequence(ASN1Sequence aSN1Sequence) {
        CertEtcToken[] certEtcTokenArr = new CertEtcToken[aSN1Sequence.size()];
        for (int i = 0; i != certEtcTokenArr.length; i++) {
            certEtcTokenArr[i] = getInstance(aSN1Sequence.getObjectAt(i));
        }
        return certEtcTokenArr;
    }

    public static CertEtcToken getInstance(Object obj) {
        if (obj instanceof CertEtcToken) {
            return (CertEtcToken) obj;
        }
        if (obj instanceof ASN1TaggedObject) {
            return new CertEtcToken((ASN1TaggedObject) obj);
        }
        if (obj != null) {
            return new CertEtcToken(Extension.getInstance(obj));
        }
        return null;
    }

    public Extension getExtension() {
        return this.extension;
    }

    public int getTagNo() {
        return this.tagNo;
    }

    public ASN1Encodable getValue() {
        return this.value;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.extension == null ? new DERTaggedObject(explicit[this.tagNo], this.tagNo, this.value) : this.extension.toASN1Primitive();
    }

    public String toString() {
        return "CertEtcToken {\n" + this.value + "}\n";
    }
}
