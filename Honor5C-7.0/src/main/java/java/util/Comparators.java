package java.util;

import java.io.Serializable;

class Comparators {

    enum NaturalOrderComparator implements Comparator<Comparable<Object>> {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.Comparators.NaturalOrderComparator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.Comparators.NaturalOrderComparator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.Comparators.NaturalOrderComparator.<clinit>():void");
        }

        public int compare(Comparable<Object> c1, Comparable<Object> c2) {
            return c1.compareTo(c2);
        }

        public Comparator<Comparable<Object>> reversed() {
            return Comparator.reverseOrder();
        }
    }

    static final class NullComparator<T> implements Comparator<T>, Serializable {
        private static final long serialVersionUID = -7569533591570686392L;
        private final boolean nullFirst;
        private final Comparator<T> real;

        NullComparator(boolean nullFirst, Comparator<? super T> real) {
            this.nullFirst = nullFirst;
            this.real = real;
        }

        public int compare(T a, T b) {
            int i = 1;
            int i2 = 0;
            if (a == null) {
                if (b != null) {
                    i2 = this.nullFirst ? -1 : 1;
                }
                return i2;
            } else if (b == null) {
                if (!this.nullFirst) {
                    i = -1;
                }
                return i;
            } else {
                if (this.real != null) {
                    i2 = this.real.compare(a, b);
                }
                return i2;
            }
        }

        public Comparator<T> thenComparing(Comparator<? super T> other) {
            Objects.requireNonNull(other);
            boolean z = this.nullFirst;
            if (this.real != null) {
                other = this.real.thenComparing((Comparator) other);
            }
            return new NullComparator(z, other);
        }

        public Comparator<T> reversed() {
            Comparator comparator = null;
            boolean z = !this.nullFirst;
            if (this.real != null) {
                comparator = this.real.reversed();
            }
            return new NullComparator(z, comparator);
        }
    }

    private Comparators() {
        throw new AssertionError((Object) "no instances");
    }
}
