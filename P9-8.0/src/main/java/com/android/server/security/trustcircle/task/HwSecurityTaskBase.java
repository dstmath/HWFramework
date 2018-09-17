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
    private static final String TAG = HwSecurityTaskBase.class.getSimpleName();
    private RetCallback mCallback;
    private HwSecurityTaskBase mParent;
    protected int mStatus = 0;

    public interface EventListener {
        boolean onEvent(HwSecurityEvent hwSecurityEvent);
    }

    public interface RetCallback {
        void onTaskCallback(HwSecurityTaskBase hwSecurityTaskBase, int i);
    }

    public interface TimerOutProc {
        void onTimerOut();
    }

    public abstract int doAction();

    public HwSecurityTaskBase(HwSecurityTaskBase parent, RetCallback callback) {
        this.mParent = parent;
        this.mCallback = callback;
        onStart();
    }

    public HwSecurityTaskBase getParent() {
        return this.mParent;
    }

    public void execute() {
        LogHelper.i(TAG, "execute task: " + getClass().getSimpleName());
        if (this.mStatus == 0) {
            this.mStatus = 1;
            int ret = doAction();
            if (ret != -1) {
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
        if (this.mStatus != 2) {
            onStop();
            this.mStatus = 2;
            if (this.mCallback != null) {
                this.mCallback.onTaskCallback(this, ret);
            }
        }
    }
}
