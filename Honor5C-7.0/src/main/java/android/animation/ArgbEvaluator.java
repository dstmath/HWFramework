package android.animation;

import android.os.Process;

public class ArgbEvaluator implements TypeEvaluator {
    private static final ArgbEvaluator sInstance = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.ArgbEvaluator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.ArgbEvaluator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.animation.ArgbEvaluator.<clinit>():void");
    }

    public static ArgbEvaluator getInstance() {
        return sInstance;
    }

    public Object evaluate(float fraction, Object startValue, Object endValue) {
        int startInt = ((Integer) startValue).intValue();
        int startA = (startInt >> 24) & Process.PROC_TERM_MASK;
        int startR = (startInt >> 16) & Process.PROC_TERM_MASK;
        int startG = (startInt >> 8) & Process.PROC_TERM_MASK;
        int startB = startInt & Process.PROC_TERM_MASK;
        int endInt = ((Integer) endValue).intValue();
        return Integer.valueOf(((((((int) (((float) (((endInt >> 24) & Process.PROC_TERM_MASK) - startA)) * fraction)) + startA) << 24) | ((((int) (((float) (((endInt >> 16) & Process.PROC_TERM_MASK) - startR)) * fraction)) + startR) << 16)) | ((((int) (((float) (((endInt >> 8) & Process.PROC_TERM_MASK) - startG)) * fraction)) + startG) << 8)) | (((int) (((float) ((endInt & Process.PROC_TERM_MASK) - startB)) * fraction)) + startB));
    }
}
