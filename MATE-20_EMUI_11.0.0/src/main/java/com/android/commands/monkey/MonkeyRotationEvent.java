package com.android.commands.monkey;

import android.app.IActivityManager;
import android.os.RemoteException;
import android.view.IWindowManager;

public class MonkeyRotationEvent extends MonkeyEvent {
    private final boolean mPersist;
    private final int mRotationDegree;

    public MonkeyRotationEvent(int degree, boolean persist) {
        super(3);
        this.mRotationDegree = degree;
        this.mPersist = persist;
    }

    @Override // com.android.commands.monkey.MonkeyEvent
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 0) {
            Logger logger = Logger.out;
            logger.println(":Sending rotation degree=" + this.mRotationDegree + ", persist=" + this.mPersist);
        }
        try {
            iwm.freezeRotation(this.mRotationDegree);
            if (this.mPersist) {
                return 1;
            }
            iwm.thawRotation();
            return 1;
        } catch (RemoteException e) {
            return -1;
        }
    }
}
