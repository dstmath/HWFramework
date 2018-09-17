package com.android.server.util;

import android.content.Context;

public class AbsUserBehaviourRecord {
    private static AbsUserBehaviourRecord record;

    public AbsUserBehaviourRecord(Context context) {
    }

    public static AbsUserBehaviourRecord getInstance(Context context) {
        return null;
    }

    public void appEnterRecord(String packageName) {
    }

    public void appExitRecord(String packageName, String backreson) {
    }
}
