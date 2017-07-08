package com.huawei.zxing;

public final class Contents {
    public static final String DATA = "ENCODE_DATA";
    public static final String[] EMAIL_KEYS = null;
    public static final String[] EMAIL_TYPE_KEYS = null;
    public static final String NOTE_KEY = "NOTE_KEY";
    public static final String[] PHONE_KEYS = null;
    public static final String[] PHONE_TYPE_KEYS = null;
    public static final String URL_KEY = "URL_KEY";

    public static final class Type {
        public static final String CONTACT = "CONTACT_TYPE";
        public static final String EMAIL = "EMAIL_TYPE";
        public static final String LOCATION = "LOCATION_TYPE";
        public static final String PHONE = "PHONE_TYPE";
        public static final String SMS = "SMS_TYPE";
        public static final String TEXT = "TEXT_TYPE";

        private Type() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.Contents.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.Contents.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.Contents.<clinit>():void");
    }

    private Contents() {
    }
}
