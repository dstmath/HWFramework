package android.util.apk;

import android.security.keystore.KeyProperties;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.ArrayMap;
import android.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DirectByteBuffer;
import java.security.DigestException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import libcore.io.Libcore;
import libcore.io.Os;

public class ApkSignatureSchemeV2Verifier {
    private static final int APK_SIGNATURE_SCHEME_V2_BLOCK_ID = 1896449818;
    private static final long APK_SIG_BLOCK_MAGIC_HI = 3617552046287187010L;
    private static final long APK_SIG_BLOCK_MAGIC_LO = 2334950737559900225L;
    private static final int APK_SIG_BLOCK_MIN_SIZE = 32;
    private static final int CHUNK_SIZE_BYTES = 1048576;
    private static final int CONTENT_DIGEST_CHUNKED_SHA256 = 1;
    private static final int CONTENT_DIGEST_CHUNKED_SHA512 = 2;
    public static final int SF_ATTRIBUTE_ANDROID_APK_SIGNED_ID = 2;
    public static final String SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME = "X-Android-APK-Signed";
    private static final int SIGNATURE_DSA_WITH_SHA256 = 769;
    private static final int SIGNATURE_ECDSA_WITH_SHA256 = 513;
    private static final int SIGNATURE_ECDSA_WITH_SHA512 = 514;
    private static final int SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA256 = 259;
    private static final int SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA512 = 260;
    private static final int SIGNATURE_RSA_PSS_WITH_SHA256 = 257;
    private static final int SIGNATURE_RSA_PSS_WITH_SHA512 = 258;

    private interface DataSource {
        void feedIntoMessageDigests(MessageDigest[] messageDigestArr, long j, int i) throws IOException;

        long size();
    }

    private static final class ByteBufferDataSource implements DataSource {
        private final ByteBuffer mBuf;

        public ByteBufferDataSource(ByteBuffer buf) {
            this.mBuf = buf.slice();
        }

        public long size() {
            return (long) this.mBuf.capacity();
        }

        public void feedIntoMessageDigests(MessageDigest[] mds, long offset, int size) throws IOException {
            ByteBuffer region;
            synchronized (this.mBuf) {
                this.mBuf.position((int) offset);
                this.mBuf.limit(((int) offset) + size);
                region = this.mBuf.slice();
            }
            for (MessageDigest md : mds) {
                region.position(0);
                md.update(region);
            }
        }
    }

    private static final class MemoryMappedFileDataSource implements DataSource {
        private static final long MEMORY_PAGE_SIZE_BYTES = OS.sysconf(OsConstants._SC_PAGESIZE);
        private static final Os OS = Libcore.os;
        private final FileDescriptor mFd;
        private final long mFilePosition;
        private final long mSize;

        public MemoryMappedFileDataSource(FileDescriptor fd, long position, long size) {
            this.mFd = fd;
            this.mFilePosition = position;
            this.mSize = size;
        }

        public long size() {
            return this.mSize;
        }

