package org.bouncycastle.asn1.eac;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1ApplicationSpecific;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.bouncycastle.asn1.DEROctetString;

public class CertificateBody extends ASN1Object {
    private static final int CAR = 2;
    private static final int CEfD = 32;
    private static final int CExD = 64;
    private static final int CHA = 16;
    private static final int CHR = 8;
    private static final int CPI = 1;
    private static final int PK = 4;
    public static final int profileType = 127;
    public static final int requestType = 13;
    private ASN1ApplicationSpecific certificateEffectiveDate;
    private ASN1ApplicationSpecific certificateExpirationDate;
    private CertificateHolderAuthorization certificateHolderAuthorization;
    private ASN1ApplicationSpecific certificateHolderReference;
    private ASN1ApplicationSpecific certificateProfileIdentifier;
    private int certificateType = 0;
    private ASN1ApplicationSpecific certificationAuthorityReference;
    private PublicKeyDataObject publicKey;
    ASN1InputStream seq;

    private CertificateBody(ASN1ApplicationSpecific aSN1ApplicationSpecific) throws IOException {
        setIso7816CertificateBody(aSN1ApplicationSpecific);
    }

    public CertificateBody(ASN1ApplicationSpecific aSN1ApplicationSpecific, CertificationAuthorityReference certificationAuthorityReference2, PublicKeyDataObject publicKeyDataObject, CertificateHolderReference certificateHolderReference2, CertificateHolderAuthorization certificateHolderAuthorization2, PackedDate packedDate, PackedDate packedDate2) {
        setCertificateProfileIdentifier(aSN1ApplicationSpecific);
        setCertificationAuthorityReference(new DERApplicationSpecific(2, certificationAuthorityReference2.getEncoded()));
        setPublicKey(publicKeyDataObject);
        setCertificateHolderReference(new DERApplicationSpecific(32, certificateHolderReference2.getEncoded()));
        setCertificateHolderAuthorization(certificateHolderAuthorization2);
        try {
            setCertificateEffectiveDate(new DERApplicationSpecific(false, 37, (ASN1Encodable) new DEROctetString(packedDate.getEncoding())));
            setCertificateExpirationDate(new DERApplicationSpecific(false, 36, (ASN1Encodable) new DEROctetString(packedDate2.getEncoding())));
        } catch (IOException e) {
            throw new IllegalArgumentException("unable to encode dates: " + e.getMessage());
        }
    }

    public static CertificateBody getInstance(Object obj) throws IOException {
        if (obj instanceof CertificateBody) {
            return (CertificateBody) obj;
        }
        if (obj != null) {
            return new CertificateBody(ASN1ApplicationSpecific.getInstance(obj));
        }
        return null;
    }

