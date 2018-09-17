package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Types;
import java.util.Date;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.logging.PlatformLogger;

class DerInputBuffer extends ByteArrayInputStream implements Cloneable {
    DerInputBuffer(byte[] buf) {
        super(buf);
    }

    DerInputBuffer(byte[] buf, int offset, int len) {
        super(buf, offset, len);
    }

    DerInputBuffer dup() {
        try {
            DerInputBuffer retval = (DerInputBuffer) clone();
            retval.mark(PlatformLogger.OFF);
            return retval;
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    byte[] toByteArray() {
        int len = available();
        if (len <= 0) {
            return null;
        }
        byte[] retval = new byte[len];
        System.arraycopy(this.buf, this.pos, retval, 0, len);
        return retval;
    }

    int getPos() {
        return this.pos;
    }

    byte[] getSlice(int startPos, int size) {
        byte[] result = new byte[size];
        System.arraycopy(this.buf, startPos, result, 0, size);
        return result;
    }

    int peek() throws IOException {
        if (this.pos < this.count) {
            return this.buf[this.pos];
        }
        throw new IOException("out of data");
    }

    public boolean equals(Object other) {
        if (other instanceof DerInputBuffer) {
            return equals((DerInputBuffer) other);
        }
        return false;
    }

    boolean equals(DerInputBuffer other) {
        if (this == other) {
            return true;
        }
        int max = available();
        if (other.available() != max) {
            return false;
        }
        for (int i = 0; i < max; i++) {
            if (this.buf[this.pos + i] != other.buf[other.pos + i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int retval = 0;
        int len = available();
        int p = this.pos;
        for (int i = 0; i < len; i++) {
            retval += this.buf[p + i] * i;
        }
        return retval;
    }

    void truncate(int len) throws IOException {
        if (len > available()) {
            throw new IOException("insufficient data");
        }
        this.count = this.pos + len;
    }

    BigInteger getBigInteger(int len, boolean makePositive) throws IOException {
        if (len > available()) {
            throw new IOException("short read of integer");
        } else if (len == 0) {
            throw new IOException("Invalid encoding: zero length Int value");
        } else {
            byte[] bytes = new byte[len];
            System.arraycopy(this.buf, this.pos, bytes, 0, len);
            skip((long) len);
            if (makePositive) {
                return new BigInteger(1, bytes);
            }
            return new BigInteger(bytes);
        }
    }

    public int getInteger(int len) throws IOException {
        BigInteger result = getBigInteger(len, false);
        if (result.compareTo(BigInteger.valueOf(-2147483648L)) < 0) {
            throw new IOException("Integer below minimum valid value");
        } else if (result.compareTo(BigInteger.valueOf(2147483647L)) <= 0) {
            return result.intValue();
        } else {
            throw new IOException("Integer exceeds maximum valid value");
        }
    }

    public byte[] getBitString(int len) throws IOException {
        if (len > available()) {
            throw new IOException("short read of bit string");
        } else if (len == 0) {
            throw new IOException("Invalid encoding: zero length bit string");
        } else {
            int numOfPadBits = this.buf[this.pos];
            if (numOfPadBits < 0 || numOfPadBits > 7) {
                throw new IOException("Invalid number of padding bits");
            }
            byte[] retval = new byte[(len - 1)];
            System.arraycopy(this.buf, this.pos + 1, retval, 0, len - 1);
            if (numOfPadBits != 0) {
                int i = len - 2;
                retval[i] = (byte) (retval[i] & (255 << numOfPadBits));
            }
            skip((long) len);
            return retval;
        }
    }

    byte[] getBitString() throws IOException {
        return getBitString(available());
    }

    BitArray getUnalignedBitString() throws IOException {
        if (this.pos >= this.count) {
            return null;
        }
        int len = available();
        int unusedBits = this.buf[this.pos] & 255;
        if (unusedBits > 7) {
            throw new IOException("Invalid value for unused bits: " + unusedBits);
        }
        byte[] bits = new byte[(len - 1)];
        int length = bits.length == 0 ? 0 : (bits.length * 8) - unusedBits;
        System.arraycopy(this.buf, this.pos + 1, bits, 0, len - 1);
        BitArray bitArray = new BitArray(length, bits);
        this.pos = this.count;
        return bitArray;
    }

    public Date getUTCTime(int len) throws IOException {
        if (len > available()) {
            throw new IOException("short read of DER UTC Time");
        } else if (len >= 11 && len <= 17) {
            return getTime(len, false);
        } else {
            throw new IOException("DER UTC Time length error");
        }
    }

    public Date getGeneralizedTime(int len) throws IOException {
        if (len > available()) {
            throw new IOException("short read of DER Generalized Time");
        } else if (len >= 13 && len <= 23) {
            return getTime(len, true);
        } else {
            throw new IOException("DER Generalized Time length error");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Date getTime(int len, boolean generalized) throws IOException {
        String type;
        byte[] bArr;
        int i;
        int year;
        int second;
        if (generalized) {
            type = "Generalized";
            bArr = this.buf;
            i = this.pos;
            this.pos = i + 1;
            year = Character.digit((char) bArr[i], 10) * PlatformLogger.SEVERE;
            bArr = this.buf;
            i = this.pos;
            this.pos = i + 1;
            year += Character.digit((char) bArr[i], 10) * 100;
            bArr = this.buf;
            i = this.pos;
            this.pos = i + 1;
            year += Character.digit((char) bArr[i], 10) * 10;
            bArr = this.buf;
            i = this.pos;
            this.pos = i + 1;
            year += Character.digit((char) bArr[i], 10);
            len -= 2;
        } else {
            type = "UTC";
            bArr = this.buf;
            i = this.pos;
            this.pos = i + 1;
            year = Character.digit((char) bArr[i], 10) * 10;
            bArr = this.buf;
            i = this.pos;
            this.pos = i + 1;
            year += Character.digit((char) bArr[i], 10);
            if (year < 50) {
                year += Types.JAVA_OBJECT;
            } else {
                year += 1900;
            }
        }
        bArr = this.buf;
        i = this.pos;
        this.pos = i + 1;
        int month = Character.digit((char) bArr[i], 10) * 10;
        bArr = this.buf;
        i = this.pos;
        this.pos = i + 1;
        month += Character.digit((char) bArr[i], 10);
        bArr = this.buf;
        i = this.pos;
        this.pos = i + 1;
        int day = Character.digit((char) bArr[i], 10) * 10;
        bArr = this.buf;
        i = this.pos;
        this.pos = i + 1;
        day += Character.digit((char) bArr[i], 10);
        bArr = this.buf;
        i = this.pos;
        this.pos = i + 1;
        int hour = Character.digit((char) bArr[i], 10) * 10;
        bArr = this.buf;
        i = this.pos;
        this.pos = i + 1;
        hour += Character.digit((char) bArr[i], 10);
        bArr = this.buf;
        i = this.pos;
        this.pos = i + 1;
        int minute = Character.digit((char) bArr[i], 10) * 10;
        bArr = this.buf;
        i = this.pos;
        this.pos = i + 1;
        minute += Character.digit((char) bArr[i], 10);
        len -= 10;
        int millis = 0;
        if (len <= 2 || len >= 12) {
            second = 0;
        } else {
            bArr = this.buf;
            i = this.pos;
            this.pos = i + 1;
            second = Character.digit((char) bArr[i], 10) * 10;
            bArr = this.buf;
            i = this.pos;
            this.pos = i + 1;
            second += Character.digit((char) bArr[i], 10);
            len -= 2;
            if (this.buf[this.pos] != 46) {
            }
            len--;
            this.pos++;
            int precision = 0;
            int peek = this.pos;
            while (true) {
                if (this.buf[peek] == 90) {
                    break;
                }
                if (this.buf[peek] == 43) {
                    break;
                }
                if (this.buf[peek] == 45) {
                    break;
                }
                peek++;
                precision++;
            }
            switch (precision) {
                case BaseCalendar.SUNDAY /*1*/:
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    millis = (Character.digit((char) bArr[i], 10) * 100) + 0;
                    break;
                case BaseCalendar.MONDAY /*2*/:
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    millis = (Character.digit((char) bArr[i], 10) * 100) + 0;
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    millis += Character.digit((char) bArr[i], 10) * 10;
                    break;
                case BaseCalendar.TUESDAY /*3*/:
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    millis = (Character.digit((char) bArr[i], 10) * 100) + 0;
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    millis += Character.digit((char) bArr[i], 10) * 10;
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    millis += Character.digit((char) bArr[i], 10);
                    break;
                default:
                    throw new IOException("Parse " + type + " time, unsupported precision for seconds value");
            }
            len -= precision;
        }
        if (month == 0 || day == 0 || month > 12 || day > 31 || hour >= 24 || minute >= 60 || second >= 60) {
            throw new IOException("Parse " + type + " time, invalid format");
        }
        CalendarSystem gcal = CalendarSystem.getGregorianCalendar();
        CalendarDate date = gcal.newCalendarDate(null);
        date.setDate(year, month, day);
        date.setTimeOfDay(hour, minute, second, millis);
        long time = gcal.getTime(date);
        if (len == 1 || len == 5) {
            bArr = this.buf;
            i = this.pos;
            this.pos = i + 1;
            int hr;
            int min;
            switch (bArr[i]) {
                case (byte) 43:
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    hr = Character.digit((char) bArr[i], 10) * 10;
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    hr += Character.digit((char) bArr[i], 10);
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    min = Character.digit((char) bArr[i], 10) * 10;
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    min += Character.digit((char) bArr[i], 10);
                    if (hr < 24 && min < 60) {
                        time -= (long) ((((hr * 60) + min) * 60) * PlatformLogger.SEVERE);
                        break;
                    }
                    throw new IOException("Parse " + type + " time, +hhmm");
                case (byte) 45:
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    hr = Character.digit((char) bArr[i], 10) * 10;
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    hr += Character.digit((char) bArr[i], 10);
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    min = Character.digit((char) bArr[i], 10) * 10;
                    bArr = this.buf;
                    i = this.pos;
                    this.pos = i + 1;
                    min += Character.digit((char) bArr[i], 10);
                    if (hr < 24 && min < 60) {
                        time += (long) ((((hr * 60) + min) * 60) * PlatformLogger.SEVERE);
                        break;
                    }
                    throw new IOException("Parse " + type + " time, -hhmm");
                case (byte) 90:
                    break;
                default:
                    throw new IOException("Parse " + type + " time, garbage offset");
            }
            return new Date(time);
        }
        throw new IOException("Parse " + type + " time, invalid offset");
    }
}
