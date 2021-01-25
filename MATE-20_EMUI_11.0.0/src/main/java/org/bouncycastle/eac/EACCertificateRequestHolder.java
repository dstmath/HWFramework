package org.bouncycastle.eac;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ParsingException;
import org.bouncycastle.asn1.eac.CVCertificateRequest;
import org.bouncycastle.asn1.eac.PublicKeyDataObject;
import org.bouncycastle.eac.operator.EACSignatureVerifier;

public class EACCertificateRequestHolder {
    private CVCertificateRequest request;

    public EACCertificateRequestHolder(CVCertificateRequest cVCertificateRequest) {
        this.request = cVCertificateRequest;
    }

    public EACCertificateRequestHolder(byte[] bArr) throws IOException {
        this(parseBytes(bArr));
    }

    private static CVCertificateRequest parseBytes(byte[] bArr) throws IOException {
        try {
            return CVCertificateRequest.getInstance(bArr);
        } catch (ClassCastException e) {
            throw new EACIOException("malformed data: " + e.getMessage(), e);
        } catch (IllegalArgumentException e2) {
            throw new EACIOException("malformed data: " + e2.getMessage(), e2);
        } catch (ASN1ParsingException e3) {
            if (e3.getCause() instanceof IOException) {
                throw ((IOException) e3.getCause());
            }
            throw new EACIOException("malformed data: " + e3.getMessage(), e3);
        }
    }

    public PublicKeyDataObject getPublicKeyDataObject() {
        return this.request.getPublicKey();
    }

    public boolean isInnerSignatureValid(EACSignatureVerifier eACSignatureVerifier) throws EACException {
        try {
            OutputStream outputStream = eACSignatureVerifier.getOutputStream();
            outputStream.write(this.request.getCertificateBody().getEncoded(ASN1Encoding.DER));
            outputStream.close();
            return eACSignatureVerifier.verify(this.request.getInnerSignature());
        } catch (Exception e) {
            throw new EACException("unable to process signature: " + e.getMessage(), e);
        }
    }

    public CVCertificateRequest toASN1Structure() {
        return this.request;
    }
}
