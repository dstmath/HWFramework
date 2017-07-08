package android.security.keystore;

import java.security.Provider;

class AndroidKeyStoreBCWorkaroundProvider extends Provider {
    private static final String KEYSTORE_PRIVATE_KEY_CLASS_NAME = "android.security.keystore.AndroidKeyStorePrivateKey";
    private static final String KEYSTORE_PUBLIC_KEY_CLASS_NAME = "android.security.keystore.AndroidKeyStorePublicKey";
    private static final String KEYSTORE_SECRET_KEY_CLASS_NAME = "android.security.keystore.AndroidKeyStoreSecretKey";
    private static final String PACKAGE_NAME = "android.security.keystore";

    AndroidKeyStoreBCWorkaroundProvider() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.keystore.AndroidKeyStoreBCWorkaroundProvider.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.keystore.AndroidKeyStoreBCWorkaroundProvider.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreBCWorkaroundProvider.<init>():void");
    }

    private void putMacImpl(String algorithm, String implClass) {
        put("Mac." + algorithm, implClass);
        put("Mac." + algorithm + " SupportedKeyClasses", KEYSTORE_SECRET_KEY_CLASS_NAME);
    }

    private void putSymmetricCipherImpl(String transformation, String implClass) {
        put("Cipher." + transformation, implClass);
        put("Cipher." + transformation + " SupportedKeyClasses", KEYSTORE_SECRET_KEY_CLASS_NAME);
    }

    private void putAsymmetricCipherImpl(String transformation, String implClass) {
        put("Cipher." + transformation, implClass);
        put("Cipher." + transformation + " SupportedKeyClasses", "android.security.keystore.AndroidKeyStorePrivateKey|android.security.keystore.AndroidKeyStorePublicKey");
    }

    private void putSignatureImpl(String algorithm, String implClass) {
        put("Signature." + algorithm, implClass);
        put("Signature." + algorithm + " SupportedKeyClasses", "android.security.keystore.AndroidKeyStorePrivateKey|android.security.keystore.AndroidKeyStorePublicKey");
    }

    public static String[] getSupportedEcdsaSignatureDigests() {
        return new String[]{KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512};
    }

    public static String[] getSupportedRsaSignatureWithPkcs1PaddingDigests() {
        return new String[]{KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_MD5, KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512};
    }
}