    private ASN1Primitive profileToASN1Object() throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(7);
        aSN1EncodableVector.add(this.certificateProfileIdentifier);
        aSN1EncodableVector.add(this.certificationAuthorityReference);
        aSN1EncodableVector.add(new DERApplicationSpecific(false, 73, (ASN1Encodable) this.publicKey));
        aSN1EncodableVector.add(this.certificateHolderReference);
        aSN1EncodableVector.add(this.certificateHolderAuthorization);
        aSN1EncodableVector.add(this.certificateEffectiveDate);
        aSN1EncodableVector.add(this.certificateExpirationDate);
        return new DERApplicationSpecific(78, aSN1EncodableVector);
    }

    private ASN1Primitive requestToASN1Object() throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(3);
        aSN1EncodableVector.add(this.certificateProfileIdentifier);
        aSN1EncodableVector.add(new DERApplicationSpecific(false, 73, (ASN1Encodable) this.publicKey));
        aSN1EncodableVector.add(this.certificateHolderReference);
        return new DERApplicationSpecific(78, aSN1EncodableVector);
    }

    private void setCertificateEffectiveDate(ASN1ApplicationSpecific aSN1ApplicationSpecific) throws IllegalArgumentException {
        if (aSN1ApplicationSpecific.getApplicationTag() == 37) {
            this.certificateEffectiveDate = aSN1ApplicationSpecific;
            this.certificateType |= 32;
            return;
        }
        throw new IllegalArgumentException("Not an Iso7816Tags.APPLICATION_EFFECTIVE_DATE tag :" + EACTags.encodeTag(aSN1ApplicationSpecific));
    }

    private void setCertificateExpirationDate(ASN1ApplicationSpecific aSN1ApplicationSpecific) throws IllegalArgumentException {
        if (aSN1ApplicationSpecific.getApplicationTag() == 36) {
            this.certificateExpirationDate = aSN1ApplicationSpecific;
            this.certificateType |= 64;
            return;
        }
        throw new IllegalArgumentException("Not an Iso7816Tags.APPLICATION_EXPIRATION_DATE tag");
    }

    private void setCertificateHolderAuthorization(CertificateHolderAuthorization certificateHolderAuthorization2) {
        this.certificateHolderAuthorization = certificateHolderAuthorization2;
        this.certificateType |= 16;
    }

    private void setCertificateHolderReference(ASN1ApplicationSpecific aSN1ApplicationSpecific) throws IllegalArgumentException {
        if (aSN1ApplicationSpecific.getApplicationTag() == 32) {
            this.certificateHolderReference = aSN1ApplicationSpecific;
            this.certificateType |= 8;
            return;
        }
        throw new IllegalArgumentException("Not an Iso7816Tags.CARDHOLDER_NAME tag");
    }

    private void setCertificateProfileIdentifier(ASN1ApplicationSpecific aSN1ApplicationSpecific) throws IllegalArgumentException {
        if (aSN1ApplicationSpecific.getApplicationTag() == 41) {
            this.certificateProfileIdentifier = aSN1ApplicationSpecific;
            this.certificateType |= 1;
            return;
        }
        throw new IllegalArgumentException("Not an Iso7816Tags.INTERCHANGE_PROFILE tag :" + EACTags.encodeTag(aSN1ApplicationSpecific));
    }

    private void setCertificationAuthorityReference(ASN1ApplicationSpecific aSN1ApplicationSpecific) throws IllegalArgumentException {
        if (aSN1ApplicationSpecific.getApplicationTag() == 2) {
            this.certificationAuthorityReference = aSN1ApplicationSpecific;
            this.certificateType |= 2;
            return;
        }
        throw new IllegalArgumentException("Not an Iso7816Tags.ISSUER_IDENTIFICATION_NUMBER tag");
    }

    private void setIso7816CertificateBody(ASN1ApplicationSpecific aSN1ApplicationSpecific) throws IOException {
        if (aSN1ApplicationSpecific.getApplicationTag() == 78) {
            ASN1InputStream aSN1InputStream = new ASN1InputStream(aSN1ApplicationSpecific.getContents());
            while (true) {
                ASN1Primitive readObject = aSN1InputStream.readObject();
                if (readObject == null) {
                    aSN1InputStream.close();
                    return;
                } else if (readObject instanceof ASN1ApplicationSpecific) {
                    ASN1ApplicationSpecific aSN1ApplicationSpecific2 = (ASN1ApplicationSpecific) readObject;
                    int applicationTag = aSN1ApplicationSpecific2.getApplicationTag();
                    if (applicationTag == 2) {
                        setCertificationAuthorityReference(aSN1ApplicationSpecific2);
                    } else if (applicationTag == 32) {
                        setCertificateHolderReference(aSN1ApplicationSpecific2);
                    } else if (applicationTag == 41) {
                        setCertificateProfileIdentifier(aSN1ApplicationSpecific2);
                    } else if (applicationTag == 73) {
                        setPublicKey(PublicKeyDataObject.getInstance(aSN1ApplicationSpecific2.getObject(16)));
                    } else if (applicationTag == 76) {
                        setCertificateHolderAuthorization(new CertificateHolderAuthorization(aSN1ApplicationSpecific2));
                    } else if (applicationTag == 36) {
                        setCertificateExpirationDate(aSN1ApplicationSpecific2);
                    } else if (applicationTag == 37) {
                        setCertificateEffectiveDate(aSN1ApplicationSpecific2);
                    } else {
                        this.certificateType = 0;
                        throw new IOException("Not a valid iso7816 ASN1ApplicationSpecific tag " + aSN1ApplicationSpecific2.getApplicationTag());
                    }
                } else {
                    throw new IOException("Not a valid iso7816 content : not a ASN1ApplicationSpecific Object :" + EACTags.encodeTag(aSN1ApplicationSpecific) + readObject.getClass());
                }
            }
        } else {
            throw new IOException("Bad tag : not an iso7816 CERTIFICATE_CONTENT_TEMPLATE");
        }
    }

    private void setPublicKey(PublicKeyDataObject publicKeyDataObject) {
        this.publicKey = PublicKeyDataObject.getInstance(publicKeyDataObject);
        this.certificateType |= 4;
    }

    public PackedDate getCertificateEffectiveDate() {
        if ((this.certificateType & 32) == 32) {
            return new PackedDate(this.certificateEffectiveDate.getContents());
        }
        return null;
    }

    public PackedDate getCertificateExpirationDate() throws IOException {
        if ((this.certificateType & 64) == 64) {
            return new PackedDate(this.certificateExpirationDate.getContents());
        }
        throw new IOException("certificate Expiration Date not set");
    }

    public CertificateHolderAuthorization getCertificateHolderAuthorization() throws IOException {
        if ((this.certificateType & 16) == 16) {
            return this.certificateHolderAuthorization;
        }
        throw new IOException("Certificate Holder Authorisation not set");
    }

    public CertificateHolderReference getCertificateHolderReference() {
        return new CertificateHolderReference(this.certificateHolderReference.getContents());
    }

    public ASN1ApplicationSpecific getCertificateProfileIdentifier() {
        return this.certificateProfileIdentifier;
    }

    public int getCertificateType() {
        return this.certificateType;
    }

    public CertificationAuthorityReference getCertificationAuthorityReference() throws IOException {
        if ((this.certificateType & 2) == 2) {
            return new CertificationAuthorityReference(this.certificationAuthorityReference.getContents());
        }
        throw new IOException("Certification authority reference not set");
    }

    public PublicKeyDataObject getPublicKey() {
        return this.publicKey;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        try {
            if (this.certificateType == 127) {
                return profileToASN1Object();
            }
            if (this.certificateType == 13) {
                return requestToASN1Object();
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
