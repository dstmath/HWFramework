package com.android.internal.telephony.jni;

import com.android.internal.telephony.ServiceStateTracker;

public class OperateTA {
    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.jni.OperateTA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.jni.OperateTA.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.jni.OperateTA.<clinit>():void");
    }

    public native int operTA(int i, int i2, int i3, String str);

    public int writeToTA(int cardType, int apnType, String Challenge) {
        operTA(1, cardType, apnType, Challenge);
        return operTA(2, cardType, apnType, Challenge);
    }

    public int writeApnToTA(int cardType, int apnType, String Challenge) {
        return operTA(2, cardType, apnType, Challenge);
    }

    public int clearToTA() {
        return operTA(3, 0, 0, ServiceStateTracker.INVALID_MCC);
    }
}
