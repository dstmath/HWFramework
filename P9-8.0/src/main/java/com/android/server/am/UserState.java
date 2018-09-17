package com.android.server.am;

import android.app.IStopUserCallback;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.util.ProgressReporter;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class UserState {
    public static final int STATE_BOOTING = 0;
    public static final int STATE_RUNNING_LOCKED = 1;
    public static final int STATE_RUNNING_UNLOCKED = 3;
    public static final int STATE_RUNNING_UNLOCKING = 2;
    public static final int STATE_SHUTDOWN = 5;
    public static final int STATE_STOPPING = 4;
    private static final String TAG = "ActivityManager";
    public int lastState = 0;
    public final UserHandle mHandle;
    public final ArrayMap<String, Long> mProviderLastReportedFg = new ArrayMap();
    public final ArrayList<IStopUserCallback> mStopCallbacks = new ArrayList();
    public final ProgressReporter mUnlockProgress;
    public int state = 0;
    public boolean switching;
    public boolean tokenProvided;

    public UserState(UserHandle handle) {
        this.mHandle = handle;
        this.mUnlockProgress = new ProgressReporter(handle.getIdentifier());
    }

    public boolean setState(int oldState, int newState) {
        if (this.state == oldState) {
            setState(newState);
            return true;
        }
        Slog.w(TAG, "Expected user " + this.mHandle.getIdentifier() + " in state " + stateToString(oldState) + " but was in state " + stateToString(this.state));
        return false;
    }

    public void setState(int newState) {
        if (newState != this.state) {
            Slog.i(TAG, "User " + this.mHandle.getIdentifier() + " state changed from " + stateToString(this.state) + " to " + stateToString(newState));
            EventLogTags.writeAmUserStateChanged(this.mHandle.getIdentifier(), newState);
            this.lastState = this.state;
            this.state = newState;
        }
    }

    public static String stateToString(int state) {
        switch (state) {
            case 0:
                return "BOOTING";
            case 1:
                return "RUNNING_LOCKED";
            case 2:
                return "RUNNING_UNLOCKING";
            case 3:
                return "RUNNING_UNLOCKED";
            case 4:
                return "STOPPING";
            case 5:
                return "SHUTDOWN";
            default:
                return Integer.toString(state);
        }
    }

    void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("state=");
        pw.print(stateToString(this.state));
        if (this.switching) {
            pw.print(" SWITCHING");
        }
        pw.println();
    }
}
