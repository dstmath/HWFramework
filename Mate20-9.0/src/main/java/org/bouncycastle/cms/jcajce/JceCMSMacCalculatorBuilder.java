package org.bouncycastle.cms.jcajce;

import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.Provider;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jcajce.io.MacOutputStream;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.MacCalculator;
import org.bouncycastle.operator.jcajce.JceGenericKey;

public class JceCMSMacCalculatorBuilder {
    private AlgorithmParameters algorithmParameters;
    /* access modifiers changed from: private */
    public EnvelopedDataHelper helper;
    private final int keySize;
    private final ASN1ObjectIdentifier macOID;
    private SecureRandom random;

    private class CMSMacCalculator implements MacCalculator {
        private AlgorithmIdentifier algorithmIdentifier;
        private SecretKey encKey;
        private Mac mac;

        CMSMacCalculator(ASN1ObjectIdentifier aSN1ObjectIdentifier, int i, AlgorithmParameters algorithmParameters, SecureRandom secureRandom) throws CMSException {
            KeyGenerator createKeyGenerator = JceCMSMacCalculatorBuilder.this.helper.createKeyGenerator(aSN1ObjectIdentifier);
            secureRandom = secureRandom == null ? new SecureRandom() : secureRandom;
            if (i < 0) {
                createKeyGenerator.init(secureRandom);
            } else {
                createKeyGenerator.init(i, secureRandom);
            }
            this.encKey = createKeyGenerator.generateKey();
            this.algorithmIdentifier = JceCMSMacCalculatorBuilder.this.helper.getAlgorithmIdentifier(aSN1ObjectIdentifier, algorithmParameters == null ? JceCMSMacCalculatorBuilder.this.helper.generateParameters(aSN1ObjectIdentifier, this.encKey, secureRandom) : algorithmParameters);
            this.mac = JceCMSMacCalculatorBuilder.this.helper.createContentMac(this.encKey, this.algorithmIdentifier);
        }

        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return this.algorithmIdentifier;
        }

        public GenericKey getKey() {
            return new JceGenericKey(this.algorithmIdentifier, this.encKey);
        }

        public byte[] getMac() {
            return this.mac.doFinal();
        }

        public OutputStream getOutputStream() {
            return new MacOutputStream(this.mac);
        }
    }

    public JceCMSMacCalculatorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this(aSN1ObjectIdentifier, -1);
    }

    public JceCMSMacCalculatorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier, int i) {
        this.helper = new EnvelopedDataHelper(new DefaultJcaJceExtHelper());
        this.macOID = aSN1ObjectIdentifier;
        this.keySize = i;
    }

    public MacCalculator build() throws CMSException {
        CMSMacCalculator cMSMacCalculator = new CMSMacCalculator(this.macOID, this.keySize, this.algorithmParameters, this.random);
        return cMSMacCalculator;
    }

    public JceCMSMacCalculatorBuilder setAlgorithmParameters(AlgorithmParameters algorithmParameters2) {
        this.algorithmParameters = algorithmParameters2;
        return this;
    }

    public JceCMSMacCalculatorBuilder setProvider(String str) {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceExtHelper(str));
        return this;
    }

    public JceCMSMacCalculatorBuilder setProvider(Provider provider) {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceExtHelper(provider));
        return this;
    }

    public JceCMSMacCalculatorBuilder setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
