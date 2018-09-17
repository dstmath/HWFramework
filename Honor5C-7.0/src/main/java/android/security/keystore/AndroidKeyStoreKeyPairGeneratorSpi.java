package android.security.keystore;

import android.security.Credentials;
import android.security.KeyPairGeneratorSpec;
import android.security.KeyStore;
import android.security.KeyStore.State;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties.Digest;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import com.android.internal.util.ArrayUtils;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERInteger;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.Certificate;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x509.TBSCertificate;
import com.android.org.bouncycastle.asn1.x509.Time;
import com.android.org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jce.X509Principal;
import com.android.org.bouncycastle.jce.provider.X509CertificateObject;
import com.android.org.bouncycastle.x509.X509V3CertificateGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class AndroidKeyStoreKeyPairGeneratorSpi extends KeyPairGeneratorSpi {
    private static final int EC_DEFAULT_KEY_SIZE = 256;
    private static final int RSA_DEFAULT_KEY_SIZE = 2048;
    private static final int RSA_MAX_KEY_SIZE = 8192;
    private static final int RSA_MIN_KEY_SIZE = 512;
    private static final List<String> SUPPORTED_EC_NIST_CURVE_NAMES = null;
    private static final Map<String, Integer> SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE = null;
    private static final List<Integer> SUPPORTED_EC_NIST_CURVE_SIZES = null;
    private boolean mEncryptionAtRestRequired;
    private String mEntryAlias;
    private int mEntryUid;
    private String mJcaKeyAlgorithm;
    private int mKeySizeBits;
    private KeyStore mKeyStore;
    private int mKeymasterAlgorithm;
    private int[] mKeymasterBlockModes;
    private int[] mKeymasterDigests;
    private int[] mKeymasterEncryptionPaddings;
    private int[] mKeymasterPurposes;
    private int[] mKeymasterSignaturePaddings;
    private final int mOriginalKeymasterAlgorithm;
    private BigInteger mRSAPublicExponent;
    private SecureRandom mRng;
    private KeyGenParameterSpec mSpec;

    public static class EC extends AndroidKeyStoreKeyPairGeneratorSpi {
        public EC() {
            super(3);
        }
    }

    public static class RSA extends AndroidKeyStoreKeyPairGeneratorSpi {
        public RSA() {
            super(1);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi.<clinit>():void");
    }

    protected AndroidKeyStoreKeyPairGeneratorSpi(int keymasterAlgorithm) {
        this.mKeymasterAlgorithm = -1;
        this.mOriginalKeymasterAlgorithm = keymasterAlgorithm;
    }

    public void initialize(int keysize, SecureRandom random) {
        throw new IllegalArgumentException(KeyGenParameterSpec.class.getName() + " or " + KeyPairGeneratorSpec.class.getName() + " required to initialize this KeyPairGenerator");
    }

    public void initialize(java.security.spec.AlgorithmParameterSpec r19, java.security.SecureRandom r20) throws java.security.InvalidAlgorithmParameterException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void. bs: [B:2:0x0006, B:24:0x00a5, B:28:0x00ab, B:56:0x020a]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r18 = this;
        r18.resetAll();
        r12 = 0;
        if (r19 != 0) goto L_0x003e;
    L_0x0006:
        r13 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0037 }
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0037 }
        r14.<init>();	 Catch:{ all -> 0x0037 }
        r15 = "Must supply params of type ";	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r15 = android.security.keystore.KeyGenParameterSpec.class;	 Catch:{ all -> 0x0037 }
        r15 = r15.getName();	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r15 = " or ";	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r15 = android.security.KeyPairGeneratorSpec.class;	 Catch:{ all -> 0x0037 }
        r15 = r15.getName();	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r14 = r14.toString();	 Catch:{ all -> 0x0037 }
        r13.<init>(r14);	 Catch:{ all -> 0x0037 }
        throw r13;	 Catch:{ all -> 0x0037 }
    L_0x0037:
        r13 = move-exception;
        if (r12 != 0) goto L_0x003d;
    L_0x003a:
        r18.resetAll();
    L_0x003d:
        throw r13;
    L_0x003e:
        r4 = 0;
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r6 = r0.mOriginalKeymasterAlgorithm;	 Catch:{ all -> 0x0037 }
        r0 = r19;	 Catch:{ all -> 0x0037 }
        r13 = r0 instanceof android.security.keystore.KeyGenParameterSpec;	 Catch:{ all -> 0x0037 }
        if (r13 == 0) goto L_0x009a;	 Catch:{ all -> 0x0037 }
    L_0x0049:
        r0 = r19;	 Catch:{ all -> 0x0037 }
        r0 = (android.security.keystore.KeyGenParameterSpec) r0;	 Catch:{ all -> 0x0037 }
        r9 = r0;	 Catch:{ all -> 0x0037 }
    L_0x004e:
        r13 = r9.getKeystoreAlias();	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r0.mEntryAlias = r13;	 Catch:{ all -> 0x0037 }
        r13 = r9.getUid();	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r0.mEntryUid = r13;	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r0.mSpec = r9;	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r0.mKeymasterAlgorithm = r6;	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r0.mEncryptionAtRestRequired = r4;	 Catch:{ all -> 0x0037 }
        r13 = r9.getKeySize();	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r0.mKeySizeBits = r13;	 Catch:{ all -> 0x0037 }
        r18.initAlgorithmSpecificParameters();	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r13 = r0.mKeySizeBits;	 Catch:{ all -> 0x0037 }
        r14 = -1;	 Catch:{ all -> 0x0037 }
        if (r13 != r14) goto L_0x0084;	 Catch:{ all -> 0x0037 }
    L_0x007c:
        r13 = getDefaultKeySize(r6);	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r0.mKeySizeBits = r13;	 Catch:{ all -> 0x0037 }
    L_0x0084:
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r13 = r0.mKeySizeBits;	 Catch:{ all -> 0x0037 }
        checkValidKeySize(r6, r13);	 Catch:{ all -> 0x0037 }
        r13 = r9.getKeystoreAlias();	 Catch:{ all -> 0x0037 }
        if (r13 != 0) goto L_0x020a;	 Catch:{ all -> 0x0037 }
    L_0x0091:
        r13 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0037 }
        r14 = "KeyStore entry alias not provided";	 Catch:{ all -> 0x0037 }
        r13.<init>(r14);	 Catch:{ all -> 0x0037 }
        throw r13;	 Catch:{ all -> 0x0037 }
    L_0x009a:
        r0 = r19;	 Catch:{ all -> 0x0037 }
        r13 = r0 instanceof android.security.KeyPairGeneratorSpec;	 Catch:{ all -> 0x0037 }
        if (r13 == 0) goto L_0x01c6;	 Catch:{ all -> 0x0037 }
    L_0x00a0:
        r0 = r19;	 Catch:{ all -> 0x0037 }
        r0 = (android.security.KeyPairGeneratorSpec) r0;	 Catch:{ all -> 0x0037 }
        r8 = r0;	 Catch:{ all -> 0x0037 }
        r11 = r8.getKeyType();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        if (r11 == 0) goto L_0x00af;
    L_0x00ab:
        r6 = android.security.keystore.KeyProperties.KeyAlgorithm.toKeymasterAsymmetricKeyAlgorithm(r11);	 Catch:{ IllegalArgumentException -> 0x00d7 }
    L_0x00af:
        switch(r6) {
            case 1: goto L_0x015b;
            case 2: goto L_0x00b2;
            case 3: goto L_0x00e1;
            default: goto L_0x00b2;
        };
    L_0x00b2:
        r13 = new java.security.ProviderException;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = new java.lang.StringBuilder;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14.<init>();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = "Unsupported algorithm: ";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = r14.append(r15);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r0 = r18;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = r0.mKeymasterAlgorithm;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = r14.append(r15);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = r14.toString();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13.<init>(r14);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        throw r13;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
    L_0x00d0:
        r3 = move-exception;
        r13 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0037 }
        r13.<init>(r3);	 Catch:{ all -> 0x0037 }
        throw r13;	 Catch:{ all -> 0x0037 }
    L_0x00d7:
        r2 = move-exception;
        r13 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "Invalid key type in parameters";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13.<init>(r14, r2);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        throw r13;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
    L_0x00e1:
        r10 = new android.security.keystore.KeyGenParameterSpec$Builder;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = r8.getKeystoreAlias();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = 12;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.<init>(r13, r14);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = 6;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = new java.lang.String[r13];	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "NONE";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 0;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-1";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 1;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-224";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 2;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-256";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 3;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-384";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 4;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-512";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 5;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setDigests(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
    L_0x0116:
        r13 = r8.getKeySize();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = -1;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        if (r13 == r14) goto L_0x0124;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
    L_0x011d:
        r13 = r8.getKeySize();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setKeySize(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
    L_0x0124:
        r13 = r8.getAlgorithmParameterSpec();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        if (r13 == 0) goto L_0x0131;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
    L_0x012a:
        r13 = r8.getAlgorithmParameterSpec();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setAlgorithmParameterSpec(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
    L_0x0131:
        r13 = r8.getSubjectDN();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setCertificateSubject(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = r8.getSerialNumber();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setCertificateSerialNumber(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = r8.getStartDate();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setCertificateNotBefore(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = r8.getEndDate();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setCertificateNotAfter(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r4 = r8.isEncryptionRequired();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = 0;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setUserAuthenticationRequired(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r9 = r10.build();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        goto L_0x004e;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
    L_0x015b:
        r10 = new android.security.keystore.KeyGenParameterSpec$Builder;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = r8.getKeystoreAlias();	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = 15;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.<init>(r13, r14);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = 7;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = new java.lang.String[r13];	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "NONE";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 0;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "MD5";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 1;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-1";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 2;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-224";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 3;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-256";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 4;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-384";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 5;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "SHA-512";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 6;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setDigests(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = 3;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = new java.lang.String[r13];	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "NoPadding";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 0;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "PKCS1Padding";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 1;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "OAEPPadding";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 2;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setEncryptionPaddings(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = 2;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = new java.lang.String[r13];	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "PKCS1";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 0;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r14 = "PSS";	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r15 = 1;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13[r15] = r14;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setSignaturePaddings(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r13 = 0;	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        r10.setRandomizedEncryptionRequired(r13);	 Catch:{ NullPointerException -> 0x00d0, NullPointerException -> 0x00d0 }
        goto L_0x0116;
    L_0x01c6:
        r13 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0037 }
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0037 }
        r14.<init>();	 Catch:{ all -> 0x0037 }
        r15 = "Unsupported params class: ";	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r15 = r19.getClass();	 Catch:{ all -> 0x0037 }
        r15 = r15.getName();	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r15 = ". Supported: ";	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r15 = android.security.keystore.KeyGenParameterSpec.class;	 Catch:{ all -> 0x0037 }
        r15 = r15.getName();	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r15 = ", ";	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r15 = android.security.KeyPairGeneratorSpec.class;	 Catch:{ all -> 0x0037 }
        r15 = r15.getName();	 Catch:{ all -> 0x0037 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0037 }
        r14 = r14.toString();	 Catch:{ all -> 0x0037 }
        r13.<init>(r14);	 Catch:{ all -> 0x0037 }
        throw r13;	 Catch:{ all -> 0x0037 }
    L_0x020a:
        r5 = android.security.keystore.KeyProperties.KeyAlgorithm.fromKeymasterAsymmetricKeyAlgorithm(r6);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = r9.getPurposes();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = android.security.keystore.KeyProperties.Purpose.allToKeymaster(r13);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0.mKeymasterPurposes = r13;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = r9.getBlockModes();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = android.security.keystore.KeyProperties.BlockMode.allToKeymaster(r13);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0.mKeymasterBlockModes = r13;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = r9.getEncryptionPaddings();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = android.security.keystore.KeyProperties.EncryptionPadding.allToKeymaster(r13);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0.mKeymasterEncryptionPaddings = r13;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = r9.getPurposes();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = r13 & 1;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        if (r13 == 0) goto L_0x0290;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
    L_0x023a:
        r13 = r9.isRandomizedEncryptionRequired();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        if (r13 == 0) goto L_0x0290;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
    L_0x0240:
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = r0.mKeymasterEncryptionPaddings;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = 0;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r15 = r14.length;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
    L_0x0246:
        if (r13 >= r15) goto L_0x0290;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
    L_0x0248:
        r7 = r14[r13];	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r16 = android.security.keystore.KeymasterUtils.isKeymasterPaddingSchemeIndCpaCompatibleWithAsymmetricCrypto(r7);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        if (r16 != 0) goto L_0x028d;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
    L_0x0250:
        r13 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = new java.lang.StringBuilder;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14.<init>();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r15 = "Randomized encryption (IND-CPA) required but may be violated by padding scheme: ";	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = r14.append(r15);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r15 = android.security.keystore.KeyProperties.EncryptionPadding.fromKeymaster(r7);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = r14.append(r15);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r15 = ". See ";	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = r14.append(r15);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r15 = android.security.keystore.KeyGenParameterSpec.class;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r15 = r15.getName();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = r14.append(r15);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r15 = " documentation.";	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = r14.append(r15);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = r14.toString();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13.<init>(r14);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        throw r13;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
    L_0x0286:
        r3 = move-exception;
        r13 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0037 }
        r13.<init>(r3);	 Catch:{ all -> 0x0037 }
        throw r13;	 Catch:{ all -> 0x0037 }
    L_0x028d:
        r13 = r13 + 1;
        goto L_0x0246;
    L_0x0290:
        r13 = r9.getSignaturePaddings();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = android.security.keystore.KeyProperties.SignaturePadding.allToKeymaster(r13);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0.mKeymasterSignaturePaddings = r13;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = r9.isDigestsSpecified();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        if (r13 == 0) goto L_0x02f3;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
    L_0x02a2:
        r13 = r9.getDigests();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13 = android.security.keystore.KeyProperties.Digest.allToKeymaster(r13);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0.mKeymasterDigests = r13;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
    L_0x02ae:
        r13 = new android.security.keymaster.KeymasterArguments;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r13.<init>();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r14 = r14.isUserAuthenticationRequired();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r15 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r15 = r15.getUserAuthenticationValidityDurationSeconds();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r16 = r0;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r16 = r16.isUserAuthenticationValidWhileOnBody();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r17 = r0;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r17 = r17.isInvalidatedByBiometricEnrollment();	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        android.security.keystore.KeymasterUtils.addUserAuthArgs(r13, r14, r15, r16, r17);	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r0.mJcaKeyAlgorithm = r5;	 Catch:{ all -> 0x0037 }
        r0 = r20;	 Catch:{ all -> 0x0037 }
        r1 = r18;	 Catch:{ all -> 0x0037 }
        r1.mRng = r0;	 Catch:{ all -> 0x0037 }
        r13 = android.security.KeyStore.getInstance();	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r0.mKeyStore = r13;	 Catch:{ all -> 0x0037 }
        r12 = 1;
        if (r12 != 0) goto L_0x02f2;
    L_0x02ef:
        r18.resetAll();
    L_0x02f2:
        return;
    L_0x02f3:
        r13 = libcore.util.EmptyArray.INT;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0 = r18;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        r0.mKeymasterDigests = r13;	 Catch:{ IllegalArgumentException -> 0x0286, IllegalArgumentException -> 0x0286 }
        goto L_0x02ae;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void");
    }

    private void resetAll() {
        this.mEntryAlias = null;
        this.mEntryUid = -1;
        this.mJcaKeyAlgorithm = null;
        this.mKeymasterAlgorithm = -1;
        this.mKeymasterPurposes = null;
        this.mKeymasterBlockModes = null;
        this.mKeymasterEncryptionPaddings = null;
        this.mKeymasterSignaturePaddings = null;
        this.mKeymasterDigests = null;
        this.mKeySizeBits = 0;
        this.mSpec = null;
        this.mRSAPublicExponent = null;
        this.mEncryptionAtRestRequired = false;
        this.mRng = null;
        this.mKeyStore = null;
    }

    private void initAlgorithmSpecificParameters() throws InvalidAlgorithmParameterException {
        AlgorithmParameterSpec algSpecificSpec = this.mSpec.getAlgorithmParameterSpec();
        switch (this.mKeymasterAlgorithm) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                BigInteger publicExponent = null;
                if (algSpecificSpec instanceof RSAKeyGenParameterSpec) {
                    RSAKeyGenParameterSpec rsaSpec = (RSAKeyGenParameterSpec) algSpecificSpec;
                    if (this.mKeySizeBits == -1) {
                        this.mKeySizeBits = rsaSpec.getKeysize();
                    } else if (this.mKeySizeBits != rsaSpec.getKeysize()) {
                        throw new InvalidAlgorithmParameterException("RSA key size must match  between " + this.mSpec + " and " + algSpecificSpec + ": " + this.mKeySizeBits + " vs " + rsaSpec.getKeysize());
                    }
                    publicExponent = rsaSpec.getPublicExponent();
                } else if (algSpecificSpec != null) {
                    throw new InvalidAlgorithmParameterException("RSA may only use RSAKeyGenParameterSpec");
                }
                if (publicExponent == null) {
                    publicExponent = RSAKeyGenParameterSpec.F4;
                }
                if (publicExponent.compareTo(BigInteger.ZERO) < 1) {
                    throw new InvalidAlgorithmParameterException("RSA public exponent must be positive: " + publicExponent);
                } else if (publicExponent.compareTo(KeymasterArguments.UINT64_MAX_VALUE) > 0) {
                    throw new InvalidAlgorithmParameterException("Unsupported RSA public exponent: " + publicExponent + ". Maximum supported value: " + KeymasterArguments.UINT64_MAX_VALUE);
                } else {
                    this.mRSAPublicExponent = publicExponent;
                }
            case Engine.DEFAULT_STREAM /*3*/:
                if (algSpecificSpec instanceof ECGenParameterSpec) {
                    String curveName = ((ECGenParameterSpec) algSpecificSpec).getName();
                    Integer ecSpecKeySizeBits = (Integer) SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.get(curveName.toLowerCase(Locale.US));
                    if (ecSpecKeySizeBits == null) {
                        throw new InvalidAlgorithmParameterException("Unsupported EC curve name: " + curveName + ". Supported: " + SUPPORTED_EC_NIST_CURVE_NAMES);
                    } else if (this.mKeySizeBits == -1) {
                        this.mKeySizeBits = ecSpecKeySizeBits.intValue();
                    } else if (this.mKeySizeBits != ecSpecKeySizeBits.intValue()) {
                        throw new InvalidAlgorithmParameterException("EC key size must match  between " + this.mSpec + " and " + algSpecificSpec + ": " + this.mKeySizeBits + " vs " + ecSpecKeySizeBits);
                    }
                } else if (algSpecificSpec != null) {
                    throw new InvalidAlgorithmParameterException("EC may only use ECGenParameterSpec");
                }
            default:
                throw new ProviderException("Unsupported algorithm: " + this.mKeymasterAlgorithm);
        }
    }

    public KeyPair generateKeyPair() {
        int flags = 0;
        if (this.mKeyStore == null || this.mSpec == null) {
            throw new IllegalStateException("Not initialized");
        }
        if (this.mEncryptionAtRestRequired) {
            flags = 1;
        }
        if ((flags & 1) == 0 || this.mKeyStore.state() == State.UNLOCKED) {
            byte[] additionalEntropy = KeyStoreCryptoOperationUtils.getRandomBytesToMixIntoKeystoreRng(this.mRng, (this.mKeySizeBits + 7) / 8);
            Credentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias, this.mEntryUid);
            String privateKeyAlias = Credentials.USER_PRIVATE_KEY + this.mEntryAlias;
            boolean success = false;
            try {
                generateKeystoreKeyPair(privateKeyAlias, constructKeyGenerationArguments(), additionalEntropy, flags);
                KeyPair keyPair = loadKeystoreKeyPair(privateKeyAlias);
                storeCertificateChain(flags, createCertificateChain(privateKeyAlias, keyPair));
                success = true;
                return keyPair;
            } finally {
                if (!success) {
                    Credentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias, this.mEntryUid);
                }
            }
        } else {
            throw new IllegalStateException("Encryption at rest using secure lock screen credential requested for key pair, but the user has not yet entered the credential");
        }
    }

    private Iterable<byte[]> createCertificateChain(String privateKeyAlias, KeyPair keyPair) throws ProviderException {
        byte[] challenge = this.mSpec.getAttestationChallenge();
        if (challenge == null) {
            return Collections.singleton(generateSelfSignedCertificateBytes(keyPair));
        }
        KeymasterArguments args = new KeymasterArguments();
        args.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE, challenge);
        return getAttestationChain(privateKeyAlias, keyPair, args);
    }

    private void generateKeystoreKeyPair(String privateKeyAlias, KeymasterArguments args, byte[] additionalEntropy, int flags) throws ProviderException {
        String str = privateKeyAlias;
        KeymasterArguments keymasterArguments = args;
        byte[] bArr = additionalEntropy;
        int errorCode = this.mKeyStore.generateKey(str, keymasterArguments, bArr, this.mEntryUid, flags, new KeyCharacteristics());
        if (errorCode != 1) {
            throw new ProviderException("Failed to generate key pair", KeyStore.getKeyStoreException(errorCode));
        }
    }

    private KeyPair loadKeystoreKeyPair(String privateKeyAlias) throws ProviderException {
        try {
            KeyPair result = AndroidKeyStoreProvider.loadAndroidKeyStoreKeyPairFromKeystore(this.mKeyStore, privateKeyAlias, this.mEntryUid);
            if (this.mJcaKeyAlgorithm.equalsIgnoreCase(result.getPrivate().getAlgorithm())) {
                return result;
            }
            throw new ProviderException("Generated key pair algorithm does not match requested algorithm: " + result.getPrivate().getAlgorithm() + " vs " + this.mJcaKeyAlgorithm);
        } catch (UnrecoverableKeyException e) {
            throw new ProviderException("Failed to load generated key pair from keystore", e);
        }
    }

    private KeymasterArguments constructKeyGenerationArguments() {
        KeymasterArguments args = new KeymasterArguments();
        args.addUnsignedInt(KeymasterDefs.KM_TAG_KEY_SIZE, (long) this.mKeySizeBits);
        args.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, this.mKeymasterAlgorithm);
        args.addEnums(KeymasterDefs.KM_TAG_PURPOSE, this.mKeymasterPurposes);
        args.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockModes);
        args.addEnums(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterEncryptionPaddings);
        args.addEnums(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterSignaturePaddings);
        args.addEnums(KeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigests);
        KeymasterUtils.addUserAuthArgs(args, this.mSpec.isUserAuthenticationRequired(), this.mSpec.getUserAuthenticationValidityDurationSeconds(), this.mSpec.isUserAuthenticationValidWhileOnBody(), this.mSpec.isInvalidatedByBiometricEnrollment());
        args.addDateIfNotNull(KeymasterDefs.KM_TAG_ACTIVE_DATETIME, this.mSpec.getKeyValidityStart());
        args.addDateIfNotNull(KeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, this.mSpec.getKeyValidityForOriginationEnd());
        args.addDateIfNotNull(KeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, this.mSpec.getKeyValidityForConsumptionEnd());
        addAlgorithmSpecificParameters(args);
        if (this.mSpec.isUniqueIdIncluded()) {
            args.addBoolean(KeymasterDefs.KM_TAG_INCLUDE_UNIQUE_ID);
        }
        return args;
    }

    private void storeCertificateChain(int flags, Iterable<byte[]> iterable) throws ProviderException {
        Iterator<byte[]> iter = iterable.iterator();
        storeCertificate(Credentials.USER_CERTIFICATE, (byte[]) iter.next(), flags, "Failed to store certificate");
        if (iter.hasNext()) {
            ByteArrayOutputStream certificateConcatenationStream = new ByteArrayOutputStream();
            while (iter.hasNext()) {
                byte[] data = (byte[]) iter.next();
                certificateConcatenationStream.write(data, 0, data.length);
            }
            storeCertificate(Credentials.CA_CERTIFICATE, certificateConcatenationStream.toByteArray(), flags, "Failed to store attestation CA certificate");
        }
    }

    private void storeCertificate(String prefix, byte[] certificateBytes, int flags, String failureMessage) throws ProviderException {
        int insertErrorCode = this.mKeyStore.insert(prefix + this.mEntryAlias, certificateBytes, this.mEntryUid, flags);
        if (insertErrorCode != 1) {
            throw new ProviderException(failureMessage, KeyStore.getKeyStoreException(insertErrorCode));
        }
    }

    private byte[] generateSelfSignedCertificateBytes(KeyPair keyPair) throws ProviderException {
        try {
            return generateSelfSignedCertificate(keyPair.getPrivate(), keyPair.getPublic()).getEncoded();
        } catch (Exception e) {
            throw new ProviderException("Failed to generate self-signed certificate", e);
        } catch (CertificateEncodingException e2) {
            throw new ProviderException("Failed to obtain encoded form of self-signed certificate", e2);
        }
    }

    private Iterable<byte[]> getAttestationChain(String privateKeyAlias, KeyPair keyPair, KeymasterArguments args) throws ProviderException {
        KeymasterCertificateChain outChain = new KeymasterCertificateChain();
        int errorCode = this.mKeyStore.attestKey(privateKeyAlias, args, outChain);
        if (errorCode != 1) {
            throw new ProviderException("Failed to generate attestation certificate chain", KeyStore.getKeyStoreException(errorCode));
        }
        Collection<byte[]> chain = outChain.getCertificates();
        if (chain.size() >= 2) {
            return chain;
        }
        throw new ProviderException("Attestation certificate chain contained " + chain.size() + " entries. At least two are required.");
    }

    private void addAlgorithmSpecificParameters(KeymasterArguments keymasterArgs) {
        switch (this.mKeymasterAlgorithm) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                keymasterArgs.addUnsignedLong(KeymasterDefs.KM_TAG_RSA_PUBLIC_EXPONENT, this.mRSAPublicExponent);
            case Engine.DEFAULT_STREAM /*3*/:
            default:
                throw new ProviderException("Unsupported algorithm: " + this.mKeymasterAlgorithm);
        }
    }

    private X509Certificate generateSelfSignedCertificate(PrivateKey privateKey, PublicKey publicKey) throws CertificateParsingException, IOException {
        String signatureAlgorithm = getCertificateSignatureAlgorithm(this.mKeymasterAlgorithm, this.mKeySizeBits, this.mSpec);
        if (signatureAlgorithm == null) {
            return generateSelfSignedCertificateWithFakeSignature(publicKey);
        }
        try {
            return generateSelfSignedCertificateWithValidSignature(privateKey, publicKey, signatureAlgorithm);
        } catch (Exception e) {
            return generateSelfSignedCertificateWithFakeSignature(publicKey);
        }
    }

    private X509Certificate generateSelfSignedCertificateWithValidSignature(PrivateKey privateKey, PublicKey publicKey, String signatureAlgorithm) throws Exception {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setPublicKey(publicKey);
        certGen.setSerialNumber(this.mSpec.getCertificateSerialNumber());
        certGen.setSubjectDN(this.mSpec.getCertificateSubject());
        certGen.setIssuerDN(this.mSpec.getCertificateSubject());
        certGen.setNotBefore(this.mSpec.getCertificateNotBefore());
        certGen.setNotAfter(this.mSpec.getCertificateNotAfter());
        certGen.setSignatureAlgorithm(signatureAlgorithm);
        return certGen.generate(privateKey);
    }

    private X509Certificate generateSelfSignedCertificateWithFakeSignature(PublicKey publicKey) throws IOException, CertificateParsingException {
        AlgorithmIdentifier sigAlgId;
        byte[] signature;
        Throwable th;
        V3TBSCertificateGenerator tbsGenerator = new V3TBSCertificateGenerator();
        switch (this.mKeymasterAlgorithm) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                sigAlgId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
                signature = new byte[1];
                break;
            case Engine.DEFAULT_STREAM /*3*/:
                sigAlgId = new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
                ASN1EncodableVector v = new ASN1EncodableVector();
                v.add(new DERInteger(0));
                v.add(new DERInteger(0));
                signature = new DERSequence().getEncoded();
                break;
            default:
                throw new ProviderException("Unsupported key algorithm: " + this.mKeymasterAlgorithm);
        }
        Throwable th2 = null;
        ASN1InputStream aSN1InputStream = null;
        try {
            ASN1InputStream publicKeyInfoIn = new ASN1InputStream(publicKey.getEncoded());
            try {
                tbsGenerator.setSubjectPublicKeyInfo(SubjectPublicKeyInfo.getInstance(publicKeyInfoIn.readObject()));
                if (publicKeyInfoIn != null) {
                    try {
                        publicKeyInfoIn.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
                tbsGenerator.setSerialNumber(new ASN1Integer(this.mSpec.getCertificateSerialNumber()));
                X509Principal subject = new X509Principal(this.mSpec.getCertificateSubject().getEncoded());
                tbsGenerator.setSubject(subject);
                tbsGenerator.setIssuer(subject);
                tbsGenerator.setStartDate(new Time(this.mSpec.getCertificateNotBefore()));
                tbsGenerator.setEndDate(new Time(this.mSpec.getCertificateNotAfter()));
                tbsGenerator.setSignature(sigAlgId);
                TBSCertificate tbsCertificate = tbsGenerator.generateTBSCertificate();
                ASN1EncodableVector result = new ASN1EncodableVector();
                result.add(tbsCertificate);
                result.add(sigAlgId);
                result.add(new DERBitString(signature));
                return new X509CertificateObject(Certificate.getInstance(new DERSequence(result)));
            } catch (Throwable th4) {
                th = th4;
                aSN1InputStream = publicKeyInfoIn;
                if (aSN1InputStream != null) {
                    try {
                        aSN1InputStream.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (aSN1InputStream != null) {
                aSN1InputStream.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
        }
    }

    private static int getDefaultKeySize(int keymasterAlgorithm) {
        switch (keymasterAlgorithm) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                return RSA_DEFAULT_KEY_SIZE;
            case Engine.DEFAULT_STREAM /*3*/:
                return EC_DEFAULT_KEY_SIZE;
            default:
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        }
    }

    private static void checkValidKeySize(int keymasterAlgorithm, int keySize) throws InvalidAlgorithmParameterException {
        switch (keymasterAlgorithm) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                if (keySize < RSA_MIN_KEY_SIZE || keySize > RSA_MAX_KEY_SIZE) {
                    throw new InvalidAlgorithmParameterException("RSA key size must be >= 512 and <= 8192");
                }
            case Engine.DEFAULT_STREAM /*3*/:
                if (!SUPPORTED_EC_NIST_CURVE_SIZES.contains(Integer.valueOf(keySize))) {
                    throw new InvalidAlgorithmParameterException("Unsupported EC key size: " + keySize + " bits. Supported: " + SUPPORTED_EC_NIST_CURVE_SIZES);
                }
            default:
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        }
    }

    private static String getCertificateSignatureAlgorithm(int keymasterAlgorithm, int keySizeBits, KeyGenParameterSpec spec) {
        if ((spec.getPurposes() & 4) == 0 || spec.isUserAuthenticationRequired() || !spec.isDigestsSpecified()) {
            return null;
        }
        int bestKeymasterDigest;
        int bestDigestOutputSizeBits;
        int keymasterDigest;
        int outputSizeBits;
        switch (keymasterAlgorithm) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                if (!ArrayUtils.contains(SignaturePadding.allToKeymaster(spec.getSignaturePaddings()), 5)) {
                    return null;
                }
                int maxDigestOutputSizeBits = keySizeBits - 240;
                bestKeymasterDigest = -1;
                bestDigestOutputSizeBits = -1;
                for (Integer intValue : getAvailableKeymasterSignatureDigests(spec.getDigests(), AndroidKeyStoreBCWorkaroundProvider.getSupportedEcdsaSignatureDigests())) {
                    keymasterDigest = intValue.intValue();
                    outputSizeBits = KeymasterUtils.getDigestOutputSizeBits(keymasterDigest);
                    if (outputSizeBits <= maxDigestOutputSizeBits) {
                        if (bestKeymasterDigest == -1) {
                            bestKeymasterDigest = keymasterDigest;
                            bestDigestOutputSizeBits = outputSizeBits;
                        } else if (outputSizeBits > bestDigestOutputSizeBits) {
                            bestKeymasterDigest = keymasterDigest;
                            bestDigestOutputSizeBits = outputSizeBits;
                        }
                    }
                }
                if (bestKeymasterDigest == -1) {
                    return null;
                }
                return Digest.fromKeymasterToSignatureAlgorithmDigest(bestKeymasterDigest) + "WithRSA";
            case Engine.DEFAULT_STREAM /*3*/:
                bestKeymasterDigest = -1;
                bestDigestOutputSizeBits = -1;
                for (Integer intValue2 : getAvailableKeymasterSignatureDigests(spec.getDigests(), AndroidKeyStoreBCWorkaroundProvider.getSupportedEcdsaSignatureDigests())) {
                    keymasterDigest = intValue2.intValue();
                    outputSizeBits = KeymasterUtils.getDigestOutputSizeBits(keymasterDigest);
                    if (outputSizeBits == keySizeBits) {
                        bestKeymasterDigest = keymasterDigest;
                        bestDigestOutputSizeBits = outputSizeBits;
                        if (bestKeymasterDigest != -1) {
                            return null;
                        }
                        return Digest.fromKeymasterToSignatureAlgorithmDigest(bestKeymasterDigest) + "WithECDSA";
                    } else if (bestKeymasterDigest == -1) {
                        bestKeymasterDigest = keymasterDigest;
                        bestDigestOutputSizeBits = outputSizeBits;
                    } else if (bestDigestOutputSizeBits < keySizeBits) {
                        if (outputSizeBits > bestDigestOutputSizeBits) {
                            bestKeymasterDigest = keymasterDigest;
                            bestDigestOutputSizeBits = outputSizeBits;
                        }
                    } else if (outputSizeBits < bestDigestOutputSizeBits && outputSizeBits >= keySizeBits) {
                        bestKeymasterDigest = keymasterDigest;
                        bestDigestOutputSizeBits = outputSizeBits;
                    }
                }
                if (bestKeymasterDigest != -1) {
                    return Digest.fromKeymasterToSignatureAlgorithmDigest(bestKeymasterDigest) + "WithECDSA";
                }
                return null;
            default:
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        }
    }

    private static Set<Integer> getAvailableKeymasterSignatureDigests(String[] authorizedKeyDigests, String[] supportedSignatureDigests) {
        int i = 0;
        Set<Integer> authorizedKeymasterKeyDigests = new HashSet();
        for (int keymasterDigest : Digest.allToKeymaster(authorizedKeyDigests)) {
            authorizedKeymasterKeyDigests.add(Integer.valueOf(keymasterDigest));
        }
        Set<Integer> supportedKeymasterSignatureDigests = new HashSet();
        int[] allToKeymaster = Digest.allToKeymaster(supportedSignatureDigests);
        int length = allToKeymaster.length;
        while (i < length) {
            supportedKeymasterSignatureDigests.add(Integer.valueOf(allToKeymaster[i]));
            i++;
        }
        Set<Integer> result = new HashSet(supportedKeymasterSignatureDigests);
        result.retainAll(authorizedKeymasterKeyDigests);
        return result;
    }
}
