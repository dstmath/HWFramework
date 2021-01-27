package com.huawei.nb.searchmanager.utils.logger;

import com.huawei.nb.searchmanager.utils.TextUtils;
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

    @Override // com.huawei.nb.searchmanager.utils.logger.Printer
    public Settings init(String str) {
        if (str != null && str.trim().length() > 0) {
            this.logTag = str;
        }
        return this.loggerSettings;
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.Printer
    public void v(String str, Object... objArr) {
        print(2, (Throwable) null, str, objArr);
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.Printer
    public void d(String str, Object... objArr) {
        print(3, (Throwable) null, str, objArr);
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.Printer
    public void i(String str, Object... objArr) {
        print(4, (Throwable) null, str, objArr);
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.Printer
    public void w(String str, Object... objArr) {
        print(5, (Throwable) null, str, objArr);
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.Printer
    public void e(String str, Object... objArr) {
        e(null, str, objArr);
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.Printer
    public void e(Throwable th, String str, Object... objArr) {
        print(6, th, str, objArr);
    }

    private void print(int i, Throwable th, String str, Object... objArr) {
        synchronized (this.mLock) {
            print(i, this.logTag, buildMessage(str, objArr), th);
        }
    }

    private void print(int i, String str, String str2, Throwable th) {
        String str3;
        synchronized (this.mLock) {
            if (th == null) {
                str3 = str2;
            } else if (str2 == null) {
                str3 = getStackTraceString(th);
            } else {
                str3 = str2 + " : " + getStackTraceString(th);
            }
            if (!TextUtils.isEmpty(str3)) {
                String buildLogHeader = buildLogHeader();
                if (!buildLogHeader.isEmpty()) {
                    str3 = buildLogHeader + ": " + str2;
                }
                try {
                    byte[] bytes = str3.getBytes(ENCODING_FORMAT);
                    int length = bytes.length;
                    if (length <= BLOCK_SIZE) {
                        printBlock(i, str, str3);
                        return;
                    }
                    for (int i2 = 0; i2 < length; i2 += BLOCK_SIZE) {
                        int i3 = length - i2;
                        if (i3 > BLOCK_SIZE) {
                            i3 = BLOCK_SIZE;
                        }
                        printBlock(i, str, new String(bytes, i2, i3, ENCODING_FORMAT));
                    }
                } catch (IOException unused) {
                }
            }
        }
    }

    private void printBlock(int i, String str, String str2) {
        LogAdapter logAdapter = this.loggerSettings.getLogAdapter();
        if (logAdapter == null) {
            return;
        }
        if (i == 2) {
            logAdapter.v(str, str2);
        } else if (i == 4) {
            logAdapter.i(str, str2);
        } else if (i == 5) {
            logAdapter.w(str, str2);
        } else if (i != 6) {
            logAdapter.d(str, str2);
        } else {
            logAdapter.e(str, str2);
        }
    }

    private String buildLogHeader() {
        StringBuilder sb = new StringBuilder();
        if (this.loggerSettings.isShowThreadInfo()) {
            sb.append("[");
            sb.append(Thread.currentThread().getName());
            sb.append("]");
        }
        if (!this.loggerSettings.isShowMethodInfo() && !this.loggerSettings.isShowLineNumber()) {
            return sb.toString();
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int stackOffset = getStackOffset(stackTrace) + this.loggerSettings.getMethodOffset();
        if (stackOffset > 0 && stackOffset < stackTrace.length) {
            if (this.loggerSettings.isShowMethodInfo()) {
                if (sb.length() != 0) {
                    sb.append(" ");
                }
                sb.append(getSimpleClassName(stackTrace[stackOffset].getClassName()));
                sb.append(".");
                sb.append(stackTrace[stackOffset].getMethodName());
            }
            if (this.loggerSettings.isShowLineNumber()) {
                if (sb.length() != 0) {
                    sb.append(" ");
                }
                sb.append(" (");
                sb.append(stackTrace[stackOffset].getFileName());
                sb.append(":");
                sb.append(stackTrace[stackOffset].getLineNumber());
                sb.append(")");
            }
        }
        return sb.toString();
    }

    private String buildMessage(String str, Object... objArr) {
        return (objArr == null || objArr.length == 0) ? str : String.format(Locale.ENGLISH, str, objArr);
    }

    private String getSimpleClassName(String str) {
        return str.substring(str.lastIndexOf(".") + 1);
    }

    private int getStackOffset(StackTraceElement[] stackTraceElementArr) {
        for (int i = 3; i < stackTraceElementArr.length; i++) {
            String className = stackTraceElementArr[i].getClassName();
            if (!(className.equals(LoggerPrinter.class.getName()) || className.equals(AndroidLogger.class.getName()))) {
                return i;
            }
        }
        return -1;
    }

    private String getStackTraceString(Throwable th) {
        if (th == null) {
            return "";
        }
        for (Throwable th2 = th; th2 != null; th2 = th2.getCause()) {
            if (th2 instanceof UnknownHostException) {
                return "";
            }
        }
        StackTraceElement[] stackTrace = th.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append(th.getMessage());
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(System.lineSeparator());
            sb.append(stackTraceElement.toString());
        }
        return sb.toString();
    }
}
