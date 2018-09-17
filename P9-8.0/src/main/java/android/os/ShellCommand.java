package android.os;

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

    public int exec(Binder target, FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        String cmd;
        int start;
        PrintWriter eout;
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
        } catch (SecurityException e) {
            eout = getErrPrintWriter();
            eout.println("Security exception: " + e.getMessage());
            eout.println();
            e.printStackTrace(eout);
        } catch (Throwable e2) {
            eout = getErrPrintWriter();
            eout.println();
            eout.println("Exception occurred while executing:");
            e2.printStackTrace(eout);
        } finally {
            if (this.mOutPrintWriter != null) {
                this.mOutPrintWriter.flush();
            }
            if (this.mErrPrintWriter != null) {
                this.mErrPrintWriter.flush();
            }
            this.mResultReceiver.send(res, null);
        }
        return res;
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

    public ParcelFileDescriptor openOutputFileForSystem(String path) {
        try {
            ParcelFileDescriptor pfd = getShellCallback().openOutputFile(path, "u:r:system_server:s0");
            if (pfd != null) {
                return pfd;
            }
        } catch (RuntimeException e) {
            getErrPrintWriter().println("Failure opening file: " + e.getMessage());
        }
        getErrPrintWriter().println("Error: Unable to open file: " + path);
        getErrPrintWriter().println("Consider using a file under /data/local/tmp/");
        return null;
    }

    public String getNextOption() {
        if (this.mCurArgData != null) {
            throw new IllegalArgumentException("No argument expected after \"" + this.mArgs[this.mArgPos - 1] + "\"");
        } else if (this.mArgPos >= this.mArgs.length) {
            return null;
        } else {
            String arg = this.mArgs[this.mArgPos];
            if (!arg.startsWith("-")) {
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
        }
    }

    public String getNextArg() {
        if (this.mCurArgData != null) {
            String arg = this.mCurArgData;
            this.mCurArgData = null;
            return arg;
        } else if (this.mArgPos >= this.mArgs.length) {
            return null;
        } else {
            String[] strArr = this.mArgs;
            int i = this.mArgPos;
            this.mArgPos = i + 1;
            return strArr[i];
        }
    }

    public String peekNextArg() {
        if (this.mCurArgData != null) {
            return this.mCurArgData;
        }
        if (this.mArgPos < this.mArgs.length) {
            return this.mArgs[this.mArgPos];
        }
        return null;
    }

    public String getNextArgRequired() {
        String arg = getNextArg();
        if (arg != null) {
            return arg;
        }
        throw new IllegalArgumentException("Argument expected after \"" + this.mArgs[this.mArgPos - 1] + "\"");
    }

    public ShellCallback getShellCallback() {
        return this.mShellCallback;
    }

    public int handleDefaultCommands(String cmd) {
        if ("dump".equals(cmd)) {
            String[] newArgs = new String[(this.mArgs.length - 1)];
            System.arraycopy(this.mArgs, 1, newArgs, 0, this.mArgs.length - 1);
            this.mTarget.doDump(this.mOut, getOutPrintWriter(), newArgs);
            return 0;
        }
        if (cmd == null || "help".equals(cmd) || "-h".equals(cmd)) {
            onHelp();
        } else {
            getOutPrintWriter().println("Unknown command: " + cmd);
        }
        return -1;
    }

    protected String[] getArgs() {
        return this.mArgs;
    }

    protected int getArgPos() {
        return this.mArgPos;
    }
}
