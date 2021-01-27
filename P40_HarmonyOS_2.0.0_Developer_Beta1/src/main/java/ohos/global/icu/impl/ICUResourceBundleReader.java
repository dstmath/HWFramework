package ohos.global.icu.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceTypeMismatchException;
import ohos.global.icu.util.VersionInfo;

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
    private static final ICUResourceBundleReader NULL_READER = new ICUResourceBundleReader();
    private static int[] PUBLIC_TYPES = {0, 1, 2, 3, 2, 2, 0, 7, 8, 8, -1, -1, -1, -1, 14, -1};
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

    static int RES_GET_INT(int i) {
        return (i << 4) >> 4;
    }

    /* access modifiers changed from: private */
    public static int RES_GET_OFFSET(int i) {
        return i & 268435455;
    }

    static int RES_GET_TYPE(int i) {
        return i >>> 28;
    }

    static int RES_GET_UINT(int i) {
        return i & 268435455;
    }

    static boolean URES_IS_ARRAY(int i) {
        return i == 8 || i == 9;
    }

    static boolean URES_IS_TABLE(int i) {
        return i == 2 || i == 5 || i == 4;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getResourceByteOffset(int i) {
        return i << 2;
    }

    /* access modifiers changed from: private */
    public static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        @Override // ohos.global.icu.impl.ICUBinary.Authenticate
        public boolean isDataVersionAcceptable(byte[] bArr) {
            return (bArr[0] == 1 && (bArr[1] & 255) >= 1) || (2 <= bArr[0] && bArr[0] <= 3);
        }
    }

    /* access modifiers changed from: private */
    public static class ReaderCacheKey {
        final String baseName;
        final String localeID;

        ReaderCacheKey(String str, String str2) {
            this.baseName = str == null ? "" : str;
            this.localeID = str2 == null ? "" : str2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ReaderCacheKey)) {
                return false;
            }
            ReaderCacheKey readerCacheKey = (ReaderCacheKey) obj;
            return this.baseName.equals(readerCacheKey.baseName) && this.localeID.equals(readerCacheKey.localeID);
        }

        public int hashCode() {
            return this.localeID.hashCode() ^ this.baseName.hashCode();
        }
    }

    /* access modifiers changed from: private */
    public static class ReaderCache extends SoftCache<ReaderCacheKey, ICUResourceBundleReader, ClassLoader> {
        private ReaderCache() {
        }

        /* access modifiers changed from: protected */
        public ICUResourceBundleReader createInstance(ReaderCacheKey readerCacheKey, ClassLoader classLoader) {
            ByteBuffer byteBuffer;
            String fullName = ICUResourceBundleReader.getFullName(readerCacheKey.baseName, readerCacheKey.localeID);
            try {
                if (readerCacheKey.baseName == null || !readerCacheKey.baseName.startsWith(ICUData.ICU_BASE_NAME)) {
                    InputStream stream = ICUData.getStream(classLoader, fullName);
                    if (stream == null) {
                        return ICUResourceBundleReader.NULL_READER;
                    }
                    byteBuffer = ICUBinary.getByteBufferFromInputStreamAndCloseStream(stream);
                } else {
                    byteBuffer = ICUBinary.getData(classLoader, fullName, fullName.substring(35));
                    if (byteBuffer == null) {
                        return ICUResourceBundleReader.NULL_READER;
                    }
                }
                return new ICUResourceBundleReader(byteBuffer, readerCacheKey.baseName, readerCacheKey.localeID, classLoader);
            } catch (IOException e) {
                throw new ICUUncheckedIOException("Data file " + fullName + " is corrupt - " + e.getMessage(), e);
            }
        }
    }

    private ICUResourceBundleReader() {
    }

    private ICUResourceBundleReader(ByteBuffer byteBuffer, String str, String str2, ClassLoader classLoader) throws IOException {
        init(byteBuffer);
        if (this.usesPoolBundle) {
            this.poolBundleReader = getReader(str, "pool", classLoader);
            ICUResourceBundleReader iCUResourceBundleReader = this.poolBundleReader;
            if (iCUResourceBundleReader == null || !iCUResourceBundleReader.isPoolBundle) {
                throw new IllegalStateException("pool.res is not a pool bundle");
            } else if (iCUResourceBundleReader.poolCheckSum != this.poolCheckSum) {
                throw new IllegalStateException("pool.res has a different checksum than this bundle");
            }
        }
    }

    static ICUResourceBundleReader getReader(String str, String str2, ClassLoader classLoader) {
        ICUResourceBundleReader iCUResourceBundleReader = (ICUResourceBundleReader) CACHE.getInstance(new ReaderCacheKey(str, str2), classLoader);
        if (iCUResourceBundleReader == NULL_READER) {
            return null;
        }
        return iCUResourceBundleReader;
    }

    private void init(ByteBuffer byteBuffer) throws IOException {
        this.dataVersion = ICUBinary.readHeader(byteBuffer, DATA_FORMAT, IS_ACCEPTABLE);
        byte b = byteBuffer.get(16);
        this.bytes = ICUBinary.sliceWithOrder(byteBuffer);
        int remaining = this.bytes.remaining();
        this.rootRes = this.bytes.getInt(0);
        int indexesInt = getIndexesInt(0);
        int i = indexesInt & 255;
        if (i > 4) {
            int i2 = i + 1;
            int i3 = i2 << 2;
            if (remaining >= i3) {
                int indexesInt2 = getIndexesInt(3);
                if (remaining >= (indexesInt2 << 2)) {
                    int i4 = indexesInt2 - 1;
                    if (b >= 3) {
                        this.poolStringIndexLimit = indexesInt >>> 8;
                    }
                    if (i > 5) {
                        int indexesInt3 = getIndexesInt(5);
                        this.noFallback = (indexesInt3 & 1) != 0;
                        this.isPoolBundle = (indexesInt3 & 2) != 0;
                        this.usesPoolBundle = (indexesInt3 & 4) != 0;
                        this.poolStringIndexLimit |= (61440 & indexesInt3) << 12;
                        this.poolStringIndex16Limit = indexesInt3 >>> 16;
                    }
                    int indexesInt4 = getIndexesInt(1);
                    if (indexesInt4 > i2) {
                        if (this.isPoolBundle) {
                            this.keyBytes = new byte[((indexesInt4 - i2) << 2)];
                            this.bytes.position(i3);
                        } else {
                            this.localKeyLimit = indexesInt4 << 2;
                            this.keyBytes = new byte[this.localKeyLimit];
                        }
                        this.bytes.get(this.keyBytes);
                    }
                    if (i > 6) {
                        int indexesInt5 = getIndexesInt(6);
                        if (indexesInt5 > indexesInt4) {
                            int i5 = (indexesInt5 - indexesInt4) * 2;
                            this.bytes.position(indexesInt4 << 2);
                            this.b16BitUnits = this.bytes.asCharBuffer();
                            this.b16BitUnits.limit(i5);
                            i4 |= i5 - 1;
                        } else {
                            this.b16BitUnits = EMPTY_16_BIT_UNITS;
                        }
                    } else {
                        this.b16BitUnits = EMPTY_16_BIT_UNITS;
                    }
                    if (i > 7) {
                        this.poolCheckSum = getIndexesInt(7);
                    }
                    if (!this.isPoolBundle || this.b16BitUnits.length() > 1) {
                        this.resourceCache = new ResourceCache(i4);
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
        return this.bytes.getInt((i + 1) << 2);
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

    private char[] getChars(int i, int i2) {
        char[] cArr = new char[i2];
        if (i2 <= 16) {
            for (int i3 = 0; i3 < i2; i3++) {
                cArr[i3] = this.bytes.getChar(i);
                i += 2;
            }
        } else {
            CharBuffer asCharBuffer = this.bytes.asCharBuffer();
            asCharBuffer.position(i / 2);
            asCharBuffer.get(cArr);
        }
        return cArr;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getInt(int i) {
        return this.bytes.getInt(i);
    }

    private int[] getInts(int i, int i2) {
        int[] iArr = new int[i2];
        if (i2 <= 16) {
            for (int i3 = 0; i3 < i2; i3++) {
                iArr[i3] = this.bytes.getInt(i);
                i += 4;
            }
        } else {
            IntBuffer asIntBuffer = this.bytes.asIntBuffer();
            asIntBuffer.position(i / 4);
            asIntBuffer.get(iArr);
        }
        return iArr;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private char[] getTable16KeyOffsets(int i) {
        int i2 = i + 1;
        int charAt = this.b16BitUnits.charAt(i);
        if (charAt <= 0) {
            return emptyChars;
        }
        char[] cArr = new char[charAt];
        if (charAt <= 16) {
            int i3 = 0;
            while (i3 < charAt) {
                cArr[i3] = this.b16BitUnits.charAt(i2);
                i3++;
                i2++;
            }
        } else {
            CharBuffer duplicate = this.b16BitUnits.duplicate();
            duplicate.position(i2);
            duplicate.get(cArr);
        }
        return cArr;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private char[] getTableKeyOffsets(int i) {
        char c = this.bytes.getChar(i);
        if (c > 0) {
            return getChars(i + 2, c);
        }
        return emptyChars;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int[] getTable32KeyOffsets(int i) {
        int i2 = getInt(i);
        if (i2 > 0) {
            return getInts(i + 4, i2);
        }
        return emptyInts;
    }

    private static String makeKeyStringFromBytes(byte[] bArr, int i) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            byte b = bArr[i];
            if (b == 0) {
                return sb.toString();
            }
            i++;
            sb.append((char) b);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getKey16String(int i) {
        int i2 = this.localKeyLimit;
        if (i < i2) {
            return makeKeyStringFromBytes(this.keyBytes, i);
        }
        return makeKeyStringFromBytes(this.poolBundleReader.keyBytes, i - i2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getKey32String(int i) {
        if (i >= 0) {
            return makeKeyStringFromBytes(this.keyBytes, i);
        }
        return makeKeyStringFromBytes(this.poolBundleReader.keyBytes, i & Integer.MAX_VALUE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setKeyFromKey16(int i, UResource.Key key) {
        int i2 = this.localKeyLimit;
        if (i < i2) {
            key.setBytes(this.keyBytes, i);
        } else {
            key.setBytes(this.poolBundleReader.keyBytes, i - i2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setKeyFromKey32(int i, UResource.Key key) {
        if (i >= 0) {
            key.setBytes(this.keyBytes, i);
        } else {
            key.setBytes(this.poolBundleReader.keyBytes, i & Integer.MAX_VALUE);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int compareKeys(CharSequence charSequence, char c) {
        int i = this.localKeyLimit;
        if (c < i) {
            return ICUBinary.compareKeys(charSequence, this.keyBytes, c);
        }
        return ICUBinary.compareKeys(charSequence, this.poolBundleReader.keyBytes, c - i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int compareKeys32(CharSequence charSequence, int i) {
        if (i >= 0) {
            return ICUBinary.compareKeys(charSequence, this.keyBytes, i);
        }
        return ICUBinary.compareKeys(charSequence, this.poolBundleReader.keyBytes, i & Integer.MAX_VALUE);
    }

    /* access modifiers changed from: package-private */
    public String getStringV2(int i) {
        String str;
        int i2;
        int i3;
        int RES_GET_OFFSET = RES_GET_OFFSET(i);
        Object obj = this.resourceCache.get(i);
        if (obj != null) {
            return (String) obj;
        }
        char charAt = this.b16BitUnits.charAt(RES_GET_OFFSET);
        if ((charAt & 64512) == 56320) {
            if (charAt < 57327) {
                i2 = charAt & 1023;
                i3 = RES_GET_OFFSET + 1;
            } else if (charAt < 57343) {
                i2 = ((charAt - 57327) << 16) | this.b16BitUnits.charAt(RES_GET_OFFSET + 1);
                i3 = RES_GET_OFFSET + 2;
            } else {
                i2 = (this.b16BitUnits.charAt(RES_GET_OFFSET + 1) << 16) | this.b16BitUnits.charAt(RES_GET_OFFSET + 2);
                i3 = RES_GET_OFFSET + 3;
            }
            str = this.b16BitUnits.subSequence(i3, i2 + i3).toString();
        } else if (charAt == 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append((char) charAt);
            while (true) {
                RES_GET_OFFSET++;
                char charAt2 = this.b16BitUnits.charAt(RES_GET_OFFSET);
                if (charAt2 == 0) {
                    break;
                }
                sb.append(charAt2);
            }
            str = sb.toString();
        }
        return (String) this.resourceCache.putIfAbsent(i, str, str.length() * 2);
    }

    private String makeStringFromBytes(int i, int i2) {
        if (i2 <= 16) {
            StringBuilder sb = new StringBuilder(i2);
            for (int i3 = 0; i3 < i2; i3++) {
                sb.append(this.bytes.getChar(i));
                i += 2;
            }
            return sb.toString();
        }
        int i4 = i / 2;
        return this.bytes.asCharBuffer().subSequence(i4, i2 + i4).toString();
    }

    /* access modifiers changed from: package-private */
    public String getString(int i) {
        int RES_GET_OFFSET = RES_GET_OFFSET(i);
        if (i != RES_GET_OFFSET && RES_GET_TYPE(i) != 6) {
            return null;
        }
        if (RES_GET_OFFSET == 0) {
            return "";
        }
        if (i != RES_GET_OFFSET) {
            int i2 = this.poolStringIndexLimit;
            if (RES_GET_OFFSET < i2) {
                return this.poolBundleReader.getStringV2(i);
            }
            return getStringV2(i - i2);
        }
        Object obj = this.resourceCache.get(i);
        if (obj != null) {
            return (String) obj;
        }
        int resourceByteOffset = getResourceByteOffset(RES_GET_OFFSET);
        String makeStringFromBytes = makeStringFromBytes(resourceByteOffset + 4, getInt(resourceByteOffset));
        return (String) this.resourceCache.putIfAbsent(i, makeStringFromBytes, makeStringFromBytes.length() * 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNoInheritanceMarker(int i) {
        int RES_GET_OFFSET = RES_GET_OFFSET(i);
        if (RES_GET_OFFSET != 0) {
            if (i == RES_GET_OFFSET) {
                int resourceByteOffset = getResourceByteOffset(RES_GET_OFFSET);
                if (getInt(resourceByteOffset) == 3 && this.bytes.getChar(resourceByteOffset + 4) == 8709 && this.bytes.getChar(resourceByteOffset + 6) == 8709 && this.bytes.getChar(resourceByteOffset + 8) == 8709) {
                    return true;
                }
                return false;
            } else if (RES_GET_TYPE(i) == 6) {
                int i2 = this.poolStringIndexLimit;
                if (RES_GET_OFFSET < i2) {
                    return this.poolBundleReader.isStringV2NoInheritanceMarker(RES_GET_OFFSET);
                }
                return isStringV2NoInheritanceMarker(RES_GET_OFFSET - i2);
            }
        }
        return false;
    }

    private boolean isStringV2NoInheritanceMarker(int i) {
        char charAt = this.b16BitUnits.charAt(i);
        return charAt == 8709 ? this.b16BitUnits.charAt(i + 1) == 8709 && this.b16BitUnits.charAt(i + 2) == 8709 && this.b16BitUnits.charAt(i + 3) == 0 : charAt == 56323 && this.b16BitUnits.charAt(i + 1) == 8709 && this.b16BitUnits.charAt(i + 2) == 8709 && this.b16BitUnits.charAt(i + 3) == 8709;
    }

    /* access modifiers changed from: package-private */
    public String getAlias(int i) {
        int RES_GET_OFFSET = RES_GET_OFFSET(i);
        if (RES_GET_TYPE(i) != 3) {
            return null;
        }
        if (RES_GET_OFFSET == 0) {
            return "";
        }
        Object obj = this.resourceCache.get(i);
        if (obj != null) {
            return (String) obj;
        }
        int resourceByteOffset = getResourceByteOffset(RES_GET_OFFSET);
        int i2 = getInt(resourceByteOffset);
        return (String) this.resourceCache.putIfAbsent(i, makeStringFromBytes(resourceByteOffset + 4, i2), i2 * 2);
    }

    /* access modifiers changed from: package-private */
    public byte[] getBinary(int i, byte[] bArr) {
        int RES_GET_OFFSET = RES_GET_OFFSET(i);
        if (RES_GET_TYPE(i) != 1) {
            return null;
        }
        if (RES_GET_OFFSET == 0) {
            return emptyBytes;
        }
        int resourceByteOffset = getResourceByteOffset(RES_GET_OFFSET);
        int i2 = getInt(resourceByteOffset);
        if (i2 == 0) {
            return emptyBytes;
        }
        if (bArr == null || bArr.length != i2) {
            bArr = new byte[i2];
        }
        int i3 = resourceByteOffset + 4;
        if (i2 <= 16) {
            int i4 = 0;
            while (i4 < i2) {
                bArr[i4] = this.bytes.get(i3);
                i4++;
                i3++;
            }
        } else {
            ByteBuffer duplicate = this.bytes.duplicate();
            duplicate.position(i3);
            duplicate.get(bArr);
        }
        return bArr;
    }

    /* access modifiers changed from: package-private */
    public ByteBuffer getBinary(int i) {
        int RES_GET_OFFSET = RES_GET_OFFSET(i);
        if (RES_GET_TYPE(i) != 1) {
            return null;
        }
        if (RES_GET_OFFSET == 0) {
            return emptyByteBuffer.duplicate();
        }
        int resourceByteOffset = getResourceByteOffset(RES_GET_OFFSET);
        int i2 = getInt(resourceByteOffset);
        if (i2 == 0) {
            return emptyByteBuffer.duplicate();
        }
        int i3 = resourceByteOffset + 4;
        ByteBuffer duplicate = this.bytes.duplicate();
        duplicate.position(i3).limit(i3 + i2);
        ByteBuffer sliceWithOrder = ICUBinary.sliceWithOrder(duplicate);
        return !sliceWithOrder.isReadOnly() ? sliceWithOrder.asReadOnlyBuffer() : sliceWithOrder;
    }

    /* access modifiers changed from: package-private */
    public int[] getIntVector(int i) {
        int RES_GET_OFFSET = RES_GET_OFFSET(i);
        if (RES_GET_TYPE(i) != 14) {
            return null;
        }
        if (RES_GET_OFFSET == 0) {
            return emptyInts;
        }
        int resourceByteOffset = getResourceByteOffset(RES_GET_OFFSET);
        return getInts(resourceByteOffset + 4, getInt(resourceByteOffset));
    }

    /* access modifiers changed from: package-private */
    public Array getArray(int i) {
        int RES_GET_TYPE = RES_GET_TYPE(i);
        if (!URES_IS_ARRAY(RES_GET_TYPE)) {
            return null;
        }
        int RES_GET_OFFSET = RES_GET_OFFSET(i);
        if (RES_GET_OFFSET == 0) {
            return EMPTY_ARRAY;
        }
        Object obj = this.resourceCache.get(i);
        if (obj != null) {
            return (Array) obj;
        }
        return (Array) this.resourceCache.putIfAbsent(i, RES_GET_TYPE == 8 ? new Array32(this, RES_GET_OFFSET) : new Array16(this, RES_GET_OFFSET), 0);
    }

    /* access modifiers changed from: package-private */
    public Table getTable(int i) {
        int i2;
        Table table;
        int size;
        int RES_GET_TYPE = RES_GET_TYPE(i);
        if (!URES_IS_TABLE(RES_GET_TYPE)) {
            return null;
        }
        int RES_GET_OFFSET = RES_GET_OFFSET(i);
        if (RES_GET_OFFSET == 0) {
            return EMPTY_TABLE;
        }
        Object obj = this.resourceCache.get(i);
        if (obj != null) {
            return (Table) obj;
        }
        if (RES_GET_TYPE == 2) {
            table = new Table1632(this, RES_GET_OFFSET);
            size = table.getSize();
        } else if (RES_GET_TYPE == 5) {
            table = new Table16(this, RES_GET_OFFSET);
            size = table.getSize();
        } else {
            table = new Table32(this, RES_GET_OFFSET);
            i2 = table.getSize() * 4;
            return (Table) this.resourceCache.putIfAbsent(i, table, i2);
        }
        i2 = size * 2;
        return (Table) this.resourceCache.putIfAbsent(i, table, i2);
    }

    /* access modifiers changed from: package-private */
    public static class ReaderValue extends UResource.Value {
        ICUResourceBundleReader reader;
        int res;

        ReaderValue() {
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public int getType() {
            return ICUResourceBundleReader.PUBLIC_TYPES[ICUResourceBundleReader.RES_GET_TYPE(this.res)];
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public String getString() {
            String string = this.reader.getString(this.res);
            if (string != null) {
                return string;
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public String getAliasString() {
            String alias = this.reader.getAlias(this.res);
            if (alias != null) {
                return alias;
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public int getInt() {
            if (ICUResourceBundleReader.RES_GET_TYPE(this.res) == 7) {
                return ICUResourceBundleReader.RES_GET_INT(this.res);
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public int getUInt() {
            if (ICUResourceBundleReader.RES_GET_TYPE(this.res) == 7) {
                return ICUResourceBundleReader.RES_GET_UINT(this.res);
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public int[] getIntVector() {
            int[] intVector = this.reader.getIntVector(this.res);
            if (intVector != null) {
                return intVector;
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public ByteBuffer getBinary() {
            ByteBuffer binary = this.reader.getBinary(this.res);
            if (binary != null) {
                return binary;
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public UResource.Array getArray() {
            Array array = this.reader.getArray(this.res);
            if (array != null) {
                return array;
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public UResource.Table getTable() {
            Table table = this.reader.getTable(this.res);
            if (table != null) {
                return table;
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public boolean isNoInheritanceMarker() {
            return this.reader.isNoInheritanceMarker(this.res);
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public String[] getStringArray() {
            Array array = this.reader.getArray(this.res);
            if (array != null) {
                return getStringArray(array);
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public String[] getStringArrayOrStringAsArray() {
            Array array = this.reader.getArray(this.res);
            if (array != null) {
                return getStringArray(array);
            }
            String string = this.reader.getString(this.res);
            if (string != null) {
                return new String[]{string};
            }
            throw new UResourceTypeMismatchException("");
        }

        @Override // ohos.global.icu.impl.UResource.Value
        public String getStringOrFirstOfArray() {
            String string;
            String string2 = this.reader.getString(this.res);
            if (string2 != null) {
                return string2;
            }
            Array array = this.reader.getArray(this.res);
            if (array != null && array.size > 0 && (string = this.reader.getString(array.getContainerResource(this.reader, 0))) != null) {
                return string;
            }
            throw new UResourceTypeMismatchException("");
        }

        private String[] getStringArray(Array array) {
            String[] strArr = new String[array.size];
            for (int i = 0; i < array.size; i++) {
                String string = this.reader.getString(array.getContainerResource(this.reader, i));
                if (string != null) {
                    strArr[i] = string;
                } else {
                    throw new UResourceTypeMismatchException("");
                }
            }
            return strArr;
        }
    }

    /* access modifiers changed from: package-private */
    public static class Container {
        protected int itemsOffset;
        protected int size;

        /* access modifiers changed from: package-private */
        public int getContainerResource(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            return -1;
        }

        public final int getSize() {
            return this.size;
        }

        /* access modifiers changed from: protected */
        public int getContainer16Resource(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            if (i < 0 || this.size <= i) {
                return -1;
            }
            int charAt = iCUResourceBundleReader.b16BitUnits.charAt(this.itemsOffset + i);
            if (charAt >= iCUResourceBundleReader.poolStringIndex16Limit) {
                charAt = (charAt - iCUResourceBundleReader.poolStringIndex16Limit) + iCUResourceBundleReader.poolStringIndexLimit;
            }
            return charAt | 1610612736;
        }

        /* access modifiers changed from: protected */
        public int getContainer32Resource(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            if (i < 0 || this.size <= i) {
                return -1;
            }
            return iCUResourceBundleReader.getInt(this.itemsOffset + (i * 4));
        }

        /* access modifiers changed from: package-private */
        public int getResource(ICUResourceBundleReader iCUResourceBundleReader, String str) {
            return getContainerResource(iCUResourceBundleReader, Integer.parseInt(str));
        }

        Container() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class Array extends Container implements UResource.Array {
        Array() {
        }

        @Override // ohos.global.icu.impl.UResource.Array
        public boolean getValue(int i, UResource.Value value) {
            if (i < 0 || i >= this.size) {
                return false;
            }
            ReaderValue readerValue = (ReaderValue) value;
            readerValue.res = getContainerResource(readerValue.reader, i);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static final class Array32 extends Array {
        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUResourceBundleReader.Container
        public int getContainerResource(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            return getContainer32Resource(iCUResourceBundleReader, i);
        }

        Array32(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            int resourceByteOffset = iCUResourceBundleReader.getResourceByteOffset(i);
            this.size = iCUResourceBundleReader.getInt(resourceByteOffset);
            this.itemsOffset = resourceByteOffset + 4;
        }
    }

    /* access modifiers changed from: private */
    public static final class Array16 extends Array {
        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUResourceBundleReader.Container
        public int getContainerResource(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            return getContainer16Resource(iCUResourceBundleReader, i);
        }

        Array16(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            this.size = iCUResourceBundleReader.b16BitUnits.charAt(i);
            this.itemsOffset = i + 1;
        }
    }

    /* access modifiers changed from: package-private */
    public static class Table extends Container implements UResource.Table {
        private static final int URESDATA_ITEM_NOT_FOUND = -1;
        protected int[] key32Offsets;
        protected char[] keyOffsets;

        Table() {
        }

        /* access modifiers changed from: package-private */
        public String getKey(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            if (i < 0 || this.size <= i) {
                return null;
            }
            char[] cArr = this.keyOffsets;
            if (cArr != null) {
                return iCUResourceBundleReader.getKey16String(cArr[i]);
            }
            return iCUResourceBundleReader.getKey32String(this.key32Offsets[i]);
        }

        /* access modifiers changed from: package-private */
        public int findTableItem(ICUResourceBundleReader iCUResourceBundleReader, CharSequence charSequence) {
            int i;
            int i2 = this.size;
            int i3 = 0;
            while (i3 < i2) {
                int i4 = (i3 + i2) >>> 1;
                char[] cArr = this.keyOffsets;
                if (cArr != null) {
                    i = iCUResourceBundleReader.compareKeys(charSequence, cArr[i4]);
                } else {
                    i = iCUResourceBundleReader.compareKeys32(charSequence, this.key32Offsets[i4]);
                }
                if (i < 0) {
                    i2 = i4;
                } else if (i <= 0) {
                    return i4;
                } else {
                    i3 = i4 + 1;
                }
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUResourceBundleReader.Container
        public int getResource(ICUResourceBundleReader iCUResourceBundleReader, String str) {
            return getContainerResource(iCUResourceBundleReader, findTableItem(iCUResourceBundleReader, str));
        }

        @Override // ohos.global.icu.impl.UResource.Table
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

        @Override // ohos.global.icu.impl.UResource.Table
        public boolean findValue(CharSequence charSequence, UResource.Value value) {
            ReaderValue readerValue = (ReaderValue) value;
            int findTableItem = findTableItem(readerValue.reader, charSequence);
            if (findTableItem < 0) {
                return false;
            }
            readerValue.res = getContainerResource(readerValue.reader, findTableItem);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static final class Table1632 extends Table {
        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUResourceBundleReader.Container
        public int getContainerResource(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            return getContainer32Resource(iCUResourceBundleReader, i);
        }

        Table1632(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            int resourceByteOffset = iCUResourceBundleReader.getResourceByteOffset(i);
            this.keyOffsets = iCUResourceBundleReader.getTableKeyOffsets(resourceByteOffset);
            this.size = this.keyOffsets.length;
            this.itemsOffset = resourceByteOffset + (((this.size + 2) & -2) * 2);
        }
    }

    /* access modifiers changed from: private */
    public static final class Table16 extends Table {
        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUResourceBundleReader.Container
        public int getContainerResource(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            return getContainer16Resource(iCUResourceBundleReader, i);
        }

        Table16(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            this.keyOffsets = iCUResourceBundleReader.getTable16KeyOffsets(i);
            this.size = this.keyOffsets.length;
            this.itemsOffset = i + 1 + this.size;
        }
    }

    /* access modifiers changed from: private */
    public static final class Table32 extends Table {
        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUResourceBundleReader.Container
        public int getContainerResource(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            return getContainer32Resource(iCUResourceBundleReader, i);
        }

        Table32(ICUResourceBundleReader iCUResourceBundleReader, int i) {
            int resourceByteOffset = iCUResourceBundleReader.getResourceByteOffset(i);
            this.key32Offsets = iCUResourceBundleReader.getTable32KeyOffsets(resourceByteOffset);
            this.size = this.key32Offsets.length;
            this.itemsOffset = resourceByteOffset + ((this.size + 1) * 4);
        }
    }

    /* access modifiers changed from: private */
    public static final class ResourceCache {
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

        /* access modifiers changed from: private */
        public static boolean storeDirectly(int i) {
            return i < 24 || CacheValue.futureInstancesWillBeStrong();
        }

        /* access modifiers changed from: private */
        public static final Object putIfCleared(Object[] objArr, int i, Object obj, int i2) {
            Object obj2;
            Object obj3 = objArr[i];
            if (!(obj3 instanceof SoftReference)) {
                return obj3;
            }
            Object obj4 = ((SoftReference) obj3).get();
            if (obj4 != null) {
                return obj4;
            }
            if (CacheValue.futureInstancesWillBeStrong()) {
                obj2 = obj;
            } else {
                obj2 = new SoftReference(obj);
            }
            objArr[i] = obj2;
            return obj;
        }

        /* access modifiers changed from: private */
        public static final class Level {
            static final /* synthetic */ boolean $assertionsDisabled = false;
            int[] keys;
            int levelBitsList;
            int mask;
            int shift;
            Object[] values;

            Level(int i, int i2) {
                this.levelBitsList = i;
                this.shift = i2;
                int i3 = 1 << (i & 15);
                this.mask = i3 - 1;
                this.keys = new int[i3];
                this.values = new Object[i3];
            }

            /* access modifiers changed from: package-private */
            public Object get(int i) {
                Level level;
                int i2 = (i >> this.shift) & this.mask;
                int i3 = this.keys[i2];
                if (i3 == i) {
                    return this.values[i2];
                }
                if (i3 != 0 || (level = (Level) this.values[i2]) == null) {
                    return null;
                }
                return level.get(i);
            }

            /* access modifiers changed from: package-private */
            public Object putIfAbsent(int i, Object obj, int i2) {
                int i3 = this.shift;
                int i4 = (i >> i3) & this.mask;
                int[] iArr = this.keys;
                int i5 = iArr[i4];
                if (i5 == i) {
                    return ResourceCache.putIfCleared(this.values, i4, obj, i2);
                }
                if (i5 == 0) {
                    Object[] objArr = this.values;
                    Level level = (Level) objArr[i4];
                    if (level != null) {
                        return level.putIfAbsent(i, obj, i2);
                    }
                    iArr[i4] = i;
                    objArr[i4] = ResourceCache.storeDirectly(i2) ? obj : new SoftReference(obj);
                    return obj;
                }
                int i6 = this.levelBitsList;
                Level level2 = new Level(i6 >> 4, i3 + (i6 & 15));
                int i7 = (i5 >> level2.shift) & level2.mask;
                level2.keys[i7] = i5;
                Object[] objArr2 = level2.values;
                Object[] objArr3 = this.values;
                objArr2[i7] = objArr3[i4];
                this.keys[i4] = 0;
                objArr3[i4] = level2;
                return level2.putIfAbsent(i, obj, i2);
            }
        }

        ResourceCache(int i) {
            while (i <= 134217727) {
                i <<= 1;
                this.maxOffsetBits--;
            }
            int i2 = this.maxOffsetBits + 2;
            if (i2 <= 7) {
                this.levelBitsList = i2;
            } else if (i2 < 10) {
                this.levelBitsList = (i2 - 3) | 48;
            } else {
                this.levelBitsList = 7;
                int i3 = i2 - 7;
                int i4 = 4;
                while (i3 > 6) {
                    if (i3 < 9) {
                        this.levelBitsList = (((i3 - 3) | 48) << i4) | this.levelBitsList;
                        return;
                    }
                    this.levelBitsList = (6 << i4) | this.levelBitsList;
                    i3 -= 6;
                    i4 += 4;
                }
                this.levelBitsList = (i3 << i4) | this.levelBitsList;
            }
        }

        private int makeKey(int i) {
            int RES_GET_TYPE = ICUResourceBundleReader.RES_GET_TYPE(i);
            return ((RES_GET_TYPE == 6 ? 1 : RES_GET_TYPE == 5 ? 3 : RES_GET_TYPE == 9 ? 2 : 0) << this.maxOffsetBits) | ICUResourceBundleReader.RES_GET_OFFSET(i);
        }

        private int findSimple(int i) {
            return Arrays.binarySearch(this.keys, 0, this.length, i);
        }

        /* access modifiers changed from: package-private */
        public synchronized Object get(int i) {
            Object obj;
            if (this.length >= 0) {
                int findSimple = findSimple(i);
                if (findSimple < 0) {
                    return null;
                }
                obj = this.values[findSimple];
            } else {
                obj = this.rootLevel.get(makeKey(i));
                if (obj == null) {
                    return null;
                }
            }
            if (obj instanceof SoftReference) {
                obj = ((SoftReference) obj).get();
            }
            return obj;
        }

        /* access modifiers changed from: package-private */
        public synchronized Object putIfAbsent(int i, Object obj, int i2) {
            if (this.length >= 0) {
                int findSimple = findSimple(i);
                if (findSimple >= 0) {
                    return putIfCleared(this.values, findSimple, obj, i2);
                } else if (this.length < 32) {
                    int i3 = ~findSimple;
                    if (i3 < this.length) {
                        int i4 = i3 + 1;
                        System.arraycopy(this.keys, i3, this.keys, i4, this.length - i3);
                        System.arraycopy(this.values, i3, this.values, i4, this.length - i3);
                    }
                    this.length++;
                    this.keys[i3] = i;
                    this.values[i3] = storeDirectly(i2) ? obj : new SoftReference(obj);
                    return obj;
                } else {
                    this.rootLevel = new Level(this.levelBitsList, 0);
                    for (int i5 = 0; i5 < 32; i5++) {
                        this.rootLevel.putIfAbsent(makeKey(this.keys[i5]), this.values[i5], 0);
                    }
                    this.keys = null;
                    this.values = null;
                    this.length = -1;
                }
            }
            return this.rootLevel.putIfAbsent(makeKey(i), obj, i2);
        }
    }

    public static String getFullName(String str, String str2) {
        if (str == null || str.length() == 0) {
            if (str2.length() == 0) {
                return ULocale.getDefault().toString();
            }
            return str2 + ICU_RESOURCE_SUFFIX;
        } else if (str.indexOf(46) != -1) {
            String replace = str.replace('.', '/');
            if (str2.length() == 0) {
                return replace + ICU_RESOURCE_SUFFIX;
            }
            return replace + "_" + str2 + ICU_RESOURCE_SUFFIX;
        } else if (str.charAt(str.length() - 1) != '/') {
            return str + PsuedoNames.PSEUDONAME_ROOT + str2 + ICU_RESOURCE_SUFFIX;
        } else {
            return str + str2 + ICU_RESOURCE_SUFFIX;
        }
    }
}
