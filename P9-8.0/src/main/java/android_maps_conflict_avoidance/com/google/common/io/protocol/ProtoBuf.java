package android_maps_conflict_avoidance.com.google.common.io.protocol;

import android_maps_conflict_avoidance.com.google.common.io.IoUtil;
import android_maps_conflict_avoidance.com.google.common.io.MarkedOutputStream;
import android_maps_conflict_avoidance.com.google.common.util.IntMap;
import android_maps_conflict_avoidance.com.google.common.util.IntMap.KeyIterator;
import android_maps_conflict_avoidance.com.google.common.util.Primitives;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class ProtoBuf {
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final Boolean FALSE = new Boolean(false);
    private static final SimpleCounter NULL_COUNTER = new SimpleCounter();
    public static final Boolean TRUE = new Boolean(true);
    private int cachedSize = Integer.MIN_VALUE;
    private ProtoBufType msgType;
    private final IntMap values;
    private IntMap wireTypes;

    private static class SimpleCounter {
        public int count;

        private SimpleCounter() {
            this.count = 0;
        }
    }

    public ProtoBuf(ProtoBufType type) {
        this.msgType = type;
        this.values = new IntMap();
    }

    public void clear() {
        this.values.clear();
        this.wireTypes = null;
    }

    public void addProtoBuf(int tag, ProtoBuf value) {
        addObject(tag, value);
    }

    public boolean getBool(int tag) {
        return ((Boolean) getObject(tag, 24)).booleanValue();
    }

    public byte[] getBytes(int tag) {
        return (byte[]) getObject(tag, 25);
    }

    public int getInt(int tag) {
        return (int) ((Long) getObject(tag, 21)).longValue();
    }

    public long getLong(int tag) {
        return ((Long) getObject(tag, 19)).longValue();
    }

    public ProtoBuf getProtoBuf(int tag) {
        return (ProtoBuf) getObject(tag, 26);
    }

    public ProtoBuf getProtoBuf(int tag, int index) {
        return (ProtoBuf) getObject(tag, index, 26);
    }

    public String getString(int tag) {
        return (String) getObject(tag, 28);
    }

    public String getString(int tag, int index) {
        return (String) getObject(tag, index, 28);
    }

    public boolean has(int tag) {
        return getCount(tag) > 0 || getDefault(tag) != null;
    }

    public ProtoBuf parse(byte[] data) throws IOException {
        parse(new ByteArrayInputStream(data), data.length);
        return this;
    }

    public ProtoBuf parse(InputStream is) throws IOException {
        parse(is, Integer.MAX_VALUE);
        return this;
    }

    public int parse(InputStream is, int available) throws IOException {
        return parseInternal(is, available, true, new SimpleCounter());
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x0192  */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x0006 A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int parseInternal(InputStream is, int available, boolean clear, SimpleCounter counter) throws IOException {
        if (clear) {
            clear();
        }
        while (available > 0) {
            long tagAndType = readVarInt(is, true, counter);
            if (tagAndType != -1) {
                available -= counter.count;
                int wireType = ((int) tagAndType) & 7;
                if (wireType != 4) {
                    Object value;
                    int tag = (int) (tagAndType >>> 3);
                    int tagType = getType(tag);
                    if (tagType == 16) {
                        if (this.wireTypes == null) {
                            this.wireTypes = new IntMap();
                        }
                        this.wireTypes.put(tag, Primitives.toInteger(wireType));
                        tagType = wireType;
                    }
                    long v;
                    int count;
                    ProtoBuf value2;
                    switch (wireType) {
                        case LayoutParams.MODE_MAP /*0*/:
                            v = readVarInt(is, false, counter);
                            available -= counter.count;
                            if (isZigZagEncodedType(tag)) {
                                v = zigZagDecode(v);
                            }
                            value2 = Primitives.toLong(v);
                            break;
                        case 1:
                        case LayoutParams.RIGHT /*5*/:
                            v = 0;
                            int shift = 0;
                            count = wireType != 5 ? 8 : 4;
                            available -= count;
                            while (true) {
                                int count2 = count;
                                count = count2 - 1;
                                if (count2 <= 0) {
                                    value2 = Primitives.toLong(v);
                                    break;
                                }
                                v |= ((long) is.read()) << shift;
                                shift += 8;
                            }
                        case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                            int total = (int) readVarInt(is, false, counter);
                            available = (available - counter.count) - total;
                            if (tagType == 27) {
                                ProtoBuf msg = new ProtoBuf((ProtoBufType) this.msgType.getData(tag));
                                msg.parseInternal(is, total, false, counter);
                                value2 = msg;
                                break;
                            }
                            Object data = total != 0 ? new byte[total] : EMPTY_BYTE_ARRAY;
                            int pos = 0;
                            while (pos < total) {
                                count = is.read(data, pos, total - pos);
                                if (count > 0) {
                                    pos += count;
                                } else {
                                    throw new IOException("Unexp.EOF");
                                }
                            }
                            value2 = data;
                            break;
                        case LayoutParams.LEFT /*3*/:
                            ProtoBuf group = new ProtoBuf(this.msgType != null ? (ProtoBufType) this.msgType.getData(tag) : null);
                            available = group.parseInternal(is, available, false, counter);
                            value2 = group;
                            break;
                        default:
                            throw new IOException("Unknown wire type " + wireType + ", reading garbage data?");
                    }
                    addObject(tag, value2);
                }
            }
            if (available < 0) {
                return available;
            }
            throw new IOException();
        }
        if (available < 0) {
        }
    }

    private static int getCount(Object o) {
        if (o != null) {
            return !(o instanceof Vector) ? 1 : ((Vector) o).size();
        } else {
            return 0;
        }
    }

    public int getCount(int tag) {
        return getCount(this.values.get(tag));
    }

    public int getType(int tag) {
        Integer tagTypeObj = null;
        int tagType = 16;
        if (this.msgType != null) {
            tagType = this.msgType.getType(tag);
        }
        if (tagType == 16) {
            if (this.wireTypes != null) {
                tagTypeObj = (Integer) this.wireTypes.get(tag);
            }
            if (tagTypeObj != null) {
                tagType = tagTypeObj.intValue();
            }
        }
        if (tagType != 16 || getCount(tag) <= 0) {
            return tagType;
        }
        Object o = getObject(tag, 0, 16);
        return ((o instanceof Long) || (o instanceof Boolean)) ? 0 : 2;
    }

    private static int getVarIntSize(long i) {
        Object obj;
        if (i >= 0) {
            obj = 1;
        } else {
            obj = null;
        }
        if (obj == null) {
            return 10;
        }
        int size = 1;
        while (true) {
            if (i < 128) {
                obj = 1;
            } else {
                obj = null;
            }
            if (obj != null) {
                return size;
            }
            size++;
            i >>= 7;
        }
    }

    public void outputWithSizeTo(OutputStream os) throws IOException {
        outputTo(os, true);
    }

    public void outputTo(OutputStream os) throws IOException {
        outputTo(os, false);
    }

    private void outputTo(OutputStream os, boolean addSize) throws IOException {
        MarkedOutputStream mos = new MarkedOutputStream();
        int size = outputToInternal(mos);
        if (addSize) {
            ((DataOutput) os).writeInt(size);
        }
        int previous = 0;
        int n = mos.numMarkers();
        for (int i = 0; i < n; i += 2) {
            int current = mos.getMarker(i);
            mos.writeContentsTo(os, previous, current - previous);
            writeVarInt(os, (long) mos.getMarker(i + 1));
            previous = current;
        }
        if (previous < mos.availableContent()) {
            mos.writeContentsTo(os, previous, mos.availableContent() - previous);
        }
    }

    private int outputToInternal(MarkedOutputStream os) throws IOException {
        KeyIterator itr = this.values.keys();
        int totalSize = 0;
        while (itr.hasNext()) {
            totalSize += outputField(itr.next(), os);
        }
        return totalSize;
    }

    private int outputField(int tag, MarkedOutputStream os) throws IOException {
        int size = getCount(tag);
        int wireType = getWireType(tag);
        int wireTypeTag = (tag << 3) | wireType;
        int totalSize = 0;
        for (int i = 0; i < size; i++) {
            totalSize += writeVarInt(os, (long) wireTypeTag);
            boolean added = false;
            int contentStart = os.availableContent();
            long v;
            switch (wireType) {
                case LayoutParams.MODE_MAP /*0*/:
                    v = ((Long) getObject(tag, i, 19)).longValue();
                    if (isZigZagEncodedType(tag)) {
                        v = zigZagEncode(v);
                    }
                    writeVarInt(os, v);
                    break;
                case 1:
                case LayoutParams.RIGHT /*5*/:
                    v = ((Long) getObject(tag, i, 19)).longValue();
                    int cnt = wireType != 5 ? 8 : 4;
                    for (int b = 0; b < cnt; b++) {
                        os.write((int) (255 & v));
                        v >>= 8;
                    }
                    break;
                case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                    Object o = getObject(tag, i, getType(tag) != 27 ? 25 : 16);
                    if (!(o instanceof byte[])) {
                        os.addMarker(os.availableContent());
                        int tmpPos = os.numMarkers();
                        os.addMarker(-1);
                        int protoSize = ((ProtoBuf) o).outputToInternal(os);
                        os.setMarker(tmpPos, protoSize);
                        totalSize += getVarIntSize((long) protoSize) + protoSize;
                        added = true;
                        break;
                    }
                    byte[] data = (byte[]) o;
                    writeVarInt(os, (long) data.length);
                    os.write(data);
                    break;
                case LayoutParams.LEFT /*3*/:
                    totalSize = (totalSize + ((ProtoBuf) getObject(tag, i, 26)).outputToInternal(os)) + writeVarInt(os, (long) ((tag << 3) | 4));
                    added = true;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            if (!added) {
                totalSize += os.availableContent() - contentStart;
            }
        }
        return totalSize;
    }

    private boolean isZigZagEncodedType(int tag) {
        int declaredType = getType(tag);
        return declaredType == 33 || declaredType == 34;
    }

    private static long zigZagEncode(long v) {
        return (v << 1) ^ (-(v >>> 63));
    }

    private static long zigZagDecode(long v) {
        return (v >>> 1) ^ (-(1 & v));
    }

    public void setInt(int tag, int value) {
        setLong(tag, (long) value);
    }

    public void setLong(int tag, long value) {
        setObject(tag, Primitives.toLong(value));
    }

    public void setString(int tag, String value) {
        setObject(tag, value);
    }

    private void assertTypeMatch(int tag, Object object) {
    }

    private Object getDefault(int tag) {
        switch (getType(tag)) {
            case LayoutParams.CENTER_VERTICAL /*16*/:
            case 26:
            case 27:
                return null;
            default:
                return this.msgType.getData(tag);
        }
    }

    private static void checkTag(int tag) {
    }

    private Object getObject(int tag, int desiredType) {
        checkTag(tag);
        Object o = this.values.get(tag);
        int count = getCount(o);
        if (count == 0) {
            return getDefault(tag);
        }
        if (count <= 1) {
            return getObjectWithoutArgChecking(tag, 0, desiredType, o);
        }
        throw new IllegalArgumentException();
    }

    private Object getObject(int tag, int index, int desiredType) {
        checkTag(tag);
        Object o = this.values.get(tag);
        if (index < getCount(o)) {
            return getObjectWithoutArgChecking(tag, index, desiredType, o);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    private Object getObjectWithoutArgChecking(int tag, int index, int desiredType, Object o) {
        Vector v = null;
        if (o instanceof Vector) {
            v = (Vector) o;
            o = v.elementAt(index);
        }
        Object o2 = convert(o, desiredType);
        if (!(o2 == o || o == null)) {
            if (v != null) {
                v.setElementAt(o2, index);
            } else {
                setObject(tag, o2);
            }
        }
        return o2;
    }

    private final int getWireType(int tag) {
        int tagType = getType(tag);
        switch (tagType) {
            case LayoutParams.MODE_MAP /*0*/:
            case 1:
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
            case LayoutParams.LEFT /*3*/:
            case LayoutParams.RIGHT /*5*/:
            case LayoutParams.CENTER_VERTICAL /*16*/:
                return tagType;
            case LayoutParams.CENTER /*17*/:
            case 22:
            case 32:
                return 1;
            case 18:
            case 23:
            case 31:
                return 5;
            case 19:
            case 20:
            case 21:
            case 24:
            case 29:
            case 30:
            case 33:
            case 34:
                return 0;
            case 25:
            case 27:
            case 28:
            case 35:
            case 36:
                return 2;
            case 26:
                return 3;
            default:
                throw new RuntimeException("Unsupp.Type:" + this.msgType + '/' + tag + '/' + tagType);
        }
    }

    private void insertObject(int tag, int index, Object o, boolean appendToEnd) {
        checkTag(tag);
        Vector current = this.values.get(tag);
        Vector v = null;
        if (current instanceof Vector) {
            v = current;
        }
        if (current != null && (v == null || v.size() != 0)) {
            assertTypeMatch(tag, o);
            if (v == null) {
                v = new Vector();
                v.addElement(current);
                this.values.put(tag, v);
            }
            if (appendToEnd) {
                v.addElement(o);
                return;
            } else {
                v.insertElementAt(o, index);
                return;
            }
        }
        setObject(tag, o);
    }

    private void addObject(int tag, Object o) {
        insertObject(tag, 0, o, true);
    }

    private static Object convert(Object obj, int tagType) {
        switch (tagType) {
            case LayoutParams.CENTER_VERTICAL /*16*/:
                return obj;
            case 19:
            case 21:
            case 22:
            case 23:
            case 31:
            case 32:
            case 33:
            case 34:
                if (!(obj instanceof Boolean)) {
                    return obj;
                }
                return Primitives.toLong(!((Boolean) obj).booleanValue() ? 0 : 1);
            case 24:
                if (obj instanceof Boolean) {
                    return obj;
                }
                switch ((int) ((Long) obj).longValue()) {
                    case LayoutParams.MODE_MAP /*0*/:
                        return FALSE;
                    case 1:
                        return TRUE;
                    default:
                        throw new IllegalArgumentException("Type mismatch");
                }
            case 25:
            case 35:
                if (obj instanceof String) {
                    return IoUtil.encodeUtf8((String) obj);
                }
                if (!(obj instanceof ProtoBuf)) {
                    return obj;
                }
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                try {
                    ((ProtoBuf) obj).outputTo(buf);
                    return buf.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException(e.toString());
                }
            case 26:
            case 27:
                if (!(obj instanceof byte[])) {
                    return obj;
                }
                try {
                    return new ProtoBuf(null).parse((byte[]) obj);
                } catch (IOException e2) {
                    throw new RuntimeException(e2.toString());
                }
            case 28:
            case 36:
                if (!(obj instanceof byte[])) {
                    return obj;
                }
                byte[] data = (byte[]) obj;
                return IoUtil.decodeUtf8(data, 0, data.length, true);
            default:
                throw new RuntimeException("Unsupp.Type");
        }
    }

    private static long readVarInt(InputStream is, boolean permitEOF, SimpleCounter counter) throws IOException {
        long result = 0;
        int shift = 0;
        counter.count = 0;
        int i = 0;
        while (i < 10) {
            int in = is.read();
            if (in != -1) {
                result |= ((long) (in & 127)) << shift;
                if ((in & 128) == 0) {
                    break;
                }
                shift += 7;
                i++;
            } else if (i == 0 && permitEOF) {
                return -1;
            } else {
                throw new IOException("EOF");
            }
        }
        counter.count = i + 1;
        return result;
    }

    private void setObject(int tag, Object o) {
        if (tag >= 0) {
            if (o != null) {
                assertTypeMatch(tag, o);
            }
            this.values.put(tag, o);
            return;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    static int writeVarInt(OutputStream os, long value) throws IOException {
        int i = 0;
        while (i < 10) {
            int toWrite = (int) (127 & value);
            value >>>= 7;
            if (value == 0) {
                os.write(toWrite);
                return i + 1;
            }
            os.write(toWrite | 128);
            i++;
        }
        return i;
    }
}
