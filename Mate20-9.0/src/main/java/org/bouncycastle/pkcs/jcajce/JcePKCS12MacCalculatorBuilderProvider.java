package org.bouncycastle.pkcs.jcajce;

import java.io.OutputStream;
import java.security.Provider;
import javax.crypto.Mac;
import javax.crypto.spec.PBEParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.jcajce.PKCS12Key;
import org.bouncycastle.jcajce.io.MacOutputStream;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.MacCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.PKCS12MacCalculatorBuilderProvider;

public class JcePKCS12MacCalculatorBuilderProvider implements PKCS12MacCalculatorBuilderProvider {
    /* access modifiers changed from: private */
    public JcaJceHelper helper = new DefaultJcaJceHelper();

    public PKCS12MacCalculatorBuilder get(final AlgorithmIdentifier algorithmIdentifier) {
        return new PKCS12MacCalculatorBuilder() {
            public MacCalculator build(char[] cArr) throws OperatorCreationException {
                final PKCS12PBEParams instance = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
                try {
                    final ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();
                    final Mac createMac = JcePKCS12MacCalculatorBuilderProvider.this.helper.createMac(algorithm.getId());
                    PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(instance.getIV(), instance.getIterations().intValue());
                    final PKCS12Key pKCS12Key = new PKCS12Key(cArr);
                    createMac.init(pKCS12Key, pBEParameterSpec);
                    AnonymousClass1 r1 = new MacCalculator() {
                        public AlgorithmIdentifier getAlgorithmIdentifier() {
                            return new AlgorithmIdentifier(algorithm, instance);
                        }

                        public GenericKey getKey() {
                            return new GenericKey(getAlgorithmIdentifier(), pKCS12Key.getEncoded());
                        }

                        public byte[] getMac() {
                            return createMac.doFinal();
                        }

                        public OutputStream getOutputStream() {
                            return new MacOutputStream(createMac);
                        }
                    };
                    return r1;
                } catch (Exception e) {
                    throw new OperatorCreationException("unable to create MAC calculator: " + e.getMessage(), e);
                }
            }

            public AlgorithmIdentifier getDigestAlgorithmIdentifier() {
                return new AlgorithmIdentifier(algorithmIdentifier.getAlgorithm(), DERNull.INSTANCE);
            }
        };
    }

    public JcePKCS12MacCalculatorBuilderProvider setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JcePKCS12MacCalculatorBuilderProvider setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }
}
