package android.util.proto;

import android.provider.Telephony.BaseMmsColumns;
import android.util.Log;
import android.util.LogException;
import com.android.internal.logging.EventLogTags;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

public final class ProtoOutputStream {
    public static final long FIELD_COUNT_MASK = 16492674416640L;
    public static final long FIELD_COUNT_PACKED = 5497558138880L;
    public static final long FIELD_COUNT_REPEATED = 2199023255552L;
    public static final int FIELD_COUNT_SHIFT = 40;
    public static final long FIELD_COUNT_SINGLE = 1099511627776L;
    public static final long FIELD_COUNT_UNKNOWN = 0;
    public static final int FIELD_ID_MASK = -8;
    public static final int FIELD_ID_SHIFT = 3;
    public static final long FIELD_TYPE_BOOL = 55834574848L;
    public static final long FIELD_TYPE_BYTES = 64424509440L;
    public static final long FIELD_TYPE_DOUBLE = 4294967296L;
    public static final long FIELD_TYPE_ENUM = 68719476736L;
    public static final long FIELD_TYPE_FIXED32 = 38654705664L;
    public static final long FIELD_TYPE_FIXED64 = 42949672960L;
    public static final long FIELD_TYPE_FLOAT = 8589934592L;
    public static final long FIELD_TYPE_INT32 = 12884901888L;
    public static final long FIELD_TYPE_INT64 = 17179869184L;
    public static final long FIELD_TYPE_MASK = 1095216660480L;
    private static final String[] FIELD_TYPE_NAMES = new String[]{"Double", "Float", "Int32", "Int64", "UInt32", "UInt64", "SInt32", "SInt64", "Fixed32", "Fixed64", "SFixed32", "SFixed64", "Bool", "String", "Bytes", "Enum", "Object"};
    public static final long FIELD_TYPE_OBJECT = 73014444032L;
    public static final long FIELD_TYPE_SFIXED32 = 47244640256L;
    public static final long FIELD_TYPE_SFIXED64 = 51539607552L;
    public static final int FIELD_TYPE_SHIFT = 32;
    public static final long FIELD_TYPE_SINT32 = 30064771072L;
    public static final long FIELD_TYPE_SINT64 = 34359738368L;
    public static final long FIELD_TYPE_STRING = 60129542144L;
    public static final long FIELD_TYPE_UINT32 = 21474836480L;
    public static final long FIELD_TYPE_UINT64 = 25769803776L;
    public static final long FIELD_TYPE_UNKNOWN = 0;
    public static final String TAG = "ProtoOutputStream";
    public static final int WIRE_TYPE_END_GROUP = 4;
    public static final int WIRE_TYPE_FIXED32 = 5;
    public static final int WIRE_TYPE_FIXED64 = 1;
    public static final int WIRE_TYPE_LENGTH_DELIMITED = 2;
    public static final int WIRE_TYPE_MASK = 7;
    public static final int WIRE_TYPE_START_GROUP = 3;
    public static final int WIRE_TYPE_VARINT = 0;
    private EncodedBuffer mBuffer;
    private boolean mCompacted;
    private int mCopyBegin;
    private int mDepth;
    private long mExpectedObjectToken;
    private int mNextObjectId;
    private OutputStream mStream;

    public ProtoOutputStream() {
        this(0);
    }

    public ProtoOutputStream(int chunkSize) {
        this.mNextObjectId = -1;
        this.mBuffer = new EncodedBuffer(chunkSize);
    }

    public ProtoOutputStream(OutputStream stream) {
        this();
        this.mStream = stream;
    }

    public ProtoOutputStream(FileDescriptor fd) {
        this(new FileOutputStream(fd));
    }

    public void write(long fieldId, double val) {
        boolean z = true;
        assertNotCompacted();
        int id = (int) fieldId;
        switch ((int) ((17587891077120L & fieldId) >> 32)) {
            case 257:
                writeDoubleImpl(id, val);
                return;
            case 258:
                writeFloatImpl(id, (float) val);
                return;
            case 259:
                writeInt32Impl(id, (int) val);
                return;
            case 260:
                writeInt64Impl(id, (long) val);
                return;
            case 261:
                writeUInt32Impl(id, (int) val);
                return;
            case 262:
                writeUInt64Impl(id, (long) val);
                return;
            case 263:
                writeSInt32Impl(id, (int) val);
                return;
            case 264:
                writeSInt64Impl(id, (long) val);
                return;
            case 265:
                writeFixed32Impl(id, (int) val);
                return;
            case 266:
                writeFixed64Impl(id, (long) val);
                return;
            case 267:
                writeSFixed32Impl(id, (int) val);
                return;
            case 268:
                writeSFixed64Impl(id, (long) val);
                return;
            case 269:
                if (val == 0.0d) {
                    z = false;
                }
                writeBoolImpl(id, z);
                return;
            case 272:
                writeEnumImpl(id, (int) val);
                return;
            case 513:
            case GL10.GL_INVALID_VALUE /*1281*/:
                writeRepeatedDoubleImpl(id, val);
                return;
            case 514:
            case GL10.GL_INVALID_OPERATION /*1282*/:
                writeRepeatedFloatImpl(id, (float) val);
                return;
            case 515:
            case GL10.GL_STACK_OVERFLOW /*1283*/:
                writeRepeatedInt32Impl(id, (int) val);
                return;
            case 516:
            case GL10.GL_STACK_UNDERFLOW /*1284*/:
                writeRepeatedInt64Impl(id, (long) val);
                return;
            case 517:
            case GL10.GL_OUT_OF_MEMORY /*1285*/:
                writeRepeatedUInt32Impl(id, (int) val);
                return;
            case 518:
            case GL11ExtensionPack.GL_INVALID_FRAMEBUFFER_OPERATION_OES /*1286*/:
                writeRepeatedUInt64Impl(id, (long) val);
                return;
            case 519:
            case 1287:
                writeRepeatedSInt32Impl(id, (int) val);
                return;
            case 520:
            case 1288:
                writeRepeatedSInt64Impl(id, (long) val);
                return;
            case 521:
            case 1289:
                writeRepeatedFixed32Impl(id, (int) val);
                return;
            case 522:
            case 1290:
                writeRepeatedFixed64Impl(id, (long) val);
                return;
            case 523:
            case 1291:
                writeRepeatedSFixed32Impl(id, (int) val);
                return;
            case 524:
            case 1292:
                writeRepeatedSFixed64Impl(id, (long) val);
                return;
            case 525:
            case 1293:
                if (val == 0.0d) {
                    z = false;
                }
                writeRepeatedBoolImpl(id, z);
                return;
            case 528:
            case 1296:
                writeRepeatedEnumImpl(id, (int) val);
                return;
            default:
                throw new IllegalArgumentException("Attempt to call write(long, double) with " + getFieldIdString(fieldId));
        }
    }

