package com.android.server.timezone;

import java.io.PrintWriter;

public interface PermissionHelper {
    boolean checkDumpPermission(String str, PrintWriter printWriter);

    void enforceCallerHasPermission(String str) throws SecurityException;
}
