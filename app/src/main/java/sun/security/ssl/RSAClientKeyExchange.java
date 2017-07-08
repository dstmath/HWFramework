package sun.security.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLProtocolException;
import sun.security.internal.spec.TlsRsaPremasterSecretParameterSpec;
import sun.security.util.KeyUtil;

final class RSAClientKeyExchange extends HandshakeMessage {
    private static final String PROP_NAME = "com.sun.net.ssl.rsaPreMasterSecretFix";
    private static final boolean rsaPreMasterSecretFix = false;
    private byte[] encrypted;
    SecretKey preMaster;
    private ProtocolVersion protocolVersion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.RSAClientKeyExchange.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.RSAClientKeyExchange.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.RSAClientKeyExchange.<clinit>():void");
    }

    RSAClientKeyExchange(ProtocolVersion protocolVersion, ProtocolVersion maxVersion, SecureRandom generator, PublicKey publicKey) throws IOException {
        if (publicKey.getAlgorithm().equals("RSA")) {
            int major;
            int minor;
            this.protocolVersion = protocolVersion;
            if (rsaPreMasterSecretFix || maxVersion.v >= ProtocolVersion.TLS11.v) {
                major = maxVersion.major;
                minor = maxVersion.minor;
            } else {
                major = protocolVersion.major;
                minor = protocolVersion.minor;
            }
            try {
                KeyGenerator kg = JsseJce.getKeyGenerator(protocolVersion.v >= ProtocolVersion.TLS12.v ? "SunTls12RsaPremasterSecret" : "SunTlsRsaPremasterSecret");
                kg.init(new TlsRsaPremasterSecretParameterSpec(major, minor), generator);
                this.preMaster = kg.generateKey();
                Cipher cipher = JsseJce.getCipher("RSA/ECB/PKCS1Padding");
                cipher.init(3, (Key) publicKey, generator);
                this.encrypted = cipher.wrap(this.preMaster);
                return;
            } catch (GeneralSecurityException e) {
                throw ((SSLKeyException) new SSLKeyException("RSA premaster secret error").initCause(e));
            }
        }
        throw new SSLKeyException("Public key not of type RSA");
    }

    RSAClientKeyExchange(ProtocolVersion currentVersion, ProtocolVersion maxVersion, SecureRandom generator, HandshakeInStream input, int messageSize, PrivateKey privateKey) throws IOException {
        if (privateKey.getAlgorithm().equals("RSA")) {
            if (currentVersion.v >= ProtocolVersion.TLS10.v) {
                this.encrypted = input.getBytes16();
            } else {
                this.encrypted = new byte[messageSize];
                if (input.read(this.encrypted) != messageSize) {
                    throw new SSLProtocolException("SSL: read PreMasterSecret: short read");
                }
            }
            try {
                Cipher cipher = JsseJce.getCipher("RSA/ECB/PKCS1Padding");
                cipher.init(4, (Key) privateKey);
                this.preMaster = (SecretKey) cipher.unwrap(this.encrypted, "TlsRsaPremasterSecret", 3);
                this.preMaster = polishPreMasterSecretKey(currentVersion, maxVersion, generator, this.preMaster, null);
                return;
            } catch (Exception e) {
                this.preMaster = polishPreMasterSecretKey(currentVersion, maxVersion, generator, null, e);
                return;
            }
        }
        throw new SSLKeyException("Private key not of type RSA");
    }

    private SecretKey polishPreMasterSecretKey(ProtocolVersion currentVersion, ProtocolVersion clientHelloVersion, SecureRandom generator, SecretKey secretKey, Exception failoverException) {
        this.protocolVersion = clientHelloVersion;
        if (failoverException != null || secretKey == null) {
            if (!(debug == null || !Debug.isOn("handshake") || failoverException == null)) {
                System.out.println("Error decrypting premaster secret:");
                failoverException.printStackTrace(System.out);
            }
            return generateDummySecret(clientHelloVersion);
        }
        byte[] encoded = secretKey.getEncoded();
        if (encoded == null) {
            if (debug != null && Debug.isOn("handshake")) {
                System.out.println("unable to get the plaintext of the premaster secret");
            }
            int keySize = KeyUtil.getKeySize(secretKey);
            if (keySize <= 0 || keySize == 384) {
                return secretKey;
            }
            if (debug != null && Debug.isOn("handshake")) {
                System.out.println("incorrect length of premaster secret: " + (keySize / 8));
            }
            return generateDummySecret(clientHelloVersion);
        } else if (encoded.length != 48) {
            if (debug != null && Debug.isOn("handshake")) {
                System.out.println("incorrect length of premaster secret: " + encoded.length);
            }
            return generateDummySecret(clientHelloVersion);
        } else if (clientHelloVersion.major == encoded[0] && clientHelloVersion.minor == encoded[1]) {
            return secretKey;
        } else {
            if (clientHelloVersion.v <= ProtocolVersion.TLS10.v && currentVersion.major == encoded[0] && currentVersion.minor == encoded[1]) {
                this.protocolVersion = currentVersion;
                return secretKey;
            }
            if (debug != null && Debug.isOn("handshake")) {
                System.out.println("Mismatching Protocol Versions, ClientHello.client_version is " + clientHelloVersion + ", while PreMasterSecret.client_version is " + ProtocolVersion.valueOf(encoded[0], encoded[1]));
            }
            return generateDummySecret(clientHelloVersion);
        }
    }

    static SecretKey generateDummySecret(ProtocolVersion version) {
        if (debug != null && Debug.isOn("handshake")) {
            System.out.println("Generating a random fake premaster secret");
        }
        try {
            KeyGenerator kg = JsseJce.getKeyGenerator(version.v >= ProtocolVersion.TLS12.v ? "SunTls12RsaPremasterSecret" : "SunTlsRsaPremasterSecret");
            kg.init(new TlsRsaPremasterSecretParameterSpec(version.major, version.minor));
            return kg.generateKey();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate dummy secret", e);
        }
    }

    int messageType() {
        return 16;
    }

    int messageLength() {
        if (this.protocolVersion.v >= ProtocolVersion.TLS10.v) {
            return this.encrypted.length + 2;
        }
        return this.encrypted.length;
    }

    void send(HandshakeOutStream s) throws IOException {
        if (this.protocolVersion.v >= ProtocolVersion.TLS10.v) {
            s.putBytes16(this.encrypted);
        } else {
            s.write(this.encrypted);
        }
    }

    void print(PrintStream s) throws IOException {
        s.println("*** ClientKeyExchange, RSA PreMasterSecret, " + this.protocolVersion);
    }
}
