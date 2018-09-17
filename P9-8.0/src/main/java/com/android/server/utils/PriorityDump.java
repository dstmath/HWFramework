package com.android.server.utils;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class PriorityDump {
    public static final String PRIORITY_ARG = "--dump_priority";

    public interface PriorityDumper {
        void dumpCritical(FileDescriptor fd, PrintWriter pw, String[] args) {
        }

        void dumpHigh(FileDescriptor fd, PrintWriter pw, String[] args) {
        }

        void dumpNormal(FileDescriptor fd, PrintWriter pw, String[] args) {
        }

        void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            dumpCritical(fd, pw, args);
            dumpHigh(fd, pw, args);
            dumpNormal(fd, pw, args);
        }
    }

    private PriorityDump() {
        throw new UnsupportedOperationException();
    }

    public static void dump(PriorityDumper dumper, FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args != null && args.length >= 2 && args[0].equals(PRIORITY_ARG)) {
            String priority = args[1];
            if (priority.equals("CRITICAL")) {
                dumper.dumpCritical(fd, pw, getStrippedArgs(args));
                return;
            } else if (priority.equals("HIGH")) {
                dumper.dumpHigh(fd, pw, getStrippedArgs(args));
                return;
            } else if (priority.equals("NORMAL")) {
                dumper.dumpNormal(fd, pw, getStrippedArgs(args));
                return;
            }
        }
        dumper.dump(fd, pw, args);
    }

    private static String[] getStrippedArgs(String[] args) {
        String[] stripped = new String[(args.length - 2)];
        System.arraycopy(args, 2, stripped, 0, stripped.length);
        return stripped;
    }
}
