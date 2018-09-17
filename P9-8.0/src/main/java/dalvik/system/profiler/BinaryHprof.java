package dalvik.system.profiler;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class BinaryHprof {
    public static final int ID_SIZE = 4;
    static String MAGIC = "JAVA PROFILE ";

    public enum ControlSettings {
        ALLOC_TRACES(1),
        CPU_SAMPLING(2);
        
        public final int bitmask;

        private ControlSettings(int bitmask) {
            this.bitmask = bitmask;
        }
    }

    public enum Tag {
        STRING_IN_UTF8(1, -4),
        LOAD_CLASS(2, 16),
        UNLOAD_CLASS(3, 4),
        STACK_FRAME(4, 24),
        STACK_TRACE(5, -12),
        ALLOC_SITES(6, -34),
        HEAP_SUMMARY(7, 24),
        START_THREAD(10, 24),
        END_THREAD(11, 4),
        HEAP_DUMP(12, 0),
        HEAP_DUMP_SEGMENT(28, 0),
        HEAP_DUMP_END(44, 0),
        CPU_SAMPLES(13, -8),
        CONTROL_SETTINGS(14, 6);
        
        private static final Map<Byte, Tag> BYTE_TO_TAG = null;
        public final int maximumSize;
        public final int minimumSize;
        public final byte tag;

        static {
            BYTE_TO_TAG = new HashMap();
            Tag[] values = values();
            int length = values.length;
            int i;
            while (i < length) {
                Tag v = values[i];
                BYTE_TO_TAG.put(Byte.valueOf(v.tag), v);
                i++;
            }
        }

        private Tag(int tag, int size) {
            this.tag = (byte) tag;
            if (size > 0) {
                this.minimumSize = size;
                this.maximumSize = size;
                return;
            }
            this.minimumSize = -size;
            this.maximumSize = 0;
        }

        public static Tag get(byte tag) {
            return (Tag) BYTE_TO_TAG.get(Byte.valueOf(tag));
        }

        public String checkSize(int actual) {
            if (actual < this.minimumSize) {
                return "expected a minimial record size of " + this.minimumSize + " for " + this + " but received " + actual;
            }
            if (this.maximumSize != 0 && actual > this.maximumSize) {
                return "expected a maximum record size of " + this.maximumSize + " for " + this + " but received " + actual;
            }
            return null;
        }
    }

    public static final String readMagic(DataInputStream in) {
        try {
            byte[] bytes = new byte[512];
            for (int i = 0; i < bytes.length; i++) {
                byte b = in.readByte();
                if (b == (byte) 0) {
                    String string = new String(bytes, 0, i, "UTF-8");
                    if (string.startsWith(MAGIC)) {
                        return string;
                    }
                    return null;
                }
                bytes[i] = b;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
