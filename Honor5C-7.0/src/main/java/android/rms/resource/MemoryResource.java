package android.rms.resource;

import android.os.Bundle;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.util.Log;

public final class MemoryResource extends HwSysResImpl {
    public static final String APP_MEMSIZE = "MemorySize";
    public static final String APP_UID = "Uid";
    private static final boolean DEBUG = false;
    private static final String TAG = "RMS.MemoryResource";
    private static MemoryResource mMemoryResource;
    private HwSysResManager mResourceManger;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.resource.MemoryResource.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.resource.MemoryResource.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.resource.MemoryResource.<clinit>():void");
    }

    public MemoryResource() {
        this.mResourceManger = HwSysResManager.getInstance();
    }

    public static synchronized MemoryResource getInstance() {
        MemoryResource memoryResource;
        synchronized (MemoryResource.class) {
            if (mMemoryResource == null) {
                mMemoryResource = new MemoryResource();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new MemoryResource");
                }
            }
            memoryResource = mMemoryResource;
        }
        return memoryResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        Bundle bd = new Bundle();
        long size = ((long) count) * 1024;
        bd.putInt(APP_UID, callingUid);
        bd.putLong(APP_MEMSIZE, size);
        return this.mResourceManger.acquireSysRes(20, null, null, bd);
    }
}
