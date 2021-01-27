package com.huawei.server.fsm;

import com.huawei.internal.util.StateMachineEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DefaultPostureStateMachine extends StateMachineEx {
    public static final String TAG_FSM_STATE_MACHINE = "Fsm_PostureStateMachine";

    protected DefaultPostureStateMachine(String name) {
        super(name);
    }

    public static synchronized DefaultPostureStateMachine getInstance() {
        DefaultPostureStateMachine defaultPostureStateMachine;
        synchronized (DefaultPostureStateMachine.class) {
            defaultPostureStateMachine = new DefaultPostureStateMachine(TAG_FSM_STATE_MACHINE);
        }
        return defaultPostureStateMachine;
    }

    public int getFoldStateChangeCount() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        DefaultPostureStateMachine.super.dump(fd, pw, args);
    }
}