    public void write(long fieldId, float val) {
        boolean z = true;
        assertNotCompacted();
        int id = (int) fieldId;
        switch ((int) ((17587891077120L & fieldId) >> 32)) {
            case 257:
                writeDoubleImpl(id, (double) val);
                return;
            case 258:
                writeFloatImpl(id, val);
                return;
            case 259:
                writeInt32Impl(id, (int) val);
                return;
            case 260:
                writeInt64Impl(id, (long) val);
                return;
            case 261:
                writeUInt32Impl(id, (int) val);
                return;
            case 262:
                writeUInt64Impl(id, (long) val);
                return;
            case 263:
                writeSInt32Impl(id, (int) val);
                return;
            case 264:
                writeSInt64Impl(id, (long) val);
                return;
            case 265:
                writeFixed32Impl(id, (int) val);
                return;
            case 266:
                writeFixed64Impl(id, (long) val);
                return;
            case 267:
                writeSFixed32Impl(id, (int) val);
                return;
            case 268:
                writeSFixed64Impl(id, (long) val);
                return;
            case 269:
                if (val == 0.0f) {
                    z = false;
                }
                writeBoolImpl(id, z);
                return;
            case 272:
                writeEnumImpl(id, (int) val);
                return;
            case 513:
            case GL10.GL_INVALID_VALUE /*1281*/:
                writeRepeatedDoubleImpl(id, (double) val);
                return;
            case 514:
            case GL10.GL_INVALID_OPERATION /*1282*/:
                writeRepeatedFloatImpl(id, val);
                return;
            case 515:
            case GL10.GL_STACK_OVERFLOW /*1283*/:
                writeRepeatedInt32Impl(id, (int) val);
                return;
            case 516:
            case GL10.GL_STACK_UNDERFLOW /*1284*/:
                writeRepeatedInt64Impl(id, (long) val);
                return;
            case 517:
            case GL10.GL_OUT_OF_MEMORY /*1285*/:
                writeRepeatedUInt32Impl(id, (int) val);
                return;
            case 518:
            case GL11ExtensionPack.GL_INVALID_FRAMEBUFFER_OPERATION_OES /*1286*/:
                writeRepeatedUInt64Impl(id, (long) val);
                return;
            case 519:
            case 1287:
                writeRepeatedSInt32Impl(id, (int) val);
                return;
            case 520:
            case 1288:
                writeRepeatedSInt64Impl(id, (long) val);
                return;
            case 521:
            case 1289:
                writeRepeatedFixed32Impl(id, (int) val);
                return;
            case 522:
            case 1290:
                writeRepeatedFixed64Impl(id, (long) val);
                return;
            case 523:
            case 1291:
                writeRepeatedSFixed32Impl(id, (int) val);
                return;
            case 524:
            case 1292:
                writeRepeatedSFixed64Impl(id, (long) val);
                return;
            case 525:
            case 1293:
                if (val == 0.0f) {
                    z = false;
                }
                writeRepeatedBoolImpl(id, z);
                return;
            case 528:
            case 1296:
                writeRepeatedEnumImpl(id, (int) val);
                return;
            default:
                throw new IllegalArgumentException("Attempt to call write(long, float) with " + getFieldIdString(fieldId));
        }
    }

    public void write(long fieldId, int val) {
        boolean z = true;
        assertNotCompacted();
        int id = (int) fieldId;
        switch ((int) ((17587891077120L & fieldId) >> 32)) {
            case 257:
                writeDoubleImpl(id, (double) val);
                return;
            case 258:
                writeFloatImpl(id, (float) val);
                return;
            case 259:
                writeInt32Impl(id, val);
                return;
            case 260:
                writeInt64Impl(id, (long) val);
                return;
            case 261:
                writeUInt32Impl(id, val);
                return;
            case 262:
                writeUInt64Impl(id, (long) val);
                return;
            case 263:
                writeSInt32Impl(id, val);
                return;
            case 264:
                writeSInt64Impl(id, (long) val);
                return;
            case 265:
                writeFixed32Impl(id, val);
                return;
            case 266:
                writeFixed64Impl(id, (long) val);
                return;
            case 267:
                writeSFixed32Impl(id, val);
                return;
            case 268:
                writeSFixed64Impl(id, (long) val);
                return;
            case 269:
                if (val == 0) {
                    z = false;
                }
                writeBoolImpl(id, z);
                return;
            case 272:
                writeEnumImpl(id, val);
                return;
            case 513:
            case GL10.GL_INVALID_VALUE /*1281*/:
                writeRepeatedDoubleImpl(id, (double) val);
                return;
            case 514:
            case GL10.GL_INVALID_OPERATION /*1282*/:
                writeRepeatedFloatImpl(id, (float) val);
                return;
            case 515:
            case GL10.GL_STACK_OVERFLOW /*1283*/:
                writeRepeatedInt32Impl(id, val);
                return;
            case 516:
            case GL10.GL_STACK_UNDERFLOW /*1284*/:
                writeRepeatedInt64Impl(id, (long) val);
                return;
            case 517:
            case GL10.GL_OUT_OF_MEMORY /*1285*/:
                writeRepeatedUInt32Impl(id, val);
                return;
            case 518:
            case GL11ExtensionPack.GL_INVALID_FRAMEBUFFER_OPERATION_OES /*1286*/:
                writeRepeatedUInt64Impl(id, (long) val);
                return;
            case 519:
            case 1287:
                writeRepeatedSInt32Impl(id, val);
                return;
            case 520:
            case 1288:
                writeRepeatedSInt64Impl(id, (long) val);
                return;
            case 521:
            case 1289:
                writeRepeatedFixed32Impl(id, val);
                return;
            case 522:
            case 1290:
                writeRepeatedFixed64Impl(id, (long) val);
                return;
            case 523:
            case 1291:
                writeRepeatedSFixed32Impl(id, val);
                return;
            case 524:
            case 1292:
                writeRepeatedSFixed64Impl(id, (long) val);
                return;
            case 525:
            case 1293:
                if (val == 0) {
                    z = false;
                }
                writeRepeatedBoolImpl(id, z);
                return;
            case 528:
            case 1296:
                writeRepeatedEnumImpl(id, val);
                return;
            default:
                throw new IllegalArgumentException("Attempt to call write(long, int) with " + getFieldIdString(fieldId));
        }
    }

