package android.icu.impl;

import java.util.Comparator;
import java.util.Iterator;

public class IterableComparator<T> implements Comparator<Iterable<T>> {
    private static final IterableComparator NOCOMPARATOR = null;
    private final Comparator<T> comparator;
    private final int shorterFirst;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.IterableComparator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.IterableComparator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.IterableComparator.<clinit>():void");
    }

    public IterableComparator() {
        this(null, true);
    }

    public IterableComparator(Comparator<T> comparator) {
        this(comparator, true);
    }

    public IterableComparator(Comparator<T> comparator, boolean shorterFirst) {
        this.comparator = comparator;
        this.shorterFirst = shorterFirst ? 1 : -1;
    }

    public int compare(Iterable<T> a, Iterable<T> b) {
        int i = 0;
        if (a == null) {
            if (b != null) {
                i = -this.shorterFirst;
            }
            return i;
        } else if (b == null) {
            return this.shorterFirst;
        } else {
            Iterator<T> bi = b.iterator();
            for (T aItem : a) {
                if (!bi.hasNext()) {
                    return this.shorterFirst;
                }
                int result;
                T bItem = bi.next();
                if (this.comparator != null) {
                    result = this.comparator.compare(aItem, bItem);
                    continue;
                } else {
                    result = ((Comparable) aItem).compareTo(bItem);
                    continue;
                }
                if (result != 0) {
                    return result;
                }
            }
            if (bi.hasNext()) {
                i = -this.shorterFirst;
            }
            return i;
        }
    }

    public static <T> int compareIterables(Iterable<T> a, Iterable<T> b) {
        return NOCOMPARATOR.compare((Iterable) a, (Iterable) b);
    }
}
