package libcore.util;

import android.system.ErrnoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import libcore.io.BufferIterator;
import libcore.io.MemoryMappedFile;

public final class ZoneInfoDB {
    private static final TzData DATA = TzData.loadTzDataWithFallback(TimeZoneDataFiles.getTimeZoneFilePaths(TZDATA_FILE));
    public static final String TZDATA_FILE = "tzdata";

    public static class TzData {
        private static final int CACHE_SIZE = 1;
        public static final int SIZEOF_INDEX_ENTRY = 52;
        private static final int SIZEOF_TZINT = 4;
        private static final int SIZEOF_TZNAME = 40;
        private int[] byteOffsets;
        private final BasicLruCache<String, ZoneInfo> cache = new BasicLruCache<String, ZoneInfo>(1) {
            protected ZoneInfo create(String id) {
                try {
                    return TzData.this.makeTimeZoneUncached(id);
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to load timezone for ID=" + id, e);
                }
            }
        };
        private boolean closed;
        private String[] ids;
        private MemoryMappedFile mappedFile;
        private int[] rawUtcOffsetsCache;
        private String version;
        private String zoneTab;

        public static TzData loadTzDataWithFallback(String... paths) {
            for (String path : paths) {
                TzData tzData = new TzData();
                if (tzData.loadData(path)) {
                    return tzData;
                }
            }
            System.logE("Couldn't find any tzdata file!");
            return createFallback();
        }

        public static TzData loadTzData(String path) {
            TzData tzData = new TzData();
            if (tzData.loadData(path)) {
                return tzData;
            }
            return null;
        }

        private static TzData createFallback() {
            TzData tzData = new TzData();
            tzData.populateFallback();
            return tzData;
        }

        private TzData() {
        }

        public BufferIterator getBufferIterator(String id) {
            checkNotClosed();
            int index = Arrays.binarySearch(this.ids, id);
            if (index < 0) {
                return null;
            }
            int byteOffset = this.byteOffsets[index];
            BufferIterator it = this.mappedFile.bigEndianIterator();
            it.skip(byteOffset);
            return it;
        }

        private void populateFallback() {
            this.version = "missing";
            this.zoneTab = "# Emergency fallback data.\n";
            this.ids = new String[]{"GMT"};
            int[] iArr = new int[1];
            this.rawUtcOffsetsCache = iArr;
            this.byteOffsets = iArr;
        }

        private boolean loadData(String path) {
            try {
                this.mappedFile = MemoryMappedFile.mmapRO(path);
                try {
                    readHeader();
                    return true;
                } catch (Exception ex) {
                    close();
                    System.logE("tzdata file \"" + path + "\" was present but invalid!", ex);
                    return false;
                }
            } catch (ErrnoException e) {
                return false;
            }
        }

        private void readHeader() throws IOException {
            BufferIterator it = this.mappedFile.bigEndianIterator();
            try {
                byte[] tzdata_version = new byte[12];
                it.readByteArray(tzdata_version, 0, tzdata_version.length);
                if (new String(tzdata_version, 0, 6, StandardCharsets.US_ASCII).equals(ZoneInfoDB.TZDATA_FILE) && tzdata_version[11] == (byte) 0) {
                    this.version = new String(tzdata_version, 6, 5, StandardCharsets.US_ASCII);
                    int fileSize = this.mappedFile.size();
                    int index_offset = it.readInt();
                    validateOffset(index_offset, fileSize);
                    int data_offset = it.readInt();
                    validateOffset(data_offset, fileSize);
                    int zonetab_offset = it.readInt();
                    validateOffset(zonetab_offset, fileSize);
                    if (index_offset >= data_offset || data_offset >= zonetab_offset) {
                        throw new IOException("Invalid offset: index_offset=" + index_offset + ", data_offset=" + data_offset + ", zonetab_offset=" + zonetab_offset + ", fileSize=" + fileSize);
                    }
                    readIndex(it, index_offset, data_offset);
                    readZoneTab(it, zonetab_offset, fileSize - zonetab_offset);
                    return;
                }
                throw new IOException("bad tzdata magic: " + Arrays.toString(tzdata_version));
            } catch (IndexOutOfBoundsException e) {
                throw new IOException("Invalid read from data file", e);
            }
        }

        private static void validateOffset(int offset, int size) throws IOException {
            if (offset < 0 || offset >= size) {
                throw new IOException("Invalid offset=" + offset + ", size=" + size);
            }
        }

