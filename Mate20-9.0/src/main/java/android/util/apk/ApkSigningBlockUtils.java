package android.util.apk;

import android.os.Trace;
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

final class ApkSigningBlockUtils {
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

    private static class MultipleDigestDataDigester implements DataDigester {
        private final MessageDigest[] mMds;

        MultipleDigestDataDigester(MessageDigest[] mds) {
            this.mMds = mds;
        }

        public void consume(ByteBuffer buffer) {
            ByteBuffer buffer2 = buffer.slice();
            for (MessageDigest md : this.mMds) {
                buffer2.position(0);
                md.update(buffer2);
            }
        }
    }

    private ApkSigningBlockUtils() {
    }

    static SignatureInfo findSignature(RandomAccessFile apk, int blockId) throws IOException, SignatureNotFoundException {
        RandomAccessFile randomAccessFile = apk;
        Pair<ByteBuffer, Long> eocdAndOffsetInFile = getEocd(apk);
        ByteBuffer eocd = (ByteBuffer) eocdAndOffsetInFile.first;
        long eocdOffset = ((Long) eocdAndOffsetInFile.second).longValue();
        if (!ZipUtils.isZip64EndOfCentralDirectoryLocatorPresent(randomAccessFile, eocdOffset)) {
            long centralDirOffset = getCentralDirOffset(eocd, eocdOffset);
            Pair<ByteBuffer, Long> apkSigningBlockAndOffsetInFile = findApkSigningBlock(randomAccessFile, centralDirOffset);
            ByteBuffer apkSigningBlock = (ByteBuffer) apkSigningBlockAndOffsetInFile.first;
            ByteBuffer byteBuffer = apkSigningBlock;
            Pair<ByteBuffer, Long> pair = apkSigningBlockAndOffsetInFile;
            SignatureInfo signatureInfo = new SignatureInfo(findApkSignatureSchemeBlock(apkSigningBlock, blockId), ((Long) apkSigningBlockAndOffsetInFile.second).longValue(), centralDirOffset, eocdOffset, eocd);
            return signatureInfo;
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
        SignatureInfo signatureInfo2 = signatureInfo;
        MemoryMappedFileDataSource memoryMappedFileDataSource = new MemoryMappedFileDataSource(apkFileDescriptor, 0, signatureInfo2.apkSigningBlockOffset);
        MemoryMappedFileDataSource memoryMappedFileDataSource2 = new MemoryMappedFileDataSource(apkFileDescriptor, signatureInfo2.centralDirOffset, signatureInfo2.eocdOffset - signatureInfo2.centralDirOffset);
        ByteBuffer eocdBuf = signatureInfo2.eocd.duplicate();
        eocdBuf.order(ByteOrder.LITTLE_ENDIAN);
        ZipUtils.setZipEocdCentralDirectoryOffset(eocdBuf, signatureInfo2.apkSigningBlockOffset);
        DataSource eocd = new ByteBufferDataSource(eocdBuf);
        int[] digestAlgorithms = new int[expectedDigests.size()];
        int digestAlgorithmCount = 0;
        for (Integer intValue : expectedDigests.keySet()) {
            digestAlgorithms[digestAlgorithmCount] = intValue.intValue();
            digestAlgorithmCount++;
        }
        try {
            int i = 0;
            byte[][] actualDigests = computeContentDigestsPer1MbChunk(digestAlgorithms, new DataSource[]{memoryMappedFileDataSource, memoryMappedFileDataSource2, eocd});
            while (i < digestAlgorithms.length) {
                if (MessageDigest.isEqual(expectedDigests.get(Integer.valueOf(digestAlgorithms[i])), actualDigests[i])) {
                    i++;
                } else {
                    throw new SecurityException(getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm) + " digest of contents did not verify");
                }
            }
            Map<Integer, byte[]> map = expectedDigests;
        } catch (DigestException e) {
            Map<Integer, byte[]> map2 = expectedDigests;
            throw new SecurityException("Failed to compute digest(s) of contents", e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:0x016a, code lost:
        r25 = r3;
        r30 = r5;
        r28 = r6;
        r31 = r8;
        r33 = r12;
        r32 = r13;
        r27 = r14;
        r6 = r21;
        r15 = r15 + 1;
        r0 = r0 + 1;
        r6 = r28;
        r2 = r37;
     */
    private static byte[][] computeContentDigestsPer1MbChunk(int[] digestAlgorithms, DataSource[] contents) throws DigestException {
        int i;
        int[] iArr = digestAlgorithms;
        DataSource[] dataSourceArr = contents;
        long totalChunkCountLong = 0;
        for (DataSource input : dataSourceArr) {
            totalChunkCountLong += getChunkCount(input.size());
        }
        if (totalChunkCountLong < 2097151) {
            int totalChunkCount = (int) totalChunkCountLong;
            byte[][] digestsOfChunks = new byte[iArr.length][];
            for (int i2 = 0; i2 < iArr.length; i2++) {
                byte[] concatenationOfChunkCountAndChunkDigests = new byte[(5 + (totalChunkCount * getContentDigestAlgorithmOutputSizeBytes(iArr[i2])))];
                concatenationOfChunkCountAndChunkDigests[0] = 90;
                setUnsignedInt32LittleEndian(totalChunkCount, concatenationOfChunkCountAndChunkDigests, 1);
                digestsOfChunks[i2] = concatenationOfChunkCountAndChunkDigests;
            }
            byte[] chunkContentPrefix = new byte[5];
            chunkContentPrefix[0] = -91;
            int chunkIndex = 0;
            MessageDigest[] mds = new MessageDigest[iArr.length];
            int i3 = 0;
            while (true) {
                int i4 = i3;
                if (i4 >= iArr.length) {
                    break;
                }
                try {
                    mds[i4] = MessageDigest.getInstance(getContentDigestAlgorithmJcaDigestAlgorithm(iArr[i4]));
                    i3 = i4 + 1;
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(jcaAlgorithmName + " digest not supported", e);
                }
            }
            DataDigester digester = new MultipleDigestDataDigester(mds);
            int i5 = dataSourceArr.length;
            int dataSourceIndex = 0;
            int dataSourceIndex2 = 0;
            while (dataSourceIndex2 < i5) {
                DataSource input2 = dataSourceArr[dataSourceIndex2];
                long inputRemaining = input2.size();
                long inputOffset = 0;
                while (true) {
                    long inputRemaining2 = inputRemaining;
                    if (inputRemaining2 <= 0) {
                        break;
                    }
                    int totalChunkCount2 = totalChunkCount;
                    int chunkSize = (int) Math.min(inputRemaining2, Trace.TRACE_TAG_DATABASE);
                    setUnsignedInt32LittleEndian(chunkSize, chunkContentPrefix, 1);
                    int i6 = 0;
                    while (true) {
                        i = i5;
                        int i7 = i6;
                        if (i7 >= mds.length) {
                            break;
                        }
                        mds[i7].update(chunkContentPrefix);
                        i6 = i7 + 1;
                        i5 = i;
                    }
                    long totalChunkCountLong2 = totalChunkCountLong;
                    long inputOffset2 = inputOffset;
                    try {
                        input2.feedIntoDataDigester(digester, inputOffset2, chunkSize);
                        int i8 = 0;
                        while (i8 < iArr.length) {
                            int digestAlgorithm = iArr[i8];
                            DataSource input3 = input2;
                            byte[] concatenationOfChunkCountAndChunkDigests2 = digestsOfChunks[i8];
                            byte[] chunkContentPrefix2 = chunkContentPrefix;
                            int expectedDigestSizeBytes = getContentDigestAlgorithmOutputSizeBytes(digestAlgorithm);
                            DataDigester digester2 = digester;
                            MessageDigest md = mds[i8];
                            MessageDigest[] mds2 = mds;
                            int actualDigestSizeBytes = md.digest(concatenationOfChunkCountAndChunkDigests2, 5 + (chunkIndex * expectedDigestSizeBytes), expectedDigestSizeBytes);
                            if (actualDigestSizeBytes == expectedDigestSizeBytes) {
                                i8++;
                                input2 = input3;
                                chunkContentPrefix = chunkContentPrefix2;
                                digester = digester2;
                                mds = mds2;
                            } else {
                                int i9 = i8;
                                StringBuilder sb = new StringBuilder();
                                byte[] bArr = concatenationOfChunkCountAndChunkDigests2;
                                sb.append("Unexpected output size of ");
                                sb.append(md.getAlgorithm());
                                sb.append(" digest: ");
                                sb.append(actualDigestSizeBytes);
                                throw new RuntimeException(sb.toString());
                            }
                        }
                        byte[] bArr2 = chunkContentPrefix;
                        inputOffset = inputOffset2 + ((long) chunkSize);
                        inputRemaining = inputRemaining2 - ((long) chunkSize);
                        chunkIndex++;
                        totalChunkCount = totalChunkCount2;
                        i5 = i;
                        totalChunkCountLong = totalChunkCountLong2;
                        input2 = input2;
                        digester = digester;
                        mds = mds;
                        DataSource[] dataSourceArr2 = contents;
                    } catch (IOException e2) {
                        DataSource dataSource = input2;
                        byte[] bArr3 = chunkContentPrefix;
                        MessageDigest[] messageDigestArr = mds;
                        DataDigester dataDigester = digester;
                        IOException iOException = e2;
                        throw new DigestException("Failed to digest chunk #" + chunkIndex + " of section #" + dataSourceIndex, e2);
                    }
                }
            }
            long j = totalChunkCountLong;
            byte[] bArr4 = chunkContentPrefix;
            MessageDigest[] messageDigestArr2 = mds;
            DataDigester dataDigester2 = digester;
            byte[][] result = new byte[iArr.length][];
            int i10 = 0;
            while (true) {
                int i11 = i10;
                if (i11 >= iArr.length) {
                    return result;
                }
                try {
                    result[i11] = MessageDigest.getInstance(getContentDigestAlgorithmJcaDigestAlgorithm(iArr[i11])).digest(digestsOfChunks[i11]);
                    i10 = i11 + 1;
                } catch (NoSuchAlgorithmException e3) {
                    NoSuchAlgorithmException noSuchAlgorithmException = e3;
                    throw new RuntimeException(jcaAlgorithmName + " digest not supported", e3);
                }
            }
        } else {
            throw new DigestException("Too many chunks: " + totalChunkCountLong);
        }
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
            if (!Arrays.equals(parseVerityDigestAndVerifySourceLength(expectedDigest, apk.length(), signatureInfo), ApkVerityBuilder.generateApkVerity(apk, signatureInfo, new ByteBufferFactory() {
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0022, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0013, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0017, code lost:
        if (r1 != null) goto L_0x0019;
     */
    public static byte[] generateApkVerity(String apkPath, ByteBufferFactory bufferFactory, SignatureInfo signatureInfo) throws IOException, SignatureNotFoundException, SecurityException, DigestException, NoSuchAlgorithmException {
        RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
        byte[] bArr = ApkVerityBuilder.generateApkVerity(apk, signatureInfo, bufferFactory).rootHash;
        apk.close();
        return bArr;
        throw th;
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
        return ((inputSizeBytes + Trace.TRACE_TAG_DATABASE) - 1) / Trace.TRACE_TAG_DATABASE;
    }

    static int compareSignatureAlgorithm(int sigAlgorithm1, int sigAlgorithm2) {
        return compareContentDigestAlgorithm(getSignatureAlgorithmContentDigestAlgorithm(sigAlgorithm1), getSignatureAlgorithmContentDigestAlgorithm(sigAlgorithm2));
    }

    private static int compareContentDigestAlgorithm(int digestAlgorithm1, int digestAlgorithm2) {
        switch (digestAlgorithm1) {
            case 1:
                switch (digestAlgorithm2) {
                    case 1:
                        return 0;
                    case 2:
                    case 3:
                        return -1;
                    default:
                        throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
                }
            case 2:
                switch (digestAlgorithm2) {
                    case 1:
                    case 3:
                        return 1;
                    case 2:
                        return 0;
                    default:
                        throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
                }
            case 3:
                switch (digestAlgorithm2) {
                    case 1:
                        return 1;
                    case 2:
                        return -1;
                    case 3:
                        return 0;
                    default:
                        throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
                }
            default:
                throw new IllegalArgumentException("Unknown digestAlgorithm1: " + digestAlgorithm1);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0035, code lost:
        return 2;
     */
    static int getSignatureAlgorithmContentDigestAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm != 769) {
            if (sigAlgorithm == SIGNATURE_VERITY_RSA_PKCS1_V1_5_WITH_SHA256 || sigAlgorithm == SIGNATURE_VERITY_ECDSA_WITH_SHA256 || sigAlgorithm == 1061) {
                return 3;
            }
            switch (sigAlgorithm) {
                case 257:
                case 259:
                    break;
                case 258:
                case 260:
                    break;
                default:
                    switch (sigAlgorithm) {
                        case 513:
                            break;
                        case 514:
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
                    }
            }
        }
        return 1;
    }

    static String getContentDigestAlgorithmJcaDigestAlgorithm(int digestAlgorithm) {
        switch (digestAlgorithm) {
            case 1:
            case 3:
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
            case 3:
                return 32;
            case 2:
                return 64;
            default:
                throw new IllegalArgumentException("Unknown content digest algorthm: " + digestAlgorithm);
        }
    }

    static String getSignatureAlgorithmJcaKeyAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm != 769) {
            if (sigAlgorithm != SIGNATURE_VERITY_RSA_PKCS1_V1_5_WITH_SHA256) {
                if (sigAlgorithm != SIGNATURE_VERITY_ECDSA_WITH_SHA256) {
                    if (sigAlgorithm != 1061) {
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
                                        throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
                                }
                        }
                    }
                }
                return KeyProperties.KEY_ALGORITHM_EC;
            }
            return KeyProperties.KEY_ALGORITHM_RSA;
        }
        return "DSA";
    }

    static Pair<String, ? extends AlgorithmParameterSpec> getSignatureAlgorithmJcaSignatureAlgorithm(int sigAlgorithm) {
        if (sigAlgorithm != 769) {
            if (sigAlgorithm != SIGNATURE_VERITY_RSA_PKCS1_V1_5_WITH_SHA256) {
                if (sigAlgorithm != SIGNATURE_VERITY_ECDSA_WITH_SHA256) {
                    if (sigAlgorithm != 1061) {
                        switch (sigAlgorithm) {
                            case 257:
                                PSSParameterSpec pSSParameterSpec = new PSSParameterSpec(KeyProperties.DIGEST_SHA256, "MGF1", MGF1ParameterSpec.SHA256, 32, 1);
                                return Pair.create("SHA256withRSA/PSS", pSSParameterSpec);
                            case 258:
                                PSSParameterSpec pSSParameterSpec2 = new PSSParameterSpec(KeyProperties.DIGEST_SHA512, "MGF1", MGF1ParameterSpec.SHA512, 64, 1);
                                return Pair.create("SHA512withRSA/PSS", pSSParameterSpec2);
                            case 259:
                                break;
                            case 260:
                                return Pair.create("SHA512withRSA", null);
                            default:
                                switch (sigAlgorithm) {
                                    case 513:
                                        break;
                                    case 514:
                                        return Pair.create("SHA512withECDSA", null);
                                    default:
                                        throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
                                }
                        }
                    }
                }
                return Pair.create("SHA256withECDSA", null);
            }
            return Pair.create("SHA256withRSA", null);
        }
        return Pair.create("SHA256withDSA", null);
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
}
