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
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
    private static final List<String> SUPPORTED_EC_NIST_CURVE_NAMES = new ArrayList();
    private static final Map<String, Integer> SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE = new HashMap();
    private static final List<Integer> SUPPORTED_EC_NIST_CURVE_SIZES = new ArrayList();
    private boolean mEncryptionAtRestRequired;
    private String mEntryAlias;
    private int mEntryUid;
    private String mJcaKeyAlgorithm;
    private int mKeySizeBits;
    private KeyStore mKeyStore;
    private int mKeymasterAlgorithm = -1;
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
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-224", Integer.valueOf(224));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp224r1", Integer.valueOf(224));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-256", Integer.valueOf(256));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp256r1", Integer.valueOf(256));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("prime256v1", Integer.valueOf(256));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-384", Integer.valueOf(MetricsEvent.ACTION_SHOW_SETTINGS_SUGGESTION));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp384r1", Integer.valueOf(MetricsEvent.ACTION_SHOW_SETTINGS_SUGGESTION));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-521", Integer.valueOf(521));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp521r1", Integer.valueOf(521));
        SUPPORTED_EC_NIST_CURVE_NAMES.addAll(SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.keySet());
        Collections.sort(SUPPORTED_EC_NIST_CURVE_NAMES);
        SUPPORTED_EC_NIST_CURVE_SIZES.addAll(new HashSet(SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.values()));
        Collections.sort(SUPPORTED_EC_NIST_CURVE_SIZES);
    }

    protected AndroidKeyStoreKeyPairGeneratorSpi(int keymasterAlgorithm) {
        this.mOriginalKeymasterAlgorithm = keymasterAlgorithm;
    }

    public void initialize(int keysize, SecureRandom random) {
        throw new IllegalArgumentException(KeyGenParameterSpec.class.getName() + " or " + KeyPairGeneratorSpec.class.getName() + " required to initialize this KeyPairGenerator");
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void, dom blocks: [B:2:0x0007, B:24:0x00a9, B:27:0x00af, B:53:0x022a]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x02a6 A:{Splitter: B:53:0x022a, ExcHandler: java.lang.IllegalArgumentException (r10_0 'e' java.lang.RuntimeException)} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d4 A:{Splitter: B:24:0x00a9, ExcHandler: java.lang.NullPointerException (r10_1 'e' java.lang.RuntimeException)} */
    public void initialize(java.security.spec.AlgorithmParameterSpec r21, java.security.SecureRandom r22) throws java.security.InvalidAlgorithmParameterException {
        /*
        r20 = this;
        r20.resetAll();
        r19 = 0;
        if (r21 != 0) goto L_0x003f;
    L_0x0007:
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0038 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0038 }
        r4.<init>();	 Catch:{ all -> 0x0038 }
        r5 = "Must supply params of type ";	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r5 = android.security.keystore.KeyGenParameterSpec.class;	 Catch:{ all -> 0x0038 }
        r5 = r5.getName();	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r5 = " or ";	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r5 = android.security.KeyPairGeneratorSpec.class;	 Catch:{ all -> 0x0038 }
        r5 = r5.getName();	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r4 = r4.toString();	 Catch:{ all -> 0x0038 }
        r3.<init>(r4);	 Catch:{ all -> 0x0038 }
        throw r3;	 Catch:{ all -> 0x0038 }
    L_0x0038:
        r3 = move-exception;
        if (r19 != 0) goto L_0x003e;
    L_0x003b:
        r20.resetAll();
    L_0x003e:
        throw r3;
    L_0x003f:
        r11 = 0;
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r13 = r0.mOriginalKeymasterAlgorithm;	 Catch:{ all -> 0x0038 }
        r0 = r21;	 Catch:{ all -> 0x0038 }
        r3 = r0 instanceof android.security.keystore.KeyGenParameterSpec;	 Catch:{ all -> 0x0038 }
        if (r3 == 0) goto L_0x009e;	 Catch:{ all -> 0x0038 }
    L_0x004a:
        r0 = r21;	 Catch:{ all -> 0x0038 }
        r0 = (android.security.keystore.KeyGenParameterSpec) r0;	 Catch:{ all -> 0x0038 }
        r16 = r0;	 Catch:{ all -> 0x0038 }
    L_0x0050:
        r3 = r16.getKeystoreAlias();	 Catch:{ all -> 0x0038 }
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r0.mEntryAlias = r3;	 Catch:{ all -> 0x0038 }
        r3 = r16.getUid();	 Catch:{ all -> 0x0038 }
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r0.mEntryUid = r3;	 Catch:{ all -> 0x0038 }
        r0 = r16;	 Catch:{ all -> 0x0038 }
        r1 = r20;	 Catch:{ all -> 0x0038 }
        r1.mSpec = r0;	 Catch:{ all -> 0x0038 }
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r0.mKeymasterAlgorithm = r13;	 Catch:{ all -> 0x0038 }
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r0.mEncryptionAtRestRequired = r11;	 Catch:{ all -> 0x0038 }
        r3 = r16.getKeySize();	 Catch:{ all -> 0x0038 }
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r0.mKeySizeBits = r3;	 Catch:{ all -> 0x0038 }
        r20.initAlgorithmSpecificParameters();	 Catch:{ all -> 0x0038 }
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r3 = r0.mKeySizeBits;	 Catch:{ all -> 0x0038 }
        r4 = -1;	 Catch:{ all -> 0x0038 }
        if (r3 != r4) goto L_0x0088;	 Catch:{ all -> 0x0038 }
    L_0x0080:
        r3 = getDefaultKeySize(r13);	 Catch:{ all -> 0x0038 }
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r0.mKeySizeBits = r3;	 Catch:{ all -> 0x0038 }
    L_0x0088:
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r3 = r0.mKeySizeBits;	 Catch:{ all -> 0x0038 }
        checkValidKeySize(r13, r3);	 Catch:{ all -> 0x0038 }
        r3 = r16.getKeystoreAlias();	 Catch:{ all -> 0x0038 }
        if (r3 != 0) goto L_0x022a;	 Catch:{ all -> 0x0038 }
    L_0x0095:
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0038 }
        r4 = "KeyStore entry alias not provided";	 Catch:{ all -> 0x0038 }
        r3.<init>(r4);	 Catch:{ all -> 0x0038 }
        throw r3;	 Catch:{ all -> 0x0038 }
    L_0x009e:
        r0 = r21;	 Catch:{ all -> 0x0038 }
        r3 = r0 instanceof android.security.KeyPairGeneratorSpec;	 Catch:{ all -> 0x0038 }
        if (r3 == 0) goto L_0x01e6;	 Catch:{ all -> 0x0038 }
    L_0x00a4:
        r0 = r21;	 Catch:{ all -> 0x0038 }
        r0 = (android.security.KeyPairGeneratorSpec) r0;	 Catch:{ all -> 0x0038 }
        r15 = r0;	 Catch:{ all -> 0x0038 }
        r18 = r15.getKeyType();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        if (r18 == 0) goto L_0x00b3;
    L_0x00af:
        r13 = android.security.keystore.KeyProperties.KeyAlgorithm.toKeymasterAsymmetricKeyAlgorithm(r18);	 Catch:{ IllegalArgumentException -> 0x00db }
    L_0x00b3:
        switch(r13) {
            case 1: goto L_0x0171;
            case 2: goto L_0x00b6;
            case 3: goto L_0x00e5;
            default: goto L_0x00b6;
        };
    L_0x00b6:
        r3 = new java.security.ProviderException;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = new java.lang.StringBuilder;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4.<init>();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = "Unsupported algorithm: ";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r20;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = r0.mKeymasterAlgorithm;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = r4.toString();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3.<init>(r4);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        throw r3;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
    L_0x00d4:
        r10 = move-exception;
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0038 }
        r3.<init>(r10);	 Catch:{ all -> 0x0038 }
        throw r3;	 Catch:{ all -> 0x0038 }
    L_0x00db:
        r2 = move-exception;
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "Invalid key type in parameters";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3.<init>(r4, r2);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        throw r3;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
    L_0x00e5:
        r17 = new android.security.keystore.KeyGenParameterSpec$Builder;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = r15.getKeystoreAlias();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = 12;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.<init>(r3, r4);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = 6;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = new java.lang.String[r3];	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "NONE";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 0;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-1";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 1;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-224";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 2;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-256";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 3;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-384";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-512";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 5;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setDigests(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
    L_0x011e:
        r3 = r15.getKeySize();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = -1;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        if (r3 == r4) goto L_0x012e;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
    L_0x0125:
        r3 = r15.getKeySize();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setKeySize(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
    L_0x012e:
        r3 = r15.getAlgorithmParameterSpec();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        if (r3 == 0) goto L_0x013d;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
    L_0x0134:
        r3 = r15.getAlgorithmParameterSpec();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setAlgorithmParameterSpec(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
    L_0x013d:
        r3 = r15.getSubjectDN();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setCertificateSubject(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = r15.getSerialNumber();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setCertificateSerialNumber(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = r15.getStartDate();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setCertificateNotBefore(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = r15.getEndDate();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setCertificateNotAfter(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r11 = r15.isEncryptionRequired();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = 0;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setUserAuthenticationRequired(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r16 = r17.build();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        goto L_0x0050;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
    L_0x0171:
        r17 = new android.security.keystore.KeyGenParameterSpec$Builder;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = r15.getKeystoreAlias();	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = 15;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.<init>(r3, r4);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = 7;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = new java.lang.String[r3];	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "NONE";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 0;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "MD5";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 1;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-1";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 2;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-224";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 3;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-256";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-384";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 5;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "SHA-512";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 6;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setDigests(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = 3;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = new java.lang.String[r3];	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "NoPadding";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 0;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "PKCS1Padding";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 1;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "OAEPPadding";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 2;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setEncryptionPaddings(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = 2;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = new java.lang.String[r3];	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "PKCS1";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 0;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r4 = "PSS";	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r5 = 1;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3[r5] = r4;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setSignaturePaddings(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r3 = 0;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        r0.setRandomizedEncryptionRequired(r3);	 Catch:{ NullPointerException -> 0x00d4, NullPointerException -> 0x00d4 }
        goto L_0x011e;
    L_0x01e6:
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0038 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0038 }
        r4.<init>();	 Catch:{ all -> 0x0038 }
        r5 = "Unsupported params class: ";	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r5 = r21.getClass();	 Catch:{ all -> 0x0038 }
        r5 = r5.getName();	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r5 = ". Supported: ";	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r5 = android.security.keystore.KeyGenParameterSpec.class;	 Catch:{ all -> 0x0038 }
        r5 = r5.getName();	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r5 = ", ";	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r5 = android.security.KeyPairGeneratorSpec.class;	 Catch:{ all -> 0x0038 }
        r5 = r5.getName();	 Catch:{ all -> 0x0038 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0038 }
        r4 = r4.toString();	 Catch:{ all -> 0x0038 }
        r3.<init>(r4);	 Catch:{ all -> 0x0038 }
        throw r3;	 Catch:{ all -> 0x0038 }
    L_0x022a:
        r12 = android.security.keystore.KeyProperties.KeyAlgorithm.fromKeymasterAsymmetricKeyAlgorithm(r13);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = r16.getPurposes();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = android.security.keystore.KeyProperties.Purpose.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0.mKeymasterPurposes = r3;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = r16.getBlockModes();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = android.security.keystore.KeyProperties.BlockMode.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0.mKeymasterBlockModes = r3;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = r16.getEncryptionPaddings();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = android.security.keystore.KeyProperties.EncryptionPadding.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0.mKeymasterEncryptionPaddings = r3;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = r16.getPurposes();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = r3 & 1;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        if (r3 == 0) goto L_0x02b0;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
    L_0x025a:
        r3 = r16.isRandomizedEncryptionRequired();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        if (r3 == 0) goto L_0x02b0;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
    L_0x0260:
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = r0.mKeymasterEncryptionPaddings;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = 0;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r5 = r4.length;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
    L_0x0266:
        if (r3 >= r5) goto L_0x02b0;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
    L_0x0268:
        r14 = r4[r3];	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r6 = android.security.keystore.KeymasterUtils.isKeymasterPaddingSchemeIndCpaCompatibleWithAsymmetricCrypto(r14);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        if (r6 != 0) goto L_0x02ad;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
    L_0x0270:
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = new java.lang.StringBuilder;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4.<init>();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r5 = "Randomized encryption (IND-CPA) required but may be violated by padding scheme: ";	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r5 = android.security.keystore.KeyProperties.EncryptionPadding.fromKeymaster(r14);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r5 = ". See ";	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r5 = android.security.keystore.KeyGenParameterSpec.class;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r5 = r5.getName();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r5 = " documentation.";	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = r4.toString();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3.<init>(r4);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        throw r3;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
    L_0x02a6:
        r10 = move-exception;
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0038 }
        r3.<init>(r10);	 Catch:{ all -> 0x0038 }
        throw r3;	 Catch:{ all -> 0x0038 }
    L_0x02ad:
        r3 = r3 + 1;
        goto L_0x0266;
    L_0x02b0:
        r3 = r16.getSignaturePaddings();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = android.security.keystore.KeyProperties.SignaturePadding.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0.mKeymasterSignaturePaddings = r3;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = r16.isDigestsSpecified();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        if (r3 == 0) goto L_0x0312;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
    L_0x02c2:
        r3 = r16.getDigests();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3 = android.security.keystore.KeyProperties.Digest.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0.mKeymasterDigests = r3;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
    L_0x02ce:
        r3 = new android.security.keymaster.KeymasterArguments;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r3.<init>();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r4 = r4.isUserAuthenticationRequired();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r5 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r5 = r5.getUserAuthenticationValidityDurationSeconds();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r6 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r6 = r6.isUserAuthenticationValidWhileOnBody();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r7 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r7 = r7.isInvalidatedByBiometricEnrollment();	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r8 = 0;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        android.security.keystore.KeymasterUtils.addUserAuthArgs(r3, r4, r5, r6, r7, r8);	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r0.mJcaKeyAlgorithm = r12;	 Catch:{ all -> 0x0038 }
        r0 = r22;	 Catch:{ all -> 0x0038 }
        r1 = r20;	 Catch:{ all -> 0x0038 }
        r1.mRng = r0;	 Catch:{ all -> 0x0038 }
        r3 = android.security.KeyStore.getInstance();	 Catch:{ all -> 0x0038 }
        r0 = r20;	 Catch:{ all -> 0x0038 }
        r0.mKeyStore = r3;	 Catch:{ all -> 0x0038 }
        r19 = 1;
        if (r19 != 0) goto L_0x0311;
    L_0x030e:
        r20.resetAll();
    L_0x0311:
        return;
    L_0x0312:
        r3 = libcore.util.EmptyArray.INT;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0 = r20;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        r0.mKeymasterDigests = r3;	 Catch:{ IllegalArgumentException -> 0x02a6, IllegalArgumentException -> 0x02a6 }
        goto L_0x02ce;
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
            case 1:
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
                    return;
                }
            case 3:
                if (algSpecificSpec instanceof ECGenParameterSpec) {
                    String curveName = ((ECGenParameterSpec) algSpecificSpec).getName();
                    Integer ecSpecKeySizeBits = (Integer) SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.get(curveName.toLowerCase(Locale.US));
                    if (ecSpecKeySizeBits == null) {
                        throw new InvalidAlgorithmParameterException("Unsupported EC curve name: " + curveName + ". Supported: " + SUPPORTED_EC_NIST_CURVE_NAMES);
                    } else if (this.mKeySizeBits == -1) {
                        this.mKeySizeBits = ecSpecKeySizeBits.intValue();
                        return;
                    } else if (this.mKeySizeBits != ecSpecKeySizeBits.intValue()) {
                        throw new InvalidAlgorithmParameterException("EC key size must match  between " + this.mSpec + " and " + algSpecificSpec + ": " + this.mKeySizeBits + " vs " + ecSpecKeySizeBits);
                    } else {
                        return;
                    }
                } else if (algSpecificSpec != null) {
                    throw new InvalidAlgorithmParameterException("EC may only use ECGenParameterSpec");
                } else {
                    return;
                }
            default:
                throw new ProviderException("Unsupported algorithm: " + this.mKeymasterAlgorithm);
        }
    }

    public KeyPair generateKeyPair() {
        if (this.mKeyStore == null || this.mSpec == null) {
            throw new IllegalStateException("Not initialized");
        }
        int flags = this.mEncryptionAtRestRequired ? 1 : 0;
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
        KeymasterUtils.addUserAuthArgs(args, this.mSpec.isUserAuthenticationRequired(), this.mSpec.getUserAuthenticationValidityDurationSeconds(), this.mSpec.isUserAuthenticationValidWhileOnBody(), this.mSpec.isInvalidatedByBiometricEnrollment(), 0);
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

    /* JADX WARNING: Removed duplicated region for block: B:6:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:6:0x001b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:8:0x0024, code:
            throw new java.security.ProviderException("Failed to generate self-signed certificate", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] generateSelfSignedCertificateBytes(KeyPair keyPair) throws ProviderException {
        try {
            return generateSelfSignedCertificate(keyPair.getPrivate(), keyPair.getPublic()).getEncoded();
        } catch (Exception e) {
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
            case 1:
                keymasterArgs.addUnsignedLong(KeymasterDefs.KM_TAG_RSA_PUBLIC_EXPONENT, this.mRSAPublicExponent);
                return;
            case 3:
                return;
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

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0084 A:{SYNTHETIC, Splitter: B:23:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0095  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0089  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private X509Certificate generateSelfSignedCertificateWithFakeSignature(PublicKey publicKey) throws IOException, CertificateParsingException {
        AlgorithmIdentifier sigAlgId;
        byte[] signature;
        Throwable th;
        V3TBSCertificateGenerator tbsGenerator = new V3TBSCertificateGenerator();
        switch (this.mKeymasterAlgorithm) {
            case 1:
                sigAlgId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
                signature = new byte[1];
                break;
            case 3:
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
        ASN1InputStream publicKeyInfoIn = null;
        try {
            ASN1InputStream publicKeyInfoIn2 = new ASN1InputStream(publicKey.getEncoded());
            try {
                tbsGenerator.setSubjectPublicKeyInfo(SubjectPublicKeyInfo.getInstance(publicKeyInfoIn2.readObject()));
                if (publicKeyInfoIn2 != null) {
                    try {
                        publicKeyInfoIn2.close();
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
                publicKeyInfoIn = publicKeyInfoIn2;
                if (publicKeyInfoIn != null) {
                    try {
                        publicKeyInfoIn.close();
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
            if (publicKeyInfoIn != null) {
            }
            if (th2 == null) {
            }
        }
    }

    private static int getDefaultKeySize(int keymasterAlgorithm) {
        switch (keymasterAlgorithm) {
            case 1:
                return 2048;
            case 3:
                return 256;
            default:
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        }
    }

    private static void checkValidKeySize(int keymasterAlgorithm, int keySize) throws InvalidAlgorithmParameterException {
        switch (keymasterAlgorithm) {
            case 1:
                if (keySize < 512 || keySize > 8192) {
                    throw new InvalidAlgorithmParameterException("RSA key size must be >= 512 and <= 8192");
                }
                return;
            case 3:
                if (!SUPPORTED_EC_NIST_CURVE_SIZES.contains(Integer.valueOf(keySize))) {
                    throw new InvalidAlgorithmParameterException("Unsupported EC key size: " + keySize + " bits. Supported: " + SUPPORTED_EC_NIST_CURVE_SIZES);
                }
                return;
            default:
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0062 A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getCertificateSignatureAlgorithm(int keymasterAlgorithm, int keySizeBits, KeyGenParameterSpec spec) {
        if ((spec.getPurposes() & 4) == 0 || spec.isUserAuthenticationRequired() || !spec.isDigestsSpecified()) {
            return null;
        }
        int bestKeymasterDigest;
        int bestDigestOutputSizeBits;
        int keymasterDigest;
        int outputSizeBits;
        switch (keymasterAlgorithm) {
            case 1:
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
            case 3:
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
                }
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
