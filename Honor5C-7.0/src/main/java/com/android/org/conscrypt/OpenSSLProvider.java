package com.android.org.conscrypt;

import java.security.Provider;

public final class OpenSSLProvider extends Provider {
    private static final String PREFIX = null;
    public static final String PROVIDER_NAME = "AndroidOpenSSL";
    private static final String STANDARD_EC_PRIVATE_KEY_INTERFACE_CLASS_NAME = "java.security.interfaces.ECPrivateKey";
    private static final String STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME = "java.security.interfaces.RSAPrivateKey";
    private static final String STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME = "java.security.interfaces.RSAPublicKey";
    private static final long serialVersionUID = 2996752495318905136L;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLProvider.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLProvider.<clinit>():void");
    }

    public OpenSSLProvider(java.lang.String r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLProvider.<init>(java.lang.String):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLProvider.<init>(java.lang.String):void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLProvider.<init>(java.lang.String):void");
    }

    public OpenSSLProvider() {
        this(PROVIDER_NAME);
    }

    private void putMacImplClass(String algorithm, String className) {
        putImplClassWithKeyConstraints("Mac." + algorithm, PREFIX + className, PREFIX + "OpenSSLKeyHolder", "RAW");
    }

    private void putSymmetricCipherImplClass(String transformation, String className) {
        putImplClassWithKeyConstraints("Cipher." + transformation, PREFIX + className, null, "RAW");
    }

    private void putRSACipherImplClass(String transformation, String className) {
        putImplClassWithKeyConstraints("Cipher." + transformation, PREFIX + className, PREFIX + "OpenSSLRSAPrivateKey" + "|" + STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + PREFIX + "OpenSSLRSAPublicKey" + "|" + STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME, null);
    }

    private void putSignatureImplClass(String algorithm, String className) {
        putImplClassWithKeyConstraints("Signature." + algorithm, PREFIX + className, PREFIX + "OpenSSLKeyHolder" + "|" + STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + STANDARD_EC_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME, "PKCS#8|X.509");
    }

    private void putRAWRSASignatureImplClass(String className) {
        putImplClassWithKeyConstraints("Signature.NONEwithRSA", PREFIX + className, PREFIX + "OpenSSLRSAPrivateKey" + "|" + STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + PREFIX + "OpenSSLRSAPublicKey" + "|" + STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME, null);
    }

    private void putECDHKeyAgreementImplClass(String className) {
        putImplClassWithKeyConstraints("KeyAgreement.ECDH", PREFIX + className, PREFIX + "OpenSSLKeyHolder" + "|" + STANDARD_EC_PRIVATE_KEY_INTERFACE_CLASS_NAME, "PKCS#8");
    }

    private void putImplClassWithKeyConstraints(String typeAndAlgName, String fullyQualifiedClassName, String supportedKeyClasses, String supportedKeyFormats) {
        put(typeAndAlgName, fullyQualifiedClassName);
        if (supportedKeyClasses != null) {
            put(typeAndAlgName + " SupportedKeyClasses", supportedKeyClasses);
        }
        if (supportedKeyFormats != null) {
            put(typeAndAlgName + " SupportedKeyFormats", supportedKeyFormats);
        }
    }
}
