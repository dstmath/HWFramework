package org.bouncycastle.cms;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.cms.KeyTransRecipientInfo;
import org.bouncycastle.asn1.cms.RecipientIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class KeyTransRecipientInformation extends RecipientInformation {
    private KeyTransRecipientInfo info;

    KeyTransRecipientInformation(KeyTransRecipientInfo keyTransRecipientInfo, AlgorithmIdentifier algorithmIdentifier, CMSSecureReadable cMSSecureReadable, AuthAttributesProvider authAttributesProvider) {
        super(keyTransRecipientInfo.getKeyEncryptionAlgorithm(), algorithmIdentifier, cMSSecureReadable, authAttributesProvider);
        KeyTransRecipientId keyTransRecipientId;
        this.info = keyTransRecipientInfo;
        RecipientIdentifier recipientIdentifier = keyTransRecipientInfo.getRecipientIdentifier();
        if (recipientIdentifier.isTagged()) {
            keyTransRecipientId = new KeyTransRecipientId(ASN1OctetString.getInstance(recipientIdentifier.getId()).getOctets());
        } else {
            IssuerAndSerialNumber instance = IssuerAndSerialNumber.getInstance(recipientIdentifier.getId());
            keyTransRecipientId = new KeyTransRecipientId(instance.getName(), instance.getSerialNumber().getValue());
        }
        this.rid = keyTransRecipientId;
    }

    /* access modifiers changed from: protected */
    public RecipientOperator getRecipientOperator(Recipient recipient) throws CMSException {
        return ((KeyTransRecipient) recipient).getRecipientOperator(this.keyEncAlg, this.messageAlgorithm, this.info.getEncryptedKey().getOctets());
    }
}
