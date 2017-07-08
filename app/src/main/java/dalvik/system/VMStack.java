package dalvik.system;

public final class VMStack {
    public static native int fillStackTraceElements(Thread thread, StackTraceElement[] stackTraceElementArr);

    public static native ClassLoader getCallingClassLoader();

    public static native ClassLoader getClosestUserClassLoader();

    public static native Class<?> getStackClass2();

    public static native StackTraceElement[] getThreadStackTrace(Thread thread);

    public static Class<?> getStackClass1() {
        return getStackClass2();
    }
}
