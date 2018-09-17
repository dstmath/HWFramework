package java.util;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import sun.misc.DoubleConsts;

public final class OptionalDouble {
    private static final OptionalDouble EMPTY = null;
    private final boolean isPresent;
    private final double value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.OptionalDouble.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.OptionalDouble.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.OptionalDouble.<clinit>():void");
    }

    private OptionalDouble() {
        this.isPresent = false;
        this.value = DoubleConsts.NaN;
    }

    public static OptionalDouble empty() {
        return EMPTY;
    }

    private OptionalDouble(double value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalDouble of(double value) {
        return new OptionalDouble(value);
    }

    public double getAsDouble() {
        if (this.isPresent) {
            return this.value;
        }
        throw new NoSuchElementException("No value present");
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void ifPresent(DoubleConsumer consumer) {
        if (this.isPresent) {
            consumer.accept(this.value);
        }
    }

    public double orElse(double other) {
        return this.isPresent ? this.value : other;
    }

    public double orElseGet(DoubleSupplier other) {
        return this.isPresent ? this.value : other.getAsDouble();
    }

    public <X extends Throwable> double orElseThrow(Supplier<X> exceptionSupplier) throws Throwable {
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
        if (!(obj instanceof OptionalDouble)) {
            return false;
        }
        OptionalDouble other = (OptionalDouble) obj;
        if (this.isPresent && other.isPresent) {
            if (Double.compare(this.value, other.value) != 0) {
                z = false;
            }
        } else if (this.isPresent != other.isPresent) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.isPresent ? Double.hashCode(this.value) : 0;
    }

    public String toString() {
        if (!this.isPresent) {
            return "OptionalDouble.empty";
        }
        return String.format("OptionalDouble[%s]", Double.valueOf(this.value));
    }
}
