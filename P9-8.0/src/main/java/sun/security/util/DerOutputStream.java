package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DerOutputStream extends ByteArrayOutputStream implements DerEncoder {
    private static ByteArrayLexOrder lexOrder = new ByteArrayLexOrder();
    private static ByteArrayTagOrder tagOrder = new ByteArrayTagOrder();

    public DerOutputStream(int size) {
        super(size);
    }

    public void write(byte tag, byte[] buf) throws IOException {
        write(tag);
        putLength(buf.length);
        write(buf, 0, buf.length);
    }

    public void write(byte tag, DerOutputStream out) throws IOException {
        write(tag);
        putLength(out.count);
        write(out.buf, 0, out.count);
    }

    public void writeImplicit(byte tag, DerOutputStream value) throws IOException {
        write(tag);
        write(value.buf, 1, value.count - 1);
    }

    public void putDerValue(DerValue val) throws IOException {
        val.encode(this);
    }

    public void putBoolean(boolean val) throws IOException {
        write(1);
        putLength(1);
        if (val) {
            write(255);
        } else {
            write(0);
        }
    }

    public void putEnumerated(int i) throws IOException {
        write(10);
        putIntegerContents(i);
    }

    public void putInteger(BigInteger i) throws IOException {
        write(2);
        byte[] buf = i.toByteArray();
        putLength(buf.length);
        write(buf, 0, buf.length);
    }

    public void putInteger(Integer i) throws IOException {
        putInteger(i.lambda$-java_util_stream_IntPipeline_14709());
    }

    public void putInteger(int i) throws IOException {
        write(2);
        putIntegerContents(i);
    }

    private void putIntegerContents(int i) throws IOException {
        bytes = new byte[4];
        int start = 0;
        bytes[3] = (byte) (i & 255);
        bytes[2] = (byte) ((65280 & i) >>> 8);
        bytes[1] = (byte) ((16711680 & i) >>> 16);
        bytes[0] = (byte) ((-16777216 & i) >>> 24);
        int j;
        if (bytes[0] == (byte) -1) {
            j = 0;
            while (j < 3 && bytes[j] == (byte) -1 && (bytes[j + 1] & 128) == 128) {
                start++;
                j++;
            }
        } else if (bytes[0] == (byte) 0) {
            j = 0;
            while (j < 3 && bytes[j] == (byte) 0 && (bytes[j + 1] & 128) == 0) {
                start++;
                j++;
            }
        }
        putLength(4 - start);
        for (int k = start; k < 4; k++) {
            write(bytes[k]);
        }
    }

    public void putBitString(byte[] bits) throws IOException {
        write(3);
        putLength(bits.length + 1);
        write(0);
        write(bits);
    }

    public void putUnalignedBitString(BitArray ba) throws IOException {
        byte[] bits = ba.toByteArray();
        write(3);
        putLength(bits.length + 1);
        write((bits.length * 8) - ba.length());
        write(bits);
    }

    public void putTruncatedUnalignedBitString(BitArray ba) throws IOException {
        putUnalignedBitString(ba.truncate());
    }

    public void putOctetString(byte[] octets) throws IOException {
        write((byte) 4, octets);
    }

    public void putNull() throws IOException {
        write(5);
        putLength(0);
    }

    public void putOID(ObjectIdentifier oid) throws IOException {
        oid.encode(this);
    }

    public void putSequence(DerValue[] seq) throws IOException {
        DerOutputStream bytes = new DerOutputStream();
        for (DerValue encode : seq) {
            encode.encode(bytes);
        }
        write((byte) 48, bytes);
    }

    public void putSet(DerValue[] set) throws IOException {
        DerOutputStream bytes = new DerOutputStream();
        for (DerValue encode : set) {
            encode.encode(bytes);
        }
        write((byte) 49, bytes);
    }

    public void putOrderedSetOf(byte tag, DerEncoder[] set) throws IOException {
        putOrderedSet(tag, set, lexOrder);
    }

    public void putOrderedSet(byte tag, DerEncoder[] set) throws IOException {
        putOrderedSet(tag, set, tagOrder);
    }

    private void putOrderedSet(byte tag, DerEncoder[] set, Comparator<byte[]> order) throws IOException {
        int i;
        DerOutputStream[] streams = new DerOutputStream[set.length];
        for (i = 0; i < set.length; i++) {
            streams[i] = new DerOutputStream();
            set[i].derEncode(streams[i]);
        }
        byte[][] bufs = new byte[streams.length][];
        for (i = 0; i < streams.length; i++) {
            bufs[i] = streams[i].toByteArray();
        }
        Arrays.sort(bufs, order);
        DerOutputStream bytes = new DerOutputStream();
        for (i = 0; i < streams.length; i++) {
            bytes.write(bufs[i]);
        }
        write(tag, bytes);
    }

    public void putUTF8String(String s) throws IOException {
        writeString(s, (byte) 12, "UTF8");
    }

    public void putPrintableString(String s) throws IOException {
        writeString(s, (byte) 19, "ASCII");
    }

    public void putT61String(String s) throws IOException {
        writeString(s, (byte) 20, "ISO-8859-1");
    }

    public void putIA5String(String s) throws IOException {
        writeString(s, (byte) 22, "ASCII");
    }

    public void putBMPString(String s) throws IOException {
        writeString(s, (byte) 30, "UnicodeBigUnmarked");
    }

    public void putGeneralString(String s) throws IOException {
        writeString(s, (byte) 27, "ASCII");
    }

    private void writeString(String s, byte stringTag, String enc) throws IOException {
        byte[] data = s.getBytes(enc);
        write(stringTag);
        putLength(data.length);
        write(data);
    }

    public void putUTCTime(Date d) throws IOException {
        putTime(d, (byte) 23);
    }

    public void putGeneralizedTime(Date d) throws IOException {
        putTime(d, (byte) 24);
    }

    private void putTime(Date d, byte tag) throws IOException {
        String pattern;
        int tag2;
        TimeZone tz = TimeZone.getTimeZone("GMT");
        if (tag2 == (byte) 23) {
            pattern = "yyMMddHHmmss'Z'";
        } else {
            tag2 = 24;
            pattern = "yyyyMMddHHmmss'Z'";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        sdf.setTimeZone(tz);
        byte[] time = sdf.format(d).getBytes("ISO-8859-1");
        write(tag2);
        putLength(time.length);
        write(time);
    }

    public void putLength(int len) throws IOException {
        if (len < 128) {
            write((byte) len);
        } else if (len < 256) {
            write(-127);
            write((byte) len);
        } else if (len < 65536) {
            write(-126);
            write((byte) (len >> 8));
            write((byte) len);
        } else if (len < 16777216) {
            write(-125);
            write((byte) (len >> 16));
            write((byte) (len >> 8));
            write((byte) len);
        } else {
            write(-124);
            write((byte) (len >> 24));
            write((byte) (len >> 16));
            write((byte) (len >> 8));
            write((byte) len);
        }
    }

    public void putTag(byte tagClass, boolean form, byte val) {
        byte tag = (byte) (tagClass | val);
        if (form) {
            tag = (byte) (tag | 32);
        }
        write(tag);
    }

    public void derEncode(OutputStream out) throws IOException {
        out.write(toByteArray());
    }
}
