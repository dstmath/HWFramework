package android.util.apk;

import android.os.Trace;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

abstract class ApkVerityBuilder {
    private static final int CHUNK_SIZE_BYTES = 4096;
    private static final byte[] DEFAULT_SALT = new byte[8];
    private static final int DIGEST_SIZE_BYTES = 32;
    private static final int FSVERITY_HEADER_SIZE_BYTES = 64;
    private static final String JCA_DIGEST_ALGORITHM = "SHA-256";
    private static final int MMAP_REGION_SIZE_BYTES = 1048576;
    private static final int ZIP_EOCD_CENTRAL_DIR_OFFSET_FIELD_OFFSET = 16;
    private static final int ZIP_EOCD_CENTRAL_DIR_OFFSET_FIELD_SIZE = 4;

    static class ApkVerityResult {
        public final ByteBuffer fsverityData;
        public final byte[] rootHash;

        ApkVerityResult(ByteBuffer fsverityData2, byte[] rootHash2) {
            this.fsverityData = fsverityData2;
            this.rootHash = rootHash2;
        }
    }

    private static class BufferedDigester implements DataDigester {
        private static final int BUFFER_SIZE = 4096;
        private int mBytesDigestedSinceReset;
        private final byte[] mDigestBuffer;
        private final MessageDigest mMd;
        private final ByteBuffer mOutput;
        private final byte[] mSalt;

        private BufferedDigester(byte[] salt, ByteBuffer output) throws NoSuchAlgorithmException {
            this.mDigestBuffer = new byte[32];
            this.mSalt = salt;
            this.mOutput = output.slice();
            this.mMd = MessageDigest.getInstance("SHA-256");
            this.mMd.update(this.mSalt);
            this.mBytesDigestedSinceReset = 0;
        }

        public void consume(ByteBuffer buffer) throws DigestException {
            int offset = buffer.position();
            int remaining = buffer.remaining();
            while (remaining > 0) {
                int allowance = Math.min(remaining, 4096 - this.mBytesDigestedSinceReset);
                buffer.limit(buffer.position() + allowance);
                this.mMd.update(buffer);
                offset += allowance;
                remaining -= allowance;
                this.mBytesDigestedSinceReset += allowance;
                if (this.mBytesDigestedSinceReset == 4096) {
                    this.mMd.digest(this.mDigestBuffer, 0, this.mDigestBuffer.length);
                    this.mOutput.put(this.mDigestBuffer);
                    this.mMd.update(this.mSalt);
                    this.mBytesDigestedSinceReset = 0;
                }
            }
        }

        public void assertEmptyBuffer() throws DigestException {
            if (this.mBytesDigestedSinceReset != 0) {
                throw new IllegalStateException("Buffer is not empty: " + this.mBytesDigestedSinceReset);
            }
        }

        /* access modifiers changed from: private */
        public void fillUpLastOutputChunk() {
            int lastBlockSize = this.mOutput.position() % 4096;
            if (lastBlockSize != 0) {
                this.mOutput.put(ByteBuffer.allocate(4096 - lastBlockSize));
            }
        }
    }

    private ApkVerityBuilder() {
    }

    static ApkVerityResult generateApkVerity(RandomAccessFile apk, SignatureInfo signatureInfo, ByteBufferFactory bufferFactory) throws IOException, SecurityException, NoSuchAlgorithmException, DigestException {
        SignatureInfo signatureInfo2 = signatureInfo;
        int[] levelOffset = calculateVerityLevelOffset(apk.length() - (signatureInfo2.centralDirOffset - signatureInfo2.apkSigningBlockOffset));
        int merkleTreeSize = levelOffset[levelOffset.length - 1];
        ByteBuffer output = bufferFactory.create(merkleTreeSize + 4096);
        output.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer tree = slice(output, 0, merkleTreeSize);
        ByteBuffer header = slice(output, merkleTreeSize, merkleTreeSize + 64);
        ByteBuffer extensions = slice(output, merkleTreeSize + 64, merkleTreeSize + 4096);
        byte[] apkDigestBytes = new byte[32];
        ByteBuffer apkDigest = ByteBuffer.wrap(apkDigestBytes);
        apkDigest.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer byteBuffer = apkDigest;
        byte[] apkDigestBytes2 = apkDigestBytes;
        calculateFsveritySignatureInternal(apk, signatureInfo2, tree, apkDigest, header, extensions);
        ByteBuffer extensions2 = extensions;
        output.position(merkleTreeSize + 64 + extensions2.limit());
        output.putInt(64 + extensions2.limit() + 4);
        output.flip();
        return new ApkVerityResult(output, apkDigestBytes2);
    }

