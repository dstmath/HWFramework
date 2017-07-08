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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import libcore.util.EmptyArray;
import sun.reflect.CallerSensitive;

public class Runtime {
    private static Runtime currentRuntime;
    private static boolean finalizeOnExit;
    private volatile String[] mLibPaths;
    private List<Thread> shutdownHooks;
    private boolean shuttingDown;
    private boolean tracingMethods;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Runtime.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Runtime.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Runtime.<clinit>():void");
    }

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
        this.shutdownHooks = new ArrayList();
        this.mLibPaths = null;
    }

    public void exit(int status) {
        int i = 0;
        synchronized (this) {
            if (!this.shuttingDown) {
                Thread[] hooks;
                this.shuttingDown = true;
                synchronized (this.shutdownHooks) {
                    hooks = new Thread[this.shutdownHooks.size()];
                    this.shutdownHooks.toArray(hooks);
                }
                for (Thread hook : hooks) {
                    hook.start();
                }
                int length = hooks.length;
                while (i < length) {
                    try {
                        hooks[i].join();
                    } catch (InterruptedException e) {
                    }
                    i++;
                }
                if (finalizeOnExit) {
                    runFinalization();
                }
                nativeExit(status);
            }
        }
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
        String str = null;
        try {
            System.out.println(Arrays.toString((Object[]) cmdarray));
            System.out.println(Arrays.toString((Object[]) envp));
            PrintStream printStream = System.out;
            if (dir != null) {
                str = dir.getCanonicalPath();
            }
            printStream.println(str);
            StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
            if (stackTraceElements != null) {
                for (StackTraceElement element : stackTraceElements) {
                    if (element != null && !"java.lang.Runtime".equals(element.getClassName())) {
                        System.out.println("Calling by::className:" + element.getClassName() + "  MethodName:" + element.getMethodName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        return new ProcessBuilder(cmdarray).environment(envp).directory(dir).start();
    }

    public int availableProcessors() {
        return (int) Libcore.os.sysconf(OsConstants._SC_NPROCESSORS_CONF);
    }

    public void runFinalization() {
        VMRuntime.runFinalization(0);
    }

    public void traceInstructions(boolean enable) {
    }

    public void traceMethodCalls(boolean enable) {
        if (enable != this.tracingMethods) {
            if (enable) {
                VMDebug.startMethodTracing();
            } else {
                VMDebug.stopMethodTracing();
            }
            this.tracingMethods = enable;
        }
    }

    @CallerSensitive
    public void load(String filename) {
        load0(VMStack.getStackClass2(), filename);
    }

    void load(String absolutePath, ClassLoader loader) {
        System.logE("java.lang.Runtime#load(String, ClassLoader) is private and will be removed in a future Android release");
        if (absolutePath == null) {
            throw new NullPointerException("absolutePath == null");
        }
        String error = doLoad(absolutePath, loader);
        if (error != null) {
            throw new UnsatisfiedLinkError(error);
        }
    }

    synchronized void load0(Class fromClass, String filename) {
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
