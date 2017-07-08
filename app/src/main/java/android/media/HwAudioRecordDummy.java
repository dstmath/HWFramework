package android.media;

import android.util.Log;

public class HwAudioRecordDummy implements IHwAudioRecord {
    private static final String TAG = "HwAudioRecordDummy";
    private static IHwAudioRecord mHwAudioRecoder;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.HwAudioRecordDummy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.HwAudioRecordDummy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.HwAudioRecordDummy.<clinit>():void");
    }

    private HwAudioRecordDummy() {
    }

    public static IHwAudioRecord getDefault() {
        return mHwAudioRecoder;
    }

    public void sendStateChangedIntent(int state) {
        Log.w(TAG, "dummy sendStateChangedIntent, state=" + state);
    }

    public boolean isAudioRecordAllowed() {
        Log.w(TAG, "dummy isAudioRecordAllowed ");
        return true;
    }

    public void checkRecordActive() {
        Log.w(TAG, "dummy checkRecordActive");
    }
}
