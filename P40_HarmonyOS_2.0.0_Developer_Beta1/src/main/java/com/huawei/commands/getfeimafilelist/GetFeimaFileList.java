package com.huawei.commands.getfeimafilelist;

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
            StringBuffer argsBuffer = new StringBuffer();
            for (String str : args) {
                argsBuffer.append(str);
                argsBuffer.append(";");
            }
            new CfgFileListShellCommand().onCommand(argsBuffer.toString());
        }
    }
}
