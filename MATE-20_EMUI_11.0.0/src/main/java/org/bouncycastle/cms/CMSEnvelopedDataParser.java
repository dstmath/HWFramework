package org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1OctetStringParser;
import org.bouncycastle.asn1.ASN1SequenceParser;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1SetParser;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.EncryptedContentInfoParser;
import org.bouncycastle.asn1.cms.EnvelopedDataParser;
import org.bouncycastle.asn1.cms.OriginatorInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSEnvelopedHelper;

public class CMSEnvelopedDataParser extends CMSContentInfoParser {
    private boolean attrNotRead;
    private AlgorithmIdentifier encAlg;
    EnvelopedDataParser envelopedData;
    private OriginatorInformation originatorInfo;
    RecipientInformationStore recipientInfoStore;
    private AttributeTable unprotectedAttributes;

    public CMSEnvelopedDataParser(InputStream inputStream) throws CMSException, IOException {
        super(inputStream);
        this.attrNotRead = true;
        this.envelopedData = new EnvelopedDataParser((ASN1SequenceParser) this._contentInfo.getContent(16));
        OriginatorInfo originatorInfo2 = this.envelopedData.getOriginatorInfo();
        if (originatorInfo2 != null) {
            this.originatorInfo = new OriginatorInformation(originatorInfo2);
        }
        ASN1Set instance = ASN1Set.getInstance(this.envelopedData.getRecipientInfos().toASN1Primitive());
        EncryptedContentInfoParser encryptedContentInfo = this.envelopedData.getEncryptedContentInfo();
        this.encAlg = encryptedContentInfo.getContentEncryptionAlgorithm();
        this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(instance, this.encAlg, new CMSEnvelopedHelper.CMSEnvelopedSecureReadable(this.encAlg, new CMSProcessableInputStream(((ASN1OctetStringParser) encryptedContentInfo.getEncryptedContent(4)).getOctetStream())));
    }

    public CMSEnvelopedDataParser(byte[] bArr) throws CMSException, IOException {
        this(new ByteArrayInputStream(bArr));
    }

    private byte[] encodeObj(ASN1Encodable aSN1Encodable) throws IOException {
        if (aSN1Encodable != null) {
            return aSN1Encodable.toASN1Primitive().getEncoded();
        }
        return null;
    }

    public AlgorithmIdentifier getContentEncryptionAlgorithm() {
        return this.encAlg;
    }

    public String getEncryptionAlgOID() {
        return this.encAlg.getAlgorithm().toString();
    }

    public byte[] getEncryptionAlgParams() {
        try {
            return encodeObj(this.encAlg.getParameters());
        } catch (Exception e) {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    public OriginatorInformation getOriginatorInfo() {
        return this.originatorInfo;
    }

    public RecipientInformationStore getRecipientInfos() {
        return this.recipientInfoStore;
    }

    public AttributeTable getUnprotectedAttributes() throws IOException {
        if (this.unprotectedAttributes == null && this.attrNotRead) {
            ASN1SetParser unprotectedAttrs = this.envelopedData.getUnprotectedAttrs();
            this.attrNotRead = false;
            if (unprotectedAttrs != null) {
                ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                while (true) {
                    ASN1Encodable readObject = unprotectedAttrs.readObject();
                    if (readObject == null) {
                        break;
                    }
                    aSN1EncodableVector.add(((ASN1SequenceParser) readObject).toASN1Primitive());
                }
                this.unprotectedAttributes = new AttributeTable(new DERSet(aSN1EncodableVector));
            }
        }
        return this.unprotectedAttributes;
    }
}
