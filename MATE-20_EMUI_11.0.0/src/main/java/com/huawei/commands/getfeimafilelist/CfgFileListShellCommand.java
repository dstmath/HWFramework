package com.huawei.commands.getfeimafilelist;

import android.os.RemoteException;
import android.os.ShellCommand;
import android.text.TextUtils;
import android.util.Log;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.util.ArrayList;

final class CfgFileListShellCommand extends ShellCommand {
    private static final int CUST_TYPE_CONFIG = 0;
    private static final String TAG = "CfgFileListShellCommand";

    CfgFileListShellCommand() {
    }

    public int onCommand(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            System.out.println("cmd is null.");
            return -1;
        }
        String[] args = cmd.split(";");
        if (args != null && args.length > 0) {
            boolean z = false;
            try {
                String str = args[CUST_TYPE_CONFIG];
                if (str.hashCode() != 3198785 || !str.equals("help")) {
                    z = true;
                }
                if (z) {
                    return getFeimaFileList(args);
                }
                System.out.println("Get file list from the Feima cfg path which exists the given file name by slot Id.");
                System.out.println("getFeimaFileList [file name] [type] [slotId]");
                System.out.println("[file name] like 'prop/local.prop'");
                System.out.println("[type] like 0 or 1. 0 == CUST_TYPE_CONFIG ,1 == CUST_TYPE_MEDIA. If type is null, type == 0");
                System.out.println("[slotId] like 0 or 1. If slotId is null, slotId == 0");
            } catch (RemoteException e) {
                System.out.println("Remote exception: " + e);
            }
        }
        return -1;
    }

    private int getFeimaFileList(String[] args) throws RemoteException {
        ArrayList<File> cfgFileList = null;
        String type = null;
        String slotId = null;
        if (args.length >= 3) {
            type = args[1];
            slotId = args[2];
        } else if (args.length >= 2) {
            type = args[1];
        }
        if (type == null) {
            try {
                cfgFileList = HwCfgFilePolicy.getCfgFileList(args[CUST_TYPE_CONFIG], (int) CUST_TYPE_CONFIG);
            } catch (NoClassDefFoundError e) {
                Log.e(TAG, "class HwCfgFilePolicy not found error");
            } catch (Exception e2) {
                Log.e(TAG, "class HwCfgFilePolicy exception");
            }
        } else {
            cfgFileList = slotId == null ? HwCfgFilePolicy.getCfgFileList(args[CUST_TYPE_CONFIG], Integer.parseInt(type)) : HwCfgFilePolicy.getCfgFileList(args[CUST_TYPE_CONFIG], Integer.parseInt(type), Integer.parseInt(slotId));
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
        return CUST_TYPE_CONFIG;
    }

    public void onHelp() {
    }
}
