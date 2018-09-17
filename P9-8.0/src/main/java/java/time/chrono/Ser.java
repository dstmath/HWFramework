package java.time.chrono;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

final class Ser implements Externalizable {
    static final byte CHRONO_LOCAL_DATE_TIME_TYPE = (byte) 2;
    static final byte CHRONO_PERIOD_TYPE = (byte) 9;
    static final byte CHRONO_TYPE = (byte) 1;
    static final byte CHRONO_ZONE_DATE_TIME_TYPE = (byte) 3;
    static final byte HIJRAH_DATE_TYPE = (byte) 6;
    static final byte JAPANESE_DATE_TYPE = (byte) 4;
    static final byte JAPANESE_ERA_TYPE = (byte) 5;
    static final byte MINGUO_DATE_TYPE = (byte) 7;
    static final byte THAIBUDDHIST_DATE_TYPE = (byte) 8;
    private static final long serialVersionUID = -6103370247208168577L;
    private Object object;
    private byte type;

    Ser(byte type, Object object) {
        this.type = type;
        this.object = object;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        writeInternal(this.type, this.object, out);
    }

    private static void writeInternal(byte type, Object object, ObjectOutput out) throws IOException {
        out.writeByte(type);
        switch (type) {
            case (byte) 1:
                ((AbstractChronology) object).writeExternal(out);
                return;
            case (byte) 2:
                ((ChronoLocalDateTimeImpl) object).writeExternal(out);
                return;
            case (byte) 3:
                ((ChronoZonedDateTimeImpl) object).writeExternal(out);
                return;
            case (byte) 4:
                ((JapaneseDate) object).writeExternal(out);
                return;
            case (byte) 5:
                ((JapaneseEra) object).writeExternal(out);
                return;
            case (byte) 6:
                ((HijrahDate) object).writeExternal(out);
                return;
            case (byte) 7:
                ((MinguoDate) object).writeExternal(out);
                return;
            case (byte) 8:
                ((ThaiBuddhistDate) object).writeExternal(out);
                return;
            case (byte) 9:
                ((ChronoPeriodImpl) object).writeExternal(out);
                return;
            default:
                throw new InvalidClassException("Unknown serialized type");
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.type = in.readByte();
        this.object = readInternal(this.type, in);
    }

    static Object read(ObjectInput in) throws IOException, ClassNotFoundException {
        return readInternal(in.readByte(), in);
    }

    private static Object readInternal(byte type, ObjectInput in) throws IOException, ClassNotFoundException {
        switch (type) {
            case (byte) 1:
                return AbstractChronology.readExternal(in);
            case (byte) 2:
                return ChronoLocalDateTimeImpl.readExternal(in);
            case (byte) 3:
                return ChronoZonedDateTimeImpl.readExternal(in);
            case (byte) 4:
                return JapaneseDate.readExternal(in);
            case (byte) 5:
                return JapaneseEra.readExternal(in);
            case (byte) 6:
                return HijrahDate.readExternal(in);
            case (byte) 7:
                return MinguoDate.readExternal(in);
            case (byte) 8:
                return ThaiBuddhistDate.readExternal(in);
            case (byte) 9:
                return ChronoPeriodImpl.readExternal(in);
            default:
                throw new StreamCorruptedException("Unknown serialized type");
        }
    }

    private Object readResolve() {
        return this.object;
    }
}