    static byte[] generateFsverityRootHash(RandomAccessFile apk, ByteBuffer apkDigest, SignatureInfo signatureInfo) throws NoSuchAlgorithmException, DigestException, IOException {
        ByteBuffer verityBlock = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer header = slice(verityBlock, 0, 64);
        ByteBuffer extensions = slice(verityBlock, 64, 4032);
        calculateFsveritySignatureInternal(apk, signatureInfo, null, null, header, extensions);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(header);
        md.update(extensions);
        md.update(apkDigest);
        return md.digest();
    }

    private static void calculateFsveritySignatureInternal(RandomAccessFile apk, SignatureInfo signatureInfo, ByteBuffer treeOutput, ByteBuffer rootHashOutput, ByteBuffer headerOutput, ByteBuffer extensionsOutput) throws IOException, NoSuchAlgorithmException, DigestException {
        SignatureInfo signatureInfo2 = signatureInfo;
        ByteBuffer byteBuffer = treeOutput;
        ByteBuffer byteBuffer2 = rootHashOutput;
        ByteBuffer byteBuffer3 = headerOutput;
        ByteBuffer byteBuffer4 = extensionsOutput;
        assertSigningBlockAlignedAndHasFullPages(signatureInfo);
        long signingBlockSize = signatureInfo2.centralDirOffset - signatureInfo2.apkSigningBlockOffset;
        int[] levelOffset = calculateVerityLevelOffset(apk.length() - signingBlockSize);
        if (byteBuffer != null) {
            byte[] apkRootHash = generateApkVerityTree(apk, signatureInfo2, DEFAULT_SALT, levelOffset, byteBuffer);
            if (byteBuffer2 != null) {
                byteBuffer2.put(apkRootHash);
                rootHashOutput.flip();
            }
        } else {
            RandomAccessFile randomAccessFile = apk;
        }
        if (byteBuffer3 != null) {
            byteBuffer3.order(ByteOrder.LITTLE_ENDIAN);
            generateFsverityHeader(byteBuffer3, apk.length(), levelOffset.length - 1, DEFAULT_SALT);
        }
        if (byteBuffer4 != null) {
            byteBuffer4.order(ByteOrder.LITTLE_ENDIAN);
            int[] iArr = levelOffset;
            generateFsverityExtensions(byteBuffer4, signatureInfo2.apkSigningBlockOffset, signingBlockSize, signatureInfo2.eocdOffset);
            return;
        }
    }

    private static void consumeByChunk(DataDigester digester, DataSource source, int chunkSize) throws IOException, DigestException {
        long inputRemaining = source.size();
        long inputOffset = 0;
        while (inputRemaining > 0) {
            int size = (int) Math.min(inputRemaining, (long) chunkSize);
            source.feedIntoDataDigester(digester, inputOffset, size);
            inputOffset += (long) size;
            inputRemaining -= (long) size;
        }
    }

    private static void generateApkVerityDigestAtLeafLevel(RandomAccessFile apk, SignatureInfo signatureInfo, byte[] salt, ByteBuffer output) throws IOException, NoSuchAlgorithmException, DigestException {
        SignatureInfo signatureInfo2 = signatureInfo;
        BufferedDigester digester = new BufferedDigester(salt, output);
        MemoryMappedFileDataSource memoryMappedFileDataSource = new MemoryMappedFileDataSource(apk.getFD(), 0, signatureInfo2.apkSigningBlockOffset);
        consumeByChunk(digester, memoryMappedFileDataSource, 1048576);
        long eocdCdOffsetFieldPosition = signatureInfo2.eocdOffset + 16;
        MemoryMappedFileDataSource memoryMappedFileDataSource2 = new MemoryMappedFileDataSource(apk.getFD(), signatureInfo2.centralDirOffset, eocdCdOffsetFieldPosition - signatureInfo2.centralDirOffset);
        consumeByChunk(digester, memoryMappedFileDataSource2, 1048576);
        ByteBuffer alternativeCentralDirOffset = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        alternativeCentralDirOffset.putInt(Math.toIntExact(signatureInfo2.apkSigningBlockOffset));
        alternativeCentralDirOffset.flip();
        digester.consume(alternativeCentralDirOffset);
        long offsetAfterEocdCdOffsetField = 4 + eocdCdOffsetFieldPosition;
        MemoryMappedFileDataSource memoryMappedFileDataSource3 = r10;
        MemoryMappedFileDataSource memoryMappedFileDataSource4 = new MemoryMappedFileDataSource(apk.getFD(), offsetAfterEocdCdOffsetField, apk.length() - offsetAfterEocdCdOffsetField);
        consumeByChunk(digester, memoryMappedFileDataSource3, 1048576);
        int lastIncompleteChunkSize = (int) (apk.length() % Trace.TRACE_TAG_APP);
        if (lastIncompleteChunkSize != 0) {
            digester.consume(ByteBuffer.allocate(4096 - lastIncompleteChunkSize));
        }
        digester.assertEmptyBuffer();
        digester.fillUpLastOutputChunk();
    }

