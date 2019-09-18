package com.android.okhttp.internal.framed;

import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.ByteString;
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Source;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Hpack {
    /* access modifiers changed from: private */
    public static final Map<ByteString, Integer> NAME_TO_FIRST_INDEX = nameToFirstIndex();
    private static final int PREFIX_4_BITS = 15;
    private static final int PREFIX_5_BITS = 31;
    private static final int PREFIX_6_BITS = 63;
    private static final int PREFIX_7_BITS = 127;
    /* access modifiers changed from: private */
    public static final Header[] STATIC_HEADER_TABLE;

    static final class Reader {
        Header[] dynamicTable = new Header[8];
        int dynamicTableByteCount = 0;
        int headerCount = 0;
        private final List<Header> headerList = new ArrayList();
        private int headerTableSizeSetting;
        private int maxDynamicTableByteCount;
        int nextHeaderIndex = (this.dynamicTable.length - 1);
        private final BufferedSource source;

        Reader(int headerTableSizeSetting2, Source source2) {
            this.headerTableSizeSetting = headerTableSizeSetting2;
            this.maxDynamicTableByteCount = headerTableSizeSetting2;
            this.source = Okio.buffer(source2);
        }

        /* access modifiers changed from: package-private */
        public int maxDynamicTableByteCount() {
            return this.maxDynamicTableByteCount;
        }

        /* access modifiers changed from: package-private */
        public void headerTableSizeSetting(int headerTableSizeSetting2) {
            this.headerTableSizeSetting = headerTableSizeSetting2;
            this.maxDynamicTableByteCount = headerTableSizeSetting2;
            adjustDynamicTableByteCount();
        }

        private void adjustDynamicTableByteCount() {
            if (this.maxDynamicTableByteCount >= this.dynamicTableByteCount) {
                return;
            }
            if (this.maxDynamicTableByteCount == 0) {
                clearDynamicTable();
            } else {
                evictToRecoverBytes(this.dynamicTableByteCount - this.maxDynamicTableByteCount);
            }
        }

        private void clearDynamicTable() {
            this.headerList.clear();
            Arrays.fill(this.dynamicTable, null);
            this.nextHeaderIndex = this.dynamicTable.length - 1;
            this.headerCount = 0;
            this.dynamicTableByteCount = 0;
        }

        private int evictToRecoverBytes(int bytesToRecover) {
            int entriesToEvict = 0;
            if (bytesToRecover > 0) {
                int j = this.dynamicTable.length;
                while (true) {
                    j--;
                    if (j < this.nextHeaderIndex || bytesToRecover <= 0) {
                        System.arraycopy(this.dynamicTable, this.nextHeaderIndex + 1, this.dynamicTable, this.nextHeaderIndex + 1 + entriesToEvict, this.headerCount);
                        this.nextHeaderIndex += entriesToEvict;
                    } else {
                        bytesToRecover -= this.dynamicTable[j].hpackSize;
                        this.dynamicTableByteCount -= this.dynamicTable[j].hpackSize;
                        this.headerCount--;
                        entriesToEvict++;
                    }
                }
                System.arraycopy(this.dynamicTable, this.nextHeaderIndex + 1, this.dynamicTable, this.nextHeaderIndex + 1 + entriesToEvict, this.headerCount);
                this.nextHeaderIndex += entriesToEvict;
            }
            return entriesToEvict;
        }

        /* access modifiers changed from: package-private */
        public void readHeaders() throws IOException {
            while (!this.source.exhausted()) {
                int b = this.source.readByte() & 255;
                if (b == 128) {
                    throw new IOException("index == 0");
                } else if ((b & 128) == 128) {
                    readIndexedHeader(readInt(b, Hpack.PREFIX_7_BITS) - 1);
                } else if (b == 64) {
                    readLiteralHeaderWithIncrementalIndexingNewName();
                } else if ((b & 64) == 64) {
                    readLiteralHeaderWithIncrementalIndexingIndexedName(readInt(b, Hpack.PREFIX_6_BITS) - 1);
                } else if ((b & 32) == 32) {
                    this.maxDynamicTableByteCount = readInt(b, Hpack.PREFIX_5_BITS);
                    if (this.maxDynamicTableByteCount < 0 || this.maxDynamicTableByteCount > this.headerTableSizeSetting) {
                        throw new IOException("Invalid dynamic table size update " + this.maxDynamicTableByteCount);
                    }
                    adjustDynamicTableByteCount();
                } else if (b == 16 || b == 0) {
                    readLiteralHeaderWithoutIndexingNewName();
                } else {
                    readLiteralHeaderWithoutIndexingIndexedName(readInt(b, Hpack.PREFIX_4_BITS) - 1);
                }
            }
        }

        public List<Header> getAndResetHeaderList() {
            List<Header> result = new ArrayList<>(this.headerList);
            this.headerList.clear();
            return result;
        }

        private void readIndexedHeader(int index) throws IOException {
            if (isStaticHeader(index)) {
                this.headerList.add(Hpack.STATIC_HEADER_TABLE[index]);
                return;
            }
            int dynamicTableIndex = dynamicTableIndex(index - Hpack.STATIC_HEADER_TABLE.length);
            if (dynamicTableIndex < 0 || dynamicTableIndex > this.dynamicTable.length - 1) {
                throw new IOException("Header index too large " + (index + 1));
            }
            this.headerList.add(this.dynamicTable[dynamicTableIndex]);
        }

        private int dynamicTableIndex(int index) {
            return this.nextHeaderIndex + 1 + index;
        }

        private void readLiteralHeaderWithoutIndexingIndexedName(int index) throws IOException {
            this.headerList.add(new Header(getName(index), readByteString()));
        }

        private void readLiteralHeaderWithoutIndexingNewName() throws IOException {
            this.headerList.add(new Header(Hpack.checkLowercase(readByteString()), readByteString()));
        }

        private void readLiteralHeaderWithIncrementalIndexingIndexedName(int nameIndex) throws IOException {
            insertIntoDynamicTable(-1, new Header(getName(nameIndex), readByteString()));
        }

        private void readLiteralHeaderWithIncrementalIndexingNewName() throws IOException {
            insertIntoDynamicTable(-1, new Header(Hpack.checkLowercase(readByteString()), readByteString()));
        }

        private ByteString getName(int index) {
            if (isStaticHeader(index)) {
                return Hpack.STATIC_HEADER_TABLE[index].name;
            }
            return this.dynamicTable[dynamicTableIndex(index - Hpack.STATIC_HEADER_TABLE.length)].name;
        }

        private boolean isStaticHeader(int index) {
            return index >= 0 && index <= Hpack.STATIC_HEADER_TABLE.length - 1;
        }

        private void insertIntoDynamicTable(int index, Header entry) {
            int index2;
            this.headerList.add(entry);
            int delta = entry.hpackSize;
            if (index != -1) {
                delta -= this.dynamicTable[dynamicTableIndex(index)].hpackSize;
            }
            if (delta > this.maxDynamicTableByteCount) {
                clearDynamicTable();
                return;
            }
            int entriesEvicted = evictToRecoverBytes((this.dynamicTableByteCount + delta) - this.maxDynamicTableByteCount);
            if (index == -1) {
                if (this.headerCount + 1 > this.dynamicTable.length) {
                    Header[] doubled = new Header[(this.dynamicTable.length * 2)];
                    System.arraycopy(this.dynamicTable, 0, doubled, this.dynamicTable.length, this.dynamicTable.length);
                    this.nextHeaderIndex = this.dynamicTable.length - 1;
                    this.dynamicTable = doubled;
                }
                this.nextHeaderIndex = this.nextHeaderIndex - 1;
                this.dynamicTable[index2] = entry;
                this.headerCount++;
            } else {
                this.dynamicTable[index + dynamicTableIndex(index) + entriesEvicted] = entry;
            }
            this.dynamicTableByteCount += delta;
        }

        private int readByte() throws IOException {
            return this.source.readByte() & 255;
        }

        /* access modifiers changed from: package-private */
        public int readInt(int firstByte, int prefixMask) throws IOException {
            int prefix = firstByte & prefixMask;
            if (prefix < prefixMask) {
                return prefix;
            }
            int result = prefixMask;
            int shift = 0;
            while (true) {
                int b = readByte();
                if ((b & 128) == 0) {
                    return result + (b << shift);
                }
                result += (b & Hpack.PREFIX_7_BITS) << shift;
                shift += 7;
            }
        }

        /* access modifiers changed from: package-private */
        public ByteString readByteString() throws IOException {
            int firstByte = readByte();
            boolean huffmanDecode = (firstByte & 128) == 128;
            int length = readInt(firstByte, Hpack.PREFIX_7_BITS);
            if (huffmanDecode) {
                return ByteString.of(Huffman.get().decode(this.source.readByteArray((long) length)));
            }
            return this.source.readByteString((long) length);
        }
    }

    static final class Writer {
        private final Buffer out;

        Writer(Buffer out2) {
            this.out = out2;
        }

        /* access modifiers changed from: package-private */
        public void writeHeaders(List<Header> headerBlock) throws IOException {
            int size = headerBlock.size();
            for (int i = 0; i < size; i++) {
                ByteString name = headerBlock.get(i).name.toAsciiLowercase();
                Integer staticIndex = (Integer) Hpack.NAME_TO_FIRST_INDEX.get(name);
                if (staticIndex != null) {
                    writeInt(staticIndex.intValue() + 1, Hpack.PREFIX_4_BITS, 0);
                    writeByteString(headerBlock.get(i).value);
                } else {
                    this.out.writeByte(0);
                    writeByteString(name);
                    writeByteString(headerBlock.get(i).value);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeInt(int value, int prefixMask, int bits) throws IOException {
            if (value < prefixMask) {
                this.out.writeByte(bits | value);
                return;
            }
            this.out.writeByte(bits | prefixMask);
            int value2 = value - prefixMask;
            while (value2 >= 128) {
                this.out.writeByte((value2 & Hpack.PREFIX_7_BITS) | 128);
                value2 >>>= 7;
            }
            this.out.writeByte(value2);
        }

        /* access modifiers changed from: package-private */
        public void writeByteString(ByteString data) throws IOException {
            writeInt(data.size(), Hpack.PREFIX_7_BITS, 0);
            this.out.write(data);
        }
    }

    static {
        Header[] headerArr = new Header[61];
        headerArr[0] = new Header(Header.TARGET_AUTHORITY, "");
        headerArr[1] = new Header(Header.TARGET_METHOD, "GET");
        headerArr[2] = new Header(Header.TARGET_METHOD, "POST");
        headerArr[3] = new Header(Header.TARGET_PATH, "/");
        headerArr[4] = new Header(Header.TARGET_PATH, "/index.html");
        headerArr[5] = new Header(Header.TARGET_SCHEME, "http");
        headerArr[6] = new Header(Header.TARGET_SCHEME, "https");
        headerArr[7] = new Header(Header.RESPONSE_STATUS, "200");
        headerArr[8] = new Header(Header.RESPONSE_STATUS, "204");
        headerArr[9] = new Header(Header.RESPONSE_STATUS, "206");
        headerArr[10] = new Header(Header.RESPONSE_STATUS, "304");
        headerArr[11] = new Header(Header.RESPONSE_STATUS, "400");
        headerArr[12] = new Header(Header.RESPONSE_STATUS, "404");
        headerArr[13] = new Header(Header.RESPONSE_STATUS, "500");
        headerArr[14] = new Header("accept-charset", "");
        headerArr[PREFIX_4_BITS] = new Header("accept-encoding", "gzip, deflate");
        headerArr[16] = new Header("accept-language", "");
        headerArr[17] = new Header("accept-ranges", "");
        headerArr[18] = new Header("accept", "");
        headerArr[19] = new Header("access-control-allow-origin", "");
        headerArr[20] = new Header("age", "");
        headerArr[21] = new Header("allow", "");
        headerArr[22] = new Header("authorization", "");
        headerArr[23] = new Header("cache-control", "");
        headerArr[24] = new Header("content-disposition", "");
        headerArr[25] = new Header("content-encoding", "");
        headerArr[26] = new Header("content-language", "");
        headerArr[27] = new Header("content-length", "");
        headerArr[28] = new Header("content-location", "");
        headerArr[29] = new Header("content-range", "");
        headerArr[30] = new Header("content-type", "");
        headerArr[PREFIX_5_BITS] = new Header("cookie", "");
        headerArr[32] = new Header("date", "");
        headerArr[33] = new Header("etag", "");
        headerArr[34] = new Header("expect", "");
        headerArr[35] = new Header("expires", "");
        headerArr[36] = new Header("from", "");
        headerArr[37] = new Header("host", "");
        headerArr[38] = new Header("if-match", "");
        headerArr[39] = new Header("if-modified-since", "");
        headerArr[40] = new Header("if-none-match", "");
        headerArr[41] = new Header("if-range", "");
        headerArr[42] = new Header("if-unmodified-since", "");
        headerArr[43] = new Header("last-modified", "");
        headerArr[44] = new Header("link", "");
        headerArr[45] = new Header("location", "");
        headerArr[46] = new Header("max-forwards", "");
        headerArr[47] = new Header("proxy-authenticate", "");
        headerArr[48] = new Header("proxy-authorization", "");
        headerArr[49] = new Header("range", "");
        headerArr[50] = new Header("referer", "");
        headerArr[51] = new Header("refresh", "");
        headerArr[52] = new Header("retry-after", "");
        headerArr[53] = new Header("server", "");
        headerArr[54] = new Header("set-cookie", "");
        headerArr[55] = new Header("strict-transport-security", "");
        headerArr[56] = new Header("transfer-encoding", "");
        headerArr[57] = new Header("user-agent", "");
        headerArr[58] = new Header("vary", "");
        headerArr[59] = new Header("via", "");
        headerArr[60] = new Header("www-authenticate", "");
        STATIC_HEADER_TABLE = headerArr;
    }

    private Hpack() {
    }

    private static Map<ByteString, Integer> nameToFirstIndex() {
        Map<ByteString, Integer> result = new LinkedHashMap<>(STATIC_HEADER_TABLE.length);
        for (int i = 0; i < STATIC_HEADER_TABLE.length; i++) {
            if (!result.containsKey(STATIC_HEADER_TABLE[i].name)) {
                result.put(STATIC_HEADER_TABLE[i].name, Integer.valueOf(i));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /* access modifiers changed from: private */
    public static ByteString checkLowercase(ByteString name) throws IOException {
        int i = 0;
        int length = name.size();
        while (i < length) {
            byte c = name.getByte(i);
            if (c < 65 || c > 90) {
                i++;
            } else {
                throw new IOException("PROTOCOL_ERROR response malformed: mixed case name: " + name.utf8());
            }
        }
        return name;
    }
}
