package org.bouncycastle.cert.crmf;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.crmf.AttributeTypeAndValue;
import org.bouncycastle.asn1.crmf.CertReqMsg;
import org.bouncycastle.asn1.crmf.CertRequest;
import org.bouncycastle.asn1.crmf.CertTemplate;
import org.bouncycastle.asn1.crmf.CertTemplateBuilder;
import org.bouncycastle.asn1.crmf.OptionalValidity;
import org.bouncycastle.asn1.crmf.PKMACValue;
import org.bouncycastle.asn1.crmf.POPOPrivKey;
import org.bouncycastle.asn1.crmf.ProofOfPossession;
import org.bouncycastle.asn1.crmf.SubsequentMessage;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.ContentSigner;

public class CertificateRequestMessageBuilder {
    private PKMACValue agreeMAC;
    private final BigInteger certReqId;
    private List controls;
    private ExtensionsGenerator extGenerator;
    private char[] password;
    private PKMACBuilder pkmacBuilder;
    private ASN1Null popRaVerified;
    private ContentSigner popSigner;
    private POPOPrivKey popoPrivKey;
    private int popoType = 2;
    private GeneralName sender;
    private CertTemplateBuilder templateBuilder;

    public CertificateRequestMessageBuilder(BigInteger bigInteger) {
        this.certReqId = bigInteger;
        this.extGenerator = new ExtensionsGenerator();
        this.templateBuilder = new CertTemplateBuilder();
        this.controls = new ArrayList();
    }

    private Time createTime(Date date) {
        if (date != null) {
            return new Time(date);
        }
        return null;
    }

    public CertificateRequestMessageBuilder addControl(Control control) {
        this.controls.add(control);
        return this;
    }

