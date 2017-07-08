package com.huawei.android.os;

public class DebugCustEx {
    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.os.DebugCustEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.os.DebugCustEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.os.DebugCustEx.<clinit>():void");
    }

    private static final native String native_getAptempInfo();

    private static final native String native_getCpuInfo();

    private static final native String native_getCurrentInfo();

    private static final native String native_getDdrInfo();

    private static final native String native_getGpuInfo();

    private static final native float native_getSurfaceFlingerFrameRate();

    public static final float getSurfaceFlingerFrameRate() {
        return native_getSurfaceFlingerFrameRate();
    }

    public static final String getCpuInfo() {
        return native_getCpuInfo();
    }

    public static final String getGpuInfo() {
        return native_getGpuInfo();
    }

    public static final String getDdrInfo() {
        return native_getDdrInfo();
    }

    public static final String getCurrentInfo() {
        return native_getCurrentInfo();
    }

    public static final String getAptempInfo() {
        return native_getAptempInfo();
    }
}
