package org.bouncycastle.cms;

import java.security.SecureRandom;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.PasswordRecipientInfo;
import org.bouncycastle.asn1.cms.RecipientInfo;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.PasswordRecipient;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.util.Arrays;

public abstract class PasswordRecipientInfoGenerator implements RecipientInfoGenerator {
    private int blockSize;
    private int iterationCount;
    private ASN1ObjectIdentifier kekAlgorithm;
    private AlgorithmIdentifier keyDerivationAlgorithm;
    private int keySize;
    protected char[] password;
    private PasswordRecipient.PRF prf;
    private SecureRandom random;
    private byte[] salt;
    private int schemeID;

    protected PasswordRecipientInfoGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier, char[] cArr) {
        this(aSN1ObjectIdentifier, cArr, getKeySize(aSN1ObjectIdentifier), ((Integer) PasswordRecipientInformation.BLOCKSIZES.get(aSN1ObjectIdentifier)).intValue());
    }

    protected PasswordRecipientInfoGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier, char[] cArr, int i, int i2) {
        this.password = cArr;
        this.schemeID = 1;
        this.kekAlgorithm = aSN1ObjectIdentifier;
        this.keySize = i;
        this.blockSize = i2;
        this.prf = PasswordRecipient.PRF.HMacSHA1;
        this.iterationCount = 1024;
    }

    private static int getKeySize(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        Integer num = (Integer) PasswordRecipientInformation.KEYSIZES.get(aSN1ObjectIdentifier);
        if (num != null) {
            return num.intValue();
        }
        throw new IllegalArgumentException("cannot find key size for algorithm: " + aSN1ObjectIdentifier);
    }

    /* access modifiers changed from: protected */
    public abstract byte[] calculateDerivedKey(int i, AlgorithmIdentifier algorithmIdentifier, int i2) throws CMSException;

    @Override // org.bouncycastle.cms.RecipientInfoGenerator
    public RecipientInfo generate(GenericKey genericKey) throws CMSException {
        byte[] bArr = new byte[this.blockSize];
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        this.random.nextBytes(bArr);
        if (this.salt == null) {
            this.salt = new byte[20];
            this.random.nextBytes(this.salt);
        }
        this.keyDerivationAlgorithm = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(this.salt, this.iterationCount, this.prf.prfAlgID));
        DEROctetString dEROctetString = new DEROctetString(generateEncryptedBytes(new AlgorithmIdentifier(this.kekAlgorithm, new DEROctetString(bArr)), calculateDerivedKey(this.schemeID, this.keyDerivationAlgorithm, this.keySize), genericKey));
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.kekAlgorithm);
        aSN1EncodableVector.add(new DEROctetString(bArr));
        return new RecipientInfo(new PasswordRecipientInfo(this.keyDerivationAlgorithm, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_PWRI_KEK, new DERSequence(aSN1EncodableVector)), dEROctetString));
    }

    /* access modifiers changed from: protected */
    public abstract byte[] generateEncryptedBytes(AlgorithmIdentifier algorithmIdentifier, byte[] bArr, GenericKey genericKey) throws CMSException;

    public PasswordRecipientInfoGenerator setPRF(PasswordRecipient.PRF prf2) {
        this.prf = prf2;
        return this;
    }

    public PasswordRecipientInfoGenerator setPasswordConversionScheme(int i) {
        this.schemeID = i;
        return this;
    }

    public PasswordRecipientInfoGenerator setSaltAndIterationCount(byte[] bArr, int i) {
        this.salt = Arrays.clone(bArr);
        this.iterationCount = i;
        return this;
    }

    public PasswordRecipientInfoGenerator setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
