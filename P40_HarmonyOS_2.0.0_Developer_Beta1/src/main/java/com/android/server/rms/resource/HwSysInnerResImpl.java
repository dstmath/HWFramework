package com.android.server.rms.resource;

import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.rms.HwSysResource;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwSysInnerResImpl implements HwSysResource {
    private static final String TAG = "RMS.HwSysInnerResImpl";

    public static HwSysResource getResource(int resourceType) {
        return null;
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        return 1;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        return 1;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        return 1;
    }

    public int queryPkgPolicy(int type, int value, String key) {
        return 0;
    }

    public void release(int callingUid, String pkg, int processTpye) {
    }

    public void clear(int callingUid, String pkg, int processTpye) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
    }

    public Bundle query() {
        return null;
    }
}
