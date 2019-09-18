package com.android.commands.getfeimafilelist;

import android.os.RemoteException;
import android.os.ShellCommand;
import android.util.Log;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.util.ArrayList;

final class CfgFileListShellCommand extends ShellCommand {
    private static final String TAG = "CfgFileListShellCommand";

    CfgFileListShellCommand() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x002a A[Catch:{ RemoteException -> 0x0054 }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x002f A[Catch:{ RemoteException -> 0x0054 }] */
    public int onCommand(String cmd) {
        if (cmd == null) {
            System.out.println("cmd is null.");
            return -1;
        }
        String[] args = cmd.split(";");
        boolean z = false;
        try {
            String str = args[0];
            if (str.hashCode() == 3198785) {
                if (str.equals("help")) {
                    if (!z) {
                        return getFeimaFileList(args);
                    }
                    System.out.println("get file list from the Feima cfg path which exists the given file name by slot Id.");
                    System.out.println("getFeimaFileList [file name] [type] [slotId]");
                    System.out.println("[file name] like 'prop/local.prop'");
                    System.out.println("[type] like 0 or 1. 0 == CUST_TYPE_CONFIG ,1 == CUST_TYPE_MEDIA. If type is null, type == 0");
                    System.out.println("[slotId] like 0 or 1. If slotId is null, slotId == 0");
                    return -1;
                }
            }
            z = true;
            if (!z) {
            }
        } catch (RemoteException e) {
            System.out.println("Remote exception: " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public int getFeimaFileList(String[] args) throws RemoteException {
        ArrayList<File> cfgFileList = null;
        String type = null;
        String slotId = null;
        try {
            if (args.length >= 3) {
                type = args[1];
                slotId = args[2];
            } else if (args.length >= 2) {
                type = args[1];
            }
            if (type == null) {
                try {
                    cfgFileList = HwCfgFilePolicy.getCfgFileList(args[0], 0);
                } catch (NoClassDefFoundError e) {
                    Log.e(TAG, "class HwCfgFilePolicy not found error");
                }
            } else {
                cfgFileList = slotId == null ? HwCfgFilePolicy.getCfgFileList(args[0], Integer.parseInt(type)) : HwCfgFilePolicy.getCfgFileList(args[0], Integer.parseInt(type), Integer.parseInt(slotId));
            }
        } catch (Exception e2) {
        }
        if (cfgFileList == null) {
            System.out.println("Not found file.");
            return -1;
        }
        if (cfgFileList.size() >= 1) {
            for (int i = cfgFileList.size() - 1; i >= 0; i--) {
                System.out.println(cfgFileList.get(i).getAbsolutePath());
            }
        }
        return 0;
    }

    public void onHelp() {
    }
}
