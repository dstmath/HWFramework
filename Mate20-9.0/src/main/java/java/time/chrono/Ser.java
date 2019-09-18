package java.time.chrono;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

final class Ser implements Externalizable {
    static final byte CHRONO_LOCAL_DATE_TIME_TYPE = 2;
    static final byte CHRONO_PERIOD_TYPE = 9;
    static final byte CHRONO_TYPE = 1;
    static final byte CHRONO_ZONE_DATE_TIME_TYPE = 3;
    static final byte HIJRAH_DATE_TYPE = 6;
    static final byte JAPANESE_DATE_TYPE = 4;
    static final byte JAPANESE_ERA_TYPE = 5;
    static final byte MINGUO_DATE_TYPE = 7;
    static final byte THAIBUDDHIST_DATE_TYPE = 8;
    private static final long serialVersionUID = -6103370247208168577L;
    private Object object;
    private byte type;

    public Ser() {
    }

    Ser(byte type2, Object object2) {
        this.type = type2;
        this.object = object2;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        writeInternal(this.type, this.object, out);
    }

    private static void writeInternal(byte type2, Object object2, ObjectOutput out) throws IOException {
        out.writeByte(type2);
        switch (type2) {
            case 1:
                ((AbstractChronology) object2).writeExternal(out);
                return;
            case 2:
                ((ChronoLocalDateTimeImpl) object2).writeExternal(out);
                return;
            case 3:
                ((ChronoZonedDateTimeImpl) object2).writeExternal(out);
                return;
            case 4:
                ((JapaneseDate) object2).writeExternal(out);
                return;
            case 5:
                ((JapaneseEra) object2).writeExternal(out);
                return;
            case 6:
                ((HijrahDate) object2).writeExternal(out);
                return;
            case 7:
                ((MinguoDate) object2).writeExternal(out);
                return;
            case 8:
                ((ThaiBuddhistDate) object2).writeExternal(out);
                return;
            case 9:
                ((ChronoPeriodImpl) object2).writeExternal(out);
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

    private static Object readInternal(byte type2, ObjectInput in) throws IOException, ClassNotFoundException {
        switch (type2) {
            case 1:
                return AbstractChronology.readExternal(in);
            case 2:
                return ChronoLocalDateTimeImpl.readExternal(in);
            case 3:
                return ChronoZonedDateTimeImpl.readExternal(in);
            case 4:
                return JapaneseDate.readExternal(in);
            case 5:
                return JapaneseEra.readExternal(in);
            case 6:
                return HijrahDate.readExternal(in);
            case 7:
                return MinguoDate.readExternal(in);
            case 8:
                return ThaiBuddhistDate.readExternal(in);
            case 9:
                return ChronoPeriodImpl.readExternal(in);
            default:
                throw new StreamCorruptedException("Unknown serialized type");
        }
    }

    private Object readResolve() {
        return this.object;
    }
}
