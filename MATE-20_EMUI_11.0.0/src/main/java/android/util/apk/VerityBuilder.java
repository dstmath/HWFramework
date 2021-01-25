package android.util.apk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public abstract class VerityBuilder {
    private static final int CHUNK_SIZE_BYTES = 4096;
    private static final byte[] DEFAULT_SALT = new byte[8];
    private static final int DIGEST_SIZE_BYTES = 32;
    private static final int FSVERITY_HEADER_SIZE_BYTES = 64;
    private static final String JCA_DIGEST_ALGORITHM = "SHA-256";
    private static final int MMAP_REGION_SIZE_BYTES = 1048576;
    private static final int ZIP_EOCD_CENTRAL_DIR_OFFSET_FIELD_OFFSET = 16;
    private static final int ZIP_EOCD_CENTRAL_DIR_OFFSET_FIELD_SIZE = 4;

    private VerityBuilder() {
    }

    public static class VerityResult {
        public final int merkleTreeSize;
        public final byte[] rootHash;
        public final ByteBuffer verityData;

        private VerityResult(ByteBuffer verityData2, int merkleTreeSize2, byte[] rootHash2) {
            this.verityData = verityData2;
            this.merkleTreeSize = merkleTreeSize2;
            this.rootHash = rootHash2;
        }
    }

    public static VerityResult generateFsVerityTree(RandomAccessFile apk, ByteBufferFactory bufferFactory) throws IOException, SecurityException, NoSuchAlgorithmException, DigestException {
        return generateVerityTreeInternal(apk, bufferFactory, null, false);
    }

    public static VerityResult generateApkVerityTree(RandomAccessFile apk, SignatureInfo signatureInfo, ByteBufferFactory bufferFactory) throws IOException, SecurityException, NoSuchAlgorithmException, DigestException {
        return generateVerityTreeInternal(apk, bufferFactory, signatureInfo, true);
    }

    private static VerityResult generateVerityTreeInternal(RandomAccessFile apk, ByteBufferFactory bufferFactory, SignatureInfo signatureInfo, boolean skipSigningBlock) throws IOException, SecurityException, NoSuchAlgorithmException, DigestException {
        long dataSize;
        long dataSize2 = apk.length();
        if (skipSigningBlock) {
            dataSize = dataSize2 - (signatureInfo.centralDirOffset - signatureInfo.apkSigningBlockOffset);
        } else {
            dataSize = dataSize2;
        }
        int[] levelOffset = calculateVerityLevelOffset(dataSize);
        int merkleTreeSize = levelOffset[levelOffset.length - 1];
        ByteBuffer output = bufferFactory.create(merkleTreeSize + 4096);
        output.order(ByteOrder.LITTLE_ENDIAN);
        return new VerityResult(output, merkleTreeSize, generateVerityTreeInternal(apk, signatureInfo, skipSigningBlock ? DEFAULT_SALT : null, levelOffset, slice(output, 0, merkleTreeSize), skipSigningBlock));
    }

    static void generateApkVerityFooter(RandomAccessFile apk, SignatureInfo signatureInfo, ByteBuffer footerOutput) throws IOException {
        footerOutput.order(ByteOrder.LITTLE_ENDIAN);
        generateApkVerityHeader(footerOutput, apk.length(), DEFAULT_SALT);
        generateApkVerityExtensions(footerOutput, signatureInfo.apkSigningBlockOffset, signatureInfo.centralDirOffset - signatureInfo.apkSigningBlockOffset, signatureInfo.eocdOffset);
    }

    static byte[] generateApkVerityRootHash(RandomAccessFile apk, ByteBuffer apkDigest, SignatureInfo signatureInfo) throws NoSuchAlgorithmException, DigestException, IOException {
        assertSigningBlockAlignedAndHasFullPages(signatureInfo);
        ByteBuffer footer = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
        generateApkVerityFooter(apk, signatureInfo, footer);
        footer.flip();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(footer);
        md.update(apkDigest);
        return md.digest();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0040, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0041, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0044, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x003b, code lost:
        r2 = move-exception;
     */
    static byte[] generateApkVerity(String apkPath, ByteBufferFactory bufferFactory, SignatureInfo signatureInfo) throws IOException, SignatureNotFoundException, SecurityException, DigestException, NoSuchAlgorithmException {
        RandomAccessFile apk = new RandomAccessFile(apkPath, "r");
        VerityResult result = generateVerityTreeInternal(apk, bufferFactory, signatureInfo, true);
        ByteBuffer footer = slice(result.verityData, result.merkleTreeSize, result.verityData.limit());
        generateApkVerityFooter(apk, signatureInfo, footer);
        footer.putInt(footer.position() + 4);
        result.verityData.limit(result.merkleTreeSize + footer.position());
        byte[] bArr = result.rootHash;
        apk.close();
        return bArr;
    }

    /* access modifiers changed from: private */
    public static class BufferedDigester implements DataDigester {
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
            byte[] bArr = this.mSalt;
            if (bArr != null) {
                this.mMd.update(bArr);
            }
            this.mBytesDigestedSinceReset = 0;
        }

        @Override // android.util.apk.DataDigester
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
                    MessageDigest messageDigest = this.mMd;
                    byte[] bArr = this.mDigestBuffer;
                    messageDigest.digest(bArr, 0, bArr.length);
                    this.mOutput.put(this.mDigestBuffer);
                    byte[] bArr2 = this.mSalt;
                    if (bArr2 != null) {
                        this.mMd.update(bArr2);
                    }
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
        /* access modifiers changed from: public */
        private void fillUpLastOutputChunk() {
            int lastBlockSize = this.mOutput.position() % 4096;
            if (lastBlockSize != 0) {
                this.mOutput.put(ByteBuffer.allocate(4096 - lastBlockSize));
            }
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

    private static void generateFsVerityDigestAtLeafLevel(RandomAccessFile file, ByteBuffer output) throws IOException, NoSuchAlgorithmException, DigestException {
        BufferedDigester digester = new BufferedDigester(null, output);
        consumeByChunk(digester, new MemoryMappedFileDataSource(file.getFD(), 0, file.length()), 1048576);
        int lastIncompleteChunkSize = (int) (file.length() % 4096);
        if (lastIncompleteChunkSize != 0) {
            digester.consume(ByteBuffer.allocate(4096 - lastIncompleteChunkSize));
        }
        digester.assertEmptyBuffer();
        digester.fillUpLastOutputChunk();
    }

    private static void generateApkVerityDigestAtLeafLevel(RandomAccessFile apk, SignatureInfo signatureInfo, byte[] salt, ByteBuffer output) throws IOException, NoSuchAlgorithmException, DigestException {
        BufferedDigester digester = new BufferedDigester(salt, output);
        consumeByChunk(digester, new MemoryMappedFileDataSource(apk.getFD(), 0, signatureInfo.apkSigningBlockOffset), 1048576);
        long eocdCdOffsetFieldPosition = signatureInfo.eocdOffset + 16;
        consumeByChunk(digester, new MemoryMappedFileDataSource(apk.getFD(), signatureInfo.centralDirOffset, eocdCdOffsetFieldPosition - signatureInfo.centralDirOffset), 1048576);
        ByteBuffer alternativeCentralDirOffset = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        alternativeCentralDirOffset.putInt(Math.toIntExact(signatureInfo.apkSigningBlockOffset));
        alternativeCentralDirOffset.flip();
        digester.consume(alternativeCentralDirOffset);
        long offsetAfterEocdCdOffsetField = 4 + eocdCdOffsetFieldPosition;
        consumeByChunk(digester, new MemoryMappedFileDataSource(apk.getFD(), offsetAfterEocdCdOffsetField, apk.length() - offsetAfterEocdCdOffsetField), 1048576);
        int lastIncompleteChunkSize = (int) (apk.length() % 4096);
        if (lastIncompleteChunkSize != 0) {
            digester.consume(ByteBuffer.allocate(4096 - lastIncompleteChunkSize));
        }
        digester.assertEmptyBuffer();
        digester.fillUpLastOutputChunk();
    }

    private static byte[] generateVerityTreeInternal(RandomAccessFile apk, SignatureInfo signatureInfo, byte[] salt, int[] levelOffset, ByteBuffer output, boolean skipSigningBlock) throws IOException, NoSuchAlgorithmException, DigestException {
        if (skipSigningBlock) {
            assertSigningBlockAlignedAndHasFullPages(signatureInfo);
            generateApkVerityDigestAtLeafLevel(apk, signatureInfo, salt, slice(output, levelOffset[levelOffset.length - 2], levelOffset[levelOffset.length - 1]));
        } else {
            generateFsVerityDigestAtLeafLevel(apk, slice(output, levelOffset[levelOffset.length - 2], levelOffset[levelOffset.length - 1]));
        }
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

    private static ByteBuffer generateApkVerityHeader(ByteBuffer buffer, long fileSize, byte[] salt) {
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
            return buffer;
        }
        throw new IllegalArgumentException("salt is not 8 bytes long");
    }

    private static ByteBuffer generateApkVerityExtensions(ByteBuffer buffer, long signingBlockOffset, long signingBlockSize, long eocdOffset) {
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
        return buffer;
    }

    private static int[] calculateVerityLevelOffset(long fileSize) {
        ArrayList<Long> levelSize = new ArrayList<>();
        while (true) {
            long levelDigestSize = divideRoundup(fileSize, 4096) * 32;
            levelSize.add(Long.valueOf(divideRoundup(levelDigestSize, 4096) * 4096));
            if (levelDigestSize <= 4096) {
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
        if (signatureInfo.apkSigningBlockOffset % 4096 != 0) {
            throw new IllegalArgumentException("APK Signing Block does not start at the page boundary: " + signatureInfo.apkSigningBlockOffset);
        } else if ((signatureInfo.centralDirOffset - signatureInfo.apkSigningBlockOffset) % 4096 != 0) {
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
