package huawei.android.hwanimation;

import android.common.HwAnimationManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class HwAnimationManagerImpl implements HwAnimationManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "AnimationUtils";
    private static HwAnimationManager mHwAnimationManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.hwanimation.HwAnimationManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.hwanimation.HwAnimationManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwanimation.HwAnimationManagerImpl.<clinit>():void");
    }

    public static synchronized HwAnimationManager getDefault() {
        HwAnimationManager hwAnimationManager;
        synchronized (HwAnimationManagerImpl.class) {
            if (mHwAnimationManager == null) {
                mHwAnimationManager = new HwAnimationManagerImpl();
            }
            hwAnimationManager = mHwAnimationManager;
        }
        return hwAnimationManager;
    }

    public Animation loadEnterAnimation(Context context, int delta) {
        Context hwextContext = null;
        try {
            hwextContext = context.createPackageContext("androidhwext", 0);
        } catch (NameNotFoundException e) {
        }
        if (hwextContext == null) {
            return null;
        }
        int rotateEnterAnimationId = hwextContext.getResources().getIdentifier("screen_rotate_" + delta + "_enter", "anim", "androidhwext");
        if (rotateEnterAnimationId != 0) {
            return AnimationUtils.loadAnimation(hwextContext, rotateEnterAnimationId);
        }
        return null;
    }

    public Animation loadExitAnimation(Context context, int delta) {
        Context hwextContext = null;
        try {
            hwextContext = context.createPackageContext("androidhwext", 0);
        } catch (NameNotFoundException e) {
        }
        if (hwextContext == null) {
            return null;
        }
        int rotateExitAnimationId = hwextContext.getResources().getIdentifier("screen_rotate_" + delta + "_exit", "anim", "androidhwext");
        if (rotateExitAnimationId != 0) {
            return AnimationUtils.loadAnimation(hwextContext, rotateExitAnimationId);
        }
        return null;
    }
}
