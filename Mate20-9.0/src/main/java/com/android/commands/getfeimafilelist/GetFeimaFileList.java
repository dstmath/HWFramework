package com.android.commands.getfeimafilelist;

import android.util.AndroidException;
import com.android.internal.os.BaseCommand;
import java.io.PrintStream;

public final class GetFeimaFileList extends BaseCommand {
    public static void main(String[] args) {
        new GetFeimaFileList().run(args);
    }

    public void onShowUsage(PrintStream out) {
        try {
            runAmCmd(new String[]{"help"});
        } catch (AndroidException e) {
            e.printStackTrace(System.err);
        }
    }

    public void onRun() throws Exception {
        runAmCmd(getRawArgs());
    }

    /* access modifiers changed from: package-private */
    public void runAmCmd(String[] args) throws AndroidException {
        if (args != null && args.length > 0) {
            StringBuffer sb = new StringBuffer();
            for (String append : args) {
                sb.append(append);
                sb.append(";");
            }
            new CfgFileListShellCommand().onCommand(sb.toString());
        }
    }
}