    public void write(long fieldId, long val) {
        boolean z = true;
        assertNotCompacted();
        int id = (int) fieldId;
        switch ((int) ((17587891077120L & fieldId) >> 32)) {
            case 257:
                writeDoubleImpl(id, (double) val);
                return;
            case 258:
                writeFloatImpl(id, (float) val);
                return;
            case 259:
                writeInt32Impl(id, (int) val);
                return;
            case 260:
                writeInt64Impl(id, val);
                return;
            case 261:
                writeUInt32Impl(id, (int) val);
                return;
            case 262:
                writeUInt64Impl(id, val);
                return;
            case 263:
                writeSInt32Impl(id, (int) val);
                return;
            case 264:
                writeSInt64Impl(id, val);
                return;
            case 265:
                writeFixed32Impl(id, (int) val);
                return;
            case 266:
                writeFixed64Impl(id, val);
                return;
            case 267:
                writeSFixed32Impl(id, (int) val);
                return;
            case 268:
                writeSFixed64Impl(id, val);
                return;
            case 269:
                if (val == 0) {
                    z = false;
                }
                writeBoolImpl(id, z);
                return;
            case 272:
                writeEnumImpl(id, (int) val);
                return;
            case 513:
            case GL10.GL_INVALID_VALUE /*1281*/:
                writeRepeatedDoubleImpl(id, (double) val);
                return;
            case 514:
            case GL10.GL_INVALID_OPERATION /*1282*/:
                writeRepeatedFloatImpl(id, (float) val);
                return;
            case 515:
            case GL10.GL_STACK_OVERFLOW /*1283*/:
                writeRepeatedInt32Impl(id, (int) val);
                return;
            case 516:
            case GL10.GL_STACK_UNDERFLOW /*1284*/:
                writeRepeatedInt64Impl(id, val);
                return;
            case 517:
            case GL10.GL_OUT_OF_MEMORY /*1285*/:
                writeRepeatedUInt32Impl(id, (int) val);
                return;
            case 518:
            case GL11ExtensionPack.GL_INVALID_FRAMEBUFFER_OPERATION_OES /*1286*/:
                writeRepeatedUInt64Impl(id, val);
                return;
            case 519:
            case 1287:
                writeRepeatedSInt32Impl(id, (int) val);
                return;
            case 520:
            case 1288:
                writeRepeatedSInt64Impl(id, val);
                return;
            case 521:
            case 1289:
                writeRepeatedFixed32Impl(id, (int) val);
                return;
            case 522:
            case 1290:
                writeRepeatedFixed64Impl(id, val);
                return;
            case 523:
            case 1291:
                writeRepeatedSFixed32Impl(id, (int) val);
                return;
            case 524:
            case 1292:
                writeRepeatedSFixed64Impl(id, val);
                return;
            case 525:
            case 1293:
                if (val == 0) {
                    z = false;
                }
                writeRepeatedBoolImpl(id, z);
                return;
            case 528:
            case 1296:
                writeRepeatedEnumImpl(id, (int) val);
                return;
            default:
                throw new IllegalArgumentException("Attempt to call write(long, long) with " + getFieldIdString(fieldId));
        }
    }

    public void write(long fieldId, boolean val) {
        assertNotCompacted();
        int id = (int) fieldId;
        switch ((int) ((17587891077120L & fieldId) >> 32)) {
            case 269:
                writeBoolImpl(id, val);
                return;
            case 525:
            case 1293:
                writeRepeatedBoolImpl(id, val);
                return;
            default:
                throw new IllegalArgumentException("Attempt to call write(long, boolean) with " + getFieldIdString(fieldId));
        }
    }

    public void write(long fieldId, String val) {
        assertNotCompacted();
        int id = (int) fieldId;
        switch ((int) ((17587891077120L & fieldId) >> 32)) {
            case 270:
                writeStringImpl(id, val);
                return;
            case MetricsEvent.DIALOG_SUPPORT_DISCLAIMER /*526*/:
            case 1294:
                writeRepeatedStringImpl(id, val);
                return;
            default:
                throw new IllegalArgumentException("Attempt to call write(long, String) with " + getFieldIdString(fieldId));
        }
    }

    public void write(long fieldId, byte[] val) {
        assertNotCompacted();
        int id = (int) fieldId;
        switch ((int) ((17587891077120L & fieldId) >> 32)) {
            case 271:
                writeBytesImpl(id, val);
                return;
            case 273:
                writeObjectImpl(id, val);
                return;
            case MetricsEvent.DIALOG_SUPPORT_PHONE /*527*/:
            case 1295:
                writeRepeatedBytesImpl(id, val);
                return;
            case 529:
            case 1297:
                writeRepeatedObjectImpl(id, val);
                return;
            default:
                throw new IllegalArgumentException("Attempt to call write(long, byte[]) with " + getFieldIdString(fieldId));
        }
    }

    public long start(long fieldId) {
        assertNotCompacted();
        int id = (int) fieldId;
        if ((FIELD_TYPE_MASK & fieldId) == FIELD_TYPE_OBJECT) {
            long count = fieldId & FIELD_COUNT_MASK;
            if (count == FIELD_COUNT_SINGLE) {
                return startObjectImpl(id, false);
            }
            if (count == FIELD_COUNT_REPEATED || count == FIELD_COUNT_PACKED) {
                return startObjectImpl(id, true);
            }
        }
        throw new IllegalArgumentException("Attempt to call start(long) with " + getFieldIdString(fieldId));
    }

    public void end(long token) {
        endObjectImpl(token, getRepeatedFromToken(token));
    }

    @Deprecated
    public void writeDouble(long fieldId, double val) {
        assertNotCompacted();
        writeDoubleImpl(checkFieldId(fieldId, 1103806595072L), val);
    }

    private void writeDoubleImpl(int id, double val) {
        if (val != 0.0d) {
            writeTag(id, 1);
            this.mBuffer.writeRawFixed64(Double.doubleToLongBits(val));
        }
    }

