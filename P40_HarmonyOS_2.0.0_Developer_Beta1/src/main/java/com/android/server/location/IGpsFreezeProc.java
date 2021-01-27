package com.android.server.location;

import android.util.ArraySet;
import java.io.PrintWriter;

public interface IGpsFreezeProc {
    public static final int WHITE_LIST_TYPE_GPS = 1;
    public static final int WHITE_LIST_TYPE_GPS_TO_NETWORK = 5;
    public static final int WHITE_LIST_TYPE_QUICKGPS_DISABLE = 4;
    public static final int WHITE_LIST_TYPE_QUICKGPS_WHITE = 3;
    public static final int WHITE_LIST_TYPE_WIFISCAN = 2;

    void dump(PrintWriter printWriter);

    ArraySet<String> getPackageWhiteList(int i);

    boolean isFreeze(String str);

    void registerFreezeListener(GpsFreezeListener gpsFreezeListener);
}
