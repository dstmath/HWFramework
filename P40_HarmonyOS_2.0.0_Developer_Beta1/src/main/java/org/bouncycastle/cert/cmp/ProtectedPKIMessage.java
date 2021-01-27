package org.bouncycastle.cert.cmp;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cmp.CMPCertificate;
import org.bouncycastle.asn1.cmp.CMPObjectIdentifiers;
import org.bouncycastle.asn1.cmp.PBMParameter;
import org.bouncycastle.asn1.cmp.PKIBody;
import org.bouncycastle.asn1.cmp.PKIHeader;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.crmf.PKMACBuilder;
import org.bouncycastle.operator.ContentVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.MacCalculator;
import org.bouncycastle.util.Arrays;

public class ProtectedPKIMessage {
    private PKIMessage pkiMessage;

    ProtectedPKIMessage(PKIMessage pKIMessage) {
        if (pKIMessage.getHeader().getProtectionAlg() != null) {
            this.pkiMessage = pKIMessage;
            return;
        }
        throw new IllegalArgumentException("PKIMessage not protected");
    }

    public ProtectedPKIMessage(GeneralPKIMessage generalPKIMessage) {
        if (generalPKIMessage.hasProtection()) {
            this.pkiMessage = generalPKIMessage.toASN1Structure();
            return;
        }
        throw new IllegalArgumentException("PKIMessage not protected");
    }

    private boolean verifySignature(byte[] bArr, ContentVerifier contentVerifier) throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.pkiMessage.getHeader());
        aSN1EncodableVector.add(this.pkiMessage.getBody());
        OutputStream outputStream = contentVerifier.getOutputStream();
        outputStream.write(new DERSequence(aSN1EncodableVector).getEncoded(ASN1Encoding.DER));
        outputStream.close();
        return contentVerifier.verify(bArr);
    }

    public PKIBody getBody() {
        return this.pkiMessage.getBody();
    }

    public X509CertificateHolder[] getCertificates() {
        CMPCertificate[] extraCerts = this.pkiMessage.getExtraCerts();
        if (extraCerts == null) {
            return new X509CertificateHolder[0];
        }
        X509CertificateHolder[] x509CertificateHolderArr = new X509CertificateHolder[extraCerts.length];
        for (int i = 0; i != extraCerts.length; i++) {
            x509CertificateHolderArr[i] = new X509CertificateHolder(extraCerts[i].getX509v3PKCert());
        }
        return x509CertificateHolderArr;
    }

    public PKIHeader getHeader() {
        return this.pkiMessage.getHeader();
    }

    public boolean hasPasswordBasedMacProtection() {
        return this.pkiMessage.getHeader().getProtectionAlg().getAlgorithm().equals((ASN1Primitive) CMPObjectIdentifiers.passwordBasedMac);
    }

    public PKIMessage toASN1Structure() {
        return this.pkiMessage;
    }

    public boolean verify(PKMACBuilder pKMACBuilder, char[] cArr) throws CMPException {
        if (CMPObjectIdentifiers.passwordBasedMac.equals((ASN1Primitive) this.pkiMessage.getHeader().getProtectionAlg().getAlgorithm())) {
            try {
                pKMACBuilder.setParameters(PBMParameter.getInstance(this.pkiMessage.getHeader().getProtectionAlg().getParameters()));
                MacCalculator build = pKMACBuilder.build(cArr);
                OutputStream outputStream = build.getOutputStream();
                ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                aSN1EncodableVector.add(this.pkiMessage.getHeader());
                aSN1EncodableVector.add(this.pkiMessage.getBody());
                outputStream.write(new DERSequence(aSN1EncodableVector).getEncoded(ASN1Encoding.DER));
                outputStream.close();
                return Arrays.areEqual(build.getMac(), this.pkiMessage.getProtection().getBytes());
            } catch (Exception e) {
                throw new CMPException("unable to verify MAC: " + e.getMessage(), e);
            }
        } else {
            throw new CMPException("protection algorithm not mac based");
        }
    }

    public boolean verify(ContentVerifierProvider contentVerifierProvider) throws CMPException {
        try {
            return verifySignature(this.pkiMessage.getProtection().getBytes(), contentVerifierProvider.get(this.pkiMessage.getHeader().getProtectionAlg()));
        } catch (Exception e) {
            throw new CMPException("unable to verify signature: " + e.getMessage(), e);
        }
    }
}
