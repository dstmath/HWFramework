package sun.security.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.DigestException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHPublicKeySpec;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLProtocolException;
import javax.security.auth.x500.X500Principal;
import sun.security.internal.spec.TlsPrfParameterSpec;
import sun.security.util.KeyUtil;

public abstract class HandshakeMessage {
    static final byte[] MD5_pad1 = null;
    static final byte[] MD5_pad2 = null;
    static final byte[] SHA_pad1 = null;
    static final byte[] SHA_pad2 = null;
    public static final Debug debug = null;
    static final byte ht_certificate = (byte) 11;
    static final byte ht_certificate_request = (byte) 13;
    static final byte ht_certificate_verify = (byte) 15;
    static final byte ht_client_hello = (byte) 1;
    static final byte ht_client_key_exchange = (byte) 16;
    static final byte ht_finished = (byte) 20;
    static final byte ht_hello_request = (byte) 0;
    static final byte ht_server_hello = (byte) 2;
    static final byte ht_server_hello_done = (byte) 14;
    static final byte ht_server_key_exchange = (byte) 12;

    static final class CertificateMsg extends HandshakeMessage {
        private X509Certificate[] chain;
        private List<byte[]> encodedChain;
        private int messageLength;

        int messageType() {
            return 11;
        }

        CertificateMsg(X509Certificate[] certs) {
            this.chain = certs;
        }

        CertificateMsg(HandshakeInStream input) throws IOException {
            int chainLen = input.getInt24();
            List<Certificate> v = new ArrayList(4);
            CertificateFactory certificateFactory = null;
            while (chainLen > 0) {
                byte[] cert = input.getBytes24();
                chainLen -= cert.length + 3;
                if (certificateFactory == null) {
                    try {
                        certificateFactory = CertificateFactory.getInstance("X.509");
                    } catch (CertificateException e) {
                        throw ((SSLProtocolException) new SSLProtocolException(e.getMessage()).initCause(e));
                    }
                }
                v.add(certificateFactory.generateCertificate(new ByteArrayInputStream(cert)));
            }
            this.chain = (X509Certificate[]) v.toArray(new X509Certificate[v.size()]);
        }

        int messageLength() {
            if (this.encodedChain == null) {
                this.messageLength = 3;
                this.encodedChain = new ArrayList(this.chain.length);
                try {
                    for (X509Certificate cert : this.chain) {
                        byte[] b = cert.getEncoded();
                        this.encodedChain.add(b);
                        this.messageLength += b.length + 3;
                    }
                } catch (CertificateEncodingException e) {
                    this.encodedChain = null;
                    throw new RuntimeException("Could not encode certificates", e);
                }
            }
            return this.messageLength;
        }

        void send(HandshakeOutStream s) throws IOException {
            s.putInt24(messageLength() - 3);
            for (byte[] b : this.encodedChain) {
                s.putBytes24(b);
            }
        }

        void print(PrintStream s) throws IOException {
            s.println("*** Certificate chain");
            if (debug != null && Debug.isOn("verbose")) {
                for (int i = 0; i < this.chain.length; i++) {
                    s.println("chain [" + i + "] = " + this.chain[i]);
                }
                s.println("***");
            }
        }

        X509Certificate[] getCertificateChain() {
            return (X509Certificate[]) this.chain.clone();
        }
    }

    static final class CertificateRequest extends HandshakeMessage {
        private static final byte[] TYPES_ECC = null;
        private static final byte[] TYPES_NO_ECC = null;
        static final int cct_dss_ephemeral_dh = 6;
        static final int cct_dss_fixed_dh = 4;
        static final int cct_dss_sign = 2;
        static final int cct_ecdsa_fixed_ecdh = 66;
        static final int cct_ecdsa_sign = 64;
        static final int cct_rsa_ephemeral_dh = 5;
        static final int cct_rsa_fixed_dh = 3;
        static final int cct_rsa_fixed_ecdh = 65;
        static final int cct_rsa_sign = 1;
        private Collection<SignatureAndHashAlgorithm> algorithms;
        private int algorithmsLen;
        DistinguishedName[] authorities;
        ProtocolVersion protocolVersion;
        byte[] types;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.HandshakeMessage.CertificateRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.HandshakeMessage.CertificateRequest.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.HandshakeMessage.CertificateRequest.<clinit>():void");
        }

        CertificateRequest(X509Certificate[] ca, KeyExchange keyExchange, Collection<SignatureAndHashAlgorithm> signAlgs, ProtocolVersion protocolVersion) throws IOException {
            this.protocolVersion = protocolVersion;
            this.authorities = new DistinguishedName[ca.length];
            for (int i = 0; i < ca.length; i += cct_rsa_sign) {
                this.authorities[i] = new DistinguishedName(ca[i].getSubjectX500Principal());
            }
            this.types = JsseJce.isEcAvailable() ? TYPES_ECC : TYPES_NO_ECC;
            if (protocolVersion.v < ProtocolVersion.TLS12.v) {
                this.algorithms = new ArrayList();
                this.algorithmsLen = 0;
            } else if (signAlgs == null || signAlgs.isEmpty()) {
                throw new SSLProtocolException("No supported signature algorithms");
            } else {
                this.algorithms = new ArrayList((Collection) signAlgs);
                this.algorithmsLen = SignatureAndHashAlgorithm.sizeInRecord() * this.algorithms.size();
            }
        }

