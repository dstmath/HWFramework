package org.bouncycastle.asn1.eac;

import java.io.IOException;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1ApplicationSpecific;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ParsingException;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.util.Arrays;

public class CVCertificateRequest extends ASN1Object {
    private static final int bodyValid = 1;
    private static final int signValid = 2;
    private CertificateBody certificateBody;
    private byte[] innerSignature = null;
    private final ASN1ApplicationSpecific original;
    private byte[] outerSignature = null;

    private CVCertificateRequest(ASN1ApplicationSpecific aSN1ApplicationSpecific) throws IOException {
        this.original = aSN1ApplicationSpecific;
        if (!aSN1ApplicationSpecific.isConstructed() || aSN1ApplicationSpecific.getApplicationTag() != 7) {
            initCertBody(aSN1ApplicationSpecific);
            return;
        }
        ASN1Sequence instance = ASN1Sequence.getInstance(aSN1ApplicationSpecific.getObject(16));
        initCertBody(ASN1ApplicationSpecific.getInstance(instance.getObjectAt(0)));
        this.outerSignature = ASN1ApplicationSpecific.getInstance(instance.getObjectAt(instance.size() - 1)).getContents();
    }

    public static CVCertificateRequest getInstance(Object obj) {
        if (obj instanceof CVCertificateRequest) {
            return (CVCertificateRequest) obj;
        }
        if (obj == null) {
            return null;
        }
        try {
            return new CVCertificateRequest(ASN1ApplicationSpecific.getInstance(obj));
        } catch (IOException e) {
            throw new ASN1ParsingException("unable to parse data: " + e.getMessage(), e);
        }
    }

    private void initCertBody(ASN1ApplicationSpecific aSN1ApplicationSpecific) throws IOException {
        if (aSN1ApplicationSpecific.getApplicationTag() == 33) {
            boolean z = false;
            Enumeration objects = ASN1Sequence.getInstance(aSN1ApplicationSpecific.getObject(16)).getObjects();
            while (objects.hasMoreElements()) {
                ASN1ApplicationSpecific instance = ASN1ApplicationSpecific.getInstance(objects.nextElement());
                int applicationTag = instance.getApplicationTag();
                if (applicationTag == 55) {
                    this.innerSignature = instance.getContents();
                    z |= true;
                } else if (applicationTag == 78) {
                    this.certificateBody = CertificateBody.getInstance(instance);
                    z |= true;
                } else {
                    throw new IOException("Invalid tag, not an CV Certificate Request element:" + instance.getApplicationTag());
                }
            }
            if (!z || !true) {
                throw new IOException("Invalid CARDHOLDER_CERTIFICATE in request:" + aSN1ApplicationSpecific.getApplicationTag());
            }
            return;
        }
        throw new IOException("not a CARDHOLDER_CERTIFICATE in request:" + aSN1ApplicationSpecific.getApplicationTag());
    }

    public CertificateBody getCertificateBody() {
        return this.certificateBody;
    }

    public byte[] getInnerSignature() {
        return Arrays.clone(this.innerSignature);
    }

    public byte[] getOuterSignature() {
        return Arrays.clone(this.outerSignature);
    }

    public PublicKeyDataObject getPublicKey() {
        return this.certificateBody.getPublicKey();
    }

    public boolean hasOuterSignature() {
        return this.outerSignature != null;
    }

    public ASN1Primitive toASN1Primitive() {
        if (this.original != null) {
            return this.original;
        }
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.certificateBody);
        try {
            aSN1EncodableVector.add(new DERApplicationSpecific(false, 55, (ASN1Encodable) new DEROctetString(this.innerSignature)));
            return new DERApplicationSpecific(33, aSN1EncodableVector);
        } catch (IOException e) {
            throw new IllegalStateException("unable to convert signature!");
        }
    }
}
