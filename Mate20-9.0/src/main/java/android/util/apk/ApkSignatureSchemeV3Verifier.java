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

    private static class PlatformNotSupportedException extends Exception {
        PlatformNotSupportedException(String s) {
            super(s);
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

    public static VerifiedSigner verify(String apkFile) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apkFile, true);
    }

    public static VerifiedSigner plsCertsNoVerifyOnlyCerts(String apkFile) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apkFile, false);
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

    private static VerifiedSigner verifySigner(ByteBuffer signerBlock, Map<Integer, byte[]> contentDigests, CertificateFactory certFactory) throws SecurityException, IOException, PlatformNotSupportedException {
        byte[] encodedCert;
        int sigAlgorithm;
        boolean sigVerified;
        List<Integer> digestsSigAlgorithms;
        CertificateFactory certificateFactory = certFactory;
        ByteBuffer signedData = ApkSigningBlockUtils.getLengthPrefixedSlice(signerBlock);
        int minSdkVersion = signerBlock.getInt();
        int maxSdkVersion = signerBlock.getInt();
        if (Build.VERSION.SDK_INT < minSdkVersion || Build.VERSION.SDK_INT > maxSdkVersion) {
            Map<Integer, byte[]> map = contentDigests;
            throw new PlatformNotSupportedException("Signer not supported by this platform version. This platform: " + Build.VERSION.SDK_INT + ", signer minSdkVersion: " + minSdkVersion + ", maxSdkVersion: " + maxSdkVersion);
        }
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
                    int sigAlgorithm2 = signature.getInt();
                    arrayList.add(Integer.valueOf(sigAlgorithm2));
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
                        Pair<String, ? extends AlgorithmParameterSpec> pair = signatureAlgorithmParams;
                        Map<Integer, byte[]> map2 = contentDigests;
                    }
                }
                sig.update(signedData);
                boolean sigVerified2 = sig.verify(bestSigAlgorithmSignatureBytes);
                if (sigVerified2) {
                    signedData.clear();
                    ByteBuffer byteBuffer2 = signatures;
                    ByteBuffer digests = ApkSigningBlockUtils.getLengthPrefixedSlice(signedData);
                    List<Integer> digestsSigAlgorithms2 = new ArrayList<>();
                    int i3 = signatureCount;
                    byte[] contentDigest = null;
                    int digestCount = 0;
                    while (digests.hasRemaining()) {
                        byte[] bestSigAlgorithmSignatureBytes2 = bestSigAlgorithmSignatureBytes;
                        int digestCount2 = digestCount + 1;
                        try {
                            ByteBuffer digests2 = digests;
                            ByteBuffer digest = ApkSigningBlockUtils.getLengthPrefixedSlice(digests);
                            try {
                                String keyAlgorithm2 = keyAlgorithm;
                                if (digest.remaining() >= 8) {
                                    try {
                                        sigAlgorithm = digest.getInt();
                                        sigVerified = sigVerified2;
                                        digestsSigAlgorithms = digestsSigAlgorithms2;
                                    } catch (IOException | BufferUnderflowException e3) {
                                        e = e3;
                                        boolean z = sigVerified2;
                                        List<Integer> list = digestsSigAlgorithms2;
                                        StringBuilder sb = new StringBuilder();
                                        Pair<String, ? extends AlgorithmParameterSpec> pair2 = signatureAlgorithmParams;
                                        sb.append("Failed to parse digest record #");
                                        sb.append(digestCount2);
                                        throw new IOException(sb.toString(), e);
                                    }
                                    try {
                                        digestsSigAlgorithms.add(Integer.valueOf(sigAlgorithm));
                                        if (sigAlgorithm == bestSigAlgorithm) {
                                            contentDigest = ApkSigningBlockUtils.readLengthPrefixedByteArray(digest);
                                        }
                                        digestCount = digestCount2;
                                        digestsSigAlgorithms2 = digestsSigAlgorithms;
                                        bestSigAlgorithmSignatureBytes = bestSigAlgorithmSignatureBytes2;
                                        digests = digests2;
                                        keyAlgorithm = keyAlgorithm2;
                                        sigVerified2 = sigVerified;
                                    } catch (IOException | BufferUnderflowException e4) {
                                        e = e4;
                                        StringBuilder sb2 = new StringBuilder();
                                        Pair<String, ? extends AlgorithmParameterSpec> pair22 = signatureAlgorithmParams;
                                        sb2.append("Failed to parse digest record #");
                                        sb2.append(digestCount2);
                                        throw new IOException(sb2.toString(), e);
                                    }
                                } else {
                                    List<Integer> list2 = digestsSigAlgorithms2;
                                    throw new IOException("Record too short");
                                }
                            } catch (IOException | BufferUnderflowException e5) {
                                e = e5;
                                String str2 = keyAlgorithm;
                                boolean z2 = sigVerified2;
                                List<Integer> list3 = digestsSigAlgorithms2;
                                StringBuilder sb22 = new StringBuilder();
                                Pair<String, ? extends AlgorithmParameterSpec> pair222 = signatureAlgorithmParams;
                                sb22.append("Failed to parse digest record #");
                                sb22.append(digestCount2);
                                throw new IOException(sb22.toString(), e);
                            }
                        } catch (IOException | BufferUnderflowException e6) {
                            e = e6;
                            ByteBuffer byteBuffer3 = digests;
                            String str3 = keyAlgorithm;
                            boolean z3 = sigVerified2;
                            List<Integer> list4 = digestsSigAlgorithms2;
                            StringBuilder sb222 = new StringBuilder();
                            Pair<String, ? extends AlgorithmParameterSpec> pair2222 = signatureAlgorithmParams;
                            sb222.append("Failed to parse digest record #");
                            sb222.append(digestCount2);
                            throw new IOException(sb222.toString(), e);
                        }
                    }
                    byte[] bArr2 = bestSigAlgorithmSignatureBytes;
                    String str4 = keyAlgorithm;
                    boolean z4 = sigVerified2;
                    Pair<String, ? extends AlgorithmParameterSpec> pair3 = signatureAlgorithmParams;
                    if (arrayList.equals(digestsSigAlgorithms2)) {
                        int certificateCount = ApkSigningBlockUtils.getSignatureAlgorithmContentDigestAlgorithm(bestSigAlgorithm);
                        byte[] previousSignerDigest = contentDigests.put(Integer.valueOf(certificateCount), contentDigest);
                        if (previousSignerDigest == null) {
                        } else if (MessageDigest.isEqual(previousSignerDigest, contentDigest)) {
                            byte[] bArr3 = contentDigest;
                        } else {
                            StringBuilder sb3 = new StringBuilder();
                            byte[] bArr4 = contentDigest;
                            sb3.append(ApkSigningBlockUtils.getContentDigestAlgorithmJcaDigestAlgorithm(certificateCount));
                            sb3.append(" contents digest does not match the digest specified by a preceding signer");
                            throw new SecurityException(sb3.toString());
                        }
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
                                    certs.add(new VerbatimX509Certificate((X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(encodedCert)), encodedCert));
                                    certificateCount2 = digestAlgorithm2;
                                    certificateCount = digestAlgorithm;
                                    certificates = certificates2;
                                    bestSigAlgorithm = bestSigAlgorithm;
                                } catch (CertificateException e7) {
                                    e = e7;
                                    int i4 = bestSigAlgorithm;
                                    byte[] bArr5 = encodedCert;
                                    StringBuilder sb4 = new StringBuilder();
                                    ArrayList arrayList3 = arrayList;
                                    sb4.append("Failed to decode certificate #");
                                    sb4.append(digestAlgorithm2);
                                    throw new SecurityException(sb4.toString(), e);
                                }
                            } catch (CertificateException e8) {
                                e = e8;
                                ByteBuffer byteBuffer4 = certificates;
                                int i5 = bestSigAlgorithm;
                                encodedCert = encodedCert2;
                                byte[] bArr52 = encodedCert;
                                StringBuilder sb42 = new StringBuilder();
                                ArrayList arrayList32 = arrayList;
                                sb42.append("Failed to decode certificate #");
                                sb42.append(digestAlgorithm2);
                                throw new SecurityException(sb42.toString(), e);
                            }
                        }
                        int digestAlgorithm3 = certificateCount;
                        ByteBuffer byteBuffer5 = certificates;
                        int i6 = bestSigAlgorithm;
                        ArrayList arrayList4 = arrayList;
                        if (certs.isEmpty() != 0) {
                            throw new SecurityException("No certificates listed");
                        } else if (!Arrays.equals(publicKeyBytes, certs.get(0).getPublicKey().getEncoded())) {
                            throw new SecurityException("Public key mismatch between certificate and signature record");
                        } else if (signedData.getInt() != minSdkVersion) {
                            throw new SecurityException("minSdkVersion mismatch between signed and unsigned in v3 signer block.");
                        } else if (signedData.getInt() == maxSdkVersion) {
                            int i7 = certificateCount2;
                            return verifyAdditionalAttributes(ApkSigningBlockUtils.getLengthPrefixedSlice(signedData), certs, certificateFactory);
                        } else {
                            throw new SecurityException("maxSdkVersion mismatch between signed and unsigned in v3 signer block.");
                        }
                    } else {
                        Map<Integer, byte[]> map3 = contentDigests;
                        byte[] bArr6 = contentDigest;
                        int i8 = bestSigAlgorithm;
                        ArrayList arrayList5 = arrayList;
                        throw new SecurityException("Signature algorithms don't match between digests and signatures records");
                    }
                } else {
                    int i9 = signatureCount;
                    int i10 = bestSigAlgorithm;
                    ArrayList arrayList6 = arrayList;
                    byte[] bArr7 = bestSigAlgorithmSignatureBytes;
                    String str5 = keyAlgorithm;
                    boolean z5 = sigVerified2;
                    Pair<String, ? extends AlgorithmParameterSpec> pair4 = signatureAlgorithmParams;
                    Map<Integer, byte[]> map4 = contentDigests;
                    throw new SecurityException(jcaSignatureAlgorithm + " signature did not verify");
                }
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e9) {
                e = e9;
                ByteBuffer byteBuffer6 = signatures;
                int i11 = signatureCount;
                int i12 = bestSigAlgorithm;
                ArrayList arrayList7 = arrayList;
                byte[] bArr8 = bestSigAlgorithmSignatureBytes;
                String str6 = keyAlgorithm;
                Pair<String, ? extends AlgorithmParameterSpec> pair5 = signatureAlgorithmParams;
                Map<Integer, byte[]> map5 = contentDigests;
                throw new SecurityException("Failed to verify " + jcaSignatureAlgorithm + " signature", e);
            }
        } else if (signatureCount == 0) {
            throw new SecurityException("No signatures found");
        } else {
            throw new SecurityException("No supported signatures found");
        }
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
                    Signature sig = Signature.getInstance((String) sigAlgParams.first);
                    sig.initVerify(publicKey);
                    if (sigAlgParams.second != null) {
                        sig.setParameter((AlgorithmParameterSpec) sigAlgParams.second);
                    }
                    sig.update(signedData);
                    if (sig.verify(signature)) {
                        ByteBuffer byteBuffer = level;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        ByteBuffer byteBuffer2 = level;
                        sb.append("Unable to verify signature of certificate #");
                        sb.append(levelCount);
                        sb.append(" using ");
                        sb.append((String) sigAlgParams.first);
                        sb.append(" when verifying Proof-of-rotation record");
                        throw new SecurityException(sb.toString());
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
            CertificateFactory certificateFactory = certFactory;
            return new VerifiedProofOfRotation(certs, flagsList);
        } catch (IOException | BufferUnderflowException e4) {
            e = e4;
            CertificateFactory certificateFactory2 = certFactory;
            throw new IOException("Failed to parse Proof-of-rotation record", e);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException e5) {
            e = e5;
            CertificateFactory certificateFactory3 = certFactory;
            throw new SecurityException("Failed to verify signature over signed data for certificate #" + levelCount + " when verifying Proof-of-rotation record", e);
        } catch (CertificateException e6) {
            e = e6;
            CertificateFactory certificateFactory4 = certFactory;
            throw new SecurityException("Failed to decode certificate #" + levelCount + " when verifying Proof-of-rotation record", e);
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
    static byte[] generateFsverityRootHash(String apkPath) throws NoSuchAlgorithmException, DigestException, IOException, SignatureNotFoundException {
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
