package org.bouncycastle.eac;

import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.bouncycastle.asn1.eac.CVCertificate;
import org.bouncycastle.asn1.eac.CertificateBody;
import org.bouncycastle.asn1.eac.CertificateHolderAuthorization;
import org.bouncycastle.asn1.eac.CertificateHolderReference;
import org.bouncycastle.asn1.eac.CertificationAuthorityReference;
import org.bouncycastle.asn1.eac.PackedDate;
import org.bouncycastle.asn1.eac.PublicKeyDataObject;
import org.bouncycastle.eac.operator.EACSigner;

public class EACCertificateBuilder {
    private static final byte[] ZeroArray = {0};
    private PackedDate certificateEffectiveDate;
    private PackedDate certificateExpirationDate;
    private CertificateHolderAuthorization certificateHolderAuthorization;
    private CertificateHolderReference certificateHolderReference;
    private CertificationAuthorityReference certificationAuthorityReference;
    private PublicKeyDataObject publicKey;

    public EACCertificateBuilder(CertificationAuthorityReference certificationAuthorityReference2, PublicKeyDataObject publicKeyDataObject, CertificateHolderReference certificateHolderReference2, CertificateHolderAuthorization certificateHolderAuthorization2, PackedDate packedDate, PackedDate packedDate2) {
        this.certificationAuthorityReference = certificationAuthorityReference2;
        this.publicKey = publicKeyDataObject;
        this.certificateHolderReference = certificateHolderReference2;
        this.certificateHolderAuthorization = certificateHolderAuthorization2;
        this.certificateEffectiveDate = packedDate;
        this.certificateExpirationDate = packedDate2;
    }

    private CertificateBody buildBody() {
        return new CertificateBody(new DERApplicationSpecific(41, ZeroArray), this.certificationAuthorityReference, this.publicKey, this.certificateHolderReference, this.certificateHolderAuthorization, this.certificateEffectiveDate, this.certificateExpirationDate);
    }

    public EACCertificateHolder build(EACSigner eACSigner) throws EACException {
        try {
            CertificateBody buildBody = buildBody();
            OutputStream outputStream = eACSigner.getOutputStream();
            outputStream.write(buildBody.getEncoded(ASN1Encoding.DER));
            outputStream.close();
            return new EACCertificateHolder(new CVCertificate(buildBody, eACSigner.getSignature()));
        } catch (Exception e) {
            throw new EACException("unable to process signature: " + e.getMessage(), e);
        }
    }
}
