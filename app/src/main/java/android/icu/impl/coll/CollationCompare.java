package android.icu.impl.coll;

public final class CollationCompare {
    static final /* synthetic */ boolean -assertionsDisabled = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationCompare.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationCompare.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationCompare.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int compareUpToQuaternary(CollationIterator left, CollationIterator right, CollationSettings settings) {
        long variableTop;
        long leftPrimary;
        long rightPrimary;
        int options = settings.options;
        if ((options & 12) == 0) {
            variableTop = 0;
        } else {
            variableTop = settings.variableTop + 1;
        }
        boolean anyVariable = false;
        while (true) {
            long ce = left.nextCE();
            leftPrimary = ce >>> 32;
            if (leftPrimary < variableTop && leftPrimary > Collation.MERGE_SEPARATOR_PRIMARY) {
                anyVariable = true;
                do {
                    left.setCurrentCE(-4294967296L & ce);
                    while (true) {
                        ce = left.nextCE();
                        leftPrimary = ce >>> 32;
                        if (leftPrimary != 0) {
                            break;
                        }
                        left.setCurrentCE(0);
                    }
                    if (leftPrimary >= variableTop) {
                        break;
                    }
                } while (leftPrimary > Collation.MERGE_SEPARATOR_PRIMARY);
            }
            if (leftPrimary != 0) {
                do {
                    ce = right.nextCE();
                    rightPrimary = ce >>> 32;
                    if (rightPrimary < variableTop && rightPrimary > Collation.MERGE_SEPARATOR_PRIMARY) {
                        anyVariable = true;
                        do {
                            right.setCurrentCE(-4294967296L & ce);
                            while (true) {
                                ce = right.nextCE();
                                rightPrimary = ce >>> 32;
                                if (rightPrimary != 0) {
                                    break;
                                }
                                right.setCurrentCE(0);
                            }
                            if (rightPrimary >= variableTop) {
                                break;
                            }
                        } while (rightPrimary > Collation.MERGE_SEPARATOR_PRIMARY);
                    }
                } while (rightPrimary == 0);
                if (leftPrimary != rightPrimary) {
                    break;
                } else if (leftPrimary == 1) {
                    break;
                }
            }
        }
        if (settings.hasReordering()) {
            leftPrimary = settings.reorder(leftPrimary);
            rightPrimary = settings.reorder(rightPrimary);
        }
        return leftPrimary < rightPrimary ? -1 : 1;
    }
}
