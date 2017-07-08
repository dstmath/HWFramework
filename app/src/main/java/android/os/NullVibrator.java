package android.os;

import android.media.AudioAttributes;

public class NullVibrator extends Vibrator {
    private static final NullVibrator sInstance = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.NullVibrator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.NullVibrator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.NullVibrator.<clinit>():void");
    }

    private NullVibrator() {
    }

    public static NullVibrator getInstance() {
        return sInstance;
    }

    public boolean hasVibrator() {
        return false;
    }

    public void vibrate(int uid, String opPkg, long milliseconds, AudioAttributes attributes) {
    }

    public void vibrate(int uid, String opPkg, long[] pattern, int repeat, AudioAttributes attributes) {
        if (repeat >= pattern.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void cancel() {
    }
}
