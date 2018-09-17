package com.android.server.rms.handler;

import android.app.mtm.MultiTaskPolicy;
import android.content.Context;
import android.os.Bundle;
import android.rms.HwSysResource;
import android.util.Log;
import com.android.server.mtm.policy.MultiTaskPolicyMemoryCreator;
import com.android.server.rms.IScene;
import com.android.server.rms.IShrinker;
import com.android.server.rms.IStateChangedListener;
import com.android.server.rms.resource.HwSysInnerResImpl;
import com.android.server.rms.scene.DownloadScene;
import com.android.server.rms.scene.MediaScene;
import com.android.server.rms.scene.PhoneScene;
import com.android.server.rms.shrinker.ProcessShrinker;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import com.android.server.rms.shrinker.SystemShrinker;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.List;

public final class MemoryHandler implements HwSysResHandler, IStateChangedListener {
    private static boolean DEBUG = false;
    private static final String TAG = "RMS.MemoryHandler";
    private static MemoryHandler mMemoryHandler;
    private final IShrinker mProcShrinker;
    private final IShrinker mProcStopShrinker;
    private List<IScene> mScenes;
    private final IShrinker mSysShrinker;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.handler.MemoryHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.handler.MemoryHandler.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.handler.MemoryHandler.<clinit>():void");
    }

    public MemoryHandler(Context context) {
        this.mScenes = new ArrayList();
        this.mSysShrinker = new SystemShrinker();
        this.mProcShrinker = new ProcessShrinker(2);
        this.mProcStopShrinker = new ProcessStopShrinker();
        addScene(new MediaScene(context));
        addScene(new DownloadScene(context));
        addScene(new PhoneScene(context));
    }

    public static synchronized MemoryHandler getInstance(Context context) {
        MemoryHandler memoryHandler;
        synchronized (MemoryHandler.class) {
            if (mMemoryHandler == null) {
                mMemoryHandler = new MemoryHandler(context);
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            memoryHandler = mMemoryHandler;
        }
        return memoryHandler;
    }

    public boolean execute(MultiTaskPolicy policy) {
        if (DEBUG) {
            Log.d(TAG, "execute !!");
        }
        if (policy == null) {
            Log.e(TAG, "MultiTaskPolicy is null");
            return false;
        }
        if ((policy.getPolicy() & 32) != 0) {
            Log.d(TAG, "POLICY_ProcessKill!!");
            killProcess(1, policy.getPolicyData().getIntArray(MultiTaskPolicyMemoryCreator.POLICY_PROCESSFORCESTOP_PARAM));
            killProcess(1, policy.getPolicyData().getIntArray(MultiTaskPolicyMemoryCreator.POLICY_PROCESSKILL_PARAM));
        }
        if ((policy.getPolicy() & HwSecDiagnoseConstant.BIT_VERIFYBOOT) != 0) {
            Log.d(TAG, "MemoryShrink!!");
            memoryShrink();
        }
        if ((policy.getPolicy() & HwGlobalActionsData.FLAG_SILENTMODE_SILENT) != 0) {
            Log.d(TAG, "MemoryDropCache!!");
            memoryShrink();
        }
        if ((policy.getPolicy() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) != 0) {
            Log.d(TAG, "ProcessShrink!!");
            processShrink(policy.getPolicyData().getIntArray(MultiTaskPolicyMemoryCreator.POLICY_PROCESSHRINK_PARAM));
        }
        return true;
    }

    public void interrupt() {
        Log.d(TAG, "interrupt---");
        this.mProcStopShrinker.interrupt();
        this.mSysShrinker.interrupt();
        this.mProcShrinker.interrupt();
    }

    private void killProcess(int mode, int[] pids) {
        if (pids != null && pids.length > 0) {
            String reason = "POLICY_ProcessKill";
            Bundle extras = new Bundle();
            extras.putInt(ProcessStopShrinker.MODE_KEY, mode);
            extras.putIntArray(ProcessStopShrinker.PID_KEY, pids);
            this.mProcStopShrinker.reclaim("POLICY_ProcessKill", extras);
        }
    }

    private void memoryShrink() {
        String reason = "POLICY_memoryShrink";
        if (!isImportantScene()) {
            this.mSysShrinker.reclaim("POLICY_memoryShrink", null);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processShrink(int[] pids) {
        if (pids != null && pids.length > 0 && !isImportantScene()) {
            Bundle data = new Bundle();
            String reason = "POLICY_ProcessShrink";
            for (int i = 0; i < pids.length; i++) {
                if (DEBUG) {
                    Log.d(TAG, "shrink process: " + pids[i]);
                }
                data.putInt(ProcessStopShrinker.PID_KEY, pids[i]);
                this.mProcShrinker.reclaim("POLICY_ProcessShrink", data);
            }
        }
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    private void addScene(IScene scene) {
        this.mScenes.add(scene);
    }

    private boolean isImportantScene() {
        for (IScene scene : this.mScenes) {
            if (scene.identify(null)) {
                return true;
            }
        }
        return false;
    }

    public void onTrigger() {
        HwSysResource sysResource = HwSysInnerResImpl.getResource(20);
        if (DEBUG) {
            Log.d(TAG, "onTrigger MEMORY");
        }
        if (sysResource != null) {
            sysResource.query();
            sysResource.acquire(null, null, null);
        }
    }

    public void onInterrupt() {
        if (DEBUG) {
            Log.d(TAG, "onInterrupt MEMORY");
        }
        interrupt();
    }
}
