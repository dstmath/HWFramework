package java.io;

import java.io.ObjectStreamClass;
import java.lang.ref.ReferenceQueue;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.reflect.misc.ReflectUtil;

public class ObjectOutputStream extends OutputStream implements ObjectOutput, ObjectStreamConstants {
    private static final boolean extendedDebugInfo = false;
    /* access modifiers changed from: private */
    public final BlockDataOutputStream bout;
    private SerialCallbackContext curContext;
    private PutFieldImpl curPut;
    private final DebugTraceInfoStack debugInfoStack;
    private int depth;
    private final boolean enableOverride;
    private boolean enableReplace;
    private final HandleTable handles;
    private byte[] primVals;
    private int protocol = 2;
    private final ReplaceTable subs;

    private static class BlockDataOutputStream extends OutputStream implements DataOutput {
        private static final int CHAR_BUF_SIZE = 256;
        private static final int MAX_BLOCK_SIZE = 1024;
        private static final int MAX_HEADER_SIZE = 5;
        private boolean blkmode = ObjectOutputStream.extendedDebugInfo;
        private final byte[] buf = new byte[1024];
        private final char[] cbuf = new char[256];
        private final DataOutputStream dout;
        private final byte[] hbuf = new byte[5];
        private final OutputStream out;
        private int pos = 0;
        private boolean warnOnceWhenWriting;

        BlockDataOutputStream(OutputStream out2) {
            this.out = out2;
            this.dout = new DataOutputStream(this);
        }

        /* access modifiers changed from: package-private */
        public boolean setBlockDataMode(boolean mode) throws IOException {
            if (this.blkmode == mode) {
                return this.blkmode;
            }
            drain();
            this.blkmode = mode;
            return !this.blkmode;
        }

        /* access modifiers changed from: package-private */
        public boolean getBlockDataMode() {
            return this.blkmode;
        }

        private void warnIfClosed() {
            if (this.warnOnceWhenWriting) {
                System.logW("The app is relying on undefined behavior. Attempting to write to a closed ObjectOutputStream could produce corrupt output in a future release of Android.", new IOException("Stream Closed"));
                this.warnOnceWhenWriting = ObjectOutputStream.extendedDebugInfo;
            }
        }

        public void write(int b) throws IOException {
            if (this.pos >= 1024) {
                drain();
            }
            byte[] bArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            bArr[i] = (byte) b;
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length, ObjectOutputStream.extendedDebugInfo);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            write(b, off, len, ObjectOutputStream.extendedDebugInfo);
        }

        public void flush() throws IOException {
            drain();
            this.out.flush();
        }

        public void close() throws IOException {
            flush();
            this.out.close();
            this.warnOnceWhenWriting = true;
        }

        /* access modifiers changed from: package-private */
        public void write(byte[] b, int off, int len, boolean copy) throws IOException {
            if (copy || this.blkmode) {
                while (len > 0) {
                    if (this.pos >= 1024) {
                        drain();
                    }
                    if (len < 1024 || copy || this.pos != 0) {
                        int wlen = Math.min(len, 1024 - this.pos);
                        System.arraycopy(b, off, this.buf, this.pos, wlen);
                        this.pos += wlen;
                        off += wlen;
                        len -= wlen;
                    } else {
                        writeBlockHeader(1024);
                        this.out.write(b, off, 1024);
                        off += 1024;
                        len -= 1024;
                    }
                }
                warnIfClosed();
                return;
            }
            drain();
            this.out.write(b, off, len);
            warnIfClosed();
        }

        /* access modifiers changed from: package-private */
        public void drain() throws IOException {
            if (this.pos != 0) {
                if (this.blkmode) {
                    writeBlockHeader(this.pos);
                }
                this.out.write(this.buf, 0, this.pos);
                this.pos = 0;
                warnIfClosed();
            }
        }

        private void writeBlockHeader(int len) throws IOException {
            if (len <= 255) {
                this.hbuf[0] = ObjectStreamConstants.TC_BLOCKDATA;
                this.hbuf[1] = (byte) len;
                this.out.write(this.hbuf, 0, 2);
            } else {
                this.hbuf[0] = ObjectStreamConstants.TC_BLOCKDATALONG;
                Bits.putInt(this.hbuf, 1, len);
                this.out.write(this.hbuf, 0, 5);
            }
            warnIfClosed();
        }

        public void writeBoolean(boolean v) throws IOException {
            if (this.pos >= 1024) {
                drain();
            }
            byte[] bArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            Bits.putBoolean(bArr, i, v);
        }

        public void writeByte(int v) throws IOException {
            if (this.pos >= 1024) {
                drain();
            }
            byte[] bArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            bArr[i] = (byte) v;
        }

        public void writeChar(int v) throws IOException {
            if (this.pos + 2 <= 1024) {
                Bits.putChar(this.buf, this.pos, (char) v);
                this.pos += 2;
                return;
            }
            this.dout.writeChar(v);
        }

