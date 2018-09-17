package java.lang;

import android.system.OsConstants;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.VMDebug;
import dalvik.system.VMRuntime;
import dalvik.system.VMStack;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import libcore.util.EmptyArray;
import sun.reflect.CallerSensitive;

public class Runtime {
    private static Runtime currentRuntime = new Runtime();
    private static boolean finalizeOnExit;
    private volatile String[] mLibPaths = null;
    private List<Thread> shutdownHooks = new ArrayList();
    private boolean shuttingDown;
    private boolean tracingMethods;

    private static native void nativeExit(int i);

    private static native String nativeLoad(String str, ClassLoader classLoader, String str2);

    private static native void runFinalization0();

    public native long freeMemory();

    public native void gc();

    public native long maxMemory();

    public native long totalMemory();

    public static Runtime getRuntime() {
        return currentRuntime;
    }

    private Runtime() {
    }

    /* JADX WARNING: Unexpected end of synchronized block */
    /* JADX WARNING: Missing block: B:8:?, code:
            r2 = new java.lang.Thread[r6.shutdownHooks.size()];
            r6.shutdownHooks.toArray(r2);
     */
    /* JADX WARNING: Missing block: B:11:0x001a, code:
            r5 = r2.length;
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:12:0x001c, code:
            if (r4 >= r5) goto L_0x002c;
     */
    /* JADX WARNING: Missing block: B:13:0x001e, code:
            r2[r4].start();
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            r4 = r2.length;
     */
    /* JADX WARNING: Missing block: B:22:0x002d, code:
            if (r3 >= r4) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            r2[r3].join();
     */
    /* JADX WARNING: Missing block: B:30:0x003b, code:
            if (finalizeOnExit == false) goto L_0x0040;
     */
    /* JADX WARNING: Missing block: B:31:0x003d, code:
            runFinalization();
     */
    /* JADX WARNING: Missing block: B:32:0x0040, code:
            nativeExit(r7);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void exit(int status) {
        int i = 0;
        synchronized (this) {
            if (!this.shuttingDown) {
                this.shuttingDown = true;
                synchronized (this.shutdownHooks) {
                }
            }
        }
        return;
        i++;
    }

    public void addShutdownHook(Thread hook) {
        if (hook == null) {
            throw new NullPointerException("hook == null");
        } else if (this.shuttingDown) {
            throw new IllegalStateException("VM already shutting down");
        } else if (hook.started) {
            throw new IllegalArgumentException("Hook has already been started");
        } else {
            synchronized (this.shutdownHooks) {
                if (this.shutdownHooks.contains(hook)) {
                    throw new IllegalArgumentException("Hook already registered.");
                }
                this.shutdownHooks.add(hook);
            }
        }
    }

    public boolean removeShutdownHook(Thread hook) {
        if (hook == null) {
            throw new NullPointerException("hook == null");
        } else if (this.shuttingDown) {
            throw new IllegalStateException("VM already shutting down");
        } else {
            boolean remove;
            synchronized (this.shutdownHooks) {
                remove = this.shutdownHooks.remove((Object) hook);
            }
            return remove;
        }
    }

    public void halt(int status) {
        nativeExit(status);
    }

    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        finalizeOnExit = value;
    }

    public Process exec(String command) throws IOException {
        return exec(command, null, null);
    }

    public Process exec(String command, String[] envp) throws IOException {
        return exec(command, envp, null);
    }

    public Process exec(String command, String[] envp, File dir) throws IOException {
        if (command.length() == 0) {
            throw new IllegalArgumentException("Empty command");
        }
        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            cmdarray[i] = st.nextToken();
            i++;
        }
        return exec(cmdarray, envp, dir);
    }

    public Process exec(String[] cmdarray) throws IOException {
        return exec(cmdarray, null, null);
    }

    public Process exec(String[] cmdarray, String[] envp) throws IOException {
        return exec(cmdarray, envp, null);
    }

    public Process exec(String[] cmdarray, String[] envp, File dir) throws IOException {
        return new ProcessBuilder(cmdarray).environment(envp).directory(dir).start();
    }

    public int availableProcessors() {
        return (int) Libcore.os.sysconf(OsConstants._SC_NPROCESSORS_CONF);
    }

    public void runFinalization() {
        VMRuntime.runFinalization(0);
    }

    public void traceInstructions(boolean on) {
    }

    public void traceMethodCalls(boolean on) {
        if (on != this.tracingMethods) {
            if (on) {
                VMDebug.startMethodTracing();
            } else {
                VMDebug.stopMethodTracing();
            }
            this.tracingMethods = on;
        }
    }

    @CallerSensitive
    public void load(String filename) {
        load0(VMStack.getStackClass1(), filename);
    }

    private void checkTargetSdkVersionForLoad(String methodName) {
        int targetSdkVersion = VMRuntime.getRuntime().getTargetSdkVersion();
        if (targetSdkVersion > 24) {
            throw new UnsupportedOperationException(methodName + " is not supported on SDK " + targetSdkVersion);
        }
    }

    void load(String absolutePath, ClassLoader loader) {
        checkTargetSdkVersionForLoad("java.lang.Runtime#load(String, ClassLoader)");
        System.logE("java.lang.Runtime#load(String, ClassLoader) is private and will be removed in a future Android release");
        if (absolutePath == null) {
            throw new NullPointerException("absolutePath == null");
        }
        String error = doLoad(absolutePath, loader);
        if (error != null) {
            throw new UnsatisfiedLinkError(error);
        }
    }

    synchronized void load0(Class<?> fromClass, String filename) {
        if (!new File(filename).isAbsolute()) {
            throw new UnsatisfiedLinkError("Expecting an absolute path of the library: " + filename);
        } else if (filename == null) {
            throw new NullPointerException("filename == null");
        } else {
            String error = doLoad(filename, fromClass.getClassLoader());
            if (error != null) {
                throw new UnsatisfiedLinkError(error);
            }
        }
    }

    @CallerSensitive
    public void loadLibrary(String libname) {
        loadLibrary0(VMStack.getCallingClassLoader(), libname);
    }

    public void loadLibrary(String libname, ClassLoader classLoader) {
        checkTargetSdkVersionForLoad("java.lang.Runtime#loadLibrary(String, ClassLoader)");
        System.logE("java.lang.Runtime#loadLibrary(String, ClassLoader) is private and will be removed in a future Android release");
        loadLibrary0(classLoader, libname);
    }

    synchronized void loadLibrary0(ClassLoader loader, String libname) {
        if (libname.indexOf(File.separatorChar) != -1) {
            throw new UnsatisfiedLinkError("Directory separator should not appear in library name: " + libname);
        }
        String libraryName = libname;
        String filename;
        String error;
        if (loader != null) {
            filename = loader.findLibrary(libname);
            if (filename == null) {
                throw new UnsatisfiedLinkError(loader + " couldn't find \"" + System.mapLibraryName(libname) + "\"");
            }
            error = doLoad(filename, loader);
            if (error != null) {
                throw new UnsatisfiedLinkError(error);
            }
            return;
        }
        filename = System.mapLibraryName(libname);
        Object candidates = new ArrayList();
        String lastError = null;
        for (String directory : getLibPaths()) {
            String candidate = directory + filename;
            candidates.add(candidate);
            if (IoUtils.canOpenReadOnly(candidate)) {
                error = doLoad(candidate, loader);
                if (error != null) {
                    lastError = error;
                } else {
                    return;
                }
            }
        }
        if (lastError != null) {
            throw new UnsatisfiedLinkError(lastError);
        }
        throw new UnsatisfiedLinkError("Library " + libname + " not found; tried " + candidates);
    }

    private String[] getLibPaths() {
        if (this.mLibPaths == null) {
            synchronized (this) {
                if (this.mLibPaths == null) {
                    this.mLibPaths = initLibPaths();
                }
            }
        }
        return this.mLibPaths;
    }

    private static String[] initLibPaths() {
        String javaLibraryPath = System.getProperty("java.library.path");
        if (javaLibraryPath == null) {
            return EmptyArray.STRING;
        }
        String[] paths = javaLibraryPath.split(":");
        for (int i = 0; i < paths.length; i++) {
            if (!paths[i].endsWith("/")) {
                paths[i] = paths[i] + "/";
            }
        }
        return paths;
    }

    private String doLoad(String name, ClassLoader loader) {
        String nativeLoad;
        String librarySearchPath = null;
        if (loader != null && (loader instanceof BaseDexClassLoader)) {
            librarySearchPath = ((BaseDexClassLoader) loader).getLdLibraryPath();
        }
        synchronized (this) {
            nativeLoad = nativeLoad(name, loader, librarySearchPath);
        }
        return nativeLoad;
    }

    @Deprecated
    public InputStream getLocalizedInputStream(InputStream in) {
        return in;
    }

    @Deprecated
    public OutputStream getLocalizedOutputStream(OutputStream out) {
        return out;
    }
}
