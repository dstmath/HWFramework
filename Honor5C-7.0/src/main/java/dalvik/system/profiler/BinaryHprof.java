package dalvik.system.profiler;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import org.w3c.dom.traversal.NodeFilter;

public final class BinaryHprof {
    public static final int ID_SIZE = 4;
    static String MAGIC;

    public enum ControlSettings {
        ;
        
        public final int bitmask;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: dalvik.system.profiler.BinaryHprof.ControlSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: dalvik.system.profiler.BinaryHprof.ControlSettings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprof.ControlSettings.<clinit>():void");
        }

        private ControlSettings(int bitmask) {
            this.bitmask = bitmask;
        }
    }

    public enum Tag {
        ;
        
        private static final Map<Byte, Tag> BYTE_TO_TAG = null;
        public final int maximumSize;
        public final int minimumSize;
        public final byte tag;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: dalvik.system.profiler.BinaryHprof.Tag.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: dalvik.system.profiler.BinaryHprof.Tag.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00f0
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprof.Tag.<clinit>():void");
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: dalvik.system.profiler.BinaryHprof.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: dalvik.system.profiler.BinaryHprof.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprof.<clinit>():void");
    }

    public BinaryHprof() {
    }

    public static final String readMagic(DataInputStream in) {
        try {
            byte[] bytes = new byte[NodeFilter.SHOW_DOCUMENT_TYPE];
            for (int i = 0; i < bytes.length; i++) {
                byte b = in.readByte();
                if (b == null) {
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
