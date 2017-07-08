package com.huawei.zxing.common;

import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.zxing.FormatException;
import java.util.Map;

public enum CharacterSetECI {
    ;
    
    private static final Map<String, CharacterSetECI> NAME_TO_ECI = null;
    private static final Map<Integer, CharacterSetECI> VALUE_TO_ECI = null;
    private final String[] otherEncodingNames;
    private final int[] values;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.common.CharacterSetECI.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.common.CharacterSetECI.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.common.CharacterSetECI.<clinit>():void");
    }

    private CharacterSetECI(int value) {
        this(r3, r4, new int[]{value}, new String[0]);
    }

    private CharacterSetECI(int value, String... otherEncodingNames) {
        this.values = new int[]{value};
        this.otherEncodingNames = otherEncodingNames;
    }

    private CharacterSetECI(int[] values, String... otherEncodingNames) {
        this.values = values;
        this.otherEncodingNames = otherEncodingNames;
    }

    public int getValue() {
        return this.values[0];
    }

    public static CharacterSetECI getCharacterSetECIByValue(int value) throws FormatException {
        if (value >= 0 && value < MotionTypeApps.TYPE_ACTIVITY) {
            return (CharacterSetECI) VALUE_TO_ECI.get(Integer.valueOf(value));
        }
        throw FormatException.getFormatInstance();
    }

    public static CharacterSetECI getCharacterSetECIByName(String name) {
        return (CharacterSetECI) NAME_TO_ECI.get(name);
    }
}
