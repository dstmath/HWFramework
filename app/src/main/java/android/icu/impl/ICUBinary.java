package android.icu.impl;

import android.icu.util.ICUUncheckedIOException;
import android.icu.util.VersionInfo;
import dalvik.bytecode.Opcodes;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.List;
import java.util.Set;
import libcore.icu.DateUtilsBridge;
import org.w3c.dom.traversal.NodeFilter;

public final class ICUBinary {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final byte CHAR_SET_ = (byte) 0;
    private static final byte CHAR_SIZE_ = (byte) 2;
    private static final String HEADER_AUTHENTICATION_FAILED_ = "ICU data file error: Header authentication failed, please check if you have a valid ICU data file";
    private static final byte MAGIC1 = (byte) -38;
    private static final byte MAGIC2 = (byte) 39;
    private static final String MAGIC_NUMBER_AUTHENTICATION_FAILED_ = "ICU data file error: Not an ICU data file";
    private static final List<DataFile> icuDataFiles = null;

    public interface Authenticate {
        boolean isDataVersionAcceptable(byte[] bArr);
    }

    private static final class DatPackageReader {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final int DATA_FORMAT = 1131245124;
        private static final IsAcceptable IS_ACCEPTABLE = null;

        private static final class IsAcceptable implements Authenticate {
            private IsAcceptable() {
            }

            public boolean isDataVersionAcceptable(byte[] version) {
                return version[0] == (byte) 1 ? true : DatPackageReader.-assertionsDisabled;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUBinary.DatPackageReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUBinary.DatPackageReader.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUBinary.DatPackageReader.<clinit>():void");
        }

        static void addBaseNamesInFolder(java.nio.ByteBuffer r1, java.lang.String r2, java.lang.String r3, java.util.Set<java.lang.String> r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUBinary.DatPackageReader.addBaseNamesInFolder(java.nio.ByteBuffer, java.lang.String, java.lang.String, java.util.Set):void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUBinary.DatPackageReader.addBaseNamesInFolder(java.nio.ByteBuffer, java.lang.String, java.lang.String, java.util.Set):void");
        }

        private static int binarySearch(java.nio.ByteBuffer r1, java.lang.CharSequence r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUBinary.DatPackageReader.binarySearch(java.nio.ByteBuffer, java.lang.CharSequence):int
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUBinary.DatPackageReader.binarySearch(java.nio.ByteBuffer, java.lang.CharSequence):int");
        }

        private DatPackageReader() {
        }

        static boolean validate(ByteBuffer bytes) {
            try {
                ICUBinary.readHeader(bytes, DATA_FORMAT, IS_ACCEPTABLE);
                int count = bytes.getInt(bytes.position());
                if (count > 0 && (bytes.position() + 4) + (count * 24) <= bytes.capacity() && startsWithPackageName(bytes, getNameOffset(bytes, 0)) && startsWithPackageName(bytes, getNameOffset(bytes, count - 1))) {
                    return true;
                }
                return -assertionsDisabled;
            } catch (IOException e) {
                return -assertionsDisabled;
            }
        }

        private static boolean startsWithPackageName(ByteBuffer bytes, int start) {
            int length = "icudt56b".length() - 1;
            for (int i = 0; i < length; i++) {
                if (bytes.get(start + i) != "icudt56b".charAt(i)) {
                    return -assertionsDisabled;
                }
            }
            int length2 = length + 1;
            byte c = bytes.get(start + length);
            if ((c == 98 || c == 108) && bytes.get(start + length2) == 47) {
                return true;
            }
            return -assertionsDisabled;
        }

        static ByteBuffer getData(ByteBuffer bytes, CharSequence key) {
            int index = binarySearch(bytes, key);
            if (index < 0) {
                return null;
            }
            ByteBuffer data = bytes.duplicate();
            data.position(getDataOffset(bytes, index));
            data.limit(getDataOffset(bytes, index + 1));
            return ICUBinary.sliceWithOrder(data);
        }

