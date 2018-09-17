package com.android.server.wifi;

import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import com.android.internal.util.StateMachine;

public class StateMachineDeathRecipient implements DeathRecipient {
    private final int mDeathCommand;
    private IBinder mLinkedBinder;
    private final StateMachine mStateMachine;

    public StateMachineDeathRecipient(StateMachine sm, int command) {
        this.mStateMachine = sm;
        this.mDeathCommand = command;
    }

    public boolean linkToDeath(IBinder binder) {
        unlinkToDeath();
        try {
            binder.linkToDeath(this, 0);
            this.mLinkedBinder = binder;
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void unlinkToDeath() {
        if (this.mLinkedBinder != null) {
            this.mLinkedBinder.unlinkToDeath(this, 0);
            this.mLinkedBinder = null;
        }
    }

    public void binderDied() {
        this.mStateMachine.sendMessage(this.mDeathCommand);
    }
}