    @Deprecated
    public void writeRepeatedDouble(long fieldId, double val) {
        assertNotCompacted();
        writeRepeatedDoubleImpl(checkFieldId(fieldId, 2203318222848L), val);
    }

    private void writeRepeatedDoubleImpl(int id, double val) {
        writeTag(id, 1);
        this.mBuffer.writeRawFixed64(Double.doubleToLongBits(val));
    }

    @Deprecated
    public void writePackedDouble(long fieldId, double[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5501853106176L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            writeKnownLengthHeader(id, N * 8);
            for (int i = 0; i < N; i++) {
                this.mBuffer.writeRawFixed64(Double.doubleToLongBits(val[i]));
            }
        }
    }

    @Deprecated
    public void writeFloat(long fieldId, float val) {
        assertNotCompacted();
        writeFloatImpl(checkFieldId(fieldId, 1108101562368L), val);
    }

    private void writeFloatImpl(int id, float val) {
        if (val != 0.0f) {
            writeTag(id, 5);
            this.mBuffer.writeRawFixed32(Float.floatToIntBits(val));
        }
    }

    @Deprecated
    public void writeRepeatedFloat(long fieldId, float val) {
        assertNotCompacted();
        writeRepeatedFloatImpl(checkFieldId(fieldId, 2207613190144L), val);
    }

    private void writeRepeatedFloatImpl(int id, float val) {
        writeTag(id, 5);
        this.mBuffer.writeRawFixed32(Float.floatToIntBits(val));
    }

    @Deprecated
    public void writePackedFloat(long fieldId, float[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5506148073472L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            writeKnownLengthHeader(id, N * 4);
            for (int i = 0; i < N; i++) {
                this.mBuffer.writeRawFixed32(Float.floatToIntBits(val[i]));
            }
        }
    }

    private void writeUnsignedVarintFromSignedInt(int val) {
        if (val >= 0) {
            this.mBuffer.writeRawVarint32(val);
        } else {
            this.mBuffer.writeRawVarint64((long) val);
        }
    }

    @Deprecated
    public void writeInt32(long fieldId, int val) {
        assertNotCompacted();
        writeInt32Impl(checkFieldId(fieldId, 1112396529664L), val);
    }

    private void writeInt32Impl(int id, int val) {
        if (val != 0) {
            writeTag(id, 0);
            writeUnsignedVarintFromSignedInt(val);
        }
    }

    @Deprecated
    public void writeRepeatedInt32(long fieldId, int val) {
        assertNotCompacted();
        writeRepeatedInt32Impl(checkFieldId(fieldId, 2211908157440L), val);
    }

    private void writeRepeatedInt32Impl(int id, int val) {
        writeTag(id, 0);
        writeUnsignedVarintFromSignedInt(val);
    }

    @Deprecated
    public void writePackedInt32(long fieldId, int[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5510443040768L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            int i;
            int size = 0;
            for (i = 0; i < N; i++) {
                int v = val[i];
                size += v >= 0 ? EncodedBuffer.getRawVarint32Size(v) : 10;
            }
            writeKnownLengthHeader(id, size);
            for (i = 0; i < N; i++) {
                writeUnsignedVarintFromSignedInt(val[i]);
            }
        }
    }

    @Deprecated
    public void writeInt64(long fieldId, long val) {
        assertNotCompacted();
        writeInt64Impl(checkFieldId(fieldId, 1116691496960L), val);
    }

    private void writeInt64Impl(int id, long val) {
        if (val != 0) {
            writeTag(id, 0);
            this.mBuffer.writeRawVarint64(val);
        }
    }

    @Deprecated
    public void writeRepeatedInt64(long fieldId, long val) {
        assertNotCompacted();
        writeRepeatedInt64Impl(checkFieldId(fieldId, 2216203124736L), val);
    }

    private void writeRepeatedInt64Impl(int id, long val) {
        writeTag(id, 0);
        this.mBuffer.writeRawVarint64(val);
    }

    @Deprecated
    public void writePackedInt64(long fieldId, long[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5514738008064L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            int i;
            int size = 0;
            for (i = 0; i < N; i++) {
                size += EncodedBuffer.getRawVarint64Size(val[i]);
            }
            writeKnownLengthHeader(id, size);
            for (i = 0; i < N; i++) {
                this.mBuffer.writeRawVarint64(val[i]);
            }
        }
    }

    @Deprecated
    public void writeUInt32(long fieldId, int val) {
        assertNotCompacted();
        writeUInt32Impl(checkFieldId(fieldId, 1120986464256L), val);
    }

    private void writeUInt32Impl(int id, int val) {
        if (val != 0) {
            writeTag(id, 0);
            this.mBuffer.writeRawVarint32(val);
        }
    }

    @Deprecated
    public void writeRepeatedUInt32(long fieldId, int val) {
        assertNotCompacted();
        writeRepeatedUInt32Impl(checkFieldId(fieldId, 2220498092032L), val);
    }

    private void writeRepeatedUInt32Impl(int id, int val) {
        writeTag(id, 0);
        this.mBuffer.writeRawVarint32(val);
    }

    @Deprecated
    public void writePackedUInt32(long fieldId, int[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5519032975360L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            int i;
            int size = 0;
            for (i = 0; i < N; i++) {
                size += EncodedBuffer.getRawVarint32Size(val[i]);
            }
            writeKnownLengthHeader(id, size);
            for (i = 0; i < N; i++) {
                this.mBuffer.writeRawVarint32(val[i]);
            }
        }
    }

    @Deprecated
    public void writeUInt64(long fieldId, long val) {
        assertNotCompacted();
        writeUInt64Impl(checkFieldId(fieldId, 1125281431552L), val);
    }

    private void writeUInt64Impl(int id, long val) {
        if (val != 0) {
            writeTag(id, 0);
            this.mBuffer.writeRawVarint64(val);
        }
    }

    @Deprecated
    public void writeRepeatedUInt64(long fieldId, long val) {
        assertNotCompacted();
        writeRepeatedUInt64Impl(checkFieldId(fieldId, 2224793059328L), val);
    }

    private void writeRepeatedUInt64Impl(int id, long val) {
        writeTag(id, 0);
        this.mBuffer.writeRawVarint64(val);
    }

