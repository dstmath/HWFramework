package com.android.internal.os;

import android.os.ShellCommand;
import java.io.PrintStream;

public abstract class BaseCommand {
    public static final String FATAL_ERROR_CODE = "Error type 1";
    public static final String NO_CLASS_ERROR_CODE = "Error type 3";
    public static final String NO_SYSTEM_ERROR_CODE = "Error type 2";
    protected final ShellCommand mArgs = new ShellCommand() {
        public int onCommand(String cmd) {
            return 0;
        }

        public void onHelp() {
        }
    };
    private String[] mRawArgs;

    public abstract void onRun() throws Exception;

    public abstract void onShowUsage(PrintStream printStream);

    public void run(String[] args) {
        if (args.length < 1) {
            onShowUsage(System.out);
            return;
        }
        this.mRawArgs = args;
        this.mArgs.init(null, null, null, null, args, null, 0);
        try {
            onRun();
        } catch (IllegalArgumentException e) {
            onShowUsage(System.err);
            System.err.println();
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e2) {
            e2.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public void showUsage() {
        onShowUsage(System.err);
    }

    public void showError(String message) {
        onShowUsage(System.err);
        System.err.println();
        System.err.println(message);
    }

    public String nextOption() {
        return this.mArgs.getNextOption();
    }

    public String nextArg() {
        return this.mArgs.getNextArg();
    }

    public String nextArgRequired() {
        return this.mArgs.getNextArgRequired();
    }

    public String[] getRawArgs() {
        return this.mRawArgs;
    }
}
