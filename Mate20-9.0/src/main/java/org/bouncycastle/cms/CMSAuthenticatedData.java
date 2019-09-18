package org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.AuthenticatedData;
import org.bouncycastle.asn1.cms.CMSAlgorithmProtection;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSEnvelopedHelper;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Encodable;

public class CMSAuthenticatedData implements Encodable {
    /* access modifiers changed from: private */
    public ASN1Set authAttrs;
    ContentInfo contentInfo;
    private byte[] mac;
    private AlgorithmIdentifier macAlg;
    private OriginatorInformation originatorInfo;
    RecipientInformationStore recipientInfoStore;
    private ASN1Set unauthAttrs;

    public CMSAuthenticatedData(InputStream inputStream) throws CMSException {
        this(CMSUtils.readContentInfo(inputStream));
    }

    public CMSAuthenticatedData(InputStream inputStream, DigestCalculatorProvider digestCalculatorProvider) throws CMSException {
        this(CMSUtils.readContentInfo(inputStream), digestCalculatorProvider);
    }

    public CMSAuthenticatedData(ContentInfo contentInfo2) throws CMSException {
        this(contentInfo2, (DigestCalculatorProvider) null);
    }

    public CMSAuthenticatedData(ContentInfo contentInfo2, DigestCalculatorProvider digestCalculatorProvider) throws CMSException {
        this.contentInfo = contentInfo2;
        AuthenticatedData instance = AuthenticatedData.getInstance(contentInfo2.getContent());
        if (instance.getOriginatorInfo() != null) {
            this.originatorInfo = new OriginatorInformation(instance.getOriginatorInfo());
        }
        ASN1Set recipientInfos = instance.getRecipientInfos();
        this.macAlg = instance.getMacAlgorithm();
        this.authAttrs = instance.getAuthAttrs();
        this.mac = instance.getMac().getOctets();
        this.unauthAttrs = instance.getUnauthAttrs();
        CMSProcessableByteArray cMSProcessableByteArray = new CMSProcessableByteArray(ASN1OctetString.getInstance(instance.getEncapsulatedContentInfo().getContent()).getOctets());
        if (this.authAttrs == null) {
            this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(recipientInfos, this.macAlg, new CMSEnvelopedHelper.CMSAuthenticatedSecureReadable(this.macAlg, cMSProcessableByteArray));
        } else if (digestCalculatorProvider != null) {
            ASN1EncodableVector all = new AttributeTable(this.authAttrs).getAll(CMSAttributes.cmsAlgorithmProtect);
            if (all.size() <= 1) {
                if (all.size() > 0) {
                    Attribute instance2 = Attribute.getInstance(all.get(0));
                    if (instance2.getAttrValues().size() == 1) {
                        CMSAlgorithmProtection instance3 = CMSAlgorithmProtection.getInstance(instance2.getAttributeValues()[0]);
                        if (!CMSUtils.isEquivalent(instance3.getDigestAlgorithm(), instance.getDigestAlgorithm())) {
                            throw new CMSException("CMS Algorithm Identifier Protection check failed for digestAlgorithm");
                        } else if (!CMSUtils.isEquivalent(instance3.getMacAlgorithm(), this.macAlg)) {
                            throw new CMSException("CMS Algorithm Identifier Protection check failed for macAlgorithm");
                        }
                    } else {
                        throw new CMSException("A cmsAlgorithmProtect attribute MUST contain exactly one value");
                    }
                }
                try {
                    this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(recipientInfos, this.macAlg, new CMSEnvelopedHelper.CMSDigestAuthenticatedSecureReadable(digestCalculatorProvider.get(instance.getDigestAlgorithm()), cMSProcessableByteArray), new AuthAttributesProvider() {
                        public ASN1Set getAuthAttributes() {
                            return CMSAuthenticatedData.this.authAttrs;
                        }
                    });
                } catch (OperatorCreationException e) {
                    throw new CMSException("unable to create digest calculator: " + e.getMessage(), e);
                }
            } else {
                throw new CMSException("Only one instance of a cmsAlgorithmProtect attribute can be present");
            }
        } else {
            throw new CMSException("a digest calculator provider is required if authenticated attributes are present");
        }
    }

    public CMSAuthenticatedData(byte[] bArr) throws CMSException {
        this(CMSUtils.readContentInfo(bArr));
    }

    public CMSAuthenticatedData(byte[] bArr, DigestCalculatorProvider digestCalculatorProvider) throws CMSException {
        this(CMSUtils.readContentInfo(bArr), digestCalculatorProvider);
    }

    private byte[] encodeObj(ASN1Encodable aSN1Encodable) throws IOException {
        if (aSN1Encodable != null) {
            return aSN1Encodable.toASN1Primitive().getEncoded();
        }
        return null;
    }

    public AttributeTable getAuthAttrs() {
        if (this.authAttrs == null) {
            return null;
        }
        return new AttributeTable(this.authAttrs);
    }

    public byte[] getContentDigest() {
        if (this.authAttrs != null) {
            return ASN1OctetString.getInstance(getAuthAttrs().get(CMSAttributes.messageDigest).getAttrValues().getObjectAt(0)).getOctets();
        }
        return null;
    }

    public ContentInfo getContentInfo() {
        return this.contentInfo;
    }

    public byte[] getEncoded() throws IOException {
        return this.contentInfo.getEncoded();
    }

    public byte[] getMac() {
        return Arrays.clone(this.mac);
    }

    public String getMacAlgOID() {
        return this.macAlg.getAlgorithm().getId();
    }

    public byte[] getMacAlgParams() {
        try {
            return encodeObj(this.macAlg.getParameters());
        } catch (Exception e) {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    public AlgorithmIdentifier getMacAlgorithm() {
        return this.macAlg;
    }

    public OriginatorInformation getOriginatorInfo() {
        return this.originatorInfo;
    }

    public RecipientInformationStore getRecipientInfos() {
        return this.recipientInfoStore;
    }

    public AttributeTable getUnauthAttrs() {
        if (this.unauthAttrs == null) {
            return null;
        }
        return new AttributeTable(this.unauthAttrs);
    }

    public ContentInfo toASN1Structure() {
        return this.contentInfo;
    }
}
