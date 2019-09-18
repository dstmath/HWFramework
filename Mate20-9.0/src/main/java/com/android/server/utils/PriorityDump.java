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

    public interface PriorityDumper {
        void dumpCritical(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
        }

        void dumpHigh(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
        }

        void dumpNormal(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
        }

        void dump(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
            dumpCritical(fd, pw, args, asProto);
            dumpHigh(fd, pw, args, asProto);
            dumpNormal(fd, pw, args, asProto);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface PriorityType {
    }

    private PriorityDump() {
        throw new UnsupportedOperationException();
    }

    /* JADX WARNING: type inference failed for: r4v3, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
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
            strippedArgs = Arrays.copyOf(strippedArgs, strippedCount);
        }
        switch (priority) {
            case 1:
                dumper.dumpCritical(fd, pw, strippedArgs, asProto);
                return;
            case 2:
                dumper.dumpHigh(fd, pw, strippedArgs, asProto);
                return;
            case 3:
                dumper.dumpNormal(fd, pw, strippedArgs, asProto);
                return;
            default:
                dumper.dump(fd, pw, strippedArgs, asProto);
                return;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0039 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003c A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003d A[RETURN] */
    private static int getPriorityType(String arg) {
        char c;
        int hashCode = arg.hashCode();
        if (hashCode == -1986416409) {
            if (arg.equals(PRIORITY_ARG_NORMAL)) {
                c = 2;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == -1560189025) {
            if (arg.equals(PRIORITY_ARG_CRITICAL)) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 2217378 && arg.equals(PRIORITY_ARG_HIGH)) {
            c = 1;
            switch (c) {
                case 0:
                    return 1;
                case 1:
                    return 2;
                case 2:
                    return 3;
                default:
                    return 0;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }
}
