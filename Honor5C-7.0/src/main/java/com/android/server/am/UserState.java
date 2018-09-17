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
    private static final String TAG = null;
    public int lastState;
    public final UserHandle mHandle;
    public final ArrayMap<String, Long> mProviderLastReportedFg;
    public final ArrayList<IStopUserCallback> mStopCallbacks;
    public final ProgressReporter mUnlockProgress;
    public int state;
    public boolean switching;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.UserState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.UserState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.UserState.<clinit>():void");
    }

    public UserState(UserHandle handle) {
        this.mStopCallbacks = new ArrayList();
        this.state = STATE_BOOTING;
        this.lastState = STATE_BOOTING;
        this.mProviderLastReportedFg = new ArrayMap();
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
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i(TAG, "User " + this.mHandle.getIdentifier() + " state changed from " + stateToString(this.state) + " to " + stateToString(newState));
        }
        this.lastState = this.state;
        this.state = newState;
    }

    private static String stateToString(int state) {
        switch (state) {
            case STATE_BOOTING /*0*/:
                return "BOOTING";
            case STATE_RUNNING_LOCKED /*1*/:
                return "RUNNING_LOCKED";
            case STATE_RUNNING_UNLOCKING /*2*/:
                return "RUNNING_UNLOCKING";
            case STATE_RUNNING_UNLOCKED /*3*/:
                return "RUNNING_UNLOCKED";
            case STATE_STOPPING /*4*/:
                return "STOPPING";
            case STATE_SHUTDOWN /*5*/:
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