        public void writeShort(int v) throws IOException {
            if (this.pos + 2 <= 1024) {
                Bits.putShort(this.buf, this.pos, (short) v);
                this.pos += 2;
                return;
            }
            this.dout.writeShort(v);
        }

        public void writeInt(int v) throws IOException {
            if (this.pos + 4 <= 1024) {
                Bits.putInt(this.buf, this.pos, v);
                this.pos += 4;
                return;
            }
            this.dout.writeInt(v);
        }

        public void writeFloat(float v) throws IOException {
            if (this.pos + 4 <= 1024) {
                Bits.putFloat(this.buf, this.pos, v);
                this.pos += 4;
                return;
            }
            this.dout.writeFloat(v);
        }

        public void writeLong(long v) throws IOException {
            if (this.pos + 8 <= 1024) {
                Bits.putLong(this.buf, this.pos, v);
                this.pos += 8;
                return;
            }
            this.dout.writeLong(v);
        }

        public void writeDouble(double v) throws IOException {
            if (this.pos + 8 <= 1024) {
                Bits.putDouble(this.buf, this.pos, v);
                this.pos += 8;
                return;
            }
            this.dout.writeDouble(v);
        }

        public void writeBytes(String s) throws IOException {
            int endoff = s.length();
            int csize = 0;
            int cpos = 0;
            int off = 0;
            while (off < endoff) {
                if (cpos >= csize) {
                    cpos = 0;
                    csize = Math.min(endoff - off, 256);
                    s.getChars(off, off + csize, this.cbuf, 0);
                }
                if (this.pos >= 1024) {
                    drain();
                }
                int n = Math.min(csize - cpos, 1024 - this.pos);
                int stop = this.pos + n;
                while (this.pos < stop) {
                    byte[] bArr = this.buf;
                    int i = this.pos;
                    this.pos = i + 1;
                    bArr[i] = (byte) this.cbuf[cpos];
                    cpos++;
                }
                off += n;
            }
        }

        public void writeChars(String s) throws IOException {
            int endoff = s.length();
            int off = 0;
            while (off < endoff) {
                int csize = Math.min(endoff - off, 256);
                s.getChars(off, off + csize, this.cbuf, 0);
                writeChars(this.cbuf, 0, csize);
                off += csize;
            }
        }

        public void writeUTF(String s) throws IOException {
            writeUTF(s, getUTFLength(s));
        }

