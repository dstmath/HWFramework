package jcifs.util;

import java.io.IOException;
import java.util.Date;

public class Encdec {
    public static final long MILLISECONDS_BETWEEN_1970_AND_1601 = 11644473600000L;
    public static final long SEC_BETWEEEN_1904_AND_1970 = 2082844800;
    public static final int TIME_1601_NANOS_64BE = 6;
    public static final int TIME_1601_NANOS_64LE = 5;
    public static final int TIME_1904_SEC_32BE = 3;
    public static final int TIME_1904_SEC_32LE = 4;
    public static final int TIME_1970_MILLIS_64BE = 7;
    public static final int TIME_1970_MILLIS_64LE = 8;
    public static final int TIME_1970_SEC_32BE = 1;
    public static final int TIME_1970_SEC_32LE = 2;

    public static int enc_uint16be(short s, byte[] dst, int di) {
        int di2 = di + TIME_1970_SEC_32BE;
        dst[di] = (byte) ((s >> TIME_1970_MILLIS_64LE) & 255);
        dst[di2] = (byte) (s & 255);
        return TIME_1970_SEC_32LE;
    }

    public static int enc_uint32be(int i, byte[] dst, int di) {
        int i2 = di + TIME_1970_SEC_32BE;
        dst[di] = (byte) ((i >> 24) & 255);
        di = i2 + TIME_1970_SEC_32BE;
        dst[i2] = (byte) ((i >> 16) & 255);
        i2 = di + TIME_1970_SEC_32BE;
        dst[di] = (byte) ((i >> TIME_1970_MILLIS_64LE) & 255);
        dst[i2] = (byte) (i & 255);
        return TIME_1904_SEC_32LE;
    }

    public static int enc_uint16le(short s, byte[] dst, int di) {
        int di2 = di + TIME_1970_SEC_32BE;
        dst[di] = (byte) (s & 255);
        dst[di2] = (byte) ((s >> TIME_1970_MILLIS_64LE) & 255);
        return TIME_1970_SEC_32LE;
    }

    public static int enc_uint32le(int i, byte[] dst, int di) {
        int i2 = di + TIME_1970_SEC_32BE;
        dst[di] = (byte) (i & 255);
        di = i2 + TIME_1970_SEC_32BE;
        dst[i2] = (byte) ((i >> TIME_1970_MILLIS_64LE) & 255);
        i2 = di + TIME_1970_SEC_32BE;
        dst[di] = (byte) ((i >> 16) & 255);
        dst[i2] = (byte) ((i >> 24) & 255);
        return TIME_1904_SEC_32LE;
    }

    public static short dec_uint16be(byte[] src, int si) {
        return (short) (((src[si] & 255) << TIME_1970_MILLIS_64LE) | (src[si + TIME_1970_SEC_32BE] & 255));
    }

    public static int dec_uint32be(byte[] src, int si) {
        return ((((src[si] & 255) << 24) | ((src[si + TIME_1970_SEC_32BE] & 255) << 16)) | ((src[si + TIME_1970_SEC_32LE] & 255) << TIME_1970_MILLIS_64LE)) | (src[si + TIME_1904_SEC_32BE] & 255);
    }

    public static short dec_uint16le(byte[] src, int si) {
        return (short) ((src[si] & 255) | ((src[si + TIME_1970_SEC_32BE] & 255) << TIME_1970_MILLIS_64LE));
    }

    public static int dec_uint32le(byte[] src, int si) {
        return (((src[si] & 255) | ((src[si + TIME_1970_SEC_32BE] & 255) << TIME_1970_MILLIS_64LE)) | ((src[si + TIME_1970_SEC_32LE] & 255) << 16)) | ((src[si + TIME_1904_SEC_32BE] & 255) << 24);
    }

    public static int enc_uint64be(long l, byte[] dst, int di) {
        enc_uint32be((int) (l & 4294967295L), dst, di + TIME_1904_SEC_32LE);
        enc_uint32be((int) ((l >> 32) & 4294967295L), dst, di);
        return TIME_1970_MILLIS_64LE;
    }

    public static int enc_uint64le(long l, byte[] dst, int di) {
        enc_uint32le((int) (l & 4294967295L), dst, di);
        enc_uint32le((int) ((l >> 32) & 4294967295L), dst, di + TIME_1904_SEC_32LE);
        return TIME_1970_MILLIS_64LE;
    }