        private void readZoneTab(BufferIterator it, int zoneTabOffset, int zoneTabSize) {
            byte[] bytes = new byte[zoneTabSize];
            it.seek(zoneTabOffset);
            it.readByteArray(bytes, 0, bytes.length);
            this.zoneTab = new String(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
        }

        private void readIndex(BufferIterator it, int indexOffset, int dataOffset) throws IOException {
            it.seek(indexOffset);
            byte[] idBytes = new byte[40];
            int indexSize = dataOffset - indexOffset;
            if (indexSize % 52 != 0) {
                throw new IOException("Index size is not divisible by 52, indexSize=" + indexSize);
            }
            int entryCount = indexSize / 52;
            this.byteOffsets = new int[entryCount];
            this.ids = new String[entryCount];
            int i = 0;
            while (i < entryCount) {
                it.readByteArray(idBytes, 0, idBytes.length);
                this.byteOffsets[i] = it.readInt();
                int[] iArr = this.byteOffsets;
                iArr[i] = iArr[i] + dataOffset;
                if (it.readInt() < 44) {
                    throw new IOException("length in index file < sizeof(tzhead)");
                }
                it.skip(4);
                int len = 0;
                while (idBytes[len] != (byte) 0 && len < idBytes.length) {
                    len++;
                }
                if (len == 0) {
                    throw new IOException("Invalid ID at index=" + i);
                }
                this.ids[i] = new String(idBytes, 0, len, StandardCharsets.US_ASCII);
                if (i <= 0 || this.ids[i].compareTo(this.ids[i - 1]) > 0) {
                    i++;
                } else {
                    throw new IOException("Index not sorted or contains multiple entries with the same ID, index=" + i + ", ids[i]=" + this.ids[i] + ", ids[i - 1]=" + this.ids[i - 1]);
                }
            }
        }

        public void validate() throws IOException {
            checkNotClosed();
            for (String id : getAvailableIDs()) {
                if (makeTimeZoneUncached(id) == null) {
                    throw new IOException("Unable to find data for ID=" + id);
                }
            }
        }

        ZoneInfo makeTimeZoneUncached(String id) throws IOException {
            BufferIterator it = getBufferIterator(id);
            if (it == null) {
                return null;
            }
            return ZoneInfo.readTimeZone(id, it, System.currentTimeMillis());
        }

        public String[] getAvailableIDs() {
            checkNotClosed();
            return (String[]) this.ids.clone();
        }

        public String[] getAvailableIDs(int rawUtcOffset) {
            checkNotClosed();
            List<String> matches = new ArrayList();
            int[] rawUtcOffsets = getRawUtcOffsets();
            for (int i = 0; i < rawUtcOffsets.length; i++) {
                if (rawUtcOffsets[i] == rawUtcOffset) {
                    matches.add(this.ids[i]);
                }
            }
            return (String[]) matches.toArray(new String[matches.size()]);
        }

        private synchronized int[] getRawUtcOffsets() {
            if (this.rawUtcOffsetsCache != null) {
                return this.rawUtcOffsetsCache;
            }
            this.rawUtcOffsetsCache = new int[this.ids.length];
            for (int i = 0; i < this.ids.length; i++) {
                this.rawUtcOffsetsCache[i] = ((ZoneInfo) this.cache.get(this.ids[i])).getRawOffset();
            }
            return this.rawUtcOffsetsCache;
        }

        public String getVersion() {
            checkNotClosed();
            return this.version;
        }

        public String getZoneTab() {
            checkNotClosed();
            return this.zoneTab;
        }

        public ZoneInfo makeTimeZone(String id) throws IOException {
            checkNotClosed();
            ZoneInfo zoneInfo = (ZoneInfo) this.cache.get(id);
            if (zoneInfo == null) {
                return null;
            }
            return (ZoneInfo) zoneInfo.clone();
        }

        public boolean hasTimeZone(String id) throws IOException {
            checkNotClosed();
            return this.cache.get(id) != null;
        }

        public void close() {
            if (!this.closed) {
                this.closed = true;
                this.ids = null;
                this.byteOffsets = null;
                this.rawUtcOffsetsCache = null;
                this.mappedFile = null;
                this.cache.evictAll();
                if (this.mappedFile != null) {
                    try {
                        this.mappedFile.close();
                    } catch (ErrnoException e) {
                    }
                }
            }
        }

        private void checkNotClosed() throws IllegalStateException {
            if (this.closed) {
                throw new IllegalStateException("TzData is closed");
            }
        }

        protected void finalize() throws Throwable {
            try {
                close();
            } finally {
                super.finalize();
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:16:0x0042 A:{SYNTHETIC, Splitter: B:16:0x0042} */
        /* JADX WARNING: Removed duplicated region for block: B:42:0x00a0  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x0047  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public static String getRulesVersion(File tzDataFile) throws IOException {
            Throwable th;
            Throwable th2 = null;
            FileInputStream is = null;
            try {
                FileInputStream is2 = new FileInputStream(tzDataFile);
                try {
                    byte[] tzdataVersion = new byte[12];
                    int bytesRead = is2.read(tzdataVersion, 0, 12);
                    if (bytesRead != 12) {
                        throw new IOException("File too short: only able to read " + bytesRead + " bytes.");
                    } else if (new String(tzdataVersion, 0, 6, StandardCharsets.US_ASCII).equals(ZoneInfoDB.TZDATA_FILE) && tzdataVersion[11] == (byte) 0) {
                        String str = new String(tzdataVersion, 6, 5, StandardCharsets.US_ASCII);
                        if (is2 != null) {
                            try {
                                is2.close();
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        }
                        if (th2 == null) {
                            return str;
                        }
                        throw th2;
                    } else {
                        throw new IOException("bad tzdata magic: " + Arrays.toString(tzdataVersion));
                    }
                } catch (Throwable th4) {
                    th = th4;
                    is = is2;
                    if (is != null) {
                    }
                    if (th2 == null) {
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                if (is != null) {
                    try {
                        is.close();
                    } catch (Throwable th6) {
                        if (th2 == null) {
                            th2 = th6;
                        } else if (th2 != th6) {
                            th2.addSuppressed(th6);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        }
    }

    private ZoneInfoDB() {
    }

    public static TzData getInstance() {
        return DATA;
    }
}