        private static int getNameOffset(ByteBuffer bytes, int index) {
            Object obj = null;
            int base = bytes.position();
            if (!-assertionsDisabled) {
                if (index >= 0 && index < bytes.getInt(base)) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return bytes.getInt((base + 4) + (index * 8)) + base;
        }

        private static int getDataOffset(ByteBuffer bytes, int index) {
            Object obj = null;
            int base = bytes.position();
            int count = bytes.getInt(base);
            if (index == count) {
                return bytes.capacity();
            }
            if (!-assertionsDisabled) {
                if (index >= 0 && index < count) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return bytes.getInt(((base + 4) + 4) + (index * 8)) + base;
        }

        static boolean addBaseName(ByteBuffer bytes, int index, String folder, String suffix, StringBuilder sb, Set<String> names) {
            int offset;
            int offset2 = getNameOffset(bytes, index) + ("icudt56b".length() + 1);
            if (folder.length() != 0) {
                int i = 0;
                while (i < folder.length()) {
                    if (bytes.get(offset2) != folder.charAt(i)) {
                        return -assertionsDisabled;
                    }
                    i++;
                    offset2++;
                }
                offset = offset2 + 1;
                if (bytes.get(offset2) != 47) {
                    return -assertionsDisabled;
                }
                offset2 = offset;
            }
            sb.setLength(0);
            while (true) {
                offset = offset2 + 1;
                byte b = bytes.get(offset2);
                if (b == null) {
                    break;
                }
                char c = (char) b;
                if (c == '/') {
                    return true;
                }
                sb.append(c);
                offset2 = offset;
            }
            int nameLimit = sb.length() - suffix.length();
            if (sb.lastIndexOf(suffix, nameLimit) >= 0) {
                names.add(sb.substring(0, nameLimit));
            }
            return true;
        }
    }

    private static abstract class DataFile {
        protected final String itemPath;

        abstract void addBaseNamesInFolder(String str, String str2, Set<String> set);

        abstract ByteBuffer getData(String str);

        DataFile(String item) {
            this.itemPath = item;
        }

        public String toString() {
            return this.itemPath;
        }
    }

    private static final class PackageDataFile extends DataFile {
        private final ByteBuffer pkgBytes;

        PackageDataFile(String item, ByteBuffer bytes) {
            super(item);
            this.pkgBytes = bytes;
        }

        ByteBuffer getData(String requestedPath) {
            return DatPackageReader.getData(this.pkgBytes, requestedPath);
        }

        void addBaseNamesInFolder(String folder, String suffix, Set<String> names) {
            DatPackageReader.addBaseNamesInFolder(this.pkgBytes, folder, suffix, names);
        }
    }

    private static final class SingleDataFile extends DataFile {
        private final File path;

        SingleDataFile(String item, File path) {
            super(item);
            this.path = path;
        }

        public String toString() {
            return this.path.toString();
        }

        ByteBuffer getData(String requestedPath) {
            if (requestedPath.equals(this.itemPath)) {
                return ICUBinary.mapFile(this.path);
            }
            return null;
        }

        void addBaseNamesInFolder(String folder, String suffix, Set<String> names) {
            if (this.itemPath.length() > folder.length() + suffix.length() && this.itemPath.startsWith(folder) && this.itemPath.endsWith(suffix) && this.itemPath.charAt(folder.length()) == '/' && this.itemPath.indexOf(47, folder.length() + 1) < 0) {
                names.add(this.itemPath.substring(folder.length() + 1, this.itemPath.length() - suffix.length()));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUBinary.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUBinary.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUBinary.<clinit>():void");
    }

    public ICUBinary() {
    }

    private static void addDataFilesFromPath(String dataPath, List<DataFile> list) {
        int pathStart = 0;
        while (pathStart < dataPath.length()) {
            int pathLimit;
            int sepIndex = dataPath.indexOf(File.pathSeparatorChar, pathStart);
            if (sepIndex >= 0) {
                pathLimit = sepIndex;
            } else {
                pathLimit = dataPath.length();
            }
            String path = dataPath.substring(pathStart, pathLimit).trim();
            if (path.endsWith(File.separator)) {
                path = path.substring(0, path.length() - 1);
            }
            if (path.length() != 0) {
                addDataFilesFromFolder(new File(path), new StringBuilder(), icuDataFiles);
            }
            if (sepIndex >= 0) {
                pathStart = sepIndex + 1;
            } else {
                return;
            }
        }
    }

    private static void addDataFilesFromFolder(File folder, StringBuilder itemPath, List<DataFile> dataFiles) {
        File[] files = folder.listFiles();
        if (files != null && files.length != 0) {
            int folderPathLength = itemPath.length();
            if (folderPathLength > 0) {
                itemPath.append('/');
                folderPathLength++;
            }
            for (File file : files) {
                String fileName = file.getName();
                if (!fileName.endsWith(".txt")) {
                    itemPath.append(fileName);
                    if (file.isDirectory()) {
                        addDataFilesFromFolder(file, itemPath, dataFiles);
                    } else if (fileName.endsWith(".dat")) {
                        ByteBuffer pkgBytes = mapFile(file);
                        if (pkgBytes != null && DatPackageReader.validate(pkgBytes)) {
                            dataFiles.add(new PackageDataFile(itemPath.toString(), pkgBytes));
                        }
                    } else {
                        dataFiles.add(new SingleDataFile(itemPath.toString(), file));
                    }
                    itemPath.setLength(folderPathLength);
                }
            }
        }
    }

    static int compareKeys(CharSequence key, ByteBuffer bytes, int offset) {
        int i = 0;
        while (true) {
            int c2 = bytes.get(offset);
            if (c2 == 0) {
                break;
            } else if (i == key.length()) {
                return -1;
            } else {
                int diff = key.charAt(i) - c2;
                if (diff != 0) {
                    return diff;
                }
                i++;
                offset++;
            }
        }
        if (i == key.length()) {
            return 0;
        }
        return 1;
    }

    static int compareKeys(CharSequence key, byte[] bytes, int offset) {
        int i = 0;
        while (true) {
            int c2 = bytes[offset];
            if (c2 == 0) {
                break;
            } else if (i == key.length()) {
                return -1;
            } else {
                int diff = key.charAt(i) - c2;
                if (diff != 0) {
                    return diff;
                }
                i++;
                offset++;
            }
        }
        if (i == key.length()) {
            return 0;
        }
        return 1;
    }

    public static ByteBuffer getData(String itemPath) {
        return getData(null, null, itemPath, -assertionsDisabled);
    }

    public static ByteBuffer getData(ClassLoader loader, String resourceName, String itemPath) {
        return getData(loader, resourceName, itemPath, -assertionsDisabled);
    }

    public static ByteBuffer getRequiredData(String itemPath) {
        return getData(null, null, itemPath, true);
    }

    private static ByteBuffer getData(ClassLoader loader, String resourceName, String itemPath, boolean required) {
        ByteBuffer bytes = getDataFromFile(itemPath);
        if (bytes != null) {
            return bytes;
        }
        if (loader == null) {
            loader = ClassLoaderUtil.getClassLoader(ICUData.class);
        }
        if (resourceName == null) {
            resourceName = "android/icu/impl/data/icudt56b/" + itemPath;
        }
        try {
            InputStream is = ICUData.getStream(loader, resourceName, required);
            if (is == null) {
                return null;
            }
            return getByteBufferFromInputStreamAndCloseStream(is);
        } catch (Throwable e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    private static ByteBuffer getDataFromFile(String itemPath) {
        for (DataFile dataFile : icuDataFiles) {
            ByteBuffer data = dataFile.getData(itemPath);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    private static ByteBuffer mapFile(File path) {
        FileInputStream file;
        try {
            file = new FileInputStream(path);
            FileChannel channel = file.getChannel();
            ByteBuffer bytes = channel.map(MapMode.READ_ONLY, 0, channel.size());
            file.close();
            return bytes;
        } catch (FileNotFoundException ignored) {
            System.err.println(ignored);
            return null;
        } catch (IOException ignored2) {
            System.err.println(ignored2);
            return null;
        } catch (Throwable th) {
            file.close();
        }
    }

    public static void addBaseNamesInFileFolder(String folder, String suffix, Set<String> names) {
        for (DataFile dataFile : icuDataFiles) {
            dataFile.addBaseNamesInFolder(folder, suffix, names);
        }
    }

    public static VersionInfo readHeaderAndDataVersion(ByteBuffer bytes, int dataFormat, Authenticate authenticate) throws IOException {
        return getVersionInfoFromCompactInt(readHeader(bytes, dataFormat, authenticate));
    }

    public static int readHeader(ByteBuffer bytes, int dataFormat, Authenticate authenticate) throws IOException {
        if (!-assertionsDisabled) {
            if ((bytes.position() == 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        byte magic1 = bytes.get(2);
        byte magic2 = bytes.get(3);
        if (magic1 == -38 && magic2 == 39) {
            byte isBigEndian = bytes.get(8);
            byte charsetFamily = bytes.get(9);
            byte sizeofUChar = bytes.get(10);
            if (isBigEndian < null || 1 < isBigEndian || charsetFamily != null || sizeofUChar != 2) {
                throw new IOException(HEADER_AUTHENTICATION_FAILED_);
            }
            bytes.order(isBigEndian != null ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            int headerSize = bytes.getChar(0);
            int sizeofUDataInfo = bytes.getChar(4);
            if (sizeofUDataInfo < 20 || headerSize < sizeofUDataInfo + 4) {
                throw new IOException("Internal Error: Header size error");
            }
            byte[] formatVersion = new byte[]{bytes.get(16), bytes.get(17), bytes.get(18), bytes.get(19)};
            if (bytes.get(12) == ((byte) (dataFormat >> 24)) && bytes.get(13) == ((byte) (dataFormat >> 16)) && bytes.get(14) == ((byte) (dataFormat >> 8)) && bytes.get(15) == ((byte) dataFormat) && (authenticate == null || authenticate.isDataVersionAcceptable(formatVersion))) {
                bytes.position(headerSize);
                return (((bytes.get(20) << 24) | ((bytes.get(21) & Opcodes.OP_CONST_CLASS_JUMBO) << 16)) | ((bytes.get(22) & Opcodes.OP_CONST_CLASS_JUMBO) << 8)) | (bytes.get(23) & Opcodes.OP_CONST_CLASS_JUMBO);
            }
            throw new IOException(HEADER_AUTHENTICATION_FAILED_ + String.format("; data format %02x%02x%02x%02x, format version %d.%d.%d.%d", new Object[]{Byte.valueOf(bytes.get(12)), Byte.valueOf(bytes.get(13)), Byte.valueOf(bytes.get(14)), Byte.valueOf(bytes.get(15)), Integer.valueOf(formatVersion[0] & Opcodes.OP_CONST_CLASS_JUMBO), Integer.valueOf(formatVersion[1] & Opcodes.OP_CONST_CLASS_JUMBO), Integer.valueOf(formatVersion[2] & Opcodes.OP_CONST_CLASS_JUMBO), Integer.valueOf(formatVersion[3] & Opcodes.OP_CONST_CLASS_JUMBO)}));
        }
        throw new IOException(MAGIC_NUMBER_AUTHENTICATION_FAILED_);
    }

    public static int writeHeader(int dataFormat, int formatVersion, int dataVersion, DataOutputStream dos) throws IOException {
        int i = 1;
        dos.writeChar(32);
        dos.writeByte(-38);
        dos.writeByte(39);
        dos.writeChar(20);
        dos.writeChar(0);
        dos.writeByte(1);
        dos.writeByte(0);
        dos.writeByte(2);
        dos.writeByte(0);
        dos.writeInt(dataFormat);
        dos.writeInt(formatVersion);
        dos.writeInt(dataVersion);
        dos.writeLong(0);
        if (!-assertionsDisabled) {
            if (dos.size() != 32) {
                i = 0;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        return 32;
    }

    public static void skipBytes(ByteBuffer bytes, int skipLength) {
        if (skipLength > 0) {
            bytes.position(bytes.position() + skipLength);
        }
    }

    public static String getString(ByteBuffer bytes, int length, int additionalSkipLength) {
        String s = bytes.asCharBuffer().subSequence(0, length).toString();
        skipBytes(bytes, (length * 2) + additionalSkipLength);
        return s;
    }

    public static char[] getChars(ByteBuffer bytes, int length, int additionalSkipLength) {
        char[] dest = new char[length];
        bytes.asCharBuffer().get(dest);
        skipBytes(bytes, (length * 2) + additionalSkipLength);
        return dest;
    }

    public static short[] getShorts(ByteBuffer bytes, int length, int additionalSkipLength) {
        short[] dest = new short[length];
        bytes.asShortBuffer().get(dest);
        skipBytes(bytes, (length * 2) + additionalSkipLength);
        return dest;
    }

    public static int[] getInts(ByteBuffer bytes, int length, int additionalSkipLength) {
        int[] dest = new int[length];
        bytes.asIntBuffer().get(dest);
        skipBytes(bytes, (length * 4) + additionalSkipLength);
        return dest;
    }

    public static long[] getLongs(ByteBuffer bytes, int length, int additionalSkipLength) {
        long[] dest = new long[length];
        bytes.asLongBuffer().get(dest);
        skipBytes(bytes, (length * 8) + additionalSkipLength);
        return dest;
    }

    public static ByteBuffer sliceWithOrder(ByteBuffer bytes) {
        return bytes.slice().order(bytes.order());
    }

    public static ByteBuffer getByteBufferFromInputStreamAndCloseStream(InputStream is) throws IOException {
        try {
            byte[] bytes;
            int avail = is.available();
            if (avail > 32) {
                bytes = new byte[avail];
            } else {
                bytes = new byte[NodeFilter.SHOW_COMMENT];
            }
            int length = 0;
            while (true) {
                int length2;
                if (length >= bytes.length) {
                    int nextByte = is.read();
                    if (nextByte < 0) {
                        break;
                    }
                    int capacity = bytes.length * 2;
                    if (capacity < NodeFilter.SHOW_COMMENT) {
                        capacity = NodeFilter.SHOW_COMMENT;
                    } else if (capacity < DateUtilsBridge.FORMAT_ABBREV_TIME) {
                        capacity *= 2;
                    }
                    byte[] newBytes = new byte[capacity];
                    System.arraycopy(bytes, 0, newBytes, 0, length);
                    bytes = newBytes;
                    length2 = length + 1;
                    newBytes[length] = (byte) nextByte;
                    length = length2;
                } else {
                    int numRead = is.read(bytes, length, bytes.length - length);
                    if (numRead < 0) {
                        break;
                    }
                    length2 = length + numRead;
                    length = length2;
                }
            }
            ByteBuffer wrap = ByteBuffer.wrap(bytes, 0, length);
            return wrap;
        } finally {
            is.close();
        }
    }

    public static VersionInfo getVersionInfoFromCompactInt(int version) {
        return VersionInfo.getInstance(version >>> 24, (version >> 16) & Opcodes.OP_CONST_CLASS_JUMBO, (version >> 8) & Opcodes.OP_CONST_CLASS_JUMBO, version & Opcodes.OP_CONST_CLASS_JUMBO);
    }

    public static byte[] getVersionByteArrayFromCompactInt(int version) {
        return new byte[]{(byte) (version >> 24), (byte) (version >> 16), (byte) (version >> 8), (byte) version};
    }
}
