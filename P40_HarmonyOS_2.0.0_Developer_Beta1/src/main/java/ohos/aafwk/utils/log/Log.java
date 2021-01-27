package ohos.aafwk.utils.log;

import java.io.PrintWriter;
import java.util.Objects;
import ohos.hiviewdfx.HiLogConstString;

public final class Log {
    public static final int DEBUG = 3;
    private static final String DUMP_PREFIX = "    ";
    public static final int ERROR = 6;
    public static final int FATAL = 7;
    private static final String GET_LOG_LEVEL = "-gll";
    public static final int INFO = 4;
    private static final String LABEL_ISNULL_LOG = "label is null";
    public static final int LOG_CORE = 3;
    public static final int LOG_INIT = 1;
    private static final String SET_LOG_LEVEL = "-sll";
    public static final int WARN = 5;
    private static int level = 3;
    private static ILogger logger = ZLogger.INSTANCE;

    private static boolean checkLevel(int i) {
        return i >= 3 && i <= 7;
    }

    public static String getDumpPrefix() {
        return "    ";
    }

    private Log() {
    }

    public static int debug(@HiLogConstString String str, Object... objArr) {
        if (level <= 3) {
            return logger.debug(LogLabel.LABEL_DEF, str, objArr);
        }
        return 0;
    }

    public static int debug(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        if (level <= 3) {
            return logger.debug(logLabel, str, objArr);
        }
        return 0;
    }

    public static int info(@HiLogConstString String str, Object... objArr) {
        if (level <= 4) {
            return logger.info(LogLabel.LABEL_DEF, str, objArr);
        }
        return 0;
    }

    public static int info(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        if (level <= 4) {
            return logger.info(logLabel, str, objArr);
        }
        return 0;
    }

    public static int warn(@HiLogConstString String str, Object... objArr) {
        if (level <= 5) {
            return logger.warn(LogLabel.LABEL_DEF, str, objArr);
        }
        return 0;
    }

    public static int warn(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        if (level <= 5) {
            return logger.warn(logLabel, str, objArr);
        }
        return 0;
    }

    public static int error(@HiLogConstString String str, Object... objArr) {
        if (level <= 6) {
            return logger.error(LogLabel.LABEL_DEF, str, objArr);
        }
        return 0;
    }

    public static int error(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        if (level <= 6) {
            return logger.error(logLabel, str, objArr);
        }
        return 0;
    }

    public static int fatal(@HiLogConstString String str, Object... objArr) {
        if (level <= 7) {
            return logger.fatal(LogLabel.LABEL_DEF, str, objArr);
        }
        return 0;
    }

    public static int fatal(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        if (level <= 7) {
            return logger.fatal(logLabel, str, objArr);
        }
        return 0;
    }

    public static boolean isDebuggable() {
        return logger.isDebuggable();
    }

    public static boolean isLoggable(LogLabel logLabel, int i) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        return logger.isLoggable(logLabel.getDomain(), logLabel.getTag(), i);
    }

    public static boolean setLevel(int i) {
        if (!checkLevel(i)) {
            return false;
        }
        level = i;
        return true;
    }

    public static int getLevel() {
        return level;
    }

    private static void setLevel(String str, PrintWriter printWriter, int i) {
        String str2;
        if (!setLevel(i)) {
            str2 = "Error: set aafwk log level must in [3-7]. input level is: " + i;
        } else {
            str2 = "Set log level success. current level set to " + i;
        }
        printWriter.println(str + str2);
    }

    private static void dumpLevel(String str, PrintWriter printWriter) {
        if (printWriter != null) {
            printWriter.println(str + ("Current Aafwk log level is " + getLevel()));
        }
    }

    public static boolean isKnownDumpCmdOpt(String str) {
        return GET_LOG_LEVEL.equals(str) || SET_LOG_LEVEL.equals(str);
    }

    public static void dumpHelp(String str, PrintWriter printWriter) {
        if (str != null && printWriter != null) {
            printWriter.println(str + "[-gll]                get aafwk kit subsystem Log Level(lv)");
            printWriter.println(str + "[-sll <lv>]           set aafwk kit subsystem Log Level(lv). lv mapping as below: ");
            printWriter.println(str + "                          3 --- DEBUG");
            printWriter.println(str + "                          4 --- INFO");
            printWriter.println(str + "                          5 --- WARN");
            printWriter.println(str + "                          6 --- ERROR");
            printWriter.println(str + "                          7 --- FATAL");
        }
    }

    public static void dump(String str, PrintWriter printWriter, String[] strArr) {
        if (!(str == null || printWriter == null || strArr == null || strArr.length <= 0)) {
            if (strArr.length != 1 || !"-ability".equals(strArr[0])) {
                for (int i = 1; i < strArr.length; i++) {
                    String str2 = strArr[i];
                    if (str2.length() > 0) {
                        if (GET_LOG_LEVEL.equals(str2)) {
                            dumpLevel(str, printWriter);
                            return;
                        } else if (SET_LOG_LEVEL.equals(str2)) {
                            int i2 = i + 1;
                            try {
                                setLevel(str, printWriter, Integer.parseInt(strArr[i2]));
                                return;
                            } catch (NumberFormatException unused) {
                                printWriter.print("Error: set aafwk log level must in digital format. input is: ");
                                printWriter.println(strArr[i2]);
                                return;
                            } catch (IndexOutOfBoundsException unused2) {
                                printWriter.println("Error: set aafwk log level missing level number in range [3-7].");
                                return;
                            }
                        } else {
                            printWriter.println("arg: [" + str2 + "] is not support, use -h for help");
                            return;
                        }
                    }
                }
                return;
            }
            dumpLevel(str, printWriter);
        }
    }
}