    @Deprecated
    public void writePackedUInt64(long fieldId, long[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5523327942656L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            int i;
            int size = 0;
            for (i = 0; i < N; i++) {
                size += EncodedBuffer.getRawVarint64Size(val[i]);
            }
            writeKnownLengthHeader(id, size);
            for (i = 0; i < N; i++) {
                this.mBuffer.writeRawVarint64(val[i]);
            }
        }
    }

    @Deprecated
    public void writeSInt32(long fieldId, int val) {
        assertNotCompacted();
        writeSInt32Impl(checkFieldId(fieldId, 1129576398848L), val);
    }

    private void writeSInt32Impl(int id, int val) {
        if (val != 0) {
            writeTag(id, 0);
            this.mBuffer.writeRawZigZag32(val);
        }
    }

    @Deprecated
    public void writeRepeatedSInt32(long fieldId, int val) {
        assertNotCompacted();
        writeRepeatedSInt32Impl(checkFieldId(fieldId, 2229088026624L), val);
    }

    private void writeRepeatedSInt32Impl(int id, int val) {
        writeTag(id, 0);
        this.mBuffer.writeRawZigZag32(val);
    }

    @Deprecated
    public void writePackedSInt32(long fieldId, int[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5527622909952L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            int i;
            int size = 0;
            for (i = 0; i < N; i++) {
                size += EncodedBuffer.getRawZigZag32Size(val[i]);
            }
            writeKnownLengthHeader(id, size);
            for (i = 0; i < N; i++) {
                this.mBuffer.writeRawZigZag32(val[i]);
            }
        }
    }

    @Deprecated
    public void writeSInt64(long fieldId, long val) {
        assertNotCompacted();
        writeSInt64Impl(checkFieldId(fieldId, 1133871366144L), val);
    }

    private void writeSInt64Impl(int id, long val) {
        if (val != 0) {
            writeTag(id, 0);
            this.mBuffer.writeRawZigZag64(val);
        }
    }

    @Deprecated
    public void writeRepeatedSInt64(long fieldId, long val) {
        assertNotCompacted();
        writeRepeatedSInt64Impl(checkFieldId(fieldId, 2233382993920L), val);
    }

    private void writeRepeatedSInt64Impl(int id, long val) {
        writeTag(id, 0);
        this.mBuffer.writeRawZigZag64(val);
    }

    @Deprecated
    public void writePackedSInt64(long fieldId, long[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5531917877248L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            int i;
            int size = 0;
            for (i = 0; i < N; i++) {
                size += EncodedBuffer.getRawZigZag64Size(val[i]);
            }
            writeKnownLengthHeader(id, size);
            for (i = 0; i < N; i++) {
                this.mBuffer.writeRawZigZag64(val[i]);
            }
        }
    }

    @Deprecated
    public void writeFixed32(long fieldId, int val) {
        assertNotCompacted();
        writeFixed32Impl(checkFieldId(fieldId, 1138166333440L), val);
    }

    private void writeFixed32Impl(int id, int val) {
        if (val != 0) {
            writeTag(id, 5);
            this.mBuffer.writeRawFixed32(val);
        }
    }

    @Deprecated
    public void writeRepeatedFixed32(long fieldId, int val) {
        assertNotCompacted();
        writeRepeatedFixed32Impl(checkFieldId(fieldId, 2237677961216L), val);
    }

    private void writeRepeatedFixed32Impl(int id, int val) {
        writeTag(id, 5);
        this.mBuffer.writeRawFixed32(val);
    }

    @Deprecated
    public void writePackedFixed32(long fieldId, int[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5536212844544L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            writeKnownLengthHeader(id, N * 4);
            for (int i = 0; i < N; i++) {
                this.mBuffer.writeRawFixed32(val[i]);
            }
        }
    }

    @Deprecated
    public void writeFixed64(long fieldId, long val) {
        assertNotCompacted();
        writeFixed64Impl(checkFieldId(fieldId, 1142461300736L), val);
    }

    private void writeFixed64Impl(int id, long val) {
        if (val != 0) {
            writeTag(id, 1);
            this.mBuffer.writeRawFixed64(val);
        }
    }

    @Deprecated
    public void writeRepeatedFixed64(long fieldId, long val) {
        assertNotCompacted();
        writeRepeatedFixed64Impl(checkFieldId(fieldId, 2241972928512L), val);
    }

    private void writeRepeatedFixed64Impl(int id, long val) {
        writeTag(id, 1);
        this.mBuffer.writeRawFixed64(val);
    }

    @Deprecated
    public void writePackedFixed64(long fieldId, long[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5540507811840L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            writeKnownLengthHeader(id, N * 8);
            for (int i = 0; i < N; i++) {
                this.mBuffer.writeRawFixed64(val[i]);
            }
        }
    }

    @Deprecated
    public void writeSFixed32(long fieldId, int val) {
        assertNotCompacted();
        writeSFixed32Impl(checkFieldId(fieldId, 1146756268032L), val);
    }

    private void writeSFixed32Impl(int id, int val) {
        if (val != 0) {
            writeTag(id, 5);
            this.mBuffer.writeRawFixed32(val);
        }
    }

    @Deprecated
    public void writeRepeatedSFixed32(long fieldId, int val) {
        assertNotCompacted();
        writeRepeatedSFixed32Impl(checkFieldId(fieldId, 2246267895808L), val);
    }

    private void writeRepeatedSFixed32Impl(int id, int val) {
        writeTag(id, 5);
        this.mBuffer.writeRawFixed32(val);
    }

    @Deprecated
    public void writePackedSFixed32(long fieldId, int[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5544802779136L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            writeKnownLengthHeader(id, N * 4);
            for (int i = 0; i < N; i++) {
                this.mBuffer.writeRawFixed32(val[i]);
            }
        }
    }

    @Deprecated
    public void writeSFixed64(long fieldId, long val) {
        assertNotCompacted();
        writeSFixed64Impl(checkFieldId(fieldId, 1151051235328L), val);
    }

    private void writeSFixed64Impl(int id, long val) {
        if (val != 0) {
            writeTag(id, 1);
            this.mBuffer.writeRawFixed64(val);
        }
    }

    @Deprecated
    public void writeRepeatedSFixed64(long fieldId, long val) {
        assertNotCompacted();
        writeRepeatedSFixed64Impl(checkFieldId(fieldId, 2250562863104L), val);
    }

    private void writeRepeatedSFixed64Impl(int id, long val) {
        writeTag(id, 1);
        this.mBuffer.writeRawFixed64(val);
    }

