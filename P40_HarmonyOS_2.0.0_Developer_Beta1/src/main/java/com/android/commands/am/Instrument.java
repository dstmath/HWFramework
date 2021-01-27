package com.android.commands.am;

import android.app.IActivityManager;
import android.app.IInstrumentationWatcher;
import android.app.UiAutomationConnection;
import android.content.ComponentName;
import android.content.pm.IPackageManager;
import android.content.pm.InstrumentationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ServiceManager;
import android.util.AndroidException;
import android.util.proto.ProtoOutputStream;
import android.view.IWindowManager;
import com.android.commands.am.InstrumentationData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Instrument {
    public static final String DEFAULT_LOG_DIR = "instrument-logs";
    private static final int STATUS_TEST_FAILED_ASSERTION = -1;
    private static final int STATUS_TEST_FAILED_OTHER = -2;
    private static final int STATUS_TEST_PASSED = 0;
    private static final int STATUS_TEST_STARTED = 1;
    private static final String TAG = "am";
    public String abi = null;
    public Bundle args = new Bundle();
    public String componentNameArg;
    public boolean disableHiddenApiChecks = false;
    public boolean disableIsolatedStorage = false;
    String logPath = null;
    private final IActivityManager mAm;
    private final IPackageManager mPm;
    private final IWindowManager mWm;
    public boolean noWindowAnimation = false;
    public String profileFile = null;
    boolean protoFile = false;
    boolean protoStd = false;
    public boolean rawMode = false;
    public int userId = STATUS_TEST_FAILED_OTHER;
    public boolean wait = false;

    private interface StatusReporter {
        void onError(String str, boolean z);

        void onInstrumentationFinishedLocked(ComponentName componentName, int i, Bundle bundle);

        void onInstrumentationStatusLocked(ComponentName componentName, int i, Bundle bundle);
    }

    public Instrument(IActivityManager am, IPackageManager pm) {
        this.mAm = am;
        this.mPm = pm;
        this.mWm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
    }

    /* access modifiers changed from: private */
    public static Collection<String> sorted(Collection<String> list) {
        ArrayList<String> copy = new ArrayList<>(list);
        Collections.sort(copy);
        return copy;
    }

    private class TextStatusReporter implements StatusReporter {
        private boolean mRawMode;

        public TextStatusReporter(boolean rawMode) {
            this.mRawMode = rawMode;
        }

        @Override // com.android.commands.am.Instrument.StatusReporter
        public void onInstrumentationStatusLocked(ComponentName name, int resultCode, Bundle results) {
            String pretty = null;
            if (!this.mRawMode && results != null) {
                pretty = results.getString("stream");
            }
            if (pretty != null) {
                System.out.print(pretty);
                return;
            }
            if (results != null) {
                for (String key : Instrument.sorted(results.keySet())) {
                    PrintStream printStream = System.out;
                    printStream.println("INSTRUMENTATION_STATUS: " + key + "=" + results.get(key));
                }
            }
            PrintStream printStream2 = System.out;
            printStream2.println("INSTRUMENTATION_STATUS_CODE: " + resultCode);
        }

        @Override // com.android.commands.am.Instrument.StatusReporter
        public void onInstrumentationFinishedLocked(ComponentName name, int resultCode, Bundle results) {
            String pretty = null;
            if (!this.mRawMode && results != null) {
                pretty = results.getString("stream");
            }
            if (pretty != null) {
                System.out.println(pretty);
                return;
            }
            if (results != null) {
                for (String key : Instrument.sorted(results.keySet())) {
                    PrintStream printStream = System.out;
                    printStream.println("INSTRUMENTATION_RESULT: " + key + "=" + results.get(key));
                }
            }
            PrintStream printStream2 = System.out;
            printStream2.println("INSTRUMENTATION_CODE: " + resultCode);
        }

        @Override // com.android.commands.am.Instrument.StatusReporter
        public void onError(String errorText, boolean commandError) {
            if (this.mRawMode) {
                PrintStream printStream = System.out;
                printStream.println("onError: commandError=" + commandError + " message=" + errorText);
            }
            if (!commandError) {
                System.out.println(errorText);
            }
        }
    }

    private class ProtoStatusReporter implements StatusReporter {
        private File mLog;
        private long mTestStartMs;

        ProtoStatusReporter() {
            if (Instrument.this.protoFile) {
                if (Instrument.this.logPath == null) {
                    File logDir = new File(Environment.getLegacyExternalStorageDirectory(), Instrument.DEFAULT_LOG_DIR);
                    if (logDir.exists() || logDir.mkdirs()) {
                        this.mLog = new File(logDir, String.format("log-%s.instrumentation_data_proto", new SimpleDateFormat("yyyyMMdd-hhmmss-SSS", Locale.US).format(new Date())));
                    } else {
                        System.err.format("Unable to create log directory: %s\n", logDir.getAbsolutePath());
                        Instrument.this.protoFile = false;
                        return;
                    }
                } else {
                    this.mLog = new File(Environment.getLegacyExternalStorageDirectory(), Instrument.this.logPath);
                    File logDir2 = this.mLog.getParentFile();
                    if (!logDir2.exists() && !logDir2.mkdirs()) {
                        System.err.format("Unable to create log directory: %s\n", logDir2.getAbsolutePath());
                        Instrument.this.protoFile = false;
                        return;
                    }
                }
                if (this.mLog.exists()) {
                    this.mLog.delete();
                }
            }
        }

        @Override // com.android.commands.am.Instrument.StatusReporter
        public void onInstrumentationStatusLocked(ComponentName name, int resultCode, Bundle results) {
            ProtoOutputStream proto = new ProtoOutputStream();
            long testStatusToken = proto.start(2246267895809L);
            proto.write(1172526071811L, resultCode);
            writeBundle(proto, 1146756268036L, results);
            if (resultCode == 1) {
                this.mTestStartMs = System.currentTimeMillis();
            } else {
                long j = this.mTestStartMs;
                if (j > 0) {
                    proto.write(InstrumentationData.TestStatus.LOGCAT, Instrument.readLogcat(j));
                }
                this.mTestStartMs = 0;
            }
            proto.end(testStatusToken);
            outputProto(proto);
        }

        @Override // com.android.commands.am.Instrument.StatusReporter
        public void onInstrumentationFinishedLocked(ComponentName name, int resultCode, Bundle results) {
            ProtoOutputStream proto = new ProtoOutputStream();
            long sessionStatusToken = proto.start(InstrumentationData.Session.SESSION_STATUS);
            proto.write(InstrumentationData.SessionStatus.STATUS_CODE, Instrument.STATUS_TEST_PASSED);
            proto.write(1172526071811L, resultCode);
            writeBundle(proto, 1146756268036L, results);
            proto.end(sessionStatusToken);
            outputProto(proto);
        }

        @Override // com.android.commands.am.Instrument.StatusReporter
        public void onError(String errorText, boolean commandError) {
            ProtoOutputStream proto = new ProtoOutputStream();
            long sessionStatusToken = proto.start(InstrumentationData.Session.SESSION_STATUS);
            proto.write(InstrumentationData.SessionStatus.STATUS_CODE, 1);
            proto.write(1138166333442L, errorText);
            proto.end(sessionStatusToken);
            outputProto(proto);
        }

        private void writeBundle(ProtoOutputStream proto, long fieldId, Bundle bundle) {
            long bundleToken = proto.start(fieldId);
            for (String key : Instrument.sorted(bundle.keySet())) {
                long entryToken = proto.startRepeatedObject(2246267895809L);
                proto.write(InstrumentationData.ResultsBundleEntry.KEY, key);
                Object val = bundle.get(key);
                if (val instanceof String) {
                    proto.write(1138166333442L, (String) val);
                } else if (val instanceof Byte) {
                    proto.write(1172526071811L, ((Byte) val).intValue());
                } else if (val instanceof Double) {
                    proto.write(InstrumentationData.ResultsBundleEntry.VALUE_DOUBLE, ((Double) val).doubleValue());
                } else if (val instanceof Float) {
                    proto.write(InstrumentationData.ResultsBundleEntry.VALUE_FLOAT, ((Float) val).floatValue());
                } else if (val instanceof Integer) {
                    proto.write(1172526071811L, ((Integer) val).intValue());
                } else if (val instanceof Long) {
                    proto.write(InstrumentationData.ResultsBundleEntry.VALUE_LONG, ((Long) val).longValue());
                } else if (val instanceof Short) {
                    proto.write(1172526071811L, (int) ((Short) val).shortValue());
                } else if (val instanceof Bundle) {
                    writeBundle(proto, InstrumentationData.ResultsBundleEntry.VALUE_BUNDLE, (Bundle) val);
                } else if (val instanceof byte[]) {
                    proto.write(InstrumentationData.ResultsBundleEntry.VALUE_BYTES, (byte[]) val);
                }
                proto.end(entryToken);
            }
            proto.end(bundleToken);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0040, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0045, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0046, code lost:
            r3.addSuppressed(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0049, code lost:
            throw r4;
         */
        private void outputProto(ProtoOutputStream proto) {
            byte[] out = proto.getBytes();
            if (Instrument.this.protoStd) {
                try {
                    System.out.write(out);
                    System.out.flush();
                } catch (IOException ex) {
                    System.err.println("Error writing finished response: ");
                    ex.printStackTrace(System.err);
                }
            }
            if (Instrument.this.protoFile) {
                try {
                    OutputStream os = new FileOutputStream(this.mLog, true);
                    os.write(proto.getBytes());
                    os.flush();
                    os.close();
                } catch (IOException ex2) {
                    System.err.format("Cannot write to %s:\n", this.mLog.getAbsolutePath());
                    ex2.printStackTrace();
                }
            }
        }
    }

    private class InstrumentationWatcher extends IInstrumentationWatcher.Stub {
        private boolean mFinished = false;
        private final StatusReporter mReporter;

        public InstrumentationWatcher(StatusReporter reporter) {
            this.mReporter = reporter;
        }

        public void instrumentationStatus(ComponentName name, int resultCode, Bundle results) {
            synchronized (this) {
                this.mReporter.onInstrumentationStatusLocked(name, resultCode, results);
                notifyAll();
            }
        }

        public void instrumentationFinished(ComponentName name, int resultCode, Bundle results) {
            synchronized (this) {
                this.mReporter.onInstrumentationFinishedLocked(name, resultCode, results);
                this.mFinished = true;
                notifyAll();
            }
        }

        public boolean waitForFinish() {
            synchronized (this) {
                while (!this.mFinished) {
                    try {
                        if (!Instrument.this.mAm.asBinder().pingBinder()) {
                            return false;
                        }
                        wait(1000);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                }
                return true;
            }
        }
    }

    private ComponentName parseComponentName(String cnArg) throws Exception {
        if (cnArg.contains("/")) {
            ComponentName cn = ComponentName.unflattenFromString(cnArg);
            if (cn != null) {
                return cn;
            }
            throw new IllegalArgumentException("Bad component name: " + cnArg);
        }
        List<InstrumentationInfo> infos = this.mPm.queryInstrumentation((String) null, (int) STATUS_TEST_PASSED).getList();
        int numInfos = infos == null ? STATUS_TEST_PASSED : infos.size();
        ArrayList<ComponentName> cns = new ArrayList<>();
        for (int i = STATUS_TEST_PASSED; i < numInfos; i++) {
            InstrumentationInfo info = infos.get(i);
            ComponentName c = new ComponentName(info.packageName, info.name);
            if (cnArg.equals(info.packageName)) {
                cns.add(c);
            }
        }
        if (cns.size() == 0) {
            throw new IllegalArgumentException("No instrumentation found for: " + cnArg);
        } else if (cns.size() == 1) {
            return cns.get(STATUS_TEST_PASSED);
        } else {
            StringBuilder cnsStr = new StringBuilder();
            int numCns = cns.size();
            for (int i2 = STATUS_TEST_PASSED; i2 < numCns; i2++) {
                cnsStr.append(cns.get(i2).flattenToString());
                cnsStr.append(", ");
            }
            cnsStr.setLength(cnsStr.length() + STATUS_TEST_FAILED_OTHER);
            throw new IllegalArgumentException("Found multiple instrumentations: " + cnsStr.toString());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0025 A[Catch:{ Exception -> 0x00ea, all -> 0x00e8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0036 A[Catch:{ Exception -> 0x00ea, all -> 0x00e8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0058 A[Catch:{ Exception -> 0x00ea, all -> 0x00e8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x008f A[Catch:{ Exception -> 0x00ea, all -> 0x00e8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0095 A[Catch:{ Exception -> 0x00ea, all -> 0x00e8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009a A[Catch:{ Exception -> 0x00ea, all -> 0x00e8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00b0 A[Catch:{ Exception -> 0x00ea, all -> 0x00e8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00cd  */
    public void run() throws Exception {
        InstrumentationWatcher watcher;
        UiAutomationConnection connection;
        ComponentName cn;
        int flags;
        StatusReporter reporter = null;
        float[] oldAnims = null;
        try {
            if (!this.protoFile) {
                if (!this.protoStd) {
                    if (this.wait) {
                        reporter = new TextStatusReporter(this.rawMode);
                    }
                    watcher = null;
                    connection = null;
                    if (reporter != null) {
                        watcher = new InstrumentationWatcher(reporter);
                        connection = new UiAutomationConnection();
                    }
                    if (this.noWindowAnimation) {
                        oldAnims = this.mWm.getAnimationScales();
                        this.mWm.setAnimationScale((int) STATUS_TEST_PASSED, 0.0f);
                        this.mWm.setAnimationScale(1, 0.0f);
                        this.mWm.setAnimationScale(2, 0.0f);
                    }
                    cn = parseComponentName(this.componentNameArg);
                    if (this.abi != null) {
                        String[] supportedAbis = Build.SUPPORTED_ABIS;
                        boolean matched = false;
                        int length = supportedAbis.length;
                        int i = STATUS_TEST_PASSED;
                        while (true) {
                            if (i >= length) {
                                break;
                            } else if (supportedAbis[i].equals(this.abi)) {
                                matched = true;
                                break;
                            } else {
                                i++;
                            }
                        }
                        if (!matched) {
                            throw new AndroidException("INSTRUMENTATION_FAILED: Unsupported instruction set " + this.abi);
                        }
                    }
                    int flags2 = STATUS_TEST_PASSED;
                    if (this.disableHiddenApiChecks) {
                        flags2 = STATUS_TEST_PASSED | 1;
                    }
                    if (!this.disableIsolatedStorage) {
                        flags = flags2 | 2;
                    } else {
                        flags = flags2;
                    }
                    if (this.mAm.startInstrumentation(cn, this.profileFile, flags, this.args, watcher, connection, this.userId, this.abi)) {
                        throw new AndroidException("INSTRUMENTATION_FAILED: " + cn.flattenToString());
                    } else if (watcher != null && !watcher.waitForFinish()) {
                        reporter.onError("INSTRUMENTATION_ABORTED: System has crashed.", false);
                        if (oldAnims != null) {
                            this.mWm.setAnimationScales(oldAnims);
                            return;
                        }
                        return;
                    } else if (oldAnims != null) {
                        this.mWm.setAnimationScales(oldAnims);
                        return;
                    } else {
                        return;
                    }
                }
            }
            reporter = new ProtoStatusReporter();
            watcher = null;
            connection = null;
            if (reporter != null) {
            }
            if (this.noWindowAnimation) {
            }
            cn = parseComponentName(this.componentNameArg);
            if (this.abi != null) {
            }
            int flags22 = STATUS_TEST_PASSED;
            if (this.disableHiddenApiChecks) {
            }
            if (!this.disableIsolatedStorage) {
            }
            if (this.mAm.startInstrumentation(cn, this.profileFile, flags, this.args, watcher, connection, this.userId, this.abi)) {
            }
        } catch (Exception ex) {
            if (STATUS_TEST_PASSED != 0) {
                reporter.onError(ex.getMessage(), true);
            }
            throw ex;
        } catch (Throwable th) {
            if (STATUS_TEST_PASSED != 0) {
                this.mWm.setAnimationScales((float[]) null);
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static String readLogcat(long startTimeMs) {
        try {
            Process process = new ProcessBuilder(new String[STATUS_TEST_PASSED]).command("logcat", "-d", "-v threadtime,uid", "-T", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(startTimeMs))).start();
            process.getOutputStream().close();
            StringBuilder str = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(process.getInputStream());
            char[] buffer = new char[4096];
            while (true) {
                int amt = reader.read(buffer, STATUS_TEST_PASSED, buffer.length);
                if (amt < 0) {
                    try {
                        break;
                    } catch (InterruptedException e) {
                    }
                } else if (amt > 0) {
                    str.append(buffer, STATUS_TEST_PASSED, amt);
                }
            }
            process.waitFor();
            return str.toString();
        } catch (IOException ex) {
            return "Error reading logcat command:\n" + ex.toString();
        }
    }
}
