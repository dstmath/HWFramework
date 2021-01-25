package huawei.android.security.securityprofile;

import android.util.Pair;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

abstract class ZipHelper {
    private static final int EOCD_CENTRAL_DIR_LENGTH_FIELD_OFFSET_ZIP = 12;
    private static final int EOCD_CENTRAL_DIR_OFFSET_FIELD_OFFSET_ZIP = 16;
    private static final int EOCD_COMMENT_LENGTH_FIELD_OFFSET_ZIP = 20;
    private static final int EOCD_LOCATOR_LENGTH_ZIP64 = 20;
    private static final int EOCD_LOCATOR_SIG_REVERSE_BYTE_ORDER_ZIP64 = 1347094023;
    private static final int EOCD_REC_MIN_LENGTH_ZIP = 22;
    private static final int EOCD_REC_SIG_ZIP = 101010256;
    private static final long MAX_VALUE_LONG = 4294967295L;
    private static final int MAX_VALUE_UINT16 = 65535;

    private ZipHelper() {
    }

    public static long getZipCentralDirectoryOffset(ByteBuffer zipEocd) {
        confirmByteOrderLittleEndian(zipEocd);
        return getUInt32(zipEocd, zipEocd.position() + 16);
    }

    public static long getZipCentralDirectoryLength(ByteBuffer zipEocd) {
        confirmByteOrderLittleEndian(zipEocd);
        return getUInt32(zipEocd, zipEocd.position() + 12);
    }

    public static final boolean isZip64EocDLocatorPresent(RandomAccessFile zipFile, long zipEocdPos) throws IOException {
        if (zipEocdPos < 20) {
            return false;
        }
        zipFile.seek(zipEocdPos - 20);
        if (zipFile.readInt() == EOCD_LOCATOR_SIG_REVERSE_BYTE_ORDER_ZIP64) {
            return true;
        }
        return false;
    }

    static Pair<ByteBuffer, Long> findZipEocdRecord(RandomAccessFile zip) throws IOException {
        if (zip.length() < 22) {
            return null;
        }
        Pair<ByteBuffer, Long> result = findZipEocdRecord(zip, 0);
        if (result == null) {
            return findZipEocdRecord(zip, 65535);
        }
        return result;
    }

    private static Pair<ByteBuffer, Long> findZipEocdRecord(RandomAccessFile zip, int maxCommentLength) throws IOException {
        if (maxCommentLength > 65535 || maxCommentLength < 0) {
            throw new IllegalArgumentException("illegal maxCommentLength: " + maxCommentLength);
        }
        long fileLength = zip.length();
        long realSize = fileLength - 22;
        if (realSize < 0) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(((int) Math.min(realSize, (long) maxCommentLength)) + 22);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        long bufFileOffset = fileLength - ((long) byteBuffer.capacity());
        zip.seek(bufFileOffset);
        zip.readFully(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.capacity());
        int bufEocdOffset = findZipEocdRecord(byteBuffer);
        if (bufEocdOffset == -1) {
            return null;
        }
        byteBuffer.position(bufEocdOffset);
        ByteBuffer eocd = byteBuffer.slice();
        eocd.order(ByteOrder.LITTLE_ENDIAN);
        return Pair.create(eocd, Long.valueOf(((long) bufEocdOffset) + bufFileOffset));
    }

    private static int findZipEocdRecord(ByteBuffer zipContents) {
        confirmByteOrderLittleEndian(zipContents);
        int eocdWithEmptyCommentStartPos = zipContents.capacity() - 22;
        if (eocdWithEmptyCommentStartPos < 0) {
            return -1;
        }
        int maxCommentLength = Math.min(65535, eocdWithEmptyCommentStartPos);
        for (int expectedCommentLength = 0; expectedCommentLength <= maxCommentLength; expectedCommentLength++) {
            int eocdStartPos = eocdWithEmptyCommentStartPos - expectedCommentLength;
            if (zipContents.getInt(eocdStartPos) == EOCD_REC_SIG_ZIP && getUInt16(zipContents, eocdStartPos + 20) == expectedCommentLength) {
                return eocdStartPos;
            }
        }
        return -1;
    }

    private static int getUInt16(ByteBuffer byteBuffer, int offset) {
        return byteBuffer.getShort(offset) & 65535;
    }

    private static long getUInt32(ByteBuffer byteBuffer, int offset) {
        return ((long) byteBuffer.getInt(offset)) & MAX_VALUE_LONG;
    }

    private static void confirmByteOrderLittleEndian(ByteBuffer byteBuffer) {
        if (byteBuffer.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("the order of byteBuffer byte must be little endian");
        }
    }
}
