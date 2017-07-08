package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.EVP_MD_CTX;
import com.android.org.conscrypt.ct.CTConstants;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

public class OpenSSLSignature extends SignatureSpi {
    private static final /* synthetic */ int[] -com-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues = null;
    private EVP_MD_CTX ctx;
    private final EngineType engineType;
    private final long evpMdRef;
    private long evpPkeyCtx;
    private OpenSSLKey key;
    private boolean signing;
    private final byte[] singleByte;

    private enum EngineType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.EngineType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.EngineType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.EngineType.<clinit>():void");
        }
    }

    static abstract class RSAPKCS1Padding extends OpenSSLSignature {
        RSAPKCS1Padding(long evpMdRef) {
            super(evpMdRef, EngineType.RSA, null);
        }

        protected final void configureEVP_PKEY_CTX(long ctx) {
            NativeCrypto.EVP_PKEY_CTX_set_rsa_padding(ctx, 1);
        }
    }

    public static final class MD5RSA extends RSAPKCS1Padding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.MD5RSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.MD5RSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.MD5RSA.<clinit>():void");
        }

        public MD5RSA() {
            super(EVP_MD);
        }
    }

    static abstract class RSAPSSPadding extends OpenSSLSignature {
        private static final String MGF1_ALGORITHM_NAME = "MGF1";
        private static final String MGF1_OID = "1.2.840.113549.1.1.8";
        private static final int TRAILER_FIELD_BC_ID = 1;
        private final String contentDigestAlgorithm;
        private String mgf1DigestAlgorithm;
        private long mgf1EvpMdRef;
        private int saltSizeBytes;

        public RSAPSSPadding(long contentDigestEvpMdRef, String contentDigestAlgorithm, int saltSizeBytes) {
            super(contentDigestEvpMdRef, EngineType.RSA, null);
            this.contentDigestAlgorithm = contentDigestAlgorithm;
            this.mgf1DigestAlgorithm = contentDigestAlgorithm;
            this.mgf1EvpMdRef = contentDigestEvpMdRef;
            this.saltSizeBytes = saltSizeBytes;
        }

        protected final void configureEVP_PKEY_CTX(long ctx) {
            NativeCrypto.EVP_PKEY_CTX_set_rsa_padding(ctx, 6);
            NativeCrypto.EVP_PKEY_CTX_set_rsa_mgf1_md(ctx, this.mgf1EvpMdRef);
            NativeCrypto.EVP_PKEY_CTX_set_rsa_pss_saltlen(ctx, this.saltSizeBytes);
        }

        protected final void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params instanceof PSSParameterSpec) {
                PSSParameterSpec spec = (PSSParameterSpec) params;
                String specContentDigest = getJcaDigestAlgorithmStandardName(spec.getDigestAlgorithm());
                if (specContentDigest == null) {
                    throw new InvalidAlgorithmParameterException("Unsupported content digest algorithm: " + spec.getDigestAlgorithm());
                } else if (this.contentDigestAlgorithm.equalsIgnoreCase(specContentDigest)) {
                    String specMgfAlgorithm = spec.getMGFAlgorithm();
                    if (MGF1_ALGORITHM_NAME.equalsIgnoreCase(specMgfAlgorithm) || MGF1_OID.equals(specMgfAlgorithm)) {
                        AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
                        if (mgfSpec instanceof MGF1ParameterSpec) {
                            MGF1ParameterSpec specMgf1Spec = (MGF1ParameterSpec) spec.getMGFParameters();
                            String specMgf1Digest = getJcaDigestAlgorithmStandardName(specMgf1Spec.getDigestAlgorithm());
                            if (specMgf1Digest == null) {
                                throw new InvalidAlgorithmParameterException("Unsupported MGF1 digest algorithm: " + specMgf1Spec.getDigestAlgorithm());
                            }
                            try {
                                long specMgf1EvpMdRef = getEVP_MDByJcaDigestAlgorithmStandardName(specMgf1Digest);
                                int specSaltSizeBytes = spec.getSaltLength();
                                if (specSaltSizeBytes < 0) {
                                    throw new InvalidAlgorithmParameterException("Salt length must be non-negative: " + specSaltSizeBytes);
                                }
                                int specTrailer = spec.getTrailerField();
                                if (specTrailer != TRAILER_FIELD_BC_ID) {
                                    throw new InvalidAlgorithmParameterException("Unsupported trailer field: " + specTrailer + ". Only " + TRAILER_FIELD_BC_ID + " supported");
                                }
                                this.mgf1DigestAlgorithm = specMgf1Digest;
                                this.mgf1EvpMdRef = specMgf1EvpMdRef;
                                this.saltSizeBytes = specSaltSizeBytes;
                                long ctx = getEVP_PKEY_CTX();
                                if (ctx != 0) {
                                    configureEVP_PKEY_CTX(ctx);
                                    return;
                                }
                                return;
                            } catch (NoSuchAlgorithmException e) {
                                throw new ProviderException("Failed to obtain EVP_MD for " + specMgf1Digest, e);
                            }
                        }
                        throw new InvalidAlgorithmParameterException("Unsupported MGF parameters: " + mgfSpec + ". Only " + MGF1ParameterSpec.class.getName() + " supported");
                    }
                    throw new InvalidAlgorithmParameterException("Unsupported MGF algorithm: " + specMgfAlgorithm + ". Only " + MGF1_ALGORITHM_NAME + " supported");
                } else {
                    throw new InvalidAlgorithmParameterException("Changing content digest algorithm not supported");
                }
            }
            throw new InvalidAlgorithmParameterException("Unsupported parameter: " + params + ". Only " + PSSParameterSpec.class.getName() + " supported");
        }

        protected final AlgorithmParameters engineGetParameters() {
            try {
                AlgorithmParameters result = AlgorithmParameters.getInstance("PSS");
                result.init(new PSSParameterSpec(this.contentDigestAlgorithm, MGF1_ALGORITHM_NAME, new MGF1ParameterSpec(this.mgf1DigestAlgorithm), this.saltSizeBytes, TRAILER_FIELD_BC_ID));
                return result;
            } catch (GeneralSecurityException e) {
                throw new ProviderException("Failed to create PSS AlgorithmParameters", e);
            }
        }

        private static String getJcaDigestAlgorithmStandardName(String algorithm) {
            if ("SHA-256".equalsIgnoreCase(algorithm) || "2.16.840.1.101.3.4.2.1".equals(algorithm)) {
                return "SHA-256";
            }
            if ("SHA-512".equalsIgnoreCase(algorithm) || "2.16.840.1.101.3.4.2.3".equals(algorithm)) {
                return "SHA-512";
            }
            if ("SHA-1".equalsIgnoreCase(algorithm) || "1.3.14.3.2.26".equals(algorithm)) {
                return "SHA-1";
            }
            if ("SHA-384".equalsIgnoreCase(algorithm) || "2.16.840.1.101.3.4.2.2".equals(algorithm)) {
                return "SHA-384";
            }
            if ("SHA-224".equalsIgnoreCase(algorithm) || "2.16.840.1.101.3.4.2.4".equals(algorithm)) {
                return "SHA-224";
            }
            return null;
        }

        private static long getEVP_MDByJcaDigestAlgorithmStandardName(String algorithm) throws NoSuchAlgorithmException {
            if ("SHA-256".equalsIgnoreCase(algorithm)) {
                return NativeCrypto.EVP_get_digestbyname("sha256");
            }
            if ("SHA-512".equalsIgnoreCase(algorithm)) {
                return NativeCrypto.EVP_get_digestbyname("sha512");
            }
            if ("SHA-1".equalsIgnoreCase(algorithm)) {
                return NativeCrypto.EVP_get_digestbyname("sha1");
            }
            if ("SHA-384".equalsIgnoreCase(algorithm)) {
                return NativeCrypto.EVP_get_digestbyname("sha384");
            }
            if ("SHA-224".equalsIgnoreCase(algorithm)) {
                return NativeCrypto.EVP_get_digestbyname("sha224");
            }
            throw new NoSuchAlgorithmException("Unsupported algorithm: " + algorithm);
        }
    }

    public static final class SHA1ECDSA extends OpenSSLSignature {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA1ECDSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA1ECDSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA1ECDSA.<clinit>():void");
        }

        public SHA1ECDSA() {
            super(EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA1RSA extends RSAPKCS1Padding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA1RSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA1RSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA1RSA.<clinit>():void");
        }

        public SHA1RSA() {
            super(EVP_MD);
        }
    }

    public static final class SHA1RSAPSS extends RSAPSSPadding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA1RSAPSS.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA1RSAPSS.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA1RSAPSS.<clinit>():void");
        }

        public SHA1RSAPSS() {
            super(EVP_MD, "SHA-1", 20);
        }
    }

    public static final class SHA224ECDSA extends OpenSSLSignature {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA224ECDSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA224ECDSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA224ECDSA.<clinit>():void");
        }

        public SHA224ECDSA() {
            super(EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA224RSA extends RSAPKCS1Padding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA224RSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA224RSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA224RSA.<clinit>():void");
        }

        public SHA224RSA() {
            super(EVP_MD);
        }
    }

    public static final class SHA224RSAPSS extends RSAPSSPadding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA224RSAPSS.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA224RSAPSS.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA224RSAPSS.<clinit>():void");
        }

        public SHA224RSAPSS() {
            super(EVP_MD, "SHA-224", 28);
        }
    }

    public static final class SHA256ECDSA extends OpenSSLSignature {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA256ECDSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA256ECDSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA256ECDSA.<clinit>():void");
        }

        public SHA256ECDSA() {
            super(EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA256RSA extends RSAPKCS1Padding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA256RSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA256RSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA256RSA.<clinit>():void");
        }

        public SHA256RSA() {
            super(EVP_MD);
        }
    }

    public static final class SHA256RSAPSS extends RSAPSSPadding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA256RSAPSS.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA256RSAPSS.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA256RSAPSS.<clinit>():void");
        }

        public SHA256RSAPSS() {
            super(EVP_MD, "SHA-256", 32);
        }
    }

    public static final class SHA384ECDSA extends OpenSSLSignature {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA384ECDSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA384ECDSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA384ECDSA.<clinit>():void");
        }

        public SHA384ECDSA() {
            super(EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA384RSA extends RSAPKCS1Padding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA384RSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA384RSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA384RSA.<clinit>():void");
        }

        public SHA384RSA() {
            super(EVP_MD);
        }
    }

    public static final class SHA384RSAPSS extends RSAPSSPadding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA384RSAPSS.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA384RSAPSS.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA384RSAPSS.<clinit>():void");
        }

        public SHA384RSAPSS() {
            super(EVP_MD, "SHA-384", 48);
        }
    }

    public static final class SHA512ECDSA extends OpenSSLSignature {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA512ECDSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA512ECDSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA512ECDSA.<clinit>():void");
        }

        public SHA512ECDSA() {
            super(EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA512RSA extends RSAPKCS1Padding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA512RSA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA512RSA.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA512RSA.<clinit>():void");
        }

        public SHA512RSA() {
            super(EVP_MD);
        }
    }

    public static final class SHA512RSAPSS extends RSAPSSPadding {
        private static final long EVP_MD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLSignature.SHA512RSAPSS.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLSignature.SHA512RSAPSS.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLSignature.SHA512RSAPSS.<clinit>():void");
        }

        public SHA512RSAPSS() {
            super(EVP_MD, "SHA-512", 64);
        }
    }

    private static /* synthetic */ int[] -getcom-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues() {
        if (-com-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues != null) {
            return -com-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues;
        }
        int[] iArr = new int[EngineType.values().length];
        try {
            iArr[EngineType.EC.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[EngineType.RSA.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -com-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues = iArr;
        return iArr;
    }

    /* synthetic */ OpenSSLSignature(long evpMdRef, EngineType engineType, OpenSSLSignature openSSLSignature) {
        this(evpMdRef, engineType);
    }

    private OpenSSLSignature(long evpMdRef, EngineType engineType) {
        this.singleByte = new byte[1];
        this.engineType = engineType;
        this.evpMdRef = evpMdRef;
    }

    private final void resetContext() {
        EVP_MD_CTX ctxLocal = new EVP_MD_CTX(NativeCrypto.EVP_MD_CTX_create());
        if (this.signing) {
            enableDSASignatureNonceHardeningIfApplicable();
            this.evpPkeyCtx = NativeCrypto.EVP_DigestSignInit(ctxLocal, this.evpMdRef, this.key.getNativeRef());
        } else {
            this.evpPkeyCtx = NativeCrypto.EVP_DigestVerifyInit(ctxLocal, this.evpMdRef, this.key.getNativeRef());
        }
        configureEVP_PKEY_CTX(this.evpPkeyCtx);
        this.ctx = ctxLocal;
    }

    protected void configureEVP_PKEY_CTX(long ctx) {
    }

    protected void engineUpdate(byte input) {
        this.singleByte[0] = input;
        engineUpdate(this.singleByte, 0, 1);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        EVP_MD_CTX ctxLocal = this.ctx;
        if (this.signing) {
            NativeCrypto.EVP_DigestSignUpdate(ctxLocal, input, offset, len);
        } else {
            NativeCrypto.EVP_DigestVerifyUpdate(ctxLocal, input, offset, len);
        }
    }

    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        if (input.isDirect()) {
            long baseAddress = NativeCrypto.getDirectBufferAddress(input);
            if (baseAddress == 0) {
                super.engineUpdate(input);
                return;
            }
            int position = input.position();
            if (position < 0) {
                throw new RuntimeException("Negative position");
            }
            long ptr = baseAddress + ((long) position);
            int len = input.remaining();
            if (len < 0) {
                throw new RuntimeException("Negative remaining amount");
            }
            EVP_MD_CTX ctxLocal = this.ctx;
            if (this.signing) {
                NativeCrypto.EVP_DigestSignUpdateDirect(ctxLocal, ptr, len);
            } else {
                NativeCrypto.EVP_DigestVerifyUpdateDirect(ctxLocal, ptr, len);
            }
            input.position(position + len);
            return;
        }
        super.engineUpdate(input);
    }

    @Deprecated
    protected Object engineGetParameter(String param) throws InvalidParameterException {
        return null;
    }

    private void checkEngineType(OpenSSLKey pkey) throws InvalidKeyException {
        int pkeyType = NativeCrypto.EVP_PKEY_type(pkey.getNativeRef());
        switch (-getcom-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues()[this.engineType.ordinal()]) {
            case CTConstants.VERSION_LENGTH /*1*/:
                if (pkeyType != NativeConstants.EVP_PKEY_EC) {
                    throw new InvalidKeyException("Signature initialized as " + this.engineType + " (not EC)");
                }
            case CTConstants.SIGNATURE_LENGTH_BYTES /*2*/:
                if (pkeyType != 6) {
                    throw new InvalidKeyException("Signature initialized as " + this.engineType + " (not RSA)");
                }
            default:
                throw new InvalidKeyException("Key must be of type " + this.engineType);
        }
    }

    private void initInternal(OpenSSLKey newKey, boolean signing) throws InvalidKeyException {
        checkEngineType(newKey);
        this.key = newKey;
        this.signing = signing;
        resetContext();
    }

    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        initInternal(OpenSSLKey.fromPrivateKey(privateKey), true);
    }

    private void enableDSASignatureNonceHardeningIfApplicable() {
        OpenSSLKey key = this.key;
        switch (-getcom-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues()[this.engineType.ordinal()]) {
            case CTConstants.VERSION_LENGTH /*1*/:
                NativeCrypto.EC_KEY_set_nonce_from_hash(key.getNativeRef(), true);
            default:
        }
    }

    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        initInternal(OpenSSLKey.fromPublicKey(publicKey), false);
    }

    @Deprecated
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
    }

    protected byte[] engineSign() throws SignatureException {
        try {
            byte[] EVP_DigestSignFinal = NativeCrypto.EVP_DigestSignFinal(this.ctx);
            resetContext();
            return EVP_DigestSignFinal;
        } catch (Exception ex) {
            throw new SignatureException(ex);
        } catch (Throwable th) {
            resetContext();
        }
    }

    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        try {
            boolean EVP_DigestVerifyFinal = NativeCrypto.EVP_DigestVerifyFinal(this.ctx, sigBytes, 0, sigBytes.length);
            resetContext();
            return EVP_DigestVerifyFinal;
        } catch (Exception ex) {
            throw new SignatureException(ex);
        } catch (Throwable th) {
            resetContext();
        }
    }

    protected final long getEVP_PKEY_CTX() {
        return this.evpPkeyCtx;
    }
}
