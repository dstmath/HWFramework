package org.bouncycastle.cms;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.KeyAgreeRecipientInfo;
import org.bouncycastle.asn1.cms.OriginatorIdentifierOrKey;
import org.bouncycastle.asn1.cms.OriginatorPublicKey;
import org.bouncycastle.asn1.cms.RecipientInfo;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.Gost2814789KeyWrapParameters;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.operator.GenericKey;

public abstract class KeyAgreeRecipientInfoGenerator implements RecipientInfoGenerator {
    private ASN1ObjectIdentifier keyAgreementOID;
    private ASN1ObjectIdentifier keyEncryptionOID;
    private SubjectPublicKeyInfo originatorKeyInfo;

    protected KeyAgreeRecipientInfoGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier, SubjectPublicKeyInfo subjectPublicKeyInfo, ASN1ObjectIdentifier aSN1ObjectIdentifier2) {
        this.originatorKeyInfo = subjectPublicKeyInfo;
        this.keyAgreementOID = aSN1ObjectIdentifier;
        this.keyEncryptionOID = aSN1ObjectIdentifier2;
    }

    /* access modifiers changed from: protected */
    public OriginatorPublicKey createOriginatorPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        return new OriginatorPublicKey(new AlgorithmIdentifier(subjectPublicKeyInfo.getAlgorithm().getAlgorithm(), DERNull.INSTANCE), subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    @Override // org.bouncycastle.cms.RecipientInfoGenerator
    public RecipientInfo generate(GenericKey genericKey) throws CMSException {
        OriginatorIdentifierOrKey originatorIdentifierOrKey = new OriginatorIdentifierOrKey(createOriginatorPublicKey(this.originatorKeyInfo));
        AlgorithmIdentifier algorithmIdentifier = (CMSUtils.isDES(this.keyEncryptionOID.getId()) || this.keyEncryptionOID.equals(PKCSObjectIdentifiers.id_alg_CMSRC2wrap)) ? new AlgorithmIdentifier(this.keyEncryptionOID, DERNull.INSTANCE) : CMSUtils.isGOST(this.keyAgreementOID) ? new AlgorithmIdentifier(this.keyEncryptionOID, new Gost2814789KeyWrapParameters(CryptoProObjectIdentifiers.id_Gost28147_89_CryptoPro_A_ParamSet)) : new AlgorithmIdentifier(this.keyEncryptionOID);
        AlgorithmIdentifier algorithmIdentifier2 = new AlgorithmIdentifier(this.keyAgreementOID, algorithmIdentifier);
        ASN1Sequence generateRecipientEncryptedKeys = generateRecipientEncryptedKeys(algorithmIdentifier2, algorithmIdentifier, genericKey);
        byte[] userKeyingMaterial = getUserKeyingMaterial(algorithmIdentifier2);
        return userKeyingMaterial != null ? new RecipientInfo(new KeyAgreeRecipientInfo(originatorIdentifierOrKey, new DEROctetString(userKeyingMaterial), algorithmIdentifier2, generateRecipientEncryptedKeys)) : new RecipientInfo(new KeyAgreeRecipientInfo(originatorIdentifierOrKey, null, algorithmIdentifier2, generateRecipientEncryptedKeys));
    }

    /* access modifiers changed from: protected */
    public abstract ASN1Sequence generateRecipientEncryptedKeys(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, GenericKey genericKey) throws CMSException;

    /* access modifiers changed from: protected */
    public abstract byte[] getUserKeyingMaterial(AlgorithmIdentifier algorithmIdentifier) throws CMSException;
}
