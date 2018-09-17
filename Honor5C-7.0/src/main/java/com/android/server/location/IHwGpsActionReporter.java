package com.android.server.location;

public interface IHwGpsActionReporter {
    public static final int ACTION_LOC_REMOVE = 0;
    public static final int ACTION_LOC_REQUEST = 1;

    boolean uploadLocationAction(int i, String str);
}
