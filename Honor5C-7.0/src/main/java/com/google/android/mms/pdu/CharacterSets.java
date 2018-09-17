package com.google.android.mms.pdu;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class CharacterSets {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int ANY_CHARSET = 0;
    public static final int BIG5 = 2026;
    public static final int DEFAULT_CHARSET = 106;
    public static final String DEFAULT_CHARSET_NAME = "utf-8";
    public static final int ISO_8859_1 = 4;
    public static final int ISO_8859_2 = 5;
    public static final int ISO_8859_3 = 6;
    public static final int ISO_8859_4 = 7;
    public static final int ISO_8859_5 = 8;
    public static final int ISO_8859_6 = 9;
    public static final int ISO_8859_7 = 10;
    public static final int ISO_8859_8 = 11;
    public static final int ISO_8859_9 = 12;
    private static final int[] MIBENUM_NUMBERS = null;
    private static final HashMap<Integer, String> MIBENUM_TO_NAME_MAP = null;
    public static final String MIMENAME_ANY_CHARSET = "*";
    public static final String MIMENAME_BIG5 = "big5";
    public static final String MIMENAME_ISO_8859_1 = "iso-8859-1";
    public static final String MIMENAME_ISO_8859_2 = "iso-8859-2";
    public static final String MIMENAME_ISO_8859_3 = "iso-8859-3";
    public static final String MIMENAME_ISO_8859_4 = "iso-8859-4";
    public static final String MIMENAME_ISO_8859_5 = "iso-8859-5";
    public static final String MIMENAME_ISO_8859_6 = "iso-8859-6";
    public static final String MIMENAME_ISO_8859_7 = "iso-8859-7";
    public static final String MIMENAME_ISO_8859_8 = "iso-8859-8";
    public static final String MIMENAME_ISO_8859_9 = "iso-8859-9";
    public static final String MIMENAME_SHIFT_JIS = "shift_JIS";
    public static final String MIMENAME_UCS2 = "iso-10646-ucs-2";
    public static final String MIMENAME_US_ASCII = "us-ascii";
    public static final String MIMENAME_UTF_16 = "utf-16";
    public static final String MIMENAME_UTF_8 = "utf-8";
    private static final String[] MIME_NAMES = null;
    private static final HashMap<String, Integer> NAME_TO_MIBENUM_MAP = null;
    public static final int SHIFT_JIS = 17;
    public static final int UCS2 = 1000;
    public static final int US_ASCII = 3;
    public static final int UTF_16 = 1015;
    public static final int UTF_8 = 106;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.mms.pdu.CharacterSets.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.mms.pdu.CharacterSets.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.mms.pdu.CharacterSets.<clinit>():void");
    }

    private CharacterSets() {
    }

    public static String getMimeName(int mibEnumValue) throws UnsupportedEncodingException {
        String name = (String) MIBENUM_TO_NAME_MAP.get(Integer.valueOf(mibEnumValue));
        if (name != null) {
            return name;
        }
        throw new UnsupportedEncodingException();
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
