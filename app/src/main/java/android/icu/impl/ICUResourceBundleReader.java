package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.UResource.ArraySink;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.TableSink;
import android.icu.impl.UResource.Value;
import android.icu.impl.locale.BaseLocale;
import android.icu.text.UTF16;
import android.icu.util.AnnualTimeZoneRule;
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
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static ReaderCache CACHE = null;
    private static final int DATA_FORMAT = 1382380354;
    private static final boolean DEBUG = false;
    private static final CharBuffer EMPTY_16_BIT_UNITS = null;
    private static final Array EMPTY_ARRAY = null;
    private static final Table EMPTY_TABLE = null;
    private static final String ICU_RESOURCE_SUFFIX = ".res";
    private static final IsAcceptable IS_ACCEPTABLE = null;
    static final int LARGE_SIZE = 24;
    private static final ICUResourceBundleReader NULL_READER = null;
    private static int[] PUBLIC_TYPES = null;
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
    private static final ByteBuffer emptyByteBuffer = null;
    private static final byte[] emptyBytes = null;
    private static final char[] emptyChars = null;
    private static final int[] emptyInts = null;
    private static final String emptyString = "";
    private CharBuffer b16BitUnits;
    private ByteBuffer bytes;
    private int dataVersion;
    private boolean isPoolBundle;
    private byte[] keyBytes;
    private int localKeyLimit;
    private boolean noFallback;
    private ICUResourceBundleReader poolBundleReader;
    private int poolCheckSum;
    private int poolStringIndex16Limit;
    private int poolStringIndexLimit;
    private ResourceCache resourceCache;
    private int rootRes;
    private boolean usesPoolBundle;

    static class Container {
        protected int itemsOffset;
        protected int size;

        final int getSize() {
            return this.size;
        }

        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return -1;
        }

        protected int getContainer16Resource(ICUResourceBundleReader reader, int index) {
            if (index < 0 || this.size <= index) {
                return -1;
            }
            int res16 = reader.b16BitUnits.charAt(this.itemsOffset + index);
            if (res16 >= reader.poolStringIndex16Limit) {
                res16 = (res16 - reader.poolStringIndex16Limit) + reader.poolStringIndexLimit;
            }
            return 1610612736 | res16;
        }

        protected int getContainer32Resource(ICUResourceBundleReader reader, int index) {
            if (index < 0 || this.size <= index) {
                return -1;
            }
            return reader.getInt(this.itemsOffset + (index * ICUResourceBundleReader.URES_INDEX_MAX_TABLE_LENGTH));
        }

        int getResource(ICUResourceBundleReader reader, String resKey) {
            return getContainerResource(reader, Integer.parseInt(resKey));
        }

        Container() {
        }
    }

    static class Array extends Container {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUResourceBundleReader.Array.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUResourceBundleReader.Array.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.Array.<clinit>():void");
        }

        Array() {
        }

        void getAllItems(ICUResourceBundleReader reader, Key key, ReaderValue value, ArraySink sink) {
            for (int i = ICUResourceBundleReader.URES_INDEX_LENGTH; i < this.size; i += ICUResourceBundleReader.URES_INDEX_KEYS_TOP) {
                int res = getContainerResource(reader, i);
                int type = ICUResourceBundleReader.RES_GET_TYPE(res);
                int numItems;
                if (ICUResourceBundleReader.URES_IS_ARRAY(type)) {
                    numItems = reader.getArrayLength(res);
                    ArraySink subSink = sink.getOrCreateArraySink(i, numItems);
                    if (subSink != null) {
                        Array array = reader.getArray(res);
                        if (!-assertionsDisabled) {
                            if ((array.size == numItems ? ICUResourceBundleReader.URES_INDEX_KEYS_TOP : ICUResourceBundleReader.URES_INDEX_LENGTH) == null) {
                                throw new AssertionError();
                            }
                        }
                        array.getAllItems(reader, key, value, subSink);
                    } else {
                        continue;
                    }
                } else if (ICUResourceBundleReader.URES_IS_TABLE(type)) {
                    numItems = reader.getTableLength(res);
                    TableSink subSink2 = sink.getOrCreateTableSink(i, numItems);
                    if (subSink2 != null) {
                        Table table = reader.getTable(res);
                        if (!-assertionsDisabled) {
                            if ((table.size == numItems ? ICUResourceBundleReader.URES_INDEX_KEYS_TOP : ICUResourceBundleReader.URES_INDEX_LENGTH) == null) {
                                throw new AssertionError();
                            }
                        }
                        table.getAllItems(reader, key, value, subSink2);
                    } else {
                        continue;
                    }
                } else {
                    value.res = res;
                    sink.put(i, value);
                }
            }
            sink.leave();
        }
    }

    private static final class Array16 extends Array {
        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer16Resource(reader, index);
        }

        Array16(ICUResourceBundleReader reader, int offset) {
            this.size = reader.b16BitUnits.charAt(offset);
            this.itemsOffset = offset + ICUResourceBundleReader.URES_INDEX_KEYS_TOP;
        }
    }

    private static final class Array32 extends Array {
        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer32Resource(reader, index);
        }

        Array32(ICUResourceBundleReader reader, int offset) {
            offset = reader.getResourceByteOffset(offset);
            this.size = reader.getInt(offset);
            this.itemsOffset = offset + ICUResourceBundleReader.URES_INDEX_MAX_TABLE_LENGTH;
        }
    }

    private static final class IsAcceptable implements Authenticate {
        /* synthetic */ IsAcceptable(IsAcceptable isAcceptable) {
            this();
        }

        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] formatVersion) {
            if (formatVersion[ICUResourceBundleReader.URES_INDEX_LENGTH] == (byte) 1 && (formatVersion[ICUResourceBundleReader.URES_INDEX_KEYS_TOP] & Opcodes.OP_CONST_CLASS_JUMBO) >= ICUResourceBundleReader.URES_INDEX_KEYS_TOP) {
                return true;
            }
            if (ICUResourceBundleReader.URES_ATT_IS_POOL_BUNDLE > formatVersion[ICUResourceBundleReader.URES_INDEX_LENGTH] || formatVersion[ICUResourceBundleReader.URES_INDEX_LENGTH] > ICUResourceBundleReader.URES_INDEX_BUNDLE_TOP) {
                return ICUResourceBundleReader.DEBUG;
            }
            return true;
        }
    }

    private static class ReaderCache extends SoftCache<ReaderCacheKey, ICUResourceBundleReader, ClassLoader> {
        /* synthetic */ ReaderCache(ReaderCache readerCache) {
            this();
        }

        private ReaderCache() {
        }

        protected /* bridge */ /* synthetic */ Object createInstance(Object key, Object loader) {
            return createInstance((ReaderCacheKey) key, (ClassLoader) loader);
        }

        protected ICUResourceBundleReader createInstance(ReaderCacheKey key, ClassLoader loader) {
            String fullName = ICUResourceBundleReader.getFullName(key.baseName, key.localeID);
            try {
                ByteBuffer inBytes;
                if (key.baseName == null || !key.baseName.startsWith(ICUResourceBundle.ICU_BASE_NAME)) {
                    InputStream stream = ICUData.getStream(loader, fullName);
                    if (stream == null) {
                        return ICUResourceBundleReader.NULL_READER;
                    }
                    inBytes = ICUBinary.getByteBufferFromInputStreamAndCloseStream(stream);
                } else {
                    inBytes = ICUBinary.getData(loader, fullName, fullName.substring(ICUResourceBundle.ICU_BASE_NAME.length() + ICUResourceBundleReader.URES_INDEX_KEYS_TOP));
                    if (inBytes == null) {
                        return ICUResourceBundleReader.NULL_READER;
                    }
                }
                return new ICUResourceBundleReader(inBytes, key.baseName, key.localeID, loader, null);
            } catch (IOException ex) {
                throw new ICUUncheckedIOException("Data file " + fullName + " is corrupt - " + ex.getMessage(), ex);
            }
        }
    }

    private static class ReaderCacheKey {
        final String baseName;
        final String localeID;

        ReaderCacheKey(String baseName, String localeID) {
            if (baseName == null) {
                baseName = ICUResourceBundleReader.emptyString;
            }
            this.baseName = baseName;
            if (localeID == null) {
                localeID = ICUResourceBundleReader.emptyString;
            }
            this.localeID = localeID;
        }

        public boolean equals(Object obj) {
            boolean z = ICUResourceBundleReader.DEBUG;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ReaderCacheKey)) {
                return ICUResourceBundleReader.DEBUG;
            }
            ReaderCacheKey info = (ReaderCacheKey) obj;
            if (this.baseName.equals(info.baseName)) {
                z = this.localeID.equals(info.localeID);
            }
            return z;
        }

        public int hashCode() {
            return this.baseName.hashCode() ^ this.localeID.hashCode();
        }
    }

    static class ReaderValue extends Value {
        ICUResourceBundleReader reader;
        private int res;

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
            throw new UResourceTypeMismatchException(ICUResourceBundleReader.emptyString);
        }

        public String getAliasString() {
            String s = this.reader.getAlias(this.res);
            if (s != null) {
                return s;
            }
            throw new UResourceTypeMismatchException(ICUResourceBundleReader.emptyString);
        }

        public int getInt() {
            if (ICUResourceBundleReader.RES_GET_TYPE(this.res) == ICUResourceBundleReader.URES_INDEX_POOL_CHECKSUM) {
                return ICUResourceBundleReader.RES_GET_INT(this.res);
            }
            throw new UResourceTypeMismatchException(ICUResourceBundleReader.emptyString);
        }

        public int getUInt() {
            if (ICUResourceBundleReader.RES_GET_TYPE(this.res) == ICUResourceBundleReader.URES_INDEX_POOL_CHECKSUM) {
                return ICUResourceBundleReader.RES_GET_UINT(this.res);
            }
            throw new UResourceTypeMismatchException(ICUResourceBundleReader.emptyString);
        }

        public int[] getIntVector() {
            int[] iv = this.reader.getIntVector(this.res);
            if (iv != null) {
                return iv;
            }
            throw new UResourceTypeMismatchException(ICUResourceBundleReader.emptyString);
        }

        public ByteBuffer getBinary() {
            ByteBuffer bb = this.reader.getBinary(this.res);
            if (bb != null) {
                return bb;
            }
            throw new UResourceTypeMismatchException(ICUResourceBundleReader.emptyString);
        }
    }

    private static final class ResourceCache {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final int NEXT_BITS = 6;
        private static final int ROOT_BITS = 7;
        private static final int SIMPLE_LENGTH = 32;
        private int[] keys;
        private int length;
        private int levelBitsList;
        private int maxOffsetBits;
        private Level rootLevel;
        private Object[] values;

        private static final class Level {
            static final /* synthetic */ boolean -assertionsDisabled = false;
            int[] keys;
            int levelBitsList;
            int mask;
            int shift;
            Object[] values;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.Level.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.Level.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 9 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 10 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.ResourceCache.Level.<clinit>():void");
            }

            Level(int levelBitsList, int shift) {
                int i = ICUResourceBundleReader.URES_INDEX_LENGTH;
                this.levelBitsList = levelBitsList;
                this.shift = shift;
                int bits = levelBitsList & 15;
                if (!-assertionsDisabled) {
                    if (bits != 0) {
                        i = ICUResourceBundleReader.URES_INDEX_KEYS_TOP;
                    }
                    if (i == 0) {
                        throw new AssertionError();
                    }
                }
                int length = ICUResourceBundleReader.URES_INDEX_KEYS_TOP << bits;
                this.mask = length - 1;
                this.keys = new int[length];
                this.values = new Object[length];
            }

            Object get(int key) {
                int index = (key >> this.shift) & this.mask;
                int k = this.keys[index];
                if (k == key) {
                    return this.values[index];
                }
                if (k == 0) {
                    Level level = this.values[index];
                    if (level != null) {
                        return level.get(key);
                    }
                }
                return null;
            }

            Object putIfAbsent(int key, Object item, int size) {
                int index = (key >> this.shift) & this.mask;
                int k = this.keys[index];
                if (k == key) {
                    return ResourceCache.putIfCleared(this.values, index, item, size);
                }
                Level level;
                if (k == 0) {
                    level = this.values[index];
                    if (level != null) {
                        return level.putIfAbsent(key, item, size);
                    }
                    this.keys[index] = key;
                    this.values[index] = ResourceCache.storeDirectly(size) ? item : new SoftReference(item);
                    return item;
                }
                level = new Level(this.levelBitsList >> ICUResourceBundleReader.URES_INDEX_MAX_TABLE_LENGTH, this.shift + (this.levelBitsList & 15));
                int i = (k >> level.shift) & level.mask;
                level.keys[i] = k;
                level.values[i] = this.values[index];
                this.keys[index] = ICUResourceBundleReader.URES_INDEX_LENGTH;
                this.values[index] = level;
                return level.putIfAbsent(key, item, size);
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.ResourceCache.<clinit>():void");
        }

        private int findSimple(int r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.findSimple(int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.ResourceCache.findSimple(int):int");
        }

        synchronized java.lang.Object putIfAbsent(int r1, java.lang.Object r2, int r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.putIfAbsent(int, java.lang.Object, int):java.lang.Object
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.ResourceCache.putIfAbsent(int, java.lang.Object, int):java.lang.Object");
        }

        private static boolean storeDirectly(int size) {
            return size >= ICUResourceBundleReader.LARGE_SIZE ? CacheValue.futureInstancesWillBeStrong() : true;
        }

        private static final Object putIfCleared(Object[] values, int index, Object item, int size) {
            Object value = values[index];
            if (!(value instanceof SoftReference)) {
                return value;
            }
            if (!-assertionsDisabled) {
                if ((size >= ICUResourceBundleReader.LARGE_SIZE ? ICUResourceBundleReader.URES_INDEX_KEYS_TOP : null) == null) {
                    throw new AssertionError();
                }
            }
            value = ((SoftReference) value).get();
            if (value != null) {
                return value;
            }
            SoftReference softReference;
            if (CacheValue.futureInstancesWillBeStrong()) {
                softReference = item;
            } else {
                softReference = new SoftReference(item);
            }
            values[index] = softReference;
            return item;
        }

        ResourceCache(int maxOffset) {
            Object obj = null;
            this.keys = new int[SIMPLE_LENGTH];
            this.values = new Object[SIMPLE_LENGTH];
            if (!-assertionsDisabled) {
                if (maxOffset != 0) {
                    obj = ICUResourceBundleReader.URES_INDEX_KEYS_TOP;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            this.maxOffsetBits = 28;
            while (maxOffset <= 134217727) {
                maxOffset <<= ICUResourceBundleReader.URES_INDEX_KEYS_TOP;
                this.maxOffsetBits--;
            }
            int keyBits = this.maxOffsetBits + ICUResourceBundleReader.URES_ATT_IS_POOL_BUNDLE;
            if (keyBits <= ROOT_BITS) {
                this.levelBitsList = keyBits;
            } else if (keyBits < 10) {
                this.levelBitsList = (keyBits - 3) | 48;
            } else {
                this.levelBitsList = ROOT_BITS;
                keyBits -= 7;
                int shift = ICUResourceBundleReader.URES_INDEX_MAX_TABLE_LENGTH;
                while (keyBits > NEXT_BITS) {
                    if (keyBits < 9) {
                        this.levelBitsList |= ((keyBits - 3) | 48) << shift;
                        return;
                    }
                    this.levelBitsList |= NEXT_BITS << shift;
                    keyBits -= 6;
                    shift += ICUResourceBundleReader.URES_INDEX_MAX_TABLE_LENGTH;
                }
                this.levelBitsList |= keyBits << shift;
            }
        }

        private int makeKey(int res) {
            int type = ICUResourceBundleReader.RES_GET_TYPE(res);
            int miniType = type == NEXT_BITS ? ICUResourceBundleReader.URES_INDEX_KEYS_TOP : type == ICUResourceBundleReader.URES_INDEX_ATTRIBUTES ? ICUResourceBundleReader.URES_INDEX_BUNDLE_TOP : type == 9 ? ICUResourceBundleReader.URES_ATT_IS_POOL_BUNDLE : ICUResourceBundleReader.URES_INDEX_LENGTH;
            return ICUResourceBundleReader.RES_GET_OFFSET(res) | (miniType << this.maxOffsetBits);
        }

        synchronized Object get(int res) {
            Object obj = null;
            synchronized (this) {
                Object value;
                if (!-assertionsDisabled) {
                    if (ICUResourceBundleReader.RES_GET_OFFSET(res) != 0) {
                        obj = ICUResourceBundleReader.URES_INDEX_KEYS_TOP;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                if (this.length >= 0) {
                    int index = findSimple(res);
                    if (index >= 0) {
                        value = this.values[index];
                    } else {
                        return null;
                    }
                }
                value = this.rootLevel.get(makeKey(res));
                if (value == null) {
                    return null;
                }
                if (value instanceof SoftReference) {
                    value = ((SoftReference) value).get();
                }
                return value;
            }
        }
    }

    static class Table extends Container {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final int URESDATA_ITEM_NOT_FOUND = -1;
        protected int[] key32Offsets;
        protected char[] keyOffsets;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUResourceBundleReader.Table.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUResourceBundleReader.Table.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.Table.<clinit>():void");
        }

        String getKey(ICUResourceBundleReader reader, int index) {
            if (index < 0 || this.size <= index) {
                return null;
            }
            String -wrap11;
            if (this.keyOffsets != null) {
                -wrap11 = reader.getKey16String(this.keyOffsets[index]);
            } else {
                -wrap11 = reader.getKey32String(this.key32Offsets[index]);
            }
            return -wrap11;
        }

        int findTableItem(ICUResourceBundleReader reader, CharSequence key) {
            int start = ICUResourceBundleReader.URES_INDEX_LENGTH;
            int limit = this.size;
            while (start < limit) {
                int result;
                int mid = (start + limit) >>> ICUResourceBundleReader.URES_INDEX_KEYS_TOP;
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
                    start = mid + ICUResourceBundleReader.URES_INDEX_KEYS_TOP;
                }
            }
            return URESDATA_ITEM_NOT_FOUND;
        }

        int getResource(ICUResourceBundleReader reader, String resKey) {
            return getContainerResource(reader, findTableItem(reader, resKey));
        }

        void getAllItems(ICUResourceBundleReader reader, Key key, ReaderValue value, TableSink sink) {
            for (int i = ICUResourceBundleReader.URES_INDEX_LENGTH; i < this.size; i += ICUResourceBundleReader.URES_INDEX_KEYS_TOP) {
                if (this.keyOffsets != null) {
                    reader.setKeyFromKey16(this.keyOffsets[i], key);
                } else {
                    reader.setKeyFromKey32(this.key32Offsets[i], key);
                }
                int res = getContainerResource(reader, i);
                int type = ICUResourceBundleReader.RES_GET_TYPE(res);
                int numItems;
                if (ICUResourceBundleReader.URES_IS_ARRAY(type)) {
                    numItems = reader.getArrayLength(res);
                    ArraySink subSink = sink.getOrCreateArraySink(key, numItems);
                    if (subSink != null) {
                        Array array = reader.getArray(res);
                        if (!-assertionsDisabled) {
                            if ((array.size == numItems ? ICUResourceBundleReader.URES_INDEX_KEYS_TOP : ICUResourceBundleReader.URES_INDEX_LENGTH) == null) {
                                throw new AssertionError();
                            }
                        }
                        array.getAllItems(reader, key, value, subSink);
                    } else {
                        continue;
                    }
                } else if (ICUResourceBundleReader.URES_IS_TABLE(type)) {
                    numItems = reader.getTableLength(res);
                    TableSink subSink2 = sink.getOrCreateTableSink(key, numItems);
                    if (subSink2 != null) {
                        Table table = reader.getTable(res);
                        if (!-assertionsDisabled) {
                            if ((table.size == numItems ? ICUResourceBundleReader.URES_INDEX_KEYS_TOP : ICUResourceBundleReader.URES_INDEX_LENGTH) == null) {
                                throw new AssertionError();
                            }
                        }
                        table.getAllItems(reader, key, value, subSink2);
                    } else {
                        continue;
                    }
                } else if (reader.isNoInheritanceMarker(res)) {
                    sink.putNoFallback(key);
                } else {
                    value.res = res;
                    sink.put(key, value);
                }
            }
            sink.leave();
        }

        Table() {
        }
    }

    private static final class Table1632 extends Table {
        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer32Resource(reader, index);
        }

        Table1632(ICUResourceBundleReader reader, int offset) {
            offset = reader.getResourceByteOffset(offset);
            this.keyOffsets = reader.getTableKeyOffsets(offset);
            this.size = this.keyOffsets.length;
            this.itemsOffset = (((this.size + ICUResourceBundleReader.URES_ATT_IS_POOL_BUNDLE) & -2) * ICUResourceBundleReader.URES_ATT_IS_POOL_BUNDLE) + offset;
        }
    }

    private static final class Table16 extends Table {
        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer16Resource(reader, index);
        }

        Table16(ICUResourceBundleReader reader, int offset) {
            this.keyOffsets = reader.getTable16KeyOffsets(offset);
            this.size = this.keyOffsets.length;
            this.itemsOffset = (offset + ICUResourceBundleReader.URES_INDEX_KEYS_TOP) + this.size;
        }
    }

    private static final class Table32 extends Table {
        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer32Resource(reader, index);
        }

        Table32(ICUResourceBundleReader reader, int offset) {
            offset = reader.getResourceByteOffset(offset);
            this.key32Offsets = reader.getTable32KeyOffsets(offset);
            this.size = this.key32Offsets.length;
            this.itemsOffset = ((this.size + ICUResourceBundleReader.URES_INDEX_KEYS_TOP) * ICUResourceBundleReader.URES_INDEX_MAX_TABLE_LENGTH) + offset;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUResourceBundleReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUResourceBundleReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.<clinit>():void");
    }

    /* synthetic */ ICUResourceBundleReader(ByteBuffer inBytes, String baseName, String localeID, ClassLoader loader, ICUResourceBundleReader iCUResourceBundleReader) {
        this(inBytes, baseName, localeID, loader);
    }

    private ICUResourceBundleReader() {
    }

    private ICUResourceBundleReader(ByteBuffer inBytes, String baseName, String localeID, ClassLoader loader) throws IOException {
        init(inBytes);
        if (this.usesPoolBundle) {
            this.poolBundleReader = getReader(baseName, "pool", loader);
            if (!this.poolBundleReader.isPoolBundle) {
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
        this.rootRes = this.bytes.getInt(URES_INDEX_LENGTH);
        int indexes0 = getIndexesInt(URES_INDEX_LENGTH);
        int indexLength = indexes0 & Opcodes.OP_CONST_CLASS_JUMBO;
        if (indexLength <= URES_INDEX_MAX_TABLE_LENGTH) {
            throw new ICUException("not enough indexes");
        }
        if (dataLength >= ((indexLength + URES_INDEX_KEYS_TOP) << URES_ATT_IS_POOL_BUNDLE)) {
            int bundleTop = getIndexesInt(URES_INDEX_BUNDLE_TOP);
            if (dataLength >= (bundleTop << URES_ATT_IS_POOL_BUNDLE)) {
                int maxOffset = bundleTop - 1;
                if (majorFormatVersion >= URES_INDEX_BUNDLE_TOP) {
                    this.poolStringIndexLimit = indexes0 >>> 8;
                }
                if (indexLength > URES_INDEX_ATTRIBUTES) {
                    int att = getIndexesInt(URES_INDEX_ATTRIBUTES);
                    this.noFallback = (att & URES_INDEX_KEYS_TOP) != 0 ? true : DEBUG;
                    this.isPoolBundle = (att & URES_ATT_IS_POOL_BUNDLE) != 0 ? true : DEBUG;
                    this.usesPoolBundle = (att & URES_INDEX_MAX_TABLE_LENGTH) != 0 ? true : DEBUG;
                    this.poolStringIndexLimit |= (61440 & att) << 12;
                    this.poolStringIndex16Limit = att >>> 16;
                }
                int keysBottom = indexLength + URES_INDEX_KEYS_TOP;
                int keysTop = getIndexesInt(URES_INDEX_KEYS_TOP);
                if (keysTop > keysBottom) {
                    if (this.isPoolBundle) {
                        this.keyBytes = new byte[((keysTop - keysBottom) << URES_ATT_IS_POOL_BUNDLE)];
                        this.bytes.position(keysBottom << URES_ATT_IS_POOL_BUNDLE);
                    } else {
                        this.localKeyLimit = keysTop << URES_ATT_IS_POOL_BUNDLE;
                        this.keyBytes = new byte[this.localKeyLimit];
                    }
                    this.bytes.get(this.keyBytes);
                }
                if (indexLength > URES_INDEX_16BIT_TOP) {
                    int _16BitTop = getIndexesInt(URES_INDEX_16BIT_TOP);
                    if (_16BitTop > keysTop) {
                        int num16BitUnits = (_16BitTop - keysTop) * URES_ATT_IS_POOL_BUNDLE;
                        this.bytes.position(keysTop << URES_ATT_IS_POOL_BUNDLE);
                        this.b16BitUnits = this.bytes.asCharBuffer();
                        this.b16BitUnits.limit(num16BitUnits);
                        maxOffset |= num16BitUnits - 1;
                    } else {
                        this.b16BitUnits = EMPTY_16_BIT_UNITS;
                    }
                } else {
                    this.b16BitUnits = EMPTY_16_BIT_UNITS;
                }
                if (indexLength > URES_INDEX_POOL_CHECKSUM) {
                    this.poolCheckSum = getIndexesInt(URES_INDEX_POOL_CHECKSUM);
                }
                if (!this.isPoolBundle || this.b16BitUnits.length() > URES_INDEX_KEYS_TOP) {
                    this.resourceCache = new ResourceCache(maxOffset);
                }
                this.bytes.position(URES_INDEX_LENGTH);
                return;
            }
        }
        throw new ICUException("not enough bytes");
    }

    private int getIndexesInt(int i) {
        return this.bytes.getInt((i + URES_INDEX_KEYS_TOP) << URES_ATT_IS_POOL_BUNDLE);
    }

    VersionInfo getVersion() {
        return ICUBinary.getVersionInfoFromCompactInt(this.dataVersion);
    }

    int getRootResource() {
        return this.rootRes;
    }

    boolean getNoFallback() {
        return this.noFallback;
    }

    boolean getUsesPoolBundle() {
        return this.usesPoolBundle;
    }

    static int RES_GET_TYPE(int res) {
        return res >>> 28;
    }

    private static int RES_GET_OFFSET(int res) {
        return 268435455 & res;
    }

    private int getResourceByteOffset(int offset) {
        return offset << URES_ATT_IS_POOL_BUNDLE;
    }

    static int RES_GET_INT(int res) {
        return (res << URES_INDEX_MAX_TABLE_LENGTH) >> URES_INDEX_MAX_TABLE_LENGTH;
    }

    static int RES_GET_UINT(int res) {
        return 268435455 & res;
    }

    static boolean URES_IS_ARRAY(int type) {
        return (type == 8 || type == 9) ? true : DEBUG;
    }

    static boolean URES_IS_TABLE(int type) {
        return (type == URES_ATT_IS_POOL_BUNDLE || type == URES_INDEX_ATTRIBUTES || type == URES_INDEX_MAX_TABLE_LENGTH) ? true : DEBUG;
    }

    private char[] getChars(int offset, int count) {
        char[] chars = new char[count];
        if (count <= 16) {
            for (int i = URES_INDEX_LENGTH; i < count; i += URES_INDEX_KEYS_TOP) {
                chars[i] = this.bytes.getChar(offset);
                offset += URES_ATT_IS_POOL_BUNDLE;
            }
        } else {
            CharBuffer temp = this.bytes.asCharBuffer();
            temp.position(offset / URES_ATT_IS_POOL_BUNDLE);
            temp.get(chars);
        }
        return chars;
    }

    private int getInt(int offset) {
        return this.bytes.getInt(offset);
    }

    private int[] getInts(int offset, int count) {
        int[] ints = new int[count];
        if (count <= 16) {
            for (int i = URES_INDEX_LENGTH; i < count; i += URES_INDEX_KEYS_TOP) {
                ints[i] = this.bytes.getInt(offset);
                offset += URES_INDEX_MAX_TABLE_LENGTH;
            }
        } else {
            IntBuffer temp = this.bytes.asIntBuffer();
            temp.position(offset / URES_INDEX_MAX_TABLE_LENGTH);
            temp.get(ints);
        }
        return ints;
    }

    private char[] getTable16KeyOffsets(int offset) {
        int offset2 = offset + URES_INDEX_KEYS_TOP;
        int length = this.b16BitUnits.charAt(offset);
        if (length <= 0) {
            return emptyChars;
        }
        char[] result = new char[length];
        if (length <= 16) {
            int i = URES_INDEX_LENGTH;
            while (i < length) {
                offset = offset2 + URES_INDEX_KEYS_TOP;
                result[i] = this.b16BitUnits.charAt(offset2);
                i += URES_INDEX_KEYS_TOP;
                offset2 = offset;
            }
            offset = offset2;
        } else {
            CharBuffer temp = this.b16BitUnits.duplicate();
            temp.position(offset2);
            temp.get(result);
            offset = offset2;
        }
        return result;
    }

    private char[] getTableKeyOffsets(int offset) {
        int length = this.bytes.getChar(offset);
        if (length > 0) {
            return getChars(offset + URES_ATT_IS_POOL_BUNDLE, length);
        }
        return emptyChars;
    }

    private int[] getTable32KeyOffsets(int offset) {
        int length = getInt(offset);
        if (length > 0) {
            return getInts(offset + URES_INDEX_MAX_TABLE_LENGTH, length);
        }
        return emptyInts;
    }

    private static String makeKeyStringFromBytes(byte[] keyBytes, int keyOffset) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            byte b = keyBytes[keyOffset];
            if (b == null) {
                return sb.toString();
            }
            keyOffset += URES_INDEX_KEYS_TOP;
            sb.append((char) b);
        }
    }

    private String getKey16String(int keyOffset) {
        if (keyOffset < this.localKeyLimit) {
            return makeKeyStringFromBytes(this.keyBytes, keyOffset);
        }
        return makeKeyStringFromBytes(this.poolBundleReader.keyBytes, keyOffset - this.localKeyLimit);
    }

    private String getKey32String(int keyOffset) {
        if (keyOffset >= 0) {
            return makeKeyStringFromBytes(this.keyBytes, keyOffset);
        }
        return makeKeyStringFromBytes(this.poolBundleReader.keyBytes, AnnualTimeZoneRule.MAX_YEAR & keyOffset);
    }

    private void setKeyFromKey16(int keyOffset, Key key) {
        if (keyOffset < this.localKeyLimit) {
            key.setBytes(this.keyBytes, keyOffset);
        } else {
            key.setBytes(this.poolBundleReader.keyBytes, keyOffset - this.localKeyLimit);
        }
    }

    private void setKeyFromKey32(int keyOffset, Key key) {
        if (keyOffset >= 0) {
            key.setBytes(this.keyBytes, keyOffset);
        } else {
            key.setBytes(this.poolBundleReader.keyBytes, AnnualTimeZoneRule.MAX_YEAR & keyOffset);
        }
    }

    private int compareKeys(CharSequence key, char keyOffset) {
        if (keyOffset < this.localKeyLimit) {
            return ICUBinary.compareKeys(key, this.keyBytes, (int) keyOffset);
        }
        return ICUBinary.compareKeys(key, this.poolBundleReader.keyBytes, keyOffset - this.localKeyLimit);
    }

    private int compareKeys32(CharSequence key, int keyOffset) {
        if (keyOffset >= 0) {
            return ICUBinary.compareKeys(key, this.keyBytes, keyOffset);
        }
        return ICUBinary.compareKeys(key, this.poolBundleReader.keyBytes, AnnualTimeZoneRule.MAX_YEAR & keyOffset);
    }

    String getStringV2(int res) {
        Object obj = URES_INDEX_KEYS_TOP;
        if (!-assertionsDisabled) {
            if ((RES_GET_TYPE(res) == URES_INDEX_16BIT_TOP ? URES_INDEX_KEYS_TOP : URES_INDEX_LENGTH) == null) {
                throw new AssertionError();
            }
        }
        int offset = RES_GET_OFFSET(res);
        if (!-assertionsDisabled) {
            if (offset == 0) {
                obj = URES_INDEX_LENGTH;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (String) value;
        }
        String s;
        int first = this.b16BitUnits.charAt(offset);
        if ((first & -1024) == UTF16.TRAIL_SURROGATE_MIN_VALUE) {
            int length;
            if (first < 57327) {
                length = first & Opcodes.OP_NEW_INSTANCE_JUMBO;
                offset += URES_INDEX_KEYS_TOP;
            } else if (first < UTF16.TRAIL_SURROGATE_MAX_VALUE) {
                length = ((first - 57327) << 16) | this.b16BitUnits.charAt(offset + URES_INDEX_KEYS_TOP);
                offset += URES_ATT_IS_POOL_BUNDLE;
            } else {
                length = (this.b16BitUnits.charAt(offset + URES_INDEX_KEYS_TOP) << 16) | this.b16BitUnits.charAt(offset + URES_ATT_IS_POOL_BUNDLE);
                offset += URES_INDEX_BUNDLE_TOP;
            }
            s = this.b16BitUnits.subSequence(offset, offset + length).toString();
        } else if (first == 0) {
            return emptyString;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append((char) first);
            while (true) {
                offset += URES_INDEX_KEYS_TOP;
                char c = this.b16BitUnits.charAt(offset);
                if (c == '\u0000') {
                    break;
                }
                sb.append(c);
            }
            s = sb.toString();
        }
        return (String) this.resourceCache.putIfAbsent(res, s, s.length() * URES_ATT_IS_POOL_BUNDLE);
    }

    private String makeStringFromBytes(int offset, int length) {
        if (length <= 16) {
            StringBuilder sb = new StringBuilder(length);
            for (int i = URES_INDEX_LENGTH; i < length; i += URES_INDEX_KEYS_TOP) {
                sb.append(this.bytes.getChar(offset));
                offset += URES_ATT_IS_POOL_BUNDLE;
            }
            return sb.toString();
        }
        offset /= URES_ATT_IS_POOL_BUNDLE;
        return this.bytes.asCharBuffer().subSequence(offset, offset + length).toString();
    }

    String getString(int res) {
        int offset = RES_GET_OFFSET(res);
        if (res != offset && RES_GET_TYPE(res) != URES_INDEX_16BIT_TOP) {
            return null;
        }
        if (offset == 0) {
            return emptyString;
        }
        if (res == offset) {
            Object value = this.resourceCache.get(res);
            if (value != null) {
                return (String) value;
            }
            offset = getResourceByteOffset(offset);
            String s = makeStringFromBytes(offset + URES_INDEX_MAX_TABLE_LENGTH, getInt(offset));
            return (String) this.resourceCache.putIfAbsent(res, s, s.length() * URES_ATT_IS_POOL_BUNDLE);
        } else if (offset < this.poolStringIndexLimit) {
            return this.poolBundleReader.getStringV2(res);
        } else {
            return getStringV2(res - this.poolStringIndexLimit);
        }
    }

    private boolean isNoInheritanceMarker(int res) {
        boolean z = DEBUG;
        int offset = RES_GET_OFFSET(res);
        if (offset != 0) {
            if (res == offset) {
                offset = getResourceByteOffset(offset);
                if (getInt(offset) == URES_INDEX_BUNDLE_TOP && this.bytes.getChar(offset + URES_INDEX_MAX_TABLE_LENGTH) == '\u2205' && this.bytes.getChar(offset + URES_INDEX_16BIT_TOP) == '\u2205' && this.bytes.getChar(offset + 8) == '\u2205') {
                    z = true;
                }
                return z;
            } else if (RES_GET_TYPE(res) == URES_INDEX_16BIT_TOP) {
                if (offset < this.poolStringIndexLimit) {
                    return this.poolBundleReader.isStringV2NoInheritanceMarker(offset);
                }
                return isStringV2NoInheritanceMarker(offset - this.poolStringIndexLimit);
            }
        }
        return DEBUG;
    }

    private boolean isStringV2NoInheritanceMarker(int offset) {
        boolean z = true;
        int first = this.b16BitUnits.charAt(offset);
        if (first == 8709) {
            if (this.b16BitUnits.charAt(offset + URES_INDEX_KEYS_TOP) != '\u2205' || this.b16BitUnits.charAt(offset + URES_ATT_IS_POOL_BUNDLE) != '\u2205') {
                z = DEBUG;
            } else if (this.b16BitUnits.charAt(offset + URES_INDEX_BUNDLE_TOP) != '\u0000') {
                z = DEBUG;
            }
            return z;
        } else if (first != 56323) {
            return DEBUG;
        } else {
            if (this.b16BitUnits.charAt(offset + URES_INDEX_KEYS_TOP) != '\u2205' || this.b16BitUnits.charAt(offset + URES_ATT_IS_POOL_BUNDLE) != '\u2205') {
                z = DEBUG;
            } else if (this.b16BitUnits.charAt(offset + URES_INDEX_BUNDLE_TOP) != '\u2205') {
                z = DEBUG;
            }
            return z;
        }
    }

    String getAlias(int res) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != URES_INDEX_BUNDLE_TOP) {
            return null;
        }
        if (offset == 0) {
            return emptyString;
        }
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (String) value;
        }
        offset = getResourceByteOffset(offset);
        int length = getInt(offset);
        return (String) this.resourceCache.putIfAbsent(res, makeStringFromBytes(offset + URES_INDEX_MAX_TABLE_LENGTH, length), length * URES_ATT_IS_POOL_BUNDLE);
    }

    byte[] getBinary(int res, byte[] ba) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != URES_INDEX_KEYS_TOP) {
            return null;
        }
        if (offset == 0) {
            return emptyBytes;
        }
        offset = getResourceByteOffset(offset);
        int length = getInt(offset);
        if (length == 0) {
            return emptyBytes;
        }
        if (ba == null || ba.length != length) {
            ba = new byte[length];
        }
        offset += URES_INDEX_MAX_TABLE_LENGTH;
        if (length <= 16) {
            int i = URES_INDEX_LENGTH;
            int offset2 = offset;
            while (i < length) {
                offset = offset2 + URES_INDEX_KEYS_TOP;
                ba[i] = this.bytes.get(offset2);
                i += URES_INDEX_KEYS_TOP;
                offset2 = offset;
            }
        } else {
            ByteBuffer temp = this.bytes.duplicate();
            temp.position(offset);
            temp.get(ba);
        }
        return ba;
    }

    ByteBuffer getBinary(int res) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != URES_INDEX_KEYS_TOP) {
            return null;
        }
        if (offset == 0) {
            return emptyByteBuffer.duplicate();
        }
        offset = getResourceByteOffset(offset);
        int length = getInt(offset);
        if (length == 0) {
            return emptyByteBuffer.duplicate();
        }
        offset += URES_INDEX_MAX_TABLE_LENGTH;
        ByteBuffer result = this.bytes.duplicate();
        result.position(offset).limit(offset + length);
        result = ICUBinary.sliceWithOrder(result);
        if (!result.isReadOnly()) {
            result = result.asReadOnlyBuffer();
        }
        return result;
    }

    int[] getIntVector(int res) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != 14) {
            return null;
        }
        if (offset == 0) {
            return emptyInts;
        }
        offset = getResourceByteOffset(offset);
        return getInts(offset + URES_INDEX_MAX_TABLE_LENGTH, getInt(offset));
    }

    private int getArrayLength(int res) {
        int offset = RES_GET_OFFSET(res);
        if (offset == 0) {
            return URES_INDEX_LENGTH;
        }
        int type = RES_GET_TYPE(res);
        if (type == 8) {
            return getInt(getResourceByteOffset(offset));
        }
        if (type == 9) {
            return this.b16BitUnits.charAt(offset);
        }
        return URES_INDEX_LENGTH;
    }

    Array getArray(int res) {
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
        return (Array) this.resourceCache.putIfAbsent(res, type == 8 ? new Array32(this, offset) : new Array16(this, offset), URES_INDEX_LENGTH);
    }

    private int getTableLength(int res) {
        int offset = RES_GET_OFFSET(res);
        if (offset == 0) {
            return URES_INDEX_LENGTH;
        }
        int type = RES_GET_TYPE(res);
        if (type == URES_ATT_IS_POOL_BUNDLE) {
            return this.bytes.getChar(getResourceByteOffset(offset));
        } else if (type == URES_INDEX_ATTRIBUTES) {
            return this.b16BitUnits.charAt(offset);
        } else {
            if (type == URES_INDEX_MAX_TABLE_LENGTH) {
                return getInt(getResourceByteOffset(offset));
            }
            return URES_INDEX_LENGTH;
        }
    }

    Table getTable(int res) {
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
        Table table;
        int size;
        if (type == URES_ATT_IS_POOL_BUNDLE) {
            table = new Table1632(this, offset);
            size = table.getSize() * URES_ATT_IS_POOL_BUNDLE;
        } else if (type == URES_INDEX_ATTRIBUTES) {
            table = new Table16(this, offset);
            size = table.getSize() * URES_ATT_IS_POOL_BUNDLE;
        } else {
            table = new Table32(this, offset);
            size = table.getSize() * URES_INDEX_MAX_TABLE_LENGTH;
        }
        return (Table) this.resourceCache.putIfAbsent(res, table, size);
    }

    public static String getFullName(String baseName, String localeName) {
        if (baseName == null || baseName.length() == 0) {
            if (localeName.length() == 0) {
                return ULocale.getDefault().toString();
            }
            return localeName + ICU_RESOURCE_SUFFIX;
        } else if (baseName.indexOf(46) != -1) {
            baseName = baseName.replace('.', '/');
            if (localeName.length() == 0) {
                return baseName + ICU_RESOURCE_SUFFIX;
            }
            return baseName + BaseLocale.SEP + localeName + ICU_RESOURCE_SUFFIX;
        } else if (baseName.charAt(baseName.length() - 1) != '/') {
            return baseName + "/" + localeName + ICU_RESOURCE_SUFFIX;
        } else {
            return baseName + localeName + ICU_RESOURCE_SUFFIX;
        }
    }
}
