package android.util.apk;

import android.security.keystore.KeyProperties;
import android.util.ArrayMap;
import android.util.Pair;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Arrays;
import java.util.Map;

/* access modifiers changed from: package-private */
public final class ApkSigningBlockUtils {
    private static final long APK_SIG_BLOCK_MAGIC_HI = 3617552046287187010L;
    private static final long APK_SIG_BLOCK_MAGIC_LO = 2334950737559900225L;
    private static final int APK_SIG_BLOCK_MIN_SIZE = 32;
    private static final int CHUNK_SIZE_BYTES = 1048576;
    static final int CONTENT_DIGEST_CHUNKED_SHA256 = 1;
    static final int CONTENT_DIGEST_CHUNKED_SHA512 = 2;
    static final int CONTENT_DIGEST_VERITY_CHUNKED_SHA256 = 3;
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

    private ApkSigningBlockUtils() {
    }

    static SignatureInfo findSignature(RandomAccessFile apk, int blockId) throws IOException, SignatureNotFoundException {
        Pair<ByteBuffer, Long> eocdAndOffsetInFile = getEocd(apk);
        ByteBuffer eocd = eocdAndOffsetInFile.first;
        long eocdOffset = eocdAndOffsetInFile.second.longValue();
        if (!ZipUtils.isZip64EndOfCentralDirectoryLocatorPresent(apk, eocdOffset)) {
            long centralDirOffset = getCentralDirOffset(eocd, eocdOffset);
            Pair<ByteBuffer, Long> apkSigningBlockAndOffsetInFile = findApkSigningBlock(apk, centralDirOffset);
            ByteBuffer apkSigningBlock = apkSigningBlockAndOffsetInFile.first;
            return new SignatureInfo(findApkSignatureSchemeBlock(apkSigningBlock, blockId), apkSigningBlockAndOffsetInFile.second.longValue(), centralDirOffset, eocdOffset, eocd);
        }
        throw new SignatureNotFoundException("ZIP64 APK not supported");
    }

    static void verifyIntegrity(Map<Integer, byte[]> expectedDigests, RandomAccessFile apk, SignatureInfo signatureInfo) throws SecurityException {
        if (!expectedDigests.isEmpty()) {
            boolean neverVerified = true;
            Map<Integer, byte[]> expected1MbChunkDigests = new ArrayMap<>();
            if (expectedDigests.containsKey(1)) {
                expected1MbChunkDigests.put(1, expectedDigests.get(1));
            }
            if (expectedDigests.containsKey(2)) {
                expected1MbChunkDigests.put(2, expectedDigests.get(2));
            }
            if (!expected1MbChunkDigests.isEmpty()) {
                try {
                    verifyIntegrityFor1MbChunkBasedAlgorithm(expected1MbChunkDigests, apk.getFD(), signatureInfo);
                    neverVerified = false;
                } catch (IOException e) {
                    throw new SecurityException("Cannot get FD", e);
                }
            }
            if (expectedDigests.containsKey(3)) {
                verifyIntegrityForVerityBasedAlgorithm(expectedDigests.get(3), apk, signatureInfo);
                neverVerified = false;
            }
            if (neverVerified) {
                throw new SecurityException("No known digest exists for integrity check");
            }
            return;
        }
        throw new SecurityException("No digests provided");
    }

