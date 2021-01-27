package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.DumpUtils;
import com.android.server.utils.PriorityDump;
import com.android.timezone.distro.DistroException;
import com.android.timezone.distro.DistroVersion;
import com.android.timezone.distro.FileUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import libcore.timezone.TimeZoneDataFiles;
import libcore.util.CoreLibraryDebug;
import libcore.util.DebugInfo;

public class RuntimeService extends Binder {
    private static final String TAG = "RuntimeService";
    private final Context mContext;

    public RuntimeService(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    @Override // android.os.Binder
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw)) {
            boolean protoFormat = hasOption(args, PriorityDump.PROTO_ARG);
            ProtoOutputStream proto = null;
            DebugInfo coreLibraryDebugInfo = CoreLibraryDebug.getDebugInfo();
            addTimeZoneApkDebugInfo(coreLibraryDebugInfo);
            if (protoFormat) {
                proto = new ProtoOutputStream(fd);
                reportTimeZoneInfoProto(coreLibraryDebugInfo, proto);
            } else {
                reportTimeZoneInfo(coreLibraryDebugInfo, pw);
            }
            if (protoFormat) {
                proto.flush();
            }
        }
    }

    private static boolean hasOption(String[] args, String arg) {
        for (String opt : args) {
            if (arg.equals(opt)) {
                return true;
            }
        }
        return false;
    }

    private static void addTimeZoneApkDebugInfo(DebugInfo coreLibraryDebugInfo) {
        addDistroVersionDebugInfo(TimeZoneDataFiles.getDataTimeZoneFile("distro_version"), "core_library.timezone.source.data_", coreLibraryDebugInfo);
    }

    private static void reportTimeZoneInfo(DebugInfo coreLibraryDebugInfo, PrintWriter pw) {
        pw.println("Core Library Debug Info: ");
        for (DebugInfo.DebugEntry debugEntry : coreLibraryDebugInfo.getDebugEntries()) {
            pw.print(debugEntry.getKey());
            pw.print(": \"");
            pw.print(debugEntry.getStringValue());
            pw.println("\"");
        }
    }

    private static void reportTimeZoneInfoProto(DebugInfo coreLibraryDebugInfo, ProtoOutputStream protoStream) {
        for (DebugInfo.DebugEntry debugEntry : coreLibraryDebugInfo.getDebugEntries()) {
            long entryToken = protoStream.start(2246267895809L);
            protoStream.write(1138166333441L, debugEntry.getKey());
            protoStream.write(1138166333442L, debugEntry.getStringValue());
            protoStream.end(entryToken);
        }
    }

    private static void addDistroVersionDebugInfo(String distroVersionFileName, String debugKeyPrefix, DebugInfo debugInfo) {
        File file = new File(distroVersionFileName);
        String statusKey = debugKeyPrefix + "status";
        if (file.exists()) {
            try {
                DistroVersion distroVersion = DistroVersion.fromBytes(FileUtils.readBytes(file, DistroVersion.DISTRO_VERSION_FILE_LENGTH));
                DebugInfo addStringEntry = debugInfo.addStringEntry(statusKey, "OK");
                addStringEntry.addStringEntry(debugKeyPrefix + "formatVersion", distroVersion.formatMajorVersion + "." + distroVersion.formatMinorVersion).addStringEntry(debugKeyPrefix + "rulesVersion", distroVersion.rulesVersion).addStringEntry(debugKeyPrefix + "revision", distroVersion.revision);
            } catch (DistroException | IOException e) {
                debugInfo.addStringEntry(statusKey, "ERROR");
                debugInfo.addStringEntry(debugKeyPrefix + "exception_class", e.getClass().getName());
                debugInfo.addStringEntry(debugKeyPrefix + "exception_msg", e.getMessage());
                logMessage("Error reading " + file, e);
            }
        } else {
            debugInfo.addStringEntry(statusKey, "NOT_FOUND");
        }
    }

    private static void logMessage(String msg, Throwable t) {
        Slog.v(TAG, msg, t);
    }
}
