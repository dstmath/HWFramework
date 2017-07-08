package com.android.dex;

import com.android.dex.util.Unsigned;

public final class TypeList implements Comparable<TypeList> {
    public static final TypeList EMPTY = null;
    private final Dex dex;
    private final short[] types;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.dex.TypeList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.dex.TypeList.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.dex.TypeList.<clinit>():void");
    }

    public TypeList(Dex dex, short[] types) {
        this.dex = dex;
        this.types = types;
    }

    public short[] getTypes() {
        return this.types;
    }

    public int compareTo(TypeList other) {
        int i = 0;
        while (i < this.types.length && i < other.types.length) {
            if (this.types[i] != other.types[i]) {
                return Unsigned.compare(this.types[i], other.types[i]);
            }
            i++;
        }
        return Unsigned.compare(this.types.length, other.types.length);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        int typesLength = this.types.length;
        for (int i = 0; i < typesLength; i++) {
            result.append(this.dex != null ? this.dex.typeNames().get(this.types[i]) : Short.valueOf(this.types[i]));
        }
        result.append(")");
        return result.toString();
    }
}
