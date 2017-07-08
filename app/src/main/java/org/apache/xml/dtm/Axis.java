package org.apache.xml.dtm;

public final class Axis {
    public static final int ALL = 16;
    public static final int ALLFROMNODE = 14;
    public static final int ANCESTOR = 0;
    public static final int ANCESTORORSELF = 1;
    public static final int ATTRIBUTE = 2;
    public static final int CHILD = 3;
    public static final int DESCENDANT = 4;
    public static final int DESCENDANTORSELF = 5;
    public static final int DESCENDANTSFROMROOT = 17;
    public static final int DESCENDANTSORSELFFROMROOT = 18;
    public static final int FILTEREDLIST = 20;
    public static final int FOLLOWING = 6;
    public static final int FOLLOWINGSIBLING = 7;
    public static final int NAMESPACE = 9;
    public static final int NAMESPACEDECLS = 8;
    public static final int PARENT = 10;
    public static final int PRECEDING = 11;
    public static final int PRECEDINGANDANCESTOR = 15;
    public static final int PRECEDINGSIBLING = 12;
    public static final int ROOT = 19;
    public static final int SELF = 13;
    private static final boolean[] isReverse = null;
    private static final String[] names = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.dtm.Axis.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.dtm.Axis.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.dtm.Axis.<clinit>():void");
    }

    public static boolean isReverse(int axis) {
        return isReverse[axis];
    }

    public static String getNames(int index) {
        return names[index];
    }

    public static int getNamesLength() {
        return names.length;
    }
}
