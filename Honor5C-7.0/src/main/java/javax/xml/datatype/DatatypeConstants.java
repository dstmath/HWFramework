package javax.xml.datatype;

import javax.xml.namespace.QName;

public final class DatatypeConstants {
    public static final int APRIL = 4;
    public static final int AUGUST = 8;
    public static final QName DATE = null;
    public static final QName DATETIME = null;
    public static final Field DAYS = null;
    public static final int DECEMBER = 12;
    public static final QName DURATION = null;
    public static final QName DURATION_DAYTIME = null;
    public static final QName DURATION_YEARMONTH = null;
    public static final int EQUAL = 0;
    public static final int FEBRUARY = 2;
    public static final int FIELD_UNDEFINED = Integer.MIN_VALUE;
    public static final QName GDAY = null;
    public static final QName GMONTH = null;
    public static final QName GMONTHDAY = null;
    public static final int GREATER = 1;
    public static final QName GYEAR = null;
    public static final QName GYEARMONTH = null;
    public static final Field HOURS = null;
    public static final int INDETERMINATE = 2;
    public static final int JANUARY = 1;
    public static final int JULY = 7;
    public static final int JUNE = 6;
    public static final int LESSER = -1;
    public static final int MARCH = 3;
    public static final int MAX_TIMEZONE_OFFSET = -840;
    public static final int MAY = 5;
    public static final Field MINUTES = null;
    public static final int MIN_TIMEZONE_OFFSET = 840;
    public static final Field MONTHS = null;
    public static final int NOVEMBER = 11;
    public static final int OCTOBER = 10;
    public static final Field SECONDS = null;
    public static final int SEPTEMBER = 9;
    public static final QName TIME = null;
    public static final Field YEARS = null;

    public static final class Field {
        private final int id;
        private final String str;

        private Field(String str, int id) {
            this.str = str;
            this.id = id;
        }

        public String toString() {
            return this.str;
        }

        public int getId() {
            return this.id;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.xml.datatype.DatatypeConstants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.xml.datatype.DatatypeConstants.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeConstants.<clinit>():void");
    }

    private DatatypeConstants() {
    }
}
