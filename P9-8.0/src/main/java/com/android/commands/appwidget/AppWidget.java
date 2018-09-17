package com.android.commands.appwidget;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import com.android.internal.appwidget.IAppWidgetService.Stub;

public class AppWidget {
    private static final String USAGE = "usage: adb shell appwidget [subcommand] [options]\n\nusage: adb shell appwidget grantbind --package <PACKAGE>  [--user <USER_ID> | current]\n  <PACKAGE> an Android package name.\n  <USER_ID> The user id under which the package is installed.\n  Example:\n  # Grant the \"foo.bar.baz\" package to bind app widgets for the current user.\n  adb shell grantbind --package foo.bar.baz --user current\n\nusage: adb shell appwidget revokebind --package <PACKAGE> [--user <USER_ID> | current]\n  <PACKAGE> an Android package name.\n  <USER_ID> The user id under which the package is installed.\n  Example:\n  # Revoke the permisison to bind app widgets from the \"foo.bar.baz\" package.\n  adb shell revokebind --package foo.bar.baz --user current\n\n";

    private static class Parser {
        private static final String ARGUMENT_GRANT_BIND = "grantbind";
        private static final String ARGUMENT_PACKAGE = "--package";
        private static final String ARGUMENT_PREFIX = "--";
        private static final String ARGUMENT_REVOKE_BIND = "revokebind";
        private static final String ARGUMENT_USER = "--user";
        private static final String VALUE_USER_CURRENT = "current";
        private final Tokenizer mTokenizer;

        public Parser(String[] args) {
            this.mTokenizer = new Tokenizer(args);
        }

        public Runnable parseCommand() {
            try {
                String operation = this.mTokenizer.nextArg();
                if (ARGUMENT_GRANT_BIND.equals(operation)) {
                    return parseSetGrantBindAppWidgetPermissionCommand(true);
                }
                if (ARGUMENT_REVOKE_BIND.equals(operation)) {
                    return parseSetGrantBindAppWidgetPermissionCommand(false);
                }
                throw new IllegalArgumentException("Unsupported operation: " + operation);
            } catch (IllegalArgumentException iae) {
                System.out.println(AppWidget.USAGE);
                System.out.println("[ERROR] " + iae.getMessage());
                return null;
            }
        }

        private SetBindAppWidgetPermissionCommand parseSetGrantBindAppWidgetPermissionCommand(boolean granted) {
            String packageName = null;
            int userId = 0;
            while (true) {
                String argument = this.mTokenizer.nextArg();
                if (argument != null) {
                    if (ARGUMENT_PACKAGE.equals(argument)) {
                        packageName = argumentValueRequired(argument);
                    } else if (ARGUMENT_USER.equals(argument)) {
                        String user = argumentValueRequired(argument);
                        if (VALUE_USER_CURRENT.equals(user)) {
                            userId = -2;
                        } else {
                            userId = Integer.parseInt(user);
                        }
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + argument);
                    }
                } else if (packageName != null) {
                    return new SetBindAppWidgetPermissionCommand(packageName, granted, userId);
                } else {
                    throw new IllegalArgumentException("Package name not specified. Did you specify --package argument?");
                }
            }
        }

        private String argumentValueRequired(String argument) {
            String value = this.mTokenizer.nextArg();
            if (!TextUtils.isEmpty(value) && !value.startsWith(ARGUMENT_PREFIX)) {
                return value;
            }
            throw new IllegalArgumentException("No value for argument: " + argument);
        }
    }

    private static class SetBindAppWidgetPermissionCommand implements Runnable {
        final boolean mGranted;
        final String mPackageName;
        final int mUserId;

        public SetBindAppWidgetPermissionCommand(String packageName, boolean granted, int userId) {
            this.mPackageName = packageName;
            this.mGranted = granted;
            this.mUserId = userId;
        }

        public void run() {
            try {
                Stub.asInterface(ServiceManager.getService("appwidget")).setBindAppWidgetPermission(this.mPackageName, this.mUserId, this.mGranted);
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }
    }

    private static class Tokenizer {
        private final String[] mArgs;
        private int mNextArg;

        public Tokenizer(String[] args) {
            this.mArgs = args;
        }

        private String nextArg() {
            if (this.mNextArg >= this.mArgs.length) {
                return null;
            }
            String[] strArr = this.mArgs;
            int i = this.mNextArg;
            this.mNextArg = i + 1;
            return strArr[i];
        }
    }

    public static void main(String[] args) {
        Runnable command = new Parser(args).parseCommand();
        if (command != null) {
            command.run();
        }
    }
}
