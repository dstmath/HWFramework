package java.lang;

import java.io.Serializable;
import java.util.jar.Pack200.Unpacker;

public final class Boolean implements Serializable, Comparable<Boolean> {
    public static final Boolean FALSE = null;
    public static final Boolean TRUE = null;
    public static final Class<Boolean> TYPE = null;
    private static final long serialVersionUID = -3665804199014368530L;
    private final boolean value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Boolean.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Boolean.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Boolean.<clinit>():void");
    }

    public Boolean(boolean value) {
        this.value = value;
    }

    public Boolean(String s) {
        this(toBoolean(s));
    }

    public static boolean parseBoolean(String s) {
        return toBoolean(s);
    }

    public boolean booleanValue() {
        return this.value;
    }

    public static Boolean valueOf(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static Boolean valueOf(String s) {
        return toBoolean(s) ? TRUE : FALSE;
    }

    public static String toString(boolean b) {
        return b ? Unpacker.TRUE : Unpacker.FALSE;
    }

    public String toString() {
        return this.value ? Unpacker.TRUE : Unpacker.FALSE;
    }

    public int hashCode() {
        return this.value ? 1231 : 1237;
    }

    public static int hashCode(boolean value) {
        return value ? 1231 : 1237;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Boolean)) {
            return false;
        }
        if (this.value == ((Boolean) obj).booleanValue()) {
            z = true;
        }
        return z;
    }

    public static boolean getBoolean(String name) {
        boolean result = false;
        try {
            result = toBoolean(System.getProperty(name));
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e2) {
        }
        return result;
    }

    public int compareTo(Boolean b) {
        return compare(this.value, b.value);
    }

    public static int compare(boolean x, boolean y) {
        if (x == y) {
            return 0;
        }
        return x ? 1 : -1;
    }

    private static boolean toBoolean(String name) {
        return name != null ? name.equalsIgnoreCase(Unpacker.TRUE) : false;
    }

    public static boolean logicalAnd(boolean a, boolean b) {
        return a ? b : false;
    }

    public static boolean logicalOr(boolean a, boolean b) {
        return !a ? b : true;
    }

    public static boolean logicalXor(boolean a, boolean b) {
        return a ^ b;
    }
}
