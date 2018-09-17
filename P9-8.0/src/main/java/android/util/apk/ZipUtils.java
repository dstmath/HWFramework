package android.util.apk;

import android.util.Pair;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

abstract class ZipUtils {
    private static final int UINT16_MAX_VALUE = 65535;
    private static final int ZIP64_EOCD_LOCATOR_SIG_REVERSE_BYTE_ORDER = 1347094023;
    private static final int ZIP64_EOCD_LOCATOR_SIZE = 20;
    private static final int ZIP_EOCD_CENTRAL_DIR_OFFSET_FIELD_OFFSET = 16;
    private static final int ZIP_EOCD_CENTRAL_DIR_SIZE_FIELD_OFFSET = 12;
    private static final int ZIP_EOCD_COMMENT_LENGTH_FIELD_OFFSET = 20;
    private static final int ZIP_EOCD_REC_MIN_SIZE = 22;
    private static final int ZIP_EOCD_REC_SIG = 101010256;

    private ZipUtils() {
    }

    static Pair<ByteBuffer, Long> findZipEndOfCentralDirectoryRecord(RandomAccessFile zip) throws IOException {
        if (zip.length() < 22) {
            return null;
        }
        Pair<ByteBuffer, Long> result = findZipEndOfCentralDirectoryRecord(zip, 0);
        if (result != null) {
            return result;
        }
        return findZipEndOfCentralDirectoryRecord(zip, 65535);
    }

    private static Pair<ByteBuffer, Long> findZipEndOfCentralDirectoryRecord(RandomAccessFile zip, int maxCommentSize) throws IOException {
        if (maxCommentSize < 0 || maxCommentSize > 65535) {
            throw new IllegalArgumentException("maxCommentSize: " + maxCommentSize);
        }
        long fileSize = zip.length();
        if (fileSize < 22) {
            return null;
        }
        ByteBuffer buf = ByteBuffer.allocate(((int) Math.min((long) maxCommentSize, fileSize - 22)) + 22);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        long bufOffsetInFile = fileSize - ((long) buf.capacity());
        zip.seek(bufOffsetInFile);
        zip.readFully(buf.array(), buf.arrayOffset(), buf.capacity());
        int eocdOffsetInBuf = findZipEndOfCentralDirectoryRecord(buf);
        if (eocdOffsetInBuf == -1) {
            return null;
        }
        buf.position(eocdOffsetInBuf);
        ByteBuffer eocd = buf.slice();
        eocd.order(ByteOrder.LITTLE_ENDIAN);
        return Pair.create(eocd, Long.valueOf(((long) eocdOffsetInBuf) + bufOffsetInFile));
    }

    private static int findZipEndOfCentralDirectoryRecord(ByteBuffer zipContents) {
        assertByteOrderLittleEndian(zipContents);
        int archiveSize = zipContents.capacity();
        if (archiveSize < 22) {
            return -1;
        }
        int maxCommentLength = Math.min(archiveSize - 22, 65535);
        int eocdWithEmptyCommentStartPosition = archiveSize - 22;
        int expectedCommentLength = 0;
        while (expectedCommentLength <= maxCommentLength) {
            int eocdStartPos = eocdWithEmptyCommentStartPosition - expectedCommentLength;
            if (zipContents.getInt(eocdStartPos) == ZIP_EOCD_REC_SIG && getUnsignedInt16(zipContents, eocdStartPos + 20) == expectedCommentLength) {
                return eocdStartPos;
            }
            expectedCommentLength++;
        }
        return -1;
    }

    public static final boolean isZip64EndOfCentralDirectoryLocatorPresent(RandomAccessFile zip, long zipEndOfCentralDirectoryPosition) throws IOException {
        boolean z = false;
        long locatorPosition = zipEndOfCentralDirectoryPosition - 20;
        if (locatorPosition < 0) {
            return false;
        }
        zip.seek(locatorPosition);
        if (zip.readInt() == ZIP64_EOCD_LOCATOR_SIG_REVERSE_BYTE_ORDER) {
            z = true;
        }
        return z;
    }

    public static long getZipEocdCentralDirectoryOffset(ByteBuffer zipEndOfCentralDirectory) {
        assertByteOrderLittleEndian(zipEndOfCentralDirectory);
        return getUnsignedInt32(zipEndOfCentralDirectory, zipEndOfCentralDirectory.position() + 16);
    }

    public static void setZipEocdCentralDirectoryOffset(ByteBuffer zipEndOfCentralDirectory, long offset) {
        assertByteOrderLittleEndian(zipEndOfCentralDirectory);
        setUnsignedInt32(zipEndOfCentralDirectory, zipEndOfCentralDirectory.position() + 16, offset);
    }

    public static long getZipEocdCentralDirectorySizeBytes(ByteBuffer zipEndOfCentralDirectory) {
        assertByteOrderLittleEndian(zipEndOfCentralDirectory);
        return getUnsignedInt32(zipEndOfCentralDirectory, zipEndOfCentralDirectory.position() + 12);
    }

    private static void assertByteOrderLittleEndian(ByteBuffer buffer) {
        if (buffer.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("ByteBuffer byte order must be little endian");
        }
    }

    private static int getUnsignedInt16(ByteBuffer buffer, int offset) {
        return buffer.getShort(offset) & 65535;
    }

    private static long getUnsignedInt32(ByteBuffer buffer, int offset) {
        return ((long) buffer.getInt(offset)) & 4294967295L;
    }

    private static void setUnsignedInt32(ByteBuffer buffer, int offset, long value) {
        if (value < 0 || value > 4294967295L) {
            throw new IllegalArgumentException("uint32 value of out range: " + value);
        }
        buffer.putInt(buffer.position() + offset, (int) value);
    }
}