    @Deprecated
    public void writePackedSFixed64(long fieldId, long[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5549097746432L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            writeKnownLengthHeader(id, N * 8);
            for (int i = 0; i < N; i++) {
                this.mBuffer.writeRawFixed64(val[i]);
            }
        }
    }

    @Deprecated
    public void writeBool(long fieldId, boolean val) {
        assertNotCompacted();
        writeBoolImpl(checkFieldId(fieldId, 1155346202624L), val);
    }

    private void writeBoolImpl(int id, boolean val) {
        if (val) {
            writeTag(id, 0);
            this.mBuffer.writeRawByte((byte) 1);
        }
    }

    @Deprecated
    public void writeRepeatedBool(long fieldId, boolean val) {
        assertNotCompacted();
        writeRepeatedBoolImpl(checkFieldId(fieldId, 2254857830400L), val);
    }

    private void writeRepeatedBoolImpl(int id, boolean val) {
        int i = 0;
        writeTag(id, 0);
        EncodedBuffer encodedBuffer = this.mBuffer;
        if (val) {
            i = 1;
        }
        encodedBuffer.writeRawByte((byte) i);
    }

    @Deprecated
    public void writePackedBool(long fieldId, boolean[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5553392713728L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            writeKnownLengthHeader(id, N);
            for (int i = 0; i < N; i++) {
                int i2;
                EncodedBuffer encodedBuffer = this.mBuffer;
                if (val[i]) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                encodedBuffer.writeRawByte((byte) i2);
            }
        }
    }

    @Deprecated
    public void writeString(long fieldId, String val) {
        assertNotCompacted();
        writeStringImpl(checkFieldId(fieldId, 1159641169920L), val);
    }

    private void writeStringImpl(int id, String val) {
        if (val != null && val.length() > 0) {
            writeUtf8String(id, val);
        }
    }

    @Deprecated
    public void writeRepeatedString(long fieldId, String val) {
        assertNotCompacted();
        writeRepeatedStringImpl(checkFieldId(fieldId, 2259152797696L), val);
    }

    private void writeRepeatedStringImpl(int id, String val) {
        if (val == null || val.length() == 0) {
            writeKnownLengthHeader(id, 0);
        } else {
            writeUtf8String(id, val);
        }
    }

    private void writeUtf8String(int id, String val) {
        try {
            byte[] buf = val.getBytes("UTF-8");
            writeKnownLengthHeader(id, buf.length);
            this.mBuffer.writeRawBuffer(buf);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("not possible");
        }
    }

    @Deprecated
    public void writeBytes(long fieldId, byte[] val) {
        assertNotCompacted();
        writeBytesImpl(checkFieldId(fieldId, 1163936137216L), val);
    }

    private void writeBytesImpl(int id, byte[] val) {
        if (val != null && val.length > 0) {
            writeKnownLengthHeader(id, val.length);
            this.mBuffer.writeRawBuffer(val);
        }
    }

    @Deprecated
    public void writeRepeatedBytes(long fieldId, byte[] val) {
        assertNotCompacted();
        writeRepeatedBytesImpl(checkFieldId(fieldId, 2263447764992L), val);
    }

    private void writeRepeatedBytesImpl(int id, byte[] val) {
        writeKnownLengthHeader(id, val == null ? 0 : val.length);
        this.mBuffer.writeRawBuffer(val);
    }

    @Deprecated
    public void writeEnum(long fieldId, int val) {
        assertNotCompacted();
        writeEnumImpl(checkFieldId(fieldId, 1168231104512L), val);
    }

    private void writeEnumImpl(int id, int val) {
        if (val != 0) {
            writeTag(id, 0);
            writeUnsignedVarintFromSignedInt(val);
        }
    }

    @Deprecated
    public void writeRepeatedEnum(long fieldId, int val) {
        assertNotCompacted();
        writeRepeatedEnumImpl(checkFieldId(fieldId, 2267742732288L), val);
    }

    private void writeRepeatedEnumImpl(int id, int val) {
        writeTag(id, 0);
        writeUnsignedVarintFromSignedInt(val);
    }

    @Deprecated
    public void writePackedEnum(long fieldId, int[] val) {
        assertNotCompacted();
        int id = checkFieldId(fieldId, 5566277615616L);
        int N = val != null ? val.length : 0;
        if (N > 0) {
            int i;
            int size = 0;
            for (i = 0; i < N; i++) {
                int v = val[i];
                size += v >= 0 ? EncodedBuffer.getRawVarint32Size(v) : 10;
            }
            writeKnownLengthHeader(id, size);
            for (i = 0; i < N; i++) {
                writeUnsignedVarintFromSignedInt(val[i]);
            }
        }
    }

    public static long makeToken(int tagSize, boolean repeated, int depth, int objectId, int sizePos) {
        return ((((repeated ? 1152921504606846976L : 0) | ((((long) tagSize) & 7) << 61)) | ((((long) depth) & 511) << 51)) | ((((long) objectId) & 524287) << 32)) | (((long) sizePos) & 4294967295L);
    }

    public static int getTagSizeFromToken(long token) {
        return (int) ((token >> 61) & 7);
    }

    public static boolean getRepeatedFromToken(long token) {
        return ((token >> 60) & 1) != 0;
    }

    public static int getDepthFromToken(long token) {
        return (int) ((token >> 51) & 511);
    }

    public static int getObjectIdFromToken(long token) {
        return (int) ((token >> 32) & 524287);
    }

    public static int getSizePosFromToken(long token) {
        return (int) token;
    }

    public static int convertObjectIdToOrdinal(int objectId) {
        return EventLogTags.SYSUI_VIEW_VISIBILITY - objectId;
    }

    public static String token2String(long token) {
        if (token == 0) {
            return "Token(0)";
        }
        return "Token(val=0x" + Long.toHexString(token) + " depth=" + getDepthFromToken(token) + " object=" + convertObjectIdToOrdinal(getObjectIdFromToken(token)) + " tagSize=" + getTagSizeFromToken(token) + " sizePos=" + getSizePosFromToken(token) + ')';
    }

    @Deprecated
    public long startObject(long fieldId) {
        assertNotCompacted();
        return startObjectImpl(checkFieldId(fieldId, 1172526071808L), false);
    }

    @Deprecated
    public void endObject(long token) {
        assertNotCompacted();
        endObjectImpl(token, false);
    }

