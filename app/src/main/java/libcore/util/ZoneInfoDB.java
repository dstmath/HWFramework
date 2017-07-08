package libcore.util;

import android.system.ErrnoException;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import libcore.io.BufferIterator;
import libcore.io.MemoryMappedFile;

public final class ZoneInfoDB {
    private static final TzData DATA = null;

    public static class TzData {
        private static final int CACHE_SIZE = 1;
        private int[] byteOffsets;
        private final BasicLruCache<String, ZoneInfo> cache;
        private String[] ids;
        private MemoryMappedFile mappedFile;
        private int[] rawUtcOffsetsCache;
        private String version;
        private String zoneTab;

        /* renamed from: libcore.util.ZoneInfoDB.TzData.1 */
        class AnonymousClass1 extends BasicLruCache<String, ZoneInfo> {
            AnonymousClass1(int $anonymous0) {
                super($anonymous0);
            }

            protected ZoneInfo create(String id) {
                BufferIterator it = TzData.this.getBufferIterator(id);
                if (it == null) {
                    return null;
                }
                return ZoneInfo.makeTimeZone(id, it);
            }
        }

        public TzData(String... paths) {
            this.cache = new AnonymousClass1(CACHE_SIZE);
            int length = paths.length;
            int i = 0;
            while (i < length) {
                if (!loadData(paths[i])) {
                    i += CACHE_SIZE;
                } else {
                    return;
                }
            }
            System.logE("Couldn't find any tzdata!");
            this.version = "missing";
            this.zoneTab = "# Emergency fallback data.\n";
            String[] strArr = new String[CACHE_SIZE];
            strArr[0] = "GMT";
            this.ids = strArr;
            int[] iArr = new int[CACHE_SIZE];
            this.rawUtcOffsetsCache = iArr;
            this.byteOffsets = iArr;
        }

        public BufferIterator getBufferIterator(String id) {
            int index = Arrays.binarySearch(this.ids, id);
            if (index < 0) {
                return null;
            }
            BufferIterator it = this.mappedFile.bigEndianIterator();
            it.skip(this.byteOffsets[index]);
            return it;
        }

        private boolean loadData(String path) {
            try {
                this.mappedFile = MemoryMappedFile.mmapRO(path);
                try {
                    readHeader();
                    return true;
                } catch (Exception ex) {
                    System.logE("tzdata file \"" + path + "\" was present but invalid!", ex);
                    return false;
                }
            } catch (ErrnoException e) {
                return false;
            }
        }

        private void readHeader() {
            BufferIterator it = this.mappedFile.bigEndianIterator();
            byte[] tzdata_version = new byte[12];
            it.readByteArray(tzdata_version, 0, tzdata_version.length);
            if (new String(tzdata_version, 0, 6, StandardCharsets.US_ASCII).equals("tzdata") && tzdata_version[11] == null) {
                this.version = new String(tzdata_version, 6, 5, StandardCharsets.US_ASCII);
                int index_offset = it.readInt();
                int data_offset = it.readInt();
                int zonetab_offset = it.readInt();
                readIndex(it, index_offset, data_offset);
                readZoneTab(it, zonetab_offset, ((int) this.mappedFile.size()) - zonetab_offset);
                return;
            }
            throw new RuntimeException("bad tzdata magic: " + Arrays.toString(tzdata_version));
        }

        private void readZoneTab(BufferIterator it, int zoneTabOffset, int zoneTabSize) {
            byte[] bytes = new byte[zoneTabSize];
            it.seek(zoneTabOffset);
            it.readByteArray(bytes, 0, bytes.length);
            this.zoneTab = new String(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
        }

        private void readIndex(BufferIterator it, int indexOffset, int dataOffset) {
            it.seek(indexOffset);
            byte[] idBytes = new byte[40];
            int entryCount = (dataOffset - indexOffset) / 52;
            char[] idChars = new char[(entryCount * 40)];
            int[] idEnd = new int[entryCount];
            int idOffset = 0;
            this.byteOffsets = new int[entryCount];
            int i = 0;
            while (i < entryCount) {
                it.readByteArray(idBytes, 0, idBytes.length);
                this.byteOffsets[i] = it.readInt();
                int[] iArr = this.byteOffsets;
                iArr[i] = iArr[i] + dataOffset;
                if (it.readInt() < 44) {
                    throw new AssertionError("length in index file < sizeof(tzhead)");
                }
                it.skip(4);
                int len = idBytes.length;
                int j = 0;
                int idOffset2 = idOffset;
                while (j < len && idBytes[j] != null) {
                    idOffset = idOffset2 + CACHE_SIZE;
                    idChars[idOffset2] = (char) (idBytes[j] & Opcodes.OP_CONST_CLASS_JUMBO);
                    j += CACHE_SIZE;
                    idOffset2 = idOffset;
                }
                idEnd[i] = idOffset2;
                i += CACHE_SIZE;
                idOffset = idOffset2;
            }
            String allIds = new String(idChars, 0, idOffset);
            this.ids = new String[entryCount];
            for (i = 0; i < entryCount; i += CACHE_SIZE) {
                int i2;
                String[] strArr = this.ids;
                if (i == 0) {
                    i2 = 0;
                } else {
                    i2 = idEnd[i - 1];
                }
                strArr[i] = allIds.substring(i2, idEnd[i]);
            }
        }

        public String[] getAvailableIDs() {
            return (String[]) this.ids.clone();
        }

        public String[] getAvailableIDs(int rawUtcOffset) {
            List<String> matches = new ArrayList();
            int[] rawUtcOffsets = getRawUtcOffsets();
            for (int i = 0; i < rawUtcOffsets.length; i += CACHE_SIZE) {
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
            for (int i = 0; i < this.ids.length; i += CACHE_SIZE) {
                this.rawUtcOffsetsCache[i] = ((ZoneInfo) this.cache.get(this.ids[i])).getRawOffset();
            }
            return this.rawUtcOffsetsCache;
        }

        public String getVersion() {
            return this.version;
        }

        public String getZoneTab() {
            return this.zoneTab;
        }

        public ZoneInfo makeTimeZone(String id) throws IOException {
            ZoneInfo zoneInfo = (ZoneInfo) this.cache.get(id);
            if (zoneInfo == null) {
                return null;
            }
            return (ZoneInfo) zoneInfo.clone();
        }

        public boolean hasTimeZone(String id) throws IOException {
            return this.cache.get(id) != null;
        }

        protected void finalize() throws Throwable {
            if (this.mappedFile != null) {
                this.mappedFile.close();
            }
            super.finalize();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.util.ZoneInfoDB.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.util.ZoneInfoDB.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.util.ZoneInfoDB.<clinit>():void");
    }

    private ZoneInfoDB() {
    }

    public static TzData getInstance() {
        return DATA;
    }
}