    private static byte[] generateApkVerityTree(RandomAccessFile apk, SignatureInfo signatureInfo, byte[] salt, int[] levelOffset, ByteBuffer output) throws IOException, NoSuchAlgorithmException, DigestException {
        generateApkVerityDigestAtLeafLevel(apk, signatureInfo, salt, slice(output, levelOffset[levelOffset.length - 2], levelOffset[levelOffset.length - 1]));
        for (int level = levelOffset.length - 3; level >= 0; level--) {
            ByteBuffer inputBuffer = slice(output, levelOffset[level + 1], levelOffset[level + 2]);
            ByteBuffer outputBuffer = slice(output, levelOffset[level], levelOffset[level + 1]);
            DataSource source = new ByteBufferDataSource(inputBuffer);
            BufferedDigester digester = new BufferedDigester(salt, outputBuffer);
            consumeByChunk(digester, source, 4096);
            digester.assertEmptyBuffer();
            digester.fillUpLastOutputChunk();
        }
        byte[] rootHash = new byte[32];
        BufferedDigester digester2 = new BufferedDigester(salt, ByteBuffer.wrap(rootHash));
        digester2.consume(slice(output, 0, 4096));
        digester2.assertEmptyBuffer();
        return rootHash;
    }

    private static ByteBuffer generateFsverityHeader(ByteBuffer buffer, long fileSize, int depth, byte[] salt) {
        if (salt.length == 8) {
            buffer.put("TrueBrew".getBytes());
            buffer.put((byte) 1);
            buffer.put((byte) 0);
            buffer.put((byte) 12);
            buffer.put((byte) 7);
            buffer.putShort(1);
            buffer.putShort(1);
            buffer.putInt(0);
            buffer.putInt(0);
            buffer.putLong(fileSize);
            buffer.put((byte) 2);
            buffer.put((byte) 0);
            buffer.put(salt);
            skip(buffer, 22);
            buffer.flip();
            return buffer;
        }
        throw new IllegalArgumentException("salt is not 8 bytes long");
    }

    private static ByteBuffer generateFsverityExtensions(ByteBuffer buffer, long signingBlockOffset, long signingBlockSize, long eocdOffset) {
        buffer.putInt(24);
        buffer.putShort(1);
        skip(buffer, 2);
        buffer.putLong(signingBlockOffset);
        buffer.putLong(signingBlockSize);
        buffer.putInt(20);
        buffer.putShort(2);
        skip(buffer, 2);
        buffer.putLong(16 + eocdOffset);
        buffer.putInt(Math.toIntExact(signingBlockOffset));
        int kPadding = 4;
        if (4 == 8) {
            kPadding = 0;
        }
        skip(buffer, kPadding);
        buffer.flip();
        return buffer;
    }

    private static int[] calculateVerityLevelOffset(long fileSize) {
        ArrayList<Long> levelSize = new ArrayList<>();
        while (true) {
            long levelDigestSize = divideRoundup(fileSize, Trace.TRACE_TAG_APP) * 32;
            levelSize.add(Long.valueOf(divideRoundup(levelDigestSize, Trace.TRACE_TAG_APP) * Trace.TRACE_TAG_APP));
            if (levelDigestSize <= Trace.TRACE_TAG_APP) {
                break;
            }
            fileSize = levelDigestSize;
        }
        int[] levelOffset = new int[(levelSize.size() + 1)];
        levelOffset[0] = 0;
        for (int i = 0; i < levelSize.size(); i++) {
            levelOffset[i + 1] = levelOffset[i] + Math.toIntExact(levelSize.get((levelSize.size() - i) - 1).longValue());
        }
        return levelOffset;
    }

    private static void assertSigningBlockAlignedAndHasFullPages(SignatureInfo signatureInfo) {
        if (signatureInfo.apkSigningBlockOffset % Trace.TRACE_TAG_APP != 0) {
            throw new IllegalArgumentException("APK Signing Block does not start at the page  boundary: " + signatureInfo.apkSigningBlockOffset);
        } else if ((signatureInfo.centralDirOffset - signatureInfo.apkSigningBlockOffset) % Trace.TRACE_TAG_APP != 0) {
            throw new IllegalArgumentException("Size of APK Signing Block is not a multiple of 4096: " + (signatureInfo.centralDirOffset - signatureInfo.apkSigningBlockOffset));
        }
    }

    private static ByteBuffer slice(ByteBuffer buffer, int begin, int end) {
        ByteBuffer b = buffer.duplicate();
        b.position(0);
        b.limit(end);
        b.position(begin);
        return b.slice();
    }

    private static void skip(ByteBuffer buffer, int bytes) {
        buffer.position(buffer.position() + bytes);
    }

    private static long divideRoundup(long dividend, long divisor) {
        return ((dividend + divisor) - 1) / divisor;
    }
}
