package android.media.scan;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.LongArray;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import libcore.io.IoBridge;
import libcore.io.Memory;

public class IsoInterface {
    public static final int BOX_FTYP = 1718909296;
    public static final int BOX_GPS = 1735422752;
    public static final int BOX_GPS0 = 1735422768;
    public static final int BOX_LOCI = 1819239273;
    public static final int BOX_META = 1835365473;
    public static final int BOX_UUID = 1970628964;
    public static final int BOX_XMP = 1481461855;
    public static final int BOX_XYZ = -1451722374;
    private static final boolean LOGV = Log.isLoggable(TAG, 2);
    private static final String TAG = "IsoInterface";
    private List<Box> mFlattened = new ArrayList();
    private List<Box> mRoots = new ArrayList();

    private static boolean isBoxParent(int type) {
        switch (type) {
            case 1684631142:
            case 1701082227:
            case 1751740006:
            case 1752069225:
            case 1768715124:
            case 1768977007:
            case 1785737832:
            case 1835297121:
            case BOX_META /* 1835365473 */:
            case 1835430497:
            case 1835626086:
            case 1836019558:
            case 1836019574:
            case 1936289382:
            case 1937007212:
            case 1953653094:
            case 1953653099:
            case 1953654118:
            case 1969517665:
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Box {
        public List<Box> children;
        public byte[] data;
        public final long[] range;
        public final int type;
        public UUID uuid;

        public Box(int type2, long[] range2) {
            this.type = type2;
            this.range = range2;
        }
    }

    private static String typeToString(int type) {
        byte[] buf = new byte[4];
        Memory.pokeInt(buf, 0, type, ByteOrder.BIG_ENDIAN);
        return new String(buf);
    }

    private static int readInt(FileDescriptor fd) throws ErrnoException, IOException {
        byte[] buf = new byte[4];
        if (Os.read(fd, buf, 0, 4) == 4) {
            return Memory.peekInt(buf, 0, ByteOrder.BIG_ENDIAN);
        }
        throw new EOFException();
    }

    private static UUID readUuid(FileDescriptor fd) throws ErrnoException, IOException {
        return new UUID((((long) readInt(fd)) << 32) | (((long) readInt(fd)) & 4294967295L), (((long) readInt(fd)) << 32) | (((long) readInt(fd)) & 4294967295L));
    }

    private static Box parseNextBox(FileDescriptor fd, long end, String prefix) throws ErrnoException, IOException {
        long pos = Os.lseek(fd, 0, OsConstants.SEEK_CUR);
        if (pos == end) {
            return null;
        }
        long len = Integer.toUnsignedLong(readInt(fd));
        if (len <= 0 || pos + len > end) {
            Log.w(TAG, "Invalid box at " + pos + " of length " + len + " reached beyond end of parent " + end);
            return null;
        }
        int type = readInt(fd);
        if (type == 1835365473) {
            readInt(fd);
        }
        Box box = new Box(type, new long[]{pos, len});
        if (LOGV) {
            Log.v(TAG, prefix + "Found box " + typeToString(type) + " at " + pos + " length " + len);
        }
        if (type == 1970628964) {
            box.uuid = readUuid(fd);
            if (LOGV) {
                Log.v(TAG, prefix + "  UUID " + box.uuid);
            }
            box.data = new byte[((int) ((len - 8) - 16))];
            IoBridge.read(fd, box.data, 0, box.data.length);
        }
        if (type == 1481461855) {
            box.data = new byte[((int) (len - 8))];
            IoBridge.read(fd, box.data, 0, box.data.length);
        }
        if (isBoxParent(type)) {
            box.children = new ArrayList();
            while (true) {
                Box child = parseNextBox(fd, pos + len, prefix + "  ");
                if (child == null) {
                    break;
                }
                box.children.add(child);
            }
        }
        Os.lseek(fd, pos + len, OsConstants.SEEK_SET);
        return box;
    }

    private IsoInterface(FileDescriptor fd) throws IOException {
        try {
            Os.lseek(fd, 4, OsConstants.SEEK_SET);
            if (readInt(fd) == 1718909296) {
                long end = Os.lseek(fd, 0, OsConstants.SEEK_END);
                Os.lseek(fd, 0, OsConstants.SEEK_SET);
                while (true) {
                    Box box = parseNextBox(fd, end, "");
                    if (box == null) {
                        break;
                    }
                    this.mRoots.add(box);
                }
                Queue<Box> queue = new LinkedList<>(this.mRoots);
                while (!queue.isEmpty()) {
                    Box box2 = queue.poll();
                    this.mFlattened.add(box2);
                    if (box2.children != null) {
                        queue.addAll(box2.children);
                    }
                }
            } else if (LOGV) {
                Log.w(TAG, "Missing 'ftyp' header");
            }
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001c, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0013, code lost:
        r2 = move-exception;
     */
    public static IsoInterface fromFile(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        IsoInterface fromFileDescriptor = fromFileDescriptor(is.getFD());
        is.close();
        return fromFileDescriptor;
    }

    public static IsoInterface fromFileDescriptor(FileDescriptor fd) throws IOException {
        return new IsoInterface(fd);
    }

    public long[] getBoxRanges(int type) {
        LongArray res = new LongArray();
        for (Box box : this.mFlattened) {
            if (box.type == type) {
                res.add(box.range[0] + 8);
                res.add(box.range[0] + box.range[1]);
            }
        }
        return res.toArray();
    }

    public long[] getBoxRanges(UUID uuid) {
        LongArray res = new LongArray();
        for (Box box : this.mFlattened) {
            if (box.type == 1970628964 && Objects.equals(box.uuid, uuid)) {
                res.add(box.range[0] + 8 + 16);
                res.add(box.range[0] + box.range[1]);
            }
        }
        return res.toArray();
    }

    public byte[] getBoxBytes(int type) {
        for (Box box : this.mFlattened) {
            if (box.type == type) {
                return box.data;
            }
        }
        return null;
    }

    public byte[] getBoxBytes(UUID uuid) {
        for (Box box : this.mFlattened) {
            if (box.type == 1970628964 && Objects.equals(box.uuid, uuid)) {
                return box.data;
            }
        }
        return null;
    }
}
