package ohos.global.icu.impl;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import ohos.global.icu.lang.UCharacterEnums;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.VersionInfo;

public final class ICUBinary {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final byte CHAR_SET_ = 0;
    private static final byte CHAR_SIZE_ = 2;
    private static final String HEADER_AUTHENTICATION_FAILED_ = "ICU data file error: Header authentication failed, please check if you have a valid ICU data file";
    private static final byte MAGIC1 = -38;
    private static final byte MAGIC2 = 39;
    private static final String MAGIC_NUMBER_AUTHENTICATION_FAILED_ = "ICU data file error: Not an ICU data file";
    private static final List<DataFile> icuDataFiles = new ArrayList();

    public interface Authenticate {
        boolean isDataVersionAcceptable(byte[] bArr);
    }

    public static byte[] getVersionByteArrayFromCompactInt(int i) {
        return new byte[]{(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i};
    }

    /* access modifiers changed from: private */
    public static final class DatPackageReader {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final int DATA_FORMAT = 1131245124;
        private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();

        private DatPackageReader() {
        }

        /* access modifiers changed from: private */
        public static final class IsAcceptable implements Authenticate {
            private IsAcceptable() {
            }

            @Override // ohos.global.icu.impl.ICUBinary.Authenticate
            public boolean isDataVersionAcceptable(byte[] bArr) {
                return bArr[0] == 1;
            }
        }

        static boolean validate(ByteBuffer byteBuffer) {
            try {
                ICUBinary.readHeader(byteBuffer, DATA_FORMAT, IS_ACCEPTABLE);
                int i = byteBuffer.getInt(byteBuffer.position());
                if (i > 0 && byteBuffer.position() + 4 + (i * 24) <= byteBuffer.capacity() && startsWithPackageName(byteBuffer, getNameOffset(byteBuffer, 0)) && startsWithPackageName(byteBuffer, getNameOffset(byteBuffer, i - 1))) {
                    return true;
                }
                return false;
            } catch (IOException unused) {
            }
        }

        private static boolean startsWithPackageName(ByteBuffer byteBuffer, int i) {
            for (int i2 = 0; i2 < 7; i2++) {
                if (byteBuffer.get(i + i2) != "icudt66b".charAt(i2)) {
                    return false;
                }
            }
            byte b = byteBuffer.get(i + 7);
            if ((b == 98 || b == 108) && byteBuffer.get(i + 8) == 47) {
                return true;
            }
            return false;
        }

        static ByteBuffer getData(ByteBuffer byteBuffer, CharSequence charSequence) {
            int binarySearch = binarySearch(byteBuffer, charSequence);
            if (binarySearch < 0) {
                return null;
            }
            ByteBuffer duplicate = byteBuffer.duplicate();
            duplicate.position(getDataOffset(byteBuffer, binarySearch));
            duplicate.limit(getDataOffset(byteBuffer, binarySearch + 1));
            return ICUBinary.sliceWithOrder(duplicate);
        }

        static void addBaseNamesInFolder(ByteBuffer byteBuffer, String str, String str2, Set<String> set) {
            int binarySearch = binarySearch(byteBuffer, str);
            if (binarySearch < 0) {
                binarySearch = ~binarySearch;
            }
            int i = byteBuffer.getInt(byteBuffer.position());
            StringBuilder sb = new StringBuilder();
            while (binarySearch < i && addBaseName(byteBuffer, binarySearch, str, str2, sb, set)) {
                binarySearch++;
            }
        }

        private static int binarySearch(ByteBuffer byteBuffer, CharSequence charSequence) {
            int i = byteBuffer.getInt(byteBuffer.position());
            int i2 = 0;
            while (i2 < i) {
                int i3 = (i2 + i) >>> 1;
                int compareKeys = ICUBinary.compareKeys(charSequence, byteBuffer, getNameOffset(byteBuffer, i3) + 9);
                if (compareKeys < 0) {
                    i = i3;
                } else if (compareKeys <= 0) {
                    return i3;
                } else {
                    i2 = i3 + 1;
                }
            }
            return ~i2;
        }

        private static int getNameOffset(ByteBuffer byteBuffer, int i) {
            int position = byteBuffer.position();
            return position + byteBuffer.getInt(position + 4 + (i * 8));
        }

        private static int getDataOffset(ByteBuffer byteBuffer, int i) {
            int position = byteBuffer.position();
            if (i == byteBuffer.getInt(position)) {
                return byteBuffer.capacity();
            }
            return position + byteBuffer.getInt(position + 4 + 4 + (i * 8));
        }

        static boolean addBaseName(ByteBuffer byteBuffer, int i, String str, String str2, StringBuilder sb, Set<String> set) {
            int nameOffset = getNameOffset(byteBuffer, i) + 9;
            if (str.length() != 0) {
                int i2 = nameOffset;
                int i3 = 0;
                while (i3 < str.length()) {
                    if (byteBuffer.get(i2) != str.charAt(i3)) {
                        return false;
                    }
                    i3++;
                    i2++;
                }
                nameOffset = i2 + 1;
                if (byteBuffer.get(i2) != 47) {
                    return false;
                }
            }
            sb.setLength(0);
            while (true) {
                int i4 = nameOffset + 1;
                byte b = byteBuffer.get(nameOffset);
                if (b != 0) {
                    char c = (char) b;
                    if (c == '/') {
                        return true;
                    }
                    sb.append(c);
                    nameOffset = i4;
                } else {
                    int length = sb.length() - str2.length();
                    if (sb.lastIndexOf(str2, length) >= 0) {
                        set.add(sb.substring(0, length));
                    }
                    return true;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static abstract class DataFile {
        protected final String itemPath;

        /* access modifiers changed from: package-private */
        public abstract void addBaseNamesInFolder(String str, String str2, Set<String> set);

        /* access modifiers changed from: package-private */
        public abstract ByteBuffer getData(String str);

        DataFile(String str) {
            this.itemPath = str;
        }

        public String toString() {
            return this.itemPath;
        }
    }

    /* access modifiers changed from: private */
    public static final class SingleDataFile extends DataFile {
        private final File path;

        SingleDataFile(String str, File file) {
            super(str);
            this.path = file;
        }

        @Override // ohos.global.icu.impl.ICUBinary.DataFile
        public String toString() {
            return this.path.toString();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUBinary.DataFile
        public ByteBuffer getData(String str) {
            if (str.equals(this.itemPath)) {
                return ICUBinary.mapFile(this.path);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUBinary.DataFile
        public void addBaseNamesInFolder(String str, String str2, Set<String> set) {
            if (this.itemPath.length() > str.length() + str2.length() && this.itemPath.startsWith(str) && this.itemPath.endsWith(str2) && this.itemPath.charAt(str.length()) == '/' && this.itemPath.indexOf(47, str.length() + 1) < 0) {
                set.add(this.itemPath.substring(str.length() + 1, this.itemPath.length() - str2.length()));
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PackageDataFile extends DataFile {
        private final ByteBuffer pkgBytes;

        PackageDataFile(String str, ByteBuffer byteBuffer) {
            super(str);
            this.pkgBytes = byteBuffer;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUBinary.DataFile
        public ByteBuffer getData(String str) {
            return DatPackageReader.getData(this.pkgBytes, str);
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.ICUBinary.DataFile
        public void addBaseNamesInFolder(String str, String str2, Set<String> set) {
            DatPackageReader.addBaseNamesInFolder(this.pkgBytes, str, str2, set);
        }
    }

    static {
        String str = ICUConfig.get(ICUBinary.class.getName() + ".dataPath");
        if (str != null) {
            addDataFilesFromPath(str, icuDataFiles);
        }
    }

    private static void addDataFilesFromPath(String str, List<DataFile> list) {
        int i;
        int i2 = 0;
        while (i2 < str.length()) {
            int indexOf = str.indexOf(File.pathSeparatorChar, i2);
            if (indexOf >= 0) {
                i = indexOf;
            } else {
                i = str.length();
            }
            String trim = str.substring(i2, i).trim();
            if (trim.endsWith(File.separator)) {
                trim = trim.substring(0, trim.length() - 1);
            }
            if (trim.length() != 0) {
                addDataFilesFromFolder(new File(trim), new StringBuilder(), icuDataFiles);
            }
            if (indexOf >= 0) {
                i2 = indexOf + 1;
            } else {
                return;
            }
        }
    }

    private static void addDataFilesFromFolder(File file, StringBuilder sb, List<DataFile> list) {
        File[] listFiles = file.listFiles();
        if (!(listFiles == null || listFiles.length == 0)) {
            int length = sb.length();
            if (length > 0) {
                sb.append('/');
                length++;
            }
            for (File file2 : listFiles) {
                String name = file2.getName();
                if (!name.endsWith(".txt")) {
                    sb.append(name);
                    if (file2.isDirectory()) {
                        addDataFilesFromFolder(file2, sb, list);
                    } else if (name.endsWith(".dat")) {
                        ByteBuffer mapFile = mapFile(file2);
                        if (mapFile != null && DatPackageReader.validate(mapFile)) {
                            list.add(new PackageDataFile(sb.toString(), mapFile));
                        }
                    } else {
                        list.add(new SingleDataFile(sb.toString(), file2));
                    }
                    sb.setLength(length);
                }
            }
        }
    }

    static int compareKeys(CharSequence charSequence, ByteBuffer byteBuffer, int i) {
        int i2 = 0;
        while (true) {
            byte b = byteBuffer.get(i);
            if (b == 0) {
                if (i2 == charSequence.length()) {
                    return 0;
                }
                return 1;
            } else if (i2 == charSequence.length()) {
                return -1;
            } else {
                int charAt = charSequence.charAt(i2) - b;
                if (charAt != 0) {
                    return charAt;
                }
                i2++;
                i++;
            }
        }
    }

    static int compareKeys(CharSequence charSequence, byte[] bArr, int i) {
        int i2 = 0;
        while (true) {
            byte b = bArr[i];
            if (b == 0) {
                if (i2 == charSequence.length()) {
                    return 0;
                }
                return 1;
            } else if (i2 == charSequence.length()) {
                return -1;
            } else {
                int charAt = charSequence.charAt(i2) - b;
                if (charAt != 0) {
                    return charAt;
                }
                i2++;
                i++;
            }
        }
    }

    public static ByteBuffer getData(String str) {
        return getData(null, null, str, false);
    }

    public static ByteBuffer getData(ClassLoader classLoader, String str, String str2) {
        return getData(classLoader, str, str2, false);
    }

    public static ByteBuffer getRequiredData(String str) {
        return getData(null, null, str, true);
    }

    private static ByteBuffer getData(ClassLoader classLoader, String str, String str2, boolean z) {
        ByteBuffer dataFromFile = getDataFromFile(str2);
        if (dataFromFile != null) {
            return dataFromFile;
        }
        if (classLoader == null) {
            classLoader = ClassLoaderUtil.getClassLoader(ICUData.class);
        }
        if (str == null) {
            str = "ohos/global/icu/impl/data/icudt66b/" + str2;
        }
        try {
            InputStream stream = ICUData.getStream(classLoader, str, z);
            if (stream == null) {
                return null;
            }
            return getByteBufferFromInputStreamAndCloseStream(stream);
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    private static ByteBuffer getDataFromFile(String str) {
        for (DataFile dataFile : icuDataFiles) {
            ByteBuffer data = dataFile.getData(str);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static ByteBuffer mapFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            FileChannel channel = fileInputStream.getChannel();
            try {
                return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            } finally {
                fileInputStream.close();
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return null;
        } catch (IOException e2) {
            System.err.println(e2);
            return null;
        }
    }

    public static void addBaseNamesInFileFolder(String str, String str2, Set<String> set) {
        for (DataFile dataFile : icuDataFiles) {
            dataFile.addBaseNamesInFolder(str, str2, set);
        }
    }

    public static VersionInfo readHeaderAndDataVersion(ByteBuffer byteBuffer, int i, Authenticate authenticate) throws IOException {
        return getVersionInfoFromCompactInt(readHeader(byteBuffer, i, authenticate));
    }

    public static int readHeader(ByteBuffer byteBuffer, int i, Authenticate authenticate) throws IOException {
        byte b = byteBuffer.get(2);
        byte b2 = byteBuffer.get(3);
        if (b == -38 && b2 == 39) {
            byte b3 = byteBuffer.get(8);
            byte b4 = byteBuffer.get(9);
            byte b5 = byteBuffer.get(10);
            if (b3 < 0 || 1 < b3 || b4 != 0 || b5 != 2) {
                throw new IOException(HEADER_AUTHENTICATION_FAILED_);
            }
            byteBuffer.order(b3 != 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            char c = byteBuffer.getChar(0);
            char c2 = byteBuffer.getChar(4);
            if (c2 < 20 || c < c2 + 4) {
                throw new IOException("Internal Error: Header size error");
            }
            byte[] bArr = {byteBuffer.get(16), byteBuffer.get(17), byteBuffer.get(18), byteBuffer.get(19)};
            if (byteBuffer.get(12) == ((byte) (i >> 24)) && byteBuffer.get(13) == ((byte) (i >> 16)) && byteBuffer.get(14) == ((byte) (i >> 8)) && byteBuffer.get(15) == ((byte) i) && (authenticate == null || authenticate.isDataVersionAcceptable(bArr))) {
                byteBuffer.position(c);
                return (byteBuffer.get(23) & 255) | (byteBuffer.get(20) << UCharacterEnums.ECharacterCategory.MATH_SYMBOL) | ((byteBuffer.get(21) & 255) << 16) | ((byteBuffer.get(22) & 255) << 8);
            }
            throw new IOException(HEADER_AUTHENTICATION_FAILED_ + String.format("; data format %02x%02x%02x%02x, format version %d.%d.%d.%d", Byte.valueOf(byteBuffer.get(12)), Byte.valueOf(byteBuffer.get(13)), Byte.valueOf(byteBuffer.get(14)), Byte.valueOf(byteBuffer.get(15)), Integer.valueOf(bArr[0] & 255), Integer.valueOf(bArr[1] & 255), Integer.valueOf(bArr[2] & 255), Integer.valueOf(bArr[3] & 255)));
        }
        throw new IOException(MAGIC_NUMBER_AUTHENTICATION_FAILED_);
    }

    public static int writeHeader(int i, int i2, int i3, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeChar(32);
        dataOutputStream.writeByte(-38);
        dataOutputStream.writeByte(39);
        dataOutputStream.writeChar(20);
        dataOutputStream.writeChar(0);
        dataOutputStream.writeByte(1);
        dataOutputStream.writeByte(0);
        dataOutputStream.writeByte(2);
        dataOutputStream.writeByte(0);
        dataOutputStream.writeInt(i);
        dataOutputStream.writeInt(i2);
        dataOutputStream.writeInt(i3);
        dataOutputStream.writeLong(0);
        return 32;
    }

    public static void skipBytes(ByteBuffer byteBuffer, int i) {
        if (i > 0) {
            byteBuffer.position(byteBuffer.position() + i);
        }
    }

    public static byte[] getBytes(ByteBuffer byteBuffer, int i, int i2) {
        byte[] bArr = new byte[i];
        byteBuffer.get(bArr);
        if (i2 > 0) {
            skipBytes(byteBuffer, i2);
        }
        return bArr;
    }

    public static String getString(ByteBuffer byteBuffer, int i, int i2) {
        String charSequence = byteBuffer.asCharBuffer().subSequence(0, i).toString();
        skipBytes(byteBuffer, (i * 2) + i2);
        return charSequence;
    }

    public static char[] getChars(ByteBuffer byteBuffer, int i, int i2) {
        char[] cArr = new char[i];
        byteBuffer.asCharBuffer().get(cArr);
        skipBytes(byteBuffer, (i * 2) + i2);
        return cArr;
    }

    public static short[] getShorts(ByteBuffer byteBuffer, int i, int i2) {
        short[] sArr = new short[i];
        byteBuffer.asShortBuffer().get(sArr);
        skipBytes(byteBuffer, (i * 2) + i2);
        return sArr;
    }

    public static int[] getInts(ByteBuffer byteBuffer, int i, int i2) {
        int[] iArr = new int[i];
        byteBuffer.asIntBuffer().get(iArr);
        skipBytes(byteBuffer, (i * 4) + i2);
        return iArr;
    }

    public static long[] getLongs(ByteBuffer byteBuffer, int i, int i2) {
        long[] jArr = new long[i];
        byteBuffer.asLongBuffer().get(jArr);
        skipBytes(byteBuffer, (i * 8) + i2);
        return jArr;
    }

    public static ByteBuffer sliceWithOrder(ByteBuffer byteBuffer) {
        return byteBuffer.slice().order(byteBuffer.order());
    }

    public static ByteBuffer getByteBufferFromInputStreamAndCloseStream(InputStream inputStream) throws IOException {
        try {
            int available = inputStream.available();
            byte[] bArr = available > 32 ? new byte[available] : new byte[128];
            int i = 0;
            while (true) {
                if (i < bArr.length) {
                    int read = inputStream.read(bArr, i, bArr.length - i);
                    if (read < 0) {
                        break;
                    }
                    i += read;
                } else {
                    int read2 = inputStream.read();
                    if (read2 < 0) {
                        break;
                    }
                    int length = bArr.length * 2;
                    if (length < 128) {
                        length = 128;
                    } else if (length < 16384) {
                        length *= 2;
                    }
                    bArr = Arrays.copyOf(bArr, length);
                    bArr[i] = (byte) read2;
                    i++;
                }
            }
            return ByteBuffer.wrap(bArr, 0, i);
        } finally {
            inputStream.close();
        }
    }

    public static VersionInfo getVersionInfoFromCompactInt(int i) {
        return VersionInfo.getInstance(i >>> 24, (i >> 16) & 255, (i >> 8) & 255, i & 255);
    }
}
