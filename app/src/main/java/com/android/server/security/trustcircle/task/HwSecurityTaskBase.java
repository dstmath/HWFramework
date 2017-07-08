package com.android.server.security.trustcircle.task;

import com.android.server.security.trustcircle.utils.LogHelper;

public abstract class HwSecurityTaskBase {
    public static final int E_CANCEL = 2;
    public static final int E_CONTINUE = -1;
    public static final int E_OK = 0;
    public static final int E_TIMEOUT = 1;
    public static final int E_UNEXPECTATION = 3;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_STARTED = 1;
    public static final int STATUS_UNSTART = 0;
    private static final String TAG = null;
    private RetCallback mCallback;
    private HwSecurityTaskBase mParent;
    protected int mStatus;

    public interface EventListener {
        boolean onEvent(HwSecurityEvent hwSecurityEvent);
    }

    public interface RetCallback {
        void onTaskCallback(HwSecurityTaskBase hwSecurityTaskBase, int i);
    }

    public interface TimerOutProc {
        void onTimerOut();
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.task.HwSecurityTaskBase.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.task.HwSecurityTaskBase.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.task.HwSecurityTaskBase.<clinit>():void");
    }

    public abstract int doAction();

    public HwSecurityTaskBase(HwSecurityTaskBase parent, RetCallback callback) {
        this.mParent = parent;
        this.mCallback = callback;
        this.mStatus = E_OK;
        onStart();
    }

    public HwSecurityTaskBase getParent() {
        return this.mParent;
    }

    public void execute() {
        LogHelper.i(TAG, "execute task: " + getClass().getSimpleName());
        if (this.mStatus == 0) {
            this.mStatus = STATUS_STARTED;
            int ret = doAction();
            if (ret != E_CONTINUE) {
                endWithResult(ret);
            }
        }
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public int getTaskStatus() {
        return this.mStatus;
    }

    protected void endWithResult(int ret) {
        LogHelper.i(TAG, "endWithResult, task: " + getClass().getSimpleName() + ", status: " + this.mStatus);
        if (this.mStatus != STATUS_FINISHED) {
            onStop();
            this.mStatus = STATUS_FINISHED;
            if (this.mCallback != null) {
                this.mCallback.onTaskCallback(this, ret);
            }
        }
    }
}
