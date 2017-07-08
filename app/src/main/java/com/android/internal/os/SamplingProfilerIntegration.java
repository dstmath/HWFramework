package com.android.internal.os;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;
import com.android.internal.content.NativeLibraryHelper;
import dalvik.system.profiler.BinaryHprofWriter;
import dalvik.system.profiler.SamplingProfiler;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.io.IoUtils;

public class SamplingProfilerIntegration {
    public static final String SNAPSHOT_DIR = "/data/snapshots";
    private static final String TAG = "SamplingProfilerIntegration";
    private static final boolean enabled = false;
    private static final AtomicBoolean pending = null;
    private static SamplingProfiler samplingProfiler;
    private static final int samplingProfilerDepth = 0;
    private static final int samplingProfilerMilliseconds = 0;
    private static final Executor snapshotWriter = null;
    private static long startMillis;

    /* renamed from: com.android.internal.os.SamplingProfilerIntegration.2 */
    static class AnonymousClass2 implements Runnable {
        final /* synthetic */ PackageInfo val$packageInfo;
        final /* synthetic */ String val$processName;

        AnonymousClass2(String val$processName, PackageInfo val$packageInfo) {
            this.val$processName = val$processName;
            this.val$packageInfo = val$packageInfo;
        }

        public void run() {
            try {
                SamplingProfilerIntegration.writeSnapshotFile(this.val$processName, this.val$packageInfo);
            } finally {
                SamplingProfilerIntegration.pending.set(false);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.SamplingProfilerIntegration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.SamplingProfilerIntegration.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.SamplingProfilerIntegration.<clinit>():void");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void start() {
        if (!enabled) {
            return;
        }
        if (samplingProfiler != null) {
            Log.e(TAG, "SamplingProfilerIntegration already started at " + new Date(startMillis));
            return;
        }
        samplingProfiler = new SamplingProfiler(samplingProfilerDepth, SamplingProfiler.newThreadGroupThreadSet(Thread.currentThread().getThreadGroup()));
        samplingProfiler.start(samplingProfilerMilliseconds);
        startMillis = System.currentTimeMillis();
    }

    public static void writeSnapshot(String processName, PackageInfo packageInfo) {
        if (!enabled) {
            return;
        }
        if (samplingProfiler == null) {
            Log.e(TAG, "SamplingProfilerIntegration is not started");
            return;
        }
        if (pending.compareAndSet(false, true)) {
            snapshotWriter.execute(new AnonymousClass2(processName, packageInfo));
        }
    }

    public static void writeZygoteSnapshot() {
        if (enabled) {
            writeSnapshotFile("zygote", null);
            samplingProfiler.shutdown();
            samplingProfiler = null;
            startMillis = 0;
        }
    }

    private static void writeSnapshotFile(String processName, PackageInfo packageInfo) {
        IOException e;
        Throwable th;
        if (enabled) {
            samplingProfiler.stop();
            String name = processName.replaceAll(":", ".");
            String path = "/data/snapshots/" + name + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + startMillis + ".snapshot";
            long start = System.currentTimeMillis();
            AutoCloseable autoCloseable = null;
            try {
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path));
                try {
                    PrintStream out = new PrintStream(outputStream);
                    generateSnapshotHeader(name, packageInfo, out);
                    if (out.checkError()) {
                        throw new IOException();
                    }
                    BinaryHprofWriter.write(samplingProfiler.getHprofData(), outputStream);
                    IoUtils.closeQuietly(outputStream);
                    new File(path).setReadable(true, false);
                    Log.i(TAG, "Wrote snapshot " + path + " in " + (System.currentTimeMillis() - start) + "ms.");
                    samplingProfiler.start(samplingProfilerMilliseconds);
                } catch (IOException e2) {
                    e = e2;
                    autoCloseable = outputStream;
                    try {
                        Log.e(TAG, "Error writing snapshot to " + path, e);
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Object outputStream2 = outputStream;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                Log.e(TAG, "Error writing snapshot to " + path, e);
                IoUtils.closeQuietly(autoCloseable);
            }
        }
    }

    private static void generateSnapshotHeader(String processName, PackageInfo packageInfo, PrintStream out) {
        out.println("Version: 3");
        out.println("Process: " + processName);
        if (packageInfo != null) {
            out.println("Package: " + packageInfo.packageName);
            out.println("Package-Version: " + packageInfo.versionCode);
        }
        out.println("Build: " + Build.FINGERPRINT);
        out.println();
    }
}
