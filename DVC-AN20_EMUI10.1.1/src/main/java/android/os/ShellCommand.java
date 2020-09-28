package android.os;

import android.annotation.UnsupportedAppUsage;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.util.FastPrintWriter;
import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public abstract class ShellCommand {
    static final boolean DEBUG = false;
    static final String TAG = "ShellCommand";
    private int mArgPos;
    private String[] mArgs;
    private String mCmd;
    private String mCurArgData;
    private FileDescriptor mErr;
    private FastPrintWriter mErrPrintWriter;
    private FileOutputStream mFileErr;
    private FileInputStream mFileIn;
    private FileOutputStream mFileOut;
    private FileDescriptor mIn;
    private InputStream mInputStream;
    private FileDescriptor mOut;
    private FastPrintWriter mOutPrintWriter;
    private ResultReceiver mResultReceiver;
    private ShellCallback mShellCallback;
    private Binder mTarget;

    public abstract int onCommand(String str);

    public abstract void onHelp();

    public void init(Binder target, FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, int firstArgPos) {
        this.mTarget = target;
        this.mIn = in;
        this.mOut = out;
        this.mErr = err;
        this.mArgs = args;
        this.mShellCallback = callback;
        this.mResultReceiver = null;
        this.mCmd = null;
        this.mArgPos = firstArgPos;
        this.mCurArgData = null;
        this.mFileIn = null;
        this.mFileOut = null;
        this.mFileErr = null;
        this.mOutPrintWriter = null;
        this.mErrPrintWriter = null;
        this.mInputStream = null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0041, code lost:
        if (r0 != null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0043, code lost:
        r0.send(r2, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0067, code lost:
        if (r0 == null) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x009d, code lost:
        if (r0 == null) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a0, code lost:
        return r2;
     */
    public int exec(Binder target, FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        String cmd;
        int start;
        ResultReceiver resultReceiver2;
        if (args == null || args.length <= 0) {
            cmd = null;
            start = 0;
        } else {
            cmd = args[0];
            start = 1;
        }
        init(target, in, out, err, args, callback, start);
        this.mCmd = cmd;
        this.mResultReceiver = resultReceiver;
        int res = -1;
        try {
            res = onCommand(this.mCmd);
            FastPrintWriter fastPrintWriter = this.mOutPrintWriter;
            if (fastPrintWriter != null) {
                fastPrintWriter.flush();
            }
            FastPrintWriter fastPrintWriter2 = this.mErrPrintWriter;
            if (fastPrintWriter2 != null) {
                fastPrintWriter2.flush();
            }
            resultReceiver2 = this.mResultReceiver;
        } catch (SecurityException e) {
            PrintWriter eout = getErrPrintWriter();
            eout.println("Security exception: " + e.getMessage());
            eout.println();
            e.printStackTrace(eout);
            FastPrintWriter fastPrintWriter3 = this.mOutPrintWriter;
            if (fastPrintWriter3 != null) {
                fastPrintWriter3.flush();
            }
            FastPrintWriter fastPrintWriter4 = this.mErrPrintWriter;
            if (fastPrintWriter4 != null) {
                fastPrintWriter4.flush();
            }
            resultReceiver2 = this.mResultReceiver;
        } catch (Throwable th) {
            FastPrintWriter fastPrintWriter5 = this.mOutPrintWriter;
            if (fastPrintWriter5 != null) {
                fastPrintWriter5.flush();
            }
            FastPrintWriter fastPrintWriter6 = this.mErrPrintWriter;
            if (fastPrintWriter6 != null) {
                fastPrintWriter6.flush();
            }
            ResultReceiver resultReceiver3 = this.mResultReceiver;
            if (resultReceiver3 != null) {
                resultReceiver3.send(-1, null);
            }
            throw th;
        }
    }

    public ResultReceiver adoptResultReceiver() {
        ResultReceiver rr = this.mResultReceiver;
        this.mResultReceiver = null;
        return rr;
    }

    public FileDescriptor getOutFileDescriptor() {
        return this.mOut;
    }

    public OutputStream getRawOutputStream() {
        if (this.mFileOut == null) {
            this.mFileOut = new FileOutputStream(this.mOut);
        }
        return this.mFileOut;
    }

    public PrintWriter getOutPrintWriter() {
        if (this.mOutPrintWriter == null) {
            this.mOutPrintWriter = new FastPrintWriter(getRawOutputStream());
        }
        return this.mOutPrintWriter;
    }

    public FileDescriptor getErrFileDescriptor() {
        return this.mErr;
    }

    public OutputStream getRawErrorStream() {
        if (this.mFileErr == null) {
            this.mFileErr = new FileOutputStream(this.mErr);
        }
        return this.mFileErr;
    }

    public PrintWriter getErrPrintWriter() {
        if (this.mErr == null) {
            return getOutPrintWriter();
        }
        if (this.mErrPrintWriter == null) {
            this.mErrPrintWriter = new FastPrintWriter(getRawErrorStream());
        }
        return this.mErrPrintWriter;
    }

    public FileDescriptor getInFileDescriptor() {
        return this.mIn;
    }

    public InputStream getRawInputStream() {
        if (this.mFileIn == null) {
            this.mFileIn = new FileInputStream(this.mIn);
        }
        return this.mFileIn;
    }

    public InputStream getBufferedInputStream() {
        if (this.mInputStream == null) {
            this.mInputStream = new BufferedInputStream(getRawInputStream());
        }
        return this.mInputStream;
    }

    public ParcelFileDescriptor openFileForSystem(String path, String mode) {
        try {
            ParcelFileDescriptor pfd = getShellCallback().openFile(path, "u:r:system_server:s0", mode);
            if (pfd != null) {
                return pfd;
            }
        } catch (RuntimeException e) {
            PrintWriter errPrintWriter = getErrPrintWriter();
            errPrintWriter.println("Failure opening file: " + e.getMessage());
        }
        PrintWriter errPrintWriter2 = getErrPrintWriter();
        errPrintWriter2.println("Error: Unable to open file: " + path);
        if (path != null && path.startsWith("/data/local/tmp/")) {
            return null;
        }
        PrintWriter errPrintWriter3 = getErrPrintWriter();
        errPrintWriter3.println("Consider using a file under " + "/data/local/tmp/");
        return null;
    }

    public String getNextOption() {
        if (this.mCurArgData == null) {
            int i = this.mArgPos;
            String[] strArr = this.mArgs;
            if (i >= strArr.length) {
                return null;
            }
            String arg = strArr[i];
            if (!arg.startsWith(NativeLibraryHelper.CLEAR_ABI_OVERRIDE)) {
                return null;
            }
            this.mArgPos++;
            if (arg.equals("--")) {
                return null;
            }
            if (arg.length() <= 1 || arg.charAt(1) == '-') {
                this.mCurArgData = null;
                return arg;
            } else if (arg.length() > 2) {
                this.mCurArgData = arg.substring(2);
                return arg.substring(0, 2);
            } else {
                this.mCurArgData = null;
                return arg;
            }
        } else {
            throw new IllegalArgumentException("No argument expected after \"" + this.mArgs[this.mArgPos - 1] + "\"");
        }
    }

    public String getNextArg() {
        if (this.mCurArgData != null) {
            String arg = this.mCurArgData;
            this.mCurArgData = null;
            return arg;
        }
        int i = this.mArgPos;
        String[] strArr = this.mArgs;
        if (i >= strArr.length) {
            return null;
        }
        this.mArgPos = i + 1;
        return strArr[i];
    }

    @UnsupportedAppUsage
    public String peekNextArg() {
        String str = this.mCurArgData;
        if (str != null) {
            return str;
        }
        int i = this.mArgPos;
        String[] strArr = this.mArgs;
        if (i < strArr.length) {
            return strArr[i];
        }
        return null;
    }

    public String getNextArgRequired() {
        String arg = getNextArg();
        if (arg != null) {
            return arg;
        }
        String prev = this.mArgs[this.mArgPos - 1];
        throw new IllegalArgumentException("Argument expected after \"" + prev + "\"");
    }

    public ShellCallback getShellCallback() {
        return this.mShellCallback;
    }

    public int handleDefaultCommands(String cmd) {
        if ("dump".equals(cmd)) {
            String[] strArr = this.mArgs;
            String[] newArgs = new String[(strArr.length - 1)];
            System.arraycopy(strArr, 1, newArgs, 0, strArr.length - 1);
            this.mTarget.doDump(this.mOut, getOutPrintWriter(), newArgs);
            return 0;
        } else if (cmd == null || "help".equals(cmd) || "-h".equals(cmd)) {
            onHelp();
            return -1;
        } else {
            PrintWriter outPrintWriter = getOutPrintWriter();
            outPrintWriter.println("Unknown command: " + cmd);
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public String[] getArgs() {
        return this.mArgs;
    }

    /* access modifiers changed from: protected */
    public int getArgPos() {
        return this.mArgPos;
    }
}
