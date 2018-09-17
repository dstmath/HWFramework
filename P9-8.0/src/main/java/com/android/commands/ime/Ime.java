package com.android.commands.ime;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.PrintStreamPrinter;
import android.util.Printer;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodManager.Stub;
import java.util.List;

public final class Ime {
    private static final String IMM_NOT_RUNNING_ERR = "Error: Could not access the Input Method Manager.  Is the system running?";
    private String[] mArgs;
    private String mCurArgData;
    IInputMethodManager mImm;
    private int mNextArg;

    public static void main(String[] args) {
        new Ime().run(args);
    }

    public void run(String[] args) {
        if (args.length < 1) {
            showUsage();
            return;
        }
        this.mImm = Stub.asInterface(ServiceManager.getService("input_method"));
        if (this.mImm == null) {
            System.err.println(IMM_NOT_RUNNING_ERR);
            return;
        }
        this.mArgs = args;
        String op = args[0];
        this.mNextArg = 1;
        if ("list".equals(op)) {
            runList();
        } else if ("enable".equals(op)) {
            runSetEnabled(true);
        } else if ("disable".equals(op)) {
            runSetEnabled(false);
        } else if ("set".equals(op)) {
            runSet();
        } else {
            if (op != null) {
                System.err.println("Error: unknown command '" + op + "'");
            }
            showUsage();
        }
    }

    private void runList() {
        boolean all = false;
        boolean brief = false;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                List<InputMethodInfo> methods;
                if (all) {
                    try {
                        methods = this.mImm.getInputMethodList();
                    } catch (RemoteException e) {
                        System.err.println(e.toString());
                        System.err.println(IMM_NOT_RUNNING_ERR);
                        return;
                    }
                }
                try {
                    methods = this.mImm.getEnabledInputMethodList();
                } catch (RemoteException e2) {
                    System.err.println(e2.toString());
                    System.err.println(IMM_NOT_RUNNING_ERR);
                    return;
                }
                if (methods != null) {
                    Printer pr = new PrintStreamPrinter(System.out);
                    for (int i = 0; i < methods.size(); i++) {
                        InputMethodInfo imi = (InputMethodInfo) methods.get(i);
                        if (brief) {
                            System.out.println(imi.getId());
                        } else {
                            System.out.println(imi.getId() + ":");
                            imi.dump(pr, "  ");
                        }
                    }
                }
                return;
            } else if (opt.equals("-a")) {
                all = true;
            } else if (opt.equals("-s")) {
                brief = true;
            } else {
                System.err.println("Error: Unknown option: " + opt);
                showUsage();
                return;
            }
        }
    }

    private void runSetEnabled(boolean state) {
        String id = nextArg();
        if (id == null) {
            System.err.println("Error: no input method ID specified");
            showUsage();
            return;
        }
        try {
            boolean res = this.mImm.setInputMethodEnabled(id, state);
            if (state) {
                System.out.println("Input method " + id + ": " + (res ? "already enabled" : "now enabled"));
            } else {
                System.out.println("Input method " + id + ": " + (res ? "now disabled" : "already disabled"));
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (RemoteException e2) {
            System.err.println(e2.toString());
            System.err.println(IMM_NOT_RUNNING_ERR);
        }
    }

    private void runSet() {
        String id = nextArg();
        if (id == null) {
            System.err.println("Error: no input method ID specified");
            showUsage();
            return;
        }
        try {
            this.mImm.setInputMethod(null, id);
            System.out.println("Input method " + id + " selected");
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (RemoteException e2) {
            System.err.println(e2.toString());
            System.err.println(IMM_NOT_RUNNING_ERR);
        }
    }

    private String nextOption() {
        if (this.mNextArg >= this.mArgs.length) {
            return null;
        }
        String arg = this.mArgs[this.mNextArg];
        if (!arg.startsWith("-")) {
            return null;
        }
        this.mNextArg++;
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

    private String nextOptionData() {
        if (this.mCurArgData != null) {
            return this.mCurArgData;
        }
        if (this.mNextArg >= this.mArgs.length) {
            return null;
        }
        String data = this.mArgs[this.mNextArg];
        this.mNextArg++;
        return data;
    }

    private String nextArg() {
        if (this.mNextArg >= this.mArgs.length) {
            return null;
        }
        String arg = this.mArgs[this.mNextArg];
        this.mNextArg++;
        return arg;
    }

    private static void showUsage() {
        System.err.println("usage: ime list [-a] [-s]");
        System.err.println("       ime enable ID");
        System.err.println("       ime disable ID");
        System.err.println("       ime set ID");
        System.err.println("");
        System.err.println("The list command prints all enabled input methods.  Use");
        System.err.println("the -a option to see all input methods.  Use");
        System.err.println("the -s option to see only a single summary line of each.");
        System.err.println("");
        System.err.println("The enable command allows the given input method ID to be used.");
        System.err.println("");
        System.err.println("The disable command disallows the given input method ID from use.");
        System.err.println("");
        System.err.println("The set command switches to the given input method ID.");
    }
}
