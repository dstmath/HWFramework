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
    private static final long HUAWEI_BLOCK_MAGIC_HI = 8746950815728558949L;
    private static final long HUAWEI_BLOCK_MAGIC_LO = 5989903388918248776L;
    private static final int HUAWEI_SECURITY_POLICY_BLOCK_ID = 1212241481;
    private static final int MIN_SIZE = 4;
    private static final int NORMAL_MANIFEST_SIZE = 8192;
    private static final int OFFSET_ANOTHER_UINT64 = 24;
    private static final int OFFSET_MAGIC = 16;
    private static final int OFFSET_PAIRS = 8;
    private static final int OFFSET_PAY_LOAD = 8;
    private static final int ONE_BYTE_BITS = 8;
    private static final int SHA256_CONTENT_DIGEST_CHUNKED = 1;
    private static final int SHA256_CONTENT_DIGEST_VERITY_CHUNKED = 3;
    static final int SHA256_DSA_SIGNATURE = 769;
    static final int SHA256_ECDSA_SIGNATURE = 513;
    static final int SHA256_RSA_PKCS1_V1_5_SIGNATURE = 259;
    static final int SHA256_RSA_PSS_SIGNATURE = 257;
    static final int SHA256_VERITY_DSA_SIGNATURE = 1061;
    static final int SHA256_VERITY_ECDSA_SIGNATURE = 1059;
    static final int SHA256_VERITY_RSA_PKCS1_V1_5_SIGNATURE = 1057;
    private static final int SHA512_CONTENT_DIGEST_CHUNKED = 2;
    static final int SHA512_ECDSA_SIGNATURE = 514;
    static final int SHA512_RSA_PKCS1_V1_5_SIGNATURE = 260;
    static final int SHA512_RSA_PSS_SIGNATURE = 258;
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

    static SignatureInformation findHwSignature(RandomAccessFile apk) throws IOException, SignatureInvalidException {
        return getSignature(apk, HUAWEI_SECURITY_POLICY_BLOCK_ID);
    }

    @NonNull
    static SignatureInformation findV2Signature(RandomAccessFile apk) throws IOException, SignatureInvalidException {
        return getSignature(apk, APK_SIGNATURE_SCHEME_V2_BLOCK_ID);
    }

    static SignatureInformation findV3Signature(RandomAccessFile apk) throws IOException, SignatureInvalidException {
        return getSignature(apk, APK_SIGNATURE_SCHEME_V3_BLOCK_ID);
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
            byte[] findDigest = findDigest(findV2Signature(apk).signatureBlockContent, digestAlgorithm);
            try {
                apk.close();
                return findDigest;
            } catch (IOException | SecurityException e) {
                Log.e(TAG, "findV2Digest failed: " + e.getMessage());
                return null;
            }
        } catch (SignatureInvalidException e2) {
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
            ApkDigest apkDigest = findDigestInline(findV2Signature(apk).signatureBlockContent);
            apk.close();
            return apkDigest;
        } catch (SignatureInvalidException e) {
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
            ByteBuffer signers = getSlicePrefixedLength(signatureBlock);
            while (signers.hasRemaining()) {
                signerCount++;
                ByteBuffer digests = getSlicePrefixedLength(getSlicePrefixedLength(getSlicePrefixedLength(signers)));
                while (true) {
                    if (digests.hasRemaining()) {
                        ByteBuffer digest = getSlicePrefixedLength(digests);
                        if (digest.remaining() < 8) {
                            Log.e(TAG, "Find digest inline failed for Record too short!");
                            return null;
                        }
                        String jcaDigestAlgorithm = getJcaDigestAlgorithm(getContentDigestAlgorithm(digest.getInt()));
                        byte[] digestData = getByteArrayPrefixedLength(digest);
                        if (digestData != null) {
                            return new ApkDigest("v2", jcaDigestAlgorithm, Base64.getEncoder().encodeToString(digestData));
                        }
                    }
                }
            }
        } catch (IOException | BufferUnderflowException e) {
            Log.e(TAG, "Find digest inline failed to parse/verify signer #0 block: " + e.getMessage());
        }
        return null;
    }

    @Nullable
    static byte[] findDigest(ByteBuffer signatureBlock, String jcaDigestAlgorithm) {
        int signerCount = 0;
        try {
            ByteBuffer signers = getSlicePrefixedLength(signatureBlock);
            while (signers.hasRemaining()) {
                signerCount++;
                byte[] contentDigest = findMatchingContentDigest(getSlicePrefixedLength(signers), jcaDigestAlgorithm);
                if (contentDigest != null) {
                    return contentDigest;
                }
            }
            return null;
        } catch (IOException | BufferUnderflowException e) {
            Log.e(TAG, "Failed to parse/verify signer #0 block: " + e.getMessage());
            return null;
        }
    }

    @Nullable
    private static byte[] findMatchingContentDigest(ByteBuffer signerBlock, String jcaDigestAlgorithm) throws IOException {
        try {
            ByteBuffer digests = getSlicePrefixedLength(getSlicePrefixedLength(signerBlock));
            while (digests.hasRemaining()) {
                ByteBuffer digest = getSlicePrefixedLength(digests);
                if (digest.remaining() < 8) {
                    Log.e(TAG, "MatchingContentDigestFailed Record too short");
                    return null;
                } else if (getJcaDigestAlgorithm(getContentDigestAlgorithm(digest.getInt())).equals(jcaDigestAlgorithm)) {
                    return getByteArrayPrefixedLength(digest);
                }
            }
        } catch (IOException | BufferUnderflowException e) {
            Log.e(TAG, "MatchingContentDigest Failed to parse digest record : " + e.getMessage());
        }
        return null;
    }

    static SignatureInformation getSignature(RandomAccessFile apk, int blockId) throws IOException, SignatureInvalidException {
        Pair<ByteBuffer, Long> apkSigningBlockAndOffsetFromApk;
        Pair<ByteBuffer, Long> eocdAndOffsetFromApk = getEocdFromApk(apk);
        ByteBuffer eocd = (ByteBuffer) eocdAndOffsetFromApk.first;
        long eocdOffset = ((Long) eocdAndOffsetFromApk.second).longValue();
        if (!ZipHelper.isZip64EocDLocatorPresent(apk, eocdOffset)) {
            long centralDirOffset = getCentralDirectoryOffset(eocd, eocdOffset);
            try {
                apkSigningBlockAndOffsetFromApk = getApkSigBlock(apk, centralDirOffset, APK_SIG_BLOCK_MAGIC_HI, APK_SIG_BLOCK_MAGIC_LO);
            } catch (SignatureInvalidException e) {
                apkSigningBlockAndOffsetFromApk = getApkSigBlock(apk, centralDirOffset, HUAWEI_BLOCK_MAGIC_HI, HUAWEI_BLOCK_MAGIC_LO);
            }
            return new SignatureInformation(eocd, eocdOffset, centralDirOffset, ((Long) apkSigningBlockAndOffsetFromApk.second).longValue(), getApkSigSchemeBlock((ByteBuffer) apkSigningBlockAndOffsetFromApk.first, blockId));
        }
        throw new SignatureInvalidException("the ZIP64 APK is not supported");
    }

    static Pair<ByteBuffer, Long> getEocdFromApk(RandomAccessFile apk) throws IOException, SignatureInvalidException {
        Pair<ByteBuffer, Long> eocdAndOffsetFromApk = ZipHelper.findZipEocdRecord(apk);
        if (eocdAndOffsetFromApk != null) {
            return eocdAndOffsetFromApk;
        }
        throw new SignatureInvalidException("Not an APK file: ZIP EOCD record is not found");
    }

    static String getJcaDigestAlgorithm(int digestAlgorithm) {
        if (digestAlgorithm == 1) {
            return HwKeyProperties.DIGEST_SHA256;
        }
        if (digestAlgorithm == 2) {
            return HwKeyProperties.DIGEST_SHA512;
        }
        if (digestAlgorithm == 3) {
            return HwKeyProperties.DIGEST_SHA256;
        }
        throw new IllegalArgumentException("Unknown content digest algorithm about: " + digestAlgorithm);
    }

    static ByteBuffer readByteBuffer(ByteBuffer buffer, int length) throws BufferUnderflowException {
        if (length >= 0) {
            int initialLimit = buffer.limit();
            int pos = buffer.position();
            int realLimit = pos + length;
            if (realLimit > initialLimit || realLimit < pos) {
                throw new BufferUnderflowException();
            }
            buffer.limit(realLimit);
            try {
                ByteBuffer result = buffer.slice();
                result.order(buffer.order());
                buffer.position(realLimit);
                return result;
            } finally {
                buffer.limit(initialLimit);
            }
        } else {
            throw new IllegalArgumentException("the size: " + length);
        }
    }

    static int getContentDigestAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm == SHA256_ECDSA_SIGNATURE) {
            return 1;
        }
        if (sigAlgorithm == SHA512_ECDSA_SIGNATURE) {
            return 2;
        }
        if (sigAlgorithm == SHA256_DSA_SIGNATURE) {
            return 1;
        }
        if (sigAlgorithm == SHA256_VERITY_RSA_PKCS1_V1_5_SIGNATURE || sigAlgorithm == SHA256_VERITY_ECDSA_SIGNATURE || sigAlgorithm == SHA256_VERITY_DSA_SIGNATURE) {
            return 3;
        }
        switch (sigAlgorithm) {
            case 257:
            case SHA256_RSA_PKCS1_V1_5_SIGNATURE /* 259 */:
                return 1;
            case SHA512_RSA_PSS_SIGNATURE /* 258 */:
            case SHA512_RSA_PKCS1_V1_5_SIGNATURE /* 260 */:
                return 2;
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) sigAlgorithm));
        }
    }

    static ByteBuffer sliceRange(ByteBuffer sourceBuffer, int begin, int end) {
        if (begin < 0) {
            throw new IllegalArgumentException("begin: " + begin);
        } else if (end >= begin) {
            int capacity = sourceBuffer.capacity();
            if (end <= sourceBuffer.capacity()) {
                int originalPosition = sourceBuffer.position();
                int originalLimit = sourceBuffer.limit();
                try {
                    sourceBuffer.position(0);
                    sourceBuffer.position(begin);
                    sourceBuffer.limit(end);
                    ByteBuffer result = sourceBuffer.slice();
                    result.order(sourceBuffer.order());
                    return result;
                } finally {
                    sourceBuffer.position(0);
                    sourceBuffer.limit(originalLimit);
                    sourceBuffer.position(originalPosition);
                }
            } else {
                throw new IllegalArgumentException("end > capacity: " + end + " > " + capacity);
            }
        } else {
            throw new IllegalArgumentException("end < begin: " + end + " < " + begin);
        }
    }

    static byte[] getByteArrayPrefixedLength(ByteBuffer byteBuffer) throws IOException {
        int length = byteBuffer.getInt();
        if (length < 0) {
            throw new IOException("Negative length:" + length);
        } else if (length <= byteBuffer.remaining()) {
            byte[] consequence = new byte[length];
            byteBuffer.get(consequence);
            return consequence;
        } else {
            throw new IOException("Underflow while reading length-prefixed value. the length: " + length + ", available: " + byteBuffer.remaining());
        }
    }

    static ByteBuffer getSlicePrefixedLength(ByteBuffer sourceBuffer) throws IOException {
        if (sourceBuffer.remaining() >= 4) {
            int length = sourceBuffer.getInt();
            if (length < 0) {
                throw new IllegalArgumentException("Negative length");
            } else if (length <= sourceBuffer.remaining()) {
                return readByteBuffer(sourceBuffer, length);
            } else {
                throw new IOException("Length-prefixed field is longer than remaining buffer. Field length: " + length + ", remaining: " + sourceBuffer.remaining());
            }
        } else {
            throw new IOException("Remaining buffer is too short to contain length of length-prefixed field. Remaining: " + sourceBuffer.remaining());
        }
    }

    static long getCentralDirectoryOffset(ByteBuffer eocdBuffer, long eocdOffset) throws SignatureInvalidException {
        long centralDirectoryOffset = ZipHelper.getZipCentralDirectoryOffset(eocdBuffer);
        if (centralDirectoryOffset > eocdOffset) {
            throw new SignatureInvalidException("ZIP Central Directory offset out of range: " + centralDirectoryOffset + ". ZIP EOCD offset: " + eocdOffset);
        } else if (centralDirectoryOffset + ZipHelper.getZipCentralDirectoryLength(eocdBuffer) == eocdOffset) {
            return centralDirectoryOffset;
        } else {
            throw new SignatureInvalidException("ZIP Central Directory is not immediately followed by EOCD");
        }
    }

    static ByteBuffer getApkSigSchemeBlock(ByteBuffer apkSigBlock, int blockId) throws SignatureInvalidException {
        assertByteOrderLittleEndian(apkSigBlock);
        ByteBuffer pairs = sliceRange(apkSigBlock, 8, apkSigBlock.capacity() - 24);
        int countEntry = 0;
        while (pairs.hasRemaining()) {
            countEntry++;
            if (pairs.remaining() >= 8) {
                long lenLong = pairs.getLong();
                if (lenLong < 4 || lenLong > 2147483647L) {
                    throw new SignatureInvalidException("APK Signing Block entry #" + countEntry + " is size out of range: " + lenLong);
                }
                int lenInt = (int) lenLong;
                int nextEntryPosition = pairs.position() + lenInt;
                if (lenInt > pairs.remaining()) {
                    throw new SignatureInvalidException("APK Signing Block entry #" + countEntry + " is size out of range: " + lenInt + ", available: " + pairs.remaining());
                } else if (pairs.getInt() == blockId) {
                    return readByteBuffer(pairs, lenInt - 4);
                } else {
                    pairs.position(nextEntryPosition);
                }
            } else {
                throw new SignatureInvalidException("Insufficient data in reading size of APK Signing Block entry #" + countEntry);
            }
        }
        throw new SignatureInvalidException("No blockId: " + blockId + " in APK Signing Block.");
    }

    static Pair<ByteBuffer, Long> getApkSigBlock(RandomAccessFile apk, long centralDirectoryOffset, long high, long low) throws IOException, SignatureInvalidException {
        if (centralDirectoryOffset >= 32) {
            ByteBuffer footerBuffer = ByteBuffer.allocate(OFFSET_ANOTHER_UINT64);
            footerBuffer.order(ByteOrder.LITTLE_ENDIAN);
            apk.seek(centralDirectoryOffset - ((long) footerBuffer.capacity()));
            apk.readFully(footerBuffer.array(), footerBuffer.arrayOffset(), footerBuffer.capacity());
            if (footerBuffer.getLong(8) == low && footerBuffer.getLong(16) == high) {
                long apkSigBlockSizeOfFooter = footerBuffer.getLong(0);
                if (apkSigBlockSizeOfFooter < ((long) footerBuffer.capacity()) || apkSigBlockSizeOfFooter > 2147483639) {
                    throw new SignatureInvalidException("APK Signing Block size is out of range: " + apkSigBlockSizeOfFooter);
                }
                int requireSize = (int) (8 + apkSigBlockSizeOfFooter);
                long apkSigBlockRealOffset = centralDirectoryOffset - ((long) requireSize);
                if (apkSigBlockRealOffset >= 0) {
                    ByteBuffer apkSigBlockBuffer = ByteBuffer.allocate(requireSize);
                    apkSigBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    apk.seek(apkSigBlockRealOffset);
                    apk.readFully(apkSigBlockBuffer.array(), apkSigBlockBuffer.arrayOffset(), apkSigBlockBuffer.capacity());
                    long apkSigBlockSizeOfHeader = apkSigBlockBuffer.getLong(0);
                    if (apkSigBlockSizeOfHeader == apkSigBlockSizeOfFooter) {
                        return Pair.create(apkSigBlockBuffer, Long.valueOf(apkSigBlockRealOffset));
                    }
                    throw new SignatureInvalidException("the sizes of APK Signing Block in header and footer do not match: " + apkSigBlockSizeOfHeader + " vs " + apkSigBlockSizeOfFooter);
                }
                throw new SignatureInvalidException("APK Signing Block offset is out of range: " + apkSigBlockRealOffset);
            }
            throw new SignatureInvalidException("It is no APK Signing Block before ZIP Central Directory");
        }
        throw new SignatureInvalidException("it is too small for APK Signing Block. ZIP Central Directory offset: " + centralDirectoryOffset);
    }

    private static void assertByteOrderLittleEndian(ByteBuffer buffer) {
        if (buffer.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("Make sure byteBuffer byte order is little endian");
        }
    }
}