    public CertificateRequestMessageBuilder addExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, ASN1Encodable aSN1Encodable) throws CertIOException {
        CRMFUtil.addExtension(this.extGenerator, aSN1ObjectIdentifier, z, aSN1Encodable);
        return this;
    }

    public CertificateRequestMessageBuilder addExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, byte[] bArr) {
        this.extGenerator.addExtension(aSN1ObjectIdentifier, z, bArr);
        return this;
    }

    public CertificateRequestMessage build() throws CRMFException {
        ProofOfPossession proofOfPossession;
        ProofOfPossession proofOfPossession2;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(new ASN1Integer(this.certReqId));
        if (!this.extGenerator.isEmpty()) {
            this.templateBuilder.setExtensions(this.extGenerator.generate());
        }
        aSN1EncodableVector.add(this.templateBuilder.build());
        if (!this.controls.isEmpty()) {
            ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
            for (Control control : this.controls) {
                aSN1EncodableVector2.add(new AttributeTypeAndValue(control.getType(), control.getValue()));
            }
            aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        }
        CertRequest instance = CertRequest.getInstance(new DERSequence(aSN1EncodableVector));
        ASN1EncodableVector aSN1EncodableVector3 = new ASN1EncodableVector();
        aSN1EncodableVector3.add(instance);
        if (this.popSigner != null) {
            CertTemplate certTemplate = instance.getCertTemplate();
            if (certTemplate.getSubject() == null || certTemplate.getPublicKey() == null) {
                ProofOfPossessionSigningKeyBuilder proofOfPossessionSigningKeyBuilder = new ProofOfPossessionSigningKeyBuilder(instance.getCertTemplate().getPublicKey());
                GeneralName generalName = this.sender;
                if (generalName != null) {
                    proofOfPossessionSigningKeyBuilder.setSender(generalName);
                } else {
                    proofOfPossessionSigningKeyBuilder.setPublicKeyMac(new PKMACValueGenerator(this.pkmacBuilder), this.password);
                }
                proofOfPossession = new ProofOfPossession(proofOfPossessionSigningKeyBuilder.build(this.popSigner));
            } else {
                proofOfPossession = new ProofOfPossession(new ProofOfPossessionSigningKeyBuilder(instance).build(this.popSigner));
            }
        } else {
            POPOPrivKey pOPOPrivKey = this.popoPrivKey;
            if (pOPOPrivKey != null) {
                proofOfPossession2 = new ProofOfPossession(this.popoType, pOPOPrivKey);
            } else {
                PKMACValue pKMACValue = this.agreeMAC;
                if (pKMACValue != null) {
                    proofOfPossession2 = new ProofOfPossession(3, POPOPrivKey.getInstance(new DERTaggedObject(false, 3, pKMACValue)));
                } else {
                    if (this.popRaVerified != null) {
                        proofOfPossession = new ProofOfPossession();
                    }
                    return new CertificateRequestMessage(CertReqMsg.getInstance(new DERSequence(aSN1EncodableVector3)));
                }
            }
            aSN1EncodableVector3.add(proofOfPossession2);
            return new CertificateRequestMessage(CertReqMsg.getInstance(new DERSequence(aSN1EncodableVector3)));
        }
        aSN1EncodableVector3.add(proofOfPossession);
        return new CertificateRequestMessage(CertReqMsg.getInstance(new DERSequence(aSN1EncodableVector3)));
    }

    public CertificateRequestMessageBuilder setAuthInfoPKMAC(PKMACBuilder pKMACBuilder, char[] cArr) {
        this.pkmacBuilder = pKMACBuilder;
        this.password = cArr;
        return this;
    }

    public CertificateRequestMessageBuilder setAuthInfoSender(X500Name x500Name) {
        return setAuthInfoSender(new GeneralName(x500Name));
    }

    public CertificateRequestMessageBuilder setAuthInfoSender(GeneralName generalName) {
        this.sender = generalName;
        return this;
    }

    public CertificateRequestMessageBuilder setIssuer(X500Name x500Name) {
        if (x500Name != null) {
            this.templateBuilder.setIssuer(x500Name);
        }
        return this;
    }

    public CertificateRequestMessageBuilder setProofOfPossessionAgreeMAC(PKMACValue pKMACValue) {
        if (this.popSigner == null && this.popRaVerified == null && this.popoPrivKey == null) {
            this.agreeMAC = pKMACValue;
            return this;
        }
        throw new IllegalStateException("only one proof of possession allowed");
    }

    public CertificateRequestMessageBuilder setProofOfPossessionRaVerified() {
        if (this.popSigner == null && this.popoPrivKey == null) {
            this.popRaVerified = DERNull.INSTANCE;
            return this;
        }
        throw new IllegalStateException("only one proof of possession allowed");
    }

    public CertificateRequestMessageBuilder setProofOfPossessionSigningKeySigner(ContentSigner contentSigner) {
        if (this.popoPrivKey == null && this.popRaVerified == null && this.agreeMAC == null) {
            this.popSigner = contentSigner;
            return this;
        }
        throw new IllegalStateException("only one proof of possession allowed");
    }

    public CertificateRequestMessageBuilder setProofOfPossessionSubsequentMessage(int i, SubsequentMessage subsequentMessage) {
        if (this.popSigner != null || this.popRaVerified != null || this.agreeMAC != null) {
            throw new IllegalStateException("only one proof of possession allowed");
        } else if (i == 2 || i == 3) {
            this.popoType = i;
            this.popoPrivKey = new POPOPrivKey(subsequentMessage);
            return this;
        } else {
            throw new IllegalArgumentException("type must be ProofOfPossession.TYPE_KEY_ENCIPHERMENT || ProofOfPossession.TYPE_KEY_AGREEMENT");
        }
    }

    public CertificateRequestMessageBuilder setProofOfPossessionSubsequentMessage(SubsequentMessage subsequentMessage) {
        if (this.popSigner == null && this.popRaVerified == null && this.agreeMAC == null) {
            this.popoType = 2;
            this.popoPrivKey = new POPOPrivKey(subsequentMessage);
            return this;
        }
        throw new IllegalStateException("only one proof of possession allowed");
    }

    public CertificateRequestMessageBuilder setPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        if (subjectPublicKeyInfo != null) {
            this.templateBuilder.setPublicKey(subjectPublicKeyInfo);
        }
        return this;
    }

    public CertificateRequestMessageBuilder setSerialNumber(BigInteger bigInteger) {
        if (bigInteger != null) {
            this.templateBuilder.setSerialNumber(new ASN1Integer(bigInteger));
        }
        return this;
    }

    public CertificateRequestMessageBuilder setSubject(X500Name x500Name) {
        if (x500Name != null) {
            this.templateBuilder.setSubject(x500Name);
        }
        return this;
    }

    public CertificateRequestMessageBuilder setValidity(Date date, Date date2) {
        this.templateBuilder.setValidity(new OptionalValidity(createTime(date), createTime(date2)));
        return this;
    }
}
