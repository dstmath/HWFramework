package dalvik.system;

import java.io.File;

public final class ZygoteHooks {
    private long token;

    private static native void nativePostForkChild(long j, int i, boolean z, String str);

    private static native long nativePreFork();

    public static native void startZygoteNoThreadCreation();

    public static native void stopZygoteNoThreadCreation();

    public void preFork() {
        Daemons.stop();
        waitUntilAllThreadsStopped();
        this.token = nativePreFork();
    }

    public void postForkChild(int debugFlags, boolean isSystemServer, String instructionSet) {
        nativePostForkChild(this.token, debugFlags, isSystemServer, instructionSet);
        Math.setRandomSeedInternal(System.currentTimeMillis());
    }

    public void postForkCommon() {
        Daemons.startPostZygoteFork();
    }

    private static void waitUntilAllThreadsStopped() {
        File tasks = new File("/proc/self/task");
        while (tasks.list().length > 1) {
            Thread.yield();
        }
    }
}
