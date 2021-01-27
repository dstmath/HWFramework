package android.util.apk;

import android.util.ArrayMap;
import android.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.GeneralSecurityException;
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0013, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0017, code lost:
        throw r2;
     */
    public static boolean hasSignature(String apkFile) throws IOException {
        try {
            RandomAccessFile apk = new RandomAccessFile(apkFile, "r");
            findSignature(apk);
            $closeResource(null, apk);
            return true;
        } catch (SignatureNotFoundException e) {
            return false;
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

    public static X509Certificate[][] unsafeGetCertsWithoutVerification(String apkFile) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apkFile, false).certs;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0013, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
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

    /* JADX INFO: Multiple debug info for r2v13 int: [D('digestAlgorithm' int), D('certificateCount' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x023e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x023e A[ExcHandler: InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException (e java.lang.Throwable), Splitter:B:32:0x00bb] */
    private static X509Certificate[] verifySigner(ByteBuffer signerBlock, Map<Integer, byte[]> contentDigests, CertificateFactory certFactory) throws SecurityException, IOException {
        String jcaSignatureAlgorithm;
        GeneralSecurityException e;
        CertificateException e2;
        Exception e3;
        ByteBuffer signedData = ApkSigningBlockUtils.getLengthPrefixedSlice(signerBlock);
        ByteBuffer signatures = ApkSigningBlockUtils.getLengthPrefixedSlice(signerBlock);
        byte[] publicKeyBytes = ApkSigningBlockUtils.readLengthPrefixedByteArray(signerBlock);
        int bestSigAlgorithm = -1;
        List<Integer> signaturesSigAlgorithms = new ArrayList<>();
        byte[] bestSigAlgorithmSignatureBytes = null;
        int signatureCount = 0;
        while (signatures.hasRemaining()) {
            signatureCount++;
            try {
                ByteBuffer signature = ApkSigningBlockUtils.getLengthPrefixedSlice(signatures);
                if (signature.remaining() >= 8) {
                    int sigAlgorithm = signature.getInt();
                    signaturesSigAlgorithms.add(Integer.valueOf(sigAlgorithm));
                    if (isSupportedSignatureAlgorithm(sigAlgorithm)) {
                        if (bestSigAlgorithm == -1 || ApkSigningBlockUtils.compareSignatureAlgorithm(sigAlgorithm, bestSigAlgorithm) > 0) {
                            bestSigAlgorithm = sigAlgorithm;
                            bestSigAlgorithmSignatureBytes = ApkSigningBlockUtils.readLengthPrefixedByteArray(signature);
                        }
                    }
                } else {
                    throw new SecurityException("Signature record too short");
                }
            } catch (IOException | BufferUnderflowException e4) {
                throw new SecurityException("Failed to parse signature record #" + signatureCount, e4);
            }
        }
        if (bestSigAlgorithm != -1) {
            String keyAlgorithm = ApkSigningBlockUtils.getSignatureAlgorithmJcaKeyAlgorithm(bestSigAlgorithm);
            Pair<String, ? extends AlgorithmParameterSpec> signatureAlgorithmParams = ApkSigningBlockUtils.getSignatureAlgorithmJcaSignatureAlgorithm(bestSigAlgorithm);
            jcaSignatureAlgorithm = signatureAlgorithmParams.first;
            AlgorithmParameterSpec jcaSignatureAlgorithmParams = signatureAlgorithmParams.second;
            PublicKey publicKey = KeyFactory.getInstance(keyAlgorithm).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            Signature sig = Signature.getInstance(jcaSignatureAlgorithm);
            sig.initVerify(publicKey);
            if (jcaSignatureAlgorithmParams != null) {
                try {
                    sig.setParameter(jcaSignatureAlgorithmParams);
                } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e5) {
                    e = e5;
                }
            }
            try {
                sig.update(signedData);
                if (sig.verify(bestSigAlgorithmSignatureBytes)) {
                    signedData.clear();
                    ByteBuffer digests = ApkSigningBlockUtils.getLengthPrefixedSlice(signedData);
                    List<Integer> digestsSigAlgorithms = new ArrayList<>();
                    int digestCount = 0;
                    byte[] contentDigest = null;
                    while (digests.hasRemaining()) {
                        int digestCount2 = digestCount + 1;
                        try {
                            ByteBuffer digest = ApkSigningBlockUtils.getLengthPrefixedSlice(digests);
                            if (digest.remaining() >= 8) {
                                try {
                                    int sigAlgorithm2 = digest.getInt();
                                    digestsSigAlgorithms.add(Integer.valueOf(sigAlgorithm2));
                                    if (sigAlgorithm2 == bestSigAlgorithm) {
                                        contentDigest = ApkSigningBlockUtils.readLengthPrefixedByteArray(digest);
                                    }
                                    digestCount = digestCount2;
                                    signatures = signatures;
                                } catch (IOException | BufferUnderflowException e6) {
                                    e3 = e6;
                                    throw new IOException("Failed to parse digest record #" + digestCount2, e3);
                                }
                            } else {
                                throw new IOException("Record too short");
                            }
                        } catch (IOException | BufferUnderflowException e7) {
                            e3 = e7;
                            throw new IOException("Failed to parse digest record #" + digestCount2, e3);
                        }
                    }
                    if (signaturesSigAlgorithms.equals(digestsSigAlgorithms)) {
                        int certificateCount = ApkSigningBlockUtils.getSignatureAlgorithmContentDigestAlgorithm(bestSigAlgorithm);
                        byte[] previousSignerDigest = contentDigests.put(Integer.valueOf(certificateCount), contentDigest);
                        if (previousSignerDigest != null) {
                            if (!MessageDigest.isEqual(previousSignerDigest, contentDigest)) {
                                throw new SecurityException(ApkSigningBlockUtils.getContentDigestAlgorithmJcaDigestAlgorithm(certificateCount) + " contents digest does not match the digest specified by a preceding signer");
                            }
                        }
                        List<X509Certificate> certs = new ArrayList<>();
                        int certificateCount2 = 0;
                        for (ByteBuffer certificates = ApkSigningBlockUtils.getLengthPrefixedSlice(signedData); certificates.hasRemaining(); certificates = certificates) {
                            int certificateCount3 = certificateCount2 + 1;
                            byte[] encodedCert = ApkSigningBlockUtils.readLengthPrefixedByteArray(certificates);
                            try {
                                try {
                                    certs.add(new VerbatimX509Certificate((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedCert)), encodedCert));
                                    certificateCount2 = certificateCount3;
                                    certificateCount = certificateCount;
                                    signaturesSigAlgorithms = signaturesSigAlgorithms;
                                } catch (CertificateException e8) {
                                    e2 = e8;
                                    throw new SecurityException("Failed to decode certificate #" + certificateCount3, e2);
                                }
                            } catch (CertificateException e9) {
                                e2 = e9;
                                throw new SecurityException("Failed to decode certificate #" + certificateCount3, e2);
                            }
                        }
                        if (certs.isEmpty()) {
                            throw new SecurityException("No certificates listed");
                        } else if (Arrays.equals(publicKeyBytes, certs.get(0).getPublicKey().getEncoded())) {
                            verifyAdditionalAttributes(ApkSigningBlockUtils.getLengthPrefixedSlice(signedData));
                            return (X509Certificate[]) certs.toArray(new X509Certificate[certs.size()]);
                        } else {
                            throw new SecurityException("Public key mismatch between certificate and signature record");
                        }
                    } else {
                        throw new SecurityException("Signature algorithms don't match between digests and signatures records");
                    }
                } else {
                    throw new SecurityException(jcaSignatureAlgorithm + " signature did not verify");
                }
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e10) {
            }
        } else if (signatureCount == 0) {
            throw new SecurityException("No signatures found");
        } else {
            throw new SecurityException("No supported signatures found");
        }
        throw new SecurityException("Failed to verify " + jcaSignatureAlgorithm + " signature", e);
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001b, code lost:
        $closeResource(r1, r0);
     */
    static byte[] getVerityRootHash(String apkPath) throws IOException, SignatureNotFoundException, SecurityException {
        RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
        findSignature(apk);
        byte[] bArr = verify(apk, false).verityRootHash;
        $closeResource(null, apk);
        return bArr;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001b, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
        $closeResource(r1, r0);
     */
    static byte[] generateApkVerity(String apkPath, ByteBufferFactory bufferFactory) throws IOException, SignatureNotFoundException, SecurityException, DigestException, NoSuchAlgorithmException {
        RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
        byte[] generateApkVerity = VerityBuilder.generateApkVerity(apkPath, bufferFactory, findSignature(apk));
        $closeResource(null, apk);
        return generateApkVerity;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002b, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002f, code lost:
        throw r2;
     */
    static byte[] generateApkVerityRootHash(String apkPath) throws IOException, SignatureNotFoundException, DigestException, NoSuchAlgorithmException {
        RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
        SignatureInfo signatureInfo = findSignature(apk);
        VerifiedSigner vSigner = verify(apk, false);
        if (vSigner.verityRootHash == null) {
            $closeResource(null, apk);
            return null;
        }
        byte[] generateApkVerityRootHash = VerityBuilder.generateApkVerityRootHash(apk, ByteBuffer.wrap(vSigner.verityRootHash), signatureInfo);
        $closeResource(null, apk);
        return generateApkVerityRootHash;
    }

    private static boolean isSupportedSignatureAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm == 513 || sigAlgorithm == 514 || sigAlgorithm == 769 || sigAlgorithm == 1057 || sigAlgorithm == 1059 || sigAlgorithm == 1061) {
            return true;
        }
        switch (sigAlgorithm) {
            case 257:
            case 258:
            case 259:
            case 260:
                return true;
            default:
                return false;
        }
    }

    public static class VerifiedSigner {
        public final X509Certificate[][] certs;
        public final byte[] verityRootHash;

        public VerifiedSigner(X509Certificate[][] certs2, byte[] verityRootHash2) {
            this.certs = certs2;
            this.verityRootHash = verityRootHash2;
        }
    }
}
