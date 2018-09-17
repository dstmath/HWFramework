package sun.security.ssl;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.net.ssl.SSLHandshakeException;
import sun.security.util.KeyUtil;

final class DHCrypt {
    private static int MAX_FAILOVER_TIMES;
    private BigInteger base;
    private BigInteger modulus;
    private PrivateKey privateKey;
    private BigInteger publicValue;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.DHCrypt.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.DHCrypt.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.DHCrypt.<clinit>():void");
    }

    DHCrypt(int keyLength, SecureRandom random) {
        try {
            KeyPairGenerator kpg = JsseJce.getKeyPairGenerator("DiffieHellman");
            kpg.initialize(keyLength, random);
            DHPublicKeySpec spec = generateDHPublicKeySpec(kpg);
            if (spec == null) {
                throw new RuntimeException("Could not generate DH keypair");
            }
            this.publicValue = spec.getY();
            this.modulus = spec.getP();
            this.base = spec.getG();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate DH keypair", e);
        }
    }

    DHCrypt(BigInteger modulus, BigInteger base, SecureRandom random) {
        this.modulus = modulus;
        this.base = base;
        try {
            KeyPairGenerator kpg = JsseJce.getKeyPairGenerator("DiffieHellman");
            kpg.initialize(new DHParameterSpec(modulus, base), random);
            DHPublicKeySpec spec = generateDHPublicKeySpec(kpg);
            if (spec == null) {
                throw new RuntimeException("Could not generate DH keypair");
            }
            this.publicValue = spec.getY();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate DH keypair", e);
        }
    }

    static DHPublicKeySpec getDHPublicKeySpec(PublicKey key) {
        if (key instanceof DHPublicKey) {
            DHPublicKey dhKey = (DHPublicKey) key;
            DHParameterSpec params = dhKey.getParams();
            return new DHPublicKeySpec(dhKey.getY(), params.getP(), params.getG());
        }
        try {
            return (DHPublicKeySpec) JsseJce.getKeyFactory("DH").getKeySpec(key, DHPublicKeySpec.class);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    BigInteger getModulus() {
        return this.modulus;
    }

    BigInteger getBase() {
        return this.base;
    }

    BigInteger getPublicKey() {
        return this.publicValue;
    }

    SecretKey getAgreedSecret(BigInteger peerPublicValue, boolean keyIsValidated) throws IOException {
        try {
            KeyFactory kf = JsseJce.getKeyFactory("DiffieHellman");
            KeySpec spec = new DHPublicKeySpec(peerPublicValue, this.modulus, this.base);
            PublicKey publicKey = kf.generatePublic(spec);
            KeyAgreement ka = JsseJce.getKeyAgreement("DiffieHellman");
            if (!(keyIsValidated || KeyUtil.isOracleJCEProvider(ka.getProvider().getName()))) {
                KeyUtil.validate(spec);
            }
            ka.init(this.privateKey);
            ka.doPhase(publicKey, true);
            return ka.generateSecret("TlsPremasterSecret");
        } catch (InvalidKeyException ike) {
            throw new SSLHandshakeException(ike.getMessage());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate secret", e);
        }
    }

    private DHPublicKeySpec generateDHPublicKeySpec(KeyPairGenerator kpg) throws GeneralSecurityException {
        boolean doExtraValiadtion = !KeyUtil.isOracleJCEProvider(kpg.getProvider().getName());
        int i = 0;
        while (i <= MAX_FAILOVER_TIMES) {
            KeyPair kp = kpg.generateKeyPair();
            this.privateKey = kp.getPrivate();
            KeySpec spec = getDHPublicKeySpec(kp.getPublic());
            if (doExtraValiadtion) {
                try {
                    KeyUtil.validate(spec);
                } catch (InvalidKeyException ivke) {
                    if (i == MAX_FAILOVER_TIMES) {
                        throw ivke;
                    }
                    i++;
                }
            }
            return spec;
        }
        return null;
    }
}
