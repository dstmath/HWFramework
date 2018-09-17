package com.android.org.bouncycastle.jcajce.provider.symmetric;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.EncryptionScheme;
import com.android.org.bouncycastle.asn1.pkcs.KeyDerivationFunc;
import com.android.org.bouncycastle.asn1.pkcs.PBES2Parameters;
import com.android.org.bouncycastle.asn1.pkcs.PBKDF2Params;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseAlgorithmParameters;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util;
import com.android.org.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Enumeration;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;

public class PBES2AlgorithmParameters {

    private static abstract class BasePBEWithHmacAlgorithmParameters extends BaseAlgorithmParameters {
        private final ASN1ObjectIdentifier cipherAlgorithm;
        private final String cipherAlgorithmShortName;
        private final AlgorithmIdentifier kdf;
        private final String kdfShortName;
        private final int keySize;
        private PBES2Parameters params;

        /* synthetic */ BasePBEWithHmacAlgorithmParameters(ASN1ObjectIdentifier kdf, String kdfShortName, int keySize, ASN1ObjectIdentifier cipherAlgorithm, String cipherAlgorithmShortName, BasePBEWithHmacAlgorithmParameters -this5) {
            this(kdf, kdfShortName, keySize, cipherAlgorithm, cipherAlgorithmShortName);
        }

        private BasePBEWithHmacAlgorithmParameters(ASN1ObjectIdentifier kdf, String kdfShortName, int keySize, ASN1ObjectIdentifier cipherAlgorithm, String cipherAlgorithmShortName) {
            this.kdf = new AlgorithmIdentifier(kdf, DERNull.INSTANCE);
            this.kdfShortName = kdfShortName;
            this.keySize = keySize;
            this.cipherAlgorithm = cipherAlgorithm;
            this.cipherAlgorithmShortName = cipherAlgorithmShortName;
        }

        protected byte[] engineGetEncoded() {
            try {
                return new DERSequence(new ASN1Encodable[]{PKCSObjectIdentifiers.id_PBES2, this.params}).getEncoded();
            } catch (IOException e) {
                throw new RuntimeException("Unable to read PBES2 parameters: " + e.toString());
            }
        }

        protected byte[] engineGetEncoded(String format) {
            if (isASN1FormatString(format)) {
                return engineGetEncoded();
            }
            return null;
        }

        protected AlgorithmParameterSpec localEngineGetParameterSpec(Class parameterSpec) throws InvalidParameterSpecException {
            if (parameterSpec == PBEParameterSpec.class) {
                PBKDF2Params pbeParamSpec = (PBKDF2Params) this.params.getKeyDerivationFunc().getParameters();
                return PBES2AlgorithmParameters.createPBEParameterSpec(pbeParamSpec.getSalt(), pbeParamSpec.getIterationCount().intValue(), ((ASN1OctetString) this.params.getEncryptionScheme().getParameters()).getOctets());
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to PBES2 parameters object.");
        }

        protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
            if (paramSpec instanceof PBEParameterSpec) {
                PBEParameterSpec pbeSpec = (PBEParameterSpec) paramSpec;
                AlgorithmParameterSpec algorithmParameterSpec = Util.getParameterSpecFromPBEParameterSpec(pbeSpec);
                if (algorithmParameterSpec instanceof IvParameterSpec) {
                    this.params = new PBES2Parameters(new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(pbeSpec.getSalt(), pbeSpec.getIterationCount(), this.keySize, this.kdf)), new EncryptionScheme(this.cipherAlgorithm, new DEROctetString(((IvParameterSpec) algorithmParameterSpec).getIV())));
                    return;
                }
                throw new IllegalArgumentException("Expecting an IV as a parameter");
            }
            throw new InvalidParameterSpecException("PBEParameterSpec required to initialise PBES2 algorithm parameters");
        }

        protected void engineInit(byte[] params) throws IOException {
            Enumeration seqObjects = ASN1Sequence.getInstance(ASN1Primitive.fromByteArray(params)).getObjects();
            if (((ASN1ObjectIdentifier) seqObjects.nextElement()).getId().equals(PKCSObjectIdentifiers.id_PBES2.getId())) {
                this.params = PBES2Parameters.getInstance(seqObjects.nextElement());
                return;
            }
            throw new IllegalArgumentException("Invalid PBES2 parameters");
        }