        CertificateRequest(HandshakeInStream input, ProtocolVersion protocolVersion) throws IOException {
            this.protocolVersion = protocolVersion;
            this.types = input.getBytes8();
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                this.algorithmsLen = input.getInt16();
                if (this.algorithmsLen < cct_dss_sign) {
                    throw new SSLProtocolException("Invalid supported_signature_algorithms field");
                }
                this.algorithms = new ArrayList();
                int remains = this.algorithmsLen;
                int sequence = 0;
                while (remains > cct_rsa_sign) {
                    sequence += cct_rsa_sign;
                    this.algorithms.add(SignatureAndHashAlgorithm.valueOf(input.getInt8(), input.getInt8(), sequence));
                    remains -= 2;
                }
                if (remains != 0) {
                    throw new SSLProtocolException("Invalid supported_signature_algorithms field");
                }
            }
            this.algorithms = new ArrayList();
            this.algorithmsLen = 0;
            int len = input.getInt16();
            ArrayList<DistinguishedName> v = new ArrayList();
            while (len >= cct_rsa_fixed_dh) {
                DistinguishedName dn = new DistinguishedName(input);
                v.add(dn);
                len -= dn.length();
            }
            if (len != 0) {
                throw new SSLProtocolException("Bad CertificateRequest DN length");
            }
            this.authorities = (DistinguishedName[]) v.toArray(new DistinguishedName[v.size()]);
        }

        X500Principal[] getAuthorities() throws IOException {
            X500Principal[] ret = new X500Principal[this.authorities.length];
            for (int i = 0; i < this.authorities.length; i += cct_rsa_sign) {
                ret[i] = this.authorities[i].getX500Principal();
            }
            return ret;
        }

        Collection<SignatureAndHashAlgorithm> getSignAlgorithms() {
            return this.algorithms;
        }

        int messageType() {
            return 13;
        }

        int messageLength() {
            int len = (this.types.length + cct_rsa_sign) + cct_dss_sign;
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                len += this.algorithmsLen + cct_dss_sign;
            }
            for (int i = 0; i < this.authorities.length; i += cct_rsa_sign) {
                len += this.authorities[i].length();
            }
            return len;
        }

        void send(HandshakeOutStream output) throws IOException {
            int i;
            output.putBytes8(this.types);
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                output.putInt16(this.algorithmsLen);
                for (SignatureAndHashAlgorithm algorithm : this.algorithms) {
                    output.putInt8(algorithm.getHashValue());
                    output.putInt8(algorithm.getSignatureValue());
                }
            }
            int len = 0;
            for (i = 0; i < this.authorities.length; i += cct_rsa_sign) {
                len += this.authorities[i].length();
            }
            output.putInt16(len);
            for (i = 0; i < this.authorities.length; i += cct_rsa_sign) {
                this.authorities[i].send(output);
            }
        }

        void print(PrintStream s) throws IOException {
            s.println("*** CertificateRequest");
            if (debug != null && Debug.isOn("verbose")) {
                int i;
                s.print("Cert Types: ");
                for (i = 0; i < this.types.length; i += cct_rsa_sign) {
                    switch (this.types[i]) {
                        case cct_rsa_sign /*1*/:
                            s.print("RSA");
                            break;
                        case cct_dss_sign /*2*/:
                            s.print("DSS");
                            break;
                        case cct_rsa_fixed_dh /*3*/:
                            s.print("Fixed DH (RSA sig)");
                            break;
                        case cct_dss_fixed_dh /*4*/:
                            s.print("Fixed DH (DSS sig)");
                            break;
                        case cct_rsa_ephemeral_dh /*5*/:
                            s.print("Ephemeral DH (RSA sig)");
                            break;
                        case cct_dss_ephemeral_dh /*6*/:
                            s.print("Ephemeral DH (DSS sig)");
                            break;
                        case cct_ecdsa_sign /*64*/:
                            s.print("ECDSA");
                            break;
                        case cct_rsa_fixed_ecdh /*65*/:
                            s.print("Fixed ECDH (RSA sig)");
                            break;
                        case cct_ecdsa_fixed_ecdh /*66*/:
                            s.print("Fixed ECDH (ECDSA sig)");
                            break;
                        default:
                            s.print("Type-" + (this.types[i] & 255));
                            break;
                    }
                    if (i != this.types.length - 1) {
                        s.print(", ");
                    }
                }
                s.println();
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    Object buffer = new StringBuffer();
                    boolean opened = false;
                    for (SignatureAndHashAlgorithm signAlg : this.algorithms) {
                        if (opened) {
                            buffer.append(", " + signAlg.getAlgorithmName());
                        } else {
                            buffer.append(signAlg.getAlgorithmName());
                            opened = true;
                        }
                    }
                    s.println("Supported Signature Algorithms: " + buffer);
                }
                s.println("Cert Authorities:");
                if (this.authorities.length == 0) {
                    s.println("<Empty>");
                    return;
                }
                for (i = 0; i < this.authorities.length; i += cct_rsa_sign) {
                    this.authorities[i].print(s);
                }
            }
        }
    }

    static final class CertificateVerify extends HandshakeMessage {
        private static final Object NULL_OBJECT = null;
        private static final Class delegate = null;
        private static final Map<Class, Object> methodCache = null;
        private static final Field spiField = null;
        private SignatureAndHashAlgorithm preferableSignatureAlgorithm;
        ProtocolVersion protocolVersion;
        private byte[] signature;

        /* renamed from: sun.security.ssl.HandshakeMessage.CertificateVerify.1 */
        static class AnonymousClass1 implements PrivilegedAction<Object> {
            final /* synthetic */ AccessibleObject val$o;

            AnonymousClass1(AccessibleObject val$o) {
                this.val$o = val$o;
            }

            public Object run() {
                this.val$o.setAccessible(true);
                return null;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.HandshakeMessage.CertificateVerify.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.HandshakeMessage.CertificateVerify.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.HandshakeMessage.CertificateVerify.<clinit>():void");
        }

        CertificateVerify(ProtocolVersion protocolVersion, HandshakeHash handshakeHash, PrivateKey privateKey, SecretKey masterSecret, SecureRandom sr, SignatureAndHashAlgorithm signAlgorithm) throws GeneralSecurityException {
            Signature sig;
            this.preferableSignatureAlgorithm = null;
            this.protocolVersion = protocolVersion;
            String algorithm = privateKey.getAlgorithm();
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                this.preferableSignatureAlgorithm = signAlgorithm;
                sig = JsseJce.getSignature(signAlgorithm.getAlgorithmName());
            } else {
                sig = getSignature(protocolVersion, algorithm);
            }
            sig.initSign(privateKey, sr);
            updateSignature(sig, protocolVersion, handshakeHash, algorithm, masterSecret);
            this.signature = sig.sign();
        }

        CertificateVerify(HandshakeInStream input, Collection<SignatureAndHashAlgorithm> localSupportedSignAlgs, ProtocolVersion protocolVersion) throws IOException {
            this.preferableSignatureAlgorithm = null;
            this.protocolVersion = protocolVersion;
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                this.preferableSignatureAlgorithm = SignatureAndHashAlgorithm.valueOf(input.getInt8(), input.getInt8(), 0);
                if (!localSupportedSignAlgs.contains(this.preferableSignatureAlgorithm)) {
                    throw new SSLHandshakeException("Unsupported SignatureAndHashAlgorithm in ServerKeyExchange message");
                }
            }
            this.signature = input.getBytes16();
        }

        SignatureAndHashAlgorithm getPreferableSignatureAlgorithm() {
            return this.preferableSignatureAlgorithm;
        }

        boolean verify(ProtocolVersion protocolVersion, HandshakeHash handshakeHash, PublicKey publicKey, SecretKey masterSecret) throws GeneralSecurityException {
            Signature sig;
            String algorithm = publicKey.getAlgorithm();
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                sig = JsseJce.getSignature(this.preferableSignatureAlgorithm.getAlgorithmName());
            } else {
                sig = getSignature(protocolVersion, algorithm);
            }
            sig.initVerify(publicKey);
            updateSignature(sig, protocolVersion, handshakeHash, algorithm, masterSecret);
            return sig.verify(this.signature);
        }

        private static Signature getSignature(ProtocolVersion protocolVersion, String algorithm) throws GeneralSecurityException {
            if (algorithm.equals("RSA")) {
                return RSASignature.getInternalInstance();
            }
            if (algorithm.equals("DSA")) {
                return JsseJce.getSignature("RawDSA");
            }
            if (algorithm.equals("EC")) {
                return JsseJce.getSignature("NONEwithECDSA");
            }
            throw new SignatureException("Unrecognized algorithm: " + algorithm);
        }

        private static void updateSignature(Signature sig, ProtocolVersion protocolVersion, HandshakeHash handshakeHash, String algorithm, SecretKey masterKey) throws SignatureException {
            MessageDigest shaClone;
            if (algorithm.equals("RSA")) {
                if (protocolVersion.v < ProtocolVersion.TLS12.v) {
                    MessageDigest md5Clone = handshakeHash.getMD5Clone();
                    shaClone = handshakeHash.getSHAClone();
                    if (protocolVersion.v < ProtocolVersion.TLS10.v) {
                        updateDigest(md5Clone, MD5_pad1, MD5_pad2, masterKey);
                        updateDigest(shaClone, SHA_pad1, SHA_pad2, masterKey);
                    }
                    RSASignature.setHashes(sig, md5Clone, shaClone);
                    return;
                }
                sig.update(handshakeHash.getAllHandshakeMessages());
            } else if (protocolVersion.v < ProtocolVersion.TLS12.v) {
                shaClone = handshakeHash.getSHAClone();
                if (protocolVersion.v < ProtocolVersion.TLS10.v) {
                    updateDigest(shaClone, SHA_pad1, SHA_pad2, masterKey);
                }
                sig.update(shaClone.digest());
            } else {
                sig.update(handshakeHash.getAllHandshakeMessages());
            }
        }

        private static void updateDigest(MessageDigest md, byte[] pad1, byte[] pad2, SecretKey masterSecret) {
            byte[] keyBytes = null;
            if ("RAW".equals(masterSecret.getFormat())) {
                keyBytes = masterSecret.getEncoded();
            }
            if (keyBytes != null) {
                md.update(keyBytes);
            } else {
                digestKey(md, masterSecret);
            }
            md.update(pad1);
            byte[] temp = md.digest();
            if (keyBytes != null) {
                md.update(keyBytes);
            } else {
                digestKey(md, masterSecret);
            }
            md.update(pad2);
            md.update(temp);
        }

        private static void makeAccessible(AccessibleObject o) {
            AccessController.doPrivileged(new AnonymousClass1(o));
        }

        private static void digestKey(MessageDigest md, SecretKey key) {
            try {
                if (md.getClass() != delegate) {
                    throw new Exception("Digest is not a MessageDigestSpi");
                }
                MessageDigestSpi spi = (MessageDigestSpi) spiField.get(md);
                Class<?> clazz = spi.getClass();
                Object r = methodCache.get(clazz);
                if (r == null) {
                    try {
                        r = clazz.getDeclaredMethod("implUpdate", SecretKey.class);
                        makeAccessible((Method) r);
                    } catch (NoSuchMethodException e) {
                        r = NULL_OBJECT;
                    }
                    methodCache.put(clazz, r);
                }
                if (r == NULL_OBJECT) {
                    throw new Exception("Digest does not support implUpdate(SecretKey)");
                }
                ((Method) r).invoke(spi, key);
            } catch (Exception e2) {
                throw new RuntimeException("Could not obtain encoded key and MessageDigest cannot digest key", e2);
            }
        }

        int messageType() {
            return 15;
        }

        int messageLength() {
            int temp = 2;
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                temp = SignatureAndHashAlgorithm.sizeInRecord() + 2;
            }
            return this.signature.length + temp;
        }

        void send(HandshakeOutStream s) throws IOException {
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                s.putInt8(this.preferableSignatureAlgorithm.getHashValue());
                s.putInt8(this.preferableSignatureAlgorithm.getSignatureValue());
            }
            s.putBytes16(this.signature);
        }

        void print(PrintStream s) throws IOException {
            s.println("*** CertificateVerify");
            if (debug != null && Debug.isOn("verbose") && this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                s.println("Signature Algorithm " + this.preferableSignatureAlgorithm.getAlgorithmName());
            }
        }
    }

    static final class ClientHello extends HandshakeMessage {
        private static final byte[] NULL_COMPRESSION = null;
        private CipherSuiteList cipherSuites;
        RandomCookie clnt_random;
        byte[] compression_methods;
        HelloExtensions extensions;
        ProtocolVersion protocolVersion;
        SessionId sessionId;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.HandshakeMessage.ClientHello.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.HandshakeMessage.ClientHello.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.HandshakeMessage.ClientHello.<clinit>():void");
        }

        ClientHello(SecureRandom generator, ProtocolVersion protocolVersion, SessionId sessionId, CipherSuiteList cipherSuites) {
            this.extensions = new HelloExtensions();
            this.protocolVersion = protocolVersion;
            this.sessionId = sessionId;
            this.cipherSuites = cipherSuites;
            if (cipherSuites.containsEC()) {
                this.extensions.add(SupportedEllipticCurvesExtension.DEFAULT);
                this.extensions.add(SupportedEllipticPointFormatsExtension.DEFAULT);
            }
            this.clnt_random = new RandomCookie(generator);
            this.compression_methods = NULL_COMPRESSION;
        }

        ClientHello(HandshakeInStream s, int messageLength) throws IOException {
            this.extensions = new HelloExtensions();
            this.protocolVersion = ProtocolVersion.valueOf(s.getInt8(), s.getInt8());
            this.clnt_random = new RandomCookie(s);
            this.sessionId = new SessionId(s.getBytes8());
            this.cipherSuites = new CipherSuiteList(s);
            this.compression_methods = s.getBytes8();
            if (messageLength() != messageLength) {
                this.extensions = new HelloExtensions(s);
            }
        }

        CipherSuiteList getCipherSuites() {
            return this.cipherSuites;
        }

        void addRenegotiationInfoExtension(byte[] clientVerifyData) {
            this.extensions.add(new RenegotiationInfoExtension(clientVerifyData, new byte[0]));
        }

        void addServerNameIndicationExtension(String hostname) {
            ArrayList<String> hostnames = new ArrayList(1);
            hostnames.add(hostname);
            try {
                this.extensions.add(new ServerNameExtension(hostnames));
            } catch (IOException e) {
            }
        }

        void addSignatureAlgorithmsExtension(Collection<SignatureAndHashAlgorithm> algorithms) {
            this.extensions.add(new SignatureAlgorithmsExtension(algorithms));
        }

        int messageType() {
            return 1;
        }

        int messageLength() {
            return (((this.sessionId.length() + 38) + (this.cipherSuites.size() * 2)) + this.compression_methods.length) + this.extensions.length();
        }

        void send(HandshakeOutStream s) throws IOException {
            s.putInt8(this.protocolVersion.major);
            s.putInt8(this.protocolVersion.minor);
            this.clnt_random.send(s);
            s.putBytes8(this.sessionId.getId());
            this.cipherSuites.send(s);
            s.putBytes8(this.compression_methods);
            this.extensions.send(s);
        }

        void print(PrintStream s) throws IOException {
            s.println("*** ClientHello, " + this.protocolVersion);
            if (debug != null && Debug.isOn("verbose")) {
                s.print("RandomCookie:  ");
                this.clnt_random.print(s);
                s.print("Session ID:  ");
                s.println(this.sessionId);
                s.println("Cipher Suites: " + this.cipherSuites);
                Debug.println(s, "Compression Methods", this.compression_methods);
                this.extensions.print(s);
                s.println("***");
            }
        }
    }

    static abstract class ServerKeyExchange extends HandshakeMessage {
        ServerKeyExchange() {
        }

        int messageType() {
            return 12;
        }
    }

    static final class DH_ServerKeyExchange extends ServerKeyExchange {
        private static final boolean dhKeyExchangeFix = false;
        private byte[] dh_Ys;
        private byte[] dh_g;
        private byte[] dh_p;
        private SignatureAndHashAlgorithm preferableSignatureAlgorithm;
        ProtocolVersion protocolVersion;
        private byte[] signature;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.HandshakeMessage.DH_ServerKeyExchange.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.HandshakeMessage.DH_ServerKeyExchange.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.HandshakeMessage.DH_ServerKeyExchange.<clinit>():void");
        }

        DH_ServerKeyExchange(DHCrypt obj, ProtocolVersion protocolVersion) {
            this.protocolVersion = protocolVersion;
            this.preferableSignatureAlgorithm = null;
            setValues(obj);
            this.signature = null;
        }

        DH_ServerKeyExchange(DHCrypt obj, PrivateKey key, byte[] clntNonce, byte[] svrNonce, SecureRandom sr, SignatureAndHashAlgorithm signAlgorithm, ProtocolVersion protocolVersion) throws GeneralSecurityException {
            Signature sig;
            this.protocolVersion = protocolVersion;
            setValues(obj);
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                this.preferableSignatureAlgorithm = signAlgorithm;
                sig = JsseJce.getSignature(signAlgorithm.getAlgorithmName());
            } else {
                this.preferableSignatureAlgorithm = null;
                if (key.getAlgorithm().equals("DSA")) {
                    sig = JsseJce.getSignature("DSA");
                } else {
                    sig = RSASignature.getInstance();
                }
            }
            sig.initSign(key, sr);
            updateSignature(sig, clntNonce, svrNonce);
            this.signature = sig.sign();
        }

        DH_ServerKeyExchange(HandshakeInStream input, ProtocolVersion protocolVersion) throws IOException, GeneralSecurityException {
            this.protocolVersion = protocolVersion;
            this.preferableSignatureAlgorithm = null;
            this.dh_p = input.getBytes16();
            this.dh_g = input.getBytes16();
            this.dh_Ys = input.getBytes16();
            KeyUtil.validate(new DHPublicKeySpec(new BigInteger(1, this.dh_Ys), new BigInteger(1, this.dh_p), new BigInteger(1, this.dh_g)));
            this.signature = null;
        }

        DH_ServerKeyExchange(HandshakeInStream input, PublicKey publicKey, byte[] clntNonce, byte[] svrNonce, int messageSize, Collection<SignatureAndHashAlgorithm> localSupportedSignAlgs, ProtocolVersion protocolVersion) throws IOException, GeneralSecurityException {
            byte[] signature;
            Signature sig;
            this.protocolVersion = protocolVersion;
            this.dh_p = input.getBytes16();
            this.dh_g = input.getBytes16();
            this.dh_Ys = input.getBytes16();
            KeyUtil.validate(new DHPublicKeySpec(new BigInteger(1, this.dh_Ys), new BigInteger(1, this.dh_p), new BigInteger(1, this.dh_g)));
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                this.preferableSignatureAlgorithm = SignatureAndHashAlgorithm.valueOf(input.getInt8(), input.getInt8(), 0);
                if (!localSupportedSignAlgs.contains(this.preferableSignatureAlgorithm)) {
                    throw new SSLHandshakeException("Unsupported SignatureAndHashAlgorithm in ServerKeyExchange message");
                }
            }
            this.preferableSignatureAlgorithm = null;
            if (dhKeyExchangeFix) {
                signature = input.getBytes16();
            } else {
                signature = new byte[(((messageSize - (this.dh_p.length + 2)) - (this.dh_g.length + 2)) - (this.dh_Ys.length + 2))];
                input.read(signature);
            }
            String algorithm = publicKey.getAlgorithm();
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                sig = JsseJce.getSignature(this.preferableSignatureAlgorithm.getAlgorithmName());
            } else if (algorithm.equals("DSA")) {
                sig = JsseJce.getSignature("DSA");
            } else if (algorithm.equals("RSA")) {
                sig = RSASignature.getInstance();
            } else {
                throw new SSLKeyException("neither an RSA or a DSA key");
            }
            sig.initVerify(publicKey);
            updateSignature(sig, clntNonce, svrNonce);
            if (!sig.verify(signature)) {
                throw new SSLKeyException("Server D-H key verification failed");
            }
        }

        BigInteger getModulus() {
            return new BigInteger(1, this.dh_p);
        }

        BigInteger getBase() {
            return new BigInteger(1, this.dh_g);
        }

        BigInteger getServerPublicKey() {
            return new BigInteger(1, this.dh_Ys);
        }

        private void updateSignature(Signature sig, byte[] clntNonce, byte[] svrNonce) throws SignatureException {
            sig.update(clntNonce);
            sig.update(svrNonce);
            int tmp = this.dh_p.length;
            sig.update((byte) (tmp >> 8));
            sig.update((byte) (tmp & 255));
            sig.update(this.dh_p);
            tmp = this.dh_g.length;
            sig.update((byte) (tmp >> 8));
            sig.update((byte) (tmp & 255));
            sig.update(this.dh_g);
            tmp = this.dh_Ys.length;
            sig.update((byte) (tmp >> 8));
            sig.update((byte) (tmp & 255));
            sig.update(this.dh_Ys);
        }

        private void setValues(DHCrypt obj) {
            this.dh_p = HandshakeMessage.toByteArray(obj.getModulus());
            this.dh_g = HandshakeMessage.toByteArray(obj.getBase());
            this.dh_Ys = HandshakeMessage.toByteArray(obj.getPublicKey());
        }

        int messageLength() {
            int temp = ((this.dh_p.length + 6) + this.dh_g.length) + this.dh_Ys.length;
            if (this.signature == null) {
                return temp;
            }
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                temp += SignatureAndHashAlgorithm.sizeInRecord();
            }
            temp += this.signature.length;
            if (dhKeyExchangeFix) {
                return temp + 2;
            }
            return temp;
        }

        void send(HandshakeOutStream s) throws IOException {
            s.putBytes16(this.dh_p);
            s.putBytes16(this.dh_g);
            s.putBytes16(this.dh_Ys);
            if (this.signature != null) {
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    s.putInt8(this.preferableSignatureAlgorithm.getHashValue());
                    s.putInt8(this.preferableSignatureAlgorithm.getSignatureValue());
                }
                if (dhKeyExchangeFix) {
                    s.putBytes16(this.signature);
                } else {
                    s.write(this.signature);
                }
            }
        }

        void print(PrintStream s) throws IOException {
            s.println("*** Diffie-Hellman ServerKeyExchange");
            if (debug != null && Debug.isOn("verbose")) {
                Debug.println(s, "DH Modulus", this.dh_p);
                Debug.println(s, "DH Base", this.dh_g);
                Debug.println(s, "Server DH Public Key", this.dh_Ys);
                if (this.signature == null) {
                    s.println("Anonymous");
                    return;
                }
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    s.println("Signature Algorithm " + this.preferableSignatureAlgorithm.getAlgorithmName());
                }
                s.println("Signed with a DSA or RSA public key");
            }
        }
    }

    static final class DistinguishedName {
        byte[] name;

        DistinguishedName(HandshakeInStream input) throws IOException {
            this.name = input.getBytes16();
        }

        DistinguishedName(X500Principal dn) {
            this.name = dn.getEncoded();
        }

        X500Principal getX500Principal() throws IOException {
            try {
                return new X500Principal(this.name);
            } catch (IllegalArgumentException e) {
                throw ((SSLProtocolException) new SSLProtocolException(e.getMessage()).initCause(e));
            }
        }

        int length() {
            return this.name.length + 2;
        }

        void send(HandshakeOutStream output) throws IOException {
            output.putBytes16(this.name);
        }

        void print(PrintStream output) throws IOException {
            output.println("<" + new X500Principal(this.name).toString() + ">");
        }
    }

    static final class ECDH_ServerKeyExchange extends ServerKeyExchange {
        private static final int CURVE_EXPLICIT_CHAR2 = 2;
        private static final int CURVE_EXPLICIT_PRIME = 1;
        private static final int CURVE_NAMED_CURVE = 3;
        private int curveId;
        private byte[] pointBytes;
        private SignatureAndHashAlgorithm preferableSignatureAlgorithm;
        ProtocolVersion protocolVersion;
        private ECPublicKey publicKey;
        private byte[] signatureBytes;

        ECDH_ServerKeyExchange(ECDHCrypt obj, PrivateKey privateKey, byte[] clntNonce, byte[] svrNonce, SecureRandom sr, SignatureAndHashAlgorithm signAlgorithm, ProtocolVersion protocolVersion) throws GeneralSecurityException {
            this.protocolVersion = protocolVersion;
            this.publicKey = (ECPublicKey) obj.getPublicKey();
            ECParameterSpec params = this.publicKey.getParams();
            this.pointBytes = JsseJce.encodePoint(this.publicKey.getW(), params.getCurve());
            this.curveId = SupportedEllipticCurvesExtension.getCurveIndex(params);
            if (privateKey != null) {
                Signature sig;
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    this.preferableSignatureAlgorithm = signAlgorithm;
                    sig = JsseJce.getSignature(signAlgorithm.getAlgorithmName());
                } else {
                    sig = getSignature(privateKey.getAlgorithm());
                }
                sig.initSign(privateKey);
                updateSignature(sig, clntNonce, svrNonce);
                this.signatureBytes = sig.sign();
            }
        }

        ECDH_ServerKeyExchange(HandshakeInStream input, PublicKey signingKey, byte[] clntNonce, byte[] svrNonce, Collection<SignatureAndHashAlgorithm> localSupportedSignAlgs, ProtocolVersion protocolVersion) throws IOException, GeneralSecurityException {
            this.protocolVersion = protocolVersion;
            int curveType = input.getInt8();
            if (curveType == CURVE_NAMED_CURVE) {
                this.curveId = input.getInt16();
                if (SupportedEllipticCurvesExtension.isSupported(this.curveId)) {
                    String curveOid = SupportedEllipticCurvesExtension.getCurveOid(this.curveId);
                    if (curveOid == null) {
                        throw new SSLHandshakeException("Unknown named curve: " + this.curveId);
                    }
                    ECParameterSpec parameters = JsseJce.getECParameterSpec(curveOid);
                    if (parameters == null) {
                        throw new SSLHandshakeException("Unsupported curve: " + curveOid);
                    }
                    this.pointBytes = input.getBytes8();
                    this.publicKey = (ECPublicKey) JsseJce.getKeyFactory("EC").generatePublic(new ECPublicKeySpec(JsseJce.decodePoint(this.pointBytes, parameters.getCurve()), parameters));
                    if (signingKey != null) {
                        Signature sig;
                        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                            this.preferableSignatureAlgorithm = SignatureAndHashAlgorithm.valueOf(input.getInt8(), input.getInt8(), 0);
                            if (!localSupportedSignAlgs.contains(this.preferableSignatureAlgorithm)) {
                                throw new SSLHandshakeException("Unsupported SignatureAndHashAlgorithm in ServerKeyExchange message");
                            }
                        }
                        this.signatureBytes = input.getBytes16();
                        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                            sig = JsseJce.getSignature(this.preferableSignatureAlgorithm.getAlgorithmName());
                        } else {
                            sig = getSignature(signingKey.getAlgorithm());
                        }
                        sig.initVerify(signingKey);
                        updateSignature(sig, clntNonce, svrNonce);
                        if (!sig.verify(this.signatureBytes)) {
                            throw new SSLKeyException("Invalid signature on ECDH server key exchange message");
                        }
                        return;
                    }
                    return;
                }
                throw new SSLHandshakeException("Unsupported curveId: " + this.curveId);
            }
            throw new SSLHandshakeException("Unsupported ECCurveType: " + curveType);
        }

        ECPublicKey getPublicKey() {
            return this.publicKey;
        }

        private static Signature getSignature(String keyAlgorithm) throws NoSuchAlgorithmException {
            if (keyAlgorithm.equals("EC")) {
                return JsseJce.getSignature("SHA1withECDSA");
            }
            if (keyAlgorithm.equals("RSA")) {
                return RSASignature.getInstance();
            }
            throw new NoSuchAlgorithmException("neither an RSA or a EC key");
        }

        private void updateSignature(Signature sig, byte[] clntNonce, byte[] svrNonce) throws SignatureException {
            sig.update(clntNonce);
            sig.update(svrNonce);
            sig.update((byte) 3);
            sig.update((byte) (this.curveId >> 8));
            sig.update((byte) this.curveId);
            sig.update((byte) this.pointBytes.length);
            sig.update(this.pointBytes);
        }

        int messageLength() {
            int sigLen = 0;
            if (this.signatureBytes != null) {
                sigLen = this.signatureBytes.length + CURVE_EXPLICIT_CHAR2;
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    sigLen += SignatureAndHashAlgorithm.sizeInRecord();
                }
            }
            return (this.pointBytes.length + 4) + sigLen;
        }

        void send(HandshakeOutStream s) throws IOException {
            s.putInt8(CURVE_NAMED_CURVE);
            s.putInt16(this.curveId);
            s.putBytes8(this.pointBytes);
            if (this.signatureBytes != null) {
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    s.putInt8(this.preferableSignatureAlgorithm.getHashValue());
                    s.putInt8(this.preferableSignatureAlgorithm.getSignatureValue());
                }
                s.putBytes16(this.signatureBytes);
            }
        }

        void print(PrintStream s) throws IOException {
            s.println("*** ECDH ServerKeyExchange");
            if (debug != null && Debug.isOn("verbose")) {
                if (this.signatureBytes == null) {
                    s.println("Anonymous");
                } else if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    s.println("Signature Algorithm " + this.preferableSignatureAlgorithm.getAlgorithmName());
                }
                s.println("Server key: " + this.publicKey);
            }
        }
    }

    static final class Finished extends HandshakeMessage {
        static final int CLIENT = 1;
        static final int SERVER = 2;
        private static final byte[] SSL_CLIENT = null;
        private static final byte[] SSL_SERVER = null;
        private CipherSuite cipherSuite;
        private ProtocolVersion protocolVersion;
        private byte[] verifyData;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.HandshakeMessage.Finished.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.HandshakeMessage.Finished.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.HandshakeMessage.Finished.<clinit>():void");
        }

        Finished(ProtocolVersion protocolVersion, HandshakeHash handshakeHash, int sender, SecretKey master, CipherSuite cipherSuite) {
            this.protocolVersion = protocolVersion;
            this.cipherSuite = cipherSuite;
            this.verifyData = getFinished(handshakeHash, sender, master);
        }

        Finished(ProtocolVersion protocolVersion, HandshakeInStream input, CipherSuite cipherSuite) throws IOException {
            this.protocolVersion = protocolVersion;
            this.cipherSuite = cipherSuite;
            this.verifyData = new byte[(protocolVersion.v >= ProtocolVersion.TLS10.v ? 12 : 36)];
            input.read(this.verifyData);
        }

        boolean verify(HandshakeHash handshakeHash, int sender, SecretKey master) {
            return Arrays.equals(getFinished(handshakeHash, sender, master), this.verifyData);
        }

        private byte[] getFinished(HandshakeHash handshakeHash, int sender, SecretKey masterKey) {
            byte[] sslLabel;
            String tlsLabel;
            if (sender == CLIENT) {
                sslLabel = SSL_CLIENT;
                tlsLabel = "client finished";
            } else if (sender == SERVER) {
                sslLabel = SSL_SERVER;
                tlsLabel = "server finished";
            } else {
                throw new RuntimeException("Invalid sender: " + sender);
            }
            if (this.protocolVersion.v >= ProtocolVersion.TLS10.v) {
                try {
                    byte[] seed;
                    String prfAlg;
                    PRF prf;
                    if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                        seed = handshakeHash.getFinishedHash();
                        prfAlg = "SunTls12Prf";
                        prf = this.cipherSuite.prfAlg;
                    } else {
                        MessageDigest md5Clone = handshakeHash.getMD5Clone();
                        MessageDigest shaClone = handshakeHash.getSHAClone();
                        seed = new byte[36];
                        md5Clone.digest(seed, 0, 16);
                        shaClone.digest(seed, 16, 20);
                        prfAlg = "SunTlsPrf";
                        prf = PRF.P_NONE;
                    }
                    AlgorithmParameterSpec spec = new TlsPrfParameterSpec(masterKey, tlsLabel, seed, 12, prf.getPRFHashAlg(), prf.getPRFHashLength(), prf.getPRFBlockSize());
                    KeyGenerator kg = JsseJce.getKeyGenerator(prfAlg);
                    kg.init(spec);
                    SecretKey prfKey = kg.generateKey();
                    if ("RAW".equals(prfKey.getFormat())) {
                        return prfKey.getEncoded();
                    }
                    throw new ProviderException("Invalid PRF output, format must be RAW");
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException("PRF failed", e);
                }
            }
            md5Clone = handshakeHash.getMD5Clone();
            shaClone = handshakeHash.getSHAClone();
            updateDigest(md5Clone, sslLabel, MD5_pad1, MD5_pad2, masterKey);
            updateDigest(shaClone, sslLabel, SHA_pad1, SHA_pad2, masterKey);
            byte[] finished = new byte[36];
            try {
                md5Clone.digest(finished, 0, 16);
                shaClone.digest(finished, 16, 20);
                return finished;
            } catch (DigestException e2) {
                throw new RuntimeException("Digest failed", e2);
            }
        }

        private static void updateDigest(MessageDigest md, byte[] sender, byte[] pad1, byte[] pad2, SecretKey masterSecret) {
            md.update(sender);
            CertificateVerify.updateDigest(md, pad1, pad2, masterSecret);
        }

        byte[] getVerifyData() {
            return this.verifyData;
        }

        int messageType() {
            return 20;
        }

        int messageLength() {
            return this.verifyData.length;
        }

        void send(HandshakeOutStream out) throws IOException {
            out.write(this.verifyData);
        }

        void print(PrintStream s) throws IOException {
            s.println("*** Finished");
            if (debug != null && Debug.isOn("verbose")) {
                Debug.println(s, "verify_data", this.verifyData);
                s.println("***");
            }
        }
    }

    static final class HelloRequest extends HandshakeMessage {
        int messageType() {
            return 0;
        }

        HelloRequest() {
        }

        HelloRequest(HandshakeInStream in) throws IOException {
        }

        int messageLength() {
            return 0;
        }

        void send(HandshakeOutStream out) throws IOException {
        }

        void print(PrintStream out) throws IOException {
            out.println("*** HelloRequest (empty)");
        }
    }

    static final class RSA_ServerKeyExchange extends ServerKeyExchange {
        private byte[] rsa_exponent;
        private byte[] rsa_modulus;
        private Signature signature;
        private byte[] signatureBytes;

        private void updateSignature(byte[] clntNonce, byte[] svrNonce) throws SignatureException {
            this.signature.update(clntNonce);
            this.signature.update(svrNonce);
            int tmp = this.rsa_modulus.length;
            this.signature.update((byte) (tmp >> 8));
            this.signature.update((byte) (tmp & 255));
            this.signature.update(this.rsa_modulus);
            tmp = this.rsa_exponent.length;
            this.signature.update((byte) (tmp >> 8));
            this.signature.update((byte) (tmp & 255));
            this.signature.update(this.rsa_exponent);
        }

        RSA_ServerKeyExchange(PublicKey ephemeralKey, PrivateKey privateKey, RandomCookie clntNonce, RandomCookie svrNonce, SecureRandom sr) throws GeneralSecurityException {
            RSAPublicKeySpec rsaKey = JsseJce.getRSAPublicKeySpec(ephemeralKey);
            this.rsa_modulus = HandshakeMessage.toByteArray(rsaKey.getModulus());
            this.rsa_exponent = HandshakeMessage.toByteArray(rsaKey.getPublicExponent());
            this.signature = RSASignature.getInstance();
            this.signature.initSign(privateKey, sr);
            updateSignature(clntNonce.random_bytes, svrNonce.random_bytes);
            this.signatureBytes = this.signature.sign();
        }

        RSA_ServerKeyExchange(HandshakeInStream input) throws IOException, NoSuchAlgorithmException {
            this.signature = RSASignature.getInstance();
            this.rsa_modulus = input.getBytes16();
            this.rsa_exponent = input.getBytes16();
            this.signatureBytes = input.getBytes16();
        }

        PublicKey getPublicKey() {
            try {
                return JsseJce.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(1, this.rsa_modulus), new BigInteger(1, this.rsa_exponent)));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        boolean verify(PublicKey certifiedKey, RandomCookie clntNonce, RandomCookie svrNonce) throws GeneralSecurityException {
            this.signature.initVerify(certifiedKey);
            updateSignature(clntNonce.random_bytes, svrNonce.random_bytes);
            return this.signature.verify(this.signatureBytes);
        }

        int messageLength() {
            return ((this.rsa_modulus.length + 6) + this.rsa_exponent.length) + this.signatureBytes.length;
        }

        void send(HandshakeOutStream s) throws IOException {
            s.putBytes16(this.rsa_modulus);
            s.putBytes16(this.rsa_exponent);
            s.putBytes16(this.signatureBytes);
        }

        void print(PrintStream s) throws IOException {
            s.println("*** RSA ServerKeyExchange");
            if (debug != null && Debug.isOn("verbose")) {
                Debug.println(s, "RSA Modulus", this.rsa_modulus);
                Debug.println(s, "RSA Public Exponent", this.rsa_exponent);
            }
        }
    }

    static final class ServerHello extends HandshakeMessage {
        CipherSuite cipherSuite;
        byte compression_method;
        HelloExtensions extensions;
        ProtocolVersion protocolVersion;
        SessionId sessionId;
        RandomCookie svr_random;

        int messageType() {
            return 2;
        }

        ServerHello() {
            this.extensions = new HelloExtensions();
        }

        ServerHello(HandshakeInStream input, int messageLength) throws IOException {
            this.extensions = new HelloExtensions();
            this.protocolVersion = ProtocolVersion.valueOf(input.getInt8(), input.getInt8());
            this.svr_random = new RandomCookie(input);
            this.sessionId = new SessionId(input.getBytes8());
            this.cipherSuite = CipherSuite.valueOf(input.getInt8(), input.getInt8());
            this.compression_method = (byte) input.getInt8();
            if (messageLength() != messageLength) {
                this.extensions = new HelloExtensions(input);
            }
        }

        int messageLength() {
            return (this.sessionId.length() + 38) + this.extensions.length();
        }

        void send(HandshakeOutStream s) throws IOException {
            s.putInt8(this.protocolVersion.major);
            s.putInt8(this.protocolVersion.minor);
            this.svr_random.send(s);
            s.putBytes8(this.sessionId.getId());
            s.putInt8(this.cipherSuite.id >> 8);
            s.putInt8(this.cipherSuite.id & 255);
            s.putInt8(this.compression_method);
            this.extensions.send(s);
        }

        void print(PrintStream s) throws IOException {
            s.println("*** ServerHello, " + this.protocolVersion);
            if (debug != null && Debug.isOn("verbose")) {
                s.print("RandomCookie:  ");
                this.svr_random.print(s);
                s.print("Session ID:  ");
                s.println(this.sessionId);
                s.println("Cipher Suite: " + this.cipherSuite);
                s.println("Compression Method: " + this.compression_method);
                this.extensions.print(s);
                s.println("***");
            }
        }
    }

    static final class ServerHelloDone extends HandshakeMessage {
        int messageType() {
            return 14;
        }

        ServerHelloDone() {
        }

        ServerHelloDone(HandshakeInStream input) {
        }

        int messageLength() {
            return 0;
        }

        void send(HandshakeOutStream s) throws IOException {
        }

        void print(PrintStream s) throws IOException {
            s.println("*** ServerHelloDone");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.HandshakeMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.HandshakeMessage.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.HandshakeMessage.<clinit>():void");
    }

    abstract int messageLength();

    abstract int messageType();

    abstract void print(PrintStream printStream) throws IOException;

    abstract void send(HandshakeOutStream handshakeOutStream) throws IOException;

    HandshakeMessage() {
    }

    static byte[] toByteArray(BigInteger bi) {
        byte[] b = bi.toByteArray();
        if (b.length <= 1 || b[0] != null) {
            return b;
        }
        int n = b.length - 1;
        byte[] newarray = new byte[n];
        System.arraycopy(b, 1, newarray, 0, n);
        return newarray;
    }

    private static byte[] genPad(int b, int count) {
        byte[] padding = new byte[count];
        Arrays.fill(padding, (byte) b);
        return padding;
    }

    final void write(HandshakeOutStream s) throws IOException {
        int len = messageLength();
        if (len >= Record.OVERFLOW_OF_INT24) {
            throw new SSLException("Handshake message too big, type = " + messageType() + ", len = " + len);
        }
        s.write(messageType());
        s.putInt24(len);
        send(s);
    }
}
