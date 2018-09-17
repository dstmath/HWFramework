package android.vr;

import android.content.Context;
import android.util.Log;
import android.vr.IVrServiceManager.Stub;

public class VrServiceManager extends Stub {
    private static String TAG;
    private Context mContext;
    private final IVRManagerService mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.vr.VrServiceManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.vr.VrServiceManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.vr.VrServiceManager.<clinit>():void");
    }

    public VrServiceManager(IVRManagerService service, Context context) {
        Log.d(TAG, "VrServiceManager constructer");
        this.mService = service;
        this.mContext = context;
    }

    public double getVsync() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        try {
            return this.mService.getVsync();
        } catch (Exception e) {
            Log.e(TAG, "call service error");
            return -1.0d;
        }
    }

    public boolean startFrontBufferDisplay() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        try {
            return this.mService.startFrontBufferDisplay();
        } catch (Exception e) {
            Log.e(TAG, "call service error");
            return false;
        }
    }

    public boolean stopFrontBufferDisplay() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        try {
            return this.mService.stopFrontBufferDisplay();
        } catch (Exception e) {
            Log.e(TAG, "call service error");
            return false;
        }
    }

    public int setSchedFifo(int tid, int rtPriority) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        try {
            return this.mService.setSchedFifo(tid, rtPriority);
        } catch (Exception e) {
            Log.e(TAG, "call service error");
            return -1;
        }
    }
}