        protected void engineInit(byte[] params, String format) throws IOException {
            if (isASN1FormatString(format)) {
                engineInit(params);
                return;
            }
            throw new IOException("Unknown parameters format in PBES2 parameters object");
        }

        protected String engineToString() {
            return "PBES2 " + this.kdfShortName + " " + this.cipherAlgorithmShortName + " Parameters";
        }
    }

    public static class Mappings extends AlgorithmProvider {
        private static final String PREFIX = PBES2AlgorithmParameters.class.getName();

        public void configure(ConfigurableProvider provider) {
            int[] shaVariants = new int[]{1, 224, 256, 384, 512};
            for (int keySize : new int[]{128, 256}) {
                for (int shaVariant : shaVariants) {
                    provider.addAlgorithm("AlgorithmParameters.PBEWithHmacSHA" + shaVariant + "AndAES_" + keySize, PREFIX + "$PBEWithHmacSHA" + shaVariant + "AES" + keySize + "AlgorithmParameters");
                }
            }
        }
    }

    public static class PBEWithHmacSHA1AES128AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA1AES128AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA1, "HmacSHA1", 16, NISTObjectIdentifiers.id_aes128_CBC, "AES128", null);
        }
    }

    public static class PBEWithHmacSHA1AES256AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA1AES256AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA1, "HmacSHA1", 32, NISTObjectIdentifiers.id_aes256_CBC, "AES256", null);
        }
    }

    public static class PBEWithHmacSHA224AES128AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA224AES128AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA224, "HmacSHA224", 16, NISTObjectIdentifiers.id_aes128_CBC, "AES128", null);
        }
    }

    public static class PBEWithHmacSHA224AES256AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA224AES256AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA224, "HmacSHA224", 32, NISTObjectIdentifiers.id_aes256_CBC, "AES256", null);
        }
    }

    public static class PBEWithHmacSHA256AES128AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA256AES128AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA256, "HmacSHA256", 16, NISTObjectIdentifiers.id_aes128_CBC, "AES128", null);
        }
    }

    public static class PBEWithHmacSHA256AES256AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA256AES256AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA256, "HmacSHA256", 32, NISTObjectIdentifiers.id_aes256_CBC, "AES256", null);
        }
    }

    public static class PBEWithHmacSHA384AES128AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA384AES128AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA384, "HmacSHA384", 16, NISTObjectIdentifiers.id_aes128_CBC, "AES128", null);
        }
    }

    public static class PBEWithHmacSHA384AES256AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA384AES256AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA384, "HmacSHA384", 32, NISTObjectIdentifiers.id_aes256_CBC, "AES256", null);
        }
    }

    public static class PBEWithHmacSHA512AES128AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA512AES128AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA512, "HmacSHA512", 16, NISTObjectIdentifiers.id_aes128_CBC, "AES128", null);
        }
    }

    public static class PBEWithHmacSHA512AES256AlgorithmParameters extends BasePBEWithHmacAlgorithmParameters {
        public PBEWithHmacSHA512AES256AlgorithmParameters() {
            super(PKCSObjectIdentifiers.id_hmacWithSHA512, "HmacSHA512", 32, NISTObjectIdentifiers.id_aes256_CBC, "AES256", null);
        }
    }

    private PBES2AlgorithmParameters() {
    }

    private static PBEParameterSpec createPBEParameterSpec(byte[] salt, int iterationCount, byte[] iv) {
        try {
            return (PBEParameterSpec) PBES2AlgorithmParameters.class.getClassLoader().loadClass("javax.crypto.spec.PBEParameterSpec").getConstructor(new Class[]{byte[].class, Integer.TYPE, AlgorithmParameterSpec.class}).newInstance(new Object[]{salt, Integer.valueOf(iterationCount), new IvParameterSpec(iv)});
        } catch (Exception e) {
            throw new IllegalStateException("Requested creation PBES2 parameters in an SDK that doesn't support them", e);
        }
    }
}
