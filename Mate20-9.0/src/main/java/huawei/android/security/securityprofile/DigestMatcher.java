package huawei.android.security.securityprofile;

import android.os.SystemProperties;
import android.util.Pair;
import android.util.Slog;
import android.util.jar.StrictJarFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import libcore.io.Streams;

public class DigestMatcher {
    private static final int APK_SIGNATURE_SCHEME_V2_BLOCK_ID = 1896449818;
    private static final long APK_SIG_BLOCK_MAGIC_HI = 3617552046287187010L;
    private static final long APK_SIG_BLOCK_MAGIC_LO = 2334950737559900225L;
    private static final int APK_SIG_BLOCK_MIN_SIZE = 32;
    public static final boolean CALCULATE_APKDIGEST = "true".equalsIgnoreCase(SystemProperties.get("ro.config.iseapp_calculate_apkdigest", "true"));
    public static final int CONTENT_DIGEST_CHUNKED_SHA256 = 1;
    public static final int CONTENT_DIGEST_CHUNKED_SHA512 = 2;
    private static final int SIGNATURE_DSA_WITH_SHA256 = 769;
    private static final int SIGNATURE_ECDSA_WITH_SHA256 = 513;
    private static final int SIGNATURE_ECDSA_WITH_SHA512 = 514;
    private static final int SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA256 = 259;
    private static final int SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA512 = 260;
    private static final int SIGNATURE_RSA_PSS_WITH_SHA256 = 257;
    private static final int SIGNATURE_RSA_PSS_WITH_SHA512 = 258;
    private static final String TAG = "SecurityProfileDigestMatcher";

    public static class SignatureNotFoundException extends Exception {
        private static final long serialVersionUID = 1;

        public SignatureNotFoundException(String message) {
            super(message);
        }

