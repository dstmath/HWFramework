package android.icu.impl;

import android.icu.impl.ICUBinary;
import android.icu.impl.UResource;
import android.icu.impl.locale.BaseLocale;
import android.icu.lang.UCharacterEnums;
import android.icu.util.ICUException;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.ULocale;
import android.icu.util.UResourceTypeMismatchException;
import android.icu.util.VersionInfo;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

public final class ICUResourceBundleReader {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static ReaderCache CACHE = new ReaderCache();
    private static final int DATA_FORMAT = 1382380354;
    private static final boolean DEBUG = false;
    private static final CharBuffer EMPTY_16_BIT_UNITS = CharBuffer.wrap("\u0000");
    private static final Array EMPTY_ARRAY = new Array();
    private static final Table EMPTY_TABLE = new Table();
    private static final String ICU_RESOURCE_SUFFIX = ".res";
    private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
    static final int LARGE_SIZE = 24;
    /* access modifiers changed from: private */
    public static final ICUResourceBundleReader NULL_READER = new ICUResourceBundleReader();
    /* access modifiers changed from: private */
    public static int[] PUBLIC_TYPES = {0, 1, 2, 3, 2, 2, 0, 7, 8, 8, -1, -1, -1, -1, 14, -1};
    private static final int URES_ATT_IS_POOL_BUNDLE = 2;
    private static final int URES_ATT_NO_FALLBACK = 1;
    private static final int URES_ATT_USES_POOL_BUNDLE = 4;
    private static final int URES_INDEX_16BIT_TOP = 6;
    private static final int URES_INDEX_ATTRIBUTES = 5;
    private static final int URES_INDEX_BUNDLE_TOP = 3;
    private static final int URES_INDEX_KEYS_TOP = 1;
    private static final int URES_INDEX_LENGTH = 0;
    private static final int URES_INDEX_MAX_TABLE_LENGTH = 4;
    private static final int URES_INDEX_POOL_CHECKSUM = 7;
    private static final ByteBuffer emptyByteBuffer = ByteBuffer.allocate(0).asReadOnlyBuffer();
    private static final byte[] emptyBytes = new byte[0];
    private static final char[] emptyChars = new char[0];
    private static final int[] emptyInts = new int[0];
    private static final String emptyString = "";
    /* access modifiers changed from: private */
    public CharBuffer b16BitUnits;
    private ByteBuffer bytes;
    private int dataVersion;
    private boolean isPoolBundle;
    private byte[] keyBytes;
    private int localKeyLimit;
    private boolean noFallback;
    private ICUResourceBundleReader poolBundleReader;
    private int poolCheckSum;
    /* access modifiers changed from: private */
    public int poolStringIndex16Limit;
    /* access modifiers changed from: private */
    public int poolStringIndexLimit;
    private ResourceCache resourceCache;
    private int rootRes;
    private boolean usesPoolBundle;

    static class Array extends Container implements UResource.Array {
        Array() {
        }

        public boolean getValue(int i, UResource.Value value) {
            if (i < 0 || i >= this.size) {
                return false;
            }
            ReaderValue readerValue = (ReaderValue) value;
            readerValue.res = getContainerResource(readerValue.reader, i);
            return true;
        }
    }

    private static final class Array16 extends Array {
        /* access modifiers changed from: package-private */
        public int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer16Resource(reader, index);
        }

