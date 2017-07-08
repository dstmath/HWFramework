package java.util;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class OptionalLong {
    private static final OptionalLong EMPTY = null;
    private final boolean isPresent;
    private final long value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.OptionalLong.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.OptionalLong.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.OptionalLong.<clinit>():void");
    }

    private OptionalLong() {
        this.isPresent = false;
        this.value = 0;
    }

    public static OptionalLong empty() {
        return EMPTY;
    }

    private OptionalLong(long value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalLong of(long value) {
        return new OptionalLong(value);
    }

    public long getAsLong() {
        if (this.isPresent) {
            return this.value;
        }
        throw new NoSuchElementException("No value present");
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void ifPresent(LongConsumer consumer) {
        if (this.isPresent) {
            consumer.accept(this.value);
        }
    }

    public long orElse(long other) {
        return this.isPresent ? this.value : other;
    }

    public long orElseGet(LongSupplier other) {
        return this.isPresent ? this.value : other.getAsLong();
    }

    public <X extends Throwable> long orElseThrow(Supplier<X> exceptionSupplier) throws Throwable {
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
        if (!(obj instanceof OptionalLong)) {
            return false;
        }
        OptionalLong other = (OptionalLong) obj;
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
        return this.isPresent ? Long.hashCode(this.value) : 0;
    }

    public String toString() {
        if (!this.isPresent) {
            return "OptionalLong.empty";
        }
        return String.format("OptionalLong[%s]", Long.valueOf(this.value));
    }
}
