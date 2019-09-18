package android.util.apk;

import android.util.ArrayMap;
import android.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ApkSignatureSchemeV2Verifier {
    private static final int APK_SIGNATURE_SCHEME_V2_BLOCK_ID = 1896449818;
    public static final int SF_ATTRIBUTE_ANDROID_APK_SIGNED_ID = 2;
    private static final int STRIPPING_PROTECTION_ATTR_ID = -1091571699;

    public static class VerifiedSigner {
        public final X509Certificate[][] certs;
        public final byte[] verityRootHash;

        public VerifiedSigner(X509Certificate[][] certs2, byte[] verityRootHash2) {
            this.certs = certs2;
            this.verityRootHash = verityRootHash2;
        }
    }

    public static boolean hasSignature(String apkFile) throws IOException {
        RandomAccessFile apk;
        try {
            apk = new RandomAccessFile(apkFile, "r");
            findSignature(apk);
            $closeResource(null, apk);
            return true;
        } catch (SignatureNotFoundException e) {
            return false;
        } catch (Throwable th) {
            $closeResource(r1, apk);
            throw th;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public static X509Certificate[][] verify(String apkFile) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apkFile, true).certs;
    }

    public static X509Certificate[][] plsCertsNoVerifyOnlyCerts(String apkFile) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apkFile, false).certs;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0018, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0011, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        $closeResource(r1, r0);
     */
    private static VerifiedSigner verify(String apkFile, boolean verifyIntegrity) throws SignatureNotFoundException, SecurityException, IOException {
        RandomAccessFile apk = new RandomAccessFile(apkFile, "r");
        VerifiedSigner verify = verify(apk, verifyIntegrity);
        $closeResource(null, apk);
        return verify;
    }

    private static VerifiedSigner verify(RandomAccessFile apk, boolean verifyIntegrity) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apk, findSignature(apk), verifyIntegrity);
    }

    private static SignatureInfo findSignature(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        return ApkSigningBlockUtils.findSignature(apk, APK_SIGNATURE_SCHEME_V2_BLOCK_ID);
    }

    private static VerifiedSigner verify(RandomAccessFile apk, SignatureInfo signatureInfo, boolean doVerifyIntegrity) throws SecurityException, IOException {
        int signerCount = 0;
        Map<Integer, byte[]> contentDigests = new ArrayMap<>();
        List<X509Certificate[]> signerCerts = new ArrayList<>();
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            try {
                ByteBuffer signers = ApkSigningBlockUtils.getLengthPrefixedSlice(signatureInfo.signatureBlock);
                while (signers.hasRemaining()) {
                    signerCount++;
                    try {
                        signerCerts.add(verifySigner(ApkSigningBlockUtils.getLengthPrefixedSlice(signers), contentDigests, certFactory));
                    } catch (IOException | SecurityException | BufferUnderflowException e) {
                        throw new SecurityException("Failed to parse/verify signer #" + signerCount + " block", e);
                    }
                }
                if (signerCount < 1) {
                    throw new SecurityException("No signers found");
                } else if (!contentDigests.isEmpty()) {
                    if (doVerifyIntegrity) {
                        ApkSigningBlockUtils.verifyIntegrity(contentDigests, apk, signatureInfo);
                    }
                    byte[] verityRootHash = null;
                    if (contentDigests.containsKey(3)) {
                        verityRootHash = ApkSigningBlockUtils.parseVerityDigestAndVerifySourceLength(contentDigests.get(3), apk.length(), signatureInfo);
                    }
                    return new VerifiedSigner((X509Certificate[][]) signerCerts.toArray(new X509Certificate[signerCerts.size()][]), verityRootHash);
                } else {
                    throw new SecurityException("No content digests found");
                }
            } catch (IOException e2) {
                throw new SecurityException("Failed to read list of signers", e2);
            }
        } catch (CertificateException e3) {
            throw new RuntimeException("Failed to obtain X.509 CertificateFactory", e3);
        }
    }

    private static X509Certificate[] verifySigner(ByteBuffer signerBlock, Map<Integer, byte[]> contentDigests, CertificateFactory certFactory) throws SecurityException, IOException {
        byte[] encodedCert;
        ByteBuffer signedData = ApkSigningBlockUtils.getLengthPrefixedSlice(signerBlock);
        ByteBuffer signatures = ApkSigningBlockUtils.getLengthPrefixedSlice(signerBlock);
        byte[] publicKeyBytes = ApkSigningBlockUtils.readLengthPrefixedByteArray(signerBlock);
        ArrayList arrayList = new ArrayList();
        byte[] bestSigAlgorithmSignatureBytes = null;
        int bestSigAlgorithm = -1;
        int signatureCount = 0;
        while (signatures.hasRemaining() != 0) {
            signatureCount++;
            try {
                ByteBuffer signature = ApkSigningBlockUtils.getLengthPrefixedSlice(signatures);
                if (signature.remaining() >= 8) {
                    int sigAlgorithm = signature.getInt();
                    arrayList.add(Integer.valueOf(sigAlgorithm));
                    if (isSupportedSignatureAlgorithm(sigAlgorithm)) {
                        if (bestSigAlgorithm == -1 || ApkSigningBlockUtils.compareSignatureAlgorithm(sigAlgorithm, bestSigAlgorithm) > 0) {
                            bestSigAlgorithm = sigAlgorithm;
                            bestSigAlgorithmSignatureBytes = ApkSigningBlockUtils.readLengthPrefixedByteArray(signature);
                        }
                    }
                } else {
                    throw new SecurityException("Signature record too short");
                }
            } catch (IOException | BufferUnderflowException e) {
                throw new SecurityException("Failed to parse signature record #" + signatureCount, e);
            }
        }
        if (bestSigAlgorithm != -1) {
            String keyAlgorithm = ApkSigningBlockUtils.getSignatureAlgorithmJcaKeyAlgorithm(bestSigAlgorithm);
            Pair<String, ? extends AlgorithmParameterSpec> signatureAlgorithmParams = ApkSigningBlockUtils.getSignatureAlgorithmJcaSignatureAlgorithm(bestSigAlgorithm);
            String jcaSignatureAlgorithm = (String) signatureAlgorithmParams.first;
            AlgorithmParameterSpec jcaSignatureAlgorithmParams = (AlgorithmParameterSpec) signatureAlgorithmParams.second;
            try {
                PublicKey publicKey = KeyFactory.getInstance(keyAlgorithm).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
                Signature sig = Signature.getInstance(jcaSignatureAlgorithm);
                sig.initVerify(publicKey);
                if (jcaSignatureAlgorithmParams != null) {
                    try {
                        sig.setParameter(jcaSignatureAlgorithmParams);
                    } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e2) {
                        e = e2;
                        ByteBuffer byteBuffer = signatures;
                        int i = signatureCount;
                        int i2 = bestSigAlgorithm;
                        ArrayList arrayList2 = arrayList;
                        byte[] bArr = bestSigAlgorithmSignatureBytes;
                        String str = keyAlgorithm;
                    }
                }
                sig.update(signedData);
                if (sig.verify(bestSigAlgorithmSignatureBytes)) {
                    signedData.clear();
                    ByteBuffer digests = ApkSigningBlockUtils.getLengthPrefixedSlice(signedData);
                    List<Integer> digestsSigAlgorithms = new ArrayList<>();
                    byte[] contentDigest = null;
                    int digestCount = 0;
                    while (true) {
                        int digestCount2 = digestCount;
                        if (digests.hasRemaining() != 0) {
                            ByteBuffer signatures2 = signatures;
                            int digestCount3 = digestCount2 + 1;
                            try {
                                ByteBuffer digest = ApkSigningBlockUtils.getLengthPrefixedSlice(digests);
                                int signatureCount2 = signatureCount;
                                try {
                                    byte[] bestSigAlgorithmSignatureBytes2 = bestSigAlgorithmSignatureBytes;
                                    if (digest.remaining() >= 8) {
                                        try {
                                            int sigAlgorithm2 = digest.getInt();
                                            digestsSigAlgorithms.add(Integer.valueOf(sigAlgorithm2));
                                            if (sigAlgorithm2 == bestSigAlgorithm) {
                                                contentDigest = ApkSigningBlockUtils.readLengthPrefixedByteArray(digest);
                                            }
                                            digestCount = digestCount3;
                                            signatures = signatures2;
                                            signatureCount = signatureCount2;
                                            bestSigAlgorithmSignatureBytes = bestSigAlgorithmSignatureBytes2;
                                        } catch (IOException | BufferUnderflowException e3) {
                                            e = e3;
                                            StringBuilder sb = new StringBuilder();
                                            String str2 = keyAlgorithm;
                                            sb.append("Failed to parse digest record #");
                                            sb.append(digestCount3);
                                            throw new IOException(sb.toString(), e);
                                        }
                                    } else {
                                        throw new IOException("Record too short");
                                    }
                                } catch (IOException | BufferUnderflowException e4) {
                                    e = e4;
                                    byte[] bArr2 = bestSigAlgorithmSignatureBytes;
                                    StringBuilder sb2 = new StringBuilder();
                                    String str22 = keyAlgorithm;
                                    sb2.append("Failed to parse digest record #");
                                    sb2.append(digestCount3);
                                    throw new IOException(sb2.toString(), e);
                                }
                            } catch (IOException | BufferUnderflowException e5) {
                                e = e5;
                                int i3 = signatureCount;
                                byte[] bArr3 = bestSigAlgorithmSignatureBytes;
                                StringBuilder sb22 = new StringBuilder();
                                String str222 = keyAlgorithm;
                                sb22.append("Failed to parse digest record #");
                                sb22.append(digestCount3);
                                throw new IOException(sb22.toString(), e);
                            }
                        } else {
                            int i4 = signatureCount;
                            byte[] bArr4 = bestSigAlgorithmSignatureBytes;
                            String str3 = keyAlgorithm;
                            if (arrayList.equals(digestsSigAlgorithms)) {
                                int certificateCount = ApkSigningBlockUtils.getSignatureAlgorithmContentDigestAlgorithm(bestSigAlgorithm);
                                byte[] previousSignerDigest = contentDigests.put(Integer.valueOf(certificateCount), contentDigest);
                                if (previousSignerDigest == null || MessageDigest.isEqual(previousSignerDigest, contentDigest)) {
                                    ByteBuffer certificates = ApkSigningBlockUtils.getLengthPrefixedSlice(signedData);
                                    List<X509Certificate> certs = new ArrayList<>();
                                    int certificateCount2 = 0;
                                    while (certificates.hasRemaining()) {
                                        int digestAlgorithm = certificateCount;
                                        int digestAlgorithm2 = certificateCount2 + 1;
                                        byte[] encodedCert2 = ApkSigningBlockUtils.readLengthPrefixedByteArray(certificates);
                                        try {
                                            ByteBuffer certificates2 = certificates;
                                            encodedCert = encodedCert2;
                                            try {
                                                int bestSigAlgorithm2 = bestSigAlgorithm;
                                                try {
                                                    certs.add(new VerbatimX509Certificate((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedCert)), encodedCert));
                                                    certificateCount2 = digestAlgorithm2;
                                                    certificateCount = digestAlgorithm;
                                                    certificates = certificates2;
                                                    bestSigAlgorithm = bestSigAlgorithm2;
                                                } catch (CertificateException e6) {
                                                    e = e6;
                                                    byte[] bArr5 = encodedCert;
                                                    StringBuilder sb3 = new StringBuilder();
                                                    ArrayList arrayList3 = arrayList;
                                                    sb3.append("Failed to decode certificate #");
                                                    sb3.append(digestAlgorithm2);
                                                    throw new SecurityException(sb3.toString(), e);
                                                }
                                            } catch (CertificateException e7) {
                                                e = e7;
                                                int i5 = bestSigAlgorithm;
                                                byte[] bArr52 = encodedCert;
                                                StringBuilder sb32 = new StringBuilder();
                                                ArrayList arrayList32 = arrayList;
                                                sb32.append("Failed to decode certificate #");
                                                sb32.append(digestAlgorithm2);
                                                throw new SecurityException(sb32.toString(), e);
                                            }
                                        } catch (CertificateException e8) {
                                            e = e8;
                                            ByteBuffer byteBuffer2 = certificates;
                                            int i6 = bestSigAlgorithm;
                                            encodedCert = encodedCert2;
                                            byte[] bArr522 = encodedCert;
                                            StringBuilder sb322 = new StringBuilder();
                                            ArrayList arrayList322 = arrayList;
                                            sb322.append("Failed to decode certificate #");
                                            sb322.append(digestAlgorithm2);
                                            throw new SecurityException(sb322.toString(), e);
                                        }
                                    }
                                    int digestAlgorithm3 = certificateCount;
                                    ByteBuffer byteBuffer3 = certificates;
                                    int i7 = bestSigAlgorithm;
                                    ArrayList arrayList4 = arrayList;
                                    if (certs.isEmpty() != 0) {
                                        throw new SecurityException("No certificates listed");
                                    } else if (Arrays.equals(publicKeyBytes, certs.get(0).getPublicKey().getEncoded())) {
                                        verifyAdditionalAttributes(ApkSigningBlockUtils.getLengthPrefixedSlice(signedData));
                                        return (X509Certificate[]) certs.toArray(new X509Certificate[certs.size()]);
                                    } else {
                                        throw new SecurityException("Public key mismatch between certificate and signature record");
                                    }
                                } else {
                                    throw new SecurityException(ApkSigningBlockUtils.getContentDigestAlgorithmJcaDigestAlgorithm(certificateCount) + " contents digest does not match the digest specified by a preceding signer");
                                }
                            } else {
                                ArrayList arrayList5 = arrayList;
                                throw new SecurityException("Signature algorithms don't match between digests and signatures records");
                            }
                        }
                    }
                } else {
                    int i8 = signatureCount;
                    int i9 = bestSigAlgorithm;
                    ArrayList arrayList6 = arrayList;
                    byte[] bArr6 = bestSigAlgorithmSignatureBytes;
                    String str4 = keyAlgorithm;
                    throw new SecurityException(jcaSignatureAlgorithm + " signature did not verify");
                }
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e9) {
                e = e9;
                ByteBuffer byteBuffer4 = signatures;
                int i10 = signatureCount;
                int i11 = bestSigAlgorithm;
                ArrayList arrayList7 = arrayList;
                byte[] bArr7 = bestSigAlgorithmSignatureBytes;
                String str5 = keyAlgorithm;
                throw new SecurityException("Failed to verify " + jcaSignatureAlgorithm + " signature", e);
            }
        } else if (signatureCount == 0) {
            throw new SecurityException("No signatures found");
        } else {
            throw new SecurityException("No supported signatures found");
        }
    }

    private static void verifyAdditionalAttributes(ByteBuffer attrs) throws SecurityException, IOException {
        while (attrs.hasRemaining()) {
            ByteBuffer attr = ApkSigningBlockUtils.getLengthPrefixedSlice(attrs);
            if (attr.remaining() < 4) {
                throw new IOException("Remaining buffer too short to contain additional attribute ID. Remaining: " + attr.remaining());
            } else if (attr.getInt() == STRIPPING_PROTECTION_ATTR_ID) {
                if (attr.remaining() < 4) {
                    throw new IOException("V2 Signature Scheme Stripping Protection Attribute  value too small.  Expected 4 bytes, but found " + attr.remaining());
                } else if (attr.getInt() == 3) {
                    throw new SecurityException("V2 signature indicates APK is signed using APK Signature Scheme v3, but none was found. Signature stripped?");
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0018, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
        $closeResource(r1, r0);
     */
    static byte[] getVerityRootHash(String apkPath) throws IOException, SignatureNotFoundException, SecurityException {
        RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
        SignatureInfo findSignature = findSignature(apk);
        byte[] bArr = verify(apk, false).verityRootHash;
        $closeResource(null, apk);
        return bArr;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0015, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
        $closeResource(r1, r0);
     */
    static byte[] generateApkVerity(String apkPath, ByteBufferFactory bufferFactory) throws IOException, SignatureNotFoundException, SecurityException, DigestException, NoSuchAlgorithmException {
        RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
        byte[] generateApkVerity = ApkSigningBlockUtils.generateApkVerity(apkPath, bufferFactory, findSignature(apk));
        $closeResource(null, apk);
        return generateApkVerity;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0029, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002d, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0030, code lost:
        throw r2;
     */
    static byte[] generateFsverityRootHash(String apkPath) throws IOException, SignatureNotFoundException, DigestException, NoSuchAlgorithmException {
        RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
        SignatureInfo signatureInfo = findSignature(apk);
        VerifiedSigner vSigner = verify(apk, false);
        if (vSigner.verityRootHash == null) {
            $closeResource(null, apk);
            return null;
        }
        byte[] generateFsverityRootHash = ApkVerityBuilder.generateFsverityRootHash(apk, ByteBuffer.wrap(vSigner.verityRootHash), signatureInfo);
        $closeResource(null, apk);
        return generateFsverityRootHash;
    }

    private static boolean isSupportedSignatureAlgorithm(int sigAlgorithm) {
        if (!(sigAlgorithm == 769 || sigAlgorithm == 1057 || sigAlgorithm == 1059 || sigAlgorithm == 1061)) {
            switch (sigAlgorithm) {
                case 257:
                case 258:
                case 259:
                case 260:
                    break;
                default:
                    switch (sigAlgorithm) {
                        case 513:
                        case 514:
                            break;
                        default:
                            return false;
                    }
            }
        }
        return true;
    }
}
