package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.time.ZoneOffset;
import sun.util.logging.PlatformLogger;

final class Ser implements Externalizable {
    static final byte ZOT = (byte) 2;
    static final byte ZOTRULE = (byte) 3;
    static final byte ZRULES = (byte) 1;
    private static final long serialVersionUID = -8885321777449118786L;
    private Object object;
    private byte type;

    Ser(byte type, Object object) {
        this.type = type;
        this.object = object;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        writeInternal(this.type, this.object, out);
    }

    static void write(Object object, DataOutput out) throws IOException {
        writeInternal((byte) 1, object, out);
    }

    private static void writeInternal(byte type, Object object, DataOutput out) throws IOException {
        out.writeByte(type);
        switch (type) {
            case (byte) 1:
                ((ZoneRules) object).writeExternal(out);
                return;
            case (byte) 2:
                ((ZoneOffsetTransition) object).writeExternal(out);
                return;
            case (byte) 3:
                ((ZoneOffsetTransitionRule) object).writeExternal(out);
                return;
            default:
                throw new InvalidClassException("Unknown serialized type");
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.type = in.readByte();
        this.object = readInternal(this.type, in);
    }

    static Object read(DataInput in) throws IOException, ClassNotFoundException {
        return readInternal(in.readByte(), in);
    }

    private static Object readInternal(byte type, DataInput in) throws IOException, ClassNotFoundException {
        switch (type) {
            case (byte) 1:
                return ZoneRules.readExternal(in);
            case (byte) 2:
                return ZoneOffsetTransition.readExternal(in);
            case (byte) 3:
                return ZoneOffsetTransitionRule.readExternal(in);
            default:
                throw new StreamCorruptedException("Unknown serialized type");
        }
    }

    private Object readResolve() {
        return this.object;
    }

    static void writeOffset(ZoneOffset offset, DataOutput out) throws IOException {
        int offsetSecs = offset.getTotalSeconds();
        int offsetByte = offsetSecs % PlatformLogger.WARNING == 0 ? offsetSecs / PlatformLogger.WARNING : 127;
        out.writeByte(offsetByte);
        if (offsetByte == 127) {
            out.writeInt(offsetSecs);
        }
    }

    static ZoneOffset readOffset(DataInput in) throws IOException {
        int offsetByte = in.readByte();
        return offsetByte == 127 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(offsetByte * PlatformLogger.WARNING);
    }

    static void writeEpochSec(long epochSec, DataOutput out) throws IOException {
        if (epochSec < -4575744000L || epochSec >= 10413792000L || epochSec % 900 != 0) {
            out.writeByte(255);
            out.writeLong(epochSec);
            return;
        }
        int store = (int) ((4575744000L + epochSec) / 900);
        out.writeByte((store >>> 16) & 255);
        out.writeByte((store >>> 8) & 255);
        out.writeByte(store & 255);
    }

    static long readEpochSec(DataInput in) throws IOException {
        int hiByte = in.readByte() & 255;
        if (hiByte == 255) {
            return in.readLong();
        }
        return (900 * ((long) (((hiByte << 16) + ((in.readByte() & 255) << 8)) + (in.readByte() & 255)))) - 4575744000L;
    }
}
