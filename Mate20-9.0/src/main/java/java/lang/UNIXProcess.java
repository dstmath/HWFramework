package java.lang;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

final class UNIXProcess extends Process {
    private static final Executor processReaperExecutor = ((Executor) AccessController.doPrivileged(new PrivilegedAction<Executor>() {
        public Executor run() {
            return Executors.newCachedThreadPool(new ProcessReaperThreadFactory());
        }
    }));
    private int exitcode;
    private boolean hasExited;
    /* access modifiers changed from: private */
    public final int pid;
    private InputStream stderr;
    private OutputStream stdin;
    private InputStream stdout;

    static class ProcessPipeInputStream extends BufferedInputStream {
        ProcessPipeInputStream(int fd) {
            super(new FileInputStream(UNIXProcess.newFileDescriptor(fd), true));
        }

        private static byte[] drainInputStream(InputStream in) throws IOException {
            byte[] a = null;
            if (in == null) {
                return null;
            }
            int n = 0;
            while (true) {
                int available = in.available();
                int j = available;
                if (available <= 0) {
                    break;
                }
                a = a == null ? new byte[j] : Arrays.copyOf(a, n + j);
                n += in.read(a, n, j);
            }
            return (a == null || n == a.length) ? a : Arrays.copyOf(a, n);
        }

        /* Debug info: failed to restart local var, previous not found, register: 3 */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
        public synchronized void processExited() {
            InputStream inputStream;
            InputStream in = this.in;
            if (in != null) {
                byte[] stragglers = drainInputStream(in);
                in.close();
                if (stragglers == null) {
                    inputStream = ProcessBuilder.NullInputStream.INSTANCE;
                } else {
                    inputStream = new ByteArrayInputStream(stragglers);
                }
                this.in = inputStream;
                if (this.buf == null) {
                    this.in = null;
                }
            }
        }
    }

    static class ProcessPipeOutputStream extends BufferedOutputStream {
        ProcessPipeOutputStream(int fd) {
            super(new FileOutputStream(UNIXProcess.newFileDescriptor(fd), true));
        }

        /* access modifiers changed from: package-private */
        public synchronized void processExited() {
            OutputStream out = this.out;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
                this.out = ProcessBuilder.NullOutputStream.INSTANCE;
            }
        }
    }

    private static class ProcessReaperThreadFactory implements ThreadFactory {
        private static final ThreadGroup group = getRootThreadGroup();

        private ProcessReaperThreadFactory() {
        }

        private static ThreadGroup getRootThreadGroup() {
            return (ThreadGroup) AccessController.doPrivileged(new PrivilegedAction<ThreadGroup>() {
                public ThreadGroup run() {
                    ThreadGroup root = Thread.currentThread().getThreadGroup();
                    while (root.getParent() != null) {
                        root = root.getParent();
                    }
                    return root;
                }
            });
        }

        public Thread newThread(Runnable grimReaper) {
            Thread t = new Thread(group, grimReaper, "process reaper", 32768);
            t.setDaemon(true);
            t.setPriority(10);
            return t;
        }
    }

    private static native void destroyProcess(int i);

    private native int forkAndExec(byte[] bArr, byte[] bArr2, int i, byte[] bArr3, int i2, byte[] bArr4, int[] iArr, boolean z) throws IOException;

    private static native void initIDs();

    /* access modifiers changed from: private */
    public native int waitForProcessExit(int i);

    static {
        initIDs();
    }

    UNIXProcess(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir, final int[] fds, boolean redirectErrorStream) throws IOException {
        this.pid = forkAndExec(prog, argBlock, argc, envBlock, envc, dir, fds, redirectErrorStream);
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws IOException {
                    UNIXProcess.this.initStreams(fds);
                    return null;
                }
            });
        } catch (PrivilegedActionException ex) {
            throw ((IOException) ex.getException());
        }
    }

    static FileDescriptor newFileDescriptor(int fd) {
        FileDescriptor fileDescriptor = new FileDescriptor();
        fileDescriptor.setInt$(fd);
        return fileDescriptor;
    }

    /* access modifiers changed from: package-private */
    public void initStreams(int[] fds) throws IOException {
        OutputStream outputStream;
        InputStream inputStream;
        InputStream inputStream2;
        if (fds[0] == -1) {
            outputStream = ProcessBuilder.NullOutputStream.INSTANCE;
        } else {
            outputStream = new ProcessPipeOutputStream(fds[0]);
        }
        this.stdin = outputStream;
        if (fds[1] == -1) {
            inputStream = ProcessBuilder.NullInputStream.INSTANCE;
        } else {
            inputStream = new ProcessPipeInputStream(fds[1]);
        }
        this.stdout = inputStream;
        if (fds[2] == -1) {
            inputStream2 = ProcessBuilder.NullInputStream.INSTANCE;
        } else {
            inputStream2 = new ProcessPipeInputStream(fds[2]);
        }
        this.stderr = inputStream2;
        processReaperExecutor.execute(new Runnable() {
            public void run() {
                UNIXProcess.this.processExited(UNIXProcess.this.waitForProcessExit(UNIXProcess.this.pid));
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void processExited(int exitcode2) {
        synchronized (this) {
            this.exitcode = exitcode2;
            this.hasExited = true;
            notifyAll();
        }
        if (this.stdout instanceof ProcessPipeInputStream) {
            ((ProcessPipeInputStream) this.stdout).processExited();
        }
        if (this.stderr instanceof ProcessPipeInputStream) {
            ((ProcessPipeInputStream) this.stderr).processExited();
        }
        if (this.stdin instanceof ProcessPipeOutputStream) {
            ((ProcessPipeOutputStream) this.stdin).processExited();
        }
    }

    public OutputStream getOutputStream() {
        return this.stdin;
    }

    public InputStream getInputStream() {
        return this.stdout;
    }

    public InputStream getErrorStream() {
        return this.stderr;
    }

    public synchronized int waitFor() throws InterruptedException {
        while (!this.hasExited) {
            wait();
        }
        return this.exitcode;
    }

    public synchronized int exitValue() {
        if (this.hasExited) {
        } else {
            throw new IllegalThreadStateException("process hasn't exited");
        }
        return this.exitcode;
    }

    public void destroy() {
        synchronized (this) {
            if (!this.hasExited) {
                destroyProcess(this.pid);
            }
        }
        try {
            this.stdin.close();
        } catch (IOException e) {
        }
        try {
            this.stdout.close();
        } catch (IOException e2) {
        }
        try {
            this.stderr.close();
        } catch (IOException e3) {
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Process[pid=");
        sb.append(this.pid);
        if (this.hasExited) {
            sb.append(" ,hasExited=true, exitcode=");
            sb.append(this.exitcode);
            sb.append("]");
        } else {
            sb.append(", hasExited=false]");
        }
        return sb.toString();
    }
}
