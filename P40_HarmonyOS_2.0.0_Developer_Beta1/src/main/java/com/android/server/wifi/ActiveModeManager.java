package com.android.server.wifi;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ActiveModeManager {
    public static final int SCAN_NONE = 0;
    public static final int SCAN_WITHOUT_HIDDEN_NETWORKS = 1;
    public static final int SCAN_WITH_HIDDEN_NETWORKS = 2;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScanMode {
    }

    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    int getScanMode();

    void start();

    void stop();
}