        Array16(ICUResourceBundleReader reader, int offset) {
            this.size = reader.b16BitUnits.charAt(offset);
            this.itemsOffset = offset + 1;
        }
    }

    private static final class Array32 extends Array {
        /* access modifiers changed from: package-private */
        public int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer32Resource(reader, index);
        }

        Array32(ICUResourceBundleReader reader, int offset) {
            int offset2 = reader.getResourceByteOffset(offset);
            this.size = reader.getInt(offset2);
            this.itemsOffset = offset2 + 4;
        }
    }

    static class Container {
        protected int itemsOffset;
        protected int size;

        public final int getSize() {
            return this.size;
        }

        /* access modifiers changed from: package-private */
        public int getContainerResource(ICUResourceBundleReader reader, int index) {
            return -1;
        }

        /* access modifiers changed from: protected */
        public int getContainer16Resource(ICUResourceBundleReader reader, int index) {
            if (index < 0 || this.size <= index) {
                return -1;
            }
            int res16 = reader.b16BitUnits.charAt(this.itemsOffset + index);
            if (res16 >= reader.poolStringIndex16Limit) {
                res16 = (res16 - reader.poolStringIndex16Limit) + reader.poolStringIndexLimit;
            }
            return 1610612736 | res16;
        }

        /* access modifiers changed from: protected */
        public int getContainer32Resource(ICUResourceBundleReader reader, int index) {
            if (index < 0 || this.size <= index) {
                return -1;
            }
            return reader.getInt(this.itemsOffset + (4 * index));
        }

        /* access modifiers changed from: package-private */
        public int getResource(ICUResourceBundleReader reader, String resKey) {
            return getContainerResource(reader, Integer.parseInt(resKey));
        }

        Container() {
        }
    }

    private static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] formatVersion) {
            return (formatVersion[0] == 1 && (formatVersion[1] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED) >= 1) || (2 <= formatVersion[0] && formatVersion[0] <= 3);
        }
    }

    private static class ReaderCache extends SoftCache<ReaderCacheKey, ICUResourceBundleReader, ClassLoader> {
        private ReaderCache() {
        }

        /* access modifiers changed from: protected */
        public ICUResourceBundleReader createInstance(ReaderCacheKey key, ClassLoader loader) {
            ByteBuffer inBytes;
            String fullName = ICUResourceBundleReader.getFullName(key.baseName, key.localeID);
            try {
                if (key.baseName == null || !key.baseName.startsWith(ICUData.ICU_BASE_NAME)) {
                    InputStream stream = ICUData.getStream(loader, fullName);
                    if (stream == null) {
                        return ICUResourceBundleReader.NULL_READER;
                    }
                    inBytes = ICUBinary.getByteBufferFromInputStreamAndCloseStream(stream);
                } else {
                    inBytes = ICUBinary.getData(loader, fullName, fullName.substring(ICUData.ICU_BASE_NAME.length() + 1));
                    if (inBytes == null) {
                        return ICUResourceBundleReader.NULL_READER;
                    }
                }
                ICUResourceBundleReader iCUResourceBundleReader = new ICUResourceBundleReader(inBytes, key.baseName, key.localeID, loader);
                return iCUResourceBundleReader;
            } catch (IOException ex) {
                throw new ICUUncheckedIOException("Data file " + fullName + " is corrupt - " + ex.getMessage(), ex);
            }
        }
    }

    private static class ReaderCacheKey {
        final String baseName;
        final String localeID;

        ReaderCacheKey(String baseName2, String localeID2) {
            this.baseName = baseName2 == null ? "" : baseName2;
            this.localeID = localeID2 == null ? "" : localeID2;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ReaderCacheKey)) {
                return false;
            }
            ReaderCacheKey info = (ReaderCacheKey) obj;
            if (!this.baseName.equals(info.baseName) || !this.localeID.equals(info.localeID)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return this.baseName.hashCode() ^ this.localeID.hashCode();
        }
    }

    static class ReaderValue extends UResource.Value {
        ICUResourceBundleReader reader;
        int res;

        ReaderValue() {
        }

        public int getType() {
            return ICUResourceBundleReader.PUBLIC_TYPES[ICUResourceBundleReader.RES_GET_TYPE(this.res)];
        }

        public String getString() {
            String s = this.reader.getString(this.res);
            if (s != null) {
                return s;
            }
            throw new UResourceTypeMismatchException("");
        }

        public String getAliasString() {
            String s = this.reader.getAlias(this.res);
            if (s != null) {
                return s;
            }
            throw new UResourceTypeMismatchException("");
        }

        public int getInt() {
            if (ICUResourceBundleReader.RES_GET_TYPE(this.res) == 7) {
                return ICUResourceBundleReader.RES_GET_INT(this.res);
            }
            throw new UResourceTypeMismatchException("");
        }

        public int getUInt() {
            if (ICUResourceBundleReader.RES_GET_TYPE(this.res) == 7) {
                return ICUResourceBundleReader.RES_GET_UINT(this.res);
            }
            throw new UResourceTypeMismatchException("");
        }

        public int[] getIntVector() {
            int[] iv = this.reader.getIntVector(this.res);
            if (iv != null) {
                return iv;
            }
            throw new UResourceTypeMismatchException("");
        }

        public ByteBuffer getBinary() {
            ByteBuffer bb = this.reader.getBinary(this.res);
            if (bb != null) {
                return bb;
            }
            throw new UResourceTypeMismatchException("");
        }

        public UResource.Array getArray() {
            Array array = this.reader.getArray(this.res);
            if (array != null) {
                return array;
            }
            throw new UResourceTypeMismatchException("");
        }

        public UResource.Table getTable() {
            Table table = this.reader.getTable(this.res);
            if (table != null) {
                return table;
            }
            throw new UResourceTypeMismatchException("");
        }

        public boolean isNoInheritanceMarker() {
            return this.reader.isNoInheritanceMarker(this.res);
        }

        public String[] getStringArray() {
            Array array = this.reader.getArray(this.res);
            if (array != null) {
                return getStringArray(array);
            }
            throw new UResourceTypeMismatchException("");
        }

        public String[] getStringArrayOrStringAsArray() {
            Array array = this.reader.getArray(this.res);
            if (array != null) {
                return getStringArray(array);
            }
            String s = this.reader.getString(this.res);
            if (s != null) {
                return new String[]{s};
            }
            throw new UResourceTypeMismatchException("");
        }

        public String getStringOrFirstOfArray() {
            String s = this.reader.getString(this.res);
            if (s != null) {
                return s;
            }
            Array array = this.reader.getArray(this.res);
            if (array != null && array.size > 0) {
                String s2 = this.reader.getString(array.getContainerResource(this.reader, 0));
                if (s2 != null) {
                    return s2;
                }
            }
            throw new UResourceTypeMismatchException("");
        }

        private String[] getStringArray(Array array) {
            String[] result = new String[array.size];
            int i = 0;
            while (i < array.size) {
                String s = this.reader.getString(array.getContainerResource(this.reader, i));
                if (s != null) {
                    result[i] = s;
                    i++;
                } else {
                    throw new UResourceTypeMismatchException("");
                }
            }
            return result;
        }
    }

    private static final class ResourceCache {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final int NEXT_BITS = 6;
        private static final int ROOT_BITS = 7;
        private static final int SIMPLE_LENGTH = 32;
        private int[] keys = new int[32];
        private int length;
        private int levelBitsList;
        private int maxOffsetBits = 28;
        private Level rootLevel;
        private Object[] values = new Object[32];

        private static final class Level {
            static final /* synthetic */ boolean $assertionsDisabled = false;
            int[] keys;
            int levelBitsList;
            int mask;
            int shift;
            Object[] values;

            static {
                Class<ICUResourceBundleReader> cls = ICUResourceBundleReader.class;
            }

            Level(int levelBitsList2, int shift2) {
                this.levelBitsList = levelBitsList2;
                this.shift = shift2;
                int length = 1 << (levelBitsList2 & 15);
                this.mask = length - 1;
                this.keys = new int[length];
                this.values = new Object[length];
            }

            /* access modifiers changed from: package-private */
            public Object get(int key) {
                int index = (key >> this.shift) & this.mask;
                int k = this.keys[index];
                if (k == key) {
                    return this.values[index];
                }
                if (k == 0) {
                    Level level = (Level) this.values[index];
                    if (level != null) {
                        return level.get(key);
                    }
                }
                return null;
            }

            /* access modifiers changed from: package-private */
            public Object putIfAbsent(int key, Object item, int size) {
                int index = (key >> this.shift) & this.mask;
                int k = this.keys[index];
                if (k == key) {
                    return ResourceCache.putIfCleared(this.values, index, item, size);
                }
                if (k == 0) {
                    Level level = (Level) this.values[index];
                    if (level != null) {
                        return level.putIfAbsent(key, item, size);
                    }
                    this.keys[index] = key;
                    this.values[index] = ResourceCache.storeDirectly(size) ? item : new SoftReference(item);
                    return item;
                }
                Level level2 = new Level(this.levelBitsList >> 4, this.shift + (this.levelBitsList & 15));
                int i = (k >> level2.shift) & level2.mask;
                level2.keys[i] = k;
                level2.values[i] = this.values[index];
                this.keys[index] = 0;
                this.values[index] = level2;
                return level2.putIfAbsent(key, item, size);
            }
        }

        static {
            Class<ICUResourceBundleReader> cls = ICUResourceBundleReader.class;
        }

        /* access modifiers changed from: private */
        public static boolean storeDirectly(int size) {
            return size < 24 || CacheValue.futureInstancesWillBeStrong();
        }

        /* access modifiers changed from: private */
        public static final Object putIfCleared(Object[] values2, int index, Object item, int size) {
            SoftReference softReference = values2[index];
            if (!(softReference instanceof SoftReference)) {
                return softReference;
            }
            Object value = softReference.get();
            if (value != null) {
                return value;
            }
            values2[index] = CacheValue.futureInstancesWillBeStrong() ? item : new SoftReference(item);
            return item;
        }

        ResourceCache(int maxOffset) {
            while (maxOffset <= 134217727) {
                maxOffset <<= 1;
                this.maxOffsetBits--;
            }
            int keyBits = this.maxOffsetBits + 2;
            if (keyBits <= 7) {
                this.levelBitsList = keyBits;
            } else if (keyBits < 10) {
                this.levelBitsList = (keyBits - 3) | 48;
            } else {
                this.levelBitsList = 7;
                int keyBits2 = keyBits - 7;
                int shift = 4;
                while (keyBits2 > 6) {
                    if (keyBits2 < 9) {
                        this.levelBitsList |= (48 | (keyBits2 - 3)) << shift;
                        return;
                    }
                    this.levelBitsList = (6 << shift) | this.levelBitsList;
                    keyBits2 -= 6;
                    shift += 4;
                }
                this.levelBitsList |= keyBits2 << shift;
            }
        }

        private int makeKey(int res) {
            int miniType;
            int type = ICUResourceBundleReader.RES_GET_TYPE(res);
            if (type == 6) {
                miniType = 1;
            } else if (type == 5) {
                miniType = 3;
            } else {
                miniType = type == 9 ? 2 : 0;
            }
            return ICUResourceBundleReader.RES_GET_OFFSET(res) | (miniType << this.maxOffsetBits);
        }

        private int findSimple(int key) {
            int start = 0;
            int limit = this.length;
            while (limit - start > 8) {
                int mid = (start + limit) / 2;
                if (key < this.keys[mid]) {
                    limit = mid;
                } else {
                    start = mid;
                }
            }
            while (start < limit) {
                int k = this.keys[start];
                if (key < k) {
                    return ~start;
                }
                if (key == k) {
                    return start;
                }
                start++;
            }
            return ~start;
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0032, code lost:
            return r0;
         */
        public synchronized Object get(int res) {
            Object value;
            if (this.length >= 0) {
                int index = findSimple(res);
                if (index < 0) {
                    return null;
                }
                value = this.values[index];
            } else {
                value = this.rootLevel.get(makeKey(res));
                if (value == null) {
                    return null;
                }
            }
            if (value instanceof SoftReference) {
                value = ((SoftReference) value).get();
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized Object putIfAbsent(int res, Object item, int size) {
            if (this.length >= 0) {
                int index = findSimple(res);
                if (index >= 0) {
                    return putIfCleared(this.values, index, item, size);
                } else if (this.length < 32) {
                    int index2 = ~index;
                    if (index2 < this.length) {
                        System.arraycopy(this.keys, index2, this.keys, index2 + 1, this.length - index2);
                        System.arraycopy(this.values, index2, this.values, index2 + 1, this.length - index2);
                    }
                    this.length++;
                    this.keys[index2] = res;
                    this.values[index2] = storeDirectly(size) ? item : new SoftReference(item);
                    return item;
                } else {
                    this.rootLevel = new Level(this.levelBitsList, 0);
                    for (int i = 0; i < 32; i++) {
                        this.rootLevel.putIfAbsent(makeKey(this.keys[i]), this.values[i], 0);
                    }
                    this.keys = null;
                    this.values = null;
                    this.length = -1;
                }
            }
            return this.rootLevel.putIfAbsent(makeKey(res), item, size);
        }
    }

    static class Table extends Container implements UResource.Table {
        private static final int URESDATA_ITEM_NOT_FOUND = -1;
        protected int[] key32Offsets;
        protected char[] keyOffsets;

        Table() {
        }

        /* access modifiers changed from: package-private */
        public String getKey(ICUResourceBundleReader reader, int index) {
            String str;
            if (index < 0 || this.size <= index) {
                return null;
            }
            if (this.keyOffsets != null) {
                str = reader.getKey16String(this.keyOffsets[index]);
            } else {
                str = reader.getKey32String(this.key32Offsets[index]);
            }
            return str;
        }

        /* access modifiers changed from: package-private */
        public int findTableItem(ICUResourceBundleReader reader, CharSequence key) {
            int result;
            int start = 0;
            int limit = this.size;
            while (start < limit) {
                int mid = (start + limit) >>> 1;
                if (this.keyOffsets != null) {
                    result = reader.compareKeys(key, this.keyOffsets[mid]);
                } else {
                    result = reader.compareKeys32(key, this.key32Offsets[mid]);
                }
                if (result < 0) {
                    limit = mid;
                } else if (result <= 0) {
                    return mid;
                } else {
                    start = mid + 1;
                }
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        public int getResource(ICUResourceBundleReader reader, String resKey) {
            return getContainerResource(reader, findTableItem(reader, resKey));
        }

        public boolean getKeyAndValue(int i, UResource.Key key, UResource.Value value) {
            if (i < 0 || i >= this.size) {
                return false;
            }
            ReaderValue readerValue = (ReaderValue) value;
            if (this.keyOffsets != null) {
                readerValue.reader.setKeyFromKey16(this.keyOffsets[i], key);
            } else {
                readerValue.reader.setKeyFromKey32(this.key32Offsets[i], key);
            }
            readerValue.res = getContainerResource(readerValue.reader, i);
            return true;
        }
    }

    private static final class Table16 extends Table {
        /* access modifiers changed from: package-private */
        public int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer16Resource(reader, index);
        }

        Table16(ICUResourceBundleReader reader, int offset) {
            this.keyOffsets = reader.getTable16KeyOffsets(offset);
            this.size = this.keyOffsets.length;
            this.itemsOffset = offset + 1 + this.size;
        }
    }

    private static final class Table1632 extends Table {
        /* access modifiers changed from: package-private */
        public int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer32Resource(reader, index);
        }

        Table1632(ICUResourceBundleReader reader, int offset) {
            int offset2 = reader.getResourceByteOffset(offset);
            this.keyOffsets = reader.getTableKeyOffsets(offset2);
            this.size = this.keyOffsets.length;
            this.itemsOffset = (2 * ((this.size + 2) & -2)) + offset2;
        }
    }

    private static final class Table32 extends Table {
        /* access modifiers changed from: package-private */
        public int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer32Resource(reader, index);
        }

        Table32(ICUResourceBundleReader reader, int offset) {
            int offset2 = reader.getResourceByteOffset(offset);
            this.key32Offsets = reader.getTable32KeyOffsets(offset2);
            this.size = this.key32Offsets.length;
            this.itemsOffset = (4 * (1 + this.size)) + offset2;
        }
    }

    private ICUResourceBundleReader() {
    }

    private ICUResourceBundleReader(ByteBuffer inBytes, String baseName, String localeID, ClassLoader loader) throws IOException {
        init(inBytes);
        if (this.usesPoolBundle) {
            this.poolBundleReader = getReader(baseName, "pool", loader);
            if (this.poolBundleReader == null || !this.poolBundleReader.isPoolBundle) {
                throw new IllegalStateException("pool.res is not a pool bundle");
            } else if (this.poolBundleReader.poolCheckSum != this.poolCheckSum) {
                throw new IllegalStateException("pool.res has a different checksum than this bundle");
            }
        }
    }

    static ICUResourceBundleReader getReader(String baseName, String localeID, ClassLoader root) {
        ICUResourceBundleReader reader = (ICUResourceBundleReader) CACHE.getInstance(new ReaderCacheKey(baseName, localeID), root);
        if (reader == NULL_READER) {
            return null;
        }
        return reader;
    }

    private void init(ByteBuffer inBytes) throws IOException {
        this.dataVersion = ICUBinary.readHeader(inBytes, DATA_FORMAT, IS_ACCEPTABLE);
        int majorFormatVersion = inBytes.get(16);
        this.bytes = ICUBinary.sliceWithOrder(inBytes);
        int dataLength = this.bytes.remaining();
        this.rootRes = this.bytes.getInt(0);
        int indexes0 = getIndexesInt(0);
        int indexLength = indexes0 & 255;
        if (indexLength > 4) {
            if (dataLength >= ((1 + indexLength) << 2)) {
                int indexesInt = getIndexesInt(3);
                int bundleTop = indexesInt;
                if (dataLength >= (indexesInt << 2)) {
                    int maxOffset = bundleTop - 1;
                    if (majorFormatVersion >= 3) {
                        this.poolStringIndexLimit = indexes0 >>> 8;
                    }
                    if (indexLength > 5) {
                        int att = getIndexesInt(5);
                        this.noFallback = (att & 1) != 0;
                        this.isPoolBundle = (att & 2) != 0;
                        this.usesPoolBundle = (att & 4) != 0;
                        this.poolStringIndexLimit |= (61440 & att) << 12;
                        this.poolStringIndex16Limit = att >>> 16;
                    }
                    int keysBottom = 1 + indexLength;
                    int keysTop = getIndexesInt(1);
                    if (keysTop > keysBottom) {
                        if (this.isPoolBundle) {
                            this.keyBytes = new byte[((keysTop - keysBottom) << 2)];
                            this.bytes.position(keysBottom << 2);
                        } else {
                            this.localKeyLimit = keysTop << 2;
                            this.keyBytes = new byte[this.localKeyLimit];
                        }
                        this.bytes.get(this.keyBytes);
                    }
                    if (indexLength > 6) {
                        int _16BitTop = getIndexesInt(6);
                        if (_16BitTop > keysTop) {
                            this.bytes.position(keysTop << 2);
                            this.b16BitUnits = this.bytes.asCharBuffer();
                            this.b16BitUnits.limit((_16BitTop - keysTop) * 2);
                            maxOffset |= ((_16BitTop - keysTop) * 2) - 1;
                        } else {
                            this.b16BitUnits = EMPTY_16_BIT_UNITS;
                        }
                    } else {
                        this.b16BitUnits = EMPTY_16_BIT_UNITS;
                    }
                    if (indexLength > 7) {
                        this.poolCheckSum = getIndexesInt(7);
                    }
                    if (!this.isPoolBundle || this.b16BitUnits.length() > 1) {
                        this.resourceCache = new ResourceCache(maxOffset);
                    }
                    this.bytes.position(0);
                    return;
                }
            }
            throw new ICUException("not enough bytes");
        }
        throw new ICUException("not enough indexes");
    }

    private int getIndexesInt(int i) {
        return this.bytes.getInt((1 + i) << 2);
    }

    /* access modifiers changed from: package-private */
    public VersionInfo getVersion() {
        return ICUBinary.getVersionInfoFromCompactInt(this.dataVersion);
    }

    /* access modifiers changed from: package-private */
    public int getRootResource() {
        return this.rootRes;
    }

    /* access modifiers changed from: package-private */
    public boolean getNoFallback() {
        return this.noFallback;
    }

    /* access modifiers changed from: package-private */
    public boolean getUsesPoolBundle() {
        return this.usesPoolBundle;
    }

    static int RES_GET_TYPE(int res) {
        return res >>> 28;
    }

    /* access modifiers changed from: private */
    public static int RES_GET_OFFSET(int res) {
        return 268435455 & res;
    }

    /* access modifiers changed from: private */
    public int getResourceByteOffset(int offset) {
        return offset << 2;
    }

    static int RES_GET_INT(int res) {
        return (res << 4) >> 4;
    }

    static int RES_GET_UINT(int res) {
        return 268435455 & res;
    }

    static boolean URES_IS_ARRAY(int type) {
        return type == 8 || type == 9;
    }

    static boolean URES_IS_TABLE(int type) {
        return type == 2 || type == 5 || type == 4;
    }

    private char[] getChars(int offset, int count) {
        char[] chars = new char[count];
        if (count <= 16) {
            for (int i = 0; i < count; i++) {
                chars[i] = this.bytes.getChar(offset);
                offset += 2;
            }
        } else {
            CharBuffer temp = this.bytes.asCharBuffer();
            temp.position(offset / 2);
            temp.get(chars);
        }
        return chars;
    }

    /* access modifiers changed from: private */
    public int getInt(int offset) {
        return this.bytes.getInt(offset);
    }

    private int[] getInts(int offset, int count) {
        int[] ints = new int[count];
        if (count <= 16) {
            for (int i = 0; i < count; i++) {
                ints[i] = this.bytes.getInt(offset);
                offset += 4;
            }
        } else {
            IntBuffer temp = this.bytes.asIntBuffer();
            temp.position(offset / 4);
            temp.get(ints);
        }
        return ints;
    }

    /* access modifiers changed from: private */
    public char[] getTable16KeyOffsets(int offset) {
        int offset2 = offset + 1;
        int length = this.b16BitUnits.charAt(offset);
        if (length <= 0) {
            return emptyChars;
        }
        char[] result = new char[length];
        if (length <= 16) {
            int i = 0;
            while (i < length) {
                result[i] = this.b16BitUnits.charAt(offset2);
                i++;
                offset2++;
            }
        } else {
            CharBuffer temp = this.b16BitUnits.duplicate();
            temp.position(offset2);
            temp.get(result);
        }
        return result;
    }

    /* access modifiers changed from: private */
    public char[] getTableKeyOffsets(int offset) {
        int length = this.bytes.getChar(offset);
        if (length > 0) {
            return getChars(offset + 2, length);
        }
        return emptyChars;
    }

    /* access modifiers changed from: private */
    public int[] getTable32KeyOffsets(int offset) {
        int length = getInt(offset);
        if (length > 0) {
            return getInts(offset + 4, length);
        }
        return emptyInts;
    }

    private static String makeKeyStringFromBytes(byte[] keyBytes2, int keyOffset) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            byte b = keyBytes2[keyOffset];
            byte b2 = b;
            if (b == 0) {
                return sb.toString();
            }
            keyOffset++;
            sb.append((char) b2);
        }
    }

    /* access modifiers changed from: private */
    public String getKey16String(int keyOffset) {
        if (keyOffset < this.localKeyLimit) {
            return makeKeyStringFromBytes(this.keyBytes, keyOffset);
        }
        return makeKeyStringFromBytes(this.poolBundleReader.keyBytes, keyOffset - this.localKeyLimit);
    }

    /* access modifiers changed from: private */
    public String getKey32String(int keyOffset) {
        if (keyOffset >= 0) {
            return makeKeyStringFromBytes(this.keyBytes, keyOffset);
        }
        return makeKeyStringFromBytes(this.poolBundleReader.keyBytes, Integer.MAX_VALUE & keyOffset);
    }

    /* access modifiers changed from: private */
    public void setKeyFromKey16(int keyOffset, UResource.Key key) {
        if (keyOffset < this.localKeyLimit) {
            key.setBytes(this.keyBytes, keyOffset);
        } else {
            key.setBytes(this.poolBundleReader.keyBytes, keyOffset - this.localKeyLimit);
        }
    }

    /* access modifiers changed from: private */
    public void setKeyFromKey32(int keyOffset, UResource.Key key) {
        if (keyOffset >= 0) {
            key.setBytes(this.keyBytes, keyOffset);
        } else {
            key.setBytes(this.poolBundleReader.keyBytes, Integer.MAX_VALUE & keyOffset);
        }
    }

    /* access modifiers changed from: private */
    public int compareKeys(CharSequence key, char keyOffset) {
        if (keyOffset < this.localKeyLimit) {
            return ICUBinary.compareKeys(key, this.keyBytes, (int) keyOffset);
        }
        return ICUBinary.compareKeys(key, this.poolBundleReader.keyBytes, keyOffset - this.localKeyLimit);
    }

    /* access modifiers changed from: private */
    public int compareKeys32(CharSequence key, int keyOffset) {
        if (keyOffset >= 0) {
            return ICUBinary.compareKeys(key, this.keyBytes, keyOffset);
        }
        return ICUBinary.compareKeys(key, this.poolBundleReader.keyBytes, Integer.MAX_VALUE & keyOffset);
    }

    /* access modifiers changed from: package-private */
    public String getStringV2(int res) {
        String s;
        int length;
        int offset;
        int offset2 = RES_GET_OFFSET(res);
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (String) value;
        }
        int first = this.b16BitUnits.charAt(offset2);
        if ((first & -1024) == 56320) {
            if (first < 57327) {
                length = first & Opcodes.OP_NEW_INSTANCE_JUMBO;
                offset = offset2 + 1;
            } else if (first < 57343) {
                length = ((first - 57327) << 16) | this.b16BitUnits.charAt(offset2 + 1);
                offset = offset2 + 2;
            } else {
                length = (this.b16BitUnits.charAt(offset2 + 1) << 16) | this.b16BitUnits.charAt(offset2 + 2);
                offset = offset2 + 3;
            }
            s = this.b16BitUnits.subSequence(offset, offset + length).toString();
        } else if (first == 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append((char) first);
            while (true) {
                offset2++;
                char charAt = this.b16BitUnits.charAt(offset2);
                char c = charAt;
                if (charAt == 0) {
                    break;
                }
                sb.append(c);
            }
            s = sb.toString();
        }
        return (String) this.resourceCache.putIfAbsent(res, s, s.length() * 2);
    }

    private String makeStringFromBytes(int offset, int length) {
        if (length <= 16) {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(this.bytes.getChar(offset));
                offset += 2;
            }
            return sb.toString();
        }
        int offset2 = offset / 2;
        return this.bytes.asCharBuffer().subSequence(offset2, offset2 + length).toString();
    }

    /* access modifiers changed from: package-private */
    public String getString(int res) {
        int offset = RES_GET_OFFSET(res);
        if (res != offset && RES_GET_TYPE(res) != 6) {
            return null;
        }
        if (offset == 0) {
            return "";
        }
        if (res == offset) {
            Object value = this.resourceCache.get(res);
            if (value != null) {
                return (String) value;
            }
            int offset2 = getResourceByteOffset(offset);
            String s = makeStringFromBytes(offset2 + 4, getInt(offset2));
            return (String) this.resourceCache.putIfAbsent(res, s, s.length() * 2);
        } else if (offset < this.poolStringIndexLimit) {
            return this.poolBundleReader.getStringV2(res);
        } else {
            return getStringV2(res - this.poolStringIndexLimit);
        }
    }

    /* access modifiers changed from: private */
    public boolean isNoInheritanceMarker(int res) {
        int offset = RES_GET_OFFSET(res);
        boolean z = false;
        if (offset != 0) {
            if (res == offset) {
                int offset2 = getResourceByteOffset(offset);
                if (getInt(offset2) == 3 && this.bytes.getChar(offset2 + 4) == 8709 && this.bytes.getChar(offset2 + 6) == 8709 && this.bytes.getChar(offset2 + 8) == 8709) {
                    z = true;
                }
                return z;
            } else if (RES_GET_TYPE(res) == 6) {
                if (offset < this.poolStringIndexLimit) {
                    return this.poolBundleReader.isStringV2NoInheritanceMarker(offset);
                }
                return isStringV2NoInheritanceMarker(offset - this.poolStringIndexLimit);
            }
        }
        return false;
    }

    private boolean isStringV2NoInheritanceMarker(int offset) {
        int first = this.b16BitUnits.charAt(offset);
        boolean z = false;
        if (first == 8709) {
            if (this.b16BitUnits.charAt(offset + 1) == 8709 && this.b16BitUnits.charAt(offset + 2) == 8709 && this.b16BitUnits.charAt(offset + 3) == 0) {
                z = true;
            }
            return z;
        } else if (first != 56323) {
            return false;
        } else {
            if (this.b16BitUnits.charAt(offset + 1) == 8709 && this.b16BitUnits.charAt(offset + 2) == 8709 && this.b16BitUnits.charAt(offset + 3) == 8709) {
                z = true;
            }
            return z;
        }
    }

    /* access modifiers changed from: package-private */
    public String getAlias(int res) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != 3) {
            return null;
        }
        if (offset == 0) {
            return "";
        }
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (String) value;
        }
        int offset2 = getResourceByteOffset(offset);
        int length = getInt(offset2);
        return (String) this.resourceCache.putIfAbsent(res, makeStringFromBytes(offset2 + 4, length), length * 2);
    }

    /* access modifiers changed from: package-private */
    public byte[] getBinary(int res, byte[] ba) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != 1) {
            return null;
        }
        if (offset == 0) {
            return emptyBytes;
        }
        int offset2 = getResourceByteOffset(offset);
        int length = getInt(offset2);
        if (length == 0) {
            return emptyBytes;
        }
        if (ba == null || ba.length != length) {
            ba = new byte[length];
        }
        int offset3 = offset2 + 4;
        if (length <= 16) {
            int i = 0;
            while (i < length) {
                ba[i] = this.bytes.get(offset3);
                i++;
                offset3++;
            }
        } else {
            ByteBuffer temp = this.bytes.duplicate();
            temp.position(offset3);
            temp.get(ba);
        }
        return ba;
    }

    /* access modifiers changed from: package-private */
    public ByteBuffer getBinary(int res) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != 1) {
            return null;
        }
        if (offset == 0) {
            return emptyByteBuffer.duplicate();
        }
        int offset2 = getResourceByteOffset(offset);
        int length = getInt(offset2);
        if (length == 0) {
            return emptyByteBuffer.duplicate();
        }
        int offset3 = offset2 + 4;
        ByteBuffer result = this.bytes.duplicate();
        result.position(offset3).limit(offset3 + length);
        ByteBuffer result2 = ICUBinary.sliceWithOrder(result);
        if (!result2.isReadOnly()) {
            result2 = result2.asReadOnlyBuffer();
        }
        return result2;
    }

    /* access modifiers changed from: package-private */
    public int[] getIntVector(int res) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != 14) {
            return null;
        }
        if (offset == 0) {
            return emptyInts;
        }
        int offset2 = getResourceByteOffset(offset);
        return getInts(offset2 + 4, getInt(offset2));
    }

    /* access modifiers changed from: package-private */
    public Array getArray(int res) {
        int type = RES_GET_TYPE(res);
        if (!URES_IS_ARRAY(type)) {
            return null;
        }
        int offset = RES_GET_OFFSET(res);
        if (offset == 0) {
            return EMPTY_ARRAY;
        }
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (Array) value;
        }
        return (Array) this.resourceCache.putIfAbsent(res, type == 8 ? new Array32(this, offset) : new Array16(this, offset), 0);
    }

    /* access modifiers changed from: package-private */
    public Table getTable(int res) {
        int size;
        Table table;
        int type = RES_GET_TYPE(res);
        if (!URES_IS_TABLE(type)) {
            return null;
        }
        int offset = RES_GET_OFFSET(res);
        if (offset == 0) {
            return EMPTY_TABLE;
        }
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (Table) value;
        }
        if (type == 2) {
            table = new Table1632(this, offset);
            size = table.getSize() * 2;
        } else if (type == 5) {
            table = new Table16(this, offset);
            size = table.getSize() * 2;
        } else {
            table = new Table32(this, offset);
            size = table.getSize() * 4;
        }
        return (Table) this.resourceCache.putIfAbsent(res, table, size);
    }

    public static String getFullName(String baseName, String localeName) {
        if (baseName == null || baseName.length() == 0) {
            if (localeName.length() == 0) {
                String uLocale = ULocale.getDefault().toString();
                String localeName2 = uLocale;
                return uLocale;
            }
            return localeName + ICU_RESOURCE_SUFFIX;
        } else if (baseName.indexOf(46) != -1) {
            String baseName2 = baseName.replace('.', '/');
            if (localeName.length() == 0) {
                return baseName2 + ICU_RESOURCE_SUFFIX;
            }
            return baseName2 + BaseLocale.SEP + localeName + ICU_RESOURCE_SUFFIX;
        } else if (baseName.charAt(baseName.length() - 1) != '/') {
            return baseName + "/" + localeName + ICU_RESOURCE_SUFFIX;
        } else {
            return baseName + localeName + ICU_RESOURCE_SUFFIX;
        }
    }
}
