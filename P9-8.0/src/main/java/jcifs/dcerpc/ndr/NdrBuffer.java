package jcifs.dcerpc.ndr;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import jcifs.util.Encdec;

public class NdrBuffer {
    public byte[] buf;
    public NdrBuffer deferred = this;
    public int index;
    public int length = 0;
    int referent;
    HashMap referents;
    public int start;

    static class Entry {
        Object obj;
        int referent;

        Entry() {
        }
    }

    public NdrBuffer(byte[] buf, int start) {
        this.buf = buf;
        this.index = start;
        this.start = start;
    }

    public NdrBuffer derive(int idx) {
        NdrBuffer nb = new NdrBuffer(this.buf, this.start);
        nb.index = idx;
        nb.deferred = this.deferred;
        return nb;
    }

    public void reset() {
        this.index = this.start;
        this.length = 0;
        this.deferred = this;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getCapacity() {
        return this.buf.length - this.start;
    }

    public int getTailSpace() {
        return this.buf.length - this.index;
    }

    public byte[] getBuffer() {
        return this.buf;
    }

    public int align(int boundary, byte value) {
        int n = align(boundary);
        for (int i = n; i > 0; i--) {
            this.buf[this.index - i] = value;
        }
        return n;
    }

    public void writeOctetArray(byte[] b, int i, int l) {
        System.arraycopy(b, i, this.buf, this.index, l);
        advance(l);
    }

    public void readOctetArray(byte[] b, int i, int l) {
        System.arraycopy(this.buf, this.index, b, i, l);
        advance(l);
    }

    public int getLength() {
        return this.deferred.length;
    }

    public void setLength(int length) {
        this.deferred.length = length;
    }

    public void advance(int n) {
        this.index += n;
        if (this.index - this.start > this.deferred.length) {
            this.deferred.length = this.index - this.start;
        }
    }

    public int align(int boundary) {
        int m = boundary - 1;
        int i = this.index - this.start;
        int n = ((i + m) & (m ^ -1)) - i;
        advance(n);
        return n;
    }

    public void enc_ndr_small(int s) {
        this.buf[this.index] = (byte) (s & 255);
        advance(1);
    }

    public int dec_ndr_small() {
        int val = this.buf[this.index] & 255;
        advance(1);
        return val;
    }

    public void enc_ndr_short(int s) {
        align(2);
        Encdec.enc_uint16le((short) s, this.buf, this.index);
        advance(2);
    }

    public int dec_ndr_short() {
        align(2);
        int val = Encdec.dec_uint16le(this.buf, this.index);
        advance(2);
        return val;
    }

    public void enc_ndr_long(int l) {
        align(4);
        Encdec.enc_uint32le(l, this.buf, this.index);
        advance(4);
    }

    public int dec_ndr_long() {
        align(4);
        int val = Encdec.dec_uint32le(this.buf, this.index);
        advance(4);
        return val;
    }

    public void enc_ndr_hyper(long h) {
        align(8);
        Encdec.enc_uint64le(h, this.buf, this.index);
        advance(8);
    }

    public long dec_ndr_hyper() {
        align(8);
        long val = Encdec.dec_uint64le(this.buf, this.index);
        advance(8);
        return val;
    }

    public void enc_ndr_string(String s) {
        align(4);
        int i = this.index;
        int len = s.length();
        Encdec.enc_uint32le(len + 1, this.buf, i);
        i += 4;
        Encdec.enc_uint32le(0, this.buf, i);
        i += 4;
        Encdec.enc_uint32le(len + 1, this.buf, i);
        i += 4;
        try {
            System.arraycopy(s.getBytes(SmbConstants.UNI_ENCODING), 0, this.buf, i, len * 2);
        } catch (UnsupportedEncodingException e) {
        }
        i += len * 2;
        int i2 = i + 1;
        this.buf[i] = (byte) 0;
        i = i2 + 1;
        this.buf[i2] = (byte) 0;
        advance(i - this.index);
    }

    public String dec_ndr_string() throws NdrException {
        align(4);
        int i = this.index;
        String val = null;
        int len = Encdec.dec_uint32le(this.buf, i);
        i += 12;
        if (len != 0) {
            int size = (len - 1) * 2;
            if (size < 0 || size > 65535) {
                try {
                    throw new NdrException(NdrException.INVALID_CONFORMANCE);
                } catch (UnsupportedEncodingException e) {
                }
            } else {
                String val2 = new String(this.buf, i, size, SmbConstants.UNI_ENCODING);
                i += size + 2;
                val = val2;
            }
        }
        advance(i - this.index);
        return val;
    }

    private int getDceReferent(Object obj) {
        if (this.referents == null) {
            this.referents = new HashMap();
            this.referent = 1;
        }
        Entry e = (Entry) this.referents.get(obj);
        if (e == null) {
            e = new Entry();
            int i = this.referent;
            this.referent = i + 1;
            e.referent = i;
            e.obj = obj;
            this.referents.put(obj, e);
        }
        return e.referent;
    }

    public void enc_ndr_referent(Object obj, int type) {
        if (obj == null) {
            enc_ndr_long(0);
            return;
        }
        switch (type) {
            case 1:
            case 3:
                enc_ndr_long(System.identityHashCode(obj));
                return;
            case 2:
                enc_ndr_long(getDceReferent(obj));
                return;
            default:
                return;
        }
    }

    public String toString() {
        return "start=" + this.start + ",index=" + this.index + ",length=" + getLength();
    }
}
