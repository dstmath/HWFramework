package com.android.server.am;

import android.app.IStopUserCallback;
import android.os.Trace;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
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
    public final ArrayList<KeyEvictedCallback> mKeyEvictedCallbacks = new ArrayList<>();
    final ArrayMap<String, Long> mProviderLastReportedFg = new ArrayMap<>();
    public final ArrayList<IStopUserCallback> mStopCallbacks = new ArrayList<>();
    public final ProgressReporter mUnlockProgress;
    public int state = 0;
    public boolean switching;
    public boolean tokenProvided;

    public interface KeyEvictedCallback {
        void keyEvicted(int i);
    }

    public UserState(UserHandle handle) {
        this.mHandle = handle;
        this.mUnlockProgress = new ProgressReporter(handle.getIdentifier());
    }

    public boolean setState(int oldState, int newState) {
        if (this.state == oldState) {
            setState(newState);
            return true;
        }
        Slog.w("ActivityManager", "Expected user " + this.mHandle.getIdentifier() + " in state " + stateToString(oldState) + " but was in state " + stateToString(this.state));
        return false;
    }

    public void setState(int newState) {
        if (newState != this.state) {
            int userId = this.mHandle.getIdentifier();
            if (this.state != 0) {
                Trace.asyncTraceEnd(64, stateToString(this.state) + " " + userId, userId);
            }
            if (newState != 5) {
                Trace.asyncTraceBegin(64, stateToString(newState) + " " + userId, userId);
            }
            Slog.i("ActivityManager", "User " + userId + " state changed from " + stateToString(this.state) + " to " + stateToString(newState));
            EventLogTags.writeAmUserStateChanged(userId, newState);
            this.lastState = this.state;
            this.state = newState;
        }
    }

    public static String stateToString(int state2) {
        if (state2 == 0) {
            return "BOOTING";
        }
        if (state2 == 1) {
            return "RUNNING_LOCKED";
        }
        if (state2 == 2) {
            return "RUNNING_UNLOCKING";
        }
        if (state2 == 3) {
            return "RUNNING_UNLOCKED";
        }
        if (state2 == 4) {
            return "STOPPING";
        }
        if (state2 != 5) {
            return Integer.toString(state2);
        }
        return "SHUTDOWN";
    }

    public static int stateToProtoEnum(int state2) {
        if (state2 == 0) {
            return 0;
        }
        if (state2 == 1) {
            return 1;
        }
        if (state2 == 2) {
            return 2;
        }
        if (state2 == 3) {
            return 3;
        }
        if (state2 == 4) {
            return 4;
        }
        if (state2 != 5) {
            return state2;
        }
        return 5;
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("state=");
        pw.print(stateToString(this.state));
        if (this.switching) {
            pw.print(" SWITCHING");
        }
        pw.println();
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1159641169921L, stateToProtoEnum(this.state));
        proto.write(1133871366146L, this.switching);
        proto.end(token);
    }
}
