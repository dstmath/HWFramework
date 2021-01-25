package org.bouncycastle.pkcs.bc;

import java.security.SecureRandom;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.operator.MacCalculator;
import org.bouncycastle.pkcs.PKCS12MacCalculatorBuilder;

public class BcPKCS12MacCalculatorBuilder implements PKCS12MacCalculatorBuilder {
    private AlgorithmIdentifier algorithmIdentifier;
    private ExtendedDigest digest;
    private int iterationCount;
    private SecureRandom random;
    private int saltLength;

    public BcPKCS12MacCalculatorBuilder() {
        this(new SHA1Digest(), new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE));
    }

    public BcPKCS12MacCalculatorBuilder(ExtendedDigest extendedDigest, AlgorithmIdentifier algorithmIdentifier2) {
        this.iterationCount = 1024;
        this.digest = extendedDigest;
        this.algorithmIdentifier = algorithmIdentifier2;
        this.saltLength = extendedDigest.getDigestSize();
    }

    @Override // org.bouncycastle.pkcs.PKCS12MacCalculatorBuilder
    public MacCalculator build(char[] cArr) {
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        byte[] bArr = new byte[this.saltLength];
        this.random.nextBytes(bArr);
        return PKCS12PBEUtils.createMacCalculator(this.algorithmIdentifier.getAlgorithm(), this.digest, new PKCS12PBEParams(bArr, this.iterationCount), cArr);
    }

    @Override // org.bouncycastle.pkcs.PKCS12MacCalculatorBuilder
    public AlgorithmIdentifier getDigestAlgorithmIdentifier() {
        return this.algorithmIdentifier;
    }

    public BcPKCS12MacCalculatorBuilder setIterationCount(int i) {
        this.iterationCount = i;
        return this;
    }
}