        public void feedIntoMessageDigests(MessageDigest[] mds, long offset, int size) throws IOException {
            long filePosition = this.mFilePosition + offset;
            long mmapFilePosition = (filePosition / MEMORY_PAGE_SIZE_BYTES) * MEMORY_PAGE_SIZE_BYTES;
            int dataStartOffsetInMmapRegion = (int) (filePosition - mmapFilePosition);
            long mmapRegionSize = (long) (size + dataStartOffsetInMmapRegion);
            try {
                long mmapPtr = OS.mmap(0, mmapRegionSize, OsConstants.PROT_READ, OsConstants.MAP_SHARED | OsConstants.MAP_POPULATE, this.mFd, mmapFilePosition);
                ByteBuffer buf = new DirectByteBuffer(size, mmapPtr + ((long) dataStartOffsetInMmapRegion), this.mFd, null, true);
                for (MessageDigest md : mds) {
                    buf.position(0);
                    md.update(buf);
                }
                if (mmapPtr != 0) {
                    try {
                        OS.munmap(mmapPtr, mmapRegionSize);
                    } catch (ErrnoException e) {
                    }
                }
            } catch (ErrnoException e2) {
                throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e2);
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        OS.munmap(0, mmapRegionSize);
                    } catch (ErrnoException e3) {
                    }
                }
            }
        }
    }

    private static class SignatureInfo {
        private final long apkSigningBlockOffset;
        private final long centralDirOffset;
        private final ByteBuffer eocd;
        private final long eocdOffset;
        private final ByteBuffer signatureBlock;

        /* synthetic */ SignatureInfo(ByteBuffer signatureBlock, long apkSigningBlockOffset, long centralDirOffset, long eocdOffset, ByteBuffer eocd, SignatureInfo -this5) {
            this(signatureBlock, apkSigningBlockOffset, centralDirOffset, eocdOffset, eocd);
        }

        private SignatureInfo(ByteBuffer signatureBlock, long apkSigningBlockOffset, long centralDirOffset, long eocdOffset, ByteBuffer eocd) {
            this.signatureBlock = signatureBlock;
            this.apkSigningBlockOffset = apkSigningBlockOffset;
            this.centralDirOffset = centralDirOffset;
            this.eocdOffset = eocdOffset;
            this.eocd = eocd;
        }
    }

    public static class SignatureNotFoundException extends Exception {
        private static final long serialVersionUID = 1;

        public SignatureNotFoundException(String message) {
            super(message);
        }

        public SignatureNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class WrappedX509Certificate extends X509Certificate {
        private final X509Certificate wrapped;

        public WrappedX509Certificate(X509Certificate wrapped) {
            this.wrapped = wrapped;
        }

        public Set<String> getCriticalExtensionOIDs() {
            return this.wrapped.getCriticalExtensionOIDs();
        }

        public byte[] getExtensionValue(String oid) {
            return this.wrapped.getExtensionValue(oid);
        }

        public Set<String> getNonCriticalExtensionOIDs() {
            return this.wrapped.getNonCriticalExtensionOIDs();
        }

        public boolean hasUnsupportedCriticalExtension() {
            return this.wrapped.hasUnsupportedCriticalExtension();
        }

        public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
            this.wrapped.checkValidity();
        }

        public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
            this.wrapped.checkValidity(date);
        }

        public int getVersion() {
            return this.wrapped.getVersion();
        }

        public BigInteger getSerialNumber() {
            return this.wrapped.getSerialNumber();
        }

        public Principal getIssuerDN() {
            return this.wrapped.getIssuerDN();
        }

        public Principal getSubjectDN() {
            return this.wrapped.getSubjectDN();
        }

        public Date getNotBefore() {
            return this.wrapped.getNotBefore();
        }

        public Date getNotAfter() {
            return this.wrapped.getNotAfter();
        }

        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return this.wrapped.getTBSCertificate();
        }

        public byte[] getSignature() {
            return this.wrapped.getSignature();
        }

        public String getSigAlgName() {
            return this.wrapped.getSigAlgName();
        }

        public String getSigAlgOID() {
            return this.wrapped.getSigAlgOID();
        }

        public byte[] getSigAlgParams() {
            return this.wrapped.getSigAlgParams();
        }

        public boolean[] getIssuerUniqueID() {
            return this.wrapped.getIssuerUniqueID();
        }

        public boolean[] getSubjectUniqueID() {
            return this.wrapped.getSubjectUniqueID();
        }

        public boolean[] getKeyUsage() {
            return this.wrapped.getKeyUsage();
        }

        public int getBasicConstraints() {
            return this.wrapped.getBasicConstraints();
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return this.wrapped.getEncoded();
        }

        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
            this.wrapped.verify(key);
        }

        public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
            this.wrapped.verify(key, sigProvider);
        }

        public String toString() {
            return this.wrapped.toString();
        }

        public PublicKey getPublicKey() {
            return this.wrapped.getPublicKey();
        }
    }

    private static class VerbatimX509Certificate extends WrappedX509Certificate {
        private byte[] encodedVerbatim;

        public VerbatimX509Certificate(X509Certificate wrapped, byte[] encodedVerbatim) {
            super(wrapped);
            this.encodedVerbatim = encodedVerbatim;
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return this.encodedVerbatim;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0025 A:{SYNTHETIC, Splitter: B:24:0x0025} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0038 A:{Catch:{ SignatureNotFoundException -> 0x002b }} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x002a A:{SYNTHETIC, Splitter: B:27:0x002a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean hasSignature(String apkFile) throws IOException {
        Throwable th;
        Throwable th2 = null;
        RandomAccessFile apk = null;
        try {
            RandomAccessFile apk2 = new RandomAccessFile(apkFile, "r");
            try {
                findSignature(apk2);
                if (apk2 != null) {
                    try {
                        apk2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return true;
                }
                try {
                    throw th2;
                } catch (SignatureNotFoundException e) {
                    apk = apk2;
                }
            } catch (Throwable th4) {
                th = th4;
                apk = apk2;
                if (apk != null) {
                    try {
                        apk.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (SignatureNotFoundException e2) {
                        return false;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (apk != null) {
            }
            if (th2 == null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0021 A:{SYNTHETIC, Splitter: B:18:0x0021} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0026  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static X509Certificate[][] verify(String apkFile) throws SignatureNotFoundException, SecurityException, IOException {
        Throwable th;
        Throwable th2 = null;
        RandomAccessFile apk = null;
        try {
            RandomAccessFile apk2 = new RandomAccessFile(apkFile, "r");
            try {
                X509Certificate[][] verify = verify(apk2);
                if (apk2 != null) {
                    try {
                        apk2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return verify;
                }
                throw th2;
            } catch (Throwable th4) {
                th = th4;
                apk = apk2;
                if (apk != null) {
                    try {
                        apk.close();
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
            if (apk != null) {
            }
            if (th2 == null) {
            }
        }
    }

    private static X509Certificate[][] verify(RandomAccessFile apk) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apk.getFD(), findSignature(apk));
    }

    private static SignatureInfo findSignature(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        Pair<ByteBuffer, Long> eocdAndOffsetInFile = getEocd(apk);
        ByteBuffer eocd = eocdAndOffsetInFile.first;
        long eocdOffset = ((Long) eocdAndOffsetInFile.second).longValue();
        if (ZipUtils.isZip64EndOfCentralDirectoryLocatorPresent(apk, eocdOffset)) {
            throw new SignatureNotFoundException("ZIP64 APK not supported");
        }
        long centralDirOffset = getCentralDirOffset(eocd, eocdOffset);
        Pair<ByteBuffer, Long> apkSigningBlockAndOffsetInFile = findApkSigningBlock(apk, centralDirOffset);
        ByteBuffer apkSigningBlock = apkSigningBlockAndOffsetInFile.first;
        return new SignatureInfo(findApkSignatureSchemeV2Block(apkSigningBlock), ((Long) apkSigningBlockAndOffsetInFile.second).longValue(), centralDirOffset, eocdOffset, eocd, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0033 A:{Splitter: B:8:0x0023, ExcHandler: java.io.IOException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0033 A:{Splitter: B:8:0x0023, ExcHandler: java.io.IOException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:11:0x0033, code:
            r14 = move-exception;
     */
    /* JADX WARNING: Missing block: B:13:0x0056, code:
            throw new java.lang.SecurityException("Failed to parse/verify signer #" + r18 + " block", r14);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static X509Certificate[][] verify(FileDescriptor apkFileDescriptor, SignatureInfo signatureInfo) throws SecurityException {
        int signerCount = 0;
        Map<Integer, byte[]> contentDigests = new ArrayMap();
        List<X509Certificate[]> signerCerts = new ArrayList();
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            try {
                ByteBuffer signers = getLengthPrefixedSlice(signatureInfo.signatureBlock);
                while (signers.hasRemaining()) {
                    signerCount++;
                    try {
                        signerCerts.add(verifySigner(getLengthPrefixedSlice(signers), contentDigests, certFactory));
                    } catch (Exception e) {
                    }
                }
                if (signerCount < 1) {
                    throw new SecurityException("No signers found");
                } else if (contentDigests.isEmpty()) {
                    throw new SecurityException("No content digests found");
                } else {
                    verifyIntegrity(contentDigests, apkFileDescriptor, signatureInfo.apkSigningBlockOffset, signatureInfo.centralDirOffset, signatureInfo.eocdOffset, signatureInfo.eocd);
                    return (X509Certificate[][]) signerCerts.toArray(new X509Certificate[signerCerts.size()][]);
                }
            } catch (IOException e2) {
                throw new SecurityException("Failed to read list of signers", e2);
            }
        } catch (CertificateException e3) {
            throw new RuntimeException("Failed to obtain X.509 CertificateFactory", e3);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x0164 A:{Splitter: B:44:0x014b, ExcHandler: java.io.IOException (r17_1 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0036 A:{Splitter: B:4:0x001d, ExcHandler: java.io.IOException (r17_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0109 A:{Splitter: B:29:0x00b7, ExcHandler: java.security.NoSuchAlgorithmException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0109 A:{Splitter: B:29:0x00b7, ExcHandler: java.security.NoSuchAlgorithmException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0109 A:{Splitter: B:29:0x00b7, ExcHandler: java.security.NoSuchAlgorithmException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0109 A:{Splitter: B:29:0x00b7, ExcHandler: java.security.NoSuchAlgorithmException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Missing block: B:9:0x0036, code:
            r17 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x005a, code:
            throw new java.lang.SecurityException("Failed to parse signature record #" + r33, r17);
     */
    /* JADX WARNING: Missing block: B:37:0x0109, code:
            r18 = move-exception;
     */
    /* JADX WARNING: Missing block: B:39:0x0134, code:
            throw new java.lang.SecurityException("Failed to verify " + r21 + " signature", r18);
     */
    /* JADX WARNING: Missing block: B:49:0x0164, code:
            r17 = move-exception;
     */
    /* JADX WARNING: Missing block: B:51:0x0186, code:
            throw new java.io.IOException("Failed to parse digest record #" + r14, r17);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static X509Certificate[] verifySigner(ByteBuffer signerBlock, Map<Integer, byte[]> contentDigests, CertificateFactory certFactory) throws SecurityException, IOException {
        int sigAlgorithm;
        ByteBuffer signedData = getLengthPrefixedSlice(signerBlock);
        ByteBuffer signatures = getLengthPrefixedSlice(signerBlock);
        byte[] publicKeyBytes = readLengthPrefixedByteArray(signerBlock);
        int signatureCount = 0;
        int bestSigAlgorithm = -1;
        byte[] bestSigAlgorithmSignatureBytes = null;
        List<Integer> signaturesSigAlgorithms = new ArrayList();
        while (signatures.hasRemaining()) {
            signatureCount++;
            try {
                ByteBuffer signature = getLengthPrefixedSlice(signatures);
                if (signature.remaining() < 8) {
                    throw new SecurityException("Signature record too short");
                }
                sigAlgorithm = signature.getInt();
                signaturesSigAlgorithms.add(Integer.valueOf(sigAlgorithm));
                if (isSupportedSignatureAlgorithm(sigAlgorithm) && (bestSigAlgorithm == -1 || compareSignatureAlgorithm(sigAlgorithm, bestSigAlgorithm) > 0)) {
                    bestSigAlgorithm = sigAlgorithm;
                    bestSigAlgorithmSignatureBytes = readLengthPrefixedByteArray(signature);
                }
            } catch (Throwable e) {
            }
        }
        if (bestSigAlgorithm != -1) {
            String keyAlgorithm = getSignatureAlgorithmJcaKeyAlgorithm(bestSigAlgorithm);
            Pair<String, ? extends AlgorithmParameterSpec> signatureAlgorithmParams = getSignatureAlgorithmJcaSignatureAlgorithm(bestSigAlgorithm);
            String jcaSignatureAlgorithm = signatureAlgorithmParams.first;
            AlgorithmParameterSpec jcaSignatureAlgorithmParams = signatureAlgorithmParams.second;
            try {
                PublicKey publicKey = KeyFactory.getInstance(keyAlgorithm).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
                Signature sig = Signature.getInstance(jcaSignatureAlgorithm);
                sig.initVerify(publicKey);
                if (jcaSignatureAlgorithmParams != null) {
                    sig.setParameter(jcaSignatureAlgorithmParams);
                }
                sig.update(signedData);
                if (sig.verify(bestSigAlgorithmSignatureBytes)) {
                    Object contentDigest = null;
                    signedData.clear();
                    ByteBuffer digests = getLengthPrefixedSlice(signedData);
                    List<Integer> digestsSigAlgorithms = new ArrayList();
                    int digestCount = 0;
                    while (digests.hasRemaining()) {
                        digestCount++;
                        try {
                            ByteBuffer digest = getLengthPrefixedSlice(digests);
                            if (digest.remaining() < 8) {
                                throw new IOException("Record too short");
                            }
                            sigAlgorithm = digest.getInt();
                            digestsSigAlgorithms.add(Integer.valueOf(sigAlgorithm));
                            if (sigAlgorithm == bestSigAlgorithm) {
                                contentDigest = readLengthPrefixedByteArray(digest);
                            }
                        } catch (Throwable e2) {
                        }
                    }
                    if (signaturesSigAlgorithms.equals(digestsSigAlgorithms)) {
                        int digestAlgorithm = getSignatureAlgorithmContentDigestAlgorithm(bestSigAlgorithm);
                        byte[] previousSignerDigest = (byte[]) contentDigests.put(Integer.valueOf(digestAlgorithm), contentDigest);
                        if (previousSignerDigest == null || (MessageDigest.isEqual(previousSignerDigest, contentDigest) ^ 1) == 0) {
                            ByteBuffer certificates = getLengthPrefixedSlice(signedData);
                            List<X509Certificate> certs = new ArrayList();
                            int certificateCount = 0;
                            while (certificates.hasRemaining()) {
                                certificateCount++;
                                byte[] encodedCert = readLengthPrefixedByteArray(certificates);
                                try {
                                    certs.add(new VerbatimX509Certificate((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedCert)), encodedCert));
                                } catch (Throwable e3) {
                                    throw new SecurityException("Failed to decode certificate #" + certificateCount, e3);
                                }
                            }
                            if (certs.isEmpty()) {
                                throw new SecurityException("No certificates listed");
                            }
                            if (Arrays.equals(publicKeyBytes, ((X509Certificate) certs.get(0)).getPublicKey().getEncoded())) {
                                return (X509Certificate[]) certs.toArray(new X509Certificate[certs.size()]);
                            }
                            throw new SecurityException("Public key mismatch between certificate and signature record");
                        }
                        throw new SecurityException(getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm) + " contents digest does not match the digest specified by a preceding signer");
                    }
                    throw new SecurityException("Signature algorithms don't match between digests and signatures records");
                }
                throw new SecurityException(jcaSignatureAlgorithm + " signature did not verify");
            } catch (Throwable e4) {
            }
        } else if (signatureCount == 0) {
            throw new SecurityException("No signatures found");
        } else {
            throw new SecurityException("No supported signatures found");
        }
    }

    private static void verifyIntegrity(Map<Integer, byte[]> expectedDigests, FileDescriptor apkFileDescriptor, long apkSigningBlockOffset, long centralDirOffset, long eocdOffset, ByteBuffer eocdBuf) throws SecurityException {
        if (expectedDigests.isEmpty()) {
            throw new SecurityException("No digests provided");
        }
        DataSource beforeApkSigningBlock = new MemoryMappedFileDataSource(apkFileDescriptor, 0, apkSigningBlockOffset);
        DataSource centralDir = new MemoryMappedFileDataSource(apkFileDescriptor, centralDirOffset, eocdOffset - centralDirOffset);
        eocdBuf = eocdBuf.duplicate();
        eocdBuf.order(ByteOrder.LITTLE_ENDIAN);
        ZipUtils.setZipEocdCentralDirectoryOffset(eocdBuf, apkSigningBlockOffset);
        DataSource byteBufferDataSource = new ByteBufferDataSource(eocdBuf);
        int[] digestAlgorithms = new int[expectedDigests.size()];
        int digestAlgorithmCount = 0;
        for (Integer intValue : expectedDigests.keySet()) {
            digestAlgorithms[digestAlgorithmCount] = intValue.intValue();
            digestAlgorithmCount++;
        }
        try {
            byte[][] actualDigests = computeContentDigests(digestAlgorithms, new DataSource[]{beforeApkSigningBlock, centralDir, byteBufferDataSource});
            int i = 0;
            while (i < digestAlgorithms.length) {
                int digestAlgorithm = digestAlgorithms[i];
                if (MessageDigest.isEqual((byte[]) expectedDigests.get(Integer.valueOf(digestAlgorithm)), actualDigests[i])) {
                    i++;
                } else {
                    throw new SecurityException(getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm) + " digest of contents did not verify");
                }
            }
        } catch (Throwable e) {
            throw new SecurityException("Failed to compute digest(s) of contents", e);
        }
    }

    private static byte[][] computeContentDigests(int[] digestAlgorithms, DataSource[] contents) throws DigestException {
        long totalChunkCountLong = 0;
        for (DataSource input : contents) {
            totalChunkCountLong += getChunkCount(input.size());
        }
        if (totalChunkCountLong >= 2097151) {
            throw new DigestException("Too many chunks: " + totalChunkCountLong);
        }
        int i;
        byte[] concatenationOfChunkCountAndChunkDigests;
        String jcaAlgorithmName;
        int digestAlgorithm;
        int totalChunkCount = (int) totalChunkCountLong;
        byte[][] digestsOfChunks = new byte[digestAlgorithms.length][];
        for (i = 0; i < digestAlgorithms.length; i++) {
            concatenationOfChunkCountAndChunkDigests = new byte[((totalChunkCount * getContentDigestAlgorithmOutputSizeBytes(digestAlgorithms[i])) + 5)];
            concatenationOfChunkCountAndChunkDigests[0] = (byte) 90;
            setUnsignedInt32LittleEndian(totalChunkCount, concatenationOfChunkCountAndChunkDigests, 1);
            digestsOfChunks[i] = concatenationOfChunkCountAndChunkDigests;
        }
        byte[] chunkContentPrefix = new byte[5];
        chunkContentPrefix[0] = (byte) -91;
        int chunkIndex = 0;
        MessageDigest[] mds = new MessageDigest[digestAlgorithms.length];
        i = 0;
        while (i < digestAlgorithms.length) {
            jcaAlgorithmName = getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithms[i]);
            try {
                mds[i] = MessageDigest.getInstance(jcaAlgorithmName);
                i++;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(jcaAlgorithmName + " digest not supported", e);
            }
        }
        int dataSourceIndex = 0;
        for (DataSource input2 : contents) {
            long inputOffset = 0;
            long inputRemaining = input2.size();
            while (inputRemaining > 0) {
                int chunkSize = (int) Math.min(inputRemaining, 1048576);
                setUnsignedInt32LittleEndian(chunkSize, chunkContentPrefix, 1);
                for (MessageDigest update : mds) {
                    update.update(chunkContentPrefix);
                }
                try {
                    input2.feedIntoMessageDigests(mds, inputOffset, chunkSize);
                    for (i = 0; i < digestAlgorithms.length; i++) {
                        digestAlgorithm = digestAlgorithms[i];
                        concatenationOfChunkCountAndChunkDigests = digestsOfChunks[i];
                        int expectedDigestSizeBytes = getContentDigestAlgorithmOutputSizeBytes(digestAlgorithm);
                        MessageDigest md = mds[i];
                        int actualDigestSizeBytes = md.digest(concatenationOfChunkCountAndChunkDigests, (chunkIndex * expectedDigestSizeBytes) + 5, expectedDigestSizeBytes);
                        if (actualDigestSizeBytes != expectedDigestSizeBytes) {
                            throw new RuntimeException("Unexpected output size of " + md.getAlgorithm() + " digest: " + actualDigestSizeBytes);
                        }
                    }
                    inputOffset += (long) chunkSize;
                    inputRemaining -= (long) chunkSize;
                    chunkIndex++;
                } catch (IOException e2) {
                    throw new DigestException("Failed to digest chunk #" + chunkIndex + " of section #" + dataSourceIndex, e2);
                }
            }
            dataSourceIndex++;
        }
        byte[][] result = new byte[digestAlgorithms.length][];
        i = 0;
        while (i < digestAlgorithms.length) {
            digestAlgorithm = digestAlgorithms[i];
            byte[] input3 = digestsOfChunks[i];
            jcaAlgorithmName = getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm);
            try {
                result[i] = MessageDigest.getInstance(jcaAlgorithmName).digest(input3);
                i++;
            } catch (NoSuchAlgorithmException e3) {
                throw new RuntimeException(jcaAlgorithmName + " digest not supported", e3);
            }
        }
        return result;
    }

    private static Pair<ByteBuffer, Long> getEocd(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        Pair<ByteBuffer, Long> eocdAndOffsetInFile = ZipUtils.findZipEndOfCentralDirectoryRecord(apk);
        if (eocdAndOffsetInFile != null) {
            return eocdAndOffsetInFile;
        }
        throw new SignatureNotFoundException("Not an APK file: ZIP End of Central Directory record not found");
    }

    private static long getCentralDirOffset(ByteBuffer eocd, long eocdOffset) throws SignatureNotFoundException {
        long centralDirOffset = ZipUtils.getZipEocdCentralDirectoryOffset(eocd);
        if (centralDirOffset > eocdOffset) {
            throw new SignatureNotFoundException("ZIP Central Directory offset out of range: " + centralDirOffset + ". ZIP End of Central Directory offset: " + eocdOffset);
        } else if (centralDirOffset + ZipUtils.getZipEocdCentralDirectorySizeBytes(eocd) == eocdOffset) {
            return centralDirOffset;
        } else {
            throw new SignatureNotFoundException("ZIP Central Directory is not immediately followed by End of Central Directory");
        }
    }

    private static final long getChunkCount(long inputSizeBytes) {
        return ((inputSizeBytes + 1048576) - 1) / 1048576;
    }

    private static boolean isSupportedSignatureAlgorithm(int sigAlgorithm) {
        switch (sigAlgorithm) {
            case 257:
            case 258:
            case 259:
            case 260:
            case 513:
            case 514:
            case 769:
                return true;
            default:
                return false;
        }
    }

    private static int compareSignatureAlgorithm(int sigAlgorithm1, int sigAlgorithm2) {
        return compareContentDigestAlgorithm(getSignatureAlgorithmContentDigestAlgorithm(sigAlgorithm1), getSignatureAlgorithmContentDigestAlgorithm(sigAlgorithm2));
    }

    private static int compareContentDigestAlgorithm(int digestAlgorithm1, int digestAlgorithm2) {
        switch (digestAlgorithm1) {
            case 1:
                switch (digestAlgorithm2) {
                    case 1:
                        return 0;
                    case 2:
                        return -1;
                    default:
                        throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
                }
            case 2:
                switch (digestAlgorithm2) {
                    case 1:
                        return 1;
                    case 2:
                        return 0;
                    default:
                        throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
                }
            default:
                throw new IllegalArgumentException("Unknown digestAlgorithm1: " + digestAlgorithm1);
        }
    }

    private static int getSignatureAlgorithmContentDigestAlgorithm(int sigAlgorithm) {
        switch (sigAlgorithm) {
            case 257:
            case 259:
            case 513:
            case 769:
                return 1;
            case 258:
            case 260:
            case 514:
                return 2;
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
        }
    }

    private static String getContentDigestAlgorithmJcaDigestAlgorithm(int digestAlgorithm) {
        switch (digestAlgorithm) {
            case 1:
                return KeyProperties.DIGEST_SHA256;
            case 2:
                return KeyProperties.DIGEST_SHA512;
            default:
                throw new IllegalArgumentException("Unknown content digest algorthm: " + digestAlgorithm);
        }
    }

    private static int getContentDigestAlgorithmOutputSizeBytes(int digestAlgorithm) {
        switch (digestAlgorithm) {
            case 1:
                return 32;
            case 2:
                return 64;
            default:
                throw new IllegalArgumentException("Unknown content digest algorthm: " + digestAlgorithm);
        }
    }

    private static String getSignatureAlgorithmJcaKeyAlgorithm(int sigAlgorithm) {
        switch (sigAlgorithm) {
            case 257:
            case 258:
            case 259:
            case 260:
                return KeyProperties.KEY_ALGORITHM_RSA;
            case 513:
            case 514:
                return KeyProperties.KEY_ALGORITHM_EC;
            case 769:
                return "DSA";
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
        }
    }

    private static Pair<String, ? extends AlgorithmParameterSpec> getSignatureAlgorithmJcaSignatureAlgorithm(int sigAlgorithm) {
        switch (sigAlgorithm) {
            case 257:
                return Pair.create("SHA256withRSA/PSS", new PSSParameterSpec(KeyProperties.DIGEST_SHA256, "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
            case 258:
                return Pair.create("SHA512withRSA/PSS", new PSSParameterSpec(KeyProperties.DIGEST_SHA512, "MGF1", MGF1ParameterSpec.SHA512, 64, 1));
            case 259:
                return Pair.create("SHA256withRSA", null);
            case 260:
                return Pair.create("SHA512withRSA", null);
            case 513:
                return Pair.create("SHA256withECDSA", null);
            case 514:
                return Pair.create("SHA512withECDSA", null);
            case 769:
                return Pair.create("SHA256withDSA", null);
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
        }
    }

    private static ByteBuffer sliceFromTo(ByteBuffer source, int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start: " + start);
        } else if (end < start) {
            throw new IllegalArgumentException("end < start: " + end + " < " + start);
        } else {
            int capacity = source.capacity();
            if (end > source.capacity()) {
                throw new IllegalArgumentException("end > capacity: " + end + " > " + capacity);
            }
            int originalLimit = source.limit();
            int originalPosition = source.position();
            try {
                source.position(0);
                source.limit(end);
                source.position(start);
                ByteBuffer result = source.slice();
                result.order(source.order());
                return result;
            } finally {
                source.position(0);
                source.limit(originalLimit);
                source.position(originalPosition);
            }
        }
    }

    private static ByteBuffer getByteBuffer(ByteBuffer source, int size) throws BufferUnderflowException {
        if (size < 0) {
            throw new IllegalArgumentException("size: " + size);
        }
        int originalLimit = source.limit();
        int position = source.position();
        int limit = position + size;
        if (limit < position || limit > originalLimit) {
            throw new BufferUnderflowException();
        }
        source.limit(limit);
        try {
            ByteBuffer result = source.slice();
            result.order(source.order());
            source.position(limit);
            return result;
        } finally {
            source.limit(originalLimit);
        }
    }

    private static ByteBuffer getLengthPrefixedSlice(ByteBuffer source) throws IOException {
        if (source.remaining() < 4) {
            throw new IOException("Remaining buffer too short to contain length of length-prefixed field. Remaining: " + source.remaining());
        }
        int len = source.getInt();
        if (len < 0) {
            throw new IllegalArgumentException("Negative length");
        } else if (len <= source.remaining()) {
            return getByteBuffer(source, len);
        } else {
            throw new IOException("Length-prefixed field longer than remaining buffer. Field length: " + len + ", remaining: " + source.remaining());
        }
    }

    private static byte[] readLengthPrefixedByteArray(ByteBuffer buf) throws IOException {
        int len = buf.getInt();
        if (len < 0) {
            throw new IOException("Negative length");
        } else if (len > buf.remaining()) {
            throw new IOException("Underflow while reading length-prefixed value. Length: " + len + ", available: " + buf.remaining());
        } else {
            byte[] result = new byte[len];
            buf.get(result);
            return result;
        }
    }

    private static void setUnsignedInt32LittleEndian(int value, byte[] result, int offset) {
        result[offset] = (byte) (value & 255);
        result[offset + 1] = (byte) ((value >>> 8) & 255);
        result[offset + 2] = (byte) ((value >>> 16) & 255);
        result[offset + 3] = (byte) ((value >>> 24) & 255);
    }

    private static Pair<ByteBuffer, Long> findApkSigningBlock(RandomAccessFile apk, long centralDirOffset) throws IOException, SignatureNotFoundException {
        if (centralDirOffset < 32) {
            throw new SignatureNotFoundException("APK too small for APK Signing Block. ZIP Central Directory offset: " + centralDirOffset);
        }
        ByteBuffer footer = ByteBuffer.allocate(24);
        footer.order(ByteOrder.LITTLE_ENDIAN);
        apk.seek(centralDirOffset - ((long) footer.capacity()));
        apk.readFully(footer.array(), footer.arrayOffset(), footer.capacity());
        if (footer.getLong(8) == APK_SIG_BLOCK_MAGIC_LO && footer.getLong(16) == APK_SIG_BLOCK_MAGIC_HI) {
            long apkSigBlockSizeInFooter = footer.getLong(0);
            if (apkSigBlockSizeInFooter < ((long) footer.capacity()) || apkSigBlockSizeInFooter > 2147483639) {
                throw new SignatureNotFoundException("APK Signing Block size out of range: " + apkSigBlockSizeInFooter);
            }
            int totalSize = (int) (8 + apkSigBlockSizeInFooter);
            long apkSigBlockOffset = centralDirOffset - ((long) totalSize);
            if (apkSigBlockOffset < 0) {
                throw new SignatureNotFoundException("APK Signing Block offset out of range: " + apkSigBlockOffset);
            }
            ByteBuffer apkSigBlock = ByteBuffer.allocate(totalSize);
            apkSigBlock.order(ByteOrder.LITTLE_ENDIAN);
            apk.seek(apkSigBlockOffset);
            apk.readFully(apkSigBlock.array(), apkSigBlock.arrayOffset(), apkSigBlock.capacity());
            long apkSigBlockSizeInHeader = apkSigBlock.getLong(0);
            if (apkSigBlockSizeInHeader == apkSigBlockSizeInFooter) {
                return Pair.create(apkSigBlock, Long.valueOf(apkSigBlockOffset));
            }
            throw new SignatureNotFoundException("APK Signing Block sizes in header and footer do not match: " + apkSigBlockSizeInHeader + " vs " + apkSigBlockSizeInFooter);
        }
        throw new SignatureNotFoundException("No APK Signing Block before ZIP Central Directory");
    }

    private static ByteBuffer findApkSignatureSchemeV2Block(ByteBuffer apkSigningBlock) throws SignatureNotFoundException {
        checkByteOrderLittleEndian(apkSigningBlock);
        ByteBuffer pairs = sliceFromTo(apkSigningBlock, 8, apkSigningBlock.capacity() - 24);
        int entryCount = 0;
        while (pairs.hasRemaining()) {
            entryCount++;
            if (pairs.remaining() < 8) {
                throw new SignatureNotFoundException("Insufficient data to read size of APK Signing Block entry #" + entryCount);
            }
            long lenLong = pairs.getLong();
            if (lenLong < 4 || lenLong > 2147483647L) {
                throw new SignatureNotFoundException("APK Signing Block entry #" + entryCount + " size out of range: " + lenLong);
            }
            int len = (int) lenLong;
            int nextEntryPos = pairs.position() + len;
            if (len > pairs.remaining()) {
                throw new SignatureNotFoundException("APK Signing Block entry #" + entryCount + " size out of range: " + len + ", available: " + pairs.remaining());
            } else if (pairs.getInt() == APK_SIGNATURE_SCHEME_V2_BLOCK_ID) {
                return getByteBuffer(pairs, len - 4);
            } else {
                pairs.position(nextEntryPos);
            }
        }
        throw new SignatureNotFoundException("No APK Signature Scheme v2 block in APK Signing Block");
    }

    private static void checkByteOrderLittleEndian(ByteBuffer buffer) {
        if (buffer.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("ByteBuffer byte order must be little endian");
        }
    }
}
