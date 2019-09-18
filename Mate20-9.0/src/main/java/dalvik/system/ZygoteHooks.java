package dalvik.system;

import java.io.File;

public final class ZygoteHooks {
    private long token;

    private static native void nativePostForkChild(long j, int i, boolean z, boolean z2, String str);

    private static native void nativePostForkCommon();

    private static native long nativePreFork();

    public static native void startZygoteNoThreadCreation();

    public static native void stopZygoteNoThreadCreation();

    public void preFork() {
        if (System.getenv("MAPLE_RUNTIME") == null) {
            Daemons.stop();
            waitUntilAllThreadsStopped();
        }
        this.token = nativePreFork();
    }

    public void postForkChild(int runtimeFlags, boolean isSystemServer, boolean isZygote, String instructionSet) {
        nativePostForkChild(this.token, runtimeFlags, isSystemServer, isZygote, instructionSet);
        Math.setRandomSeedInternal(System.currentTimeMillis());
    }

    public void postForkCommon() {
        if (System.getenv("MAPLE_RUNTIME") == null) {
            Daemons.startPostZygoteFork();
        } else {
            nativePostForkCommon();
        }
    }

    private static void waitUntilAllThreadsStopped() {
        File tasks = new File("/proc/self/task");
        while (tasks.list().length > 1) {
            Thread.yield();
        }
    }
}