        /* access modifiers changed from: package-private */
        public void writeBooleans(boolean[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (this.pos >= 1024) {
                    drain();
                }
                int stop = Math.min(endoff, (1024 - this.pos) + off);
                while (off < stop) {
                    byte[] bArr = this.buf;
                    int i = this.pos;
                    this.pos = i + 1;
                    Bits.putBoolean(bArr, i, v[off]);
                    off++;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeChars(char[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (this.pos <= 1022) {
                    int stop = Math.min(endoff, off + ((1024 - this.pos) >> 1));
                    while (off < stop) {
                        Bits.putChar(this.buf, this.pos, v[off]);
                        this.pos += 2;
                        off++;
                    }
                } else {
                    this.dout.writeChar(v[off]);
                    off++;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeShorts(short[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (this.pos <= 1022) {
                    int stop = Math.min(endoff, off + ((1024 - this.pos) >> 1));
                    while (off < stop) {
                        Bits.putShort(this.buf, this.pos, v[off]);
                        this.pos += 2;
                        off++;
                    }
                } else {
                    this.dout.writeShort(v[off]);
                    off++;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeInts(int[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (this.pos <= 1020) {
                    int stop = Math.min(endoff, off + ((1024 - this.pos) >> 2));
                    while (off < stop) {
                        Bits.putInt(this.buf, this.pos, v[off]);
                        this.pos += 4;
                        off++;
                    }
                } else {
                    this.dout.writeInt(v[off]);
                    off++;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeFloats(float[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (this.pos <= 1020) {
                    int chunklen = Math.min(endoff - off, (1024 - this.pos) >> 2);
                    ObjectOutputStream.floatsToBytes(v, off, this.buf, this.pos, chunklen);
                    off += chunklen;
                    this.pos += chunklen << 2;
                } else {
                    this.dout.writeFloat(v[off]);
                    off++;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeLongs(long[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (this.pos <= 1016) {
                    int stop = Math.min(endoff, off + ((1024 - this.pos) >> 3));
                    while (off < stop) {
                        Bits.putLong(this.buf, this.pos, v[off]);
                        this.pos += 8;
                        off++;
                    }
                } else {
                    this.dout.writeLong(v[off]);
                    off++;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeDoubles(double[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (this.pos <= 1016) {
                    int chunklen = Math.min(endoff - off, (1024 - this.pos) >> 3);
                    ObjectOutputStream.doublesToBytes(v, off, this.buf, this.pos, chunklen);
                    off += chunklen;
                    this.pos += chunklen << 3;
                } else {
                    this.dout.writeDouble(v[off]);
                    off++;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public long getUTFLength(String s) {
            long j;
            int len = s.length();
            long utflen = 0;
            int off = 0;
            while (off < len) {
                int csize = Math.min(len - off, 256);
                s.getChars(off, off + csize, this.cbuf, 0);
                long utflen2 = utflen;
                for (int cpos = 0; cpos < csize; cpos++) {
                    char c = this.cbuf[cpos];
                    if (c >= 1 && c <= 127) {
                        j = 1;
                    } else if (c > 2047) {
                        j = 3;
                    } else {
                        j = 2;
                    }
                    utflen2 += j;
                }
                off += csize;
                utflen = utflen2;
            }
            return utflen;
        }

        /* access modifiers changed from: package-private */
        public void writeUTF(String s, long utflen) throws IOException {
            if (utflen <= 65535) {
                writeShort((int) utflen);
                if (utflen == ((long) s.length())) {
                    writeBytes(s);
                } else {
                    writeUTFBody(s);
                }
            } else {
                throw new UTFDataFormatException();
            }
        }

        /* access modifiers changed from: package-private */
        public void writeLongUTF(String s) throws IOException {
            writeLongUTF(s, getUTFLength(s));
        }

        /* access modifiers changed from: package-private */
        public void writeLongUTF(String s, long utflen) throws IOException {
            writeLong(utflen);
            if (utflen == ((long) s.length())) {
                writeBytes(s);
            } else {
                writeUTFBody(s);
            }
        }

        private void writeUTFBody(String s) throws IOException {
            int len = s.length();
            int off = 0;
            while (off < len) {
                int csize = Math.min(len - off, 256);
                s.getChars(off, off + csize, this.cbuf, 0);
                for (int cpos = 0; cpos < csize; cpos++) {
                    char c = this.cbuf[cpos];
                    if (this.pos <= 1021) {
                        if (c <= 127 && c != 0) {
                            byte[] bArr = this.buf;
                            int i = this.pos;
                            this.pos = i + 1;
                            bArr[i] = (byte) c;
                        } else if (c > 2047) {
                            this.buf[this.pos + 2] = (byte) (((c >> 0) & 63) | 128);
                            this.buf[this.pos + 1] = (byte) (((c >> 6) & 63) | 128);
                            this.buf[this.pos + 0] = (byte) (224 | ((c >> 12) & 15));
                            this.pos += 3;
                        } else {
                            this.buf[this.pos + 1] = (byte) (((c >> 0) & 63) | 128);
                            this.buf[this.pos + 0] = (byte) (192 | ((c >> 6) & 31));
                            this.pos += 2;
                        }
                    } else if (c <= 127 && c != 0) {
                        write((int) c);
                    } else if (c > 2047) {
                        write(((c >> 12) & 15) | 224);
                        write(((c >> 6) & 63) | 128);
                        write(((c >> 0) & 63) | 128);
                    } else {
                        write(((c >> 6) & 31) | 192);
                        write(((c >> 0) & 63) | 128);
                    }
                }
                off += csize;
            }
        }
    }

    private static class Caches {
        static final ConcurrentMap<ObjectStreamClass.WeakClassKey, Boolean> subclassAudits = new ConcurrentHashMap();
        static final ReferenceQueue<Class<?>> subclassAuditsQueue = new ReferenceQueue<>();

        private Caches() {
        }
    }

    private static class DebugTraceInfoStack {
        private final List<String> stack = new ArrayList();

        DebugTraceInfoStack() {
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            this.stack.clear();
        }

        /* access modifiers changed from: package-private */
        public void pop() {
            this.stack.remove(this.stack.size() - 1);
        }

        /* access modifiers changed from: package-private */
        public void push(String entry) {
            List<String> list = this.stack;
            list.add("\t- " + entry);
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            if (!this.stack.isEmpty()) {
                int i = this.stack.size();
                while (i > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.stack.get(i - 1));
                    sb.append(i != 1 ? "\n" : "");
                    buffer.append(sb.toString());
                    i--;
                }
            }
            return buffer.toString();
        }
    }

    private static class HandleTable {
        private final float loadFactor;
        private int[] next;
        private Object[] objs;
        private int size;
        private int[] spine;
        private int threshold;

        HandleTable(int initialCapacity, float loadFactor2) {
            this.loadFactor = loadFactor2;
            this.spine = new int[initialCapacity];
            this.next = new int[initialCapacity];
            this.objs = new Object[initialCapacity];
            this.threshold = (int) (((float) initialCapacity) * loadFactor2);
            clear();
        }

        /* access modifiers changed from: package-private */
        public int assign(Object obj) {
            if (this.size >= this.next.length) {
                growEntries();
            }
            if (this.size >= this.threshold) {
                growSpine();
            }
            insert(obj, this.size);
            int i = this.size;
            this.size = i + 1;
            return i;
        }

        /* access modifiers changed from: package-private */
        public int lookup(Object obj) {
            if (this.size == 0) {
                return -1;
            }
            int i = this.spine[hash(obj) % this.spine.length];
            while (i >= 0) {
                if (this.objs[i] == obj) {
                    return i;
                }
                i = this.next[i];
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            Arrays.fill(this.spine, -1);
            Arrays.fill(this.objs, 0, this.size, (Object) null);
            this.size = 0;
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.size;
        }

        private void insert(Object obj, int handle) {
            int index = hash(obj) % this.spine.length;
            this.objs[handle] = obj;
            this.next[handle] = this.spine[index];
            this.spine[index] = handle;
        }

        private void growSpine() {
            this.spine = new int[((this.spine.length << 1) + 1)];
            this.threshold = (int) (((float) this.spine.length) * this.loadFactor);
            Arrays.fill(this.spine, -1);
            for (int i = 0; i < this.size; i++) {
                insert(this.objs[i], i);
            }
        }

        private void growEntries() {
            int newLength = (this.next.length << 1) + 1;
            int[] newNext = new int[newLength];
            System.arraycopy((Object) this.next, 0, (Object) newNext, 0, this.size);
            this.next = newNext;
            Object[] newObjs = new Object[newLength];
            System.arraycopy((Object) this.objs, 0, (Object) newObjs, 0, this.size);
            this.objs = newObjs;
        }

        private int hash(Object obj) {
            return System.identityHashCode(obj) & Integer.MAX_VALUE;
        }
    }

    public static abstract class PutField {
        public abstract void put(String str, byte b);

        public abstract void put(String str, char c);

        public abstract void put(String str, double d);

        public abstract void put(String str, float f);

        public abstract void put(String str, int i);

        public abstract void put(String str, long j);

        public abstract void put(String str, Object obj);

        public abstract void put(String str, short s);

        public abstract void put(String str, boolean z);

        @Deprecated
        public abstract void write(ObjectOutput objectOutput) throws IOException;
    }

    private class PutFieldImpl extends PutField {
        private final ObjectStreamClass desc;
        private final Object[] objVals;
        private final byte[] primVals;

        PutFieldImpl(ObjectStreamClass desc2) {
            this.desc = desc2;
            this.primVals = new byte[desc2.getPrimDataSize()];
            this.objVals = new Object[desc2.getNumObjFields()];
        }

        public void put(String name, boolean val) {
            Bits.putBoolean(this.primVals, getFieldOffset(name, Boolean.TYPE), val);
        }

        public void put(String name, byte val) {
            this.primVals[getFieldOffset(name, Byte.TYPE)] = val;
        }

        public void put(String name, char val) {
            Bits.putChar(this.primVals, getFieldOffset(name, Character.TYPE), val);
        }

        public void put(String name, short val) {
            Bits.putShort(this.primVals, getFieldOffset(name, Short.TYPE), val);
        }

        public void put(String name, int val) {
            Bits.putInt(this.primVals, getFieldOffset(name, Integer.TYPE), val);
        }

        public void put(String name, float val) {
            Bits.putFloat(this.primVals, getFieldOffset(name, Float.TYPE), val);
        }

        public void put(String name, long val) {
            Bits.putLong(this.primVals, getFieldOffset(name, Long.TYPE), val);
        }

        public void put(String name, double val) {
            Bits.putDouble(this.primVals, getFieldOffset(name, Double.TYPE), val);
        }

        public void put(String name, Object val) {
            this.objVals[getFieldOffset(name, Object.class)] = val;
        }

        public void write(ObjectOutput out) throws IOException {
            if (ObjectOutputStream.this == out) {
                int i = 0;
                out.write(this.primVals, 0, this.primVals.length);
                ObjectStreamField[] fields = this.desc.getFields(ObjectOutputStream.extendedDebugInfo);
                int numPrimFields = fields.length - this.objVals.length;
                while (i < this.objVals.length) {
                    if (!fields[numPrimFields + i].isUnshared()) {
                        out.writeObject(this.objVals[i]);
                        i++;
                    } else {
                        throw new IOException("cannot write unshared object");
                    }
                }
                return;
            }
            throw new IllegalArgumentException("wrong stream");
        }

        /* access modifiers changed from: package-private */
        public void writeFields() throws IOException {
            int i = 0;
            ObjectOutputStream.this.bout.write(this.primVals, 0, this.primVals.length, ObjectOutputStream.extendedDebugInfo);
            ObjectStreamField[] fields = this.desc.getFields(ObjectOutputStream.extendedDebugInfo);
            int numPrimFields = fields.length - this.objVals.length;
            while (true) {
                int i2 = i;
                if (i2 < this.objVals.length) {
                    ObjectOutputStream.this.writeObject0(this.objVals[i2], fields[numPrimFields + i2].isUnshared());
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }

        private int getFieldOffset(String name, Class<?> type) {
            ObjectStreamField field = this.desc.getField(name, type);
            if (field != null) {
                return field.getOffset();
            }
            throw new IllegalArgumentException("no such field " + name + " with type " + type);
        }
    }

    private static class ReplaceTable {
        private final HandleTable htab;
        private Object[] reps;

        ReplaceTable(int initialCapacity, float loadFactor) {
            this.htab = new HandleTable(initialCapacity, loadFactor);
            this.reps = new Object[initialCapacity];
        }

        /* access modifiers changed from: package-private */
        public void assign(Object obj, Object rep) {
            int index = this.htab.assign(obj);
            while (index >= this.reps.length) {
                grow();
            }
            this.reps[index] = rep;
        }

        /* access modifiers changed from: package-private */
        public Object lookup(Object obj) {
            int index = this.htab.lookup(obj);
            return index >= 0 ? this.reps[index] : obj;
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            Arrays.fill(this.reps, 0, this.htab.size(), (Object) null);
            this.htab.clear();
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.htab.size();
        }

        private void grow() {
            Object[] newReps = new Object[((this.reps.length << 1) + 1)];
            System.arraycopy((Object) this.reps, 0, (Object) newReps, 0, this.reps.length);
            this.reps = newReps;
        }
    }

    /* access modifiers changed from: private */
    public static native void doublesToBytes(double[] dArr, int i, byte[] bArr, int i2, int i3);

    /* access modifiers changed from: private */
    public static native void floatsToBytes(float[] fArr, int i, byte[] bArr, int i2, int i3);

    public ObjectOutputStream(OutputStream out) throws IOException {
        verifySubclass();
        this.bout = new BlockDataOutputStream(out);
        this.handles = new HandleTable(10, 3.0f);
        this.subs = new ReplaceTable(10, 3.0f);
        this.enableOverride = extendedDebugInfo;
        writeStreamHeader();
        this.bout.setBlockDataMode(true);
        this.debugInfoStack = null;
    }

    protected ObjectOutputStream() throws IOException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        this.bout = null;
        this.handles = null;
        this.subs = null;
        this.enableOverride = true;
        this.debugInfoStack = null;
    }

    public void useProtocolVersion(int version) throws IOException {
        if (this.handles.size() == 0) {
            switch (version) {
                case 1:
                case 2:
                    this.protocol = version;
                    return;
                default:
                    throw new IllegalArgumentException("unknown version: " + version);
            }
        } else {
            throw new IllegalStateException("stream non-empty");
        }
    }

    public final void writeObject(Object obj) throws IOException {
        if (this.enableOverride) {
            writeObjectOverride(obj);
            return;
        }
        try {
            writeObject0(obj, extendedDebugInfo);
        } catch (IOException ex) {
            if (this.depth == 0) {
                try {
                    writeFatalException(ex);
                } catch (IOException e) {
                }
            }
            throw ex;
        }
    }

    /* access modifiers changed from: protected */
    public void writeObjectOverride(Object obj) throws IOException {
        if (!this.enableOverride) {
            throw new IOException();
        }
    }

    public void writeUnshared(Object obj) throws IOException {
        try {
            writeObject0(obj, true);
        } catch (IOException ex) {
            if (this.depth == 0) {
                writeFatalException(ex);
            }
            throw ex;
        }
    }

    public void defaultWriteObject() throws IOException {
        SerialCallbackContext ctx = this.curContext;
        if (ctx != null) {
            Object curObj = ctx.getObj();
            ObjectStreamClass curDesc = ctx.getDesc();
            this.bout.setBlockDataMode(extendedDebugInfo);
            defaultWriteFields(curObj, curDesc);
            this.bout.setBlockDataMode(true);
            return;
        }
        throw new NotActiveException("not in call to writeObject");
    }

    public PutField putFields() throws IOException {
        if (this.curPut == null) {
            SerialCallbackContext ctx = this.curContext;
            if (ctx != null) {
                Object obj = ctx.getObj();
                this.curPut = new PutFieldImpl(ctx.getDesc());
            } else {
                throw new NotActiveException("not in call to writeObject");
            }
        }
        return this.curPut;
    }

    public void writeFields() throws IOException {
        if (this.curPut != null) {
            this.bout.setBlockDataMode(extendedDebugInfo);
            this.curPut.writeFields();
            this.bout.setBlockDataMode(true);
            return;
        }
        throw new NotActiveException("no current PutField object");
    }

    public void reset() throws IOException {
        if (this.depth == 0) {
            this.bout.setBlockDataMode(extendedDebugInfo);
            this.bout.writeByte(121);
            clear();
            this.bout.setBlockDataMode(true);
            return;
        }
        throw new IOException("stream active");
    }

    /* access modifiers changed from: protected */
    public void annotateClass(Class<?> cls) throws IOException {
    }

    /* access modifiers changed from: protected */
    public void annotateProxyClass(Class<?> cls) throws IOException {
    }

    /* access modifiers changed from: protected */
    public Object replaceObject(Object obj) throws IOException {
        return obj;
    }

    /* access modifiers changed from: protected */
    public boolean enableReplaceObject(boolean enable) throws SecurityException {
        if (enable == this.enableReplace) {
            return enable;
        }
        if (enable) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        this.enableReplace = enable;
        return !this.enableReplace;
    }

    /* access modifiers changed from: protected */
    public void writeStreamHeader() throws IOException {
        this.bout.writeShort(-21267);
        this.bout.writeShort(5);
    }

    /* access modifiers changed from: protected */
    public void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        desc.writeNonProxy(this);
    }

    public void write(int val) throws IOException {
        this.bout.write(val);
    }

    public void write(byte[] buf) throws IOException {
        this.bout.write(buf, 0, buf.length, extendedDebugInfo);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (buf != null) {
            int endoff = off + len;
            if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
                throw new IndexOutOfBoundsException();
            }
            this.bout.write(buf, off, len, extendedDebugInfo);
            return;
        }
        throw new NullPointerException();
    }

    public void flush() throws IOException {
        this.bout.flush();
    }

    /* access modifiers changed from: protected */
    public void drain() throws IOException {
        this.bout.drain();
    }

    public void close() throws IOException {
        flush();
        this.bout.close();
    }

    public void writeBoolean(boolean val) throws IOException {
        this.bout.writeBoolean(val);
    }

    public void writeByte(int val) throws IOException {
        this.bout.writeByte(val);
    }

    public void writeShort(int val) throws IOException {
        this.bout.writeShort(val);
    }

    public void writeChar(int val) throws IOException {
        this.bout.writeChar(val);
    }

    public void writeInt(int val) throws IOException {
        this.bout.writeInt(val);
    }

    public void writeLong(long val) throws IOException {
        this.bout.writeLong(val);
    }

    public void writeFloat(float val) throws IOException {
        this.bout.writeFloat(val);
    }

    public void writeDouble(double val) throws IOException {
        this.bout.writeDouble(val);
    }

    public void writeBytes(String str) throws IOException {
        this.bout.writeBytes(str);
    }

    public void writeChars(String str) throws IOException {
        this.bout.writeChars(str);
    }

    public void writeUTF(String str) throws IOException {
        this.bout.writeUTF(str);
    }

    /* access modifiers changed from: package-private */
    public int getProtocolVersion() {
        return this.protocol;
    }

    /* access modifiers changed from: package-private */
    public void writeTypeString(String str) throws IOException {
        if (str == null) {
            writeNull();
            return;
        }
        int lookup = this.handles.lookup(str);
        int handle = lookup;
        if (lookup != -1) {
            writeHandle(handle);
        } else {
            writeString(str, extendedDebugInfo);
        }
    }

    private void verifySubclass() {
        Class<?> cl = getClass();
        if (cl != ObjectOutputStream.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                ObjectStreamClass.processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
                ObjectStreamClass.WeakClassKey key = new ObjectStreamClass.WeakClassKey(cl, Caches.subclassAuditsQueue);
                Boolean result = Caches.subclassAudits.get(key);
                if (result == null) {
                    result = Boolean.valueOf(auditSubclass(cl));
                    Caches.subclassAudits.putIfAbsent(key, result);
                }
                if (!result.booleanValue()) {
                    sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
                }
            }
        }
    }

    private static boolean auditSubclass(final Class<?> subcl) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                Class<? super ObjectOutputStream> cls = Class.this;
                while (cls != ObjectOutputStream.class) {
                    try {
                        cls.getDeclaredMethod("writeUnshared", Object.class);
                        return Boolean.FALSE;
                    } catch (NoSuchMethodException e) {
                        try {
                            cls.getDeclaredMethod("putFields", null);
                            return Boolean.FALSE;
                        } catch (NoSuchMethodException e2) {
                            cls = cls.getSuperclass();
                        }
                    }
                }
                return Boolean.TRUE;
            }
        })).booleanValue();
    }

    private void clear() {
        this.subs.clear();
        this.handles.clear();
    }

    /* access modifiers changed from: private */
    public void writeObject0(Object obj, boolean unshared) throws IOException {
        boolean oldMode = this.bout.setBlockDataMode(extendedDebugInfo);
        this.depth++;
        try {
            Object lookup = this.subs.lookup(obj);
            Object obj2 = lookup;
            if (lookup == null) {
                writeNull();
                return;
            }
            if (!unshared) {
                int lookup2 = this.handles.lookup(obj2);
                int h = lookup2;
                if (lookup2 != -1) {
                    writeHandle(h);
                    this.depth--;
                    this.bout.setBlockDataMode(oldMode);
                    return;
                }
            }
            Object orig = obj2;
            Class<?> cl = obj2.getClass();
            ObjectStreamClass desc = ObjectStreamClass.lookup(cl, true);
            if (desc.hasWriteReplaceMethod()) {
                Object invokeWriteReplace = desc.invokeWriteReplace(obj2);
                obj2 = invokeWriteReplace;
                if (invokeWriteReplace != null) {
                    Class<?> cls = obj2.getClass();
                    Class<?> repCl = cls;
                    if (cls != cl) {
                        cl = repCl;
                        desc = ObjectStreamClass.lookup(cl, true);
                    }
                }
            }
            if (this.enableReplace) {
                Object rep = replaceObject(obj2);
                if (!(rep == obj2 || rep == null)) {
                    cl = rep.getClass();
                    desc = ObjectStreamClass.lookup(cl, true);
                }
                obj2 = rep;
            }
            if (obj2 != orig) {
                this.subs.assign(orig, obj2);
                if (obj2 == null) {
                    writeNull();
                    this.depth--;
                    this.bout.setBlockDataMode(oldMode);
                    return;
                } else if (!unshared) {
                    int lookup3 = this.handles.lookup(obj2);
                    int h2 = lookup3;
                    if (lookup3 != -1) {
                        writeHandle(h2);
                        this.depth--;
                        this.bout.setBlockDataMode(oldMode);
                        return;
                    }
                }
            }
            if (obj2 instanceof Class) {
                writeClass((Class) obj2, unshared);
            } else if (obj2 instanceof ObjectStreamClass) {
                writeClassDesc((ObjectStreamClass) obj2, unshared);
            } else if (obj2 instanceof String) {
                writeString((String) obj2, unshared);
            } else if (cl.isArray()) {
                writeArray(obj2, desc, unshared);
            } else if (obj2 instanceof Enum) {
                writeEnum((Enum) obj2, desc, unshared);
            } else if (obj2 instanceof Serializable) {
                writeOrdinaryObject(obj2, desc, unshared);
            } else {
                throw new NotSerializableException(cl.getName());
            }
            this.depth--;
            this.bout.setBlockDataMode(oldMode);
        } finally {
            this.depth--;
            this.bout.setBlockDataMode(oldMode);
        }
    }

    private void writeNull() throws IOException {
        this.bout.writeByte(112);
    }

    private void writeHandle(int handle) throws IOException {
        this.bout.writeByte(113);
        this.bout.writeInt(ObjectStreamConstants.baseWireHandle + handle);
    }

    private void writeClass(Class<?> cl, boolean unshared) throws IOException {
        this.bout.writeByte(118);
        writeClassDesc(ObjectStreamClass.lookup(cl, true), extendedDebugInfo);
        this.handles.assign(unshared ? null : cl);
    }

    private void writeClassDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
        if (desc == null) {
            writeNull();
            return;
        }
        if (!unshared) {
            int lookup = this.handles.lookup(desc);
            int handle = lookup;
            if (lookup != -1) {
                writeHandle(handle);
                return;
            }
        }
        if (desc.isProxy()) {
            writeProxyDesc(desc, unshared);
        } else {
            writeNonProxyDesc(desc, unshared);
        }
    }

    private boolean isCustomSubclass() {
        if (getClass().getClassLoader() != ObjectOutputStream.class.getClassLoader()) {
            return true;
        }
        return extendedDebugInfo;
    }

    private void writeProxyDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
        this.bout.writeByte(125);
        this.handles.assign(unshared ? null : desc);
        Class<?> cl = desc.forClass();
        Class<?>[] ifaces = cl.getInterfaces();
        this.bout.writeInt(ifaces.length);
        for (Class<?> name : ifaces) {
            this.bout.writeUTF(name.getName());
        }
        this.bout.setBlockDataMode(true);
        if (cl != null && isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateProxyClass(cl);
        this.bout.setBlockDataMode(extendedDebugInfo);
        this.bout.writeByte(120);
        writeClassDesc(desc.getSuperDesc(), extendedDebugInfo);
    }

    private void writeNonProxyDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
        this.bout.writeByte(114);
        this.handles.assign(unshared ? null : desc);
        if (this.protocol == 1) {
            desc.writeNonProxy(this);
        } else {
            writeClassDescriptor(desc);
        }
        Class<?> cl = desc.forClass();
        this.bout.setBlockDataMode(true);
        if (cl != null && isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateClass(cl);
        this.bout.setBlockDataMode(extendedDebugInfo);
        this.bout.writeByte(120);
        writeClassDesc(desc.getSuperDesc(), extendedDebugInfo);
    }

    private void writeString(String str, boolean unshared) throws IOException {
        this.handles.assign(unshared ? null : str);
        long utflen = this.bout.getUTFLength(str);
        if (utflen <= 65535) {
            this.bout.writeByte(116);
            this.bout.writeUTF(str, utflen);
            return;
        }
        this.bout.writeByte(124);
        this.bout.writeLongUTF(str, utflen);
    }

    private void writeArray(Object array, ObjectStreamClass desc, boolean unshared) throws IOException {
        this.bout.writeByte(117);
        writeClassDesc(desc, extendedDebugInfo);
        this.handles.assign(unshared ? null : array);
        Class<?> ccl = desc.forClass().getComponentType();
        if (!ccl.isPrimitive()) {
            this.bout.writeInt(len);
            for (Object writeObject0 : (Object[]) array) {
                writeObject0(writeObject0, extendedDebugInfo);
            }
        } else if (ccl == Integer.TYPE) {
            int[] ia = (int[]) array;
            this.bout.writeInt(ia.length);
            this.bout.writeInts(ia, 0, ia.length);
        } else if (ccl == Byte.TYPE) {
            byte[] ba = (byte[]) array;
            this.bout.writeInt(ba.length);
            this.bout.write(ba, 0, ba.length, true);
        } else if (ccl == Long.TYPE) {
            long[] ja = (long[]) array;
            this.bout.writeInt(ja.length);
            this.bout.writeLongs(ja, 0, ja.length);
        } else if (ccl == Float.TYPE) {
            float[] fa = (float[]) array;
            this.bout.writeInt(fa.length);
            this.bout.writeFloats(fa, 0, fa.length);
        } else if (ccl == Double.TYPE) {
            double[] da = (double[]) array;
            this.bout.writeInt(da.length);
            this.bout.writeDoubles(da, 0, da.length);
        } else if (ccl == Short.TYPE) {
            short[] sa = (short[]) array;
            this.bout.writeInt(sa.length);
            this.bout.writeShorts(sa, 0, sa.length);
        } else if (ccl == Character.TYPE) {
            char[] ca = (char[]) array;
            this.bout.writeInt(ca.length);
            this.bout.writeChars(ca, 0, ca.length);
        } else if (ccl == Boolean.TYPE) {
            boolean[] za = (boolean[]) array;
            this.bout.writeInt(za.length);
            this.bout.writeBooleans(za, 0, za.length);
        } else {
            throw new InternalError();
        }
    }

    private void writeEnum(Enum<?> en, ObjectStreamClass desc, boolean unshared) throws IOException {
        this.bout.writeByte(126);
        ObjectStreamClass sdesc = desc.getSuperDesc();
        writeClassDesc(sdesc.forClass() == Enum.class ? desc : sdesc, extendedDebugInfo);
        this.handles.assign(unshared ? null : en);
        writeString(en.name(), extendedDebugInfo);
    }

    private void writeOrdinaryObject(Object obj, ObjectStreamClass desc, boolean unshared) throws IOException {
        desc.checkSerialize();
        this.bout.writeByte(115);
        writeClassDesc(desc, extendedDebugInfo);
        this.handles.assign(unshared ? null : obj);
        if (!desc.isExternalizable() || desc.isProxy()) {
            writeSerialData(obj, desc);
        } else {
            writeExternalData((Externalizable) obj);
        }
    }

    /* JADX INFO: finally extract failed */
    private void writeExternalData(Externalizable obj) throws IOException {
        PutFieldImpl oldPut = this.curPut;
        this.curPut = null;
        SerialCallbackContext oldContext = this.curContext;
        try {
            this.curContext = null;
            if (this.protocol == 1) {
                obj.writeExternal(this);
            } else {
                this.bout.setBlockDataMode(true);
                obj.writeExternal(this);
                this.bout.setBlockDataMode(extendedDebugInfo);
                this.bout.writeByte(120);
            }
            this.curContext = oldContext;
            this.curPut = oldPut;
        } catch (Throwable th) {
            this.curContext = oldContext;
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    private void writeSerialData(Object obj, ObjectStreamClass desc) throws IOException {
        ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
        for (ObjectStreamClass.ClassDataSlot classDataSlot : slots) {
            ObjectStreamClass slotDesc = classDataSlot.desc;
            if (slotDesc.hasWriteObjectMethod()) {
                PutFieldImpl oldPut = this.curPut;
                this.curPut = null;
                SerialCallbackContext oldContext = this.curContext;
                try {
                    this.curContext = new SerialCallbackContext(obj, slotDesc);
                    this.bout.setBlockDataMode(true);
                    slotDesc.invokeWriteObject(obj, this);
                    this.bout.setBlockDataMode(extendedDebugInfo);
                    this.bout.writeByte(120);
                    this.curContext.setUsed();
                    this.curContext = oldContext;
                    this.curPut = oldPut;
                } catch (Throwable th) {
                    this.curContext.setUsed();
                    this.curContext = oldContext;
                    throw th;
                }
            } else {
                defaultWriteFields(obj, slotDesc);
            }
        }
    }

    private void defaultWriteFields(Object obj, ObjectStreamClass desc) throws IOException {
        Class<?> cl = desc.forClass();
        if (cl == null || obj == null || cl.isInstance(obj)) {
            desc.checkDefaultSerialize();
            int primDataSize = desc.getPrimDataSize();
            if (this.primVals == null || this.primVals.length < primDataSize) {
                this.primVals = new byte[primDataSize];
            }
            desc.getPrimFieldValues(obj, this.primVals);
            this.bout.write(this.primVals, 0, primDataSize, extendedDebugInfo);
            ObjectStreamField[] fields = desc.getFields(extendedDebugInfo);
            Object[] objVals = new Object[desc.getNumObjFields()];
            int numPrimFields = fields.length - objVals.length;
            desc.getObjFieldValues(obj, objVals);
            for (int i = 0; i < objVals.length; i++) {
                writeObject0(objVals[i], fields[numPrimFields + i].isUnshared());
            }
            return;
        }
        throw new ClassCastException();
    }

    private void writeFatalException(IOException ex) throws IOException {
        clear();
        boolean oldMode = this.bout.setBlockDataMode(extendedDebugInfo);
        try {
            this.bout.writeByte(123);
            writeObject0(ex, extendedDebugInfo);
            clear();
        } finally {
            this.bout.setBlockDataMode(oldMode);
        }
    }
}