    private static void verifyIntegrityFor1MbChunkBasedAlgorithm(Map<Integer, byte[]> expectedDigests, FileDescriptor apkFileDescriptor, SignatureInfo signatureInfo) throws SecurityException {
        DataSource beforeApkSigningBlock = new MemoryMappedFileDataSource(apkFileDescriptor, 0, signatureInfo.apkSigningBlockOffset);
        DataSource centralDir = new MemoryMappedFileDataSource(apkFileDescriptor, signatureInfo.centralDirOffset, signatureInfo.eocdOffset - signatureInfo.centralDirOffset);
        ByteBuffer eocdBuf = signatureInfo.eocd.duplicate();
        eocdBuf.order(ByteOrder.LITTLE_ENDIAN);
        ZipUtils.setZipEocdCentralDirectoryOffset(eocdBuf, signatureInfo.apkSigningBlockOffset);
        DataSource eocd = new ByteBufferDataSource(eocdBuf);
        int[] digestAlgorithms = new int[expectedDigests.size()];
        int digestAlgorithmCount = 0;
        for (Integer num : expectedDigests.keySet()) {
            digestAlgorithms[digestAlgorithmCount] = num.intValue();
            digestAlgorithmCount++;
        }
        try {
            byte[][] actualDigests = computeContentDigestsPer1MbChunk(digestAlgorithms, new DataSource[]{beforeApkSigningBlock, centralDir, eocd});
            for (int i = 0; i < digestAlgorithms.length; i++) {
                int digestAlgorithm = digestAlgorithms[i];
                if (!MessageDigest.isEqual(expectedDigests.get(Integer.valueOf(digestAlgorithm)), actualDigests[i])) {
                    throw new SecurityException(getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm) + " digest of contents did not verify");
                }
            }
        } catch (DigestException e) {
            throw new SecurityException("Failed to compute digest(s) of contents", e);
        }
    }

    /* JADX INFO: Multiple debug info for r8v12 byte[]: [D('chunkContentPrefix' byte[]), D('concatenationOfChunkCountAndChunkDigests' byte[])] */
    /* JADX INFO: Multiple debug info for r7v7 java.security.MessageDigest: [D('digestAlgorithm' int), D('md' java.security.MessageDigest)] */
    private static byte[][] computeContentDigestsPer1MbChunk(int[] digestAlgorithms, DataSource[] contents) throws DigestException {
        int[] iArr = digestAlgorithms;
        DataSource[] dataSourceArr = contents;
        int i = 0;
        long totalChunkCountLong = 0;
        for (DataSource input : dataSourceArr) {
            totalChunkCountLong += getChunkCount(input.size());
        }
        if (totalChunkCountLong < 2097151) {
            int totalChunkCount = (int) totalChunkCountLong;
            byte[][] digestsOfChunks = new byte[iArr.length][];
            for (int i2 = 0; i2 < iArr.length; i2++) {
                byte[] concatenationOfChunkCountAndChunkDigests = new byte[((totalChunkCount * getContentDigestAlgorithmOutputSizeBytes(iArr[i2])) + 5)];
                concatenationOfChunkCountAndChunkDigests[0] = 90;
                setUnsignedInt32LittleEndian(totalChunkCount, concatenationOfChunkCountAndChunkDigests, 1);
                digestsOfChunks[i2] = concatenationOfChunkCountAndChunkDigests;
            }
            byte[] chunkContentPrefix = new byte[5];
            chunkContentPrefix[0] = -91;
            int chunkIndex = 0;
            MessageDigest[] mds = new MessageDigest[iArr.length];
            for (int i3 = 0; i3 < iArr.length; i3++) {
                String jcaAlgorithmName = getContentDigestAlgorithmJcaDigestAlgorithm(iArr[i3]);
                try {
                    mds[i3] = MessageDigest.getInstance(jcaAlgorithmName);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(jcaAlgorithmName + " digest not supported", e);
                }
            }
            DataDigester digester = new MultipleDigestDataDigester(mds);
            int chunkIndex2 = dataSourceArr.length;
            int dataSourceIndex = 0;
            while (i < chunkIndex2) {
                DataSource input2 = dataSourceArr[i];
                long inputOffset = 0;
                int chunkIndex3 = chunkIndex;
                long inputRemaining = input2.size();
                while (inputRemaining > 0) {
                    int chunkSize = (int) Math.min(inputRemaining, 1048576L);
                    setUnsignedInt32LittleEndian(chunkSize, chunkContentPrefix, 1);
                    int i4 = 0;
                    while (i4 < mds.length) {
                        mds[i4].update(chunkContentPrefix);
                        i4++;
                        totalChunkCountLong = totalChunkCountLong;
                    }
                    try {
                        input2.feedIntoDataDigester(digester, inputOffset, chunkSize);
                        int i5 = 0;
                        while (i5 < digestAlgorithms.length) {
                            int digestAlgorithm = digestAlgorithms[i5];
                            byte[] concatenationOfChunkCountAndChunkDigests2 = digestsOfChunks[i5];
                            int expectedDigestSizeBytes = getContentDigestAlgorithmOutputSizeBytes(digestAlgorithm);
                            MessageDigest md = mds[i5];
                            int actualDigestSizeBytes = md.digest(concatenationOfChunkCountAndChunkDigests2, (chunkIndex3 * expectedDigestSizeBytes) + 5, expectedDigestSizeBytes);
                            if (actualDigestSizeBytes == expectedDigestSizeBytes) {
                                i5++;
                                chunkContentPrefix = chunkContentPrefix;
                                input2 = input2;
                                mds = mds;
                            } else {
                                throw new RuntimeException("Unexpected output size of " + md.getAlgorithm() + " digest: " + actualDigestSizeBytes);
                            }
                        }
                        inputOffset += (long) chunkSize;
                        inputRemaining -= (long) chunkSize;
                        chunkIndex3++;
                        iArr = digestAlgorithms;
                        chunkContentPrefix = chunkContentPrefix;
                        totalChunkCountLong = totalChunkCountLong;
                    } catch (IOException e2) {
                        throw new DigestException("Failed to digest chunk #" + chunkIndex3 + " of section #" + dataSourceIndex, e2);
                    }
                }
                dataSourceIndex++;
                i++;
                dataSourceArr = contents;
                chunkIndex = chunkIndex3;
                totalChunkCount = totalChunkCount;
                chunkIndex2 = chunkIndex2;
                chunkContentPrefix = chunkContentPrefix;
                totalChunkCountLong = totalChunkCountLong;
            }
            byte[][] result = new byte[iArr.length][];
            for (int i6 = 0; i6 < iArr.length; i6++) {
                int digestAlgorithm2 = iArr[i6];
                byte[] input3 = digestsOfChunks[i6];
                String jcaAlgorithmName2 = getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm2);
                try {
                    result[i6] = MessageDigest.getInstance(jcaAlgorithmName2).digest(input3);
                } catch (NoSuchAlgorithmException e3) {
                    throw new RuntimeException(jcaAlgorithmName2 + " digest not supported", e3);
                }
            }
            return result;
        }
        throw new DigestException("Too many chunks: " + totalChunkCountLong);
    }

    static byte[] parseVerityDigestAndVerifySourceLength(byte[] data, long fileSize, SignatureInfo signatureInfo) throws SecurityException {
        if (data.length == 32 + 8) {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(32);
            if (buffer.getLong() == fileSize - (signatureInfo.centralDirOffset - signatureInfo.apkSigningBlockOffset)) {
                return Arrays.copyOfRange(data, 0, 32);
            }
            throw new SecurityException("APK content size did not verify");
        }
        throw new SecurityException("Verity digest size is wrong: " + data.length);
    }

    private static void verifyIntegrityForVerityBasedAlgorithm(byte[] expectedDigest, RandomAccessFile apk, SignatureInfo signatureInfo) throws SecurityException {
        try {
            if (!Arrays.equals(parseVerityDigestAndVerifySourceLength(expectedDigest, apk.length(), signatureInfo), VerityBuilder.generateApkVerityTree(apk, signatureInfo, new ByteBufferFactory() {
                /* class android.util.apk.ApkSigningBlockUtils.AnonymousClass1 */

                @Override // android.util.apk.ByteBufferFactory
                public ByteBuffer create(int capacity) {
                    return ByteBuffer.allocate(capacity);
                }
            }).rootHash)) {
                throw new SecurityException("APK verity digest of contents did not verify");
            }
        } catch (IOException | DigestException | NoSuchAlgorithmException e) {
            throw new SecurityException("Error during verification", e);
        }
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

    private static long getChunkCount(long inputSizeBytes) {
        return ((inputSizeBytes + 1048576) - 1) / 1048576;
    }

    static int compareSignatureAlgorithm(int sigAlgorithm1, int sigAlgorithm2) {
        return compareContentDigestAlgorithm(getSignatureAlgorithmContentDigestAlgorithm(sigAlgorithm1), getSignatureAlgorithmContentDigestAlgorithm(sigAlgorithm2));
    }

    private static int compareContentDigestAlgorithm(int digestAlgorithm1, int digestAlgorithm2) {
        if (digestAlgorithm1 != 1) {
            if (digestAlgorithm1 == 2) {
                if (digestAlgorithm2 != 1) {
                    if (digestAlgorithm2 == 2) {
                        return 0;
                    }
                    if (digestAlgorithm2 != 3) {
                        throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
                    }
                }
                return 1;
            } else if (digestAlgorithm1 != 3) {
                throw new IllegalArgumentException("Unknown digestAlgorithm1: " + digestAlgorithm1);
            } else if (digestAlgorithm2 == 1) {
                return 1;
            } else {
                if (digestAlgorithm2 == 2) {
                    return -1;
                }
                if (digestAlgorithm2 == 3) {
                    return 0;
                }
                throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
            }
        } else if (digestAlgorithm2 == 1) {
            return 0;
        } else {
            if (digestAlgorithm2 == 2 || digestAlgorithm2 == 3) {
                return -1;
            }
            throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
        }
    }

    static int getSignatureAlgorithmContentDigestAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm == 513) {
            return 1;
        }
        if (sigAlgorithm == 514) {
            return 2;
        }
        if (sigAlgorithm == 769) {
            return 1;
        }
        if (sigAlgorithm == 1057 || sigAlgorithm == 1059 || sigAlgorithm == 1061) {
            return 3;
        }
        switch (sigAlgorithm) {
            case 257:
            case 259:
                return 1;
            case 258:
            case 260:
                return 2;
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
        }
    }

    static String getContentDigestAlgorithmJcaDigestAlgorithm(int digestAlgorithm) {
        if (digestAlgorithm == 1) {
            return KeyProperties.DIGEST_SHA256;
        }
        if (digestAlgorithm == 2) {
            return KeyProperties.DIGEST_SHA512;
        }
        if (digestAlgorithm == 3) {
            return KeyProperties.DIGEST_SHA256;
        }
        throw new IllegalArgumentException("Unknown content digest algorthm: " + digestAlgorithm);
    }

    private static int getContentDigestAlgorithmOutputSizeBytes(int digestAlgorithm) {
        if (digestAlgorithm == 1) {
            return 32;
        }
        if (digestAlgorithm == 2) {
            return 64;
        }
        if (digestAlgorithm == 3) {
            return 32;
        }
        throw new IllegalArgumentException("Unknown content digest algorthm: " + digestAlgorithm);
    }

    static String getSignatureAlgorithmJcaKeyAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm == 513 || sigAlgorithm == 514) {
            return KeyProperties.KEY_ALGORITHM_EC;
        }
        if (sigAlgorithm == 769) {
            return "DSA";
        }
        if (sigAlgorithm == 1057) {
            return KeyProperties.KEY_ALGORITHM_RSA;
        }
        if (sigAlgorithm == 1059) {
            return KeyProperties.KEY_ALGORITHM_EC;
        }
        if (sigAlgorithm == 1061) {
            return "DSA";
        }
        switch (sigAlgorithm) {
            case 257:
            case 258:
            case 259:
            case 260:
                return KeyProperties.KEY_ALGORITHM_RSA;
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
        }
    }

    static Pair<String, ? extends AlgorithmParameterSpec> getSignatureAlgorithmJcaSignatureAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm != 513) {
            if (sigAlgorithm == 514) {
                return Pair.create("SHA512withECDSA", null);
            }
            if (sigAlgorithm != 769) {
                if (sigAlgorithm != 1057) {
                    if (sigAlgorithm != 1059) {
                        if (sigAlgorithm != 1061) {
                            switch (sigAlgorithm) {
                                case 257:
                                    return Pair.create("SHA256withRSA/PSS", new PSSParameterSpec(KeyProperties.DIGEST_SHA256, "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
                                case 258:
                                    return Pair.create("SHA512withRSA/PSS", new PSSParameterSpec(KeyProperties.DIGEST_SHA512, "MGF1", MGF1ParameterSpec.SHA512, 64, 1));
                                case 259:
                                    break;
                                case 260:
                                    return Pair.create("SHA512withRSA", null);
                                default:
                                    throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
                            }
                        }
                    }
                }
                return Pair.create("SHA256withRSA", null);
            }
            return Pair.create("SHA256withDSA", null);
        }
        return Pair.create("SHA256withECDSA", null);
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

    static void setUnsignedInt32LittleEndian(int value, byte[] result, int offset) {
        result[offset] = (byte) (value & 255);
        result[offset + 1] = (byte) ((value >>> 8) & 255);
        result[offset + 2] = (byte) ((value >>> 16) & 255);
        result[offset + 3] = (byte) ((value >>> 24) & 255);
    }

    static Pair<ByteBuffer, Long> findApkSigningBlock(RandomAccessFile apk, long centralDirOffset) throws IOException, SignatureNotFoundException {
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

    /* access modifiers changed from: private */
    public static class MultipleDigestDataDigester implements DataDigester {
        private final MessageDigest[] mMds;

        MultipleDigestDataDigester(MessageDigest[] mds) {
            this.mMds = mds;
        }

        @Override // android.util.apk.DataDigester
        public void consume(ByteBuffer buffer) {
            ByteBuffer buffer2 = buffer.slice();
            MessageDigest[] messageDigestArr = this.mMds;
            for (MessageDigest md : messageDigestArr) {
                buffer2.position(0);
                md.update(buffer2);
            }
        }
    }
}
