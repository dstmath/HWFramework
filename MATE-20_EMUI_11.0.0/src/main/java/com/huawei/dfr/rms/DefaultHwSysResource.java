package com.huawei.dfr.rms;

import android.os.Bundle;
import android.rms.DefaultDFRHwSysResource;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DefaultHwSysResource extends DefaultDFRHwSysResource {
    private static final String TAG = "DefaultHwSysResource";

    public static DefaultHwSysResource getDefault() {
        return new DefaultHwSysResource();
    }

    public int acquire(int i, String s, int i1) {
        return 0;
    }

    public int acquire(int i, String s, int i1, int i2) {
        return 0;
    }

    public int queryPkgPolicy(int i, int i1, String s) {
        return 0;
    }

    public void release(int i, String s, int i1) {
    }

    public void clear(int i, String s, int i1) {
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter) {
    }

    public Bundle query() {
        return null;
    }

    public void init(String[] args) {
    }
}