    public static long dec_uint64be(byte[] src, int si) {
        return ((((long) dec_uint32be(src, si)) & 4294967295L) << 32) | (((long) dec_uint32be(src, si + TIME_1904_SEC_32LE)) & 4294967295L);
    }

    public static long dec_uint64le(byte[] src, int si) {
        return ((((long) dec_uint32le(src, si + TIME_1904_SEC_32LE)) & 4294967295L) << 32) | (((long) dec_uint32le(src, si)) & 4294967295L);
    }

    public static int enc_floatle(float f, byte[] dst, int di) {
        return enc_uint32le(Float.floatToIntBits(f), dst, di);
    }

    public static int enc_floatbe(float f, byte[] dst, int di) {
        return enc_uint32be(Float.floatToIntBits(f), dst, di);
    }

    public static float dec_floatle(byte[] src, int si) {
        return Float.intBitsToFloat(dec_uint32le(src, si));
    }

    public static float dec_floatbe(byte[] src, int si) {
        return Float.intBitsToFloat(dec_uint32be(src, si));
    }

    public static int enc_doublele(double d, byte[] dst, int di) {
        return enc_uint64le(Double.doubleToLongBits(d), dst, di);
    }

    public static int enc_doublebe(double d, byte[] dst, int di) {
        return enc_uint64be(Double.doubleToLongBits(d), dst, di);
    }

    public static double dec_doublele(byte[] src, int si) {
        return Double.longBitsToDouble(dec_uint64le(src, si));
    }

    public static double dec_doublebe(byte[] src, int si) {
        return Double.longBitsToDouble(dec_uint64be(src, si));
    }

    public static int enc_time(Date date, byte[] dst, int di, int enc) {
        switch (enc) {
            case TIME_1970_SEC_32BE /*1*/:
                return enc_uint32be((int) (date.getTime() / 1000), dst, di);
            case TIME_1970_SEC_32LE /*2*/:
                return enc_uint32le((int) (date.getTime() / 1000), dst, di);
            case TIME_1904_SEC_32BE /*3*/:
                return enc_uint32be((int) (((date.getTime() / 1000) + SEC_BETWEEEN_1904_AND_1970) & -1), dst, di);
            case TIME_1904_SEC_32LE /*4*/:
                return enc_uint32le((int) (((date.getTime() / 1000) + SEC_BETWEEEN_1904_AND_1970) & -1), dst, di);
            case TIME_1601_NANOS_64LE /*5*/:
                return enc_uint64le((date.getTime() + MILLISECONDS_BETWEEN_1970_AND_1601) * 10000, dst, di);
            case TIME_1601_NANOS_64BE /*6*/:
                return enc_uint64be((date.getTime() + MILLISECONDS_BETWEEN_1970_AND_1601) * 10000, dst, di);
            case TIME_1970_MILLIS_64BE /*7*/:
                return enc_uint64be(date.getTime(), dst, di);
            case TIME_1970_MILLIS_64LE /*8*/:
                return enc_uint64le(date.getTime(), dst, di);
            default:
                throw new IllegalArgumentException("Unsupported time encoding");
        }
    }

    public static Date dec_time(byte[] src, int si, int enc) {
        switch (enc) {
            case TIME_1970_SEC_32BE /*1*/:
                return new Date(((long) dec_uint32be(src, si)) * 1000);
            case TIME_1970_SEC_32LE /*2*/:
                return new Date(((long) dec_uint32le(src, si)) * 1000);
            case TIME_1904_SEC_32BE /*3*/:
                return new Date(((((long) dec_uint32be(src, si)) & 4294967295L) - SEC_BETWEEEN_1904_AND_1970) * 1000);
            case TIME_1904_SEC_32LE /*4*/:
                return new Date(((((long) dec_uint32le(src, si)) & 4294967295L) - SEC_BETWEEEN_1904_AND_1970) * 1000);
            case TIME_1601_NANOS_64LE /*5*/:
                return new Date((dec_uint64le(src, si) / 10000) - MILLISECONDS_BETWEEN_1970_AND_1601);
            case TIME_1601_NANOS_64BE /*6*/:
                return new Date((dec_uint64be(src, si) / 10000) - MILLISECONDS_BETWEEN_1970_AND_1601);
            case TIME_1970_MILLIS_64BE /*7*/:
                return new Date(dec_uint64be(src, si));
            case TIME_1970_MILLIS_64LE /*8*/:
                return new Date(dec_uint64le(src, si));
            default:
                throw new IllegalArgumentException("Unsupported time encoding");
        }
    }

