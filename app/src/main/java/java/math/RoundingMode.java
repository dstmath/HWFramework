package java.math;

import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public enum RoundingMode {
    ;
    
    private final int bigDecimalRM;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.math.RoundingMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.math.RoundingMode.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.math.RoundingMode.<clinit>():void");
    }

    private RoundingMode(int rm) {
        this.bigDecimalRM = rm;
    }

    public static RoundingMode valueOf(int mode) {
        switch (mode) {
            case XmlPullParser.START_DOCUMENT /*0*/:
                return UP;
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return DOWN;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                return CEILING;
            case XmlPullParser.END_TAG /*3*/:
                return FLOOR;
            case NodeFilter.SHOW_TEXT /*4*/:
                return HALF_UP;
            case XmlPullParser.CDSECT /*5*/:
                return HALF_DOWN;
            case XmlPullParser.ENTITY_REF /*6*/:
                return HALF_EVEN;
            case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                return UNNECESSARY;
            default:
                throw new IllegalArgumentException("Invalid rounding mode");
        }
    }
}
