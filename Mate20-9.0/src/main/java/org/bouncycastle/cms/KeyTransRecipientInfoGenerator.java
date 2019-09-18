package org.bouncycastle.cms;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.cms.KeyTransRecipientInfo;
import org.bouncycastle.asn1.cms.RecipientIdentifier;
import org.bouncycastle.asn1.cms.RecipientInfo;
import org.bouncycastle.operator.AsymmetricKeyWrapper;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.OperatorException;

public abstract class KeyTransRecipientInfoGenerator implements RecipientInfoGenerator {
    private IssuerAndSerialNumber issuerAndSerial;
    private byte[] subjectKeyIdentifier;
    protected final AsymmetricKeyWrapper wrapper;

    protected KeyTransRecipientInfoGenerator(IssuerAndSerialNumber issuerAndSerialNumber, AsymmetricKeyWrapper asymmetricKeyWrapper) {
        this.issuerAndSerial = issuerAndSerialNumber;
        this.wrapper = asymmetricKeyWrapper;
    }

    protected KeyTransRecipientInfoGenerator(byte[] bArr, AsymmetricKeyWrapper asymmetricKeyWrapper) {
        this.subjectKeyIdentifier = bArr;
        this.wrapper = asymmetricKeyWrapper;
    }

    public final RecipientInfo generate(GenericKey genericKey) throws CMSException {
        try {
            return new RecipientInfo(new KeyTransRecipientInfo(this.issuerAndSerial != null ? new RecipientIdentifier(this.issuerAndSerial) : new RecipientIdentifier((ASN1OctetString) new DEROctetString(this.subjectKeyIdentifier)), this.wrapper.getAlgorithmIdentifier(), new DEROctetString(this.wrapper.generateWrappedKey(genericKey))));
        } catch (OperatorException e) {
            throw new CMSException("exception wrapping content key: " + e.getMessage(), e);
        }
    }
}
