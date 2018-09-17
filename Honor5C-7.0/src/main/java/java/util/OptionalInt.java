package java.util;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class OptionalInt {
    private static final OptionalInt EMPTY = null;
    private final boolean isPresent;
    private final int value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.OptionalInt.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.OptionalInt.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.OptionalInt.<clinit>():void");
    }

    private OptionalInt() {
        this.isPresent = false;
        this.value = 0;
    }

    public static OptionalInt empty() {
        return EMPTY;
    }

    private OptionalInt(int value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalInt of(int value) {
        return new OptionalInt(value);
    }

    public int getAsInt() {
        if (this.isPresent) {
            return this.value;
        }
        throw new NoSuchElementException("No value present");
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void ifPresent(IntConsumer consumer) {
        if (this.isPresent) {
            consumer.accept(this.value);
        }
    }

    public int orElse(int other) {
        return this.isPresent ? this.value : other;
    }

    public int orElseGet(IntSupplier other) {
        return this.isPresent ? this.value : other.getAsInt();
    }

    public <X extends Throwable> int orElseThrow(Supplier<X> exceptionSupplier) throws Throwable {
        if (this.isPresent) {
            return this.value;
        }
        throw ((Throwable) exceptionSupplier.get());
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OptionalInt)) {
            return false;
        }
        OptionalInt other = (OptionalInt) obj;
        if (this.isPresent && other.isPresent) {
            if (this.value != other.value) {
                z = false;
            }
        } else if (this.isPresent != other.isPresent) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.isPresent ? Integer.hashCode(this.value) : 0;
    }

    public String toString() {
        if (!this.isPresent) {
            return "OptionalInt.empty";
        }
        return String.format("OptionalInt[%s]", Integer.valueOf(this.value));
    }
}