    public static int enc_utf8(String str, byte[] dst, int di, int dlim) throws IOException {
        int start = di;
        int strlen = str.length();
        int i = 0;
        int i2 = di;
        while (i2 < dlim && i < strlen) {
            int ch = str.charAt(i);
            if (ch < TIME_1970_SEC_32BE || ch > 127) {
                if (ch <= 2047) {
                    if (dlim - i2 < TIME_1970_SEC_32LE) {
                        break;
                    }
                    di = i2 + TIME_1970_SEC_32BE;
                    dst[i2] = (byte) (((ch >> TIME_1601_NANOS_64BE) & 31) | 192);
                    i2 = di + TIME_1970_SEC_32BE;
                    dst[di] = (byte) (((ch >> 0) & 63) | SmbConstants.FLAGS_RESPONSE);
                    di = i2;
                } else if (dlim - i2 < TIME_1904_SEC_32BE) {
                    break;
                } else {
                    di = i2 + TIME_1970_SEC_32BE;
                    dst[i2] = (byte) (((ch >> 12) & 15) | 224);
                    i2 = di + TIME_1970_SEC_32BE;
                    dst[di] = (byte) (((ch >> TIME_1601_NANOS_64BE) & 63) | SmbConstants.FLAGS_RESPONSE);
                    di = i2 + TIME_1970_SEC_32BE;
                    dst[i2] = (byte) (((ch >> 0) & 63) | SmbConstants.FLAGS_RESPONSE);
                }
            } else {
                di = i2 + TIME_1970_SEC_32BE;
                dst[i2] = (byte) ch;
            }
            i += TIME_1970_SEC_32BE;
            i2 = di;
        }
        return i2 - start;
    }

    public static String dec_utf8(byte[] src, int si, int slim) throws IOException {
        char[] uni = new char[(slim - si)];
        int ui = 0;
        int si2 = si;
        while (si2 < slim) {
            si = si2 + TIME_1970_SEC_32BE;
            int ch = src[si2] & 255;
            if (ch == 0) {
                break;
            }
            if (ch < SmbConstants.FLAGS_RESPONSE) {
                uni[ui] = (char) ch;
            } else if ((ch & 224) == 192) {
                if (slim - si < TIME_1970_SEC_32LE) {
                    break;
                }
                uni[ui] = (char) ((ch & 31) << TIME_1601_NANOS_64BE);
                si2 = si + TIME_1970_SEC_32BE;
                ch = src[si] & 255;
                uni[ui] = (char) (uni[ui] | (ch & 63));
                if ((ch & 192) != SmbConstants.FLAGS_RESPONSE || uni[ui] < '\u0080') {
                    throw new IOException("Invalid UTF-8 sequence");
                }
                si = si2;
            } else if ((ch & 240) == 224) {
                if (slim - si < TIME_1904_SEC_32BE) {
                    break;
                }
                uni[ui] = (char) ((ch & 15) << 12);
                si2 = si + TIME_1970_SEC_32BE;
                ch = src[si] & 255;
                if ((ch & 192) != SmbConstants.FLAGS_RESPONSE) {
                    throw new IOException("Invalid UTF-8 sequence");
                }
                uni[ui] = (char) (uni[ui] | ((ch & 63) << TIME_1601_NANOS_64BE));
                si = si2 + TIME_1970_SEC_32BE;
                ch = src[si2] & 255;
                uni[ui] = (char) (uni[ui] | (ch & 63));
                if ((ch & 192) != SmbConstants.FLAGS_RESPONSE || uni[ui] < '\u0800') {
                    throw new IOException("Invalid UTF-8 sequence");
                }
            } else {
                throw new IOException("Unsupported UTF-8 sequence");
            }
            ui += TIME_1970_SEC_32BE;
            si2 = si;
        }
        return new String(uni, 0, ui);
    }

    public static String dec_ucs2le(byte[] src, int si, int slim, char[] buf) throws IOException {
        int bi = 0;
        while (si + TIME_1970_SEC_32BE < slim) {
            buf[bi] = (char) dec_uint16le(src, si);
            if (buf[bi] == '\u0000') {
                break;
            }
            bi += TIME_1970_SEC_32BE;
            si += TIME_1970_SEC_32LE;
        }
        return new String(buf, 0, bi);
    }
}