        public SignatureNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static byte[] calculateManifestDigest(String apkPath, String digestAlgorithm) {
        StrictJarFile jarFile = null;
        InputStream inputStream = null;
        try {
            jarFile = new StrictJarFile(apkPath, false, false);
            ZipEntry ze = jarFile.findEntry("META-INF/MANIFEST.MF");
            if (ze != null) {
                InputStream inputStream2 = jarFile.getInputStream(ze);
                byte[] b = Streams.readFully(inputStream2);
                MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
                md.update(b);
                byte[] digest = md.digest();
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e) {
                        Slog.w(TAG, "close inputStream exception!");
                    }
                }
                try {
                    jarFile.close();
                } catch (IOException e2) {
                    Slog.w(TAG, "close jar file counter exception!");
                }
                return digest;
            }
            Slog.d(TAG, "ZipEntry is null");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    Slog.w(TAG, "close inputStream exception!");
                }
            }
            try {
                jarFile.close();
            } catch (IOException e4) {
                Slog.w(TAG, "close jar file counter exception!");
            }
            return null;
        } catch (NoSuchAlgorithmException e5) {
            Slog.e(TAG, e5.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    Slog.w(TAG, "close inputStream exception!");
                }
            }
            if (jarFile != null) {
                jarFile.close();
            }
        } catch (IOException e7) {
            Slog.e(TAG, e7.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e8) {
                    Slog.w(TAG, "close inputStream exception!");
                }
            }
            if (jarFile != null) {
                jarFile.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e9) {
                    Slog.w(TAG, "close inputStream exception!");
                }
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e10) {
                    Slog.w(TAG, "close jar file counter exception!");
                }
            }
            throw th;
        }
    }

    public static boolean packageMatchesDigest(String apkPath, ApkDigest apkDigest) {
        byte[] calculatedDigest = apkDigest.apkSignatureScheme.equals("v1_manifest") ? calculateManifestDigest(apkPath, apkDigest.digestAlgorithm) : findDigest(apkPath, apkDigest.digestAlgorithm);
        if (calculatedDigest == null) {
            return false;
        }
        return apkDigest.base64Digest.equals(Base64.getEncoder().encodeToString(calculatedDigest));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0015, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0016, code lost:
        r4 = r3;
        r3 = r2;
        r2 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0011, code lost:
        r3 = null;
     */
    public static byte[] findDigest(String apkFile, String digestAlgorithm) {
        Throwable th;
        Throwable th2;
        try {
            RandomAccessFile apk = new RandomAccessFile(apkFile, "r");
            byte[] findDigest = findDigest(apk, digestAlgorithm);
            $closeResource(null, apk);
            return findDigest;
            $closeResource(th, apk);
            throw th2;
        } catch (SignatureNotFoundException e) {
            return null;
        } catch (IOException e2) {
            return null;
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

    private static byte[] findDigest(RandomAccessFile apk, String digestAlgorithm) throws IOException, SignatureNotFoundException {
        return findDigest(findSignatureBlock(apk), digestAlgorithm);
    }

    private static ByteBuffer findSignatureBlock(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        Pair<ByteBuffer, Long> eocdAndOffsetInFile = getEocd(apk);
        ByteBuffer eocd = (ByteBuffer) eocdAndOffsetInFile.first;
        long eocdOffset = ((Long) eocdAndOffsetInFile.second).longValue();
        if (!ZipUtils.isZip64EndOfCentralDirectoryLocatorPresent(apk, eocdOffset)) {
            return findApkSignatureSchemeV2Block((ByteBuffer) findApkSigningBlock(apk, getCentralDirOffset(eocd, eocdOffset)).first);
        }
        throw new SignatureNotFoundException("ZIP64 APK not supported");
    }

    private static byte[] findDigest(ByteBuffer signatureBlock, String jcaDigestAlgorithm) throws SecurityException {
        int signerCount = 0;
        try {
            ByteBuffer signers = getLengthPrefixedSlice(signatureBlock);
            while (signers.hasRemaining()) {
                signerCount++;
                try {
                    byte[] contentDigest = findMatchingContentDigest(getLengthPrefixedSlice(signers), jcaDigestAlgorithm);
                    if (contentDigest != null) {
                        return contentDigest;
                    }
                } catch (IOException | SecurityException | BufferUnderflowException e) {
                    throw new SecurityException("Failed to parse/verify signer #" + signerCount + " block", e);
                }
            }
            return null;
        } catch (IOException e2) {
            throw new SecurityException("Failed to read list of signers", e2);
        }
    }

    private static byte[] findMatchingContentDigest(ByteBuffer signerBlock, String jcaDigestAlgorithm) throws SecurityException, IOException {
        ByteBuffer digests = getLengthPrefixedSlice(getLengthPrefixedSlice(signerBlock));
        while (digests.hasRemaining()) {
            try {
                ByteBuffer digest = getLengthPrefixedSlice(digests);
                if (digest.remaining() < 8) {
                    throw new IOException("Record too short");
                } else if (getContentDigestAlgorithmJcaDigestAlgorithm(getSignatureAlgorithmContentDigestAlgorithm(digest.getInt())).equals(jcaDigestAlgorithm)) {
                    return readLengthPrefixedByteArray(digest);
                }
            } catch (IOException | BufferUnderflowException e) {
                throw new IOException("Failed to parse digest record ", e);
            }
        }
        return null;
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

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0026, code lost:
        return 2;
     */
    private static int getSignatureAlgorithmContentDigestAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm != SIGNATURE_DSA_WITH_SHA256) {
            switch (sigAlgorithm) {
                case SIGNATURE_RSA_PSS_WITH_SHA256 /*257*/:
                case SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA256 /*259*/:
                    break;
                case SIGNATURE_RSA_PSS_WITH_SHA512 /*258*/:
                case SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA512 /*260*/:
                    break;
                default:
                    switch (sigAlgorithm) {
                        case SIGNATURE_ECDSA_WITH_SHA256 /*513*/:
                            break;
                        case SIGNATURE_ECDSA_WITH_SHA512 /*514*/:
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Integer.toHexString(sigAlgorithm));
                    }
            }
        }
        return 1;
    }

    private static String getContentDigestAlgorithmJcaDigestAlgorithm(int digestAlgorithm) {
        switch (digestAlgorithm) {
            case 1:
                return "SHA-256";
            case 2:
                return "SHA-512";
            default:
                throw new IllegalArgumentException("Unknown content digest algorthm: " + digestAlgorithm);
        }
    }

    /* JADX INFO: finally extract failed */
    private static ByteBuffer sliceFromTo(ByteBuffer source, int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start: " + start);
        } else if (end >= start) {
            int capacity = source.capacity();
            if (end <= source.capacity()) {
                int originalLimit = source.limit();
                int originalPosition = source.position();
                try {
                    source.position(0);
                    source.limit(end);
                    source.position(start);
                    ByteBuffer result = source.slice();
                    result.order(source.order());
                    source.position(0);
                    source.limit(originalLimit);
                    source.position(originalPosition);
                    return result;
                } catch (Throwable th) {
                    source.position(0);
                    source.limit(originalLimit);
                    source.position(originalPosition);
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("end > capacity: " + end + " > " + capacity);
            }
        } else {
            throw new IllegalArgumentException("end < start: " + end + " < " + start);
        }
    }

    private static ByteBuffer getByteBuffer(ByteBuffer source, int size) throws BufferUnderflowException {
        if (size >= 0) {
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
        } else {
            throw new IllegalArgumentException("size: " + size);
        }
    }

    private static ByteBuffer getLengthPrefixedSlice(ByteBuffer source) throws IOException {
        if (source.remaining() >= 4) {
            int len = source.getInt();
            if (len < 0) {
                throw new IllegalArgumentException("Negative length");
            } else if (len <= source.remaining()) {
                return getByteBuffer(source, len);
            } else {
                throw new IOException("Length-prefixed field longer than remaining buffer. Field length: " + len + ", remaining: " + source.remaining());
            }
        } else {
            throw new IOException("Remaining buffer too short to contain length of length-prefixed field. Remaining: " + source.remaining());
        }
    }

    private static byte[] readLengthPrefixedByteArray(ByteBuffer buf) throws IOException {
        int len = buf.getInt();
        if (len < 0) {
            throw new IOException("Negative length");
        } else if (len <= buf.remaining()) {
            byte[] result = new byte[len];
            buf.get(result);
            return result;
        } else {
            throw new IOException("Underflow while reading length-prefixed value. Length: " + len + ", available: " + buf.remaining());
        }
    }

    private static Pair<ByteBuffer, Long> findApkSigningBlock(RandomAccessFile apk, long centralDirOffset) throws IOException, SignatureNotFoundException {
        if (centralDirOffset >= 32) {
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
                if (apkSigBlockOffset >= 0) {
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
                throw new SignatureNotFoundException("APK Signing Block offset out of range: " + apkSigBlockOffset);
            }
            throw new SignatureNotFoundException("No APK Signing Block before ZIP Central Directory");
        }
        throw new SignatureNotFoundException("APK too small for APK Signing Block. ZIP Central Directory offset: " + centralDirOffset);
    }

    private static ByteBuffer findApkSignatureSchemeV2Block(ByteBuffer apkSigningBlock) throws SignatureNotFoundException {
        checkByteOrderLittleEndian(apkSigningBlock);
        ByteBuffer pairs = sliceFromTo(apkSigningBlock, 8, apkSigningBlock.capacity() - 24);
        int entryCount = 0;
        while (pairs.hasRemaining()) {
            entryCount++;
            if (pairs.remaining() >= 8) {
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
            } else {
                throw new SignatureNotFoundException("Insufficient data to read size of APK Signing Block entry #" + entryCount);
            }
        }
        throw new SignatureNotFoundException("No APK Signature Scheme v2 block in APK Signing Block");
    }

    private static void checkByteOrderLittleEndian(ByteBuffer buffer) {
        if (buffer.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("ByteBuffer byte order must be little endian");
        }
    }

    public static ApkDigest getApkDigest(String apkPath) {
        ApkDigest digest = calculateV2ApkDigest(apkPath);
        if (digest == null) {
            return calculateV1ApkDigest(apkPath);
        }
        return digest;
    }

    public static ApkDigest calculateV1ApkDigest(String apkPath) {
        byte[] manifestDigest = calculateManifestDigest(apkPath, "SHA-256");
        if (manifestDigest != null) {
            return new ApkDigest("v1_manifest", "SHA-256", Base64.getEncoder().encodeToString(manifestDigest));
        }
        Slog.w(TAG, "calculateV1ApkDigest got null");
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001a, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001b, code lost:
        r5 = r3;
        r3 = r2;
        r2 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0015, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0016, code lost:
        r3 = null;
     */
    public static ApkDigest calculateV2ApkDigest(String apkPath) {
        Throwable th;
        Throwable th2;
        try {
            RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
            ApkDigest apkDigest = findDigestInline(findSignatureBlock(apk));
            $closeResource(null, apk);
            return apkDigest;
            $closeResource(th, apk);
            throw th2;
        } catch (SignatureNotFoundException e) {
            Slog.w(TAG, "calculateV2ApkDigest SignatureNotFound : " + e.getMessage());
            return null;
        } catch (IOException e2) {
            Slog.e(TAG, "calculateV2ApkDigest IOException : " + e2.getMessage());
            return null;
        } catch (Exception e3) {
            Slog.e(TAG, "calculateV2ApkDigest Exception : " + e3.getMessage());
            return null;
        }
    }

    private static ApkDigest findDigestInline(ByteBuffer signatureBlock) {
        int signerCount = 0;
        try {
            ByteBuffer signers = getLengthPrefixedSlice(signatureBlock);
            while (signers.hasRemaining()) {
                signerCount++;
                try {
                    ByteBuffer digests = getLengthPrefixedSlice(getLengthPrefixedSlice(getLengthPrefixedSlice(signers)));
                    while (true) {
                        if (digests.hasRemaining()) {
                            ByteBuffer digest = getLengthPrefixedSlice(digests);
                            if (digest.remaining() >= 8) {
                                String jcaDigestAlgorithm = getContentDigestAlgorithmJcaDigestAlgorithm(getSignatureAlgorithmContentDigestAlgorithm(digest.getInt()));
                                byte[] digestData = readLengthPrefixedByteArray(digest);
                                if (digestData != null) {
                                    return new ApkDigest("v2", jcaDigestAlgorithm, Base64.getEncoder().encodeToString(digestData));
                                }
                            } else {
                                throw new IOException("Record too short");
                            }
                        }
                    }
                } catch (IOException | SecurityException | BufferUnderflowException e) {
                    throw new SecurityException("Failed to parse/verify signer #" + signerCount + " block", e);
                }
            }
            return null;
        } catch (IOException e2) {
            throw new SecurityException("Failed to read list of signers", e2);
        }
    }
}
