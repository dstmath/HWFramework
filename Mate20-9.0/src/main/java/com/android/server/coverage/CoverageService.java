package com.android.server.coverage;

import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.ShellCommand;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import org.jacoco.agent.rt.RT;

public class CoverageService extends Binder {
    public static final String COVERAGE_SERVICE = "coverage";
    public static final boolean ENABLED;

    private static class CoverageCommand extends ShellCommand {
        private CoverageCommand() {
        }

        public int onCommand(String cmd) {
            if ("dump".equals(cmd)) {
                return onDump();
            }
            if ("reset".equals(cmd)) {
                return onReset();
            }
            return handleDefaultCommands(cmd);
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Coverage commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("  dump [FILE]");
            pw.println("    Dump code coverage to FILE.");
            pw.println("  reset");
            pw.println("    Reset coverage information.");
        }

        private int onDump() {
            BufferedOutputStream output;
            String dest = getNextArg();
            if (dest == null) {
                dest = "/data/local/tmp/coverage.ec";
            } else {
                File f = new File(dest);
                if (f.isDirectory()) {
                    dest = new File(f, "coverage.ec").getAbsolutePath();
                }
            }
            ParcelFileDescriptor fd = openFileForSystem(dest, "w");
            if (fd == null) {
                return -1;
            }
            try {
                output = new BufferedOutputStream(new ParcelFileDescriptor.AutoCloseOutputStream(fd));
                output.write(RT.getAgent().getExecutionData(false));
                output.flush();
                getOutPrintWriter().println(String.format("Dumped coverage data to %s", new Object[]{dest}));
                output.close();
                return 0;
            } catch (IOException e) {
                PrintWriter errPrintWriter = getErrPrintWriter();
                errPrintWriter.println("Failed to dump coverage data: " + e.getMessage());
                return -1;
            } catch (Throwable th) {
                r4.addSuppressed(th);
            }
            throw th;
        }

        private int onReset() {
            RT.getAgent().reset();
            getOutPrintWriter().println("Reset coverage data");
            return 0;
        }
    }

    static {
        boolean shouldEnable = true;
        try {
            Class.forName("org.jacoco.agent.rt.RT");
        } catch (ClassNotFoundException e) {
            shouldEnable = false;
        }
        ENABLED = shouldEnable;
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new CoverageCommand().exec(this, in, out, err, args, callback, resultReceiver);
    }
}
