package com.huawei.hsm.permission.minimms;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

class CharacterSets {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int ANY_CHARSET = 0;
    private static final int BIG5 = 2026;
    public static final int DEFAULT_CHARSET = 106;
    public static final String DEFAULT_CHARSET_NAME = "utf-8";
    private static final int ISO_8859_1 = 4;
    private static final int ISO_8859_2 = 5;
    private static final int ISO_8859_3 = 6;
    private static final int ISO_8859_4 = 7;
    private static final int ISO_8859_5 = 8;
    private static final int ISO_8859_6 = 9;
    private static final int ISO_8859_7 = 10;
    private static final int ISO_8859_8 = 11;
    private static final int ISO_8859_9 = 12;
    private static final int[] MIBENUM_NUMBERS = null;
    private static final HashMap<Integer, String> MIBENUM_TO_NAME_MAP = null;
    private static final String MIMENAME_ANY_CHARSET = "*";
    private static final String MIMENAME_BIG5 = "big5";
    private static final String MIMENAME_ISO_8859_1 = "iso-8859-1";
    private static final String MIMENAME_ISO_8859_2 = "iso-8859-2";
    private static final String MIMENAME_ISO_8859_3 = "iso-8859-3";
    private static final String MIMENAME_ISO_8859_4 = "iso-8859-4";
    private static final String MIMENAME_ISO_8859_5 = "iso-8859-5";
    private static final String MIMENAME_ISO_8859_6 = "iso-8859-6";
    private static final String MIMENAME_ISO_8859_7 = "iso-8859-7";
    private static final String MIMENAME_ISO_8859_8 = "iso-8859-8";
    private static final String MIMENAME_ISO_8859_9 = "iso-8859-9";
    private static final String MIMENAME_SHIFT_JIS = "shift_JIS";
    private static final String MIMENAME_UCS2 = "iso-10646-ucs-2";
    private static final String MIMENAME_US_ASCII = "us-ascii";
    private static final String MIMENAME_UTF_16 = "utf-16";
    private static final String MIMENAME_UTF_8 = "utf-8";
    private static final String[] MIME_NAMES = null;
    private static final HashMap<String, Integer> NAME_TO_MIBENUM_MAP = null;
    private static final int SHIFT_JIS = 17;
    private static final int UCS2 = 1000;
    private static final int US_ASCII = 3;
    private static final int UTF_16 = 1015;
    private static final int UTF_8 = 106;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hsm.permission.minimms.CharacterSets.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hsm.permission.minimms.CharacterSets.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hsm.permission.minimms.CharacterSets.<clinit>():void");
    }

    private CharacterSets() {
    }

    public static int getMibEnumValue(String mimeName) throws UnsupportedEncodingException {
        if (mimeName == null) {
            return -1;
        }
        Integer mibEnumValue = (Integer) NAME_TO_MIBENUM_MAP.get(mimeName);
        if (mibEnumValue != null) {
            return mibEnumValue.intValue();
        }
        throw new UnsupportedEncodingException();
    }
}
