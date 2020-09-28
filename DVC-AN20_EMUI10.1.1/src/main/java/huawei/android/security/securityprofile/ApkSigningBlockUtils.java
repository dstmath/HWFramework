package huawei.android.security.securityprofile;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import com.huawei.security.keystore.HwKeyProperties;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class ApkSigningBlockUtils {
    private static final int APK_SIGNATURE_SCHEME_V2_BLOCK_ID = 1896449818;
    private static final int APK_SIGNATURE_SCHEME_V3_BLOCK_ID = -262969152;
    private static final long APK_SIG_BLOCK_MAGIC_HI = 3617552046287187010L;
    private static final long APK_SIG_BLOCK_MAGIC_LO = 2334950737559900225L;
    private static final int APK_SIG_BLOCK_MIN_SIZE = 32;
    static final int CONTENT_DIGEST_CHUNKED_SHA256 = 1;
    static final int CONTENT_DIGEST_CHUNKED_SHA512 = 2;
    static final int CONTENT_DIGEST_VERITY_CHUNKED_SHA256 = 3;
    private static final long HUAWEI_BLOCK_MAGIC_HI = 8746950815728558949L;
    private static final long HUAWEI_BLOCK_MAGIC_LO = 5989903388918248776L;
    private static final int HUAWEI_SECURITY_POLICY_BLOCK_ID = 1212241481;
    private static final int NORMAL_MANIFEST_SIZE = 8192;
    private static final int ONE_BYTE_BITS = 8;
    static final int SIGNATURE_DSA_WITH_SHA256 = 769;
    static final int SIGNATURE_ECDSA_WITH_SHA256 = 513;
    static final int SIGNATURE_ECDSA_WITH_SHA512 = 514;
    static final int SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA256 = 259;
    static final int SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA512 = 260;
    static final int SIGNATURE_RSA_PSS_WITH_SHA256 = 257;
    static final int SIGNATURE_RSA_PSS_WITH_SHA512 = 258;
    static final int SIGNATURE_VERITY_DSA_WITH_SHA256 = 1061;
    static final int SIGNATURE_VERITY_ECDSA_WITH_SHA256 = 1059;
    static final int SIGNATURE_VERITY_RSA_PKCS1_V1_5_WITH_SHA256 = 1057;
    private static final String TAG = "ApkSigningBlockUtils";

    private ApkSigningBlockUtils() {
    }

    public static ApkDigest calculateApkDigest(String apkPath) {
        if (apkPath == null) {
            return null;
        }
        ApkDigest digest = calculateV2ApkDigest(apkPath);
        if (digest == null) {
            return calculateV1ApkDigest(apkPath);
        }
        return digest;
    }

    static SignatureInfo findHwSignature(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        return findSignature(apk, HUAWEI_SECURITY_POLICY_BLOCK_ID);
    }

    @NonNull
    static SignatureInfo findV2Signature(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        return findSignature(apk, APK_SIGNATURE_SCHEME_V2_BLOCK_ID);
    }

    static SignatureInfo findV3Signature(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        return findSignature(apk, APK_SIGNATURE_SCHEME_V3_BLOCK_ID);
    }

    @Nullable
    static byte[] findV1Digest(String apkPath, String digestAlgorithm) {
        return calculateManifestDigest(apkPath, digestAlgorithm);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0024, code lost:
        throw r4;
     */
    @Nullable
    static byte[] findV2Digest(String apkPath, String digestAlgorithm) {
        try {
            RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
            byte[] findDigest = findDigest(findV2Signature(apk).signatureBlock, digestAlgorithm);
            try {
                apk.close();
                return findDigest;
            } catch (IOException | SecurityException e) {
                Log.e(TAG, "findV2Digest failed: " + e.getMessage());
                return null;
            }
        } catch (SignatureNotFoundException e2) {
            Log.e(TAG, "findV2Digest no APK V2 signature block: " + e2.getMessage());
            return null;
        } catch (FileNotFoundException e3) {
            Log.e(TAG, "findV2Digest failed: invalid file");
            return null;
        }
    }

    @Nullable
    static byte[] findV3Digest(String apkPath, String digestAlgorithm) {
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006f, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0070, code lost:
        if (r5 != null) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0076, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0077, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007a, code lost:
        throw r7;
     */
    @Nullable
    private static byte[] calculateManifestDigest(String apkPath, String digestAlgorithm) {
        JarFile jarFile = new JarFile(apkPath, false);
        ZipEntry zipEntry = jarFile.getEntry("META-INF/MANIFEST.MF");
        if (zipEntry == null) {
            Log.w(TAG, "ZipEntry is null");
            try {
                jarFile.close();
            } catch (IOException e) {
                Log.w(TAG, "close jar file counter exception: " + e.getMessage());
            }
            return null;
        }
        try {
            InputStream inputStream = jarFile.getInputStream(zipEntry);
            byte[] bytes = readFully(inputStream);
            MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
            messageDigest.update(bytes);
            byte[] digest = messageDigest.digest();
            if (inputStream != null) {
                inputStream.close();
            }
            try {
                jarFile.close();
            } catch (IOException e2) {
                Log.w(TAG, "close jar file counter exception: " + e2.getMessage());
            }
            return digest;
        } catch (IOException | NoSuchAlgorithmException e3) {
            Log.e(TAG, "calculate v1 manifest digest failed: " + e3.getMessage());
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e4) {
                    Log.w(TAG, "close jar file counter exception: " + e4.getMessage());
                }
            }
            return null;
        } catch (Throwable th) {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e5) {
                    Log.w(TAG, "close jar file counter exception: " + e5.getMessage());
                }
            }
            throw th;
        }
    }

    private static byte[] readFully(InputStream in) throws IOException {
        try {
            return readFullyNoClose(in);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                Log.w(TAG, "ignore input strean close exception!");
            }
        }
    }

    private static byte[] readFullyNoClose(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            while (true) {
                int count = in.read(buffer);
                if (count != -1) {
                    out.write(buffer, 0, count);
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                    }
                }
            }
            return out.toByteArray();
        } finally {
            try {
                out.close();
            } catch (IOException e2) {
                Log.w(TAG, "close ByteArrayOutputStream exception!");
            }
        }
    }

    @Nullable
    private static ApkDigest calculateV1ApkDigest(String apkPath) {
        byte[] manifestDigest = calculateManifestDigest(apkPath, HwKeyProperties.DIGEST_SHA256);
        if (manifestDigest != null) {
            return new ApkDigest("v1_manifest", HwKeyProperties.DIGEST_SHA256, Base64.getEncoder().encodeToString(manifestDigest));
        }
        Log.w(TAG, "calculateV1ApkDigest got null");
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0021, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0024, code lost:
        throw r4;
     */
    @Nullable
    private static ApkDigest calculateV2ApkDigest(String apkPath) {
        try {
            RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
            ApkDigest apkDigest = findDigestInline(findV2Signature(apk).signatureBlock);
            apk.close();
            return apkDigest;
        } catch (SignatureNotFoundException e) {
            Log.e(TAG, "calculateV2ApkDigest SignatureNotFound : " + e.getMessage());
            return null;
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "calculateV2ApkDigest invalid file");
            return null;
        } catch (IOException e3) {
            Log.e(TAG, "calculateV2ApkDigest IOException : " + e3.getMessage());
            return null;
        }
    }

    @Nullable
    private static ApkDigest findDigestInline(ByteBuffer signatureBlock) {
        int signerCount = 0;
        try {
            ByteBuffer signers = getLengthPrefixedSlice(signatureBlock);
            while (signers.hasRemaining()) {
                signerCount++;
                ByteBuffer digests = getLengthPrefixedSlice(getLengthPrefixedSlice(getLengthPrefixedSlice(signers)));
                while (true) {
                    if (digests.hasRemaining()) {
                        ByteBuffer digest = getLengthPrefixedSlice(digests);
                        if (digest.remaining() < 8) {
                            Log.e(TAG, "Find digest inline failed for Record too short!");
                            return null;
                        }
                        String jcaDigestAlgorithm = getContentDigestAlgorithmJcaDigestAlgorithm(getSignatureAlgorithmContentDigestAlgorithm(digest.getInt()));
                        byte[] digestData = readLengthPrefixedByteArray(digest);
                        if (digestData != null) {
                            return new ApkDigest("v2", jcaDigestAlgorithm, Base64.getEncoder().encodeToString(digestData));
                        }
                    }
                }
            }
        } catch (IOException | BufferUnderflowException e) {
            Log.e(TAG, "Find digest inline failed to parse/verify signer #" + 0 + " block: " + e.getMessage());
        }
        return null;
    }

    @Nullable
    static byte[] findDigest(ByteBuffer signatureBlock, String jcaDigestAlgorithm) {
        int signerCount = 0;
        try {
            ByteBuffer signers = getLengthPrefixedSlice(signatureBlock);
            while (signers.hasRemaining()) {
                signerCount++;
                byte[] contentDigest = findMatchingContentDigest(getLengthPrefixedSlice(signers), jcaDigestAlgorithm);
                if (contentDigest != null) {
                    return contentDigest;
                }
            }
            return null;
        } catch (IOException | BufferUnderflowException e) {
            Log.e(TAG, "Failed to parse/verify signer #" + 0 + " block: " + e.getMessage());
            return null;
        }
    }

    @Nullable
    private static byte[] findMatchingContentDigest(ByteBuffer signerBlock, String jcaDigestAlgorithm) throws IOException {
        try {
            ByteBuffer digests = getLengthPrefixedSlice(getLengthPrefixedSlice(signerBlock));
            while (digests.hasRemaining()) {
                ByteBuffer digest = getLengthPrefixedSlice(digests);
                if (digest.remaining() < 8) {
                    Log.e(TAG, "MatchingContentDigestFailed Record too short");
                    return null;
                } else if (getContentDigestAlgorithmJcaDigestAlgorithm(getSignatureAlgorithmContentDigestAlgorithm(digest.getInt())).equals(jcaDigestAlgorithm)) {
                    return readLengthPrefixedByteArray(digest);
                }
            }
        } catch (IOException | BufferUnderflowException e) {
            Log.e(TAG, "MatchingContentDigest Failed to parse digest record : " + e.getMessage());
        }
        return null;
    }

    static SignatureInfo findSignature(RandomAccessFile apk, int blockId) throws IOException, SignatureNotFoundException {
        Pair<ByteBuffer, Long> apkSigningBlockAndOffsetInFile;
        Pair<ByteBuffer, Long> eocdAndOffsetInFile = getEocd(apk);
        ByteBuffer eocd = (ByteBuffer) eocdAndOffsetInFile.first;
        long eocdOffset = ((Long) eocdAndOffsetInFile.second).longValue();
        if (!ZipUtils.isZip64EndOfCentralDirectoryLocatorPresent(apk, eocdOffset)) {
            long centralDirOffset = getCentralDirOffset(eocd, eocdOffset);
            try {
                apkSigningBlockAndOffsetInFile = findApkSigningBlock(apk, centralDirOffset, APK_SIG_BLOCK_MAGIC_HI, APK_SIG_BLOCK_MAGIC_LO);
            } catch (SignatureNotFoundException e) {
                apkSigningBlockAndOffsetInFile = findApkSigningBlock(apk, centralDirOffset, HUAWEI_BLOCK_MAGIC_HI, HUAWEI_BLOCK_MAGIC_LO);
            }
            ByteBuffer apkSigningBlock = (ByteBuffer) apkSigningBlockAndOffsetInFile.first;
            return new SignatureInfo(findApkSignatureSchemeBlock(apkSigningBlock, blockId), ((Long) apkSigningBlockAndOffsetInFile.second).longValue(), centralDirOffset, eocdOffset, eocd);
        }
        throw new SignatureNotFoundException("ZIP64 APK not supported");
    }

    static Pair<ByteBuffer, Long> getEocd(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        Pair<ByteBuffer, Long> eocdAndOffsetInFile = ZipUtils.findZipEndOfCentralDirectoryRecord(apk);
        if (eocdAndOffsetInFile != null) {
            return eocdAndOffsetInFile;
        }
        throw new SignatureNotFoundException("Not an APK file: ZIP End of Central Directory record not found");
    }

    static long getCentralDirOffset(ByteBuffer eocd, long eocdOffset) throws SignatureNotFoundException {
        long centralDirOffset = ZipUtils.getZipEocdCentralDirectoryOffset(eocd);
        if (centralDirOffset > eocdOffset) {
            throw new SignatureNotFoundException("ZIP Central Directory offset out of range: " + centralDirOffset + ". ZIP End of Central Directory offset: " + eocdOffset);
        } else if (centralDirOffset + ZipUtils.getZipEocdCentralDirectorySizeBytes(eocd) == eocdOffset) {
            return centralDirOffset;
        } else {
            throw new SignatureNotFoundException("ZIP Central Directory is not immediately followed by End of Central Directory");
        }
    }

    static int getSignatureAlgorithmContentDigestAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm == SIGNATURE_ECDSA_WITH_SHA256) {
            return 1;
        }
        if (sigAlgorithm == SIGNATURE_ECDSA_WITH_SHA512) {
            return 2;
        }
        if (sigAlgorithm == SIGNATURE_DSA_WITH_SHA256) {
            return 1;
        }
        if (sigAlgorithm == SIGNATURE_VERITY_RSA_PKCS1_V1_5_WITH_SHA256 || sigAlgorithm == SIGNATURE_VERITY_ECDSA_WITH_SHA256 || sigAlgorithm == SIGNATURE_VERITY_DSA_WITH_SHA256) {
            return 3;
        }
        switch (sigAlgorithm) {
            case SIGNATURE_RSA_PSS_WITH_SHA256 /*{ENCODED_INT: 257}*/:
            case SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA256 /*{ENCODED_INT: 259}*/:
                return 1;
            case SIGNATURE_RSA_PSS_WITH_SHA512 /*{ENCODED_INT: 258}*/:
            case SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA512 /*{ENCODED_INT: 260}*/:
                return 2;
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) sigAlgorithm));
        }
    }

    static String getContentDigestAlgorithmJcaDigestAlgorithm(int digestAlgorithm) {
        if (digestAlgorithm == 1) {
            return HwKeyProperties.DIGEST_SHA256;
        }
        if (digestAlgorithm == 2) {
            return HwKeyProperties.DIGEST_SHA512;
        }
        if (digestAlgorithm == 3) {
            return HwKeyProperties.DIGEST_SHA256;
        }
        throw new IllegalArgumentException("Unknown content digest algorithm: " + digestAlgorithm);
    }

    /* JADX INFO: finally extract failed */
    static ByteBuffer sliceFromTo(ByteBuffer source, int start, int end) {
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

    static ByteBuffer getByteBuffer(ByteBuffer source, int size) throws BufferUnderflowException {
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

    static ByteBuffer getLengthPrefixedSlice(ByteBuffer source) throws IOException {
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

    static byte[] readLengthPrefixedByteArray(ByteBuffer buf) throws IOException {
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

    static Pair<ByteBuffer, Long> findApkSigningBlock(RandomAccessFile apk, long centralDirOffset, long hi, long lo) throws IOException, SignatureNotFoundException {
        if (centralDirOffset >= 32) {
            ByteBuffer footer = ByteBuffer.allocate(24);
            footer.order(ByteOrder.LITTLE_ENDIAN);
            apk.seek(centralDirOffset - ((long) footer.capacity()));
            apk.readFully(footer.array(), footer.arrayOffset(), footer.capacity());
            if (footer.getLong(8) == lo && footer.getLong(16) == hi) {
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

    static ByteBuffer findApkSignatureSchemeBlock(ByteBuffer apkSigningBlock, int blockId) throws SignatureNotFoundException {
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
                } else if (pairs.getInt() == blockId) {
                    return getByteBuffer(pairs, len - 4);
                } else {
                    pairs.position(nextEntryPos);
                }
            } else {
                throw new SignatureNotFoundException("Insufficient data to read size of APK Signing Block entry #" + entryCount);
            }
        }
        throw new SignatureNotFoundException("No block with ID " + blockId + " in APK Signing Block.");
    }

    private static void checkByteOrderLittleEndian(ByteBuffer buffer) {
        if (buffer.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("ByteBuffer byte order must be little endian");
        }
    }
}
