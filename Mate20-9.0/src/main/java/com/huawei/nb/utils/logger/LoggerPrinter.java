package com.huawei.nb.utils.logger;

import android.text.TextUtils;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Locale;

final class LoggerPrinter implements Printer {
    private static final int BLOCK_SIZE = 4000;
    private static final String ENCODING_FORMAT = "utf-8";
    private static final int LEVEL_DEBUG = 3;
    private static final int LEVEL_ERROR = 6;
    private static final int LEVEL_INFO = 4;
    private static final int LEVEL_VERBOSE = 2;
    private static final int LEVEL_WARN = 5;
    private static final int START_STACK_OFFSET = 3;
    private String logTag;
    private final Settings loggerSettings = new Settings();
    private final Object mLock = new Object();

    LoggerPrinter() {
        init("");
    }

    public Settings init(String tag) {
        if (tag != null && tag.trim().length() > 0) {
            this.logTag = tag;
        }
        return this.loggerSettings;
    }

    public void v(String msg, Object... args) {
        print(2, (Throwable) null, msg, args);
    }

    public void d(String msg, Object... args) {
        print(3, (Throwable) null, msg, args);
    }

    public void i(String msg, Object... args) {
        print(4, (Throwable) null, msg, args);
    }

    public void w(String msg, Object... args) {
        print(5, (Throwable) null, msg, args);
    }

    public void e(String msg, Object... args) {
        e(null, msg, args);
    }

    public void e(Throwable throwable, String msg, Object... args) {
        print(6, throwable, msg, args);
    }

    private void print(int level, Throwable throwable, String msg, Object... args) {
        synchronized (this.mLock) {
            print(level, this.logTag, buildMessage(msg, args), throwable);
        }
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    private void print(int priority, String tag, String msg, Throwable throwable) {
        synchronized (this.mLock) {
            if (throwable != null) {
                if (msg == null) {
                    msg = getStackTraceString(throwable);
                } else {
                    msg = msg + " : " + getStackTraceString(throwable);
                }
            }
            if (!TextUtils.isEmpty(msg)) {
                if (!buildLogHeader().isEmpty()) {
                    msg = header + ": " + msg;
                }
                try {
                    byte[] bytes = msg.getBytes(ENCODING_FORMAT);
                    int length = bytes.length;
                    if (length <= BLOCK_SIZE) {
                        printBlock(priority, tag, msg);
                        return;
                    }
                    for (int i = 0; i < length; i += BLOCK_SIZE) {
                        printBlock(priority, tag, new String(bytes, i, length - i > BLOCK_SIZE ? BLOCK_SIZE : length - i, ENCODING_FORMAT));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Unsupported encoding format utf-8");
                }
            }
        }
    }

    private void printBlock(int level, String tag, String message) {
        LogAdapter logAdapter = this.loggerSettings.getLogAdapter();
        if (logAdapter != null) {
            switch (level) {
                case 2:
                    logAdapter.v(tag, message);
                    return;
                case 4:
                    logAdapter.i(tag, message);
                    return;
                case 5:
                    logAdapter.w(tag, message);
                    return;
                case 6:
                    logAdapter.e(tag, message);
                    return;
                default:
                    logAdapter.d(tag, message);
                    return;
            }
        }
    }

    private String buildLogHeader() {
        StringBuilder header = new StringBuilder();
        if (this.loggerSettings.isShowThreadInfo()) {
            header.append("[").append(Thread.currentThread().getName()).append("]");
        }
        if (this.loggerSettings.isShowMethodInfo() || this.loggerSettings.isShowLineNumber()) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            int stackOffset = getStackOffset(trace) + this.loggerSettings.getMethodOffset();
            if (stackOffset > 0 && stackOffset < trace.length) {
                if (this.loggerSettings.isShowMethodInfo()) {
                    if (header.length() != 0) {
                        header.append(" ");
                    }
                    header.append(getSimpleClassName(trace[stackOffset].getClassName())).append(".").append(trace[stackOffset].getMethodName());
                }
                if (this.loggerSettings.isShowLineNumber()) {
                    if (header.length() != 0) {
                        header.append(" ");
                    }
                    header.append(" (").append(trace[stackOffset].getFileName()).append(":").append(trace[stackOffset].getLineNumber()).append(")");
                }
            }
        }
        return header.toString();
    }

    private String buildMessage(String msg, Object... args) {
        return (args == null || args.length == 0) ? msg : String.format(Locale.US, msg, args);
    }

    private String getSimpleClassName(String className) {
        return className.substring(className.lastIndexOf(".") + 1);
    }

    private int getStackOffset(StackTraceElement[] trace) {
        for (int i = 3; i < trace.length; i++) {
            String className = trace[i].getClassName();
            if (!className.equals(LoggerPrinter.class.getName()) && !className.equals(AndroidLogger.class.getName())) {
                return i;
            }
        }
        return -1;
    }

    private String getStackTraceString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        for (Throwable trw = throwable; trw != null; trw = trw.getCause()) {
            if (trw instanceof UnknownHostException) {
                return "";
            }
        }
        StackTraceElement[] traces = throwable.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(throwable.getMessage());
        for (StackTraceElement traceElement : traces) {
            sb.append("\n").append(traceElement.toString());
        }
        return sb.toString();
    }
}
