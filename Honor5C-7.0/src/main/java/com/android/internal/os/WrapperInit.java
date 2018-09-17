package com.android.internal.os;

import android.os.Process;
import android.util.Slog;
import com.android.internal.os.ZygoteInit.MethodAndArgsCaller;
import dalvik.system.VMRuntime;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import libcore.io.IoUtils;

public class WrapperInit {
    private static final String TAG = "AndroidRuntime";

    private WrapperInit() {
    }

    public static void main(String[] args) {
        try {
            int fdNum = Integer.parseInt(args[0], 10);
            int targetSdkVersion = Integer.parseInt(args[1], 10);
            if (fdNum != 0) {
                try {
                    FileDescriptor fd = new FileDescriptor();
                    fd.setInt$(fdNum);
                    DataOutputStream os = new DataOutputStream(new FileOutputStream(fd));
                    os.writeInt(Process.myPid());
                    os.close();
                    IoUtils.closeQuietly(fd);
                } catch (IOException ex) {
                    Slog.d(TAG, "Could not write pid of wrapped process to Zygote pipe.", ex);
                }
            }
            ZygoteInit.preload();
            String[] runtimeArgs = new String[(args.length - 2)];
            System.arraycopy(args, 2, runtimeArgs, 0, runtimeArgs.length);
            RuntimeInit.wrapperInit(targetSdkVersion, runtimeArgs);
        } catch (MethodAndArgsCaller caller) {
            caller.run();
        }
    }

    public static void execApplication(String invokeWith, String niceName, int targetSdkVersion, String instructionSet, FileDescriptor pipeFd, String[] args) {
        String appProcess;
        StringBuilder command = new StringBuilder(invokeWith);
        if (VMRuntime.is64BitInstructionSet(instructionSet)) {
            appProcess = "/system/bin/app_process64";
        } else {
            appProcess = "/system/bin/app_process32";
        }
        command.append(' ');
        command.append(appProcess);
        command.append(" /system/bin --application");
        if (niceName != null) {
            command.append(" '--nice-name=").append(niceName).append("'");
        }
        command.append(" com.android.internal.os.WrapperInit ");
        command.append(pipeFd != null ? pipeFd.getInt$() : 0);
        command.append(' ');
        command.append(targetSdkVersion);
        Zygote.appendQuotedShellArgs(command, args);
        Zygote.execShell(command.toString());
    }
}