    @Deprecated
    public long startRepeatedObject(long fieldId) {
        assertNotCompacted();
        return startObjectImpl(checkFieldId(fieldId, 2272037699584L), true);
    }

    @Deprecated
    public void endRepeatedObject(long token) {
        assertNotCompacted();
        endObjectImpl(token, true);
    }

    private long startObjectImpl(int id, boolean repeated) {
        writeTag(id, 2);
        int sizePos = this.mBuffer.getWritePos();
        this.mDepth++;
        this.mNextObjectId--;
        this.mBuffer.writeRawFixed32((int) (this.mExpectedObjectToken >> 32));
        this.mBuffer.writeRawFixed32((int) this.mExpectedObjectToken);
        long old = this.mExpectedObjectToken;
        this.mExpectedObjectToken = makeToken(getTagSize(id), repeated, this.mDepth, this.mNextObjectId, sizePos);
        return this.mExpectedObjectToken;
    }

    private void endObjectImpl(long token, boolean repeated) {
        int depth = getDepthFromToken(token);
        boolean expectedRepeated = getRepeatedFromToken(token);
        int sizePos = getSizePosFromToken(token);
        int childRawSize = (this.mBuffer.getWritePos() - sizePos) - 8;
        if (repeated != expectedRepeated) {
            if (repeated) {
                throw new IllegalArgumentException("endRepeatedObject called where endObject should have been");
            }
            throw new IllegalArgumentException("endObject called where endRepeatedObject should have been");
        } else if ((this.mDepth & 511) == depth && this.mExpectedObjectToken == token) {
            this.mExpectedObjectToken = (((long) this.mBuffer.getRawFixed32At(sizePos)) << 32) | (((long) this.mBuffer.getRawFixed32At(sizePos + 4)) & 4294967295L);
            this.mDepth--;
            if (childRawSize > 0) {
                this.mBuffer.editRawFixed32(sizePos, -childRawSize);
                this.mBuffer.editRawFixed32(sizePos + 4, -1);
            } else if (repeated) {
                this.mBuffer.editRawFixed32(sizePos, 0);
                this.mBuffer.editRawFixed32(sizePos + 4, 0);
            } else {
                this.mBuffer.rewindWriteTo(sizePos - getTagSizeFromToken(token));
            }
        } else {
            throw new IllegalArgumentException("Mismatched startObject/endObject calls. Current depth " + this.mDepth + " token=" + token2String(token) + " expectedToken=" + token2String(this.mExpectedObjectToken));
        }
    }

    @Deprecated
    public void writeObject(long fieldId, byte[] value) {
        assertNotCompacted();
        writeObjectImpl(checkFieldId(fieldId, 1172526071808L), value);
    }

    void writeObjectImpl(int id, byte[] value) {
        if (value != null && value.length != 0) {
            writeKnownLengthHeader(id, value.length);
            this.mBuffer.writeRawBuffer(value);
        }
    }

    @Deprecated
    public void writeRepeatedObject(long fieldId, byte[] value) {
        assertNotCompacted();
        writeRepeatedObjectImpl(checkFieldId(fieldId, 2272037699584L), value);
    }

    void writeRepeatedObjectImpl(int id, byte[] value) {
        writeKnownLengthHeader(id, value == null ? 0 : value.length);
        this.mBuffer.writeRawBuffer(value);
    }

    public static long makeFieldId(int id, long fieldFlags) {
        return (((long) id) & 4294967295L) | fieldFlags;
    }

    public static int checkFieldId(long fieldId, long expectedFlags) {
        long fieldCount = fieldId & FIELD_COUNT_MASK;
        long fieldType = fieldId & FIELD_TYPE_MASK;
        long expectedCount = expectedFlags & FIELD_COUNT_MASK;
        long expectedType = expectedFlags & FIELD_TYPE_MASK;
        if (((int) fieldId) == 0) {
            throw new IllegalArgumentException("Invalid proto field " + ((int) fieldId) + " fieldId=" + Long.toHexString(fieldId));
        } else if (fieldType == expectedType && (fieldCount == expectedCount || (fieldCount == FIELD_COUNT_PACKED && expectedCount == FIELD_COUNT_REPEATED))) {
            return (int) fieldId;
        } else {
            String countString = getFieldCountString(fieldCount);
            String typeString = getFieldTypeString(fieldType);
            StringBuilder sb;
            if (typeString == null || countString == null) {
                sb = new StringBuilder();
                if (expectedType == FIELD_TYPE_OBJECT) {
                    sb.append(BaseMmsColumns.START);
                } else {
                    sb.append("write");
                }
                sb.append(getFieldCountString(expectedCount));
                sb.append(getFieldTypeString(expectedType));
                sb.append(" called with an invalid fieldId: 0x");
                sb.append(Long.toHexString(fieldId));
                sb.append(". The proto field ID might be ");
                sb.append((int) fieldId);
                sb.append('.');
                throw new IllegalArgumentException(sb.toString());
            }
            sb = new StringBuilder();
            if (expectedType == FIELD_TYPE_OBJECT) {
                sb.append(BaseMmsColumns.START);
            } else {
                sb.append("write");
            }
            sb.append(getFieldCountString(expectedCount));
            sb.append(getFieldTypeString(expectedType));
            sb.append(" called for field ");
            sb.append((int) fieldId);
            sb.append(" which should be used with ");
            if (fieldType == FIELD_TYPE_OBJECT) {
                sb.append(BaseMmsColumns.START);
            } else {
                sb.append("write");
            }
            sb.append(countString);
            sb.append(typeString);
            if (fieldCount == FIELD_COUNT_PACKED) {
                sb.append(" or writeRepeated");
                sb.append(typeString);
            }
            sb.append('.');
            throw new IllegalArgumentException(sb.toString());
        }
    }

    private static String getFieldTypeString(long fieldType) {
        int index = ((int) ((FIELD_TYPE_MASK & fieldType) >>> 32)) - 1;
        if (index < 0 || index >= FIELD_TYPE_NAMES.length) {
            return null;
        }
        return FIELD_TYPE_NAMES[index];
    }

    private static String getFieldCountString(long fieldCount) {
        if (fieldCount == FIELD_COUNT_SINGLE) {
            return LogException.NO_VALUE;
        }
        if (fieldCount == FIELD_COUNT_REPEATED) {
            return "Repeated";
        }
        if (fieldCount == FIELD_COUNT_PACKED) {
            return "Packed";
        }
        return null;
    }

