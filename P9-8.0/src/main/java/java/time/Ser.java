package java.time;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

final class Ser implements Externalizable {
    static final byte DURATION_TYPE = (byte) 1;
    static final byte INSTANT_TYPE = (byte) 2;
    static final byte LOCAL_DATE_TIME_TYPE = (byte) 5;
    static final byte LOCAL_DATE_TYPE = (byte) 3;
    static final byte LOCAL_TIME_TYPE = (byte) 4;
    static final byte MONTH_DAY_TYPE = (byte) 13;
    static final byte OFFSET_DATE_TIME_TYPE = (byte) 10;
    static final byte OFFSET_TIME_TYPE = (byte) 9;
    static final byte PERIOD_TYPE = (byte) 14;
    static final byte YEAR_MONTH_TYPE = (byte) 12;
    static final byte YEAR_TYPE = (byte) 11;
    static final byte ZONE_DATE_TIME_TYPE = (byte) 6;
    static final byte ZONE_OFFSET_TYPE = (byte) 8;
    static final byte ZONE_REGION_TYPE = (byte) 7;
    private static final long serialVersionUID = -7683839454370182990L;
    private Object object;
    private byte type;

    Ser(byte type, Object object) {
        this.type = type;
        this.object = object;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        writeInternal(this.type, this.object, out);
    }

    static void writeInternal(byte type, Object object, ObjectOutput out) throws IOException {
        out.writeByte(type);
        switch (type) {
            case (byte) 1:
                ((Duration) object).writeExternal(out);
                return;
            case (byte) 2:
                ((Instant) object).writeExternal(out);
                return;
            case (byte) 3:
                ((LocalDate) object).writeExternal(out);
                return;
            case (byte) 4:
                ((LocalTime) object).writeExternal(out);
                return;
            case (byte) 5:
                ((LocalDateTime) object).writeExternal(out);
                return;
            case (byte) 6:
                ((ZonedDateTime) object).writeExternal(out);
                return;
            case (byte) 7:
                ((ZoneRegion) object).writeExternal(out);
                return;
            case (byte) 8:
                ((ZoneOffset) object).writeExternal(out);
                return;
            case (byte) 9:
                ((OffsetTime) object).writeExternal(out);
                return;
            case (byte) 10:
                ((OffsetDateTime) object).writeExternal(out);
                return;
            case (byte) 11:
                ((Year) object).writeExternal(out);
                return;
            case (byte) 12:
                ((YearMonth) object).writeExternal(out);
                return;
            case (byte) 13:
                ((MonthDay) object).writeExternal(out);
                return;
            case (byte) 14:
                ((Period) object).writeExternal(out);
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
                return Duration.readExternal(in);
            case (byte) 2:
                return Instant.readExternal(in);
            case (byte) 3:
                return LocalDate.readExternal(in);
            case (byte) 4:
                return LocalTime.readExternal(in);
            case (byte) 5:
                return LocalDateTime.readExternal(in);
            case (byte) 6:
                return ZonedDateTime.readExternal(in);
            case (byte) 7:
                return ZoneRegion.readExternal(in);
            case (byte) 8:
                return ZoneOffset.readExternal(in);
            case (byte) 9:
                return OffsetTime.readExternal(in);
            case (byte) 10:
                return OffsetDateTime.readExternal(in);
            case (byte) 11:
                return Year.readExternal(in);
            case (byte) 12:
                return YearMonth.readExternal(in);
            case (byte) 13:
                return MonthDay.readExternal(in);
            case (byte) 14:
                return Period.readExternal(in);
            default:
                throw new StreamCorruptedException("Unknown serialized type");
        }
    }

    private Object readResolve() {
        return this.object;
    }
}
