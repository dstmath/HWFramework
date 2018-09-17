package android.hsm;

import android.security.keymaster.KeymasterDefs;

public class HwAudioPermWrapper {
    private static final String TAG = null;
    private boolean isAudioBlocked;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hsm.HwAudioPermWrapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hsm.HwAudioPermWrapper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hsm.HwAudioPermWrapper.<clinit>():void");
    }

    public HwAudioPermWrapper() {
        this.isAudioBlocked = false;
    }

    public void confirmPermission() {
        this.isAudioBlocked = !HwSystemManager.allowOp(KeymasterDefs.KM_ALGORITHM_HMAC);
    }

    public boolean confirmPermissionWithResult() {
        this.isAudioBlocked = !HwSystemManager.allowOp(KeymasterDefs.KM_ALGORITHM_HMAC);
        return this.isAudioBlocked;
    }

    public boolean isBlocked() {
        return this.isAudioBlocked;
    }

    public int mockRead(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        for (int count = offsetInBytes; count < offsetInBytes + sizeInBytes; count++) {
            audioData[count] = (byte) 0;
        }
        return 0;
    }

    public int mockRead(short[] audioData, int offsetInShorts, int sizeInShorts) {
        for (int count = offsetInShorts; count < offsetInShorts + sizeInShorts; count++) {
            audioData[count] = (short) 0;
        }
        return 0;
    }
}
