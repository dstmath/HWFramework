package com.android.server.policy;

import android.os.Handler;

public interface IHWExtMotionRotationProcessor {

    public interface WindowOrientationListenerProxy {
        void notifyProposedRotation(int i);

        void setCurrentOrientation(int i);
    }

    void disableMotionRotation();

    void enableMotionRotation(Handler handler);

    int getProposedRotation();
}
