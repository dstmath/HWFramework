package org.bouncycastle.jcajce.provider.asymmetric.ec;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.agreement.ECDHCBasicAgreement;
import org.bouncycastle.crypto.agreement.ECDHCUnifiedAgreement;
import org.bouncycastle.crypto.agreement.ECMQVBasicAgreement;
import org.bouncycastle.crypto.agreement.kdf.ConcatenationKDFGenerator;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.params.ECDHUPrivateParameters;
import org.bouncycastle.crypto.params.ECDHUPublicParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.MQVPrivateParameters;
import org.bouncycastle.crypto.params.MQVPublicParameters;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.jcajce.provider.asymmetric.util.BaseAgreementSpi;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.spec.DHUParameterSpec;
import org.bouncycastle.jcajce.spec.MQVParameterSpec;
import org.bouncycastle.jcajce.spec.UserKeyingMaterialSpec;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.interfaces.MQVPrivateKey;
import org.bouncycastle.jce.interfaces.MQVPublicKey;
import org.bouncycastle.util.Arrays;

public class KeyAgreementSpi extends BaseAgreementSpi {
    private static final X9IntegerConverter converter = new X9IntegerConverter();
    private Object agreement;
    private DHUParameterSpec dheParameters;
    private String kaAlgorithm;
    private MQVParameterSpec mqvParameters;
    private ECDomainParameters parameters;
    private byte[] result;

