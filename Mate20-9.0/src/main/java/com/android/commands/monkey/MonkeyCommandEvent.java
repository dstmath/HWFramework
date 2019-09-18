package com.android.commands.monkey;

import android.app.IActivityManager;
import android.view.IWindowManager;

public class MonkeyCommandEvent extends MonkeyEvent {
    private String mCmd;

    public MonkeyCommandEvent(String cmd) {
        super(4);
        this.mCmd = cmd;
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (this.mCmd != null) {
            try {
                int status = Runtime.getRuntime().exec(this.mCmd).waitFor();
                Logger logger = Logger.err;
                logger.println("// Shell command " + this.mCmd + " status was " + status);
            } catch (Exception e) {
                Logger logger2 = Logger.err;
                logger2.println("// Exception from " + this.mCmd + ":");
                Logger.err.println(e.toString());
            }
        }
        return 1;
    }
}
