package org.bouncycastle.cert.cmp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cmp.CMPCertificate;
import org.bouncycastle.asn1.cmp.InfoTypeAndValue;
import org.bouncycastle.asn1.cmp.PKIBody;
import org.bouncycastle.asn1.cmp.PKIFreeText;
import org.bouncycastle.asn1.cmp.PKIHeader;
import org.bouncycastle.asn1.cmp.PKIHeaderBuilder;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.MacCalculator;

public class ProtectedPKIMessageBuilder {
    private PKIBody body;
    private List extraCerts;
    private List generalInfos;
    private PKIHeaderBuilder hdrBuilder;

    public ProtectedPKIMessageBuilder(int i, GeneralName generalName, GeneralName generalName2) {
        this.generalInfos = new ArrayList();
        this.extraCerts = new ArrayList();
        this.hdrBuilder = new PKIHeaderBuilder(i, generalName, generalName2);
    }

    public ProtectedPKIMessageBuilder(GeneralName generalName, GeneralName generalName2) {
        this(2, generalName, generalName2);
    }

    private byte[] calculateMac(MacCalculator macCalculator, PKIHeader pKIHeader, PKIBody pKIBody) throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(pKIHeader);
        aSN1EncodableVector.add(pKIBody);
        OutputStream outputStream = macCalculator.getOutputStream();
        outputStream.write(new DERSequence(aSN1EncodableVector).getEncoded(ASN1Encoding.DER));
        outputStream.close();
        return macCalculator.getMac();
    }

    private byte[] calculateSignature(ContentSigner contentSigner, PKIHeader pKIHeader, PKIBody pKIBody) throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(pKIHeader);
        aSN1EncodableVector.add(pKIBody);
        OutputStream outputStream = contentSigner.getOutputStream();
        outputStream.write(new DERSequence(aSN1EncodableVector).getEncoded(ASN1Encoding.DER));
        outputStream.close();
        return contentSigner.getSignature();
    }

    private void finaliseHeader(AlgorithmIdentifier algorithmIdentifier) {
        this.hdrBuilder.setProtectionAlg(algorithmIdentifier);
        if (!this.generalInfos.isEmpty()) {
            this.hdrBuilder.setGeneralInfo((InfoTypeAndValue[]) this.generalInfos.toArray(new InfoTypeAndValue[this.generalInfos.size()]));
        }
    }

    private ProtectedPKIMessage finaliseMessage(PKIHeader pKIHeader, DERBitString dERBitString) {
        if (this.extraCerts.isEmpty()) {
            return new ProtectedPKIMessage(new PKIMessage(pKIHeader, this.body, dERBitString));
        }
        CMPCertificate[] cMPCertificateArr = new CMPCertificate[this.extraCerts.size()];
        for (int i = 0; i != cMPCertificateArr.length; i++) {
            cMPCertificateArr[i] = new CMPCertificate(((X509CertificateHolder) this.extraCerts.get(i)).toASN1Structure());
        }
        return new ProtectedPKIMessage(new PKIMessage(pKIHeader, this.body, dERBitString, cMPCertificateArr));
    }

    public ProtectedPKIMessageBuilder addCMPCertificate(X509CertificateHolder x509CertificateHolder) {
        this.extraCerts.add(x509CertificateHolder);
        return this;
    }

    public ProtectedPKIMessageBuilder addGeneralInfo(InfoTypeAndValue infoTypeAndValue) {
        this.generalInfos.add(infoTypeAndValue);
        return this;
    }

    public ProtectedPKIMessage build(ContentSigner contentSigner) throws CMPException {
        if (this.body != null) {
            finaliseHeader(contentSigner.getAlgorithmIdentifier());
            PKIHeader build = this.hdrBuilder.build();
            try {
                return finaliseMessage(build, new DERBitString(calculateSignature(contentSigner, build, this.body)));
            } catch (IOException e) {
                throw new CMPException("unable to encode signature input: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("body must be set before building");
        }
    }

    public ProtectedPKIMessage build(MacCalculator macCalculator) throws CMPException {
        if (this.body != null) {
            finaliseHeader(macCalculator.getAlgorithmIdentifier());
            PKIHeader build = this.hdrBuilder.build();
            try {
                return finaliseMessage(build, new DERBitString(calculateMac(macCalculator, build, this.body)));
            } catch (IOException e) {
                throw new CMPException("unable to encode MAC input: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("body must be set before building");
        }
    }

    public ProtectedPKIMessageBuilder setBody(PKIBody pKIBody) {
        this.body = pKIBody;
        return this;
    }

    public ProtectedPKIMessageBuilder setFreeText(PKIFreeText pKIFreeText) {
        this.hdrBuilder.setFreeText(pKIFreeText);
        return this;
    }

    public ProtectedPKIMessageBuilder setMessageTime(Date date) {
        this.hdrBuilder.setMessageTime(new ASN1GeneralizedTime(date));
        return this;
    }

    public ProtectedPKIMessageBuilder setRecipKID(byte[] bArr) {
        this.hdrBuilder.setRecipKID(bArr);
        return this;
    }

    public ProtectedPKIMessageBuilder setRecipNonce(byte[] bArr) {
        this.hdrBuilder.setRecipNonce(bArr);
        return this;
    }

    public ProtectedPKIMessageBuilder setSenderKID(byte[] bArr) {
        this.hdrBuilder.setSenderKID(bArr);
        return this;
    }

    public ProtectedPKIMessageBuilder setSenderNonce(byte[] bArr) {
        this.hdrBuilder.setSenderNonce(bArr);
        return this;
    }

    public ProtectedPKIMessageBuilder setTransactionID(byte[] bArr) {
        this.hdrBuilder.setTransactionID(bArr);
        return this;
    }
}
