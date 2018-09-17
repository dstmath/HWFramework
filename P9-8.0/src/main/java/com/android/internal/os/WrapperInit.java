package com.android.internal.os;

import android.os.Process;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructCapUserData;
import android.system.StructCapUserHeader;
import android.util.BootTimingsTraceLog;
import android.util.Slog;
import com.android.internal.os.Zygote.MethodAndArgsCaller;
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
            ZygoteInit.preload(new BootTimingsTraceLog("WrapperInitTiming", 16384));
            String[] runtimeArgs = new String[(args.length - 2)];
            System.arraycopy(args, 2, runtimeArgs, 0, runtimeArgs.length);
            wrapperInit(targetSdkVersion, runtimeArgs);
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
        preserveCapabilities();
        Zygote.execShell(command.toString());
    }

    private static void wrapperInit(int targetSdkVersion, String[] argv) throws MethodAndArgsCaller {
        ClassLoader classLoader = null;
        if (argv != null && argv.length > 2 && argv[0].equals("-cp")) {
            classLoader = ZygoteInit.createPathClassLoader(argv[1], targetSdkVersion);
            Thread.currentThread().setContextClassLoader(classLoader);
            String[] removedArgs = new String[(argv.length - 2)];
            System.arraycopy(argv, 2, removedArgs, 0, argv.length - 2);
            argv = removedArgs;
        }
        RuntimeInit.applicationInit(targetSdkVersion, argv, classLoader);
    }

    private static void preserveCapabilities() {
        StructCapUserHeader header = new StructCapUserHeader(OsConstants._LINUX_CAPABILITY_VERSION_3, 0);
        try {
            StructCapUserData[] data = Os.capget(header);
            if (!(data[0].permitted == data[0].inheritable && data[1].permitted == data[1].inheritable)) {
                data[0] = new StructCapUserData(data[0].effective, data[0].permitted, data[0].permitted);
                data[1] = new StructCapUserData(data[1].effective, data[1].permitted, data[1].permitted);
                try {
                    Os.capset(header, data);
                } catch (ErrnoException e) {
                    Slog.e(TAG, "RuntimeInit: Failed capset", e);
                    return;
                }
            }
            for (int i = 0; i < 64; i++) {
                int dataIndex = OsConstants.CAP_TO_INDEX(i);
                if ((data[dataIndex].inheritable & OsConstants.CAP_TO_MASK(i)) != 0) {
                    try {
                        Os.prctl(OsConstants.PR_CAP_AMBIENT, (long) OsConstants.PR_CAP_AMBIENT_RAISE, (long) i, 0, 0);
                    } catch (ErrnoException ex) {
                        Slog.e(TAG, "RuntimeInit: Failed to raise ambient capability " + i, ex);
                    }
                }
            }
        } catch (ErrnoException e2) {
            Slog.e(TAG, "RuntimeInit: Failed capget", e2);
        }
    }
}
