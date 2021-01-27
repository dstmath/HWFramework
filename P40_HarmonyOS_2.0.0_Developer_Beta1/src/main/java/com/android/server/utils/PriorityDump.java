package com.android.server.utils;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public final class PriorityDump {
    public static final String PRIORITY_ARG = "--dump-priority";
    public static final String PRIORITY_ARG_CRITICAL = "CRITICAL";
    public static final String PRIORITY_ARG_HIGH = "HIGH";
    public static final String PRIORITY_ARG_NORMAL = "NORMAL";
    private static final int PRIORITY_TYPE_CRITICAL = 1;
    private static final int PRIORITY_TYPE_HIGH = 2;
    private static final int PRIORITY_TYPE_INVALID = 0;
    private static final int PRIORITY_TYPE_NORMAL = 3;
    public static final String PROTO_ARG = "--proto";

    @Retention(RetentionPolicy.SOURCE)
    private @interface PriorityType {
    }

    private PriorityDump() {
        throw new UnsupportedOperationException();
    }

    public static void dump(PriorityDumper dumper, FileDescriptor fd, PrintWriter pw, String[] args) {
        boolean asProto = false;
        int priority = 0;
        if (args == null) {
            dumper.dump(fd, pw, args, false);
            return;
        }
        String[] strippedArgs = new String[args.length];
        int strippedCount = 0;
        int argIndex = 0;
        while (argIndex < args.length) {
            if (args[argIndex].equals(PROTO_ARG)) {
                asProto = true;
            } else if (!args[argIndex].equals(PRIORITY_ARG)) {
                strippedArgs[strippedCount] = args[argIndex];
                strippedCount++;
            } else if (argIndex + 1 < args.length) {
                argIndex++;
                priority = getPriorityType(args[argIndex]);
            }
            argIndex++;
        }
        if (strippedCount < args.length) {
            strippedArgs = (String[]) Arrays.copyOf(strippedArgs, strippedCount);
        }
        if (priority == 1) {
            dumper.dumpCritical(fd, pw, strippedArgs, asProto);
        } else if (priority == 2) {
            dumper.dumpHigh(fd, pw, strippedArgs, asProto);
        } else if (priority != 3) {
            dumper.dump(fd, pw, strippedArgs, asProto);
        } else {
            dumper.dumpNormal(fd, pw, strippedArgs, asProto);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0040 A[RETURN] */
    private static int getPriorityType(String arg) {
        char c;
        int hashCode = arg.hashCode();
        if (hashCode != -1986416409) {
            if (hashCode != -1560189025) {
                if (hashCode == 2217378 && arg.equals(PRIORITY_ARG_HIGH)) {
                    c = 1;
                    if (c != 0) {
                        return 1;
                    }
                    if (c == 1) {
                        return 2;
                    }
                    if (c != 2) {
                        return 0;
                    }
                    return 3;
                }
            } else if (arg.equals(PRIORITY_ARG_CRITICAL)) {
                c = 0;
                if (c != 0) {
                }
            }
        } else if (arg.equals(PRIORITY_ARG_NORMAL)) {
            c = 2;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    public interface PriorityDumper {
        default void dumpCritical(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
        }

        default void dumpHigh(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
        }

        default void dumpNormal(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
        }

        default void dump(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
            dumpCritical(fd, pw, args, asProto);
            dumpHigh(fd, pw, args, asProto);
            dumpNormal(fd, pw, args, asProto);
        }
    }
}