    public static class CDHwithSHA1KDFAndSharedInfo extends KeyAgreementSpi {
        public CDHwithSHA1KDFAndSharedInfo() {
            super("ECCDHwithSHA1KDF", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class CDHwithSHA224KDFAndSharedInfo extends KeyAgreementSpi {
        public CDHwithSHA224KDFAndSharedInfo() {
            super("ECCDHwithSHA224KDF", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class CDHwithSHA256KDFAndSharedInfo extends KeyAgreementSpi {
        public CDHwithSHA256KDFAndSharedInfo() {
            super("ECCDHwithSHA256KDF", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class CDHwithSHA384KDFAndSharedInfo extends KeyAgreementSpi {
        public CDHwithSHA384KDFAndSharedInfo() {
            super("ECCDHwithSHA384KDF", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class CDHwithSHA512KDFAndSharedInfo extends KeyAgreementSpi {
        public CDHwithSHA512KDFAndSharedInfo() {
            super("ECCDHwithSHA512KDF", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DH extends KeyAgreementSpi {
        public DH() {
            super("ECDH", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) null);
        }
    }

    public static class DHC extends KeyAgreementSpi {
        public DHC() {
            super("ECDHC", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) null);
        }
    }

    public static class DHUC extends KeyAgreementSpi {
        public DHUC() {
            super("ECCDHU", new ECDHCUnifiedAgreement(), (DerivationFunction) null);
        }
    }

    public static class DHUwithSHA1CKDF extends KeyAgreementSpi {
        public DHUwithSHA1CKDF() {
            super("ECCDHUwithSHA1CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHUwithSHA1KDF extends KeyAgreementSpi {
        public DHUwithSHA1KDF() {
            super("ECCDHUwithSHA1KDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHUwithSHA224CKDF extends KeyAgreementSpi {
        public DHUwithSHA224CKDF() {
            super("ECCDHUwithSHA224CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHUwithSHA224KDF extends KeyAgreementSpi {
        public DHUwithSHA224KDF() {
            super("ECCDHUwithSHA224KDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHUwithSHA256CKDF extends KeyAgreementSpi {
        public DHUwithSHA256CKDF() {
            super("ECCDHUwithSHA256CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHUwithSHA256KDF extends KeyAgreementSpi {
        public DHUwithSHA256KDF() {
            super("ECCDHUwithSHA256KDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHUwithSHA384CKDF extends KeyAgreementSpi {
        public DHUwithSHA384CKDF() {
            super("ECCDHUwithSHA384CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHUwithSHA384KDF extends KeyAgreementSpi {
        public DHUwithSHA384KDF() {
            super("ECCDHUwithSHA384KDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHUwithSHA512CKDF extends KeyAgreementSpi {
        public DHUwithSHA512CKDF() {
            super("ECCDHUwithSHA512CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DHUwithSHA512KDF extends KeyAgreementSpi {
        public DHUwithSHA512KDF() {
            super("ECCDHUwithSHA512KDF", new ECDHCUnifiedAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DHwithSHA1CKDF extends KeyAgreementSpi {
        public DHwithSHA1CKDF() {
            super("ECDHwithSHA1CKDF", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHwithSHA1KDF extends KeyAgreementSpi {
        public DHwithSHA1KDF() {
            super("ECDHwithSHA1KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHwithSHA1KDFAndSharedInfo extends KeyAgreementSpi {
        public DHwithSHA1KDFAndSharedInfo() {
            super("ECDHwithSHA1KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHwithSHA224KDFAndSharedInfo extends KeyAgreementSpi {
        public DHwithSHA224KDFAndSharedInfo() {
            super("ECDHwithSHA224KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHwithSHA256CKDF extends KeyAgreementSpi {
        public DHwithSHA256CKDF() {
            super("ECDHwithSHA256CKDF", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHwithSHA256KDFAndSharedInfo extends KeyAgreementSpi {
        public DHwithSHA256KDFAndSharedInfo() {
            super("ECDHwithSHA256KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHwithSHA384CKDF extends KeyAgreementSpi {
        public DHwithSHA384CKDF() {
            super("ECDHwithSHA384CKDF", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHwithSHA384KDFAndSharedInfo extends KeyAgreementSpi {
        public DHwithSHA384KDFAndSharedInfo() {
            super("ECDHwithSHA384KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHwithSHA512CKDF extends KeyAgreementSpi {
        public DHwithSHA512CKDF() {
            super("ECDHwithSHA512CKDF", (BasicAgreement) new ECDHCBasicAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DHwithSHA512KDFAndSharedInfo extends KeyAgreementSpi {
        public DHwithSHA512KDFAndSharedInfo() {
            super("ECDHwithSHA512KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class ECKAEGwithRIPEMD160KDF extends KeyAgreementSpi {
        public ECKAEGwithRIPEMD160KDF() {
            super("ECKAEGwithRIPEMD160KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(new RIPEMD160Digest()));
        }
    }

    public static class ECKAEGwithSHA1KDF extends KeyAgreementSpi {
        public ECKAEGwithSHA1KDF() {
            super("ECKAEGwithSHA1KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class ECKAEGwithSHA224KDF extends KeyAgreementSpi {
        public ECKAEGwithSHA224KDF() {
            super("ECKAEGwithSHA224KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class ECKAEGwithSHA256KDF extends KeyAgreementSpi {
        public ECKAEGwithSHA256KDF() {
            super("ECKAEGwithSHA256KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class ECKAEGwithSHA384KDF extends KeyAgreementSpi {
        public ECKAEGwithSHA384KDF() {
            super("ECKAEGwithSHA384KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class ECKAEGwithSHA512KDF extends KeyAgreementSpi {
        public ECKAEGwithSHA512KDF() {
            super("ECKAEGwithSHA512KDF", (BasicAgreement) new ECDHBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class MQV extends KeyAgreementSpi {
        public MQV() {
            super("ECMQV", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) null);
        }
    }

    public static class MQVwithSHA1CKDF extends KeyAgreementSpi {
        public MQVwithSHA1CKDF() {
            super("ECMQVwithSHA1CKDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class MQVwithSHA1KDF extends KeyAgreementSpi {
        public MQVwithSHA1KDF() {
            super("ECMQVwithSHA1KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class MQVwithSHA1KDFAndSharedInfo extends KeyAgreementSpi {
        public MQVwithSHA1KDFAndSharedInfo() {
            super("ECMQVwithSHA1KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class MQVwithSHA224CKDF extends KeyAgreementSpi {
        public MQVwithSHA224CKDF() {
            super("ECMQVwithSHA224CKDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class MQVwithSHA224KDF extends KeyAgreementSpi {
        public MQVwithSHA224KDF() {
            super("ECMQVwithSHA224KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class MQVwithSHA224KDFAndSharedInfo extends KeyAgreementSpi {
        public MQVwithSHA224KDFAndSharedInfo() {
            super("ECMQVwithSHA224KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class MQVwithSHA256CKDF extends KeyAgreementSpi {
        public MQVwithSHA256CKDF() {
            super("ECMQVwithSHA256CKDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class MQVwithSHA256KDF extends KeyAgreementSpi {
        public MQVwithSHA256KDF() {
            super("ECMQVwithSHA256KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class MQVwithSHA256KDFAndSharedInfo extends KeyAgreementSpi {
        public MQVwithSHA256KDFAndSharedInfo() {
            super("ECMQVwithSHA256KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class MQVwithSHA384CKDF extends KeyAgreementSpi {
        public MQVwithSHA384CKDF() {
            super("ECMQVwithSHA384CKDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class MQVwithSHA384KDF extends KeyAgreementSpi {
        public MQVwithSHA384KDF() {
            super("ECMQVwithSHA384KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class MQVwithSHA384KDFAndSharedInfo extends KeyAgreementSpi {
        public MQVwithSHA384KDFAndSharedInfo() {
            super("ECMQVwithSHA384KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class MQVwithSHA512CKDF extends KeyAgreementSpi {
        public MQVwithSHA512CKDF() {
            super("ECMQVwithSHA512CKDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new ConcatenationKDFGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class MQVwithSHA512KDF extends KeyAgreementSpi {
        public MQVwithSHA512KDF() {
            super("ECMQVwithSHA512KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class MQVwithSHA512KDFAndSharedInfo extends KeyAgreementSpi {
        public MQVwithSHA512KDFAndSharedInfo() {
            super("ECMQVwithSHA512KDF", (BasicAgreement) new ECMQVBasicAgreement(), (DerivationFunction) new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    protected KeyAgreementSpi(String str, BasicAgreement basicAgreement, DerivationFunction derivationFunction) {
        super(str, derivationFunction);
        this.kaAlgorithm = str;
        this.agreement = basicAgreement;
    }

    protected KeyAgreementSpi(String str, ECDHCUnifiedAgreement eCDHCUnifiedAgreement, DerivationFunction derivationFunction) {
        super(str, derivationFunction);
        this.kaAlgorithm = str;
        this.agreement = eCDHCUnifiedAgreement;
    }

    private static String getSimpleName(Class cls) {
        String name = cls.getName();
        return name.substring(name.lastIndexOf(46) + 1);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: byte[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: org.bouncycastle.crypto.params.ECPublicKeyParameters} */
    /* JADX WARNING: type inference failed for: r1v0 */
    /* JADX WARNING: type inference failed for: r1v8 */
    /* JADX WARNING: type inference failed for: r1v12 */
    /* JADX WARNING: type inference failed for: r1v15 */
    /* JADX WARNING: type inference failed for: r1v16 */
    /* JADX WARNING: type inference failed for: r1v18 */
    /* JADX WARNING: type inference failed for: r1v20 */
    /* JADX WARNING: Multi-variable type inference failed */
    private void initFromKey(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        ECPrivateKeyParameters eCPrivateKeyParameters;
        ECPublicKeyParameters eCPublicKeyParameters;
        ECPrivateKeyParameters eCPrivateKeyParameters2;
        ? r1 = 0;
        if (this.agreement instanceof ECMQVBasicAgreement) {
            this.mqvParameters = null;
            boolean z = key instanceof MQVPrivateKey;
            if (z || (algorithmParameterSpec instanceof MQVParameterSpec)) {
                if (z) {
                    MQVPrivateKey mQVPrivateKey = (MQVPrivateKey) key;
                    ECPrivateKeyParameters eCPrivateKeyParameters3 = (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(mQVPrivateKey.getStaticPrivateKey());
                    eCPrivateKeyParameters2 = (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(mQVPrivateKey.getEphemeralPrivateKey());
                    if (mQVPrivateKey.getEphemeralPublicKey() != null) {
                        r1 = (ECPublicKeyParameters) ECUtils.generatePublicKeyParameter(mQVPrivateKey.getEphemeralPublicKey());
                    }
                    eCPrivateKeyParameters = eCPrivateKeyParameters3;
                    eCPublicKeyParameters = r1;
                } else {
                    MQVParameterSpec mQVParameterSpec = (MQVParameterSpec) algorithmParameterSpec;
                    eCPrivateKeyParameters = (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter((PrivateKey) key);
                    eCPrivateKeyParameters2 = (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(mQVParameterSpec.getEphemeralPrivateKey());
                    if (mQVParameterSpec.getEphemeralPublicKey() != null) {
                        r1 = (ECPublicKeyParameters) ECUtils.generatePublicKeyParameter(mQVParameterSpec.getEphemeralPublicKey());
                    }
                    this.mqvParameters = mQVParameterSpec;
                    this.ukmParameters = mQVParameterSpec.getUserKeyingMaterial();
                    eCPublicKeyParameters = r1;
                }
                MQVPrivateParameters mQVPrivateParameters = new MQVPrivateParameters(eCPrivateKeyParameters, eCPrivateKeyParameters2, eCPublicKeyParameters);
                this.parameters = eCPrivateKeyParameters.getParameters();
                ((ECMQVBasicAgreement) this.agreement).init(mQVPrivateParameters);
                return;
            }
            throw new InvalidAlgorithmParameterException(this.kaAlgorithm + " key agreement requires " + getSimpleName(MQVParameterSpec.class) + " for initialisation");
        } else if (algorithmParameterSpec instanceof DHUParameterSpec) {
            if (this.agreement instanceof ECDHCUnifiedAgreement) {
                DHUParameterSpec dHUParameterSpec = (DHUParameterSpec) algorithmParameterSpec;
                ECPrivateKeyParameters eCPrivateKeyParameters4 = (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter((PrivateKey) key);
                ECPrivateKeyParameters eCPrivateKeyParameters5 = (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(dHUParameterSpec.getEphemeralPrivateKey());
                if (dHUParameterSpec.getEphemeralPublicKey() != null) {
                    r1 = (ECPublicKeyParameters) ECUtils.generatePublicKeyParameter(dHUParameterSpec.getEphemeralPublicKey());
                }
                this.dheParameters = dHUParameterSpec;
                this.ukmParameters = dHUParameterSpec.getUserKeyingMaterial();
                ECDHUPrivateParameters eCDHUPrivateParameters = new ECDHUPrivateParameters(eCPrivateKeyParameters4, eCPrivateKeyParameters5, r1);
                this.parameters = eCPrivateKeyParameters4.getParameters();
                ((ECDHCUnifiedAgreement) this.agreement).init(eCDHUPrivateParameters);
                return;
            }
            throw new InvalidAlgorithmParameterException(this.kaAlgorithm + " key agreement cannot be used with " + getSimpleName(DHUParameterSpec.class));
        } else if (!(key instanceof PrivateKey)) {
            throw new InvalidKeyException(this.kaAlgorithm + " key agreement requires " + getSimpleName(ECPrivateKey.class) + " for initialisation");
        } else if (this.kdf != null || !(algorithmParameterSpec instanceof UserKeyingMaterialSpec)) {
            ECPrivateKeyParameters eCPrivateKeyParameters6 = (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter((PrivateKey) key);
            this.parameters = eCPrivateKeyParameters6.getParameters();
            if (algorithmParameterSpec instanceof UserKeyingMaterialSpec) {
                r1 = ((UserKeyingMaterialSpec) algorithmParameterSpec).getUserKeyingMaterial();
            }
            this.ukmParameters = r1;
            ((BasicAgreement) this.agreement).init(eCPrivateKeyParameters6);
        } else {
            throw new InvalidAlgorithmParameterException("no KDF specified for UserKeyingMaterialSpec");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] bigIntToBytes(BigInteger bigInteger) {
        return converter.integerToBytes(bigInteger, converter.getByteLength(this.parameters.getCurve()));
    }

    /* access modifiers changed from: protected */
    public byte[] calcSecret() {
        return Arrays.clone(this.result);
    }

    /* access modifiers changed from: protected */
    public Key engineDoPhase(Key key, boolean z) throws InvalidKeyException, IllegalStateException {
        CipherParameters cipherParameters;
        if (this.parameters == null) {
            throw new IllegalStateException(this.kaAlgorithm + " not initialised.");
        } else if (z) {
            if (this.agreement instanceof ECMQVBasicAgreement) {
                if (!(key instanceof MQVPublicKey)) {
                    cipherParameters = new MQVPublicParameters((ECPublicKeyParameters) ECUtils.generatePublicKeyParameter((PublicKey) key), (ECPublicKeyParameters) ECUtils.generatePublicKeyParameter(this.mqvParameters.getOtherPartyEphemeralKey()));
                } else {
                    MQVPublicKey mQVPublicKey = (MQVPublicKey) key;
                    cipherParameters = new MQVPublicParameters((ECPublicKeyParameters) ECUtils.generatePublicKeyParameter(mQVPublicKey.getStaticKey()), (ECPublicKeyParameters) ECUtils.generatePublicKeyParameter(mQVPublicKey.getEphemeralKey()));
                }
            } else if (this.agreement instanceof ECDHCUnifiedAgreement) {
                cipherParameters = new ECDHUPublicParameters((ECPublicKeyParameters) ECUtils.generatePublicKeyParameter((PublicKey) key), (ECPublicKeyParameters) ECUtils.generatePublicKeyParameter(this.dheParameters.getOtherPartyEphemeralKey()));
            } else if (key instanceof PublicKey) {
                cipherParameters = ECUtils.generatePublicKeyParameter((PublicKey) key);
            } else {
                throw new InvalidKeyException(this.kaAlgorithm + " key agreement requires " + getSimpleName(ECPublicKey.class) + " for doPhase");
            }
            try {
                this.result = this.agreement instanceof BasicAgreement ? bigIntToBytes(((BasicAgreement) this.agreement).calculateAgreement(cipherParameters)) : ((ECDHCUnifiedAgreement) this.agreement).calculateAgreement(cipherParameters);
                return null;
            } catch (Exception e) {
                throw new InvalidKeyException("calculation failed: " + e.getMessage()) {
                    public Throwable getCause() {
                        return e;
                    }
                };
            }
        } else {
            throw new IllegalStateException(this.kaAlgorithm + " can only be between two parties.");
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(Key key, SecureRandom secureRandom) throws InvalidKeyException {
        try {
            initFromKey(key, null);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (algorithmParameterSpec == null || (algorithmParameterSpec instanceof MQVParameterSpec) || (algorithmParameterSpec instanceof UserKeyingMaterialSpec) || (algorithmParameterSpec instanceof DHUParameterSpec)) {
            initFromKey(key, algorithmParameterSpec);
            return;
        }
        throw new InvalidAlgorithmParameterException("No algorithm parameters supported");
    }
}
