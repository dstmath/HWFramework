package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Types;
import java.util.Date;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;

class DerInputBuffer extends ByteArrayInputStream implements Cloneable {
    DerInputBuffer(byte[] buf) {
        super(buf);
    }

    DerInputBuffer(byte[] buf, int offset, int len) {
        super(buf, offset, len);
    }

    /* access modifiers changed from: package-private */
    public DerInputBuffer dup() {
        try {
            DerInputBuffer retval = (DerInputBuffer) clone();
            retval.mark(Integer.MAX_VALUE);
            return retval;
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] toByteArray() {
        int len = available();
        if (len <= 0) {
            return null;
        }
        byte[] retval = new byte[len];
        System.arraycopy(this.buf, this.pos, retval, 0, len);
        return retval;
    }

    /* access modifiers changed from: package-private */
    public int getPos() {
        return this.pos;
    }

    /* access modifiers changed from: package-private */
    public byte[] getSlice(int startPos, int size) {
        byte[] result = new byte[size];
        System.arraycopy(this.buf, startPos, result, 0, size);
        return result;
    }

    /* access modifiers changed from: package-private */
    public int peek() throws IOException {
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

    /* access modifiers changed from: package-private */
    public boolean equals(DerInputBuffer other) {
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

    /* access modifiers changed from: package-private */
    public void truncate(int len) throws IOException {
        if (len <= available()) {
            this.count = this.pos + len;
            return;
        }
        throw new IOException("insufficient data");
    }

    /* access modifiers changed from: package-private */
    public BigInteger getBigInteger(int len, boolean makePositive) throws IOException {
        if (len > available()) {
            throw new IOException("short read of integer");
        } else if (len != 0) {
            byte[] bytes = new byte[len];
            System.arraycopy(this.buf, this.pos, bytes, 0, len);
            skip((long) len);
            if (len >= 2 && bytes[0] == 0 && bytes[1] >= 0) {
                throw new IOException("Invalid encoding: redundant leading 0s");
            } else if (makePositive) {
                return new BigInteger(1, bytes);
            } else {
                return new BigInteger(bytes);
            }
        } else {
            throw new IOException("Invalid encoding: zero length Int value");
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
        } else if (len != 0) {
            byte numOfPadBits = this.buf[this.pos];
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
        } else {
            throw new IOException("Invalid encoding: zero length bit string");
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] getBitString() throws IOException {
        return getBitString(available());
    }

    /* access modifiers changed from: package-private */
    public BitArray getUnalignedBitString() throws IOException {
        if (this.pos >= this.count) {
            return null;
        }
        int len = available();
        int unusedBits = this.buf[this.pos] & 255;
        if (unusedBits <= 7) {
            byte[] bits = new byte[(len - 1)];
            int length = bits.length == 0 ? 0 : (bits.length * 8) - unusedBits;
            System.arraycopy(this.buf, this.pos + 1, bits, 0, len - 1);
            BitArray bitArray = new BitArray(length, bits);
            this.pos = this.count;
            return bitArray;
        }
        throw new IOException("Invalid value for unused bits: " + unusedBits);
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

    private Date getTime(int len, boolean generalized) throws IOException {
        int len2;
        int year;
        String type;
        int second;
        if (generalized) {
            type = "Generalized";
            byte[] bArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            byte[] bArr2 = this.buf;
            int i2 = this.pos;
            this.pos = i2 + 1;
            int year2 = (Character.digit((char) bArr[i], 10) * 1000) + (Character.digit((char) bArr2[i2], 10) * 100);
            byte[] bArr3 = this.buf;
            int i3 = this.pos;
            this.pos = i3 + 1;
            int year3 = year2 + (Character.digit((char) bArr3[i3], 10) * 10);
            byte[] bArr4 = this.buf;
            int i4 = this.pos;
            this.pos = i4 + 1;
            year = year3 + Character.digit((char) bArr4[i4], 10);
            len2 = len - 2;
        } else {
            type = "UTC";
            byte[] bArr5 = this.buf;
            int i5 = this.pos;
            this.pos = i5 + 1;
            byte[] bArr6 = this.buf;
            int i6 = this.pos;
            this.pos = i6 + 1;
            int year4 = (Character.digit((char) bArr5[i5], 10) * 10) + Character.digit((char) bArr6[i6], 10);
            if (year4 < 50) {
                year = year4 + Types.JAVA_OBJECT;
            } else {
                year = year4 + 1900;
            }
            len2 = len;
        }
        byte[] bArr7 = this.buf;
        int i7 = this.pos;
        this.pos = i7 + 1;
        byte[] bArr8 = this.buf;
        int i8 = this.pos;
        this.pos = i8 + 1;
        int month = (Character.digit((char) bArr7[i7], 10) * 10) + Character.digit((char) bArr8[i8], 10);
        byte[] bArr9 = this.buf;
        int i9 = this.pos;
        this.pos = i9 + 1;
        byte[] bArr10 = this.buf;
        int i10 = this.pos;
        this.pos = i10 + 1;
        int day = (Character.digit((char) bArr9[i9], 10) * 10) + Character.digit((char) bArr10[i10], 10);
        byte[] bArr11 = this.buf;
        int i11 = this.pos;
        this.pos = i11 + 1;
        byte[] bArr12 = this.buf;
        int i12 = this.pos;
        this.pos = i12 + 1;
        int hour = (Character.digit((char) bArr11[i11], 10) * 10) + Character.digit((char) bArr12[i12], 10);
        byte[] bArr13 = this.buf;
        int i13 = this.pos;
        this.pos = i13 + 1;
        byte[] bArr14 = this.buf;
        int i14 = this.pos;
        this.pos = i14 + 1;
        int minute = (Character.digit((char) bArr13[i13], 10) * 10) + Character.digit((char) bArr14[i14], 10);
        int len3 = len2 - 10;
        int millis = 0;
        if (len3 <= 2 || len3 >= 12) {
            second = 0;
        } else {
            byte[] bArr15 = this.buf;
            int i15 = this.pos;
            this.pos = i15 + 1;
            byte[] bArr16 = this.buf;
            int i16 = this.pos;
            this.pos = i16 + 1;
            second = (Character.digit((char) bArr15[i15], 10) * 10) + Character.digit((char) bArr16[i16], 10);
            len3 -= 2;
            if (this.buf[this.pos] == 46 || this.buf[this.pos] == 44) {
                int len4 = len3 - 1;
                this.pos++;
                int precision = 0;
                int peek = this.pos;
                while (this.buf[peek] != 90 && this.buf[peek] != 43 && this.buf[peek] != 45) {
                    peek++;
                    precision++;
                }
                switch (precision) {
                    case 1:
                        byte[] bArr17 = this.buf;
                        int i17 = this.pos;
                        this.pos = i17 + 1;
                        millis = 0 + (Character.digit((char) bArr17[i17], 10) * 100);
                        break;
                    case 2:
                        byte[] bArr18 = this.buf;
                        int i18 = this.pos;
                        this.pos = i18 + 1;
                        int millis2 = 0 + (Character.digit((char) bArr18[i18], 10) * 100);
                        byte[] bArr19 = this.buf;
                        int i19 = this.pos;
                        this.pos = i19 + 1;
                        millis = millis2 + (Character.digit((char) bArr19[i19], 10) * 10);
                        break;
                    case 3:
                        byte[] bArr20 = this.buf;
                        int i20 = this.pos;
                        this.pos = i20 + 1;
                        int millis3 = 0 + (Character.digit((char) bArr20[i20], 10) * 100);
                        byte[] bArr21 = this.buf;
                        int i21 = this.pos;
                        this.pos = i21 + 1;
                        int millis4 = millis3 + (Character.digit((char) bArr21[i21], 10) * 10);
                        byte[] bArr22 = this.buf;
                        int i22 = this.pos;
                        this.pos = i22 + 1;
                        millis = millis4 + Character.digit((char) bArr22[i22], 10);
                        break;
                    default:
                        throw new IOException("Parse " + type + " time, unsupported precision for seconds value");
                }
                len3 = len4 - precision;
            }
        }
        int second2 = second;
        if (month == 0 || day == 0 || month > 12 || day > 31 || hour >= 24 || minute >= 60 || second2 >= 60) {
            throw new IOException("Parse " + type + " time, invalid format");
        }
        CalendarSystem gcal = CalendarSystem.getGregorianCalendar();
        CalendarDate date = gcal.newCalendarDate(null);
        date.setDate(year, month, day);
        date.setTimeOfDay(hour, minute, second2, millis);
        long time = gcal.getTime(date);
        if (len3 == 1 || len3 == 5) {
            byte[] bArr23 = this.buf;
            int i23 = this.pos;
            this.pos = i23 + 1;
            byte b = bArr23[i23];
            if (b == 43) {
                byte[] bArr24 = this.buf;
                int i24 = this.pos;
                this.pos = i24 + 1;
                int hr = 10 * Character.digit((char) bArr24[i24], 10);
                byte[] bArr25 = this.buf;
                int i25 = this.pos;
                this.pos = i25 + 1;
                int hr2 = hr + Character.digit((char) bArr25[i25], 10);
                byte[] bArr26 = this.buf;
                int i26 = this.pos;
                this.pos = i26 + 1;
                byte[] bArr27 = this.buf;
                int i27 = this.pos;
                this.pos = i27 + 1;
                int min = (Character.digit((char) bArr26[i26], 10) * 10) + Character.digit((char) bArr27[i27], 10);
                if (hr2 >= 24 || min >= 60) {
                    int i28 = min;
                    throw new IOException("Parse " + type + " time, +hhmm");
                }
                int i29 = min;
                time -= (long) ((((hr2 * 60) + min) * 60) * 1000);
            } else if (b == 45) {
                byte[] bArr28 = this.buf;
                int i30 = this.pos;
                this.pos = i30 + 1;
                byte[] bArr29 = this.buf;
                int i31 = this.pos;
                int i32 = second2;
                this.pos = i31 + 1;
                int hr3 = (Character.digit((char) bArr28[i30], 10) * 10) + Character.digit((char) bArr29[i31], 10);
                byte[] bArr30 = this.buf;
                int i33 = this.pos;
                this.pos = i33 + 1;
                byte[] bArr31 = this.buf;
                int i34 = this.pos;
                this.pos = i34 + 1;
                int min2 = (Character.digit((char) bArr30[i33], 10) * 10) + Character.digit((char) bArr31[i34], 10);
                if (hr3 >= 24 || min2 >= 60) {
                    throw new IOException("Parse " + type + " time, -hhmm");
                }
                time += (long) (((hr3 * 60) + min2) * 60 * 1000);
            } else if (b == 90) {
                int i35 = second2;
            } else {
                throw new IOException("Parse " + type + " time, garbage offset");
            }
            return new Date(time);
        }
        throw new IOException("Parse " + type + " time, invalid offset");
    }
}
