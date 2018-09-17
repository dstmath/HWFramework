package java.io;

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
    private final BlockDataOutputStream bout;
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
        private boolean blkmode = false;
        private final byte[] buf = new byte[1024];
        private final char[] cbuf = new char[256];
        private final DataOutputStream dout;
        private final byte[] hbuf = new byte[5];
        private final OutputStream out;
        private int pos = 0;
        private boolean warnOnceWhenWriting;

        BlockDataOutputStream(OutputStream out) {
            this.out = out;
            this.dout = new DataOutputStream(this);
        }

        boolean setBlockDataMode(boolean mode) throws IOException {
            if (this.blkmode == mode) {
                return this.blkmode;
            }
            drain();
            this.blkmode = mode;
            return this.blkmode ^ 1;
        }

        boolean getBlockDataMode() {
            return this.blkmode;
        }

        private void warnIfClosed() {
            if (this.warnOnceWhenWriting) {
                System.logW("The app is relying on undefined behavior. Attempting to write to a closed ObjectOutputStream could produce corrupt output in a future release of Android.", new IOException("Stream Closed"));
                this.warnOnceWhenWriting = false;
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
            write(b, 0, b.length, false);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            write(b, off, len, false);
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

        void write(byte[] b, int off, int len, boolean copy) throws IOException {
            if (!copy ? this.blkmode : true) {
                while (len > 0) {
                    if (this.pos >= 1024) {
                        drain();
                    }
                    if (len < 1024 || (copy ^ 1) == 0 || this.pos != 0) {
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

        void drain() throws IOException {
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
            int cpos = 0;
            int csize = 0;
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
                    int cpos2 = cpos + 1;
                    bArr[i] = (byte) this.cbuf[cpos];
                    cpos = cpos2;
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

        void writeBooleans(boolean[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (this.pos >= 1024) {
                    drain();
                }
                int stop = Math.min(endoff, (1024 - this.pos) + off);
                int off2 = off;
                while (off2 < stop) {
                    byte[] bArr = this.buf;
                    int i = this.pos;
                    this.pos = i + 1;
                    off = off2 + 1;
                    Bits.putBoolean(bArr, i, v[off2]);
                    off2 = off;
                }
                off = off2;
            }
        }

        void writeChars(char[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1022) {
                    int stop = Math.min(endoff, off2 + ((1024 - this.pos) >> 1));
                    while (off2 < stop) {
                        off = off2 + 1;
                        Bits.putChar(this.buf, this.pos, v[off2]);
                        this.pos += 2;
                        off2 = off;
                    }
                    off = off2;
                } else {
                    off = off2 + 1;
                    this.dout.writeChar(v[off2]);
                }
                off2 = off;
            }
        }

        void writeShorts(short[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1022) {
                    int stop = Math.min(endoff, off2 + ((1024 - this.pos) >> 1));
                    while (off2 < stop) {
                        off = off2 + 1;
                        Bits.putShort(this.buf, this.pos, v[off2]);
                        this.pos += 2;
                        off2 = off;
                    }
                    off = off2;
                } else {
                    off = off2 + 1;
                    this.dout.writeShort(v[off2]);
                }
                off2 = off;
            }
        }

        void writeInts(int[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1020) {
                    int stop = Math.min(endoff, off2 + ((1024 - this.pos) >> 2));
                    while (off2 < stop) {
                        off = off2 + 1;
                        Bits.putInt(this.buf, this.pos, v[off2]);
                        this.pos += 4;
                        off2 = off;
                    }
                    off = off2;
                } else {
                    off = off2 + 1;
                    this.dout.writeInt(v[off2]);
                }
                off2 = off;
            }
        }

        void writeFloats(float[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1020) {
                    int chunklen = Math.min(endoff - off2, (1024 - this.pos) >> 2);
                    ObjectOutputStream.floatsToBytes(v, off2, this.buf, this.pos, chunklen);
                    off = off2 + chunklen;
                    this.pos += chunklen << 2;
                } else {
                    off = off2 + 1;
                    this.dout.writeFloat(v[off2]);
                }
                off2 = off;
            }
        }

        void writeLongs(long[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1016) {
                    int stop = Math.min(endoff, off2 + ((1024 - this.pos) >> 3));
                    while (off2 < stop) {
                        off = off2 + 1;
                        Bits.putLong(this.buf, this.pos, v[off2]);
                        this.pos += 8;
                        off2 = off;
                    }
                    off = off2;
                } else {
                    off = off2 + 1;
                    this.dout.writeLong(v[off2]);
                }
                off2 = off;
            }
        }

        void writeDoubles(double[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1016) {
                    int chunklen = Math.min(endoff - off2, (1024 - this.pos) >> 3);
                    ObjectOutputStream.doublesToBytes(v, off2, this.buf, this.pos, chunklen);
                    off = off2 + chunklen;
                    this.pos += chunklen << 3;
                } else {
                    off = off2 + 1;
                    this.dout.writeDouble(v[off2]);
                }
                off2 = off;
            }
        }

        long getUTFLength(String s) {
            int len = s.length();
            long utflen = 0;
            int off = 0;
            while (off < len) {
                int csize = Math.min(len - off, 256);
                s.getChars(off, off + csize, this.cbuf, 0);
                for (int cpos = 0; cpos < csize; cpos++) {
                    long j;
                    char c = this.cbuf[cpos];
                    if (c >= 1 && c <= 127) {
                        j = 1;
                    } else if (c > 2047) {
                        j = 3;
                    } else {
                        j = 2;
                    }
                    utflen += j;
                }
                off += csize;
            }
            return utflen;
        }

        void writeUTF(String s, long utflen) throws IOException {
            if (utflen > 65535) {
                throw new UTFDataFormatException();
            }
            writeShort((int) utflen);
            if (utflen == ((long) s.length())) {
                writeBytes(s);
            } else {
                writeUTFBody(s);
            }
        }

        void writeLongUTF(String s) throws IOException {
            writeLongUTF(s, getUTFLength(s));
        }

        void writeLongUTF(String s, long utflen) throws IOException {
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
                            this.buf[this.pos + 0] = (byte) (((c >> 12) & 15) | 224);
                            this.pos += 3;
                        } else {
                            this.buf[this.pos + 1] = (byte) (((c >> 0) & 63) | 128);
                            this.buf[this.pos + 0] = (byte) (((c >> 6) & 31) | 192);
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
        static final ConcurrentMap<WeakClassKey, Boolean> subclassAudits = new ConcurrentHashMap();
        static final ReferenceQueue<Class<?>> subclassAuditsQueue = new ReferenceQueue();

        private Caches() {
        }
    }

    private static class DebugTraceInfoStack {
        private final List<String> stack = new ArrayList();

        DebugTraceInfoStack() {
        }

        void clear() {
            this.stack.clear();
        }

        void pop() {
            this.stack.remove(this.stack.size() - 1);
        }

        void push(String entry) {
            this.stack.add("\t- " + entry);
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            if (!this.stack.isEmpty()) {
                int i = this.stack.size();
                while (i > 0) {
                    buffer.append((String) this.stack.get(i - 1)).append(i != 1 ? "\n" : "");
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

        HandleTable(int initialCapacity, float loadFactor) {
            this.loadFactor = loadFactor;
            this.spine = new int[initialCapacity];
            this.next = new int[initialCapacity];
            this.objs = new Object[initialCapacity];
            this.threshold = (int) (((float) initialCapacity) * loadFactor);
            clear();
        }

        int assign(Object obj) {
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

        int lookup(Object obj) {
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

        void clear() {
            Arrays.fill(this.spine, -1);
            Arrays.fill(this.objs, 0, this.size, null);
            this.size = 0;
        }

        int size() {
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
            System.arraycopy(this.next, 0, newNext, 0, this.size);
            this.next = newNext;
            Object newObjs = new Object[newLength];
            System.arraycopy(this.objs, 0, newObjs, 0, this.size);
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

        PutFieldImpl(ObjectStreamClass desc) {
            this.desc = desc;
            this.primVals = new byte[desc.getPrimDataSize()];
            this.objVals = new Object[desc.getNumObjFields()];
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
            if (ObjectOutputStream.this != out) {
                throw new IllegalArgumentException("wrong stream");
            }
            out.write(this.primVals, 0, this.primVals.length);
            ObjectStreamField[] fields = this.desc.getFields(false);
            int numPrimFields = fields.length - this.objVals.length;
            for (int i = 0; i < this.objVals.length; i++) {
                if (fields[numPrimFields + i].isUnshared()) {
                    throw new IOException("cannot write unshared object");
                }
                out.writeObject(this.objVals[i]);
            }
        }

        void writeFields() throws IOException {
            ObjectOutputStream.this.bout.write(this.primVals, 0, this.primVals.length, false);
            ObjectStreamField[] fields = this.desc.getFields(false);
            int numPrimFields = fields.length - this.objVals.length;
            for (int i = 0; i < this.objVals.length; i++) {
                ObjectOutputStream.this.writeObject0(this.objVals[i], fields[numPrimFields + i].isUnshared());
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

        void assign(Object obj, Object rep) {
            int index = this.htab.assign(obj);
            while (index >= this.reps.length) {
                grow();
            }
            this.reps[index] = rep;
        }

        Object lookup(Object obj) {
            int index = this.htab.lookup(obj);
            return index >= 0 ? this.reps[index] : obj;
        }

        void clear() {
            Arrays.fill(this.reps, 0, this.htab.size(), null);
            this.htab.clear();
        }

        int size() {
            return this.htab.size();
        }

        private void grow() {
            Object newReps = new Object[((this.reps.length << 1) + 1)];
            System.arraycopy(this.reps, 0, newReps, 0, this.reps.length);
            this.reps = newReps;
        }
    }

    private static native void doublesToBytes(double[] dArr, int i, byte[] bArr, int i2, int i3);

    private static native void floatsToBytes(float[] fArr, int i, byte[] bArr, int i2, int i3);

    public ObjectOutputStream(OutputStream out) throws IOException {
        verifySubclass();
        this.bout = new BlockDataOutputStream(out);
        this.handles = new HandleTable(10, 3.0f);
        this.subs = new ReplaceTable(10, 3.0f);
        this.enableOverride = false;
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
        if (this.handles.size() != 0) {
            throw new IllegalStateException("stream non-empty");
        }
        switch (version) {
            case 1:
            case 2:
                this.protocol = version;
                return;
            default:
                throw new IllegalArgumentException("unknown version: " + version);
        }
    }

    public final void writeObject(Object obj) throws IOException {
        if (this.enableOverride) {
            writeObjectOverride(obj);
            return;
        }
        try {
            writeObject0(obj, false);
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

    protected void writeObjectOverride(Object obj) throws IOException {
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
        if (ctx == null) {
            throw new NotActiveException("not in call to writeObject");
        }
        Object curObj = ctx.getObj();
        ObjectStreamClass curDesc = ctx.getDesc();
        this.bout.setBlockDataMode(false);
        defaultWriteFields(curObj, curDesc);
        this.bout.setBlockDataMode(true);
    }

    public PutField putFields() throws IOException {
        if (this.curPut == null) {
            SerialCallbackContext ctx = this.curContext;
            if (ctx == null) {
                throw new NotActiveException("not in call to writeObject");
            }
            Object curObj = ctx.getObj();
            this.curPut = new PutFieldImpl(ctx.getDesc());
        }
        return this.curPut;
    }

    public void writeFields() throws IOException {
        if (this.curPut == null) {
            throw new NotActiveException("no current PutField object");
        }
        this.bout.setBlockDataMode(false);
        this.curPut.writeFields();
        this.bout.setBlockDataMode(true);
    }

    public void reset() throws IOException {
        if (this.depth != 0) {
            throw new IOException("stream active");
        }
        this.bout.setBlockDataMode(false);
        this.bout.writeByte(121);
        clear();
        this.bout.setBlockDataMode(true);
    }

    protected void annotateClass(Class<?> cls) throws IOException {
    }

    protected void annotateProxyClass(Class<?> cls) throws IOException {
    }

    protected Object replaceObject(Object obj) throws IOException {
        return obj;
    }

    protected boolean enableReplaceObject(boolean enable) throws SecurityException {
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
        return this.enableReplace ^ 1;
    }

    protected void writeStreamHeader() throws IOException {
        this.bout.writeShort(-21267);
        this.bout.writeShort(5);
    }

    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        desc.writeNonProxy(this);
    }

    public void write(int val) throws IOException {
        this.bout.write(val);
    }

    public void write(byte[] buf) throws IOException {
        this.bout.write(buf, 0, buf.length, false);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (buf == null) {
            throw new NullPointerException();
        }
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.bout.write(buf, off, len, false);
    }

    public void flush() throws IOException {
        this.bout.flush();
    }

    protected void drain() throws IOException {
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

    int getProtocolVersion() {
        return this.protocol;
    }

    void writeTypeString(String str) throws IOException {
        if (str == null) {
            writeNull();
            return;
        }
        int handle = this.handles.lookup(str);
        if (handle != -1) {
            writeHandle(handle);
        } else {
            writeString(str, false);
        }
    }

    private void verifySubclass() {
        Class<?> cl = getClass();
        if (cl != ObjectOutputStream.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                ObjectStreamClass.processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
                WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
                Boolean result = (Boolean) Caches.subclassAudits.get(key);
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
                Class<?> cl = subcl;
                while (cl != ObjectOutputStream.class) {
                    try {
                        cl.getDeclaredMethod("writeUnshared", Object.class);
                        return Boolean.FALSE;
                    } catch (NoSuchMethodException e) {
                        try {
                            cl.getDeclaredMethod("putFields", (Class[]) null);
                            return Boolean.FALSE;
                        } catch (NoSuchMethodException e2) {
                            cl = cl.getSuperclass();
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

    private void writeObject0(Object obj, boolean unshared) throws IOException {
        Throwable th;
        boolean oldMode = this.bout.setBlockDataMode(false);
        this.depth++;
        try {
            obj = this.subs.lookup(obj);
            if (obj == null) {
                writeNull();
                this.depth--;
                this.bout.setBlockDataMode(oldMode);
                return;
            }
            int h;
            Object obj2;
            if (!unshared) {
                h = this.handles.lookup(obj);
                if (h != -1) {
                    writeHandle(h);
                    this.depth--;
                    this.bout.setBlockDataMode(oldMode);
                    return;
                }
            }
            Object orig = obj;
            Class<?> cl = obj.getClass();
            ObjectStreamClass desc = ObjectStreamClass.lookup(cl, true);
            if (desc.hasWriteReplaceMethod()) {
                obj2 = desc.invokeWriteReplace(obj);
                if (obj2 != null) {
                    try {
                        Class<?> repCl = obj2.getClass();
                        if (repCl != cl) {
                            cl = repCl;
                            desc = ObjectStreamClass.lookup(repCl, true);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        obj = obj2;
                        this.depth--;
                        this.bout.setBlockDataMode(oldMode);
                        throw th;
                    }
                }
            }
            obj2 = obj;
            if (this.enableReplace) {
                Object rep = replaceObject(obj2);
                if (!(rep == obj2 || rep == null)) {
                    cl = rep.getClass();
                    desc = ObjectStreamClass.lookup(cl, true);
                }
                obj2 = rep;
            }
            if (obj2 != obj) {
                this.subs.assign(obj, obj2);
                if (obj2 == null) {
                    writeNull();
                    this.depth--;
                    this.bout.setBlockDataMode(oldMode);
                    return;
                } else if (!unshared) {
                    h = this.handles.lookup(obj2);
                    if (h != -1) {
                        writeHandle(h);
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
        } catch (Throwable th3) {
            th = th3;
            this.depth--;
            this.bout.setBlockDataMode(oldMode);
            throw th;
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
        writeClassDesc(ObjectStreamClass.lookup(cl, true), false);
        HandleTable handleTable = this.handles;
        if (unshared) {
            cl = null;
        }
        handleTable.assign(cl);
    }

    private void writeClassDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
        if (desc == null) {
            writeNull();
            return;
        }
        if (!unshared) {
            int handle = this.handles.lookup(desc);
            if (handle != -1) {
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
        return getClass().getClassLoader() != ObjectOutputStream.class.getClassLoader();
    }

    private void writeProxyDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
        Object obj = null;
        this.bout.writeByte(125);
        HandleTable handleTable = this.handles;
        if (!unshared) {
            ObjectStreamClass obj2 = desc;
        }
        handleTable.assign(obj2);
        Class cl = desc.forClass();
        Class<?>[] ifaces = cl.getInterfaces();
        this.bout.writeInt(ifaces.length);
        for (Class name : ifaces) {
            this.bout.writeUTF(name.getName());
        }
        this.bout.setBlockDataMode(true);
        if (cl != null && isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateProxyClass(cl);
        this.bout.setBlockDataMode(false);
        this.bout.writeByte(120);
        writeClassDesc(desc.getSuperDesc(), false);
    }

    private void writeNonProxyDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
        Object obj = null;
        this.bout.writeByte(114);
        HandleTable handleTable = this.handles;
        if (!unshared) {
            ObjectStreamClass obj2 = desc;
        }
        handleTable.assign(obj2);
        if (this.protocol == 1) {
            desc.writeNonProxy(this);
        } else {
            writeClassDescriptor(desc);
        }
        Class cl = desc.forClass();
        this.bout.setBlockDataMode(true);
        if (cl != null && isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateClass(cl);
        this.bout.setBlockDataMode(false);
        this.bout.writeByte(120);
        writeClassDesc(desc.getSuperDesc(), false);
    }

    private void writeString(String str, boolean unshared) throws IOException {
        Object obj;
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = null;
        } else {
            String obj2 = str;
        }
        handleTable.assign(obj2);
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
        Object obj;
        this.bout.writeByte(117);
        writeClassDesc(desc, false);
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = null;
        } else {
            obj = array;
        }
        handleTable.assign(obj);
        Class<?> ccl = desc.forClass().getComponentType();
        if (!ccl.isPrimitive()) {
            this.bout.writeInt(len);
            for (Object obj2 : (Object[]) array) {
                writeObject0(obj2, false);
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
        Object obj;
        this.bout.writeByte(126);
        ObjectStreamClass sdesc = desc.getSuperDesc();
        if (sdesc.forClass() != Enum.class) {
            desc = sdesc;
        }
        writeClassDesc(desc, false);
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = null;
        } else {
            Enum<?> obj2 = en;
        }
        handleTable.assign(obj2);
        writeString(en.name(), false);
    }

    private void writeOrdinaryObject(Object obj, ObjectStreamClass desc, boolean unshared) throws IOException {
        Object obj2;
        desc.checkSerialize();
        this.bout.writeByte(115);
        writeClassDesc(desc, false);
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj2 = null;
        } else {
            obj2 = obj;
        }
        handleTable.assign(obj2);
        if (!desc.isExternalizable() || (desc.isProxy() ^ 1) == 0) {
            writeSerialData(obj, desc);
        } else {
            writeExternalData((Externalizable) obj);
        }
    }

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
                this.bout.setBlockDataMode(false);
                this.bout.writeByte(120);
            }
            this.curContext = oldContext;
            this.curPut = oldPut;
        } catch (Throwable th) {
            this.curContext = oldContext;
        }
    }

    private void writeSerialData(Object obj, ObjectStreamClass desc) throws IOException {
        ClassDataSlot[] slots = desc.getClassDataLayout();
        for (ClassDataSlot classDataSlot : slots) {
            ObjectStreamClass slotDesc = classDataSlot.desc;
            if (slotDesc.hasWriteObjectMethod()) {
                PutFieldImpl oldPut = this.curPut;
                this.curPut = null;
                SerialCallbackContext oldContext = this.curContext;
                try {
                    this.curContext = new SerialCallbackContext(obj, slotDesc);
                    this.bout.setBlockDataMode(true);
                    slotDesc.invokeWriteObject(obj, this);
                    this.bout.setBlockDataMode(false);
                    this.bout.writeByte(120);
                    this.curPut = oldPut;
                } finally {
                    this.curContext.setUsed();
                    this.curContext = oldContext;
                }
            } else {
                defaultWriteFields(obj, slotDesc);
            }
        }
    }

    private void defaultWriteFields(Object obj, ObjectStreamClass desc) throws IOException {
        Class<?> cl = desc.forClass();
        if (cl == null || obj == null || (cl.isInstance(obj) ^ 1) == 0) {
            desc.checkDefaultSerialize();
            int primDataSize = desc.getPrimDataSize();
            if (this.primVals == null || this.primVals.length < primDataSize) {
                this.primVals = new byte[primDataSize];
            }
            desc.getPrimFieldValues(obj, this.primVals);
            this.bout.write(this.primVals, 0, primDataSize, false);
            ObjectStreamField[] fields = desc.getFields(false);
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
        boolean oldMode = this.bout.setBlockDataMode(false);
        try {
            this.bout.writeByte(123);
            writeObject0(ex, false);
            clear();
        } finally {
            this.bout.setBlockDataMode(oldMode);
        }
    }
}
