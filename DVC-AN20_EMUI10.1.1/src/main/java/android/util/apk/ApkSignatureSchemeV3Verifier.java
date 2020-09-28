package android.util.apk;

import android.os.Build;
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
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ApkSignatureSchemeV3Verifier {
    private static final int APK_SIGNATURE_SCHEME_V3_BLOCK_ID = -262969152;
    private static final int PROOF_OF_ROTATION_ATTR_ID = 1000370060;
    public static final int SF_ATTRIBUTE_ANDROID_APK_SIGNED_ID = 3;

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

    public static VerifiedSigner verify(String apkFile) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apkFile, true);
    }

    public static VerifiedSigner unsafeGetCertsWithoutVerification(String apkFile) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apkFile, false);
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
        return ApkSigningBlockUtils.findSignature(apk, APK_SIGNATURE_SCHEME_V3_BLOCK_ID);
    }

    private static VerifiedSigner verify(RandomAccessFile apk, SignatureInfo signatureInfo, boolean doVerifyIntegrity) throws SecurityException, IOException {
        int signerCount = 0;
        Map<Integer, byte[]> contentDigests = new ArrayMap<>();
        VerifiedSigner result = null;
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            try {
                ByteBuffer signers = ApkSigningBlockUtils.getLengthPrefixedSlice(signatureInfo.signatureBlock);
                while (signers.hasRemaining()) {
                    try {
                        result = verifySigner(ApkSigningBlockUtils.getLengthPrefixedSlice(signers), contentDigests, certFactory);
                        signerCount++;
                    } catch (PlatformNotSupportedException e) {
                    } catch (IOException | SecurityException | BufferUnderflowException e2) {
                        throw new SecurityException("Failed to parse/verify signer #" + signerCount + " block", e2);
                    }
                }
                if (signerCount < 1 || result == null) {
                    throw new SecurityException("No signers found");
                } else if (signerCount != 1) {
                    throw new SecurityException("APK Signature Scheme V3 only supports one signer: multiple signers found.");
                } else if (!contentDigests.isEmpty()) {
                    if (doVerifyIntegrity) {
                        ApkSigningBlockUtils.verifyIntegrity(contentDigests, apk, signatureInfo);
                    }
                    if (contentDigests.containsKey(3)) {
                        result.verityRootHash = ApkSigningBlockUtils.parseVerityDigestAndVerifySourceLength(contentDigests.get(3), apk.length(), signatureInfo);
                    }
                    return result;
                } else {
                    throw new SecurityException("No content digests found");
                }
            } catch (IOException e3) {
                throw new SecurityException("Failed to read list of signers", e3);
            }
        } catch (CertificateException e4) {
            throw new RuntimeException("Failed to obtain X.509 CertificateFactory", e4);
        }
    }

    /* JADX INFO: Multiple debug info for r11v3 boolean: [D('sig' java.security.Signature), D('sigVerified' boolean)] */
    /* JADX INFO: Multiple debug info for r7v7 int: [D('bestSigAlgorithm' int), D('certificateCount' int)] */
    /* JADX INFO: Multiple debug info for r8v12 int: [D('signatureCount' int), D('digestCount' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0298, code lost:
        r0 = e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0298 A[ExcHandler: InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException (e java.lang.Throwable), Splitter:B:36:0x00d3] */
    private static VerifiedSigner verifySigner(ByteBuffer signerBlock, Map<Integer, byte[]> contentDigests, CertificateFactory certFactory) throws SecurityException, IOException, PlatformNotSupportedException {
        String jcaSignatureAlgorithm;
        int sigAlgorithm;
        ByteBuffer signedData = ApkSigningBlockUtils.getLengthPrefixedSlice(signerBlock);
        int minSdkVersion = signerBlock.getInt();
        int maxSdkVersion = signerBlock.getInt();
        if (Build.VERSION.SDK_INT < minSdkVersion || Build.VERSION.SDK_INT > maxSdkVersion) {
            throw new PlatformNotSupportedException("Signer not supported by this platform version. This platform: " + Build.VERSION.SDK_INT + ", signer minSdkVersion: " + minSdkVersion + ", maxSdkVersion: " + maxSdkVersion);
        }
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
                    int sigAlgorithm2 = signature.getInt();
                    signaturesSigAlgorithms.add(Integer.valueOf(sigAlgorithm2));
                    if (isSupportedSignatureAlgorithm(sigAlgorithm2)) {
                        if (bestSigAlgorithm == -1 || ApkSigningBlockUtils.compareSignatureAlgorithm(sigAlgorithm2, bestSigAlgorithm) > 0) {
                            bestSigAlgorithm = sigAlgorithm2;
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
            jcaSignatureAlgorithm = signatureAlgorithmParams.first;
            AlgorithmParameterSpec jcaSignatureAlgorithmParams = signatureAlgorithmParams.second;
            PublicKey publicKey = KeyFactory.getInstance(keyAlgorithm).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            Signature sig = Signature.getInstance(jcaSignatureAlgorithm);
            sig.initVerify(publicKey);
            if (jcaSignatureAlgorithmParams != null) {
                try {
                    sig.setParameter(jcaSignatureAlgorithmParams);
                } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e2) {
                    e = e2;
                }
            }
            try {
                sig.update(signedData);
                boolean sigVerified = sig.verify(bestSigAlgorithmSignatureBytes);
                if (sigVerified) {
                    signedData.clear();
                    ByteBuffer digests = ApkSigningBlockUtils.getLengthPrefixedSlice(signedData);
                    List<Integer> digestsSigAlgorithms = new ArrayList<>();
                    int digestCount = 0;
                    byte[] contentDigest = null;
                    while (digests.hasRemaining()) {
                        int digestCount2 = digestCount + 1;
                        try {
                            ByteBuffer digest = ApkSigningBlockUtils.getLengthPrefixedSlice(digests);
                            try {
                                if (digest.remaining() >= 8) {
                                    try {
                                        sigAlgorithm = digest.getInt();
                                    } catch (IOException | BufferUnderflowException e3) {
                                        e = e3;
                                        throw new IOException("Failed to parse digest record #" + digestCount2, e);
                                    }
                                    try {
                                        digestsSigAlgorithms.add(Integer.valueOf(sigAlgorithm));
                                        if (sigAlgorithm == bestSigAlgorithm) {
                                            contentDigest = ApkSigningBlockUtils.readLengthPrefixedByteArray(digest);
                                        }
                                        digestCount = digestCount2;
                                        digestsSigAlgorithms = digestsSigAlgorithms;
                                        signatureCount = signatureCount;
                                        bestSigAlgorithmSignatureBytes = bestSigAlgorithmSignatureBytes;
                                        sigVerified = sigVerified;
                                        keyAlgorithm = keyAlgorithm;
                                    } catch (IOException | BufferUnderflowException e4) {
                                        e = e4;
                                        throw new IOException("Failed to parse digest record #" + digestCount2, e);
                                    }
                                } else {
                                    throw new IOException("Record too short");
                                }
                            } catch (IOException | BufferUnderflowException e5) {
                                e = e5;
                                throw new IOException("Failed to parse digest record #" + digestCount2, e);
                            }
                        } catch (IOException | BufferUnderflowException e6) {
                            e = e6;
                            throw new IOException("Failed to parse digest record #" + digestCount2, e);
                        }
                    }
                    if (signaturesSigAlgorithms.equals(digestsSigAlgorithms)) {
                        int digestAlgorithm = ApkSigningBlockUtils.getSignatureAlgorithmContentDigestAlgorithm(bestSigAlgorithm);
                        byte[] previousSignerDigest = contentDigests.put(Integer.valueOf(digestAlgorithm), contentDigest);
                        if (previousSignerDigest != null) {
                            if (!MessageDigest.isEqual(previousSignerDigest, contentDigest)) {
                                throw new SecurityException(ApkSigningBlockUtils.getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm) + " contents digest does not match the digest specified by a preceding signer");
                            }
                        }
                        List<X509Certificate> certs = new ArrayList<>();
                        int certificateCount = 0;
                        for (ByteBuffer certificates = ApkSigningBlockUtils.getLengthPrefixedSlice(signedData); certificates.hasRemaining(); certificates = certificates) {
                            int certificateCount2 = certificateCount + 1;
                            byte[] encodedCert = ApkSigningBlockUtils.readLengthPrefixedByteArray(certificates);
                            try {
                                certs.add(new VerbatimX509Certificate((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedCert)), encodedCert));
                                certificateCount = certificateCount2;
                                bestSigAlgorithm = bestSigAlgorithm;
                                digestAlgorithm = digestAlgorithm;
                            } catch (CertificateException e7) {
                                throw new SecurityException("Failed to decode certificate #" + certificateCount2, e7);
                            }
                        }
                        if (certs.isEmpty()) {
                            throw new SecurityException("No certificates listed");
                        } else if (!Arrays.equals(publicKeyBytes, certs.get(0).getPublicKey().getEncoded())) {
                            throw new SecurityException("Public key mismatch between certificate and signature record");
                        } else if (signedData.getInt() != minSdkVersion) {
                            throw new SecurityException("minSdkVersion mismatch between signed and unsigned in v3 signer block.");
                        } else if (signedData.getInt() == maxSdkVersion) {
                            return verifyAdditionalAttributes(ApkSigningBlockUtils.getLengthPrefixedSlice(signedData), certs, certFactory);
                        } else {
                            throw new SecurityException("maxSdkVersion mismatch between signed and unsigned in v3 signer block.");
                        }
                    } else {
                        throw new SecurityException("Signature algorithms don't match between digests and signatures records");
                    }
                } else {
                    throw new SecurityException(jcaSignatureAlgorithm + " signature did not verify");
                }
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e8) {
            }
        } else if (signatureCount == 0) {
            throw new SecurityException("No signatures found");
        } else {
            throw new SecurityException("No supported signatures found");
        }
        throw new SecurityException("Failed to verify " + jcaSignatureAlgorithm + " signature", e);
    }

    private static VerifiedSigner verifyAdditionalAttributes(ByteBuffer attrs, List<X509Certificate> certs, CertificateFactory certFactory) throws IOException {
        X509Certificate[] certChain = (X509Certificate[]) certs.toArray(new X509Certificate[certs.size()]);
        VerifiedProofOfRotation por = null;
        while (attrs.hasRemaining()) {
            ByteBuffer attr = ApkSigningBlockUtils.getLengthPrefixedSlice(attrs);
            if (attr.remaining() < 4) {
                throw new IOException("Remaining buffer too short to contain additional attribute ID. Remaining: " + attr.remaining());
            } else if (attr.getInt() == PROOF_OF_ROTATION_ATTR_ID) {
                if (por == null) {
                    por = verifyProofOfRotationStruct(attr, certFactory);
                    try {
                        if (por.certs.size() <= 0) {
                            continue;
                        } else if (!Arrays.equals(por.certs.get(por.certs.size() - 1).getEncoded(), certChain[0].getEncoded())) {
                            throw new SecurityException("Terminal certificate in Proof-of-rotation record does not match APK signing certificate");
                        }
                    } catch (CertificateEncodingException e) {
                        throw new SecurityException("Failed to encode certificate when comparing Proof-of-rotation record and signing certificate", e);
                    }
                } else {
                    throw new SecurityException("Encountered multiple Proof-of-rotation records when verifying APK Signature Scheme v3 signature");
                }
            }
        }
        return new VerifiedSigner(certChain, por);
    }

    private static VerifiedProofOfRotation verifyProofOfRotationStruct(ByteBuffer porBuf, CertificateFactory certFactory) throws SecurityException, IOException {
        int levelCount = 0;
        int lastSigAlgorithm = -1;
        X509Certificate lastCert = null;
        List<X509Certificate> certs = new ArrayList<>();
        List<Integer> flagsList = new ArrayList<>();
        try {
            porBuf.getInt();
            HashSet<X509Certificate> certHistorySet = new HashSet<>();
            while (porBuf.hasRemaining()) {
                levelCount++;
                ByteBuffer level = ApkSigningBlockUtils.getLengthPrefixedSlice(porBuf);
                ByteBuffer signedData = ApkSigningBlockUtils.getLengthPrefixedSlice(level);
                int flags = level.getInt();
                int sigAlgorithm = level.getInt();
                byte[] signature = ApkSigningBlockUtils.readLengthPrefixedByteArray(level);
                if (lastCert != null) {
                    Pair<String, ? extends AlgorithmParameterSpec> sigAlgParams = ApkSigningBlockUtils.getSignatureAlgorithmJcaSignatureAlgorithm(lastSigAlgorithm);
                    PublicKey publicKey = lastCert.getPublicKey();
                    Signature sig = Signature.getInstance(sigAlgParams.first);
                    sig.initVerify(publicKey);
                    if (sigAlgParams.second != null) {
                        sig.setParameter(sigAlgParams.second);
                    }
                    sig.update(signedData);
                    if (!sig.verify(signature)) {
                        throw new SecurityException("Unable to verify signature of certificate #" + levelCount + " using " + ((String) sigAlgParams.first) + " when verifying Proof-of-rotation record");
                    }
                }
                signedData.rewind();
                byte[] encodedCert = ApkSigningBlockUtils.readLengthPrefixedByteArray(signedData);
                int signedSigAlgorithm = signedData.getInt();
                if (lastCert != null) {
                    if (lastSigAlgorithm != signedSigAlgorithm) {
                        throw new SecurityException("Signing algorithm ID mismatch for certificate #" + levelCount + " when verifying Proof-of-rotation record");
                    }
                }
                try {
                    lastCert = new VerbatimX509Certificate((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedCert)), encodedCert);
                    lastSigAlgorithm = sigAlgorithm;
                    if (!certHistorySet.contains(lastCert)) {
                        certHistorySet.add(lastCert);
                        certs.add(lastCert);
                        flagsList.add(Integer.valueOf(flags));
                    } else {
                        throw new SecurityException("Encountered duplicate entries in Proof-of-rotation record at certificate #" + levelCount + ".  All signing certificates should be unique");
                    }
                } catch (IOException | BufferUnderflowException e) {
                    e = e;
                    throw new IOException("Failed to parse Proof-of-rotation record", e);
                } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException e2) {
                    e = e2;
                    throw new SecurityException("Failed to verify signature over signed data for certificate #" + levelCount + " when verifying Proof-of-rotation record", e);
                } catch (CertificateException e3) {
                    e = e3;
                    throw new SecurityException("Failed to decode certificate #" + levelCount + " when verifying Proof-of-rotation record", e);
                }
            }
            return new VerifiedProofOfRotation(certs, flagsList);
        } catch (IOException | BufferUnderflowException e4) {
            e = e4;
            throw new IOException("Failed to parse Proof-of-rotation record", e);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException e5) {
            e = e5;
            throw new SecurityException("Failed to verify signature over signed data for certificate #" + levelCount + " when verifying Proof-of-rotation record", e);
        } catch (CertificateException e6) {
            e = e6;
            throw new SecurityException("Failed to decode certificate #" + levelCount + " when verifying Proof-of-rotation record", e);
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
    static byte[] generateApkVerityRootHash(String apkPath) throws NoSuchAlgorithmException, DigestException, IOException, SignatureNotFoundException {
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

    public static class VerifiedProofOfRotation {
        public final List<X509Certificate> certs;
        public final List<Integer> flagsList;

        public VerifiedProofOfRotation(List<X509Certificate> certs2, List<Integer> flagsList2) {
            this.certs = certs2;
            this.flagsList = flagsList2;
        }
    }

    public static class VerifiedSigner {
        public final X509Certificate[] certs;
        public final VerifiedProofOfRotation por;
        public byte[] verityRootHash;

        public VerifiedSigner(X509Certificate[] certs2, VerifiedProofOfRotation por2) {
            this.certs = certs2;
            this.por = por2;
        }
    }

    /* access modifiers changed from: private */
    public static class PlatformNotSupportedException extends Exception {
        PlatformNotSupportedException(String s) {
            super(s);
        }
    }
}
