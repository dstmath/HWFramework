package android.rms;

import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DefaultDFRHwSysResource implements HwSysResource {
    @Override // android.rms.HwSysResource
    public int acquire(int i, String s, int i1) {
        return 0;
    }

    @Override // android.rms.HwSysResource
    public int acquire(int i, String s, int i1, int i2) {
        return 0;
    }

    @Override // android.rms.HwSysResource
    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        return 0;
    }

    @Override // android.rms.HwSysResource
    public int queryPkgPolicy(int i, int i1, String s) {
        return 0;
    }

    @Override // android.rms.HwSysResource
    public void release(int i, String s, int i1) {
    }

    @Override // android.rms.HwSysResource
    public void clear(int i, String s, int i1) {
    }

    @Override // android.rms.HwSysResource
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter) {
    }

    @Override // android.rms.HwSysResource
    public Bundle query() {
        return null;
    }
}