    private String getFieldIdString(long fieldId) {
        long fieldCount = fieldId & FIELD_COUNT_MASK;
        if (getFieldCountString(fieldCount) == null) {
            String countString = "fieldCount=" + fieldCount;
        }
        long fieldType = fieldId & FIELD_TYPE_MASK;
        String typeString = getFieldTypeString(fieldType);
        if (typeString == null) {
            typeString = "fieldType=" + fieldType;
        }
        return fieldCount + " " + typeString + " tag=" + ((int) fieldId) + " fieldId=0x" + Long.toHexString(fieldId);
    }

    private static int getTagSize(int id) {
        return EncodedBuffer.getRawVarint32Size(id << 3);
    }

    public void writeTag(int id, int wireType) {
        this.mBuffer.writeRawVarint32((id << 3) | wireType);
    }

    private void writeKnownLengthHeader(int id, int size) {
        writeTag(id, 2);
        this.mBuffer.writeRawFixed32(size);
        this.mBuffer.writeRawFixed32(size);
    }

    private void assertNotCompacted() {
        if (this.mCompacted) {
            throw new IllegalArgumentException("write called after compact");
        }
    }

    public byte[] getBytes() {
        compactIfNecessary();
        return this.mBuffer.getBytes(this.mBuffer.getReadableSize());
    }

    private void compactIfNecessary() {
        if (!this.mCompacted) {
            if (this.mDepth != 0) {
                throw new IllegalArgumentException("Trying to compact with " + this.mDepth + " missing calls to endObject");
            }
            this.mBuffer.startEditing();
            int readableSize = this.mBuffer.getReadableSize();
            editEncodedSize(readableSize);
            this.mBuffer.rewindRead();
            compactSizes(readableSize);
            if (this.mCopyBegin < readableSize) {
                this.mBuffer.writeFromThisBuffer(this.mCopyBegin, readableSize - this.mCopyBegin);
            }
            this.mBuffer.startEditing();
            this.mCompacted = true;
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0063, code:
            if ((r12.mBuffer.readRawByte() & 128) == 0) goto L_0x0009;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int editEncodedSize(int rawSize) {
        int objectEnd = this.mBuffer.getReadPos() + rawSize;
        int encodedSize = 0;
        while (true) {
            int tagPos = this.mBuffer.getReadPos();
            if (tagPos >= objectEnd) {
                return encodedSize;
            }
            int tag = readRawTag();
            encodedSize += EncodedBuffer.getRawVarint32Size(tag);
            int wireType = tag & 7;
            switch (wireType) {
                case 0:
                    while (true) {
                        encodedSize++;
                        break;
                    }
                case 1:
                    encodedSize += 8;
                    this.mBuffer.skipRead(8);
                    break;
                case 2:
                    int childRawSize = this.mBuffer.readRawFixed32();
                    int childEncodedSizePos = this.mBuffer.getReadPos();
                    int childEncodedSize = this.mBuffer.readRawFixed32();
                    if (childRawSize < 0) {
                        childEncodedSize = editEncodedSize(-childRawSize);
                        this.mBuffer.editRawFixed32(childEncodedSizePos, childEncodedSize);
                    } else if (childEncodedSize != childRawSize) {
                        throw new RuntimeException("Pre-computed size where the precomputed size and the raw size in the buffer don't match! childRawSize=" + childRawSize + " childEncodedSize=" + childEncodedSize + " childEncodedSizePos=" + childEncodedSizePos);
                    } else {
                        this.mBuffer.skipRead(childRawSize);
                    }
                    encodedSize += EncodedBuffer.getRawVarint32Size(childEncodedSize) + childEncodedSize;
                    break;
                case 3:
                case 4:
                    throw new RuntimeException("groups not supported at index " + tagPos);
                case 5:
                    encodedSize += 4;
                    this.mBuffer.skipRead(4);
                    break;
                default:
                    throw new ProtoParseException("editEncodedSize Bad tag tag=0x" + Integer.toHexString(tag) + " wireType=" + wireType + " -- " + this.mBuffer.getDebugString());
            }
        }
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void compactSizes(int rawSize) {
        int objectEnd = this.mBuffer.getReadPos() + rawSize;
        while (true) {
            int tagPos = this.mBuffer.getReadPos();
            if (tagPos < objectEnd) {
                int tag = readRawTag();
                int wireType = tag & 7;
                switch (wireType) {
                    case 0:
                        while ((this.mBuffer.readRawByte() & 128) != 0) {
                        }
                        break;
                    case 1:
                        this.mBuffer.skipRead(8);
                        break;
                    case 2:
                        this.mBuffer.writeFromThisBuffer(this.mCopyBegin, this.mBuffer.getReadPos() - this.mCopyBegin);
                        int childRawSize = this.mBuffer.readRawFixed32();
                        int childEncodedSize = this.mBuffer.readRawFixed32();
                        this.mBuffer.writeRawVarint32(childEncodedSize);
                        this.mCopyBegin = this.mBuffer.getReadPos();
                        if (childRawSize < 0) {
                            compactSizes(-childRawSize);
                            break;
                        } else {
                            this.mBuffer.skipRead(childEncodedSize);
                            break;
                        }
                    case 3:
                    case 4:
                        throw new RuntimeException("groups not supported at index " + tagPos);
                    case 5:
                        this.mBuffer.skipRead(4);
                        break;
                    default:
                        throw new ProtoParseException("compactSizes Bad tag tag=0x" + Integer.toHexString(tag) + " wireType=" + wireType + " -- " + this.mBuffer.getDebugString());
                }
            }
            return;
        }
    }

    public void flush() {
        if (this.mStream != null && this.mDepth == 0 && !this.mCompacted) {
            compactIfNecessary();
            try {
                this.mStream.write(this.mBuffer.getBytes(this.mBuffer.getReadableSize()));
                this.mStream.flush();
            } catch (IOException ex) {
                throw new RuntimeException("Error flushing proto to stream", ex);
            }
        }
    }

    private int readRawTag() {
        if (this.mBuffer.getReadPos() == this.mBuffer.getReadableSize()) {
            return 0;
        }
        return (int) this.mBuffer.readRawUnsigned();
    }

    public void dump(String tag) {
        Log.d(tag, this.mBuffer.getDebugString());
        this.mBuffer.dumpBuffers(tag);
    }
}
